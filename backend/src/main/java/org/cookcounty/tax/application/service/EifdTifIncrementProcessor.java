package org.cookcounty.tax.application.service;

import org.cookcounty.tax.application.batch.EifdTifIncrementKernel;
import org.cookcounty.tax.application.batch.EifdTifIncrementKernel.AgencySummaryResult;
import org.cookcounty.tax.application.batch.EifdTifIncrementKernel.AllocatedFrozen;
import org.cookcounty.tax.application.batch.EifdTifIncrementKernel.AllocationResult;
import org.cookcounty.tax.application.batch.EifdTifIncrementKernel.ControlRanges;
import org.cookcounty.tax.application.batch.EifdTifIncrementKernel.DivisionActionInput;
import org.cookcounty.tax.application.batch.EifdTifIncrementKernel.FrozenAgencyResult;
import org.cookcounty.tax.application.batch.EifdTifIncrementKernel.PercentageResult;
import org.cookcounty.tax.application.batch.EifdTifIncrementKernel.PostingResult;
import org.cookcounty.tax.application.batch.EifdTifIncrementKernel.RollupResult;
import org.cookcounty.tax.application.batch.EifdTifIncrementKernel.Selection;
import org.cookcounty.tax.application.batch.EifdTifIncrementKernel.TaxCodeMaster;
import org.cookcounty.tax.application.batch.EifdTifIncrementKernel.TownReportResult;
import org.cookcounty.tax.application.batch.EifdTifIncrementProcessResult;
import org.cookcounty.tax.application.batch.EifdTifIncrementProcessResult.FrozenValuationReportFact;
import org.cookcounty.tax.application.batch.EifdTifIncrementProcessResult.Message;
import org.cookcounty.tax.application.batch.EifdTifIncrementProcessResult.Output;
import org.cookcounty.tax.application.service.EifdTifIncrementOutcomeProjector.Execution;
import org.cookcounty.tax.domain.contract.dto.EifdTifIncrementRunRequest;
import org.cookcounty.tax.domain.model.AgencyEqualizedValuation;
import org.cookcounty.tax.domain.model.AgencyReference;
import org.cookcounty.tax.domain.model.AssessmentDetail;
import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.cookcounty.tax.domain.model.FrozenValuation;
import org.cookcounty.tax.domain.model.TaxCodeMasterReference;
import org.cookcounty.tax.domain.model.TownReference;
import org.cookcounty.tax.domain.port.out.AgencyEqualizedValuationRepository;
import org.cookcounty.tax.domain.port.out.AssessmentDetailRepository;
import org.cookcounty.tax.domain.port.out.AssessmentParcelRepository;
import org.cookcounty.tax.domain.port.out.EifdTifReferenceDataRepository;
import org.cookcounty.tax.domain.port.out.FrozenValuationRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionOperations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/// Runs nine ordered increment steps over shared-live stores and transient handoffs.
///
/// Each mutating step owns one whole-step transaction. A later failure retains earlier committed
/// updates and the partial outputs permitted by that failure path.
@Component
public final class EifdTifIncrementProcessor {

    private final AssessmentParcelRepository parcelRepository;
    private final AssessmentDetailRepository detailRepository;
    private final FrozenValuationRepository frozenRepository;
    private final AgencyEqualizedValuationRepository agencyRepository;
    private final EifdTifReferenceDataRepository referenceDataRepository;
    private final AssessmentDetailValuator detailValuator;
    private final EifdTifIncrementKernel kernel;
    private final EifdTifIncrementOutcomeProjector outcomeProjector;

    /// Executes each shared-live write callback as one whole-step transaction.
    private final TransactionOperations transactions;

