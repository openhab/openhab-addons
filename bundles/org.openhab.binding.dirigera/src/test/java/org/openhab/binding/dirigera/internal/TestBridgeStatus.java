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
package org.openhab.binding.dirigera.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.openhab.binding.dirigera.internal.Constants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.dirigera.internal.handler.DirigeraBridgeProvider;
import org.openhab.binding.dirigera.internal.handler.sensor.MotionSensorHandler;
import org.openhab.binding.dirigera.internal.mock.CallbackMock;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.internal.ThingImpl;

/**
 * {@link TestBridgeStatus} Tests different bridge modes for device initialization
 *
 * @author Bernd Weymann - Initial Contribution
 */
@NonNullByDefault
class TestBridgeStatus {
    private static final ThingStatusInfo INITIALIZING = new ThingStatusInfo(ThingStatus.INITIALIZING,
            ThingStatusDetail.NONE, null);
    private static final ThingStatusInfo UNKNOWN = new ThingStatusInfo(ThingStatus.UNKNOWN, ThingStatusDetail.NONE,
            null);
    private static final ThingStatusInfo ONLINE = new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
    private static final ThingStatusInfo OFFLINE_CONFIG_ERROR = new ThingStatusInfo(ThingStatus.OFFLINE,
            ThingStatusDetail.CONFIGURATION_ERROR, null);

    private static Stream<Arguments> testBridgeStatusChanges() {
        return Stream.of(//
                Arguments.of(UNKNOWN, INITIALIZING, UNKNOWN), // Bridge not ready yet
                Arguments.of(ONLINE, OFFLINE_CONFIG_ERROR, OFFLINE_CONFIG_ERROR), // Stay at config error, no init shall
                                                                                  // happen
                Arguments.of(ONLINE, INITIALIZING, ONLINE) // Go from init straight to ONLINE
        );
    }

    @ParameterizedTest
    @MethodSource
    void testBridgeStatusChanges(ThingStatusInfo bridgeStatusInfo, ThingStatusInfo handlerStatusInfo,
            ThingStatusInfo expectedStatusInfo) {
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge();
        hubBridge.setStatusInfo(bridgeStatusInfo);

        ThingImpl thing = new ThingImpl(THING_TYPE_MOTION_SENSOR, "test-device");
        thing.setBridgeUID(hubBridge.getBridgeUID());
        MotionSensorHandler handler = new MotionSensorHandler(thing, MOTION_SENSOR_MAP);
        CallbackMock thingCallback = new CallbackMock();
        thingCallback.setBridge(hubBridge);
        handler.setCallback(thingCallback);

        // set the right id
        Map<String, Object> config = new HashMap<>();
        config.put("id", "ee61c57f-8efa-44f4-ba8a-d108ae054138_1");
        handler.handleConfigurationUpdate(config);

        hubBridge.setStatusInfo(bridgeStatusInfo);
        thingCallback.statusUpdated(thing, handlerStatusInfo);
        handler.initialize();
        thingCallback.waitForStatus(expectedStatusInfo.getStatus());
        assertEquals(expectedStatusInfo.getStatus(), thingCallback.getStatus().getStatus(), "Status Check");
        assertEquals(expectedStatusInfo.getStatusDetail(), thingCallback.getStatus().getStatusDetail(),
                "Status Detail Check");
    }
}
