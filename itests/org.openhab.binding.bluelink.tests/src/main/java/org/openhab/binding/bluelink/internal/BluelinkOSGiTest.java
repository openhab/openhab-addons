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
package org.openhab.binding.bluelink.internal;

import static org.eclipse.jdt.annotation.Checks.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.openhab.binding.bluelink.internal.BluelinkBindingConstants.THING_TYPE_ACCOUNT;
import static org.openhab.binding.bluelink.internal.BluelinkBindingConstants.THING_TYPE_VEHICLE;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.bluelink.internal.api.BluelinkApi;
import org.openhab.binding.bluelink.internal.dto.VehicleInfo;
import org.openhab.binding.bluelink.internal.handler.BluelinkAccountHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.test.java.JavaOSGiTest;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;

/**
 * OSGi integration tests for the Bluelink binding.
 *
 * @author Marcus Better - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
public class BluelinkOSGiTest extends JavaOSGiTest {

    private static final String TEST_VIN = "VIN1234";

    private @NonNullByDefault({}) ThingRegistry thingRegistry;
    private @Mock @NonNullByDefault({}) BluelinkApi mockApi;

    @BeforeEach
    public void setUp() {
        thingRegistry = getService(ThingRegistry.class, ThingRegistry.class);
        assertNotNull(thingRegistry);
    }

    @Test
    public void testBridgeCreation() {
        final Configuration bridgeConfig = new Configuration(
                Map.of("username", "test@example.com", "password", "testpassword"));
        final Bridge bridge = requireNonNull(
                (Bridge) thingRegistry.createThingOfType(THING_TYPE_ACCOUNT,
                        new ThingUID(THING_TYPE_ACCOUNT, "testAccount"), null, "testaccount", bridgeConfig),
                "Bridge is null");
        thingRegistry.add(bridge);

        // Bridge will be OFFLINE initially since we haven't mocked the API
        // This test verifies OSGi wiring is correct
        waitForAssert(() -> assertNotNull(bridge.getHandler()));
    }

    @Test
    public void testBridgeGoesOnlineWithMockedApi() throws Exception {
        // Setup mock API
        when(mockApi.login()).thenReturn(true);
        when(mockApi.getVehicles())
                .thenReturn(List.of(new VehicleInfo("REG123", "My Car", TEST_VIN, "E", "IONIQ5", "2", 1000.0)));

        final Configuration bridgeConfig = new Configuration(
                Map.of("username", "test@example.com", "password", "testpassword"));
        final Bridge bridge = requireNonNull(
                (Bridge) thingRegistry.createThingOfType(THING_TYPE_ACCOUNT,
                        new ThingUID(THING_TYPE_ACCOUNT, "testAccount2"), null, "testaccount", bridgeConfig),
                "Bridge is null");
        thingRegistry.add(bridge);

        waitForAssert(() -> assertNotNull(bridge.getHandler()));

        final BluelinkAccountHandler accountHandler = (BluelinkAccountHandler) bridge.getHandler();
        assertNotNull(accountHandler);

        // Inject mock API using reflection
        injectApi(accountHandler, mockApi);

        // Trigger initialization with mock
        accountHandler.initialize();

        // Wait for handler to go online
        waitForAssert(() -> assertEquals(ThingStatus.ONLINE, bridge.getStatus()));
    }

    @Test
    public void testVehicleCreation() {
        final Configuration bridgeConfig = new Configuration(
                Map.of("username", "test@example.com", "password", "testpassword"));
        final Bridge bridge = requireNonNull(
                (Bridge) thingRegistry.createThingOfType(THING_TYPE_ACCOUNT,
                        new ThingUID(THING_TYPE_ACCOUNT, "testAccount3"), null, "testaccount", bridgeConfig),
                "Bridge is null");
        thingRegistry.add(bridge);

        final Configuration vehicleConfig = new Configuration(Map.of("vin", TEST_VIN));
        final Thing vehicle = requireNonNull(thingRegistry.createThingOfType(THING_TYPE_VEHICLE,
                new ThingUID(THING_TYPE_VEHICLE, "testVehicle"), bridge.getUID(), "testvehicle", vehicleConfig),
                "Vehicle is null");
        thingRegistry.add(vehicle);

        waitForAssert(() -> assertNotNull(vehicle.getHandler()));
    }

    private void injectApi(BluelinkAccountHandler handler, BluelinkApi api) {
        try {
            Field apiField = BluelinkAccountHandler.class.getDeclaredField("api");
            apiField.setAccessible(true);
            apiField.set(handler, api);
        } catch (Exception e) {
            throw new AssertionError("Failed to inject mock API", e);
        }
    }
}
