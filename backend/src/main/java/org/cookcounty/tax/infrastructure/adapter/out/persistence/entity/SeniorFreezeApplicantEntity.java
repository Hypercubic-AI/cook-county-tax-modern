package org.cookcounty.tax.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.util.Objects;

// GENERATED-IMPORTS:start

import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;

// GENERATED-IMPORTS:end

/// Mutable persistence boundary for one Senior Freeze applicant snapshot.
///
/// JPA creates an incomplete instance before it hydrates the fields. The generated identity is
/// absent until insertion, and the optimistic-lock version is absent until JPA inserts or hydrates
/// the row. JPA changes the version after successful updates to detect concurrent writes.
///
/// Accessors return or assign the stored representation without domain normalization. They do not
/// pad identifiers, constrain decimal precision, or parse the historical date representations. In
/// particular, nonzero date values can remain noncalendar compatibility data. Construct {@link
/// org.cookcounty.tax.domain.model.SeniorFreezeApplicant} to apply its numeric boundaries. Callers
/// must populate all non-null domain fields before mapping this entity to that record.
@Entity
@Table(name = "senior_freeze_applicants")
public class SeniorFreezeApplicantEntity {

    /// Database-generated surrogate identity. It is null until JPA assigns or hydrates it.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    /// Optimistic-lock value managed by JPA. It is null before JPA inserts or hydrates the entity.
    @Version private @Nullable Long version;

    // GENERATED-FIELDS:start

    /// Applicant age in whole years.
    @Column(name = "age")
    private @Nullable Integer age;

    /// Applicant address retained from the applicant or mailing record. The database representation
    /// is `CHARACTER VARYING(22)`.
    @Column(name = "applicant_address")
    private @Nullable String applicantAddress;

    /// Applicant city retained from the applicant or mailing record. The database representation is
    /// `CHARACTER VARYING(12)`.
    @Column(name = "applicant_city")
    private @Nullable String applicantCity;

    /// Applicant first name retained from the applicant or mailing record. The database
    /// representation is `CHARACTER VARYING(15)`.
    @Column(name = "applicant_first_name")
    private @Nullable String applicantFirstName;

    /// Applicant last name retained from the applicant or mailing record. The database
    /// representation is `CHARACTER VARYING(20)`.
    @Column(name = "applicant_last_name")
    private @Nullable String applicantLastName;

    /// Applicant middle initial retained from the applicant or mailing record. The database
    /// representation is `CHARACTER VARYING(1)`.
    @Column(name = "applicant_middle_initial")
    private @Nullable String applicantMiddleInitial;

    /// Applicant old name retained from the applicant or mailing record. The database
    /// representation is `CHARACTER VARYING(22)`.
    @Column(name = "applicant_old_name")
    private @Nullable String applicantOldName;

    /// Applicant state retained from the applicant or mailing record. The database representation
    /// is `CHARACTER VARYING(2)`.
    @Column(name = "applicant_state")
    private @Nullable String applicantState;

    /// Applicant title retained from the applicant or mailing record. The database representation
    /// is `CHARACTER VARYING(2)`.
    @Column(name = "applicant_title")
    private @Nullable String applicantTitle;

    /// Canonical nine-digit postal identifier.
    @Column(name = "applicant_zip_code", length = 9)
    private @Nullable String applicantZipCode;

    /// Year selected for the frozen base value.
    @Column(name = "base_year")
    private @Nullable Integer baseYear;

    /// Eligible base-year equalized valuation in whole dollars. The database representation is
    /// `NUMERIC(9,0)`.
    @Column(name = "base_year_eligible_equalized_value", precision = 9, scale = 0)
    private @Nullable BigDecimal baseYearEligibleEqualizedValue;

    /// Maintained code that identifies base-year treatment. The database representation is
    /// `CHARACTER VARYING(1)`.
    @Column(name = "base_year_indicator")
    private @Nullable String baseYearIndicator;

    /// Processing batch identifier.
    @Column(name = "batch_number")
    private @Nullable Integer batchNumber;

    /// Birth-date key in the `MMDDCCYY` character layout; accepted noncalendar text remains
    /// unchanged. The database representation is `CHARACTER VARYING(8)`. This compatibility value
    /// is not parsed or validated as a calendar date here.
    @Column(name = "birth_date", length = 8)
    private @Nullable String birthDate;

    /// Civil-service benefit portion of household income in dollars with two fractional digits. The
    /// database representation is `NUMERIC(9,2)`.
    @Column(name = "civil_service_benefits", precision = 9, scale = 2)
    private @Nullable BigDecimal civilServiceBenefits;

    /// Number of cooperative shares claimed by the applicant.
    @Column(name = "cooperative_senior_shares")
    private @Nullable Integer cooperativeSeniorShares;

    /// Denial date in numeric `CCYYMMDD` layout; zero is absent and other source values remain
    /// unchanged. This compatibility value is not parsed or validated as a calendar date here.
    @Column(name = "denial_date")
    private @Nullable Integer denialDate;

    /// First received date in numeric `CCYYMMDD` layout; zero is absent and other source values
    /// remain unchanged. This compatibility value is not parsed or validated as a calendar date
    /// here.
    @Column(name = "first_application_date")
    private @Nullable Integer firstApplicationDate;

    /// Year selected for the applicant's homeowner base.
    @Column(name = "homeowner_base_year")
    private @Nullable Integer homeownerBaseYear;

    /// Applicant homeowner base-year assessed valuation in whole dollars. The database
    /// representation is `NUMERIC(9,0)`.
    @Column(name = "homeowner_base_year_assessed_value", precision = 9, scale = 0)
    private @Nullable BigDecimal homeownerBaseYearAssessedValue;

    /// Applicant homeowner base-year factor with four fractional digits. The database
    /// representation is `NUMERIC(5,4)`.
    @Column(name = "homeowner_base_year_equalization_factor", precision = 5, scale = 4)
    private @Nullable BigDecimal homeownerBaseYearEqualizationFactor;

    /// Applicant homeowner base-year equalized valuation in whole dollars. The database
    /// representation is `NUMERIC(9,0)`.
    @Column(name = "homeowner_base_year_equalized_value", precision = 9, scale = 0)
    private @Nullable BigDecimal homeownerBaseYearEqualizedValue;

    /// Maintained code for homeowner eligibility.
    @Column(name = "homeowner_eligibility_indicator")
    private @Nullable Integer homeownerEligibilityIndicator;

    /// Maintained homeowner disposition code. The database representation is `CHARACTER
    /// VARYING(1)`.
    @Column(name = "homeowner_status")
    private @Nullable String homeownerStatus;

    /// Homestead processing batch identifier.
    @Column(name = "homestead_batch_number")
    private @Nullable Integer homesteadBatchNumber;

    /// Homestead share percentage with three fractional digits. The database representation is
    /// `NUMERIC(6,3)`.
    @Column(name = "homestead_percent_shares", precision = 6, scale = 3)
    private @Nullable BigDecimal homesteadPercentShares;

