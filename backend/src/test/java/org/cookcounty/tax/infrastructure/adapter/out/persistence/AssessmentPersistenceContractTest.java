package org.cookcounty.tax.infrastructure.adapter.out.persistence;

import static com.google.common.truth.Truth.assertThat;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.cookcounty.tax.domain.model.AgencyEqualizedValuation;
import org.cookcounty.tax.domain.model.AssessmentDetail;
import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.cookcounty.tax.domain.model.FrozenAgencyAdjustment;
import org.cookcounty.tax.domain.model.FrozenValuation;
import org.cookcounty.tax.domain.port.out.AgencyEqualizedValuationRepository;
import org.cookcounty.tax.domain.port.out.AssessmentDetailRepository;
import org.cookcounty.tax.domain.port.out.AssessmentParcelRepository;
import org.cookcounty.tax.domain.port.out.FrozenAgencyAdjustmentRepository;
import org.cookcounty.tax.domain.port.out.FrozenValuationRepository;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;

import java.math.BigDecimal;

class AssessmentPersistenceContractTest extends PostgreSqlIntegrationTestSupport {

    @Autowired private AssessmentParcelRepository parcelRepository;
    @Autowired private AssessmentDetailRepository detailRepository;
    @Autowired private FrozenValuationRepository frozenValuationRepository;
    @Autowired private AgencyEqualizedValuationRepository agencyValuationRepository;
    @Autowired private FrozenAgencyAdjustmentRepository adjustmentRepository;

    @Test
    void parcelReloadPreservesSignedValuationPeriodsAndRejectsAStaleBatchReplacement() {
        AssessmentParcel created = parcelRepository.save(assessmentParcel(null, null, 611));
        assertThat(created.id()).isNotNull();
        assertThat(created.version()).isEqualTo(0L);
        clearPersistenceContext();

        AssessmentParcel loaded = parcelRepository.findById(created.id()).orElseThrow();
        assertThat(loaded).isEqualTo(assessmentParcel(created.id(), 0L, 611));
        assertThat(loaded.parcelNumber()).isEqualTo("000000000042007");
        assertThat(loaded.volumeNumber()).isEqualTo("007");
        assertThat(loaded.taxCode()).isEqualTo("00042");
        assertThat(loaded.priorLandValue()).isEqualTo(new BigDecimal("-123456789"));
        assertThat(loaded.currentLandValue()).isEqualTo(new BigDecimal("234567891"));
        assertThat(loaded.eifdPriorTotalValue()).isEqualTo(new BigDecimal("-456789123"));
        assertThat(loaded.eifdCurrentTotalValue()).isEqualTo(new BigDecimal("567891234"));

        AssessmentParcel current = parcelRepository.save(loaded.withOverallClass(799));
        clearPersistenceContext();
        assertThat(loaded.version()).isEqualTo(0L);
        assertThat(current.version()).isEqualTo(1L);
        assertThrows(
                OptimisticLockingFailureException.class,
                () -> parcelRepository.save(loaded.withOverallClass(488)));
        clearPersistenceContext();
        AssessmentParcel afterConflict = parcelRepository.findById(created.id()).orElseThrow();
        assertThat(afterConflict.overallClass()).isEqualTo(799);
        assertThat(afterConflict.version()).isEqualTo(current.version());
    }

