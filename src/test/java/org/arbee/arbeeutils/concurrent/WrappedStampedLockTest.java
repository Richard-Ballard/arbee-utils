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

package org.arbee.arbeeutils.concurrent;

import com.google.common.collect.ImmutableList;
import org.arbee.arbeeutils.test.MockUtils;
import org.arbee.arbeeutils.time.TimeTick;
import org.arbee.arbeeutils.time.TimeTicks;
import org.jetbrains.annotations.NotNull;
import org.mockito.verification.VerificationMode;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.StampedLock;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@Test
public class WrappedStampedLockTest {

    private static final long ANY_STAMP = 999L;
    private static final boolean ANY_IS_VALID_STAMP = false;

    private static final ImmutableList<TimeTick> ANY_TIME_TICKS = ImmutableList.of();

    @NotNull
    private Supplier<TimeTick> getCurrentTimeTickSupplier(@NotNull final ImmutableList<TimeTick> timeTicks) {
        assert timeTicks != null;

        return MockUtils.mockSupplierMultipleAnswers(timeTicks);
    }

    @NotNull
    private StampedLock getDelegate(final long readLockStamp,
                                    final long optimisticReadStamp,
                                    final boolean isValidStamp,
                                    final long writeStamp,
                                    @NotNull final long... tryConvertToWriteLockStamp) throws InterruptedException {
        assert tryConvertToWriteLockStamp != null;

        assert tryConvertToWriteLockStamp.length > 0;

        final StampedLock lock = mock(StampedLock.class);

        when(lock.readLock())
                .thenReturn(readLockStamp);

        when(lock.tryReadLock(anyLong(),
                              any(TimeUnit.class)))
                .thenReturn(readLockStamp);

        when(lock.tryOptimisticRead())
                .thenReturn(optimisticReadStamp);

        when(lock.validate(anyLong()))
                .thenReturn(isValidStamp);

        when(lock.writeLock())
                .thenReturn(writeStamp);

        when(lock.tryWriteLock(anyLong(),
                               any(TimeUnit.class)))
                .thenReturn(writeStamp);


        final List<Long> allConvert = Arrays.stream(tryConvertToWriteLockStamp)
                                            .boxed()
                                            .collect(Collectors.toList());

        final Long[] subsequentConvert = (allConvert.size() > 1) ? allConvert.subList(1,
                                                                                      allConvert.size())
                                                                             .toArray(new Long[allConvert.size() - 1])
                                                                 : new Long[0];

        when(lock.tryConvertToWriteLock(anyLong()))
                .thenReturn(allConvert.get(0),
                            subsequentConvert);

        return lock;
    }

    @NotNull
    private StampedLock getAsDelegate(@NotNull final ReadWriteLock readWriteLock,
                                      @NotNull final Lock readLock,
                                      @NotNull final Lock writeLock) {
        assert readWriteLock != null;
        assert readLock != null;
        assert writeLock != null;

        final StampedLock lock = mock(StampedLock.class);

        when(lock.asReadWriteLock())
                .thenReturn(readWriteLock);

        when(lock.asReadLock())
                .thenReturn(readLock);

        when(lock.asWriteLock())
                .thenReturn(writeLock);

        return lock;
    }

    @NotNull
    private ReadWriteLock getReadWriteLock() {
        return mock(ReadWriteLock.class);
    }

    @NotNull
    private Lock getLock() {
        return mock(Lock.class);
    }

    @NotNull
    private WrappedStampedLock createSimpleWrappedStampedLock(@NotNull final StampedLock delegate) {
        assert delegate != null;

        return new WrappedStampedLock(delegate,
                                      WrappedReadWriteLock::new,
                                      WrappedLock::new,
                                      getCurrentTimeTickSupplier(ANY_TIME_TICKS));
    }

