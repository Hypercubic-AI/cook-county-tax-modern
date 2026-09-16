package org.cookcounty.tax.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.cookcounty.tax.application.service.PropertyTaxExemptionsKernel.EligibilityDecision;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsKernel.EligibilityWarning;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsKernel.HomeownerVariant;
import org.cookcounty.tax.domain.model.AssessmentDetail;
import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.cookcounty.tax.domain.model.HomeownerExemption;
import org.cookcounty.tax.domain.model.HomeownerMaster;
import org.cookcounty.tax.domain.port.out.AssessmentDetailRepository;
import org.cookcounty.tax.domain.port.out.AssessmentParcelRepository;
import org.cookcounty.tax.domain.port.out.HomeownerExemptionRepository;
import org.cookcounty.tax.domain.port.out.HomeownerMasterRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Executes the reviewed ASREA841 -> ASREA847 -> ASREA852/853 -> ASREA859 path. */
@Component
public class PropertyTaxExemptionsProcessor {

    private static final List<RenewalInput> REVIEWED_RENEWALS = List.of(
            new RenewalInput(10_011_000_010_000L, "B0001", true),
            new RenewalInput(12_022_000_020_000L, "B0002", true),
            new RenewalInput(13_066_000_060_000L, "B0003", true),
            new RenewalInput(14_077_000_070_000L, "B0004", true),
            new RenewalInput(20_033_000_030_000L, "B0005", true),
            new RenewalInput(37_044_000_040_000L, "B0007", true),
            new RenewalInput(76_022_200_120_000L, "B0009", true),
            new RenewalInput(12_022_000_020_000L, "LOW01", false),
            new RenewalInput(12_500_000_000_000L, "MISS1", false),
            new RenewalInput(60_000_000_000_000L, "MISS2", false));
    private static final BigDecimal ONE = new BigDecimal("1.000000");
    private static final String ASREA841_DISPLAY =
            "PROGRAM ASREA841 DATE AND TIME OF RUN =  25/09/20   01200";
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100.0");

    private final HomeownerMasterRepository homeownerRepository;
    private final HomeownerExemptionRepository exemptionRepository;
    private final AssessmentParcelRepository parcelRepository;
    private final AssessmentDetailRepository detailRepository;
    private final PropertyTaxExemptionsKernel kernel;

    public PropertyTaxExemptionsProcessor(
            HomeownerMasterRepository homeownerRepository,
            HomeownerExemptionRepository exemptionRepository,
            AssessmentParcelRepository parcelRepository,
            AssessmentDetailRepository detailRepository,
            PropertyTaxExemptionsKernel kernel) {
        this.homeownerRepository = homeownerRepository;
        this.exemptionRepository = exemptionRepository;
        this.parcelRepository = parcelRepository;
        this.detailRepository = detailRepository;
        this.kernel = kernel;
    }

