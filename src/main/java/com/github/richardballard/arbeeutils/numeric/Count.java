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

package com.github.richardballard.arbeeutils.numeric;

import com.github.richardballard.arbeecoretypes.numeric.BigNumber;
import com.google.common.base.Preconditions;
import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * This class represents a zero based count.  For any method that creates an instance (e.g.
 * {@link #valueOf(BigInteger, OnExceedBoundary)} there is an {@code onExceedBoundary} parameter which indicates what
 * should happen if the result would be negative (which doesn't make sense for a count).
 */
@SuppressWarnings({"WeakerAccess", "unused"})
@Immutable
public final class Count extends BigNumber {

  private static final long serialVersionUID = 8108782711353619422L;

  public static final Count ZERO = new Count(BigInteger.ZERO);
  public static final Count MIN_VALUE = ZERO;

  public enum OnExceedBoundary {
    /**
     * e.g. if the value is > MIN_VALUE then set to MIN_VALUE
     */
    USE_BOUNDARY_VALUE,

    /**
     * throw an exception
     */
    THROW
  }

  public static @NotNull Count valueOf(final @NotNull BigInteger value,
                                       final @NotNull OnExceedBoundary onExceedBoundary) {

    final Count result;

    final int comparisonToZero = value.compareTo(BigInteger.ZERO);
    if(comparisonToZero == 0) {
      result = ZERO;
    }
    else if(comparisonToZero > 0) {
      result = new Count(value);
    }
    else {
      if(onExceedBoundary == OnExceedBoundary.THROW) {
        throw new IllegalArgumentException("value must be >= 0, not " + value);
      }
      else if(onExceedBoundary == OnExceedBoundary.USE_BOUNDARY_VALUE) {
        result = ZERO;
      }
      else {
        throw new IllegalStateException("unknown onExceedBoundary - " + onExceedBoundary);
      }
    }

    return result;
  }

  private static @NotNull BigInteger asBigInteger(final @NotNull BigNumber value) {

    return value.bigIntegerValue();
  }

  private static @NotNull BigInteger asBigInteger(final long value) {

    return BigInteger.valueOf(value);
  }

  private static @NotNull BigInteger asBigInteger(final @NotNull Number value) {

    return asBigInteger(value.longValue());
  }


  public static @NotNull Count valueOf(final @NotNull BigNumber value,
                                       final @NotNull OnExceedBoundary onExceedBoundary) {

    return valueOf(asBigInteger(value),
                   onExceedBoundary);
  }

  public static @NotNull Count valueOf(final long value,
                                       final @NotNull OnExceedBoundary onExceedBoundary) {

    return valueOf(asBigInteger(value),
                   onExceedBoundary);
  }

  /**
   *
   * @param value this will be truncated to an integral value (not rounded)
   */
  public static @NotNull Count valueOf(final @NotNull Number value,
                                       final @NotNull OnExceedBoundary onExceedBoundary) {

    return valueOf(asBigInteger(value),
                   onExceedBoundary);
  }

  private final @NotNull BigInteger value;

  private Count(final @NotNull BigInteger value) {

    Preconditions.checkArgument(value.compareTo(BigInteger.ZERO) >= 0);

    this.value = value;
  }

  /**
   *
   * @return the value which will be >= 0
   */
  public @NotNull BigInteger getValue() {
    return value;
  }

  @Override
  public @NotNull BigInteger bigIntegerValue() {
    return value;
  }

  @Override
  public @NotNull BigDecimal bigDecimalValue() {
    return new BigDecimal(value);
  }

  /**
   *
   * @return the value which will be >= 0 (cast to int)
   */
  @Override
  public int intValue() {
    return value.intValue();
  }

  /**
   *
   * @return the value which will be >= 0
   */
  @Override
  public long longValue() {
    return value.longValue();
  }

  /**
   *
   * @return the value which will be >= 0 (cast to float)
   */
  @Override
  public float floatValue() {
    return value.floatValue();
  }

  /**
   *
   * @return the value which will be >= 0 (cast to double)
   */
  @Override
  public double doubleValue() {
    return value.doubleValue();
  }

  public @NotNull Count plus(final @NotNull BigInteger augend,
                             final @NotNull OnExceedBoundary onExceedBoundary) {

    return valueOf(value.add(augend),
                   onExceedBoundary);
  }

  public @NotNull Count plus(final @NotNull BigNumber augend,
                             final @NotNull OnExceedBoundary onExceedBoundary) {

    return plus(asBigInteger(augend),
                onExceedBoundary);
  }

  public @NotNull Count plus(final long augend,
                             final @NotNull OnExceedBoundary onExceedBoundary) {

    return plus(asBigInteger(augend),
                onExceedBoundary);
  }

  /**
   *
   * @param augend this will be truncated to an integral value (not rounded)
   */
  public @NotNull Count plus(final @NotNull Number augend,
                             final @NotNull OnExceedBoundary onExceedBoundary) {

    return plus(asBigInteger(augend),
                onExceedBoundary);
  }

  public @NotNull Count minus(final @NotNull BigInteger subtrahend,
                              final @NotNull OnExceedBoundary onExceedBoundary) {

    return valueOf(value.subtract(subtrahend),
                   onExceedBoundary);
  }

  public @NotNull Count minus(final @NotNull BigNumber subtrahend,
                              final @NotNull OnExceedBoundary onExceedBoundary) {

    return minus(asBigInteger(subtrahend),
                 onExceedBoundary);
  }

  public @NotNull Count minus(final long augend,
                              final @NotNull OnExceedBoundary onExceedBoundary) {

    return minus(asBigInteger(augend),
                 onExceedBoundary);
  }

  /**
   *
   * @param subtrahend this will be truncated to an integral value (not rounded)
   */
  public @NotNull Count minus(final @NotNull Number subtrahend,
                              final @NotNull OnExceedBoundary onExceedBoundary) {

    return minus(asBigInteger(subtrahend),
                 onExceedBoundary);
  }


  /**
   * If the result (before conversion to a {@link Count} instance) has a fractional component then this will be
   * truncated.
   */
  public @NotNull Count multipliedBy(final @NotNull BigDecimal multiplicand,
                                     final @NotNull OnExceedBoundary onExceedBoundary) {

    final BigDecimal result = new BigDecimal(value).multiply(multiplicand);

    return valueOf(result.toBigInteger(),
                   onExceedBoundary);
  }

  public @NotNull Count multipliedBy(final @NotNull BigInteger multiplicand,
                                     final @NotNull OnExceedBoundary onExceedBoundary) {

    return valueOf(value.multiply(multiplicand),
                   onExceedBoundary);
  }

  /**
   * If the result (before conversion to a {@link Count} instance) has a fractional component then this will be
   * truncated.
   */
  public @NotNull Count multipliedBy(final @NotNull BigNumber multiplicand,
                                     final @NotNull OnExceedBoundary onExceedBoundary) {

    return multipliedBy(multiplicand.bigDecimalValue(),
                        onExceedBoundary);
  }

  public @NotNull Count multipliedBy(final long multiplicand,
                                     final @NotNull OnExceedBoundary onExceedBoundary) {

    return multipliedBy(asBigInteger(multiplicand),
                        onExceedBoundary);
  }

  /**
   * If the result (before conversion to a {@link Count} instance) has a fractional component then this will be
   * truncated.
   */
  public @NotNull Count multipliedBy(final @NotNull Number multiplicand,
                                     final @NotNull OnExceedBoundary onExceedBoundary) {

    return multipliedBy(BigDecimal.valueOf(multiplicand.doubleValue()),
                        onExceedBoundary);
  }

  /**
   * If the result (before conversion to a {@link Count} instance) has a fractional component then this will be
   * truncated.
   */
  public @NotNull Count dividedBy(final @NotNull BigDecimal divisor,
                                  final @NotNull OnExceedBoundary onExceedBoundary) {

    final BigDecimal result = new BigDecimal(value).divide(divisor,
                                                           // make sure there is plenty of room in the
                                                           // scale so that the integer component is not
                                                           // rounded
                                                           10,
                                                           RoundingMode.HALF_UP);

    return valueOf(result.toBigInteger(),
                   onExceedBoundary);
  }

  public @NotNull Count dividedBy(final @NotNull BigInteger divisor,
                                  final @NotNull OnExceedBoundary onExceedBoundary) {

    return valueOf(value.divide(divisor),
                   onExceedBoundary);
  }

  /**
   * If the result (before conversion to a {@link Count} instance) has a fractional component then this will be
   * truncated.
   */
  public @NotNull Count dividedBy(final @NotNull BigNumber divisor,
                                  final @NotNull OnExceedBoundary onExceedBoundary) {

    return dividedBy(divisor.bigDecimalValue(),
                     onExceedBoundary);
  }

  public @NotNull Count dividedBy(final long divisor,
                                  final @NotNull OnExceedBoundary onExceedBoundary) {

    return dividedBy(asBigInteger(divisor),
                     onExceedBoundary);
  }

  /**
   * If the result (before conversion to a {@link Count} instance) has a fractional component then this will be
   * truncated.
   */
  public @NotNull Count dividedBy(final @NotNull Number divisor,
                                  final @NotNull OnExceedBoundary onExceedBoundary) {

    return multipliedBy(BigDecimal.valueOf(divisor.doubleValue()),
                        onExceedBoundary);
  }

  @Override
  public boolean equals(final Object o) {
    if(this == o) {
      return true;
    }
    if(o == null || getClass() != o.getClass()) {
      return false;
    }

    final Count count = (Count) o;

    return value.equals(count.value);

  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public String toString() {
    return "Count{" +
           "value=" + String.format("%,d", value) +
           '}';
  }
}

