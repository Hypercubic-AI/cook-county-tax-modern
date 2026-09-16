package org.cookcounty.tax.application.service;

import org.cookcounty.tax.domain.model.AssessmentDetail;
import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/// Maps the Assessor class to a Clerk major digit and accumulates whole-dollar value buckets.
///
/// Details remain in source order. A qualifying improvement owns the immediately following
/// questionnaire occurrence, which neither presence detection nor accumulation processes.
@Component
public final class AssessedValueBucketingKernel {

    /// Complete Assessor-to-Clerk major-class cross-reference, keyed by assessment class. Unknown
    /// classes use the separately documented major-digit fallback.
    private static final Map<Integer, String> CLERK_CLASS_BY_ASSESSOR_CLASS =
            Map.<Integer, String>ofEntries(
                    Map.entry(100, "2"),
                    Map.entry(101, "2"),
                    Map.entry(190, "2"),
                    Map.entry(197, "2"),
                    Map.entry(200, "2"),
                    Map.entry(201, "2"),
                    Map.entry(202, "2"),
                    Map.entry(203, "2"),
                    Map.entry(204, "2"),
                    Map.entry(205, "2"),
                    Map.entry(206, "2"),
                    Map.entry(207, "2"),
                    Map.entry(208, "2"),
                    Map.entry(209, "2"),
                    Map.entry(210, "2"),
                    Map.entry(211, "2"),
                    Map.entry(212, "2"),
                    Map.entry(213, "2"),
                    Map.entry(218, "2"),
                    Map.entry(219, "2"),
                    Map.entry(220, "2"),
                    Map.entry(221, "2"),
                    Map.entry(224, "2"),
                    Map.entry(225, "2"),
                    Map.entry(234, "2"),
                    Map.entry(236, "2"),
                    Map.entry(239, "2"),
                    Map.entry(240, "2"),
                    Map.entry(241, "2"),
                    Map.entry(278, "2"),
                    Map.entry(288, "2"),
                    Map.entry(290, "2"),
                    Map.entry(294, "2"),
                    Map.entry(295, "2"),
                    Map.entry(297, "2"),
                    Map.entry(299, "2"),
                    Map.entry(300, "3"),
                    Map.entry(301, "2"),
                    Map.entry(313, "3"),
                    Map.entry(314, "3"),
                    Map.entry(315, "3"),
                    Map.entry(318, "3"),
                    Map.entry(319, "3"),
                    Map.entry(320, "3"),
                    Map.entry(321, "3"),
                    Map.entry(390, "3"),
                    Map.entry(391, "3"),
                    Map.entry(396, "3"),
                    Map.entry(397, "3"),
                    Map.entry(399, "2"),
                    Map.entry(400, "2"),
                    Map.entry(401, "2"),
                    Map.entry(402, "2"),
                    Map.entry(403, "2"),
                    Map.entry(404, "2"),
                    Map.entry(405, "2"),
                    Map.entry(406, "2"),
                    Map.entry(407, "2"),
                    Map.entry(408, "2"),
                    Map.entry(409, "2"),
                    Map.entry(410, "2"),
                    Map.entry(411, "2"),
                    Map.entry(412, "2"),
                    Map.entry(413, "2"),
                    Map.entry(414, "3"),
                    Map.entry(415, "3"),
                    Map.entry(416, "3"),
                    Map.entry(417, "3"),
                    Map.entry(418, "3"),
                    Map.entry(419, "3"),
                    Map.entry(420, "3"),
                    Map.entry(421, "3"),
                    Map.entry(422, "3"),
                    Map.entry(423, "3"),
                    Map.entry(424, "3"),
                    Map.entry(425, "3"),
                    Map.entry(426, "3"),
                    Map.entry(427, "3"),
                    Map.entry(428, "3"),
                    Map.entry(429, "3"),
                    Map.entry(430, "3"),
                    Map.entry(431, "3"),
                    Map.entry(432, "3"),
                    Map.entry(433, "3"),
                    Map.entry(434, "3"),
                    Map.entry(435, "3"),
                    Map.entry(436, "3"),
                    Map.entry(439, "3"),
                    Map.entry(440, "3"),
                    Map.entry(441, "3"),
                    Map.entry(450, "5"),
                    Map.entry(478, "3"),
                    Map.entry(480, "5"),
                    Map.entry(481, "5"),
                    Map.entry(483, "5"),
                    Map.entry(487, "5"),
                    Map.entry(488, "3"),
                    Map.entry(489, "5"),
                    Map.entry(490, "3"),
                    Map.entry(491, "3"),
                    Map.entry(492, "3"),
                    Map.entry(493, "5"),
                    Map.entry(494, "2"),
                    Map.entry(495, "2"),
                    Map.entry(496, "2"),
                    Map.entry(497, "3"),
                    Map.entry(499, "3"),
                    Map.entry(500, "5"),
                    Map.entry(501, "2"),
                    Map.entry(516, "3"),
                    Map.entry(517, "3"),
                    Map.entry(522, "3"),
                    Map.entry(523, "3"),
                    Map.entry(526, "3"),
                    Map.entry(527, "3"),
                    Map.entry(528, "3"),
                    Map.entry(529, "3"),
                    Map.entry(530, "3"),
                    Map.entry(531, "3"),
                    Map.entry(532, "3"),
                    Map.entry(533, "3"),
                    Map.entry(535, "3"),
                    Map.entry(550, "5"),
                    Map.entry(580, "5"),
                    Map.entry(581, "5"),
                    Map.entry(583, "5"),
                    Map.entry(587, "5"),
                    Map.entry(589, "5"),
                    Map.entry(590, "5"),
                    Map.entry(591, "3"),
                    Map.entry(592, "3"),
                    Map.entry(593, "5"),
                    Map.entry(597, "5"),
                    Map.entry(599, "3"),
                    Map.entry(600, "5"),
                    Map.entry(601, "2"),
                    Map.entry(633, "3"),
                    Map.entry(637, "3"),
                    Map.entry(638, "3"),
                    Map.entry(650, "5"),
                    Map.entry(651, "5"),
                    Map.entry(654, "3"),
                    Map.entry(655, "3"),
                    Map.entry(663, "5"),
                    Map.entry(666, "3"),
                    Map.entry(668, "3"),
                    Map.entry(669, "3"),
                    Map.entry(670, "5"),
                    Map.entry(671, "5"),
                    Map.entry(673, "5"),
                    Map.entry(677, "5"),
                    Map.entry(679, "5"),
                    Map.entry(680, "5"),
                    Map.entry(681, "5"),
                    Map.entry(683, "5"),
                    Map.entry(687, "5"),
                    Map.entry(689, "5"),
                    Map.entry(690, "5"),
                    Map.entry(693, "5"),
                    Map.entry(697, "5"),
                    Map.entry(699, "5"),
                    Map.entry(700, "3"),
                    Map.entry(701, "3"),
                    Map.entry(716, "3"),
                    Map.entry(717, "3"),
                    Map.entry(722, "3"),
                    Map.entry(723, "3"),
                    Map.entry(726, "3"),
                    Map.entry(727, "3"),
                    Map.entry(728, "3"),
                    Map.entry(729, "3"),
                    Map.entry(730, "3"),
                    Map.entry(731, "3"),
                    Map.entry(732, "3"),
                    Map.entry(733, "3"),
                    Map.entry(735, "3"),
                    Map.entry(742, "3"),
                    Map.entry(743, "3"),
                    Map.entry(745, "3"),
                    Map.entry(746, "3"),
                    Map.entry(747, "3"),
                    Map.entry(748, "3"),
                    Map.entry(752, "3"),
                    Map.entry(753, "3"),
                    Map.entry(756, "3"),
                    Map.entry(757, "3"),
                    Map.entry(758, "3"),
                    Map.entry(760, "3"),
                    Map.entry(761, "3"),
                    Map.entry(762, "3"),
                    Map.entry(764, "3"),
                    Map.entry(765, "3"),
                    Map.entry(767, "3"),
                    Map.entry(772, "3"),
                    Map.entry(774, "3"),
                    Map.entry(790, "3"),
                    Map.entry(791, "3"),
                    Map.entry(792, "3"),
                    Map.entry(797, "3"),
                    Map.entry(798, "3"),
                    Map.entry(799, "3"),
                    Map.entry(800, "3"),
                    Map.entry(801, "3"),
                    Map.entry(816, "3"),
                    Map.entry(817, "3"),
                    Map.entry(822, "3"),
                    Map.entry(823, "3"),
                    Map.entry(826, "3"),
                    Map.entry(827, "3"),
                    Map.entry(828, "3"),
                    Map.entry(829, "3"),
                    Map.entry(830, "3"),
                    Map.entry(831, "3"),
                    Map.entry(832, "3"),
                    Map.entry(833, "3"),
                    Map.entry(835, "3"),
                    Map.entry(850, "5"),
                    Map.entry(880, "5"),
                    Map.entry(881, "5"),
                    Map.entry(883, "5"),
                    Map.entry(887, "5"),
                    Map.entry(889, "5"),
                    Map.entry(890, "3"),
                    Map.entry(891, "3"),
                    Map.entry(892, "3"),
                    Map.entry(893, "5"),
                    Map.entry(897, "3"),
                    Map.entry(899, "3"),
                    Map.entry(900, "3"),
                    Map.entry(901, "2"),
                    Map.entry(913, "3"),
                    Map.entry(914, "3"),
                    Map.entry(915, "3"),
                    Map.entry(918, "3"),
                    Map.entry(919, "3"),
                    Map.entry(920, "3"),
                    Map.entry(921, "3"),
                    Map.entry(959, "3"),
                    Map.entry(990, "3"),
                    Map.entry(991, "3"),
                    Map.entry(996, "3"),
                    Map.entry(997, "3"));

