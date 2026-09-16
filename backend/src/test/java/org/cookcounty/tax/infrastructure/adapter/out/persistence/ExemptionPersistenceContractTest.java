package org.cookcounty.tax.infrastructure.adapter.out.persistence;

import static com.google.common.truth.Truth.assertThat;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.cookcounty.tax.domain.model.HomeownerExemption;
import org.cookcounty.tax.domain.model.HomeownerMaster;
import org.cookcounty.tax.domain.model.MaintainedHomesteadExemption;
import org.cookcounty.tax.domain.model.SeniorFreezeApplicant;
import org.cookcounty.tax.domain.model.SeniorFreezeMaster;
import org.cookcounty.tax.domain.port.out.HomeownerExemptionRepository;
import org.cookcounty.tax.domain.port.out.HomeownerMasterRepository;
import org.cookcounty.tax.domain.port.out.MaintainedHomesteadExemptionRepository;
import org.cookcounty.tax.domain.port.out.SeniorFreezeApplicantRepository;
import org.cookcounty.tax.domain.port.out.SeniorFreezeMasterRepository;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Supplier;

class ExemptionPersistenceContractTest extends PostgreSqlIntegrationTestSupport {
    @Autowired private HomeownerMasterRepository homeownerMasters;
    @Autowired private HomeownerExemptionRepository homeownerExemptions;
    @Autowired private MaintainedHomesteadExemptionRepository maintainedHomesteadExemptions;
    @Autowired private SeniorFreezeMasterRepository seniorFreezeMasters;
    @Autowired private SeniorFreezeApplicantRepository seniorFreezeApplicants;

    @Test
    void homeownerMasterPreservesPaddedSourceIdentifiersAndFixedPointBoundaries() {
        HomeownerMaster source = homeownerMaster(null, null);

        HomeownerMaster saved = homeownerMasters.save(source);
        Long id = Objects.requireNonNull(saved.id());
        HomeownerMaster reloaded = reload(() -> homeownerMasters.findById(id).orElseThrow());

        assertThat(saved.version()).isEqualTo(0L);
        assertThat(reloaded).isEqualTo(homeownerMaster(id, 0L));
        assertThat(reloaded.propertyNumber()).isEqualTo("000000000000007");
        assertThat(reloaded.volumeNumber()).isEqualTo("003");
        assertThat(reloaded.taxCode()).isEqualTo("00042");
        assertThat(reloaded.zipCode()).isEqualTo("000006061");
        assertThat(reloaded.assessedValue()).isEqualTo(new BigDecimal("999999999"));
        assertThat(reloaded.equalizationFactor()).isEqualTo(new BigDecimal("9.9999"));
        assertThat(reloaded.proration()).isEqualTo(new BigDecimal("0.999999"));
    }

    @Test
    void staleHomeownerMasterCannotReplaceACommittedAnnualStatus() {
        HomeownerMaster original = homeownerMasters.save(homeownerMaster(null, null));
        HomeownerMaster winner = homeownerMasters.save(original.withResponseStatus(2));

        Long id = Objects.requireNonNull(original.id());
        assertThrows(
                OptimisticLockingFailureException.class,
                () -> homeownerMasters.save(original.withResponseStatus(9)));
        HomeownerMaster reloaded = reload(() -> homeownerMasters.findById(id).orElseThrow());

        assertThat(original.version()).isEqualTo(0L);
        assertThat(winner.version()).isEqualTo(1L);
        assertThat(reloaded).isEqualTo(winner);
        assertThat(reloaded.responseStatus()).isEqualTo(2);
    }

    @Test
    void annualHomeownerExemptionPreservesBothParcelIdentifiersAndNullableSourceData() {
        HomeownerExemption source = homeownerExemption(null, null);

        HomeownerExemption saved = homeownerExemptions.save(source);
        Long id = Objects.requireNonNull(saved.id());
        HomeownerExemption reloaded = reload(() -> homeownerExemptions.findById(id).orElseThrow());

        assertThat(saved.version()).isEqualTo(0L);
        assertThat(reloaded).isEqualTo(homeownerExemption(id, 0L));
        assertThat(reloaded.propertyNumber()).isEqualTo("000000000000008");
        assertThat(reloaded.keyParcelNumber()).isEqualTo("000000000000009");
        assertThat(reloaded.zipCode()).isNull();
        assertThat(reloaded.equalizedValue()).isNull();
        assertThat(reloaded.proration()).isEqualTo(new BigDecimal("9.999999"));
    }