    /// Creates the processor with explicit calculation, projection, and transaction collaborators.
    ///
    /// The transaction collaborator must execute each callback. A no-transaction test
    /// implementation can execute callbacks without committing, but a mock that omits callbacks
    /// also omits every repository write and does not represent this contract.
    public EifdTifIncrementProcessor(
            AssessmentParcelRepository parcelRepository,
            AssessmentDetailRepository detailRepository,
            FrozenValuationRepository frozenRepository,
            AgencyEqualizedValuationRepository agencyRepository,
            EifdTifReferenceDataRepository referenceDataRepository,
            AssessmentDetailValuator detailValuator,
            EifdTifIncrementKernel kernel,
            EifdTifIncrementOutcomeProjector outcomeProjector,
            @Qualifier("eifdTifIncrementTransactions") TransactionOperations transactions) {
        this.parcelRepository = parcelRepository;
        this.detailRepository = detailRepository;
        this.frozenRepository = frozenRepository;
        this.agencyRepository = agencyRepository;
        this.referenceDataRepository = referenceDataRepository;
        this.detailValuator = detailValuator;
        this.kernel = kernel;
        this.outcomeProjector = outcomeProjector;
        this.transactions = transactions;
    }

    /// Runs every step in source order and stops on the first unrecoverable failure.
    ///
    /// Frozen-valuation and agency-posting writes commit in separate whole-step transactions. The
    /// returned counts and messages include eligible partial products from prior steps.
    public EifdTifIncrementProcessResult process(EifdTifIncrementRunRequest request) {
        ControlRanges controls =
                kernel.validateControls(
                        request.reassessmentControl(),
                        request.processingYear(),
                        request.annualEqualizationFactor());

        List<AssessmentParcel> parcels =
                new ArrayList<>(
                        parcelRepository.findAllInInputOrder().stream()
                                .filter(parcel -> parcel.propertyDivisionNumber() != null)
                                .map(this::eifdProjection)
                                .toList());
        parcels.sort(
                Comparator.comparing(
                                (AssessmentParcel parcel) ->
                                        Objects.requireNonNull(parcel.propertyDivisionNumber()))
                        .thenComparingLong(parcel -> Long.parseLong(parcel.parcelNumber())));

        List<AssessmentDetail> details =
                detailRepository.findAllInPersistenceOrder().stream()
                        .map(detailValuator::value)
                        .toList();
        Map<ParcelKey, List<AssessmentDetail>> detailsByParcel = new HashMap<>();
        for (AssessmentDetail detail : details) {
            detailsByParcel
                    .computeIfAbsent(
                            new ParcelKey(detail.parcelVolumeNumber(), detail.parcelNumber()),
                            unused -> new ArrayList<>())
                    .add(detail);
        }

        List<FrozenValuation> frozenValues =
                new ArrayList<>(frozenRepository.findAllInPersistenceOrder());
        frozenValues.sort(Comparator.comparing(FrozenValuation::divisionNumber));
        List<FrozenValuationReportFact> frozenBefore =
                frozenValues.stream().map(this::frozenValuationReportFact).toList();

        Map<String, FrozenValuation> frozenByDivision = new LinkedHashMap<>();
        for (FrozenValuation frozen : frozenValues) {
            frozenByDivision.put(frozen.divisionNumber(), frozen);
        }
        List<FrozenValuation> frozenReplacements =
                accumulateMasterDivisions(parcels, frozenByDivision);
        List<FrozenValuationReportFact> frozenValuationReportFacts =
                frozenReplacements.stream().map(this::frozenValuationReportFact).toList();
        transactions.executeWithoutResult(
                unused -> frozenReplacements.forEach(frozenRepository::save));
        int frozenUpdated = frozenReplacements.size();
        frozenValues = new ArrayList<>(frozenByDivision.values());
        frozenValues.sort(Comparator.comparing(FrozenValuation::divisionNumber));

        List<Selection> selections = select(parcels, detailsByParcel, controls);
        RollupResult rollups = kernel.rollup(selections);
        int recordsRead =
                frozenValues.size() + parcels.size() + details.size() + rollups.recordsRead();
        int recordsWritten = frozenValues.size();
        int recordsRejected = 0;
        List<Output> outputs = new ArrayList<>();
        outputs.add(new Output("frozenValuationReconciliationReport", frozenValues.size(), null));
        List<Message> messages = new ArrayList<>();
        if (rollups.failed()) {
            messages.add(
                    new Message(
                            "ERROR",
                            "Frozen valuation selections are out of sequence at "
                                    + rollups.offendingKey()
                                    + ".",
                            "asrea743-001"));
            recordsWritten += rollups.taxCodeTotals().size() + rollups.divisionTotals().size();
            return result(false, recordsRead, recordsWritten, frozenUpdated, 1, outputs, messages);
        }

        PercentageResult percentages;
        try {
            percentages = kernel.percentages(rollups.taxCodeTotals(), rollups.divisionTotals());
        } catch (EifdTifIncrementKernel.RuleViolation violation) {
            messages.add(new Message("ERROR", violation.getMessage(), violation.ruleId()));
            return result(false, recordsRead, recordsWritten, frozenUpdated, 1, outputs, messages);
        }
        recordsRead += rollups.taxCodeTotals().size() + rollups.divisionTotals().size();
        recordsWritten +=
                rollups.taxCodeTotals().size()
                        + rollups.divisionTotals().size()
                        + percentages.percentages().size();
        recordsRejected += percentages.unmatchedTaxCodeTotals().size();

        frozenByDivision = new HashMap<>();
        for (FrozenValuation frozen : frozenValues) {
            frozenByDivision.put(frozen.divisionNumber(), frozen);
        }
        AllocationResult allocations = kernel.allocate(percentages.percentages(), frozenByDivision);
        recordsRead += percentages.percentages().size();
        recordsWritten += allocations.allocations().size();
        recordsRejected += allocations.unmatchedCount();

        Map<String, TaxCodeMaster> masters = loadTaxCodeMasters(allocations.allocations());
        FrozenAgencyResult frozenAgencies =
                kernel.expandFrozenAgencies(
                        allocations.allocations(), masters, controls.equalizationFactor());
        recordsRead += frozenAgencies.recordsRead();
        recordsWritten += frozenAgencies.details().size();
        if (frozenAgencies.failed()) {
            messages.add(
                    new Message(
                            "ERROR",
                            "No matching tax-code master record for tax code "
                                    + frozenAgencies.missingTaxCode()
                                    + ".",
                            "asrea748-005"));
            return result(
                    false,
                    recordsRead,
                    recordsWritten,
                    frozenUpdated,
                    recordsRejected + 1,
                    outputs,
                    messages);
        }

        Map<Long, String> agencyDescriptions = loadAgencyDescriptions(frozenAgencies.details());
        AgencySummaryResult summaries =
                kernel.summarizeAgencies(frozenAgencies.details(), agencyDescriptions);
        recordsRead += summaries.recordsRead();
        recordsWritten += summaries.summaries().size();
        if (summaries.failed()) {
            messages.add(
                    new Message(
                            "ERROR",
                            "No agency reference description exists for agency "
                                    + summaries.offendingAgency()
                                    + ".",
                            "clrtm749-003"));
            return result(
                    false,
                    recordsRead,
                    recordsWritten,
                    frozenUpdated,
                    recordsRejected + 1,
                    outputs,
                    messages);
        }

        Map<Integer, String> townNames = loadTownNames(frozenAgencies.details());
        TownReportResult townReport = kernel.reportTowns(frozenAgencies.details(), townNames);
        recordsRead += frozenAgencies.details().size();
        if (townReport.failed()) {
            messages.add(
                    new Message("ERROR", "Agency-town input is out of sequence.", "clrtm750-001"));
            return result(
                    false,
                    recordsRead,
                    recordsWritten,
                    frozenUpdated,
                    recordsRejected + 1,
                    outputs,
                    messages);
        }

        int agencyReportRecords = summaries.summaries().size() + 5;
        int townReportRecords =
                townReport.townTotals().isEmpty() ? 0 : townReport.townTotals().size() * 8 + 3;
        int appendRecords = townReport.townTotals().size() * 13;
        outputs.add(new Output("agencySummaryReport", agencyReportRecords, null));
        outputs.add(new Output("townWithinAgencyReport", townReportRecords, null));
        outputs.add(new Output("agencyYearAppendRecords", appendRecords, null));
        recordsWritten += agencyReportRecords + townReportRecords + appendRecords;

        List<AgencyEqualizedValuation> agencies =
                new ArrayList<>(agencyRepository.findAllInPersistenceOrder());
        Map<String, AgencyEqualizedValuation> agencyByNumber = new LinkedHashMap<>();
        for (AgencyEqualizedValuation agency : agencies) {
            agencyByNumber.put(agency.agencyNumber(), agency);
        }
        PostingResult posting = kernel.postAgencySummaries(summaries.summaries(), agencyByNumber);
        recordsRead += posting.recordsRead();
        recordsRejected += posting.unmatchedAgencies().size();
        int agencyUpdated = 0;
        if (posting.failed()) {
            messages.add(
                    new Message(
                            "ERROR", "Agency posting input is out of sequence.", "clrtm756-001"));
            return result(
                    false,
                    recordsRead,
                    recordsWritten,
                    frozenUpdated + agencyUpdated,
                    recordsRejected + 1,
                    outputs,
                    messages);
        }
        agencyUpdated = posting.updated().size();
        transactions.executeWithoutResult(
                unused -> posting.updated().values().forEach(agencyRepository::save));
        for (Long unmatched : posting.unmatchedAgencies()) {
            messages.add(
                    new Message(
                            "WARNING",
                            "NO MATCHING AGENCY ON AGENCY EQUALIZED VALUATION FILE: " + unmatched,
                            "clrtm756-005"));
        }

        // The reviewed unmatched line intentionally carries the matched-read counter. Projection
        // preserves that observable rather than reinterpreting it.
        messages.add(
                new Message(
                        "INFO",
                        "TOTAL NUMBER OF CLERK'S NEW PROPERTY RECORDS READ "
                                + posting.recordsRead(),
                        null));
        messages.add(
                new Message(
                        "INFO",
                        "TOTAL NUMBER OF CLERK'S NEW PROPERTY RECORDS UNMATCHED "
                                + posting.updated().size(),
                        null));
        messages.add(
                new Message(
                        "INFO",
                        "TOTAL NUMBER OF VSAM AGENCY EQ-VALUATION RECORDS UPDATED " + agencyUpdated,
                        null));
        messages.add(new Message("INFO", "EIFD/TIF increment batch completed successfully.", null));
        EifdTifIncrementProcessResult result =
                new EifdTifIncrementProcessResult(
                        true,
                        0,
                        recordsRead,
                        recordsWritten,
                        frozenUpdated + agencyUpdated,
                        recordsRejected,
                        outputs,
                        messages,
                        frozenValuationReportFacts);
        Execution execution =
                new Execution(
                        request,
                        parcels,
                        frozenBefore,
                        frozenValues,
                        rollups,
                        percentages,
                        allocations,
                        frozenAgencies,
                        summaries,
                        townReport,
                        agencies,
                        posting);
        return new EifdTifIncrementProcessResult(
                result.completed(),
                result.returnCode(),
                result.recordsRead(),
                result.recordsWritten(),
                result.recordsUpdated(),
                result.recordsRejected(),
                result.outputs(),
                result.messages(),
                result.frozenValuationReportFacts(),
                outcomeProjector.project(execution, result));
    }

