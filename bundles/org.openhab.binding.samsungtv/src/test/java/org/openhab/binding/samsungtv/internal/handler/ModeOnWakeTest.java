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

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ModeOnWake}
 *
 * @author Nick Waterton - Initial contribution
 */
@NonNullByDefault
public class ModeOnWakeTest {

    private final AtomicLong now = new AtomicLong(1000);
    private final ModeOnWake modeOnWake = new ModeOnWake(now::get);

    @Test
    public void requestsCommandedModeOnce() {
        modeOnWake.set(true);
        assertTrue(modeOnWake.take(false));
        assertFalse(modeOnWake.take(false));
    }

    @Test
    public void nothingToRequestInCommandedMode() {
        modeOnWake.set(false);
        assertFalse(modeOnWake.take(false));
    }

    @Test
    public void offBeforeFirstReportCancels() {
        modeOnWake.set(true);
        modeOnWake.clear();
        assertFalse(modeOnWake.take(false));
    }

    @Test
    public void expiresAfterAMinute() {
        modeOnWake.set(true);
        now.addAndGet(60000);
        assertFalse(modeOnWake.take(false));
    }

    @Test
    public void nothingPendingWithoutCommand() {
        assertFalse(modeOnWake.take(true));
        assertFalse(modeOnWake.take(false));
    }
}
