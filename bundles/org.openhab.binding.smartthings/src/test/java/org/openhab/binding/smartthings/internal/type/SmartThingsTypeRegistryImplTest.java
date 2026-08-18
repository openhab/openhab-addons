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
package org.openhab.binding.smartthings.internal.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.smartthings.internal.SmartThingsBindingConstants;
import org.openhab.binding.smartthings.internal.dto.SmartThingsCapability;
import org.openhab.binding.smartthings.internal.dto.SmartThingsComponent;
import org.openhab.binding.smartthings.internal.dto.SmartThingsDevice;
import org.openhab.core.config.core.ConfigDescription;
import org.openhab.core.config.core.ConfigDescriptionParameter;
import org.openhab.core.config.core.ConfigDescriptionParameter.Type;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ThingType;

/**
 * Tests for dynamic SmartThings type UID helpers.
 */
@NonNullByDefault
class SmartThingsTypeRegistryImplTest {
    private static final URI DYNAMIC_DEVICE_CONFIG_DESCRIPTION_URI = URI
            .create("thing-type:smartthings:dynamic-device");

    @Test
    void getChannelGroupIdMatchesDynamicTypeGeneration() {
        assertEquals("Sound_Bar_main_audioVolume",
                SmartThingsTypeRegistryImpl.getChannelGroupId("Sound_Bar", "main", "audioVolume"));
    }

    @Test
    void getChannelGroupIdIncludesCapabilityNamespace() {
        assertEquals("Sound_Bar_main_custom_soundmode",
                SmartThingsTypeRegistryImpl.getChannelGroupId("Sound_Bar", "main", "custom.soundmode"));
    }

    @Test
    void generatedThingTypeConfigDescriptionIncludesDeviceIdParameter() {
        SmartThingsTypeRegistryImpl registry = new SmartThingsTypeRegistryImpl();
        SmartThingsConfigDescriptionProviderImpl configDescriptionProvider = new SmartThingsConfigDescriptionProviderImpl();
        registry.setThingTypeProvider(new SmartThingsThingTypeProviderImpl());
        registry.setConfigDescriptionProvider(configDescriptionProvider);

        registry.register("washer", "Dynamic_Washer", createDevice());

        ConfigDescription configDescription = configDescriptionProvider
                .getConfigDescription(DYNAMIC_DEVICE_CONFIG_DESCRIPTION_URI, null);
        assertNotNull(configDescription);

        Optional<ConfigDescriptionParameter> deviceIdParameter = configDescription.getParameters().stream()
                .filter(parameter -> SmartThingsBindingConstants.DEVICE_ID.equals(parameter.getName())).findFirst();
        assertNotNull(deviceIdParameter.orElse(null));
        assertEquals(Type.TEXT, deviceIdParameter.get().getType());
        assertTrue(deviceIdParameter.get().isRequired());
    }

    @Test
    void generatedThingTypeUsesSharedDynamicDeviceConfigDescriptionUri() {
        SmartThingsTypeRegistryImpl registry = new SmartThingsTypeRegistryImpl();
        SmartThingsThingTypeProviderImpl thingTypeProvider = new SmartThingsThingTypeProviderImpl();
        SmartThingsConfigDescriptionProviderImpl configDescriptionProvider = new SmartThingsConfigDescriptionProviderImpl();
        registry.setThingTypeProvider(thingTypeProvider);
        registry.setConfigDescriptionProvider(configDescriptionProvider);

        registry.register("vacuum", "Robot_Vacuum", createDevice());

        ThingType thingType = thingTypeProvider
                .getInternalThingType(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Robot_Vacuum"));
        assertNotNull(thingType);
        assertEquals(DYNAMIC_DEVICE_CONFIG_DESCRIPTION_URI, thingType.getConfigDescriptionURI());
        assertNotNull(configDescriptionProvider.getConfigDescription(DYNAMIC_DEVICE_CONFIG_DESCRIPTION_URI, null));
        assertNull(configDescriptionProvider.getConfigDescription(URI.create("thing-type:smartthings:Robot_Vacuum"),
                null));
    }

    @Test
    void generatedThingTypeDoesNotCollideWithStaticThingTypeUid() {
        SmartThingsTypeRegistryImpl registry = new SmartThingsTypeRegistryImpl();
        SmartThingsThingTypeProviderImpl thingTypeProvider = new SmartThingsThingTypeProviderImpl();
        registry.setThingTypeProvider(thingTypeProvider);

        registry.register("air_conditioner", "Samsung_Room_A_C", createDevice());

        assertNull(thingTypeProvider.getInternalThingType(SmartThingsBindingConstants.THING_TYPE_SAMSUNG_ROOM_A_C));
    }

    private SmartThingsDevice createDevice() {
        SmartThingsCapability capability = new SmartThingsCapability();
        capability.id = "switch";

        SmartThingsComponent component = new SmartThingsComponent();
        component.id = "main";
        component.capabilities = new SmartThingsCapability[] { capability };

        SmartThingsDevice device = new SmartThingsDevice();
        device.deviceId = "device-123";
        device.label = "Dynamic Washer";
        device.components = new SmartThingsComponent[] { component };
        return device;
    }
}
