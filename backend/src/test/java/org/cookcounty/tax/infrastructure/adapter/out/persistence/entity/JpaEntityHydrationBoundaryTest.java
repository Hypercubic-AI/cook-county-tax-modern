package org.cookcounty.tax.infrastructure.adapter.out.persistence.entity;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JpaEntityHydrationBoundaryTest {
    @ParameterizedTest(name = "{0}")
    @MethodSource("requiredReads")
    void requiredReadsRejectUnhydratedEntities(Supplier<?> read) {
        assertThrows(NullPointerException.class, read::get);
    }

    private static Stream<Arguments> requiredReads() {
        return Stream.of(
                requiredRead(
                        "senior freeze applicant age", new SeniorFreezeApplicantEntity()::getAge),
                requiredRead(
                        "senior freeze master base-value year",
                        new SeniorFreezeMasterEntity()::getBaseValueYear),
                requiredRead(
                        "maintained homestead application year",
                        new MaintainedHomesteadExemptionEntity()::getApplicationYear),
                requiredRead(
                        "homeowner exemption application year",
                        new HomeownerExemptionEntity()::getApplicationYear),
                requiredRead(
                        "homeowner master application year",
                        new HomeownerMasterEntity()::getApplicationYear),
                requiredRead(
                        "tax-rate equalized-value source order",
                        new TaxRateEqualizedValueEntity()::getSourceOrder),
                requiredRead(
                        "property-tax renewal source order",
                        new PropertyTaxRenewalEntity()::getSourceOrder),
                requiredRead(
                        "tax-rate division source order",
                        new TaxRateDivisionEntity()::getSourceOrder));
    }

    private static Arguments requiredRead(String name, Supplier<?> read) {
        return Arguments.of(Named.of(name, read));
    }
}