    /// Number of homestead shares.
    @Column(name = "homestead_shares")
    private @Nullable Integer homesteadShares;

    /// Maintained homestead disposition code. The database representation is `CHARACTER
    /// VARYING(1)`.
    @Column(name = "homestead_status")
    private @Nullable String homesteadStatus;

    /// Year associated with the homestead application.
    @Column(name = "homestead_year_applied")
    private @Nullable Integer homesteadYearApplied;

    /// Interest portion of household income in dollars with two fractional digits. The database
    /// representation is `NUMERIC(9,2)`.
    @Column(name = "interest_income", precision = 9, scale = 2)
    private @Nullable BigDecimal interestIncome;

    /// Latest received date in numeric `CCYYMMDD` layout; zero is absent and other source values
    /// remain unchanged. This compatibility value is not parsed or validated as a calendar date
    /// here.
    @Column(name = "last_application_date")
    private @Nullable Integer lastApplicationDate;

    /// Maintained code for life-care-facility status. The database representation is `CHARACTER
    /// VARYING(1)`.
    @Column(name = "life_care_facility_indicator")
    private @Nullable String lifeCareFacilityIndicator;

    /// Maintained record-maintenance code.
    @Column(name = "maintenance_indicator")
    private @Nullable Integer maintenanceIndicator;

    /// Maintained name-maintenance code.
    @Column(name = "name_maintenance_indicator")
    private @Nullable Integer nameMaintenanceIndicator;

    /// Net-capital-gain portion of household income in dollars with two fractional digits. The
    /// database representation is `NUMERIC(9,2)`.
    @Column(name = "net_capital_gain", precision = 9, scale = 2)
    private @Nullable BigDecimal netCapitalGain;

    /// Net-rental portion of household income in dollars with two fractional digits. The database
    /// representation is `NUMERIC(9,2)`.
    @Column(name = "net_rental_income", precision = 9, scale = 2)
    private @Nullable BigDecimal netRentalIncome;

    /// Maintained code that declares no household income. The database representation is `CHARACTER
    /// VARYING(1)`.
    @Column(name = "no_income_indicator")
    private @Nullable String noIncomeIndicator;

    /// Maintained code that records notarization. The database representation is `CHARACTER
    /// VARYING(1)`.
    @Column(name = "notarized_indicator")
    private @Nullable String notarizedIndicator;

    /// Other-benefit portion of household income in dollars with two fractional digits. The
    /// database representation is `NUMERIC(9,2)`.
    @Column(name = "other_benefits", precision = 9, scale = 2)
    private @Nullable BigDecimal otherBenefits;

    /// Other portion of household income in dollars with two fractional digits. The database
    /// representation is `NUMERIC(9,2)`.
    @Column(name = "other_income", precision = 9, scale = 2)
    private @Nullable BigDecimal otherIncome;

    /// Applicant share as a decimal fraction with six fractional digits. The database
    /// representation is `NUMERIC(6,6)`.
    @Column(name = "percent_senior_shares", precision = 6, scale = 6)
    private @Nullable BigDecimal percentSeniorShares;

    /// Canonical ten-digit telephone identifier.
    @Column(name = "phone_number", length = 10)
    private @Nullable String phoneNumber;

    /// Public-aid portion of household income in dollars with two fractional digits. The database
    /// representation is `NUMERIC(9,2)`.
    @Column(name = "public_aid", precision = 9, scale = 2)
    private @Nullable BigDecimal publicAid;

    /// Qualification date in numeric `CCYYMMDD` layout; zero is absent and other source values
    /// remain unchanged. This compatibility value is not parsed or validated as a calendar date
    /// here.
    @Column(name = "qualification_date")
    private @Nullable Integer qualificationDate;

    /// Railroad-benefit portion of household income in dollars with two fractional digits. The
    /// database representation is `NUMERIC(9,2)`.
    @Column(name = "railroad_benefits", precision = 9, scale = 2)
    private @Nullable BigDecimal railroadBenefits;

    /// Returned date in numeric `CCYYMMDD` layout; zero is absent and other source values remain
    /// unchanged. This compatibility value is not parsed or validated as a calendar date here.
    @Column(name = "returned_date")
    private @Nullable Integer returnedDate;

    /// Senior Freeze percentage with one fractional digit. The database representation is
    /// `NUMERIC(2,1)`.
    @Column(name = "senior_freeze_percent", precision = 2, scale = 1)
    private @Nullable BigDecimal seniorFreezePercent;

    /// Maintained application disposition code. The database representation is `CHARACTER
    /// VARYING(1)`.
    @Column(name = "senior_freeze_status")
    private @Nullable String seniorFreezeStatus;

    /// Maintained code that records the applicant signature. The database representation is
    /// `CHARACTER VARYING(1)`.
    @Column(name = "signed_indicator")
    private @Nullable String signedIndicator;

    /// Social Security portion of household income in dollars with two fractional digits. The
    /// database representation is `NUMERIC(9,2)`.
    @Column(name = "social_security_income", precision = 9, scale = 2)
    private @Nullable BigDecimal socialSecurityIncome;

    /// Canonical 11-digit source identifier.
    @Column(name = "social_security_number", length = 11)
    private @Nullable String socialSecurityNumber;

    /// Applicant household income total in dollars with two fractional digits. The database
    /// representation is `NUMERIC(9,2)`.
    @Column(name = "total_income", precision = 9, scale = 2)
    private @Nullable BigDecimal totalIncome;

    /// Veterans-benefit portion of household income in dollars with two fractional digits. The
    /// database representation is `NUMERIC(9,2)`.
    @Column(name = "veterans_benefits", precision = 9, scale = 2)
    private @Nullable BigDecimal veteransBenefits;

    /// Wage portion of household income in dollars with two fractional digits. The database
    /// representation is `NUMERIC(9,2)`.
    @Column(name = "wages", precision = 9, scale = 2)
    private @Nullable BigDecimal wages;

    // GENERATED-FIELDS:end

    // GENERATED-ACCESSORS:start
    /// Returns the database-generated surrogate identity.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#id()} for the domain
    /// identity contract.
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
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#version()} for the domain
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

    /// Returns the persisted applicant age in whole years.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#age()} for the domain
    /// meaning.
    ///
    /// @return applicant age in whole years
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getAge() {
        return Objects.requireNonNull(age, "age must be populated before reading");
    }

    /// Stores the applicant age in whole years without validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#age()} for the domain
    /// meaning.
    ///
    /// @param age applicant age in whole years
    public void setAge(Integer age) {
        this.age = age;
    }

    /// Returns the persisted applicant address retained from the applicant or mailing record.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#applicantAddress()} for the
    /// domain meaning.
    ///
    /// @return applicant address retained from the applicant or mailing record
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getApplicantAddress() {
        return Objects.requireNonNull(
                applicantAddress, "applicantAddress must be populated before reading");
    }

    /// Stores the applicant address retained from the applicant or mailing record without
    /// validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#applicantAddress()} for the
    /// domain meaning.
    ///
    /// @param applicantAddress applicant address retained from the applicant or mailing record
    public void setApplicantAddress(String applicantAddress) {
        this.applicantAddress = applicantAddress;
    }

