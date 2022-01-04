/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
//import org.openhab.binding.bluetooth.radoneye.internal.RadoneyeDataParser;
//import org.openhab.binding.bluetooth.radoneye.internal.RadoneyeParserException;

/**
 * Tests {@link RadoneyeParserTest}.
 *
 * @author Peter Obel - Initial contribution
 * @author the authors of the airthings bluetooth binding
 */
@NonNullByDefault
public class RadoneyeParserTest {

    @Test
    public void testEmptyData() {
        int[] data = {};
        //assertThrows(RadoneyeParserException.class, () -> RadoneyeDataParser.parseRd200Data(data));
    }

    @Test
    public void testWrongDataLen() throws RadoneyeParserException {
        int[] data = { 1, 55, 51, 0, 122, 0, 61, 0, 119, 9, 11, 194, 169, 2, 46, 0, 0 };
        //assertThrows(RadoneyeParserException.class, () -> RadoneyeDataParser.parseRd200Data(data));
    }

    @Test
    public void testParsingRd200() throws RadoneyeParserException {
        int[] data = { 12, 0, 248, 112, 201, 193, 136, 14, 150, 0, 1, 0, 217, 176, 14, 0, 255, 255, 255, 255 };
        Map<String, Number> result = RadoneyeDataParser.parseRd200Data(data);

        //assertEquals(37.2, result.get(RadoneyeDataParser.HUMIDITY));
        //assertEquals(150, result.get(RadoneyeDataParser.TVOC));
        //assertEquals(16.05, result.get(RadoneyeDataParser.TEMPERATURE));
    }
}
