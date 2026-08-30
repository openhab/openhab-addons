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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.ShellyDevices.*;
import static org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
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
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Tests for {@link ShellyLightHandler} and {@link ShellyLightModel} classes.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class ShellyLightHandlerLightModelTest {

    private static ShellyStatusLightChannel lightChannel(@Nullable Boolean isOn, @Nullable Integer red,
            @Nullable Integer green, @Nullable Integer blue, @Nullable Integer white, @Nullable Integer gain,
            @Nullable Integer brightness, @Nullable Integer temp, @Nullable Integer effect,
            @Nullable Boolean hasTimer) {
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
        assertNotNull(updates.get("main#hsb"));
        assertNotNull(updates.get("main#temperature"));
        assertNotNull(updates.get("main#temperature-abs"));

        try {
            handler.acquireLock();
            ShellyLightModel model = handler.getLightModelByIndex(0);
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
            ShellyLightModel model = handler.getLightModelByIndex(0);
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
            ShellyLightModel model = handler.getLightModelByIndex(0);
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
            ShellyLightModel model = handler.getLightModelByIndex(0);
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
            ShellyLightModel model = handler.getLightModelByIndex(0);
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
            ShellyLightModel model = handler.getLightModelByIndex(0);
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
                new ChannelUID(new ChannelGroupUID(new ThingUID(THING_TYPE_SHELLYRGBW2_WHITE, "test"), "light1"),
                        "brightness"),
                new PercentType(73));

        try {
            handler.acquireLock();
            ShellyLightModel model = handler.getLightModelByIndex(0);
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

        ShellyStatusLightChannel dto = lightChannel(true, 255, 0, 0, 0, 80, null, null, 2, true);
        when(api.getLightStatus()).thenReturn(singleLightStatus(dto));

        boolean updated = handler.updateDeviceStatus(new ShellySettingsStatus());

        assertTrue(updated);
        Map<String, State> updates = handler.getChannelUpdates();
        assertNotNull(handler.getLightModelByIndex(0));
        assertEquals(OnOffType.ON, updates.get("control#power"));
        assertEquals(PercentType.HUNDRED, updates.get("color#red"));
        assertEquals(PercentType.ZERO, updates.get("color#green"));
        assertEquals(PercentType.ZERO, updates.get("color#blue"));
        assertEquals(PercentType.ZERO, updates.get("color#white"));
        assertEquals(new StringType("red"), updates.get("color#full"));
        assertEquals(new PercentType(80), updates.get("color#gain"));
        assertEquals(new DecimalType(2), updates.get("color#effect"));
    }

    @Test
    void updateDeviceStatusCreatesModelAndUpdatesColorTempChannels() throws Exception {
        ShellyTestLightHandler handler = ShellyTestLightHandler.create(THING_TYPE_SHELLYBULB);
        Shelly1HttpApi api = (Shelly1HttpApi) getField(handler, ShellyBaseHandler.class, "api");
        assertNotNull(api);

        ShellyStatusLightChannel dto = lightChannel(true, null, null, null, null, null, 80, 4000, null, true);
        when(api.getLightStatus()).thenReturn(singleLightStatus(dto));

        boolean updated = handler.updateDeviceStatus(new ShellySettingsStatus());

        assertTrue(updated);
        Map<String, State> updates = handler.getChannelUpdates();
        assertNotNull(handler.getLightModelByIndex(0));

        assertEquals(new PercentType(80), updates.get("white#brightness"));
        assertEquals(QuantityType.valueOf(4000, Units.KELVIN), updates.get("white#temperature-abs"));

        Object ct = updates.get("white#temperature");
        assertTrue(ct instanceof PercentType);
        assertEquals(54, Math.round(((PercentType) ct).doubleValue()));
    }

    @Test
    void updateDeviceStatusSynchronizesModelModeFromProfileDeviceMode() throws Exception {
        ShellyTestLightHandler handler = ShellyTestLightHandler.create(THING_TYPE_SHELLYBULB);
        Shelly1HttpApi api = (Shelly1HttpApi) getField(handler, ShellyBaseHandler.class, "api");
        assertNotNull(api);
        handler.profile.device.mode = "white";

        ShellyStatusLightChannel dto = lightChannel(true, 255, 0, 0, 0, 50, null, null, 0, false);
        when(api.getLightStatus()).thenReturn(singleLightStatus(dto));

        handler.updateDeviceStatus(new ShellySettingsStatus());

        ShellyLightModel model = handler.getLightModelByIndex(0);
        assertNotNull(model);
        assertEquals(ShellyLightModel.Mode.COLOR, model.getMode());
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

        ShellyStatusLightChannel dto = lightChannel(true, 0, 0, 255, 0, 30, null, null, 0, true);
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

    @ParameterizedTest
    @MethodSource("lightHandlerRemoteApiProvider")
    void parameterizedUpdateRemoteDeviceFromLightModelCallsExpectedApiAndProducesExpectedParameters(
    // @formatter:off
            ThingTypeUID thingTypeUID,
            int channelGroupNo,
            int expectedApiLightIndex, 
            @Nullable String profileOverride,
            String commandGroup,
            String commandChannel,
            Command command,
            @Nullable String expectedMode,
            boolean expectedSetLightParms,
            Map<String, String> expectedParms) throws Exception {
    // @formatter:on

        ShellyTestLightHandler handler = ShellyTestLightHandler.create(thingTypeUID);
        Shelly1HttpApi api = (Shelly1HttpApi) getField(handler, ShellyBaseHandler.class, "api");
        assertNotNull(api);

        handler.profile.device.profile = profileOverride;
        handler.profile.maxTemp = 6500;
        handler.profile.minTemp = 2700;

        if (THING_TYPE_SHELLYPLUSRGBWPM.equals(thingTypeUID) || THING_TYPE_SHELLYPRORGBWWPM.equals(thingTypeUID)) {
            handler.profile.isRGBW2 = true;
            handler.profile.inColor = List
                    .of(SHELLY2_PROFILE_RGB, SHELLY2_PROFILE_RGBW, SHELLY2_PROFILE_RGBCCT, SHELLY2_PROFILE_RGBX2LIGHT)
                    .contains(profileOverride) && channelGroupNo == 0;
        } else if (THING_TYPE_SHELLYRGBW2_COLOR.equals(thingTypeUID)) {
            handler.profile.isRGBW2 = true;
            handler.profile.inColor = true;
        } else if (THING_TYPE_SHELLYRGBW2_WHITE.equals(thingTypeUID)) {
            handler.profile.isRGBW2 = true;
            handler.profile.inColor = false;
        }

        String actualGroup = commandGroup;
        if (CHANNEL_GROUP_LIGHT_INDEX.equals(commandGroup)) {
            actualGroup = CHANNEL_GROUP_LIGHT_INDEX + channelGroupNo;
        }

        if (expectedMode != null) {
            handler.profile.device.mode = SHELLY_MODE_WHITE.equals(expectedMode) ? SHELLY_MODE_COLOR
                    : SHELLY_MODE_WHITE;
        }

        doNothing().when(api).setLightParms(anyInt(), anyMap());
        doNothing().when(api).setLightMode(anyString());

        ChannelUID channelUID = new ChannelUID(new ChannelGroupUID(new ThingUID(thingTypeUID, "test"), actualGroup),
                commandChannel);

        boolean handled = handler.handleDeviceCommand(channelUID, command);

        assertTrue(handled, "command should be handled for " + thingTypeUID + " / " + profileOverride);

        if (expectedMode != null) {
            verify(api).setLightMode(expectedMode);
        } else {
            verify(api, never()).setLightMode(anyString());
        }

        if (expectedSetLightParms) {
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Map<String, String>> parmsCaptor = ArgumentCaptor
                    .forClass((Class<Map<String, String>>) (Class<?>) Map.class);
            verify(api).setLightParms(eq(expectedApiLightIndex), parmsCaptor.capture());
            assertEquals(expectedParms, parmsCaptor.getValue(), "unexpected light parameter map");
        } else {
            verify(api, never()).setLightParms(anyInt(), anyMap());
        }

        verifyNoMoreInteractions(api);
    }

    private static Stream<Arguments> lightHandlerRemoteApiProvider() {
        String avgColorTemp = String.valueOf(Math.round(1000000.0 / (((1000000.0 / 6500) + (1000000.0 / 2700)) / 2)));
        return Stream.of(
        // @formatter:off
            Arguments.of(THING_TYPE_SHELLYBULB, 
                    0,
                    0,
                    null,
                    CHANNEL_GROUP_COLOR_CONTROL, 
                    CHANNEL_COLOR_RED,
                    PercentType.HUNDRED, 
                    null, 
                    true, 
                    Map.of(SHELLY_COLOR_RED, "255", SHELLY_COLOR_GREEN, "0", SHELLY_COLOR_BLUE, "0", SHELLY_COLOR_WHITE, "0")
                    ),

            Arguments.of(THING_TYPE_SHELLYDUO,
                    0,
                    0,
                    null,
                    CHANNEL_GROUP_WHITE_CONTROL,
                    CHANNEL_BRIGHTNESS,
                    new PercentType(42),
                    null,
                    true,
                    Map.of(SHELLY_COLOR_BRIGHTNESS, "42", SHELLY_LIGHT_TURN, SHELLY_API_ON)
                    ),

            Arguments.of(THING_TYPE_SHELLYVINTAGE,
                    0,
                    0,
                    null,
                    CHANNEL_GROUP_WHITE_CONTROL,
                    CHANNEL_BRIGHTNESS,
                    new PercentType(25),
                    null,
                    true,
                    Map.of(SHELLY_COLOR_BRIGHTNESS, "25", SHELLY_LIGHT_TURN, SHELLY_API_ON)
                    ),

            Arguments.of(THING_TYPE_SHELLYDUORGBW,
                    0,
                    0,
                    null,
                    CHANNEL_GROUP_COLOR_CONTROL,
                    CHANNEL_COLOR_FULL,
                    new StringType("yellow"),
                    null,
                    false,
                    Map.of(SHELLY_COLOR_RED, "255", SHELLY_COLOR_GREEN, "255", SHELLY_COLOR_BLUE, "0", SHELLY_COLOR_WHITE, "0")
                    ),

            Arguments.of(THING_TYPE_SHELLYRGBW2_COLOR,
                    0,
                    0,
                    null,
                    CHANNEL_GROUP_COLOR_CONTROL,
                    CHANNEL_COLOR_BLUE,
                    PercentType.HUNDRED,
                    null,
                    true,
                    Map.of(SHELLY_COLOR_RED, "0", SHELLY_COLOR_GREEN, "0", SHELLY_COLOR_BLUE, "255", SHELLY_COLOR_WHITE, "0")
                    ),

            Arguments.of(THING_TYPE_SHELLYRGBW2_WHITE,
                    1,
                    0,
                    null,
                    CHANNEL_GROUP_LIGHT_INDEX,
                    CHANNEL_BRIGHTNESS,
                    new PercentType(73),
                    null,
                    true,
                    Map.of(SHELLY_COLOR_BRIGHTNESS, "73", SHELLY_LIGHT_TURN, SHELLY_API_ON)
                    ),
            
            Arguments.of(THING_TYPE_SHELLYRGBW2_WHITE,
                    2,
                    1,
                    null,
                    CHANNEL_GROUP_LIGHT_INDEX,
                    CHANNEL_BRIGHTNESS,
                    new PercentType(61),
                    null,
                    true,
                    Map.of( SHELLY_COLOR_BRIGHTNESS, "61", SHELLY_LIGHT_TURN, SHELLY_API_ON)
                    ),

            Arguments.of(THING_TYPE_SHELLYPLUSRGBWPM,
                    0,
                    0,
                    SHELLY2_PROFILE_RGBW,
                    CHANNEL_GROUP_COLOR_CONTROL,
                    CHANNEL_COLOR_GREEN,
                    PercentType.HUNDRED,
                    null,
                    true,
                    Map.of(SHELLY_COLOR_RED, "0", SHELLY_COLOR_GREEN, "255", SHELLY_COLOR_BLUE, "0", SHELLY_COLOR_WHITE, "0")
                    ),

            Arguments.of(THING_TYPE_SHELLYPLUSRGBWPM,
                    1,
                    0,
                    SHELLY2_PROFILE_LIGHT,
                    CHANNEL_GROUP_LIGHT_INDEX,
                    CHANNEL_BRIGHTNESS,
                    new PercentType(55),
                    null,
                    true,
                    Map.of(SHELLY_COLOR_BRIGHTNESS, "55", SHELLY_LIGHT_TURN, SHELLY_API_ON)
                    ),
            
            Arguments.of(THING_TYPE_SHELLYPLUSRGBWPM,
                    2,
                    1,
                    SHELLY2_PROFILE_LIGHT,
                    CHANNEL_GROUP_LIGHT_INDEX,
                    CHANNEL_BRIGHTNESS,
                    new PercentType(45),
                    null,
                    true,
                    Map.of( SHELLY_COLOR_BRIGHTNESS, "45", SHELLY_LIGHT_TURN, SHELLY_API_ON)
                    ),

            Arguments.of(THING_TYPE_SHELLYPRORGBWWPM,
                    0,
                    0,
                    SHELLY2_PROFILE_RGB,
                    CHANNEL_GROUP_COLOR_CONTROL,
                    CHANNEL_COLOR_RED,
                    new PercentType(1),
                    null,
                    true,
                    Map.of(SHELLY_COLOR_RED, "3", SHELLY_COLOR_GREEN, "0", SHELLY_COLOR_BLUE, "0")
                    ),

            Arguments.of(THING_TYPE_SHELLYPRORGBWWPM,
                    0,
                    0,
                    SHELLY2_PROFILE_RGBW,
                    CHANNEL_GROUP_COLOR_CONTROL,
                    CHANNEL_COLOR_WHITE,
                    new PercentType(33),
                    null,
                    true,
                    Map.of(SHELLY_COLOR_RED, "0", SHELLY_COLOR_GREEN, "0", SHELLY_COLOR_BLUE, "0", SHELLY_COLOR_WHITE, "84")
                    ),
            
            Arguments.of(THING_TYPE_SHELLYPRORGBWWPM,
                    1,
                    0,
                    SHELLY2_PROFILE_LIGHT,
                    CHANNEL_GROUP_LIGHT_INDEX,
                    CHANNEL_BRIGHTNESS,
                    new PercentType(44),
                    null,
                    true,
                    Map.of(SHELLY_COLOR_BRIGHTNESS, "44", SHELLY_LIGHT_TURN, SHELLY_API_ON)
                    ),
            
            Arguments.of(THING_TYPE_SHELLYPRORGBWWPM,
                    2,
                    1,
                    SHELLY2_PROFILE_LIGHT,
                    CHANNEL_GROUP_LIGHT_INDEX,
                    CHANNEL_BRIGHTNESS,
                    new PercentType(66),
                    null,
                    true,
                    Map.of(SHELLY_COLOR_BRIGHTNESS, "66", SHELLY_LIGHT_TURN, SHELLY_API_ON)
                    ),
            
            Arguments.of(THING_TYPE_SHELLYPRORGBWWPM,
                    0,
                    0,
                    SHELLY2_PROFILE_RGBCCT,
                    CHANNEL_GROUP_COLOR_CONTROL,
                    CHANNEL_COLOR_BLUE,
                    PercentType.HUNDRED,
                    null,
                    true,
                    Map.of(SHELLY_COLOR_RED, "0", SHELLY_COLOR_GREEN, "0", SHELLY_COLOR_BLUE, "255")
                    ),
            
            Arguments.of(THING_TYPE_SHELLYPRORGBWWPM,
                    1,
                    1,
                    SHELLY2_PROFILE_RGBCCT,
                    CHANNEL_GROUP_LIGHT_INDEX,
                    CHANNEL_COLOR_TEMP,
                    new PercentType(50),
                    null,
                    true,
                    Map.of( SHELLY_COLOR_TEMP, avgColorTemp)
                    ),
            
            Arguments.of(THING_TYPE_SHELLYPRORGBWWPM,
                    0,
                    0,
                    SHELLY2_PROFILE_RGBX2LIGHT,
                    CHANNEL_GROUP_COLOR_CONTROL,
                    CHANNEL_COLOR_GREEN,
                    new PercentType(40),
                    null,
                    true,
                    Map.of(SHELLY_COLOR_RED, "0", SHELLY_COLOR_GREEN, "102", SHELLY_COLOR_BLUE, "0")
                    ),
            
            Arguments.of(THING_TYPE_SHELLYPRORGBWWPM,
                    2,
                    2,
                    SHELLY2_PROFILE_RGBX2LIGHT,
                    CHANNEL_GROUP_LIGHT_INDEX,
                    CHANNEL_BRIGHTNESS,
                    new PercentType(70),
                    null,
                    true,
                    Map.of(SHELLY_COLOR_BRIGHTNESS, "70", SHELLY_LIGHT_TURN, SHELLY_API_ON)
                    ),
            
            Arguments.of(THING_TYPE_SHELLYPRORGBWWPM,
                    1,
                    0,
                    SHELLY2_PROFILE_CCTX2,
                    CHANNEL_GROUP_LIGHT_INDEX,
                    CHANNEL_COLOR_TEMP,
                    new PercentType(50),
                    null,
                    true, Map.of(SHELLY_COLOR_TEMP, avgColorTemp)
                    ),
            
            Arguments.of(THING_TYPE_SHELLYPRORGBWWPM,
                    1,
                    0,
                    SHELLY2_PROFILE_CCTX2,
                    CHANNEL_GROUP_LIGHT_INDEX,
                    CHANNEL_BRIGHTNESS,
                    new PercentType(35),
                    null,
                    true,
                    Map.of(SHELLY_COLOR_BRIGHTNESS, "35", SHELLY_LIGHT_TURN, SHELLY_API_ON)
                    ),
            
            Arguments.of(THING_TYPE_SHELLYPRORGBWWPM,
                    2,
                    1,
                    SHELLY2_PROFILE_CCTX2,
                    CHANNEL_GROUP_LIGHT_INDEX,
                    CHANNEL_COLOR_TEMP,
                    new PercentType(50),
                    null,
                    true,
                    Map.of(SHELLY_COLOR_TEMP, avgColorTemp)
                    ),
            
            Arguments.of(THING_TYPE_SHELLYPRORGBWWPM,
                    2,
                    1,
                    SHELLY2_PROFILE_CCTX2,
                    CHANNEL_GROUP_LIGHT_INDEX,
                    CHANNEL_BRIGHTNESS,
                    new PercentType(35),
                    null,
                    true,
                    Map.of(SHELLY_COLOR_BRIGHTNESS, "35", SHELLY_LIGHT_TURN, SHELLY_API_ON)
                    ),

            Arguments.of(THING_TYPE_SHELLYPLUSDUOBULB,
                    0,
                    0,
                    null,
                    CHANNEL_GROUP_WHITE_CONTROL,
                    CHANNEL_COLOR_TEMP,
                    new PercentType(50),
                    null,
                    true,
                    Map.of(SHELLY_COLOR_TEMP, avgColorTemp)
                    ),

            Arguments.of(THING_TYPE_SHELLYPLUSCOLORBULB,
                    0,
                    0,
                    null,
                    CHANNEL_GROUP_COLOR_CONTROL,
                    CHANNEL_COLOR_RED,
                    PercentType.HUNDRED,
                    null,
                    true,
                    Map.of(SHELLY_COLOR_RED, "255", SHELLY_COLOR_GREEN, "0", SHELLY_COLOR_BLUE, "0", SHELLY_COLOR_WHITE, "0")
                    )
        // @formatter:on
        );
    }

    @ParameterizedTest
    @MethodSource("rgbwHsbCommandProvider")
    void parameterizedHsbCommandOnRgbwDeviceSendsExpectedRgbw(HSBType command, String expectedRed, String expectedGreen,
            String expectedBlue, String expectedWhite) throws Exception {
        ShellyTestLightHandler handler = ShellyTestLightHandler.create(THING_TYPE_SHELLYPRORGBWWPM);
        Shelly1HttpApi api = (Shelly1HttpApi) getField(handler, ShellyBaseHandler.class, "api");
        assertNotNull(api);

        handler.profile.device.profile = SHELLY2_PROFILE_RGBW;
        handler.profile.maxTemp = 6500;
        handler.profile.minTemp = 2700;
        handler.profile.isRGBW2 = true;
        handler.profile.inColor = true;

        doNothing().when(api).setLightParms(anyInt(), anyMap());
        doNothing().when(api).setLightMode(anyString());

        // Seed the model with a prior RGBW state
        ShellyLightModel model = ShellyLightModel.create(handler, 0, handler.profile, DIM_STEPSIZE);
        model.acquire();
        try {
            model.setRGBX(new int[] { 12, 34, 56, 78 });
            model.setOnOff(true);
        } finally {
            model.release();
        }
        handler.lightModels.put(0, model);

        clearInvocations(api);

        ChannelUID channelUID = new ChannelUID(
                new ChannelGroupUID(new ThingUID(THING_TYPE_SHELLYPRORGBWWPM, "test"), CHANNEL_GROUP_COLOR_CONTROL),
                CHANNEL_COLOR_PICKER);

        boolean handled = handler.handleDeviceCommand(channelUID, command);

        assertTrue(handled, "HSB command should be handled");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> parmsCaptor = ArgumentCaptor
                .forClass((Class<Map<String, String>>) (Class<?>) Map.class);

        verify(api).setLightParms(eq(0), parmsCaptor.capture());

        Map<String, String> parms = parmsCaptor.getValue();
        assertEquals(expectedRed, parms.get(SHELLY_COLOR_RED));
        assertEquals(expectedGreen, parms.get(SHELLY_COLOR_GREEN));
        assertEquals(expectedBlue, parms.get(SHELLY_COLOR_BLUE));
        assertEquals(expectedWhite, parms.get(SHELLY_COLOR_WHITE));
    }

    private static Stream<Arguments> rgbwHsbCommandProvider() {
        PercentType fifty = new PercentType(50);
        return Stream.of(
        // @formatter:off
            Arguments.of(HSBType.RED, "255", "0", "0", "0"),
            Arguments.of(HSBType.GREEN, "0", "255", "0", "0"), 
            Arguments.of(HSBType.BLUE, "0", "0", "255", "0"),
            Arguments.of(HSBType.WHITE, "0", "0", "0", "255"), 
            Arguments.of(HSBType.BLACK, "0", "0", "0", "255"),
            Arguments.of(new HSBType(new DecimalType(180), PercentType.HUNDRED, PercentType.HUNDRED), "0", "255", "255", "0"), // cyan
            Arguments.of(new HSBType(new DecimalType(300), PercentType.HUNDRED, PercentType.HUNDRED), "255", "0", "255", "0"), // magenta
            Arguments.of(new HSBType(new DecimalType(60), PercentType.HUNDRED, PercentType.HUNDRED), "255", "255", "0", "0"),  // yellow
            Arguments.of(new HSBType(new DecimalType(180), fifty, PercentType.HUNDRED), "0", "128", "128", "128"), // pastel cyan
            Arguments.of(new HSBType(new DecimalType(300), fifty, PercentType.HUNDRED), "128", "0", "128", "128"), // pastel magenta
            Arguments.of(new HSBType(new DecimalType(60), fifty, PercentType.HUNDRED), "128", "128", "0", "128") // pastel yellow
        // @formatter:on
        );
    }

    @ParameterizedTest
    @MethodSource("rgbHsbCommandProvider")
    void parameterizedHsbCommandOnRgbDeviceSendsExpectedRgb(HSBType command, String expectedRed, String expectedGreen,
            String expectedBlue) throws Exception {
        ShellyTestLightHandler handler = ShellyTestLightHandler.create(THING_TYPE_SHELLYPRORGBWWPM);
        Shelly1HttpApi api = (Shelly1HttpApi) getField(handler, ShellyBaseHandler.class, "api");
        assertNotNull(api);

        handler.profile.device.profile = SHELLY2_PROFILE_RGB;
        handler.profile.maxTemp = 6500;
        handler.profile.minTemp = 2700;
        handler.profile.isRGBW2 = true;
        handler.profile.inColor = true;

        doNothing().when(api).setLightParms(anyInt(), anyMap());
        doNothing().when(api).setLightMode(anyString());

        // Seed the model with a prior RGB state
        ShellyLightModel model = ShellyLightModel.create(handler, 0, handler.profile, DIM_STEPSIZE);
        model.acquire();
        try {
            model.setRGBX(new int[] { 12, 34, 56 });
            model.setOnOff(true);
        } finally {
            model.release();
        }
        handler.lightModels.put(0, model);

        clearInvocations(api);

        ChannelUID channelUID = new ChannelUID(
                new ChannelGroupUID(new ThingUID(THING_TYPE_SHELLYPRORGBWWPM, "test"), CHANNEL_GROUP_COLOR_CONTROL),
                CHANNEL_COLOR_PICKER);

        boolean handled = handler.handleDeviceCommand(channelUID, command);

        assertTrue(handled, "HSB command should be handled");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> parmsCaptor = ArgumentCaptor
                .forClass((Class<Map<String, String>>) (Class<?>) Map.class);

        verify(api).setLightParms(eq(0), parmsCaptor.capture());

        Map<String, String> parms = parmsCaptor.getValue();
        assertEquals(expectedRed, parms.get(SHELLY_COLOR_RED));
        assertEquals(expectedGreen, parms.get(SHELLY_COLOR_GREEN));
        assertEquals(expectedBlue, parms.get(SHELLY_COLOR_BLUE));
        assertFalse(parms.containsKey(SHELLY_COLOR_WHITE), "white should not be sent for RGB-only devices");
    }

    private static Stream<Arguments> rgbHsbCommandProvider() {
        PercentType fifty = new PercentType(50);
        return Stream.of(
        // @formatter:off
            Arguments.of(HSBType.RED, "255", "0", "0"),
            Arguments.of(HSBType.GREEN, "0", "255", "0"),
            Arguments.of(HSBType.BLUE, "0", "0", "255"),
            Arguments.of(HSBType.WHITE, "255", "255", "255"),
            Arguments.of(HSBType.BLACK, "255", "255", "255"),
            Arguments.of(new HSBType(new DecimalType(180), PercentType.HUNDRED, PercentType.HUNDRED), "0", "255", "255"), // cyan
            Arguments.of(new HSBType(new DecimalType(300), PercentType.HUNDRED, PercentType.HUNDRED), "255", "0", "255"), // magenta
            Arguments.of(new HSBType(new DecimalType(60), PercentType.HUNDRED, PercentType.HUNDRED), "255", "255", "0"),  // yellow
            Arguments.of(new HSBType(new DecimalType(180), fifty, PercentType.HUNDRED), "128", "255", "255"), // pastel cyan
            Arguments.of(new HSBType(new DecimalType(300), fifty, PercentType.HUNDRED), "255", "128", "255"), // pastel magenta
            Arguments.of(new HSBType(new DecimalType(60), fifty, PercentType.HUNDRED), "255", "255", "128") // pastel yellow
        // @formatter:on
        );
    }

    @ParameterizedTest
    @MethodSource("lightHandlerChannelUpdateProvider")
    void parameterizedLightHandlerChannelUpdates(ThingTypeUID thingTypeUID, int channelGroupNo,
            int expectedApiLightIndex, @Nullable String profileOverride, String commandGroup, String commandChannel,
            Command command, ShellyLightModel.Mode expectedMode, Map<String, State> expectedUpdates) {

        ShellyTestLightHandler handler = ShellyTestLightHandler.create(thingTypeUID);
        handler.profile.device.profile = profileOverride;
        handler.profile.maxTemp = 6500;
        handler.profile.minTemp = 2700;

        if (THING_TYPE_SHELLYPLUSRGBWPM.equals(thingTypeUID) || THING_TYPE_SHELLYPRORGBWWPM.equals(thingTypeUID)) {
            handler.profile.isRGBW2 = true;
            handler.profile.inColor = List
                    .of(SHELLY2_PROFILE_RGB, SHELLY2_PROFILE_RGBW, SHELLY2_PROFILE_RGBCCT, SHELLY2_PROFILE_RGBX2LIGHT)
                    .contains(profileOverride) && channelGroupNo == 0;
        } else if (THING_TYPE_SHELLYRGBW2_COLOR.equals(thingTypeUID)) {
            handler.profile.isRGBW2 = true;
            handler.profile.inColor = true;
        } else if (THING_TYPE_SHELLYRGBW2_WHITE.equals(thingTypeUID)) {
            handler.profile.isRGBW2 = true;
            handler.profile.inColor = false;
        }

        String actualGroup = commandGroup;
        if (CHANNEL_GROUP_LIGHT_INDEX.equals(commandGroup)) {
            actualGroup = CHANNEL_GROUP_LIGHT_INDEX + channelGroupNo;
        }

        ChannelUID channelUID = new ChannelUID(new ChannelGroupUID(new ThingUID(thingTypeUID, "test"), actualGroup),
                commandChannel);

        boolean handled = handler.handleDeviceCommand(channelUID, command);

        assertTrue(handled, "command should be handled for " + thingTypeUID + " / " + profileOverride);

        try {
            handler.acquireLock();
            ShellyLightModel model = handler.getLightModelByGroupNumber(channelGroupNo);
            assertNotNull(model, "expected light model for lightId " + channelGroupNo);
            assertEquals(expectedMode, model.getMode(), "unexpected operating mode");
            assertEquals(expectedApiLightIndex, model.getApiLightIndex(), "unexpected API light index");
        } finally {
            handler.releaseLock();
        }

        Map<String, State> updates = handler.getChannelUpdates();

        for (Map.Entry<String, State> entry : expectedUpdates.entrySet()) {
            assertTrue(updates.containsKey(entry.getKey()), "missing update for " + entry.getKey());
            assertEquals(entry.getValue(), updates.get(entry.getKey()), "unexpected value for " + entry.getKey());
        }
    }

    private static Stream<Arguments> lightHandlerChannelUpdateProvider() {
        return Stream.of(
        // @formatter:off
            Arguments.of(
                THING_TYPE_SHELLYBULB, 0, 0, null,
                CHANNEL_GROUP_COLOR_CONTROL, CHANNEL_COLOR_RED, PercentType.HUNDRED,
                ShellyLightModel.Mode.COLOR,
                Map.of(
                    "color#red", PercentType.HUNDRED,
                    "color#green", PercentType.ZERO,
                    "color#blue", PercentType.ZERO,
                    "color#white", PercentType.ZERO,
                    "color#full", new StringType("red")
                )
            ),

            Arguments.of(
                THING_TYPE_SHELLYDUO, 0, 0, null,
                CHANNEL_GROUP_WHITE_CONTROL, CHANNEL_BRIGHTNESS, new PercentType(42),
                ShellyLightModel.Mode.WHITE,
                Map.of(
                    "white#brightness", new PercentType(42)
                )
            ),

            Arguments.of(
                THING_TYPE_SHELLYVINTAGE, 0, 0, null,
                CHANNEL_GROUP_WHITE_CONTROL, CHANNEL_BRIGHTNESS, new PercentType(25),
                ShellyLightModel.Mode.WHITE,
                Map.of(
                    "white#brightness", new PercentType(25)
                )
            ),

            Arguments.of(
                THING_TYPE_SHELLYRGBW2_COLOR, 0, 0, null,
                CHANNEL_GROUP_COLOR_CONTROL, CHANNEL_COLOR_BLUE, PercentType.HUNDRED,
                ShellyLightModel.Mode.COLOR,
                Map.of(
                    "color#red", PercentType.ZERO,
                    "color#green", PercentType.ZERO,
                    "color#blue", PercentType.HUNDRED,
                    "color#white", PercentType.ZERO,
                    "color#full", new StringType("blue")
                )
            ),

            Arguments.of(
                THING_TYPE_SHELLYRGBW2_WHITE, 1, 0, null,
                CHANNEL_GROUP_LIGHT_INDEX, CHANNEL_BRIGHTNESS, new PercentType(73),
                ShellyLightModel.Mode.WHITE,
                Map.of(
                    "light1#brightness", new PercentType(73)
                )
            ),

            Arguments.of(
                THING_TYPE_SHELLYRGBW2_WHITE, 2, 1, null,
                CHANNEL_GROUP_LIGHT_INDEX, CHANNEL_BRIGHTNESS, new PercentType(61),
                ShellyLightModel.Mode.WHITE,
                Map.of(
                    "light2#brightness", new PercentType(61)
                )
            ),

            Arguments.of(
                THING_TYPE_SHELLYPLUSRGBWPM, 0, 0, SHELLY2_PROFILE_RGBW,
                CHANNEL_GROUP_COLOR_CONTROL, CHANNEL_COLOR_GREEN, PercentType.HUNDRED,
                ShellyLightModel.Mode.COLOR,
                Map.of(
                    "color#red", PercentType.ZERO,
                    "color#green", PercentType.HUNDRED,
                    "color#blue", PercentType.ZERO,
                    "color#white", PercentType.ZERO,
                    "color#full", new StringType("green")
                )
            ),

            Arguments.of(
                THING_TYPE_SHELLYPLUSRGBWPM, 1, 0, SHELLY2_PROFILE_LIGHT,
                CHANNEL_GROUP_LIGHT_INDEX, CHANNEL_BRIGHTNESS, new PercentType(55),
                ShellyLightModel.Mode.WHITE,
                Map.of(
                    "light1#brightness", new PercentType(55)
                )
            ),

            Arguments.of(
                THING_TYPE_SHELLYPLUSRGBWPM, 2, 1, SHELLY2_PROFILE_LIGHT,
                CHANNEL_GROUP_LIGHT_INDEX, CHANNEL_BRIGHTNESS, new PercentType(45),
                ShellyLightModel.Mode.WHITE,
                Map.of(
                    "light2#brightness", new PercentType(45)
                )
            ),

            Arguments.of(
                THING_TYPE_SHELLYPRORGBWWPM, 0, 0, SHELLY2_PROFILE_RGB,
                CHANNEL_GROUP_COLOR_CONTROL, CHANNEL_COLOR_RED, new PercentType(1),
                ShellyLightModel.Mode.COLOR,
                Map.of(
                    "color#red", new PercentType(1),
                    "color#green", PercentType.ZERO,
                    "color#blue", PercentType.ZERO,
                    "color#full", UnDefType.UNDEF
                )
            ),

            Arguments.of(
                THING_TYPE_SHELLYPRORGBWWPM, 0, 0, SHELLY2_PROFILE_RGBW,
                CHANNEL_GROUP_COLOR_CONTROL, CHANNEL_COLOR_WHITE, new PercentType(33),
                ShellyLightModel.Mode.COLOR,
                Map.of(
                    "color#red", PercentType.ZERO,
                    "color#green", PercentType.ZERO,
                    "color#blue", PercentType.ZERO,
                    "color#white", new PercentType(33),
                    "color#full", UnDefType.UNDEF
                )
            ),

            Arguments.of(
                THING_TYPE_SHELLYPRORGBWWPM, 1, 0, SHELLY2_PROFILE_LIGHT,
                CHANNEL_GROUP_LIGHT_INDEX, CHANNEL_BRIGHTNESS, new PercentType(44),
                ShellyLightModel.Mode.WHITE,
                Map.of(
                    "light1#brightness", new PercentType(44)
                )
            ),

            Arguments.of(
                THING_TYPE_SHELLYPRORGBWWPM, 2, 1, SHELLY2_PROFILE_LIGHT,
                CHANNEL_GROUP_LIGHT_INDEX, CHANNEL_BRIGHTNESS, new PercentType(66),
                ShellyLightModel.Mode.WHITE,
                Map.of(
                    "light2#brightness", new PercentType(66)
                )
            ),

            Arguments.of(
                THING_TYPE_SHELLYPRORGBWWPM, 0, 0, SHELLY2_PROFILE_RGBCCT,
                CHANNEL_GROUP_COLOR_CONTROL, CHANNEL_COLOR_BLUE, PercentType.HUNDRED,
                ShellyLightModel.Mode.COLOR,
                Map.of(
                    "color#red", PercentType.ZERO,
                    "color#green", PercentType.ZERO,
                    "color#blue", PercentType.HUNDRED,
                    "color#full", new StringType("blue")
                )
            ),

            Arguments.of(
                THING_TYPE_SHELLYPRORGBWWPM, 1, 1, SHELLY2_PROFILE_RGBCCT,
                CHANNEL_GROUP_LIGHT_INDEX, CHANNEL_COLOR_TEMP, new PercentType(50),
                ShellyLightModel.Mode.WHITE,
                Map.of(
                    "light1#temperature", new PercentType(50)
                )
            ),

            Arguments.of(
                THING_TYPE_SHELLYPRORGBWWPM, 0, 0, SHELLY2_PROFILE_RGBX2LIGHT,
                CHANNEL_GROUP_COLOR_CONTROL, CHANNEL_COLOR_GREEN, new PercentType(40),
                ShellyLightModel.Mode.COLOR,
                Map.of(
                    "color#red", PercentType.ZERO,
                    "color#green", new PercentType(40),
                    "color#blue", PercentType.ZERO,
                    "color#full", UnDefType.UNDEF
                )
            ),

            Arguments.of(
                THING_TYPE_SHELLYPRORGBWWPM, 1, 1, SHELLY2_PROFILE_RGBX2LIGHT,
                CHANNEL_GROUP_LIGHT_INDEX, CHANNEL_BRIGHTNESS, new PercentType(70),
                ShellyLightModel.Mode.WHITE,
                Map.of(
                    "light1#brightness", new PercentType(70)
                )
            ),

            Arguments.of(
                THING_TYPE_SHELLYPRORGBWWPM, 2, 1, SHELLY2_PROFILE_CCTX2,
                CHANNEL_GROUP_LIGHT_INDEX, CHANNEL_COLOR_TEMP, new PercentType(60),
                ShellyLightModel.Mode.WHITE,
                Map.of(
                    "light2#temperature", new PercentType(60)
                )
            ),

            Arguments.of(
                THING_TYPE_SHELLYPRORGBWWPM, 1, 0, SHELLY2_PROFILE_CCTX2,
                CHANNEL_GROUP_LIGHT_INDEX, CHANNEL_BRIGHTNESS, new PercentType(35),
                ShellyLightModel.Mode.WHITE,
                Map.of(
                    "light1#brightness", new PercentType(35)
                )
            ),

            Arguments.of(
                THING_TYPE_SHELLYPLUSDUOBULB, 0, 0, null,
                CHANNEL_GROUP_WHITE_CONTROL, CHANNEL_COLOR_TEMP, new PercentType(50),
                ShellyLightModel.Mode.WHITE,
                Map.of(
                    "white#temperature", new PercentType(50)
                )
            ),

            Arguments.of(
                THING_TYPE_SHELLYPLUSCOLORBULB, 0, 0, null,
                CHANNEL_GROUP_COLOR_CONTROL, CHANNEL_COLOR_RED, PercentType.HUNDRED,
                ShellyLightModel.Mode.COLOR,
                Map.of(
                    "color#red", PercentType.HUNDRED,
                    "color#green", PercentType.ZERO,
                    "color#blue", PercentType.ZERO,
                    "color#white", PercentType.ZERO,
                    "color#full", new StringType("red")
                )
            )
        // @formatter:on
        );
    }
}
