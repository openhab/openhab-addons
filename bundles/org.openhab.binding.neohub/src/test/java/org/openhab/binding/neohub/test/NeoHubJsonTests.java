/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.neohub.test;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.neohub.internal.NeoHubBindingConstants.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.neohub.internal.NeoHubAbstractDeviceData;
import org.openhab.binding.neohub.internal.NeoHubAbstractDeviceData.AbstractRecord;
import org.openhab.binding.neohub.internal.NeoHubConfiguration;
import org.openhab.binding.neohub.internal.NeoHubGetEngineersData;
import org.openhab.binding.neohub.internal.NeoHubInfoResponse;
import org.openhab.binding.neohub.internal.NeoHubInfoResponse.InfoRecord;
import org.openhab.binding.neohub.internal.NeoHubLiveDeviceData;
import org.openhab.binding.neohub.internal.NeoHubReadDcbResponse;
import org.openhab.binding.neohub.internal.NeoHubSocket;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * JUnit for testing JSON parsing.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class NeoHubJsonTests {

    /*
     * to actually run tests on a physical device you must have a hub physically available, and its IP address must be
     * correctly configured in the "hubIPAddress" string constant e.g. "192.168.1.123"
     * note: only run the test if such a device is actually available
     */
    private static final String HUB_IP_ADDRESS = "192.168.1.xxx";

    public static final Pattern VALID_IP_V4_ADDRESS = Pattern
            .compile("\\b((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.|$)){4}\\b");

    /**
     * Load the test JSON payload string from a file
     */
    private String load(String fileName) {
        try (FileReader file = new FileReader(String.format("src/test/resources/%s.json", fileName));
                BufferedReader reader = new BufferedReader(file)) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            return builder.toString();
        } catch (IOException e) {
            fail(e.getMessage());
        }
        return "";
    }

    /**
     * Test an INFO JSON response string as produced by older firmware versions
     */
    @Test
    public void testInfoJsonOld() {
        // load INFO JSON response string in old JSON format
        NeoHubAbstractDeviceData infoResponse = NeoHubInfoResponse.createDeviceData(load("info_old"));
        assertNotNull(infoResponse);

        // missing device
        AbstractRecord device = infoResponse.getDeviceRecord("Aardvark");
        assertNull(device);

        // existing type 12 thermostat device
        device = infoResponse.getDeviceRecord("Dining Room");
        assertNotNull(device);
        assertEquals("Dining Room", device.getDeviceName());
        assertEquals(new BigDecimal("22.0"), device.getTargetTemperature());
        assertEquals(new BigDecimal("22.2"), device.getActualTemperature());
        assertEquals(new BigDecimal("23"), device.getFloorTemperature());
        assertTrue(device instanceof InfoRecord);
        assertEquals(12, ((InfoRecord) device).getDeviceType());
        assertFalse(device.isStandby());
        assertFalse(device.isHeating());
        assertFalse(device.isPreHeating());
        assertFalse(device.isTimerOn());
        assertFalse(device.offline());
        assertFalse(device.stateManual());
        assertTrue(device.stateAuto());
        assertFalse(device.isWindowOpen());
        assertFalse(device.isBatteryLow());

        // existing type 6 plug device (MANUAL OFF)
        device = infoResponse.getDeviceRecord("Plug South");
        assertNotNull(device);
        assertEquals("Plug South", device.getDeviceName());
        assertTrue(device instanceof InfoRecord);
        assertEquals(6, ((InfoRecord) device).getDeviceType());
        assertFalse(device.isTimerOn());
        assertTrue(device.stateManual());

        // existing type 6 plug device (MANUAL ON)
        device = infoResponse.getDeviceRecord("Plug North");
        assertNotNull(device);
        assertEquals("Plug North", device.getDeviceName());
        assertTrue(device instanceof InfoRecord);
        assertEquals(6, ((InfoRecord) device).getDeviceType());
        assertTrue(device.isTimerOn());
        assertTrue(device.stateManual());

        // existing type 6 plug device (AUTO OFF)
        device = infoResponse.getDeviceRecord("Watering System");
        assertNotNull(device);
        assertEquals("Watering System", device.getDeviceName());
        assertTrue(device instanceof InfoRecord);
        assertEquals(6, ((InfoRecord) device).getDeviceType());
        assertFalse(device.isTimerOn());
        assertFalse(device.stateManual());
    }

    /**
     * Test an INFO JSON response string as produced by newer firmware versions
     */
    @Test
    public void testInfoJsonNew() {
        // load INFO JSON response string in new JSON format
        NeoHubAbstractDeviceData infoResponse = NeoHubInfoResponse.createDeviceData(load("info_new"));
        assertNotNull(infoResponse);

        // existing device (new JSON format)
        AbstractRecord device = infoResponse.getDeviceRecord("Dining Room");
        assertNotNull(device);
        assertEquals("Dining Room", device.getDeviceName());
        assertFalse(device.offline());
        assertFalse(device.isWindowOpen());

        // existing repeater device
        device = infoResponse.getDeviceRecord("repeaternode54473");
        assertNotNull(device);
        assertEquals("repeaternode54473", device.getDeviceName());
        assertEquals(new BigDecimal("127"), device.getFloorTemperature());
        assertEquals(new BigDecimal("255.255"), device.getActualTemperature());
    }

    /**
     * Test for a READ_DCB JSON string that has valid CORF C response
     */
    @Test
    public void testReadDcbJson() {
        // load READ_DCB JSON response string with valid CORF C response
        NeoHubReadDcbResponse dcbResponse = NeoHubReadDcbResponse.createSystemData(load("dcb_celsius"));
        assertNotNull(dcbResponse);
        assertEquals(SIUnits.CELSIUS, dcbResponse.getTemperatureUnit());
        assertEquals("2134", dcbResponse.getFirmwareVersion());

        // load READ_DCB JSON response string with valid CORF F response
        dcbResponse = NeoHubReadDcbResponse.createSystemData(load("dcb_fahrenheit"));
        assertNotNull(dcbResponse);
        assertEquals(ImperialUnits.FAHRENHEIT, dcbResponse.getTemperatureUnit());

        // load READ_DCB JSON response string with missing CORF element
        dcbResponse = NeoHubReadDcbResponse.createSystemData(load("dcb_corf_missing"));
        assertNotNull(dcbResponse);
        assertEquals(SIUnits.CELSIUS, dcbResponse.getTemperatureUnit());

        // load READ_DCB JSON response string where CORF element is an empty string
        dcbResponse = NeoHubReadDcbResponse.createSystemData(load("dcb_corf_empty"));
        assertNotNull(dcbResponse);
        assertEquals(SIUnits.CELSIUS, dcbResponse.getTemperatureUnit());
    }

    /**
     * Test an INFO JSON string that has a door contact and a temperature sensor
     */
    @Test
    public void testInfoJsonWithSensors() {
        /*
         * load an INFO JSON response string that has a closed door contact and a
         * temperature sensor
         */
        // save("info_sensors_closed", NEOHUB_JSON_TEST_STRING_INFO_SENSORS_CLOSED);
        NeoHubAbstractDeviceData infoResponse = NeoHubInfoResponse.createDeviceData(load("info_sensors_closed"));
        assertNotNull(infoResponse);

        // existing contact device type 5 (CLOSED)
        AbstractRecord device = infoResponse.getDeviceRecord("Back Door");
        assertNotNull(device);
        assertEquals("Back Door", device.getDeviceName());
        assertTrue(device instanceof InfoRecord);
        assertEquals(5, ((InfoRecord) device).getDeviceType());
        assertFalse(device.isWindowOpen());
        assertFalse(device.isBatteryLow());

        // existing temperature sensor type 14
        device = infoResponse.getDeviceRecord("Master Bedroom");
        assertNotNull(device);
        assertEquals("Master Bedroom", device.getDeviceName());
        assertTrue(device instanceof InfoRecord);
        assertEquals(14, ((InfoRecord) device).getDeviceType());
        assertEquals(new BigDecimal("19.5"), device.getActualTemperature());

        // existing thermostat type 1
        device = infoResponse.getDeviceRecord("Living Room Floor");
        assertNotNull(device);
        assertEquals("Living Room Floor", device.getDeviceName());
        assertTrue(device instanceof InfoRecord);
        assertEquals(1, ((InfoRecord) device).getDeviceType());
        assertEquals(new BigDecimal("19.8"), device.getActualTemperature());

        // load an INFO JSON response string that has an open door contact
        // save("info_sensors_open", NEOHUB_JSON_TEST_STRING_INFO_SENSORS_OPEN);
        infoResponse = NeoHubInfoResponse.createDeviceData(load("info_sensors_open"));
        assertNotNull(infoResponse);

        // existing contact device type 5 (OPEN)
        device = infoResponse.getDeviceRecord("Back Door");
        assertNotNull(device);
        assertEquals("Back Door", device.getDeviceName());
        assertTrue(device instanceof InfoRecord);
        assertEquals(5, ((InfoRecord) device).getDeviceType());
        assertTrue(device.isWindowOpen());
        assertTrue(device.isBatteryLow());
    }

    /**
     * From NeoHub rev2.6 onwards the READ_DCB command is "deprecated" so we can
     * also test the replacement GET_SYSTEM command (valid CORF response)
     */
    @Test
    public void testGetSystemJson() {
        // load GET_SYSTEM JSON response string
        NeoHubReadDcbResponse dcbResponse;
        dcbResponse = NeoHubReadDcbResponse.createSystemData(load("system"));
        assertNotNull(dcbResponse);
        assertEquals(SIUnits.CELSIUS, dcbResponse.getTemperatureUnit());
        assertEquals("2134", dcbResponse.getFirmwareVersion());
    }

    /**
     * From NeoHub rev2.6 onwards the INFO command is "deprecated" so we must test
     * the replacement GET_LIVE_DATA command
     */
    @Test
    public void testGetLiveDataJson() {
        // load GET_LIVE_DATA JSON response string
        NeoHubLiveDeviceData liveDataResponse = NeoHubLiveDeviceData.createDeviceData(load("live_data"));
        assertNotNull(liveDataResponse);

        // test the time stamps
        assertEquals(1588494785, liveDataResponse.getTimestampEngineers());
        assertEquals(0, liveDataResponse.getTimestampSystem());

        // missing device
        AbstractRecord device = liveDataResponse.getDeviceRecord("Aardvark");
        assertNull(device);

        // test an existing thermostat device
        device = liveDataResponse.getDeviceRecord("Dining Room");
        assertNotNull(device);
        assertEquals("Dining Room", device.getDeviceName());
        assertEquals(new BigDecimal("22.0"), device.getTargetTemperature());
        assertEquals(new BigDecimal("22.2"), device.getActualTemperature());
        assertEquals(new BigDecimal("20.50"), device.getFloorTemperature());
        assertFalse(device.isStandby());
        assertFalse(device.isHeating());
        assertFalse(device.isPreHeating());
        assertFalse(device.isTimerOn());
        assertFalse(device.offline());
        assertFalse(device.stateManual());
        assertTrue(device.stateAuto());
        assertFalse(device.isWindowOpen());
        assertFalse(device.isBatteryLow());

        // test a plug device (MANUAL OFF)
        device = liveDataResponse.getDeviceRecord("Living Room South");
        assertNotNull(device);
        assertEquals("Living Room South", device.getDeviceName());
        assertFalse(device.isTimerOn());
        assertTrue(device.stateManual());

        // test a plug device (MANUAL ON)
        device = liveDataResponse.getDeviceRecord("Living Room North");
        assertNotNull(device);
        assertEquals("Living Room North", device.getDeviceName());
        assertTrue(device.isTimerOn());
        assertTrue(device.stateManual());

        // test a plug device (AUTO OFF)
        device = liveDataResponse.getDeviceRecord("Green Wall Watering");
        assertNotNull(device);
        assertEquals("Green Wall Watering", device.getDeviceName());
        assertFalse(device.isTimerOn());
        assertFalse(device.stateManual());

        // test a device that is offline
        device = liveDataResponse.getDeviceRecord("Shower Room");
        assertNotNull(device);
        assertEquals("Shower Room", device.getDeviceName());
        assertTrue(device.offline());

        // test a device with a low battery
        device = liveDataResponse.getDeviceRecord("Conservatory");
        assertNotNull(device);
        assertEquals("Conservatory", device.getDeviceName());
        assertTrue(device.isBatteryLow());

        // test a device with an open window alarm
        device = liveDataResponse.getDeviceRecord("Door Contact");
        assertNotNull(device);
        assertEquals("Door Contact", device.getDeviceName());
        assertTrue(device.isWindowOpen());

        // test a wireless temperature sensor
        device = liveDataResponse.getDeviceRecord("Room Sensor");
        assertNotNull(device);
        assertEquals("Room Sensor", device.getDeviceName());
        assertEquals(new BigDecimal("21.5"), device.getActualTemperature());

        // test a repeater node
        device = liveDataResponse.getDeviceRecord("repeaternode54473");
        assertNotNull(device);
        assertEquals("repeaternode54473", device.getDeviceName());
        assertTrue(MATCHER_HEATMISER_REPEATER.matcher(device.getDeviceName()).matches());
    }

    /**
     * From NeoHub rev2.6 onwards the INFO command is "deprecated" and the DEVICE_ID
     * element is not returned in the GET_LIVE_DATA call so we must test the
     * replacement GET_ENGINEERS command
     */
    @Test
    public void testGetEngineersJson() {
        // load GET_ENGINEERS JSON response string
        NeoHubGetEngineersData engResponse = NeoHubGetEngineersData.createEngineersData(load("engineers"));
        assertNotNull(engResponse);

        // test device ID (type 12 thermostat device)
        assertEquals(12, engResponse.getDeviceType("Dining Room"));

        // test device ID (type 6 plug device)
        assertEquals(6, engResponse.getDeviceType("Living Room South"));
    }

    /**
     * send JSON request to the socket and retrieve JSON response
     */
    private String testCommunicationInner(String requestJson) {
        NeoHubConfiguration config = new NeoHubConfiguration();
        config.hostName = HUB_IP_ADDRESS;
        config.socketTimeout = 5;
        try {
            NeoHubSocket socket = new NeoHubSocket(config, "test");
            String responseJson = socket.sendMessage(requestJson);
            socket.close();
            return responseJson;
        } catch (Exception e) {
            assertTrue(false);
        }
        return "";
    }

    /**
     * Test the communications
     */
    @Test
    public void testCommunications() {
        /*
         * tests the actual communication with a real physical device on 'hubIpAddress'
         * note: only run the test if such a device is actually available
         */
        if (!VALID_IP_V4_ADDRESS.matcher(HUB_IP_ADDRESS).matches()) {
            return;
        }

        String responseJson = testCommunicationInner(CMD_CODE_INFO);
        assertFalse(responseJson.isEmpty());

        responseJson = testCommunicationInner(CMD_CODE_READ_DCB);
        assertFalse(responseJson.isEmpty());

        NeoHubReadDcbResponse dcbResponse = NeoHubReadDcbResponse.createSystemData(responseJson);
        assertNotNull(dcbResponse);

        long timeStamp = dcbResponse.timeStamp;
        assertEquals(Instant.now().getEpochSecond(), timeStamp, 1);

        responseJson = testCommunicationInner(CMD_CODE_GET_LIVE_DATA);
        assertFalse(responseJson.isEmpty());

        NeoHubLiveDeviceData liveDataResponse = NeoHubLiveDeviceData.createDeviceData(responseJson);
        assertNotNull(liveDataResponse);

        assertTrue(timeStamp > liveDataResponse.getTimestampEngineers());
        assertTrue(timeStamp > liveDataResponse.getTimestampSystem());

        responseJson = testCommunicationInner(CMD_CODE_GET_ENGINEERS);
        assertFalse(responseJson.isEmpty());

        responseJson = testCommunicationInner(CMD_CODE_GET_SYSTEM);
        assertFalse(responseJson.isEmpty());

        responseJson = testCommunicationInner(String.format(CMD_CODE_TEMP, "20", "Hallway"));
        assertFalse(responseJson.isEmpty());
    }

    @Test
    public void testJsonValidation() {
        JsonElement jsonElement;

        jsonElement = JsonParser.parseString("");
        assertFalse(jsonElement.isJsonObject());

        jsonElement = JsonParser.parseString("xx");
        assertFalse(jsonElement.isJsonObject());

        jsonElement = JsonParser.parseString("{}");
        assertTrue(jsonElement.isJsonObject());
        assertEquals(0, ((JsonObject) jsonElement).keySet().size());

        jsonElement = JsonParser.parseString(load("dcb_celsius"));
        assertTrue(jsonElement.isJsonObject());
        assertTrue(((JsonObject) jsonElement).keySet().size() > 0);

        jsonElement = JsonParser.parseString(load("live_data"));
        assertTrue(jsonElement.isJsonObject());
        assertTrue(((JsonObject) jsonElement).keySet().size() > 0);

        jsonElement = JsonParser.parseString(load("engineers"));
        assertTrue(jsonElement.isJsonObject());
        assertTrue(((JsonObject) jsonElement).keySet().size() > 0);

        jsonElement = JsonParser.parseString(load("info_new"));
        assertTrue(jsonElement.isJsonObject());
        assertTrue(((JsonObject) jsonElement).keySet().size() > 0);

        jsonElement = JsonParser.parseString(load("info_old"));
        assertTrue(jsonElement.isJsonObject());
        assertTrue(((JsonObject) jsonElement).keySet().size() > 0);

        jsonElement = JsonParser.parseString(load("system"));
        assertTrue(jsonElement.isJsonObject());
        assertTrue(((JsonObject) jsonElement).keySet().size() > 0);

        jsonElement = JsonParser.parseString(load("info_sensors_closed"));
        assertTrue(jsonElement.isJsonObject());
        assertTrue(((JsonObject) jsonElement).keySet().size() > 0);
    }
}
