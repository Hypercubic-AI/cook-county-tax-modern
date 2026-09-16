package org.cookcounty.tax.application.service;

import org.cookcounty.tax.application.service.PropertyTaxExemptionsKernel.EligibilityDecision;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsKernel.EligibilityWarning;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsKernel.HomeownerVariant;
import org.cookcounty.tax.domain.model.AssessmentDetail;
import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.cookcounty.tax.domain.model.HomeownerExemption;
import org.cookcounty.tax.domain.model.HomeownerMaster;
import org.cookcounty.tax.domain.model.PropertyTaxRenewal;
import org.cookcounty.tax.domain.port.out.AssessmentDetailRepository;
import org.cookcounty.tax.domain.port.out.AssessmentParcelRepository;
import org.cookcounty.tax.domain.port.out.HomeownerExemptionRepository;
import org.cookcounty.tax.domain.port.out.HomeownerMasterRepository;
import org.cookcounty.tax.domain.port.out.PropertyTaxRenewalRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/// Applies renewal matching, homeowner eligibility, and annual exemption publication.
///
/// Renewal rows and homeowners retain repository order for merge behavior. One transaction owns
/// homeowner replacements and the complete annual exemption-generation replacement. An unexpected
/// failure rolls back those writes.
@Component
public class PropertyTaxExemptionsProcessor {

    /// Prefix for the runtime-compatible comparison header.
    private static final String RENEWAL_MERGE_DISPLAY_PREFIX =
            "PROGRAM ASREA841 DATE AND TIME OF RUN =  ";

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100.0");

    private final HomeownerMasterRepository homeownerRepository;
    private final HomeownerExemptionRepository exemptionRepository;
    private final AssessmentParcelRepository parcelRepository;
    private final AssessmentDetailRepository detailRepository;
    private final PropertyTaxRenewalRepository renewalRepository;
    private final PropertyTaxExemptionsKernel kernel;

    /// Creates the processor over maintained renewal, homeowner, and assessment repositories.
    public PropertyTaxExemptionsProcessor(
            HomeownerMasterRepository homeownerRepository,
            HomeownerExemptionRepository exemptionRepository,
            AssessmentParcelRepository parcelRepository,
            AssessmentDetailRepository detailRepository,
            PropertyTaxRenewalRepository renewalRepository,
            PropertyTaxExemptionsKernel kernel) {
        this.homeownerRepository = homeownerRepository;
        this.exemptionRepository = exemptionRepository;
        this.parcelRepository = parcelRepository;
        this.detailRepository = detailRepository;
        this.renewalRepository = renewalRepository;
        this.kernel = kernel;
    }

