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
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.*;
import static org.openhab.core.util.LightModel.LedOperatingMode.*;
import static org.openhab.core.util.LightModel.LightCapabilities.*;
import static org.openhab.core.util.LightModel.RgbDataType.*;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.core.util.LightModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ShellyLightModel} extends the OH Core {@link LightModel} with Shelly specific functions.
 * 
 * TODO add more java doc
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ShellyLightModel extends LightModel {

    private final Logger logger = LoggerFactory.getLogger(ShellyLightModel.class);

    /**
     * The RGBX enum is used to indicate which part of an RGBX array to use.
     */
    public enum RGBX {
        R,
        G,
        B,
        CW,
        WW
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
    private final int lightId;
    private final String thingName;
    private final ReentrantLock lock = new ReentrantLock();

    private Mode shellyMode = Mode.WHITE;
    private int effect = 0;

    // initial values used to determine if the model dirty state changes
    private volatile Mode initialShellyMode;
    private volatile int initialEffect;
    private volatile int[] initialRGBx;
    private volatile @Nullable PercentType initialBrightness;
    private volatile @Nullable OnOffType initialOnOff;
    private volatile @Nullable QuantityType<?> initialColorTemperature;
    private volatile @Nullable String initialSnapshot;
    private volatile @Nullable Object lockContext;
    private volatile @Nullable Object dataSource;

    /**
     * Public static class factory that creates a {@link ShellyLightModel} with the correct parameters based on the
     * given {@link ThingTypeUID} and {@link ShellyDeviceProfile}.
     */
    public static ShellyLightModel create(String thingName, int lightId, ThingTypeUID thingTypeUID,
            ShellyDeviceProfile profile, double stepSize) {
        Parameters params = getParams(thingTypeUID, profile.device.profile);
        return new ShellyLightModel(thingName, lightId, params.lightCapabilities, params.rgbDataType, null,
                reciprocal(profile.maxTemp), reciprocal(profile.minTemp), stepSize, null, null, params.ledOperatingMode,
                params.shellyMode);
    }

    /**
     * Get the light capabilities, RGB data type, and LED operating mode for the {@link ShellyLightModel} from the
     * given {@link ThingTypeUID}. It is assumed that the ThingTypeUID carries the necessary clues to create the
     * LightModel with the correct parameters.
     * 
     * TODO for generation 2 and generation 3 the working assumption is that profile is a string like "light", "rgb",
     * "rgbw", "cct", "rgbcct" which determines the light model capabilities
     * 
     * !! THIS CODE MAY CHANGE WHEN THE BINDING IS ACTUALLY UPDATED TO SUPPORT GEN2 AND GEN3 LIGHTS !!
     * 
     * @param thingTypeUID the ThingTypeUID of the Shelly light
     * @param gen23Profile the profile of the Shelly gen 2/3 light
     */
    private static Parameters getParams(ThingTypeUID thingTypeUID, String gen23Profile) {
        // ==== GENERATION 1 ====
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

        // ==== GENERATION 2 ====
        if (THING_TYPE_SHELLYPLUSRGBWPM.equals(thingTypeUID)) {
            switch (gen23Profile) {
                case SHELLY2_PROFILE_RGB:
                    return new Parameters(COLOR, RGB_NO_BRIGHTNESS, RGB_ONLY, Mode.COLOR);
                case SHELLY2_PROFILE_RGBW:
                    return new Parameters(COLOR, RGB_W_NO_BRIGHTNESS, RGB_ONLY, Mode.COLOR);
            }
        }

        // **************** UNCOMMENT FOLLOWING LINES WHEN READY ****************

        // if (THING_TYPE_SHELLYPLUSRGBWWPM.equals(thingTypeUID)) {
        // switch (profile) {
        // case SHELLY2_PROFILE_RGBCCT:
        // return new Parameters(COLOR, RGB_C_W_NO_BRIGHTNESS, COMBINED, Mode.COLOR);
        // case SHELLY2_PROFILE_RGBX2LIGHT:
        // return new Parameters(COLOR, RGB_NO_BRIGHTNESS, RGB_ONLY, Mode.COLOR);
        // }
        // }

        // ==== GENERATION 3 ====
        // if (THING_TYPE_SHELLYDUOBULBG3.equals(thingTypeUID)) { check profile SHELLY2_PROFILE_CCT
        // return new Parameters(BRIGHTNESS_WITH_COLOR_TEMPERATURE, DEFAULT, WHITE_ONLY, Mode.COLOR_TEMP);
        // }

        // if (THING_TYPE_SHELLYCOLORBLBG3.equals(thingTypeUID)) { check profile SHELLY2_PROFILE_RGBCCT
        // return new Parameters(COLOR_WITH_COLOR_TEMPERATURE, RGB_NO_BRIGHTNESS, COMBINED, Mode.COLOR);
        // }

        throw new IllegalArgumentException("Error creating Light Model for: " + thingTypeUID.toString());
    }

    /**
     * Private constructor to create a ShellyLightModel with the given parameters.
     * 
     * @param ledOperatingMode
     * @param shellyMode
     */
    private ShellyLightModel(String thingName, int lightId, LightCapabilities lightCapabilities,
            RgbDataType rgbDataType, @Nullable Double minimumOnBrightness, @Nullable Double mirekControlCoolest,
            @Nullable Double mirekControlWarmest, @Nullable Double stepSize, @Nullable Double coolWhiteLedMirek,
            @Nullable Double warmWhiteLedMirek, LedOperatingMode ledOperatingMode, Mode shellyModeParam)
            throws IllegalArgumentException {
        super(lightCapabilities, rgbDataType, minimumOnBrightness, mirekControlCoolest, mirekControlWarmest, stepSize,
                coolWhiteLedMirek, warmWhiteLedMirek);

        this.thingName = thingName;
        this.lightId = lightId;
        shellyMode = shellyModeParam;
        setLedOperatingMode(ledOperatingMode);
        rgbxLength = WHITE_ONLY == ledOperatingMode ? 3 : getRGBx().length;
        initialRGBx = new int[rgbxLength];
        initialShellyMode = shellyMode;

        logger.debug(
                "{}: Light model for {} lightId={}: Created with capabilities={}, rgbDataType={}, ledOperatingMode={}, shellyMode={}",
                thingName, lightId, lightCapabilities, rgbDataType, ledOperatingMode, initialShellyMode);
    }

    /**
     * OpenHAB light control standard main entry point:
     * Override handleCommand and set the mode and dirty flags accordingly.
     */
    @Override
    public void handleCommand(Command command) {
        super.handleCommand(command);
        setMode(Mode.COLOR);
        if (command instanceof HSBType) {
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
    }

    /**
     * Get the brightness state. This is the brightness when in color temperature mode.
     */
    public State getBrightnessState() {
        return getBrightness(true) instanceof PercentType pct ? pct : UnDefType.UNDEF;
    }

    /**
     * Set the brightness (i.e. the brightness when color temperature mode).
     */
    public void setBrightness(int brightness) {
        setBrightness((double) brightness);
        setMode(Mode.COLOR_TEMP);
    }

    /**
     * Set the brightness (i.e. the brightness when color temperature mode).
     */
    public void setBrightness(Command command) {
        if (!(command instanceof HSBType)) {
            super.handleCommand(command);
            setMode(Mode.COLOR_TEMP);
        }
    }

    /**
     * Check if the brightness has been changed since lock() was called.
     */
    public boolean isBrightnessDirty() {
        return !Objects.equals(initialBrightness, getBrightness(true));
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
     * Set the color component at the given RGBX index.
     */
    public void setColor(RGBX index, int value) {
        cacheRGBX[index.ordinal()] = value;
        double[] rgbx = getRGBx();
        rgbx[index.ordinal()] = value;
        setRGBx(rgbx);
        setMode(Mode.COLOR);
    }

    /**
     * Check if the color has been changed since lock() was called.
     */
    public boolean isColorDirty() {
        return !Arrays.equals(initialRGBx, 0, rgbxLength, cacheRGBX, 0, rgbxLength);
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
     * Set the color temperature.
     */
    public void setColorTemp(double kelvin) {
        setMirek(reciprocal(kelvin));
        setMode(Mode.COLOR_TEMP);
    }

    /**
     * Check if the color temperature has been changed since lock() was called.
     */
    public boolean isColorTempDirty() {
        return !Objects.equals(initialColorTemperature, getColorTemperature());
    }

    /**
     * Get the effect as a DecimalType.
     */
    public DecimalType getEffectState() {
        return new DecimalType(effect);
    }

    /**
     * Set the effect.
     */
    public void setEffect(int value) {
        effect = value;
    }

    /**
     * Check if the effect has been changed since lock() was called.
     */
    public boolean isEffectDirty() {
        return !Objects.equals(initialEffect, effect);
    }

    /**
     * Get the gain state. This is the brightness when in color mode.
     */
    public State getGainState() {
        return getBrightnessState();
    }

    /**
     * Set gain (i.e. the brightness when color mode).
     */
    public void setGain(double gain) {
        setBrightness((double) gain);
        setMode(Mode.COLOR);
    }

    /**
     * Set gain (i.e. the brightness when color mode).
     */
    public void setGain(Command command) {
        if (!(command instanceof HSBType)) {
            super.handleCommand(command);
            setMode(Mode.COLOR);
        }
    }

    /**
     * Check if the gain has been changed since lock() was called.
     */
    public boolean isGainDirty() {
        return isBrightnessDirty();
    }

    /**
     * Get the shelly device mode.
     */
    public Mode getMode() {
        return shellyMode;
    }

    public OnOffType getModeState() {
        return OnOffType.from(shellyMode == Mode.COLOR);
    }

    /**
     * Set the shelly device mode.
     */
    public void setMode(Mode shellyMode) {
        this.shellyMode = shellyMode;
    }

    /**
     * Check if the mode has been changed since lock() was called.
     */
    public boolean isModeDirty() {
        return initialShellyMode != shellyMode;
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
     * Set the on/off state.
     */
    @Override
    public void setOnOff(boolean on) {
        super.setOnOff(on);
    }

    /**
     * Check if the on/off state has been changed since lock() was called.
     */
    public boolean isOnOffDirty() {
        return !Objects.equals(initialOnOff, getOnOff(true));
    }

    /**
     * Get the RGBX values from cache.
     */
    public int[] getRGBX() {
        return Arrays.copyOf(cacheRGBX, rgbxLength);
    }

    /**
     * Set the RGBX values.
     */
    public void setRGBX(int[] rgbx) {
        setRGBx(Arrays.stream(rgbx).mapToDouble(i -> (double) i).toArray());
        refreshCache(rgbx);
        setMode(Mode.COLOR);
    }

    /**
     * Set the RGBW values.
     */
    public void setRGBX(int red, int green, int blue, int white) {
        setRGBX(new int[] { red, green, blue, white });
    }

    /**
     * Set the RGBW values from a comma-separated string.
     */
    public void setRGBX(String rgbx) {
        setRGBX(Arrays.stream(rgbx.split(",")).map(String::trim).mapToInt(Integer::parseInt).toArray());
    }

    /**
     * Set the full color from a Command. The command can be a comma-separated string of RGBW values, or one
     * of the predefined color names.
     */
    public void setRGBX(Command command) throws IllegalArgumentException {
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
     * Check if any of the dirty flags have been set since lock() was called.
     */
    public boolean isDirty() {
        return isOnOffDirty() || isBrightnessDirty() || isColorDirty() || isColorTempDirty() || isEffectDirty()
                || isModeDirty();
    }

    /**
     * Acquire the lock. And save the current model state to allow for dirty flag checking.
     * 
     * @param lockContext an object that identifies the context that is acquiring the lock
     * @param dataSource an object that identifies the source of the data that is acquiring the lock
     */
    public void lock(Object lockContext, Object dataSource) {
        lock.lock();
        this.lockContext = lockContext;
        this.dataSource = dataSource;
        initialSnapshot = logger.isDebugEnabled() ? toString() : null;
        initialRGBx = getRGBX();
        initialOnOff = getOnOff(true);
        initialEffect = effect;
        initialShellyMode = shellyMode;
        initialBrightness = getBrightness(true);
        initialColorTemperature = getColorTemperature();
        logger.debug("{}: Light model for lightId {}: Lock acquired by {}", thingName, lightId, lockContext);
    }

    /**
     * Release the lock.
     */
    public void unlock() {
        if (isDirty()) {
            logger.debug("{}: Light model for lightId {}: Updated by {}\n - Old: [{}]\n - New: [{}]", thingName,
                    lightId, dataSource, initialSnapshot, this);
        }
        lock.unlock();
    }
}
