/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.State;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The {@link EvccSiteHandlerTest} is responsible for testing the EvccPvHandler implementation
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EvccPvHandlerTest extends AbstractThingHandlerTestClass<EvccPvHandler> {

    private final JsonObject testState = new JsonObject();
    private final JsonObject testObject = new JsonObject();
    private final JsonObject verifyObject = new JsonObject();

    @Override
    protected EvccPvHandler createHandler() {
        return new EvccPvHandler(thing, channelTypeRegistry) {

            @Override
            protected void updateStatus(ThingStatus status, ThingStatusDetail detail) {
                lastThingStatus = status;
                lastThingStatusDetail = detail;
            }

            @Override
            protected void updateStatus(ThingStatus status) {
                lastThingStatus = status;
            }

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
            protected void updateState(ChannelUID channelUID, State state) {
            }
        };
    }

    @BeforeEach
    public void setup() {
        when(thing.getUID()).thenReturn(new ThingUID("test:thing:uid"));
        when(thing.getProperties()).thenReturn(Map.of("index", "0", "type", "pv"));
        when(thing.getChannels()).thenReturn(new ArrayList<>());
        Configuration configuration = mock(Configuration.class);
        when(configuration.get("index")).thenReturn("0");
        when(configuration.get("id")).thenReturn("vehicle_1");
        when(thing.getConfiguration()).thenReturn(configuration);
        handler = spy(createHandler());

        verifyObject.addProperty("power", 2000);

        testObject.addProperty("power", 2000);
        JsonArray loadpointArray = new JsonArray();
        loadpointArray.add(testObject);
        testState.add("pv", loadpointArray);
    }

    @SuppressWarnings("null")
    @Test
    public void testInitializeWithBridgeHandlerWithValidState() {
        EvccBridgeHandler bridgeHandler = mock(EvccBridgeHandler.class);
        handler.bridgeHandler = bridgeHandler;
        when(bridgeHandler.getCachedEvccState()).thenReturn(testState);

        handler.initialize();
        assertSame(ThingStatus.ONLINE, lastThingStatus);
    }

    @SuppressWarnings("null")
    @Test
    public void testPrepareApiResponseForChannelStateUpdateIsInitialized() {
        handler.bridgeHandler = mock(EvccBridgeHandler.class);
        handler.isInitialized = true;

        handler.prepareApiResponseForChannelStateUpdate(testState);
        assertSame(ThingStatus.ONLINE, lastThingStatus);
    }

    @SuppressWarnings("null")
    @Test
    public void testPrepareApiResponseForChannelStateUpdateIsNotInitialized() {
        handler.bridgeHandler = mock(EvccBridgeHandler.class);

        handler.prepareApiResponseForChannelStateUpdate(testState);
        assertSame(ThingStatus.UNKNOWN, lastThingStatus);
    }

    @SuppressWarnings("null")
    @Test
    public void testGetStateFromCachedState() {
        JsonObject result = handler.getStateFromCachedState(testState);
        assertSame(testState.getAsJsonArray("pv").get(0), result);
    }
}
