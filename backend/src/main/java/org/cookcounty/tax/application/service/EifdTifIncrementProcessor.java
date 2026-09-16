package org.cookcounty.tax.application.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import org.cookcounty.tax.application.batch.EifdTifIncrementProcessResult.Asrea740ReportFact;
import org.cookcounty.tax.application.batch.EifdTifIncrementProcessResult.Message;
import org.cookcounty.tax.application.batch.EifdTifIncrementProcessResult.Output;
import org.cookcounty.tax.application.service.EifdTifIncrementOutcomeProjector.Execution;
import org.cookcounty.tax.domain.model.AgencyEqualizedValuation;
import org.cookcounty.tax.domain.model.AssessmentDetail;
import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.cookcounty.tax.domain.model.FrozenValuation;
import org.cookcounty.tax.domain.port.out.AgencyEqualizedValuationRepository;
import org.cookcounty.tax.domain.port.out.AssessmentDetailRepository;
import org.cookcounty.tax.domain.port.out.AssessmentParcelRepository;
import org.cookcounty.tax.domain.port.out.FrozenValuationRepository;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.EifdTifIncrementRunRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Runs the nine accepted EIFD steps over shared-live stores and transient handoffs. */
@Component
public final class EifdTifIncrementProcessor {

    private static final Map<Long, String> REVIEWED_AGENCY_REFERENCES = Map.ofEntries(
            Map.entry(1L, "COOK COUNTY GENERAL SERVICES"),
            Map.entry(2L, "CHICAGO CENTRAL MUNICIPAL FUND"),
            Map.entry(3L, "NORTH PRAIRIE SCHOOL DISTRICT 901"),
            Map.entry(4L, "LAKEFRONT COMMUNITY COLLEGE DISTRICT"),
            Map.entry(5L, "WEST COOK LIBRARY DISTRICT"),
            Map.entry(6L, "SOUTH CANAL PARK DISTRICT"),
            Map.entry(7L, "DES PLAINES VALLEY SANITARY DISTRICT"),
            Map.entry(11L, "SOUTH SUBURBAN FIRE DISTRICT"),
            Map.entry(12L, "CALUMET REGIONAL TRANSIT DISTRICT"));

    private static final Map<Long, Integer> REVIEWED_TOWNS = Map.ofEntries(
            Map.entry(1L, 10), Map.entry(2L, 12), Map.entry(3L, 13),
            Map.entry(4L, 14), Map.entry(5L, 20), Map.entry(6L, 22),
            Map.entry(7L, 37), Map.entry(11L, 71), Map.entry(12L, 76));

    private static final Map<Integer, String> REVIEWED_TOWN_NAMES = Map.ofEntries(
            Map.entry(1, "BARRINGTON"), Map.entry(3, "BLOOM"),
            Map.entry(4, "BREMEN"), Map.entry(5, "CALUMET"),
            Map.entry(11, "LEYDEN"), Map.entry(13, "MAINE"),
            Map.entry(28, "THORNTON"), Map.entry(32, "JEFFERSON"),
            Map.entry(37, "SOUTH"));

    private final AssessmentParcelRepository parcelRepository;
    private final AssessmentDetailRepository detailRepository;
    private final FrozenValuationRepository frozenRepository;
    private final AgencyEqualizedValuationRepository agencyRepository;
    private final AssessmentDetailValuator detailValuator;
    private final EifdTifIncrementKernel kernel;
    private final EifdTifIncrementOutcomeProjector outcomeProjector;

    @Autowired
    public EifdTifIncrementProcessor(
            AssessmentParcelRepository parcelRepository,
            AssessmentDetailRepository detailRepository,
            FrozenValuationRepository frozenRepository,
            AgencyEqualizedValuationRepository agencyRepository,
            AssessmentDetailValuator detailValuator) {
        this(parcelRepository, detailRepository, frozenRepository, agencyRepository,
                detailValuator, new EifdTifIncrementKernel());
    }

