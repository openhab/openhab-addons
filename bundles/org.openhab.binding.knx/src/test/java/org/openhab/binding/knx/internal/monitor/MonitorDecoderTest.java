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
package org.openhab.binding.knx.internal.monitor;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Type;

/**
 * Checks that the monitor decodes like a channel and falls back to raw bytes instead of logging.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class MonitorDecoderTest {

    @Test
    void aTemperatureDecodesWithItsUnit() {
        Type value = MonitorDecoder.decode("9.001", new byte[] { 0x0c, 0x33 }, DecimalType.class);

        assertEquals(new ValueText("21.5", "°C"), MonitorDecoder.text(assertInstanceOf(QuantityType.class, value)));
    }

    @Test
    void aSwitchDecodesAsTheChannelWould() {
        Type value = MonitorDecoder.decode("1.001", new byte[] { 1 }, OnOffType.class);

        assertEquals(OnOffType.ON, value);
        assertEquals(new ValueText("ON", null), MonitorDecoder.text(OnOffType.ON));
    }

    @Test
    void thePreferredTypeOfTheChannelCounts() {
        assertEquals(PercentType.HUNDRED,
                MonitorDecoder.decode("5.001", new byte[] { (byte) 0xff }, PercentType.class));
    }

    @Test
    void aPayloadTooShortForTheTypeIsNotDecoded() {
        assertNull(MonitorDecoder.decode("9.001", new byte[] { 0x0c }, DecimalType.class));
    }

    @Test
    void aPayloadTooLongForTheTypeIsNotDecoded() {
        assertNull(MonitorDecoder.decode("9.001", new byte[] { 0x0c, 0x1a, 0x00 }, DecimalType.class));
        assertNull(MonitorDecoder.decode("1.001", new byte[] { 0x01, 0x00 }, OnOffType.class));
    }

    @Test
    void anUnknownTypeIsNotDecoded() {
        assertNull(MonitorDecoder.decode("999.999", new byte[] { 0x01 }, DecimalType.class));
        assertNull(MonitorDecoder.decode("nonsense", new byte[] { 0x01 }, DecimalType.class));
    }

    @Test
    void onlyTypesTheBindingDecodesAreSupported() {
        assertTrue(MonitorDecoder.isSupported("9.001"));
        assertTrue(MonitorDecoder.isSupported("1"));
        assertFalse(MonitorDecoder.isSupported("999.999"));
        assertFalse(MonitorDecoder.isSupported("0.001"));
        assertFalse(MonitorDecoder.isSupported(""));
    }

    @Test
    void anEmptyPayloadIsNotDecoded() {
        assertNull(MonitorDecoder.decode("1.001", new byte[0], OnOffType.class));
    }

    @Test
    void textOfVariableLengthIsDecoded() {
        byte[] text = "Kitchen\0".getBytes(StandardCharsets.UTF_8);

        assertEquals(new StringType("Kitchen"), MonitorDecoder.decode("28.001", text, StringType.class));
    }

    @Test
    void textOfFixedLengthNeedsAllFourteenBytes() {
        byte[] fourteen = new byte[14];
        fourteen[0] = 'A';

        assertEquals(new StringType("A"), MonitorDecoder.decode("16.000", fourteen, StringType.class));
        assertNull(MonitorDecoder.decode("16.000", new byte[] { 'A' }, StringType.class));
    }
}