    @Test
    void detailReloadPreservesSourceFactorsAndOccurrenceOrderWithoutDecimalRounding() {
        AssessmentDetail first =
                detailRepository.save(assessmentDetail(null, null, 2, "000000000042007"));
        AssessmentDetail second =
                detailRepository.save(assessmentDetail(null, null, 9, "000000000042008"));
        assertThat(first.id()).isNotNull();
        assertThat(first.version()).isEqualTo(0L);
        assertThat(second.version()).isEqualTo(0L);
        clearPersistenceContext();

        AssessmentDetail loaded = detailRepository.findById(first.id()).orElseThrow();
        assertThat(loaded).isEqualTo(assessmentDetail(first.id(), 0L, 2, "000000000042007"));
        assertThat(loaded.keyParcelNumber()).isEqualTo("000000000000019");
        assertThat(loaded.conditionFactor()).isEqualTo(new BigDecimal("98.7"));
        assertThat(loaded.cornerFactor()).isEqualTo(new BigDecimal("1.2345"));
        assertThat(loaded.depthFactor()).isEqualTo(new BigDecimal("1.125"));
        assertThat(loaded.extraCornerFactor()).isEqualTo(new BigDecimal("0.54321"));

        AssessmentDetail current =
                detailRepository.save(loaded.withValuation(new BigDecimal("345678912")));
        assertThat(current.version()).isEqualTo(1L);
        assertThrows(
                OptimisticLockingFailureException.class,
                () -> detailRepository.save(loaded.withValuation(new BigDecimal("456789123"))));
        clearPersistenceContext();
        assertThat(detailRepository.findById(first.id()).orElseThrow().valuation())
                .isEqualTo(new BigDecimal("345678912"));
        assertThat(loaded.percentAssessed()).isEqualTo(new BigDecimal("87.65432"));
        assertThat(loaded.unitPrice()).isEqualTo(new BigDecimal("98765.43"));
        assertThat(detailRepository.findAllInInputOrder())
                .containsExactly(current, second)
                .inOrder();
    }

    @Test
    void frozenValuationReloadKeepsPriorCurrentActionGroupsAndSignedCountsDistinct() {
        FrozenValuation first =
                frozenValuationRepository.save(frozenValuation(null, null, "00000000000073", 0));
        FrozenValuation second =
                frozenValuationRepository.save(frozenValuation(null, null, "00000000000009", 101));
        assertThat(first.id()).isNotNull();
        assertThat(first.version()).isEqualTo(0L);
        assertThat(second.version()).isEqualTo(0L);
        clearPersistenceContext();

        FrozenValuation loaded = frozenValuationRepository.findById(first.id()).orElseThrow();
        assertThat(loaded).isEqualTo(frozenValuation(first.id(), 0L, "00000000000073", 0));
        assertThat(loaded.divisionNumber()).isEqualTo("00000000000073");
        assertThat(loaded.changeActionPriorLandValue()).isEqualTo(new BigDecimal("-6"));
        assertThat(loaded.changeActionCurrentLandValue()).isEqualTo(new BigDecimal("-2"));
        assertThat(loaded.noChangeActionPriorParcelCount()).isEqualTo(-19L);
        assertThat(loaded.noChangeActionCurrentParcelCount()).isEqualTo(-15L);
        assertThat(frozenValuationRepository.findAllInPersistenceOrder())
                .containsExactly(first, second)
                .inOrder();

        FrozenValuation current =
                frozenValuationRepository.save(
                        frozenValuation(first.id(), first.version(), first.divisionNumber(), 201));
        assertThat(current.version()).isEqualTo(1L);
        assertThrows(
                OptimisticLockingFailureException.class,
                () ->
                        frozenValuationRepository.save(
                                frozenValuation(
                                        first.id(), first.version(), first.divisionNumber(), 301)));
        clearPersistenceContext();
        assertThat(
                        frozenValuationRepository
                                .findById(first.id())
                                .orElseThrow()
                                .proposedTotalValue())
                .isEqualTo(amount(-229));
    }

