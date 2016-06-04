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

import com.google.common.util.concurrent.Monitor;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * This class is a simple 'gate' that has one of two states; open or closed.  If closed then when a thread
 * calls one of the {@code awaitOpen} methods it will wait until another thread opens the gate.  A gate may be closed
 * and opened any number of times.
 */
@ThreadSafe
public class GateBarrier {

    public enum State {
        OPEN,
        CLOSED
    }

    @NotNull
    private State state;

    @NotNull
    private final Monitor monitor;

    @NotNull
    private final Monitor.Guard isOpenGuard;

    public GateBarrier(@NotNull final State initialState) {
        assert initialState != null;

        this.state = initialState;

        this.monitor = new Monitor(false);
        this.isOpenGuard = new Monitor.Guard(monitor) {
            @Override
            public boolean isSatisfied() {
                return state == State.OPEN;
            }
        };
    }

    /**
     * This returns the current state.  Note that the state may have changed at any point after calling this method.
     */
    @NotNull
    public State getState() {
        monitor.enter();
        try {
            return state;
        }
        finally {
            monitor.leave();
        }
    }

    @NotNull
    public GateBarrier open() {
        monitor.enter();
        try {
            state = State.OPEN;
        }
        finally {
            monitor.leave();
        }

        return this;
    }

    @NotNull
    public GateBarrier close() {
        monitor.enter();
        try {
            state = State.CLOSED;
        }
        finally {
            monitor.leave();
        }

        return this;
    }

    public void awaitOpen() {
        monitor.enterWhenUninterruptibly(isOpenGuard);
        monitor.leave();
    }

    public enum TimeBoundOpenResult {
        OPEN,
        TIME_OUT
    }

    @NotNull
    public TimeBoundOpenResult awaitOpen(@NotNull final Duration timeOut) {
        assert timeOut != null;

        final TimeBoundOpenResult result;
        if(monitor.enterWhenUninterruptibly(isOpenGuard,
                                            timeOut.toNanos(),
                                            TimeUnit.NANOSECONDS)) {
            try {
                result = TimeBoundOpenResult.OPEN;
            }
            finally {
                monitor.leave();
            }
        }
        else {
            result = TimeBoundOpenResult.TIME_OUT;
        }

        return result;

    }


}