    /// Executes one selected homeowner variant for the supplied business date and time.
    ///
    /// One transaction owns homeowner replacements and the annual exemption replacement.
    ///
    /// @param businessDate date that supplies the two-digit application year
    /// @param businessTime source-format run time retained in batch evidence
    /// @param variant homeowner eligibility policy for this run
    /// @return immutable counts, publications, dispositions, and comparator evidence
    @Transactional
    public ProcessResult process(
            LocalDate businessDate, String businessTime, HomeownerVariant variant) {
        List<Rejection> rejections = new ArrayList<>();
        List<HomeownerMaster> homeowners =
                deduplicateHomeowners(homeownerRepository.findAllInPropertyOrder(), rejections);
        RenewalMerge renewalMerge = mergeRenewals(homeowners, rejections);
        List<RenewalPrintRecord> renewalPrintRecords = renewalMerge.printRecords();
        int renewalUpdates = renewalPrintRecords.size();
        List<RenewalErrorRecord> renewalErrorRecords = renewalMerge.errorRecords();
        List<Reconciliation> reconciliations = new ArrayList<>();

        reconciliations.add(
                new Reconciliation(
                        "RENEWAL MERGE",
                        renewalErrorRecords.isEmpty() ? "RECONCILED" : "RECONCILED_WITH_REJECTIONS",
                        homeowners.size() + renewalMerge.recordsRead(),
                        renewalUpdates,
                        homeowners.size(),
                        renewalErrorRecords.size(),
                        List.of("asrea841-001", "asrea841-002", "asrea841-003", "asrea841-004"),
                        "The ordered merge read "
                                + renewalMerge.recordsRead()
                                + " renewal records, updated "
                                + renewalUpdates
                                + " homeowners, and rejected "
                                + renewalErrorRecords.size()
                                + " renewal records."));
        reconciliations.add(
                new Reconciliation(
                        "OWNER CONTACT REFRESH",
                        "RECONCILED",
                        homeowners.size(),
                        0,
                        homeowners.size(),
                        0,
                        List.of("asrea847-001", "asrea847-002", "asrea847-003", "asrea847-004"),
                        "No Property Master owner detail is published by this app; existing contact"
                                + " fields were preserved."));

        List<AssessmentParcel> parcels =
                orderedRealEstateParcels(parcelRepository.findAllInInputOrder(), rejections);
        Map<ParcelKey, List<AssessmentDetail>> details =
                groupDetails(detailRepository.findAllInInputOrder());
        Map<String, AssessmentParcel> parcelByProperty = new LinkedHashMap<>();
        parcels.forEach(parcel -> parcelByProperty.put(parcel.parcelNumber(), parcel));
        Map<String, HomeownerMaster> homeownerByProperty = new LinkedHashMap<>();
        homeowners.forEach(
                homeowner ->
                        homeownerByProperty.putIfAbsent(homeowner.propertyNumber(), homeowner));

        List<HomeownerMaster> eligibleHomeowners = new ArrayList<>();
        Map<ParcelKey, EligibilityDecision> eligibilityByParcel = new LinkedHashMap<>();
        int eligibilityUpdates = 0;
        List<EligibilityPrintRecord> ineligibleRecords = new ArrayList<>();
        String eligibilityRule =
                variant == HomeownerVariant.ENUMERATED ? "asrea852-002" : "asrea853-002";
        for (AssessmentParcel parcel : parcels) {
            ParcelKey parcelKey = new ParcelKey(parcel.volumeNumber(), parcel.parcelNumber());
            List<AssessmentDetail> parcelDetails = details.getOrDefault(parcelKey, List.of());
            EligibilityDecision decision = kernel.evaluate(parcel, parcelDetails, variant);
            eligibilityByParcel.put(parcelKey, decision);
            if (!decision.eligible()) {
                rejections.add(
                        new Rejection(
                                "HOMEOWNER ELIGIBILITY ASSESSMENT",
                                recordKey(parcel),
                                "BYPASSED",
                                "Parcel is non-residential under the selected homeowner eligibility"
                                        + " rules.",
                                eligibilityRule));
                ineligibleRecords.add(
                        new EligibilityPrintRecord(
                                parcel.volumeNumber(),
                                parcel.parcelNumber(),
                                parcel.taxCode(),
                                parcel.overallClass()));
                continue;
            }

            HomeownerMaster homeowner = homeownerByProperty.get(parcel.parcelNumber());
            homeowner =
                    homeowner == null
                            ? newHomeowner(parcel, decision, businessDate)
                            : refreshEligibility(homeowner, parcel, decision, businessDate);
            homeowner = homeownerRepository.save(homeowner);
            eligibleHomeowners.add(homeowner);
            eligibilityUpdates++;
            for (EligibilityWarning warning : decision.warnings()) {
                rejections.add(
                        new Rejection(
                                "HOMEOWNER ELIGIBILITY ASSESSMENT",
                                recordKey(parcel),
                                "WARNING",
                                warning.message(),
                                warning.ruleId()));
            }
        }

        reconciliations.add(
                new Reconciliation(
                        "HOMEOWNER ELIGIBILITY GENERATION",
                        "RECONCILED",
                        parcels.size() + homeowners.size(),
                        eligibleHomeowners.size(),
                        eligibleHomeowners.size(),
                        0,
                        variantRuleIds(variant),
                        "The eligibility pass examined "
                                + parcels.size()
                                + " assessment parcels and produced "
                                + eligibleHomeowners.size()
                                + " homeowner records."));

        List<HomeownerExemption> exemptions = new ArrayList<>(eligibleHomeowners.size());
        for (HomeownerMaster homeowner : eligibleHomeowners) {
            AssessmentParcel parcel = parcelByProperty.get(homeowner.propertyNumber());
            if (parcel == null) {
                throw new IllegalStateException(
                        "Eligible homeowner has no current assessment parcel");
            }
            EligibilityDecision decision =
                    Objects.requireNonNull(
                            eligibilityByParcel.get(
                                    new ParcelKey(parcel.volumeNumber(), parcel.parcelNumber())),
                            "eligible parcel decision");
            exemptions.add(toExemption(homeowner, parcel, decision));
        }
        replaceExemptionGeneration(exemptions);

        reconciliations.add(
                new Reconciliation(
                        "SENIOR-FREEZE FILE GENERATION",
                        "RECONCILED",
                        parcels.size() + eligibleHomeowners.size(),
                        eligibleHomeowners.size(),
                        exemptions.size(),
                        0,
                        List.of(
                                "asrea859-001",
                                "asrea859-002",
                                "asrea859-003",
                                "asrea859-004",
                                "asrea859-005"),
                        exemptions.isEmpty()
                                ? "The annual exemption output was published as an explicit empty"
                                        + " generation."
                                : "The annual exemption generation was published."));

        BatchEvidence evidence =
                new BatchEvidence(
                        List.copyOf(renewalPrintRecords),
                        List.copyOf(renewalErrorRecords),
                        List.copyOf(ineligibleRecords),
                        exemptions.stream().map(AnnualExemptionRecord::from).toList(),
                        homeowners.size(),
                        parcels.size(),
                        eligibleHomeowners.size(),
                        List.of(comparisonHeader(businessDate, businessTime)));
        List<OutputRecord> outputs = publications(variant, evidence);
        List<RuleDisposition> ruleOutcomes =
                ruleOutcomes(variant, evidence, eligibilityUpdates, rejections);
        List<Message> messages =
                List.of(
                        new Message(
                                "INFO",
                                "The selected homeowner eligibility generation path completed with"
                                        + " return code 0.",
                                variant == HomeownerVariant.ENUMERATED
                                        ? "asrea852-001"
                                        : "asrea853-001"),
                        new Message(
                                "INFO",
                                "The rejected direct-update senior-freeze base-value calculation"
                                    + " and unreviewed senior-freeze paths remain source-only and"
                                    + " were not executed.",
                                "ashma850-001"));

        int rejectedCount =
                (int)
                        rejections.stream()
                                .filter(rejection -> "REJECTED".equals(rejection.outcome()))
                                .count();
        int recordsRead =
                renewalMerge.recordsRead()
                        + (homeowners.size() * 3)
                        + (parcels.size() * 2)
                        + eligibleHomeowners.size();
        int recordsWritten =
                (homeowners.size() * 2) + eligibleHomeowners.size() + exemptions.size();
        return new ProcessResult(
                0,
                recordsRead,
                recordsWritten,
                renewalUpdates + eligibilityUpdates,
                rejectedCount,
                List.copyOf(outputs),
                messages,
                List.copyOf(rejections),
                List.copyOf(reconciliations),
                List.copyOf(ruleOutcomes),
                evidence);
    }

