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

import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@Test
public class WrappedLockTest {


    @NotNull
    private Lock getDelegate() {
        return mock(Lock.class);
    }

    public void inLockSupplierLocks() {
        final Lock delegate = getDelegate();

        final WrappedLock lock = new WrappedLock(delegate);

        final String value = "value";

        final String actual = lock.inLock(() -> {
            verify(delegate).lock();

            return value;
        });

        assertThat(actual)
                .isEqualTo(value);
    }

    public void inLockSupplierUnlocks() {
        final Lock delegate = getDelegate();

        final WrappedLock lock = new WrappedLock(delegate);

        final String value = "value";
        final String actual = lock.inLock(() -> value);

        verify(delegate).unlock();

        assertThat(actual)
                .isEqualTo(value);
    }

    public void inLockSupplierUnlocksWhenException() {
        final Lock delegate = getDelegate();

        final RuntimeException exc = new RuntimeException("test");

        final Supplier<String> supplier = () -> {
            throw exc;
        };

        assertThatThrownBy(() -> new WrappedLock(delegate).inLock(supplier))
                .isEqualTo(exc);

        verify(delegate).unlock();
    }

    @SuppressWarnings("unchecked")
    @NotNull
    private Supplier<String> getSupplier() {
        return mock(Supplier.class);
    }

    public void inLockSupplierTimeoutDoesntCallOperationOrUnlock() throws InterruptedException {
        final Duration timeout = Duration.ofDays(123L);

        final Lock delegate = getDelegate();
        when(delegate.tryLock(timeout.toNanos(),
                              TimeUnit.NANOSECONDS))
                .thenReturn(false);


        final Supplier<String> supplier = getSupplier();
        assertThatThrownBy(() -> new WrappedLock(delegate).inLock(supplier,
                                                                  timeout))
                .isInstanceOf(AcquireTimeoutException.class);

        verify(supplier, never()).get();
        verify(delegate, never()).unlock();
    }
}
