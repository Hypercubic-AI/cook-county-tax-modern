package org.cookcounty.tax.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.util.Objects;

// GENERATED-IMPORTS:start

import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;

// GENERATED-IMPORTS:end

/// Mutable persistence boundary for one Senior Freeze parcel snapshot.
///
/// JPA creates an incomplete instance before it hydrates the fields. The generated identity is
/// absent until insertion, and the optimistic-lock version is absent until JPA inserts or hydrates
/// the row. JPA changes the version after successful updates to detect concurrent writes.
///
/// Accessors return or assign the stored representation without domain normalization. They do not
/// pad fixed-width identifiers or constrain decimal precision and scale. Construct {@link
/// org.cookcounty.tax.domain.model.SeniorFreezeMaster} to apply those boundaries. Callers must
/// populate all non-null domain fields before mapping this entity to that record.
@Entity
@Table(name = "senior_freeze_masters")
public class SeniorFreezeMasterEntity {

    /// Database-generated surrogate identity. It is null until JPA assigns or hydrates it.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    /// Optimistic-lock value managed by JPA. It is null before JPA inserts or hydrates the entity.
    @Version private @Nullable Long version;

    // GENERATED-FIELDS:start

    /// Whether base value was calculated manually, using the maintained code. The database
    /// representation is `CHARACTER VARYING(1)`.
    @Column(name = "base_value_manual_calculation_indicator")
    private @Nullable String baseValueManualCalculationIndicator;

    /// Whether base value calculation was withheld, using the maintained code. The database
    /// representation is `CHARACTER VARYING(1)`.
    @Column(name = "base_value_no_calculation_indicator")
    private @Nullable String baseValueNoCalculationIndicator;

    /// Four-digit year selected for the frozen base value.
    @Column(name = "base_value_year")
    private @Nullable Integer baseValueYear;

    /// Property classification in that base year.
    @Column(name = "base_value_year_class")
    private @Nullable Integer baseValueYearClass;

    /// Eligible base-year full assessed valuation in whole dollars. The database representation is
    /// `NUMERIC(9,0)`.
    @Column(name = "base_year_eligible_computed_full_assessed_value", precision = 9, scale = 0)
    private @Nullable BigDecimal baseYearEligibleComputedFullAssessedValue;

    /// Base-year equalized valuation in whole dollars. The database representation is
    /// `NUMERIC(9,0)`.
    @Column(name = "base_year_equalized_value", precision = 9, scale = 0)
    private @Nullable BigDecimal baseYearEqualizedValue;

    /// Base-year full assessed valuation in whole dollars. The database representation is
    /// `NUMERIC(9,0)`.
    @Column(name = "base_year_full_assessed_value", precision = 9, scale = 0)
    private @Nullable BigDecimal baseYearFullAssessedValue;

    /// Total eligible base-year computed equalized valuation in whole dollars. The database
    /// representation is `NUMERIC(9,0)`.
    @Column(name = "base_year_total_eligible_computed_equalized_value", precision = 9, scale = 0)
    private @Nullable BigDecimal baseYearTotalEligibleComputedEqualizedValue;

    /// Number of cooperative building shares.
    @Column(name = "building_shares")
    private @Nullable Integer buildingShares;

    /// Number of building units.
    @Column(name = "building_units")
    private @Nullable Integer buildingUnits;

    /// Maintained calculation-method code. The database representation is `CHARACTER VARYING(1)`.
    @Column(name = "calculation_type")
    private @Nullable String calculationType;

    /// Assessed valuation in whole dollars when the class expires. The database representation is
    /// `NUMERIC(9,0)`.
    @Column(name = "class288_expiration_assessed_value", precision = 9, scale = 0)
    private @Nullable BigDecimal class288ExpirationAssessedValue;

    /// Equalized valuation in whole dollars when the class expires. The database representation is
    /// `NUMERIC(9,0)`.
    @Column(name = "class288_expiration_equalized_value", precision = 9, scale = 0)
    private @Nullable BigDecimal class288ExpirationEqualizedValue;

    /// Assessed valuation above the class limit in whole dollars. The database representation is
    /// `NUMERIC(9,0)`.
    @Column(name = "class288_over_limit_assessed_value", precision = 9, scale = 0)
    private @Nullable BigDecimal class288OverLimitAssessedValue;

    /// Equalized valuation above the class limit in whole dollars. The database representation is
    /// `NUMERIC(9,0)`.
    @Column(name = "class288_over_limit_equalized_value", precision = 9, scale = 0)
    private @Nullable BigDecimal class288OverLimitEqualizedValue;

    /// Current property classification code.
    @Column(name = "current_year_class")
    private @Nullable Integer currentYearClass;

    /// Current eligible computed assessed valuation in whole dollars. The database representation
    /// is `NUMERIC(9,0)`.
    @Column(name = "current_year_eligible_computed_assessed_value", precision = 9, scale = 0)
    private @Nullable BigDecimal currentYearEligibleComputedAssessedValue;

    /// Current eligible computed equalized valuation in whole dollars. The database representation
    /// is `NUMERIC(9,0)`.
    @Column(name = "current_year_eligible_computed_equalized_value", precision = 9, scale = 0)
    private @Nullable BigDecimal currentYearEligibleComputedEqualizedValue;

    /// Whether the current record uses farm treatment, using the maintained code. The database
    /// representation is `CHARACTER VARYING(1)`.
    @Column(name = "current_year_farm_indicator")
    private @Nullable String currentYearFarmIndicator;

    /// Signed final equalized-value difference in whole dollars. The database representation is
    /// `NUMERIC(9,0)`.
    @Column(name = "current_year_final_equalized_value_difference", precision = 9, scale = 0)
    private @Nullable BigDecimal currentYearFinalEqualizedValueDifference;

    /// Current full assessed valuation in whole dollars. The database representation is
    /// `NUMERIC(9,0)`.
    @Column(name = "current_year_full_assessed_value", precision = 9, scale = 0)
    private @Nullable BigDecimal currentYearFullAssessedValue;

    /// Current full equalized valuation in whole dollars. The database representation is
    /// `NUMERIC(9,0)`.
    @Column(name = "current_year_full_equalized_value", precision = 9, scale = 0)
    private @Nullable BigDecimal currentYearFullEqualizedValue;

    /// Current ineligible assessed valuation in whole dollars. The database representation is
    /// `NUMERIC(9,0)`.
    @Column(name = "current_year_not_eligible_assessed_value", precision = 9, scale = 0)
    private @Nullable BigDecimal currentYearNotEligibleAssessedValue;

    /// Current ineligible equalized valuation in whole dollars. The database representation is
    /// `NUMERIC(9,0)`.
    @Column(name = "current_year_not_eligible_equalized_value", precision = 9, scale = 0)
    private @Nullable BigDecimal currentYearNotEligibleEqualizedValue;