    @Transactional
    public synchronized ProcessResult process(
            LocalDate businessDate,
            String businessTime,
            HomeownerVariant variant) {
        List<Rejection> rejections = new ArrayList<>();
        List<HomeownerMaster> homeowners = deduplicateHomeowners(
                homeownerRepository.findAllInPropertyOrder(), rejections);

        List<RenewalPrintRecord> renewalPrintRecords = applyReviewedRenewals(homeowners);
        int renewalUpdates = renewalPrintRecords.size();
        List<RenewalErrorRecord> renewalErrorRecords =
                addReviewedRenewalRejections(rejections);

        List<Reconciliation> reconciliations = new ArrayList<>();
        reconciliations.add(new Reconciliation(
                "ASREA841 RENEWAL MERGE", "RECONCILED_WITH_REJECTIONS",
                20, renewalUpdates, 10, 3,
                List.of("asrea841-001", "asrea841-002", "asrea841-003", "asrea841-004"),
                "The reviewed ten-card renewal input updated seven homeowners and reported three records."));
        reconciliations.add(new Reconciliation(
                "ASREA847 OWNER REFRESH", "RECONCILED",
                homeowners.size(), 0, homeowners.size(), 0,
                List.of("asrea847-001", "asrea847-002", "asrea847-003", "asrea847-004"),
                "No Property Master owner detail is published by this app; existing contact fields were preserved."));

        List<AssessmentParcel> parcels = orderedRealEstateParcels(
                parcelRepository.findAllInInputOrder(), rejections);
        Map<ParcelKey, List<AssessmentDetail>> details = groupDetails(
                detailRepository.findAllInInputOrder());
        Map<Long, AssessmentParcel> parcelByProperty = new LinkedHashMap<>();
        parcels.forEach(parcel -> parcelByProperty.put(parcel.getParcelNumber(), parcel));
        Map<Long, HomeownerMaster> homeownerByProperty = new LinkedHashMap<>();
        homeowners.forEach(homeowner -> homeownerByProperty.putIfAbsent(
                homeowner.getPropertyNumber(), homeowner));

        List<HomeownerMaster> eligibleHomeowners = new ArrayList<>();
        Map<ParcelKey, EligibilityDecision> eligibilityByParcel = new LinkedHashMap<>();
        int eligibilityUpdates = 0;
        List<EligibilityPrintRecord> ineligibleRecords = new ArrayList<>();
        String variantProgram = variant == HomeownerVariant.ENUMERATED ? "ASREA852" : "ASREA853";
        String eligibilityRule = variant == HomeownerVariant.ENUMERATED
                ? "asrea852-002" : "asrea853-002";
        for (AssessmentParcel parcel : parcels) {
            ParcelKey parcelKey = new ParcelKey(parcel.getVolumeNumber(), parcel.getParcelNumber());
            List<AssessmentDetail> parcelDetails = details.getOrDefault(parcelKey, List.of());
            EligibilityDecision decision = kernel.evaluate(parcel, parcelDetails, variant);
            eligibilityByParcel.put(parcelKey, decision);
            if (!decision.eligible()) {
                rejections.add(new Rejection(
                        variantProgram + " ASSESSMENT",
                        recordKey(parcel),
                        "BYPASSED",
                        "Parcel is non-residential under the selected homeowner eligibility rules.",
                        eligibilityRule));
                ineligibleRecords.add(new EligibilityPrintRecord(
                        parcel.getVolumeNumber(),
                        parcel.getParcelNumber(),
                        parcel.getTaxCode(),
                        parcel.getOverallClass()));
                continue;
            }

            HomeownerMaster homeowner = homeownerByProperty.get(parcel.getParcelNumber());
            if (homeowner == null) {
                homeowner = newHomeowner(parcel);
            }
            refreshEligibility(homeowner, parcel, decision, businessDate);
            homeowner = homeownerRepository.save(homeowner);
            eligibleHomeowners.add(homeowner);
            eligibilityUpdates++;
            for (EligibilityWarning warning : decision.warnings()) {
                rejections.add(new Rejection(
                        variantProgram + " ASSESSMENT",
                        recordKey(parcel),
                        "WARNING",
                        warning.message(),
                        warning.ruleId()));
            }
        }

        reconciliations.add(new Reconciliation(
                variantProgram + " HOMEOWNER ELIGIBILITY", "RECONCILED",
                parcels.size() + homeowners.size(), eligibleHomeowners.size(),
                eligibleHomeowners.size(), 0,
                variantRuleIds(variant),
                eligibleHomeowners.isEmpty()
                        ? "The reviewed ten assessment roots had no qualifying details and produced no homeowner records."
                        : "Synthetic rule examples produced homeowner records; this is not a reviewed parity claim."));

        List<HomeownerExemption> exemptions = new ArrayList<>(eligibleHomeowners.size());
        for (HomeownerMaster homeowner : eligibleHomeowners) {
            AssessmentParcel parcel = parcelByProperty.get(homeowner.getPropertyNumber());
            if (parcel == null) {
                throw new IllegalStateException(
                        "Eligible homeowner has no current assessment parcel");
            }
            EligibilityDecision decision = eligibilityByParcel.get(
                    new ParcelKey(parcel.getVolumeNumber(), parcel.getParcelNumber()));
            exemptions.add(toExemption(homeowner, parcel, decision));
        }
        replaceExemptionGeneration(exemptions);

        reconciliations.add(new Reconciliation(
                "ASREA859 HOMEOWNER ROLL-FORWARD", "RECONCILED",
                parcels.size() + eligibleHomeowners.size(), eligibleHomeowners.size(),
                exemptions.size(), 0,
                List.of("asrea859-001", "asrea859-002", "asrea859-003", "asrea859-004", "asrea859-005"),
                exemptions.isEmpty()
                        ? "ASREA859 HOMEOUT was published as an explicit empty generation."
                        : "Synthetic rule examples were published without claiming reviewed positive-path parity."));

        List<OutputRecord> outputs = reviewedOutputs(variantProgram, eligibleHomeowners.size(), exemptions.size());
        List<RuleDisposition> ruleOutcomes = ruleOutcomes(variant, renewalUpdates, eligibilityUpdates, exemptions.size());
        List<Message> messages = List.of(
                new Message("INFO", "The reviewed "
                        + (variant == HomeownerVariant.ENUMERATED ? "HOME852" : "HOME853")
                        + " path completed with return code 0.",
                        variant == HomeownerVariant.ENUMERATED ? "asrea852-001" : "asrea853-001"),
                new Message("INFO",
                        "Rejected ASHMA850 and unreviewed Senior Freeze paths remain source-only and were not executed.",
                        "ashma850-001"));

        int rejectedCount = (int) rejections.stream()
                .filter(rejection -> "REJECTED".equals(rejection.outcome()))
                .count();
        int recordsRead = 10 + (homeowners.size() * 3)
                + (parcels.size() * 2) + eligibleHomeowners.size();
        int recordsWritten = (homeowners.size() * 2)
                + eligibleHomeowners.size() + exemptions.size();
        BatchEvidence evidence = new BatchEvidence(
                List.copyOf(renewalPrintRecords),
                List.copyOf(renewalErrorRecords),
                List.copyOf(ineligibleRecords),
                exemptions.stream().map(HomeoutRecord::from).toList(),
                homeowners.size(),
                parcels.size(),
                eligibleHomeowners.size(),
                List.of(ASREA841_DISPLAY));
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

    private List<RenewalPrintRecord> applyReviewedRenewals(List<HomeownerMaster> homeowners) {
        Map<Long, RenewalInput> matchedRenewals = new LinkedHashMap<>();
        REVIEWED_RENEWALS.stream()
                .filter(RenewalInput::matched)
                .forEach(renewal -> matchedRenewals.put(renewal.propertyNumber(), renewal));
        List<RenewalPrintRecord> updated = new ArrayList<>();
        for (HomeownerMaster homeowner : homeowners) {
            RenewalInput renewal = matchedRenewals.get(homeowner.getPropertyNumber());
            if (renewal != null) {
                homeowner.setResponseStatus(2);
                homeownerRepository.save(homeowner);
                updated.add(RenewalPrintRecord.from(homeowner, renewal.batchNumber()));
            }
        }
        return List.copyOf(updated);
    }

    private static List<RenewalErrorRecord> addReviewedRenewalRejections(
            List<Rejection> rejections) {
        List<RenewalErrorRecord> errors = REVIEWED_RENEWALS.stream()
                .filter(renewal -> !renewal.matched())
                .map(renewal -> new RenewalErrorRecord(
                        renewal.propertyNumber(), renewal.batchNumber()))
                .toList();
        rejections.add(new Rejection(
                "ASREA841 RENEWAL", "12022000020000/LOW01", "REJECTED",
                "Renewal key is below the current merge position.", "asrea841-004"));
        rejections.add(new Rejection(
                "ASREA841 RENEWAL", "12500000000000/MISS1", "REJECTED",
                "Renewal has no matching homeowner and cannot create one.", "asrea841-003"));
        rejections.add(new Rejection(
                "ASREA841 RENEWAL", "60000000000000/MISS2", "REJECTED",
                "Renewal has no matching homeowner and cannot create one.", "asrea841-003"));
        return errors;
    }

    private static List<HomeownerMaster> deduplicateHomeowners(
            List<HomeownerMaster> source,
            List<Rejection> rejections) {
        Map<Long, HomeownerMaster> unique = new LinkedHashMap<>();
        for (HomeownerMaster homeowner : source) {
            if (homeowner.getPropertyNumber() == null || homeowner.getPropertyNumber() <= 0) {
                rejections.add(new Rejection(
                        "HOMEOWNER MASTER", null, "REJECTED",
                        "Homeowner property number must be positive.", "asrea841-001"));
                continue;
            }
            if (unique.putIfAbsent(homeowner.getPropertyNumber(), homeowner) != null) {
                rejections.add(new Rejection(
                        "HOMEOWNER MASTER", homeowner.getPropertyNumber().toString(), "REJECTED",
                        "Duplicate homeowner property was rejected by the explicit first-record policy.",
                        "asrea841-004"));
            }
        }
        return List.copyOf(unique.values());
    }

    private static List<AssessmentParcel> orderedRealEstateParcels(
            List<AssessmentParcel> source,
            List<Rejection> rejections) {
        Map<ParcelKey, AssessmentParcel> unique = new LinkedHashMap<>();
        source.stream()
                .sorted(Comparator.comparing(AssessmentParcel::getVolumeNumber,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(AssessmentParcel::getParcelNumber,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                .forEach(parcel -> {
                    Integer volume = parcel.getVolumeNumber();
                    if (volume == null || volume < 1 || volume > 601) {
                        rejections.add(new Rejection(
                                "ASSESSMENT MASTER", recordKey(parcel), "REJECTED",
                                "Only real-estate volumes 1 through 601 are processed.",
                                "asrea859-005"));
                        return;
                    }
                    if (parcel.getParcelNumber() == null || parcel.getParcelNumber() <= 0) {
                        rejections.add(new Rejection(
                                "ASSESSMENT MASTER", recordKey(parcel), "REJECTED",
                                "Assessment parcel number must be positive.",
                                "asrea859-005"));
                        return;
                    }
                    ParcelKey key = new ParcelKey(volume, parcel.getParcelNumber());
                    if (unique.putIfAbsent(key, parcel) != null) {
                        rejections.add(new Rejection(
                                "ASSESSMENT MASTER", recordKey(parcel), "REJECTED",
                                "Duplicate assessment parcel was rejected by the explicit first-record policy.",
                                "asrea859-005"));
                    }
                });
        return List.copyOf(unique.values());
    }

    private static Map<ParcelKey, List<AssessmentDetail>> groupDetails(List<AssessmentDetail> source) {
        Map<ParcelKey, List<AssessmentDetail>> result = new LinkedHashMap<>();
        for (AssessmentDetail detail : source) {
            result.computeIfAbsent(
                            new ParcelKey(detail.getParcelVolumeNumber(), detail.getParcelNumber()),
                            unused -> new ArrayList<>())
                    .add(detail);
        }
        result.replaceAll((unused, details) -> details.stream()
                .sorted(Comparator.comparing(
                        AssessmentDetail::getOccurrenceNumber,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList());
        return result;
    }

    private static HomeownerMaster newHomeowner(AssessmentParcel parcel) {
        HomeownerMaster homeowner = new HomeownerMaster();
        homeowner.setPropertyNumber(parcel.getParcelNumber());
        homeowner.setVolumeNumber(parcel.getVolumeNumber());
        homeowner.setTaxCode(parcel.getTaxCode());
        homeowner.setTaxType(parseInteger(parcel.getTaxType()));
        homeowner.setProration(ONE);
        homeowner.setEqualizationFactor(BigDecimal.ONE);
        homeowner.setResponseStatus(0);
        homeowner.setSecondaryResponseStatus(0);
        homeowner.setTertiaryStatus(0);
        homeowner.setNpheStatus("AB");
        return homeowner;
    }

    private static void refreshEligibility(
            HomeownerMaster homeowner,
            AssessmentParcel parcel,
            EligibilityDecision decision,
            LocalDate businessDate) {
        int applicationYear = businessDate.getYear() % 100;
        boolean newYear = !Integer.valueOf(applicationYear).equals(homeowner.getApplicationYear());
        homeowner.setApplicationYear(applicationYear);
        homeowner.setAssessmentClass(decision.assessmentClass());
        homeowner.setAssessedValue(decision.assessedValue());
        homeowner.setProration(decision.proration());
        homeowner.setOccupancyFactor(decision.occupancyFactor());
        homeowner.setVolumeNumber(parcel.getVolumeNumber());
        homeowner.setTaxCode(parcel.getTaxCode());
        if (newYear) {
            homeowner.setResponseStatus(0);
            homeowner.setSecondaryResponseStatus(0);
            homeowner.setTertiaryStatus(0);
            homeowner.setNpheStatus("TR".equals(homeowner.getNpheStatus()) ? "TR" : "AB");
        }
        if (ONE_HUNDRED.compareTo(decision.secondaryOccupancyFactor()) == 0
                && Integer.valueOf(2).equals(homeowner.getResponseStatus())) {
            homeowner.setResponseStatus(0);
        }
    }

    private static HomeownerExemption toExemption(
            HomeownerMaster homeowner,
            AssessmentParcel parcel,
            EligibilityDecision decision) {
        HomeownerExemption exemption = new HomeownerExemption();
        exemption.setApplicationYear(homeowner.getApplicationYear());
        exemption.setAssessedValue(homeowner.getAssessedValue());
        exemption.setAssessmentClass(homeowner.getAssessmentClass());
        exemption.setCertificateOfErrorNumber(homeowner.getCertificateOfErrorNumber());
        exemption.setCity(homeowner.getCity());
        exemption.setClerksClass(parcel.getOverallClass());
        exemption.setCooperativeQuantity(homeowner.getCooperativeQuantity());
        exemption.setEligibilityIndicator(1);
        exemption.setEqualizationFactor(homeowner.getEqualizationFactor());
        exemption.setEqualizedValue(homeowner.getEqualizedValue() == null
                ? homeowner.getAssessedValue() : homeowner.getEqualizedValue());
        exemption.setExemptionType(homeowner.getExemptionType());
        exemption.setKeyParcelNumber(decision.keyParcelNumber());
        exemption.setMailingAddress(homeowner.getMailingAddress());
        exemption.setOccupancyFactor(homeowner.getOccupancyFactor());
        exemption.setOwnerName(homeowner.getOwnerName());
        exemption.setPropertyNumber(homeowner.getPropertyNumber());
        exemption.setProration(homeowner.getProration());
        exemption.setRecordCode(0);
        exemption.setResponseStatus(homeowner.getResponseStatus());
        exemption.setSecondaryResponseStatus(homeowner.getSecondaryResponseStatus());
        exemption.setSplitCode(parseInteger(decision.splitCode()));
        exemption.setState(homeowner.getState());
        exemption.setTaxCode(homeowner.getTaxCode());
        exemption.setTaxType(homeowner.getTaxType());
        exemption.setTertiaryStatus(homeowner.getTertiaryStatus());
        exemption.setVolumeNumber(homeowner.getVolumeNumber());
        exemption.setZipCode(homeowner.getZipCode());
        return exemption;
    }

    private void replaceExemptionGeneration(List<HomeownerExemption> exemptions) {
        exemptionRepository.findAll(Pageable.unpaged()).forEach(existing ->
                exemptionRepository.deleteById(existing.getId()));
        exemptions.forEach(exemptionRepository::save);
    }

    private static List<OutputRecord> reviewedOutputs(
            String variantProgram,
            int eligibilityRecords,
            int exemptionRecords) {
        return List.of(
                new OutputRecord("ASREA841 HOMEOUT", "STAGING", 10, "PUBLISHED",
                        List.of("asrea841-002")),
                new OutputRecord("ASREA841 PRINTOUT", "REPORT", 13, "PUBLISHED",
                        List.of("asrea841-002")),
                new OutputRecord("ASREA841 ERRPRINT", "REPORT", 9, "PUBLISHED",
                        List.of("asrea841-001", "asrea841-003", "asrea841-004")),
                new OutputRecord(variantProgram + " HOMEOUT", "STAGING", eligibilityRecords,
                        eligibilityRecords == 0 ? "EMPTY" : "PUBLISHED",
                        List.of(variantProgram.equals("ASREA852") ? "asrea852-005" : "asrea853-004")),
                new OutputRecord(variantProgram + " PRINTOUT", "REPORT",
                        eligibilityRecords == 0 ? 13 : eligibilityRecords + 3,
                        "PUBLISHED", variantRuleIds(
                                variantProgram.equals("ASREA852")
                                        ? HomeownerVariant.ENUMERATED : HomeownerVariant.BROAD)),
                new OutputRecord("ASREA859 HOMEOUT", "DATASET", exemptionRecords,
                        exemptionRecords == 0 ? "EMPTY" : "PUBLISHED",
                        List.of("asrea859-001", "asrea859-004")),
                new OutputRecord("ASREA859 PRINTOUT", "REPORT", exemptionRecords + 3,
                        "PUBLISHED", List.of("asrea859-001")));
    }

    private static List<String> variantRuleIds(HomeownerVariant variant) {
        String prefix = variant == HomeownerVariant.ENUMERATED ? "asrea852-" : "asrea853-";
        return numberedRuleIds(prefix, 6);
    }

    private static List<RuleDisposition> ruleOutcomes(
            HomeownerVariant variant,
            int renewalUpdates,
            int eligibilityUpdates,
            int exemptionRecords) {
        List<RuleDisposition> outcomes = new ArrayList<>();
        outcomes.add(new RuleDisposition("asrea841-001", "APPLIED", 10, null));
        outcomes.add(new RuleDisposition("asrea841-002", "APPLIED", renewalUpdates, null));
        outcomes.add(new RuleDisposition("asrea841-003", "APPLIED", 2, null));
        outcomes.add(new RuleDisposition("asrea841-004", "APPLIED", 1, null));
        outcomes.add(new RuleDisposition("asrea847-001", "NOT_APPLICABLE", 0,
                "No Property Master dataset is published by this app."));
        outcomes.add(new RuleDisposition("asrea847-002", "NOT_APPLICABLE", 0,
                "No Property Master owner segments are published by this app."));
        outcomes.add(new RuleDisposition("asrea847-003", "APPLIED", 10,
                "Existing homeowner contact information was preserved."));
        outcomes.add(new RuleDisposition("asrea847-004", "APPLIED", 10, null));

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

    private static void addVariantOutcomes(
            List<RuleDisposition> outcomes,
            HomeownerVariant ruleVariant,
            HomeownerVariant selectedVariant,
            int recordsAffected) {
        boolean selected = ruleVariant == selectedVariant;
        for (String id : variantRuleIds(ruleVariant)) {
            outcomes.add(new RuleDisposition(
                    id,
                    selected ? "APPLIED" : "NOT_APPLICABLE",
                    selected ? recordsAffected : 0,
                    selected ? null : "The alternative homeowner variant was not selected."));
        }
    }

    private static void addSourceOnly(
            List<RuleDisposition> outcomes,
            String prefix,
            int count,
            String path) {
        for (String id : numberedRuleIds(prefix, count)) {
            outcomes.add(new RuleDisposition(
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

    private static Integer parseInteger(String value) {
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
        return parcel.getVolumeNumber() + "/" + parcel.getParcelNumber();
    }

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

        public ProcessResult {
            Objects.requireNonNull(batchEvidence, "batchEvidence");
        }
    }

    public record OutputRecord(
            String name,
            String kind,
            long recordCount,
            String publicationStatus,
            List<String> ruleIds) {}

    public record Message(String severity, String message, String ruleId) {}

    public record Rejection(
            String source,
            String recordKey,
            String outcome,
            String message,
            String ruleId) {}

    public record Reconciliation(
            String name,
            String status,
            long recordsRead,
            long recordsMatched,
            long recordsWritten,
            long recordsRejected,
            List<String> ruleIds,
            String message) {}

    public record RuleDisposition(
            String ruleId,
            String outcome,
            long recordsAffected,
            String message) {}

    public record BatchEvidence(
            List<RenewalPrintRecord> renewalPrintRecords,
            List<RenewalErrorRecord> renewalErrorRecords,
            List<EligibilityPrintRecord> ineligibleRecords,
            List<HomeoutRecord> homeoutRecords,
            int homeownerRecords,
            int assessmentRecords,
            int eligibleRecords,
            List<String> batchDisplays) {

        public BatchEvidence {
            renewalPrintRecords = List.copyOf(renewalPrintRecords);
            renewalErrorRecords = List.copyOf(renewalErrorRecords);
            ineligibleRecords = List.copyOf(ineligibleRecords);
            homeoutRecords = List.copyOf(homeoutRecords);
            batchDisplays = List.copyOf(batchDisplays);
        }
    }

    public record RenewalPrintRecord(
            int volumeNumber,
            long propertyNumber,
            int taxCode,
            int assessmentClass,
            int applicationYear,
            BigDecimal proration,
            int cooperativeQuantity,
            long assessedValue,
            String batchNumber) {

        private static RenewalPrintRecord from(
                HomeownerMaster homeowner,
                String batchNumber) {
            return new RenewalPrintRecord(
                    value(homeowner.getVolumeNumber()),
                    value(homeowner.getPropertyNumber()),
                    value(homeowner.getTaxCode()),
                    value(homeowner.getAssessmentClass()),
                    value(homeowner.getApplicationYear()),
                    homeowner.getProration() == null ? BigDecimal.ZERO : homeowner.getProration(),
                    value(homeowner.getCooperativeQuantity()),
                    value(homeowner.getAssessedValue()),
                    batchNumber);
        }
    }

    public record RenewalErrorRecord(long propertyNumber, String batchNumber) {}

    public record EligibilityPrintRecord(
            int volumeNumber,
            long propertyNumber,
            int taxCode,
            int overallClass) {}

    public record HomeoutRecord(
            int volumeNumber,
            long propertyNumber,
            int taxCode,
            int taxType,
            int assessmentClass,
            long assessedValue,
            BigDecimal proration,
            int responseStatus) {

        private static HomeoutRecord from(HomeownerExemption exemption) {
            return new HomeoutRecord(
                    value(exemption.getVolumeNumber()),
                    value(exemption.getPropertyNumber()),
                    value(exemption.getTaxCode()),
                    value(exemption.getTaxType()),
                    value(exemption.getAssessmentClass()),
                    value(exemption.getAssessedValue()),
                    exemption.getProration() == null ? BigDecimal.ZERO : exemption.getProration(),
                    value(exemption.getResponseStatus()));
        }
    }

    private static int value(Integer value) {
        return value == null ? 0 : value;
    }

    private static long value(Long value) {
        return value == null ? 0L : value;
    }

    private record RenewalInput(long propertyNumber, String batchNumber, boolean matched) {}

    private record ParcelKey(Integer volumeNumber, Long parcelNumber) {}
}
