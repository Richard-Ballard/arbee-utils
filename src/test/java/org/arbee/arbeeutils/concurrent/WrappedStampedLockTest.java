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

import org.arbee.arbeeutils.test.MockUtils;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@Test
public class WrappedStampedLockTest {

    private static final long ANY_STAMP = 999L;
    private static final boolean ANY_IS_VALID_STAMP = false;

    @NotNull
    private StampedLock getDelegate(final long readLockStamp,
                                    final long optimisticReadStamp,
                                    final boolean isValidStamp,
                                    final long writeStamp) {
        final StampedLock lock = mock(StampedLock.class);

        when(lock.readLock())
                .thenReturn(readLockStamp);

        when(lock.tryOptimisticRead())
                .thenReturn(optimisticReadStamp);

        when(lock.validate(anyLong()))
                .thenReturn(isValidStamp);

        when(lock.writeLock())
                .thenReturn(writeStamp);

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

    public void pessimisticReadUnlocksOnSuccessful() {
        final long stamp = 123L;
        final StampedLock delegate = getDelegate(stamp,
                                                 ANY_STAMP,
                                                 ANY_IS_VALID_STAMP,
                                                 ANY_STAMP);

        final WrappedStampedLock lock = new WrappedStampedLock(delegate);

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

    public void pessimisticReadUnlocksOnException() {
        final long stamp = 123L;
        final StampedLock delegate = getDelegate(stamp,
                                                 ANY_STAMP,
                                                 ANY_IS_VALID_STAMP,
                                                 ANY_STAMP);

        final WrappedStampedLock lock = new WrappedStampedLock(delegate);

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

    public void optimisticReadFallsBackToPessimistic() {
        final long readLockStamp = 123L;
        final long optimisticReadStamp = 234L;
        final StampedLock delegate = getDelegate(readLockStamp,
                                                 optimisticReadStamp,
                                                 false,
                                                 ANY_STAMP);

        final WrappedStampedLock lock = new WrappedStampedLock(delegate);

        final String operationResult = "result";

        assertThat(lock.optimisticRead(() -> operationResult))
                .isEqualTo(operationResult);

        verify(delegate, times(2)).tryOptimisticRead();
        verify(delegate).readLock();
    }

    public void optimisticReadReturnsIfValid() {
        final long optimisticReadStamp = 234L;
        final StampedLock delegate = getDelegate(ANY_STAMP,
                                                 optimisticReadStamp,
                                                 true,
                                                 ANY_STAMP);

        final WrappedStampedLock lock = new WrappedStampedLock(delegate);

        final String operationResult = "result";

        assertThat(lock.optimisticRead(() -> operationResult))
                .isEqualTo(operationResult);

        verify(delegate).tryOptimisticRead();
        verify(delegate, never()).readLock();
    }

    public void writeUnlocksOnSuccess() {
        final long writeStamp = 123L;

        final StampedLock delegate = getDelegate(ANY_STAMP,
                                                 ANY_STAMP,
                                                 ANY_IS_VALID_STAMP,
                                                 writeStamp);

        final WrappedStampedLock lock = new WrappedStampedLock(delegate);

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

    public void writeUnlocksOnException() {
        final long stamp = 123L;
        final StampedLock delegate = getDelegate(ANY_STAMP,
                                                 ANY_STAMP,
                                                 ANY_IS_VALID_STAMP,
                                                 stamp);

        final WrappedStampedLock lock = new WrappedStampedLock(delegate);

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
                                                               getWrappedLockFromLockFunction(getWrappedLock()));

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
                                                               wrappedLockFromLockFunction);

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
                                                               wrappedLockFromLockFunction);

        assertThat(lock.asWriteLock())
                .isEqualTo(wrappedLock);

        verify(delegate).asWriteLock();
        verify(wrappedLockFromLockFunction).apply(sourceLock);
    }

}