    /// Number of homeowner units.
    @Column(name = "homeowner_units")
    private @Nullable Integer homeownerUnits;

    /// Number of homestead units.
    @Column(name = "homestead_units")
    private @Nullable Integer homesteadUnits;

    /// Canonical source-width parcel identifier selected from qualifying assessment details.
    @Column(name = "key_parcel_number", length = 14)
    private @Nullable String keyParcelNumber;

    /// Mailing city retained from the applicant or mailing record. The database representation is
    /// `CHARACTER VARYING(28)`.
    @Column(name = "mailing_city")
    private @Nullable String mailingCity;

    /// Mailing direction retained from the applicant or mailing record. The database representation
    /// is `CHARACTER VARYING(2)`.
    @Column(name = "mailing_direction")
    private @Nullable String mailingDirection;

    /// House-number characters retained from the mailing record. The database representation is
    /// `CHARACTER VARYING(5)`.
    @Column(name = "mailing_house_number")
    private @Nullable String mailingHouseNumber;

    /// Mailing state code. The database representation is `CHARACTER VARYING(2)`.
    @Column(name = "mailing_state")
    private @Nullable String mailingState;

    /// Mailing street name. The database representation is `CHARACTER VARYING(22)`.
    @Column(name = "mailing_street")
    private @Nullable String mailingStreet;

    /// Mailing street suffix. The database representation is `CHARACTER VARYING(4)`.
    @Column(name = "mailing_suffix")
    private @Nullable String mailingSuffix;

    /// Canonical nine-digit postal identifier.
    @Column(name = "mailing_zip_code", length = 9)
    private @Nullable String mailingZipCode;

    /// Maintained record-maintenance code.
    @Column(name = "maintenance_indicator")
    private @Nullable Integer maintenanceIndicator;

    /// Applicant or owner name retained with the parcel. The database representation is `CHARACTER
    /// VARYING(50)`.
    @Column(name = "master_name")
    private @Nullable String masterName;

    /// Occupancy percentage used by exemption eligibility. The database representation is
    /// `NUMERIC(5,1)`.
    @Column(name = "occupancy_factor", precision = 5, scale = 1)
    private @Nullable BigDecimal occupancyFactor;

    /// Base-value year before the current calculation.
    @Column(name = "original_base_value_year")
    private @Nullable Integer originalBaseValueYear;

    /// Prior eligible base-year full assessed valuation in whole dollars. The database
    /// representation is `NUMERIC(9,0)`.
    @Column(
            name = "original_base_year_eligible_computed_full_assessed_value",
            precision = 9,
            scale = 0)
    private @Nullable BigDecimal originalBaseYearEligibleComputedFullAssessedValue;

    /// Prior base-year equalized valuation in whole dollars. The database representation is
    /// `NUMERIC(9,0)`.
    @Column(name = "original_base_year_equalized_value", precision = 9, scale = 0)
    private @Nullable BigDecimal originalBaseYearEqualizedValue;

    /// Prior base-year full assessed valuation in whole dollars. The database representation is
    /// `NUMERIC(9,0)`.
    @Column(name = "original_base_year_full_assessed_value", precision = 9, scale = 0)
    private @Nullable BigDecimal originalBaseYearFullAssessedValue;

    /// Prior total eligible base-year computed equalized valuation in whole dollars. The database
    /// representation is `NUMERIC(9,0)`.
    @Column(
            name = "original_base_year_total_eligible_computed_equalized_value",
            precision = 9,
            scale = 0)
    private @Nullable BigDecimal originalBaseYearTotalEligibleComputedEqualizedValue;

    /// Prior signed current-year equalized-value difference in whole dollars. The database
    /// representation is `NUMERIC(9,0)`.
    @Column(
            name = "original_current_year_final_equalized_value_difference",
            precision = 9,
            scale = 0)
    private @Nullable BigDecimal originalCurrentYearFinalEqualizedValueDifference;

    /// Prior manual-calculation code. The database representation is `CHARACTER VARYING(1)`.
    @Column(name = "original_manual_calculation_indicator")
    private @Nullable String originalManualCalculationIndicator;

    /// Eligible parcel share as a decimal fraction with six fractional digits. The database
    /// representation is `NUMERIC(7,6)`.
    @Column(name = "property_proration", precision = 7, scale = 6)
    private @Nullable BigDecimal propertyProration;

    /// Record category within the Senior Freeze or homeowner output. The database representation is
    /// `CHARACTER VARYING(1)`.
    @Column(name = "record_code")
    private @Nullable String recordCode;

    /// Number of Senior Freeze cooperative shares.
    @Column(name = "senior_freeze_shares")
    private @Nullable Integer seniorFreezeShares;

    /// Property split classification when qualifying detail supplied it.
    @Column(name = "split_code")
    private @Nullable Integer splitCode;

    // GENERATED-FIELDS:end

    // GENERATED-ACCESSORS:start
    /// Returns the database-generated surrogate identity.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#id()} for the domain identity
    /// contract.
    ///
    /// @return the identity, or `null` before JPA assigns or hydrates it
    public @Nullable Long getId() {
        return id;
    }

    /// Supplies the surrogate identity when a persisted snapshot is reconstituted.
    ///
    /// A `null` value marks a transient entity. JPA assigns the identity during insertion.
    ///
    /// @param id persisted identity, or `null` for a transient entity
    public void setId(@Nullable Long id) {
        this.id = id;
    }

    /// Returns the optimistic-lock value that JPA uses to detect concurrent writes.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#version()} for the domain
    /// version contract.
    ///
    /// @return the version, or `null` before JPA inserts or hydrates the entity
    public @Nullable Long getVersion() {
        return version;
    }

    /// Supplies the optimistic-lock value when a persisted snapshot is reconstituted.
    ///
    /// JPA owns later version changes. A `null` value represents an entity without persisted
    /// version state.
    ///
    /// @param version persisted lock version, or `null` before persistence
    public void setVersion(@Nullable Long version) {
        this.version = version;
    }

    /// Returns the maintained code that indicates whether the base value was calculated manually.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#baseValueManualCalculationIndicator()}
    /// for the domain meaning.
    ///
    /// @return whether base value was calculated manually, using the maintained code
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getBaseValueManualCalculationIndicator() {
        return Objects.requireNonNull(
                baseValueManualCalculationIndicator,
                "baseValueManualCalculationIndicator must be populated before reading");
    }

