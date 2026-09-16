
package org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper;

import org.cookcounty.tax.domain.model.AgencyEqualizedValuation;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.AgencyEqualizedValuationEntity;

public class AgencyEqualizedValuationMapper {

    // GENERATED-MAPPING-TO-DOMAIN:start
    public static AgencyEqualizedValuation toDomain(AgencyEqualizedValuationEntity entity) {
        AgencyEqualizedValuation model = new AgencyEqualizedValuation();
        model.setId(entity.getId());

        model.setAgencyNumber(entity.getAgencyNumber());

        model.setAnnexedPropertyEqualizedValue(entity.getAnnexedPropertyEqualizedValue());

        model.setBurdenPercent(entity.getBurdenPercent());

        model.setConnectingAgency1(entity.getConnectingAgency1());

        model.setConnectingAgency2(entity.getConnectingAgency2());

        model.setConnectingAgency3(entity.getConnectingAgency3());

        model.setConnectingAgency4(entity.getConnectingAgency4());

        model.setCookCountyAirPollutionValue(entity.getCookCountyAirPollutionValue());

        model.setCookCountyRailroadValue(entity.getCookCountyRailroadValue());

        model.setCookCountyRealEstateValue(entity.getCookCountyRealEstateValue());

        model.setCookCountyUseTaxValue(entity.getCookCountyUseTaxValue());

        model.setDeKalbCountyEqualizedValue(entity.getDeKalbCountyEqualizedValue());

        model.setDisconnectedPropertyEqualizedValue(entity.getDisconnectedPropertyEqualizedValue());

        model.setDisconnectedTifDifference(entity.getDisconnectedTifDifference());

        model.setDuPageCountyEqualizedValue(entity.getDuPageCountyEqualizedValue());

        model.setGrundyCountyEqualizedValue(entity.getGrundyCountyEqualizedValue());

        model.setKaneCountyEqualizedValue(entity.getKaneCountyEqualizedValue());

        model.setKankakeeCountyEqualizedValue(entity.getKankakeeCountyEqualizedValue());

        model.setKendallCountyEqualizedValue(entity.getKendallCountyEqualizedValue());

        model.setLaSalleCountyEqualizedValue(entity.getLaSalleCountyEqualizedValue());

        model.setLakeCountyEqualizedValue(entity.getLakeCountyEqualizedValue());

        model.setLimitingTaxRateOverride(entity.getLimitingTaxRateOverride());

        model.setLivingstonCountyEqualizedValue(entity.getLivingstonCountyEqualizedValue());

        model.setMcHenryCountyEqualizedValue(entity.getMcHenryCountyEqualizedValue());

        model.setNewPropertyEqualizedValue(entity.getNewPropertyEqualizedValue());

        model.setOverlapAnnexedPropertyEqualizedValue(entity.getOverlapAnnexedPropertyEqualizedValue());

        model.setOverlapDisconnectedPropertyEqualizedValue(entity.getOverlapDisconnectedPropertyEqualizedValue());

        model.setOverlapDisconnectedTifDifference(entity.getOverlapDisconnectedTifDifference());

        model.setOverlapNewPropertyEqualizedValue(entity.getOverlapNewPropertyEqualizedValue());

        model.setParentAgency1(entity.getParentAgency1());

        model.setParentAgency2(entity.getParentAgency2());

        model.setParentAgency3(entity.getParentAgency3());

        model.setParentAgency4(entity.getParentAgency4());

        model.setParentAgency5(entity.getParentAgency5());

        model.setPreviousTaxYear1(entity.getPreviousTaxYear1());

        model.setPreviousTaxYear1Extension(entity.getPreviousTaxYear1Extension());

        model.setPreviousTaxYear2(entity.getPreviousTaxYear2());

        model.setPreviousTaxYear2Extension(entity.getPreviousTaxYear2Extension());

        model.setPreviousTaxYear3(entity.getPreviousTaxYear3());

        model.setPreviousTaxYear3Extension(entity.getPreviousTaxYear3Extension());

        model.setTaxCapIndicator(entity.getTaxCapIndicator());

        model.setTaxYear(entity.getTaxYear());

        model.setWillCountyEqualizedValue(entity.getWillCountyEqualizedValue());



        return model;
    }
    // GENERATED-MAPPING-TO-DOMAIN:end

