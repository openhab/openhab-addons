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
package org.openhab.binding.ecovacs.internal.api.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.binding.ecovacs.internal.api.EcovacsDevice;
import org.openhab.binding.ecovacs.internal.api.model.CleanMode;
import org.openhab.binding.ecovacs.internal.api.model.DeviceCapability;
import org.openhab.binding.ecovacs.internal.api.util.DataParsingException;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * @author Andreas Rothstein - Initial contribution
 */
@NonNullByDefault
class JsonReportParserTest {
    private final Gson gson = new Gson();
    private final EcovacsDevice device = mock(EcovacsDevice.class);
    private final EcovacsDevice.EventListener listener = mock(EcovacsDevice.EventListener.class);
    private final JsonReportParser parser = new JsonReportParser(device, listener, ProtocolVersion.JSON_V2, gson,
            LoggerFactory.getLogger(JsonReportParserTest.class));

    @BeforeEach
    void setup() {
        when(device.getSerialNumber()).thenReturn("test-device");
        when(device.hasCapability(DeviceCapability.WORK_STATE_REPORTING)).thenReturn(true);
    }

    @ParameterizedTest
    @CsvSource({ "0,cleaning,auto,idle,AUTO", "0,cleaning,freeClean,idle,SPOT_AREA", "1,cleaning,freeClean,idle,PAUSE",
            "0,idle,auto,goCharging,RETURNING", "0,cleaning,freeClean,goCharging,RETURNING",
            "0,cleaning,freeClean,goEmptying,RETURNING", "0,cleaning,freeClean,washing,WASHING",
            "0,idle,auto,emptying,EMPTYING", "0,idle,auto,drying,DRYING", "0,idle,auto,idle,IDLE" })
    void handlesWorkStates(int paused, String robot, String clean, String station, CleanMode expected)
            throws Exception {
        String data = """
                {"paused":%d,"robotState":{"state":"%s","cleanState":{"type":"%s"}},
                 "stationState":{"state":"%s"}}
                """.formatted(paused, robot, clean, station);
        parser.handleMessage("onworkstate", wrap(data));
        verify(listener).onCleaningModeUpdated(device, expected, Optional.empty());
    }

    @ParameterizedTest
    @ValueSource(strings = { "{}", "{\"robotState\":{}}", "{\"robotState\":{\"state\":\"video\"}}",
            "{\"robotState\":{\"state\":\"cleaning\"}}", "{\"robotState\":{\"state\":\"cleaning\",\"cleanState\":{}}}",
            "{\"robotState\":{\"state\":\"unknown\"},\"stationState\":{\"state\":\"idle\"}}" })
    void unknownOrPartialWorkStateDoesNotDisconnect(String data) throws Exception {
        assertDoesNotThrow(() -> parser.handleMessage("onworkstate", wrap(data)));
        verify(listener, never()).onCleaningModeUpdated(any(), any(), any());
    }

    @Test
    void idleWithoutStationIsAccepted() throws Exception {
        parser.handleMessage("onworkstate", wrap("{\"robotState\":{\"state\":\"idle\"}}"));
        verify(listener).onCleaningModeUpdated(device, CleanMode.IDLE, Optional.empty());
    }

    @Test
    void legacyChargeAndBatteryReportsStillWork() throws Exception {
        parser.handleMessage("onchargestate", wrap("{\"isCharging\":1}"));
        parser.handleMessage("onbattery", wrap("{\"value\":73}"));
        verify(listener).onChargingStateUpdated(device, true);
        verify(listener).onBatteryLevelUpdated(device, 73);
    }

    @Test
    void x12sProfileLoadsWithExistingCapabilities() throws Exception {
        try (var reader = new InputStreamReader(
                Objects.requireNonNull(
                        getClass().getClassLoader().getResourceAsStream("devices/supported_device_list.json")),
                StandardCharsets.UTF_8)) {
            DeviceDescription[] descriptions = gson.fromJson(reader, DeviceDescription[].class);
            DeviceDescription desc = Arrays.stream(descriptions).filter(d -> "tj7mri".equals(d.deviceClass)).findFirst()
                    .orElseThrow();
            assertEquals(ProtocolVersion.JSON_V2, desc.protoVersion);
            assertTrue(desc.usesMqtt);
            assertTrue(desc.capabilities.contains(DeviceCapability.WORK_STATE_REPORTING));
            assertTrue(desc.capabilities.contains(DeviceCapability.FREE_CLEAN_FOR_SPOT_AREA));
            assertTrue(desc.capabilities.contains(DeviceCapability.LEGACY_CLEAN_MAP_FALLBACK));
            assertTrue(desc.capabilities.contains(DeviceCapability.USES_CLEAN_RESULTS_LOG_API));
            assertFalse(desc.capabilities.contains(null));
        }
    }

    @ParameterizedTest
    @CsvSource({ "0,cleaning,freeClean,washing,SPOT_AREA", "0,cleaning,auto,goCharging,AUTO",
            "0,idle,auto,drying,DRYING", "1,cleaning,auto,washing,PAUSE" })
    void legacyProfilesKeepOriginalWorkStateInterpretation(int paused, String robot, String clean, String station,
            CleanMode expected) throws Exception {
        when(device.hasCapability(DeviceCapability.WORK_STATE_REPORTING)).thenReturn(false);
        String data = """
                {"paused":%d,"robotState":{"state":"%s","cleanState":{"type":"%s"}},
                 "stationState":{"state":"%s"}}
                """.formatted(paused, robot, clean, station);
        parser.handleMessage("onworkstate", wrap(data));
        verify(listener).onCleaningModeUpdated(device, expected, Optional.empty());
    }

    @Test
    void unknownLegacyWorkStateStillReportsParsingFailure() {
        when(device.hasCapability(DeviceCapability.WORK_STATE_REPORTING)).thenReturn(false);
        assertThrows(DataParsingException.class,
                () -> parser.handleMessage("onworkstate", wrap("{\"robotState\":{\"state\":\"video\"}}")));
    }

    private String wrap(String data) {
        return "{\"header\":{\"fwVer\":\"test\"},\"body\":{\"data\":" + data + "}}";
    }
}
