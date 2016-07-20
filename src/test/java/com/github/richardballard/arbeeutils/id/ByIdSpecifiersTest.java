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

package com.github.richardballard.arbeeutils.id;

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Test
public class ByIdSpecifiersTest {

    private void assertAllBased(final @NotNull ByIdSpecifier<String> specifier,
                                @NotNull final ImmutableSet<String> exclusions) {
        assert specifier != null;
        assert exclusions != null;

        assertThat(specifier)
                .isInstanceOf(AllBasedByIdSpecifier.class);

        assertThat(specifier.getExcludeIds())
                .contains(exclusions);
    }

    private void assertNoneBased(@NotNull final ByIdSpecifier<String> specifier,
                                 @NotNull final ImmutableSet<String> inclusions) {
        assert specifier != null;
        assert inclusions != null;

        assertThat(specifier)
                .isInstanceOf(NoneBasedByIdSpecifier.class);

        assertThat(specifier.getIncludeIds())
                .contains(inclusions);
    }

    public void excludeNoneReturnsExpected() {
        assertAllBased(ByIdSpecifiers.excludeNone(),
                       ImmutableSet.of());
    }

    public void excludeAllReturnsExpected() {
        assertNoneBased(ByIdSpecifiers.excludeAll(),
                        ImmutableSet.of());
    }

    public void excludeOnlyMultipleReturnsExpected() {
        final ImmutableSet<String> exclusions = ImmutableSet.of("cat", "rat");

        assertAllBased(ByIdSpecifiers.excludeOnly(exclusions),
                       exclusions);
    }

    public void excludeOnlySingleReturnsExpected() {
        final String exclusion = "cat";

        assertAllBased(ByIdSpecifiers.excludeOnly(exclusion),
                       ImmutableSet.of(exclusion));
    }


    public void includeNoneReturnsExpected() {
        assertNoneBased(ByIdSpecifiers.includeNone(),
                       ImmutableSet.of());
    }

    public void includeAllReturnsExpected() {
        assertAllBased(ByIdSpecifiers.includeAll(),
                       ImmutableSet.of());
    }

    public void includeOnlyMultipleReturnsExpected() {
        final ImmutableSet<String> inclusions = ImmutableSet.of("cat", "rat");

        assertNoneBased(ByIdSpecifiers.includeOnly(inclusions),
                        inclusions);
    }

    public void includeOnlySingleReturnsExpected() {
        final String inclusion = "cat";

        assertNoneBased(ByIdSpecifiers.includeOnly(inclusion),
                        ImmutableSet.of(inclusion));
    }
}
