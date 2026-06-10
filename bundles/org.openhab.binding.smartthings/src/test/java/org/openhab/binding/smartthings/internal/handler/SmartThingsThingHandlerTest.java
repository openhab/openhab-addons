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
package org.openhab.binding.smartthings.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.smartthings.internal.SmartThingsBindingConstants;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ThingBuilder;

/**
 * Tests for {@link SmartThingsThingHandler}.
 */
@NonNullByDefault
class SmartThingsThingHandlerTest {

    @Test
    void resolveDeviceIdFallsBackToDiscoveredThingProperty() {
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Washer"),
                        new ThingUID("smartthings:Washer:account:Washer"))
                .withProperties(Map.of(SmartThingsBindingConstants.DEVICE_ID, "device-123")).build();
        SmartThingsThingHandler handler = new SmartThingsThingHandler(thing);

        assertEquals("device-123", handler.resolveDeviceId());
    }

    @Test
    void resolveDeviceIdPrefersThingConfiguration() {
        Configuration config = new Configuration(Map.of(SmartThingsBindingConstants.DEVICE_ID, "configured-device"));
        Thing thing = ThingBuilder
                .create(new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Washer"),
                        new ThingUID("smartthings:Washer:account:Washer"))
                .withConfiguration(config)
                .withProperties(Map.of(SmartThingsBindingConstants.DEVICE_ID, "property-device")).build();
        SmartThingsThingHandler handler = new SmartThingsThingHandler(thing);

        assertEquals("configured-device", handler.resolveDeviceId());
    }
}
