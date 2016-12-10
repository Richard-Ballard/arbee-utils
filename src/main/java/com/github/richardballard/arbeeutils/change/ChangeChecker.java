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

package com.github.richardballard.arbeeutils.change;

import com.github.richardballard.arbeeutils.concurrent.WrappedLock;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Instances of this class have a supplier of values and hold the 'last known value' from that supplier.  When the
 * {@link #checkForChange()} method is called it may optionally return any change from the supplier.
 */
@ThreadSafe
public class ChangeChecker<T> {

    @NotNull
    private final Supplier<Optional<T>> valueSupplier;

    private final @NotNull WrappedLock lock;

    @NotNull
    private Optional<T> knownValue;

    public ChangeChecker(@NotNull final Supplier<Optional<T>> valueSupplier) {
        assert valueSupplier != null;

        this.valueSupplier = valueSupplier;

        this.lock = new WrappedLock(new ReentrantLock(false));
        this.knownValue = Optional.empty();
    }

    @NotNull
    public Optional<ChangedValue<T>> checkForChange() {
        return lock.inLock(() -> {
            Optional<ChangedValue<T>> result = Optional.empty();

            final Optional<T> mostRecentValue = valueSupplier.get();
            if(!knownValue.equals(mostRecentValue)) {

                // make sure we capture 'knownValue' before resetting it
                result = Optional.of(new ChangedValue<>(knownValue,
                                                        mostRecentValue));

                knownValue = mostRecentValue;
            }

            return result;
        });
    }

}
