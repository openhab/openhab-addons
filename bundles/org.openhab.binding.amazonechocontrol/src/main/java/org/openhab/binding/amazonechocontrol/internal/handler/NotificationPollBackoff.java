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

/**
 * The {@link NotificationPollBackoff} tracks consecutive failures of the notification poll and derives the
 * reaction to each of them: when the next attempt is due, whether the failure is worth a warning and whether
 * the notification channels still describe reality.
 * <p>
 * Amazon throttles the notification endpoint aggressively and a flat retry interval keeps a throttled account
 * throttled, because every retry is itself a counted request. The delay therefore doubles from
 * {@link #MIN_INTERVAL} up to {@link #MAX_INTERVAL}, which is the regular refresh interval.
 * <p>
 * The failure count, the current delay and the resulting deadline are one state, so they live in one object
 * behind one lock: the poll is triggered both from the polling job and from push messages arriving on an HTTP
 * client thread. Keeping the deadline in a separate field of the caller allowed an interleaving in which a
 * successful poll and a failing one each wrote one half of the state, leaving a backoff that was never due.
 * The lock is deliberately not held across the network call - it only guards these fields.
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault
class NotificationPollBackoff {
    /** Delay before the first retry, in seconds. */
    static final int MIN_INTERVAL = 300;
    /** Upper bound of the retry delay, in seconds. Backing off further than the regular poll is pointless. */
    static final int MAX_INTERVAL = AccountHandler.CHECK_DATA_INTERVAL;
    /** Number of consecutive failures after which the notification channels are set to UNDEF. */
    static final int FAILURES_BEFORE_UNDEF = 3;

    private int interval = MIN_INTERVAL;
    private int failures = 0;
    private long nextAttempt = 0;

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
     * Records a failed poll and schedules the next attempt.
     *
     * @param now the current time in milliseconds
     * @return the reaction to this failure
     */
    synchronized Failure onFailure(long now) {
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
     * @return {@code true} if this poll ended a failure streak
     */
    synchronized boolean onSuccess() {
        boolean recovered = failures > 0;
        reset();
        return recovered;
    }

    /**
     * Clears the backoff without reporting a recovery, for example after a re-login.
     */
    synchronized void reset() {
        failures = 0;
        interval = MIN_INTERVAL;
        nextAttempt = 0;
    }

    /**
     * @param now the current time in milliseconds
     * @return {@code true} if a poll has to wait, because the previous one failed and the delay has not elapsed
     */
    synchronized boolean shouldSkip(long now) {
        return failures > 0 && now < nextAttempt;
    }

    /**
     * @param now the current time in milliseconds
     * @return {@code true} if a failed poll is due to be retried
     */
    synchronized boolean isDue(long now) {
        return failures > 0 && now >= nextAttempt;
    }
}
