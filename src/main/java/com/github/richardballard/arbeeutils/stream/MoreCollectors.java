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

import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Some extra {@link Collector} implementations for things like guava lists.
 */
@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
@Immutable
public enum MoreCollectors {
  ;

  /**
   * Based on code from <a href="https://dzone.com/articles/java-8-collectors-guava">here</a>
   */
  public static @NotNull <T> Collector<T, ?, ImmutableList<T>> toImmutableList() {

    final Supplier<ImmutableList.Builder<T>> supplier = ImmutableList.Builder::new;

    final BiConsumer<ImmutableList.Builder<T>, T> accumulator = ImmutableList.Builder::add;

    final BinaryOperator<ImmutableList.Builder<T>> combiner = (l, r) -> l.addAll(r.build());

    final Function<ImmutableList.Builder<T>, ImmutableList<T>> finisher = ImmutableList.Builder::build;

    return Collector.of(supplier,
                        accumulator,
                        combiner,
                        finisher);
  }

  /**
   * Based on code from <a href="https://dzone.com/articles/java-8-collectors-guava">here</a>
   */
  public static @NotNull <T> Collector<T, ?, ImmutableSet<T>> toImmutableSet() {

    final Supplier<ImmutableSet.Builder<T>> supplier = ImmutableSet.Builder::new;

    final BiConsumer<ImmutableSet.Builder<T>, T> accumulator = ImmutableSet.Builder::add;

    final BinaryOperator<ImmutableSet.Builder<T>> combiner = (l, r) -> l.addAll(r.build());

    final Function<ImmutableSet.Builder<T>, ImmutableSet<T>> finisher = ImmutableSet.Builder::build;

    return Collector.of(supplier,
                        accumulator,
                        combiner,
                        finisher);
  }

  /**
   * Based on code from <a href="https://dzone.com/articles/java-8-collectors-guava">here</a>
   */
  public static @NotNull <T, K, V> Collector<T, ?, ImmutableMap<K, V>>
  toImmutableMap(final @NotNull Function<? super T, ? extends K> keyMapper,
                 final @NotNull Function<? super T, ? extends V> valueMapper) {

    final Supplier<ImmutableMap.Builder<K, V>> supplier = ImmutableMap.Builder::new;

    final BiConsumer<ImmutableMap.Builder<K, V>, T> accumulator = (b, t) -> b.put(keyMapper.apply(t),
                                                                                  valueMapper.apply(t));

    final BinaryOperator<ImmutableMap.Builder<K, V>> combiner = (l, r) -> l.putAll(r.build());

    final Function<ImmutableMap.Builder<K, V>, ImmutableMap<K, V>> finisher = ImmutableMap.Builder::build;

    return Collector.of(supplier,
                        accumulator,
                        combiner,
                        finisher);
  }
}
