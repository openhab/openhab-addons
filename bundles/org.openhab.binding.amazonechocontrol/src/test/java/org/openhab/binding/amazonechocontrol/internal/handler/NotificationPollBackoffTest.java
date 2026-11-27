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
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.amazonechocontrol.internal.handler.NotificationPollBackoff.Failure;
import org.openhab.binding.amazonechocontrol.internal.handler.NotificationPollBackoff.Start;
import org.openhab.binding.amazonechocontrol.internal.handler.NotificationPollBackoff.Success;

/**
 * The {@link NotificationPollBackoffTest} contains tests for the {@link NotificationPollBackoff} class
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault
public class NotificationPollBackoffTest {
    private static final long NOW = 1_700_000_000_000L;

    private long start(NotificationPollBackoff backoff, long now) {
        Start start = backoff.tryStart(now);
        assertThat(start.outcome(), is(Start.Outcome.STARTED));
        return start.token();
    }

    private Failure fail(NotificationPollBackoff backoff, long now) {
        Failure failure = backoff.onFailure(start(backoff, now), now);
        assertThat(failure, is(notNullValue()));
        return Objects.requireNonNull(failure);
    }

    private Success succeed(NotificationPollBackoff backoff, long now) {
        Success success = backoff.onSuccess(start(backoff, now));
        assertThat(success, is(notNullValue()));
        return Objects.requireNonNull(success);
    }

    @Test
    public void testDelayDoublesUpToMaximum() {
        NotificationPollBackoff backoff = new NotificationPollBackoff();
        long now = NOW;

        for (int expectedDelay : new int[] { 300, 600, 1200, 2400, 3600, 3600 }) {
            Failure failure = fail(backoff, now);
            assertThat(failure.delaySeconds(), is(expectedDelay));
            now += failure.delaySeconds() * 1000L;
        }
    }

    @Test
    public void testOnlyTheFirstFailureOfAStreakIsFlagged() {
        NotificationPollBackoff backoff = new NotificationPollBackoff();
        long now = NOW;

        assertThat(fail(backoff, now).firstOfStreak(), is(true));
        now += 300_000L;
        assertThat(fail(backoff, now).firstOfStreak(), is(false));
        now += 600_000L;
        assertThat(fail(backoff, now).firstOfStreak(), is(false));
        now += 1_200_000L;

        succeed(backoff, now);

        assertThat(fail(backoff, now).firstOfStreak(), is(true));
    }

    @Test
    public void testUndefIsPublishedFromTheThirdFailureOnwards() {
        NotificationPollBackoff backoff = new NotificationPollBackoff();
        long now = NOW;

        boolean[] expected = { false, false, true, true, true };
        for (boolean expectedUndef : expected) {
            Failure failure = fail(backoff, now);
            assertThat(failure.publishUndef(), is(expectedUndef));
            now += failure.delaySeconds() * 1000L;
        }
    }

    @Test
    public void testUndefThresholdIsCrossedOnlyOnce() {
        NotificationPollBackoff backoff = new NotificationPollBackoff();
        long now = NOW;

        boolean[] expected = { false, false, true, false, false };
        for (boolean expectedCrossing : expected) {
            Failure failure = fail(backoff, now);
            assertThat(failure.crossedUndefThreshold(), is(expectedCrossing));
            now += failure.delaySeconds() * 1000L;
        }
    }

    @Test
    public void testPollWaitsUntilTheDelayElapsed() {
        NotificationPollBackoff backoff = new NotificationPollBackoff();

        assertThat(backoff.shouldSkip(NOW), is(false));
        assertThat(backoff.isDue(NOW), is(false));

        fail(backoff, NOW);

        assertThat(backoff.shouldSkip(NOW), is(true));
        assertThat(backoff.shouldSkip(NOW + 299_999L), is(true));
        assertThat(backoff.isDue(NOW + 299_999L), is(false));
        assertThat(backoff.shouldSkip(NOW + 300_000L), is(false));
        assertThat(backoff.isDue(NOW + 300_000L), is(true));

        assertThat(backoff.tryStart(NOW + 299_999L).outcome(), is(Start.Outcome.BACKING_OFF));
    }

    @Test
    public void testEveryFailureSchedulesAFiniteRetry() {
        NotificationPollBackoff backoff = new NotificationPollBackoff();
        long now = NOW;

        for (int attempt = 0; attempt < 8; attempt++) {
            Failure failure = fail(backoff, now);
            assertThat(failure.delaySeconds(), lessThanOrEqualTo(NotificationPollBackoff.MAX_INTERVAL_SECONDS));
            assertThat(backoff.shouldSkip(now), is(true));
            now += failure.delaySeconds() * 1000L;
            assertThat(backoff.isDue(now), is(true));
        }

        assertThat(succeed(backoff, now).endedStreak(), is(true));
        assertThat(backoff.shouldSkip(now), is(false));
        assertThat(backoff.isDue(Long.MAX_VALUE), is(false));
    }

    @Test
    public void testSuccessResetsCounterAndDelay() {
        NotificationPollBackoff backoff = new NotificationPollBackoff();
        long now = NOW;

        now += fail(backoff, now).delaySeconds() * 1000L;
        now += fail(backoff, now).delaySeconds() * 1000L;
        now += fail(backoff, now).delaySeconds() * 1000L;

        assertThat(succeed(backoff, now).endedStreak(), is(true));
        assertThat(backoff.shouldSkip(now), is(false));

        Failure afterReset = fail(backoff, now);
        assertThat(afterReset.delaySeconds(), is(300));
        assertThat(afterReset.firstOfStreak(), is(true));
        assertThat(afterReset.publishUndef(), is(false));
        assertThat(afterReset.crossedUndefThreshold(), is(false));
    }

    @Test
    public void testSuccessWithoutPrecedingFailureDoesNotReportRecovery() {
        NotificationPollBackoff backoff = new NotificationPollBackoff();

        assertThat(succeed(backoff, NOW).endedStreak(), is(false));
        assertThat(succeed(backoff, NOW).endedStreak(), is(false));
    }

    @Test
    public void testResetClearsTheBackoffSilently() {
        NotificationPollBackoff backoff = new NotificationPollBackoff();
        long now = NOW;

        now += fail(backoff, now).delaySeconds() * 1000L;
        now += fail(backoff, now).delaySeconds() * 1000L;
        backoff.reset();

        assertThat(backoff.shouldSkip(now), is(false));
        assertThat(backoff.isDue(now), is(false));
        assertThat(succeed(backoff, now).endedStreak(), is(false));
        assertThat(fail(backoff, now).delaySeconds(), is(300));
    }

    @Test
    public void testSecondCallerDuringFlightIsRefused() {
        NotificationPollBackoff backoff = new NotificationPollBackoff();

        long token = start(backoff, NOW);

        assertThat(backoff.tryStart(NOW).outcome(), is(Start.Outcome.ALREADY_RUNNING));
        assertThat(backoff.shouldSkip(NOW), is(true));
        assertThat(backoff.isDue(NOW), is(false));

        assertThat(backoff.onSuccess(token), is(notNullValue()));
        assertThat(backoff.tryStart(NOW).outcome(), is(Start.Outcome.STARTED));
    }

    @Test
    public void testFailureReleasesTheFlightForTheRetry() {
        NotificationPollBackoff backoff = new NotificationPollBackoff();

        long token = start(backoff, NOW);
        Failure failure = Objects.requireNonNull(backoff.onFailure(token, NOW));

        assertThat(backoff.tryStart(NOW).outcome(), is(Start.Outcome.BACKING_OFF));
        assertThat(backoff.tryStart(NOW + failure.delaySeconds() * 1000L).outcome(), is(Start.Outcome.STARTED));
    }

    @Test
    public void testResetReleasesTheRunningAttemptImmediately() {
        NotificationPollBackoff backoff = new NotificationPollBackoff();

        start(backoff, NOW);
        backoff.reset();

        assertThat(backoff.tryStart(NOW).outcome(), is(Start.Outcome.STARTED));
    }

    @Test
    public void testLateFailureOfReplacedAttemptIsDiscarded() {
        NotificationPollBackoff backoff = new NotificationPollBackoff();

        long oldToken = start(backoff, NOW);
        backoff.reset();
        long newToken = start(backoff, NOW);

        assertThat(backoff.onFailure(oldToken, NOW), is(nullValue()));
        assertThat(backoff.tryStart(NOW).outcome(), is(Start.Outcome.ALREADY_RUNNING));

        Failure failure = Objects.requireNonNull(backoff.onFailure(newToken, NOW));
        assertThat(failure.firstOfStreak(), is(true));
        assertThat(failure.delaySeconds(), is(300));
    }

    @Test
    public void testLateSuccessOfReplacedAttemptIsDiscarded() {
        NotificationPollBackoff backoff = new NotificationPollBackoff();

        long oldToken = start(backoff, NOW);
        backoff.reset();
        fail(backoff, NOW);

        assertThat(backoff.onSuccess(oldToken), is(nullValue()));
        assertThat(backoff.shouldSkip(NOW), is(true));
        assertThat(backoff.tryStart(NOW).outcome(), is(Start.Outcome.BACKING_OFF));
    }

    @Test
    public void testAbortReleasesWithoutRecordingAResult() {
        NotificationPollBackoff backoff = new NotificationPollBackoff();

        long token = start(backoff, NOW);
        backoff.abort(token);

        assertThat(backoff.shouldSkip(NOW), is(false));
        assertThat(fail(backoff, NOW).firstOfStreak(), is(true));
    }

    @Test
    public void testStaleAbortDoesNotReleaseTheNextAttempt() {
        NotificationPollBackoff backoff = new NotificationPollBackoff();

        long oldToken = start(backoff, NOW);
        assertThat(backoff.onSuccess(oldToken), is(notNullValue()));
        start(backoff, NOW);

        assertThat(backoff.abort(oldToken), is(false));

        assertThat(backoff.tryStart(NOW).outcome(), is(Start.Outcome.ALREADY_RUNNING));
    }

    @Test
    public void testRefusedTriggerIsHandedToTheFinishingSuccess() {
        NotificationPollBackoff backoff = new NotificationPollBackoff();

        long token = start(backoff, NOW);
        assertThat(backoff.tryStart(NOW).outcome(), is(Start.Outcome.ALREADY_RUNNING));

        Success success = Objects.requireNonNull(backoff.onSuccess(token));
        assertThat(success.pollAgain(), is(true));

        assertThat(succeed(backoff, NOW).pollAgain(), is(false));
    }

    @Test
    public void testDoubleRefusalNeedsOnlyOneCatchUp() {
        NotificationPollBackoff backoff = new NotificationPollBackoff();

        long token = start(backoff, NOW);
        assertThat(backoff.tryStart(NOW).outcome(), is(Start.Outcome.ALREADY_RUNNING));
        assertThat(backoff.tryStart(NOW).outcome(), is(Start.Outcome.ALREADY_RUNNING));

        assertThat(Objects.requireNonNull(backoff.onSuccess(token)).pollAgain(), is(true));
        assertThat(succeed(backoff, NOW).pollAgain(), is(false));
    }

    @Test
    public void testRefusedTriggerExpiresOnFailure() {
        NotificationPollBackoff backoff = new NotificationPollBackoff();

        long token = start(backoff, NOW);
        assertThat(backoff.tryStart(NOW).outcome(), is(Start.Outcome.ALREADY_RUNNING));

        Failure failure = Objects.requireNonNull(backoff.onFailure(token, NOW));

        assertThat(backoff.tryStart(NOW + failure.delaySeconds() * 1000L - 1).outcome(), is(Start.Outcome.BACKING_OFF));
        assertThat(succeed(backoff, NOW + failure.delaySeconds() * 1000L).pollAgain(), is(false));
    }

    @Test
    public void testResetDiscardsARefusedTrigger() {
        NotificationPollBackoff backoff = new NotificationPollBackoff();

        start(backoff, NOW);
        assertThat(backoff.tryStart(NOW).outcome(), is(Start.Outcome.ALREADY_RUNNING));
        backoff.reset();

        assertThat(succeed(backoff, NOW).pollAgain(), is(false));
    }

    @Test
    public void testAbortHandsBackTheRefusedTrigger() {
        NotificationPollBackoff backoff = new NotificationPollBackoff();

        long token = start(backoff, NOW);
        assertThat(backoff.abort(token), is(false));

        token = start(backoff, NOW);
        assertThat(backoff.tryStart(NOW).outcome(), is(Start.Outcome.ALREADY_RUNNING));
        assertThat(backoff.abort(token), is(true));
        assertThat(backoff.abort(token), is(false));
        assertThat(succeed(backoff, NOW).pollAgain(), is(false));
    }
}
