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
import static org.openhab.binding.shelly.internal.ShellyDevices.*;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.handler.ShellyLightModel.RGBX.*;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.handler.ShellyLightModel.Mode;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Tests for {@link ShellyLightModel} basic functionality.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class ShellyLightModelTest {

    private static final double STEP = 10.0;

    private static ShellyLightHandler mockHandler(ThingTypeUID thingTypeUID) {
        ShellyLightHandler handler = mock(ShellyLightHandler.class);
        Thing thing = mock(Thing.class);
        when(thing.getLabel()).thenReturn("Test Thing");
        when(thing.getThingTypeUID()).thenReturn(thingTypeUID);
        when(handler.getThing()).thenReturn(thing);
        return handler;
    }

    @Test
    void duoStartsInWhiteOnlyMode() {
        ShellyLightModel model = ShellyLightModel.create(mockHandler(THING_TYPE_SHELLYDUO), 0,
                new ShellyDeviceProfile(THING_TYPE_SHELLYDUO), STEP);
        assertEquals(Mode.WHITE, model.getMode());
    }

    @Test
    void bulbStartsInColorMode() {
        ShellyLightModel model = ShellyLightModel.create(mockHandler(THING_TYPE_SHELLYBULB), 0,
                new ShellyDeviceProfile(THING_TYPE_SHELLYBULB), STEP);
        assertEquals(Mode.COLOR, model.getMode());
    }

    @Test
    void setModeMarksDirtyWhenChanged() {
        ShellyLightModel model = ShellyLightModel.create(mockHandler(THING_TYPE_SHELLYBULB), 0,
                new ShellyDeviceProfile(THING_TYPE_SHELLYBULB), STEP);

        model.setMode(Mode.WHITE);
        model.acquire();

        model.setMode(Mode.COLOR);
        assertTrue(model.isModeDirty());
        model.release();

        model.acquire();
        assertFalse(model.isModeDirty());

        model.setMode(Mode.WHITE);
        assertTrue(model.isModeDirty());
        assertEquals(Mode.WHITE, model.getMode());
    }

    @Test
    void acquireLockResetsAllFlags() {
        ShellyLightModel model = ShellyLightModel.create(mockHandler(THING_TYPE_SHELLYBULB), 0,
                new ShellyDeviceProfile(THING_TYPE_SHELLYBULB), STEP);

        model.acquire();

        model.setMode(Mode.WHITE);
        model.setColor(R, 255);
        model.setBrightness(42);
        model.setGain(33);
        model.setEffect(4);
        model.setColorTemp(4000);
        model.setOnOff(true);

        model.release();
        model.acquire();

        assertFalse(model.isModeDirty());
        assertFalse(model.isColorDirty());
        assertFalse(model.isBrightnessDirty());
        assertFalse(model.isGainDirty());
        assertFalse(model.isEffectDirty());
        assertFalse(model.isColorTempDirty());
        assertFalse(model.isOnOffDirty());
    }

    @Test
    void handleColorUpdatesSelectedComponentAndMarksDirty() {
        ShellyLightModel model = ShellyLightModel.create(mockHandler(THING_TYPE_SHELLYBULB), 0,
                new ShellyDeviceProfile(THING_TYPE_SHELLYBULB), STEP);

        model.acquire();

        model.setColor(R, 255);
        model.setColor(G, 128);
        model.setColor(B, 0);
        model.setColor(CW, 64);

        assertEquals(255, model.getColor(R));
        assertEquals(128, model.getColor(G));
        assertEquals(0, model.getColor(B));
        assertEquals(64, model.getColor(CW));
        assertTrue(model.isColorDirty());
    }

    @Test
    void handleRgbwStringParsesCsvAndMarksDirty() {
        ShellyLightModel model = ShellyLightModel.create(mockHandler(THING_TYPE_SHELLYBULB), 0,
                new ShellyDeviceProfile(THING_TYPE_SHELLYBULB), STEP);

        model.acquire();

        model.setRGBX("255, 128, 0, 64");

        assertEquals(255, model.getColor(R));
        assertEquals(128, model.getColor(G));
        assertEquals(0, model.getColor(B));
        assertEquals(64, model.getColor(CW));
        assertTrue(model.isColorDirty());
    }

    @Test
    void handleRgbwStringRejectsInvalidCsv() {
        ShellyLightModel model = ShellyLightModel.create(mockHandler(THING_TYPE_SHELLYBULB), 0,
                new ShellyDeviceProfile(THING_TYPE_SHELLYBULB), STEP);
        assertThrows(NumberFormatException.class, () -> model.setRGBX("red,green,blue,white"));
    }

    @Test
    void modeSynchronizesToColor() {
        ShellyLightModel model = ShellyLightModel.create(mockHandler(THING_TYPE_SHELLYBULB), 0,
                new ShellyDeviceProfile(THING_TYPE_SHELLYBULB), STEP);

        model.setMode(Mode.WHITE);

        model.acquire();
        model.setRGBX(255, 0, 0, 0);

        assertEquals(Mode.COLOR, model.getMode());
        assertTrue(model.isModeDirty());
    }

    @Test
    void handleGainUpdatesGainAndMarksDirty() {
        ShellyLightModel model = ShellyLightModel.create(mockHandler(THING_TYPE_SHELLYBULB), 0,
                new ShellyDeviceProfile(THING_TYPE_SHELLYBULB), STEP);

        model.setGain(77);

        assertTrue(model.getGainState() instanceof PercentType);
        assertEquals(77, ((PercentType) model.getGainState()).intValue());
        assertTrue(model.isGainDirty());
    }

    @Test
    void handleEffectUpdatesEffectAndMarksDirty() {
        ShellyLightModel model = ShellyLightModel.create(mockHandler(THING_TYPE_SHELLYBULB), 0,
                new ShellyDeviceProfile(THING_TYPE_SHELLYBULB), STEP);

        model.setEffect(5);

        assertTrue(model.getEffectState() instanceof DecimalType);
        assertEquals(5, ((DecimalType) model.getEffectState()).intValue());
        assertTrue(model.isEffectDirty());
    }

    @Test
    void handleBrightnessUpdatesBrightnessAndMarksDirty() {
        ShellyLightModel model = ShellyLightModel.create(mockHandler(THING_TYPE_SHELLYDUO), 0,
                new ShellyDeviceProfile(THING_TYPE_SHELLYDUO), STEP);

        model.setBrightness(42);

        assertTrue(model.isBrightnessDirty());
        PercentType bri = model.getBrightness(true);
        assertNotNull(bri);
        assertEquals(42, bri.intValue());
    }

    @Test
    void handleColorTempUpdatesKelvinAndMarksDirty() {
        ShellyLightModel model = ShellyLightModel.create(mockHandler(THING_TYPE_SHELLYDUO), 0,
                new ShellyDeviceProfile(THING_TYPE_SHELLYDUO), STEP);

        model.setColorTemp(4000);

        assertTrue(model.isColorTempDirty());
        QuantityType<?> qty = model.getColorTemperature();
        assertNotNull(qty);
        qty = qty.toUnit(Units.KELVIN);
        assertNotNull(qty);
        QuantityType<?> kelvin = qty.toUnit(Units.KELVIN);
        assertNotNull(kelvin);
        assertEquals(4000, kelvin.intValue());
        assertEquals(Mode.WHITE, model.getMode());
    }

    @Test
    void handleOnOffMarksDirty() {
        ShellyLightModel model = ShellyLightModel.create(mockHandler(THING_TYPE_SHELLYDUO), 0,
                new ShellyDeviceProfile(THING_TYPE_SHELLYDUO), STEP);

        model.setBrightness(50);
        model.setOnOff(true);

        assertTrue(model.isOnOffDirty());
        assertEquals(OnOffType.ON, model.getOnOff(true));
    }

    @Test
    void handleCommandOnSetsOnlyOnOffDirtyAndBrightnessDirty() {
        ShellyLightModel model = ShellyLightModel.create(mockHandler(THING_TYPE_SHELLYDUO), 0,
                new ShellyDeviceProfile(THING_TYPE_SHELLYDUO), STEP);

        // Set initial brightness
        model.setBrightness(50);

        // Turn ON (this sets mode=COLOR)
        model.handleCommand(OnOffType.ON);

        // Capture initial state
        model.acquire();

        // Turn OFF (this sets brightness=0)
        model.handleCommand(OnOffType.OFF);

        assertTrue(model.isOnOffDirty(), "ON/OFF should be dirty");
        assertTrue(model.isBrightnessDirty(), "Brightness changes when turning OFF");
        assertFalse(model.isColorDirty(), "Color should not be dirty for a white-only lamp");
    }

    @Test
    void handleCommandPercentMarksGainAndOnOffDirty() {
        ShellyLightModel model = ShellyLightModel.create(mockHandler(THING_TYPE_SHELLYDUO), 0,
                new ShellyDeviceProfile(THING_TYPE_SHELLYDUO), STEP);

        model.acquire();

        model.handleCommand(new PercentType(60));

        assertTrue(model.isGainDirty());
        assertTrue(model.isOnOffDirty());
        assertFalse(model.isColorDirty());
    }

    @Test
    void handleCommandIncreaseMarksGainAndOnOffDirty() {
        ShellyLightModel model = ShellyLightModel.create(mockHandler(THING_TYPE_SHELLYDUO), 0,
                new ShellyDeviceProfile(THING_TYPE_SHELLYDUO), STEP);

        model.acquire();

        model.handleCommand(IncreaseDecreaseType.INCREASE);

        assertTrue(model.isGainDirty());
        assertTrue(model.isOnOffDirty());
        assertFalse(model.isColorDirty());
    }

    @Test
    void handleCommandHsbMarksColorGainAndOnOffDirty() {
        ShellyLightModel model = ShellyLightModel.create(mockHandler(THING_TYPE_SHELLYBULB), 0,
                new ShellyDeviceProfile(THING_TYPE_SHELLYBULB), STEP);

        model.acquire();

        model.handleCommand(HSBType.RED);

        assertTrue(model.isColorDirty());
        assertTrue(model.isGainDirty());
        assertTrue(model.isOnOffDirty());
    }

    @Test
    void gainAndBrightnessAreSynonyms() {
        ShellyLightModel model = ShellyLightModel.create(mockHandler(THING_TYPE_SHELLYBULB), 0,
                new ShellyDeviceProfile(THING_TYPE_SHELLYBULB), STEP);
        model.setBrightness(100);
        model.setGain(20);

        assertEquals(new PercentType(20), model.getBrightnessState());
        assertEquals(new PercentType(20), model.getGainState());

        model.setBrightness(100);

        assertEquals(PercentType.HUNDRED, model.getBrightnessState());
        assertEquals(PercentType.HUNDRED, model.getGainState());
    }

    @Test
    void toStringContainsUsefulFields() {
        ShellyLightModel model = ShellyLightModel.create(mockHandler(THING_TYPE_SHELLYBULB), 0,
                new ShellyDeviceProfile(THING_TYPE_SHELLYBULB), STEP);
        model.setRGBX(255, 0, 0, 0);
        model.setBrightness(100);
        model.setGain(20);
        model.setEffect(1);
        model.setColorTemp(4000);

        String s = model.toString();

        assertTrue(s.contains("mode"));
        assertTrue(s.contains("power"));
        assertTrue(s.contains("rgbw"));
        assertTrue(s.contains("effect"));
    }

    @Test
    void testHSB() {
        ShellyLightModel model = ShellyLightModel.create(mockHandler(THING_TYPE_SHELLYBULB), 0,
                new ShellyDeviceProfile(THING_TYPE_SHELLYBULB), STEP);
        model.setGain(100);

        model.setRGBX(255, 0, 0, 0);
        assertEquals(HSBType.RED, model.getColor());

        model.setRGBX(0, 255, 0, 0);
        assertEquals(HSBType.GREEN, model.getColor());

        model.setRGBX(0, 0, 255, 0);
        assertEquals(HSBType.BLUE, model.getColor());

        model.setRGBX(0, 0, 0, 0);
        assertEquals(HSBType.WHITE, model.getColor());

        model.setRGBX(0, 0, 0, 255);
        assertEquals(HSBType.WHITE, model.getColor());

        model.setRGBX(100, 100, 100, 155);
        assertEquals(HSBType.WHITE, model.getColor());

        model.setGain(0);
        model.setRGBX(0, 0, 0, 0);
        assertEquals(HSBType.BLACK, model.getColor());
    }

    @Test
    void testColorTemp() {
        ShellyLightModel model = ShellyLightModel.create(mockHandler(THING_TYPE_SHELLYBULB), 0,
                new ShellyDeviceProfile(THING_TYPE_SHELLYBULB), STEP);
        model.setBrightness(100);

        model.setColorTemp(3000);
        QuantityType<?> qty = model.getColorTemperature();
        assertNotNull(qty);
        qty = qty.toUnit(Units.KELVIN);
        assertNotNull(qty);
        assertEquals(3000, qty.intValue());

        model.setColorTemp(6500);
        qty = model.getColorTemperature();
        assertNotNull(qty);
        qty = qty.toUnit(Units.KELVIN);
        assertNotNull(qty);
        assertEquals(6500, qty.intValue());
    }

    @Test
    void setRgbxCommandRedMapsToExpectedRgbw() {
        ShellyLightModel model = ShellyLightModel.create(mockHandler(THING_TYPE_SHELLYBULB), 0,
                new ShellyDeviceProfile(THING_TYPE_SHELLYBULB), STEP);

        model.setRGBX(new StringType("red"));

        assertEquals(255, model.getColor(R));
        assertEquals(0, model.getColor(G));
        assertEquals(0, model.getColor(B));
        assertEquals(0, model.getColor(CW));
    }

    @Test
    void setRgbxCommandGreenMapsToExpectedRgbw() {
        ShellyLightModel model = ShellyLightModel.create(mockHandler(THING_TYPE_SHELLYBULB), 0,
                new ShellyDeviceProfile(THING_TYPE_SHELLYBULB), STEP);

        model.setRGBX(new StringType("green"));

        assertEquals(0, model.getColor(R));
        assertEquals(255, model.getColor(G));
        assertEquals(0, model.getColor(B));
        assertEquals(0, model.getColor(CW));
    }

    @Test
    void setRgbxCommandBlueMapsToExpectedRgbw() {
        ShellyLightModel model = ShellyLightModel.create(mockHandler(THING_TYPE_SHELLYBULB), 0,
                new ShellyDeviceProfile(THING_TYPE_SHELLYBULB), STEP);

        model.setRGBX(new StringType("blue"));

        assertEquals(0, model.getColor(R));
        assertEquals(0, model.getColor(G));
        assertEquals(255, model.getColor(B));
        assertEquals(0, model.getColor(CW));
    }

    @Test
    void setRgbxCommandYellowMapsToExpectedRgbw() {
        ShellyLightModel model = ShellyLightModel.create(mockHandler(THING_TYPE_SHELLYBULB), 0,
                new ShellyDeviceProfile(THING_TYPE_SHELLYBULB), STEP);

        model.setRGBX(new StringType("yellow"));

        assertEquals(255, model.getColor(R));
        assertEquals(255, model.getColor(G));
        assertEquals(0, model.getColor(B));
        assertEquals(0, model.getColor(CW));
    }

    @Test
    void setRgbxCommandWhiteMapsToExpectedRgbw() {
        ShellyLightModel model = ShellyLightModel.create(mockHandler(THING_TYPE_SHELLYBULB), 0,
                new ShellyDeviceProfile(THING_TYPE_SHELLYBULB), STEP);

        model.setRGBX(new StringType("white"));

        assertEquals(0, model.getColor(R));
        assertEquals(0, model.getColor(G));
        assertEquals(0, model.getColor(B));
        assertEquals(255, model.getColor(CW));
    }

    @Test
    void setRgbxCommandRejectsUnknownColorName() {
        ShellyLightModel model = ShellyLightModel.create(mockHandler(THING_TYPE_SHELLYBULB), 0,
                new ShellyDeviceProfile(THING_TYPE_SHELLYBULB), STEP);

        assertThrows(IllegalArgumentException.class, () -> model.setRGBX(new StringType("magenta-ish")));
    }

    @ParameterizedTest
    @MethodSource("thingTypeProvider")
    void testCapabilitiesForEachThingType(ThingTypeUID thingTypeUID, int componentIndex,
            @Nullable String profileOverride, boolean expectSupportsOnOff, boolean expectSupportsColor,
            boolean expectSupportsColorTemperature, boolean expectSupportsBrightness) {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(thingTypeUID);
        profile.maxTemp = 6500;
        profile.minTemp = 2700;
        profile.device.profile = profileOverride;
        ShellyLightModel model = ShellyLightModel.create(mockHandler(thingTypeUID), componentIndex, profile, STEP);

        String uid = thingTypeUID.toString() + " ";
        assertEquals(expectSupportsOnOff, model.supportsOnOffChannel(), uid + "on/off");
        assertEquals(expectSupportsColor, model.supportsColorChannel(true), uid + "color");
        assertEquals(expectSupportsColorTemperature, model.supportsColorTempChannel(true), uid + "color temp");
        assertEquals(expectSupportsBrightness, model.supportsBrightnessChannel(true), uid + "brightness");
    }

    static Stream<Arguments> thingTypeProvider() {
        // TODO ask maintainer to confirm this table
        return Stream.of( //
        // @formatter:off
            Arguments.of(THING_TYPE_SHELLYBULB, 0, null, true, true, true, true),
            Arguments.of(THING_TYPE_SHELLYDUO, 0, null, true, false, true, true),
            Arguments.of(THING_TYPE_SHELLYVINTAGE, 0, null, true, false, false, true), // NOTE: Vintage white-only!
            Arguments.of(THING_TYPE_SHELLYDUORGBW, 0, null, true, false, true, true),
            Arguments.of(THING_TYPE_SHELLYRGBW2_COLOR, 0, null, true, true, false, false),
            Arguments.of(THING_TYPE_SHELLYRGBW2_WHITE, 0, null, false, false, false, true),
            Arguments.of(THING_TYPE_SHELLYRGBW2_WHITE, 1, null, false, false, false, true),
            Arguments.of(THING_TYPE_SHELLYPLUSRGBWPM, 0, SHELLY2_PROFILE_RGBW, true, true, false, false),
            Arguments.of(THING_TYPE_SHELLYPLUSRGBWPM, 0, SHELLY2_PROFILE_LIGHT, false, false, false, true),
            Arguments.of(THING_TYPE_SHELLYPLUSRGBWPM, 1, SHELLY2_PROFILE_LIGHT, false, false, false, true),
            Arguments.of(THING_TYPE_SHELLYPRORGBWWPM, 0, SHELLY2_PROFILE_RGBCCT, true, true, false, false),
            Arguments.of(THING_TYPE_SHELLYPRORGBWWPM, 1, SHELLY2_PROFILE_RGBCCT, false, false, true, true),
            Arguments.of(THING_TYPE_SHELLYPRORGBWWPM, 0, SHELLY2_PROFILE_CCTX2, false, false, true, true),
            Arguments.of(THING_TYPE_SHELLYPRORGBWWPM, 1, SHELLY2_PROFILE_CCTX2, false, false, true, true),
            Arguments.of(THING_TYPE_SHELLYPRORGBWWPM, 0, SHELLY2_PROFILE_RGBX2LIGHT, true, true, false, false),
            Arguments.of(THING_TYPE_SHELLYPRORGBWWPM, 1, SHELLY2_PROFILE_RGBX2LIGHT, false, false, false, true),
            Arguments.of(THING_TYPE_SHELLYPRORGBWWPM, 0, SHELLY2_PROFILE_LIGHT, false, false, false, true),
            Arguments.of(THING_TYPE_SHELLYPRORGBWWPM, 1, SHELLY2_PROFILE_LIGHT, false, false, false, true),
            // TODO check if Generation 3 bulbs in fact provide a profile
            Arguments.of(THING_TYPE_SHELLYPLUSDUOBULB, 0, null, true, false, true, true),
            Arguments.of(THING_TYPE_SHELLYPLUSCOLORBULB, 0, null, true, true, true, true)
        // @formatter:on
        );
    }
}
