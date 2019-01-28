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

import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@SuppressWarnings({"WeakerAccess", "unused"})
@Immutable
public class ChangedValue<T> {
  private final @NotNull Optional<T> previousValue;

  private final @NotNull Optional<T> newValue;

  public ChangedValue(final @NotNull Optional<T> previousValue,
                      final @NotNull Optional<T> newValue) {

    this.previousValue = previousValue;
    this.newValue = newValue;
  }

  public @NotNull Optional<T> getPreviousValue() {
    return previousValue;
  }

  public @NotNull Optional<T> getNewValue() {
    return newValue;
  }


  @Override
  public boolean equals(final Object o) {
    if(this == o) {
      return true;
    }
    if(o == null || getClass() != o.getClass()) {
      return false;
    }

    final ChangedValue<?> that = (ChangedValue<?>) o;

    if(!previousValue.equals(that.previousValue)) {
      return false;
    }
    return newValue.equals(that.newValue);

  }

  @Override
  public int hashCode() {
    int result = previousValue.hashCode();
    result = 31 * result + newValue.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "ChangedValue{" +
           "previousValue=" + previousValue +
           ", newValue=" + newValue +
           '}';
  }
}
