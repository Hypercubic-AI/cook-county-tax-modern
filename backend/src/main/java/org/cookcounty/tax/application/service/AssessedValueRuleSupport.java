package org.cookcounty.tax.application.service;

import java.util.Set;

import org.cookcounty.tax.domain.model.AssessmentDetail;
import org.cookcounty.tax.domain.model.AssessmentParcel;

final class AssessedValueRuleSupport {

    private static final Set<Integer> QUESTIONNAIRE_BEARING_CLASSES = Set.of(
            202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 234, 278, 295,
            402, 403, 404, 405, 406, 407, 408, 409, 410, 411, 412, 434, 478, 495);

    private static final Set<Integer> RESIDENTIAL_BUCKET_CLASSES = Set.of(
            202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213,
            234, 236, 278, 294, 295, 297);

    private AssessedValueRuleSupport() {}

    static boolean isQuestionnaireBearing(AssessmentDetail detail) {
        return isImprovement(detail) && QUESTIONNAIRE_BEARING_CLASSES.contains(detail.getAssessmentClass());
    }

    static boolean isType5ResidentialQuestionnaireBearing(AssessmentDetail detail) {
        if (!isImprovement(detail) || detail.getAssessmentClass() == null) {
            return false;
        }
        int minorClass = Math.floorMod(detail.getAssessmentClass(), 100);
        return (minorClass >= 2 && minorClass <= 12)
                || minorClass == 34
                || minorClass == 78
                || minorClass == 95;
    }

    static boolean isResidentialBucketClass(AssessmentDetail detail) {
        return isImprovement(detail) && RESIDENTIAL_BUCKET_CLASSES.contains(detail.getAssessmentClass());
    }

    static boolean isImprovement(AssessmentDetail detail) {
        String type = detail.getDetailType();
        return "2".equals(type) || "3".equals(type) || "4".equals(type) || "5".equals(type);
    }

    static int township(AssessmentParcel parcel) {
        Integer taxCode = parcel.getTaxCode();
        return taxCode == null ? 0 : Math.abs(taxCode) / 1_000;
    }

    static String recordKey(AssessmentParcel parcel) {
        return township(parcel) + "/" + orZero(parcel.getVolumeNumber()) + "/"
                + orZero(parcel.getParcelNumber()) + "/" + nullToEmpty(parcel.getTaxType());
    }

    static int compareOverallOrder(AssessmentParcel left, AssessmentParcel right) {
        int comparison = Integer.compare(township(left), township(right));
        if (comparison != 0) {
            return comparison;
        }
        comparison = Integer.compare(orZero(left.getVolumeNumber()), orZero(right.getVolumeNumber()));
        if (comparison != 0) {
            return comparison;
        }
        comparison = Long.compare(orZero(left.getParcelNumber()), orZero(right.getParcelNumber()));
        if (comparison != 0) {
            return comparison;
        }
        return nullToEmpty(left.getTaxType()).compareTo(nullToEmpty(right.getTaxType()));
    }

    static int compareBucketingOrder(AssessmentParcel left, AssessmentParcel right) {
        int comparison = Integer.compare(township(left), township(right));
        if (comparison != 0) {
            return comparison;
        }
        comparison = Integer.compare(orZero(left.getVolumeNumber()), orZero(right.getVolumeNumber()));
        if (comparison != 0) {
            return comparison;
        }
        return Long.compare(orZero(left.getParcelNumber()), orZero(right.getParcelNumber()));
    }

    static long value(AssessmentDetail detail) {
        return detail.getValuation() == null ? 0L : detail.getValuation();
    }

    static int orZero(Integer value) {
        return value == null ? 0 : value;
    }

    static long orZero(Long value) {
        return value == null ? 0L : value;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
