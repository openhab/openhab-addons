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
package org.openhab.binding.semsportal.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.semsportal.internal.dto.SEMSLoginResponse;
import org.openhab.binding.semsportal.internal.dto.SEMSResponse;
import org.openhab.binding.semsportal.internal.dto.SEMSStatusResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Iwan Bron - Initial contribution
 */
@NonNullByDefault
public class SEMSJsonParserTest {
    @Test
    public void testParseSuccessStatusResult() throws Exception {
        String json = Files.readString(Paths.get("src/test/resources/success_status.json"));
        SEMSStatusResponse response = getGson().fromJson(json, SEMSStatusResponse.class);
        assertNotNull(response, "Expected deserialized StatusResponse");
        // response cannot be null, was asserted before
        assertTrue(response.isOk(), "Successresponse should be OK");
        assertNotNull(response.getStatus(), "Expected deserialized StatusResponse.status");
        assertEquals(381.0, response.getStatus().getCurrentOutput(), "Current Output parsed correctly");
        assertEquals(0.11, response.getStatus().getDayIncome(), "Day income parsed correctly");
        assertEquals(0.5, response.getStatus().getDayTotal(), "Day total parsed correctly");
        assertEquals(ZonedDateTime.of(2021, 2, 6, 11, 22, 48, 0, ZoneId.systemDefault()),
                response.getStatus().getLastUpdate(), "Last update parsed correctly");
        assertEquals(17.2, response.getStatus().getMonthTotal(), "Month total parsed correctly");
        assertEquals(7379.0, response.getStatus().getOverallTotal(), "Overall total parsed correctly");

        assertEquals(1623.38, response.getStatus().getTotalIncome(), "Total income parsed correctly");
    }

    @Test
    public void testParseErrorStatusResult() throws Exception {
        String json = Files.readString(Paths.get("src/test/resources/error_status.json"));
        SEMSResponse response = getGson().fromJson(json, SEMSResponse.class);
        assertNotNull(response, "Expected deserialized StatusResponse");
        // response cannot be null, was asserted before
        assertEquals(response.getCode(), SEMSResponse.EXCEPTION, "Error response shoud have error code");
        assertTrue(response.isError(), "Error response should have isError = true");
    }

    @Test
    public void testParseSuccessLoginResult() throws Exception {
        String json = Files.readString(Paths.get("src/test/resources/success_login.json"));
        SEMSLoginResponse response = getGson().fromJson(json, SEMSLoginResponse.class);
        assertNotNull(response, "Expected deserialized LoginResponse");
        assertTrue(response.isOk(), "Success response should result in OK");
        assertNotNull(response.getToken(), "Success response should result in token");
    }

    @Test
    public void testParseErrorLoginResult() throws Exception {
        String json = Files.readString(Paths.get("src/test/resources/error_login.json"));
        SEMSLoginResponse response = getGson().fromJson(json, SEMSLoginResponse.class);
        assertNotNull(response, "Expected deserialized LoginResponse");
        assertFalse(response.isOk(), "Error response should not result in OK");
        assertNull(response.getToken(), "Error response should have null token");
    }

    private Gson getGson() {
        return new GsonBuilder().setDateFormat(SEMSPortalBindingConstants.DATE_FORMAT).create();
    }
}
