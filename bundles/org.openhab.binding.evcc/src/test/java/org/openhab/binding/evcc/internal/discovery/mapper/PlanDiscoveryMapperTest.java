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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.evcc.internal.handler.EvccBatteryHandlerTest;
import org.openhab.binding.evcc.internal.handler.EvccBridgeHandler;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link PlanDiscoveryMapperTest} is responsible for testing the PlanDiscoveryMapper implementation
 *
 * @author Marcel Goerentz - Initial contribution
 */
@SuppressWarnings("null")
@NonNullByDefault
public class PlanDiscoveryMapperTest {

    private static JsonObject exampleResponse = new JsonObject();
    private final EvccBridgeHandler bridgeHandler = mock(EvccBridgeHandler.class);
    private final Bridge bridge = mock(Bridge.class);
    private final LocaleProvider lp = mock(LocaleProvider.class);
    private final TranslationProvider tp = mock(TranslationProvider.class);
    private final Bundle bundle = mock(Bundle.class);
    private final BundleContext ctx = mock(BundleContext.class);

    @BeforeAll
    static void setUpOnce() {
        try (InputStream is = EvccBatteryHandlerTest.class.getClassLoader()
                .getResourceAsStream("responses/example_response.json")) {
            if (is == null) {
                throw new IllegalArgumentException("Couldn't find response file");
            }
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            exampleResponse = JsonParser.parseString(json).getAsJsonObject();
        } catch (IOException e) {
            fail("Failed to read example response file", e);
        }
    }

    @Test
    void oneTimeOnlyShouldDiscoverOneResult() throws NoSuchAlgorithmException {
        // vehicle JSON with only a "plan" section
        JsonObject state = exampleResponse.deepCopy();

        JsonObject vehicle = state.getAsJsonObject(JSON_KEY_VEHICLES).get("vehicle_2").getAsJsonObject();

        when(bridgeHandler.getThing()).thenReturn(bridge);
        when(bridge.getUID()).thenReturn(new ThingUID("evcc:server:dummy"));
        when(bundle.getBundleContext()).thenReturn(ctx);

        PlanDiscoveryMapper mapper = new PlanDiscoveryMapper(ctx, tp, lp);
        Collection<DiscoveryResult> results = mapper.discoverFromVehicle(vehicle, "vehicle_2", "My Car", bridgeHandler);

        assertEquals(1, results.size());
        DiscoveryResult r = results.iterator().next();
        Map<String, Object> props = r.getProperties();
        assertEquals("0", props.get(PROPERTY_INDEX));
        assertEquals("vehicle_2", props.get(PROPERTY_VEHICLE_ID));
        assertTrue(props.containsKey(PROPERTY_ID));
        assertEquals(THING_TYPE_PLAN, r.getThingTypeUID());
    }

    @Test
    void repeatingOnlyShouldDiscoverAllRepeatingIndices() throws NoSuchAlgorithmException {
        // vehicle JSON with only "repeatingPlans" (size = 2)
        JsonObject vehicle = new JsonObject();
        JsonArray repeating = new JsonArray();
        JsonObject p1 = new JsonObject();
        p1.addProperty("time", "07:30");
        JsonObject p2 = new JsonObject();
        p2.addProperty("time", "19:00");
        repeating.add(p1);
        repeating.add(p2);
        vehicle.add("repeatingPlans", repeating);

        when(bridgeHandler.getThing()).thenReturn(bridge);
        when(bridge.getUID()).thenReturn(new ThingUID("evcc:server:dummy"));
        when(bundle.getBundleContext()).thenReturn(ctx);

        PlanDiscoveryMapper mapper = new PlanDiscoveryMapper(ctx, tp, lp);
        Collection<DiscoveryResult> results = mapper.discoverFromVehicle(vehicle, "vehicle_1", "My Car", bridgeHandler);

        assertEquals(2, results.size());
        // indices should be 1..N (because 0 is one-time)
        for (DiscoveryResult r : results) {
            Map<String, Object> props = r.getProperties();
            assertTrue(props.get(PROPERTY_INDEX).equals("1") || props.get(PROPERTY_INDEX).equals("2"));
            assertEquals("vehicle_1", props.get(PROPERTY_VEHICLE_ID));
            assertTrue(props.containsKey(PROPERTY_ID));
            // UID must be of thing type "plan"
            assertEquals(THING_TYPE_PLAN, r.getThingTypeUID());
        }
    }
}
