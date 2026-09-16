package org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper;

import org.cookcounty.tax.domain.model.AgencyEqualizedValuation;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.AgencyEqualizedValuationEntity;

import java.util.Objects;

/// Canonical, complete mapping between the immutable domain snapshot and its persistence entity.
public final class AgencyEqualizedValuationMapper {

    private AgencyEqualizedValuationMapper() {}

    /// Hydrates every domain component, including the persistence identifier and version.
    public static AgencyEqualizedValuation toDomain(AgencyEqualizedValuationEntity entity) {
        return new AgencyEqualizedValuation(
                entity.getId(),
                entity.getVersion(),
                Objects.requireNonNull(entity.getAgencyNumber(), "agencyNumber"),
                Objects.requireNonNull(
                        entity.getAnnexedPropertyEqualizedValue(), "annexedPropertyEqualizedValue"),
                Objects.requireNonNull(entity.getBurdenPercent(), "burdenPercent"),
                Objects.requireNonNull(entity.getConnectingAgency1(), "connectingAgency1"),
                Objects.requireNonNull(entity.getConnectingAgency2(), "connectingAgency2"),
                Objects.requireNonNull(entity.getConnectingAgency3(), "connectingAgency3"),
                Objects.requireNonNull(entity.getConnectingAgency4(), "connectingAgency4"),
                Objects.requireNonNull(
                        entity.getCookCountyAirPollutionValue(), "cookCountyAirPollutionValue"),
                Objects.requireNonNull(
                        entity.getCookCountyRailroadValue(), "cookCountyRailroadValue"),
                Objects.requireNonNull(
                        entity.getCookCountyRealEstateValue(), "cookCountyRealEstateValue"),
                Objects.requireNonNull(entity.getCookCountyUseTaxValue(), "cookCountyUseTaxValue"),
                Objects.requireNonNull(
                        entity.getDeKalbCountyEqualizedValue(), "deKalbCountyEqualizedValue"),
                Objects.requireNonNull(
                        entity.getDisconnectedPropertyEqualizedValue(),
                        "disconnectedPropertyEqualizedValue"),
                Objects.requireNonNull(
                        entity.getDisconnectedTifDifference(), "disconnectedTifDifference"),
                Objects.requireNonNull(
                        entity.getDuPageCountyEqualizedValue(), "duPageCountyEqualizedValue"),
                Objects.requireNonNull(
                        entity.getGrundyCountyEqualizedValue(), "grundyCountyEqualizedValue"),
                Objects.requireNonNull(
                        entity.getKaneCountyEqualizedValue(), "kaneCountyEqualizedValue"),
                Objects.requireNonNull(
                        entity.getKankakeeCountyEqualizedValue(), "kankakeeCountyEqualizedValue"),
                Objects.requireNonNull(
                        entity.getKendallCountyEqualizedValue(), "kendallCountyEqualizedValue"),
                Objects.requireNonNull(
                        entity.getLaSalleCountyEqualizedValue(), "laSalleCountyEqualizedValue"),
                Objects.requireNonNull(
                        entity.getLakeCountyEqualizedValue(), "lakeCountyEqualizedValue"),
                Objects.requireNonNull(
                        entity.getLimitingTaxRateOverride(), "limitingTaxRateOverride"),
                Objects.requireNonNull(
                        entity.getLivingstonCountyEqualizedValue(),
                        "livingstonCountyEqualizedValue"),
                Objects.requireNonNull(
                        entity.getMcHenryCountyEqualizedValue(), "mcHenryCountyEqualizedValue"),
                Objects.requireNonNull(
                        entity.getNewPropertyEqualizedValue(), "newPropertyEqualizedValue"),
                Objects.requireNonNull(
                        entity.getOverlapAnnexedPropertyEqualizedValue(),
                        "overlapAnnexedPropertyEqualizedValue"),
                Objects.requireNonNull(
                        entity.getOverlapDisconnectedPropertyEqualizedValue(),
                        "overlapDisconnectedPropertyEqualizedValue"),
                Objects.requireNonNull(
                        entity.getOverlapDisconnectedTifDifference(),
                        "overlapDisconnectedTifDifference"),
                Objects.requireNonNull(
                        entity.getOverlapNewPropertyEqualizedValue(),
                        "overlapNewPropertyEqualizedValue"),
                Objects.requireNonNull(entity.getParentAgency1(), "parentAgency1"),
                Objects.requireNonNull(entity.getParentAgency2(), "parentAgency2"),
                Objects.requireNonNull(entity.getParentAgency3(), "parentAgency3"),
                Objects.requireNonNull(entity.getParentAgency4(), "parentAgency4"),
                Objects.requireNonNull(entity.getParentAgency5(), "parentAgency5"),
                Objects.requireNonNull(entity.getPreviousTaxYear1(), "previousTaxYear1"),
                Objects.requireNonNull(
                        entity.getPreviousTaxYear1Extension(), "previousTaxYear1Extension"),
                Objects.requireNonNull(entity.getPreviousTaxYear2(), "previousTaxYear2"),
                Objects.requireNonNull(
                        entity.getPreviousTaxYear2Extension(), "previousTaxYear2Extension"),
                Objects.requireNonNull(entity.getPreviousTaxYear3(), "previousTaxYear3"),
                Objects.requireNonNull(
                        entity.getPreviousTaxYear3Extension(), "previousTaxYear3Extension"),
                Objects.requireNonNull(entity.getTaxCapIndicator(), "taxCapIndicator"),
                Objects.requireNonNull(entity.getTaxYear(), "taxYear"),
                Objects.requireNonNull(
                        entity.getWillCountyEqualizedValue(), "willCountyEqualizedValue"));
    }