    /// Builds one selection per parcel from eligible scale-zero detail values.
    ///
    /// Persisted frozen valuations belong to the later allocation step. They must not seed these
    /// detail totals, or a parcel with no details can acquire a false first-time valuation.
    private List<Selection> select(
            List<AssessmentParcel> parcels,
            Map<ParcelKey, List<AssessmentDetail>> detailsByParcel,
            ControlRanges controls) {
        List<Selection> selections = new ArrayList<>(parcels.size());
        for (AssessmentParcel parcel : parcels) {
            BigDecimal current288 = BigDecimal.ZERO;
            BigDecimal firstTime = BigDecimal.ZERO;
            for (AssessmentDetail detail :
                    detailsByParcel.getOrDefault(
                            new ParcelKey(parcel.volumeNumber(), parcel.parcelNumber()),
                            List.of())) {
                int category = category(detail.detailType());
                BigDecimal valuation = detail.valuation();
                if (detail.assessmentClass() != null
                        && detail.improvementYear() != null
                        && kernel.qualifiesCurrent288(
                                category,
                                detail.assessmentClass(),
                                detail.improvementYear(),
                                controls.processingYear(),
                                false)) {
                    current288 = current288.add(valuation);
                }
                if (detail.age() != null
                        && detail.assessmentClass() != null
                        && kernel.qualifiesFirstTime(
                                category, detail.age(), detail.assessmentClass())) {
                    firstTime = firstTime.add(valuation);
                }
            }
            selections.add(
                    new Selection(
                            Objects.requireNonNull(parcel.propertyDivisionNumber()),
                            parcel.taxCode(),
                            current288,
                            firstTime,
                            BigDecimal.ZERO));
        }
        return selections;
    }