    // GENERATED-MAPPING-TO-ENTITY:start
    public static AgencyEqualizedValuationEntity toEntity(AgencyEqualizedValuation model) {
        AgencyEqualizedValuationEntity entity = new AgencyEqualizedValuationEntity();
        entity.setId(model.getId());

        entity.setAgencyNumber(model.getAgencyNumber());

        entity.setAnnexedPropertyEqualizedValue(model.getAnnexedPropertyEqualizedValue());

        entity.setBurdenPercent(model.getBurdenPercent());

        entity.setConnectingAgency1(model.getConnectingAgency1());

        entity.setConnectingAgency2(model.getConnectingAgency2());

        entity.setConnectingAgency3(model.getConnectingAgency3());

        entity.setConnectingAgency4(model.getConnectingAgency4());

        entity.setCookCountyAirPollutionValue(model.getCookCountyAirPollutionValue());

        entity.setCookCountyRailroadValue(model.getCookCountyRailroadValue());

        entity.setCookCountyRealEstateValue(model.getCookCountyRealEstateValue());

        entity.setCookCountyUseTaxValue(model.getCookCountyUseTaxValue());

        entity.setDeKalbCountyEqualizedValue(model.getDeKalbCountyEqualizedValue());

        entity.setDisconnectedPropertyEqualizedValue(model.getDisconnectedPropertyEqualizedValue());

        entity.setDisconnectedTifDifference(model.getDisconnectedTifDifference());

        entity.setDuPageCountyEqualizedValue(model.getDuPageCountyEqualizedValue());

        entity.setGrundyCountyEqualizedValue(model.getGrundyCountyEqualizedValue());

        entity.setKaneCountyEqualizedValue(model.getKaneCountyEqualizedValue());

        entity.setKankakeeCountyEqualizedValue(model.getKankakeeCountyEqualizedValue());

        entity.setKendallCountyEqualizedValue(model.getKendallCountyEqualizedValue());

        entity.setLaSalleCountyEqualizedValue(model.getLaSalleCountyEqualizedValue());

        entity.setLakeCountyEqualizedValue(model.getLakeCountyEqualizedValue());

        entity.setLimitingTaxRateOverride(model.getLimitingTaxRateOverride());

        entity.setLivingstonCountyEqualizedValue(model.getLivingstonCountyEqualizedValue());

        entity.setMcHenryCountyEqualizedValue(model.getMcHenryCountyEqualizedValue());

        entity.setNewPropertyEqualizedValue(model.getNewPropertyEqualizedValue());

        entity.setOverlapAnnexedPropertyEqualizedValue(model.getOverlapAnnexedPropertyEqualizedValue());

        entity.setOverlapDisconnectedPropertyEqualizedValue(model.getOverlapDisconnectedPropertyEqualizedValue());

        entity.setOverlapDisconnectedTifDifference(model.getOverlapDisconnectedTifDifference());

        entity.setOverlapNewPropertyEqualizedValue(model.getOverlapNewPropertyEqualizedValue());

        entity.setParentAgency1(model.getParentAgency1());

        entity.setParentAgency2(model.getParentAgency2());

        entity.setParentAgency3(model.getParentAgency3());

        entity.setParentAgency4(model.getParentAgency4());

        entity.setParentAgency5(model.getParentAgency5());

        entity.setPreviousTaxYear1(model.getPreviousTaxYear1());

        entity.setPreviousTaxYear1Extension(model.getPreviousTaxYear1Extension());

        entity.setPreviousTaxYear2(model.getPreviousTaxYear2());

        entity.setPreviousTaxYear2Extension(model.getPreviousTaxYear2Extension());

        entity.setPreviousTaxYear3(model.getPreviousTaxYear3());

        entity.setPreviousTaxYear3Extension(model.getPreviousTaxYear3Extension());

        entity.setTaxCapIndicator(model.getTaxCapIndicator());

        entity.setTaxYear(model.getTaxYear());

        entity.setWillCountyEqualizedValue(model.getWillCountyEqualizedValue());



        return entity;
    }
    // GENERATED-MAPPING-TO-ENTITY:end
}