    /// Returns the persisted applicant city retained from the applicant or mailing record.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#applicantCity()} for the
    /// domain meaning.
    ///
    /// @return applicant city retained from the applicant or mailing record
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getApplicantCity() {
        return Objects.requireNonNull(
                applicantCity, "applicantCity must be populated before reading");
    }

    /// Stores the applicant city retained from the applicant or mailing record without validation
    /// or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#applicantCity()} for the
    /// domain meaning.
    ///
    /// @param applicantCity applicant city retained from the applicant or mailing record
    public void setApplicantCity(String applicantCity) {
        this.applicantCity = applicantCity;
    }

    /// Returns the persisted applicant first name retained from the applicant or mailing record.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#applicantFirstName()} for
    /// the domain meaning.
    ///
    /// @return applicant first name retained from the applicant or mailing record
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getApplicantFirstName() {
        return Objects.requireNonNull(
                applicantFirstName, "applicantFirstName must be populated before reading");
    }

    /// Stores the applicant first name retained from the applicant or mailing record without
    /// validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#applicantFirstName()} for
    /// the domain meaning.
    ///
    /// @param applicantFirstName applicant first name retained from the applicant or mailing record
    public void setApplicantFirstName(String applicantFirstName) {
        this.applicantFirstName = applicantFirstName;
    }

    /// Returns the persisted applicant last name retained from the applicant or mailing record.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#applicantLastName()} for
    /// the domain meaning.
    ///
    /// @return applicant last name retained from the applicant or mailing record
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getApplicantLastName() {
        return Objects.requireNonNull(
                applicantLastName, "applicantLastName must be populated before reading");
    }

    /// Stores the applicant last name retained from the applicant or mailing record without
    /// validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#applicantLastName()} for
    /// the domain meaning.
    ///
    /// @param applicantLastName applicant last name retained from the applicant or mailing record
    public void setApplicantLastName(String applicantLastName) {
        this.applicantLastName = applicantLastName;
    }

    /// Returns the persisted applicant middle initial retained from the applicant or mailing
    /// record.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#applicantMiddleInitial()}
    /// for the domain meaning.
    ///
    /// @return applicant middle initial retained from the applicant or mailing record
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getApplicantMiddleInitial() {
        return Objects.requireNonNull(
                applicantMiddleInitial, "applicantMiddleInitial must be populated before reading");
    }

    /// Stores the applicant middle initial retained from the applicant or mailing record without
    /// validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#applicantMiddleInitial()}
    /// for the domain meaning.
    ///
    /// @param applicantMiddleInitial applicant middle initial retained from the applicant or
    ///   mailing record
    public void setApplicantMiddleInitial(String applicantMiddleInitial) {
        this.applicantMiddleInitial = applicantMiddleInitial;
    }

    /// Returns the persisted applicant old name retained from the applicant or mailing record.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#applicantOldName()} for the
    /// domain meaning.
    ///
    /// @return applicant old name retained from the applicant or mailing record
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getApplicantOldName() {
        return Objects.requireNonNull(
                applicantOldName, "applicantOldName must be populated before reading");
    }

    /// Stores the applicant old name retained from the applicant or mailing record without
    /// validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#applicantOldName()} for the
    /// domain meaning.
    ///
    /// @param applicantOldName applicant old name retained from the applicant or mailing record
    public void setApplicantOldName(String applicantOldName) {
        this.applicantOldName = applicantOldName;
    }

    /// Returns the persisted applicant state retained from the applicant or mailing record.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#applicantState()} for the
    /// domain meaning.
    ///
    /// @return applicant state retained from the applicant or mailing record
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getApplicantState() {
        return Objects.requireNonNull(
                applicantState, "applicantState must be populated before reading");
    }

    /// Stores the applicant state retained from the applicant or mailing record without validation
    /// or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#applicantState()} for the
    /// domain meaning.
    ///
    /// @param applicantState applicant state retained from the applicant or mailing record
    public void setApplicantState(String applicantState) {
        this.applicantState = applicantState;
    }

    /// Returns the persisted applicant title retained from the applicant or mailing record.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#applicantTitle()} for the
    /// domain meaning.
    ///
    /// @return applicant title retained from the applicant or mailing record
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getApplicantTitle() {
        return Objects.requireNonNull(
                applicantTitle, "applicantTitle must be populated before reading");
    }

    /// Stores the applicant title retained from the applicant or mailing record without validation
    /// or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#applicantTitle()} for the
    /// domain meaning.
    ///
    /// @param applicantTitle applicant title retained from the applicant or mailing record
    public void setApplicantTitle(String applicantTitle) {
        this.applicantTitle = applicantTitle;
    }

    /// Returns the persisted canonical nine-digit postal identifier.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#applicantZipCode()} for the
    /// domain meaning. The database column permits up to 9 characters. Leading zeros are
    /// significant, and this accessor does not pad or validate digits.
    ///
    /// @return canonical nine-digit postal identifier
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getApplicantZipCode() {
        return Objects.requireNonNull(
                applicantZipCode, "applicantZipCode must be populated before reading");
    }

    /// Stores the canonical nine-digit postal identifier without validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#applicantZipCode()} for the
    /// domain meaning. The database column permits up to 9 characters. Leading zeros are
    /// significant, and this accessor does not pad or validate digits.
    ///
    /// @param applicantZipCode canonical nine-digit postal identifier
    public void setApplicantZipCode(String applicantZipCode) {
        this.applicantZipCode = applicantZipCode;
    }

    /// Returns the persisted year selected for the frozen base value.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#baseYear()} for the domain
    /// meaning.
    ///
    /// @return year selected for the frozen base value
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getBaseYear() {
        return Objects.requireNonNull(baseYear, "baseYear must be populated before reading");
    }

    /// Stores the year selected for the frozen base value without validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#baseYear()} for the domain
    /// meaning.
    ///
    /// @param baseYear year selected for the frozen base value
    public void setBaseYear(Integer baseYear) {
        this.baseYear = baseYear;
    }

    /// Returns the persisted eligible base-year equalized valuation in whole dollars.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeApplicant#baseYearEligibleEqualizedValue()} for
    /// the domain meaning. The database column has precision 9 and scale 0. This accessor does not
    /// round or check the value.
    ///
    /// @return eligible base-year equalized valuation in whole dollars
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getBaseYearEligibleEqualizedValue() {
        return Objects.requireNonNull(
                baseYearEligibleEqualizedValue,
                "baseYearEligibleEqualizedValue must be populated before reading");
    }

    /// Stores the eligible base-year equalized valuation in whole dollars without validation or
    /// normalization.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeApplicant#baseYearEligibleEqualizedValue()} for
    /// the domain meaning. The database column has precision 9 and scale 0. This accessor does not
    /// round or check the value.
    ///
    /// @param baseYearEligibleEqualizedValue eligible base-year equalized valuation in whole
    ///   dollars
    public void setBaseYearEligibleEqualizedValue(BigDecimal baseYearEligibleEqualizedValue) {
        this.baseYearEligibleEqualizedValue = baseYearEligibleEqualizedValue;
    }