    /// Flushes every division delta, including the final group and zero-valued open groups.
    private List<FrozenValuation> accumulateMasterDivisions(
            List<AssessmentParcel> parcels, Map<String, FrozenValuation> frozenByDivision) {
        Map<String, FrozenValuation> deltas = new LinkedHashMap<>();
        for (AssessmentParcel parcel : parcels) {
            String division = Objects.requireNonNull(parcel.propertyDivisionNumber());
            FrozenValuation delta = masterDelta(division, parcel);
            deltas.compute(
                    division, (unused, existing) -> kernel.accumulateFrozen(existing, delta));
        }
        List<FrozenValuation> replacements = new ArrayList<>();
        for (Map.Entry<String, FrozenValuation> entry : deltas.entrySet()) {
            FrozenValuation stored = frozenByDivision.get(entry.getKey());
            FrozenValuation replacement = kernel.accumulateFrozen(stored, entry.getValue());
            if (stored != null || kernel.shouldInsertFrozen(replacement)) {
                frozenByDivision.put(entry.getKey(), replacement);
                replacements.add(replacement);
            }
        }
        return replacements;
    }

    /// Loads each allocated tax code from the persisted reference repository.
    private Map<String, TaxCodeMaster> loadTaxCodeMasters(List<AllocatedFrozen> allocations) {
        Map<String, TaxCodeMaster> masters = new HashMap<>();
        for (AllocatedFrozen allocation : allocations) {
            referenceDataRepository
                    .findTaxCodeMaster(allocation.taxCode())
                    .ifPresent(
                            reference ->
                                    masters.put(
                                            reference.taxCode(), toKernelTaxCodeMaster(reference)));
        }
        return masters;
    }

