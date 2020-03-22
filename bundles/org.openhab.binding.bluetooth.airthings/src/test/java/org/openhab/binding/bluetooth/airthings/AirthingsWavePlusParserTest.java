/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import static org.junit.Assert.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;
import org.openhab.binding.bluetooth.airthings.internal.AirthingsParserException;
import org.openhab.binding.bluetooth.airthings.internal.AirthingsWavePlusDataParser;

/**
 * Tests {@link AirthingsWavePlusParserTest}.
 *
 * @author Pauli Anttila - Initial contribution
 */
@NonNullByDefault
public class AirthingsWavePlusParserTest {

    @Test(expected = AirthingsParserException.class)
    public void testWrongVersion() throws AirthingsParserException {
        int[] data = { 5, 55, 51, 0, 122, 0, 61, 0, 119, 9, 11, 194, 169, 2, 46, 0, 0, 0, 4, 20 };
        new AirthingsWavePlusDataParser(data);
    }

    @Test(expected = AirthingsParserException.class)
    public void testEmptyData() throws AirthingsParserException {
        int[] data = {};
        new AirthingsWavePlusDataParser(data);
    }

    @Test(expected = AirthingsParserException.class)
    public void testWrongDataLen() throws AirthingsParserException {
        int[] data = { 1, 55, 51, 0, 122, 0, 61, 0, 119, 9, 11, 194, 169, 2, 46, 0, 0 };
        new AirthingsWavePlusDataParser(data);
    }

    @Test
    public void testParsing() throws AirthingsParserException {
        int[] data = { 1, 55, 51, 0, 122, 0, 61, 0, 119, 9, 11, 194, 169, 2, 46, 0, 0, 0, 4, 20 };
        AirthingsWavePlusDataParser parser = new AirthingsWavePlusDataParser(data);

        assertEquals(27.5, parser.getHumidity(), 0.01);
        assertEquals(681, parser.getCo2());
        assertEquals(46, parser.getTvoc());
        assertEquals(24.23, parser.getTemperature(), 0.01);
        assertEquals(993.5, parser.getPressure(), 0.01);
        assertEquals(61, parser.getRadonLongTermAvg());
        assertEquals(122, parser.getRadonShortTermAvg());
    }
}
