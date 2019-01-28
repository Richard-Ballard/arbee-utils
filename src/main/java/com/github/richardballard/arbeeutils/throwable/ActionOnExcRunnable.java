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

package com.github.richardballard.arbeeutils.throwable;


import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.function.Consumer;

/**
 * A simple wrapper around a delegate {@link Runnable} that performs some operation when when an exception is thrown
 * by the delegate's {@link Runnable#run()} method.
 */
@SuppressWarnings({"WeakerAccess", "StaticMethodOnlyUsedInOneClass"})
@ThreadSafe
public class ActionOnExcRunnable implements Runnable {
  private final @NotNull Consumer<? super RuntimeException> exceptionConsumer;

  private final @NotNull Runnable delegate;

  public ActionOnExcRunnable(final @NotNull Consumer<? super RuntimeException> exceptionConsumer,
                             final @NotNull Runnable delegate) {

    this.exceptionConsumer = exceptionConsumer;
    this.delegate = delegate;
  }

  @Override
  public void run() {
    try {
      delegate.run();
    }
    catch(final RuntimeException exc) {
      exceptionConsumer.accept(exc);
    }
  }

  /**
   * Returns an instance that, on exception, will log to the given {@code logger} and then carry on (i.e. will not
   * rethrow the exception).
   */
  public static @NotNull ActionOnExcRunnable logErrorAndSwallow(final @NotNull Logger logger,
                                                                final @NotNull Runnable delegate) {

    return new ActionOnExcRunnable(exc -> logger.error("Caught exc",
                                                       exc),
                                   delegate);
  }

  /**
   * Returns an instance that, on exception, will log to the given {@code logger} and then rethrow the exception.
   */
  public static @NotNull ActionOnExcRunnable logErrorAndRethrow(final @NotNull Logger logger,
                                                                final @NotNull Runnable delegate) {

    return new ActionOnExcRunnable(
        exc -> {
          logger.error("Caught exc",
                       exc);

          //noinspection ProhibitedExceptionThrown
          throw exc;
        },
        delegate);
  }
}
