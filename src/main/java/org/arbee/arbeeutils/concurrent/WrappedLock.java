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

import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

/**
 * This class is a simple wrapper around an instance of {@link Lock} that allows you to call the lock methods in a
 * functional type manner.
 */
@ThreadSafe
public class WrappedLock {
    @NotNull
    private final Lock delegate;

    public WrappedLock(@NotNull final Lock delegate) {
        assert delegate != null;

        this.delegate = delegate;
    }

    private <T> T inLock(@NotNull final Runnable acquireLockRunnable,
                         @NotNull final Supplier<T> operation) {
        assert acquireLockRunnable != null;
        assert operation != null;

        acquireLockRunnable.run();
        try {
            return operation.get();
        }
        finally {
            delegate.unlock();
        }
    }

    public <T> T inLock(@NotNull final Supplier<T> operation) {
        assert operation != null;

        return inLock(delegate::lock,
                      operation);
    }

    public <T> T inLock(@NotNull final Supplier<T> operation,
                        @NotNull final Duration acquireTimeout) throws AcquireTimeoutException {
        assert operation != null;
        assert acquireTimeout != null;

        return inLock(() -> {
            if(!MoreUninterruptibles.tryLockUninterruptibly(delegate,
                                                            acquireTimeout)) {
                throw new AcquireTimeoutException();
            }
        },
                      operation);
    }

    public void inLock(@NotNull final Runnable operation) {
        assert operation != null;

        inLock(() -> {
            operation.run();

            return null;
        });
    }

    public void inLock(@NotNull final Runnable operation,
                       @NotNull final Duration acquireTimeout) throws AcquireTimeoutException {
        assert operation != null;
        assert acquireTimeout != null;

        inLock(() -> {
            operation.run();

            return null;
        },
               acquireTimeout);
    }
}
