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

import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.locks.StampedLock;
import java.util.function.Supplier;

/**
 * This class is a wrapper around an instance of {@link StampedLock} that allows the caller to use the delegate
 * in a more functional manner.
 * <p/>
 * For read locks it has the concept of {@code pessimistic} and {@code optimistic} read locks.
 * <p/>
 * <b>Pessimistic Read Locks</b>
 * <p/>
 * Pessimistic locks are only allowed when there are no outstanding write locks.  Multiple pessimistic read locks are
 * allowed simultaneously.
 * <p/>
 * <b>Optimistic Read Locks</b>
 * <p/>
 * Optimistic locks may be are 'started' while there are any other kind of locks (including write locks) outstanding.
 * Once the operation is finished the code tests to see if a write lock was in progress during the operation and if
 * so then the optimistic lock fails.  The code attempts a number of optimistic locks before failing over to an pessimistic
 * lock.  Because of this very loose kind of locking (and the fact that the operation may be called any number of times),
 * the operation itself (that is run during the optimistic lock attempt) must not update any state - it must read only.
 * See {@link #optimisticRead(Supplier)} for more information.
 */
@ThreadSafe
public class WrappedStampedLock {

    // keep this number small as the typical case for doing a optimistic lock is a short operation.  If the first
    // lock attempt fails and the operation is short then chances are that the subsequent attempts will also fail.  RMB 2016/06/05
    private static final int NUM_OPTIMISTIC_ATTEMPTS = 2;

    @NotNull
    private final StampedLock delegate;

    public WrappedStampedLock(@NotNull final StampedLock delegate) {
        assert delegate != null;

        this.delegate = delegate;
    }

    public WrappedStampedLock() {
        this(new StampedLock());
    }


    /**
     * Runs the given {@code operation} in a pessimistic read lock.  While the operation is running the only other
     * locks that *may* be running are pessimistic and optimistic read locks.
     */
    public <T> T pessimisticRead(@NotNull final Supplier<T> operation) {
        assert operation != null;

        final long stamp = delegate.readLock();
        try {
            return operation.get();
        }
        finally {
            delegate.unlockRead(stamp);
        }
    }

    /**
     * See the class doc ({@link WrappedStampedLock}) for an overview of optimistic locks.
     * <p/>
     * The operation may be run at the same time as any other operation (including a write lock).
     * Because of this it should only read data, and not make decisions based on the data it reads.  Consider the example,
     * if it were to read a long value and if the value is < 10 throw a BadValueException.
     * The thread that is running the optimistic lock operation reads the first 32 bits of the long.  Another thread
     * writes a new value to the long (in a write lock).  The read thread reads the remaining 32 bits and so reads
     * a invalid number and throws BadValueException.  If it were to not make the decision to throw if the value < 10
     * then after reading the code would see that a write lock had occurred whilst reading and so re-attempt the optimistic
     * read (thus returning the correct number).
     * <p/>
     * Another example - if the code in the optimistic read iterates through a list then another thread may update
     * the list and so the reader gets a {@link java.util.ConcurrentModificationException}.  In this case a
     * {@link #pessimisticRead(Supplier)} should be used.
     */
    public <T> T optimisticRead(@NotNull final Supplier<T> operation) {
        assert operation != null;

        T result = null;    // setting this to null isn't needed *except* to keep the compiler happy - RMB 2016/06/05

        boolean validReadPerformed = false;
        for(int i = 0; i < NUM_OPTIMISTIC_ATTEMPTS && !validReadPerformed; i++) {
            final long stamp = delegate.tryOptimisticRead();
            if(stamp != 0L) {        // this will be 0 if exclusively locked
                result = operation.get();
                if(delegate.validate(stamp)) {
                    validReadPerformed = true;        // this will break the loop
                }
            }
        }

        if(!validReadPerformed) {
            result = pessimisticRead(operation);
        }

        return result;
    }

    /**
     * Performs an operation within a write lock.  When this operation is running the only other locks that *may* be
     * running are optimistic reads.
     */
    public <T> T write(@NotNull final Supplier<T> operation) {
        assert operation != null;

        final long stamp = delegate.writeLock();
        try {
            return operation.get();
        }
        finally {
            delegate.unlockWrite(stamp);
        }
    }

    /**
     * See {@link #write(Supplier)}
     */
    public void write(@NotNull final Runnable operation) {
        assert operation != null;

        write(() -> {
            operation.run();

            //noinspection ReturnOfNull
            return null;
        });
    }
}
