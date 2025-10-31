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
import org.openhab.core.config.core.Configuration;
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

    private final JsonObject modifiedTestState = exampleResponse.deepCopy();
    private final JsonObject testObject = exampleResponse.getAsJsonArray("loadpoints").get(0).getAsJsonObject();
    private final JsonObject modifiedVerifyObject = verifyObject.deepCopy().getAsJsonArray("loadpoints").get(0)
            .getAsJsonObject();

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
        when(thing.getUID()).thenReturn(new ThingUID("test:thing:uid"));
        when(thing.getProperties()).thenReturn(Map.of("index", "0", "type", "loadpoint"));
        when(thing.getChannels()).thenReturn(new ArrayList<>());
        Configuration configuration = mock(Configuration.class);
        when(configuration.get("index")).thenReturn("0");
        when(configuration.get("id")).thenReturn("vehicle_1");
        when(thing.getConfiguration()).thenReturn(configuration);
        handler = spy(createHandler());

        modifiedVerifyObject.addProperty("chargedEnergy", 50);
        modifiedVerifyObject.addProperty(JSON_KEY_OFFERED_CURRENT, 6);
        modifiedVerifyObject.addProperty(JSON_KEY_CONNECTED, true);
        modifiedVerifyObject.addProperty(JSON_KEY_PHASES_CONFIGURED, "3");
        modifiedVerifyObject.addProperty("chargeCurrentL1", 6);
        modifiedVerifyObject.addProperty("chargeCurrentL2", 7);
        modifiedVerifyObject.addProperty("chargeCurrentL3", 8);
        modifiedVerifyObject.addProperty("chargeVoltageL1", 230.0);
        modifiedVerifyObject.addProperty("chargeVoltageL2", 231.0);
        modifiedVerifyObject.addProperty("chargeVoltageL3", 229.0);
        modifiedVerifyObject.remove(JSON_KEY_CHARGE_CURRENT);
        modifiedVerifyObject.remove(JSON_KEY_VEHICLE_PRESENT);
        modifiedVerifyObject.remove(JSON_KEY_PHASES);

        testObject.addProperty("chargedEnergy", 50);
        testObject.addProperty(JSON_KEY_CHARGE_CURRENT, 6);
        testObject.addProperty(JSON_KEY_VEHICLE_PRESENT, true);
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
        JsonArray loadpointArray = exampleResponse.getAsJsonArray("loadpoints");
        loadpointArray.set(0, testObject);
        modifiedTestState.add("loadpoints", loadpointArray);
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
    @Test
    public void testPrepareApiResponseForChannelStateUpdateIsInitialized() {
        handler.bridgeHandler = mock(EvccBridgeHandler.class);
        handler.isInitialized = true;

        handler.prepareApiResponseForChannelStateUpdate(exampleResponse);
        assertSame(ThingStatus.ONLINE, lastThingStatus);
    }

    @SuppressWarnings("null")
    @Test
    public void testPrepareApiResponseForChannelStateUpdateIsNotInitialized() {
        handler.bridgeHandler = mock(EvccBridgeHandler.class);

        handler.prepareApiResponseForChannelStateUpdate(exampleResponse);
        assertSame(ThingStatus.UNKNOWN, lastThingStatus);
    }

    @SuppressWarnings("null")
    @Test
    public void testJsonGetsModifiedCorrectly() {
        handler.prepareApiResponseForChannelStateUpdate(exampleResponse);
        assertEquals(modifiedVerifyObject, modifiedTestState.getAsJsonArray("loadpoints").get(0));
    }

    @SuppressWarnings("null")
    @Test
    public void testGetStateFromCachedState() {
        JsonObject result = handler.getStateFromCachedState(exampleResponse);
        assertSame(exampleResponse.getAsJsonArray("loadpoints").get(0), result);
    }
}
