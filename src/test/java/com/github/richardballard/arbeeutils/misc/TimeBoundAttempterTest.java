/*
 * (C) Copyright 2016 Richard Ballard.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.richardballard.arbeeutils.misc;

import com.google.common.collect.ImmutableList;
import com.github.richardballard.arbeeutils.numeric.Count;
import com.github.richardballard.arbeeutils.test.MockUtils;
import com.github.richardballard.arbeeutils.time.TimeTick;
import com.github.richardballard.arbeeutils.time.TimeTicks;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@Test
public class TimeBoundAttempterTest {

    private static final String ANY_RESULT = "anyResult";

    private static final TimeTick TIME_TICK_1 = TimeTicks.explicitTimeTick(1L);
    private static final TimeTick TIME_TICK_2 = TimeTicks.explicitTimeTick(2L);
    private static final TimeTick TIME_TICK_3 = TimeTicks.explicitTimeTick(3L);

    @NotNull
    private Supplier<TimeTick> getCurrentTimeTickSupplier(@NotNull final ImmutableList<TimeTick> timeTicks) {
        assert timeTicks != null;

        return MockUtils.mockSupplierMultipleAnswers(timeTicks);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    private Consumer<TimeBoundAttempter.OperationWithAttemptCount<String>> getAttemptFailedConsumer() {
        return mock(Consumer.class);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    private Function<TimeBoundAttempter.OperationWithAttemptCount<String>, String> getAllAttemptsFailedFunction(@NotNull final String result) {
        assert result != null;

        return MockUtils.mockFunctionSingleAnswer((Class<TimeBoundAttempter.OperationWithAttemptCount<String>>)(Class<?>)TimeBoundAttempter.OperationWithAttemptCount.class,
                                                  result);
    }

    @NotNull
    private Supplier<String> getOperation(@NotNull final String value) {
        assert value != null;

        return MockUtils.mockSupplierSingleAnswer(value);
    }

    @SuppressWarnings("unchecked")
    public void successfulOperationDoesntCallHelpers() {
        final Consumer<TimeBoundAttempter.OperationWithAttemptCount<String>> attemptFailedConsumer
                = getAttemptFailedConsumer();

        final Function<TimeBoundAttempter.OperationWithAttemptCount<String>, String> allAttemptsFailedFunction
                = getAllAttemptsFailedFunction(ANY_RESULT);

        final TimeBoundAttempter<String> attempter
                = new TimeBoundAttempter<>(Duration.ofHours(1L),
                                           getCurrentTimeTickSupplier(ImmutableList.of(TIME_TICK_1)),
                                           attemptFailedConsumer,
                                           allAttemptsFailedFunction);

        final String operationResult = "cow";
        assertThat(attempter.performOperation(getOperation(operationResult)))
                .isEqualTo(operationResult);

        verify(attemptFailedConsumer, never()).accept(any(TimeBoundAttempter.OperationWithAttemptCount.class));
        verify(allAttemptsFailedFunction, never()).apply(any(TimeBoundAttempter.OperationWithAttemptCount.class));
    }

    @SuppressWarnings("unchecked")
    public void unsuccessfulOperationCallsAttemptFailed() {
        final Consumer<TimeBoundAttempter.OperationWithAttemptCount<String>> attemptFailedConsumer
                = getAttemptFailedConsumer();

        final Function<TimeBoundAttempter.OperationWithAttemptCount<String>, String> allAttemptsFailedFunction
                = getAllAttemptsFailedFunction(ANY_RESULT);

        final TimeBoundAttempter<String> attempter
                = new TimeBoundAttempter<>(Duration.ofHours(1L),
                                           getCurrentTimeTickSupplier(ImmutableList.of(TIME_TICK_1,
                                                                                       TIME_TICK_2)),
                                           attemptFailedConsumer,
                                           allAttemptsFailedFunction);

        final String operationResult = "cow";
        final Supplier<String> operation = getOperation(operationResult);
        final AtomicInteger callCount = new AtomicInteger(0);
        when(operation.get())
                .thenAnswer(invocation -> {
                    // the first 2 fail and the third passes
                    if(callCount.incrementAndGet() < 3) {
                        throw new TimeBoundAttempter.RetryRequestedException();
                    }
                    else {
                        return operationResult;
                    }
                });

        assertThat(attempter.performOperation(operation))
                .isEqualTo(operationResult);

        verify(attemptFailedConsumer)
                .accept(new TimeBoundAttempter.OperationWithAttemptCount<>(operation,
                                                                           Count.valueOf(1,
                                                                                         Count.OnExceedBoundary.THROW)));
        verify(attemptFailedConsumer)
                .accept(new TimeBoundAttempter.OperationWithAttemptCount<>(operation,
                                                                           Count.valueOf(2,
                                                                                         Count.OnExceedBoundary.THROW)));
        verify(attemptFailedConsumer, never())
                .accept(new TimeBoundAttempter.OperationWithAttemptCount<>(operation,
                                                                           Count.valueOf(3,
                                                                                         Count.OnExceedBoundary.THROW)));

        verify(allAttemptsFailedFunction, never()).apply(any(TimeBoundAttempter.OperationWithAttemptCount.class));
    }

    @SuppressWarnings("unchecked")
    public void unsuccessfulOperationCallsAllAttemptsFailed() {
        final Consumer<TimeBoundAttempter.OperationWithAttemptCount<String>> attemptFailedConsumer
                = getAttemptFailedConsumer();

        final String result = "bee";
        final Function<TimeBoundAttempter.OperationWithAttemptCount<String>, String> allAttemptsFailedFunction
                = getAllAttemptsFailedFunction(result);

        final TimeBoundAttempter<String> attempter
                = new TimeBoundAttempter<>(Duration.ofNanos(1L),
                                           getCurrentTimeTickSupplier(ImmutableList.of(TIME_TICK_1,
                                                                                       TIME_TICK_2,
                                                                                       TIME_TICK_3)),
                                           attemptFailedConsumer,
                                           allAttemptsFailedFunction);

        final Supplier<String> operation = getOperation(ANY_RESULT);
        when(operation.get())
                .thenAnswer(invocation -> {
                    throw new TimeBoundAttempter.RetryRequestedException();
                });

        assertThat(attempter.performOperation(operation))
                .isEqualTo(result);

        verify(attemptFailedConsumer)
                .accept(new TimeBoundAttempter.OperationWithAttemptCount<>(operation,
                                                                           Count.valueOf(1,
                                                                                         Count.OnExceedBoundary.THROW)));
        verify(attemptFailedConsumer, never())
                .accept(new TimeBoundAttempter.OperationWithAttemptCount<>(operation,
                                                                           Count.valueOf(2,
                                                                                         Count.OnExceedBoundary.THROW)));

        verify(allAttemptsFailedFunction)
                .apply(new TimeBoundAttempter.OperationWithAttemptCount<>(operation,
                                                                          Count.valueOf(2,
                                                                                        Count.OnExceedBoundary.THROW)));
    }

}
