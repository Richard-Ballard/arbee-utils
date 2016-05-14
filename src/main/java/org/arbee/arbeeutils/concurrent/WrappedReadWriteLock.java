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

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;

/**
 * This class is a simple wrapper around an instance of {@link ReadWriteLock} that allows you to call the more common
 * methods in a functional manner.
 */
public class WrappedReadWriteLock {

    @NotNull
    private final ReadWriteLock delegate;

    @NotNull
    private final Function<? super Lock, ? extends WrappedLock> wrappedLockFromLockFunction;

    WrappedReadWriteLock(@NotNull final ReadWriteLock delegate,
                         @NotNull final Function<? super Lock, ? extends WrappedLock> wrappedLockFromLockFunction) {
        assert delegate != null;
        assert wrappedLockFromLockFunction != null;

        this.delegate = delegate;
        this.wrappedLockFromLockFunction = wrappedLockFromLockFunction;
    }

    public WrappedReadWriteLock(@NotNull final ReadWriteLock delegate) {
        this(delegate,
             WrappedLock::new);

        assert delegate != null;
    }

    @NotNull
    public WrappedLock readLock() {
        return Objects.requireNonNull(wrappedLockFromLockFunction.apply(delegate.readLock()));
    }

    @NotNull
    public WrappedLock writeLock() {
        return Objects.requireNonNull(wrappedLockFromLockFunction.apply(delegate.writeLock()));
    }
}