    /// Returns the persisted maintained code that identifies base-year treatment.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#baseYearIndicator()} for
    /// the domain meaning.
    ///
    /// @return maintained code that identifies base-year treatment
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getBaseYearIndicator() {
        return Objects.requireNonNull(
                baseYearIndicator, "baseYearIndicator must be populated before reading");
    }

    /// Stores the maintained code that identifies base-year treatment without validation or
    /// normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#baseYearIndicator()} for
    /// the domain meaning.
    ///
    /// @param baseYearIndicator maintained code that identifies base-year treatment
    public void setBaseYearIndicator(String baseYearIndicator) {
        this.baseYearIndicator = baseYearIndicator;
    }

    /// Returns the persisted processing batch identifier.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#batchNumber()} for the
    /// domain meaning.
    ///
    /// @return processing batch identifier
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getBatchNumber() {
        return Objects.requireNonNull(batchNumber, "batchNumber must be populated before reading");
    }

    /// Stores the processing batch identifier without validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#batchNumber()} for the
    /// domain meaning.
    ///
    /// @param batchNumber processing batch identifier
    public void setBatchNumber(Integer batchNumber) {
        this.batchNumber = batchNumber;
    }

    /// Returns the persisted birth-date key in the `MMDDCCYY` character layout; accepted
    /// noncalendar text remains unchanged.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#birthDate()} for the domain
    /// meaning. This raw compatibility value is not parsed or validated as a calendar date.
    ///
    /// @return birth-date key in the `MMDDCCYY` character layout; accepted noncalendar text remains
    ///   unchanged
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getBirthDate() {
        return Objects.requireNonNull(birthDate, "birthDate must be populated before reading");
    }

    /// Stores the raw birth-date value without parsing or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#birthDate()} for the domain
    /// meaning. This raw compatibility value is not parsed or validated as a calendar date.
    ///
    /// @param birthDate birth-date key in the `MMDDCCYY` character layout; accepted noncalendar
    ///   text remains unchanged
    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    /// Returns the persisted civil-service benefit portion of household income in dollars with two
    /// fractional digits.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#civilServiceBenefits()} for
    /// the domain meaning. The database column has precision 9 and scale 2. This accessor does not
    /// round or check the value.
    ///
    /// @return civil-service benefit portion of household income in dollars with two fractional
    ///   digits
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getCivilServiceBenefits() {
        return Objects.requireNonNull(
                civilServiceBenefits, "civilServiceBenefits must be populated before reading");
    }

    /// Stores the civil-service benefit portion of household income in dollars with two fractional
    /// digits without validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#civilServiceBenefits()} for
    /// the domain meaning. The database column has precision 9 and scale 2. This accessor does not
    /// round or check the value.
    ///
    /// @param civilServiceBenefits civil-service benefit portion of household income in dollars
    ///   with two fractional digits
    public void setCivilServiceBenefits(BigDecimal civilServiceBenefits) {
        this.civilServiceBenefits = civilServiceBenefits;
    }

    /// Returns the persisted number of cooperative shares claimed by the applicant.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#cooperativeSeniorShares()}
    /// for the domain meaning.
    ///
    /// @return number of cooperative shares claimed by the applicant
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getCooperativeSeniorShares() {
        return Objects.requireNonNull(
                cooperativeSeniorShares,
                "cooperativeSeniorShares must be populated before reading");
    }

    /// Stores the number of cooperative shares claimed by the applicant without validation or
    /// normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#cooperativeSeniorShares()}
    /// for the domain meaning.
    ///
    /// @param cooperativeSeniorShares number of cooperative shares claimed by the applicant
    public void setCooperativeSeniorShares(Integer cooperativeSeniorShares) {
        this.cooperativeSeniorShares = cooperativeSeniorShares;
    }

    /// Returns the persisted denial date in numeric `CCYYMMDD` layout; zero is absent and other
    /// source values remain unchanged.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#denialDate()} for the
    /// domain meaning. This raw compatibility value is not parsed or validated as a calendar date.
    ///
    /// @return denial date in numeric `CCYYMMDD` layout; zero is absent and other source values
    ///   remain unchanged
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getDenialDate() {
        return Objects.requireNonNull(denialDate, "denialDate must be populated before reading");
    }

    /// Stores the raw denial-date value without parsing or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#denialDate()} for the
    /// domain meaning. This raw compatibility value is not parsed or validated as a calendar date.
    ///
    /// @param denialDate denial date in numeric `CCYYMMDD` layout; zero is absent and other source
    ///   values remain unchanged
    public void setDenialDate(Integer denialDate) {
        this.denialDate = denialDate;
    }

    /// Returns the persisted first received date in numeric `CCYYMMDD` layout; zero is absent and
    /// other source values remain unchanged.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#firstApplicationDate()} for
    /// the domain meaning. This raw compatibility value is not parsed or validated as a calendar
    /// date.
    ///
    /// @return first received date in numeric `CCYYMMDD` layout; zero is absent and other source
    ///   values remain unchanged
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getFirstApplicationDate() {
        return Objects.requireNonNull(
                firstApplicationDate, "firstApplicationDate must be populated before reading");
    }

    /// Stores the raw first-received-date value without parsing or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#firstApplicationDate()} for
    /// the domain meaning. This raw compatibility value is not parsed or validated as a calendar
    /// date.
    ///
    /// @param firstApplicationDate first received date in numeric `CCYYMMDD` layout; zero is absent
    ///   and other source values remain unchanged
    public void setFirstApplicationDate(Integer firstApplicationDate) {
        this.firstApplicationDate = firstApplicationDate;
    }

    /// Returns the persisted year selected for the applicant's homeowner base.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#homeownerBaseYear()} for
    /// the domain meaning.
    ///
    /// @return year selected for the applicant's homeowner base
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getHomeownerBaseYear() {
        return Objects.requireNonNull(
                homeownerBaseYear, "homeownerBaseYear must be populated before reading");
    }

    /// Stores the year selected for the applicant's homeowner base without validation or
    /// normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#homeownerBaseYear()} for
    /// the domain meaning.
    ///
    /// @param homeownerBaseYear year selected for the applicant's homeowner base
    public void setHomeownerBaseYear(Integer homeownerBaseYear) {
        this.homeownerBaseYear = homeownerBaseYear;
    }

    /// Returns the persisted applicant homeowner base-year assessed valuation in whole dollars.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeApplicant#homeownerBaseYearAssessedValue()} for
    /// the domain meaning. The database column has precision 9 and scale 0. This accessor does not
    /// round or check the value.
    ///
    /// @return applicant homeowner base-year assessed valuation in whole dollars
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getHomeownerBaseYearAssessedValue() {
        return Objects.requireNonNull(
                homeownerBaseYearAssessedValue,
                "homeownerBaseYearAssessedValue must be populated before reading");
    }

