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

package com.github.richardballard.arbeeutils.concurrent;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.StampedLock;

/**
 * Based on {@link com.google.common.util.concurrent.Uninterruptibles} but with additional cases handled.
 */
@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
public enum MoreUninterruptibles {
  ;

  /**
   * Invokes {@link Lock#tryLock(long, TimeUnit)} uninterruptibly
   */
  public static boolean tryLockUninterruptibly(final @NotNull Lock lock,
                                               final @NotNull Duration timeout) {

    boolean interrupted = false;
    try {
      while (true) {
        try {
          return lock.tryLock(timeout.toNanos(),
                              TimeUnit.NANOSECONDS);
        }
        catch(final InterruptedException ignored) {
          interrupted = true;
        }
      }
    }
    finally {
      if (interrupted) {
        Thread.currentThread().interrupt();
      }
    }
  }

  /**
   * Invokes {@link StampedLock#tryReadLock(long, TimeUnit)} uninterruptibly
   */
  public static long tryReadLockUninterruptibly(final @NotNull StampedLock lock,
                                                final @NotNull Duration timeout) {

    boolean interrupted = false;
    try {
      while (true) {
        try {
          return lock.tryReadLock(timeout.toNanos(),
                                  TimeUnit.NANOSECONDS);
        }
        catch(final InterruptedException ignored) {
          interrupted = true;
        }
      }
    }
    finally {
      if (interrupted) {
        Thread.currentThread().interrupt();
      }
    }
  }


  /**
   * Invokes {@link StampedLock#tryWriteLock(long, TimeUnit)} uninterruptibly
   */
  public static long tryWriteLockUninterruptibly(final @NotNull StampedLock lock,
                                                 final @NotNull Duration timeout) {

    boolean interrupted = false;
    try {
      while (true) {
        try {
          return lock.tryWriteLock(timeout.toNanos(),
                                   TimeUnit.NANOSECONDS);
        }
        catch(final InterruptedException ignored) {
          interrupted = true;
        }
      }
    }
    finally {
      if (interrupted) {
        Thread.currentThread().interrupt();
      }
    }
  }
}