    /// Merges source-ordered renewals with maintained homeowners.
    ///
    /// A renewal updates the first homeowner with the same property. A later renewal for an already
    /// consumed property is below the merge position. A property that does not exist is unmatched.
    private RenewalMerge mergeRenewals(
            List<HomeownerMaster> homeowners, List<Rejection> rejections) {
        Map<String, HomeownerMaster> homeownersByProperty = new LinkedHashMap<>();
        homeowners.forEach(
                homeowner ->
                        homeownersByProperty.putIfAbsent(homeowner.propertyNumber(), homeowner));
        var consumedProperties = new java.util.HashSet<String>();
        List<RenewalPrintRecord> printRecords = new ArrayList<>();
        List<RenewalErrorRecord> errorRecords = new ArrayList<>();
        List<PropertyTaxRenewal> renewals = renewalRepository.findAllInSourceOrder();
        for (PropertyTaxRenewal renewal : renewals) {
            HomeownerMaster homeowner = homeownersByProperty.get(renewal.propertyNumber());
            if (homeowner != null && consumedProperties.add(renewal.propertyNumber())) {
                HomeownerMaster renewed = homeownerRepository.save(homeowner.withResponseStatus(2));
                printRecords.add(RenewalPrintRecord.from(renewed, renewal.batchNumber()));
                continue;
            }

            errorRecords.add(
                    new RenewalErrorRecord(renewal.propertyNumber(), renewal.batchNumber()));
            boolean belowMergePosition = homeowner != null;
            rejections.add(
                    new Rejection(
                            "RENEWAL INPUT",
                            new BigInteger(renewal.propertyNumber()).toString()
                                    + "/"
                                    + renewal.batchNumber(),
                            "REJECTED",
                            belowMergePosition
                                    ? "Renewal key is below the current merge position."
                                    : "Renewal has no matching homeowner and cannot create one.",
                            belowMergePosition ? "asrea841-004" : "asrea841-003"));
        }
        return new RenewalMerge(
                List.copyOf(printRecords), List.copyOf(errorRecords), renewals.size());
    }

    private static List<HomeownerMaster> deduplicateHomeowners(
            List<HomeownerMaster> source, List<Rejection> rejections) {
        Map<String, HomeownerMaster> unique = new LinkedHashMap<>();
        for (HomeownerMaster homeowner : source) {
            if (homeowner.propertyNumber() == null
                    || new BigInteger(homeowner.propertyNumber()).signum() <= 0) {
                rejections.add(
                        new Rejection(
                                "HOMEOWNER MASTER",
                                null,
                                "REJECTED",
                                "Homeowner property number must be positive.",
                                "asrea841-001"));
                continue;
            }
            if (unique.putIfAbsent(homeowner.propertyNumber(), homeowner) != null) {
                rejections.add(
                        new Rejection(
                                "HOMEOWNER MASTER",
                                homeowner.propertyNumber(),
                                "REJECTED",
                                "Duplicate homeowner property was rejected by the explicit"
                                        + " first-record policy.",
                                "asrea841-004"));
            }
        }
        return List.copyOf(unique.values());
    }

