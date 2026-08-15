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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.shelly.internal.ShellyDevices.THING_TYPE_SHELLYBULB;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.Test;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1CoapServer;
import org.openhab.binding.shelly.internal.api1.Shelly1HttpApi;
import org.openhab.binding.shelly.internal.config.ShellyBindingRuntimeConfig;
import org.openhab.binding.shelly.internal.provider.ShellyTranslationProvider;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.State;

import sun.misc.Unsafe;

/**
 * Tests for {@link ShellyLightHandler} and {@link ShellyLightModel} classes.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class ShellyLightHandlerLightModelTests {

    /**
     * A test harness for ShellyLightHandler that allows us to create an instance without calling the
     * constructor, and manually initialize the required fields.
     */
    public class TestLightHandler extends ShellyLightHandler {

        public Map<String, State> updates;

        public TestLightHandler(Thing thing, ShellyTranslationProvider translationProvider,
                ShellyBindingRuntimeConfig bindingConfig, ShellyThingTable thingTable, Shelly1CoapServer coapServer,
                HttpClient httpClient, WebSocketClient webSocketClient) {
            super(thing, translationProvider, bindingConfig, thingTable, coapServer, httpClient, webSocketClient);
        }

        public static TestLightHandler create(ThingTypeUID thingTypeUID) {
            try {
                Field f = Unsafe.class.getDeclaredField("theUnsafe");
                f.setAccessible(true);
                Unsafe unsafe = (Unsafe) f.get(null);

                // allocate ShellyLightHandler WITHOUT calling its constructor
                TestLightHandler handler = (TestLightHandler) unsafe.allocateInstance(TestLightHandler.class);

                // manually initialize required fields
                handler.profile = new ShellyDeviceProfile(thingTypeUID);
                handler.profile.initialized = true;
                handler.profile.isLight = true;
                handler.profile.isRGBW2 = true;
                handler.profile.inColor = true;
                handler.profile.device.mode = "color";

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

                when(thing.getUID()).thenReturn(uid);
                when(thing.getThingTypeUID()).thenReturn(thingTypeUID);
                when(thing.getLabel()).thenReturn("TestThing");
                when(thing.getConfiguration()).thenReturn(cfg);

                Field thingField = BaseThingHandler.class.getDeclaredField("thing");
                thingField.setAccessible(true);
                thingField.set(handler, thing);

                Shelly1HttpApi api = mock(Shelly1HttpApi.class);
                Field apiField = ShellyBaseHandler.class.getDeclaredField("api");
                apiField.setAccessible(true);
                apiField.set(handler, api);

                handler.updates = new HashMap<>();

                return handler;

            } catch (Exception e) {
                throw new RuntimeException("Failed to create TestLightHandler", e);
            }
        }

        @Override
        public boolean updateChannel(String channelId, State value, boolean force) {
            updates.put(channelId, value);
            return true;
        }

        public Map<String, State> getUpdates() {
            return updates;
        }
    }

    @Test
    void testFunctionalityOfTestHarness() {
        TestLightHandler handler = TestLightHandler.create(THING_TYPE_SHELLYBULB);

        handler.handleDeviceCommand(
                new ChannelUID(new ChannelGroupUID(new ThingUID(THING_TYPE_SHELLYBULB, "test"), "color"), "red"),
                PercentType.HUNDRED);

        Map<String, State> updates = handler.getUpdates();
        assertEquals(6, updates.size());
        assertEquals(PercentType.HUNDRED, updates.get("color#red"));
        assertEquals(PercentType.ZERO, updates.get("color#green"));
        assertEquals(PercentType.ZERO, updates.get("color#blue"));
        assertEquals(PercentType.ZERO, updates.get("color#white"));
        assertEquals(new StringType("red"), updates.get("color#full"));
        Object obj = updates.get("color#hsb");
        assertTrue(obj instanceof HSBType);
        assertEquals(0, ((HSBType) obj).getHue().intValue());
        assertEquals(100, ((HSBType) obj).getSaturation().intValue());
        assertEquals(0, ((HSBType) obj).getBrightness().intValue());

        try {
            handler.acquireLock();
            ShellyLightModel model = handler.getLightModel(0);
            assertNotNull(model);
            int[] rgbx = model.getRGBX();
            assertArrayEquals(new int[] { 255, 0, 0, 0 }, rgbx);
        } finally {
            assertFalse(handler.releaseLock()); // not dirty, so releaseLock returns false
        }
    }

    // TODO add detail test for THING_TYPE_SHELLYBULB
    // TODO add detail test for THING_TYPE_SHELLYDUO
    // TODO add detail test for THING_TYPE_SHELLYVINTAGE
    // TODO add detail test for THING_TYPE_SHELLYDUORGBW
    // TODO add detail test for THING_TYPE_SHELLYRGBW2_COLOR
    // TODO add detail test for THING_TYPE_SHELLYRGBW2_WHITE
    // TODO add detail test for THING_TYPE_SHELLYPLUSRGBWPM / SHELLY2_PROFILE_RGBW
    // TODO add detail test for THING_TYPE_SHELLYPLUSRGBWPM / SHELLY2_PROFILE_LIGHT
    // TODO add detail test for THING_TYPE_SHELLYPRORGBWWPM / SHELLY2_PROFILE_RGB
    // TODO add detail test for THING_TYPE_SHELLYPRORGBWWPM / SHELLY2_PROFILE_RGBW
    // TODO add detail test for THING_TYPE_SHELLYPRORGBWWPM / SHELLY2_PROFILE_LIGHT
    // TODO add detail test for THING_TYPE_SHELLYPRORGBWWPM / SHELLY2_PROFILE_RGBCCT
    // TODO add detail test for THING_TYPE_SHELLYPRORGBWWPM / SHELLY2_PROFILE_RGBX2LIGHT
    // TODO add detail test for THING_TYPE_SHELLYPRORGBWWPM / SHELLY2_PROFILE_CCTX2
    // TODO add detail test for THING_TYPE_SHELLYPLUSDUOBULB
    // TODO add detail test for THING_TYPE_SHELLYPLUSCOLORBULB
}
