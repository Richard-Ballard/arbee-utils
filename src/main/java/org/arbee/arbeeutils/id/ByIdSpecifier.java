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
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * This interface describes a specifier for ids.  The interface can be used both as a {@link Predicate} and to list the
 * inclusions or exclusions.  Instances are typically obtained by calling methods on {@link ByIdSpecifiers}.
 */
public interface ByIdSpecifier<T> extends Predicate<T> {

    /**
     * Returns any inclusions if this is applicable to the type.
     * <p/>
     * If this returns {@link Optional#empty()} then the instance is 'all based' - i.e. it matches all by default and
     * may have exclusions.
     * <p/>
     * If this returns a present optional then the instance is 'none based' - i.e. it matches none by default and may
     * have some inclusions.
     * <p/>
     * If this returns an empty set then it means 'exclude everything' (i.e. match nothing).
     *
     * @return empty if inclusions are not applicable, e.g. if this instance is one that includes everything with some exclusions.
     */
    @SuppressWarnings("OptionalContainsCollection")
    @NotNull
    Optional<ImmutableSet<T>> getIncludeIds();

    /**
     * Returns any exclusions if this is applicable to the type.
     * <p/>
     * If this returns {@link Optional#empty()} then the instance is 'none based' - i.e. it matches none by default and
     * may have inclusions.
     * <p/>
     * If this returns a present optional then the instance is 'all based' - i.e. it matches all by default and may
     * have some exclusions.
     * <p/>
     * If this returns an empty set then it means 'include everything'

     * @return empty if exclusions are not applicable, e.g. if this instance is one that excludes everything with some inclusions.
     */
    @SuppressWarnings("OptionalContainsCollection")
    @NotNull
    Optional<ImmutableSet<T>> getExcludeIds();
}
