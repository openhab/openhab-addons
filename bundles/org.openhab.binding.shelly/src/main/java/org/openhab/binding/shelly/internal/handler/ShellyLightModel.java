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
import org.openhab.core.util.LightModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ShellyLightModel} is used to represent the state of a single light aggregate in a Shelly
 * device. It extends the OpenHAB Core {@link LightModel} with Shelly specific functions and wrappers.
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
        COLOR
    }

    /**
     * A record that carries the light capabilities, RGB data type, and LED operating mode.
     */
    private record Parameters(LightCapabilities lightCapabilities, RgbDataType rgbDataType,
            LedOperatingMode ledOperatingMode, Mode shellyMode, boolean modeChangesAllowed) {
    }

    /*
     * The {@link LightModel} class does not round trip RGBX values cleanly (e.g. because [255,155,155,0] is a
     * functional synonym for [100,0,0,155]) so we cache the input values here to avoid data loss or confusion.
     */
    private final int[] cacheRGBX = new int[RGBX.values().length];
    private final int rgbxLength;
    private final int lightId;
    private final ShellyLightHandler lightHandler;
    private final ReentrantLock lock = new ReentrantLock();
    private final boolean modeChangesAllowed;

    // essential fields copied from profile
    private final boolean isBulb;
    private final boolean isDuo;
    private final boolean isRGBW2;
    private final boolean isGen2;
    private final boolean isG3DuoBulb;
    private final boolean isG3ColorBulb;
    private final boolean isProfileLIGHT;
    private final boolean isProfileRGB;
    private final boolean isProfileRGBW;
    private final boolean isProfileRGBCCT;
    private final boolean isProfileRGBX2LIGHT;
    private final boolean isProfileCCTX2;

    private Mode shellyMode = Mode.WHITE;
    private int effect = 0;

    // initial values used to determine if the model dirty state changes
    private volatile Mode initialShellyMode;
    private volatile int initialEffect;
    private volatile int[] initialRGBX;
    private volatile @Nullable PercentType initialBrightness;
    private volatile @Nullable OnOffType initialOnOff;
    private volatile @Nullable QuantityType<?> initialColorTemperature;
    private volatile @Nullable String initialSnapshot;

    /**
     * Public static class factory that creates a {@link ShellyLightModel} with the correct parameters based on the
     * given {@link ThingTypeUID}, light Id and {@link ShellyDeviceProfile}.
     */
    public static ShellyLightModel create(ShellyLightHandler lightHandler, int lightId, ThingTypeUID thingTypeUID,
            ShellyDeviceProfile profile, double stepSize) {
        Parameters params = getParams(thingTypeUID, lightId, profile.device.profile);
        return new ShellyLightModel(lightHandler, lightId, params.lightCapabilities, params.rgbDataType,
                reciprocal(profile.maxTemp), reciprocal(profile.minTemp), stepSize, params.ledOperatingMode,
                params.shellyMode, params.modeChangesAllowed, profile);
    }

    /**
     * Get the light capabilities, RGB data type, and LED operating mode for the {@link ShellyLightModel} from the
     * given {@link ThingTypeUID}. It is assumed that the ThingTypeUID (and for Gen 2/3 devices the device profile)
     * carry all necessary clues to create the LightModel with the correct parameters.
     * 
     * @param thingTypeUID the ThingTypeUID of the Shelly light
     * @param gen23DeviceProfile the Shelly Gen 2/3 device profile
     */
    private static Parameters getParams(ThingTypeUID thingTypeUID, int lightIndex, String gen23DeviceProfile) {
        // ==== GENERATION 1 ====
        if (THING_TYPE_SHELLYBULB.equals(thingTypeUID)) {
            return new Parameters(COLOR_WITH_COLOR_TEMPERATURE, RGB_W_NO_BRIGHTNESS, COMBINED, Mode.COLOR, true);
        }
        if (THING_TYPE_SHELLYDUO.equals(thingTypeUID)) {
            return new Parameters(BRIGHTNESS_WITH_COLOR_TEMPERATURE, DEFAULT, WHITE_ONLY, Mode.WHITE, false);
        }
        if (THING_TYPE_SHELLYVINTAGE.equals(thingTypeUID)) {
            return new Parameters(BRIGHTNESS, DEFAULT, WHITE_ONLY, Mode.WHITE, false);
        }
        if (THING_TYPE_SHELLYDUORGBW.equals(thingTypeUID)) {
            return new Parameters(COLOR_WITH_COLOR_TEMPERATURE, RGB_W_NO_BRIGHTNESS, COMBINED, Mode.COLOR, true);
        }

        // ==== GENERATION 2 ====
        if (THING_TYPE_SHELLYRGBW2_COLOR.equals(thingTypeUID)) {
            return new Parameters(COLOR, RGB_W_NO_BRIGHTNESS, COMBINED, Mode.COLOR, false);
        }
        if (THING_TYPE_SHELLYRGBW2_WHITE.equals(thingTypeUID)) {
            return new Parameters(BRIGHTNESS, DEFAULT, WHITE_ONLY, Mode.WHITE, false);
        }

        // ==== GENERATION 3 ====
        if (THING_TYPE_SHELLYPLUSRGBWPM.equals(thingTypeUID)) {
            switch (gen23DeviceProfile) {
                case SHELLY2_PROFILE_RGB:
                    return new Parameters(COLOR, RGB_NO_BRIGHTNESS, RGB_ONLY, Mode.COLOR, false);
                case SHELLY2_PROFILE_RGBW:
                    return new Parameters(COLOR, RGB_W_NO_BRIGHTNESS, RGB_ONLY, Mode.COLOR, false);
                case SHELLY2_PROFILE_LIGHT:
                    return new Parameters(BRIGHTNESS, DEFAULT, WHITE_ONLY, Mode.WHITE, false);
            }
        }
        if (THING_TYPE_SHELLYPRORGBWWPM.equals(thingTypeUID)) {
            switch (gen23DeviceProfile) {
                case SHELLY2_PROFILE_RGB:
                    return new Parameters(COLOR, RGB_NO_BRIGHTNESS, RGB_ONLY, Mode.COLOR, false);
                case SHELLY2_PROFILE_RGBW:
                    return new Parameters(COLOR, RGB_W_NO_BRIGHTNESS, RGB_ONLY, Mode.COLOR, false);
                case SHELLY2_PROFILE_LIGHT:
                    return new Parameters(BRIGHTNESS, DEFAULT, WHITE_ONLY, Mode.WHITE, false);
                case SHELLY2_PROFILE_RGBCCT:
                    // TODO check these parameters
                    return new Parameters(COLOR_WITH_COLOR_TEMPERATURE, RGB_NO_BRIGHTNESS, COMBINED, Mode.COLOR, true);
                case SHELLY2_PROFILE_RGBX2LIGHT:
                    if (lightIndex > 0) {
                        return new Parameters(BRIGHTNESS, DEFAULT, WHITE_ONLY, Mode.WHITE, false);
                    }
                    return new Parameters(COLOR, RGB_NO_BRIGHTNESS, RGB_ONLY, Mode.COLOR, false);
                case SHELLY2_PROFILE_CCTX2:
                    return new Parameters(BRIGHTNESS_WITH_COLOR_TEMPERATURE, DEFAULT, WHITE_ONLY, Mode.WHITE, false);
            }
        }
        if (THING_TYPE_SHELLYPLUSDUOBULB.equals(thingTypeUID)) {
            return new Parameters(BRIGHTNESS_WITH_COLOR_TEMPERATURE, DEFAULT, WHITE_ONLY, Mode.WHITE, false);
        }
        if (THING_TYPE_SHELLYPLUSCOLORBULB.equals(thingTypeUID)) {
            // TODO check these parameters
            return new Parameters(COLOR_WITH_COLOR_TEMPERATURE, RGB_NO_BRIGHTNESS, COMBINED, Mode.COLOR, true);
        }

        throw new IllegalArgumentException("Error creating Light Model for: " + thingTypeUID.toString());
    }

    /**
     * Private constructor to create a ShellyLightModel with the given parameters.
     * 
     * @param thingTypeUID
     * 
     * @param profile
     */
    private ShellyLightModel(ShellyLightHandler lightHandler, int lightId, LightCapabilities lightCapabilities,
            RgbDataType rgbDataType, Double mirekControlCoolest, Double mirekControlWarmest, Double stepSize,
            LedOperatingMode ledOperatingMode, Mode shellyMode, boolean modeChangesAllowed, ShellyDeviceProfile profile)
            throws IllegalArgumentException {

        super(lightCapabilities, rgbDataType, null, mirekControlCoolest, mirekControlWarmest, stepSize, null, null);
        super.setLedOperatingMode(ledOperatingMode);

        this.lightHandler = lightHandler;
        this.lightId = lightId;
        this.shellyMode = shellyMode;
        this.modeChangesAllowed = modeChangesAllowed;

        // initialize light capability flags from the device profile (Generation 1)
        isBulb = profile.isBulb;
        isDuo = profile.isDuo;
        isRGBW2 = profile.isRGBW2;
        isGen2 = profile.isGen2;
        isG3DuoBulb = profile.isRGBBulb; // TODO I think mapping in #20909 is wrong
        isG3ColorBulb = profile.isRGBCCT; // TODO I think mapping in #20909 is wrong

        // initialize light capability flags from the device profile (Generation 2/3)
        isProfileLIGHT = SHELLY2_PROFILE_LIGHT.equalsIgnoreCase(profile.device.profile);
        isProfileRGB = SHELLY2_PROFILE_RGB.equalsIgnoreCase(profile.device.profile);
        isProfileRGBW = SHELLY2_PROFILE_RGBW.equalsIgnoreCase(profile.device.profile);
        isProfileRGBCCT = SHELLY2_PROFILE_RGBCCT.equalsIgnoreCase(profile.device.profile);
        isProfileRGBX2LIGHT = SHELLY2_PROFILE_RGBX2LIGHT.equalsIgnoreCase(profile.device.profile);
        isProfileCCTX2 = SHELLY2_PROFILE_CCTX2.equalsIgnoreCase(profile.device.profile);

        rgbxLength = WHITE_ONLY == ledOperatingMode ? 3 : super.getRGBx().length;
        initialRGBX = new int[rgbxLength];
        initialShellyMode = shellyMode;

        logger.debug(
                "{}: created model for lightId:{} with capabilities:{}, rgbDataType:{}, ledOperatingMode:{}, shellyMode:{}, modeChangeAllowed:{}, ct-range: [{} K..{} K]",
                lightHandler.thingName, lightId, lightCapabilities, rgbDataType, ledOperatingMode, initialShellyMode,
                modeChangesAllowed, Math.round(reciprocal(mirekControlWarmest)),
                Math.round(reciprocal(mirekControlCoolest)));
    }

    /**
     * OpenHAB light control standard main entry point:
     * Override handleCommand and set the mode and dirty flags accordingly.
     */
    @Override
    public void handleCommand(Command command) {
        logger.trace("{}: light {} handleCommand({})", lightHandler.thingName, lightId, command);
        super.handleCommand(command);
        if (command instanceof HSBType) {
            setMode(Mode.COLOR);
            refreshCache(Arrays.stream(getRGBx()).mapToInt(d -> (int) Math.round(d)).toArray());
        }
    }

    /**
     * OpenHAB light control standard main entry point:
     * Override handleColorTemperatureCommand and set the mode and dirty flags accordingly.
     */
    @Override
    public void handleColorTemperatureCommand(Command command) {
        logger.trace("{}: light {} handleColorTemperatureCommand({})", lightHandler.thingName, lightId, command);
        super.handleColorTemperatureCommand(command);
        setMode(Mode.WHITE);
    }

    /**
     * Get the brightness state. This is the brightness when in color temperature mode.
     */
    public State getBrightnessState() {
        return toNonNull(super.getBrightness(true));
    }

    /**
     * Set the brightness (i.e. the brightness when color temperature mode).
     */
    public void setBrightness(int brightness) {
        logger.trace("{}: light {} setBrightness({})", lightHandler.thingName, lightId, brightness);
        super.setBrightness(brightness);
        setMode(Mode.WHITE);
    }

    /**
     * Set the brightness (i.e. the brightness when color temperature mode).
     */
    public void setBrightness(Command command) {
        if (!(command instanceof HSBType)) {
            logger.trace("{}: light {} setBrightness({})", lightHandler.thingName, lightId, command);
            super.handleCommand(command);
            setMode(Mode.WHITE);
        }
    }

    /**
     * Check if the brightness has been changed since lock() was called.
     */
    public boolean isBrightnessDirty() {
        return !Objects.equals(initialBrightness, super.getBrightness(true));
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
        return toNonNull(super.getColor());
    }

    /**
     * Set the color component at the given RGBX index.
     */
    public void setColor(RGBX index, int value) {
        logger.trace("{}: light {} setColor({},{})", lightHandler.thingName, lightId, index, value);
        cacheRGBX[index.ordinal()] = value;
        double[] rgbx = getRGBx();
        rgbx[index.ordinal()] = value;
        super.setRGBx(rgbx);
        setMode(Mode.COLOR);
    }

    /**
     * Check if the color has been changed since lock() was called.
     */
    public boolean isColorDirty() {
        return !Arrays.equals(initialRGBX, 0, rgbxLength, cacheRGBX, 0, rgbxLength);
    }

    /**
     * Convert Kelvin to Mirek or vice-versa.
     */
    private static double reciprocal(double value) {
        return (Double.isNaN(value) || value == 0.0) ? 0 : 1000000.0 / value;
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
        return toNonNull(super.getColorTemperature());
    }

    /**
     * Get the color temperature as a PercentType.
     */
    public State getColorTemperaturePercentState() {
        return toNonNull(super.getColorTemperaturePercent());
    }

    /**
     * Set the color temperature.
     */
    public void setColorTemp(double kelvin) {
        logger.trace("{}: light {} setColorTemp({})", lightHandler.thingName, lightId, kelvin);
        super.setMirek(reciprocal(kelvin));
        setMode(Mode.WHITE);
    }

    /**
     * Check if the color temperature has been changed since lock() was called.
     */
    public boolean isColorTempDirty() {
        return !Objects.equals(initialColorTemperature, super.getColorTemperature());
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
        logger.trace("{}: light {} setEffect({})", lightHandler.thingName, lightId, value);
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
        return toNonNull(super.getBrightness(true));
    }

    /**
     * Set gain (i.e. the brightness when color mode).
     */
    public void setGain(double gain) {
        logger.trace("{}: light {} setGain({})", lightHandler.thingName, lightId, gain);
        super.setBrightness(gain);
        setMode(Mode.COLOR);
    }

    /**
     * Set gain (i.e. the brightness when color mode).
     */
    public void setGain(Command command) {
        if (!(command instanceof HSBType)) {
            logger.trace("{}: light {} setGain({})", lightHandler.thingName, lightId, command);
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
     * Get the light Id.
     */
    public int getLightId() {
        return lightId;
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
        if (modeChangesAllowed) {
            logger.trace("{}: light {} setMode({})", lightHandler.thingName, lightId, shellyMode);
            this.shellyMode = shellyMode;
        }
    }

    /**
     * Check if the mode has been changed since lock() was called.
     */
    public boolean isModeDirty() {
        return initialShellyMode != shellyMode;
    }

    public State getOnOffState() {
        return toNonNull(super.getOnOff(true));
    }

    /**
     * Set the on/off state.
     */
    @Override
    public void setOnOff(boolean on) {
        logger.trace("{}: light {} setOnOff({})", lightHandler.thingName, lightId, on);
        super.setOnOff(on);
    }

    /**
     * Check if the on/off state has been changed since lock() was called.
     */
    public boolean isOnOffDirty() {
        return !Objects.equals(initialOnOff, super.getOnOff(true));
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
        logger.trace("{}: light {} setRGBX({})", lightHandler.thingName, lightId, rgbx);
        super.setRGBx(Arrays.stream(rgbx).mapToDouble(i -> (double) i).toArray());
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
        return "mode:%s, power:%s, gain/bri:%s, rgbw:%s, color-temp:%s, color-temp-abs:%s, effect:%s".formatted(
                getMode(), getOnOffState(), getBrightnessState(), Arrays.toString(getRGBX()),
                getColorTemperaturePercentState(), getColorTemperatureAbsoluteState(), getEffectState());
    }

    /**
     * Check if any of the dirty flags have been set since lock() was called.
     */
    public boolean isDirty() {
        return isOnOffDirty() || isBrightnessDirty() || isColorDirty() || isColorTempDirty() || isEffectDirty()
                || isModeDirty();
    }

    /**
     * Acquire the lock, and save the initial model state to allow for subsequent dirty flag checks.
     */
    public void acquire() {
        lock.lock();
        initialSnapshot = logger.isDebugEnabled() ? toString() : null;
        initialRGBX = getRGBX();
        initialOnOff = super.getOnOff(true);
        initialEffect = effect;
        initialShellyMode = shellyMode;
        initialBrightness = super.getBrightness(true);
        initialColorTemperature = super.getColorTemperature();
        logger.debug("{}: light model {} acquired", lightHandler.thingName, lightId);
    }

    /**
     * Release the lock, and check if the model is dirty and if so update the handlers channels from this light model.
     * 
     * @return true if the model was dirty and the channels were updated, false otherwise.
     */
    public boolean release() {
        boolean updated = lightHandler.updateChannelsFromLightModel(this);
        if (updated) {
            logger.debug("{}: light {} model updated..\n => OLD: {}\n => NEW: {}", lightHandler.thingName, lightId,
                    initialSnapshot, this);
        }
        logger.debug("{}: light model {} released", lightHandler.thingName, lightId);
        lock.unlock();
        return updated;
    }

    /**
     * Returns true if the light model supports brightness channels, false otherwise.
     * All devices with color temperature support brightness, plus RGBW2 and all non-gen2
     * devices operating in white mode.
     * 
     * NOTE: gain channel is the brightness channel when devices are in color mode.
     * 
     * @param index the zero based index of the light model in the map
     * @param mode the light model operating mode
     * @return true if such channels are supported, false otherwise
     */
    public boolean supportsBrightnessChannel() {
        return
        // @formatter:off
            supportsColorTempChannel() ||
            (isRGBW2 && Mode.WHITE == shellyMode) ||
            (!isGen2 && Mode.WHITE == shellyMode) ||
            (isProfileLIGHT) ||
            (isProfileRGBCCT && lightId > 0) || 
            (isProfileRGBX2LIGHT && lightId > 0) ||
            (isProfileCCTX2)
        // @formatter:on
        ;
    }

    /**
     * Returns true if the light model supports color channels (RGB or RGBW), false otherwise.
     * In case of dual mode devices, the inColor flag is used to refine the check.
     * In case of multiple profile devices, the model id is used to refine the check.
     *
     * @return true if such channels are supported, false otherwise
     */
    public boolean supportsColorChannel() {
        return
        // @formatter:off
           (isBulb) ||
           (isRGBW2 && Mode.COLOR == shellyMode) ||
           (isG3ColorBulb) ||
           (isProfileRGB) ||
           (isProfileRGBW) ||
           (isProfileRGBCCT && lightId == 0) || 
           (isProfileRGBX2LIGHT && lightId == 0)
        // @formatter:on
        ;
    }

    /**
     * Returns true if the light model supports color temperature channels, false otherwise.
     * 
     * @return true if such channels are supported, false otherwise
     */
    public boolean supportsColorTempChannel() {
        return
        // @formatter:off
            (isDuo) || 
            (isBulb) || 
            (isG3DuoBulb) || 
            (isG3ColorBulb) || 
            (isProfileCCTX2) ||
            (isProfileRGBCCT && lightId > 0) 
        // @formatter:on
        ;
    }

    /**
     * Returns true if the light model supports effect channels, false otherwise.
     *
     * @return true if such channels are supported, false otherwise
     */
    public boolean supportsEffectChannel() {
        return supportsColorChannel();
    }

    /**
     * Returns true if the light model supports gain channels, false otherwise.
     * NOTE: gain channel is the brightness channel when devices are in color mode.
     *
     * @return true if such channels are supported, false otherwise
     */
    public boolean supportsGainChannel() {
        return supportsColorChannel();
    }

    /**
     * Returns true if the light model supports a switch channel, false otherwise.
     * All devices, except RGBW2 devices operating in light mode, support switch
     * .. BUT for the first light model only!
     *
     * @return true if such channels are supported, false otherwise
     */
    public boolean supportsOnOffChannel() {
        return
        // @formatter:off
            (isDuo) || 
            (isBulb) ||
            (isRGBW2 && Mode.COLOR == shellyMode) ||
            (isG3DuoBulb) || 
            (isG3ColorBulb) ||
            (isProfileCCTX2 && lightId == 0) ||
            (isProfileRGB) ||
            (isProfileRGBW) ||
            (isProfileRGBCCT && lightId == 0) || 
            (isProfileRGBX2LIGHT && lightId == 0)
        // @formatter:on
        ;
    }
}
