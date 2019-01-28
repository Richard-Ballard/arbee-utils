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

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@Test
public class ActionOnExcRunnableTest {

  @SuppressWarnings("unchecked")
  private @NotNull Consumer<RuntimeException> getExceptionConsumer() {
    return mock(Consumer.class);
  }

  private @NotNull Runnable getDelegate(final @NotNull RuntimeException exc) {
    final Runnable runnable = mock(Runnable.class);

    doThrow(exc).when(runnable)
                .run();

    return runnable;
  }

  public void exceptionIsPassedToConsumer() {
    final RuntimeException exc = new RuntimeException("test");

    final Consumer<RuntimeException> exceptionConsumer = getExceptionConsumer();

    final ActionOnExcRunnable runnable = new ActionOnExcRunnable(exceptionConsumer,
                                                                 getDelegate(exc));

    runnable.run();

    verify(exceptionConsumer).accept(exc);
  }

  private @NotNull Logger getLogger() {
    return mock(Logger.class);
  }

  public void logAndSwallowLogsAndSwallows() {
    final Logger logger = getLogger();

    final RuntimeException exc = new RuntimeException("test");

    final ActionOnExcRunnable runnable = ActionOnExcRunnable.logErrorAndSwallow(logger,
                                                                                getDelegate(exc));

    runnable.run();

    verify(logger).error("Caught exc",
                         exc);

  }

  public void logAndRethrowLogsAndRethrows() {
    final Logger logger = getLogger();

    final RuntimeException exc = new RuntimeException("test");

    final ActionOnExcRunnable runnable = ActionOnExcRunnable.logErrorAndRethrow(logger,
                                                                                getDelegate(exc));

    assertThatThrownBy(runnable::run)
        .isEqualTo(exc);

    verify(logger).error("Caught exc",
                         exc);

  }
}
