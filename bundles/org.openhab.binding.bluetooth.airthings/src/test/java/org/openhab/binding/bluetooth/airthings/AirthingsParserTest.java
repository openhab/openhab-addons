/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.airthings;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bluetooth.airthings.internal.AirthingsDataParser;
import org.openhab.binding.bluetooth.airthings.internal.AirthingsParserException;

/**
 * Tests {@link AirthingsParserTest}.
 *
 * @author Pauli Anttila - Initial contribution
 */
@NonNullByDefault
public class AirthingsParserTest {

    @Test
    public void testWrongVersion() {
        int[] data = { 5, 55, 51, 0, 122, 0, 61, 0, 119, 9, 11, 194, 169, 2, 46, 0, 0, 0, 4, 20 };
        assertThrows(AirthingsParserException.class, () -> AirthingsDataParser.parseWavePlusData(data));
    }

    @Test
    public void testEmptyData() {
        int[] data = {};
        assertThrows(AirthingsParserException.class, () -> AirthingsDataParser.parseWavePlusData(data));
    }

    @Test
    public void testWrongDataLen() throws AirthingsParserException {
        int[] data = { 1, 55, 51, 0, 122, 0, 61, 0, 119, 9, 11, 194, 169, 2, 46, 0, 0 };
        assertThrows(AirthingsParserException.class, () -> AirthingsDataParser.parseWavePlusData(data));
    }

    @Test
    public void testParsingPlus() throws AirthingsParserException {
        int[] data = { 1, 55, 51, 0, 122, 0, 61, 0, 119, 9, 11, 194, 169, 2, 46, 0, 0, 0, 4, 20 };
        Map<String, Number> result = AirthingsDataParser.parseWavePlusData(data);

        assertEquals(27.5, result.get(AirthingsDataParser.HUMIDITY));
        assertEquals(681, result.get(AirthingsDataParser.CO2));
        assertEquals(46, result.get(AirthingsDataParser.TVOC));
        assertEquals(24.23, result.get(AirthingsDataParser.TEMPERATURE));
        assertEquals(993.5, result.get(AirthingsDataParser.PRESSURE));
        assertEquals(61, result.get(AirthingsDataParser.RADON_LONG_TERM_AVG));
        assertEquals(122, result.get(AirthingsDataParser.RADON_SHORT_TERM_AVG));
    }

    @Test
    public void testParsingMini() throws AirthingsParserException {
        int[] data = { 12, 0, 248, 112, 201, 193, 136, 14, 150, 0, 1, 0, 217, 176, 14, 0, 255, 255, 255, 255 };
        Map<String, Number> result = AirthingsDataParser.parseWaveMiniData(data);

        assertEquals(37.2, result.get(AirthingsDataParser.HUMIDITY));
        assertEquals(150, result.get(AirthingsDataParser.TVOC));
        assertEquals(16.05, result.get(AirthingsDataParser.TEMPERATURE));
    }
}