    private static List<AssessmentParcel> orderedRealEstateParcels(
            List<AssessmentParcel> source, List<Rejection> rejections) {
        Map<ParcelKey, AssessmentParcel> unique = new LinkedHashMap<>();
        source.stream()
                .sorted(
                        Comparator.comparing(
                                        AssessmentParcel::volumeNumber,
                                        Comparator.comparing(
                                                PropertyTaxExemptionsProcessor::numericIdentifier))
                                .thenComparing(
                                        AssessmentParcel::parcelNumber,
                                        Comparator.comparing(
                                                PropertyTaxExemptionsProcessor::numericIdentifier)))
                .forEach(
                        parcel -> {
                            String volume = parcel.volumeNumber();
                            if (volume == null
                                    || new BigInteger(volume).compareTo(BigInteger.ONE) < 0
                                    || new BigInteger(volume).compareTo(BigInteger.valueOf(601))
                                            > 0) {
                                rejections.add(
                                        new Rejection(
                                                "ASSESSMENT MASTER",
                                                recordKey(parcel),
                                                "REJECTED",
                                                "Only real-estate volumes 1 through 601 are"
                                                        + " processed.",
                                                "asrea859-005"));
                                return;
                            }
                            if (parcel.parcelNumber() == null
                                    || new BigInteger(parcel.parcelNumber()).signum() <= 0) {
                                rejections.add(
                                        new Rejection(
                                                "ASSESSMENT MASTER",
                                                recordKey(parcel),
                                                "REJECTED",
                                                "Assessment parcel number must be positive.",
                                                "asrea859-005"));
                                return;
                            }
                            ParcelKey key = new ParcelKey(volume, parcel.parcelNumber());
                            if (unique.putIfAbsent(key, parcel) != null) {
                                rejections.add(
                                        new Rejection(
                                                "ASSESSMENT MASTER",
                                                recordKey(parcel),
                                                "REJECTED",
                                                "Duplicate assessment parcel was rejected by the"
                                                        + " explicit first-record policy.",
                                                "asrea859-005"));
                            }
                        });
        return List.copyOf(unique.values());
    }

    private static Map<ParcelKey, List<AssessmentDetail>> groupDetails(
            List<AssessmentDetail> source) {
        Map<ParcelKey, List<AssessmentDetail>> result = new LinkedHashMap<>();
        for (AssessmentDetail detail : source) {
            result.computeIfAbsent(
                            new ParcelKey(detail.parcelVolumeNumber(), detail.parcelNumber()),
                            unused -> new ArrayList<>())
                    .add(detail);
        }
        result.replaceAll(
                (unused, details) ->
                        details.stream()
                                .sorted(Comparator.comparing(AssessmentDetail::occurrenceNumber))
                                .toList());
        return result;
    }

    private static HomeownerMaster newHomeowner(
            AssessmentParcel parcel, EligibilityDecision decision, LocalDate businessDate) {
        return new HomeownerMaster(
                null,
                null,
                businessDate.getYear() % 100,
                decision.assessedValue(),
                decision.assessmentClass(),
                null,
                null,
                null,
                BigDecimal.ONE,
                null,
                null,
                null,
                null,
                null,
                "AB",
                decision.occupancyFactor(),
                null,
                parcel.parcelNumber(),
                decision.proration(),
                0,
                0,
                null,
                parcel.taxCode(),
                parseInteger(parcel.taxType()),
                null,
                0,
                parcel.volumeNumber(),
                null);
    }

    /// Refreshes annual eligibility fields while preserving maintained base-year state.
    ///
    /// A new application year resets response states. It changes the base-year establishment code
    /// to `AB` unless the existing source-supported `TR` code must remain.
    ///
    /// @return a replacement homeowner snapshot. This method does not persist it.
    private static HomeownerMaster refreshEligibility(
            HomeownerMaster homeowner,
            AssessmentParcel parcel,
            EligibilityDecision decision,
            LocalDate businessDate) {
        int applicationYear = businessDate.getYear() % 100;
        boolean newYear = applicationYear != homeowner.applicationYear();
        int responseStatus = newYear ? 0 : homeowner.responseStatus();
        int secondaryStatus = newYear ? 0 : homeowner.secondaryResponseStatus();
        int tertiaryStatus = newYear ? 0 : homeowner.tertiaryStatus();
        String baseYearEstablishmentCode =
                newYear && !"TR".equals(homeowner.baseYearEstablishmentCode())
                        ? "AB"
                        : homeowner.baseYearEstablishmentCode();
        if (ONE_HUNDRED.compareTo(decision.secondaryOccupancyFactor()) == 0
                && responseStatus == 2) {
            responseStatus = 0;
        }
        return homeowner.withEligibility(
                applicationYear,
                decision.assessedValue(),
                decision.assessmentClass(),
                decision.occupancyFactor(),
                decision.proration(),
                responseStatus,
                secondaryStatus,
                tertiaryStatus,
                baseYearEstablishmentCode,
                parcel.taxCode(),
                parcel.volumeNumber());
    }