    /// Copies every domain component to a mutable entity for persistence.
    public static AgencyEqualizedValuationEntity toEntity(AgencyEqualizedValuation model) {
        AgencyEqualizedValuationEntity entity = new AgencyEqualizedValuationEntity();
        entity.setId(model.id());
        entity.setVersion(model.version());
        entity.setAgencyNumber(model.agencyNumber());
        entity.setAnnexedPropertyEqualizedValue(model.annexedPropertyEqualizedValue());
        entity.setBurdenPercent(model.burdenPercent());
        entity.setConnectingAgency1(model.connectingAgency1());
        entity.setConnectingAgency2(model.connectingAgency2());
        entity.setConnectingAgency3(model.connectingAgency3());
        entity.setConnectingAgency4(model.connectingAgency4());
        entity.setCookCountyAirPollutionValue(model.cookCountyAirPollutionValue());
        entity.setCookCountyRailroadValue(model.cookCountyRailroadValue());
        entity.setCookCountyRealEstateValue(model.cookCountyRealEstateValue());
        entity.setCookCountyUseTaxValue(model.cookCountyUseTaxValue());
        entity.setDeKalbCountyEqualizedValue(model.deKalbCountyEqualizedValue());
        entity.setDisconnectedPropertyEqualizedValue(model.disconnectedPropertyEqualizedValue());
        entity.setDisconnectedTifDifference(model.disconnectedTifDifference());
        entity.setDuPageCountyEqualizedValue(model.duPageCountyEqualizedValue());
        entity.setGrundyCountyEqualizedValue(model.grundyCountyEqualizedValue());
        entity.setKaneCountyEqualizedValue(model.kaneCountyEqualizedValue());
        entity.setKankakeeCountyEqualizedValue(model.kankakeeCountyEqualizedValue());
        entity.setKendallCountyEqualizedValue(model.kendallCountyEqualizedValue());
        entity.setLaSalleCountyEqualizedValue(model.laSalleCountyEqualizedValue());
        entity.setLakeCountyEqualizedValue(model.lakeCountyEqualizedValue());
        entity.setLimitingTaxRateOverride(model.limitingTaxRateOverride());
        entity.setLivingstonCountyEqualizedValue(model.livingstonCountyEqualizedValue());
        entity.setMcHenryCountyEqualizedValue(model.mcHenryCountyEqualizedValue());
        entity.setNewPropertyEqualizedValue(model.newPropertyEqualizedValue());
        entity.setOverlapAnnexedPropertyEqualizedValue(
                model.overlapAnnexedPropertyEqualizedValue());
        entity.setOverlapDisconnectedPropertyEqualizedValue(
                model.overlapDisconnectedPropertyEqualizedValue());
        entity.setOverlapDisconnectedTifDifference(model.overlapDisconnectedTifDifference());
        entity.setOverlapNewPropertyEqualizedValue(model.overlapNewPropertyEqualizedValue());
        entity.setParentAgency1(model.parentAgency1());
        entity.setParentAgency2(model.parentAgency2());
        entity.setParentAgency3(model.parentAgency3());
        entity.setParentAgency4(model.parentAgency4());
        entity.setParentAgency5(model.parentAgency5());
        entity.setPreviousTaxYear1(model.previousTaxYear1());
        entity.setPreviousTaxYear1Extension(model.previousTaxYear1Extension());
        entity.setPreviousTaxYear2(model.previousTaxYear2());
        entity.setPreviousTaxYear2Extension(model.previousTaxYear2Extension());
        entity.setPreviousTaxYear3(model.previousTaxYear3());
        entity.setPreviousTaxYear3Extension(model.previousTaxYear3Extension());
        entity.setTaxCapIndicator(model.taxCapIndicator());
        entity.setTaxYear(model.taxYear());
        entity.setWillCountyEqualizedValue(model.willCountyEqualizedValue());
        return entity;
    }
}
