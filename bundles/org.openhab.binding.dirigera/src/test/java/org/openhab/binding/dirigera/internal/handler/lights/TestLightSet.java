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
package org.openhab.binding.dirigera.internal.handler.lights;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.openhab.binding.dirigera.internal.Constants.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dirigera.internal.handler.BaseHandler;
import org.openhab.binding.dirigera.internal.handler.DirigeraBridgeProvider;
import org.openhab.binding.dirigera.internal.handler.light.LightSetHandler;
import org.openhab.binding.dirigera.internal.mock.CallbackMock;
import org.openhab.binding.dirigera.internal.mock.DirigeraAPISimu;
import org.openhab.binding.dirigera.internal.mock.HandlerFactoryMock;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.internal.ThingImpl;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;

/**
 * {@link TestLightSet} Tests the {@link LightSetHandler}:
 * <ul>
 * <li>Handler factory creates the correct type</li>
 * <li>Channels are initialized and refresh correctly</li>
 * <li>{@code handleCommand} routes to the {@code devices/set/{id}} endpoint
 * (via {@link DirigeraAPISimu#getSetPatch}) and NOT to the regular device endpoint</li>
 * </ul>
 *
 * Uses {@code src/test/resources/home/matter-home.json} which already contains two member
 * lights whose {@code deviceSet} array references {@link #SET_ID}:
 * <ul>
 * <li>{@link #MEMBER1_ID} – "Loft Floor Lamp", {@code isReachable: false}</li>
 * <li>{@link #MEMBER2_ID} – "LED Strip Sideboard", {@code isReachable: true}, {@code isOn: false}, hue≈35, sat≈99</li>
 * </ul>
 *
 * {@code initializeDevice} polls {@code readDevice} for each member so the handler reaches
 * ONLINE autonomously — no manual websocket event needed in tests.
 *
 * @author Bernd Weymann - Initial Contribution
 */
@NonNullByDefault
class TestLightSet {

    /** Set ID as defined in matter-home.json */
    static final String SET_ID = "cff0e225-eb44-452d-bdae-774811f814c8";
    /** "Loft Floor Lamp" – isReachable: false */
    static final String MEMBER1_ID = "891790db-8c17-483a-a1a6-c85bffd3a373_1";
    /** "LED Strip Sideboard" – isReachable: true, isOn: false, hue≈35, sat≈99 */
    static final String MEMBER2_ID = "c27faa27-4c18-464f-81a0-a31ce57d83d5_1";

    static final String HOME_FILE = "src/test/resources/home/matter-home.json";

    ThingTypeUID thingTypeUID = THING_TYPE_LIGHT_SET;

    BaseHandler getHandler() {
        ThingHandler factoryHandler = DirigeraBridgeProvider.createHandler(thingTypeUID,
                DirigeraBridgeProvider.prepareSimuBridge(HOME_FILE, false, List.of()), SET_ID);
        assertTrue(factoryHandler instanceof LightSetHandler);
        assertTrue(factoryHandler instanceof BaseHandler);
        BaseHandler handler = (BaseHandler) factoryHandler;
        ThingHandlerCallback callback = handler.getCallback();
        assertNotNull(callback);
        assertTrue(callback instanceof CallbackMock);
        return handler;
    }

    // -------------------------------------------------------------------------
    // Test 1: factory creates a LightSetHandler
    // -------------------------------------------------------------------------
    @Test
    void testHandlerCreation() {
        HandlerFactoryMock hfm = new HandlerFactoryMock(mock(StorageService.class));
        assertTrue(hfm.supportsThingType(thingTypeUID), "Factory must support light-set");
        ThingImpl thing = new ThingImpl(thingTypeUID, "test-lightset");
        ThingHandler th = hfm.createHandler(thing);
        assertNotNull(th);
        assertTrue(th instanceof LightSetHandler, "createHandler must return LightSetHandler");
    }

