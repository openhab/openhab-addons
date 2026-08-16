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
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.ShellyDevices.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsRgbwLight;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusLight;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusLightChannel;
import org.openhab.binding.shelly.internal.api1.Shelly1HttpApi;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.State;

/**
 * Tests for {@link ShellyLightHandler} and {@link ShellyLightModel} classes.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class ShellyLightHandlerLightModelTests {

    private static ShellyStatusLightChannel lightChannel(Boolean isOn, Integer red, Integer green, Integer blue,
            Integer white, Integer gain, Integer brightness, Integer temp, Integer effect, Boolean hasTimer) {
        ShellyStatusLightChannel light = new ShellyStatusLightChannel();
        light.ison = isOn;
        light.red = red;
        light.green = green;
        light.blue = blue;
        light.white = white;
        light.gain = gain;
        light.brightness = brightness;
        light.temp = temp;
        light.effect = effect;
        light.hasTimer = hasTimer;
        return light;
    }

    private static ShellyStatusLight singleLightStatus(ShellyStatusLightChannel channel) {
        ShellyStatusLight status = new ShellyStatusLight();
        status.lights = new ArrayList<>();
        status.lights.add(channel);
        return status;
    }

    private static @Nullable Object getField(Object target, Class<?> declaringClass, String fieldName)
            throws Exception {
        Field field = declaringClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    @Test
    void testFunctionalityOfTestHarness() {
        ShellyTestLightHandler handler = ShellyTestLightHandler.create(THING_TYPE_SHELLYBULB);

        handler.handleDeviceCommand(
                new ChannelUID(new ChannelGroupUID(new ThingUID(THING_TYPE_SHELLYBULB, "test"), "color"), "red"),
                PercentType.HUNDRED);

        Map<String, State> updates = handler.getChannelUpdates();
        assertEquals(9, updates.size());
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
        assertTrue(updates.containsKey(CHANNEL_GROUP_PRIMARY + "#" + CHANNEL_PRIMARY_COLOR));
        assertTrue(updates.containsKey(CHANNEL_GROUP_PRIMARY + "#" + CHANNEL_PRIMARY_COLOR_TEMP));
        assertTrue(updates.containsKey(CHANNEL_GROUP_PRIMARY + "#" + CHANNEL_PRIMARY_COLOR_TEMP_ABS));

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

    @Test
    void bulbCreatesExpectedLightModel() {
        ShellyTestLightHandler handler = ShellyTestLightHandler.create(THING_TYPE_SHELLYBULB);

        handler.handleDeviceCommand(
                new ChannelUID(new ChannelGroupUID(new ThingUID(THING_TYPE_SHELLYBULB, "test"), "color"), "red"),
                PercentType.HUNDRED);

        try {
            handler.acquireLock();
            ShellyLightModel model = handler.getLightModel(0);
            assertNotNull(model);
            assertEquals(ShellyLightModel.Mode.COLOR, model.getMode());
            assertArrayEquals(new int[] { 255, 0, 0, 0 }, model.getRGBX());
        } finally {
            handler.releaseLock();
        }
    }

    @Test
    void duoCreatesExpectedLightModel() {
        ShellyTestLightHandler handler = ShellyTestLightHandler.create(THING_TYPE_SHELLYDUO);

        handler.handleDeviceCommand(
                new ChannelUID(new ChannelGroupUID(new ThingUID(THING_TYPE_SHELLYDUO, "test"), "white"), "brightness"),
                new PercentType(42));

        try {
            handler.acquireLock();
            ShellyLightModel model = handler.getLightModel(0);
            assertNotNull(model);
            assertEquals(ShellyLightModel.Mode.WHITE, model.getMode());
            assertEquals(new PercentType(42), model.getBrightnessState());
        } finally {
            handler.releaseLock();
        }
    }

    @Test
    void vintageCreatesExpectedLightModel() {
        ShellyTestLightHandler handler = ShellyTestLightHandler.create(THING_TYPE_SHELLYVINTAGE);

        handler.handleDeviceCommand(
                new ChannelUID(new ChannelGroupUID(new ThingUID(THING_TYPE_SHELLYVINTAGE, "test"), "white"),
                        "brightness"),
                new PercentType(25));

        try {
            handler.acquireLock();
            ShellyLightModel model = handler.getLightModel(0);
            assertNotNull(model);
            assertEquals(ShellyLightModel.Mode.WHITE, model.getMode());
            assertEquals(new PercentType(25), model.getBrightnessState());
        } finally {
            handler.releaseLock();
        }
    }

    @Test
    void duoRgbwCreatesExpectedLightModel() {
        ShellyTestLightHandler handler = ShellyTestLightHandler.create(THING_TYPE_SHELLYDUORGBW);

        handler.handleDeviceCommand(
                new ChannelUID(new ChannelGroupUID(new ThingUID(THING_TYPE_SHELLYDUORGBW, "test"), "color"), "full"),
                new StringType("white"));

        try {
            handler.acquireLock();
            ShellyLightModel model = handler.getLightModel(0);
            assertNotNull(model);
            assertEquals(ShellyLightModel.Mode.COLOR, model.getMode());
            assertArrayEquals(new int[] { 0, 0, 0, 255 }, model.getRGBX());
        } finally {
            handler.releaseLock();
        }
    }

    @Test
    void rgbw2ColorCreatesExpectedLightModel() {
        ShellyTestLightHandler handler = ShellyTestLightHandler.create(THING_TYPE_SHELLYRGBW2_COLOR);

        handler.handleDeviceCommand(
                new ChannelUID(new ChannelGroupUID(new ThingUID(THING_TYPE_SHELLYRGBW2_COLOR, "test"), "color"),
                        "blue"),
                PercentType.HUNDRED);

        try {
            handler.acquireLock();
            ShellyLightModel model = handler.getLightModel(0);
            assertNotNull(model);
            assertEquals(ShellyLightModel.Mode.COLOR, model.getMode());
            assertArrayEquals(new int[] { 0, 0, 255, 0 }, model.getRGBX());
        } finally {
            handler.releaseLock();
        }
    }

    @Test
    void rgbw2WhiteCreatesExpectedLightModel() {
        ShellyTestLightHandler handler = ShellyTestLightHandler.create(THING_TYPE_SHELLYRGBW2_WHITE);

        handler.handleDeviceCommand(
                new ChannelUID(new ChannelGroupUID(new ThingUID(THING_TYPE_SHELLYRGBW2_WHITE, "test"), "white"),
                        "brightness"),
                new PercentType(73));

        try {
            handler.acquireLock();
            ShellyLightModel model = handler.getLightModel(0);
            assertNotNull(model);
            assertEquals(ShellyLightModel.Mode.WHITE, model.getMode());
            assertEquals(new PercentType(73), model.getBrightnessState());
        } finally {
            handler.releaseLock();
        }
    }

    @Test
    void updateDeviceStatusReturnsFalseWhenProfileNotInitialized() throws Exception {
        ShellyTestLightHandler handler = ShellyTestLightHandler.create(THING_TYPE_SHELLYBULB);
        handler.profile.initialized = false;

        boolean updated = handler.updateDeviceStatus(new ShellySettingsStatus());

        assertFalse(updated);
        verify((Shelly1HttpApi) getField(handler, ShellyBaseHandler.class, "api"), never()).getLightStatus();
    }

    @Test
    void updateDeviceStatusCreatesModelAndUpdatesColorChannels() throws Exception {
        ShellyTestLightHandler handler = ShellyTestLightHandler.create(THING_TYPE_SHELLYBULB);
        Shelly1HttpApi api = (Shelly1HttpApi) getField(handler, ShellyBaseHandler.class, "api");
        assertNotNull(api);

        ShellyStatusLightChannel dto = lightChannel(true, 255, 0, 0, 0, 100, 80, 4000, 2, true);
        when(api.getLightStatus()).thenReturn(singleLightStatus(dto));

        boolean updated = handler.updateDeviceStatus(new ShellySettingsStatus());

        assertTrue(updated);
        Map<String, State> updates = handler.getChannelUpdates();
        assertNotNull(handler.getLightModel(0));
        assertEquals(OnOffType.ON, updates.get("control#power"));
        assertEquals(PercentType.HUNDRED, updates.get("color#red"));
        assertEquals(PercentType.ZERO, updates.get("color#green"));
        assertEquals(PercentType.ZERO, updates.get("color#blue"));
        assertEquals(PercentType.ZERO, updates.get("color#white"));
        assertEquals(new StringType("red"), updates.get("color#full"));
        assertEquals(new PercentType(80), updates.get("color#gain"));
        assertEquals(new PercentType(80), updates.get("white#brightness"));
        assertEquals(new DecimalType(2), updates.get("color#effect"));
    }

    @Test
    void updateDeviceStatusUpdatesPrimaryChannelsWhenAvailable() throws Exception {
        ShellyTestLightHandler handler = ShellyTestLightHandler.create(THING_TYPE_SHELLYBULB);
        Shelly1HttpApi api = (Shelly1HttpApi) getField(handler, ShellyBaseHandler.class, "api");
        assertNotNull(api);

        ShellyStatusLightChannel dto = lightChannel(true, 255, 0, 0, 0, 75, 60, 3500, 0, false);
        when(api.getLightStatus()).thenReturn(singleLightStatus(dto));

        boolean updated = handler.updateDeviceStatus(new ShellySettingsStatus());

        assertTrue(updated);
        Map<String, State> updates = handler.getChannelUpdates();
        assertTrue(updates.containsKey(CHANNEL_GROUP_PRIMARY + "#" + CHANNEL_PRIMARY_COLOR));
        assertTrue(updates.containsKey(CHANNEL_GROUP_PRIMARY + "#" + CHANNEL_PRIMARY_COLOR_TEMP));
        assertTrue(updates.containsKey(CHANNEL_GROUP_PRIMARY + "#" + CHANNEL_PRIMARY_COLOR_TEMP_ABS));
    }

    @Test
    void updateDeviceStatusSynchronizesModelModeFromProfileDeviceMode() throws Exception {
        ShellyTestLightHandler handler = ShellyTestLightHandler.create(THING_TYPE_SHELLYBULB);
        Shelly1HttpApi api = (Shelly1HttpApi) getField(handler, ShellyBaseHandler.class, "api");
        assertNotNull(api);
        handler.profile.device.mode = "white";

        ShellyStatusLightChannel dto = lightChannel(true, 255, 0, 0, 0, 50, 40, 3000, 0, false);
        when(api.getLightStatus()).thenReturn(singleLightStatus(dto));

        handler.updateDeviceStatus(new ShellySettingsStatus());

        ShellyLightModel model = handler.getLightModel(0);
        assertNotNull(model);
        assertEquals(ShellyLightModel.Mode.WHITE, model.getMode());
    }

    @Test
    void updateDeviceStatusUpdatesTimerChannelsFromProfileSettings() throws Exception {
        ShellyTestLightHandler handler = ShellyTestLightHandler.create(THING_TYPE_SHELLYBULB);
        Shelly1HttpApi api = (Shelly1HttpApi) getField(handler, ShellyBaseHandler.class, "api");
        assertNotNull(api);

        ShellySettingsRgbwLight settings = new ShellySettingsRgbwLight();
        settings.autoOn = 5.0;
        settings.autoOff = 10.0;
        handler.profile.settings.lights = new ArrayList<>();
        Objects.requireNonNull(handler.profile.settings.lights).add(settings);

        ShellyStatusLightChannel dto = lightChannel(true, 0, 0, 255, 0, 30, 20, 4000, 0, true);
        when(api.getLightStatus()).thenReturn(singleLightStatus(dto));

        boolean updated = handler.updateDeviceStatus(new ShellySettingsStatus());

        assertTrue(updated);
        Map<String, State> updates = handler.getChannelUpdates();

        assertEquals(OnOffType.ON, updates.get("control#timerActive"));
        State qty = updates.get("control#autoOn");
        assertTrue(qty instanceof QuantityType<?>);
        QuantityType<?> sec = ((QuantityType<?>) qty).toUnit(Units.SECOND);
        assertNotNull(sec);
        assertEquals(5, sec.intValue());

        qty = updates.get("control#autoOff");
        assertTrue(qty instanceof QuantityType<?>);
        sec = ((QuantityType<?>) qty).toUnit(Units.SECOND);
        assertNotNull(sec);
        assertEquals(10, sec.intValue());
    }

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
