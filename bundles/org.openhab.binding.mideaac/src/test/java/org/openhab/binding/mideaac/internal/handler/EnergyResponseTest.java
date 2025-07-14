/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mideaac.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HexFormat;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * The {@link EnergyResponseTest} tests the Energy response methods
 * from the device using test strings. There are two decoding
 * algorithms supported by different Midea ACs.
 *
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class EnergyResponseTest {
    byte[] dataEnergy = HexFormat.of().parseHex("c1210144000005e00000000000000006000aeb000000487a5e");
    byte[] dataEnergy2 = HexFormat.of().parseHex("C1210144000246540000000000000000000000001953");
    byte[] dataEnergy3 = HexFormat.of().parseHex("C1210145350000000000000000000000000000001953");
    EnergyResponse responseEnergy = new EnergyResponse(dataEnergy);
    EnergyResponse responseEnergy2 = new EnergyResponse(dataEnergy2);
    EnergyResponse responseEnergy3 = new EnergyResponse(dataEnergy3);

    /**
     * Test Humidity response
     */
    @Test
    public void testGetHumidity() {
        assertEquals(53, responseEnergy3.getHumidity());
    }

    /**
     * Test Energy Kilowatt Hours 3 tests
     */
    @Test
    public void testGetKilowattHours() {
        double kilowattHours = responseEnergy.getKilowattHours();
        assertEquals(15.04, kilowattHours);
    }

    @Test
    public void testGetKilowattHours2() {
        double kilowattHours = responseEnergy2.getKilowattHours();
        assertEquals(1490.76, kilowattHours);
    }

    @Test
    public void testGetKilowattHours2BCD() {
        double kilowattHours = responseEnergy2.getKilowattHoursBCD();
        assertEquals(246.54, kilowattHours);
    }

    /**
     * Test amperes 2 tests
     */
    @Test
    public void testAmperes() {
        double amperes = responseEnergy.getAmperes();
        assertEquals(0.6, amperes);
    }

    @Test
    public void testAmperesBCD() {
        double amperes = responseEnergy.getAmperesBCD();
        assertEquals(0.6, amperes);
    }

    /**
     * Test watts 2 tests
     */
    @Test
    public void testGetWatts() {
        double watts = responseEnergy.getWatts();
        assertEquals(279.5, watts);
    }

    @Test
    public void testGetWattsBCD() {
        double watts = responseEnergy.getWattsBCD();
        assertEquals(115.1, watts);
    }
}
