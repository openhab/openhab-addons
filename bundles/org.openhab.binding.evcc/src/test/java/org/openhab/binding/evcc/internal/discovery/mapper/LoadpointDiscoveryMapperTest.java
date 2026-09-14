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
import static org.openhab.binding.evcc.internal.EvccBindingConstants.JSON_KEY_CHARGER_FEATURE_HEATING;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.JSON_KEY_LOADPOINTS;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.PROPERTY_INDEX;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.PROPERTY_TITLE;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.THING_TYPE_HEATING;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.THING_TYPE_LOADPOINT;

import java.util.Iterator;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.evcc.internal.handler.EvccBridgeHandler;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The {@link LoadpointDiscoveryMapperTest} is responsible for testing the LoadpointDiscoveryMapper implementation
 *
 * @author Marcel Goerentz - Initial contribution
 */
@SuppressWarnings("null")
@NonNullByDefault
public class LoadpointDiscoveryMapperTest {

    private final EvccBridgeHandler bridgeHandler = mock(EvccBridgeHandler.class);
    private final Bridge bridge = mock(Bridge.class);

    @Test
    void discoverShouldUseReadableFallbackLabelsForUntitledLoadpoints() {
        JsonObject state = new JsonObject();
        JsonArray loadpoints = new JsonArray();
        loadpoints.add(new JsonObject());
        JsonObject heating = new JsonObject();
        heating.addProperty(JSON_KEY_CHARGER_FEATURE_HEATING, true);
        loadpoints.add(heating);
        state.add(JSON_KEY_LOADPOINTS, loadpoints);

        when(bridgeHandler.getThing()).thenReturn(bridge);
        when(bridge.getUID()).thenReturn(new ThingUID("evcc:server:dummy"));

        LoadpointDiscoveryMapper mapper = new LoadpointDiscoveryMapper();
        Iterator<DiscoveryResult> iterator = mapper.discover(state, bridgeHandler).iterator();

        DiscoveryResult loadpointResult = iterator.next();
        assertEquals("Loadpoint 1", loadpointResult.getLabel());
        assertEquals(THING_TYPE_LOADPOINT, loadpointResult.getThingTypeUID());
        assertEquals(0, loadpointResult.getProperties().get(PROPERTY_INDEX));
        assertEquals("Loadpoint 1", loadpointResult.getProperties().get(PROPERTY_TITLE));

        DiscoveryResult heatingResult = iterator.next();
        assertEquals("Heating 2", heatingResult.getLabel());
        assertEquals(THING_TYPE_HEATING, heatingResult.getThingTypeUID());
        assertEquals(1, heatingResult.getProperties().get(PROPERTY_INDEX));
        assertEquals("Heating 2", heatingResult.getProperties().get(PROPERTY_TITLE));
    }
}
