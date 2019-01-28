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

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;

/**
 * This class is a simple wrapper around an instance of {@link ReadWriteLock} that allows you to call the more common
 * methods in a functional manner.
 */
@SuppressWarnings("WeakerAccess")
public class WrappedReadWriteLock {

  private final @NotNull ReadWriteLock delegate;

  private final @NotNull Function<? super Lock, ? extends WrappedLock> wrappedLockFromLockFunction;

  WrappedReadWriteLock(final @NotNull ReadWriteLock delegate,
                       final @NotNull Function<? super Lock, ? extends WrappedLock> wrappedLockFromLockFunction) {

    this.delegate = delegate;
    this.wrappedLockFromLockFunction = wrappedLockFromLockFunction;
  }

  public WrappedReadWriteLock(final @NotNull ReadWriteLock delegate) {
    this(delegate,
         WrappedLock::new);
  }

  public @NotNull WrappedLock readLock() {
    return Objects.requireNonNull(wrappedLockFromLockFunction.apply(delegate.readLock()));
  }

  public @NotNull WrappedLock writeLock() {
    return Objects.requireNonNull(wrappedLockFromLockFunction.apply(delegate.writeLock()));
  }
}
