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

package com.github.richardballard.arbeeutils.execution;


import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import com.github.richardballard.arbeeutils.numeric.Count;
import com.github.richardballard.arbeeutils.time.TimeTick;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This class has the concept of an operation that is performed - if it fails then it has the option to ask for a retry
 * (within a max duration).  This is all run on the current thread.
 */
@SuppressWarnings("WeakerAccess")
@ThreadSafe
public class TimeBoundAttempter<T> {

  private final @NotNull Duration maxOperationDuration;

  private final @NotNull Supplier<? extends TimeTick> currentTimeTickSupplier;

  private final @NotNull Consumer<? super OperationWithAttemptCount<T>> attemptFailedConsumer;

  private final @NotNull Function<? super OperationWithAttemptCount<T>, ? extends T> allAttemptsFailedFunction;


  /**
   *
   * @param maxOperationDuration the max duration that will be spent on retrying an unsuccessful operation
   * @param attemptFailedConsumer if an attempt fails then this will be called. Typically this will be a delay.
   * @param allAttemptsFailedFunction if all attempts fail then this will be called.  The result of this function
   *                                  will be used as the result of the call to {@link #performOperation(Supplier)}
   */
  public TimeBoundAttempter(final @NotNull Duration maxOperationDuration,
                            final @NotNull Supplier<? extends TimeTick> currentTimeTickSupplier,
                            final @NotNull Consumer<? super OperationWithAttemptCount<T>> attemptFailedConsumer,
                            final @NotNull Function<? super OperationWithAttemptCount<T>, ? extends T> allAttemptsFailedFunction) {

    this.maxOperationDuration = maxOperationDuration;
    this.currentTimeTickSupplier = currentTimeTickSupplier;
    this.attemptFailedConsumer = attemptFailedConsumer;
    this.allAttemptsFailedFunction = allAttemptsFailedFunction;
  }

  /**
   * @param operation this should throw {@link RetryRequestedException} if the operation should be retried
   * @return the operation result (if successful) or {@code allAttemptsFailedFunction} result if failed
   */
  public T performOperation(final @NotNull Supplier<? extends T> operation) {

    final TimeTick startTimeTick = currentTimeTickSupplier.get();
    final TimeTick maxTimeTick = startTimeTick.plus(maxOperationDuration);

    //noinspection ForLoopWithMissingComponent
    for(int attemptCount = 1; ; attemptCount++) {
      try {
        return operation.get();
      }
      catch(final RetryRequestedException ignored) {
        final OperationWithAttemptCount<T> operationWithAttemptCount
            = new OperationWithAttemptCount<>(operation,
                                              Count.valueOf(attemptCount,
                                                            Count.OnExceedBoundary.THROW));

        // if it is not past max
        if(currentTimeTickSupplier.get().compareTo(maxTimeTick) <= 0) {
          attemptFailedConsumer.accept(operationWithAttemptCount);
        }
        else {
          return allAttemptsFailedFunction.apply(operationWithAttemptCount);
        }
      }
    }
  }

  public static class RetryRequestedException extends RuntimeException {

    private static final long serialVersionUID = 3301371646563173566L;
  }

  @SuppressWarnings("unused")
  @Immutable
  public static class OperationWithAttemptCount<T> {
    private final @NotNull Supplier<? extends T> operation;

    private final @NotNull Count attemptCount;

    public OperationWithAttemptCount(final @NotNull Supplier<? extends T> operation,
                                     final @NotNull Count attemptCount) {

      this.operation = operation;
      this.attemptCount = attemptCount;
    }

    public @NotNull Supplier<? extends T> getOperation() {
      return operation;
    }

    public @NotNull Count getAttemptCount() {
      return attemptCount;
    }

    @Override
    public boolean equals(final Object o) {
      if(this == o) {
        return true;
      }
      if(o == null || getClass() != o.getClass()) {
        return false;
      }

      final OperationWithAttemptCount<?> that = (OperationWithAttemptCount<?>) o;

      if(!operation.equals(that.operation)) {
        return false;
      }
      return attemptCount.equals(that.attemptCount);

    }

    @Override
    public int hashCode() {
      int result = operation.hashCode();
      result = 31 * result + attemptCount.hashCode();
      return result;
    }

    @Override
    public String toString() {
      return "OperationWithAttemptCount{" +
             "operation=" + operation +
             ", attemptCount=" + attemptCount +
             '}';
    }
  }
}
