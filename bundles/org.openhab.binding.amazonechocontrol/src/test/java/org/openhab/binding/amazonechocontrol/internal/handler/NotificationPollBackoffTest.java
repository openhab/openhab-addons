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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.amazonechocontrol.internal.handler.NotificationPollBackoff.Failure;

/**
 * The {@link NotificationPollBackoffTest} contains tests for the {@link NotificationPollBackoff} class
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault
public class NotificationPollBackoffTest {
    private static final long NOW = 1_700_000_000_000L;

    @Test
    public void testDelayDoublesUpToMaximum() {
        NotificationPollBackoff backoff = new NotificationPollBackoff();

        assertThat(backoff.onFailure(NOW).delaySeconds(), is(300));
        assertThat(backoff.onFailure(NOW).delaySeconds(), is(600));
        assertThat(backoff.onFailure(NOW).delaySeconds(), is(1200));
        assertThat(backoff.onFailure(NOW).delaySeconds(), is(2400));
        assertThat(backoff.onFailure(NOW).delaySeconds(), is(3600));
        assertThat(backoff.onFailure(NOW).delaySeconds(), is(3600));
    }

    @Test
    public void testOnlyTheFirstFailureOfAStreakIsFlagged() {
        NotificationPollBackoff backoff = new NotificationPollBackoff();

        assertThat(backoff.onFailure(NOW).firstOfStreak(), is(true));
        assertThat(backoff.onFailure(NOW).firstOfStreak(), is(false));
        assertThat(backoff.onFailure(NOW).firstOfStreak(), is(false));

        backoff.onSuccess();

        assertThat(backoff.onFailure(NOW).firstOfStreak(), is(true));
    }

    @Test
    public void testUndefIsPublishedFromTheThirdFailureOnwards() {
        NotificationPollBackoff backoff = new NotificationPollBackoff();

        assertThat(backoff.onFailure(NOW).publishUndef(), is(false));
        assertThat(backoff.onFailure(NOW).publishUndef(), is(false));
        assertThat(backoff.onFailure(NOW).publishUndef(), is(true));
        assertThat(backoff.onFailure(NOW).publishUndef(), is(true));
        assertThat(backoff.onFailure(NOW).publishUndef(), is(true));
    }

    @Test
    public void testUndefThresholdIsCrossedOnlyOnce() {
        NotificationPollBackoff backoff = new NotificationPollBackoff();

        assertThat(backoff.onFailure(NOW).crossedUndefThreshold(), is(false));
        assertThat(backoff.onFailure(NOW).crossedUndefThreshold(), is(false));
        assertThat(backoff.onFailure(NOW).crossedUndefThreshold(), is(true));
        assertThat(backoff.onFailure(NOW).crossedUndefThreshold(), is(false));
        assertThat(backoff.onFailure(NOW).crossedUndefThreshold(), is(false));
    }

    @Test
    public void testPollWaitsUntilTheDelayElapsed() {
        NotificationPollBackoff backoff = new NotificationPollBackoff();

        assertThat(backoff.shouldSkip(NOW), is(false));
        assertThat(backoff.isDue(NOW), is(false));

        backoff.onFailure(NOW);

        assertThat(backoff.shouldSkip(NOW), is(true));
        assertThat(backoff.shouldSkip(NOW + 299_999L), is(true));
        assertThat(backoff.isDue(NOW + 299_999L), is(false));
        assertThat(backoff.shouldSkip(NOW + 300_000L), is(false));
        assertThat(backoff.isDue(NOW + 300_000L), is(true));
    }

    @Test
    public void testEveryFailureSchedulesAFiniteRetry() {
        NotificationPollBackoff backoff = new NotificationPollBackoff();

        for (int attempt = 0; attempt < 8; attempt++) {
            Failure failure = backoff.onFailure(NOW);
            assertThat(failure.delaySeconds(), lessThanOrEqualTo(NotificationPollBackoff.MAX_INTERVAL));
            assertThat(backoff.shouldSkip(NOW), is(true));
            assertThat(backoff.isDue(NOW + failure.delaySeconds() * 1000L), is(true));
        }

        assertThat(backoff.onSuccess(), is(true));
        assertThat(backoff.shouldSkip(NOW), is(false));
        assertThat(backoff.isDue(Long.MAX_VALUE), is(false));
    }

    @Test
    public void testSuccessResetsCounterAndDelay() {
        NotificationPollBackoff backoff = new NotificationPollBackoff();

        backoff.onFailure(NOW);
        backoff.onFailure(NOW);
        backoff.onFailure(NOW);

        assertThat(backoff.onSuccess(), is(true));
        assertThat(backoff.shouldSkip(NOW), is(false));

        Failure afterReset = backoff.onFailure(NOW);
        assertThat(afterReset.delaySeconds(), is(300));
        assertThat(afterReset.firstOfStreak(), is(true));
        assertThat(afterReset.publishUndef(), is(false));
        assertThat(afterReset.crossedUndefThreshold(), is(false));
    }

    @Test
    public void testSuccessWithoutPrecedingFailureDoesNotReportRecovery() {
        NotificationPollBackoff backoff = new NotificationPollBackoff();

        assertThat(backoff.onSuccess(), is(false));
        assertThat(backoff.onSuccess(), is(false));
    }

    @Test
    public void testResetClearsTheBackoffSilently() {
        NotificationPollBackoff backoff = new NotificationPollBackoff();

        backoff.onFailure(NOW);
        backoff.onFailure(NOW);
        backoff.reset();

        assertThat(backoff.shouldSkip(NOW), is(false));
        assertThat(backoff.isDue(NOW), is(false));
        assertThat(backoff.onSuccess(), is(false));
        assertThat(backoff.onFailure(NOW).delaySeconds(), is(300));
    }
}
