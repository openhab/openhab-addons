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
package org.openhab.binding.lgthinq.lgservices;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.openhab.binding.lgthinq.lgservices.api.RestResult;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.model.DevicePowerState;
import org.openhab.binding.lgthinq.lgservices.model.DeviceTypes;
import org.openhab.binding.lgthinq.lgservices.model.LGAPIVerion;
import org.openhab.binding.lgthinq.lgservices.model.MonitoringResultFormat;
import org.openhab.binding.lgthinq.lgservices.model.devices.dishwasher.DishWasherCapability;
import org.openhab.binding.lgthinq.lgservices.model.devices.dishwasher.DishWasherSnapshot;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Test class to validate DishWasher B64 monitoring data fix.
 * 
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
class DishWasherB64Test {

    @TempDir
    @Nullable
    Path tempDir;

    @Nullable
    private DishWasherTestService service;

    @BeforeEach
    void setUp() {
        Path dir = Objects.requireNonNull(tempDir);
        System.setProperty("THINQ_USER_DATA_FOLDER", dir.toAbsolutePath().toString());
        HttpClient httpClient = Objects.requireNonNull(Mockito.mock(HttpClient.class));
        service = new DishWasherTestService(httpClient);
    }

    @Test
    void testDishWasherB64InV2() throws LGThinqApiException, IOException,
            org.openhab.binding.lgthinq.lgservices.errors.LGThinqDeviceV1MonitorExpiredException,
            org.openhab.binding.lgthinq.lgservices.errors.LGThinqUnmarshallException {
        DishWasherCapability cap = new DishWasherCapability();
        cap.setDeviceType(DeviceTypes.DISH_WASHER);
        cap.setDeviceVersion(LGAPIVerion.V2_0);
        // Initially force JSON format as it was before the fix
        cap.setMonitoringDataFormat(MonitoringResultFormat.JSON_FORMAT);

        Map<String, Object> workList = new HashMap<>();
        workList.put("deviceId", "test-dw");
        workList.put("deviceState", "E");
        workList.put("format", "B64");
        workList.put("returnCode", "0000");
        // "AAAAAwoAAAMKAABwAAMAAAAAAAAAAAAB" is the data from the issue
        workList.put("returnData", "AAAAAwoAAAMKAABwAAMAAAAAAAAAAAAB");

        DishWasherSnapshot shot = Objects.requireNonNull(service).getMonitorData("test-dw", DeviceTypes.DISH_WASHER,
                cap, workList);

        assertNotNull(shot);
        assertTrue(shot.isOnline());
        // If it reached here without JsonParseException, the fix in LGThinQAbstractApiClientService worked.
    }

    private static class DishWasherTestService
            extends LGThinQAbstractApiClientService<DishWasherCapability, DishWasherSnapshot> {
        public DishWasherTestService(HttpClient httpClient) {
            super(DishWasherCapability.class, DishWasherSnapshot.class, httpClient);
        }

        // Expose protected method for testing
        @Override
        public @Nullable DishWasherSnapshot getMonitorData(String deviceId, DeviceTypes deviceType,
                DishWasherCapability deviceCapability, Map<String, Object> workList)
                throws LGThinqApiException, org.openhab.binding.lgthinq.lgservices.errors.LGThinqUnmarshallException,
                org.openhab.binding.lgthinq.lgservices.errors.LGThinqDeviceV1MonitorExpiredException {
            return super.getMonitorData(deviceId, deviceType, deviceCapability, workList);
        }

        @Override
        public void turnDevicePower(String bridgeName, String deviceId, DevicePowerState newPowerState)
                throws LGThinqApiException {
        }

        @Override
        protected boolean beforeGetDataDevice(String bridgeName, String deviceId) {
            return false;
        }

        @Override
        protected RestResult sendCommand(String bridgeName, String deviceId, String controlPath, String controlKey,
                String command, String keyName, String value) throws Exception {
            return new RestResult("", 200);
        }

        @Override
        protected RestResult sendCommand(String bridgeName, String deviceId, String controlPath, String controlKey,
                String command, @Nullable String keyName, @Nullable String value, @Nullable ObjectNode extraNode)
                throws Exception {
            return new RestResult("", 200);
        }

        @Override
        protected Map<String, Object> handleGenericErrorResult(@Nullable RestResult resp) throws LGThinqApiException {
            return Collections.emptyMap();
        }
    }
}
