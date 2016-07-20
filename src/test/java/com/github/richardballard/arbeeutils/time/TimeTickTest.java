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

package com.github.richardballard.arbeeutils.time;

import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Test
public class TimeTickTest {
    public void instanceCanBeCreatedWithNegativeTicks() {
        //noinspection ResultOfObjectAllocationIgnored
        new TimeTick(-1L);
    }

    @DataProvider(name = "compareToTestData")
    @NotNull
    public CompareToTestData[][] getCompareToTestData() {
        return new CompareToTestData[][] {
                { new CompareToTestData(new TimeTick(1L), new TimeTick(1L), 0) },
                { new CompareToTestData(new TimeTick(1L), new TimeTick(2L), -1) },
                { new CompareToTestData(new TimeTick(2L), new TimeTick(1L), 1) },

                { new CompareToTestData(new TimeTick(-1L), new TimeTick(-1L), 0) },
                { new CompareToTestData(new TimeTick(-1L), new TimeTick(-2L), 1) },
                { new CompareToTestData(new TimeTick(-2L), new TimeTick(-1L), -1) },
        };
    }

    @Test(dataProvider = "compareToTestData")
    public void compareToReturnsExpected(@NotNull final CompareToTestData data) {
        assert data != null;

        assertThat(data.getFirst().compareTo(data.getSecond()))
                .isEqualTo(data.getExpectedResult());
    }

    @Immutable
    public static class CompareToTestData {
        @NotNull
        private final TimeTick first;

        @NotNull
        private final TimeTick second;

        private final int expectedResult;

        public CompareToTestData(@NotNull final TimeTick first,
                                 @NotNull final TimeTick second,
                                 final int expectedResult) {
            assert first != null;
            assert second != null;

            this.first = first;
            this.second = second;
            this.expectedResult = expectedResult;
        }

        @NotNull
        public TimeTick getFirst() {
            return first;
        }

        @NotNull
        public TimeTick getSecond() {
            return second;
        }

        public int getExpectedResult() {
            return expectedResult;
        }

        @Override
        public String toString() {
            return "CompareToTestData{" +
                   "first=" + first +
                   ", second=" + second +
                   ", expectedResult=" + expectedResult +
                   '}';
        }
    }

    public void durationSinceThrowsIfStartAfterEnd() {
        final TimeTick startTick = new TimeTick(6L);
        final TimeTick endTick = new TimeTick(5L);

        assertThatThrownBy(() -> endTick.durationSince(startTick))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("start tick (" + startTick + ") must not be after this tick (" + endTick + ')');
    }

    public void durationSinceHandlesZeroDuration() {
        final TimeTick startTick = new TimeTick(6L);

        assertThat(startTick.durationSince(startTick))
                .isEqualTo(Duration.ZERO);
    }

    public void durationSinceHandlesNonZeroDuration() {
        final TimeTick startTick = new TimeTick(6L);
        final TimeTick endTick = new TimeTick(10L);

        assertThat(endTick.durationSince(startTick))
                .isEqualTo(Duration.ofNanos(4L));
    }

    public void plusThrowsOnOverflow() {
        final TimeTick startTick = new TimeTick(Long.MAX_VALUE);

        assertThatThrownBy(() -> startTick.plus(Duration.ofNanos(1L)))
                .isInstanceOf(ArithmeticException.class);
    }

    public void plusThrowsOnUnderflow() {
        final TimeTick startTick = new TimeTick(Long.MIN_VALUE);

        assertThatThrownBy(() -> startTick.plus(Duration.ofNanos(-1L)))
                .isInstanceOf(ArithmeticException.class);
    }

    public void plusAddsPositive() {
        final TimeTick startTick = new TimeTick(1L);

        assertThat(startTick.plus(Duration.ofNanos(10L)))
                .isEqualTo(new TimeTick(11L));
    }

    public void plusAddsNegative() {
        final TimeTick startTick = new TimeTick(1L);

        assertThat(startTick.plus(Duration.ofNanos(-10L)))
                .isEqualTo(new TimeTick(-9L));
    }
}
