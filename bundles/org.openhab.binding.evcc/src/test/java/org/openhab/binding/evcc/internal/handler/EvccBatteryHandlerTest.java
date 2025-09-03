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
import static org.mockito.Mockito.when;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The {@link EvccBatteryHandlerTest} is responsible for testing the EvccBatteryHandler implementation
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EvccBatteryHandlerTest extends AbstractThingHandlerTestClass<EvccBatteryHandler> {

    @Override
    protected EvccBatteryHandler createHandler() {
        return new EvccBatteryHandler(thing, channelTypeRegistry) {
            protected void updateStatus(ThingStatus status, ThingStatusDetail detail) {
                lastThingStatus = status;
                lastThingStatusDetail = detail;
            }

            protected void updateStatus(ThingStatus status) {
                lastThingStatus = status;
            }

            public void logUnknownChannelXmlAsync(String key, String itemType) {
            }
        };
    }

    @SuppressWarnings("null")
    @Test
    public void testInitializeWithBridgeHandlerWithValidState() {
        EvccBridgeHandler bridgeHandler = mock(EvccBridgeHandler.class);
        handler.bridgeHandler = bridgeHandler;

        JsonObject batteryState = new JsonObject();
        batteryState.addProperty("soc", 50);
        JsonArray batteryArray = new JsonArray();
        batteryArray.add(batteryState);
        JsonObject state = new JsonObject();
        state.add("battery", batteryArray);

        when(bridgeHandler.getCachedEvccState()).thenReturn(state);

        handler.initialize();
        assertSame(ThingStatus.ONLINE, lastThingStatus);
    }

    @SuppressWarnings("null")
    @Test
    public void testPrepareApiResponseForChannelStateUpdateIsInitialized() {
        EvccBridgeHandler bridgeHandler = mock(EvccBridgeHandler.class);
        handler.bridgeHandler = bridgeHandler;
        handler.isInitialized = true;

        JsonObject batteryState = new JsonObject();
        batteryState.addProperty("soc", 50);
        JsonArray batteryArray = new JsonArray();
        batteryArray.add(batteryState);
        JsonObject state = new JsonObject();
        state.add("battery", batteryArray);

        handler.prepareApiResponseForChannelStateUpdate(state);
        assertSame(ThingStatus.ONLINE, lastThingStatus);
    }

    @SuppressWarnings("null")
    @Test
    public void testPrepareApiResponseForChannelStateUpdateIsNotInitialized() {
        EvccBridgeHandler bridgeHandler = mock(EvccBridgeHandler.class);
        handler.bridgeHandler = bridgeHandler;

        JsonObject batteryState = new JsonObject();
        batteryState.addProperty("soc", 50);
        JsonArray batteryArray = new JsonArray();
        batteryArray.add(batteryState);
        JsonObject state = new JsonObject();
        state.add("battery", batteryArray);

        handler.prepareApiResponseForChannelStateUpdate(state);
        assertSame(ThingStatus.UNKNOWN, lastThingStatus);
    }
}
