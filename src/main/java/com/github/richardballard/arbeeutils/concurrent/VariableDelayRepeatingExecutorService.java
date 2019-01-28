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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import com.github.richardballard.arbeeutils.time.TimeTick;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * This class is a variant of the basic function of {@link ScheduledExecutorService} but it allows the operation
 * that gets run to decide whether to run again or not.  The delay between execution is specified when
 * {@link #start(OperationConfig, Supplier, Duration)} is called.  The execution delay can be variable based on time,
 * for example a user initiated call to a service where initially you poll frequently for a response but over time you
 * can poll progressively less frequently.
 */
@SuppressWarnings("WeakerAccess")
@ThreadSafe
public class VariableDelayRepeatingExecutorService {
  private static final Logger LOGGER = LoggerFactory.getLogger(VariableDelayRepeatingExecutorService.class);

  private final @NotNull ScheduledExecutorService executorService;

  private final @NotNull Supplier<? extends TimeTick> currentTimeTickSupplier;

  public VariableDelayRepeatingExecutorService(final @NotNull ScheduledExecutorService executorService,
                                               final @NotNull Supplier<? extends TimeTick> currentTimeTickSupplier) {

    this.executorService = executorService;
    this.currentTimeTickSupplier = currentTimeTickSupplier;
  }

  private @NotNull ImmutableSortedMap<TimeTick, Duration> mapFromOperationConfig(
      final @NotNull OperationConfig operationConfig) {

    final ImmutableSortedMap.Builder<TimeTick, Duration> mapBuilder = ImmutableSortedMap.naturalOrder();

    TimeTick tick = currentTimeTickSupplier.get();
    for(final OperationConfig.FrequencyForDuration frequencyForDuration :
        operationConfig.getFrequencyForDurations()) {

      // translate it from 'how long this frequency occurs for' to actual time ticks
      mapBuilder.put(tick,
                     frequencyForDuration.getExecutionFrequency());

      tick = tick.plus(frequencyForDuration.getActiveForDuration());
    }

    // add the final one
    mapBuilder.put(tick,
                   operationConfig.getFinalExecutionFrequency());

    return mapBuilder.build();
  }

  private void schedule(final @NotNull DelayedExecution execution) {

    executorService.schedule(execution.getRunningConfig(),
                             execution.getDelay().toNanos(),
                             TimeUnit.NANOSECONDS);
  }

  /**
   * Start an operation (after waiting for {@code initialDelay}).
   */
  public void start(final @NotNull OperationConfig operationConfig,
                    final @NotNull Supplier<WhatToDoNext> operation,
                    final @NotNull Duration initialDelay) {

    final RunningConfig runningConfig = new RunningConfig(operation,
                                                          mapFromOperationConfig(operationConfig),
                                                          this::schedule,
                                                          currentTimeTickSupplier);

    schedule(new DelayedExecution(runningConfig,
                                  initialDelay));
  }

  @VisibleForTesting
  @ThreadSafe
  static class RunningConfig implements Runnable {
    private final @NotNull Supplier<WhatToDoNext> operation;

    private final @NotNull ImmutableSortedMap<TimeTick, Duration> timeTickToExecutionFrequencyMap;

    private final @NotNull Consumer<? super DelayedExecution> executionReceiver;

    private final @NotNull Supplier<? extends TimeTick> currentTimeTickSupplier;

    public RunningConfig(final @NotNull Supplier<WhatToDoNext> operation,
                         final @NotNull ImmutableSortedMap<TimeTick, Duration> timeTickToExecutionFrequencyMap,
                         final @NotNull Consumer<? super DelayedExecution> executionReceiver,
                         final @NotNull Supplier<? extends TimeTick> currentTimeTickSupplier) {

      Preconditions.checkArgument(!timeTickToExecutionFrequencyMap.isEmpty());

      this.operation = operation;
      this.timeTickToExecutionFrequencyMap = timeTickToExecutionFrequencyMap;
      this.executionReceiver = executionReceiver;
      this.currentTimeTickSupplier = currentTimeTickSupplier;
    }

    @Override
    public void run() {
      WhatToDoNext whatToDoNext;
      try {
        whatToDoNext = operation.get();
      }
      catch(final RuntimeException exc) {
        LOGGER.warn("caught exception when executing operation",
                    exc);

        whatToDoNext = WhatToDoNext.STOP_EXECUTION;
      }

      if(whatToDoNext == WhatToDoNext.DELAY_THEN_EXECUTE) {
        final Map.Entry<TimeTick, Duration> floorEntry
            = timeTickToExecutionFrequencyMap.floorEntry(currentTimeTickSupplier.get());

        // this should never be null as when the map is constructed the current time (which is prior to 'now')
        // is added
        Preconditions.checkState(floorEntry != null);

        final Duration delay = floorEntry.getValue();

        executionReceiver.accept(new DelayedExecution(this,
                                                      delay));
      }
    }
  }

  @Immutable
  private static class DelayedExecution {
    private final @NotNull RunningConfig runningConfig;

    private final @NotNull Duration delay;

    public DelayedExecution(final @NotNull RunningConfig runningConfig,
                            final @NotNull Duration delay) {

      this.runningConfig = runningConfig;
      this.delay = delay;
    }

    public @NotNull RunningConfig getRunningConfig() {
      return runningConfig;
    }

    public @NotNull Duration getDelay() {
      return delay;
    }
  }

  @Immutable
  public enum WhatToDoNext {
    /**
     * Wait the appropriate delay before performing another execution.
     */
    DELAY_THEN_EXECUTE,

    /**
     * Do not perform any more executions
     */
    STOP_EXECUTION
  }

  @Immutable
  public static class OperationConfig {

    private final @NotNull ImmutableList<FrequencyForDuration> frequencyForDurations;

    private final @NotNull Duration finalExecutionFrequency;

    /**
     * Set up a map from 'time block' (i.e. when this is in effect) to execution frequency.  For example:
     * <p/>
     * - activeFor 2s, executionFreq 10s
     * - activeFor 5s, executionFreq 13s
     * - finalExecFreq 30s
     * <p/>
     * In the config above it is saying "for the first 2s then the execution frequency is 10, for the next 5s the
     * execution frequency is 13s.  After this previous 5s is complete then for every time thereafter an execution
     * frequency of 30s is in effect"
     *
     * @param frequencyForDurations A list of durations (bound for a specified time period).
     * @param finalExecutionFrequency When all of the elements of {@code frequencyForDurations} have passed this is
     *                                the execution frequency that will be in effect.
     */
    public OperationConfig(final @NotNull ImmutableList<FrequencyForDuration> frequencyForDurations,
                           final @NotNull Duration finalExecutionFrequency) {

      this.frequencyForDurations = frequencyForDurations;
      this.finalExecutionFrequency = finalExecutionFrequency;
    }

    public @NotNull ImmutableList<FrequencyForDuration> getFrequencyForDurations() {
      return frequencyForDurations;
    }

    public @NotNull Duration getFinalExecutionFrequency() {
      return finalExecutionFrequency;
    }

    @Immutable
    public static class FrequencyForDuration {
      private final @NotNull Duration activeForDuration;

      private final @NotNull Duration executionFrequency;

      /**
       *
       * @param activeForDuration how long this execution frequency is in effect for
       * @param executionFrequency the execution frequency when time is within the {@code activeForDuration}
       */
      public FrequencyForDuration(final @NotNull Duration activeForDuration,
                                  final @NotNull Duration executionFrequency) {

        this.activeForDuration = activeForDuration;
        this.executionFrequency = executionFrequency;
      }

      public @NotNull Duration getActiveForDuration() {
        return activeForDuration;
      }

      public @NotNull Duration getExecutionFrequency() {
        return executionFrequency;
      }
    }
  }
}