    /// Stores the applicant homeowner base-year assessed valuation in whole dollars without
    /// validation or normalization.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeApplicant#homeownerBaseYearAssessedValue()} for
    /// the domain meaning. The database column has precision 9 and scale 0. This accessor does not
    /// round or check the value.
    ///
    /// @param homeownerBaseYearAssessedValue applicant homeowner base-year assessed valuation in
    ///   whole dollars
    public void setHomeownerBaseYearAssessedValue(BigDecimal homeownerBaseYearAssessedValue) {
        this.homeownerBaseYearAssessedValue = homeownerBaseYearAssessedValue;
    }

    /// Returns the persisted applicant homeowner base-year factor with four fractional digits.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeApplicant#homeownerBaseYearEqualizationFactor()}
    /// for the domain meaning. The database column has precision 5 and scale 4. This accessor does
    /// not round or check the value.
    ///
    /// @return applicant homeowner base-year factor with four fractional digits
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getHomeownerBaseYearEqualizationFactor() {
        return Objects.requireNonNull(
                homeownerBaseYearEqualizationFactor,
                "homeownerBaseYearEqualizationFactor must be populated before reading");
    }

    /// Stores the applicant homeowner base-year factor with four fractional digits without
    /// validation or normalization.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeApplicant#homeownerBaseYearEqualizationFactor()}
    /// for the domain meaning. The database column has precision 5 and scale 4. This accessor does
    /// not round or check the value.
    ///
    /// @param homeownerBaseYearEqualizationFactor applicant homeowner base-year factor with four
    ///   fractional digits
    public void setHomeownerBaseYearEqualizationFactor(
            BigDecimal homeownerBaseYearEqualizationFactor) {
        this.homeownerBaseYearEqualizationFactor = homeownerBaseYearEqualizationFactor;
    }

    /// Returns the persisted applicant homeowner base-year equalized valuation in whole dollars.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeApplicant#homeownerBaseYearEqualizedValue()} for
    /// the domain meaning. The database column has precision 9 and scale 0. This accessor does not
    /// round or check the value.
    ///
    /// @return applicant homeowner base-year equalized valuation in whole dollars
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getHomeownerBaseYearEqualizedValue() {
        return Objects.requireNonNull(
                homeownerBaseYearEqualizedValue,
                "homeownerBaseYearEqualizedValue must be populated before reading");
    }

    /// Stores the applicant homeowner base-year equalized valuation in whole dollars without
    /// validation or normalization.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeApplicant#homeownerBaseYearEqualizedValue()} for
    /// the domain meaning. The database column has precision 9 and scale 0. This accessor does not
    /// round or check the value.
    ///
    /// @param homeownerBaseYearEqualizedValue applicant homeowner base-year equalized valuation in
    ///   whole dollars
    public void setHomeownerBaseYearEqualizedValue(BigDecimal homeownerBaseYearEqualizedValue) {
        this.homeownerBaseYearEqualizedValue = homeownerBaseYearEqualizedValue;
    }

    /// Returns the persisted maintained code for homeowner eligibility.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeApplicant#homeownerEligibilityIndicator()} for
    /// the domain meaning.
    ///
    /// @return maintained code for homeowner eligibility
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getHomeownerEligibilityIndicator() {
        return Objects.requireNonNull(
                homeownerEligibilityIndicator,
                "homeownerEligibilityIndicator must be populated before reading");
    }

    /// Stores the maintained code for homeowner eligibility without validation or normalization.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeApplicant#homeownerEligibilityIndicator()} for
    /// the domain meaning.
    ///
    /// @param homeownerEligibilityIndicator maintained code for homeowner eligibility
    public void setHomeownerEligibilityIndicator(Integer homeownerEligibilityIndicator) {
        this.homeownerEligibilityIndicator = homeownerEligibilityIndicator;
    }

    /// Returns the persisted maintained homeowner disposition code.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#homeownerStatus()} for the
    /// domain meaning.
    ///
    /// @return maintained homeowner disposition code
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getHomeownerStatus() {
        return Objects.requireNonNull(
                homeownerStatus, "homeownerStatus must be populated before reading");
    }

    /// Stores the maintained homeowner disposition code without validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#homeownerStatus()} for the
    /// domain meaning.
    ///
    /// @param homeownerStatus maintained homeowner disposition code
    public void setHomeownerStatus(String homeownerStatus) {
        this.homeownerStatus = homeownerStatus;
    }

    /// Returns the persisted homestead processing batch identifier.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#homesteadBatchNumber()} for
    /// the domain meaning.
    ///
    /// @return homestead processing batch identifier
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getHomesteadBatchNumber() {
        return Objects.requireNonNull(
                homesteadBatchNumber, "homesteadBatchNumber must be populated before reading");
    }

    /// Stores the homestead processing batch identifier without validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#homesteadBatchNumber()} for
    /// the domain meaning.
    ///
    /// @param homesteadBatchNumber homestead processing batch identifier
    public void setHomesteadBatchNumber(Integer homesteadBatchNumber) {
        this.homesteadBatchNumber = homesteadBatchNumber;
    }

    /// Returns the persisted homestead share percentage with three fractional digits.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#homesteadPercentShares()}
    /// for the domain meaning. The database column has precision 6 and scale 3. This accessor does
    /// not round or check the value.
    ///
    /// @return homestead share percentage with three fractional digits
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getHomesteadPercentShares() {
        return Objects.requireNonNull(
                homesteadPercentShares, "homesteadPercentShares must be populated before reading");
    }

    /// Stores the homestead share percentage with three fractional digits without validation or
    /// normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#homesteadPercentShares()}
    /// for the domain meaning. The database column has precision 6 and scale 3. This accessor does
    /// not round or check the value.
    ///
    /// @param homesteadPercentShares homestead share percentage with three fractional digits
    public void setHomesteadPercentShares(BigDecimal homesteadPercentShares) {
        this.homesteadPercentShares = homesteadPercentShares;
    }

    /// Returns the persisted number of homestead shares.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#homesteadShares()} for the
    /// domain meaning.
    ///
    /// @return number of homestead shares
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getHomesteadShares() {
        return Objects.requireNonNull(
                homesteadShares, "homesteadShares must be populated before reading");
    }

    /// Stores the number of homestead shares without validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#homesteadShares()} for the
    /// domain meaning.
    ///
    /// @param homesteadShares number of homestead shares
    public void setHomesteadShares(Integer homesteadShares) {
        this.homesteadShares = homesteadShares;
    }

    /// Returns the persisted maintained homestead disposition code.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#homesteadStatus()} for the
    /// domain meaning.
    ///
    /// @return maintained homestead disposition code
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getHomesteadStatus() {
        return Objects.requireNonNull(
                homesteadStatus, "homesteadStatus must be populated before reading");
    }

    /// Stores the maintained homestead disposition code without validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#homesteadStatus()} for the
    /// domain meaning.
    ///
    /// @param homesteadStatus maintained homestead disposition code
    public void setHomesteadStatus(String homesteadStatus) {
        this.homesteadStatus = homesteadStatus;
    }

