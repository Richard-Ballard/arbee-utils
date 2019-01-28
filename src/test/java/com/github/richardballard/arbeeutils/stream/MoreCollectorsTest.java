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

package com.github.richardballard.arbeeutils.stream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;

import static org.assertj.core.api.Assertions.assertThat;

@Test
public class MoreCollectorsTest {

  @DataProvider(name = "testData")
  public @NotNull TestData[][] getTestData() {
    final Function<Integer, Integer> keyMapper = UnaryOperator.identity();
    final Function<Integer, String> valueMapper = k -> "v" + k;

    return new TestData[][] {
        // ImmutableList
        { new TestData(MoreCollectors.toImmutableList(),
                       ImmutableList.of(),
                       ImmutableList.class,
                       "[]") },

        { new TestData(MoreCollectors.toImmutableList(),
                       ImmutableList.of(1, 2, 3),
                       ImmutableList.class,
                       "[1, 2, 3]") },

        { new TestData(MoreCollectors.toImmutableList(),
                       ImmutableList.of(3, 2, 1),
                       ImmutableList.class,
                       "[3, 2, 1]") },

        // ImmutableSet
        { new TestData(MoreCollectors.toImmutableSet(),
                       ImmutableList.of(),
                       ImmutableSet.class,
                       "[]") },

        { new TestData(MoreCollectors.toImmutableSet(),
                       ImmutableList.of(1, 2, 3),
                       ImmutableSet.class,
                       "[1, 2, 3]") },

        { new TestData(MoreCollectors.toImmutableSet(),
                       ImmutableList.of(3, 2, 1),
                       ImmutableSet.class,
                       "[3, 2, 1]") },

        // ImmutableMap
        { new TestData(MoreCollectors.toImmutableMap(keyMapper,
                                                     valueMapper),
                       ImmutableList.of(),
                       ImmutableMap.class,
                       "{}") },

        { new TestData(MoreCollectors.toImmutableMap(keyMapper,
                                                     valueMapper),
                       ImmutableList.of(1, 2, 3),
                       ImmutableMap.class,
                       "{1=v1, 2=v2, 3=v3}") },

        { new TestData(MoreCollectors.toImmutableMap(keyMapper,
                                                     valueMapper),
                       ImmutableList.of(3, 2, 1),
                       ImmutableMap.class,
                       "{3=v3, 2=v2, 1=v1}") },

        };
  }

  @Test(dataProvider = "testData")
  public void serialStreamReturnsExpected(final @NotNull TestData data) {

    final Object result = data.getInputValues()
                              .stream()
                              .collect(data.getCollector());

    assertThat(result)
        .isInstanceOf(data.getOutputClass());

    assertThat(result.toString())
        .isEqualTo(data.getOutputToString());
  }

  @Test(dataProvider = "testData")
  public void parallelStreamReturnsExpected(final @NotNull TestData data) {

    final Object result = data.getInputValues()
                              .parallelStream()
                              .collect(data.getCollector());

    assertThat(result)
        .isInstanceOf(data.getOutputClass());

    assertThat(result.toString())
        .isEqualTo(data.getOutputToString());
  }


  @SuppressWarnings("WeakerAccess")
  @Immutable
  public static class TestData {

    private final @NotNull Collector<Integer, ?, ?> collector;

    private final @NotNull ImmutableList<Integer> inputValues;

    private final @NotNull Class<?> outputClass;

    private final @NotNull String outputToString;

    public TestData(final @NotNull Collector<Integer, ?, ?> collector,
                    final @NotNull ImmutableList<Integer> inputValues,
                    final @NotNull Class<?> outputClass,
                    final @NotNull String outputToString) {

      this.collector = collector;
      this.inputValues = inputValues;
      this.outputClass = outputClass;
      this.outputToString = outputToString;
    }

    public @NotNull Collector<Integer, ?, ?> getCollector() {
      return collector;
    }

    public @NotNull ImmutableList<Integer> getInputValues() {
      return inputValues;
    }

    public @NotNull Class<?> getOutputClass() {
      return outputClass;
    }

    public @NotNull String getOutputToString() {
      return outputToString;
    }

    @Override
    public String toString() {
      return "TestData{" +
             "collector=" + collector +
             ", inputValues=" + inputValues +
             ", outputClass=" + outputClass +
             ", outputToString='" + outputToString + '\'' +
             '}';
    }
  }
}