    /// Restores the sparse persisted agency slots to all 40 source positions.
    private TaxCodeMaster toKernelTaxCodeMaster(TaxCodeMasterReference reference) {
        List<Long> agencyPositions = new ArrayList<>(java.util.Collections.nCopies(40, 0L));
        reference
                .agencySlots()
                .forEach(
                        slot ->
                                agencyPositions.set(
                                        slot.position() - 1, Long.parseLong(slot.agencyNumber())));
        return new TaxCodeMaster(
                reference.taxCode(),
                reference.taxRate(),
                Integer.parseInt(kernel.deriveTownNumber(reference.taxCode())),
                agencyPositions);
    }

    /// Loads report descriptions for each positive agency in source order.
    private Map<Long, String> loadAgencyDescriptions(
            List<EifdTifIncrementKernel.FrozenAgencyDetail> details) {
        Map<Long, String> descriptions = new HashMap<>();
        for (EifdTifIncrementKernel.FrozenAgencyDetail detail : details) {
            String agencyNumber = String.format("%09d", detail.agency());
            referenceDataRepository
                    .findAgency(agencyNumber)
                    .map(AgencyReference::description)
                    .ifPresent(description -> descriptions.put(detail.agency(), description));
        }
        return descriptions;
    }

    /// Loads town names for the town codes present in agency details.
    private Map<Integer, String> loadTownNames(
            List<EifdTifIncrementKernel.FrozenAgencyDetail> details) {
        Map<Integer, String> names = new HashMap<>();
        for (EifdTifIncrementKernel.FrozenAgencyDetail detail : details) {
            String townNumber = String.format("%02d", detail.town());
            referenceDataRepository
                    .findTown(townNumber)
                    .map(TownReference::name)
                    .ifPresent(name -> names.put(detail.town(), name));
        }
        return names;
    }

