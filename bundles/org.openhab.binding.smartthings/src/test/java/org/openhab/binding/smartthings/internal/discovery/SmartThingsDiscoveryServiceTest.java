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
        assertEquals("smartthings:generic-television:account:Samsung_The_Frame", result.getThingUID().getAsString());
        verifyNoInteractions(typeRegistry);
    }

    @Test
    void registerDeviceUsesDynamicThingTypeWhenDynamicThingsAreEnabled() {
        SmartThingsTypeRegistry typeRegistry = mock(SmartThingsTypeRegistry.class);
        TestDiscoveryService discoveryService = createDiscoveryService(typeRegistry, true);
        SmartThingsDevice device = createTelevisionDevice();

        discoveryService.registerDevice(device, true);

        DiscoveryResult result = discoveryService.discoveryResult;
        assertNotNull(result);
        assertEquals("smartthings:dynamic-Samsung_The_Frame:account:Samsung_The_Frame",
                result.getThingUID().getAsString());
        verify(typeRegistry).register("television", "Samsung_The_Frame", device);
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
        SmartThingsCategory category = new SmartThingsCategory();
        category.name = "Television";

        SmartThingsComponent component = new SmartThingsComponent();
        component.id = SmartThingsBindingConstants.GROUP_ID_MAIN;
        component.categories = new SmartThingsCategory[] { category };

        SmartThingsDevice device = new SmartThingsDevice();
        device.deviceId = "device-123";
        device.name = "Samsung The Frame";
        device.label = "Samsung The Frame";
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
