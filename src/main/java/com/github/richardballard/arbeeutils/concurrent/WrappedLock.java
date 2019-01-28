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

import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

/**
 * This class is a simple wrapper around an instance of {@link Lock} that allows you to call the lock methods in a
 * functional type manner.
 */
@SuppressWarnings({"WeakerAccess", "UnusedReturnValue", "unused"})
@ThreadSafe
public class WrappedLock {
  private final @NotNull Lock delegate;

  public WrappedLock(final @NotNull Lock delegate) {

    this.delegate = delegate;
  }

  private <T> T inLock(final @NotNull Runnable acquireLockRunnable,
                       final @NotNull Supplier<T> operation) {

    acquireLockRunnable.run();
    try {
      return operation.get();
    }
    finally {
      delegate.unlock();
    }
  }

  public <T> T inLock(final @NotNull Supplier<T> operation) {

    return inLock(delegate::lock,
                  operation);
  }

  public <T> T inLock(final @NotNull Supplier<T> operation,
                      final @NotNull Duration acquireTimeout) throws AcquireTimeoutException {

    return inLock(() -> {
                    if(!MoreUninterruptibles.tryLockUninterruptibly(delegate,
                                                                    acquireTimeout)) {
                      throw new AcquireTimeoutException();
                    }
                  },
                  operation);
  }

  public void inLock(final @NotNull Runnable operation) {

    inLock(() -> {
      operation.run();

      //noinspection ReturnOfNull
      return null;
    });
  }

  public void inLock(final @NotNull Runnable operation,
                     final @NotNull Duration acquireTimeout) throws AcquireTimeoutException {

    inLock(() -> {
             operation.run();

             //noinspection ReturnOfNull
             return null;
           },
           acquireTimeout);
  }
}
