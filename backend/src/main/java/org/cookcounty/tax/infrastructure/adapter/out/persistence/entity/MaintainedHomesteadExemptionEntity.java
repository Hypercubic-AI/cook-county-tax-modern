package org.cookcounty.tax.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.util.Objects;

// GENERATED-IMPORTS:start

import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;

// GENERATED-IMPORTS:end

/// Mutable persistence boundary for maintained homestead exemption snapshots.
///
/// Identity and version are absent until JPA hydrates or inserts the entity.
///
/// Accessors expose the stored snapshot without domain normalization or eligibility calculations.
/// Non-null fields require complete JPA hydration or mapper initialization before use.
/// [org.cookcounty.tax.domain.model.MaintainedHomesteadExemption] defines the domain value
/// contracts.
@Entity
@Table(name = "maintained_homestead_exemptions")
public class MaintainedHomesteadExemptionEntity {

    /// Generated database identity. Null identifies a snapshot that is not yet persisted.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    /// Optimistic-lock state. Null means the persistence provider has not supplied a version.
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

    @Column(name = "clerks_class")
    private @Nullable Integer clerksClass;

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
    private @Nullable String taxType;

    @Column(name = "tertiary_status")
    private @Nullable Integer tertiaryStatus;

    @Column(name = "volume_number", length = 3)
    private @Nullable String volumeNumber;

    @Column(name = "zip_code", length = 9)
    private @Nullable String zipCode;

    // GENERATED-FIELDS:end

    // GENERATED-ACCESSORS:start
    /// Returns the database identity, or null before the first successful persistence operation.
    public @Nullable Long getId() {
        return id;
    }

    /// Assigns persistence identity. Null retains the new-snapshot state.
    public void setId(@Nullable Long id) {
        this.id = id;
    }

    /// Returns the optimistic-lock version, or null before persistence supplies one.
    public @Nullable Long getVersion() {
        return version;
    }

    /// Assigns the stored optimistic-lock version without performing a database version check.
    public void setVersion(@Nullable Long version) {
        this.version = version;
    }

