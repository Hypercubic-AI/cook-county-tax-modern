package org.cookcounty.tax.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.util.Objects;

// GENERATED-IMPORTS:start

import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;

// GENERATED-IMPORTS:end

/// Mutable persistence representation of the current homeowner exemption state for one property.
///
/// The persisted business fields correspond to {@link
/// org.cookcounty.tax.domain.model.HomeownerMaster}. The entity uses field-based JPA access. Its
/// accessors return or assign values without applying the domain record's fixed-width identifier
/// padding or exact-decimal checks. Callers must use the domain boundary before they treat an
/// entity value as normalized.
///
/// JPA assigns the surrogate identity when it inserts a new row. JPA also initializes and advances
/// the optimistic-lock version during persistence. Both values can be `null` for a transient
/// entity, but hydrated and successfully persisted entities have provider-managed values.
@Entity
@Table(name = "homeowner_masters")
public class HomeownerMasterEntity {

    /// Database-generated surrogate identity. This is not the 15-digit property identifier.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    /// Provider-managed optimistic-lock value for detecting concurrent row updates.
    @Version private @Nullable Long version;

    // GENERATED-FIELDS:start

    @Column(name = "application_year")
    private @Nullable Integer applicationYear;

    @Column(name = "assessed_value", precision = 9, scale = 0)
    private @Nullable BigDecimal assessedValue;

    @Column(name = "assessment_class")
    private @Nullable Integer assessmentClass;

    @Column(name = "certificate_of_error_number")
    private @Nullable Integer certificateOfErrorNumber;

    @Column(name = "city")
    private @Nullable String city;

    @Column(name = "cooperative_quantity")
    private @Nullable Integer cooperativeQuantity;

    @Column(name = "equalization_factor", precision = 5, scale = 4)
    private @Nullable BigDecimal equalizationFactor;

    @Column(name = "equalized_value", precision = 9, scale = 0)
    private @Nullable BigDecimal equalizedValue;

    @Column(name = "exemption_type")
    private @Nullable Integer exemptionType;

    @Column(name = "mailing_address")
    private @Nullable String mailingAddress;

    @Column(name = "base_year_exemption_amount", precision = 9, scale = 0)
    private @Nullable BigDecimal baseYearExemptionAmount;

    @Column(name = "exemption_base_year")
    private @Nullable Integer exemptionBaseYear;

    @Column(name = "base_year_establishment_code")
    private @Nullable String baseYearEstablishmentCode;

    @Column(name = "occupancy_factor", precision = 5, scale = 1)
    private @Nullable BigDecimal occupancyFactor;

    @Column(name = "owner_name")
    private @Nullable String ownerName;

    @Column(name = "property_number", length = 15)
    private @Nullable String propertyNumber;

    @Column(name = "proration", precision = 7, scale = 6)
    private @Nullable BigDecimal proration;

    @Column(name = "response_status")
    private @Nullable Integer responseStatus;

    @Column(name = "secondary_response_status")
    private @Nullable Integer secondaryResponseStatus;

    @Column(name = "state")
    private @Nullable String state;

    @Column(name = "tax_code", length = 5)
    private @Nullable String taxCode;

    @Column(name = "tax_type")
    private @Nullable Integer taxType;

    @Column(name = "temporary_assessed_value", precision = 9, scale = 0)
    private @Nullable BigDecimal temporaryAssessedValue;

    @Column(name = "tertiary_status")
    private @Nullable Integer tertiaryStatus;

    @Column(name = "volume_number", length = 3)
    private @Nullable String volumeNumber;

    @Column(name = "zip_code", length = 9)
    private @Nullable String zipCode;

    // GENERATED-FIELDS:end

    // GENERATED-ACCESSORS:start
    /// Returns the database-generated surrogate identity.
    ///
    /// The value is `null` before JPA assigns an identity to a new entity.
    public @Nullable Long getId() {
        return id;
    }

    /// Assigns the persistence identity during mapping or hydration.
    ///
    /// JPA owns identity generation for inserted rows.
    ///
    /// @param id generated identity, or `null` for a transient entity
    public void setId(@Nullable Long id) {
        this.id = id;
    }

