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
package org.openhab.binding.evcc.internal.discovery.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.JSON_KEY_BATTERY;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.JSON_KEY_DEVICES;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.JSON_KEY_TITLE;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.PROPERTY_INDEX;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.PROPERTY_TITLE;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.THING_TYPE_BATTERY;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.evcc.internal.handler.EvccBridgeHandler;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The {@link BatteryDiscoveryMapperTest} is responsible for testing the BatteryDiscoveryMapper implementation
 *
 * @author Marcel Goerentz - Initial contribution
 */
@SuppressWarnings("null")
@NonNullByDefault
public class BatteryDiscoveryMapperTest {

    private final EvccBridgeHandler bridgeHandler = mock(EvccBridgeHandler.class);
    private final Bridge bridge = mock(Bridge.class);

    @Test
    void discoverShouldPreserveTitleCaseForBatteries() {
        JsonObject state = new JsonObject();
        JsonObject battery = new JsonObject();
        JsonArray devices = new JsonArray();
        JsonObject device = new JsonObject();
        device.addProperty(JSON_KEY_TITLE, "LG ESS Home 8");
        device.addProperty("power", -222);
        device.addProperty("capacity", 8.82);
        device.addProperty("soc", 36.6);
        device.addProperty("controllable", true);
        devices.add(device);
        battery.add(JSON_KEY_DEVICES, devices);
        state.add(JSON_KEY_BATTERY, battery);

        when(bridgeHandler.getThing()).thenReturn(bridge);
        when(bridge.getUID()).thenReturn(new ThingUID("evcc:server:dummy"));

        BatteryDiscoveryMapper mapper = new BatteryDiscoveryMapper();
        Collection<DiscoveryResult> results = mapper.discover(state, bridgeHandler);

        DiscoveryResult result = results.iterator().next();
        assertEquals("LG ESS Home 8", result.getLabel());
        assertEquals(THING_TYPE_BATTERY, result.getThingTypeUID());
        assertEquals(0, result.getProperties().get(PROPERTY_INDEX));
        assertEquals("LG ESS Home 8", result.getProperties().get(PROPERTY_TITLE));
    }

    @Test
    void discoverShouldUseFallbackLabelForBatteriesWithoutTitle() {
        JsonObject state = new JsonObject();
        JsonObject battery = new JsonObject();
        JsonArray devices = new JsonArray();
        JsonObject device = new JsonObject();
        device.addProperty("power", 0);
        device.addProperty("capacity", 13.4);
        device.addProperty("soc", 55);
        device.addProperty("controllable", true);
        devices.add(device);
        battery.add(JSON_KEY_DEVICES, devices);
        state.add(JSON_KEY_BATTERY, battery);

        when(bridgeHandler.getThing()).thenReturn(bridge);
        when(bridge.getUID()).thenReturn(new ThingUID("evcc:server:dummy"));

        BatteryDiscoveryMapper mapper = new BatteryDiscoveryMapper();
        Collection<DiscoveryResult> results = mapper.discover(state, bridgeHandler);

        DiscoveryResult result = results.iterator().next();
        assertEquals("battery0", result.getLabel());
        assertEquals(THING_TYPE_BATTERY, result.getThingTypeUID());
        assertEquals(0, result.getProperties().get(PROPERTY_INDEX));
        assertEquals("battery0", result.getProperties().get(PROPERTY_TITLE));
    }
}