    private static HomeownerExemption toExemption(
            HomeownerMaster homeowner, AssessmentParcel parcel, EligibilityDecision decision) {
        BigDecimal equalizedValue =
                homeowner.equalizedValue() == null
                        ? homeowner.assessedValue()
                        : homeowner.equalizedValue();
        return new HomeownerExemption(
                null,
                null,
                homeowner.applicationYear(),
                homeowner.assessedValue(),
                homeowner.assessmentClass(),
                homeowner.certificateOfErrorNumber(),
                homeowner.city(),
                parcel.overallClass(),
                homeowner.cooperativeQuantity(),
                1,
                homeowner.equalizationFactor(),
                equalizedValue,
                homeowner.exemptionType(),
                decision.keyParcelNumber(),
                homeowner.mailingAddress(),
                homeowner.occupancyFactor(),
                homeowner.ownerName(),
                homeowner.propertyNumber(),
                homeowner.proration(),
                0,
                homeowner.responseStatus(),
                homeowner.secondaryResponseStatus(),
                parseInteger(decision.splitCode()),
                homeowner.state(),
                homeowner.taxCode(),
                homeowner.taxType(),
                homeowner.tertiaryStatus(),
                homeowner.volumeNumber(),
                homeowner.zipCode());
    }

    private void replaceExemptionGeneration(List<HomeownerExemption> exemptions) {
        exemptionRepository
                .findAllInPersistenceOrder()
                .forEach(
                        existing ->
                                exemptionRepository.deleteById(
                                        Objects.requireNonNull(
                                                existing.id(), "persisted exemption id")));
        exemptions.forEach(exemptionRepository::save);
    }

    /// Describes publications from the same record counts used by the fixed-format projection.
    ///
    /// Renewal reports append six total lines. Eligibility and final-generation reports append
    /// three total lines. Empty data generations remain distinct from these nonempty reports.
    private static List<OutputRecord> publications(
            HomeownerVariant variant, BatchEvidence evidence) {
        String eligibilityGenerationProgram =
                variant == HomeownerVariant.ENUMERATED ? "ASREA852" : "ASREA853";
        int eligibilityRecords = evidence.eligibleRecords();
        int exemptionRecords = evidence.annualExemptionRecords().size();
        return List.of(
                new OutputRecord(
                        "ASREA841 HOMEOUT",
                        "STAGING",
                        evidence.homeownerRecords(),
                        evidence.homeownerRecords() == 0 ? "EMPTY" : "PUBLISHED",
                        List.of("asrea841-002")),
                new OutputRecord(
                        "ASREA841 PRINTOUT",
                        "REPORT",
                        evidence.renewalPrintRecords().size() + 6,
                        "PUBLISHED",
                        List.of("asrea841-002")),
                new OutputRecord(
                        "ASREA841 ERRPRINT",
                        "REPORT",
                        evidence.renewalErrorRecords().size() + 6,
                        "PUBLISHED",
                        List.of("asrea841-001", "asrea841-003", "asrea841-004")),
                new OutputRecord(
                        eligibilityGenerationProgram + " HOMEOUT",
                        "STAGING",
                        eligibilityRecords,
                        eligibilityRecords == 0 ? "EMPTY" : "PUBLISHED",
                        List.of(
                                variant == HomeownerVariant.ENUMERATED
                                        ? "asrea852-005"
                                        : "asrea853-004")),
                new OutputRecord(
                        eligibilityGenerationProgram + " PRINTOUT",
                        "REPORT",
                        evidence.ineligibleRecords().size() + 3,
                        "PUBLISHED",
                        variantRuleIds(variant)),
                new OutputRecord(
                        "ASREA859 HOMEOUT",
                        "DATASET",
                        exemptionRecords,
                        exemptionRecords == 0 ? "EMPTY" : "PUBLISHED",
                        List.of("asrea859-001", "asrea859-004")),
                new OutputRecord(
                        "ASREA859 PRINTOUT", "REPORT", 3, "PUBLISHED", List.of("asrea859-001")));
    }

    private static List<String> variantRuleIds(HomeownerVariant variant) {
        String prefix = variant == HomeownerVariant.ENUMERATED ? "asrea852-" : "asrea853-";
        return numberedRuleIds(prefix, 6);
    }

