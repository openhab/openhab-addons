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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The {@link EvccSiteHandlerTest} is responsible for testing the EvccSiteHandler implementation
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EvccSiteHandlerTest extends AbstractThingHandlerTestClass<EvccSiteHandler> {

    private final JsonObject gridConfigured = new JsonObject();
    private final JsonObject modifiedVerifyObject = verifyObject.deepCopy();

    @Override
    protected EvccSiteHandler createHandler() {
        return new EvccSiteHandler(thing, channelTypeRegistry) {

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
        };
    }

    @BeforeEach
    public void setup() {
        when(thing.getUID()).thenReturn(new ThingUID("test:thing:uid"));
        when(thing.getProperties()).thenReturn(Map.of("index", "0", "type", "pv"));
        when(thing.getChannels()).thenReturn(new ArrayList<>());
        handler = spy(createHandler());

        modifiedVerifyObject.addProperty("gridPower", 2000);
        modifiedVerifyObject.addProperty("gridEnergy", 10000);
        modifiedVerifyObject.addProperty("gridCurrentL1", 6);
        modifiedVerifyObject.addProperty("gridCurrentL2", 7);
        modifiedVerifyObject.addProperty("gridCurrentL3", 8);
        modifiedVerifyObject.addProperty("gridVoltageL1", 230.0);
        modifiedVerifyObject.addProperty("gridVoltageL2", 231.0);
        modifiedVerifyObject.addProperty("gridVoltageL3", 229.0);
        modifiedVerifyObject.remove("gridConfigured");
        modifiedVerifyObject.remove("grid");

        gridConfigured.addProperty("power", 2000);
        gridConfigured.addProperty("energy", 10000);
        JsonArray currents = new JsonArray();
        currents.add(6);
        currents.add(7);
        currents.add(8);
        gridConfigured.add("currents", currents);
        JsonArray voltages = new JsonArray();
        voltages.add(230.0);
        voltages.add(231.0);
        voltages.add(229.0);
        gridConfigured.add("voltages", voltages);
    }

    @SuppressWarnings("null")
    @Test
    public void testInitializeWithBridgeHandlerWithValidState() {
        EvccBridgeHandler bridgeHandler = mock(EvccBridgeHandler.class);
        handler.bridgeHandler = bridgeHandler;
        when(bridgeHandler.getCachedEvccState()).thenReturn(exampleResponse);

        handler.initialize();
        assertSame(ThingStatus.ONLINE, lastThingStatus);
    }

    @SuppressWarnings("null")
    @Nested
    public class TestPrepareApiResponseForChannelStateUpdate {

        @Test
        public void handlerIsInitialized() {
            handler.bridgeHandler = mock(EvccBridgeHandler.class);
            handler.isInitialized = true;

            handler.prepareApiResponseForChannelStateUpdate(exampleResponse);
            assertSame(ThingStatus.ONLINE, lastThingStatus);
        }

        @Test
        public void handlerIsNotInitialized() {
            handler.bridgeHandler = mock(EvccBridgeHandler.class);

            handler.prepareApiResponseForChannelStateUpdate(exampleResponse);
            assertSame(ThingStatus.UNKNOWN, lastThingStatus);
        }

        @Test
        public void stateContainsGridConfigured() {
            handler.bridgeHandler = mock(EvccBridgeHandler.class);

            exampleResponse.addProperty("gridConfigured", true);
            exampleResponse.add("grid", gridConfigured);
            handler.prepareApiResponseForChannelStateUpdate(exampleResponse);
            assertEquals(modifiedVerifyObject, exampleResponse);
        }
    }

    @SuppressWarnings("null")
    @Test
    public void testGetStateFromCachedState() {
        JsonObject result = handler.getStateFromCachedState(exampleResponse);
        assertSame(exampleResponse, result);
    }
}
