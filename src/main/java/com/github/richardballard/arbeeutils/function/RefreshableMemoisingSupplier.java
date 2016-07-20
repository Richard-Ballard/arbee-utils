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

package com.github.richardballard.arbeeutils.function;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;

/**
 * This class is an instance of {@link Supplier} that memoises the element.  That is to say, it has a delegate from
 * which it initially loads the element, and from this point on the {@link #get()} method returns that stored element.
 * It has the {@link #invalidate()} method that forces a reload from the delegate on the next call to {@link #get()}.
 */
@ThreadSafe
public class RefreshableMemoisingSupplier<T> implements Supplier<T> {

    @NotNull
    private final Supplier<? extends T> delegate;

    @NotNull
    private final LongAdder counter;

    @NotNull
    private volatile Optional<LoadedElement<T>> loadedElement;

    public RefreshableMemoisingSupplier(@NotNull final Supplier<? extends T> delegate) {
        assert delegate != null;

        this.delegate = delegate;

        this.counter = new LongAdder();
        this.loadedElement = Optional.empty();
    }

    /**
     * Returns the memoised element, loading it if necessary.
     */
    @Override
    public T get() {

        // Take a snapshot of the counter.  This way if another thread increments the counter while we are loading from
        // the delegate then the next time this method is called the counter will not match and so the memoised element
        // will not be used - RMB 2016/06/17
        final long counterSnapshot = counter.sum();

        // if the counter is still the same as when the element was loaded
        final Optional<T> optionalElement = loadedElement.filter(le -> le.getLoadedOnCount() == counterSnapshot)
                                                         .map(LoadedElement::getElement);

        final T element;
        if(optionalElement.isPresent()) {
            element = optionalElement.get();
        }
        else {
            // load from the delegate
            element = delegate.get();

            loadedElement = Optional.of(new LoadedElement<>(element,
                                                            counterSnapshot));
        }

        return element;
    }

    /**
     * Invalidates any element that is memoised so that the next call to {@link #get()} will pull the element from
     * the delegate.
     */
    public void invalidate() {
        counter.increment();
    }

    /**
     * Convenience method that calls {@link #invalidate()} and then returns the result of {@link #get()}
     */
    public T refresh() {
        invalidate();
        return get();
    }

    @Immutable
    private static class LoadedElement<T> {
        @Nullable
        private final T element;

        private final long loadedOnCount;

        public LoadedElement(@Nullable final T element,
                             final long loadedOnCount) {

            this.element = element;
            this.loadedOnCount = loadedOnCount;
        }

        @Nullable
        public T getElement() {
            return element;
        }

        public long getLoadedOnCount() {
            return loadedOnCount;
        }
    }
}