    /// Residential improvements must exceed this whole-dollar value to select the homeowner bucket.
    private static final BigDecimal RESIDENTIAL_VALUE_THRESHOLD = new BigDecimal("1000");

    /// Class-299 improvements must exceed this whole-dollar value to select the homeowner bucket.
    private static final BigDecimal CLASS_299_VALUE_THRESHOLD = new BigDecimal("1499");

    /// Returns a complete parcel with the accepted bucket slots replaced.
    ///
    /// @param parcel parcel whose overall class selects the bucket policy
    /// @param details source-ordered details, including questionnaire companions
    /// @return replacement parcel, report totals, change state, and recoverable observations
    public Result bucket(AssessmentParcel parcel, List<AssessmentDetail> details) {
        List<AssessedValueRuleMessage> messages = new ArrayList<>();
        int overallClass = AssessedValueRuleSupport.orZero(parcel.overallClass());
        String clerkClass = CLERK_CLASS_BY_ASSESSOR_CLASS.get(overallClass);
        if (clerkClass == null) {
            clerkClass = Integer.toString(Math.abs(overallClass) / 100 % 10);
            if (overallClass != 0) {
                messages.add(
                        new AssessedValueRuleMessage(
                                "WARNING",
                                "CLERK_CLASS_MISS",
                                "asrea151-002",
                                "The overall class was absent from the Clerk cross-reference; the"
                                        + " major digit was used.",
                                AssessedValueRuleSupport.recordKey(parcel)));
            }
        }
        AssessmentParcel bucketed;

        Presence presence = findPresence(details);
        BigDecimal farm = BigDecimal.ZERO;
        BigDecimal homeowner = BigDecimal.ZERO;
        BigDecimal nonHomeowner = BigDecimal.ZERO;
        for (int index = 0; index < details.size(); index++) {
            AssessmentDetail detail = details.get(index);
            int assessmentClass = AssessedValueRuleSupport.orZero(detail.assessmentClass());
            BigDecimal value = detail.valuation();
            if (presence.class239Land()
                    && (("1".equals(detail.detailType()) && assessmentClass == 239)
                            || (AssessedValueRuleSupport.isImprovement(detail)
                                    && assessmentClass == 224))) {
                farm = farm.add(value);
            } else if (overallClass / 100 == 2
                    && presence.class200Land()
                    && (presence.qualifyingResidential() || presence.qualifying299())) {
                homeowner = homeowner.add(value);
            } else {
                nonHomeowner = nonHomeowner.add(value);
            }
            if (AssessedValueRuleSupport.isQuestionnaireBearing(detail)) {
                index++;
            }
        }

        bucketed = parcel.withBucketing(clerkClass, farm, homeowner.add(nonHomeowner));
        return new Result(
                bucketed,
                !parcel.equals(bucketed),
                farm,
                homeowner,
                nonHomeowner,
                List.copyOf(messages));
    }