    /// Returns the two-digit application year carried by homeowner processing.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getApplicationYear() {
        return Objects.requireNonNull(
                applicationYear, "applicationYear must be populated before reading");
    }

    /// Stores the two-digit application year without changing eligibility or response
    /// classifications.
    public void setApplicationYear(Integer applicationYear) {
        this.applicationYear = applicationYear;
    }

    /// Returns the signed assessed valuation in whole dollars, stored with nine-digit precision.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getAssessedValue() {
        return Objects.requireNonNull(
                assessedValue, "assessedValue must be populated before reading");
    }

    /// Stores the assessed valuation without rounding. Domain construction checks precision and
    /// scale.
    public void setAssessedValue(BigDecimal assessedValue) {
        this.assessedValue = assessedValue;
    }

    /// Returns the property's assessment classification code, not an eligibility decision.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getAssessmentClass() {
        return Objects.requireNonNull(
                assessmentClass, "assessmentClass must be populated before reading");
    }

    /// Stores the assessment classification without recalculating the exemption.
    public void setAssessmentClass(Integer assessmentClass) {
        this.assessmentClass = assessmentClass;
    }

    /// Returns the maintained certificate-of-error reference.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getCertificateOfErrorNumber() {
        return Objects.requireNonNull(
                certificateOfErrorNumber,
                "certificateOfErrorNumber must be populated before reading");
    }

    /// Stores the certificate-of-error reference without resolving an external certificate record.
    public void setCertificateOfErrorNumber(Integer certificateOfErrorNumber) {
        this.certificateOfErrorNumber = certificateOfErrorNumber;
    }

    /// Returns the mailing city retained from the owner-contact snapshot.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getCity() {
        return Objects.requireNonNull(city, "city must be populated before reading");
    }

    /// Stores the mailing city without validating or refreshing the owner address.
    public void setCity(String city) {
        this.city = city;
    }

    /// Returns the Clerk classification carried by the exemption snapshot.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getClerksClass() {
        return Objects.requireNonNull(clerksClass, "clerksClass must be populated before reading");
    }

    /// Stores the Clerk classification without applying an assessment cross-reference.
    public void setClerksClass(Integer clerksClass) {
        this.clerksClass = clerksClass;
    }

    /// Returns the cooperative unit or share count carried by the exemption.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getCooperativeQuantity() {
        return Objects.requireNonNull(
                cooperativeQuantity, "cooperativeQuantity must be populated before reading");
    }

    /// Stores the cooperative quantity without calculating ownership or eligibility.
    public void setCooperativeQuantity(Integer cooperativeQuantity) {
        this.cooperativeQuantity = cooperativeQuantity;
    }

    /// Returns the equalization multiplier with four fractional digits and five-digit precision.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getEqualizationFactor() {
        return Objects.requireNonNull(
                equalizationFactor, "equalizationFactor must be populated before reading");
    }

    /// Stores the exact equalization multiplier without applying it to the assessed valuation.
    public void setEqualizationFactor(BigDecimal equalizationFactor) {
        this.equalizationFactor = equalizationFactor;
    }

    /// Returns the signed equalized valuation in whole dollars, stored with nine-digit precision.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getEqualizedValue() {
        return Objects.requireNonNull(
                equalizedValue, "equalizedValue must be populated before reading");
    }

    /// Stores the equalized valuation without rounding or recomputing it from the assessed value.
    public void setEqualizedValue(BigDecimal equalizedValue) {
        this.equalizedValue = equalizedValue;
    }

    /// Returns the exemption category code assigned by the maintained processing path.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getExemptionType() {
        return Objects.requireNonNull(
                exemptionType, "exemptionType must be populated before reading");
    }

    /// Stores the exemption category without interpreting its legal code set.
    public void setExemptionType(Integer exemptionType) {
        this.exemptionType = exemptionType;
    }

    /// Returns the mailing street line retained from the owner-contact snapshot.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getMailingAddress() {
        return Objects.requireNonNull(
                mailingAddress, "mailingAddress must be populated before reading");
    }

    /// Stores the mailing street line without validating or refreshing owner-contact data.
    public void setMailingAddress(String mailingAddress) {
        this.mailingAddress = mailingAddress;
    }

    /// Returns the maintained exemption amount for the base-year group, in whole dollars.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getBaseYearExemptionAmount() {
        return Objects.requireNonNull(
                baseYearExemptionAmount,
                "baseYearExemptionAmount must be populated before reading");
    }

    /// Sets the whole-dollar exemption amount for the base-year group.
    public void setBaseYearExemptionAmount(BigDecimal baseYearExemptionAmount) {
        this.baseYearExemptionAmount = baseYearExemptionAmount;
    }

    /// Returns the four-digit year associated with the maintained exemption amount.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getExemptionBaseYear() {
        return Objects.requireNonNull(
                exemptionBaseYear, "exemptionBaseYear must be populated before reading");
    }

    /// Sets the four-digit year associated with the maintained exemption amount.
    public void setExemptionBaseYear(Integer exemptionBaseYear) {
        this.exemptionBaseYear = exemptionBaseYear;
    }

    /// Returns the two-character code that records how the process established the base year.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getBaseYearEstablishmentCode() {
        return Objects.requireNonNull(
                baseYearEstablishmentCode,
                "baseYearEstablishmentCode must be populated before reading");
    }

    /// Sets the two-character code that records how the process established the base year.
    public void setBaseYearEstablishmentCode(String baseYearEstablishmentCode) {
        this.baseYearEstablishmentCode = baseYearEstablishmentCode;
    }

    /// Returns the occupancy percentage with one fractional digit and five-digit precision.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getOccupancyFactor() {
        return Objects.requireNonNull(
                occupancyFactor, "occupancyFactor must be populated before reading");
    }

    /// Stores the occupancy percentage without calculating an eligible property share.
    public void setOccupancyFactor(BigDecimal occupancyFactor) {
        this.occupancyFactor = occupancyFactor;
    }

    /// Returns the owner name retained from the owner-contact snapshot.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getOwnerName() {
        return Objects.requireNonNull(ownerName, "ownerName must be populated before reading");
    }

    /// Stores the owner name without updating other contact fields.
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    /// Returns the canonical 15-digit property key, including leading zeros.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getPropertyNumber() {
        return Objects.requireNonNull(
                propertyNumber, "propertyNumber must be populated before reading");
    }

    /// Stores the property key as supplied. Domain construction enforces its digit width.
    public void setPropertyNumber(String propertyNumber) {
        this.propertyNumber = propertyNumber;
    }

    /// Returns the eligible property share as an exact six-place decimal fraction.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public BigDecimal getProration() {
        return Objects.requireNonNull(proration, "proration must be populated before reading");
    }

    /// Stores the eligible share without rounding or recalculating the exemption.
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

    /// Stores the primary response classification without changing secondary or tertiary state.
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

    /// Stores the secondary response classification without changing primary or tertiary state.
    public void setSecondaryResponseStatus(Integer secondaryResponseStatus) {
        this.secondaryResponseStatus = secondaryResponseStatus;
    }

    /// Returns the mailing state code retained from the owner-contact snapshot.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getState() {
        return Objects.requireNonNull(state, "state must be populated before reading");
    }

    /// Stores the mailing state code without validating or refreshing the owner address.
    public void setState(String state) {
        this.state = state;
    }

    /// Returns the canonical five-digit taxing-district key, including leading zeros.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getTaxCode() {
        return Objects.requireNonNull(taxCode, "taxCode must be populated before reading");
    }

    /// Stores the taxing-district key as supplied. Domain construction enforces its digit width.
    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }

    /// Returns the tax-type code carried with the maintained property snapshot.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getTaxType() {
        return Objects.requireNonNull(taxType, "taxType must be populated before reading");
    }

    /// Stores the tax-type code without deriving a property classification.
    public void setTaxType(String taxType) {
        this.taxType = taxType;
    }

    /// Returns the third homeowner response classification.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public Integer getTertiaryStatus() {
        return Objects.requireNonNull(
                tertiaryStatus, "tertiaryStatus must be populated before reading");
    }

    /// Stores the tertiary response classification without changing primary or secondary state.
    public void setTertiaryStatus(Integer tertiaryStatus) {
        this.tertiaryStatus = tertiaryStatus;
    }

    /// Returns the canonical three-digit assessment volume used in source ordering.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getVolumeNumber() {
        return Objects.requireNonNull(
                volumeNumber, "volumeNumber must be populated before reading");
    }

    /// Stores the assessment volume as supplied. Domain construction enforces its digit width.
    public void setVolumeNumber(String volumeNumber) {
        this.volumeNumber = volumeNumber;
    }

    /// Returns the canonical nine-digit postal identifier, including leading zeros.
    ///
    /// @throws NullPointerException if JPA or the mapper has not populated the value
    public String getZipCode() {
        return Objects.requireNonNull(zipCode, "zipCode must be populated before reading");
    }

    /// Stores the postal identifier as supplied. Domain construction enforces its digit width.
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    // GENERATED-ACCESSORS:end
}
