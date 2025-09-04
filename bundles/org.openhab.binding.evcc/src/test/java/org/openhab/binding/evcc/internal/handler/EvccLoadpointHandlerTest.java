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
import static org.openhab.binding.evcc.internal.EvccBindingConstants.*;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The {@link EvccLoadpointHandlerTest} is responsible for testing the EvccLoadpointHandler implementation
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EvccLoadpointHandlerTest extends AbstractThingHandlerTestClass<EvccLoadpointHandler> {

    private final JsonObject testState = new JsonObject();
    private final JsonObject testObject = new JsonObject();
    private final JsonObject verifyObject = new JsonObject();

    @Override
    protected EvccLoadpointHandler createHandler() {
        return new EvccLoadpointHandler(thing, channelTypeRegistry) {

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
        handler = spy(createHandler());
        when(thing.getUID()).thenReturn(new ThingUID("test:thing:uid"));
        when(thing.getProperties()).thenReturn(Map.of("index", "0", "type", "loadpoint"));
        when(thing.getChannels()).thenReturn(new ArrayList<>());

        verifyObject.addProperty("chargedEnergy", 50);
        verifyObject.addProperty(JSON_KEY_OFFERED_CURRENT, 6);
        verifyObject.addProperty(JSON_KEY_CONNECTED, true);
        verifyObject.addProperty(JSON_KEY_CHARGING, true);
        verifyObject.addProperty(JSON_KEY_PHASES_CONFIGURED, "3");
        verifyObject.addProperty("chargeCurrentL1", 6);
        verifyObject.addProperty("chargeCurrentL2", 7);
        verifyObject.addProperty("chargeCurrentL3", 8);
        verifyObject.addProperty("chargeVoltageL1", 230.0);
        verifyObject.addProperty("chargeVoltageL2", 231.0);
        verifyObject.addProperty("chargeVoltageL3", 229.0);

        testObject.addProperty("chargedEnergy", 50);
        testObject.addProperty(JSON_KEY_CHARGE_CURRENT, 6);
        testObject.addProperty(JSON_KEY_VEHICLE_PRESENT, true);
        testObject.addProperty(JSON_KEY_ENABLED, true);
        testObject.addProperty(JSON_KEY_PHASES, "3");
        JsonArray currents = new JsonArray();
        currents.add(6);
        currents.add(7);
        currents.add(8);
        testObject.add(JSON_KEY_CHARGE_CURRENTS, currents);
        JsonArray voltages = new JsonArray();
        voltages.add(230.0);
        voltages.add(231.0);
        voltages.add(229.0);
        testObject.add(JSON_KEY_CHARGE_VOLTAGES, voltages);
        JsonArray loadpointArray = new JsonArray();
        loadpointArray.add(testObject);
        testState.add("loadpoints", loadpointArray);
        testState.addProperty("version", "0.207.0");
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
    public void testJsonGetsModifiedCorrectly() {
        handler.prepareApiResponseForChannelStateUpdate(testState);
        assertEquals(verifyObject, testState.getAsJsonArray("loadpoints").get(0));
    }

    @SuppressWarnings("null")
    @Test
    public void testGetStateFromCachedState() {
        JsonObject result = handler.getStateFromCachedState(testState);
        assertSame(testState.getAsJsonArray("loadpoints").get(0), result);
    }
}
