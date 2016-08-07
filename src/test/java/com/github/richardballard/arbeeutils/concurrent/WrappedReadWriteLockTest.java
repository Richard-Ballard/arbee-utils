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

import com.github.richardballard.arbeeutils.test.MockUtils;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Test
public class WrappedReadWriteLockTest {

    private static final Lock ANY_LOCK = getLock();

    @NotNull
    private static Lock getLock() {
        return mock(Lock.class);
    }

    @NotNull
    private ReadWriteLock getDelegate(@NotNull final Lock readLock,
                                      @NotNull final Lock writeLock) {
        assert readLock != null;
        assert writeLock != null;

        final ReadWriteLock readWriteLock = mock(ReadWriteLock.class);

        when(readWriteLock.readLock())
                .thenReturn(readLock);

        when(readWriteLock.writeLock())
                .thenReturn(writeLock);

        return readWriteLock;
    }

    @NotNull
    private Function<Lock, WrappedLock> getWrappedLockFromLockFunction(@NotNull final WrappedLock wrappedLock) {

        return MockUtils.mockFunctionSingleAnswer(Lock.class,
                                                  wrappedLock);
    }

    @NotNull
    private WrappedLock getWrappedLock() {
        return mock(WrappedLock.class);
    }

    public void readLockWrapsDelegate() {

        final WrappedLock wrappedLock = getWrappedLock();

        final Function<Lock, WrappedLock> wrappedLockFromLockFunction = getWrappedLockFromLockFunction(wrappedLock);

        final Lock readLock = getLock();
        final WrappedReadWriteLock wrappedReadWriteLock = new WrappedReadWriteLock(getDelegate(readLock,
                                                                                               ANY_LOCK),
                                                                                   wrappedLockFromLockFunction);

        assertThat(wrappedReadWriteLock.readLock())
                .isEqualTo(wrappedLock);

        verify(wrappedLockFromLockFunction).apply(readLock);
    }

    public void writeLockWrapsDelegate() {

        final WrappedLock wrappedLock = getWrappedLock();

        final Function<Lock, WrappedLock> wrappedLockFromLockFunction = getWrappedLockFromLockFunction(wrappedLock);

        final Lock writeLock = getLock();
        final WrappedReadWriteLock wrappedReadWriteLock = new WrappedReadWriteLock(getDelegate(ANY_LOCK,
                                                                                               writeLock),
                                                                                   wrappedLockFromLockFunction);

        assertThat(wrappedReadWriteLock.writeLock())
                .isEqualTo(wrappedLock);

        verify(wrappedLockFromLockFunction).apply(writeLock);
    }
}