    /// Stores the maintained manual-calculation code without validation or normalization.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#baseValueManualCalculationIndicator()}
    /// for the domain meaning.
    ///
    /// @param baseValueManualCalculationIndicator whether base value was calculated manually, using
    ///   the maintained code
    public void setBaseValueManualCalculationIndicator(String baseValueManualCalculationIndicator) {
        this.baseValueManualCalculationIndicator = baseValueManualCalculationIndicator;
    }

    /// Returns the maintained code that indicates whether base-value calculation was withheld.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#baseValueNoCalculationIndicator()} for
    /// the domain meaning.
    ///
    /// @return whether base value calculation was withheld, using the maintained code
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getBaseValueNoCalculationIndicator() {
        return Objects.requireNonNull(
                baseValueNoCalculationIndicator,
                "baseValueNoCalculationIndicator must be populated before reading");
    }

    /// Stores the maintained no-calculation code without validation or normalization.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#baseValueNoCalculationIndicator()} for
    /// the domain meaning.
    ///
    /// @param baseValueNoCalculationIndicator whether base value calculation was withheld, using
    ///   the maintained code
    public void setBaseValueNoCalculationIndicator(String baseValueNoCalculationIndicator) {
        this.baseValueNoCalculationIndicator = baseValueNoCalculationIndicator;
    }

    /// Returns the persisted four-digit year selected for the frozen base value.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#baseValueYear()} for the
    /// domain meaning.
    ///
    /// @return four-digit year selected for the frozen base value
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getBaseValueYear() {
        return Objects.requireNonNull(
                baseValueYear, "baseValueYear must be populated before reading");
    }

    /// Stores the four-digit year selected for the frozen base value without validation or
    /// normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#baseValueYear()} for the
    /// domain meaning.
    ///
    /// @param baseValueYear four-digit year selected for the frozen base value
    public void setBaseValueYear(Integer baseValueYear) {
        this.baseValueYear = baseValueYear;
    }

    /// Returns the persisted property classification in that base year.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#baseValueYearClass()} for the
    /// domain meaning.
    ///
    /// @return property classification in that base year
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getBaseValueYearClass() {
        return Objects.requireNonNull(
                baseValueYearClass, "baseValueYearClass must be populated before reading");
    }

    /// Stores the property classification in that base year without validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#baseValueYearClass()} for the
    /// domain meaning.
    ///
    /// @param baseValueYearClass property classification in that base year
    public void setBaseValueYearClass(Integer baseValueYearClass) {
        this.baseValueYearClass = baseValueYearClass;
    }

    /// Returns the persisted eligible base-year full assessed valuation in whole dollars.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#baseYearEligibleComputedFullAssessedValue()}
    /// for the domain meaning. The database column has precision 9 and scale 0. This accessor does
    /// not round or check the value.
    ///
    /// @return eligible base-year full assessed valuation in whole dollars
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getBaseYearEligibleComputedFullAssessedValue() {
        return Objects.requireNonNull(
                baseYearEligibleComputedFullAssessedValue,
                "baseYearEligibleComputedFullAssessedValue must be populated before reading");
    }

    /// Stores the eligible base-year full assessed valuation in whole dollars without validation or
    /// normalization.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#baseYearEligibleComputedFullAssessedValue()}
    /// for the domain meaning. The database column has precision 9 and scale 0. This accessor does
    /// not round or check the value.
    ///
    /// @param baseYearEligibleComputedFullAssessedValue eligible base-year full assessed valuation
    ///   in whole dollars
    public void setBaseYearEligibleComputedFullAssessedValue(
            BigDecimal baseYearEligibleComputedFullAssessedValue) {
        this.baseYearEligibleComputedFullAssessedValue = baseYearEligibleComputedFullAssessedValue;
    }

    /// Returns the persisted base-year equalized valuation in whole dollars.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#baseYearEqualizedValue()} for
    /// the domain meaning. The database column has precision 9 and scale 0. This accessor does not
    /// round or check the value.
    ///
    /// @return base-year equalized valuation in whole dollars
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getBaseYearEqualizedValue() {
        return Objects.requireNonNull(
                baseYearEqualizedValue, "baseYearEqualizedValue must be populated before reading");
    }

    /// Stores the base-year equalized valuation in whole dollars without validation or
    /// normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#baseYearEqualizedValue()} for
    /// the domain meaning. The database column has precision 9 and scale 0. This accessor does not
    /// round or check the value.
    ///
    /// @param baseYearEqualizedValue base-year equalized valuation in whole dollars
    public void setBaseYearEqualizedValue(BigDecimal baseYearEqualizedValue) {
        this.baseYearEqualizedValue = baseYearEqualizedValue;
    }

    /// Returns the persisted base-year full assessed valuation in whole dollars.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#baseYearFullAssessedValue()}
    /// for the domain meaning. The database column has precision 9 and scale 0. This accessor does
    /// not round or check the value.
    ///
    /// @return base-year full assessed valuation in whole dollars
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getBaseYearFullAssessedValue() {
        return Objects.requireNonNull(
                baseYearFullAssessedValue,
                "baseYearFullAssessedValue must be populated before reading");
    }

    /// Stores the base-year full assessed valuation in whole dollars without validation or
    /// normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#baseYearFullAssessedValue()}
    /// for the domain meaning. The database column has precision 9 and scale 0. This accessor does
    /// not round or check the value.
    ///
    /// @param baseYearFullAssessedValue base-year full assessed valuation in whole dollars
    public void setBaseYearFullAssessedValue(BigDecimal baseYearFullAssessedValue) {
        this.baseYearFullAssessedValue = baseYearFullAssessedValue;
    }

    /// Returns the persisted total eligible base-year computed equalized valuation in whole
    /// dollars.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#baseYearTotalEligibleComputedEqualizedValue()}
    /// for the domain meaning. The database column has precision 9 and scale 0. This accessor does
    /// not round or check the value.
    ///
    /// @return total eligible base-year computed equalized valuation in whole dollars
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getBaseYearTotalEligibleComputedEqualizedValue() {
        return Objects.requireNonNull(
                baseYearTotalEligibleComputedEqualizedValue,
                "baseYearTotalEligibleComputedEqualizedValue must be populated before reading");
    }

    /// Stores the total eligible base-year computed equalized valuation in whole dollars without
    /// validation or normalization.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#baseYearTotalEligibleComputedEqualizedValue()}
    /// for the domain meaning. The database column has precision 9 and scale 0. This accessor does
    /// not round or check the value.
    ///
    /// @param baseYearTotalEligibleComputedEqualizedValue total eligible base-year computed
    ///   equalized valuation in whole dollars
    public void setBaseYearTotalEligibleComputedEqualizedValue(
            BigDecimal baseYearTotalEligibleComputedEqualizedValue) {
        this.baseYearTotalEligibleComputedEqualizedValue =
                baseYearTotalEligibleComputedEqualizedValue;
    }

