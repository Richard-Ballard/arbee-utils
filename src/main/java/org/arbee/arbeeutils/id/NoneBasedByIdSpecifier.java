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

package org.arbee.arbeeutils.id;

import com.google.common.collect.ImmutableSet;
import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * This implementation of {@link ByIdSpecifier} defaults to excluding everything, but may have inclusions.
 */
@Immutable
class NoneBasedByIdSpecifier<T> implements ByIdSpecifier<T> {

    @NotNull
    private final ImmutableSet<T> inclusions;

    public NoneBasedByIdSpecifier(@NotNull final Iterable<? extends T> inclusions) {
        assert inclusions != null;

        this.inclusions = ImmutableSet.copyOf(inclusions);
    }

    @Override
    public boolean test(@NotNull final T value) {

        assert value != null;

        return inclusions.contains(value);
    }

    @SuppressWarnings("OptionalContainsCollection")
    @NotNull
    @Override
    public Optional<ImmutableSet<T>> getIncludeIds() {
        return Optional.of(inclusions);
    }

    /**
     * This instance has no record of exclude ids - return empty to indicate this is not applicable.
     */
    @SuppressWarnings("OptionalContainsCollection")
    @NotNull
    @Override
    public Optional<ImmutableSet<T>> getExcludeIds() {
        return Optional.empty();
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }

        final NoneBasedByIdSpecifier<?> that = (NoneBasedByIdSpecifier<?>) o;

        return inclusions.equals(that.inclusions);

    }

    @Override
    public int hashCode() {
        return inclusions.hashCode();
    }

    @Override
    public String toString() {
        return "NoneBasedByIdSpecifier{" +
               "inclusions=" + inclusions +
               '}';
    }

}
