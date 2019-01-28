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

import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Instances of this class have a supplier of values and hold the 'last known value' from that supplier.  They are
 * also constructed with a consumer that is notified when a change in value occurs.
 */
@SuppressWarnings("unused")
@ThreadSafe
public class OnChangeNotifier<T> {

    private final @NotNull ChangeChecker<T> changeChecker;

    private final @NotNull Consumer<? super ChangedValue<T>> changedValueConsumer;

    private OnChangeNotifier(final @NotNull ChangeChecker<T> changeChecker,
                             final @NotNull Consumer<? super ChangedValue<T>> changedValueConsumer) {

        this.changeChecker = changeChecker;
        this.changedValueConsumer = changedValueConsumer;
    }

    public OnChangeNotifier(final @NotNull Supplier<Optional<T>> valueSupplier,
                            final @NotNull Consumer<? super ChangedValue<T>> changedValueConsumer) {
        this(new ChangeChecker<>(valueSupplier),
             changedValueConsumer);
    }

    public void checkForChange() {
        changeChecker.checkForChange().ifPresent(changedValueConsumer);
    }

}
