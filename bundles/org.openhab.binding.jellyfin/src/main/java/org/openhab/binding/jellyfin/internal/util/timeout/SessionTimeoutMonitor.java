/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.jellyfin.internal.util.timeout;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Monitors session activity and fires a callback when no session update has been received
 * within the configured timeout period.
 *
 * <p>
 * The monitor tracks the timestamp of the last recorded activity. When the periodic check
 * runs, it invokes the {@code onTimeout} callback if:
 * <ol>
 * <li>the {@code hasSession} supplier returns {@code true}, and</li>
 * <li>no activity has been recorded within {@code timeoutMs} milliseconds.</li>
 * </ol>
 *
 * <p>
 * Usage:
 *
 * <pre>{@code
 * var monitor = new SessionTimeoutMonitor(60_000);
 * monitor.start(scheduler, "device-id", () -> currentSession != null, this::onSessionTimedOut);
 * // on each session update:
 * monitor.recordActivity();
 * // on dispose:
 * monitor.stop();
 * }</pre>
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public final class SessionTimeoutMonitor {

    private final Logger logger = LoggerFactory.getLogger(SessionTimeoutMonitor.class);

    private final long timeoutMs;

    /** Millisecond timestamp of the last recorded activity; 0 means no activity yet. */
    private volatile long lastActivityTimestamp = 0;

    @Nullable
    private ScheduledFuture<?> monitorTask;

    /**
     * Creates a new monitor with the given timeout threshold.
     *
     * @param timeoutMs session timeout in milliseconds
     */
    public SessionTimeoutMonitor(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    /**
     * Starts the periodic timeout check.
     *
     * <p>
     * The check runs with an initial delay equal to {@code timeoutMs} and then repeats every
     * {@code timeoutMs / 2} milliseconds (so at most two check intervals pass before timeout
     * is detected).
     *
     * @param scheduler executor used to schedule the check
     * @param deviceId device identifier used in log messages
     * @param hasSession supplier that returns {@code true} when a session is currently active
     * @param onTimeout callback invoked when a session timeout is detected
     */
    public void start(ScheduledExecutorService scheduler, String deviceId, BooleanSupplier hasSession,
            Runnable onTimeout) {
        monitorTask = scheduler.scheduleWithFixedDelay(() -> check(deviceId, hasSession, onTimeout), timeoutMs,
                timeoutMs / 2, TimeUnit.MILLISECONDS);
        logger.debug("Session timeout monitor started for device: {} (timeout={}ms)", deviceId, timeoutMs);
    }

    /**
     * Stops the periodic timeout check. Safe to call even if not started.
     */
    public void stop() {
        ScheduledFuture<?> task = monitorTask;
        if (task != null && !task.isDone()) {
            task.cancel(true);
            monitorTask = null;
        }
    }

    /**
     * Records a session activity event, resetting the timeout countdown.
     * Should be called every time a session update is received from the server.
     */
    public void recordActivity() {
        lastActivityTimestamp = System.currentTimeMillis();
    }

    /**
     * Resets the activity timestamp to zero, as if no activity has ever been recorded.
     * Useful after a session has been explicitly cleared.
     */
    public void resetActivity() {
        lastActivityTimestamp = 0;
    }

    /**
     * Returns {@code true} if activity has been recorded and the timeout threshold has elapsed
     * since the last activity.
     *
     * @return {@code true} if the session has timed out
     */
    public boolean isTimedOut() {
        long last = lastActivityTimestamp;
        return last > 0 && (System.currentTimeMillis() - last) > timeoutMs;
    }

    private void check(String deviceId, BooleanSupplier hasSession, Runnable onTimeout) {
        if (!hasSession.getAsBoolean()) {
            logger.trace("[SESSION] No active session for device {} (timeout check skipped)", deviceId);
            return;
        }

        long last = lastActivityTimestamp;
        if (last == 0) {
            return;
        }
        long elapsed = System.currentTimeMillis() - last;

        logger.trace("[SESSION] Timeout check for device {}: {}s since last update (threshold: {}s)", deviceId,
                elapsed / 1000, timeoutMs / 1000);

        if (elapsed > timeoutMs) {
            logger.warn("[SESSION] Session timeout detected for device {} ({}s without update, threshold: {}s)",
                    deviceId, elapsed / 1000, timeoutMs / 1000);
            onTimeout.run();
        } else if (elapsed > (timeoutMs / 2)) {
            logger.debug("[SESSION] Device {} approaching timeout: {}s since last update", deviceId, elapsed / 1000);
        }
    }
}