    /// Returns the persisted number of cooperative building shares.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#buildingShares()} for the
    /// domain meaning.
    ///
    /// @return number of cooperative building shares
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getBuildingShares() {
        return Objects.requireNonNull(
                buildingShares, "buildingShares must be populated before reading");
    }

    /// Stores the number of cooperative building shares without validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#buildingShares()} for the
    /// domain meaning.
    ///
    /// @param buildingShares number of cooperative building shares
    public void setBuildingShares(Integer buildingShares) {
        this.buildingShares = buildingShares;
    }

    /// Returns the persisted number of building units.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#buildingUnits()} for the
    /// domain meaning.
    ///
    /// @return number of building units
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getBuildingUnits() {
        return Objects.requireNonNull(
                buildingUnits, "buildingUnits must be populated before reading");
    }

    /// Stores the number of building units without validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#buildingUnits()} for the
    /// domain meaning.
    ///
    /// @param buildingUnits number of building units
    public void setBuildingUnits(Integer buildingUnits) {
        this.buildingUnits = buildingUnits;
    }

    /// Returns the persisted maintained calculation-method code.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#calculationType()} for the
    /// domain meaning.
    ///
    /// @return maintained calculation-method code
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getCalculationType() {
        return Objects.requireNonNull(
                calculationType, "calculationType must be populated before reading");
    }

    /// Stores the maintained calculation-method code without validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#calculationType()} for the
    /// domain meaning.
    ///
    /// @param calculationType maintained calculation-method code
    public void setCalculationType(String calculationType) {
        this.calculationType = calculationType;
    }

    /// Returns the persisted assessed valuation in whole dollars when the class expires.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#class288ExpirationAssessedValue()} for
    /// the domain meaning. The database column has precision 9 and scale 0. This accessor does not
    /// round or check the value.
    ///
    /// @return assessed valuation in whole dollars when the class expires
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getClass288ExpirationAssessedValue() {
        return Objects.requireNonNull(
                class288ExpirationAssessedValue,
                "class288ExpirationAssessedValue must be populated before reading");
    }

    /// Stores the assessed valuation in whole dollars when the class expires without validation or
    /// normalization.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#class288ExpirationAssessedValue()} for
    /// the domain meaning. The database column has precision 9 and scale 0. This accessor does not
    /// round or check the value.
    ///
    /// @param class288ExpirationAssessedValue assessed valuation in whole dollars when the class
    ///   expires
    public void setClass288ExpirationAssessedValue(BigDecimal class288ExpirationAssessedValue) {
        this.class288ExpirationAssessedValue = class288ExpirationAssessedValue;
    }

    /// Returns the persisted equalized valuation in whole dollars when the class expires.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#class288ExpirationEqualizedValue()} for
    /// the domain meaning. The database column has precision 9 and scale 0. This accessor does not
    /// round or check the value.
    ///
    /// @return equalized valuation in whole dollars when the class expires
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getClass288ExpirationEqualizedValue() {
        return Objects.requireNonNull(
                class288ExpirationEqualizedValue,
                "class288ExpirationEqualizedValue must be populated before reading");
    }

    /// Stores the equalized valuation in whole dollars when the class expires without validation or
    /// normalization.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#class288ExpirationEqualizedValue()} for
    /// the domain meaning. The database column has precision 9 and scale 0. This accessor does not
    /// round or check the value.
    ///
    /// @param class288ExpirationEqualizedValue equalized valuation in whole dollars when the class
    ///   expires
    public void setClass288ExpirationEqualizedValue(BigDecimal class288ExpirationEqualizedValue) {
        this.class288ExpirationEqualizedValue = class288ExpirationEqualizedValue;
    }

    /// Returns the persisted assessed valuation above the class limit in whole dollars.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#class288OverLimitAssessedValue()} for the
    /// domain meaning. The database column has precision 9 and scale 0. This accessor does not
    /// round or check the value.
    ///
    /// @return assessed valuation above the class limit in whole dollars
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getClass288OverLimitAssessedValue() {
        return Objects.requireNonNull(
                class288OverLimitAssessedValue,
                "class288OverLimitAssessedValue must be populated before reading");
    }

    /// Stores the assessed valuation above the class limit in whole dollars without validation or
    /// normalization.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#class288OverLimitAssessedValue()} for the
    /// domain meaning. The database column has precision 9 and scale 0. This accessor does not
    /// round or check the value.
    ///
    /// @param class288OverLimitAssessedValue assessed valuation above the class limit in whole
    ///   dollars
    public void setClass288OverLimitAssessedValue(BigDecimal class288OverLimitAssessedValue) {
        this.class288OverLimitAssessedValue = class288OverLimitAssessedValue;
    }

    /// Returns the persisted equalized valuation above the class limit in whole dollars.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#class288OverLimitEqualizedValue()} for
    /// the domain meaning. The database column has precision 9 and scale 0. This accessor does not
    /// round or check the value.
    ///
    /// @return equalized valuation above the class limit in whole dollars
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getClass288OverLimitEqualizedValue() {
        return Objects.requireNonNull(
                class288OverLimitEqualizedValue,
                "class288OverLimitEqualizedValue must be populated before reading");
    }

    /// Stores the equalized valuation above the class limit in whole dollars without validation or
    /// normalization.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#class288OverLimitEqualizedValue()} for
    /// the domain meaning. The database column has precision 9 and scale 0. This accessor does not
    /// round or check the value.
    ///
    /// @param class288OverLimitEqualizedValue equalized valuation above the class limit in whole
    ///   dollars
    public void setClass288OverLimitEqualizedValue(BigDecimal class288OverLimitEqualizedValue) {
        this.class288OverLimitEqualizedValue = class288OverLimitEqualizedValue;
    }

    /// Returns the persisted current property classification code.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#currentYearClass()} for the
    /// domain meaning.
    ///
    /// @return current property classification code
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getCurrentYearClass() {
        return Objects.requireNonNull(
                currentYearClass, "currentYearClass must be populated before reading");
    }

    /// Stores the current property classification code without validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#currentYearClass()} for the
    /// domain meaning.
    ///
    /// @param currentYearClass current property classification code
    public void setCurrentYearClass(Integer currentYearClass) {
        this.currentYearClass = currentYearClass;
    }

    /// Returns the persisted current eligible computed assessed valuation in whole dollars.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#currentYearEligibleComputedAssessedValue()}
    /// for the domain meaning. The database column has precision 9 and scale 0. This accessor does
    /// not round or check the value.
    ///
    /// @return current eligible computed assessed valuation in whole dollars
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getCurrentYearEligibleComputedAssessedValue() {
        return Objects.requireNonNull(
                currentYearEligibleComputedAssessedValue,
                "currentYearEligibleComputedAssessedValue must be populated before reading");
    }

