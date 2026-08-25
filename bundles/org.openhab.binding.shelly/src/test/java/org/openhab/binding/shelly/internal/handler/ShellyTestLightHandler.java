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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1CoapServer;
import org.openhab.binding.shelly.internal.api1.Shelly1HttpApi;
import org.openhab.binding.shelly.internal.config.ShellyBindingRuntimeConfig;
import org.openhab.binding.shelly.internal.provider.ShellyTranslationProvider;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.State;

import sun.misc.Unsafe;

/**
 * A test harness for {@link ShellyLightHandler} that allows us to create a handler instance outside
 * of the OH framework. This is achieved by avoiding calling the real constructor. The harness creates
 * or mocks some required fields and getters, and provides a way to capture channel updates for
 * testing purposes.
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
            HttpClient httpClient, WebSocketClient webSocketClient) {
        super(thing, translationProvider, bindingConfig, thingTable, coapServer, httpClient, webSocketClient);
    }

    public static ShellyTestLightHandler create(ThingTypeUID thingTypeUID) {
        try {
            // use Unsafe to allocate an instance of TestLightHandler without calling its constructor
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            Unsafe unsafe = (Unsafe) f.get(null);

            // allocate ShellyLightHandler without calling its constructor
            ShellyTestLightHandler handler = (ShellyTestLightHandler) Objects.requireNonNull(unsafe)
                    .allocateInstance(ShellyTestLightHandler.class);

            // manually initialize required fields
            handler.profile = new ShellyDeviceProfile(thingTypeUID);
            handler.profile.initialized = true;

            if (THING_TYPE_SHELLYBULB.equals(thingTypeUID)) {
                handler.profile.inColor = true;
            }
            if (THING_TYPE_SHELLYRGBW2_COLOR.equals(thingTypeUID)) {
                handler.profile.inColor = true;
            }

            Field lmField = ShellyLightHandler.class.getDeclaredField("lightModels");
            lmField.setAccessible(true);
            lmField.set(handler, new TreeMap<Integer, ShellyLightModel>());

            Field baseLog = ShellyBaseHandler.class.getDeclaredField("logger");
            baseLog.setAccessible(true);
            baseLog.set(handler, org.slf4j.LoggerFactory.getLogger("ShellyTest"));

            Field lightLog = ShellyLightHandler.class.getDeclaredField("logger");
            lightLog.setAccessible(true);
            lightLog.set(handler, org.slf4j.LoggerFactory.getLogger("ShellyTest"));

            Thing thing = mock(Thing.class);

            ThingUID uid = new ThingUID(thingTypeUID, "test");

            Configuration cfg = new Configuration();
            cfg.setProperties(new HashMap<>());

            lenient().when(thing.getUID()).thenReturn(uid);
            lenient().when(thing.getThingTypeUID()).thenReturn(thingTypeUID);
            lenient().when(thing.getLabel()).thenReturn("TestThing");
            lenient().when(thing.getConfiguration()).thenReturn(cfg);

            Field thingField = BaseThingHandler.class.getDeclaredField("thing");
            thingField.setAccessible(true);
            thingField.set(handler, thing);

            handler.channelUpdates = new HashMap<>();
            handler.apiCalls = new ArrayList<>();

            Shelly1HttpApi api = mock(Shelly1HttpApi.class, invocation -> {
                handler.apiCalls.add(new ApiCall(invocation.getMethod().getName(), invocation.getArguments()));
                return RETURNS_DEFAULTS.answer(invocation);
            });
            Field apiField = ShellyBaseHandler.class.getDeclaredField("api");
            apiField.setAccessible(true);
            apiField.set(handler, api);

            return handler;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create ShellyTestLightHandler", e);
        }
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
