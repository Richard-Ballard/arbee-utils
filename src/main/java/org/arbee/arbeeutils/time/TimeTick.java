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

package org.arbee.arbeeutils.time;

import com.google.common.base.Preconditions;
import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/**
 * This class represents an arbitrary point in time (to nanosecond precision).  The difference between this and
 * {@link java.time.Instant} is that {@code Instant} is bound to wall time/system current time, but this class is not.
 * If you are using {@code Instant} to measure the duration or elapsed time between to points then if the OS clock is
 * changed between the start and end instant then the difference will be skewed.  However, since {@code TimeTick}
 * uses {@link System#nanoTime()} it is unaffected by changed to the OS clock.
 */
@Immutable
public class TimeTick implements Comparable<TimeTick> {

    private final long nanoTicks;

    TimeTick(final long nanoTicks) {
        this.nanoTicks = nanoTicks;
    }

    @NotNull
    public Duration durationSince(@NotNull final TimeTick startTick) {
        assert startTick != null;

        Preconditions.checkArgument(startTick.compareTo(this) <= 0,
                                    "start tick (%s) must not be after this tick (%s)",
                                    startTick,
                                    this);

        return Duration.ofNanos(nanoTicks - startTick.nanoTicks);
    }

    /**
     *
     * @throws ArithmeticException if the addition would cause an overflow or underflow
     */
    @NotNull
    public TimeTick plus(@NotNull final Duration duration) throws ArithmeticException {
        assert duration != null;

        final TimeTick result;
        if(duration.isZero()) {
            result = this;
        }
        else {
            result = new TimeTick(Math.addExact(nanoTicks, duration.toNanos()));
        }

        return result;
    }


    @SuppressWarnings("SubtractionInCompareTo")
    @Override
    public int compareTo(@NotNull final TimeTick other) {
        assert other != null;

        // Note, usually I would call Long.compare() for this, but the notes for System.nanoTime() explicitly say
        // that when comparing longs that are from this method you should use t1 - t0 < 0.  This is usually a bad
        // idea because of numeric overflows, but with System.nanoTime() the overflows are handled, even expected.
        // RMB 2016/05/29
        final int result;

        final long otherNanoTicks = other.nanoTicks;

        if(nanoTicks == otherNanoTicks) {
            result = 0;
        }
        else if(nanoTicks - otherNanoTicks < 0) {
            result = -1;
        }
        else {
            result = 1;
        }

        return result;
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }

        final TimeTick timeTick = (TimeTick) o;

        return nanoTicks == timeTick.nanoTicks;
    }

    @Override
    public int hashCode() {
        return (int) (nanoTicks ^ nanoTicks >>> 32);
    }

    @Override
    public String toString() {
        return "TimeTick{" +
               "nanoTicks=" + nanoTicks +
               '}';
    }
}
