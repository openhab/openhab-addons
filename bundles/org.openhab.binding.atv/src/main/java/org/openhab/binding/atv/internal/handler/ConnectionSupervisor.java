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

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.dto.PowerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decides how to reach a device and what its Thing should show while it is not connected. It does no I/O:
 * the handler reports what happened and carries out the returned decision.
 *
 * <p>
 * Some Apple TVs drop every connection and fall off the network for minutes at a time while in standby,
 * coming back only in short windows. A device counts as asleep only when it said so itself: it reported
 * power Off before its connection dropped, a check during a reachable window read Off, or a Bonjour Sleep
 * Proxy is answering for it. Whether the device is on the network is judged only by real connection
 * results, never by mDNS, whose cache keeps listing a device that stopped answering.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
final class ConnectionSupervisor {

    enum State {
        /** Not connected and not known to be asleep. Full connects are retried with backoff. */
        DISCONNECTED,
        CONNECTED,
        /** The device is in standby and does not hold a connection. It is checked periodically. */
        ASLEEP
    }

    /** What to do with a device the scan found. */
    enum Plan {
        CONNECT,
        /** Ask the device over a short Companion connection whether it is still asleep. */
        CHECK,
        /** Leave the device alone until the next attempt. */
        WAIT
    }

    /** What the Thing shows while not connected. */
    enum Status {
        ASLEEP,
        OFFLINE
    }

    record Retry(Status status, long delaySeconds) {
    }

    static final long ASLEEP_CHECK_SECONDS = 20;
    // Fast enough to land in a sleeping device's brief reachable windows while a wake is pending.
    static final long WAKE_RETRY_SECONDS = 5;
    // Longer than the longest gap between reachable windows seen on a sleeping device (about 3 minutes).
    static final Duration GONE_AFTER = Duration.ofMinutes(10);
    // Gives up a wake on a device that is really off, after enough reachable windows have passed.
    static final Duration WAKE_TIMEOUT = Duration.ofMinutes(5);
    // A sleeping device announces itself every few seconds. Longer than the asleep check and close to the
    // longest backoff, so announcements cannot make attempts much more frequent than either.
    static final Duration ANNOUNCEMENT_DEBOUNCE = Duration.ofSeconds(30);
    private static final long[] BACKOFF_SECONDS = { 3, 10, 30, 60 };

    private final Logger logger = LoggerFactory.getLogger(ConnectionSupervisor.class);
    private final String deviceLabel;
    private final Clock clock;

    private State state = State.DISCONNECTED;
    // Power last reported on the current connection. Unknown readings are ignored so they cannot hide an Off.
    private PowerState reportedPower = PowerState.Unknown;
    private Instant lastReachable;
    private int failures;
    private @Nullable Instant wakeDeadline;
    private Instant lastAnnouncementCheck = Instant.EPOCH;

    /**
     * Creates a supervisor for one device.
     *
     * @param deviceLabel names the device in log messages
     * @param clock source of the current time
     */
    ConnectionSupervisor(String deviceLabel, Clock clock) {
        this.deviceLabel = deviceLabel;
        this.clock = clock;
        this.lastReachable = clock.instant();
    }

    synchronized State state() {
        return state;
    }

    /** Whether a user wake request is still active. Expires after {@link #WAKE_TIMEOUT}. */
    synchronized boolean isWaking() {
        Instant deadline = wakeDeadline;
        if (deadline == null) {
            return false;
        }
        if (!clock.instant().isBefore(deadline)) {
            logger.debug("{}: wake request expired without a connection", deviceLabel);
            wakeDeadline = null;
            return false;
        }
        return true;
    }

    /** Records a user request to turn the device on while it is not connected. */
    synchronized void requestWake() {
        logger.debug("{}: wake requested", deviceLabel);
        wakeDeadline = clock.instant().plus(WAKE_TIMEOUT);
    }

    /**
     * Clears a pending wake once connected.
     *
     * @return whether a wake was pending, so the caller should send the power-on command
     */
    synchronized boolean consumeWake() {
        boolean waking = isWaking();
        wakeDeadline = null;
        return waking;
    }

