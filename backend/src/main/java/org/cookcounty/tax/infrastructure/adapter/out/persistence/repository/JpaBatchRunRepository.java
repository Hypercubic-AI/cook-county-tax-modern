package org.cookcounty.tax.infrastructure.adapter.out.persistence.repository;

import org.cookcounty.tax.domain.port.out.BatchRunStore.State;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.BatchRunEntity;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

/// JPA access for persisted request-driven batch runs.
public interface JpaBatchRunRepository extends JpaRepository<BatchRunEntity, Long> {

    /// Finds the unique reservation for a capability and caller key.
    Optional<BatchRunEntity> findByCapabilityAndIdempotencyKey(
            String capability, String idempotencyKey);

    /// Finds a run without allowing an identifier to select another capability's response.
    Optional<BatchRunEntity> findByCapabilityAndId(String capability, Long id);

    /// Extends a lease only for its current owner and a nonterminal run.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            """
            update BatchRunEntity run
               set run.leaseExpiresAt = :leaseExpiresAt,
                   run.version = run.version + 1
             where run.capability = :capability
               and run.id = :id
               and run.ownerId = :ownerId
               and run.state in :nonterminalStates
            """)
    int renew(
            @Param("capability") String capability,
            @Param("id") long id,
            @Param("ownerId") String ownerId,
            @Param("leaseExpiresAt") Instant leaseExpiresAt,
            @Param("nonterminalStates") Collection<State> nonterminalStates);

    /// Publishes one snapshot if recovery or completion has not made the run terminal.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            """
            update BatchRunEntity run
               set run.responseJson = :responseJson,
                   run.state = :targetState,
                   run.ownerId = :nextOwnerId,
                   run.leaseExpiresAt = :leaseExpiresAt,
                   run.version = run.version + 1
             where run.capability = :capability
               and run.id = :id
               and run.ownerId = :ownerId
               and run.state in :nonterminalStates
            """)
    int replaceOwned(
            @Param("capability") String capability,
            @Param("id") long id,
            @Param("ownerId") String ownerId,
            @Param("targetState") State targetState,
            @Param("responseJson") String responseJson,
            @Param("nextOwnerId") @Nullable String nextOwnerId,
            @Param("leaseExpiresAt") @Nullable Instant leaseExpiresAt,
            @Param("nonterminalStates") Collection<State> nonterminalStates);

    /// Changes only expired nonterminal runs to their prebuilt recovery snapshot.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            """
            update BatchRunEntity run
               set run.responseJson = run.recoveryResponseJson,
                   run.state = :failedState,
                   run.ownerId = null,
                   run.leaseExpiresAt = null,
                   run.version = run.version + 1
             where run.state in :nonterminalStates
               and run.leaseExpiresAt <= :now
            """)
    int recoverExpired(
            @Param("now") Instant now,
            @Param("failedState") State failedState,
            @Param("nonterminalStates") Collection<State> nonterminalStates);
}
