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
import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * This class represents a zero based count.  For any method that creates an instance (e.g. {@link #valueOf(BigInteger, OnExceedBoundary)}
 * there is an {@code onExceedBoundary} parameter which indicates what should happen if the result would be negative
 * (which doesn't make sense for a count).
 */
@Immutable
public class Count extends BigNumber {

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

    @NotNull
    public static Count valueOf(@NotNull final BigInteger value,
                                @NotNull final OnExceedBoundary onExceedBoundary) {
        assert value != null;
        assert onExceedBoundary != null;

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

    @NotNull
    private static BigInteger asBigInteger(final @NotNull BigNumber value) {
        assert value != null;

        return value.bigIntegerValue();
    }

    @NotNull
    private static BigInteger asBigInteger(final long value) {

        return BigInteger.valueOf(value);
    }

    @NotNull
    private static BigInteger asBigInteger(@NotNull final Number value) {
        assert value != null;

        return asBigInteger(value.longValue());
    }


    @NotNull
    public static Count valueOf(final @NotNull BigNumber value,
                                @NotNull final OnExceedBoundary onExceedBoundary) {
        assert value != null;
        assert onExceedBoundary != null;

        return valueOf(asBigInteger(value),
                       onExceedBoundary);
    }

    @NotNull
    public static Count valueOf(final long value,
                                @NotNull final OnExceedBoundary onExceedBoundary) {
        assert onExceedBoundary != null;

        return valueOf(asBigInteger(value),
                       onExceedBoundary);
    }

    /**
     *
     * @param value this will be truncated to an integral value (not rounded)
     */
    @NotNull
    public static Count valueOf(@NotNull final Number value,
                                @NotNull final OnExceedBoundary onExceedBoundary) {
        assert value != null;
        assert onExceedBoundary != null;

        return valueOf(asBigInteger(value),
                       onExceedBoundary);
    }

    @NotNull
    private final BigInteger value;

    private Count(@NotNull final BigInteger value) {
        assert value != null;
        assert value.compareTo(BigInteger.ZERO) >= 0;

        this.value = value;
    }

    /**
     *
     * @return the value which will be >= 0
     */
    @NotNull
    public BigInteger getValue() {
        return value;
    }

    @NotNull
    @Override
    public BigInteger bigIntegerValue() {
        return value;
    }

    @NotNull
    @Override
    public BigDecimal bigDecimalValue() {
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

    @NotNull
    public Count plus(@NotNull final BigInteger augend,
                      @NotNull final OnExceedBoundary onExceedBoundary) {
        assert augend != null;
        assert onExceedBoundary != null;

        return valueOf(value.add(augend),
                       onExceedBoundary);
    }

    @NotNull
    public Count plus(final @NotNull BigNumber augend,
                      @NotNull final OnExceedBoundary onExceedBoundary) {
        assert augend != null;
        assert onExceedBoundary != null;

        return plus(asBigInteger(augend),
                    onExceedBoundary);
    }

    @NotNull
    public Count plus(final long augend,
                      @NotNull final OnExceedBoundary onExceedBoundary) {
        assert onExceedBoundary != null;

        return plus(asBigInteger(augend),
                    onExceedBoundary);
    }

    /**
     *
     * @param augend this will be truncated to an integral value (not rounded)
     */
    @NotNull
    public Count plus(@NotNull final Number augend,
                      @NotNull final OnExceedBoundary onExceedBoundary) {
        assert augend != null;
        assert onExceedBoundary != null;

        return plus(asBigInteger(augend),
                    onExceedBoundary);
    }

    @NotNull
    public Count minus(@NotNull final BigInteger subtrahend,
                       @NotNull final OnExceedBoundary onExceedBoundary) {
        assert subtrahend != null;
        assert onExceedBoundary != null;

        return valueOf(value.subtract(subtrahend),
                       onExceedBoundary);
    }

    @NotNull
    public Count minus(final @NotNull BigNumber subtrahend,
                       @NotNull final OnExceedBoundary onExceedBoundary) {
        assert subtrahend != null;
        assert onExceedBoundary != null;

        return minus(asBigInteger(subtrahend),
                     onExceedBoundary);
    }

    @NotNull
    public Count minus(final long augend,
                       @NotNull final OnExceedBoundary onExceedBoundary) {
        assert onExceedBoundary != null;

        return minus(asBigInteger(augend),
                     onExceedBoundary);
    }

    /**
     *
     * @param subtrahend this will be truncated to an integral value (not rounded)
     */
    @NotNull
    public Count minus(@NotNull final Number subtrahend,
                       @NotNull final OnExceedBoundary onExceedBoundary) {
        assert subtrahend != null;
        assert onExceedBoundary != null;

        return minus(asBigInteger(subtrahend),
                     onExceedBoundary);
    }


    /**
     * If the result (before conversion to a {@link Count} instance) has a fractional component then this will be
     * truncated.
     */
    @NotNull
    public Count multipliedBy(@NotNull final BigDecimal multiplicand,
                              @NotNull final OnExceedBoundary onExceedBoundary) {
        assert multiplicand != null;
        assert onExceedBoundary != null;

        final BigDecimal result = new BigDecimal(value).multiply(multiplicand);

        return valueOf(result.toBigInteger(),
                       onExceedBoundary);
    }

    @NotNull
    public Count multipliedBy(@NotNull final BigInteger multiplicand,
                              @NotNull final OnExceedBoundary onExceedBoundary) {
        assert multiplicand != null;
        assert onExceedBoundary != null;

        return valueOf(value.multiply(multiplicand),
                       onExceedBoundary);
    }

    /**
     * If the result (before conversion to a {@link Count} instance) has a fractional component then this will be
     * truncated.
     */
    @NotNull
    public Count multipliedBy(@NotNull final BigNumber multiplicand,
                              @NotNull final OnExceedBoundary onExceedBoundary) {
        assert multiplicand != null;
        assert onExceedBoundary != null;

        return multipliedBy(multiplicand.bigDecimalValue(),
                            onExceedBoundary);
    }

    @NotNull
    public Count multipliedBy(final long multiplicand,
                              @NotNull final OnExceedBoundary onExceedBoundary) {
        assert onExceedBoundary != null;

        return multipliedBy(asBigInteger(multiplicand),
                            onExceedBoundary);
    }

    /**
     * If the result (before conversion to a {@link Count} instance) has a fractional component then this will be
     * truncated.
     */
    @NotNull
    public Count multipliedBy(@NotNull final Number multiplicand,
                              @NotNull final OnExceedBoundary onExceedBoundary) {
        assert multiplicand != null;
        assert onExceedBoundary != null;

        return multipliedBy(BigDecimal.valueOf(multiplicand.doubleValue()),
                            onExceedBoundary);
    }

    /**
     * If the result (before conversion to a {@link Count} instance) has a fractional component then this will be
     * truncated.
     */
    @NotNull
    public Count dividedBy(@NotNull final BigDecimal divisor,
                           @NotNull final OnExceedBoundary onExceedBoundary) {
        assert divisor != null;
        assert onExceedBoundary != null;

        final BigDecimal result = new BigDecimal(value).divide(divisor,
                                                               10,      // make sure there is plenty of room in the scale so that the integer component is not rounded
                                                               RoundingMode.HALF_UP);

        return valueOf(result.toBigInteger(),
                       onExceedBoundary);
    }

    @NotNull
    public Count dividedBy(@NotNull final BigInteger divisor,
                           @NotNull final OnExceedBoundary onExceedBoundary) {
        assert divisor != null;
        assert onExceedBoundary != null;

        return valueOf(value.divide(divisor),
                       onExceedBoundary);
    }

    /**
     * If the result (before conversion to a {@link Count} instance) has a fractional component then this will be
     * truncated.
     */
    @NotNull
    public Count dividedBy(@NotNull final BigNumber divisor,
                           @NotNull final OnExceedBoundary onExceedBoundary) {
        assert divisor != null;
        assert onExceedBoundary != null;

        return dividedBy(divisor.bigDecimalValue(),
                         onExceedBoundary);
    }

    @NotNull
    public Count dividedBy(final long divisor,
                           @NotNull final OnExceedBoundary onExceedBoundary) {
        assert onExceedBoundary != null;

        return dividedBy(asBigInteger(divisor),
                         onExceedBoundary);
    }

    /**
     * If the result (before conversion to a {@link Count} instance) has a fractional component then this will be
     * truncated.
     */
    @NotNull
    public Count dividedBy(@NotNull final Number divisor,
                           @NotNull final OnExceedBoundary onExceedBoundary) {
        assert divisor != null;
        assert onExceedBoundary != null;

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