    /// Detects the parcel-wide conditions that control farm and homeowner accumulation.
    private static Presence findPresence(List<AssessmentDetail> details) {
        boolean class239Land = false;
        boolean class200Land = false;
        boolean qualifyingResidential = false;
        boolean qualifying299 = false;
        for (int index = 0; index < details.size(); index++) {
            AssessmentDetail detail = details.get(index);
            int assessmentClass = AssessedValueRuleSupport.orZero(detail.assessmentClass());
            BigDecimal value = detail.valuation();
            if ("1".equals(detail.detailType()) && assessmentClass == 239) {
                class239Land = true;
            } else if ("1".equals(detail.detailType()) && assessmentClass == 200) {
                class200Land = true;
            } else if (AssessedValueRuleSupport.isResidentialBucketClass(detail)
                    && value.compareTo(RESIDENTIAL_VALUE_THRESHOLD) > 0) {
                qualifyingResidential = true;
            } else if (AssessedValueRuleSupport.isImprovement(detail)
                    && assessmentClass == 299
                    && value.compareTo(CLASS_299_VALUE_THRESHOLD) > 0) {
                qualifying299 = true;
            }
            if (AssessedValueRuleSupport.isQuestionnaireBearing(detail)) {
                index++;
            }
        }
        return new Presence(class239Land, class200Land, qualifyingResidential, qualifying299);
    }

    /// Bucketing result with the complete replacement parcel.
    ///
    /// @param parcel replacement parcel after bucketing
    /// @param changed whether any bucketed parcel field changed
    /// @param farmValue accumulated farm value in whole dollars
    /// @param homeownerValue accumulated homeowner value in whole dollars
    /// @param nonHomeownerValue accumulated non-homeowner value in whole dollars
    /// @param messages recoverable class-mapping observations
    public record Result(
            AssessmentParcel parcel,
            boolean changed,
            BigDecimal farmValue,
            BigDecimal homeownerValue,
            BigDecimal nonHomeownerValue,
            List<AssessedValueRuleMessage> messages) {
        /// Defensively copies the observation list.
        public Result {
            messages = List.copyOf(messages);
        }
    }

    private record Presence(
            boolean class239Land,
            boolean class200Land,
            boolean qualifyingResidential,
            boolean qualifying299) {}
}
