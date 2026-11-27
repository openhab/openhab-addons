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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.*;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.binding.builder.ThingStatusInfoBuilder;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Tests the complete websocket update path (bridge → router → Thing handler → framework callback) and
 * verifies that channel and Thing status updates are only sent for values that actually changed.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
class EvccStateUpdateTest {

    private static final ThingUID BRIDGE_UID = new ThingUID(THING_TYPE_SERVER, "server");
    private static final String VEHICLE_ID = "car";

    private final ChannelTypeRegistry channelTypeRegistry = mock(ChannelTypeRegistry.class);
    private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
    private EvccBridgeHandler bridgeHandler = newBridgeHandler();
    private final Bridge bridge = mock(Bridge.class);
    private int loadpointDispatches;

    private static EvccBridgeHandler newBridgeHandler() {
        HttpClientFactory httpClientFactory = mock(HttpClientFactory.class);
        when(httpClientFactory.getCommonHttpClient()).thenReturn(mock(HttpClient.class));
        return new EvccBridgeHandler(mock(Bridge.class), httpClientFactory, mock(TranslationProvider.class),
                mock(LocaleProvider.class));
    }

    @BeforeEach
    void setUp() {
        bridgeHandler = newBridgeHandler();
        when(bridge.getUID()).thenReturn(BRIDGE_UID);
        when(bridge.getHandler()).thenReturn(bridgeHandler);
        when(callback.isChannelLinked(any())).thenReturn(true);
        // Mirror the framework, which stores the reported status on the Thing.
        doAnswer(invocation -> {
            Thing thing = invocation.getArgument(0);
            thing.setStatusInfo(invocation.getArgument(1));
            return null;
        }).when(callback).statusUpdated(any(), any());
    }

    private static Thing buildThing(ThingTypeUID thingTypeUID, String id, Map<String, String> properties,
            Map<String, Object> configuration, String... channelIds) {
        ThingUID thingUID = new ThingUID(thingTypeUID, BRIDGE_UID, id);
        List<Channel> channels = java.util.Arrays.stream(channelIds)
                .map(channelId -> ChannelBuilder.create(new ChannelUID(thingUID, channelId)).build()).toList();
        return ThingBuilder.create(thingTypeUID, thingUID).withBridge(BRIDGE_UID).withProperties(properties)
                .withConfiguration(new Configuration(configuration)).withChannels(channels).build();
    }

    private EvccLoadpointHandler startLoadpointHandler(int index) {
        Thing thing = buildThing(THING_TYPE_LOADPOINT, "lp" + index, Map.of(PROPERTY_INDEX, String.valueOf(index)),
                Map.of(), "loadpoint-charging", "loadpoint-connected", "loadpoint-charge-power",
                "loadpoint-charge-duration");
        EvccLoadpointHandler handler = new EvccLoadpointHandler(thing, channelTypeRegistry) {
            @Override
            protected @Nullable Bridge getBridge() {
                return bridge;
            }

            @Override
            public void logUnknownChannelXmlAsync(String key, String itemType) {
            }

            @Override
            public void applyNormalizedUpdate(JsonObject normalized) {
                loadpointDispatches++;
                super.applyNormalizedUpdate(normalized);
            }
        };
        handler.setCallback(callback);
        handler.initialize();
        return handler;
    }

    private EvccVehicleHandler startVehicleHandler() {
        Thing thing = buildThing(THING_TYPE_VEHICLE, VEHICLE_ID, Map.of(), Map.of(PROPERTY_VEHICLE_ID, VEHICLE_ID),
                "vehicle-limit-soc");
        EvccVehicleHandler handler = new EvccVehicleHandler(thing, channelTypeRegistry) {
            @Override
            protected @Nullable Bridge getBridge() {
                return bridge;
            }

            @Override
            public void logUnknownChannelXmlAsync(String key, String itemType) {
            }

            @Override
            protected void performApiRequest(String url, String method, JsonElement payload) {
            }
        };
        handler.setCallback(callback);
        handler.initialize();
        return handler;
    }

    private static JsonObject idleLoadpoint() {
        JsonObject loadpoint = new JsonObject();
        loadpoint.addProperty("charging", false);
        loadpoint.addProperty("connected", false);
        loadpoint.addProperty("chargePower", 0);
        loadpoint.addProperty("chargeDuration", 0);
        return loadpoint;
    }

    private void sendFullState(JsonObject... loadpoints) {
        JsonArray array = new JsonArray();
        for (JsonObject loadpoint : loadpoints) {
            array.add(loadpoint);
        }
        JsonObject state = new JsonObject();
        state.add(JSON_KEY_LOADPOINTS, array);
        bridgeHandler.onFullState(state);
    }

    private void sendDelta(String key, Object value) {
        JsonObject delta = new JsonObject();
        if (value instanceof Number number) {
            delta.addProperty(key, number);
        } else if (value instanceof Boolean bool) {
            delta.addProperty(key, bool);
        } else if (value instanceof JsonElement element) {
            delta.add(key, element);
        } else {
            delta.addProperty(key, value.toString());
        }
        bridgeHandler.onPartialUpdate(delta);
    }

    private static ChannelUID channel(Thing thing, String channelId) {
        return new ChannelUID(thing.getUID(), channelId);
    }

