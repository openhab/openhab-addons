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
package org.openhab.binding.nest.internal.wwn.dto;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.nest.internal.wwn.dto.WWNDataUtil.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.openhab.core.library.unit.SIUnits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test cases for gson parsing of model classes
 *
 * @author David Bennett - Initial contribution
 * @author Wouter Born - Increase test coverage
 */
public class WWNGsonParsingTest {

    private final Logger logger = LoggerFactory.getLogger(WWNGsonParsingTest.class);

    private static void assertEqualDateTime(String expected, Date actual) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        assertEquals(expected, sdf.format(actual));
    }

    @Test
    public void verifyCompleteInput() throws IOException {
        WWNTopLevelData topLevel = fromJson("top-level-data.json", WWNTopLevelData.class);

        assertEquals(topLevel.getDevices().getThermostats().size(), 1);
        assertNotNull(topLevel.getDevices().getThermostats().get(THERMOSTAT1_DEVICE_ID));
        assertEquals(topLevel.getDevices().getCameras().size(), 2);
        assertNotNull(topLevel.getDevices().getCameras().get(CAMERA1_DEVICE_ID));
        assertNotNull(topLevel.getDevices().getCameras().get(CAMERA2_DEVICE_ID));
        assertEquals(topLevel.getDevices().getSmokeCoAlarms().size(), 4);
        assertNotNull(topLevel.getDevices().getSmokeCoAlarms().get(SMOKE1_DEVICE_ID));
        assertNotNull(topLevel.getDevices().getSmokeCoAlarms().get(SMOKE2_DEVICE_ID));
        assertNotNull(topLevel.getDevices().getSmokeCoAlarms().get(SMOKE3_DEVICE_ID));
        assertNotNull(topLevel.getDevices().getSmokeCoAlarms().get(SMOKE4_DEVICE_ID));
    }

    @Test
    public void verifyCompleteStreamingInput() throws IOException {
        WWNTopLevelStreamingData topLevelStreamingData = fromJson("top-level-streaming-data.json",
                WWNTopLevelStreamingData.class);

        assertEquals("/", topLevelStreamingData.getPath());

        WWNTopLevelData data = topLevelStreamingData.getData();
        assertEquals(data.getDevices().getThermostats().size(), 1);
        assertNotNull(data.getDevices().getThermostats().get(THERMOSTAT1_DEVICE_ID));
        assertEquals(data.getDevices().getCameras().size(), 2);
        assertNotNull(data.getDevices().getCameras().get(CAMERA1_DEVICE_ID));
        assertNotNull(data.getDevices().getCameras().get(CAMERA2_DEVICE_ID));
        assertEquals(data.getDevices().getSmokeCoAlarms().size(), 4);
        assertNotNull(data.getDevices().getSmokeCoAlarms().get(SMOKE1_DEVICE_ID));
        assertNotNull(data.getDevices().getSmokeCoAlarms().get(SMOKE2_DEVICE_ID));
        assertNotNull(data.getDevices().getSmokeCoAlarms().get(SMOKE3_DEVICE_ID));
        assertNotNull(data.getDevices().getSmokeCoAlarms().get(SMOKE4_DEVICE_ID));
    }

    @Test
    public void verifyThermostat() throws IOException {
        WWNThermostat thermostat = fromJson("thermostat-data.json", WWNThermostat.class);
        logger.debug("Thermostat: {}", thermostat);

        assertTrue(thermostat.isOnline());
        assertTrue(thermostat.isCanHeat());
        assertTrue(thermostat.isHasLeaf());
        assertFalse(thermostat.isCanCool());
        assertFalse(thermostat.isFanTimerActive());
        assertFalse(thermostat.isLocked());
        assertFalse(thermostat.isSunlightCorrectionActive());
        assertTrue(thermostat.isSunlightCorrectionEnabled());
        assertFalse(thermostat.isUsingEmergencyHeat());
        assertEquals(THERMOSTAT1_DEVICE_ID, thermostat.getDeviceId());
        assertEquals(Integer.valueOf(15), thermostat.getFanTimerDuration());
        assertEqualDateTime("2017-02-02T21:00:06.000Z", thermostat.getLastConnection());
        assertEqualDateTime("1970-01-01T00:00:00.000Z", thermostat.getFanTimerTimeout());
        assertEquals(Double.valueOf(24.0), thermostat.getEcoTemperatureHigh());
        assertEquals(Double.valueOf(12.5), thermostat.getEcoTemperatureLow());
        assertEquals(Double.valueOf(22.0), thermostat.getLockedTempMax());
        assertEquals(Double.valueOf(20.0), thermostat.getLockedTempMin());
        assertEquals(WWNThermostat.Mode.HEAT, thermostat.getMode());
        assertEquals("Living Room (Living Room)", thermostat.getName());
        assertEquals("Living Room Thermostat (Living Room)", thermostat.getNameLong());
        assertEquals(null, thermostat.getPreviousHvacMode());
        assertEquals("5.6-7", thermostat.getSoftwareVersion());
        assertEquals(WWNThermostat.State.OFF, thermostat.getHvacState());
        assertEquals(STRUCTURE1_STRUCTURE_ID, thermostat.getStructureId());
        assertEquals(Double.valueOf(15.5), thermostat.getTargetTemperature());
        assertEquals(Double.valueOf(24.0), thermostat.getTargetTemperatureHigh());
        assertEquals(Double.valueOf(20.0), thermostat.getTargetTemperatureLow());
        assertEquals(SIUnits.CELSIUS, thermostat.getTemperatureUnit());
        assertEquals(Integer.valueOf(0), thermostat.getTimeToTarget());
        assertEquals(THERMOSTAT1_WHERE_ID, thermostat.getWhereId());
        assertEquals("Living Room", thermostat.getWhereName());
    }

    @Test
    public void thermostatTimeToTargetSupportedValueParsing() {
        assertEquals((Integer) 0, WWNThermostat.parseTimeToTarget("~0"));
        assertEquals((Integer) 5, WWNThermostat.parseTimeToTarget("<5"));
        assertEquals((Integer) 10, WWNThermostat.parseTimeToTarget("<10"));
        assertEquals((Integer) 15, WWNThermostat.parseTimeToTarget("~15"));
        assertEquals((Integer) 90, WWNThermostat.parseTimeToTarget("~90"));
        assertEquals((Integer) 120, WWNThermostat.parseTimeToTarget(">120"));
    }

    @Test
    public void thermostatTimeToTargetUnsupportedValueParsing() {
        assertThrows(NumberFormatException.class, () -> WWNThermostat.parseTimeToTarget("#5"));
    }

    @Test
    public void verifyCamera() throws IOException {
        WWNCamera camera = fromJson("camera-data.json", WWNCamera.class);
        logger.debug("Camera: {}", camera);

        assertTrue(camera.isOnline());
        assertEquals("Upstairs", camera.getName());
        assertEquals("Upstairs Camera", camera.getNameLong());
        assertEquals(STRUCTURE1_STRUCTURE_ID, camera.getStructureId());
        assertEquals(CAMERA1_WHERE_ID, camera.getWhereId());
        assertTrue(camera.isAudioInputEnabled());
        assertFalse(camera.isPublicShareEnabled());
        assertFalse(camera.isStreaming());
        assertFalse(camera.isVideoHistoryEnabled());
        assertEquals("https://camera_app_url", camera.getAppUrl());
        assertEquals(CAMERA1_DEVICE_ID, camera.getDeviceId());
        assertNull(camera.getLastConnection());
        assertEqualDateTime("2017-01-22T08:19:20.000Z", camera.getLastIsOnlineChange());
        assertNull(camera.getPublicShareUrl());
        assertEquals("https://camera_snapshot_url", camera.getSnapshotUrl());
        assertEquals("205-600052", camera.getSoftwareVersion());
        assertEquals("https://camera_web_url", camera.getWebUrl());
        assertEquals("https://last_event_animated_image_url", camera.getLastEvent().getAnimatedImageUrl());
        assertEquals(2, camera.getLastEvent().getActivityZones().size());
        assertEquals("id1", camera.getLastEvent().getActivityZones().get(0));
        assertEquals("https://last_event_app_url", camera.getLastEvent().getAppUrl());
        assertEqualDateTime("2017-01-22T07:40:38.680Z", camera.getLastEvent().getEndTime());
        assertEquals("https://last_event_image_url", camera.getLastEvent().getImageUrl());
        assertEqualDateTime("2017-01-22T07:40:19.020Z", camera.getLastEvent().getStartTime());
        assertEqualDateTime("2017-02-05T07:40:19.020Z", camera.getLastEvent().getUrlsExpireTime());
        assertEquals("https://last_event_web_url", camera.getLastEvent().getWebUrl());
        assertTrue(camera.getLastEvent().isHasMotion());
        assertFalse(camera.getLastEvent().isHasPerson());
        assertFalse(camera.getLastEvent().isHasSound());
    }

    @Test
    public void verifySmokeDetector() throws IOException {
        WWNSmokeDetector smokeDetector = fromJson("smoke-detector-data.json", WWNSmokeDetector.class);
        logger.debug("SmokeDetector: {}", smokeDetector);

        assertTrue(smokeDetector.isOnline());
        assertEquals(SMOKE1_WHERE_ID, smokeDetector.getWhereId());
        assertEquals(SMOKE1_DEVICE_ID, smokeDetector.getDeviceId());
        assertEquals("Downstairs", smokeDetector.getName());
        assertEquals("Downstairs Nest Protect", smokeDetector.getNameLong());
        assertEqualDateTime("2017-02-02T20:53:05.338Z", smokeDetector.getLastConnection());
        assertEquals(WWNSmokeDetector.BatteryHealth.OK, smokeDetector.getBatteryHealth());
        assertEquals(WWNSmokeDetector.AlarmState.OK, smokeDetector.getCoAlarmState());
        assertEquals(WWNSmokeDetector.AlarmState.OK, smokeDetector.getSmokeAlarmState());
        assertEquals("3.1rc9", smokeDetector.getSoftwareVersion());
        assertEquals(STRUCTURE1_STRUCTURE_ID, smokeDetector.getStructureId());
        assertEquals(WWNSmokeDetector.UiColorState.GREEN, smokeDetector.getUiColorState());
    }

    @Test
    public void verifyAccessToken() throws IOException {
        WWNAccessTokenData accessToken = fromJson("access-token-data.json", WWNAccessTokenData.class);
        logger.debug("AccessTokenData: {}", accessToken);

        assertEquals("access_token", accessToken.getAccessToken());
        assertEquals(Long.valueOf(315360000L), accessToken.getExpiresIn());
    }

    @Test
    public void verifyStructure() throws IOException {
        WWNStructure structure = fromJson("structure-data.json", WWNStructure.class);
        logger.debug("Structure: {}", structure);

        assertEquals("Home", structure.getName());
        assertEquals("US", structure.getCountryCode());
        assertEquals("98056", structure.getPostalCode());
        assertEquals(WWNStructure.HomeAwayState.HOME, structure.getAway());
        assertEqualDateTime("2017-02-02T03:10:08.000Z", structure.getEtaBegin());
        assertNull(structure.getEta());
        assertNull(structure.getPeakPeriodEndTime());
        assertNull(structure.getPeakPeriodStartTime());
        assertEquals(STRUCTURE1_STRUCTURE_ID, structure.getStructureId());
        assertEquals("America/Los_Angeles", structure.getTimeZone());
        assertFalse(structure.isRhrEnrollment());
    }

    @Test
    public void verifyError() throws IOException {
        WWNErrorData error = fromJson("error-data.json", WWNErrorData.class);
        logger.debug("ErrorData: {}", error);

        assertEquals("blocked", error.getError());
        assertEquals("https://developer.nest.com/documentation/cloud/error-messages#blocked", error.getType());
        assertEquals("blocked", error.getMessage());
        assertEquals("bb514046-edc9-4bca-8239-f7a3cfb0925a", error.getInstance());
    }
}
