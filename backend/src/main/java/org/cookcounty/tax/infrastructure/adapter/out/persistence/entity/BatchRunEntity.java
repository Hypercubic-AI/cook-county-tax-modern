package org.cookcounty.tax.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import org.cookcounty.tax.domain.port.out.BatchRunStore.State;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.Objects;

/// Persistence model for a request-driven batch run.
///
/// The response and recovery snapshots are JSON because each capability has a distinct public
/// schema. Ownership and lease columns let all instances arbitrate recovery in the database.
@Entity
@Table(
        name = "batch_runs",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uq_batch_runs_capability_key",
                        columnNames = {"capability", "idempotency_key"}))
public class BatchRunEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    @Version private @Nullable Long version;

    @Column(nullable = false, length = 64)
    private @Nullable String capability;

    @Column(name = "idempotency_key", nullable = false, length = 128)
    private @Nullable String idempotencyKey;

    @Column(nullable = false, length = 512)
    private @Nullable String fingerprint;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_json", nullable = false, columnDefinition = "jsonb")
    private @Nullable String responseJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "recovery_response_json", nullable = false, columnDefinition = "jsonb")
    private @Nullable String recoveryResponseJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private @Nullable State state;

    @Column(name = "owner_id", length = 128)
    private @Nullable String ownerId;

    @Column(name = "lease_expires_at")
    private @Nullable Instant leaseExpiresAt;

    /// Creates a transient shell for Hibernate.
    protected BatchRunEntity() {}

    /// Creates a queued reservation. The adapter sets both snapshots before commit.
    public BatchRunEntity(
            String capability,
            String idempotencyKey,
            String fingerprint,
            String ownerId,
            Instant leaseExpiresAt) {
        this.capability = requireText(capability, "capability", 64);
        this.idempotencyKey = requireText(idempotencyKey, "idempotencyKey", 128);
        this.fingerprint = requireText(fingerprint, "fingerprint", 512);
        this.ownerId = requireText(ownerId, "ownerId", 128);
        this.leaseExpiresAt = Objects.requireNonNull(leaseExpiresAt, "leaseExpiresAt");
        this.state = State.QUEUED;
        this.responseJson = "{}";
        this.recoveryResponseJson = "{}";
    }

    /// Returns the database identity. A transient entity has no identity.
    ///
    /// @return assigned identity, or null before persistence
    public @Nullable Long getId() {
        return id;
    }

    /// Returns the canonical request fingerprint for exact replay checks.
    public String getFingerprint() {
        return Objects.requireNonNull(fingerprint, "run fingerprint is not initialized");
    }

    /// Returns the current complete public response as JSON.
    public String getResponseJson() {
        return Objects.requireNonNull(responseJson, "run response is not initialized");
    }

    /// Sets the queued and recovery snapshots inside the reservation transaction.
    public void setSnapshots(String responseJson, String recoveryResponseJson) {
        this.responseJson = Objects.requireNonNull(responseJson, "responseJson");
        this.recoveryResponseJson =
                Objects.requireNonNull(recoveryResponseJson, "recoveryResponseJson");
    }

    private static String requireText(String value, String name, int maximumLength) {
        Objects.requireNonNull(value, name);
        if (value.isBlank() || value.length() > maximumLength) {
            throw new IllegalArgumentException(name + " is blank or exceeds its storage limit");
        }
        return value;
    }
}