    // -------------------------------------------------------------------------
    // Test 2: channel states correct after initialization + REFRESH
    // -------------------------------------------------------------------------
    @Test
    void testInitialization() {
        BaseHandler handler = getHandler();
        Thing thing = handler.getThing();
        CallbackMock callback = (CallbackMock) handler.getCallback();
        assertNotNull(callback);
        checkLightSetStates(callback);

        // REFRESH must reproduce the same states
        callback.clear();
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_POWER_STATE), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_LIGHT_COLOR), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_STARTUP_BEHAVIOR), RefreshType.REFRESH);
        checkLightSetStates(callback);
    }

    /**
     * Asserts channel states against matter-home.json MEMBER2 (LED Strip Sideboard):
     * isOn=false, hue≈35, sat≈100, brightness=0 (power off), startupOnOff=startOn → 1.
     * OTA channels are not supported by a light set and are intentionally excluded.
     */
    void checkLightSetStates(CallbackMock callback) {
        State onOffState = callback.getState("dirigera:light-set:test-device:power");
        assertNotNull(onOffState);
        assertTrue(onOffState instanceof OnOffType);
        assertTrue(OnOffType.OFF.equals(onOffState), "Power state");

        State hsbState = callback.getState("dirigera:light-set:test-device:color");
        assertNotNull(hsbState);
        assertTrue(hsbState instanceof HSBType);
        assertEquals(35, ((HSBType) hsbState).getHue().intValue(), "Hue");
        assertEquals(100, ((HSBType) hsbState).getSaturation().intValue(), "Saturation");
        assertEquals(0, ((HSBType) hsbState).getBrightness().intValue(), "Brightness (OFF → 0)");

        State startupState = callback.getState("dirigera:light-set:test-device:startup");
        assertNotNull(startupState);
        assertTrue(startupState instanceof DecimalType);
        assertEquals(1, ((DecimalType) startupState).intValue(), "Startup Behavior");
    }

    // -------------------------------------------------------------------------
    // Test 3: handleCommand routes to devices/set/{id}, NOT devices/{id}
    // -------------------------------------------------------------------------

    @Test
    void testHandleCommandPowerOnRoutesToSetEndpoint() {
        BaseHandler handler = getHandler();
        Thing thing = handler.getThing();
        DirigeraAPISimu api = (DirigeraAPISimu) handler.gateway().api();

        // Device starts isOn:false — sending ON must call sendSetAttributes, not sendAttributes
        api.clear();
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_POWER_STATE), OnOffType.ON);

        String setPatch = api.getSetPatch(SET_ID);
        assertNotNull(setPatch, "sendSetAttributes must be called for power-on");
        assertTrue(setPatch.contains("\"isOn\":true"), "Payload must contain isOn:true — got: " + setPatch);
        assertNull(api.peekPatch(SET_ID), "sendAttributes must NOT be called for light-set (power-on)");
    }

    @Test
    void testHandleCommandBrightnessRoutesToSetEndpoint() {
        BaseHandler handler = getHandler();
        Thing thing = handler.getThing();

        // check initial states before sending brightness command and clear received states after check
        CallbackMock callback = (CallbackMock) handler.getCallback();
        assertNotNull(callback);
        checkLightSetStates(callback);
        callback.clear();

        // Switch set ON first and wait for callback confirmation
        handler.handleUpdate(new JSONObject().put("attributes", new JSONObject().put("isOn", true)));
        State onOffState = callback.getState("dirigera:light-set:test-device:power");
        assertNotNull(onOffState);
        assertTrue(onOffState instanceof OnOffType);
        assertTrue(OnOffType.ON.equals(onOffState), "Power state");

        DirigeraAPISimu api = (DirigeraAPISimu) handler.gateway().api();
        api.clear();

        // Brightness is only dispatched immediately when isPowered() — needs isOn:true AND online:true.
        // Simulate a full readDevice-style update (no isReachable key, so LightSetHandler passes it
        // straight to super) that sets currentPowerState=ON without triggering powerChanged restores.
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_LIGHT_BRIGHTNESS), new PercentType(75));

        String setPatch = api.getSetPatch(SET_ID);
        assertNotNull(setPatch, "sendSetAttributes must be called for brightness");
        assertTrue(setPatch.contains("lightLevel"), "Payload must contain lightLevel — got: " + setPatch);
        assertNull(api.peekPatch(SET_ID), "sendAttributes must NOT be called for light-set (brightness)");
    }

    @Test
    void testHandleCommandColorRoutesToSetEndpoint() {
        BaseHandler handler = getHandler();
        Thing thing = handler.getThing();
        DirigeraAPISimu api = (DirigeraAPISimu) handler.gateway().api();

        // Color is dispatched immediately regardless of power state (changeProperty adds COLOR to queue)
        api.clear();
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_LIGHT_COLOR), new HSBType("120,80,60"));

        String setPatch = api.getSetPatch(SET_ID);
        assertNotNull(setPatch, "sendSetAttributes must be called for color");
        assertTrue(setPatch.contains("colorHue") || setPatch.contains("isOn"),
                "Payload must reference colorHue or isOn — got: " + setPatch);
        assertNull(api.peekPatch(SET_ID), "sendAttributes must NOT be called for light-set (color)");
    }
}
