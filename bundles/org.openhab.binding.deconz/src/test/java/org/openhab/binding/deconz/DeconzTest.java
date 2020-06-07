/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.deconz;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.discovery.DiscoveryListener;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openhab.binding.deconz.internal.discovery.ThingDiscoveryService;
import org.openhab.binding.deconz.internal.dto.BridgeFullState;
import org.openhab.binding.deconz.internal.handler.DeconzBridgeHandler;
import org.openhab.binding.deconz.internal.types.LightType;
import org.openhab.binding.deconz.internal.types.LightTypeDeserializer;
import org.openhab.binding.deconz.internal.types.ThermostatMode;
import org.openhab.binding.deconz.internal.types.ThermostatModeGsonTypeAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This class provides tests for deconz binding
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class DeconzTest {
    private @NonNullByDefault({}) Gson gson;

    @Mock
    private @NonNullByDefault({}) DiscoveryListener discoveryListener;

    @Mock
    private @NonNullByDefault({}) DeconzBridgeHandler bridgeHandler;

    @Mock
    private @NonNullByDefault({}) Bridge bridge;

    @Before
    public void initialize() {
        initMocks(this);

        Mockito.doAnswer(answer -> bridge).when(bridgeHandler).getThing();
        Mockito.doAnswer(answer -> new ThingUID("deconz", "mybridge")).when(bridge).getUID();

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LightType.class, new LightTypeDeserializer());
        gsonBuilder.registerTypeAdapter(ThermostatMode.class, new ThermostatModeGsonTypeAdapter());
        gson = gsonBuilder.create();
    }

    @Test
    public void discoveryTest() throws IOException {
        BridgeFullState bridgeFullState = getObjectFromJson("discovery.json", BridgeFullState.class, gson);
        Assert.assertNotNull(bridgeFullState);
        Assert.assertEquals(6, bridgeFullState.lights.size());
        Assert.assertEquals(9, bridgeFullState.sensors.size());

        ThingDiscoveryService discoveryService = new ThingDiscoveryService();
        discoveryService.setThingHandler(bridgeHandler);
        discoveryService.addDiscoveryListener(discoveryListener);

        discoveryService.stateRequestFinished(bridgeFullState);
        Mockito.verify(discoveryListener, times(15)).thingDiscovered(any(), any());
    }

    public static <T> T getObjectFromJson(String filename, Class<T> clazz, Gson gson) throws IOException {
        String json = IOUtils.toString(DeconzTest.class.getResourceAsStream(filename), StandardCharsets.UTF_8.name());
        return gson.fromJson(json, clazz);
    }
}