    public void pessimisticReadUnlocksOnSuccessful() throws InterruptedException {
        final long stamp = 123L;
        final StampedLock delegate = getDelegate(stamp,
                                                 ANY_STAMP,
                                                 ANY_IS_VALID_STAMP,
                                                 ANY_STAMP,
                                                 ANY_STAMP);

        final WrappedStampedLock lock = createSimpleWrappedStampedLock(delegate);

        final String result = "result";

        final Supplier<String> operation = () -> {
            verify(delegate).readLock();
            verify(delegate, never()).unlockRead(anyLong());

            return result;
        };

        assertThat(lock.pessimisticRead(operation))
                .isEqualTo(result);

        verify(delegate).unlockRead(stamp);

    }

    public void pessimisticReadUnlocksOnException() throws InterruptedException {
        final long stamp = 123L;
        final StampedLock delegate = getDelegate(stamp,
                                                 ANY_STAMP,
                                                 ANY_IS_VALID_STAMP,
                                                 ANY_STAMP,
                                                 ANY_STAMP);

        final WrappedStampedLock lock = createSimpleWrappedStampedLock(delegate);

        final RuntimeException exc = new RuntimeException("test");

        final Supplier<String> operation = () -> {
            verify(delegate).readLock();
            verify(delegate, never()).unlockRead(anyLong());

            throw exc;
        };

        assertThatThrownBy(() -> lock.pessimisticRead(operation))
                .isEqualTo(exc);

        verify(delegate).unlockRead(stamp);

    }

    public void pessimisticReadThrowsOnTimeout() throws InterruptedException {
        final long stamp = 0L;      // 0 means it couldn't acquire
        final StampedLock delegate = getDelegate(stamp,
                                                 ANY_STAMP,
                                                 ANY_IS_VALID_STAMP,
                                                 ANY_STAMP,
                                                 ANY_STAMP);

        final WrappedStampedLock lock = createSimpleWrappedStampedLock(delegate);

        final Duration timeout = Duration.ofSeconds(123L);
        assertThatThrownBy(() -> lock.pessimisticRead(() -> "not important",
                                                      timeout))
                .isInstanceOf(AcquireTimeoutException.class);

        verify(delegate).tryReadLock(timeout.toNanos(),
                                     TimeUnit.NANOSECONDS);
    }


    public void optimisticReadFallsBackToPessimistic() throws InterruptedException {
        final long readLockStamp = 123L;
        final long optimisticReadStamp = 234L;
        final StampedLock delegate = getDelegate(readLockStamp,
                                                 optimisticReadStamp,
                                                 false,
                                                 ANY_STAMP,
                                                 ANY_STAMP);

        final WrappedStampedLock lock = createSimpleWrappedStampedLock(delegate);

        final String operationResult = "result";

        assertThat(lock.optimisticRead(() -> operationResult))
                .isEqualTo(operationResult);

        verify(delegate, times(2)).tryOptimisticRead();
        verify(delegate).readLock();
    }

    public void optimisticReadReturnsIfValid() throws InterruptedException {
        final long optimisticReadStamp = 234L;
        final StampedLock delegate = getDelegate(ANY_STAMP,
                                                 optimisticReadStamp,
                                                 true,
                                                 ANY_STAMP,
                                                 ANY_STAMP);

        final WrappedStampedLock lock = createSimpleWrappedStampedLock(delegate);

        final String operationResult = "result";

        assertThat(lock.optimisticRead(() -> operationResult))
                .isEqualTo(operationResult);

        verify(delegate).tryOptimisticRead();
        verify(delegate, never()).readLock();
    }

    public void optimisticReadOkIfWithinTimeout() throws InterruptedException {
        final long readStamp = 234L;
        final StampedLock delegate = getDelegate(readStamp,
                                                 0L,        // fail the tryOptimistic read so it fails over to a pessimistic read
                                                 false,
                                                 ANY_STAMP,
                                                 ANY_STAMP);

        final TimeTick startTimeTick = TimeTicks.explicitTimeTick(1L);
        final TimeTick endTimeTick = startTimeTick.plus(Duration.ofSeconds(12L));
        final WrappedStampedLock lock = new WrappedStampedLock(delegate,
                                                               WrappedReadWriteLock::new,
                                                               WrappedLock::new,
                                                               getCurrentTimeTickSupplier(ImmutableList.of(startTimeTick,
                                                                                                           endTimeTick)));

        final String operationResult = "result";

        final Duration timeout = Duration.ofSeconds(20L);
        assertThat(lock.optimisticRead(() -> operationResult,
                                       timeout))
                .isEqualTo(operationResult);

        // make sure we actually hit the right code
        verify(delegate).tryReadLock(timeout.minus(endTimeTick.durationSince(startTimeTick)).toNanos(),
                                     TimeUnit.NANOSECONDS);
    }