    /// Returns the persisted year associated with the homestead application.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#homesteadYearApplied()} for
    /// the domain meaning.
    ///
    /// @return year associated with the homestead application
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getHomesteadYearApplied() {
        return Objects.requireNonNull(
                homesteadYearApplied, "homesteadYearApplied must be populated before reading");
    }

    /// Stores the year associated with the homestead application without validation or
    /// normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#homesteadYearApplied()} for
    /// the domain meaning.
    ///
    /// @param homesteadYearApplied year associated with the homestead application
    public void setHomesteadYearApplied(Integer homesteadYearApplied) {
        this.homesteadYearApplied = homesteadYearApplied;
    }

    /// Returns the persisted interest portion of household income in dollars with two fractional
    /// digits.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#interestIncome()} for the
    /// domain meaning. The database column has precision 9 and scale 2. This accessor does not
    /// round or check the value.
    ///
    /// @return interest portion of household income in dollars with two fractional digits
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getInterestIncome() {
        return Objects.requireNonNull(
                interestIncome, "interestIncome must be populated before reading");
    }

    /// Stores the interest portion of household income in dollars with two fractional digits
    /// without validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#interestIncome()} for the
    /// domain meaning. The database column has precision 9 and scale 2. This accessor does not
    /// round or check the value.
    ///
    /// @param interestIncome interest portion of household income in dollars with two fractional
    ///   digits
    public void setInterestIncome(BigDecimal interestIncome) {
        this.interestIncome = interestIncome;
    }

    /// Returns the persisted latest received date in numeric `CCYYMMDD` layout; zero is absent and
    /// other source values remain unchanged.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#lastApplicationDate()} for
    /// the domain meaning. This raw compatibility value is not parsed or validated as a calendar
    /// date.
    ///
    /// @return latest received date in numeric `CCYYMMDD` layout; zero is absent and other source
    ///   values remain unchanged
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getLastApplicationDate() {
        return Objects.requireNonNull(
                lastApplicationDate, "lastApplicationDate must be populated before reading");
    }

    /// Stores the raw latest-received-date value without parsing or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#lastApplicationDate()} for
    /// the domain meaning. This raw compatibility value is not parsed or validated as a calendar
    /// date.
    ///
    /// @param lastApplicationDate latest received date in numeric `CCYYMMDD` layout; zero is absent
    ///   and other source values remain unchanged
    public void setLastApplicationDate(Integer lastApplicationDate) {
        this.lastApplicationDate = lastApplicationDate;
    }

    /// Returns the persisted maintained code for life-care-facility status.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeApplicant#lifeCareFacilityIndicator()} for the
    /// domain meaning.
    ///
    /// @return maintained code for life-care-facility status
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getLifeCareFacilityIndicator() {
        return Objects.requireNonNull(
                lifeCareFacilityIndicator,
                "lifeCareFacilityIndicator must be populated before reading");
    }

    /// Stores the maintained code for life-care-facility status without validation or
    /// normalization.
    ///
    /// See {@link
    /// org.cookcounty.tax.domain.model.SeniorFreezeApplicant#lifeCareFacilityIndicator()} for the
    /// domain meaning.
    ///
    /// @param lifeCareFacilityIndicator maintained code for life-care-facility status
    public void setLifeCareFacilityIndicator(String lifeCareFacilityIndicator) {
        this.lifeCareFacilityIndicator = lifeCareFacilityIndicator;
    }

    /// Returns the persisted maintained record-maintenance code.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#maintenanceIndicator()} for
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
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#maintenanceIndicator()} for
    /// the domain meaning.
    ///
    /// @param maintenanceIndicator maintained record-maintenance code
    public void setMaintenanceIndicator(Integer maintenanceIndicator) {
        this.maintenanceIndicator = maintenanceIndicator;
    }

    /// Returns the persisted maintained name-maintenance code.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#nameMaintenanceIndicator()}
    /// for the domain meaning.
    ///
    /// @return maintained name-maintenance code
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getNameMaintenanceIndicator() {
        return Objects.requireNonNull(
                nameMaintenanceIndicator,
                "nameMaintenanceIndicator must be populated before reading");
    }

    /// Stores the maintained name-maintenance code without validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#nameMaintenanceIndicator()}
    /// for the domain meaning.
    ///
    /// @param nameMaintenanceIndicator maintained name-maintenance code
    public void setNameMaintenanceIndicator(Integer nameMaintenanceIndicator) {
        this.nameMaintenanceIndicator = nameMaintenanceIndicator;
    }

    /// Returns the persisted net-capital-gain portion of household income in dollars with two
    /// fractional digits.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#netCapitalGain()} for the
    /// domain meaning. The database column has precision 9 and scale 2. This accessor does not
    /// round or check the value.
    ///
    /// @return net-capital-gain portion of household income in dollars with two fractional digits
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getNetCapitalGain() {
        return Objects.requireNonNull(
                netCapitalGain, "netCapitalGain must be populated before reading");
    }

    /// Stores the net-capital-gain portion of household income in dollars with two fractional
    /// digits without validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#netCapitalGain()} for the
    /// domain meaning. The database column has precision 9 and scale 2. This accessor does not
    /// round or check the value.
    ///
    /// @param netCapitalGain net-capital-gain portion of household income in dollars with two
    ///   fractional digits
    public void setNetCapitalGain(BigDecimal netCapitalGain) {
        this.netCapitalGain = netCapitalGain;
    }

    /// Returns the persisted net-rental portion of household income in dollars with two fractional
    /// digits.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#netRentalIncome()} for the
    /// domain meaning. The database column has precision 9 and scale 2. This accessor does not
    /// round or check the value.
    ///
    /// @return net-rental portion of household income in dollars with two fractional digits
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getNetRentalIncome() {
        return Objects.requireNonNull(
                netRentalIncome, "netRentalIncome must be populated before reading");
    }

    /// Stores the net-rental portion of household income in dollars with two fractional digits
    /// without validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#netRentalIncome()} for the
    /// domain meaning. The database column has precision 9 and scale 2. This accessor does not
    /// round or check the value.
    ///
    /// @param netRentalIncome net-rental portion of household income in dollars with two fractional
    ///   digits
    public void setNetRentalIncome(BigDecimal netRentalIncome) {
        this.netRentalIncome = netRentalIncome;
    }

    /// Returns the persisted maintained code that declares no household income.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#noIncomeIndicator()} for
    /// the domain meaning.
    ///
    /// @return maintained code that declares no household income
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getNoIncomeIndicator() {
        return Objects.requireNonNull(
                noIncomeIndicator, "noIncomeIndicator must be populated before reading");
    }

    /// Stores the maintained code that declares no household income without validation or
    /// normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#noIncomeIndicator()} for
    /// the domain meaning.
    ///
    /// @param noIncomeIndicator maintained code that declares no household income
    public void setNoIncomeIndicator(String noIncomeIndicator) {
        this.noIncomeIndicator = noIncomeIndicator;
    }

