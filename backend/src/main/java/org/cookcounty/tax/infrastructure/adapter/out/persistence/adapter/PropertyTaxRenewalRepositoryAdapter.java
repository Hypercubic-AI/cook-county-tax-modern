package org.cookcounty.tax.infrastructure.adapter.out.persistence.adapter;

import org.cookcounty.tax.domain.model.PropertyTaxRenewal;
import org.cookcounty.tax.domain.port.out.PropertyTaxRenewalRepository;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.repository.JpaPropertyTaxRenewalRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/// Loads the complete repository-backed renewal input without embedded fixture values.
///
/// The adapter preserves the stored source order required by renewal matching.
@Component
public final class PropertyTaxRenewalRepositoryAdapter implements PropertyTaxRenewalRepository {
    private final JpaPropertyTaxRenewalRepository repository;

    /// Creates the renewal adapter over the maintained reference repository.
    public PropertyTaxRenewalRepositoryAdapter(JpaPropertyTaxRenewalRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<PropertyTaxRenewal> findAllInSourceOrder() {
        return repository.findAllByOrderBySourceOrderAsc().stream()
                .map(row -> new PropertyTaxRenewal(row.getPropertyNumber(), row.getBatchNumber()))
                .toList();
    }
}
