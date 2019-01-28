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

package com.github.richardballard.arbeeutils.concurrent;

import net.jcip.annotations.ThreadSafe;
import com.github.richardballard.arbeeutils.time.TimeTick;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.locks.StampedLock;
import java.util.function.Supplier;

@SuppressWarnings("unused")
@ThreadSafe
public class WrappedStampedLockFactory {
  private final @NotNull Supplier<? extends TimeTick> currentTimeTickSupplier;

  public WrappedStampedLockFactory(final @NotNull Supplier<? extends TimeTick> currentTimeTickSupplier) {

    this.currentTimeTickSupplier = currentTimeTickSupplier;
  }

  public @NotNull WrappedStampedLock get(final @NotNull StampedLock delegate) {

    return new WrappedStampedLock(delegate,
                                  WrappedReadWriteLock::new,
                                  WrappedLock::new,
                                  currentTimeTickSupplier);
  }

  public @NotNull WrappedStampedLock get() {
    return get(new StampedLock());
  }

}
