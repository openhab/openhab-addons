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

import static org.openhab.binding.shelly.internal.ShellyDevices.*;
import static org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.*;
import static org.openhab.core.util.LightModel.LedOperatingMode.*;
import static org.openhab.core.util.LightModel.LightCapabilities.*;
import static org.openhab.core.util.LightModel.RgbDataType.*;

import java.util.Arrays;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.core.util.LightModel;

/**
 * The {@link ShellyLightModel} extends the OH Core {@link LightModel} with Shelly specific functions.
 * 
 * TODO add more java doc
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ShellyLightModel extends LightModel {

    /**
     * The RGBX enum is used to indicate which part of an RGBX array to use.
     */
    public enum RGBX {
        R,
        G,
        B,
        WC, // white (cold)
        WW // white (warm)
    }

    /**
     * The Mode enum is used to indicate which mode the Shelly light is using.
     */
    public enum Mode {
        WHITE,
        COLOR,
        COLOR_TEMP
    }

    /**
     * A record that carries the light capabilities, RGB data type, and LED operating mode.
     */
    private record Parameters(LightCapabilities lightCapabilities, RgbDataType rgbDataType,
            LedOperatingMode ledOperatingMode, Mode shellyMode) {
    }

    /*
     * The {@link LightModel} class does not round trip RGBX values cleanly (e.g. because [255,155,155,0] is a
     * functional synonym for [100,0,0,155]) so we cache the input values here to avoid data loss or confusion.
     */
    private final int[] cacheRGBX = new int[RGBX.values().length];
    private final int rgbxLength;

    private Mode shellyMode = Mode.WHITE;
    private int effect = 0;

    private boolean modeDirty;
    private boolean colorDirty;
    private boolean brightnessDirty;
    private boolean gainDirty;
    private boolean effectDirty;
    private boolean colorTempDirty;
    private boolean onOffDirty;

    /**
     * Public static class factory that creates a {@link ShellyLightModel} with the correct parameters based on the
     * given {@link ThingTypeUID} and {@link ShellyDeviceProfile}.
     */
    public static ShellyLightModel create(ThingTypeUID thingTypeUID, ShellyDeviceProfile profile, double stepSize) {
        Parameters params = getParams(thingTypeUID);
        ShellyLightModel model = new ShellyLightModel(params.lightCapabilities, params.rgbDataType, 0.4,
                reciprocal(profile.maxTemp), reciprocal(profile.minTemp), stepSize, null, null,
                params.ledOperatingMode);
        model.setLedOperatingMode(params.ledOperatingMode);
        model.shellyMode = params.shellyMode;
        return model;
    }

    /**
     * Get the light capabilities, RGB data type, and LED operating mode for the {@link ShellyLightModel} from the
     * given {@link ThingTypeUID}. It is assumed that the ThingTypeUID carries the necessary clues to create the
     * LightModel with the correct parameters.
     */
    private static Parameters getParams(ThingTypeUID thingTypeUID) {
        // GENERATION 1:
        if (THING_TYPE_SHELLYBULB.equals(thingTypeUID)) {
            return new Parameters(COLOR_WITH_COLOR_TEMPERATURE, RGB_W_NO_BRIGHTNESS, COMBINED, Mode.COLOR);
        }
        if (THING_TYPE_SHELLYDUO.equals(thingTypeUID)) {
            return new Parameters(BRIGHTNESS_WITH_COLOR_TEMPERATURE, DEFAULT, WHITE_ONLY, Mode.COLOR_TEMP);
        }
        if (THING_TYPE_SHELLYVINTAGE.equals(thingTypeUID)) {
            return new Parameters(BRIGHTNESS, DEFAULT, WHITE_ONLY, Mode.WHITE);
        }
        if (THING_TYPE_SHELLYDUORGBW.equals(thingTypeUID)) {
            return new Parameters(COLOR_WITH_COLOR_TEMPERATURE, RGB_W_NO_BRIGHTNESS, COMBINED, Mode.COLOR);
        }
        if (THING_TYPE_SHELLYRGBW2_COLOR.equals(thingTypeUID)) {
            return new Parameters(COLOR, RGB_W_NO_BRIGHTNESS, COMBINED, Mode.COLOR);
        }
        if (THING_TYPE_SHELLYRGBW2_WHITE.equals(thingTypeUID)) {
            return new Parameters(BRIGHTNESS, DEFAULT, WHITE_ONLY, Mode.COLOR_TEMP);
        }

        // GENERATION 2 and 3:
        if (THING_TYPE_SHELLYPLUSRGBWPM.equals(thingTypeUID)) {
            // TODO || (THING_TYPE_SHELLYPLUSRGBWWPM.equals(thingTypeUID)) { add missing GEN2 plus RGBWWPM
            // TODO || (THING_TYPE_SHELLYDUOBULBG3.equals(thingTypeUID)) { add missing GEN3 duo bulb
            // TODO || (THING_TYPE_SHELLYCOLORBLBG3.equals(thingTypeUID)) { add missing GEN3 color bulb

            /*
             * TODO working assumption is that the thingTypeUID.getId() is a string like "shellyplusrgbwpm-light"
             * or "shellyplusrgbwpm-rgb" or "shellyplusrgbwpm-rgbw" or "shellyplusrgbwpm-cct" or
             * "shellycolorblbg3-rgbcct" or "shellyduobulbg3-cct" which can determine the light capabilities
             * 
             * THIS CODE MAY CHANGE WHEN THE BINDING IS ACTUALLY UPDATED TO SUPPORT GEN2 AND GEN3 LIGHTS !!
             */
            String thingTypeId = thingTypeUID.getId();
            int pos = thingTypeId.lastIndexOf('-');
            if (pos != -1) {
                String thingIdConfigurationModeSuffix = thingTypeId.substring(pos + 1);
                switch (thingIdConfigurationModeSuffix) {
                    case "light":
                        return new Parameters(BRIGHTNESS, DEFAULT, WHITE_ONLY, Mode.WHITE);
                    case "rgb":
                        return new Parameters(COLOR, RGB_NO_BRIGHTNESS, RGB_ONLY, Mode.COLOR);
                    case "rgbw":
                        return new Parameters(COLOR, RGB_W_NO_BRIGHTNESS, COMBINED, Mode.COLOR);
                    case "cct":
                        return new Parameters(BRIGHTNESS_WITH_COLOR_TEMPERATURE, DEFAULT, WHITE_ONLY, Mode.COLOR_TEMP);
                    case "rgbcct":
                        return new Parameters(COLOR_WITH_COLOR_TEMPERATURE, RGB_W_NO_BRIGHTNESS, COMBINED, Mode.COLOR);
                }
            }
        }
        throw new IllegalArgumentException("Error creating Light Model for: " + thingTypeUID.toString());
    }

    /**
     * Private constructor to create a ShellyLightModel with the given parameters.
     * 
     * @param baseOperatingMode
     */
    private ShellyLightModel(LightCapabilities lightCapabilities, RgbDataType rgbDataType,
            @Nullable Double minimumOnBrightness, @Nullable Double mirekControlCoolest,
            @Nullable Double mirekControlWarmest, @Nullable Double stepSize, @Nullable Double coolWhiteLedMirek,
            @Nullable Double warmWhiteLedMirek, LedOperatingMode baseOperatingMode) throws IllegalArgumentException {
        super(lightCapabilities, rgbDataType, minimumOnBrightness, mirekControlCoolest, mirekControlWarmest, stepSize,
                coolWhiteLedMirek, warmWhiteLedMirek);
        rgbxLength = getRGBx().length;
    }

    /**
     * OpenHAB light control standard main entry point:
     * Override handleCommand and set the mode and dirty flags accordingly.
     */
    @Override
    public void handleCommand(Command command) {
        super.handleCommand(command);
        setMode(Mode.COLOR);
        colorDirty = command instanceof HSBType;
        gainDirty = colorDirty || command instanceof PercentType || command instanceof IncreaseDecreaseType;
        onOffDirty = gainDirty || command instanceof OnOffType;
        if (colorDirty) {
            refreshCache(Arrays.stream(getRGBx()).mapToInt(d -> (int) Math.round(d)).toArray());
        }
    }

    /**
     * OpenHAB light control standard main entry point:
     * Override handleColorTemperatureCommand and set the mode and dirty flags accordingly.
     */
    @Override
    public void handleColorTemperatureCommand(Command command) {
        super.handleColorTemperatureCommand(command);
        setMode(Mode.COLOR_TEMP);
        colorTempDirty = true;
    }

    /**
     * Get the brightness state. This is the brightness when in color temperature mode.
     */
    public State getBrightnessState() {
        return getBrightness(true) instanceof PercentType pct ? pct : UnDefType.UNDEF;
    }

    /**
     * Set the brightness (i.e. the brightness when color temperature mode). And set the dirty flag.
     */
    public void setBrightness(int brightness) {
        setBrightness((double) brightness);
        setMode(Mode.COLOR_TEMP);
        brightnessDirty = true;
    }

    /**
     * Check if the brightness has been changed since the dirty flags were last cleared.
     */
    public boolean isBrightnessDirty() {
        return brightnessDirty;
    }

    /**
     * Get the color component at the given RGBW index as an int.
     */
    public int getColor(RGBX index) {
        return cacheRGBX[index.ordinal()];
    }

    /**
     * Get the color component at the given RGBW index as a PercentType.
     */
    public PercentType getColorState(RGBX index) {
        return new PercentType((int) Math.round(getColor(index) * 100.0 / 255.0));
    }

    /**
     * Get the color as an HSBType.
     */
    public State getColorState() {
        return getColor() instanceof HSBType hsb ? hsb : UnDefType.UNDEF;
    }

    /**
     * Set the color component at the given RGBX index. And set the dirty flag.
     */
    public void setColor(RGBX index, int value) {
        cacheRGBX[index.ordinal()] = value;
        double[] rgbx = getRGBx();
        rgbx[index.ordinal()] = value;
        setRGBx(rgbx);
        setMode(Mode.COLOR);
        colorDirty = true;
    }

    /**
     * Check if the color has been changed since the dirty flags were last cleared.
     */
    public boolean isColorDirty() {
        return colorDirty;
    }

    /**
     * Convert Kelvin to Mirek or vice-versa.
     */
    private static double reciprocal(double value) {
        return Double.NaN == value ? 0 : 1000000.0 / value;
    }

    /**
     * Refresh the cache of RGBX values from the LightModel.
     */
    private void refreshCache(int[] rgbx) {
        for (int i = 0; i < rgbx.length; i++) {
            cacheRGBX[i] = rgbx[i];
        }
    }

    /**
     * Get the color temperature as a QuantityType.
     */
    public State getColorTemperatureAbsoluteState() {
        return getColorTemperature() instanceof QuantityType<?> qty ? qty : UnDefType.UNDEF;
    }

    /**
     * Get the color temperature as a PercentType.
     */
    public State getColorTemperaturePercentState() {
        return getColorTemperaturePercent() instanceof PercentType pct
                ? new PercentType((int) Math.round(pct.doubleValue()))
                : UnDefType.UNDEF;
    }

    /**
     * Set the color temperature. And set the dirty flag.
     */
    public void setColorTemp(double kelvin) {
        setMirek(reciprocal(kelvin));
        setMode(Mode.COLOR_TEMP);
        colorTempDirty = true;
    }

    /**
     * Check if the color temperature has been changed since the dirty flags were last cleared.
     */
    public boolean isColorTempDirty() {
        return colorTempDirty;
    }

    /**
     * Get the effect as a DecimalType.
     */
    public DecimalType getEffectState() {
        return new DecimalType(effect);
    }

    /**
     * Set the effect. And set the dirty flag.
     */
    public void setEffect(int value) {
        effect = value;
        effectDirty = true;
    }

    /**
     * Check if the effect has been changed since the dirty flags were last cleared.
     */
    public boolean isEffectDirty() {
        return effectDirty;
    }

    /**
     * Get the gain state. This is the brightness when in color mode.
     */
    public State getGainState() {
        return getBrightnessState();
    }

    /**
     * Set the gain (color mode brightness). And set the dirty flag.
     */
    public void setGain(double gain) {
        setBrightness((double) gain);
        setMode(Mode.COLOR);
        gainDirty = true;
    }

    /**
     * Check if the gain has been changed since the dirty flags were last cleared.
     */
    public boolean isGainDirty() {
        return gainDirty;
    }

    /**
     * Get the shelly device mode.
     */
    public Mode getMode() {
        return shellyMode;
    }

    /**
     * Set the shelly device mode. And set the dirty flag.
     */
    public void setMode(Mode shellyMode) {
        this.shellyMode = shellyMode;
        modeDirty = true;
    }

    /**
     * Check if the mode has been changed since the dirty flags were last cleared.
     */
    public boolean isModeDirty() {
        return modeDirty;
    }

    /**
     * Check if the RGB values are valid for the current led operating mode.
     */
    public boolean isRgbValid() { // TODO check logic for all light types
        return Mode.COLOR == shellyMode;
    }

    public State getOnOffState() {
        return getOnOff(true) instanceof OnOffType onOff ? onOff : UnDefType.UNDEF;
    }

    /**
     * Set the on/off state. And set the dirty flag.
     */
    @Override
    public void setOnOff(boolean on) {
        super.setOnOff(on);
        onOffDirty = true;
    }

    /**
     * Check if the on/off state has been changed since the dirty flags were last cleared.
     */
    public boolean isOnOffDirty() {
        return onOffDirty;
    }

    /**
     * Get the RGBX values from cache.
     */
    public int[] getRGBX() {
        return Arrays.copyOf(cacheRGBX, rgbxLength);
    }

    /**
     * Set the RGBX values. And set the dirty flag.
     */
    public void setRGBX(int[] rgbx) {
        setRGBx(Arrays.stream(rgbx).mapToDouble(i -> (double) i).toArray());
        refreshCache(rgbx);
        setMode(Mode.COLOR);
        colorDirty = true;
    }

    /**
     * Set the RGBW values. And set the dirty flag.
     */
    public void setRGBX(int red, int green, int blue, int white) {
        setRGBX(new int[] { red, green, blue, white });
    }

    /**
     * Set the RGBW values from a comma-separated string. And set the dirty flag.
     */
    public void setRGBX(String rgbx) {
        setRGBX(Arrays.stream(rgbx.split(",")).map(String::trim).mapToInt(Integer::parseInt).toArray());
    }

    /**
     * Set the full color from a Command. The command can be a comma-separated string of RGBW values, or one of the
     * predefined color names. And set the dirty flag.
     */
    public void setFullColorCommand(Command command) throws IllegalArgumentException {
        String color = command.toString().toLowerCase(Locale.ROOT);
        if (color.contains(",")) {
            setRGBX(color);
        } else if (color.equals(SHELLY_COLOR_RED)) {
            setRGBX(SHELLY_MAX_COLOR, 0, 0, 0);
        } else if (color.equals(SHELLY_COLOR_GREEN)) {
            setRGBX(0, SHELLY_MAX_COLOR, 0, 0);
        } else if (color.equals(SHELLY_COLOR_BLUE)) {
            setRGBX(0, 0, SHELLY_MAX_COLOR, 0);
        } else if (color.equals(SHELLY_COLOR_YELLOW)) {
            setRGBX(SHELLY_MAX_COLOR, SHELLY_MAX_COLOR, 0, 0);
        } else if (color.equals(SHELLY_COLOR_WHITE)) {
            setRGBX(0, 0, 0, SHELLY_MAX_COLOR);
        } else {
            throw new IllegalArgumentException("Invalid full color selection: " + color);
        }
    }

    @Override
    public String toString() {
        return "mode=%s, power=%s, bri=%s, rgbw=%s, color-temp=%s%%, color-temp-abs=%s, min=%.0f K, max=%.0f K, effect=%s"
                .formatted(getMode(), getOnOffState(), getBrightnessState(), Arrays.toString(getRGBX()),
                        getColorTemperaturePercentState(), getColorTemperatureAbsoluteState(),
                        reciprocal(configGetMirekControlWarmest()), reciprocal(configGetMirekControlCoolest()),
                        getEffectState());
    }

    /**
     * Check if any of the dirty flags are set.
     */
    public boolean isDirty() {
        return modeDirty || colorDirty || brightnessDirty || gainDirty || effectDirty || colorTempDirty || onOffDirty;
    }

    /**
     * Clear all dirty flags.
     */
    public void clearDirtyFlags() {
        modeDirty = false;
        colorDirty = false;
        brightnessDirty = false;
        gainDirty = false;
        effectDirty = false;
        colorTempDirty = false;
        onOffDirty = false;
    }
}