    /// Stores the current eligible computed assessed valuation in whole dollars without validation
    /// or normalization.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#currentYearEligibleComputedAssessedValue()}
    /// for the domain meaning. The database column has precision 9 and scale 0. This accessor does
    /// not round or check the value.
    ///
    /// @param currentYearEligibleComputedAssessedValue current eligible computed assessed valuation
    ///   in whole dollars
    public void setCurrentYearEligibleComputedAssessedValue(
            BigDecimal currentYearEligibleComputedAssessedValue) {
        this.currentYearEligibleComputedAssessedValue = currentYearEligibleComputedAssessedValue;
    }

    /// Returns the persisted current eligible computed equalized valuation in whole dollars.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#currentYearEligibleComputedEqualizedValue()}
    /// for the domain meaning. The database column has precision 9 and scale 0. This accessor does
    /// not round or check the value.
    ///
    /// @return current eligible computed equalized valuation in whole dollars
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getCurrentYearEligibleComputedEqualizedValue() {
        return Objects.requireNonNull(
                currentYearEligibleComputedEqualizedValue,
                "currentYearEligibleComputedEqualizedValue must be populated before reading");
    }

    /// Stores the current eligible computed equalized valuation in whole dollars without validation
    /// or normalization.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#currentYearEligibleComputedEqualizedValue()}
    /// for the domain meaning. The database column has precision 9 and scale 0. This accessor does
    /// not round or check the value.
    ///
    /// @param currentYearEligibleComputedEqualizedValue current eligible computed equalized
    ///   valuation in whole dollars
    public void setCurrentYearEligibleComputedEqualizedValue(
            BigDecimal currentYearEligibleComputedEqualizedValue) {
        this.currentYearEligibleComputedEqualizedValue = currentYearEligibleComputedEqualizedValue;
    }

    /// Returns the maintained code that indicates whether the current record uses farm treatment.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#currentYearFarmIndicator()}
    /// for the domain meaning.
    ///
    /// @return whether the current record uses farm treatment, using the maintained code
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getCurrentYearFarmIndicator() {
        return Objects.requireNonNull(
                currentYearFarmIndicator,
                "currentYearFarmIndicator must be populated before reading");
    }

    /// Stores the maintained farm-treatment code without validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#currentYearFarmIndicator()}
    /// for the domain meaning.
    ///
    /// @param currentYearFarmIndicator whether the current record uses farm treatment, using the
    ///   maintained code
    public void setCurrentYearFarmIndicator(String currentYearFarmIndicator) {
        this.currentYearFarmIndicator = currentYearFarmIndicator;
    }

    /// Returns the persisted signed final equalized-value difference in whole dollars.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#currentYearFinalEqualizedValueDifference()}
    /// for the domain meaning. The database column has precision 9 and scale 0. This accessor does
    /// not round or check the value.
    ///
    /// @return signed final equalized-value difference in whole dollars
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getCurrentYearFinalEqualizedValueDifference() {
        return Objects.requireNonNull(
                currentYearFinalEqualizedValueDifference,
                "currentYearFinalEqualizedValueDifference must be populated before reading");
    }

    /// Stores the signed final equalized-value difference in whole dollars without validation or
    /// normalization.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#currentYearFinalEqualizedValueDifference()}
    /// for the domain meaning. The database column has precision 9 and scale 0. This accessor does
    /// not round or check the value.
    ///
    /// @param currentYearFinalEqualizedValueDifference signed final equalized-value difference in
    ///   whole dollars
    public void setCurrentYearFinalEqualizedValueDifference(
            BigDecimal currentYearFinalEqualizedValueDifference) {
        this.currentYearFinalEqualizedValueDifference = currentYearFinalEqualizedValueDifference;
    }

    /// Returns the persisted current full assessed valuation in whole dollars.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#currentYearFullAssessedValue()} for the
    /// domain meaning. The database column has precision 9 and scale 0. This accessor does not
    /// round or check the value.
    ///
    /// @return current full assessed valuation in whole dollars
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getCurrentYearFullAssessedValue() {
        return Objects.requireNonNull(
                currentYearFullAssessedValue,
                "currentYearFullAssessedValue must be populated before reading");
    }

    /// Stores the current full assessed valuation in whole dollars without validation or
    /// normalization.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#currentYearFullAssessedValue()} for the
    /// domain meaning. The database column has precision 9 and scale 0. This accessor does not
    /// round or check the value.
    ///
    /// @param currentYearFullAssessedValue current full assessed valuation in whole dollars
    public void setCurrentYearFullAssessedValue(BigDecimal currentYearFullAssessedValue) {
        this.currentYearFullAssessedValue = currentYearFullAssessedValue;
    }

    /// Returns the persisted current full equalized valuation in whole dollars.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#currentYearFullEqualizedValue()} for the
    /// domain meaning. The database column has precision 9 and scale 0. This accessor does not
    /// round or check the value.
    ///
    /// @return current full equalized valuation in whole dollars
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getCurrentYearFullEqualizedValue() {
        return Objects.requireNonNull(
                currentYearFullEqualizedValue,
                "currentYearFullEqualizedValue must be populated before reading");
    }

    /// Stores the current full equalized valuation in whole dollars without validation or
    /// normalization.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#currentYearFullEqualizedValue()} for the
    /// domain meaning. The database column has precision 9 and scale 0. This accessor does not
    /// round or check the value.
    ///
    /// @param currentYearFullEqualizedValue current full equalized valuation in whole dollars
    public void setCurrentYearFullEqualizedValue(BigDecimal currentYearFullEqualizedValue) {
        this.currentYearFullEqualizedValue = currentYearFullEqualizedValue;
    }

    /// Returns the persisted current ineligible assessed valuation in whole dollars.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#currentYearNotEligibleAssessedValue()}
    /// for the domain meaning. The database column has precision 9 and scale 0. This accessor does
    /// not round or check the value.
    ///
    /// @return current ineligible assessed valuation in whole dollars
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getCurrentYearNotEligibleAssessedValue() {
        return Objects.requireNonNull(
                currentYearNotEligibleAssessedValue,
                "currentYearNotEligibleAssessedValue must be populated before reading");
    }

    /// Stores the current ineligible assessed valuation in whole dollars without validation or
    /// normalization.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#currentYearNotEligibleAssessedValue()}
    /// for the domain meaning. The database column has precision 9 and scale 0. This accessor does
    /// not round or check the value.
    ///
    /// @param currentYearNotEligibleAssessedValue current ineligible assessed valuation in whole
    ///   dollars
    public void setCurrentYearNotEligibleAssessedValue(
            BigDecimal currentYearNotEligibleAssessedValue) {
        this.currentYearNotEligibleAssessedValue = currentYearNotEligibleAssessedValue;
    }

