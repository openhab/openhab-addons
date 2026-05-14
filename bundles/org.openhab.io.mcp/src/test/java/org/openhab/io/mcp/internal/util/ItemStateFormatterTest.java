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
package org.openhab.io.mcp.internal.util;

import static org.junit.jupiter.api.Assertions.*;

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.UnDefType;

/**
 * Tests for {@link ItemStateFormatter}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class ItemStateFormatterTest {

    @Test
    void testFormatStateNull() {
        assertEquals("no value", ItemStateFormatter.formatState(UnDefType.NULL));
    }

    @Test
    void testFormatStateUndef() {
        assertEquals("undefined", ItemStateFormatter.formatState(UnDefType.UNDEF));
    }

    @Test
    void testFormatStateHsb() {
        HSBType hsb = new HSBType("120,50,75");
        String result = ItemStateFormatter.formatState(hsb);
        assertTrue(result.contains("hue: 120"));
        assertTrue(result.contains("saturation: 50%"));
        assertTrue(result.contains("brightness: 75%"));
    }

    @Test
    void testFormatStatePercent() {
        assertEquals("42%", ItemStateFormatter.formatState(new PercentType(42)));
    }

    @Test
    void testFormatStatePercentZero() {
        assertEquals("0%", ItemStateFormatter.formatState(new PercentType(0)));
    }

    @Test
    void testFormatStateDateTime() {
        ZonedDateTime zdt = ZonedDateTime.parse("2025-01-15T10:30:00Z");
        DateTimeType dt = new DateTimeType(zdt);
        String result = ItemStateFormatter.formatState(dt);
        assertTrue(result.contains("2025-01-15"), "Expected ISO date in: " + result);
    }

    @Test
    void testFormatStateOnOff() {
        assertEquals("ON", ItemStateFormatter.formatState(OnOffType.ON));
        assertEquals("OFF", ItemStateFormatter.formatState(OnOffType.OFF));
    }

    @Test
    void testFormatStateDecimal() {
        assertEquals("21.5", ItemStateFormatter.formatState(new DecimalType(21.5)));
    }

    @Test
    void testFormatStateString() {
        assertEquals("hello", ItemStateFormatter.formatState(new StringType("hello")));
    }
}