    /// Returns the provider-managed optimistic-lock version.
    ///
    /// The value is `null` before JPA initializes the persistence lifecycle.
    public @Nullable Long getVersion() {
        return version;
    }

    /// Assigns the optimistic-lock version during mapping or hydration.
    ///
    /// JPA owns version initialization and advancement.
    ///
    /// @param version persisted version, or `null` before persistence
    public void setVersion(@Nullable Long version) {
        this.version = version;
    }

    /// Returns the two-digit application year described by {@link
    /// org.cookcounty.tax.domain.model.HomeownerMaster#applicationYear()}.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getApplicationYear() {
        return Objects.requireNonNull(
                applicationYear, "applicationYear must be populated before reading");
    }

    /// Stores the two-digit application year.
    ///
    /// @param applicationYear application year to persist
    public void setApplicationYear(Integer applicationYear) {
        this.applicationYear = applicationYear;
    }

    /// Returns the assessed valuation in whole dollars.
    ///
    /// Persistence supports exactly nine digits and no fractional digits.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getAssessedValue() {
        return Objects.requireNonNull(
                assessedValue, "assessedValue must be populated before reading");
    }

    /// Stores an assessed valuation for a `NUMERIC(9,0)` column.
    ///
    /// @param assessedValue whole-dollar assessed valuation
    public void setAssessedValue(BigDecimal assessedValue) {
        this.assessedValue = assessedValue;
    }

    /// Returns the assessment classification selected for the property.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getAssessmentClass() {
        return Objects.requireNonNull(
                assessmentClass, "assessmentClass must be populated before reading");
    }

    /// Stores the property's assessment classification.
    ///
    /// @param assessmentClass assessment classification to persist
    public void setAssessmentClass(Integer assessmentClass) {
        this.assessmentClass = assessmentClass;
    }

    /// Returns the certificate-of-error reference, or `null` when the record has none.
    public @Nullable Integer getCertificateOfErrorNumber() {
        return certificateOfErrorNumber;
    }

    /// Stores the optional certificate-of-error reference.
    ///
    /// @param certificateOfErrorNumber reference to persist, or `null` when absent
    public void setCertificateOfErrorNumber(@Nullable Integer certificateOfErrorNumber) {
        this.certificateOfErrorNumber = certificateOfErrorNumber;
    }

    /// Returns the mailing city, or `null` when owner-detail input did not supply one.
    public @Nullable String getCity() {
        return city;
    }

    /// Stores the optional mailing city.
    ///
    /// @param city mailing city to persist, or `null` when absent
    public void setCity(@Nullable String city) {
        this.city = city;
    }

    /// Returns the cooperative units or shares, or `null` when the processing path supplied none.
    public @Nullable Integer getCooperativeQuantity() {
        return cooperativeQuantity;
    }

    /// Stores the optional cooperative quantity.
    ///
    /// @param cooperativeQuantity units or shares to persist, or `null` when absent
    public void setCooperativeQuantity(@Nullable Integer cooperativeQuantity) {
        this.cooperativeQuantity = cooperativeQuantity;
    }

    /// Returns the factor applied to assessed value.
    ///
    /// Persistence supports five total digits with four fractional digits.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getEqualizationFactor() {
        return Objects.requireNonNull(
                equalizationFactor, "equalizationFactor must be populated before reading");
    }

    /// Stores an equalization factor for a `NUMERIC(5,4)` column.
    ///
    /// @param equalizationFactor factor with four fractional digits
    public void setEqualizationFactor(BigDecimal equalizationFactor) {
        this.equalizationFactor = equalizationFactor;
    }

    /// Returns the equalized valuation in whole dollars, or `null` when none was supplied.
    public @Nullable BigDecimal getEqualizedValue() {
        return equalizedValue;
    }

    /// Stores an optional equalized valuation for a `NUMERIC(9,0)` column.
    ///
    /// @param equalizedValue whole-dollar valuation, or `null` when absent
    public void setEqualizedValue(@Nullable BigDecimal equalizedValue) {
        this.equalizedValue = equalizedValue;
    }

    /// Returns the exemption category code, or `null` when no category was assigned.
    public @Nullable Integer getExemptionType() {
        return exemptionType;
    }

    /// Stores the optional exemption category code.
    ///
    /// @param exemptionType category code to persist, or `null` when unassigned
    public void setExemptionType(@Nullable Integer exemptionType) {
        this.exemptionType = exemptionType;
    }

    /// Returns the mailing street line, or `null` when owner-detail input supplied none.
    public @Nullable String getMailingAddress() {
        return mailingAddress;
    }

    /// Stores the optional mailing street line.
    ///
    /// @param mailingAddress street line to persist, or `null` when absent
    public void setMailingAddress(@Nullable String mailingAddress) {
        this.mailingAddress = mailingAddress;
    }

    /// Returns the maintained base-year exemption amount in whole dollars.
    ///
    /// The value is `null` when no amount was supplied. Persistence supports nine digits and no
    /// fractional digits.
    public @Nullable BigDecimal getBaseYearExemptionAmount() {
        return baseYearExemptionAmount;
    }

    /// Stores an optional base-year exemption amount for a `NUMERIC(9,0)` column.
    ///
    /// @param baseYearExemptionAmount whole-dollar amount, or `null` when absent
    public void setBaseYearExemptionAmount(@Nullable BigDecimal baseYearExemptionAmount) {
        this.baseYearExemptionAmount = baseYearExemptionAmount;
    }

    /// Returns the four-digit year associated with the maintained exemption amount.
    ///
    /// The value is `null` when no base year was supplied.
    public @Nullable Integer getExemptionBaseYear() {
        return exemptionBaseYear;
    }

    /// Stores the optional year associated with the maintained exemption amount.
    ///
    /// @param exemptionBaseYear four-digit year, or `null` when absent
    public void setExemptionBaseYear(@Nullable Integer exemptionBaseYear) {
        this.exemptionBaseYear = exemptionBaseYear;
    }

    /// Returns the two-character code for how the process established the base year.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getBaseYearEstablishmentCode() {
        return Objects.requireNonNull(
                baseYearEstablishmentCode,
                "baseYearEstablishmentCode must be populated before reading");
    }

    /// Stores the code for how the process established the base year.
    ///
    /// @param baseYearEstablishmentCode two-character establishment code
    public void setBaseYearEstablishmentCode(String baseYearEstablishmentCode) {
        this.baseYearEstablishmentCode = baseYearEstablishmentCode;
    }

    /// Returns the occupancy percentage used by eligibility.
    ///
    /// Persistence supports five total digits with one fractional digit.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getOccupancyFactor() {
        return Objects.requireNonNull(
                occupancyFactor, "occupancyFactor must be populated before reading");
    }

    /// Stores an occupancy percentage for a `NUMERIC(5,1)` column.
    ///
    /// @param occupancyFactor occupancy percentage with one fractional digit
    public void setOccupancyFactor(BigDecimal occupancyFactor) {
        this.occupancyFactor = occupancyFactor;
    }

    /// Returns the owner name, or `null` when owner-detail input supplied none.
    public @Nullable String getOwnerName() {
        return ownerName;
    }

    /// Stores the optional owner name.
    ///
    /// @param ownerName owner name to persist, or `null` when absent
    public void setOwnerName(@Nullable String ownerName) {
        this.ownerName = ownerName;
    }

    /// Returns the canonical 15-digit property identifier.
    ///
    /// The string representation preserves leading zeros.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getPropertyNumber() {
        return Objects.requireNonNull(
                propertyNumber, "propertyNumber must be populated before reading");
    }

    /// Stores the canonical property identifier without numeric conversion.
    ///
    /// @param propertyNumber 15-digit identifier, including leading zeros
    public void setPropertyNumber(String propertyNumber) {
        this.propertyNumber = propertyNumber;
    }

    /// Returns the eligible property share as a decimal fraction.
    ///
    /// Persistence supports seven total digits with six fractional digits.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getProration() {
        return Objects.requireNonNull(proration, "proration must be populated before reading");
    }

    /// Stores an eligible share for a `NUMERIC(7,6)` column.
    ///
    /// @param proration decimal fraction with six fractional digits
    public void setProration(BigDecimal proration) {
        this.proration = proration;
    }

    /// Returns the primary homeowner response classification.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getResponseStatus() {
        return Objects.requireNonNull(
                responseStatus, "responseStatus must be populated before reading");
    }

    /// Stores the primary homeowner response classification.
    ///
    /// @param responseStatus primary response classification
    public void setResponseStatus(Integer responseStatus) {
        this.responseStatus = responseStatus;
    }

    /// Returns the secondary homeowner response classification.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getSecondaryResponseStatus() {
        return Objects.requireNonNull(
                secondaryResponseStatus,
                "secondaryResponseStatus must be populated before reading");
    }

    /// Stores the secondary homeowner response classification.
    ///
    /// @param secondaryResponseStatus secondary response classification
    public void setSecondaryResponseStatus(Integer secondaryResponseStatus) {
        this.secondaryResponseStatus = secondaryResponseStatus;
    }

    /// Returns the mailing state code, or `null` when owner-detail input supplied none.
    public @Nullable String getState() {
        return state;
    }

    /// Stores the optional mailing state code.
    ///
    /// @param state state code to persist, or `null` when absent
    public void setState(@Nullable String state) {
        this.state = state;
    }

    /// Returns the canonical five-digit taxing-district code.
    ///
    /// The string representation preserves leading zeros.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getTaxCode() {
        return Objects.requireNonNull(taxCode, "taxCode must be populated before reading");
    }

    /// Stores the taxing-district code without numeric conversion.
    ///
    /// @param taxCode five-digit code, including leading zeros
    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }

    /// Returns the tax-type code, or `null` when the assessment supplied none.
    public @Nullable Integer getTaxType() {
        return taxType;
    }

    /// Stores the optional tax-type code.
    ///
    /// @param taxType tax-type code to persist, or `null` when absent
    public void setTaxType(@Nullable Integer taxType) {
        this.taxType = taxType;
    }

    /// Returns the temporary assessed valuation in whole dollars.
    ///
    /// The value is `null` when no temporary valuation is maintained. Persistence supports nine
    /// digits and no fractional digits.
    public @Nullable BigDecimal getTemporaryAssessedValue() {
        return temporaryAssessedValue;
    }

    /// Stores an optional temporary valuation for a `NUMERIC(9,0)` column.
    ///
    /// @param temporaryAssessedValue whole-dollar valuation, or `null` when absent
    public void setTemporaryAssessedValue(@Nullable BigDecimal temporaryAssessedValue) {
        this.temporaryAssessedValue = temporaryAssessedValue;
    }

    /// Returns the third homeowner response classification.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getTertiaryStatus() {
        return Objects.requireNonNull(
                tertiaryStatus, "tertiaryStatus must be populated before reading");
    }

    /// Stores the third homeowner response classification.
    ///
    /// @param tertiaryStatus third response classification
    public void setTertiaryStatus(Integer tertiaryStatus) {
        this.tertiaryStatus = tertiaryStatus;
    }

    /// Returns the canonical three-digit assessment volume.
    ///
    /// The string representation preserves leading zeros.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getVolumeNumber() {
        return Objects.requireNonNull(
                volumeNumber, "volumeNumber must be populated before reading");
    }

    /// Stores the assessment volume without numeric conversion.
    ///
    /// @param volumeNumber three-digit volume, including leading zeros
    public void setVolumeNumber(String volumeNumber) {
        this.volumeNumber = volumeNumber;
    }

    /// Returns the canonical nine-digit postal identifier.
    ///
    /// The value is `null` when owner-detail input supplied none. The string representation
    /// preserves leading zeros.
    public @Nullable String getZipCode() {
        return zipCode;
    }

    /// Stores the optional postal identifier without numeric conversion.
    ///
    /// @param zipCode nine-digit identifier including leading zeros, or `null` when absent
    public void setZipCode(@Nullable String zipCode) {
        this.zipCode = zipCode;
    }

    // GENERATED-ACCESSORS:end
}