    public void optimisticReadThrowsOnTimeout() throws InterruptedException {
        final StampedLock delegate = getDelegate(0L,        // 0 for a tryReadLock indicates a timeout
                                                 0L,        // fail the tryOptimistic read so it fails over to a pessimistic read
                                                 false,
                                                 ANY_STAMP,
                                                 ANY_STAMP);

        final TimeTick startTimeTick = TimeTicks.explicitTimeTick(1L);
        final TimeTick endTimeTick = startTimeTick.plus(Duration.ofSeconds(12L));
        final WrappedStampedLock lock = new WrappedStampedLock(delegate,
                                                               WrappedReadWriteLock::new,
                                                               WrappedLock::new,
                                                               getCurrentTimeTickSupplier(ImmutableList.of(startTimeTick,
                                                                                                           endTimeTick)));


        final Duration timeout = Duration.ofSeconds(3L);
        assertThatThrownBy(() -> lock.optimisticRead(() -> "not important",
                                                     timeout))
                .isInstanceOf(AcquireTimeoutException.class);

        // make sure we actually hit the right code
        verify(delegate).tryReadLock(timeout.minus(endTimeTick.durationSince(startTimeTick)).toNanos(),
                                     TimeUnit.NANOSECONDS);
    }


    public void writeUnlocksOnSuccess() throws InterruptedException {
        final long writeStamp = 123L;

        final StampedLock delegate = getDelegate(ANY_STAMP,
                                                 ANY_STAMP,
                                                 ANY_IS_VALID_STAMP,
                                                 writeStamp,
                                                 ANY_STAMP);

        final WrappedStampedLock lock = createSimpleWrappedStampedLock(delegate);

        final String operationResult = "result";

        final Supplier<String> operation = () -> {
            verify(delegate).writeLock();
            verify(delegate, never()).unlockWrite(anyLong());

            return operationResult;
        };

        assertThat(lock.write(operation))
                .isEqualTo(operationResult);

        verify(delegate).unlockWrite(writeStamp);
    }

    public void writeUnlocksOnException() throws InterruptedException {
        final long stamp = 123L;
        final StampedLock delegate = getDelegate(ANY_STAMP,
                                                 ANY_STAMP,
                                                 ANY_IS_VALID_STAMP,
                                                 stamp,
                                                 ANY_STAMP);

        final WrappedStampedLock lock = createSimpleWrappedStampedLock(delegate);

        final RuntimeException exc = new RuntimeException("test");

        final Supplier<String> operation = () -> {
            verify(delegate).writeLock();
            verify(delegate, never()).unlockWrite(anyLong());

            throw exc;
        };

        assertThatThrownBy(() -> lock.write(operation))
                .isEqualTo(exc);

        verify(delegate).unlockWrite(stamp);
    }


    public void writeThrowsOnTimeout() throws InterruptedException {
        final long stamp = 0L;      // 0 means it couldn't acquire
        final StampedLock delegate = getDelegate(ANY_STAMP,
                                                 ANY_STAMP,
                                                 ANY_IS_VALID_STAMP,
                                                 stamp,
                                                 ANY_STAMP);

        final WrappedStampedLock lock = createSimpleWrappedStampedLock(delegate);

        final Duration timeout = Duration.ofSeconds(123L);
        assertThatThrownBy(() -> lock.write(() -> "not important",
                                            timeout))
                .isInstanceOf(AcquireTimeoutException.class);

        verify(delegate).tryWriteLock(timeout.toNanos(),
                                      TimeUnit.NANOSECONDS);
    }

