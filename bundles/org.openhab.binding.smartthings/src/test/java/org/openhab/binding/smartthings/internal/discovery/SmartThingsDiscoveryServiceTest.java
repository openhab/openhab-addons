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
package org.openhab.binding.smartthings.internal.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.smartthings.internal.SmartThingsBindingConstants;
import org.openhab.binding.smartthings.internal.dto.SmartThingsCategory;
import org.openhab.binding.smartthings.internal.dto.SmartThingsComponent;
import org.openhab.binding.smartthings.internal.dto.SmartThingsDevice;
import org.openhab.binding.smartthings.internal.handler.SmartThingsBridgeHandler;
import org.openhab.binding.smartthings.internal.type.SmartThingsTypeRegistry;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.binding.builder.BridgeBuilder;

/**
 * Tests for {@link SmartThingsDiscoveryService}.
 */
@NonNullByDefault
class SmartThingsDiscoveryServiceTest {

    @Test
    void registerDeviceUsesStaticThingTypeWhenDynamicThingsAreDisabled() {
        SmartThingsTypeRegistry typeRegistry = mock(SmartThingsTypeRegistry.class);
        TestDiscoveryService discoveryService = createDiscoveryService(typeRegistry, false);
        SmartThingsDevice device = createTelevisionDevice();

        discoveryService.registerDevice(device, true);

        DiscoveryResult result = discoveryService.discoveryResult;
        assertNotNull(result);
        assertEquals("smartthings:Samsung_The_Frame:account:Samsung_The_Frame", result.getThingUID().getAsString());
        verifyNoInteractions(typeRegistry);
    }

    @Test
    void registerDeviceUsesGenericTelevisionThingTypeForOtherTelevisions() {
        SmartThingsTypeRegistry typeRegistry = mock(SmartThingsTypeRegistry.class);
        TestDiscoveryService discoveryService = createDiscoveryService(typeRegistry, false);
        SmartThingsDevice device = createDevice("Television", "Samsung TV", "Living Room TV");

        discoveryService.registerDevice(device, true);

        DiscoveryResult result = discoveryService.discoveryResult;
        assertNotNull(result);
        assertEquals("smartthings:generic-television:account:Living_Room_TV", result.getThingUID().getAsString());
        verifyNoInteractions(typeRegistry);
    }

    @Test
    void registerDeviceUsesStaticThingTypeForFrameDeviceTypeName() {
        SmartThingsTypeRegistry typeRegistry = mock(SmartThingsTypeRegistry.class);
        TestDiscoveryService discoveryService = createDiscoveryService(typeRegistry, false);
        SmartThingsDevice device = createDevice("Television", "Samsung TV", "Living Room TV");
        device.name = null;
        device.deviceTypeName = "Samsung The Frame";

        discoveryService.registerDevice(device, true);

        DiscoveryResult result = discoveryService.discoveryResult;
        assertNotNull(result);
        assertEquals("smartthings:Samsung_The_Frame:account:Living_Room_TV", result.getThingUID().getAsString());
        verifyNoInteractions(typeRegistry);
    }

    @Test
    void registerDeviceUsesStaticThingTypeForKnownDeviceWhenDynamicThingsAreEnabled() {
        SmartThingsTypeRegistry typeRegistry = mock(SmartThingsTypeRegistry.class);
        TestDiscoveryService discoveryService = createDiscoveryService(typeRegistry, true);
        SmartThingsDevice device = createAirConditionerDevice();

        discoveryService.registerDevice(device, true);

        DiscoveryResult result = discoveryService.discoveryResult;
        assertNotNull(result);
        assertEquals("smartthings:Samsung_Room_A_C:account:Raumklimaanlage_Enzo", result.getThingUID().getAsString());
        verifyNoInteractions(typeRegistry);
    }

