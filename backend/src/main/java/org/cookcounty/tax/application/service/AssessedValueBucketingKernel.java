package org.cookcounty.tax.application.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cookcounty.tax.domain.model.AssessmentDetail;
import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.springframework.stereotype.Component;

/** Parcel-local ASREA151 Clerk-class mapping and valuation bucketing policy. */
@Component
public class AssessedValueBucketingKernel {

    private static final String CLASS_TABLE =
            "2100210121902197" +
            "2200220122022203" +
            "2204220522062207" +
            "2208220922102211" +
            "2212221322242225" +
            "2234223622392240" +
            "2241227822882290" +
            "2294229522972299" +
            "2301239924002401" +
            "2402240324042405" +
            "2406240724082409" +
            "2410241124122413" +
            "2494249524962501" +
            "2601221822192220" +
            "2221548736373638" +
            "3654365536663668" +
            "3669374237433745" +
            "3746374737483752" +
            "3753375637573758" +
            "3760376137623764" +
            "3765376737723774" +
            "3798" +
            "3300331333143315" +
            "3318331933203321" +
            "3390339133963397" +
            "3414341534163417" +
            "3418341934203421" +
            "3422342334243425" +
            "3426342734283429" +
            "3430343134323433" +
            "3434343534363439" +
            "3440344134783488" +
            "3490349134923497" +
            "3499351635173522" +
            "3523352635273528" +
            "3529353035313532" +
            "35333535" +
            "3591359235993633" +
            "5450548054815483" +
            "5489" +
            "5493550055505580" +
            "5581558355875589" +
            "559055935597" +
            "5600568056815683" +
            "56875689" +
            "5690569356975699" +
            "5650565156635670" +
            "5671567356775679" +
            "3700370137163717" +
            "3722372337263727" +
            "3728372937303731" +
            "3732373337353790" +
            "3791379237973799" +
            "3800380138163817" +
            "3822382338263827" +
            "3828382938303831" +
            "3832383338353890" +
            "3891389238973899" +
            "5850588058815883" +
            "5887588958933900" +
            "2901391339143915" +
            "3918391939203921" +
            "3959399039913996" +
            "3997";

    private static final Map<Integer, String> CLERK_CLASS_BY_ASSESSOR_CLASS = createClassTable();

    public Result bucket(AssessmentParcel parcel, List<AssessmentDetail> details) {
        ParcelState before = ParcelState.capture(parcel);
        List<AssessedValueRuleMessage> messages = new ArrayList<>();
        int overallClass = AssessedValueRuleSupport.orZero(parcel.getOverallClass());
        String clerkClass = CLERK_CLASS_BY_ASSESSOR_CLASS.get(overallClass);
        if (clerkClass == null) {
            clerkClass = Integer.toString(Math.abs(overallClass) / 100 % 10);
            if (overallClass != 0) {
                messages.add(new AssessedValueRuleMessage(
                        "WARNING", "CLERK_CLASS_MISS", "asrea151-002",
                        "The overall class was absent from the Clerk cross-reference; the major digit was used.",
                        AssessedValueRuleSupport.recordKey(parcel)));
            }
        }
        parcel.setClerkMajorClass(clerkClass);

        Presence presence = findPresence(details);
        long farm = 0L;
        long homeowner = 0L;
        long nonHomeowner = 0L;
        for (int index = 0; index < details.size(); index++) {
            AssessmentDetail detail = details.get(index);
            int assessmentClass = AssessedValueRuleSupport.orZero(detail.getAssessmentClass());
            long value = AssessedValueRuleSupport.value(detail);
            if (presence.class239Land()
                    && (("1".equals(detail.getDetailType()) && assessmentClass == 239)
                    || (AssessedValueRuleSupport.isImprovement(detail) && assessmentClass == 224))) {
                farm += value;
            } else if (overallClass / 100 == 2
                    && presence.class200Land()
                    && (presence.qualifyingResidential() || presence.qualifying299())) {
                homeowner += value;
            } else {
                nonHomeowner += value;
            }
            if (AssessedValueRuleSupport.isQuestionnaireBearing(detail)) {
                index++;
            }
        }

        parcel.setPriorTotalValue(0L);
        parcel.setCurrentLandValue(0L);
        parcel.setFarmValue(farm);
        parcel.setCombinedHomeownerNonHomeownerValue(homeowner + nonHomeowner);
        parcel.setArchivedPreConversionProposedTotal(0L);
        return new Result(
                !before.equals(ParcelState.capture(parcel)), farm, homeowner, nonHomeowner,
                List.copyOf(messages));
    }

    private static Presence findPresence(List<AssessmentDetail> details) {
        boolean class239Land = false;
        boolean class200Land = false;
        boolean qualifyingResidential = false;
        boolean qualifying299 = false;
        for (int index = 0; index < details.size(); index++) {
            AssessmentDetail detail = details.get(index);
            int assessmentClass = AssessedValueRuleSupport.orZero(detail.getAssessmentClass());
            long value = AssessedValueRuleSupport.value(detail);
            if ("1".equals(detail.getDetailType()) && assessmentClass == 239) {
                class239Land = true;
            } else if ("1".equals(detail.getDetailType()) && assessmentClass == 200) {
                class200Land = true;
            } else if (AssessedValueRuleSupport.isResidentialBucketClass(detail) && value > 1_000L) {
                qualifyingResidential = true;
            } else if (AssessedValueRuleSupport.isImprovement(detail)
                    && assessmentClass == 299 && value > 1_499L) {
                qualifying299 = true;
            }
            if (AssessedValueRuleSupport.isQuestionnaireBearing(detail)) {
                index++;
            }
        }
        return new Presence(class239Land, class200Land, qualifyingResidential, qualifying299);
    }

    private static Map<Integer, String> createClassTable() {
        if (CLASS_TABLE.length() != 242 * 4) {
            throw new IllegalStateException("ASREA151 class table must contain 242 entries");
        }
        Map<Integer, String> result = new HashMap<>(242);
        for (int offset = 0; offset < CLASS_TABLE.length(); offset += 4) {
            result.put(
                    Integer.parseInt(CLASS_TABLE.substring(offset + 1, offset + 4)),
                    CLASS_TABLE.substring(offset, offset + 1));
        }
        return Map.copyOf(result);
    }

    public record Result(
            boolean changed,
            long farmValue,
            long homeownerValue,
            long nonHomeownerValue,
            List<AssessedValueRuleMessage> messages) {}

    private record Presence(
            boolean class239Land,
            boolean class200Land,
            boolean qualifyingResidential,
            boolean qualifying299) {}

    private record ParcelState(
            String clerkMajorClass,
            Long priorTotalValue,
            Long currentLandValue,
            Long farmValue,
            Long combinedHomeownerNonHomeownerValue,
            Long archivedPreConversionProposedTotal) {

        static ParcelState capture(AssessmentParcel parcel) {
            return new ParcelState(
                    parcel.getClerkMajorClass(), parcel.getPriorTotalValue(), parcel.getCurrentLandValue(),
                    parcel.getFarmValue(), parcel.getCombinedHomeownerNonHomeownerValue(),
                    parcel.getArchivedPreConversionProposedTotal());
        }
    }
}
