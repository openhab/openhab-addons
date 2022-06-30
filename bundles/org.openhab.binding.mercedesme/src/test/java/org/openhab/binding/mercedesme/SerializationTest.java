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
package org.openhab.binding.mercedesme;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mercedesme.internal.Constants;
import org.openhab.binding.mercedesme.internal.server.Utils;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;

/**
 * The {@link SerializationTest} is testing token serial- & deserialization
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class SerializationTest {

    @Test
    public void test() {
        AccessTokenResponse atr = new AccessTokenResponse();
        atr.setAccessToken("abc");
        atr.setExpiresIn(123);
        atr.setRefreshToken("xyz");
        atr.setTokenType("Bearer");

        String serialization = Utils.toString(atr);
        Object deserialization = Utils.fromString(serialization);
        assertTrue(atr.equals(deserialization));
    }

    @Test
    public void testReplacement() {
        String url = String.format(Constants.STATUS_URL, "W1N");
        assertEquals("https://api.mercedes-benz.com/vehicledata/v2/vehicles/W1N/containers/vehiclestatus", url);
    }
}
