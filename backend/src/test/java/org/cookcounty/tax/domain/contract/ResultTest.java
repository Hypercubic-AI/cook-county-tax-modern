package org.cookcounty.tax.domain.contract;

import static com.google.common.truth.Truth.assertThat;

import static org.junit.jupiter.api.Assertions.assertThrows;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

class ResultTest {

    @Property
    void identityPreservesBothOutcomeAlternatives(@ForAll int value, @ForAll boolean rejected) {
        Result<Integer, String> input =
                rejected ? new Result.Err<>("rejected-" + value) : new Result.Ok<>(value);

        assertThat(input.map(item -> item)).isEqualTo(input);
        assertThat(input.flatMap(Result.Ok::new)).isEqualTo(input);
    }

    @Property
    void compositionPreservesSuccessAndFailureAcrossRegrouping(
            @ForAll @IntRange(min = -1_000, max = 1_000) int value) {
        Result<Integer, String> input = new Result.Ok<>(value);

        assertThat(input.flatMap(ResultTest::requireNonnegative))
                .isEqualTo(requireNonnegative(value));
        assertThat(
                        input.flatMap(ResultTest::requireNonnegative)
                                .flatMap(ResultTest::doubleWithinLimit))
                .isEqualTo(
                        input.flatMap(
                                item ->
                                        requireNonnegative(item)
                                                .flatMap(ResultTest::doubleWithinLimit)));
    }

    private static Result<Integer, String> requireNonnegative(int value) {
        return value < 0 ? new Result.Err<>("negative") : new Result.Ok<>(value);
    }

    private static Result<Integer, String> doubleWithinLimit(int value) {
        return value > 100 ? new Result.Err<>("over-limit") : new Result.Ok<>(value * 2);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("compositionCases")
    void compositionTransformsOnlySuccessfulValues(
            String caseName, Result<Integer, String> input, Result<String, String> expected) {
        Result<String, String> actual =
                input.map(value -> value + 1).flatMap(value -> new Result.Ok<>("value-" + value));

        assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> compositionCases() {
        return Stream.of(
                Arguments.of(
                        "success passes through map and flatMap",
                        new Result.Ok<Integer, String>(41),
                        new Result.Ok<String, String>("value-42")),
                Arguments.of(
                        "failure passes through without invoking transformations",
                        new Result.Err<Integer, String>("rejected"),
                        new Result.Err<String, String>("rejected")));
    }

    @Test
    void failureDoesNotInvokeEitherTransformation() {
        AtomicBoolean invoked = new AtomicBoolean();
        Result<Integer, String> failure = new Result.Err<>("rejected");

        Result<String, String> result =
                failure.map(
                                value -> {
                                    invoked.set(true);
                                    return value.toString();
                                })
                        .flatMap(
                                value -> {
                                    invoked.set(true);
                                    return new Result.Ok<>(value);
                                });

        assertThat(invoked.get()).isFalse();
        assertThat(result).isEqualTo(new Result.Err<String, String>("rejected"));
    }

    // This negative test deliberately violates static contracts to check runtime boundary guards.
    @org.jspecify.annotations.NullUnmarked
    @Test
    void resultAlternativesAndSuccessfulTransformationsRejectNull() {
        assertThrows(NullPointerException.class, () -> new Result.Ok<String, String>(null));
        assertThrows(NullPointerException.class, () -> new Result.Err<String, String>(null));
        assertThrows(
                NullPointerException.class,
                () -> new Result.Ok<String, String>("value").map(ignored -> null));
        assertThrows(
                NullPointerException.class,
                () -> new Result.Ok<String, String>("value").flatMap(ignored -> null));
    }
}