    @Test
    void maintainedHomesteadPreservesRequiredBaseYearGroupAndSourceIdentifiers() {
        MaintainedHomesteadExemption source = maintainedHomestead(null, null);

        MaintainedHomesteadExemption saved = maintainedHomesteadExemptions.save(source);
        Long id = Objects.requireNonNull(saved.id());
        MaintainedHomesteadExemption reloaded =
                reload(() -> maintainedHomesteadExemptions.findById(id).orElseThrow());

        assertThat(saved.version()).isEqualTo(0L);
        assertThat(reloaded).isEqualTo(maintainedHomestead(id, 0L));
        assertThat(reloaded.propertyNumber()).isEqualTo("000000000000010");
        assertThat(reloaded.baseYearExemptionAmount()).isEqualTo(new BigDecimal("999999999"));
        assertThat(reloaded.exemptionBaseYear()).isEqualTo(1999);
        assertThat(reloaded.baseYearEstablishmentCode()).isEqualTo("TR");
        assertThat(reloaded.taxCode()).isEqualTo("00005");
        assertThat(reloaded.volumeNumber()).isEqualTo("006");
    }

    @Test
    void seniorFreezeMasterPreservesSignedDifferencesAndDistinctAnnualValuations() {
        SeniorFreezeMaster source = seniorFreezeMaster(null, null);

        SeniorFreezeMaster saved = seniorFreezeMasters.save(source);
        Long id = Objects.requireNonNull(saved.id());
        SeniorFreezeMaster reloaded = reload(() -> seniorFreezeMasters.findById(id).orElseThrow());

        assertThat(saved.version()).isEqualTo(0L);
        assertThat(reloaded).isEqualTo(seniorFreezeMaster(id, 0L));
        assertThat(reloaded.keyParcelNumber()).isEqualTo("00000000000011");
        assertThat(reloaded.mailingZipCode()).isEqualTo("000006062");
        assertThat(reloaded.currentYearFinalEqualizedValueDifference())
                .isEqualTo(new BigDecimal("-999999999"));
        assertThat(reloaded.originalCurrentYearFinalEqualizedValueDifference())
                .isEqualTo(new BigDecimal("999999999"));
        assertThat(reloaded.currentYearFullAssessedValue()).isEqualTo(new BigDecimal("700000007"));
        assertThat(reloaded.currentYearFullEqualizedValue()).isEqualTo(new BigDecimal("800000008"));
    }

    @Test
    void seniorFreezeApplicantPreservesRawDatesIdentifiersAndDistinctIncomeComponents() {
        SeniorFreezeApplicant source = seniorFreezeApplicant(null, null);

        SeniorFreezeApplicant saved = seniorFreezeApplicants.save(source);
        Long id = Objects.requireNonNull(saved.id());
        SeniorFreezeApplicant reloaded =
                reload(() -> seniorFreezeApplicants.findById(id).orElseThrow());

        assertThat(saved.version()).isEqualTo(0L);
        assertThat(reloaded).isEqualTo(seniorFreezeApplicant(id, 0L));
        assertThat(reloaded.applicantZipCode()).isEqualTo("000006063");
        assertThat(reloaded.phoneNumber()).isEqualTo("0000000012");
        assertThat(reloaded.socialSecurityNumber()).isEqualTo("00000000013");
        assertThat(reloaded.birthDate()).isEqualTo("02311940");
        assertThat(reloaded.denialDate()).isEqualTo(20240230);
        assertThat(reloaded.qualificationDate()).isEqualTo(20231340);
        assertThat(reloaded.civilServiceBenefits()).isEqualTo(new BigDecimal("9999999.99"));
        assertThat(reloaded.wages()).isEqualTo(new BigDecimal("-9999999.99"));
        assertThat(reloaded.interestIncome()).isEqualTo(new BigDecimal("1111111.11"));
        assertThat(reloaded.otherIncome()).isEqualTo(new BigDecimal("5555555.55"));
        assertThat(reloaded.totalIncome()).isEqualTo(new BigDecimal("-7654321.09"));
    }

    private <T> T reload(Supplier<T> operation) {
        clearPersistenceContext();
        return operation.get();
    }

    private static HomeownerMaster homeownerMaster(@Nullable Long id, @Nullable Long version) {
        return new HomeownerMaster(
                id,
                version,
                26,
                new BigDecimal("999999999"),
                299,
                314159,
                "CHICAGO",
                17,
                new BigDecimal("9.9999"),
                new BigDecimal("0"),
                1,
                "1 W SOURCE ST",
                new BigDecimal("999999998"),
                2001,
                "TR",
                new BigDecimal("100.0"),
                "ADA OWNER",
                "7",
                new BigDecimal("0.999999"),
                4,
                5,
                "IL",
                "42",
                6,
                new BigDecimal("-999999999"),
                7,
                "3",
                "6061");
    }