    /// Returns the persisted maintained code that records notarization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#notarizedIndicator()} for
    /// the domain meaning.
    ///
    /// @return maintained code that records notarization
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getNotarizedIndicator() {
        return Objects.requireNonNull(
                notarizedIndicator, "notarizedIndicator must be populated before reading");
    }

    /// Stores the maintained code that records notarization without validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#notarizedIndicator()} for
    /// the domain meaning.
    ///
    /// @param notarizedIndicator maintained code that records notarization
    public void setNotarizedIndicator(String notarizedIndicator) {
        this.notarizedIndicator = notarizedIndicator;
    }

    /// Returns the persisted other-benefit portion of household income in dollars with two
    /// fractional digits.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#otherBenefits()} for the
    /// domain meaning. The database column has precision 9 and scale 2. This accessor does not
    /// round or check the value.
    ///
    /// @return other-benefit portion of household income in dollars with two fractional digits
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getOtherBenefits() {
        return Objects.requireNonNull(
                otherBenefits, "otherBenefits must be populated before reading");
    }

    /// Stores the other-benefit portion of household income in dollars with two fractional digits
    /// without validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#otherBenefits()} for the
    /// domain meaning. The database column has precision 9 and scale 2. This accessor does not
    /// round or check the value.
    ///
    /// @param otherBenefits other-benefit portion of household income in dollars with two
    ///   fractional digits
    public void setOtherBenefits(BigDecimal otherBenefits) {
        this.otherBenefits = otherBenefits;
    }

    /// Returns the persisted other portion of household income in dollars with two fractional
    /// digits.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#otherIncome()} for the
    /// domain meaning. The database column has precision 9 and scale 2. This accessor does not
    /// round or check the value.
    ///
    /// @return other portion of household income in dollars with two fractional digits
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getOtherIncome() {
        return Objects.requireNonNull(otherIncome, "otherIncome must be populated before reading");
    }

    /// Stores the other portion of household income in dollars with two fractional digits without
    /// validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#otherIncome()} for the
    /// domain meaning. The database column has precision 9 and scale 2. This accessor does not
    /// round or check the value.
    ///
    /// @param otherIncome other portion of household income in dollars with two fractional digits
    public void setOtherIncome(BigDecimal otherIncome) {
        this.otherIncome = otherIncome;
    }

    /// Returns the persisted applicant share as a decimal fraction with six fractional digits.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#percentSeniorShares()} for
    /// the domain meaning. The database column has precision 6 and scale 6. This accessor does not
    /// round or check the value.
    ///
    /// @return applicant share as a decimal fraction with six fractional digits
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getPercentSeniorShares() {
        return Objects.requireNonNull(
                percentSeniorShares, "percentSeniorShares must be populated before reading");
    }

    /// Stores the applicant share as a decimal fraction with six fractional digits without
    /// validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#percentSeniorShares()} for
    /// the domain meaning. The database column has precision 6 and scale 6. This accessor does not
    /// round or check the value.
    ///
    /// @param percentSeniorShares applicant share as a decimal fraction with six fractional digits
    public void setPercentSeniorShares(BigDecimal percentSeniorShares) {
        this.percentSeniorShares = percentSeniorShares;
    }

    /// Returns the persisted canonical ten-digit telephone identifier.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#phoneNumber()} for the
    /// domain meaning. The database column permits up to 10 characters. Leading zeros are
    /// significant, and this accessor does not pad or validate digits.
    ///
    /// @return canonical ten-digit telephone identifier
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getPhoneNumber() {
        return Objects.requireNonNull(phoneNumber, "phoneNumber must be populated before reading");
    }

    /// Stores the canonical ten-digit telephone identifier without validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#phoneNumber()} for the
    /// domain meaning. The database column permits up to 10 characters. Leading zeros are
    /// significant, and this accessor does not pad or validate digits.
    ///
    /// @param phoneNumber canonical ten-digit telephone identifier
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /// Returns the persisted public-aid portion of household income in dollars with two fractional
    /// digits.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#publicAid()} for the domain
    /// meaning. The database column has precision 9 and scale 2. This accessor does not round or
    /// check the value.
    ///
    /// @return public-aid portion of household income in dollars with two fractional digits
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getPublicAid() {
        return Objects.requireNonNull(publicAid, "publicAid must be populated before reading");
    }

    /// Stores the public-aid portion of household income in dollars with two fractional digits
    /// without validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#publicAid()} for the domain
    /// meaning. The database column has precision 9 and scale 2. This accessor does not round or
    /// check the value.
    ///
    /// @param publicAid public-aid portion of household income in dollars with two fractional
    ///   digits
    public void setPublicAid(BigDecimal publicAid) {
        this.publicAid = publicAid;
    }

    /// Returns the persisted qualification date in numeric `CCYYMMDD` layout; zero is absent and
    /// other source values remain unchanged.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#qualificationDate()} for
    /// the domain meaning. This raw compatibility value is not parsed or validated as a calendar
    /// date.
    ///
    /// @return qualification date in numeric `CCYYMMDD` layout; zero is absent and other source
    ///   values remain unchanged
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getQualificationDate() {
        return Objects.requireNonNull(
                qualificationDate, "qualificationDate must be populated before reading");
    }

    /// Stores the raw qualification-date value without parsing or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#qualificationDate()} for
    /// the domain meaning. This raw compatibility value is not parsed or validated as a calendar
    /// date.
    ///
    /// @param qualificationDate qualification date in numeric `CCYYMMDD` layout; zero is absent and
    ///   other source values remain unchanged
    public void setQualificationDate(Integer qualificationDate) {
        this.qualificationDate = qualificationDate;
    }

    /// Returns the persisted railroad-benefit portion of household income in dollars with two
    /// fractional digits.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#railroadBenefits()} for the
    /// domain meaning. The database column has precision 9 and scale 2. This accessor does not
    /// round or check the value.
    ///
    /// @return railroad-benefit portion of household income in dollars with two fractional digits
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getRailroadBenefits() {
        return Objects.requireNonNull(
                railroadBenefits, "railroadBenefits must be populated before reading");
    }

    /// Stores the railroad-benefit portion of household income in dollars with two fractional
    /// digits without validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#railroadBenefits()} for the
    /// domain meaning. The database column has precision 9 and scale 2. This accessor does not
    /// round or check the value.
    ///
    /// @param railroadBenefits railroad-benefit portion of household income in dollars with two
    ///   fractional digits
    public void setRailroadBenefits(BigDecimal railroadBenefits) {
        this.railroadBenefits = railroadBenefits;
    }

    /// Returns the persisted returned date in numeric `CCYYMMDD` layout; zero is absent and other
    /// source values remain unchanged.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#returnedDate()} for the
    /// domain meaning. This raw compatibility value is not parsed or validated as a calendar date.
    ///
    /// @return returned date in numeric `CCYYMMDD` layout; zero is absent and other source values
    ///   remain unchanged
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getReturnedDate() {
        return Objects.requireNonNull(
                returnedDate, "returnedDate must be populated before reading");
    }

