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

package org.arbee.arbeeutils.numeric;

import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Test
public class CountTest {

    public void valueOfThrowsWhenNegative() {

        final long longValue = -1L;

        assertThatThrownBy(() -> Count.valueOf(BigInteger.valueOf(longValue),
                                           Count.OnExceedBoundary.THROW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("value must be >= 0, not " + longValue);
    }

    public void valueOfSetsToZeroWhenNegative() {

        final Count count = Count.valueOf(BigInteger.valueOf(-1L),
                                          Count.OnExceedBoundary.USE_BOUNDARY_VALUE);

        assertThat(count)
                .isEqualTo(Count.ZERO);
    }

    public void plusAdds() {

        assertThat(Count.valueOf(10L,
                                 Count.OnExceedBoundary.THROW)
                        .plus(3L,
                              Count.OnExceedBoundary.THROW))
                .isEqualTo(Count.valueOf(13L,
                                         Count.OnExceedBoundary.THROW));
    }

    public void plusThrowsWhenNegative() {

        assertThatThrownBy(() -> Count.valueOf(BigInteger.valueOf(1L),
                                               Count.OnExceedBoundary.THROW)
                                      .plus(-5L,
                                            Count.OnExceedBoundary.THROW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("value must be >= 0, not -4");
    }

    public void plusSetsToZeroWhenNegative() {

        assertThat(Count.valueOf(BigInteger.valueOf(1L),
                                 Count.OnExceedBoundary.THROW)
                        .plus(-5L,
                              Count.OnExceedBoundary.USE_BOUNDARY_VALUE))
                .isEqualTo(Count.ZERO);
    }

    public void minusSubtracts() {

        assertThat(Count.valueOf(10L,
                                 Count.OnExceedBoundary.THROW)
                        .minus(3L,
                               Count.OnExceedBoundary.THROW))
                .isEqualTo(Count.valueOf(7L,
                                         Count.OnExceedBoundary.THROW));
    }

    public void minusThrowsWhenNegative() {

        assertThatThrownBy(() -> Count.valueOf(BigInteger.valueOf(1L),
                                               Count.OnExceedBoundary.THROW)
                                      .minus(5L,
                                             Count.OnExceedBoundary.THROW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("value must be >= 0, not -4");
    }

    public void minusSetsToZeroWhenNegative() {

        assertThat(Count.valueOf(BigInteger.valueOf(1L),
                                 Count.OnExceedBoundary.THROW)
                        .minus(5L,
                               Count.OnExceedBoundary.USE_BOUNDARY_VALUE))
                .isEqualTo(Count.ZERO);
    }

    public void multipliedByMultiplies() {

        assertThat(Count.valueOf(10L,
                                 Count.OnExceedBoundary.THROW)
                        .multipliedBy(3L,
                               Count.OnExceedBoundary.THROW))
                .isEqualTo(Count.valueOf(30L,
                                         Count.OnExceedBoundary.THROW));
    }

    public void multipliedByThrowsWhenNegative() {

        assertThatThrownBy(() -> Count.valueOf(BigInteger.valueOf(2L),
                                               Count.OnExceedBoundary.THROW)
                                      .multipliedBy(-5L,
                                             Count.OnExceedBoundary.THROW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("value must be >= 0, not -10");
    }

    public void multipliedBySetsToZeroWhenNegative() {

        assertThat(Count.valueOf(BigInteger.valueOf(2L),
                                 Count.OnExceedBoundary.THROW)
                        .multipliedBy(-5L,
                               Count.OnExceedBoundary.USE_BOUNDARY_VALUE))
                .isEqualTo(Count.ZERO);
    }

    @NotNull
    private BigNumber getBigNumber(@NotNull final BigInteger bigInteger,
                                   @NotNull final BigDecimal bigDecimal) {
        assert bigInteger != null;
        assert bigDecimal != null;

        final BigNumber bigNumber = mock(BigNumber.class);

        when(bigNumber.bigIntegerValue())
                .thenReturn(bigInteger);

        when(bigNumber.bigDecimalValue())
                .thenReturn(bigDecimal);

        return bigNumber;
    }

    public void multipliedByBigNumberUsesBigDecimal() {
        final BigInteger bigInteger = BigInteger.valueOf(1L);
        final BigDecimal bigDecimal = new BigDecimal("2.2");

        assertThat(Count.valueOf(10L,
                                 Count.OnExceedBoundary.THROW)
                        .multipliedBy(getBigNumber(bigInteger,
                                                   bigDecimal),
                                      Count.OnExceedBoundary.THROW))
                .isEqualTo(Count.valueOf(22L,
                                         Count.OnExceedBoundary.THROW));



    }

    @NotNull
    private Number getNumber(final long longValue,
                             final double doubleValue) {

        final Number number = mock(Number.class);

        when(number.longValue())
                .thenReturn(longValue);

        when(number.doubleValue())
                .thenReturn(doubleValue);

        return number;
    }

    public void multipliedByNumberUsesBigDecimal() {
        final long longValue = 1L;
        final double doubleValue = 2.2;

        assertThat(Count.valueOf(10L,
                                 Count.OnExceedBoundary.THROW)
                        .multipliedBy(getNumber(longValue,
                                                doubleValue),
                                      Count.OnExceedBoundary.THROW))
                .isEqualTo(Count.valueOf(22L,
                                         Count.OnExceedBoundary.THROW));



    }
}