    @NotNull
    private Function<ReadWriteLock, WrappedReadWriteLock>
    getWrappedReadWriteLockFromReadWriteLockFunction(@NotNull final WrappedReadWriteLock wrappedReadWriteLock) {
        assert wrappedReadWriteLock != null;

        return MockUtils.mockFunctionSingleAnswer(ReadWriteLock.class,
                                                  wrappedReadWriteLock);
    }

    @NotNull
    private Function<Lock, WrappedLock>  getWrappedLockFromLockFunction(@NotNull final WrappedLock wrappedLock) {
        assert wrappedLock != null;

        return MockUtils.mockFunctionSingleAnswer(Lock.class,
                                                  wrappedLock);
    }

    @NotNull
    private WrappedReadWriteLock getWrappedReadWriteLock() {
        return mock(WrappedReadWriteLock.class);
    }

    @NotNull
    private WrappedLock getWrappedLock() {
        return mock(WrappedLock.class);
    }

    public void asReadWriteLockCallsFunction() {
        final WrappedReadWriteLock wrappedReadWriteLock = getWrappedReadWriteLock();

        final ReadWriteLock readWriteLock = getReadWriteLock();

        final Function<ReadWriteLock, WrappedReadWriteLock> wrappedReadWriteLockFromReadWriteLockFunction =
                getWrappedReadWriteLockFromReadWriteLockFunction(wrappedReadWriteLock);

        final WrappedStampedLock lock = new WrappedStampedLock(getAsDelegate(readWriteLock,
                                                                             getLock(),
                                                                             getLock()),
                                                               wrappedReadWriteLockFromReadWriteLockFunction,
                                                               getWrappedLockFromLockFunction(getWrappedLock()),
                                                               getCurrentTimeTickSupplier(ANY_TIME_TICKS));

        assertThat(lock.asReadWriteLock())
                .isEqualTo(wrappedReadWriteLock);

        verify(wrappedReadWriteLockFromReadWriteLockFunction).apply(readWriteLock);
    }

    public void asReadLockCallsFunction() {
        final WrappedLock wrappedLock = getWrappedLock();

        final Lock sourceLock = getLock();

        final Function<Lock, WrappedLock> wrappedLockFromLockFunction = getWrappedLockFromLockFunction(wrappedLock);

        final StampedLock delegate = getAsDelegate(getReadWriteLock(),
                                                   sourceLock,
                                                   getLock());

        final WrappedStampedLock lock = new WrappedStampedLock(delegate,
                                                               getWrappedReadWriteLockFromReadWriteLockFunction(getWrappedReadWriteLock()),
                                                               wrappedLockFromLockFunction,
                                                               getCurrentTimeTickSupplier(ANY_TIME_TICKS));

        assertThat(lock.asReadLock())
                .isEqualTo(wrappedLock);

        verify(delegate).asReadLock();
        verify(wrappedLockFromLockFunction).apply(sourceLock);
    }

    public void asWriteLockCallsFunction() {
        final WrappedLock wrappedLock = getWrappedLock();

        final Lock sourceLock = getLock();

        final Function<Lock, WrappedLock> wrappedLockFromLockFunction = getWrappedLockFromLockFunction(wrappedLock);

        final StampedLock delegate = getAsDelegate(getReadWriteLock(),
                                                   getLock(),
                                                   sourceLock);

        final WrappedStampedLock lock = new WrappedStampedLock(delegate,
                                                               getWrappedReadWriteLockFromReadWriteLockFunction(getWrappedReadWriteLock()),
                                                               wrappedLockFromLockFunction,
                                                               getCurrentTimeTickSupplier(ANY_TIME_TICKS));

        assertThat(lock.asWriteLock())
                .isEqualTo(wrappedLock);

        verify(delegate).asWriteLock();
        verify(wrappedLockFromLockFunction).apply(sourceLock);
    }

    @NotNull
    private Supplier<String> getWriteIfOperation(@NotNull final String value) {
        assert value != null;

        return MockUtils.mockSupplierSingleAnswer(value);
    }

