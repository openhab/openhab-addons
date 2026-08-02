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
import static org.openhab.binding.shelly.internal.ShellyDevices.*;
import static org.openhab.binding.shelly.internal.handler.ShellyLightModel.RGBX.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.handler.ShellyLightModel.Mode;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;

@NonNullByDefault
class ShellyLightModelTest {

    private static final double STEP = 10.0;

    @Test
    void duoStartsInWhiteOnlyMode() {
        ShellyLightModel model = ShellyLightModel.create(THING_TYPE_SHELLYDUO,
                new ShellyDeviceProfile(THING_TYPE_SHELLYDUO), STEP);
        assertEquals(Mode.COLOR_TEMP, model.getMode());
    }

    @Test
    void bulbStartsInColorMode() {
        ShellyLightModel model = ShellyLightModel.create(THING_TYPE_SHELLYBULB,
                new ShellyDeviceProfile(THING_TYPE_SHELLYBULB), STEP);
        assertEquals(Mode.COLOR, model.getMode());
    }

    @Test
    void setModeMarksDirtyWhenChanged() {
        ShellyLightModel model = ShellyLightModel.create(THING_TYPE_SHELLYBULB,
                new ShellyDeviceProfile(THING_TYPE_SHELLYBULB), STEP);

        model.setMode(Mode.COLOR);
        model.clearDirtyFlags();

        assertFalse(model.isModeDirty());

        model.setMode(Mode.COLOR_TEMP);
        assertTrue(model.isModeDirty());
        assertEquals(Mode.COLOR_TEMP, model.getMode());
    }

    @Test
    void clearDirtyFlagsResetsAllFlags() {
        ShellyLightModel model = ShellyLightModel.create(THING_TYPE_SHELLYBULB,
                new ShellyDeviceProfile(THING_TYPE_SHELLYBULB), STEP);

        model.setMode(Mode.COLOR_TEMP);
        model.setColor(R, 255);
        model.setBrightness(42);
        model.setGain(33);
        model.setEffect(4);
        model.setColorTemp(4000);
        model.setOnOff(true);

        model.clearDirtyFlags();

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
        ShellyLightModel model = ShellyLightModel.create(THING_TYPE_SHELLYBULB,
                new ShellyDeviceProfile(THING_TYPE_SHELLYBULB), STEP);

        model.setColor(R, 255);
        model.setColor(G, 128);
        model.setColor(B, 0);
        model.setColor(WC, 64);

        assertEquals(255, model.getColor(R));
        assertEquals(128, model.getColor(G));
        assertEquals(0, model.getColor(B));
        assertEquals(64, model.getColor(WC));
        assertTrue(model.isColorDirty());
    }

    @Test
    void handleRgbwStringParsesCsvAndMarksDirty() {
        ShellyLightModel model = ShellyLightModel.create(THING_TYPE_SHELLYBULB,
                new ShellyDeviceProfile(THING_TYPE_SHELLYBULB), STEP);

        model.setRGBX("255, 128, 0, 64");

        assertEquals(255, model.getColor(R));
        assertEquals(128, model.getColor(G));
        assertEquals(0, model.getColor(B));
        assertEquals(64, model.getColor(WC));
        assertTrue(model.isColorDirty());
    }

    @Test
    void handleRgbwStringRejectsInvalidCsv() {
        ShellyLightModel model = ShellyLightModel.create(THING_TYPE_SHELLYBULB,
                new ShellyDeviceProfile(THING_TYPE_SHELLYBULB), STEP);
        assertThrows(NumberFormatException.class, () -> model.setRGBX("red,green,blue,white"));
    }

    @Test
    void modeSynchronizesToRgb() {
        ShellyLightModel model = ShellyLightModel.create(THING_TYPE_SHELLYBULB,
                new ShellyDeviceProfile(THING_TYPE_SHELLYBULB), STEP);

        model.setMode(Mode.COLOR_TEMP);
        model.setRGBX(255, 0, 0, 0);

        assertEquals(Mode.COLOR, model.getMode());
        assertTrue(model.isModeDirty());
    }

    @Test
    void handleGainUpdatesGainAndMarksDirty() {
        ShellyLightModel model = ShellyLightModel.create(THING_TYPE_SHELLYBULB,
                new ShellyDeviceProfile(THING_TYPE_SHELLYBULB), STEP);

        model.setGain(77);

        assertTrue(model.getGainState() instanceof PercentType);
        assertEquals(77, ((PercentType) model.getGainState()).intValue());
        assertTrue(model.isGainDirty());
    }

