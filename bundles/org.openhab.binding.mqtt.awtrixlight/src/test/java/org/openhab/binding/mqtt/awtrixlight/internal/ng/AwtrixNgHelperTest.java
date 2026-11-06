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
package org.openhab.binding.mqtt.awtrixlight.internal.ng;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.HSBType;

/**
 * Test cases for the color conversion of the {@link AwtrixNgHelper}.
 *
 * @author Keith T. Garner - Initial contribution
 */
class AwtrixNgHelperTest {

    @Test
    void toHexUsesUppercaseSixDigits() {
        assertEquals("#FF0000", AwtrixNgHelper.toHex(HSBType.RED));
        assertEquals("#000000", AwtrixNgHelper.toHex(HSBType.BLACK));
        assertEquals("#FFFFFF", AwtrixNgHelper.toHex(HSBType.WHITE));
    }

    @Test
    void hexRoundTrip() {
        HSBType color = AwtrixNgHelper.fromHex("#3366FF");

        assertNotNull(color);
        assertEquals("#3366FF", AwtrixNgHelper.toHex(color));
    }

    @Test
    void fromHexRejectsOtherFormats() {
        assertNull(AwtrixNgHelper.fromHex(null));
        assertNull(AwtrixNgHelper.fromHex(""));
        assertNull(AwtrixNgHelper.fromHex("FF0000"));
        assertNull(AwtrixNgHelper.fromHex("#F00"));
        assertNull(AwtrixNgHelper.fromHex("#GG0000"));
    }

    @Test
    void normalizeColorAcceptsHexColors() {
        assertEquals("#FF0000", AwtrixNgHelper.normalizeColor("#ff0000"));
        assertEquals("#FF0000", AwtrixNgHelper.normalizeColor("FF0000"));
        assertEquals("#F00", AwtrixNgHelper.normalizeColor(" f00 "));
    }

    @Test
    void normalizeColorRejectsOtherInput() {
        assertNull(AwtrixNgHelper.normalizeColor(null));
        assertNull(AwtrixNgHelper.normalizeColor(""));
        assertNull(AwtrixNgHelper.normalizeColor("red"));
        assertNull(AwtrixNgHelper.normalizeColor("#FF00"));
        assertNull(AwtrixNgHelper.normalizeColor("#GG0000"));
        assertNull(AwtrixNgHelper.normalizeColor("[255,0,0]"));
    }

    @Test
    void jsonObjectKeepsTheOrderAndLeavesNullsOut() {
        assertEquals("{\"name\":\"Time\",\"fast\":true}", AwtrixNgHelper.jsonObject("name", "Time", "fast", true));
        assertEquals("{\"disabled\":[\"Battery\"]}",
                AwtrixNgHelper.jsonObject("order", null, "disabled", java.util.List.of("Battery")));
        assertEquals("{}", AwtrixNgHelper.jsonObject());
    }
}
