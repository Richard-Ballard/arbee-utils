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

package com.github.richardballard.arbeeutils.misc;

import com.google.common.collect.ImmutableList;
import com.github.richardballard.arbeeutils.test.MockUtils;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;

import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

@Test
public class ChangeCheckerTest {

    private static final Optional<String> EMPTY_VALUE = Optional.empty();
    private static final Optional<String> A_VALUE = Optional.of("a");
    private static final Optional<String> B_VALUE = Optional.of("b");

    @NotNull
    private Supplier<Optional<String>> getValueSupplier(@NotNull final ImmutableList<Optional<String>> values) {
        return MockUtils.mockSupplierMultipleAnswers(values);
    }

    public void firstCallReturnsChange() {
        final ChangeChecker<String> checker = new ChangeChecker<>(getValueSupplier(ImmutableList.of(A_VALUE)));

        assertThat(checker.checkForChange())
                .contains(new ChangedValue<>(EMPTY_VALUE,
                                             A_VALUE));
    }

    public void noChangeDoesntReturnChange() {
        final ChangeChecker<String> checker = new ChangeChecker<>(getValueSupplier(ImmutableList.of(A_VALUE,
                                                                                                    A_VALUE)));

        assertThat(checker.checkForChange())
                .contains(new ChangedValue<>(EMPTY_VALUE,
                                             A_VALUE));

        assertThat(checker.checkForChange())
                .isEmpty();
    }

    public void variousChangesAreHandled() {
        final ChangeChecker<String> checker = new ChangeChecker<>(getValueSupplier(ImmutableList.of(EMPTY_VALUE,
                                                                                                    A_VALUE,
                                                                                                    A_VALUE,
                                                                                                    EMPTY_VALUE,
                                                                                                    B_VALUE,
                                                                                                    EMPTY_VALUE)));

        assertThat(checker.checkForChange())
                .isEmpty();

        assertThat(checker.checkForChange())
                .contains(new ChangedValue<>(EMPTY_VALUE,
                                             A_VALUE));

        assertThat(checker.checkForChange())
                .isEmpty();

        assertThat(checker.checkForChange())
                .contains(new ChangedValue<>(A_VALUE,
                                             EMPTY_VALUE));

        assertThat(checker.checkForChange())
                .contains(new ChangedValue<>(EMPTY_VALUE,
                                             B_VALUE));

        assertThat(checker.checkForChange())
                .contains(new ChangedValue<>(B_VALUE,
                                             EMPTY_VALUE));
    }
}
