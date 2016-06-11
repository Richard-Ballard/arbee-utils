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

package org.arbee.arbeeutils.test;

import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ThreadSafe
public enum MockUtils {
    ;

    /**
     * Returns a mock function that always returns {@code value}.
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public static <K, V> Function<K, V> mockFunctionSingleAnswer(@NotNull final Class<? extends K> keyClass,
                                                                 @Nullable final V value) {
        assert keyClass != null;

        final Function<K, V> function = mock(Function.class);

        when(function.apply(any(keyClass)))
                .thenReturn(value);

        return function;
    }
}