    private static HomeownerExemption homeownerExemption(
            @Nullable Long id, @Nullable Long version) {
        return new HomeownerExemption(
                id,
                version,
                27,
                new BigDecimal("999999999"),
                211,
                null,
                null,
                12,
                null,
                1,
                new BigDecimal("0.0001"),
                null,
                null,
                "9",
                null,
                new BigDecimal("0.0"),
                null,
                "8",
                new BigDecimal("9.999999"),
                2,
                3,
                4,
                null,
                null,
                "4",
                null,
                5,
                "2",
                null);
    }

    private static MaintainedHomesteadExemption maintainedHomestead(
            @Nullable Long id, @Nullable Long version) {
        return new MaintainedHomesteadExemption(
                id,
                version,
                28,
                new BigDecimal("0"),
                212,
                2718,
                "EVANSTON",
                8,
                9,
                new BigDecimal("1.2345"),
                new BigDecimal("999999998"),
                2,
                "10 MAINTAINED AVE",
                new BigDecimal("999999999"),
                1999,
                "TR",
                new BigDecimal("99.9"),
                "GRACE OWNER",
                "10",
                new BigDecimal("1.000000"),
                6,
                7,
                "IL",
                "5",
                "H",
                8,
                "6",
                "6064");
    }

    private static SeniorFreezeMaster seniorFreezeMaster(
            @Nullable Long id, @Nullable Long version) {
        return new SeniorFreezeMaster(
                id,
                version,
                "Y",
                "N",
                2000,
                201,
                new BigDecimal("100000001"),
                new BigDecimal("200000002"),
                new BigDecimal("300000003"),
                new BigDecimal("400000004"),
                101,
                102,
                "M",
                new BigDecimal("500000005"),
                new BigDecimal("600000006"),
                new BigDecimal("700000007"),
                new BigDecimal("800000008"),
                299,
                new BigDecimal("900000009"),
                new BigDecimal("999999998"),
                "F",
                new BigDecimal("-999999999"),
                new BigDecimal("700000007"),
                new BigDecimal("800000008"),
                new BigDecimal("900000009"),
                new BigDecimal("999999998"),
                103,
                104,
                "11",
                "OAK PARK",
                "NW",
                "123A",
                "IL",
                "SOURCE STREET",
                "AVE",
                "6062",
                2,
                "LIN SENIOR",
                new BigDecimal("100.0"),
                1998,
                new BigDecimal("111111111"),
                new BigDecimal("222222222"),
                new BigDecimal("333333333"),
                new BigDecimal("444444444"),
                new BigDecimal("999999999"),
                "N",
                new BigDecimal("0.123456"),
                "S",
                105,
                7);
    }

    private static SeniorFreezeApplicant seniorFreezeApplicant(
            @Nullable Long id, @Nullable Long version) {
        return new SeniorFreezeApplicant(
                id,
                version,
                85,
                "14 APPLICANT ROAD",
                "CHICAGO",
                "MAYA",
                "APPLICANT",
                "Q",
                "MAYA OLDNAME",
                "IL",
                "MS",
                "6063",
                1997,
                new BigDecimal("999999999"),
                "B",
                1701,
                "02311940",
                new BigDecimal("9999999.99"),
                31,
                20240230,
                20240131,
                1998,
                new BigDecimal("999999998"),
                new BigDecimal("9.9999"),
                new BigDecimal("999999997"),
                1,
                "A",
                1702,
                new BigDecimal("999.999"),
                32,
                "H",
                2024,
                new BigDecimal("1111111.11"),
                20241201,
                "L",
                2,
                3,
                new BigDecimal("-2222222.22"),
                new BigDecimal("3333333.33"),
                "N",
                "Y",
                new BigDecimal("-4444444.44"),
                new BigDecimal("5555555.55"),
                new BigDecimal("0.999999"),
                "12",
                new BigDecimal("-6666666.66"),
                20231340,
                new BigDecimal("7777777.77"),
                0,
                new BigDecimal("9.9"),
                "A",
                "Y",
                new BigDecimal("-8888888.88"),
                "13",
                new BigDecimal("-7654321.09"),
                new BigDecimal("1234567.89"),
                new BigDecimal("-9999999.99"));
    }
}
