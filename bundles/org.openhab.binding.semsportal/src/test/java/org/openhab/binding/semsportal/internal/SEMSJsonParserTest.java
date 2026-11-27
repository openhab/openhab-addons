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
package org.openhab.binding.semsportal.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.semsportal.internal.dto.BaseResponse;
import org.openhab.binding.semsportal.internal.dto.CountingResponse;
import org.openhab.binding.semsportal.internal.dto.FlowResponse;
import org.openhab.binding.semsportal.internal.dto.Inverter;
import org.openhab.binding.semsportal.internal.dto.InverterStatusResponse;
import org.openhab.binding.semsportal.internal.dto.LoginResponse;
import org.openhab.binding.semsportal.internal.dto.ProductionResponse;
import org.openhab.binding.semsportal.internal.dto.SEMSToken;
import org.openhab.binding.semsportal.internal.dto.Station;
import org.openhab.binding.semsportal.internal.dto.StationListResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Iwan Bron - Initial contribution
 */
@NonNullByDefault
public class SEMSJsonParserTest {

    @Test
    public void testParseSuccessLoginResult() throws Exception {
        LoginResponse response = parse("success_login.json", LoginResponse.class);
        assertTrue(response.isOk(), "Success response should result in OK");
        SEMSToken token = response.getToken();
        assertNotNull(token, "Success response should result in token");
        assertEquals("00000000-0000-0000-0000-000000000001", token.getUid());
        assertEquals("0123456789abcdef0123456789abcdef", token.getToken());
        assertEquals("semsPlusWeb", token.getClient());
        assertEquals("https://us-gateway.semsportal.com/web/sems", token.getApi(),
                "Success response should give the gateway of the region");
    }

    @Test
    public void testParseErrorLoginResult() throws Exception {
        LoginResponse response = parse("error_login.json", LoginResponse.class);
        assertFalse(response.isOk(), "Error response should not result in OK");
        assertEquals(BaseResponse.INVALID, response.getCode());
        assertEquals("Email or password error.", response.getMsg());
        assertNull(response.getToken(), "Error response should have null token");
    }

    @Test
    public void testParseErrorAccessResult() throws Exception {
        FlowResponse response = parse("error_access.json", FlowResponse.class);
        assertFalse(response.isOk(), "Error response should not result in OK");
        assertTrue(response.isNoAccess(), "Error response should say that there is no access");
        assertFalse(response.isSessionInvalid(), "No access is not a session problem");
        assertNull(response.getCurrentOutput(), "Error response has no data");
        assertNull(response.getRefreshTime(), "Error response has no data");
    }

    @Test
    public void testSessionInvalid() {
        for (String code : new String[] { "C0602", "100002" }) {
            BaseResponse response = getGson().fromJson("{\"code\": \"" + code + "\"}", BaseResponse.class);
            assertNotNull(response);
            assertTrue(response != null && response.isSessionInvalid(), "Code " + code + " is a session problem");
        }
    }

    @Test
    public void testParseSuccessListResult() throws Exception {
        StationListResponse response = parse("success_list.json", StationListResponse.class);
        assertTrue(response.isOk(), "Success response should result in OK");
        assertEquals(1, response.getStations().size(), "List response should have station list");
        Station station = response.getStations().get(0);
        assertEquals("00000000-0000-0000-0000-0000000000a1", station.getStationId());
        assertEquals("Example station", station.getName());
        assertEquals(8.19, station.getCapacity());
    }

    @Test
    public void testParseDevices() throws Exception {
        InverterStatusResponse response = parse("success_devices.json", InverterStatusResponse.class);
        assertTrue(response.isOk(), "Success response should result in OK");
        // only inverters, in the order of the response
        List<Inverter> inverters = response.getInverters();
        assertEquals(2, inverters.size(), "The smart meter is not an inverter");
        assertEquals("99999MSU99XX0001", inverters.get(0).getSerialNumber());
        assertEquals("GW8500", inverters.get(0).getModel());
        assertEquals(5, inverters.get(0).getStatus());
        assertEquals("99999MSU99XX0002", inverters.get(1).getSerialNumber());
        assertNull(inverters.get(1).getModel(), "No details, no model");
        assertEquals(-1, inverters.get(1).getStatus());
    }

    @Test
    public void testParseDevicesWithoutInverters() throws Exception {
        // a station that does not exist is answered with an empty list instead of an error
        InverterStatusResponse response = getGson().fromJson("{\"code\": \"00000\", \"data\": {\"total\": 0}}",
                InverterStatusResponse.class);
        assertNotNull(response);
        assertTrue(response != null && response.getInverters().isEmpty());
    }

    @Test
    public void testParseFlow() throws Exception {
        FlowResponse response = parse("success_flow.json", FlowResponse.class);
        assertTrue(response.isOk(), "Success response should result in OK");
        assertEquals(2.044, response.getCurrentOutput(), "Current output parsed correctly");
        assertEquals("2026-11-01T10:44:00", response.getRefreshTime(), "Refresh time parsed correctly");
    }

    @Test
    public void testParseCounting() throws Exception {
        CountingResponse response = parse("success_counting.json", CountingResponse.class);
        assertTrue(response.isOk(), "Success response should result in OK");
        assertEquals(9.1, response.getValue(CountingResponse.TODAY), "Day total parsed correctly");
        assertEquals(789.6, response.getValue(CountingResponse.MONTH), "Month total parsed correctly");
        assertEquals(49119.3, response.getValue(CountingResponse.TOTAL), "Overall total parsed correctly");
        assertNull(response.getValue("unknown"), "A value that is not in the response is null");
    }

    @Test
    public void testParseCountingWithoutNumber() {
        CountingResponse response = getGson().fromJson(
                "{\"code\": \"00000\", \"data\": [{\"factors\": [{\"code\": \"proPvStatsToday\", \"data\": \"n/a\"}]}]}",
                CountingResponse.class);
        assertNotNull(response);
        assertTrue(response != null && response.getValue(CountingResponse.TODAY) == null);
    }

    @Test
    public void testParseProduction() throws Exception {
        ProductionResponse response = parse("success_production.json", ProductionResponse.class);
        assertTrue(response.isOk(), "Success response should result in OK");
        assertEquals(9.1, response.getIncome(), "Income parsed correctly");
    }

    private <T> T parse(String file, Class<T> type) throws IOException {
        String json = Files.readString(Paths.get("src/test/resources/" + file));
        return Objects.requireNonNull(getGson().fromJson(json, type), "Expected deserialized " + type.getSimpleName());
    }

    private Gson getGson() {
        return new GsonBuilder().create();
    }
}
