/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.webservice.sse;

import java.util.Random;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the exponential backoff with jitter backoff strategy.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
class ExponentialBackoffWithJitter implements BackoffStrategy {
    private static final long INITIAL_RECONNECT_ATTEMPT_WAIT_TIME_IN_SECONDS = 5;
    private static final long MAXIMUM_RECONNECT_ATTEMPT_WAIT_TIME_IN_SECONDS = 3600;

    private final long minimumWaitTimeInSeconds;
    private final long maximumWaitTimeInSeconds;
    private final long retryIntervalInSeconds;
    private final Random random;

    private final Logger logger = LoggerFactory.getLogger(ExponentialBackoffWithJitter.class);

    /**
     * Creates a new {@link ExponentialBackoffWithJitter}.
     */
    public ExponentialBackoffWithJitter() {
        this(INITIAL_RECONNECT_ATTEMPT_WAIT_TIME_IN_SECONDS, MAXIMUM_RECONNECT_ATTEMPT_WAIT_TIME_IN_SECONDS,
                INITIAL_RECONNECT_ATTEMPT_WAIT_TIME_IN_SECONDS);
    }

    ExponentialBackoffWithJitter(long minimumWaitTimeInSeconds, long maximumWaitTimeInSeconds,
            long retryIntervalInSeconds) {
        this(minimumWaitTimeInSeconds, maximumWaitTimeInSeconds, retryIntervalInSeconds, new Random());
    }

    ExponentialBackoffWithJitter(long minimumWaitTimeInSeconds, long maximumWaitTimeInSeconds,
            long retryIntervalInSeconds, Random random) {
        if (minimumWaitTimeInSeconds < 0) {
            throw new IllegalArgumentException("minimumWaitTimeInSeconds must not be smaller than zero");
        }
        if (maximumWaitTimeInSeconds < 0) {
            throw new IllegalArgumentException("maximumWaitTimeInSeconds must not be smaller than zero");
        }
        if (retryIntervalInSeconds < 0) {
            throw new IllegalArgumentException("retryIntervalInSeconds must not be smaller than zero");
        }
        if (maximumWaitTimeInSeconds < minimumWaitTimeInSeconds) {
            throw new IllegalArgumentException(
                    "maximumWaitTimeInSeconds must not be smaller than minimumWaitTimeInSeconds");
        }
        if (maximumWaitTimeInSeconds < retryIntervalInSeconds) {
            throw new IllegalArgumentException(
                    "maximumWaitTimeInSeconds must not be smaller than retryIntervalInSeconds");
        }

        this.minimumWaitTimeInSeconds = minimumWaitTimeInSeconds;
        this.maximumWaitTimeInSeconds = maximumWaitTimeInSeconds;
        this.retryIntervalInSeconds = retryIntervalInSeconds;
        this.random = random;
    }

    @Override
    public long getMinimumSecondsUntilRetry() {
        return minimumWaitTimeInSeconds;
    }

    @Override
    public long getMaximumSecondsUntilRetry() {
        return maximumWaitTimeInSeconds;
    }

    @Override
    public long getSecondsUntilRetry(int failedAttempts) {
        if (failedAttempts < 0) {
            logger.warn("The number of failed attempts must not be smaller than zero, was {}.", failedAttempts);
        }

        return minimumWaitTimeInSeconds
                + getRandomLongWithUpperLimit(Math.min(maximumWaitTimeInSeconds - minimumWaitTimeInSeconds,
                        retryIntervalInSeconds * (long) Math.pow(2, Math.max(0, failedAttempts))));
    }

    private long getRandomLongWithUpperLimit(long upperLimit) {
        return Math.abs(random.nextLong()) % (upperLimit + 1);
    }
}
