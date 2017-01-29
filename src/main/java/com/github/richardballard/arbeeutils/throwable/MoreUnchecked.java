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

import com.google.common.base.Throwables;
import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;
import java.util.function.Consumer;


/**
 * Like {@link org.jooq.lambda.Unchecked} but the methods of this class actually make the call to the lambda
 * (rather than returning a construct that *will* make the call).
 */
@Immutable
public enum MoreUnchecked {
    ;

    /**
     * A {@link Consumer} that wraps any {@link Throwable} in a {@link RuntimeException}.
     */
    public static final Consumer<Throwable> PROPAGATE_AS_RUNTIME_EXCEPTION = thr -> {
        throw MoreThrowables.asRuntimeException(thr);
    };

    /**
     * Calls {@link Callable#call()}.  If this throws then the exception will be passed to {@code handler}
     */
    public static <T> T call(@NotNull final Callable<T> callable,
                             @NotNull final Consumer<? super Throwable> handler) {
        assert callable != null;
        assert handler != null;

        //noinspection OverlyBroadCatchBlock
        try {
            return callable.call();
        }
        catch (final Throwable e) {
            handler.accept(e);

            throw new IllegalStateException("Exception handler must throw a RuntimeException", e);
        }
    }

    /**
     * Same as {@link #call(Callable, Consumer)} but uses {@link #PROPAGATE_AS_RUNTIME_EXCEPTION} as the handler
     */
    public static <T> T call(@NotNull final Callable<T> callable) {
        assert callable != null;

        return call(callable,
                    PROPAGATE_AS_RUNTIME_EXCEPTION);
    }
}
