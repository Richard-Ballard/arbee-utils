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

package org.arbee.arbeeutils.misc;


import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.function.Consumer;

/**
 * A simple wrapper around a delegate {@link Runnable} that performs some operation when when an exception is thrown
 * by the delegate's {@link Runnable#run()} method.
 */
@ThreadSafe
public class ActionOnExcRunnable implements Runnable {
    @NotNull
    private final Consumer<? super RuntimeException> exceptionConsumer;

    @NotNull
    private final Runnable delegate;

    public ActionOnExcRunnable(@NotNull final Consumer<? super RuntimeException> exceptionConsumer,
                               @NotNull final Runnable delegate) {
        assert exceptionConsumer != null;
        assert delegate != null;

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
    @NotNull
    public static ActionOnExcRunnable logErrorAndSwallow(@NotNull final Logger logger,
                                                         @NotNull final Runnable delegate) {
        assert logger != null;
        assert delegate != null;

        return new ActionOnExcRunnable(exc -> logger.error("Caught exc",
                                                           exc),
                                       delegate);
    }

    /**
     * Returns an instance that, on exception, will log to the given {@code logger} and then rethrow the exception.
     */
    @NotNull
    public static ActionOnExcRunnable logErrorAndRethrow(@NotNull final Logger logger,
                                                         @NotNull final Runnable delegate) {
        assert logger != null;
        assert delegate != null;

        return new ActionOnExcRunnable(exc -> {
            logger.error("Caught exc",
                         exc);

            throw exc;
        },
                                       delegate);
    }
}
