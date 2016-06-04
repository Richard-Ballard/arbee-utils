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

package org.arbee.arbeeutils.concurrent;

import org.arbee.arbeeutils.thread.StandardThreadOperations;
import org.arbee.arbeeutils.time.TimeTick;
import org.arbee.arbeeutils.time.TimeTicks;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

@Test
public class GateBarrierTest {

    public void openSetsState() {
        final GateBarrier barrier = new GateBarrier(GateBarrier.State.CLOSED);

        assertThat(barrier.getState())
                .isEqualTo(GateBarrier.State.CLOSED);

        barrier.open();

        assertThat(barrier.getState())
                .isEqualTo(GateBarrier.State.OPEN);
    }

    public void closeSetsState() {
        final GateBarrier barrier = new GateBarrier(GateBarrier.State.OPEN);

        assertThat(barrier.getState())
                .isEqualTo(GateBarrier.State.OPEN);

        barrier.close();

        assertThat(barrier.getState())
                .isEqualTo(GateBarrier.State.CLOSED);
    }

    public void awaitOpenNoTimeoutDoesntWaitIfOpen() {
        final GateBarrier barrier = new GateBarrier(GateBarrier.State.OPEN);

        // this will block forever if our test fails
        barrier.awaitOpen();
    }

    public void awaitOpenNoTimeoutWaits() {
        final GateBarrier barrier = new GateBarrier(GateBarrier.State.CLOSED);

        final Supplier<TimeTick> timeTickSupplier = TimeTicks.systemCurrentTimeTickSupplier();

        final TimeTick startTick = timeTickSupplier.get();

        final Duration sleepDuration = Duration.ofMillis(100L);

        Executors.newSingleThreadExecutor().submit(() -> {
            StandardThreadOperations.INSTANCE.sleep(sleepDuration);

            assertThat(barrier.getState())
                    .isEqualTo(GateBarrier.State.CLOSED);

            barrier.open();
        });

        barrier.awaitOpen();

        // make sure that the time it took to get here was because of the sleeping thread that opened it
        final TimeTick endTick = timeTickSupplier.get();

        assertThat(endTick.durationSince(startTick))
                .isGreaterThanOrEqualTo(sleepDuration);
    }

    public void awaitOpenTimeBoundTimesOut() {
        final GateBarrier barrier = new GateBarrier(GateBarrier.State.CLOSED);

        final Supplier<TimeTick> timeTickSupplier = TimeTicks.systemCurrentTimeTickSupplier();

        final TimeTick startTick = timeTickSupplier.get();

        final Duration timeout = Duration.ofMillis(100L);
        assertThat(barrier.awaitOpen(timeout))
                .isEqualTo(GateBarrier.TimeBoundOpenResult.TIME_OUT);

        final TimeTick endTick = timeTickSupplier.get();

        assertThat(endTick.durationSince(startTick))
                .isGreaterThanOrEqualTo(timeout);
    }

    public void awaitOpenTimeBoundBeatsTimeout() throws InterruptedException {
        final GateBarrier barrier = new GateBarrier(GateBarrier.State.CLOSED);

        final Supplier<TimeTick> timeTickSupplier = TimeTicks.systemCurrentTimeTickSupplier();

        final TimeTick startTick = timeTickSupplier.get();

        final Duration timeout = Duration.ofMillis(700L);

        Executors.newSingleThreadExecutor().submit(() -> {
            assertThat(barrier.getState())
                    .isEqualTo(GateBarrier.State.CLOSED);

            barrier.open();
        });

        assertThat(barrier.awaitOpen(timeout))
                .isEqualTo(GateBarrier.TimeBoundOpenResult.OPEN);

        final TimeTick endTick = timeTickSupplier.get();

        assertThat(endTick.durationSince(startTick))
                .isLessThanOrEqualTo(timeout);
    }

    public void gateMayBeReclosed() {
        final GateBarrier barrier = new GateBarrier(GateBarrier.State.OPEN);

        assertThat(barrier.getState())
                .isEqualTo(GateBarrier.State.OPEN);

        barrier.close();

        assertThat(barrier.getState())
                .isEqualTo(GateBarrier.State.CLOSED);

        barrier.open();

        assertThat(barrier.getState())
                .isEqualTo(GateBarrier.State.OPEN);

        barrier.close();

        assertThat(barrier.getState())
                .isEqualTo(GateBarrier.State.CLOSED);


    }
}
