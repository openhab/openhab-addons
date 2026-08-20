/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.amazonechocontrol.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link NotificationPollBackoff} tracks consecutive failures of the notification poll: when the next
 * attempt is due, whether the failure is worth a warning and whether the channels shall be set to UNDEF. The
 * retry delay doubles from {@link #MIN_INTERVAL} up to {@link #MAX_INTERVAL}. A poll has to be reserved
 * through {@link #tryStart(long)}, which admits a single running attempt at a time and hands out a token
 * identifying it; {@link #reset()} releases the running attempt, whose late result is then discarded. All
 * state sits behind one lock, which is deliberately not held across the network call.
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault
class NotificationPollBackoff {
    /** Delay before the first retry, in seconds. */
    static final int MIN_INTERVAL = 300;
    /** Upper bound of the retry delay, in seconds. */
    static final int MAX_INTERVAL = AccountHandler.CHECK_DATA_INTERVAL;
    static final int FAILURES_BEFORE_UNDEF = 3;

    private static final long NO_ATTEMPT = 0;

    private int interval = MIN_INTERVAL;
    private int failures = 0;
    private long nextAttempt = 0;
    private long attemptCounter = 0;
    private long inFlightToken = NO_ATTEMPT;
    private boolean pollAgain = false;

    /**
     * The decision on starting a poll: only {@link Outcome#STARTED} permits the request, and {@code token}
     * then identifies the attempt for {@link #onSuccess(long)}, {@link #onFailure(long, long)} and
     * {@link #abort(long)}.
     */
    record Start(Outcome outcome, long token) {
        enum Outcome {
            STARTED,
            BACKING_OFF,
            ALREADY_RUNNING
        }
    }

    /**
     * The reaction to a single failed poll.
     *
     * @param firstOfStreak this is the first failure after a successful poll
     * @param crossedUndefThreshold this failure is the one that reached {@link #FAILURES_BEFORE_UNDEF}
     * @param publishUndef the channels no longer describe reality and shall be set to UNDEF
     * @param delaySeconds seconds to wait before the next attempt
     */
    record Failure(boolean firstOfStreak, boolean crossedUndefThreshold, boolean publishUndef, int delaySeconds) {
    }

    /**
     * The reaction to a successful poll.
     *
     * @param endedStreak the poll ended a failure streak
     * @param pollAgain a trigger was refused while this attempt was running and shall be caught up on
     */
    record Success(boolean endedStreak, boolean pollAgain) {
    }

    /**
     * Atomically decides whether a poll may run now and reserves it, admitting one running attempt at a time; a
     * trigger refused because an attempt is already running is remembered and handed to whoever ends that attempt.
     */
    synchronized Start tryStart(long now) {
        if (inFlightToken != NO_ATTEMPT) {
            pollAgain = true;
            return new Start(Start.Outcome.ALREADY_RUNNING, NO_ATTEMPT);
        }
        if (failures > 0 && now < nextAttempt) {
            return new Start(Start.Outcome.BACKING_OFF, NO_ATTEMPT);
        }
        inFlightToken = ++attemptCounter;
        return new Start(Start.Outcome.STARTED, inFlightToken);
    }

    /**
     * Records a failed poll.
     *
     * @return the reaction, or {@code null} if the attempt was superseded by a {@link #reset()} and its result
     *         is discarded
     */
    synchronized @Nullable Failure onFailure(long token, long now) {
        if (token == NO_ATTEMPT || token != inFlightToken) {
            return null;
        }
        inFlightToken = NO_ATTEMPT;
        // a trigger refused during this attempt expires here, because a push must not undercut the backoff deadline
        pollAgain = false;
        failures++;
        int delaySeconds = interval;
        interval = Math.min(interval * 2, MAX_INTERVAL);
        nextAttempt = now + delaySeconds * 1000L;
        // publishUndef is ">=", not "==": a handler that registers during an outage has to be told as well.
        return new Failure(failures == 1, failures == FAILURES_BEFORE_UNDEF, failures >= FAILURES_BEFORE_UNDEF,
                delaySeconds);
    }

    /**
     * Records a successful poll and clears the backoff.
     *
     * @return the reaction, or {@code null} if the attempt was superseded by a {@link #reset()} and its result
     *         is discarded
     */
    synchronized @Nullable Success onSuccess(long token) {
        if (token == NO_ATTEMPT || token != inFlightToken) {
            return null;
        }
        boolean endedStreak = failures > 0;
        boolean catchUp = pollAgain;
        reset();
        return new Success(endedStreak, catchUp);
    }

    /**
     * Releases a reserved attempt that reported neither success nor failure, and is a no-op otherwise.
     *
     * @return {@code true} if a trigger was refused while the released attempt was running and shall be caught
     *         up on
     */
    synchronized boolean abort(long token) {
        if (token != NO_ATTEMPT && token == inFlightToken) {
            inFlightToken = NO_ATTEMPT;
            boolean catchUp = pollAgain;
            pollAgain = false;
            return catchUp;
        }
        return false;
    }

    /**
     * Clears the backoff without reporting a recovery, for example after a re-login; a running attempt is
     * released so the next poll may start immediately, and its late result will be discarded.
     */
    synchronized void reset() {
        failures = 0;
        interval = MIN_INTERVAL;
        nextAttempt = 0;
        inFlightToken = NO_ATTEMPT;
        // a pending catch-up is dropped, because the caller resetting the backoff polls immediately anyway
        pollAgain = false;
    }

    /** A poll right now would be refused, because of a pending backoff delay or a running attempt. */
    synchronized boolean shouldSkip(long now) {
        return inFlightToken != NO_ATTEMPT || (failures > 0 && now < nextAttempt);
    }

    /** A failed poll is ready for its retry and no attempt is running. */
    synchronized boolean isDue(long now) {
        return inFlightToken == NO_ATTEMPT && failures > 0 && now >= nextAttempt;
    }
}