    /**
     * Decides what to do once the scan found the device.
     *
     * @param deepSleep whether a Bonjour Sleep Proxy answered for the device
     */
    synchronized Plan onFound(boolean deepSleep) {
        if (isWaking()) {
            return Plan.CONNECT;
        }
        if (deepSleep) {
            // Any connection, including a check, would make the proxy wake the device.
            markReachable();
            enter(State.ASLEEP, "a sleep proxy answers for it");
            return Plan.WAIT;
        }
        return state == State.ASLEEP ? Plan.CHECK : Plan.CONNECT;
    }

    /**
     * Decides what to do after an asleep check.
     *
     * @param reach how the check went. A completed check is {@link Reachability#ANSWERED}.
     * @param power the power state the check read, or Unknown if it failed or the device did not say
     */
    synchronized Plan onChecked(Reachability reach, PowerState power) {
        if (reach != Reachability.NO_ANSWER) {
            markReachable();
        }
        // A check that the device answered without reporting Off (including a Companion failure that is not a
        // network failure) means it may be awake, so try a full connect rather than assuming it still sleeps.
        if (reach == Reachability.ANSWERED && power != PowerState.Off) {
            logger.debug("{}: asleep check read power {}; connecting", deviceLabel, power);
            return Plan.CONNECT;
        }
        return Plan.WAIT;
    }

    /** Status and delay after a {@link Plan#WAIT}. */
    synchronized Retry waitRetry() {
        if (state == State.ASLEEP && isGone()) {
            return disconnected("no answer for " + GONE_AFTER.toMinutes() + " minutes");
        }
        return new Retry(Status.ASLEEP, delay(ASLEEP_CHECK_SECONDS));
    }

    /**
     * Records a failed attempt: the device was not found, or a full connect failed.
     *
     * @param reach what the failure shows about the device
     */
    synchronized Retry onFailed(Reachability reach) {
        if (reach != Reachability.NO_ANSWER) {
            markReachable();
        }
        if (state == State.ASLEEP) {
            return waitRetry();
        }
        return disconnected("connect failed");
    }

    /**
     * Records a successful full connect.
     *
     * @param power the power state known right after connecting
     */
    synchronized void onConnected(PowerState power) {
        reportedPower = PowerState.Unknown;
        onPower(power);
        failures = 0;
        markReachable();
        enter(State.CONNECTED, "connected");
    }

    /** Records a power state reported on the current connection. */
    synchronized void onPower(PowerState power) {
        if (power != PowerState.Unknown) {
            reportedPower = power;
        }
    }

    /** Status and delay after an established connection dropped. */
    synchronized Retry onConnectionLost() {
        markReachable();
        if (reportedPower == PowerState.Off) {
            enter(State.ASLEEP, "connection lost after the device reported power Off");
            return new Retry(Status.ASLEEP, delay(ASLEEP_CHECK_SECONDS));
        }
        failures = 0;
        return disconnected("connection lost");
    }

    /**
     * Decides whether an mDNS announcement from the device should trigger an attempt now. Announcements do
     * not show that the device woke, only that it may be worth asking sooner than the next scheduled attempt.
     */
    synchronized boolean onAnnouncement() {
        if (state == State.CONNECTED) {
            return false;
        }
        Instant now = clock.instant();
        if (now.isBefore(lastAnnouncementCheck.plus(ANNOUNCEMENT_DEBOUNCE))) {
            return false;
        }
        lastAnnouncementCheck = now;
        return true;
    }

    private Retry disconnected(String reason) {
        enter(State.DISCONNECTED, reason);
        long backoff = BACKOFF_SECONDS[Math.min(failures, BACKOFF_SECONDS.length - 1)];
        failures++;
        return new Retry(Status.OFFLINE, delay(backoff));
    }

    private long delay(long normalSeconds) {
        return isWaking() ? Math.min(normalSeconds, WAKE_RETRY_SECONDS) : normalSeconds;
    }

    private boolean isGone() {
        return !clock.instant().isBefore(lastReachable.plus(GONE_AFTER));
    }

    private void markReachable() {
        lastReachable = clock.instant();
    }

    private void enter(State next, String reason) {
        if (state != next) {
            logger.debug("{}: {} -> {} ({})", deviceLabel, state, next, reason);
            state = next;
        }
    }
}