    /// Counts the records examined, updated, or rejected by each applicable processing rule.
    private static List<RuleDisposition> ruleOutcomes(
            HomeownerVariant variant,
            BatchEvidence evidence,
            int eligibilityUpdates,
            List<Rejection> rejections) {
        int exemptionRecords = evidence.annualExemptionRecords().size();
        List<RuleDisposition> outcomes = new ArrayList<>();
        outcomes.add(
                new RuleDisposition(
                        "asrea841-001",
                        "APPLIED",
                        evidence.renewalPrintRecords().size()
                                + evidence.renewalErrorRecords().size(),
                        null));
        outcomes.add(
                new RuleDisposition(
                        "asrea841-002", "APPLIED", evidence.renewalPrintRecords().size(), null));
        outcomes.add(
                new RuleDisposition(
                        "asrea841-003",
                        "APPLIED",
                        rejectedByRule(rejections, "asrea841-003"),
                        null));
        outcomes.add(
                new RuleDisposition(
                        "asrea841-004",
                        "APPLIED",
                        rejectedByRule(rejections, "asrea841-004"),
                        null));
        outcomes.add(
                new RuleDisposition(
                        "asrea847-001",
                        "NOT_APPLICABLE",
                        0,
                        "No Property Master dataset is published by this app."));
        outcomes.add(
                new RuleDisposition(
                        "asrea847-002",
                        "NOT_APPLICABLE",
                        0,
                        "No Property Master owner segments are published by this app."));
        outcomes.add(
                new RuleDisposition(
                        "asrea847-003",
                        "APPLIED",
                        evidence.homeownerRecords(),
                        "Existing homeowner contact information was preserved."));
        outcomes.add(
                new RuleDisposition("asrea847-004", "APPLIED", evidence.homeownerRecords(), null));

        addVariantOutcomes(outcomes, HomeownerVariant.ENUMERATED, variant, eligibilityUpdates);
        addVariantOutcomes(outcomes, HomeownerVariant.BROAD, variant, eligibilityUpdates);
        for (String id : numberedRuleIds("asrea859-", 5)) {
            outcomes.add(new RuleDisposition(id, "APPLIED", exemptionRecords, null));
        }

        addSourceOnly(outcomes, "ashma825-", 4, "Unreviewed Senior Freeze path");
        addSourceOnly(outcomes, "ashma827-", 5, "Unreviewed Senior Freeze path");
        addSourceOnly(outcomes, "ashma840-", 4, "Unreviewed Senior Freeze path");
        addSourceOnly(outcomes, "ashma845-", 4, "Unreviewed Senior Freeze path");
        addSourceOnly(outcomes, "ashma850-", 6, "Rejected direct-update Senior Freeze path");
        addSourceOnly(outcomes, "ashma857-", 6, "Unreviewed Senior Freeze path");
        addSourceOnly(outcomes, "ashma859-", 7, "Unreviewed Senior Freeze path");
        addSourceOnly(outcomes, "asrea863-", 6, "Source-only manual maintenance path");
        addSourceOnly(outcomes, "asrea864-", 7, "Source-only manual maintenance path");
        addSourceOnly(outcomes, "asrea872-", 4, "Source-only reporting path");
        addSourceOnly(outcomes, "asrea874-", 6, "Source-only disabled/veteran path");
        return outcomes;
    }

    /// Counts actual rejected records without assigning scenario-specific rule totals.
    private static long rejectedByRule(List<Rejection> rejections, String ruleId) {
        return rejections.stream()
                .filter(rejection -> "REJECTED".equals(rejection.outcome()))
                .filter(rejection -> ruleId.equals(rejection.ruleId()))
                .count();
    }

    private static void addVariantOutcomes(
            List<RuleDisposition> outcomes,
            HomeownerVariant ruleVariant,
            HomeownerVariant selectedVariant,
            int recordsAffected) {
        boolean selected = ruleVariant == selectedVariant;
        for (String id : variantRuleIds(ruleVariant)) {
            outcomes.add(
                    new RuleDisposition(
                            id,
                            selected ? "APPLIED" : "NOT_APPLICABLE",
                            selected ? recordsAffected : 0,
                            selected
                                    ? null
                                    : "The alternative homeowner variant was not selected."));
        }
    }

    private static void addSourceOnly(
            List<RuleDisposition> outcomes, String prefix, int count, String path) {
        for (String id : numberedRuleIds(prefix, count)) {
            outcomes.add(
                    new RuleDisposition(
                            id,
                            "NOT_APPLICABLE",
                            0,
                            path + " is attached for provenance only and was not executed."));
        }
    }

    private static List<String> numberedRuleIds(String prefix, int count) {
        List<String> ids = new ArrayList<>(count);
        for (int index = 1; index <= count; index++) {
            ids.add(prefix + String.format("%03d", index));
        }
        return List.copyOf(ids);
    }

    /// Converts a canonical identifier only at a numeric comparison boundary.
    private static BigInteger numericIdentifier(String value) {
        return new BigInteger(value);
    }

