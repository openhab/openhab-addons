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
 * Tracks consecutive failures of the notification poll and admits one running attempt at a time,
 * identified by a token. The retry delay doubles from {@link #MIN_INTERVAL_SECONDS} to
 * {@link #MAX_INTERVAL_SECONDS}.
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault
class NotificationPollBackoff {
    static final int MIN_INTERVAL_SECONDS = 300;
    static final int MAX_INTERVAL_SECONDS = AccountHandler.CHECK_DATA_INTERVAL;
    static final int FAILURES_BEFORE_UNDEF = 3;

    private static final long NO_ATTEMPT = 0;

    private int intervalSeconds = MIN_INTERVAL_SECONDS;
    private int failures = 0;
    private long nextAttempt = 0;
    private long attemptCounter = 0;
    private long inFlightToken = NO_ATTEMPT;
    private boolean pollAgain = false;

    /** {@code token} identifies the one attempt that {@link Outcome#STARTED} grants; it is the key to ending it. */
    record Start(Outcome outcome, long token) {
        enum Outcome {
            STARTED,
            BACKING_OFF,
            ALREADY_RUNNING
        }
    }

    record Failure(boolean firstOfStreak, boolean crossedUndefThreshold, boolean publishUndef, int delaySeconds) {
    }

    record Success(boolean endedStreak, boolean pollAgain) {
    }

    /** Reserves a poll if one may run now; a trigger refused meanwhile is handed to whoever ends the attempt. */
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

    /** Records a failed poll; {@code null} when a {@link #reset()} superseded the attempt. */
    synchronized @Nullable Failure onFailure(long token, long now) {
        if (token == NO_ATTEMPT || token != inFlightToken) {
            return null;
        }
        inFlightToken = NO_ATTEMPT;
        // a trigger refused during this attempt expires here, because a push must not undercut the backoff deadline
        pollAgain = false;
        failures++;
        int delaySeconds = intervalSeconds;
        intervalSeconds = Math.min(intervalSeconds * 2, MAX_INTERVAL_SECONDS);
        nextAttempt = now + delaySeconds * 1000L;
        // publishUndef is ">=", not "==": a handler that registers during an outage has to be told as well.
        return new Failure(failures == 1, failures == FAILURES_BEFORE_UNDEF, failures >= FAILURES_BEFORE_UNDEF,
                delaySeconds);
    }

    /** Records a successful poll and clears the backoff; {@code null} when superseded. */
    synchronized @Nullable Success onSuccess(long token) {
        if (token == NO_ATTEMPT || token != inFlightToken) {
            return null;
        }
        boolean endedStreak = failures > 0;
        boolean catchUp = pollAgain;
        reset();
        return new Success(endedStreak, catchUp);
    }

    /** Releases an attempt that reported neither success nor failure; {@code true} if a trigger waits. */
    synchronized boolean abort(long token) {
        if (token != NO_ATTEMPT && token == inFlightToken) {
            inFlightToken = NO_ATTEMPT;
            boolean catchUp = pollAgain;
            pollAgain = false;
            return catchUp;
        }
        return false;
    }

    /** Clears the backoff and releases a running attempt, whose late result is then discarded. */
    synchronized void reset() {
        failures = 0;
        intervalSeconds = MIN_INTERVAL_SECONDS;
        nextAttempt = 0;
        inFlightToken = NO_ATTEMPT;
        // a pending catch-up is dropped, because the caller resetting the backoff polls immediately anyway
        pollAgain = false;
    }

    synchronized boolean shouldSkip(long now) {
        return inFlightToken != NO_ATTEMPT || (failures > 0 && now < nextAttempt);
    }

    synchronized boolean isDue(long now) {
        return inFlightToken == NO_ATTEMPT && failures > 0 && now >= nextAttempt;
    }
}
