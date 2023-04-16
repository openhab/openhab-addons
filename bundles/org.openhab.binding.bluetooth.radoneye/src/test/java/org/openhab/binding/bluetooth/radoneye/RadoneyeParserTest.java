/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.radoneye;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bluetooth.radoneye.internal.RadoneyeDataParser;
import org.openhab.binding.bluetooth.radoneye.internal.RadoneyeParserException;

/**
 * Tests {@link RadoneyeParserTest}.
 *
 * @author Peter Obel - Initial contribution
 */
@NonNullByDefault
public class RadoneyeParserTest {

    @Test
    public void testEmptyData() {
        int[] data = {};
        assertThrows(RadoneyeParserException.class, () -> RadoneyeDataParser.parseRd200Data(1, data));
    }

    @Test
    public void testWrongDataLen() throws RadoneyeParserException {
        int[] data = { 1, 55, 51, 0, 122, 0, 61, 0, 119, 9, 11, 194, 169, 2, 46, 0, 0 };
        assertThrows(RadoneyeParserException.class, () -> RadoneyeDataParser.parseRd200Data(1, data));
    }

    @Test
    public void testParsingRd200v1() throws RadoneyeParserException {
        int[] data = { 80, 16, 31, -123, 43, 64, 123, 20, 94, 64, 92, -113, -118, 64, 15, 0, 12, 0, 0, 0 };
        Map<String, Number> result = RadoneyeDataParser.parseRd200Data(1, data);

        assertEquals(99, result.get(RadoneyeDataParser.RADON).intValue());
    }

    @Test
    public void testParsingRd200v2() throws RadoneyeParserException {
        int[] data = { 0xff, 0xff, 0x5b, 0x00, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff };
        Map<String, Number> result = RadoneyeDataParser.parseRd200Data(2, data);

        assertEquals(91, result.get(RadoneyeDataParser.RADON).intValue());
    }
}
