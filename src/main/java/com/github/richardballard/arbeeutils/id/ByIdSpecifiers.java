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

package com.github.richardballard.arbeeutils.id;


import com.google.common.collect.ImmutableSet;
import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

/**
 * This class has static methods for obtaining the various flavours of {@link ByIdSpecifier}.
 */
@Immutable
public enum ByIdSpecifiers {
    ;

    private static final ByIdSpecifier<Object> EXCLUDE_NONE = new AllBasedByIdSpecifier<>(ImmutableSet.of());
    private static final ByIdSpecifier<Object> INCLUDE_NONE = new NoneBasedByIdSpecifier<>(ImmutableSet.of());


    /**
     * @return an instance that excludes nothing (i.e. includes everything)
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public static <T> ByIdSpecifier<T> excludeNone() {
        return (ByIdSpecifier<T>)EXCLUDE_NONE;
    }

    /**
     * @return an instance that excludes everything (i.e. includes nothing)
     */
    @NotNull
    public static <T> ByIdSpecifier<T> excludeAll() {
        return includeNone();
    }

    /**
     * @return an instance that excludes only the specified ids
     */
    @NotNull
    public static <T> ByIdSpecifier<T> excludeOnly(@NotNull final Iterable<? extends T> exclusions) {
        assert exclusions != null;

        return new AllBasedByIdSpecifier<T>(ImmutableSet.copyOf(exclusions));
    }

    /**
     * @return an instance that excludes only the specified id
     */
    @NotNull
    public static <T> ByIdSpecifier<T> excludeOnly(@NotNull final T exclusion) {
        assert exclusion != null;

        return new AllBasedByIdSpecifier<T>(ImmutableSet.of(exclusion));
    }

    /**
     * @return an instance that includes nothing (i.e. excludes everything)
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public static <T> ByIdSpecifier<T> includeNone() {
        return (ByIdSpecifier<T>)INCLUDE_NONE;
    }

    /**
     * @return an instance that includes everything (i.e. excludes nothing)
     */
    @NotNull
    public static <T> ByIdSpecifier<T> includeAll() {
        return excludeNone();
    }

    /**
     * @return an instance that includes only the specified ids
     */
    @NotNull
    public static <T> ByIdSpecifier<T> includeOnly(@NotNull final Iterable<? extends T> inclusions) {
        assert inclusions != null;

        return new NoneBasedByIdSpecifier<T>(ImmutableSet.copyOf(inclusions));
    }

    /**
     * @return an instance that includes only the specified id
     */
    @NotNull
    public static <T> ByIdSpecifier<T> includeOnly(@NotNull final T inclusion) {
        assert inclusion != null;

        return new NoneBasedByIdSpecifier<T>(ImmutableSet.of(inclusion));
    }
}
