/*
 * (C) Copyright 2017 Richard Ballard.
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

public enum MoreThrowables {
    ;

    /**
     * If the given {@code throwable} is an instance of {@link RuntimeException} then it is returned, otherwise it is
     * wrapped in a {@link RuntimeException}.
     */
    @NotNull
    public static RuntimeException asRuntimeException(@NotNull final Throwable throwable) {
        assert throwable != null;

        final RuntimeException result;
        if(throwable instanceof RuntimeException) {
            result = (RuntimeException) throwable;
        }
        else {
            result = new RuntimeException(throwable);
        }

        return result;
    }
}
