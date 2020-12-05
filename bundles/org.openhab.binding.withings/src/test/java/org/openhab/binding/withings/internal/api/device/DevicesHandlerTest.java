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
package org.openhab.binding.withings.internal.api.device;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.withings.internal.api.AbstractAPIHandlerTest;

/**
 * @author Sven Strohschein - Initial contribution
 */
public class DevicesHandlerTest extends AbstractAPIHandlerTest {

    private DevicesHandler devicesHandler;

    @BeforeEach
    public void before() {
        devicesHandler = new DevicesHandler(accessTokenServiceMock, httpClientMock);
    }

    @Test
    public void testLoadDevices() {
        mockAccessToken();
        mockRequest("{\n" + "    \"status\": 0,\n" + "    \"body\": {\n" + "        \"devices\": [\n"
                + "            {\n" + "                \"type\": \"Scale\",\n"
                + "                \"battery\": \"medium\",\n" + "                \"model\": \"Smart Body Analyzer\",\n"
                + "                \"model_id\": 4,\n" + "                \"timezone\": \"Europe/Berlin\",\n"
                + "                \"last_session_date\": 1603832115,\n"
                + "                \"deviceid\": \"5116c0a294e4ce90c9f0af500119c531bef0aa51\"\n" + "            }\n"
                + "        ]\n" + "    }\n" + "}");

        List<DevicesResponseDTO.Device> devices = devicesHandler.loadDevices();
        assertEquals(1, devices.size());

        DevicesResponseDTO.Device device = devices.get(0);
        assertEquals("5116c0a294e4ce90c9f0af500119c531bef0aa51", device.getDeviceId());
        assertEquals("Scale", device.getType());
        assertEquals(4, device.getModelId().intValue());
        assertEquals("Smart Body Analyzer", device.getModel());
        assertEquals("medium", device.getBattery());
        assertEquals(1603832115000L, device.getLastSessionDate().getTime());
    }

    @Test
    public void testLoadDevices_NotSuccessful() {
        mockAccessToken();
        mockRequest("{\n" + "    \"status\": 1,\n" + "    \"body\": {\n" + "        \"devices\": [\n"
                + "            {\n" + "                \"type\": \"Scale\",\n"
                + "                \"battery\": \"medium\",\n" + "                \"model\": \"Smart Body Analyzer\",\n"
                + "                \"model_id\": 4,\n" + "                \"timezone\": \"Europe/Berlin\",\n"
                + "                \"last_session_date\": 1603832115,\n"
                + "                \"deviceid\": \"5116c0a294e4ce90c9f0af500119c531bef0aa51\"\n" + "            }\n"
                + "        ]\n" + "    }\n" + "}");

        List<DevicesResponseDTO.Device> devices = devicesHandler.loadDevices();
        assertEquals(0, devices.size());
    }

    @Test
    public void testLoadDevices_BodyMissing() {
        mockAccessToken();
        mockRequest("{\n" + "    \"status\": 0\n}");

        List<DevicesResponseDTO.Device> devices = devicesHandler.loadDevices();
        assertEquals(0, devices.size());
    }

    @Test
    public void testLoadDevices_BodyEmpty() {
        mockAccessToken();
        mockRequest("{\n" + "    \"status\": 0,\n    \"body\": {\n    }\n" + "}");

        List<DevicesResponseDTO.Device> devices = devicesHandler.loadDevices();
        assertEquals(0, devices.size());
    }

    @Test
    public void testLoadDevices_DevicesEmpty() {
        mockAccessToken();
        mockRequest("{\n" + "    \"status\": 0,\n" + "    \"body\": {\n" + "        \"devices\": [\n" + "        ]\n"
                + "    }\n" + "}");

        List<DevicesResponseDTO.Device> devices = devicesHandler.loadDevices();
        assertEquals(0, devices.size());
    }

    @Test
    public void testLoadDevices_Exception() {
        mockAccessToken();
        mockRequestWithException();

        // TimeoutException occurs, but it is only logged to fulfill the coding guidelines of OpenHAB
        List<DevicesResponseDTO.Device> devices = devicesHandler.loadDevices();
        assertEquals(0, devices.size());
    }

    @Test
    public void testLoadDevices_AccessTokenMissing() {
        List<DevicesResponseDTO.Device> devices = devicesHandler.loadDevices();
        assertEquals(0, devices.size());
    }
}