    @Test
    void agencyReloadPreservesOrderedRelationshipsAndExactRatesAndExtensions() {
        AgencyEqualizedValuation created =
                agencyValuationRepository.save(
                        agencyValuation(null, null, new BigDecimal("73.19")));
        assertThat(created.id()).isNotNull();
        assertThat(created.version()).isEqualTo(0L);
        clearPersistenceContext();

        AgencyEqualizedValuation loaded =
                agencyValuationRepository.findById(created.id()).orElseThrow();
        assertThat(loaded).isEqualTo(agencyValuation(created.id(), 0L, new BigDecimal("73.19")));
        assertThat(loaded.agencyNumber()).isEqualTo("000000731");
        assertThat(loaded.connectingAgency1()).isEqualTo("000000101");
        assertThat(loaded.connectingAgency4()).isEqualTo("000000104");
        assertThat(loaded.parentAgency1()).isEqualTo("000000201");
        assertThat(loaded.parentAgency5()).isEqualTo("000000205");
        assertThat(loaded.burdenPercent()).isEqualTo(new BigDecimal("73.19"));
        assertThat(loaded.limitingTaxRateOverride()).isEqualTo(new BigDecimal("-12.345678"));
        assertThat(loaded.previousTaxYear2Extension()).isEqualTo(new BigDecimal("34567890123.45"));

        AgencyEqualizedValuation current =
                agencyValuationRepository.save(
                        agencyValuation(created.id(), created.version(), new BigDecimal("64.28")));
        assertThat(current.version()).isEqualTo(1L);
        assertThrows(
                OptimisticLockingFailureException.class,
                () ->
                        agencyValuationRepository.save(
                                agencyValuation(
                                        created.id(), created.version(), new BigDecimal("55.37"))));
        clearPersistenceContext();
        assertThat(agencyValuationRepository.findById(created.id()).orElseThrow().burdenPercent())
                .isEqualTo(new BigDecimal("64.28"));
    }

    @Test
    void frozenAgencyReloadPreservesSignedPostingAmountsAndCompositeLookupIdentity() {
        FrozenAgencyAdjustment created =
                adjustmentRepository.save(
                        frozenAgencyAdjustment(null, null, new BigDecimal("-12.345")));
        assertThat(created.id()).isNotNull();
        assertThat(created.version()).isEqualTo(0L);
        clearPersistenceContext();

        FrozenAgencyAdjustment loaded =
                adjustmentRepository
                        .findByTaxCodeAndAgencyNumber("00427", "000000731")
                        .orElseThrow();
        assertThat(loaded)
                .isEqualTo(frozenAgencyAdjustment(created.id(), 0L, new BigDecimal("-12.345")));
        assertThat(loaded.annexedAssessedValue()).isEqualTo(new BigDecimal("-1234567890123"));
        assertThat(loaded.expiredIncentiveTaxAmount()).isEqualTo(new BigDecimal("-123456789.12"));
        assertThat(loaded.frozenTaxAmount()).isEqualTo(new BigDecimal("-1234567890123.45"));
        assertThat(loaded.taxRate()).isEqualTo(new BigDecimal("-12.345"));
        assertThat(loaded.tifPriorFrozenEqualizedValue())
                .isEqualTo(new BigDecimal("-901234567890"));

        FrozenAgencyAdjustment current =
                adjustmentRepository.save(
                        frozenAgencyAdjustment(
                                created.id(), created.version(), new BigDecimal("23.456")));
        assertThat(current.version()).isEqualTo(1L);
        assertThrows(
                OptimisticLockingFailureException.class,
                () ->
                        adjustmentRepository.save(
                                frozenAgencyAdjustment(
                                        created.id(),
                                        created.version(),
                                        new BigDecimal("-34.567"))));
        clearPersistenceContext();
        assertThat(
                        adjustmentRepository
                                .findByTaxCodeAndAgencyNumber("00427", "000000731")
                                .orElseThrow()
                                .taxRate())
                .isEqualTo(new BigDecimal("23.456"));
    }

    private static AssessmentParcel assessmentParcel(
            @Nullable Long id, @Nullable Long version, Integer overallClass) {
        return new AssessmentParcel(
                id,
                version,
                new BigDecimal("-987654321"),
                "A",
                "7",
                new BigDecimal("-876543210"),
                new BigDecimal("345678912"),
                new BigDecimal("234567891"),
                new BigDecimal("580246803"),
                37,
                new BigDecimal("-765432109"),
                overallClass,
                "000000000042007",
                "C",
                new BigDecimal("-234567891"),
                new BigDecimal("-123456789"),
                new BigDecimal("-358024680"),
                new BigDecimal("456789123"),
                new BigDecimal("345678912"),
                new BigDecimal("802468035"),
                19,
                "00042",
                "R",
                "007",
                null,
                null,
                new BigDecimal("-123456789"),
                new BigDecimal("-333332334"),
                new BigDecimal("-456789123"),
                new BigDecimal("234567891"),
                new BigDecimal("333323343"),
                new BigDecimal("567891234"));
    }