    @Test
    void registerDeviceUsesStaticThingTypeForOven() {
        SmartThingsTypeRegistry typeRegistry = mock(SmartThingsTypeRegistry.class);
        TestDiscoveryService discoveryService = createDiscoveryService(typeRegistry, false);
        SmartThingsDevice device = createDevice("Oven", "Samsung Oven", "Kitchen Oven");

        discoveryService.registerDevice(device, true);

        DiscoveryResult result = discoveryService.discoveryResult;
        assertNotNull(result);
        assertEquals("smartthings:Samsung_Oven:account:Kitchen_Oven", result.getThingUID().getAsString());
        verifyNoInteractions(typeRegistry);
    }

    @Test
    void registerDeviceUsesStaticThingTypeForSoundbar() {
        SmartThingsTypeRegistry typeRegistry = mock(SmartThingsTypeRegistry.class);
        TestDiscoveryService discoveryService = createDiscoveryService(typeRegistry, false);
        SmartThingsDevice device = createDevice("Soundbar", "Samsung Soundbar", "Living Room Soundbar");

        discoveryService.registerDevice(device, true);

        DiscoveryResult result = discoveryService.discoveryResult;
        assertNotNull(result);
        assertEquals("smartthings:Samsung_Soundbar:account:Living_Room_Soundbar", result.getThingUID().getAsString());
        verifyNoInteractions(typeRegistry);
    }

    @Test
    void registerDeviceUsesDynamicThingTypeForUnknownDeviceWhenDynamicThingsAreEnabled() {
        SmartThingsTypeRegistry typeRegistry = mock(SmartThingsTypeRegistry.class);
        TestDiscoveryService discoveryService = createDiscoveryService(typeRegistry, true);
        SmartThingsDevice device = createUnknownDevice();

        discoveryService.registerDevice(device, true);

        DiscoveryResult result = discoveryService.discoveryResult;
        assertNotNull(result);
        assertEquals("smartthings:Robot_Vacuum:account:Robot_Vacuum", result.getThingUID().getAsString());
        verify(typeRegistry).register("vacuum", "Robot_Vacuum", device);
    }

    private TestDiscoveryService createDiscoveryService(SmartThingsTypeRegistry typeRegistry,
            boolean useDynamicThings) {
        TestDiscoveryService discoveryService = new TestDiscoveryService();
        SmartThingsBridgeHandler bridgeHandler = mock(SmartThingsBridgeHandler.class);
        when(bridgeHandler.getThing()).thenReturn(accountBridge());
        when(bridgeHandler.useDynamicThings()).thenReturn(useDynamicThings);
        discoveryService.setSmartThingsTypeRegistry(typeRegistry);
        discoveryService.setThingHandler(bridgeHandler);
        return discoveryService;
    }

    private Bridge accountBridge() {
        return BridgeBuilder.create(SmartThingsBindingConstants.THING_TYPE_ACCOUNT, "account").build();
    }

    private SmartThingsDevice createTelevisionDevice() {
        return createDevice("Television", "Samsung The Frame", "Samsung The Frame");
    }

    private SmartThingsDevice createAirConditionerDevice() {
        return createDevice("Air Conditioner", "Samsung Room A/C", "Raumklimaanlage Enzo");
    }

    private SmartThingsDevice createUnknownDevice() {
        return createDevice("Vacuum", "Robot Vacuum", "Robot Vacuum");
    }

    private SmartThingsDevice createDevice(String categoryName, String name, String label) {
        SmartThingsCategory category = new SmartThingsCategory();
        category.name = categoryName;

        SmartThingsComponent component = new SmartThingsComponent();
        component.id = SmartThingsBindingConstants.GROUP_ID_MAIN;
        component.categories = new SmartThingsCategory[] { category };

        SmartThingsDevice device = new SmartThingsDevice();
        device.deviceId = "device-123";
        device.name = name;
        device.label = label;
        device.components = new SmartThingsComponent[] { component };
        return device;
    }

    private static class TestDiscoveryService extends SmartThingsDiscoveryService {
        private @Nullable DiscoveryResult discoveryResult;

        @Override
        protected void thingDiscovered(DiscoveryResult discoveryResult) {
            this.discoveryResult = discoveryResult;
        }
    }
}
