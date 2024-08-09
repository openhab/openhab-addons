/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.deconz.internal.Util;
import org.openhab.binding.deconz.internal.discovery.ThingDiscoveryService;
import org.openhab.binding.deconz.internal.dto.BridgeFullState;
import org.openhab.binding.deconz.internal.handler.DeconzBridgeHandler;
import org.openhab.binding.deconz.internal.types.GroupType;
import org.openhab.binding.deconz.internal.types.GroupTypeDeserializer;
import org.openhab.binding.deconz.internal.types.LightType;
import org.openhab.binding.deconz.internal.types.LightTypeDeserializer;
import org.openhab.binding.deconz.internal.types.ResourceType;
import org.openhab.binding.deconz.internal.types.ResourceTypeDeserializer;
import org.openhab.binding.deconz.internal.types.ThermostatMode;
import org.openhab.binding.deconz.internal.types.ThermostatModeGsonTypeAdapter;
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This class provides tests for deconz binding
 *
 * @author Jan N. Klug - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class DeconzTest {
    private @NonNullByDefault({}) Gson gson;

    private @Mock @NonNullByDefault({}) DiscoveryListener discoveryListener;
    private @Mock @NonNullByDefault({}) DeconzBridgeHandler bridgeHandler;
    private @Mock @NonNullByDefault({}) Bridge bridge;

    @BeforeEach
    public void initialize() {
        Mockito.doAnswer(answer -> bridge).when(bridgeHandler).getThing();
        Mockito.doAnswer(answer -> new ThingUID("deconz", "mybridge")).when(bridge).getUID();

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LightType.class, new LightTypeDeserializer());
        gsonBuilder.registerTypeAdapter(GroupType.class, new GroupTypeDeserializer());
        gsonBuilder.registerTypeAdapter(ResourceType.class, new ResourceTypeDeserializer());
        gsonBuilder.registerTypeAdapter(ThermostatMode.class, new ThermostatModeGsonTypeAdapter());
        gson = gsonBuilder.create();
    }

    @Test
    public void discoveryTest() throws IOException {
        BridgeFullState bridgeFullState = getObjectFromJson("discovery.json", BridgeFullState.class, gson);
        assertNotNull(bridgeFullState);
        assertEquals(6, bridgeFullState.lights.size());
        assertEquals(9, bridgeFullState.sensors.size());

        Mockito.doAnswer(answer -> CompletableFuture.completedFuture(Optional.of(bridgeFullState))).when(bridgeHandler)
                .getBridgeFullState();
        ThingDiscoveryService discoveryService = new ThingDiscoveryService();
        discoveryService.modified(Map.of(DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY, false));
        discoveryService.setThingHandler(bridgeHandler);
        discoveryService.initialize();
        discoveryService.addDiscoveryListener(discoveryListener);
        discoveryService.startScan();
        Mockito.verify(discoveryListener, times(20)).thingDiscovered(any(), any());
    }

    public static <T> T getObjectFromJson(String filename, Class<T> clazz, Gson gson) throws IOException {
        try (InputStream inputStream = DeconzTest.class.getResourceAsStream(filename)) {
            if (inputStream == null) {
                throw new IOException("inputstream is null");
            }
            byte[] bytes = inputStream.readAllBytes();
            if (bytes == null) {
                throw new IOException("Resulting byte-array empty");
            }
            String json = new String(bytes, StandardCharsets.UTF_8);
            return Objects.requireNonNull(gson.fromJson(json, clazz));
        }
    }

    @Test
    public void dateTimeConversionTest() {
        DateTimeType dateTime = Util.convertTimestampToDateTime("2020-08-22T11:09Z");
        assertEquals(new DateTimeType(ZonedDateTime.parse("2020-08-22T11:09:00Z")), dateTime);

        dateTime = Util.convertTimestampToDateTime("2020-08-22T11:09:47");
        assertEquals(new DateTimeType(ZonedDateTime.parse("2020-08-22T11:09:47Z")).toZone(ZoneId.systemDefault()),
                dateTime);
    }
}
