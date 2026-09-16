package org.cookcounty.tax.application.service;

import org.cookcounty.tax.domain.model.AssessmentDetail;
import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Set;

/// Shared source-order and whole-unit operations for assessed-value stages.
final class AssessedValueRuleSupport {
    private static final Set<Integer> QUESTIONNAIRE_BEARING_CLASSES =
            Set.of(
                    202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 234, 278, 295, 402, 403,
                    404, 405, 406, 407, 408, 409, 410, 411, 412, 434, 478, 495);

    private static final Set<Integer> RESIDENTIAL_BUCKET_CLASSES =
            Set.of(
                    202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213, 234, 236, 278, 294,
                    295, 297);

    private AssessedValueRuleSupport() {}

    static boolean isQuestionnaireBearing(AssessmentDetail detail) {
        return isImprovement(detail)
                && QUESTIONNAIRE_BEARING_CLASSES.contains(detail.assessmentClass());
    }

    static boolean isType5ResidentialQuestionnaireBearing(AssessmentDetail detail) {
        if (!isImprovement(detail) || detail.assessmentClass() == null) {
            return false;
        }
        int minorClass = Math.floorMod(detail.assessmentClass(), 100);
        return (minorClass >= 2 && minorClass <= 12)
                || minorClass == 34
                || minorClass == 78
                || minorClass == 95;
    }

    static boolean isResidentialBucketClass(AssessmentDetail detail) {
        return isImprovement(detail)
                && RESIDENTIAL_BUCKET_CLASSES.contains(detail.assessmentClass());
    }

    static boolean isImprovement(AssessmentDetail detail) {
        String type = detail.detailType();
        return "2".equals(type) || "3".equals(type) || "4".equals(type) || "5".equals(type);
    }

    /// Reads the township prefix from the signed five-digit source tax code.
    static int township(AssessmentParcel parcel) {
        return Math.abs(Integer.parseInt(parcel.taxCode())) / 1_000;
    }

    static String recordKey(AssessmentParcel parcel) {
        return township(parcel)
                + "/"
                + parcel.volumeNumber()
                + "/"
                + parcel.parcelNumber()
                + "/"
                + parcel.taxType();
    }

    static int compareOverallOrder(AssessmentParcel left, AssessmentParcel right) {
        int comparison = Integer.compare(township(left), township(right));
        if (comparison != 0) {
            return comparison;
        }
        comparison =
                Long.compare(
                        sourceInteger(left.volumeNumber()), sourceInteger(right.volumeNumber()));
        if (comparison != 0) {
            return comparison;
        }
        comparison =
                Long.compare(
                        sourceInteger(left.parcelNumber()), sourceInteger(right.parcelNumber()));
        if (comparison != 0) {
            return comparison;
        }
        return left.taxType().compareTo(right.taxType());
    }

    static int compareBucketingOrder(AssessmentParcel left, AssessmentParcel right) {
        int comparison = Integer.compare(township(left), township(right));
        if (comparison != 0) {
            return comparison;
        }
        comparison =
                Long.compare(
                        sourceInteger(left.volumeNumber()), sourceInteger(right.volumeNumber()));
        if (comparison != 0) {
            return comparison;
        }
        return Long.compare(
                sourceInteger(left.parcelNumber()), sourceInteger(right.parcelNumber()));
    }

    static int orZero(@Nullable Integer value) {
        return value == null ? 0 : value;
    }

    static long orZero(@Nullable Long value) {
        return value == null ? 0L : value;
    }

    /// Supplies the source calculation's zero value for an absent exact decimal.
    static BigDecimal orZero(@Nullable BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /// Parses canonical source decimal text only where numeric ordering or encoding requires it.
    static long sourceInteger(String value) {
        return Long.parseLong(value);
    }
}