    EifdTifIncrementProcessor(
            AssessmentParcelRepository parcelRepository,
            AssessmentDetailRepository detailRepository,
            FrozenValuationRepository frozenRepository,
            AgencyEqualizedValuationRepository agencyRepository,
            AssessmentDetailValuator detailValuator,
            EifdTifIncrementKernel kernel) {
        this.parcelRepository = parcelRepository;
        this.detailRepository = detailRepository;
        this.frozenRepository = frozenRepository;
        this.agencyRepository = agencyRepository;
        this.detailValuator = detailValuator;
        this.kernel = kernel;
        this.outcomeProjector = new EifdTifIncrementOutcomeProjector();
    }

    public EifdTifIncrementProcessResult process(EifdTifIncrementRunRequest request) {
        ControlRanges controls = kernel.validateControls(
                request.getReassessmentControl(), request.getProcessingYear(),
                request.getAnnualEqualizationFactor());

        List<AssessmentParcel> parcels = new ArrayList<>(parcelRepository.findAllInInputOrder()
                .stream().map(this::eifdProjection).toList());
        parcels.sort(Comparator.comparing(AssessmentParcel::getParcelNumber,
                Comparator.nullsLast(Comparator.naturalOrder())));

        List<AssessmentDetail> details = new ArrayList<>(
                detailRepository.findAll(Pageable.unpaged()).getContent());
        for (AssessmentDetail detail : details) {
            detailValuator.value(detail);
        }
        Map<Long, List<AssessmentDetail>> detailsByParcel = new HashMap<>();
        for (AssessmentDetail detail : details) {
            detailsByParcel.computeIfAbsent(detail.getParcelNumber(), unused -> new ArrayList<>())
                    .add(detail);
        }

        List<FrozenValuation> frozenValues = new ArrayList<>(
                frozenRepository.findAll(Pageable.unpaged()).getContent());
        frozenValues.sort(Comparator.comparing(FrozenValuation::getDivisionNumber));
        List<Asrea740ReportFact> frozenBefore = frozenValues.stream()
                .map(this::reportFact)
                .toList();

        List<Asrea740ReportFact> asrea740ReportFacts = new ArrayList<>(frozenValues.size());
        // ASREA740 commits each matched division before the sequential handoffs begin.
        int frozenUpdated = 0;
        int matchedMasters = Math.min(frozenValues.size(), parcels.size());
        for (int index = 0; index < matchedMasters; index++) {
            FrozenValuation replacement = kernel.accumulateFrozen(
                    frozenValues.get(index), masterDelta(frozenValues.get(index), parcels.get(index)));
            // ASREA740 prints this prepared value before the later repository rewrite.
            asrea740ReportFacts.add(reportFact(replacement));
            frozenRepository.save(replacement);
            frozenValues.set(index, replacement);
            frozenUpdated++;
        }

        List<Selection> selections = select(parcels, frozenValues, detailsByParcel, controls);
        RollupResult rollups = kernel.rollup(selections);
        int recordsRead = frozenValues.size() + parcels.size() + details.size() + rollups.recordsRead();
        int recordsWritten = frozenValues.size();
        int recordsRejected = 0;
        List<Output> outputs = new ArrayList<>();
        outputs.add(new Output(
                "frozenValuationReconciliationReport", frozenValues.size(), null));
        List<Message> messages = new ArrayList<>();
        if (rollups.failed()) {
            messages.add(new Message("ERROR",
                    "Frozen valuation selections are out of sequence at " + rollups.offendingKey() + ".",
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
        recordsWritten += rollups.taxCodeTotals().size() + rollups.divisionTotals().size()
                + percentages.percentages().size();
        recordsRejected += percentages.unmatchedTaxCodeTotals().size();

        Map<String, FrozenValuation> frozenByDivision = new HashMap<>();
        for (FrozenValuation frozen : frozenValues) {
            frozenByDivision.put(frozen.getDivisionNumber(), frozen);
        }
        AllocationResult allocations = kernel.allocate(percentages.percentages(), frozenByDivision);
        recordsRead += percentages.percentages().size();
        recordsWritten += allocations.allocations().size();
        recordsRejected += allocations.unmatchedCount();

        Map<String, TaxCodeMaster> masters = buildReviewedTaxCodeProjection(allocations.allocations());
        FrozenAgencyResult frozenAgencies = kernel.expandFrozenAgencies(
                allocations.allocations(), masters, controls.equalizationFactor());
        recordsRead += frozenAgencies.recordsRead();
        recordsWritten += frozenAgencies.details().size();
        if (frozenAgencies.failed()) {
            messages.add(new Message("ERROR",
                    "No matching tax-code master record for tax code "
                            + frozenAgencies.missingTaxCode() + ".",
                    "asrea748-005"));
            return result(false, recordsRead, recordsWritten, frozenUpdated,
                    recordsRejected + 1, outputs, messages);
        }

        AgencySummaryResult summaries = kernel.summarizeAgencies(
                frozenAgencies.details(), REVIEWED_AGENCY_REFERENCES);
        recordsRead += summaries.recordsRead();
        recordsWritten += summaries.summaries().size();
        if (summaries.failed()) {
            messages.add(new Message("ERROR",
                    "No agency reference description exists for agency "
                            + summaries.offendingAgency() + ".",
                    "clrtm749-003"));
            return result(false, recordsRead, recordsWritten, frozenUpdated,
                    recordsRejected + 1, outputs, messages);
        }

        TownReportResult townReport = kernel.reportTowns(
                frozenAgencies.details(), REVIEWED_TOWN_NAMES);
        recordsRead += frozenAgencies.details().size();
        if (townReport.failed()) {
            messages.add(new Message("ERROR", "Agency-town input is out of sequence.",
                    "clrtm750-001"));
            return result(false, recordsRead, recordsWritten, frozenUpdated,
                    recordsRejected + 1, outputs, messages);
        }

        int agencyReportRecords = summaries.summaries().size() + 5;
        int townReportRecords = townReport.townTotals().isEmpty()
                ? 0 : townReport.townTotals().size() * 8 + 3;
        int appendRecords = townReport.townTotals().size() * 13;
        outputs.add(new Output("agencySummaryReport", agencyReportRecords, null));
        outputs.add(new Output("townWithinAgencyReport", townReportRecords, null));
        outputs.add(new Output("agencyYearAppendRecords", appendRecords, null));
        recordsWritten += agencyReportRecords + townReportRecords + appendRecords;

        List<AgencyEqualizedValuation> agencies = new ArrayList<>(
                agencyRepository.findAll(Pageable.unpaged()).getContent());
        Map<String, AgencyEqualizedValuation> agencyByNumber = new LinkedHashMap<>();
        for (AgencyEqualizedValuation agency : agencies) {
            agencyByNumber.put(agency.getAgencyNumber(), agency);
        }
        PostingResult posting = kernel.postAgencySummaries(summaries.summaries(), agencyByNumber);
        recordsRead += posting.recordsRead();
        recordsRejected += posting.unmatchedAgencies().size();
        int agencyUpdated = 0;
        for (AgencyEqualizedValuation replacement : posting.updated().values()) {
            agencyRepository.save(replacement);
            agencyUpdated++;
        }
        if (posting.failed()) {
            messages.add(new Message("ERROR", "Agency posting input is out of sequence.",
                    "clrtm756-001"));
            return result(false, recordsRead, recordsWritten, frozenUpdated + agencyUpdated,
                    recordsRejected + 1, outputs, messages);
        }
        for (Long unmatched : posting.unmatchedAgencies()) {
            messages.add(new Message("WARNING",
                    "NO MATCHING AGENCY ON AGENCY EQUALIZED VALUATION FILE: " + unmatched,
                    "clrtm756-005"));
        }

        // These are the reviewed CLRTM756 logical report lines. The source's UNMATCHED line
        // intentionally carries the matched-read counter, and is not reinterpreted here.
        messages.add(new Message("INFO",
                "TOTAL NUMBER OF CLERK'S NEW PROPERTY RECORDS READ " + posting.recordsRead(), null));
        messages.add(new Message("INFO",
                "TOTAL NUMBER OF CLERK'S NEW PROPERTY RECORDS UNMATCHED "
                        + posting.updated().size(), null));
        messages.add(new Message("INFO",
                "TOTAL NUMBER OF VSAM AGENCY EQ-VALUATION RECORDS UPDATED "
                        + agencyUpdated, null));
        messages.add(new Message("INFO", "EIFD/TIF increment batch completed successfully.", null));
        EifdTifIncrementProcessResult result = new EifdTifIncrementProcessResult(
                true, 0, recordsRead, recordsWritten, frozenUpdated + agencyUpdated,
                recordsRejected, outputs, messages, asrea740ReportFacts);
        Execution execution = new Execution(
                request, parcels, frozenBefore, frozenValues, rollups, percentages, allocations,
                frozenAgencies, summaries, townReport, agencies, posting);
        return new EifdTifIncrementProcessResult(
                result.completed(), result.returnCode(), result.recordsRead(),
                result.recordsWritten(), result.recordsUpdated(), result.recordsRejected(),
                result.outputs(), result.messages(), result.asrea740ReportFacts(),
                outcomeProjector.project(execution, result));
    }

    private List<Selection> select(
            List<AssessmentParcel> parcels,
            List<FrozenValuation> frozenValues,
            Map<Long, List<AssessmentDetail>> detailsByParcel,
            ControlRanges controls) {
        List<Selection> selections = new ArrayList<>(parcels.size());
        int count = Math.min(parcels.size(), frozenValues.size());
        for (int index = 0; index < count; index++) {
            AssessmentParcel parcel = parcels.get(index);
            FrozenValuation frozen = frozenValues.get(index);
            long agency = Long.parseLong(frozen.getDivisionNumber());
            long current288 = 0L;
            long firstTime = 0L;
            if (REVIEWED_AGENCY_REFERENCES.containsKey(agency)) {
                current288 = value(frozen.getProposedCurrent288Value());
                firstTime = value(frozen.getProposedImprovementValue());
            }
            for (AssessmentDetail detail : detailsByParcel.getOrDefault(parcel.getParcelNumber(), List.of())) {
                int category = category(detail.getDetailType());
                long valuation = detail.getValuation() == null ? 0L : detail.getValuation();
                if (detail.getAssessmentClass() != null && detail.getImprovementYear() != null
                        && kernel.qualifiesCurrent288(category, detail.getAssessmentClass(),
                                detail.getImprovementYear(), controls.processingYear(), false)) {
                    current288 += valuation;
                }
                if (detail.getAge() != null && detail.getAssessmentClass() != null
                        && kernel.qualifiesFirstTime(category, detail.getAge(),
                                detail.getAssessmentClass())) {
                    firstTime += valuation;
                }
            }
            selections.add(new Selection(
                    frozen.getDivisionNumber(),
                    String.format("%05d", parcel.getTaxCode() == null ? 0 : parcel.getTaxCode()),
                    current288, firstTime, REVIEWED_AGENCY_REFERENCES.containsKey(agency)
                            ? value(frozen.getProposedExpired288Value()) : 0L));
        }
        return selections;
    }

    private Map<String, TaxCodeMaster> buildReviewedTaxCodeProjection(
            List<AllocatedFrozen> allocations) {
        Map<String, TaxCodeMaster> masters = new HashMap<>();
        for (AllocatedFrozen allocation : allocations) {
            long agency = Long.parseLong(allocation.division());
            Integer town = REVIEWED_TOWNS.get(agency);
            if (town == null || !REVIEWED_AGENCY_REFERENCES.containsKey(agency)) {
                continue;
            }
            List<Long> positions = new ArrayList<>(40);
            positions.add(agency);
            while (positions.size() < 40) {
                positions.add(0L);
            }
            masters.put(allocation.taxCode(),
                    new TaxCodeMaster(allocation.taxCode(), BigDecimal.ZERO, town, positions));
        }
        return masters;
    }

    private int category(String detailType) {
        if (detailType == null || detailType.isBlank()) {
            return 0;
        }
        char first = detailType.charAt(0);
        return Character.isDigit(first) ? first - '0' : 0;
    }

    private AssessmentParcel eifdProjection(AssessmentParcel source) {
        AssessmentParcel projected = new AssessmentParcel();
        projected.setParcelNumber(source.getParcelNumber());
        projected.setTaxCode(source.getTaxCode());
        projected.setTaxType(source.getTaxType());
        projected.setParcelStatus(source.getParcelStatus());
        projected.setPriorParcelStatus(source.getPriorParcelStatus());
        projected.setPriorLandValue(source.getEifdPriorLandValue());
        projected.setPriorImprovementValue(source.getEifdPriorImprovementValue());
        projected.setPriorTotalValue(source.getEifdPriorTotalValue());
        projected.setCurrentLandValue(source.getEifdCurrentLandValue());
        projected.setCurrentImprovementValue(source.getEifdCurrentImprovementValue());
        projected.setCurrentTotalValue(source.getEifdCurrentTotalValue());
        return projected;
    }

    private FrozenValuation masterDelta(FrozenValuation stored, AssessmentParcel parcel) {
        boolean exemptTransition = fullyExemptPriorBecameNonexempt(parcel);
        String division = stored.getDivisionNumber();
        return kernel.selectActionTotals(new DivisionActionInput(
                division,
                division.substring(0, 4),
                value(parcel.getPriorLandValue()),
                value(parcel.getPriorImprovementValue()),
                value(parcel.getPriorTotalValue()),
                1L,
                value(parcel.getCurrentLandValue()),
                value(parcel.getCurrentImprovementValue()),
                value(parcel.getCurrentTotalValue()),
                1L,
                0L,
                0L,
                0L,
                0L,
                permitChanged(parcel),
                exemptTransition));
    }

    private boolean fullyExemptPriorBecameNonexempt(AssessmentParcel parcel) {
        return "1".equals(parcel.getPriorParcelStatus())
                && parcel.getParcelStatus() != null
                && !"1".equals(parcel.getParcelStatus());
    }

    private boolean permitChanged(AssessmentParcel parcel) {
        // The reviewed MASTNEW layout overlays this shared master field as M-PERMIND.
        return "2".equals(parcel.getTaxType()) || "3".equals(parcel.getTaxType());
    }

    private Asrea740ReportFact reportFact(FrozenValuation value) {
        return new Asrea740ReportFact(
                value.getDivisionNumber(),
                value(value.getPriorLandValue()),
                value(value.getPriorImprovementValue()),
                value(value.getPriorTotalValue()),
                value(value.getPriorParcelCount()),
                value(value.getCurrentLandValue()),
                value(value.getCurrentImprovementValue()),
                value(value.getCurrentTotalValue()),
                value(value.getCurrentParcelCount()),
                value(value.getProposedImprovementValue()),
                value(value.getProposedExpired288Value()),
                value(value.getProposedCurrent288Value()),
                value(value.getProposedTotalValue()),
                value(value.getProposedActualValue()),
                value(value.getChangeActionPriorLandValue()),
                value(value.getChangeActionPriorImprovementValue()),
                value(value.getChangeActionPriorTotalValue()),
                value(value.getChangeActionPriorParcelCount()),
                value(value.getChangeActionCurrentLandValue()),
                value(value.getChangeActionCurrentImprovementValue()),
                value(value.getChangeActionCurrentTotalValue()),
                value(value.getChangeActionCurrentParcelCount()),
                value(value.getNoChangeActionPriorLandValue()),
                value(value.getNoChangeActionPriorImprovementValue()),
                value(value.getNoChangeActionPriorTotalValue()),
                value(value.getNoChangeActionPriorParcelCount()),
                value(value.getNoChangeActionCurrentLandValue()),
                value(value.getNoChangeActionCurrentImprovementValue()),
                value(value.getNoChangeActionCurrentTotalValue()),
                value(value.getNoChangeActionCurrentParcelCount()));
    }

    private EifdTifIncrementProcessResult result(
            boolean completed,
            int recordsRead,
            int recordsWritten,
            int recordsUpdated,
            int recordsRejected,
            List<Output> outputs,
            List<Message> messages) {
        EifdTifIncrementProcessResult result = new EifdTifIncrementProcessResult(
                completed, completed ? 0 : 16, recordsRead, recordsWritten,
                recordsUpdated, recordsRejected, outputs, messages);
        return completed ? result : new EifdTifIncrementProcessResult(
                result.completed(), result.returnCode(), result.recordsRead(),
                result.recordsWritten(), result.recordsUpdated(), result.recordsRejected(),
                result.outputs(), result.messages(), outcomeProjector.failed(result));
    }

    private long value(Long value) {
        return value == null ? 0L : value;
    }
}