    /// Returns the persisted current ineligible equalized valuation in whole dollars.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#currentYearNotEligibleEqualizedValue()}
    /// for the domain meaning. The database column has precision 9 and scale 0. This accessor does
    /// not round or check the value.
    ///
    /// @return current ineligible equalized valuation in whole dollars
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getCurrentYearNotEligibleEqualizedValue() {
        return Objects.requireNonNull(
                currentYearNotEligibleEqualizedValue,
                "currentYearNotEligibleEqualizedValue must be populated before reading");
    }

    /// Stores the current ineligible equalized valuation in whole dollars without validation or
    /// normalization.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#currentYearNotEligibleEqualizedValue()}
    /// for the domain meaning. The database column has precision 9 and scale 0. This accessor does
    /// not round or check the value.
    ///
    /// @param currentYearNotEligibleEqualizedValue current ineligible equalized valuation in whole
    ///   dollars
    public void setCurrentYearNotEligibleEqualizedValue(
            BigDecimal currentYearNotEligibleEqualizedValue) {
        this.currentYearNotEligibleEqualizedValue = currentYearNotEligibleEqualizedValue;
    }

    /// Returns the persisted number of homeowner units.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#homeownerUnits()} for the
    /// domain meaning.
    ///
    /// @return number of homeowner units
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getHomeownerUnits() {
        return Objects.requireNonNull(
                homeownerUnits, "homeownerUnits must be populated before reading");
    }

    /// Stores the number of homeowner units without validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#homeownerUnits()} for the
    /// domain meaning.
    ///
    /// @param homeownerUnits number of homeowner units
    public void setHomeownerUnits(Integer homeownerUnits) {
        this.homeownerUnits = homeownerUnits;
    }

    /// Returns the persisted number of homestead units.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#homesteadUnits()} for the
    /// domain meaning.
    ///
    /// @return number of homestead units
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getHomesteadUnits() {
        return Objects.requireNonNull(
                homesteadUnits, "homesteadUnits must be populated before reading");
    }

    /// Stores the number of homestead units without validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#homesteadUnits()} for the
    /// domain meaning.
    ///
    /// @param homesteadUnits number of homestead units
    public void setHomesteadUnits(Integer homesteadUnits) {
        this.homesteadUnits = homesteadUnits;
    }

    /// Returns the persisted canonical source-width parcel identifier selected from qualifying
    /// assessment details.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#keyParcelNumber()} for the
    /// domain meaning. The database column permits up to 14 characters. Leading zeros are
    /// significant, and this accessor does not pad or validate digits.
    ///
    /// @return canonical source-width parcel identifier selected from qualifying assessment details
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getKeyParcelNumber() {
        return Objects.requireNonNull(
                keyParcelNumber, "keyParcelNumber must be populated before reading");
    }

    /// Stores the canonical source-width parcel identifier selected from qualifying assessment
    /// details without validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#keyParcelNumber()} for the
    /// domain meaning. The database column permits up to 14 characters. Leading zeros are
    /// significant, and this accessor does not pad or validate digits.
    ///
    /// @param keyParcelNumber canonical source-width parcel identifier selected from qualifying
    ///   assessment details
    public void setKeyParcelNumber(String keyParcelNumber) {
        this.keyParcelNumber = keyParcelNumber;
    }

    /// Returns the persisted mailing city retained from the applicant or mailing record.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#mailingCity()} for the domain
    /// meaning.
    ///
    /// @return mailing city retained from the applicant or mailing record
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getMailingCity() {
        return Objects.requireNonNull(mailingCity, "mailingCity must be populated before reading");
    }

    /// Stores the mailing city retained from the applicant or mailing record without validation or
    /// normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#mailingCity()} for the domain
    /// meaning.
    ///
    /// @param mailingCity mailing city retained from the applicant or mailing record
    public void setMailingCity(String mailingCity) {
        this.mailingCity = mailingCity;
    }

    /// Returns the persisted mailing direction retained from the applicant or mailing record.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#mailingDirection()} for the
    /// domain meaning.
    ///
    /// @return mailing direction retained from the applicant or mailing record
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getMailingDirection() {
        return Objects.requireNonNull(
                mailingDirection, "mailingDirection must be populated before reading");
    }

    /// Stores the mailing direction retained from the applicant or mailing record without
    /// validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#mailingDirection()} for the
    /// domain meaning.
    ///
    /// @param mailingDirection mailing direction retained from the applicant or mailing record
    public void setMailingDirection(String mailingDirection) {
        this.mailingDirection = mailingDirection;
    }

    /// Returns the persisted house-number characters retained from the mailing record.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#mailingHouseNumber()} for the
    /// domain meaning.
    ///
    /// @return house-number characters retained from the mailing record
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getMailingHouseNumber() {
        return Objects.requireNonNull(
                mailingHouseNumber, "mailingHouseNumber must be populated before reading");
    }

    /// Stores the house-number characters retained from the mailing record without validation or
    /// normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#mailingHouseNumber()} for the
    /// domain meaning.
    ///
    /// @param mailingHouseNumber house-number characters retained from the mailing record
    public void setMailingHouseNumber(String mailingHouseNumber) {
        this.mailingHouseNumber = mailingHouseNumber;
    }

    /// Returns the persisted mailing state code.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#mailingState()} for the domain
    /// meaning.
    ///
    /// @return mailing state code
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getMailingState() {
        return Objects.requireNonNull(
                mailingState, "mailingState must be populated before reading");
    }

    /// Stores the mailing state code without validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#mailingState()} for the domain
    /// meaning.
    ///
    /// @param mailingState mailing state code
    public void setMailingState(String mailingState) {
        this.mailingState = mailingState;
    }

    /// Returns the persisted mailing street name.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#mailingStreet()} for the
    /// domain meaning.
    ///
    /// @return mailing street name
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getMailingStreet() {
        return Objects.requireNonNull(
                mailingStreet, "mailingStreet must be populated before reading");
    }

    /// Stores the mailing street name without validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#mailingStreet()} for the
    /// domain meaning.
    ///
    /// @param mailingStreet mailing street name
    public void setMailingStreet(String mailingStreet) {
        this.mailingStreet = mailingStreet;
    }

    /// Returns the persisted mailing street suffix.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#mailingSuffix()} for the
    /// domain meaning.
    ///
    /// @return mailing street suffix
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getMailingSuffix() {
        return Objects.requireNonNull(
                mailingSuffix, "mailingSuffix must be populated before reading");
    }

