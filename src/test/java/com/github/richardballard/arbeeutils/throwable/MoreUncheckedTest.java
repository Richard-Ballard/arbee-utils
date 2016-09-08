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

import com.github.richardballard.arbeetestutils.test.MoreMockUtils;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@Test
public class MoreUncheckedTest {

    private static final String ANY_VALUE = "anyValue";


    public void callReturnsWhereNoException() throws Exception {
        final String value = "value";
        final Callable<String> callable = MoreMockUtils.mockCallableSingleAnswer(value);

        final Consumer<Throwable> handler = MoreMockUtils.mockConsumer();

        assertThat(MoreUnchecked.call(callable,
                                      handler))
                .isEqualTo(value);

        // make sure it was only called once
        verify(callable).call();

        verify(handler, never()).accept(any(Throwable.class));
    }

    public void callCallsHandlerWhenException() throws Exception {
        final Callable<String> callable = MoreMockUtils.mockCallableSingleAnswer(ANY_VALUE);

        final IOException originExc = new IOException("test");
        when(callable.call())
                .thenThrow(originExc);

        final Consumer<Throwable> handler = MoreMockUtils.mockConsumer();

        final RuntimeException subsequentExc = new RuntimeException("sub");
        doThrow(subsequentExc)
                .when(handler)
                .accept(any(Throwable.class));

        assertThatThrownBy(() -> MoreUnchecked.call(callable,
                                                    handler))
                .isEqualTo(subsequentExc);

        verify(handler).accept(originExc);
    }

    public void callsThrowsIfHandlerDoesntThrow() throws Exception {
        final Callable<String> callable = MoreMockUtils.mockCallableSingleAnswer(ANY_VALUE);

        final IOException originExc = new IOException("test");
        when(callable.call())
                .thenThrow(originExc);

        assertThatThrownBy(() -> MoreUnchecked.call(callable,
                                                    MoreMockUtils.mockConsumer()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Exception handler must throw a RuntimeException");
    }
}
