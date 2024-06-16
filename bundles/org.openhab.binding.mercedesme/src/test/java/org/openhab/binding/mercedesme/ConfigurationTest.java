/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.mercedesme;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mercedesme.internal.Constants;
import org.openhab.binding.mercedesme.internal.config.AccountConfiguration;
import org.openhab.binding.mercedesme.internal.server.Utils;

/**
 * The {@link ConfigurationTest} Test configuration settings
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class ConfigurationTest {

    @Test
    void testScope() {
        AccountConfiguration ac = new AccountConfiguration();
        assertEquals(
                "openid offline_access mb:vehicle:mbdata:payasyoudrive mb:vehicle:mbdata:vehiclestatus mb:vehicle:mbdata:vehiclelock mb:vehicle:mbdata:fuelstatus mb:vehicle:mbdata:evstatus",
                ac.getScope());
    }

    @Test
    void testApiUrlEndpoint() {
        String url = Constants.FUEL_URL;
        String[] endpoint = url.split("/");
        String finalEndpoint = endpoint[endpoint.length - 1];
        assertEquals("fuelstatus", finalEndpoint);
    }

    @Test
    void testRound() {
        int socValue = 66;
        double batteryCapacity = 66.5;
        float chargedValue = Math.round(socValue * 1000 * (float) batteryCapacity / 1000) / (float) 100;
        assertEquals(43.89, chargedValue, 0.01);
        float unchargedValue = Math.round((100 - socValue) * 1000 * (float) batteryCapacity / 1000) / (float) 100;
        assertEquals(22.61, unchargedValue, 0.01);
        assertEquals(batteryCapacity, chargedValue + unchargedValue, 0.01);
    }

    @Test
    public void testCallbackUrl() throws SocketException {
        String ip = Utils.getCallbackIP();
        String message = "IP " + ip + " not reachable";
        try {
            assertTrue(InetAddress.getByName(ip).isReachable(10000), message);
        } catch (IOException e) {
            fail(message);
        }
    }
}
