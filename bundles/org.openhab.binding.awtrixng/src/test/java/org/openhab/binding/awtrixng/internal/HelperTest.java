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
package org.openhab.binding.awtrixng.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.HSBType;

/**
 * Test cases for the color conversion of the {@link Helper}.
 *
 * @author Keith T. Garner - Initial contribution
 */
class HelperTest {

    @Test
    void toHexUsesUppercaseSixDigits() {
        assertEquals("#FF0000", Helper.toHex(HSBType.RED));
        assertEquals("#000000", Helper.toHex(HSBType.BLACK));
        assertEquals("#FFFFFF", Helper.toHex(HSBType.WHITE));
    }

    @Test
    void hexRoundTrip() {
        HSBType color = Helper.fromHex("#3366FF");

        assertNotNull(color);
        assertEquals("#3366FF", Helper.toHex(color));
    }

    @Test
    void fromHexRejectsOtherFormats() {
        assertNull(Helper.fromHex(null));
        assertNull(Helper.fromHex(""));
        assertNull(Helper.fromHex("FF0000"));
        assertNull(Helper.fromHex("#F00"));
        assertNull(Helper.fromHex("#GG0000"));
    }

    @Test
    void normalizeColorAcceptsHexColors() {
        assertEquals("#FF0000", Helper.normalizeColor("#ff0000"));
        assertEquals("#FF0000", Helper.normalizeColor("FF0000"));
        assertEquals("#F00", Helper.normalizeColor(" f00 "));
    }

    @Test
    void normalizeColorRejectsOtherInput() {
        assertNull(Helper.normalizeColor(null));
        assertNull(Helper.normalizeColor(""));
        assertNull(Helper.normalizeColor("red"));
        assertNull(Helper.normalizeColor("#FF00"));
        assertNull(Helper.normalizeColor("#GG0000"));
        assertNull(Helper.normalizeColor("[255,0,0]"));
    }

    @Test
    void jsonObjectKeepsTheOrderAndLeavesNullsOut() {
        assertEquals("{\"name\":\"Time\",\"fast\":true}", Helper.jsonObject("name", "Time", "fast", true));
        assertEquals("{\"disabled\":[\"Battery\"]}",
                Helper.jsonObject("order", null, "disabled", java.util.List.of("Battery")));
        assertEquals("{}", Helper.jsonObject());
    }
}
