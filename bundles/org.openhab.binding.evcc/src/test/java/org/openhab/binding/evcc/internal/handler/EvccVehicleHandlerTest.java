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
package org.openhab.binding.evcc.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.evcc.internal.handler.routing.MessageRouter;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.type.ChannelTypeRegistry;

import com.google.gson.JsonElement;

@NonNullByDefault
public class EvccVehicleHandlerTest {

    private final Thing thing = mock(Thing.class);
    private final ChannelTypeRegistry channelTypeRegistry = mock(ChannelTypeRegistry.class);
    private final List<String> requestedUrls = new ArrayList<>();
    private EvccVehicleHandler handler = createHandler();

    private EvccVehicleHandler createHandler() {
        return new EvccVehicleHandler(thing, channelTypeRegistry) {
            @Override
            public void logUnknownChannelXmlAsync(String key, String itemType) {
            }

            @Nullable
            @Override
            protected Bridge getBridge() {
                return null;
            }

            @Override
            public void updateThing(Thing thing) {
            }

            @Override
            protected void performApiRequest(String url, String method, JsonElement payload) {
                requestedUrls.add(url);
            }
        };
    }

    @BeforeEach
    public void setup() {
        requestedUrls.clear();
        when(thing.getUID()).thenReturn(new ThingUID("test:thing:uid"));
        when(thing.getProperties()).thenReturn(Map.of("vehicleId", "vehicle_1", "type", "vehicle"));
        when(thing.getChannels()).thenReturn(new ArrayList<>());
        Configuration configuration = mock(Configuration.class);
        when(configuration.get("vehicleId")).thenReturn("vehicle_1");
        when(thing.getConfiguration()).thenReturn(configuration);
        handler = createHandler();
        EvccBridgeHandler bridgeHandler = mock(EvccBridgeHandler.class);
        when(bridgeHandler.getBaseURL()).thenReturn("http://evcc/api");
        when(bridgeHandler.getMessageRouter()).thenReturn(mock(MessageRouter.class));
        handler.bridgeHandler = bridgeHandler;
        handler.initialize();
    }

    @Test
    public void commandsUseVehicleApiPaths() {
        handler.handleCommand(new ChannelUID("test:thing:uid:vehicle-mode"),
                new org.openhab.core.library.types.StringType("smart"));
        handler.handleCommand(new ChannelUID("test:thing:uid:vehicle-soc"),
                new org.openhab.core.library.types.DecimalType(80));

        assertEquals(
                List.of("http://evcc/api/vehicles/vehicle_1/mode/smart", "http://evcc/api/vehicles/vehicle_1/soc/80"),
                requestedUrls);
    }
}
