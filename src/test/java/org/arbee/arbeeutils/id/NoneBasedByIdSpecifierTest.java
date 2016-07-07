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

package org.arbee.arbeeutils.id;

import com.google.common.collect.ImmutableSet;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Test
public class NoneBasedByIdSpecifierTest {

    public void testIncludesInclusionsOnly() {
        final NoneBasedByIdSpecifier<String> specifier = new NoneBasedByIdSpecifier<>(ImmutableSet.of("cat",
                                                                                                      "rat"));

        assertThat(specifier.test("ant"))
                .isFalse();

        assertThat(specifier.test("cat"))
                .isTrue();

        assertThat(specifier.test("bee"))
                .isFalse();

        assertThat(specifier.test("rat"))
                .isTrue();
    }

    public void getIncludeIdsHasInclusions() {
        final NoneBasedByIdSpecifier<String> specifier = new NoneBasedByIdSpecifier<>(ImmutableSet.of("cat",
                                                                                                      "rat"));

        //noinspection OptionalGetWithoutIsPresent
        assertThat(specifier.getIncludeIds().get())
                .containsOnly("cat",
                              "rat");
    }

    public void getExcludeIdsIsEmpty() {

        final NoneBasedByIdSpecifier<String> specifier = new NoneBasedByIdSpecifier<>(ImmutableSet.of("cat",
                                                                                                      "rat"));

        assertThat(specifier.getExcludeIds())
                .isEmpty();
    }
}
