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
package org.openhab.binding.samsungtv.internal.handler;

import java.util.function.LongSupplier;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ModeOnWake} remembers the mode a power or artMode ON command asked for while the TV wakes. A Frame TV
 * wakes in the mode it was switched off from, so the commanded mode is requested if the first art mode report within
 * the next minute differs.
 *
 * @author Nick Waterton - Initial contribution
 */
@NonNullByDefault
class ModeOnWake {

    private static final long WINDOW_MS = 60000;

    private final LongSupplier clock;
    private long until = 0;
    private boolean artMode;

    ModeOnWake() {
        this(System::currentTimeMillis);
    }

    ModeOnWake(LongSupplier clock) {
        this.clock = clock;
    }

    /**
     * @param artMode true for an artMode ON command, false for a power ON command
     */
    synchronized void set(boolean artMode) {
        this.artMode = artMode;
        until = clock.getAsLong() + WINDOW_MS;
    }

    synchronized void clear() {
        until = 0;
    }

    /**
     * Only the first art mode report after a wake counts.
     *
     * @param reportedArtMode the art mode the TV reports
     * @return true if a wake is pending and the TV is not in the commanded mode
     */
    synchronized boolean take(boolean reportedArtMode) {
        if (clock.getAsLong() < until) {
            until = 0;
            return reportedArtMode != artMode;
        }
        return false;
    }
}