    /// Stores the mailing street suffix without validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#mailingSuffix()} for the
    /// domain meaning.
    ///
    /// @param mailingSuffix mailing street suffix
    public void setMailingSuffix(String mailingSuffix) {
        this.mailingSuffix = mailingSuffix;
    }

    /// Returns the persisted canonical nine-digit postal identifier.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#mailingZipCode()} for the
    /// domain meaning. The database column permits up to 9 characters. Leading zeros are
    /// significant, and this accessor does not pad or validate digits.
    ///
    /// @return canonical nine-digit postal identifier
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getMailingZipCode() {
        return Objects.requireNonNull(
                mailingZipCode, "mailingZipCode must be populated before reading");
    }

    /// Stores the canonical nine-digit postal identifier without validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#mailingZipCode()} for the
    /// domain meaning. The database column permits up to 9 characters. Leading zeros are
    /// significant, and this accessor does not pad or validate digits.
    ///
    /// @param mailingZipCode canonical nine-digit postal identifier
    public void setMailingZipCode(String mailingZipCode) {
        this.mailingZipCode = mailingZipCode;
    }

    /// Returns the persisted maintained record-maintenance code.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#maintenanceIndicator()} for
    /// the domain meaning.
    ///
    /// @return maintained record-maintenance code
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getMaintenanceIndicator() {
        return Objects.requireNonNull(
                maintenanceIndicator, "maintenanceIndicator must be populated before reading");
    }

    /// Stores the maintained record-maintenance code without validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#maintenanceIndicator()} for
    /// the domain meaning.
    ///
    /// @param maintenanceIndicator maintained record-maintenance code
    public void setMaintenanceIndicator(Integer maintenanceIndicator) {
        this.maintenanceIndicator = maintenanceIndicator;
    }

    /// Returns the persisted applicant or owner name retained with the parcel.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#masterName()} for the domain
    /// meaning.
    ///
    /// @return applicant or owner name retained with the parcel
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getMasterName() {
        return Objects.requireNonNull(masterName, "masterName must be populated before reading");
    }

    /// Stores the applicant or owner name retained with the parcel without validation or
    /// normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#masterName()} for the domain
    /// meaning.
    ///
    /// @param masterName applicant or owner name retained with the parcel
    public void setMasterName(String masterName) {
        this.masterName = masterName;
    }

    /// Returns the persisted occupancy percentage used by exemption eligibility.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#occupancyFactor()} for the
    /// domain meaning. The database column has precision 5 and scale 1. This accessor does not
    /// round or check the value.
    ///
    /// @return occupancy percentage used by exemption eligibility
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getOccupancyFactor() {
        return Objects.requireNonNull(
                occupancyFactor, "occupancyFactor must be populated before reading");
    }

    /// Stores the occupancy percentage used by exemption eligibility without validation or
    /// normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#occupancyFactor()} for the
    /// domain meaning. The database column has precision 5 and scale 1. This accessor does not
    /// round or check the value.
    ///
    /// @param occupancyFactor occupancy percentage used by exemption eligibility
    public void setOccupancyFactor(BigDecimal occupancyFactor) {
        this.occupancyFactor = occupancyFactor;
    }

    /// Returns the persisted base-value year before the current calculation.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#originalBaseValueYear()} for
    /// the domain meaning.
    ///
    /// @return base-value year before the current calculation
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getOriginalBaseValueYear() {
        return Objects.requireNonNull(
                originalBaseValueYear, "originalBaseValueYear must be populated before reading");
    }

    /// Stores the base-value year before the current calculation without validation or
    /// normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#originalBaseValueYear()} for
    /// the domain meaning.
    ///
    /// @param originalBaseValueYear base-value year before the current calculation
    public void setOriginalBaseValueYear(Integer originalBaseValueYear) {
        this.originalBaseValueYear = originalBaseValueYear;
    }

    /// Returns the persisted prior eligible base-year full assessed valuation in whole dollars.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#originalBaseYearEligibleComputedFullAssessedValue()}
    /// for the domain meaning. The database column has precision 9 and scale 0. This accessor does
    /// not round or check the value.
    ///
    /// @return prior eligible base-year full assessed valuation in whole dollars
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getOriginalBaseYearEligibleComputedFullAssessedValue() {
        return Objects.requireNonNull(
                originalBaseYearEligibleComputedFullAssessedValue,
                "originalBaseYearEligibleComputedFullAssessedValue must be populated before"
                    + " reading");
    }

    /// Stores the prior eligible base-year full assessed valuation in whole dollars without
    /// validation or normalization.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#originalBaseYearEligibleComputedFullAssessedValue()}
    /// for the domain meaning. The database column has precision 9 and scale 0. This accessor does
    /// not round or check the value.
    ///
    /// @param originalBaseYearEligibleComputedFullAssessedValue prior eligible base-year full
    ///   assessed valuation in whole dollars
    public void setOriginalBaseYearEligibleComputedFullAssessedValue(
            BigDecimal originalBaseYearEligibleComputedFullAssessedValue) {
        this.originalBaseYearEligibleComputedFullAssessedValue =
                originalBaseYearEligibleComputedFullAssessedValue;
    }

    /// Returns the persisted prior base-year equalized valuation in whole dollars.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#originalBaseYearEqualizedValue()} for the
    /// domain meaning. The database column has precision 9 and scale 0. This accessor does not
    /// round or check the value.
    ///
    /// @return prior base-year equalized valuation in whole dollars
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getOriginalBaseYearEqualizedValue() {
        return Objects.requireNonNull(
                originalBaseYearEqualizedValue,
                "originalBaseYearEqualizedValue must be populated before reading");
    }

    /// Stores the prior base-year equalized valuation in whole dollars without validation or
    /// normalization.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#originalBaseYearEqualizedValue()} for the
    /// domain meaning. The database column has precision 9 and scale 0. This accessor does not
    /// round or check the value.
    ///
    /// @param originalBaseYearEqualizedValue prior base-year equalized valuation in whole dollars
    public void setOriginalBaseYearEqualizedValue(BigDecimal originalBaseYearEqualizedValue) {
        this.originalBaseYearEqualizedValue = originalBaseYearEqualizedValue;
    }

    /// Returns the persisted prior base-year full assessed valuation in whole dollars.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#originalBaseYearFullAssessedValue()} for
    /// the domain meaning. The database column has precision 9 and scale 0. This accessor does not
    /// round or check the value.
    ///
    /// @return prior base-year full assessed valuation in whole dollars
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getOriginalBaseYearFullAssessedValue() {
        return Objects.requireNonNull(
                originalBaseYearFullAssessedValue,
                "originalBaseYearFullAssessedValue must be populated before reading");
    }

