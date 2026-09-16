package org.cookcounty.tax.infrastructure.adapter.out.persistence.adapter;

import org.cookcounty.tax.domain.model.AgencyReference;
import org.cookcounty.tax.domain.model.TaxCodeMasterReference;
import org.cookcounty.tax.domain.model.TownReference;
import org.cookcounty.tax.domain.port.out.EifdTifReferenceDataRepository;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.mapper.EifdTifReferenceDataMapper;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.repository.JpaAgencyReferenceRepository;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.repository.JpaTaxCodeAgencySlotRepository;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.repository.JpaTaxCodeMasterReferenceRepository;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.repository.JpaTownReferenceRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/// JPA adapter for the persisted read-only increment reference populations.
@Component
public final class EifdTifReferenceDataRepositoryAdapter implements EifdTifReferenceDataRepository {
    private final JpaAgencyReferenceRepository agencyRepository;
    private final JpaTownReferenceRepository townRepository;
    private final JpaTaxCodeMasterReferenceRepository taxCodeRepository;
    private final JpaTaxCodeAgencySlotRepository agencySlotRepository;

    /// Connects each reference family and its ordered agency-position repository.
    public EifdTifReferenceDataRepositoryAdapter(
            JpaAgencyReferenceRepository agencyRepository,
            JpaTownReferenceRepository townRepository,
            JpaTaxCodeMasterReferenceRepository taxCodeRepository,
            JpaTaxCodeAgencySlotRepository agencySlotRepository) {
        this.agencyRepository = agencyRepository;
        this.townRepository = townRepository;
        this.taxCodeRepository = taxCodeRepository;
        this.agencySlotRepository = agencySlotRepository;
    }

    @Override
    public Optional<AgencyReference> findAgency(String agencyNumber) {
        return agencyRepository.findById(agencyNumber).map(EifdTifReferenceDataMapper::toDomain);
    }

    @Override
    public Optional<TownReference> findTown(String townNumber) {
        return townRepository.findById(townNumber).map(EifdTifReferenceDataMapper::toDomain);
    }

    @Override
    public Optional<TaxCodeMasterReference> findTaxCodeMaster(String taxCode) {
        return taxCodeRepository
                .findById(taxCode)
                .map(
                        source ->
                                EifdTifReferenceDataMapper.toDomain(
                                        source,
                                        agencySlotRepository
                                                .findAllByIdTaxCodeOrderByIdSlotPosition(taxCode)));
    }
}
