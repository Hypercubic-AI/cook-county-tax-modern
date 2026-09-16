package org.cookcounty.tax.domain.contract;

import java.util.Objects;
import java.util.function.Function;

/// An expected outcome. Infrastructure exceptions remain exceptions.
///
/// @param <T> successful value type
/// @param <E> recoverable business failure type
public sealed interface Result<T, E> permits Result.Ok, Result.Err {
    /// A successful value. Neither the value nor the result is nullable.
    ///
    /// @param value successful operation result
    /// @param <T> successful value type
    /// @param <E> recoverable failure type of the enclosing operation
    record Ok<T, E>(T value) implements Result<T, E> {
        /// Rejects an absent success value at construction.
        public Ok {
            Objects.requireNonNull(value, "value");
        }
    }

    /// A recoverable failure that callers must handle explicitly.
    ///
    /// @param error recoverable failure with the information needed by the caller
    /// @param <T> successful value type of the enclosing operation
    /// @param <E> recoverable failure type
    record Err<T, E>(E error) implements Result<T, E> {
        /// Rejects an absent failure value at construction.
        public Err {
            Objects.requireNonNull(error, "error");
        }
    }

    /// Transforms a success without changing an existing failure.
    ///
    /// The transformation runs once for a success and does not run for a failure. Exceptions from
    /// the transformation remain exceptions.
    ///
    /// @param transform transformation that returns a nonnull successful value
    /// @param <U> transformed success type
    /// @return the transformed success, or the original failure value
    default <U> Result<U, E> map(Function<? super T, ? extends U> transform) {
        return switch (this) {
            case Ok<T, E>(var value) -> new Ok<>(transform.apply(value));
            case Err<T, E>(var error) -> new Err<>(error);
        };
    }

    /// Composes operations without exceptions for expected failures.
    ///
    /// The next operation runs only for a success. Its failure replaces that success. An existing
    /// failure bypasses the next operation and keeps its original value.
    ///
    /// @param transform next operation that returns a nonnull typed outcome
    /// @param <U> next operation's success type
    /// @return the next outcome, or the original failure value
    default <U> Result<U, E> flatMap(Function<? super T, Result<U, E>> transform) {
        return switch (this) {
            case Ok<T, E>(var value) -> Objects.requireNonNull(transform.apply(value));
            case Err<T, E>(var error) -> new Err<>(error);
        };
    }
}