    /// Stores the prior base-year full assessed valuation in whole dollars without validation or
    /// normalization.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#originalBaseYearFullAssessedValue()} for
    /// the domain meaning. The database column has precision 9 and scale 0. This accessor does not
    /// round or check the value.
    ///
    /// @param originalBaseYearFullAssessedValue prior base-year full assessed valuation in whole
    ///   dollars
    public void setOriginalBaseYearFullAssessedValue(BigDecimal originalBaseYearFullAssessedValue) {
        this.originalBaseYearFullAssessedValue = originalBaseYearFullAssessedValue;
    }

    /// Returns the persisted prior total eligible base-year computed equalized valuation in whole
    /// dollars.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#originalBaseYearTotalEligibleComputedEqualizedValue()}
    /// for the domain meaning. The database column has precision 9 and scale 0. This accessor does
    /// not round or check the value.
    ///
    /// @return prior total eligible base-year computed equalized valuation in whole dollars
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getOriginalBaseYearTotalEligibleComputedEqualizedValue() {
        return Objects.requireNonNull(
                originalBaseYearTotalEligibleComputedEqualizedValue,
                "originalBaseYearTotalEligibleComputedEqualizedValue must be populated before"
                    + " reading");
    }

    /// Stores the prior total eligible base-year computed equalized valuation in whole dollars
    /// without validation or normalization.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#originalBaseYearTotalEligibleComputedEqualizedValue()}
    /// for the domain meaning. The database column has precision 9 and scale 0. This accessor does
    /// not round or check the value.
    ///
    /// @param originalBaseYearTotalEligibleComputedEqualizedValue prior total eligible base-year
    ///   computed equalized valuation in whole dollars
    public void setOriginalBaseYearTotalEligibleComputedEqualizedValue(
            BigDecimal originalBaseYearTotalEligibleComputedEqualizedValue) {
        this.originalBaseYearTotalEligibleComputedEqualizedValue =
                originalBaseYearTotalEligibleComputedEqualizedValue;
    }

    /// Returns the persisted prior signed current-year equalized-value difference in whole dollars.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#originalCurrentYearFinalEqualizedValueDifference()}
    /// for the domain meaning. The database column has precision 9 and scale 0. This accessor does
    /// not round or check the value.
    ///
    /// @return prior signed current-year equalized-value difference in whole dollars
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getOriginalCurrentYearFinalEqualizedValueDifference() {
        return Objects.requireNonNull(
                originalCurrentYearFinalEqualizedValueDifference,
                "originalCurrentYearFinalEqualizedValueDifference must be populated before"
                    + " reading");
    }

    /// Stores the prior signed current-year equalized-value difference in whole dollars without
    /// validation or normalization.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#originalCurrentYearFinalEqualizedValueDifference()}
    /// for the domain meaning. The database column has precision 9 and scale 0. This accessor does
    /// not round or check the value.
    ///
    /// @param originalCurrentYearFinalEqualizedValueDifference prior signed current-year
    ///   equalized-value difference in whole dollars
    public void setOriginalCurrentYearFinalEqualizedValueDifference(
            BigDecimal originalCurrentYearFinalEqualizedValueDifference) {
        this.originalCurrentYearFinalEqualizedValueDifference =
                originalCurrentYearFinalEqualizedValueDifference;
    }

    /// Returns the persisted prior manual-calculation code.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#originalManualCalculationIndicator()} for
    /// the domain meaning.
    ///
    /// @return prior manual-calculation code
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getOriginalManualCalculationIndicator() {
        return Objects.requireNonNull(
                originalManualCalculationIndicator,
                "originalManualCalculationIndicator must be populated before reading");
    }

    /// Stores the prior manual-calculation code without validation or normalization.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeMaster#originalManualCalculationIndicator()} for
    /// the domain meaning.
    ///
    /// @param originalManualCalculationIndicator prior manual-calculation code
    public void setOriginalManualCalculationIndicator(String originalManualCalculationIndicator) {
        this.originalManualCalculationIndicator = originalManualCalculationIndicator;
    }

    /// Returns the persisted eligible parcel share as a decimal fraction with six fractional
    /// digits.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#propertyProration()} for the
    /// domain meaning. The database column has precision 7 and scale 6. This accessor does not
    /// round or check the value.
    ///
    /// @return eligible parcel share as a decimal fraction with six fractional digits
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getPropertyProration() {
        return Objects.requireNonNull(
                propertyProration, "propertyProration must be populated before reading");
    }

    /// Stores the eligible parcel share as a decimal fraction with six fractional digits without
    /// validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#propertyProration()} for the
    /// domain meaning. The database column has precision 7 and scale 6. This accessor does not
    /// round or check the value.
    ///
    /// @param propertyProration eligible parcel share as a decimal fraction with six fractional
    ///   digits
    public void setPropertyProration(BigDecimal propertyProration) {
        this.propertyProration = propertyProration;
    }

    /// Returns the persisted record category within the Senior Freeze or homeowner output.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#recordCode()} for the domain
    /// meaning.
    ///
    /// @return record category within the Senior Freeze or homeowner output
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getRecordCode() {
        return Objects.requireNonNull(recordCode, "recordCode must be populated before reading");
    }

    /// Stores the record category within the Senior Freeze or homeowner output without validation
    /// or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#recordCode()} for the domain
    /// meaning.
    ///
    /// @param recordCode record category within the Senior Freeze or homeowner output
    public void setRecordCode(String recordCode) {
        this.recordCode = recordCode;
    }

    /// Returns the persisted number of Senior Freeze cooperative shares.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#seniorFreezeShares()} for the
    /// domain meaning.
    ///
    /// @return number of Senior Freeze cooperative shares
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getSeniorFreezeShares() {
        return Objects.requireNonNull(
                seniorFreezeShares, "seniorFreezeShares must be populated before reading");
    }

    /// Stores the number of Senior Freeze cooperative shares without validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#seniorFreezeShares()} for the
    /// domain meaning.
    ///
    /// @param seniorFreezeShares number of Senior Freeze cooperative shares
    public void setSeniorFreezeShares(Integer seniorFreezeShares) {
        this.seniorFreezeShares = seniorFreezeShares;
    }

    /// Returns the persisted property split classification when qualifying detail supplied it.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#splitCode()} for the domain
    /// meaning.
    ///
    /// @return property split classification when qualifying detail supplied it
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getSplitCode() {
        return Objects.requireNonNull(splitCode, "splitCode must be populated before reading");
    }

    /// Stores the property split classification when qualifying detail supplied it without
    /// validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeMaster#splitCode()} for the domain
    /// meaning.
    ///
    /// @param splitCode property split classification when qualifying detail supplied it
    public void setSplitCode(Integer splitCode) {
        this.splitCode = splitCode;
    }

    // GENERATED-ACCESSORS:end
}
