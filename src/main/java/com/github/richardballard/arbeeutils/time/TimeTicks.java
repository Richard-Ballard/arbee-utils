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

package com.github.richardballard.arbeeutils.time;

import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

@Immutable
public enum TimeTicks {
  ;

  private static final @NotNull Supplier<TimeTick> SYSTEM_CURRENT_TIME_TICK_SUPPLIER =
      () -> new TimeTick(System.nanoTime());

  /**
   * Returns a {@link Supplier} that gives the current {@link TimeTick}.
   */
  public static @NotNull Supplier<TimeTick> systemCurrentTimeTickSupplier() {
    return SYSTEM_CURRENT_TIME_TICK_SUPPLIER;
  }

  /**
   * This method is intended for tests where a known value must be used to make things deterministic
   */
  public static @NotNull TimeTick explicitTimeTick(final long nanoTicks) {
    return new TimeTick(nanoTicks);
  }
}