    /// Reads the leading detail-category digit, or zero when the optional category is unavailable.
    private int category(@Nullable String detailType) {
        if (detailType == null || detailType.isBlank()) {
            return 0;
        }
        char first = detailType.charAt(0);
        return Character.isDigit(first) ? first - '0' : 0;
    }

    /// Creates the assessment view used by increment calculations.
    ///
    /// The projection preserves every parcel component and substitutes the six reviewed increment
    /// values for the generic prior and current valuation components.
    private AssessmentParcel eifdProjection(AssessmentParcel source) {
        return new AssessmentParcel(
                source.id(),
                source.version(),
                source.archivedPreConversionProposedTotal(),
                source.assessmentStatus(),
                source.clerkMajorClass(),
                source.combinedHomeownerNonHomeownerValue(),
                requireIncrementValue(
                        source.eifdCurrentImprovementValue(), "current improvement value"),
                requireIncrementValue(source.eifdCurrentLandValue(), "current land value"),
                requireIncrementValue(source.eifdCurrentTotalValue(), "current total value"),
                source.detailQuestionnaireCount(),
                source.farmValue(),
                source.overallClass(),
                source.parcelNumber(),
                source.parcelStatus(),
                requireIncrementValue(
                        source.eifdPriorImprovementValue(), "prior improvement value"),
                requireIncrementValue(source.eifdPriorLandValue(), "prior land value"),
                requireIncrementValue(source.eifdPriorTotalValue(), "prior total value"),
                source.proposedImprovementValue(),
                source.proposedLandValue(),
                source.proposedTotalValue(),
                source.salesSegmentCount(),
                source.taxCode(),
                source.taxType(),
                source.volumeNumber(),
                source.priorParcelStatus(),
                source.propertyDivisionNumber(),
                source.eifdPriorLandValue(),
                source.eifdPriorImprovementValue(),
                source.eifdPriorTotalValue(),
                source.eifdCurrentLandValue(),
                source.eifdCurrentImprovementValue(),
                source.eifdCurrentTotalValue());
    }

    /// Rejects an incomplete increment input instead of treating its missing valuation as zero.
    private static BigDecimal requireIncrementValue(@Nullable BigDecimal value, String field) {
        if (value == null) {
            throw new IllegalStateException("The increment input does not supply " + field);
        }
        return value;
    }

    /// Builds one complete scale-zero frozen-valuation delta for a parcel.
    private FrozenValuation masterDelta(String division, AssessmentParcel parcel) {
        boolean exemptTransition = fullyExemptPriorBecameNonexempt(parcel);
        return kernel.selectActionTotals(
                new DivisionActionInput(
                        division,
                        division.substring(0, 4),
                        amount(parcel.priorLandValue()),
                        amount(parcel.priorImprovementValue()),
                        amount(parcel.priorTotalValue()),
                        1L,
                        amount(parcel.currentLandValue()),
                        amount(parcel.currentImprovementValue()),
                        amount(parcel.currentTotalValue()),
                        1L,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        permitChanged(parcel),
                        exemptTransition));
    }