    /// Stores the raw returned-date value without parsing or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#returnedDate()} for the
    /// domain meaning. This raw compatibility value is not parsed or validated as a calendar date.
    ///
    /// @param returnedDate returned date in numeric `CCYYMMDD` layout; zero is absent and other
    ///   source values remain unchanged
    public void setReturnedDate(Integer returnedDate) {
        this.returnedDate = returnedDate;
    }

    /// Returns the persisted Senior Freeze percentage with one fractional digit.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#seniorFreezePercent()} for
    /// the domain meaning. The database column has precision 2 and scale 1. This accessor does not
    /// round or check the value.
    ///
    /// @return Senior Freeze percentage with one fractional digit
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getSeniorFreezePercent() {
        return Objects.requireNonNull(
                seniorFreezePercent, "seniorFreezePercent must be populated before reading");
    }

    /// Stores the Senior Freeze percentage with one fractional digit without validation or
    /// normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#seniorFreezePercent()} for
    /// the domain meaning. The database column has precision 2 and scale 1. This accessor does not
    /// round or check the value.
    ///
    /// @param seniorFreezePercent Senior Freeze percentage with one fractional digit
    public void setSeniorFreezePercent(BigDecimal seniorFreezePercent) {
        this.seniorFreezePercent = seniorFreezePercent;
    }

    /// Returns the persisted maintained application disposition code.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#seniorFreezeStatus()} for
    /// the domain meaning.
    ///
    /// @return maintained application disposition code
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getSeniorFreezeStatus() {
        return Objects.requireNonNull(
                seniorFreezeStatus, "seniorFreezeStatus must be populated before reading");
    }

    /// Stores the maintained application disposition code without validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#seniorFreezeStatus()} for
    /// the domain meaning.
    ///
    /// @param seniorFreezeStatus maintained application disposition code
    public void setSeniorFreezeStatus(String seniorFreezeStatus) {
        this.seniorFreezeStatus = seniorFreezeStatus;
    }

    /// Returns the persisted maintained code that records the applicant signature.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#signedIndicator()} for the
    /// domain meaning.
    ///
    /// @return maintained code that records the applicant signature
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getSignedIndicator() {
        return Objects.requireNonNull(
                signedIndicator, "signedIndicator must be populated before reading");
    }

    /// Stores the maintained code that records the applicant signature without validation or
    /// normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#signedIndicator()} for the
    /// domain meaning.
    ///
    /// @param signedIndicator maintained code that records the applicant signature
    public void setSignedIndicator(String signedIndicator) {
        this.signedIndicator = signedIndicator;
    }

    /// Returns the persisted Social Security portion of household income in dollars with two
    /// fractional digits.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#socialSecurityIncome()} for
    /// the domain meaning. The database column has precision 9 and scale 2. This accessor does not
    /// round or check the value.
    ///
    /// @return Social Security portion of household income in dollars with two fractional digits
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getSocialSecurityIncome() {
        return Objects.requireNonNull(
                socialSecurityIncome, "socialSecurityIncome must be populated before reading");
    }

    /// Stores the Social Security portion of household income in dollars with two fractional digits
    /// without validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#socialSecurityIncome()} for
    /// the domain meaning. The database column has precision 9 and scale 2. This accessor does not
    /// round or check the value.
    ///
    /// @param socialSecurityIncome Social Security portion of household income in dollars with two
    ///   fractional digits
    public void setSocialSecurityIncome(BigDecimal socialSecurityIncome) {
        this.socialSecurityIncome = socialSecurityIncome;
    }

    /// Returns the persisted canonical 11-digit source identifier.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#socialSecurityNumber()} for
    /// the domain meaning. The database column permits up to 11 characters. Leading zeros are
    /// significant, and this accessor does not pad or validate digits.
    ///
    /// @return canonical 11-digit source identifier
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getSocialSecurityNumber() {
        return Objects.requireNonNull(
                socialSecurityNumber, "socialSecurityNumber must be populated before reading");
    }

    /// Stores the canonical 11-digit source identifier without validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#socialSecurityNumber()} for
    /// the domain meaning. The database column permits up to 11 characters. Leading zeros are
    /// significant, and this accessor does not pad or validate digits.
    ///
    /// @param socialSecurityNumber canonical 11-digit source identifier
    public void setSocialSecurityNumber(String socialSecurityNumber) {
        this.socialSecurityNumber = socialSecurityNumber;
    }

    /// Returns the persisted applicant household income total in dollars with two fractional
    /// digits.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#totalIncome()} for the
    /// domain meaning. The database column has precision 9 and scale 2. This accessor does not
    /// round or check the value.
    ///
    /// @return applicant household income total in dollars with two fractional digits
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getTotalIncome() {
        return Objects.requireNonNull(totalIncome, "totalIncome must be populated before reading");
    }

    /// Stores the applicant household income total in dollars with two fractional digits without
    /// validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#totalIncome()} for the
    /// domain meaning. The database column has precision 9 and scale 2. This accessor does not
    /// round or check the value.
    ///
    /// @param totalIncome applicant household income total in dollars with two fractional digits
    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    /// Returns the persisted veterans-benefit portion of household income in dollars with two
    /// fractional digits.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#veteransBenefits()} for the
    /// domain meaning. The database column has precision 9 and scale 2. This accessor does not
    /// round or check the value.
    ///
    /// @return veterans-benefit portion of household income in dollars with two fractional digits
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getVeteransBenefits() {
        return Objects.requireNonNull(
                veteransBenefits, "veteransBenefits must be populated before reading");
    }

    /// Stores the veterans-benefit portion of household income in dollars with two fractional
    /// digits without validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#veteransBenefits()} for the
    /// domain meaning. The database column has precision 9 and scale 2. This accessor does not
    /// round or check the value.
    ///
    /// @param veteransBenefits veterans-benefit portion of household income in dollars with two
    ///   fractional digits
    public void setVeteransBenefits(BigDecimal veteransBenefits) {
        this.veteransBenefits = veteransBenefits;
    }

    /// Returns the persisted wage portion of household income in dollars with two fractional
    /// digits.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#wages()} for the domain
    /// meaning. The database column has precision 9 and scale 2. This accessor does not round or
    /// check the value.
    ///
    /// @return wage portion of household income in dollars with two fractional digits
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getWages() {
        return Objects.requireNonNull(wages, "wages must be populated before reading");
    }

    /// Stores the wage portion of household income in dollars with two fractional digits without
    /// validation or normalization.
    ///
    /// See {@link org.cookcounty.tax.domain.model.SeniorFreezeApplicant#wages()} for the domain
    /// meaning. The database column has precision 9 and scale 2. This accessor does not round or
    /// check the value.
    ///
    /// @param wages wage portion of household income in dollars with two fractional digits
    public void setWages(BigDecimal wages) {
        this.wages = wages;
    }

    // GENERATED-ACCESSORS:end
}