    @Test
    void handleEffectUpdatesEffectAndMarksDirty() {
        ShellyLightModel model = ShellyLightModel.create(THING_TYPE_SHELLYBULB,
                new ShellyDeviceProfile(THING_TYPE_SHELLYBULB), STEP);

        model.setEffect(5);

        assertTrue(model.getEffectState() instanceof DecimalType);
        assertEquals(5, ((DecimalType) model.getEffectState()).intValue());
        assertTrue(model.isEffectDirty());
    }

    @Test
    void handleBrightnessUpdatesBrightnessAndMarksDirty() {
        ShellyLightModel model = ShellyLightModel.create(THING_TYPE_SHELLYDUO,
                new ShellyDeviceProfile(THING_TYPE_SHELLYDUO), STEP);

        model.setBrightness(42);

        assertTrue(model.isBrightnessDirty());
        PercentType bri = model.getBrightness(true);
        assertNotNull(bri);
        assertEquals(42, bri.intValue());
    }

    @Test
    void handleColorTempUpdatesKelvinAndMarksDirty() {
        ShellyLightModel model = ShellyLightModel.create(THING_TYPE_SHELLYDUO,
                new ShellyDeviceProfile(THING_TYPE_SHELLYDUO), STEP);

        model.setColorTemp(4000);

        assertTrue(model.isColorTempDirty());
        QuantityType<?> qty = model.getColorTemperature();
        assertNotNull(qty);
        qty = qty.toUnit(Units.KELVIN);
        assertNotNull(qty);
        assertEquals(4000, qty.toUnit(Units.KELVIN).intValue());
        assertEquals(Mode.COLOR_TEMP, model.getMode());
    }

    @Test
    void handleOnOffMarksDirty() {
        ShellyLightModel model = ShellyLightModel.create(THING_TYPE_SHELLYDUO,
                new ShellyDeviceProfile(THING_TYPE_SHELLYDUO), STEP);

        model.setBrightness(50);
        model.setOnOff(true);

        assertTrue(model.isOnOffDirty());
        assertEquals(OnOffType.ON, model.getOnOff(true));
    }

    @Test
    void handleCommandOnSetsOnOffDirtyOnly() {
        ShellyLightModel model = ShellyLightModel.create(THING_TYPE_SHELLYDUO,
                new ShellyDeviceProfile(THING_TYPE_SHELLYDUO), STEP);

        model.handleCommand(OnOffType.ON);

        assertTrue(model.isOnOffDirty());
        assertFalse(model.isColorDirty());
        assertFalse(model.isBrightnessDirty());
    }

    @Test
    void handleCommandPercentMarksGainAndOnOffDirty() {
        ShellyLightModel model = ShellyLightModel.create(THING_TYPE_SHELLYDUO,
                new ShellyDeviceProfile(THING_TYPE_SHELLYDUO), STEP);

        model.handleCommand(new PercentType(60));

        assertTrue(model.isGainDirty());
        assertTrue(model.isOnOffDirty());
        assertFalse(model.isColorDirty());
    }

    @Test
    void handleCommandIncreaseMarksGainAndOnOffDirty() {
        ShellyLightModel model = ShellyLightModel.create(THING_TYPE_SHELLYDUO,
                new ShellyDeviceProfile(THING_TYPE_SHELLYDUO), STEP);

        model.handleCommand(IncreaseDecreaseType.INCREASE);

        assertTrue(model.isGainDirty());
        assertTrue(model.isOnOffDirty());
        assertFalse(model.isColorDirty());
    }

    @Test
    void handleCommandHsbMarksColorGainAndOnOffDirty() {
        ShellyLightModel model = ShellyLightModel.create(THING_TYPE_SHELLYBULB,
                new ShellyDeviceProfile(THING_TYPE_SHELLYBULB), STEP);

        model.handleCommand(HSBType.RED);

        assertTrue(model.isColorDirty());
        assertTrue(model.isGainDirty());
        assertTrue(model.isOnOffDirty());
    }

    @Test
    void gainAndBrightnessAreSynonyms() {
        ShellyLightModel model = ShellyLightModel.create(THING_TYPE_SHELLYBULB,
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
        ShellyLightModel model = ShellyLightModel.create(THING_TYPE_SHELLYBULB,
                new ShellyDeviceProfile(THING_TYPE_SHELLYBULB), STEP);
        model.setRGBX(255, 0, 0, 0);
        model.setBrightness(100);
        model.setGain(20);
        model.setEffect(1);
        model.setColorTemp(4000);

        String s = model.toString();

        assertTrue(s.contains("mode="));
        assertTrue(s.contains("power="));
        assertTrue(s.contains("rgbw="));
        assertTrue(s.contains("effect="));
    }

    @Test
    void testHSB() {
        ShellyLightModel model = ShellyLightModel.create(THING_TYPE_SHELLYBULB,
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
        ShellyLightModel model = ShellyLightModel.create(THING_TYPE_SHELLYBULB,
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
}
