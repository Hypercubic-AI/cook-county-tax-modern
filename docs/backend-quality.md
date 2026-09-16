# Backend quality and source compatibility

A parcel identifier such as `000000000042007` must survive a database round trip without losing its zeros. A completed run must remain readable after the application restarts. These are behavior contracts, not formatting preferences.

The backend applies the HC-703 quality rules without changing the accepted HTTP contracts or source-compatible report formats. Internal Java names describe their purpose. Existing program labels, report headers, dataset names, and rule identifiers remain where an external contract requires them.

## Verification

Use Java 25. Run the complete backend verification from the repository root:

```sh
cd backend
mvn verify
```

Verification runs Checkstyle, Error Prone, NullAway, JUnit, jqwik, ArchUnit, PostgreSQL integration tests, and PIT. PIT requires a mutation score of at least 70 percent. Do not lower the threshold or exclude production classes to make a failure disappear.

The final HC-703 verification in the enclosing Isomorphic application passed `mvn verify` with 224 tests. It had no failures, errors, or skips. Checkstyle reported zero violations. PIT reported a 77 percent mutation score and 90 percent line coverage for mutated classes. Compiler warnings remain, so this result does not mean that every static-analysis finding is resolved.

PostgreSQL integration tests use Testcontainers by default. To use an existing test server, set all three variables:

```text
TEST_DATABASE_URL
TEST_DATABASE_USERNAME
TEST_DATABASE_PASSWORD
```

Each test context creates and removes a temporary schema. Tests must not target a production database. The `backend-quality` GitHub Actions job supplies one PostgreSQL 17 service to all test and PIT JVMs. Its credentials apply only to that temporary CI service.

## Nullability and expected failures

Each Java package declares JSpecify `@NullMarked`. A nullable field uses `@Nullable` because absence is a real state. For example, an unsaved entity has no generated identity. A request field can also be absent before Bean Validation runs.

Local variables rely on flow analysis instead of `@Nullable` annotations. Entity reference fields can remain null during hydration. Required getters reject premature reads with `Objects.requireNonNull`. Getters for optional values retain nullable return contracts. Do not fabricate empty strings or zero values to satisfy the checker.

Expected business outcomes use `Result<Success, Failure>`. Infrastructure exceptions remain exceptions. The inbound adapter handles every expected failure and preserves the accepted HTTP status and body.

For example, the current start endpoints return HTTP 201 for creation and an exact replay. A replay retains the original run identity. A conflicting request returns HTTP 409. A general style rule does not authorize a change to those responses.

## Spring composition

HTTP and batch adapters own transport mapping. Application services own orchestration and business validation order. Spring supplies managed collaborators through constructors. The official [Spring bean introduction](https://docs.spring.io/spring-framework/reference/core/beans/introduction.html) explains the container and the beans that it manages.

Use constructor injection for required collaborators. Constructor injection makes required dependencies visible and supports direct unit construction. Do not use field injection or application-context lookups.

The increment processor receives its stateless kernel, output projector, and qualified transaction operations through its constructor. Its transaction callback still covers one complete step. Do not turn request values or per-run state into singleton beans.

Transactional services must support Spring's proxy mechanism. A call through `this` does not pass through that proxy. Keep transaction ownership explicit when you move a method between classes.

## Javadoc that explains a contract

The documentation for `Result.flatMap` describes when an operation runs and which failure survives:

```java
/// Composes operations without exceptions for expected failures.
///
/// The next operation runs only for a success. Its failure replaces that success. An existing
/// failure bypasses the next operation and keeps its original value.
///
/// @param transform next operation that returns a nonnull typed outcome
/// @param <U> next operation's success type
/// @return the next outcome, or the original failure value
```

This description tells a caller more than “maps the result.” It describes the success and failure paths without requiring the caller to read the method body.

The `BatchRunStore` documentation explains ownership and recovery:

```java
/// The database owns run identifiers and capability-scoped replay keys. Each nonterminal run has
/// one owner and a bounded lease. Recovery changes an expired run to its persisted failure
/// snapshot. Recovery never submits work. A terminal state cannot change again.
```

A useful field description identifies its meaning, units, and legal absence. A useful method description identifies side effects, failures, and transaction ownership. Do not infer a legal range from the few values in a test fixture.

## Persistence and restart boundaries

Domain records and HTTP records are separate from mutable JPA entities. Repository adapters own database conversion. Optimistic versions prevent a stale batch snapshot from replacing a newer write.

Business identifiers retain their source representation. Decimal quantities use `BigDecimal` with explicit precision and scale. Valid business dates use `java.time`. Source fields that admit noncalendar values retain their raw representation instead of rejecting an established source state.

The database owns batch identity, replay keys, response snapshots, and leases. A restarted process can read completed results without a process-local cache. Recovery marks expired nonterminal runs as failed. It does not automatically repeat a possibly non-idempotent business step.

### Deploying migration V23

1. Stop accepting new work on every pre-V23 application instance.
2. Wait for active runs to finish.
3. Stop the pre-V23 instances.
4. Apply V23 and start the updated application.
5. Check persisted run states before you submit new work.

Do not run pre-V23 and V23 instances together. Older instances cannot renew leases. Application hosts must synchronize UTC clocks. Set `BATCH_RUN_LEASE` longer than the longest expected application or database pause.

Production startup requires `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, and `CORS_ALLOWED_ORIGINS`. Configure these values for the deployment. Do not add literal credentials or permissive production defaults.

## Reference data and evidence

Maintain production input through the repository ports. Do not replace an incomplete reference population with a hardcoded table copied from a scenario. Scenario records belong in test fixtures, not new-deployment migrations.

Use source code, layouts, accepted specifications, and complete observations to choose descriptive names. Keep uncertainty explicit. Put the mapping to opaque source identifiers in evidence instead of Java class names.

Golden assertions read the reviewed artifacts. The increment test derives its expected frozen-value scalars from the golden report instead of a second table of expected numbers. The original capture represents some packed-decimal fields as text. Its byte representation and report representation require different decoding.

## Qodana migration review

The local Qodana Community for JVM 2026.2.1 review in the enclosing Isomorphic application reduced findings from 364 to 143. The comparison reported 221 resolved findings and no new findings.

The corrections included 172 incomplete entity nullability contracts, 30 local nullability annotations, eight helper parameter contracts, and six implicit exact-arithmetic policies. The hydration regression failed in eight cases before the correction and passed afterward. A numeric smoke probe preserved value, scale, and rejection behavior in 24 comparisons.

The remaining findings are not all confirmed defects. They include 114 explicit annotation defaults, 12 defensive checks, six calls that validate values, and five JPA-managed fields. Do not delete persistence fields or validation calls to reduce the count.

Qodana identified migration gaps that the passing build did not report. It remains a migration review tool, not a permanent gate based on the raw finding count. NullAway, Checkstyle, and behavior tests remain part of normal verification.

## Authoring substitutions

The enclosing Isomorphic application owns the authoring substitutions that generated this backend. That application also owns their source snapshots, SHA-256 checks, application configuration, and replay commands. This standalone repository does not contain `rifle/refire`, so it does not present substitution commands that cannot run here.

Run substitution replay and fresh generation from the enclosing application. Treat those checks as authoring evidence. They do not replace `cd backend && mvn verify` in this repository.
