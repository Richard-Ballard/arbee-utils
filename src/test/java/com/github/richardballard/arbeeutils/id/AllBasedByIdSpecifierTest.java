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
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Test
public class AllBasedByIdSpecifierTest {

  public void testExcludesExclusionsOnly() {
    final AllBasedByIdSpecifier<String> specifier = new AllBasedByIdSpecifier<>(ImmutableSet.of("cat",
                                                                                                "rat"));

    assertThat(specifier.test("ant"))
        .isTrue();

    assertThat(specifier.test("cat"))
        .isFalse();

    assertThat(specifier.test("bee"))
        .isTrue();

    assertThat(specifier.test("rat"))
        .isFalse();
  }

  public void getIncludeIdsIsEmpty() {
    final AllBasedByIdSpecifier<String> specifier = new AllBasedByIdSpecifier<>(ImmutableSet.of("cat",
                                                                                                "rat"));

    assertThat(specifier.getIncludeIds())
        .isEmpty();
  }

  public void getExcludeIdsHasExclusions() {
    final AllBasedByIdSpecifier<String> specifier = new AllBasedByIdSpecifier<>(ImmutableSet.of("cat",
                                                                                                "rat"));

    //noinspection OptionalGetWithoutIsPresent
    assertThat(specifier.getExcludeIds().get())
        .containsOnly("cat",
                      "rat");
  }
}