    private void testWriteIfTestFails(@NotNull final WrappedStampedLock.TestFailedLockContext testFailedLockContext)
            throws InterruptedException {
        assert testFailedLockContext != null;

        final StampedLock delegate = getDelegate(ANY_STAMP,
                                                 ANY_STAMP,
                                                 ANY_IS_VALID_STAMP,
                                                 ANY_STAMP,
                                                 ANY_STAMP);

        final WrappedStampedLock lock = createSimpleWrappedStampedLock(delegate);

        final String failedText = "failed";
        final Supplier<String> testPassedOperation = getWriteIfOperation("passed");
        final Supplier<String> testFailedOperation = getWriteIfOperation(failedText);

        when(testFailedOperation.get())
                .thenAnswer(invocation -> {
                    final VerificationMode verificationMode;

                    if(testFailedLockContext == WrappedStampedLock.TestFailedLockContext.NO_LOCK) {
                        verificationMode = times(1);
                    }
                    else if(testFailedLockContext == WrappedStampedLock.TestFailedLockContext.IN_PESSIMISTIC_READ_LOCK) {
                        verificationMode = never();
                    }
                    else {
                        throw new IllegalStateException("unknown context - " + testFailedLockContext);
                    }

                    verify(delegate, verificationMode).unlock(anyLong());

                    return failedText;
                });

        final BooleanSupplier test = MockUtils.mockBooleanSupplierSingleAnswer(false);
        when(test.getAsBoolean())
                .thenAnswer(invocation -> {
                    verify(delegate).readLock();
                    verify(delegate, never()).unlock(anyLong());

                    return false;
                });

        assertThat(lock.writeIf(test,
                                testPassedOperation,
                                testFailedOperation,
                                testFailedLockContext))
                .isEqualTo(failedText);

        verify(testPassedOperation, never()).get();
        verify(testFailedOperation).get();

        verify(delegate).unlock(anyLong());
        verify(delegate, never()).tryConvertToWriteLock(anyLong());

    }

    public void writeIfTestFailedRunsOperationInLock() throws InterruptedException {

        testWriteIfTestFails(WrappedStampedLock.TestFailedLockContext.IN_PESSIMISTIC_READ_LOCK);
    }

    public void writeIfTestFailedRunsOperationOutsideLock() throws InterruptedException {
        testWriteIfTestFails(WrappedStampedLock.TestFailedLockContext.NO_LOCK);
    }

    public void writeIfTestPassedHandlesConversionSuccessful() throws InterruptedException {
        final long readLockStamp = 123L;
        final long convertStamp = 345L; // non-0 means that the conversion passed

        final StampedLock delegate = getDelegate(readLockStamp,
                                                 ANY_STAMP,
                                                 ANY_IS_VALID_STAMP,
                                                 ANY_STAMP,
                                                 convertStamp);

        final WrappedStampedLock lock = createSimpleWrappedStampedLock(delegate);

        final String passedText = "passed";
        final Supplier<String> testPassedOperation = getWriteIfOperation(passedText);
        final Supplier<String> testFailedOperation = getWriteIfOperation("failed");

        assertThat(lock.writeIf(() -> true,
                                testPassedOperation,
                                testFailedOperation,
                                WrappedStampedLock.TestFailedLockContext.NO_LOCK))
                .isEqualTo(passedText);

        verify(testPassedOperation).get();
        verify(testFailedOperation, never()).get();

        verify(delegate).readLock();
        verify(delegate, never()).unlockRead(anyLong());

        verify(delegate).tryConvertToWriteLock(readLockStamp);
        verify(delegate).unlock(convertStamp);

        verify(delegate, never()).writeLock();

    }

