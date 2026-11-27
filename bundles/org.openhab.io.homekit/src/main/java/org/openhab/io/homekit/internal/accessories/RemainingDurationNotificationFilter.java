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
package org.openhab.io.homekit.internal.accessories;

import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * HomeKit controllers count down locally, so only countdown corrections and completion need notifications.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
final class RemainingDurationNotificationFilter {
    private final LongSupplier nanoTime;
    private int lastNotifiedDuration;
    private long lastNotificationTime;

    RemainingDurationNotificationFilter(int initialDuration, LongSupplier nanoTime) {
        this.nanoTime = nanoTime;
        lastNotifiedDuration = initialDuration;
        lastNotificationTime = nanoTime.getAsLong();
    }

    synchronized boolean shouldNotify(int remainingDuration) {
        if (remainingDuration == 0 && lastNotifiedDuration == 0) {
            return false;
        }

        long now = nanoTime.getAsLong();
        long countdownNanos = TimeUnit.SECONDS.toNanos((long) lastNotifiedDuration - remainingDuration);
        long elapsedNanos = now - lastNotificationTime;
        // Whole-second duration values can differ by less than a second due to sampling and rounding.
        if (remainingDuration == 0 || remainingDuration > lastNotifiedDuration
                || Math.abs(countdownNanos - elapsedNanos) >= TimeUnit.SECONDS.toNanos(1)) {
            lastNotifiedDuration = remainingDuration;
            lastNotificationTime = now;
            return true;
        }
        return false;
    }
}
