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

package com.github.richardballard.arbeeutils.concurrent;

import com.github.richardballard.arbeetestutils.test.MoreMockUtils;
import com.github.richardballard.arbeeutils.time.TimeTick;
import com.github.richardballard.arbeeutils.time.TimeTicks;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@Test
public class VariableDelayRepeatingExecutorServiceTest {

    private static final ImmutableList<TimeTick> ANY_TIME_TICKS = ImmutableList.of(TimeTicks.explicitTimeTick(999L));
    private static final VariableDelayRepeatingExecutorService.OperationConfig ANY_OPERATION_CONFIG
            = new VariableDelayRepeatingExecutorService.OperationConfig(ImmutableList.of(),
                                                                        Duration.ofSeconds(999L));
    private static final Duration ANY_INITIAL_DELAY = Duration.ofSeconds(98765L);


    @NotNull
    private ScheduledExecutorService getExecutorService() {
        return mock(ScheduledExecutorService.class);
    }

    @NotNull
    private Supplier<TimeTick> getCurrentTimeTickSupplier(final @NotNull ImmutableList<TimeTick> timeTicks) {
        assert timeTicks != null;

        return MoreMockUtils.mockSupplierMultipleAnswers(timeTicks);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    private Supplier<VariableDelayRepeatingExecutorService.WhatToDoNext> getOperation(final int executeCount) {
        final Supplier<VariableDelayRepeatingExecutorService.WhatToDoNext> supplier = mock(Supplier.class);

        final AtomicInteger callCount = new AtomicInteger(0);

        when(supplier.get())
                // < executeCount because this count has to include the initial execution
                .thenAnswer(invocation -> (callCount.incrementAndGet() < executeCount)
                                          ? VariableDelayRepeatingExecutorService.WhatToDoNext.DELAY_THEN_EXECUTE
                                          : VariableDelayRepeatingExecutorService.WhatToDoNext.STOP_EXECUTION);

        return supplier;
    }

    public void initialDelayIsHonoured() {
        final ScheduledExecutorService executorService = getExecutorService();
        final VariableDelayRepeatingExecutorService service
                = new VariableDelayRepeatingExecutorService(executorService,
                                                            getCurrentTimeTickSupplier(ANY_TIME_TICKS));

        final Duration initialDelay = Duration.ofSeconds(123L);
        service.start(ANY_OPERATION_CONFIG,
                      getOperation(1),
                      initialDelay);

        verify(executorService).schedule(any(Runnable.class),
                                         eq(initialDelay.toNanos()),
                                         eq(TimeUnit.NANOSECONDS));

    }

    public void operationResultIsHonoured() {
        final ScheduledExecutorService executorService = getExecutorService();
        final VariableDelayRepeatingExecutorService service
                = new VariableDelayRepeatingExecutorService(executorService,
                                                            getCurrentTimeTickSupplier(ANY_TIME_TICKS));

        service.start(ANY_OPERATION_CONFIG,
                      getOperation(2),
                      ANY_INITIAL_DELAY);

        final ArgumentCaptor<VariableDelayRepeatingExecutorService.RunningConfig> runningConfigCaptor
                = ArgumentCaptor.forClass(VariableDelayRepeatingExecutorService.RunningConfig.class);

        verify(executorService).schedule(runningConfigCaptor.capture(),
                                         anyLong(),
                                         any(TimeUnit.class));

        final VariableDelayRepeatingExecutorService.RunningConfig runningConfig = runningConfigCaptor.getValue();

        verify(executorService, times(1)).schedule(eq(runningConfig),
                                                   anyLong(),
                                                   any(TimeUnit.class));

        runningConfig.run();

        verify(executorService, times(2)).schedule(eq(runningConfig),
                                                   anyLong(),
                                                   any(TimeUnit.class));

        runningConfig.run();

        // should not have been scheduled again
        verify(executorService, times(2)).schedule(eq(runningConfig),
                                                   anyLong(),
                                                   any(TimeUnit.class));



    }

    public void timeIsBrokenIntoBands() {
        final ScheduledExecutorService executorService = getExecutorService();
        final TimeTick initialTimeTick = TimeTicks.explicitTimeTick(100L);
        final Supplier<TimeTick> currentTimeTickSupplier = getCurrentTimeTickSupplier(ImmutableList.of(initialTimeTick));

        final VariableDelayRepeatingExecutorService service
                = new VariableDelayRepeatingExecutorService(executorService,
                                                            currentTimeTickSupplier);


        final Duration firstActive = Duration.ofNanos(10L);
        final Duration firstFrequency = Duration.ofNanos(100L);
        final Duration secondActive = Duration.ofNanos(3L);
        final Duration secondFrequency = Duration.ofNanos(300L);
        final Duration thirdActive = Duration.ofNanos(5L);
        final Duration thirdFrequency = Duration.ofNanos(500L);
        final Duration finalFrequency = Duration.ofNanos(900L);

        final VariableDelayRepeatingExecutorService.OperationConfig operationConfig
                = new VariableDelayRepeatingExecutorService.OperationConfig(ImmutableList.of(new VariableDelayRepeatingExecutorService.OperationConfig.FrequencyForDuration(firstActive,
                                                                                                                                                                            firstFrequency),
                                                                                             new VariableDelayRepeatingExecutorService.OperationConfig.FrequencyForDuration(secondActive,
                                                                                                                                                                            secondFrequency),
                                                                                             new VariableDelayRepeatingExecutorService.OperationConfig.FrequencyForDuration(thirdActive,
                                                                                                                                                                            thirdFrequency)),
                                                                            finalFrequency);
        service.start(operationConfig,
                      getOperation(99),
                      ANY_INITIAL_DELAY);

        final ArgumentCaptor<VariableDelayRepeatingExecutorService.RunningConfig> runningConfigCaptor
                = ArgumentCaptor.forClass(VariableDelayRepeatingExecutorService.RunningConfig.class);

        verify(executorService).schedule(runningConfigCaptor.capture(),
                                         anyLong(),
                                         any(TimeUnit.class));

        final VariableDelayRepeatingExecutorService.RunningConfig runningConfig = runningConfigCaptor.getValue();

        when(currentTimeTickSupplier.get())
                .thenReturn(initialTimeTick);

        runningConfig.run();

        verify(executorService,
               times(1)).schedule(any(Runnable.class),
                                  eq(firstFrequency.toNanos()),
                                  eq(TimeUnit.NANOSECONDS));

        // just before the boundary
        when(currentTimeTickSupplier.get())
                .thenReturn(initialTimeTick.plus(firstActive.minusNanos(1L)));

        runningConfig.run();

        verify(executorService,
               times(2)).schedule(any(Runnable.class),
                                  eq(firstFrequency.toNanos()),
                                  eq(TimeUnit.NANOSECONDS));

        // on the boundary
        when(currentTimeTickSupplier.get())
                .thenReturn(initialTimeTick.plus(firstActive));

        runningConfig.run();

        verify(executorService,
               times(1)).schedule(any(Runnable.class),
                                  eq(secondFrequency.toNanos()),
                                  eq(TimeUnit.NANOSECONDS));

        // somewhere in between boundaries
        when(currentTimeTickSupplier.get())
                .thenReturn(initialTimeTick.plus(firstActive.plus(Duration.ofNanos(2L))));

        runningConfig.run();

        verify(executorService,
               times(2)).schedule(any(Runnable.class),
                                  eq(secondFrequency.toNanos()),
                                  eq(TimeUnit.NANOSECONDS));

        // third
        when(currentTimeTickSupplier.get())
                .thenReturn(initialTimeTick.plus(firstActive.plus(secondActive)));

        runningConfig.run();

        verify(executorService,
               times(1)).schedule(any(Runnable.class),
                                  eq(thirdFrequency.toNanos()),
                                  eq(TimeUnit.NANOSECONDS));

        // after third
        when(currentTimeTickSupplier.get())
                .thenReturn(initialTimeTick.plus(firstActive.plus(secondActive.plus(thirdActive))));

        runningConfig.run();

        verify(executorService,
               times(1)).schedule(any(Runnable.class),
                                  eq(finalFrequency.toNanos()),
                                  eq(TimeUnit.NANOSECONDS));

        // waaaay after third
        when(currentTimeTickSupplier.get())
                .thenReturn(initialTimeTick.plus(Duration.ofHours(999L)));

        runningConfig.run();

        verify(executorService,
               times(2)).schedule(any(Runnable.class),
                                  eq(finalFrequency.toNanos()),
                                  eq(TimeUnit.NANOSECONDS));

    }
}