    private static AssessmentDetail assessmentDetail(
            @Nullable Long id, @Nullable Long version, Integer occurrence, String parcelNumber) {
        return new AssessmentDetail(
                id,
                version,
                47,
                987654321L,
                288,
                "GR",
                new BigDecimal("98.7"),
                new BigDecimal("1.2345"),
                3,
                7654321L,
                new BigDecimal("1.125"),
                "Q",
                "5",
                new BigDecimal("0.54321"),
                654321L,
                87,
                "000000000000019",
                new BigDecimal("76.5"),
                93,
                new BigDecimal("65.4"),
                occurrence,
                parcelNumber,
                "007",
                new BigDecimal("87.65432"),
                new BigDecimal("-123456789"),
                "S",
                "SQ",
                new BigDecimal("98765.43"),
                new BigDecimal("-234567891"));
    }

    private static FrozenValuation frozenValuation(
            @Nullable Long id, @Nullable Long version, String divisionNumber, int offset) {
        return new FrozenValuation(
                id,
                version,
                amount(-1 - offset),
                amount(-2 - offset),
                -3L - offset,
                amount(-4 - offset),
                amount(-5 - offset),
                amount(-6 - offset),
                -7L - offset,
                amount(-8 - offset),
                amount(9 + offset),
                amount(10 + offset),
                11L + offset,
                amount(12 + offset),
                divisionNumber,
                amount(-13 - offset),
                amount(-14 - offset),
                -15L - offset,
                amount(-16 - offset),
                amount(-17 - offset),
                amount(-18 - offset),
                -19L - offset,
                amount(-20 - offset),
                amount(21 + offset),
                amount(22 + offset),
                23L + offset,
                amount(24 + offset),
                amount(-25 - offset),
                amount(-26 - offset),
                amount(-27 - offset),
                amount(-29 - offset),
                amount(-28 - offset));
    }

    private static AgencyEqualizedValuation agencyValuation(
            @Nullable Long id, @Nullable Long version, BigDecimal burdenPercent) {
        return new AgencyEqualizedValuation(
                id,
                version,
                "000000731",
                amount(12345678901L),
                burdenPercent,
                "000000101",
                "000000102",
                "000000103",
                "000000104",
                amount(23456789012L),
                amount(34567890123L),
                amount(456789012345L),
                amount(56789012345L),
                amount(67890123456L),
                amount(78901234567L),
                amount(89012345678L),
                amount(90123456789L),
                amount(81234567890L),
                amount(72345678901L),
                amount(63456789012L),
                amount(54567890123L),
                amount(45678901234L),
                amount(56789012346L),
                new BigDecimal("-12.345678"),
                amount(36789012345L),
                amount(27890123456L),
                amount(18901234567L),
                amount(29012345678L),
                amount(30123456789L),
                amount(41234567890L),
                amount(52345678901L),
                "000000201",
                "000000202",
                "000000203",
                "000000204",
                "000000205",
                2023,
                new BigDecimal("23456789012.34"),
                2022,
                new BigDecimal("34567890123.45"),
                2021,
                new BigDecimal("45678901234.56"),
                true,
                2024,
                amount(98765432109L));
    }

    private static FrozenAgencyAdjustment frozenAgencyAdjustment(
            @Nullable Long id, @Nullable Long version, BigDecimal taxRate) {
        return new FrozenAgencyAdjustment(
                id,
                version,
                "000000731",
                amount(-1234567890123L),
                amount(123456789012L),
                amount(-234567890123L),
                amount(345678901234L),
                amount(-456789012345L),
                amount(567890123456L),
                amount(-67890123456L),
                new BigDecimal("-123456789.12"),
                amount(78901234567L),
                amount(-890123456789L),
                amount(901234567890L),
                new BigDecimal("-1234567890123.45"),
                "00427",
                taxRate,
                amount(123456789012L),
                amount(-234567890123L),
                amount(-901234567890L),
                amount(345678901234L));
    }

    private static BigDecimal amount(long value) {
        return BigDecimal.valueOf(value);
    }
}
