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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.PROPERTY_FORECAST;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.PROPERTY_ID;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.PROPERTY_SUBTYPE;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.PROPERTY_TYPE;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.SUPPORTED_FORECAST_TYPES;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.evcc.internal.handler.EvccBatteryHandlerTest;
import org.openhab.binding.evcc.internal.handler.EvccBridgeHandler;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link ForecastDiscoveryMapperTest} is responsible for testing the ForecastDiscoveryMapper implementation
 *
 * @author Marcel Goerentz - Initial contribution
 */
@SuppressWarnings("null")
@NonNullByDefault
public class ForecastDiscoveryMapperTest {
    private static JsonObject exampleResponse = new JsonObject();
    private final EvccBridgeHandler bridgeHandler = mock(EvccBridgeHandler.class);
    private final Bridge bridge = mock(Bridge.class);
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
    void discoverShouldOnlyFindSupportedForecastTypes() {
        when(bridgeHandler.getThing()).thenReturn(bridge);
        when(bridge.getUID()).thenReturn(new ThingUID("evcc:server:dummy"));
        when(bundle.getBundleContext()).thenReturn(ctx);

        ForecastDiscoveryMapper mapper = new ForecastDiscoveryMapper();
        Collection<DiscoveryResult> results = mapper.discover(exampleResponse, bridgeHandler);
        for (DiscoveryResult result : results) {
            Map<String, Object> props = result.getProperties();
            assertEquals(PROPERTY_FORECAST, props.get(PROPERTY_TYPE));
            assertTrue(SUPPORTED_FORECAST_TYPES.contains((String) props.get(PROPERTY_SUBTYPE)));
            assertEquals(PROPERTY_ID, result.getRepresentationProperty());
            ThingUID expectedUID = new ThingUID("evcc:forecast:dummy:" + (String) props.get(PROPERTY_SUBTYPE));
            assertEquals(expectedUID, result.getThingUID());
        }
    }
}
