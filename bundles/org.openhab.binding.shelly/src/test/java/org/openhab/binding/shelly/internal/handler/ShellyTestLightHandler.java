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
package org.openhab.binding.shelly.internal.handler;

import static org.mockito.Mockito.*;
import static org.openhab.binding.shelly.internal.ShellyDevices.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.mockito.MockedConstruction;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1CoapServer;
import org.openhab.binding.shelly.internal.api1.Shelly1HttpApi;
import org.openhab.binding.shelly.internal.config.ShellyBindingRuntimeConfig;
import org.openhab.binding.shelly.internal.provider.ShellyStateDescriptionProvider;
import org.openhab.binding.shelly.internal.provider.ShellyTranslationProvider;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.State;

/**
 * A test harness for {@link ShellyLightHandler} that allows us to create a handler instance outside
 * of the OH framework. The harness creates or mocks some required fields and getters, and provides a
 * way to capture channel updates for testing purposes.
 * 
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ShellyTestLightHandler extends ShellyLightHandler {

    // A record to capture API calls made by the handler
    public record ApiCall(String method, Object[] args) {
    }

    // Map of channel id to update state
    public @NonNullByDefault({}) Map<String, State> channelUpdates;

    // List of calls made to the API
    private @NonNullByDefault({}) List<ApiCall> apiCalls;

    public ShellyTestLightHandler(Thing thing, ShellyTranslationProvider translationProvider,
            ShellyBindingRuntimeConfig bindingConfig, ShellyThingTable thingTable, Shelly1CoapServer coapServer,
            HttpClient httpClient, WebSocketClient webSocketClient, LocationProvider locationProvider,
            ShellyStateDescriptionProvider stateDescriptionProvider) {
        super(thing, translationProvider, bindingConfig, thingTable, coapServer, httpClient, webSocketClient,
                locationProvider, stateDescriptionProvider);
    }

    public static ShellyTestLightHandler create(ThingTypeUID thingTypeUID) {
        Thing thing = mock(Thing.class);

        ThingUID uid = new ThingUID(thingTypeUID, "test");
        Configuration cfg = new Configuration();
        cfg.setProperties(new HashMap<>());

        lenient().when(thing.getUID()).thenReturn(uid);
        // First constructor-time lookup forces a Gen1 API instance; all later lookups return the requested type.
        lenient().when(thing.getThingTypeUID()).thenReturn(THING_TYPE_SHELLYBULB, thingTypeUID);
        lenient().when(thing.getLabel()).thenReturn("TestThing");
        lenient().when(thing.getConfiguration()).thenReturn(cfg);
        lenient().when(thing.getProperties()).thenReturn(new HashMap<>());

        ShellyTranslationProvider translationProvider = mock(ShellyTranslationProvider.class, invocation -> {
            if (String.class.equals(invocation.getMethod().getReturnType()) && invocation.getArguments().length > 0
                    && invocation.getArguments()[0] instanceof String text) {
                return text;
            }
            return RETURNS_DEFAULTS.answer(invocation);
        });

        ShellyBindingRuntimeConfig bindingConfig = mock(ShellyBindingRuntimeConfig.class);
        lenient().when(bindingConfig.getLocalIP()).thenReturn("");
        lenient().when(bindingConfig.getHttpPort()).thenReturn(8080);
        lenient().when(bindingConfig.getDefaultUserId()).thenReturn("");
        lenient().when(bindingConfig.getDefaultPassword()).thenReturn("");

        ShellyThingTable thingTable = mock(ShellyThingTable.class);
        Shelly1CoapServer coapServer = mock(Shelly1CoapServer.class);
        HttpClient httpClient = mock(HttpClient.class);
        WebSocketClient webSocketClient = mock(WebSocketClient.class);
        LocationProvider locationProvider = mock(LocationProvider.class);
        ShellyStateDescriptionProvider stateDescriptionProvider = mock(ShellyStateDescriptionProvider.class);

        List<ApiCall> recordedApiCalls = new ArrayList<>();
        ShellyTestLightHandler handler;
        try (MockedConstruction<Shelly1HttpApi> ignored = mockConstruction(Shelly1HttpApi.class,
                withSettings().defaultAnswer(invocation -> {
                    recordedApiCalls.add(new ApiCall(invocation.getMethod().getName(), invocation.getArguments()));
                    return RETURNS_DEFAULTS.answer(invocation);
                }))) {
            handler = new ShellyTestLightHandler(thing, translationProvider, bindingConfig, thingTable, coapServer,
                    httpClient, webSocketClient, locationProvider, stateDescriptionProvider);
        }

        handler.setCallback(mock(ThingHandlerCallback.class));
        handler.profile = new ShellyDeviceProfile(thingTypeUID);
        handler.profile.initialized = true;
        if (THING_TYPE_SHELLYBULB.equals(thingTypeUID) || THING_TYPE_SHELLYRGBW2_COLOR.equals(thingTypeUID)) {
            handler.profile.inColor = true;
        }
        handler.channelUpdates = new HashMap<>();
        handler.apiCalls = recordedApiCalls;
        return handler;
    }

    @Override
    public boolean areChannelsCreated() {
        // for testing purposes, we expect that channels are already created
        return true;
    }

    @Override
    public boolean updateChannel(String channelId, State value, boolean force) {
        // capture the channel update in the map for testing purposes
        channelUpdates.put(channelId, value);
        return true;
    }

    public Map<String, State> getChannelUpdates() {
        return Objects.requireNonNull(channelUpdates);
    }

    public void setProfile(ShellyDeviceProfile profile) {
        this.profile = profile;
    }

    public void addLightModel(int id, ThingTypeUID thingTypeUID, ShellyDeviceProfile profile, double stepSize) {
        ShellyLightModel model = ShellyLightModel.create(this, id, profile, stepSize);
        Objects.requireNonNull(lightModels).put(id, model);
    }

    public List<ApiCall> getApiCalls() {
        return List.copyOf(Objects.requireNonNull(apiCalls));
    }
}