    public void writeIfTestPassedHandlesConversionFailure() throws InterruptedException {
        final long readLockStamp = 123L;
        final long writeStamp = 456L;
        final long firstConvertStamp = 0L;       // 0 means that the conversion failed
        final long secondConvertStamp = 789L;    // non-0 means that the conversion passed


        final StampedLock delegate = getDelegate(readLockStamp,
                                                 ANY_STAMP,
                                                 ANY_IS_VALID_STAMP,
                                                 writeStamp,
                                                 firstConvertStamp,
                                                 secondConvertStamp);

        final WrappedStampedLock lock = createSimpleWrappedStampedLock(delegate);

        final String passedText = "passed";
        final Supplier<String> testPassedOperation = getWriteIfOperation(passedText);
        final Supplier<String> testFailedOperation = getWriteIfOperation("failed");

        assertThat(lock.writeIf(() -> true,
                                testPassedOperation,
                                testFailedOperation,
                                WrappedStampedLock.TestFailedLockContext.NO_LOCK))
                .isEqualTo(passedText);

        verify(testPassedOperation).get();
        verify(testFailedOperation, never()).get();

        verify(delegate).readLock();
        verify(delegate).unlockRead(readLockStamp);

        verify(delegate).tryConvertToWriteLock(readLockStamp);
        verify(delegate, never()).unlock(writeStamp);

        verify(delegate, never()).unlock(firstConvertStamp);
        verify(delegate).unlock(secondConvertStamp);

    }

    public void writeIfOkIfWithinTimeout() throws InterruptedException {

        final long readLockStamp = 123L;
        final long convertStamp = 345L; // non-0 means that the conversion passed

        final StampedLock delegate = getDelegate(readLockStamp,
                                                 ANY_STAMP,
                                                 ANY_IS_VALID_STAMP,
                                                 ANY_STAMP,
                                                 convertStamp);

        final TimeTick startTimeTick = TimeTicks.explicitTimeTick(1L);
        final TimeTick endTimeTick = startTimeTick.plus(Duration.ofSeconds(12L));
        final WrappedStampedLock lock = new WrappedStampedLock(delegate,
                                                               WrappedReadWriteLock::new,
                                                               WrappedLock::new,
                                                               getCurrentTimeTickSupplier(ImmutableList.of(startTimeTick,
                                                                                                           endTimeTick)));

        final String passedText = "passed";
        final Supplier<String> testPassedOperation = getWriteIfOperation(passedText);
        final Supplier<String> testFailedOperation = getWriteIfOperation("failed");

        final Duration timeout = Duration.ofSeconds(20L);

        assertThat(lock.writeIf(() -> true,
                                testPassedOperation,
                                testFailedOperation,
                                WrappedStampedLock.TestFailedLockContext.NO_LOCK,
                                timeout))
                .isEqualTo(passedText);

        verify(testPassedOperation).get();

        // make sure we actually hit the right code
        verify(delegate).tryReadLock(timeout.minus(endTimeTick.durationSince(startTimeTick)).toNanos(),
                                     TimeUnit.NANOSECONDS);
    }

    public void writeIfThrowsIfTimeout() throws InterruptedException {

        final long readLockStamp = 0L;  // 0 means that it couldn't acquire

        final StampedLock delegate = getDelegate(readLockStamp,
                                                 ANY_STAMP,
                                                 ANY_IS_VALID_STAMP,
                                                 ANY_STAMP,
                                                 ANY_STAMP);

        final TimeTick startTimeTick = TimeTicks.explicitTimeTick(1L);
        final TimeTick endTimeTick = startTimeTick.plus(Duration.ofSeconds(12L));
        final WrappedStampedLock lock = new WrappedStampedLock(delegate,
                                                               WrappedReadWriteLock::new,
                                                               WrappedLock::new,
                                                               getCurrentTimeTickSupplier(ImmutableList.of(startTimeTick,
                                                                                                           endTimeTick)));

        final String passedText = "passed";
        final Supplier<String> testPassedOperation = getWriteIfOperation(passedText);
        final Supplier<String> testFailedOperation = getWriteIfOperation("failed");

        final Duration timeout = Duration.ofSeconds(5L);

        assertThatThrownBy(() -> lock.writeIf(() -> true,
                                              testPassedOperation,
                                              testFailedOperation,
                                              WrappedStampedLock.TestFailedLockContext.NO_LOCK,
                                              timeout))
                .isInstanceOf(AcquireTimeoutException.class);

        // make sure we actually hit the right code
        verify(delegate).tryReadLock(timeout.minus(endTimeTick.durationSince(startTimeTick)).toNanos(),
                                     TimeUnit.NANOSECONDS);
    }

}
