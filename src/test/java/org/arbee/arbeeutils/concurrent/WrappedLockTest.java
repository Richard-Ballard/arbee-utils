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

import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;

import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@Test
public class WrappedLockTest {


    @NotNull
    private Lock getDelegate() {
        return mock(Lock.class);
    }

    public void inLockRunnableLocks() {
        final Lock delegate = getDelegate();

        final WrappedLock lock = new WrappedLock(delegate);

        lock.inLock(() -> verify(delegate).lock());
    }

    public void inLockRunnableUnlocks() {
        final Lock delegate = getDelegate();

        final WrappedLock lock = new WrappedLock(delegate);

        lock.inLock(() -> {});

        verify(delegate).unlock();
    }

    public void inLockRunnableUnlocksWhenException() {
        final Lock delegate = getDelegate();

        final RuntimeException exc = new RuntimeException("test");

        assertThatThrownBy(() -> new WrappedLock(delegate).inLock(() -> {
            throw exc;
        })).isEqualTo(exc);

        verify(delegate).unlock();
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
}
