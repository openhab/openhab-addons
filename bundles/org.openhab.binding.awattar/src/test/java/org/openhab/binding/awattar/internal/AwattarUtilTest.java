/**
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
package org.openhab.binding.awattar.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.test.java.JavaTest;

@NonNullByDefault
class AwattarUtilTest extends JavaTest {
    private static final ZoneId UTC = ZoneId.of("UTC");

    @Test
    void returnsDelayUntilNextMinute() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-03T12:34:45.123Z"), UTC);

        assertEquals(14_877, AwattarUtil.getMillisToNextMinute(1, clock));
    }

    @Test
    void schedulesNextMinuteWhenAlreadyOnMinuteBoundary() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-03T12:34:00Z"), UTC);

        assertEquals(60_000, AwattarUtil.getMillisToNextMinute(1, clock));
    }
}
