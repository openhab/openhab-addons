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
package org.openhab.binding.bmwcardata.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link LogRedaction}.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
class LogRedactionTest {

    private static final String VIN = "WBA21CF0X0CN12345";
    private static final String LOCATION = "vehicle.cabin.infotainment.navigation.currentLocation";

    @Test
    void shortensVin() {
        assertEquals("WBA…2345", LogRedaction.vin(VIN));
        assertEquals("short", LogRedaction.vin("short"));
        assertEquals("null", LogRedaction.vin(null));
    }

    @Test
    void shortensVinsInText() {
        assertEquals("/customers/vehicles/WBA…2345/telematicData?containerId=abc1234567890",
                LogRedaction.vins("/customers/vehicles/" + VIN + "/telematicData?containerId=abc1234567890"));
        assertEquals("bmw/raw/WBA…2345", LogRedaction.vins("bmw/raw/" + VIN));
        assertEquals("WBA…2345 and WBA…2345", LogRedaction.vins(VIN + " and " + VIN));
    }

    @Test
    void leavesOtherTextAlone() {
        // a 17 character run inside a longer token is not a VIN, nor is a 13 character container id
        String text = "token eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9 container abc1234567890";
        assertEquals(text, LogRedaction.vins(text));
        assertEquals("vehicle.vehicle.travelledDistance", LogRedaction.vins("vehicle.vehicle.travelledDistance"));
    }

    @Test
    void hidesSecretsInTokenResponse() {
        String payload = "{\"access_token\":\"eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ4In0.c2ln\",\"refresh_token\":\"abc\","
                + "\"id_token\":\"def\",\"gcid\":\"6f1a2b3c-0000-1111-2222-333344445555\",\"expires_in\":3599,"
                + "\"token_type\":\"Bearer\"}";
        assertEquals("{\"access_token\":\"***\",\"refresh_token\":\"***\",\"id_token\":\"***\",\"gcid\":\"***\","
                + "\"expires_in\":3599,\"token_type\":\"Bearer\"}", LogRedaction.payload(payload));
    }

    @Test
    void redactsStreamMessage() {
        String payload = "{\"vin\":\"" + VIN + "\",\"entityId\":\"8291cb2d-de60-425d-97d3-83b440b80308\","
                + "\"topic\":\"" + VIN + "\",\"data\":{" + "\"" + LOCATION
                + ".latitude\":{\"value\":50.85,\"unit\":\"degrees\"}," //
                + "\"" + LOCATION + ".longitude\":{\"value\":4.35},"
                + "\"vehicle.vehicle.travelledDistance\":{\"value\":12345,\"unit\":\"km\"}}}";
        String redacted = LogRedaction.payload(payload);
        assertEquals("{\"vin\":\"WBA…2345\",\"entityId\":\"***\",\"topic\":\"WBA…2345\",\"data\":{" + "\"" + LOCATION
                + ".latitude\":{\"value\":\"***\",\"unit\":\"degrees\"}," //
                + "\"" + LOCATION + ".longitude\":{\"value\":\"***\"},"
                + "\"vehicle.vehicle.travelledDistance\":{\"value\":12345,\"unit\":\"km\"}}}", redacted);
    }

    @Test
    void redactsVinsInArraysAndMemberNames() {
        assertEquals("[{\"vin\":\"WBA…2345\"},{\"WBA…2345\":true}]",
                LogRedaction.payload("[{\"vin\":\"" + VIN + "\"},{\"" + VIN + "\":true}]"));
    }

    @Test
    void redactsTextThatIsNotJson() {
        String redacted = LogRedaction.payload("Bearer eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ4In0.c2ln for " + VIN + " {");
        assertEquals("Bearer *** for WBA…2345 {", redacted);
        assertEquals("", LogRedaction.payload(""));
        assertEquals("null", LogRedaction.payload(null));
    }

    @Test
    void hidesSecretFormFields() {
        assertEquals(LogRedaction.HIDDEN, LogRedaction.value("device_code", "abc"));
        assertEquals(LogRedaction.HIDDEN, LogRedaction.value("client_id", "6f1a2b3c-0000-1111-2222-333344445555"));
        assertEquals("urn:ietf:params:oauth:grant-type:device_code",
                LogRedaction.value("grant_type", "urn:ietf:params:oauth:grant-type:device_code"));
        assertFalse(LogRedaction.value("scope", "openid").contains(LogRedaction.HIDDEN));
    }
}
