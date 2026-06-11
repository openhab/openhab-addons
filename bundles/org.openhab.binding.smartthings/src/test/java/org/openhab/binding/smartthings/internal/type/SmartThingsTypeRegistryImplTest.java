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

/**
 * Tests for dynamic SmartThings type UID helpers.
 */
@NonNullByDefault
class SmartThingsTypeRegistryImplTest {

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
                .getConfigDescription(URI.create("thing-type:smartthings:Dynamic_Washer"), null);
        assertNotNull(configDescription);

        Optional<ConfigDescriptionParameter> deviceIdParameter = configDescription.getParameters().stream()
                .filter(parameter -> SmartThingsBindingConstants.DEVICE_ID.equals(parameter.getName())).findFirst();
        assertNotNull(deviceIdParameter.orElse(null));
        assertEquals(Type.TEXT, deviceIdParameter.get().getType());
        assertTrue(deviceIdParameter.get().isRequired());
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