    /// Formats the observed interpreter header for comparison output.
    ///
    /// The interpreter truncates `YYYYMMDD` to `YYYYMM`, then emits `YY/MM/CC`. This differs from
    /// the COBOL source's `MM/DD/YY` edit. It also emits the `HHMM` value in a five-digit numeric
    /// field.
    private static String comparisonHeader(LocalDate businessDate, String businessTime) {
        LocalTime time = LocalTime.parse(businessTime);
        return RENEWAL_MERGE_DISPLAY_PREFIX
                + String.format(
                        "%02d/%02d/%02d   0%02d%02d",
                        businessDate.getYear() % 100,
                        businessDate.getMonthValue(),
                        businessDate.getYear() / 100,
                        time.getHour(),
                        time.getMinute());
    }

    private static @Nullable Integer parseInteger(@Nullable String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static String recordKey(AssessmentParcel parcel) {
        return parcel.volumeNumber() + "/" + parcel.parcelNumber();
    }

    /// Immutable business and comparator result for one transactional processing run.
    ///
    /// @param returnCode zero on business completion
    /// @param recordsRead maintained homeowner rows examined
    /// @param recordsWritten annual exemption rows published
    /// @param recordsUpdated homeowner renewal rows replaced
    /// @param recordsRejected rows rejected by a documented business rule
    /// @param outputs logical output publications
    /// @param messages run-level messages
    /// @param rejections rejected row details
    /// @param reconciliations source-to-output count checks
    /// @param ruleOutcomes per-rule dispositions
    /// @param batchEvidence immutable fixed-layout projection input
    public record ProcessResult(
            int returnCode,
            int recordsRead,
            int recordsWritten,
            int recordsUpdated,
            int recordsRejected,
            List<OutputRecord> outputs,
            List<Message> messages,
            List<Rejection> rejections,
            List<Reconciliation> reconciliations,
            List<RuleDisposition> ruleOutcomes,
            BatchEvidence batchEvidence) {

        /// Copies outcome lists and requires projection evidence for the completed business result.
        public ProcessResult {
            outputs = List.copyOf(outputs);
            messages = List.copyOf(messages);
            rejections = List.copyOf(rejections);
            reconciliations = List.copyOf(reconciliations);
            ruleOutcomes = List.copyOf(ruleOutcomes);
            Objects.requireNonNull(batchEvidence, "batchEvidence");
        }
    }

    /// Describes one logical output publication.
    ///
    /// @param name stable output name
    /// @param kind output category
    /// @param recordCount number of logical records
    /// @param publicationStatus publication disposition
    /// @param ruleIds rules represented by the output
    public record OutputRecord(
            String name,
            String kind,
            long recordCount,
            String publicationStatus,
            List<String> ruleIds) {
        /// Copies the governing rule identities without retaining a caller-owned mutable list.
        public OutputRecord {
            ruleIds = List.copyOf(ruleIds);
        }
    }

    /// Reports a run diagnostic with its severity and optional governing rule.
    ///
    /// @param severity consumer-visible severity
    /// @param message explanatory run message
    /// @param ruleId governing rule when the message is rule-specific
    public record Message(String severity, String message, @Nullable String ruleId) {}

    /// Describes one rejected input without changing the immutable source snapshot.
    ///
    /// @param source input category
    /// @param recordKey stable row key when one was available
    /// @param outcome rejection disposition
    /// @param message reason for rejection
    /// @param ruleId governing rule
    public record Rejection(
            String source,
            @Nullable String recordKey,
            String outcome,
            String message,
            String ruleId) {}

    /// Reports one source-to-output reconciliation.
    ///
    /// @param name stable reconciliation name
    /// @param status reconciliation disposition
    /// @param recordsRead source rows read
    /// @param recordsMatched source rows matched
    /// @param recordsWritten output rows written
    /// @param recordsRejected source rows rejected
    /// @param ruleIds rules covered by the reconciliation
    /// @param message count explanation
    public record Reconciliation(
            String name,
            String status,
            long recordsRead,
            long recordsMatched,
            long recordsWritten,
            long recordsRejected,
            List<String> ruleIds,
            String message) {
        /// Copies the governing rule identities independently of the caller's reconciliation list.
        public Reconciliation {
            ruleIds = List.copyOf(ruleIds);
        }
    }

    /// Records a rule's processing outcome and affected-record count.
    ///
    /// @param ruleId governing rule
    /// @param outcome resulting disposition
    /// @param recordsAffected count of affected records
    /// @param message optional explanatory detail
    public record RuleDisposition(
            String ruleId, String outcome, long recordsAffected, @Nullable String message) {}

    /// Immutable inputs for the fixed-layout comparator projection.
    ///
    /// @param renewalPrintRecords matched-renewal publication rows
    /// @param renewalErrorRecords unmatched-renewal publication rows
    /// @param ineligibleRecords homeowner ineligibility publication rows
    /// @param annualExemptionRecords annual exemption publication rows
    /// @param homeownerRecords maintained homeowner rows examined
    /// @param assessmentRecords parcel rows examined
    /// @param eligibleRecords parcels that produced exemptions
    /// @param batchDisplays ordered batch display lines
    public record BatchEvidence(
            List<RenewalPrintRecord> renewalPrintRecords,
            List<RenewalErrorRecord> renewalErrorRecords,
            List<EligibilityPrintRecord> ineligibleRecords,
            List<AnnualExemptionRecord> annualExemptionRecords,
            int homeownerRecords,
            int assessmentRecords,
            int eligibleRecords,
            List<String> batchDisplays) {

        /// Copies report inputs so later caller mutations cannot change the recorded batch
        /// evidence.
        public BatchEvidence {
            renewalPrintRecords = List.copyOf(renewalPrintRecords);
            renewalErrorRecords = List.copyOf(renewalErrorRecords);
            ineligibleRecords = List.copyOf(ineligibleRecords);
            annualExemptionRecords = List.copyOf(annualExemptionRecords);
            batchDisplays = List.copyOf(batchDisplays);
        }
    }

    /// Matched renewal report values captured before the homeowner replacement.
    ///
    /// @param volumeNumber assessment volume
    /// @param propertyNumber property identity
    /// @param taxCode tax district code
    /// @param assessmentClass property classification code
    /// @param applicationYear two-digit application year
    /// @param proration eligible share as a six-place decimal fraction
    /// @param cooperativeQuantity cooperative units
    /// @param assessedValue assessed valuation in whole dollars
    /// @param batchNumber five-character renewal batch identifier
    public record RenewalPrintRecord(
            String volumeNumber,
            String propertyNumber,
            String taxCode,
            int assessmentClass,
            int applicationYear,
            BigDecimal proration,
            int cooperativeQuantity,
            BigDecimal assessedValue,
            String batchNumber) {

        private static RenewalPrintRecord from(HomeownerMaster homeowner, String batchNumber) {
            return new RenewalPrintRecord(
                    value(homeowner.volumeNumber()),
                    value(homeowner.propertyNumber()),
                    value(homeowner.taxCode()),
                    value(homeowner.assessmentClass()),
                    value(homeowner.applicationYear()),
                    homeowner.proration() == null ? BigDecimal.ZERO : homeowner.proration(),
                    value(homeowner.cooperativeQuantity()),
                    value(homeowner.assessedValue()),
                    batchNumber);
        }
    }

    /// Identifies an unmatched renewal for the batch error output.
    ///
    /// @param propertyNumber unmatched property identity
    /// @param batchNumber five-character renewal batch identifier
    public record RenewalErrorRecord(String propertyNumber, String batchNumber) {}

    /// Identifies an ineligible property for the classification report.
    ///
    /// @param volumeNumber assessment volume
    /// @param propertyNumber property identity
    /// @param taxCode tax district code
    /// @param overallClass ineligible property classification code
    public record EligibilityPrintRecord(
            String volumeNumber, String propertyNumber, String taxCode, int overallClass) {}

    /// Annual exemption values published for downstream processing.
    ///
    /// @param volumeNumber assessment volume
    /// @param propertyNumber property identity
    /// @param taxCode tax district code
    /// @param taxType exemption tax-type code
    /// @param assessmentClass qualifying property classification code
    /// @param assessedValue assessed valuation in whole dollars
    /// @param proration eligible share as a six-place decimal fraction
    /// @param responseStatus homeowner response status
    public record AnnualExemptionRecord(
            String volumeNumber,
            String propertyNumber,
            String taxCode,
            int taxType,
            int assessmentClass,
            BigDecimal assessedValue,
            BigDecimal proration,
            int responseStatus) {

        private static AnnualExemptionRecord from(HomeownerExemption exemption) {
            return new AnnualExemptionRecord(
                    value(exemption.volumeNumber()),
                    value(exemption.propertyNumber()),
                    value(exemption.taxCode()),
                    value(exemption.taxType()),
                    value(exemption.assessmentClass()),
                    value(exemption.assessedValue()),
                    exemption.proration() == null ? BigDecimal.ZERO : exemption.proration(),
                    value(exemption.responseStatus()));
        }
    }

    private static int value(@Nullable Integer value) {
        return value == null ? 0 : value;
    }

    /// Complete result of one renewal and homeowner ordered merge.
    ///
    /// @param printRecords matched renewal rows
    /// @param errorRecords rejected renewal rows
    /// @param recordsRead renewal input rows examined
    private record RenewalMerge(
            List<RenewalPrintRecord> printRecords,
            List<RenewalErrorRecord> errorRecords,
            int recordsRead) {
        private RenewalMerge {
            printRecords = List.copyOf(printRecords);
            errorRecords = List.copyOf(errorRecords);
        }
    }

    private static String value(@Nullable String value) {
        return value == null ? "0" : value;
    }

    private static BigDecimal value(@Nullable BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private record ParcelKey(String volumeNumber, String parcelNumber) {}
}
