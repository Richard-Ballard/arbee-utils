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

import java.util.Optional;

/**
 * This implementation of {@link ByIdSpecifier} defaults to including everything, but may have exclusions.
 */
@SuppressWarnings("WeakerAccess")
@Immutable
class AllBasedByIdSpecifier<T> implements ByIdSpecifier<T> {

  private final @NotNull ImmutableSet<T> exclusions;

  public AllBasedByIdSpecifier(final @NotNull Iterable<? extends T> exclusions) {

    this.exclusions = ImmutableSet.copyOf(exclusions);
  }

  @Override
  public boolean test(final @NotNull T value) {

    return !exclusions.contains(value);
  }

  /**
   * This instance has no record of include ids - return empty to indicate this is not applicable.
   */
  @Override
  @SuppressWarnings("OptionalContainsCollection")
  public @NotNull Optional<ImmutableSet<T>> getIncludeIds() {
    return Optional.empty();
  }

  @Override
  @SuppressWarnings("OptionalContainsCollection")
  public @NotNull Optional<ImmutableSet<T>> getExcludeIds() {
    return Optional.of(exclusions);
  }

  @Override
  public boolean equals(final Object o) {
    if(this == o) {
      return true;
    }
    if(o == null || getClass() != o.getClass()) {
      return false;
    }

    final AllBasedByIdSpecifier<?> that = (AllBasedByIdSpecifier<?>) o;

    return exclusions.equals(that.exclusions);

  }

  @Override
  public int hashCode() {
    return exclusions.hashCode();
  }

  @Override
  public String toString() {
    return "AllBasedByIdSpecifier{" +
           "exclusions=" + exclusions +
           '}';
  }

}
