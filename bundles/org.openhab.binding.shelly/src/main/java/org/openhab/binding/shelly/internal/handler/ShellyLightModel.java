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

import static org.openhab.core.util.LightModel.LedOperatingMode.*;
import static org.openhab.core.util.LightModel.LightCapabilities.*;
import static org.openhab.core.util.LightModel.RgbDataType.DEFAULT;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.Command;
import org.openhab.core.util.LightModel;

/**
 * The {@link ShellyLightModel} extends the OH Core {@link LightModel} with Shelly specific functions.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ShellyLightModel extends LightModel {

    /**
     * The RGBW enum is used to indicate which part of an RGBW array to use.
     */
    public enum RGBW {
        R,
        G,
        B,
        W
    }

    private int gain = 0;
    private int effect = 0;

    private boolean modeDirty;
    private boolean colorDirty;
    private boolean brightnessDirty;
    private boolean gainDirty;
    private boolean effectDirty;
    private boolean colorTempDirty;
    private boolean onOffDirty;

    public ShellyLightModel(ShellyDeviceProfile profile, double stepSize) {
        super(profile.isDuo ? BRIGHTNESS_WITH_COLOR_TEMPERATURE : COLOR_WITH_COLOR_TEMPERATURE, DEFAULT, 0.4,
                reciprocal(profile.maxTemp), reciprocal(profile.minTemp), stepSize, null, null);
        setLedOperatingMode(profile.isDuo ? WHITE_ONLY : RGB_ONLY);
    }

    /**
     * Set the brightness. Do not set the dirty flag.
     */
    public void setBrightness(int brightness) {
        setBrightness((double) brightness);
    }

    /**
     * Set the brightness. And set the dirty flag.
     */
    public void handleBrightness(int brightness) {
        setBrightness((double) brightness);
        brightnessDirty = true;
    }

    /**
     * Check if the brightness has been changed since the dirty flags were last cleared.
     */
    public boolean isBrightnessDirty() {
        return brightnessDirty;
    }

    /**
     * Get the color component at the given RGBW index as a PercentType.
     */
    public PercentType getColor(RGBW index) {
        double[] rgbw = getRGBx();
        return new PercentType((int) Math.round(rgbw[index.ordinal()] * 100.0 / 255.0));
    }

    /**
     * Set the color component at the given RGBW index. Do not set the dirty flag.
     */
    public void setColor(RGBW index, int value) {
        double[] rgbw = getRGBx();
        rgbw[index.ordinal()] = value;
        setRGBx(rgbw);
    }

    /**
     * Set the color component at the given RGBW index. And set the dirty flag.
     */
    public void handleColor(RGBW index, int value) {
        double[] rgbw = getRGBx();
        rgbw[index.ordinal()] = value;
        alignMode(rgbw);
        setRGBx(rgbw);
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
        return 1000000.0 / value;
    }

    /**
     * Set the color temperature. Do not set the dirty flag.
     */
    public void setColorTemp(double kelvin) {
        setMirek(reciprocal(kelvin));
    }

    /**
     * Set the color temperature. And set the dirty flag.
     */
    public void handleColorTemp(int kelvin) {
        setMirek(reciprocal(kelvin));
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
    public DecimalType getEffect() {
        return new DecimalType(effect);
    }

    /**
     * Set the effect. And set the dirty flag.
     */
    public void setEffect(int value) {
        effect = value;
    }

    /**
     * Set the effect. Mark it as dirty.
     */
    public void handleEffect(int value) {
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
     * Get the gain as a DecimalType.
     */
    public DecimalType getGain() {
        return new DecimalType(gain);
    }

    /**
     * Set the gain. And set the dirty flag.
     */
    public void setGain(int value) {
        gain = value;
    }

    /**
     * Set the gain. Mark it as dirty.
     */
    public void handleGain(int value) {
        gain = value;
        gainDirty = true;
    }

    /**
     * Check if the gain has been changed since the dirty flags were last cleared.
     */
    public boolean isGainDirty() {
        return gainDirty;
    }

    /**
     * Get the led operating mode.
     */
    public LedOperatingMode getMode() {
        return getLedOperatingMode();
    }

    /**
     * Set the led operating mode. Do not set the dirty flag.
     */
    public void setMode(LedOperatingMode mode) {
        setLedOperatingMode(mode);
    }

    /**
     * Set the led operating mode. And set the dirty flag.
     */
    public void handleMode(LedOperatingMode mode) {
        if (mode != getLedOperatingMode()) {
            setLedOperatingMode(mode);
            modeDirty = true;
        }
    }

    /**
     * Adjust the led operating mode based on the RGB values.
     * If any of the RGB values are non-zero, set the mode to RGB_ONLY, otherwise set it to WHITE_ONLY.
     */
    private void alignMode(double[] rgbw) {
        if (COLOR_WITH_COLOR_TEMPERATURE == configGetLightCapabilities()) {
            LedOperatingMode mode = rgbw[0] > 0 || rgbw[1] > 0 || rgbw[2] > 0 ? RGB_ONLY : WHITE_ONLY;
            if (mode != getLedOperatingMode()) {
                setLedOperatingMode(mode);
                modeDirty = true;
            }
        }
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
    public boolean isRgbValid() {
        return WHITE_ONLY != getLedOperatingMode();
    }

    /**
     * Set the on/off state. And set the dirty flag.
     */
    public void handleOnOff(boolean on) {
        setOnOff(on);
        onOffDirty = true;
    }

    /**
     * Check if the on/off state has been changed since the dirty flags were last cleared.
     */
    public boolean isOnOffDirty() {
        return onOffDirty;
    }

    /**
     * Set the RGBW values. Do not set the dirty flag.
     */
    public void setRGBW(int red, int green, int blue, int white) {
        double[] rgbw = new double[] { red, green, blue, white };
        setRGBx(rgbw);
    }

    /**
     * Set the RGBW values. And set the dirty flag.
     */
    public void handleRGBW(int red, int green, int blue, int white) {
        double[] rgbw = new double[] { red, green, blue, white };
        alignMode(rgbw);
        setRGBx(rgbw);
        colorDirty = true;
    }

    /**
     * Set the RGBW values from a comma-separated string. And set the dirty flag.
     */
    public void handleRGBW(String rgbwStr) {
        double[] rgbw = Arrays.stream(rgbwStr.split(",")).map(String::trim).mapToDouble(Double::parseDouble).toArray();
        alignMode(rgbw);
        setRGBx(rgbw);
        colorDirty = true;
    }

    @Override
    public String toString() {
        double[] rgbw = getRGBx();
        return "mode=%s, power=%s, rgbw=(%f,%f,%f,%f), bri=%s, color-temp=%.1f K, min=%.1f K, max=%.1f K, gain=%.1f, effect=%d"
                .formatted(getMode(), getOnOff(true), rgbw[0], rgbw[1], rgbw[2], rgbw[3], getBrightness(),
                        reciprocal(getMirek()), reciprocal(configGetMirekControlWarmest()),
                        reciprocal(configGetMirekControlCoolest()), gain, effect);
    }

    /**
     * Override handleCommand and set the dirty flags accordingly.
     */
    @Override
    public void handleCommand(Command command) {
        super.handleCommand(command);
        colorDirty = command instanceof HSBType;
        brightnessDirty = colorDirty || command instanceof PercentType || command instanceof IncreaseDecreaseType;
        onOffDirty = brightnessDirty || command instanceof OnOffType;
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
