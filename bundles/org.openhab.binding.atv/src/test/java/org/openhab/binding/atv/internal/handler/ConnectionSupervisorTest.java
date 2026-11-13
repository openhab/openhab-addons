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
package org.openhab.binding.atv.internal.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.atv.internal.client.dto.PowerState;
import org.openhab.binding.atv.internal.handler.ConnectionSupervisor.Plan;
import org.openhab.binding.atv.internal.handler.ConnectionSupervisor.Retry;
import org.openhab.binding.atv.internal.handler.ConnectionSupervisor.State;
import org.openhab.binding.atv.internal.handler.ConnectionSupervisor.Status;

/**
 * Tests for {@link ConnectionSupervisor}. The scenarios follow logs from an Apple TV that drops off the
 * network in standby.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class ConnectionSupervisorTest {

    private final MutableClock clock = new MutableClock();
    private final ConnectionSupervisor supervisor = new ConnectionSupervisor("test", clock);

    @Test
    public void testPowerOffThenDropMeansAsleep() {
        supervisor.onConnected(PowerState.On);
        supervisor.onPower(PowerState.Off);
        clock.advance(Duration.ofSeconds(35));

        Retry retry = supervisor.onConnectionLost();

        assertThat(supervisor.state(), is(State.ASLEEP));
        assertThat(retry, is(new Retry(Status.ASLEEP, ConnectionSupervisor.ASLEEP_CHECK_SECONDS)));
        assertThat(supervisor.onFound(false), is(Plan.CHECK));
    }

    @Test
    public void testDropWhileOnIsAFailureWithFastRetry() {
        supervisor.onConnected(PowerState.On);

        Retry retry = supervisor.onConnectionLost();

        assertThat(supervisor.state(), is(State.DISCONNECTED));
        assertThat(retry, is(new Retry(Status.OFFLINE, 3)));
        assertThat(supervisor.onFound(false), is(Plan.CONNECT));
    }

    @Test
    public void testUnknownPowerDoesNotHideAnEarlierOff() {
        supervisor.onConnected(PowerState.Off);
        supervisor.onPower(PowerState.Unknown);

        supervisor.onConnectionLost();

        assertThat(supervisor.state(), is(State.ASLEEP));
    }

    @Test
    public void testOffFromAPreviousConnectionDoesNotCarryOver() {
        supervisor.onConnected(PowerState.Off);
        supervisor.onConnectionLost();
        supervisor.onConnected(PowerState.Unknown);

        supervisor.onConnectionLost();

        assertThat(supervisor.state(), is(State.DISCONNECTED));
    }

    @Test
    public void testDropWithoutOffThenCheckReadsOffEndsAsleep() {
        supervisor.onConnected(PowerState.On);
        supervisor.onConnectionLost();
        // The reconnect lands in a reachable window and the device reports it is asleep.
        assertThat(supervisor.onFound(false), is(Plan.CONNECT));
        supervisor.onConnected(PowerState.Off);

        supervisor.onConnectionLost();

        assertThat(supervisor.state(), is(State.ASLEEP));
    }

    @Test
    public void testStaysAsleepThroughReachabilityGaps() {
        fallAsleep();
        // An hour of 20 s checks: reachable for a minute, then nothing for three minutes, over and over.
        for (int cycle = 0; cycle < 15; cycle++) {
            for (int i = 0; i < 3; i++) {
                assertThat(check(Reachability.ANSWERED, PowerState.Off), is(Plan.WAIT));
                assertThat(supervisor.waitRetry().status(), is(Status.ASLEEP));
                clock.advance(Duration.ofSeconds(20));
            }
            for (int i = 0; i < 9; i++) {
                assertThat(check(Reachability.NO_ANSWER, PowerState.Unknown), is(Plan.WAIT));
                assertThat(supervisor.waitRetry().status(), is(Status.ASLEEP));
                clock.advance(Duration.ofSeconds(20));
            }
        }
        assertThat(supervisor.state(), is(State.ASLEEP));
    }

    @Test
    public void testRefusedConnectionCountsAsReachable() {
        fallAsleep();
        for (int i = 0; i < 60; i++) {
            check(Reachability.HOST_UP, PowerState.Unknown);
            assertThat(supervisor.waitRetry().status(), is(Status.ASLEEP));
            clock.advance(Duration.ofSeconds(20));
        }
        assertThat(supervisor.state(), is(State.ASLEEP));
    }

    @Test
    public void testAsleepDeviceThatStopsAnsweringGoesOfflineAfterTenMinutes() {
        fallAsleep();
        clock.advance(ConnectionSupervisor.GONE_AFTER.minusSeconds(1));
        check(Reachability.NO_ANSWER, PowerState.Unknown);
        assertThat(supervisor.waitRetry().status(), is(Status.ASLEEP));

        clock.advance(Duration.ofSeconds(1));
        check(Reachability.NO_ANSWER, PowerState.Unknown);
        Retry retry = supervisor.waitRetry();

        assertThat(retry.status(), is(Status.OFFLINE));
        assertThat(supervisor.state(), is(State.DISCONNECTED));
        // Gone devices get full connects, not asleep checks.
        assertThat(supervisor.onFound(false), is(Plan.CONNECT));
    }

    @Test
    public void testScanResultsDoNotCountAsReachable() {
        fallAsleep();
        // The mDNS cache keeps finding the device, but nothing connects.
        for (int i = 0; i < 31; i++) {
            assertThat(supervisor.onFound(false), is(Plan.CHECK));
            check(Reachability.NO_ANSWER, PowerState.Unknown);
            supervisor.waitRetry();
            clock.advance(Duration.ofSeconds(20));
        }
        assertThat(supervisor.state(), is(State.DISCONNECTED));
    }

    @Test
    public void testNotFoundWhileAsleepStaysAsleepUntilGone() {
        fallAsleep();
        assertThat(supervisor.onFailed(Reachability.NO_ANSWER).status(), is(Status.ASLEEP));
        clock.advance(ConnectionSupervisor.GONE_AFTER);
        assertThat(supervisor.onFailed(Reachability.NO_ANSWER).status(), is(Status.OFFLINE));
    }

    @Test
    public void testCheckThatReadsOnConnects() {
        fallAsleep();
        assertThat(check(Reachability.ANSWERED, PowerState.On), is(Plan.CONNECT));
    }

    @Test
    public void testCheckWithoutAPowerReadingConnects() {
        fallAsleep();
        assertThat(check(Reachability.ANSWERED, PowerState.Unknown), is(Plan.CONNECT));
    }

    @Test
    public void testFailedFullConnectAfterCheckStaysAsleep() {
        fallAsleep();
        assertThat(check(Reachability.ANSWERED, PowerState.On), is(Plan.CONNECT));
        Retry retry = supervisor.onFailed(Reachability.NO_ANSWER);
        assertThat(retry.status(), is(Status.ASLEEP));
        assertThat(supervisor.state(), is(State.ASLEEP));
    }

    @Test
    public void testAwakeDeviceWithBrokenCompanionIsNotAsleep() {
        // Connection drops while On, and every reconnect fails although the device answers.
        supervisor.onConnected(PowerState.On);
        supervisor.onConnectionLost();
        for (int i = 0; i < 10; i++) {
            assertThat(supervisor.onFound(false), is(Plan.CONNECT));
            assertThat(supervisor.onFailed(Reachability.ANSWERED).status(), is(Status.OFFLINE));
        }
        assertThat(supervisor.state(), is(State.DISCONNECTED));
    }

    @Test
    public void testFailuresBackOff() {
        assertThat(supervisor.onFailed(Reachability.NO_ANSWER).delaySeconds(), is(3L));
        assertThat(supervisor.onFailed(Reachability.NO_ANSWER).delaySeconds(), is(10L));
        assertThat(supervisor.onFailed(Reachability.NO_ANSWER).delaySeconds(), is(30L));
        assertThat(supervisor.onFailed(Reachability.NO_ANSWER).delaySeconds(), is(60L));
        assertThat(supervisor.onFailed(Reachability.NO_ANSWER).delaySeconds(), is(60L));

        supervisor.onConnected(PowerState.On);
        assertThat(supervisor.onConnectionLost().delaySeconds(), is(3L));
    }

    @Test
    public void testSleepProxyMeansAsleepWithoutChecking() {
        assertThat(supervisor.onFound(true), is(Plan.WAIT));
        assertThat(supervisor.state(), is(State.ASLEEP));
        assertThat(supervisor.waitRetry().status(), is(Status.ASLEEP));
        // The proxy keeps answering, so the device is never reported gone.
        for (int i = 0; i < 60; i++) {
            clock.advance(Duration.ofSeconds(20));
            assertThat(supervisor.onFound(true), is(Plan.WAIT));
            assertThat(supervisor.waitRetry().status(), is(Status.ASLEEP));
        }
    }

    @Test
    public void testWakeConnectsFastEvenBehindASleepProxy() {
        fallAsleep();
        supervisor.requestWake();

        assertThat(supervisor.isWaking(), is(true));
        assertThat(supervisor.onFound(true), is(Plan.CONNECT));
        assertThat(supervisor.onFailed(Reachability.NO_ANSWER).delaySeconds(),
                is(ConnectionSupervisor.WAKE_RETRY_SECONDS));
        assertThat(supervisor.waitRetry().delaySeconds(), is(ConnectionSupervisor.WAKE_RETRY_SECONDS));
    }

    @Test
    public void testWakeIsSentOnceConnected() {
        fallAsleep();
        supervisor.requestWake();
        clock.advance(Duration.ofMinutes(2));
        supervisor.onConnected(PowerState.Off);

        assertThat(supervisor.consumeWake(), is(true));
        assertThat(supervisor.consumeWake(), is(false));
        assertThat(supervisor.isWaking(), is(false));
    }

    @Test
    public void testWakeExpires() {
        fallAsleep();
        supervisor.requestWake();
        clock.advance(ConnectionSupervisor.WAKE_TIMEOUT);

        assertThat(supervisor.isWaking(), is(false));
        assertThat(supervisor.onFound(false), is(Plan.CHECK));
        assertThat(supervisor.waitRetry().delaySeconds(), is(ConnectionSupervisor.ASLEEP_CHECK_SECONDS));
        supervisor.onConnected(PowerState.On);
        assertThat(supervisor.consumeWake(), is(false));
    }

    @Test
    public void testAnnouncementsAreDebounced() {
        fallAsleep();
        assertThat(supervisor.onAnnouncement(), is(true));
        clock.advance(Duration.ofSeconds(4));
        assertThat(supervisor.onAnnouncement(), is(false));
        clock.advance(ConnectionSupervisor.ANNOUNCEMENT_DEBOUNCE);
        assertThat(supervisor.onAnnouncement(), is(true));
    }

    @Test
    public void testAnnouncementsIgnoredWhileConnected() {
        supervisor.onConnected(PowerState.On);
        assertThat(supervisor.onAnnouncement(), is(false));
    }

    private void fallAsleep() {
        supervisor.onConnected(PowerState.Off);
        supervisor.onConnectionLost();
        assertThat(supervisor.state(), is(State.ASLEEP));
    }

    private Plan check(Reachability reach, PowerState power) {
        assertThat(supervisor.onFound(false), is(Plan.CHECK));
        return supervisor.onChecked(reach, power);
    }

    @NonNullByDefault({})
    private static final class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-07-30T12:00:00Z");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
