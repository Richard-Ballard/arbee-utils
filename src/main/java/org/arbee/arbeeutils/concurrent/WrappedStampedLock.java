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

import com.google.common.annotations.VisibleForTesting;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.StampedLock;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This class is a wrapper around an instance of {@link StampedLock} that allows the caller to use the delegate
 * in a more functional manner.
 * <p/>
 * As with {@link StampedLock} this lock is non-reentrant, i.e. if a thread holds a lock and then attempts to aquire
 * another lock then it will deadlock.
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
 * Optimistic locks may be 'started' while there are any other kind of locks (including write locks) outstanding.
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

    @NotNull
    private final Function<ReadWriteLock, WrappedReadWriteLock> wrappedReadWriteLockFromReadWriteLockFunction;

    @NotNull
    private final Function<Lock, WrappedLock> wrappedLockFromLockFunction;

    @VisibleForTesting
    WrappedStampedLock(@NotNull final StampedLock delegate,
                       @NotNull final Function<ReadWriteLock, WrappedReadWriteLock> wrappedReadWriteLockFromReadWriteLockFunction,
                       @NotNull final Function<Lock, WrappedLock> wrappedLockFromLockFunction) {
        assert delegate != null;
        assert wrappedReadWriteLockFromReadWriteLockFunction != null;
        assert wrappedLockFromLockFunction != null;

        this.delegate = delegate;
        this.wrappedReadWriteLockFromReadWriteLockFunction = wrappedReadWriteLockFromReadWriteLockFunction;
        this.wrappedLockFromLockFunction = wrappedLockFromLockFunction;
    }

    public WrappedStampedLock(@NotNull final StampedLock delegate) {
        this(delegate,
             WrappedReadWriteLock::new,
             WrappedLock::new);

        assert delegate != null;
    }

    public WrappedStampedLock() {
        this(new StampedLock());
    }

    @NotNull
    public WrappedReadWriteLock asReadWriteLock() {
        return Objects.requireNonNull(wrappedReadWriteLockFromReadWriteLockFunction.apply(delegate.asReadWriteLock()));
    }

    @NotNull
    public WrappedLock asReadLock() {
        return Objects.requireNonNull(wrappedLockFromLockFunction.apply(delegate.asReadLock()));
    }

    @NotNull
    public WrappedLock asWriteLock() {
        return Objects.requireNonNull(wrappedLockFromLockFunction.apply(delegate.asWriteLock()));
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

    public enum TestFailedLockContext {
        /**
         * The operation will be run from within a pessimistic read lock
         */
        IN_PESSIMISTIC_READ_LOCK,

        /**
         * The operation will not be run in any lock
         */
        NO_LOCK
    }

    // todo - do a timeout version - RMB 2016/6/11
    // todo - do a void version - RMB 2016/6/11

    /**
     *
     * @param test this will be run in a pessimistic read lock.  If it passes (returns true) then a write lock will be
     *             obtained and {@code onTestPassedOperation} will be run and the result returned.  If it fails (returns
     *             false) then {@lonk onTestFailedOperation} will be run and the result returned.
     * @param onTestPassedOperation This will be run (and result returned) if {@code test} returns true.
     * @param onTestFailedOperation This will be run (and result returned) if {@code test} returns false.  The lock
     *                              context that will be used when this is called is determined by the value of
     *                              {@code testFailedLockContext}.
     * @param testFailedLockContext If the test fails then {@code onTestFailedOperation} will be called.  This parameter
     *                              determines what sort of lock to use (if any) when calling {@code testFailedLockContext}.
     */
    public <T> T writeIf(@NotNull final BooleanSupplier test,
                         @NotNull final Supplier<T> onTestPassedOperation,
                         @NotNull final Supplier<T> onTestFailedOperation,
                         @NotNull final TestFailedLockContext testFailedLockContext) {
        assert test != null;
        assert onTestPassedOperation != null;
        assert onTestFailedOperation != null;
        assert testFailedLockContext != null;

        long stamp = delegate.readLock();
        try {
            while(test.getAsBoolean()) {
                final long writeStamp = delegate.tryConvertToWriteLock(stamp);
                if(writeStamp == 0L) {              // conversion failed
                    delegate.unlockRead(stamp);
                    stamp = delegate.writeLock();
                }
                else {                              // conversion was successful
                    stamp = writeStamp;

                    return onTestPassedOperation.get();
                }
            }

            if(testFailedLockContext == TestFailedLockContext.IN_PESSIMISTIC_READ_LOCK) {
                return onTestFailedOperation.get();
            }
        }
        finally {
            delegate.unlock(stamp); // could be read or write lock
        }

        assert testFailedLockContext == TestFailedLockContext.NO_LOCK : testFailedLockContext;

        return onTestFailedOperation.get();
    }
}