    /// Detects the reviewed transition from fully exempt to a nonexempt status.
    private boolean fullyExemptPriorBecameNonexempt(AssessmentParcel parcel) {
        return "1".equals(parcel.priorParcelStatus())
                && parcel.parcelStatus() != null
                && !"1".equals(parcel.parcelStatus());
    }

    /// Detects the reviewed permit-change indicator values.
    private boolean permitChanged(AssessmentParcel parcel) {
        // The shared assessment field supplies the reviewed permit-change indicator.
        return "2".equals(parcel.taxType()) || "3".equals(parcel.taxType());
    }

    /// Captures frozen valuations in source order without narrowing monetary values.
    ///
    /// The snapshot has no persistence effect. The enclosing whole-step transaction owns the
    /// related repository write, and a later step failure does not remove that committed effect.
    private FrozenValuationReportFact frozenValuationReportFact(FrozenValuation value) {
        return new FrozenValuationReportFact(
                value.divisionNumber(),
                amount(value.priorLandValue()),
                amount(value.priorImprovementValue()),
                amount(value.priorTotalValue()),
                count(value.priorParcelCount()),
                amount(value.currentLandValue()),
                amount(value.currentImprovementValue()),
                amount(value.currentTotalValue()),
                count(value.currentParcelCount()),
                amount(value.proposedImprovementValue()),
                amount(value.proposedExpired288Value()),
                amount(value.proposedCurrent288Value()),
                amount(value.proposedTotalValue()),
                amount(value.proposedActualValue()),
                amount(value.changeActionPriorLandValue()),
                amount(value.changeActionPriorImprovementValue()),
                amount(value.changeActionPriorTotalValue()),
                count(value.changeActionPriorParcelCount()),
                amount(value.changeActionCurrentLandValue()),
                amount(value.changeActionCurrentImprovementValue()),
                amount(value.changeActionCurrentTotalValue()),
                count(value.changeActionCurrentParcelCount()),
                amount(value.noChangeActionPriorLandValue()),
                amount(value.noChangeActionPriorImprovementValue()),
                amount(value.noChangeActionPriorTotalValue()),
                count(value.noChangeActionPriorParcelCount()),
                amount(value.noChangeActionCurrentLandValue()),
                amount(value.noChangeActionCurrentImprovementValue()),
                amount(value.noChangeActionCurrentTotalValue()),
                count(value.noChangeActionCurrentParcelCount()));
    }

    /// Builds a terminal process result and projects failure evidence when required.
    private EifdTifIncrementProcessResult result(
            boolean completed,
            int recordsRead,
            int recordsWritten,
            int recordsUpdated,
            int recordsRejected,
            List<Output> outputs,
            List<Message> messages) {
        EifdTifIncrementProcessResult result =
                new EifdTifIncrementProcessResult(
                        completed,
                        completed ? 0 : 16,
                        recordsRead,
                        recordsWritten,
                        recordsUpdated,
                        recordsRejected,
                        outputs,
                        messages);
        return completed
                ? result
                : new EifdTifIncrementProcessResult(
                        result.completed(),
                        result.returnCode(),
                        result.recordsRead(),
                        result.recordsWritten(),
                        result.recordsUpdated(),
                        result.recordsRejected(),
                        result.outputs(),
                        result.messages(),
                        outcomeProjector.failed(result));
    }

    /// Converts an optional source count to its source-defined zero value.
    private long count(@Nullable Long value) {
        return value == null ? 0L : value;
    }

    /// Converts an optional whole-unit monetary value to exact scale-zero zero when absent.
    private BigDecimal amount(@Nullable BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /// Identifies one parcel detail group by the complete source owner key.
    private record ParcelKey(String volumeNumber, String parcelNumber) {}
}