    @Test
    void unrelatedFieldChangesDoNotUpdateOtherLoadpointChannels() {
        EvccLoadpointHandler handler = startLoadpointHandler(0);
        sendFullState(idleLoadpoint());
        clearInvocations(callback);

        for (int i = 1; i <= 20; i++) {
            sendDelta("loadpoints.0.chargeDuration", i);
        }

        ChannelUID chargeDuration = channel(handler.getThing(), "loadpoint-charge-duration");
        verify(callback, times(20)).stateUpdated(eq(chargeDuration), any());
        verify(callback, never()).stateUpdated(eq(channel(handler.getThing(), "loadpoint-charging")), any());
        verify(callback, never()).stateUpdated(eq(channel(handler.getThing(), "loadpoint-connected")), any());
        verify(callback, never()).stateUpdated(eq(channel(handler.getThing(), "loadpoint-charge-power")), any());
    }

    @Test
    void changesOfAnotherLoadpointDoNotUpdateThisLoadpoint() {
        startLoadpointHandler(0);
        sendFullState(idleLoadpoint(), idleLoadpoint());
        clearInvocations(callback);

        sendDelta("loadpoints.1.chargePower", 11000);

        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    void identicalValuesAreOnlySentOnce() {
        EvccLoadpointHandler handler = startLoadpointHandler(0);
        sendFullState(idleLoadpoint());
        ChannelUID chargePower = channel(handler.getThing(), "loadpoint-charge-power");
        clearInvocations(callback);

        sendDelta("loadpoints.0.chargePower", 0);
        verify(callback, never()).stateUpdated(eq(chargePower), any());

        sendDelta("loadpoints.0.chargePower", 1500);
        sendDelta("loadpoints.0.chargePower", 1500);
        verify(callback, times(1)).stateUpdated(chargePower, new QuantityType<>(1500, Units.WATT));
    }

    @Test
    void repeatedFullStateDoesNotResendUnchangedValues() {
        EvccLoadpointHandler handler = startLoadpointHandler(0);
        sendFullState(idleLoadpoint());
        clearInvocations(callback);

        sendFullState(idleLoadpoint());

        verify(callback, never()).stateUpdated(eq(channel(handler.getThing(), "loadpoint-charging")), any());
    }

    @Test
    void reinitializedHandlerSendsAllValuesAgain() {
        EvccLoadpointHandler handler = startLoadpointHandler(0);
        sendFullState(idleLoadpoint());
        clearInvocations(callback);

        handler.dispose();
        handler.initialize();

        verify(callback).stateUpdated(channel(handler.getThing(), "loadpoint-charging"), OnOffType.OFF);
    }

    @Test
    void refreshCommandAlwaysSendsTheCurrentValue() {
        EvccLoadpointHandler handler = startLoadpointHandler(0);
        sendFullState(idleLoadpoint());
        ChannelUID charging = channel(handler.getThing(), "loadpoint-charging");
        clearInvocations(callback);

        handler.handleCommand(charging, RefreshType.REFRESH);

        verify(callback).stateUpdated(charging, OnOffType.OFF);
    }

    @Test
    void valueIsSentAgainAfterACommandWasReceivedForTheChannel() {
        EvccVehicleHandler handler = startVehicleHandler();
        JsonObject vehicle = new JsonObject();
        vehicle.addProperty("limitSoc", 80);
        JsonObject vehicles = new JsonObject();
        vehicles.add(VEHICLE_ID, vehicle);
        JsonObject state = new JsonObject();
        state.add(JSON_KEY_VEHICLES, vehicles);
        bridgeHandler.onFullState(state);
        ChannelUID limitSoc = channel(handler.getThing(), "vehicle-limit-soc");
        clearInvocations(callback);

        // The item may have been auto-updated to the commanded value, so if evcc keeps reporting
        // the previous value (e.g. because it rejected the command), that value must be sent again.
        handler.handleCommand(limitSoc, new QuantityType<>(100, Units.PERCENT));
        sendDelta(JSON_KEY_VEHICLES, vehicles.deepCopy());

        verify(callback).stateUpdated(eq(limitSoc), any(State.class));
    }

    @Test
    void bridgeComingBackOnlineDoesNotDuplicateRoutes() {
        EvccLoadpointHandler handler = startLoadpointHandler(0);
        sendFullState(idleLoadpoint());
        ThingStatusInfo online = ThingStatusInfoBuilder.create(ThingStatus.ONLINE).build();
        handler.bridgeStatusChanged(online);
        handler.bridgeStatusChanged(online);
        loadpointDispatches = 0;

        sendDelta("loadpoints.0.chargeDuration", 1);

        assertEquals(1, loadpointDispatches);
    }

    @Test
    void unchangedOnlineStatusIsNotReportedForEveryUpdate() {
        EvccLoadpointHandler handler = startLoadpointHandler(0);
        sendFullState(idleLoadpoint());
        clearInvocations(callback);

        for (int i = 1; i <= 20; i++) {
            sendDelta("loadpoints.0.chargeDuration", i);
        }

        verify(callback, never()).statusUpdated(eq(handler.getThing()), any(ThingStatusInfo.class));
    }
}
