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
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
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
     * A record that carries the required light capabilities, RGB data type, LED operating mode, and whether the
     * operating mode is fixed or changeabl.
     * 
     * @param lightCapabilities the required light capabilities
     * @param rgbDataType the required RGB data type
     * @param ledOperatingMode the required LED operating mode
     * @param modelOperatingMode the required COLOR/WHITE operating mode
     * @param isOperatingModeReadOnly true if the operating mode shall be read-only, false otherwise
     */
    private record Parameters(LightCapabilities lightCapabilities, RgbDataType rgbDataType,
            LedOperatingMode ledOperatingMode, Mode modelOperatingMode, boolean isOperatingModeReadOnly) {
    }

    /*
     * The {@link LightModel} class does not round trip RGBX values cleanly (e.g. because [255,155,155,0] is a
     * functional synonym for [100,0,0,155]) so we cache the input values here to avoid data loss or confusion.
     */
    private final int[] cacheRGBX;
    private final int rgbxLength;
    private final int channelGroupNumber;
    private final ShellyLightHandler handler;
    private final ReentrantLock lock = new ReentrantLock();
    private final boolean isOperatingModeReadOnly;

    // essential fields copied from profile
    private final boolean isBulb;
    private final boolean isDuo;
    private final boolean isRGBW2;
    private final boolean isVintage;
    private final boolean isG3DuoBulb;
    private final boolean isG3ColorBulb;
    private final boolean isProfileLIGHT;
    private final boolean isProfileRGB;
    private final boolean isProfileRGBW;
    private final boolean isProfileRGBCCT;
    private final boolean isProfileRGBX2LIGHT;
    private final boolean isProfileCCTX2;

    private Mode operatingMode = Mode.WHITE;
    private int effect = 0;

    // initial values used to determine if the model dirty state changes
    private volatile Mode baselineOperatingMode;
    private volatile int baselineEffect;
    private volatile int[] baselineRGBX;
    private volatile @Nullable PercentType baselineBrightness;
    private volatile @Nullable OnOffType baselineOnOff;
    private volatile @Nullable QuantityType<?> baselineColorTemperature;
    private volatile @Nullable String baselineSnapshot;

    /**
     * Public static class factory that creates a {@link ShellyLightModel} with the correct parameters based on the
     * given {@link ThingTypeUID},the component index, and the {@link ShellyDeviceProfile}.
     * 
     * @param handler the ShellyLightHandler that owns this model
     * @param channelGroupNumber the channel group number of the light within the device
     * @param deviceProfile the ShellyDeviceProfile for the device
     * @param stepSize the step size for the light model
     * @return a new ShellyLightModel with the correct parameters
     */
    public static ShellyLightModel create(ShellyLightHandler handler, int channelGroupNumber,
            ShellyDeviceProfile deviceProfile, double stepSize) {
        Parameters required = getRequiredParamaters(handler, channelGroupNumber, deviceProfile.device.profile);
        return new ShellyLightModel(handler, channelGroupNumber, deviceProfile.device.profile,
                required.lightCapabilities, required.rgbDataType, required.ledOperatingMode,
                reciprocal(deviceProfile.maxTemp), reciprocal(deviceProfile.minTemp), stepSize,
                required.modelOperatingMode, required.isOperatingModeReadOnly);
    }

    /**
     * Get the required light capabilities, RGB data type, and LED operating mode for the {@link ShellyLightModel}
     * from the given {@link ShellyLightHandler}, the component index in the device, and the device profile string
     * (if any).
     * 
     * @param handler the {@link ShellyLightHandler} that owns this model
     * @param componentIndex the index of the light component within the device
     * @param configProfile the Shelly Gen 2/3 device configured operating profile, may be null e.g. for Gen 1 devices
     * @return a Parameters record with the required light capabilities, RGB data type, LED operating mode etc.
     */
    private static Parameters getRequiredParamaters(ShellyLightHandler handler, int componentIndex,
            @Nullable String configProfile) {
        ThingTypeUID thingTypeUID = handler.getThing().getThingTypeUID();

        // ==== GENERATION 1 ====
        if (THING_TYPE_SHELLYBULB.equals(thingTypeUID)) {
            return new Parameters(COLOR_WITH_COLOR_TEMPERATURE, RGB_W_NO_BRIGHTNESS, COMBINED, Mode.COLOR, false);
        }

        if (THING_TYPE_SHELLYDUO.equals(thingTypeUID)) {
            return new Parameters(BRIGHTNESS_WITH_COLOR_TEMPERATURE, RGB_W_NO_BRIGHTNESS, WHITE_ONLY, Mode.WHITE, true);
        }

        if (THING_TYPE_SHELLYVINTAGE.equals(thingTypeUID)) {
            return new Parameters(BRIGHTNESS, RGB_W_NO_BRIGHTNESS, WHITE_ONLY, Mode.WHITE, true);
        }

        if (THING_TYPE_SHELLYDUORGBW.equals(thingTypeUID)) {
            return new Parameters(COLOR_WITH_COLOR_TEMPERATURE, RGB_W_NO_BRIGHTNESS, COMBINED, Mode.COLOR, false);
        }

        // ==== GENERATION 2 ====
        if (THING_TYPE_SHELLYRGBW2_COLOR.equals(thingTypeUID)) {
            return new Parameters(COLOR, RGB_W_NO_BRIGHTNESS, COMBINED, Mode.COLOR, true);
        }

        if (THING_TYPE_SHELLYRGBW2_WHITE.equals(thingTypeUID)) {
            return new Parameters(BRIGHTNESS, RGB_W_NO_BRIGHTNESS, WHITE_ONLY, Mode.WHITE, true);
        }

        // ==== GENERATION 3 ====
        if (THING_TYPE_SHELLYPLUSRGBWPM.equals(thingTypeUID)) {
            if (SHELLY2_PROFILE_RGB.equals(configProfile)) {
                return new Parameters(COLOR, RGB_NO_BRIGHTNESS, RGB_ONLY, Mode.COLOR, true);
            }
            if (SHELLY2_PROFILE_RGBW.equals(configProfile)) {
                return new Parameters(COLOR, RGB_W_NO_BRIGHTNESS, COMBINED, Mode.COLOR, true);
            }
            if (SHELLY2_PROFILE_LIGHT.equals(configProfile)) {
                return new Parameters(BRIGHTNESS, RGB_W_NO_BRIGHTNESS, WHITE_ONLY, Mode.WHITE, true);
            }
        }

        if (THING_TYPE_SHELLYPRORGBWWPM.equals(thingTypeUID)) {
            if (SHELLY2_PROFILE_RGB.equals(configProfile)) {
                return new Parameters(COLOR, RGB_NO_BRIGHTNESS, RGB_ONLY, Mode.COLOR, true);
            }
            if (SHELLY2_PROFILE_RGBW.equals(configProfile)) {
                return new Parameters(COLOR, RGB_W_NO_BRIGHTNESS, COMBINED, Mode.COLOR, true);
            }
            if (SHELLY2_PROFILE_LIGHT.equals(configProfile)) {
                return new Parameters(BRIGHTNESS, RGB_W_NO_BRIGHTNESS, WHITE_ONLY, Mode.WHITE, true);
            }
            if (SHELLY2_PROFILE_RGBCCT.equals(configProfile)) {
                return componentIndex > 0
                        ? new Parameters(BRIGHTNESS_WITH_COLOR_TEMPERATURE, RGB_W_NO_BRIGHTNESS, WHITE_ONLY, Mode.WHITE,
                                true)
                        : new Parameters(COLOR_WITH_COLOR_TEMPERATURE, RGB_NO_BRIGHTNESS, RGB_ONLY, Mode.COLOR, true);
            }
            if (SHELLY2_PROFILE_RGBX2LIGHT.equals(configProfile)) {
                return componentIndex > 0
                        ? new Parameters(BRIGHTNESS, RGB_W_NO_BRIGHTNESS, WHITE_ONLY, Mode.WHITE, true)
                        : new Parameters(COLOR, RGB_NO_BRIGHTNESS, RGB_ONLY, Mode.COLOR, true);
            }
            if (SHELLY2_PROFILE_CCTX2.equals(configProfile)) {
                return new Parameters(BRIGHTNESS_WITH_COLOR_TEMPERATURE, RGB_W_NO_BRIGHTNESS, WHITE_ONLY, Mode.WHITE,
                        true);
            }
        }

        if (THING_TYPE_SHELLYPLUSDUOBULB.equals(thingTypeUID)) {
            return new Parameters(BRIGHTNESS_WITH_COLOR_TEMPERATURE, RGB_W_NO_BRIGHTNESS, WHITE_ONLY, Mode.WHITE, true);
        }

        if (THING_TYPE_SHELLYPLUSCOLORBULB.equals(thingTypeUID)) {
            return new Parameters(COLOR_WITH_COLOR_TEMPERATURE, RGB_W_NO_BRIGHTNESS, COMBINED, Mode.COLOR, false);
        }

        throw new IllegalArgumentException("%s: Error creating Light Model for %s"
                .formatted(handler.getThing().getLabel(), thingTypeUID.toString()));
    }

    /**
     * Private constructor to create a ShellyLightModel with the given parameters.
     * 
     * @param handler the ShellyLightHandler that owns this model
     * @param channelGroupNumber the channel group number of the light within the device
     * @param configProfile the Shelly Gen 2/3 device profile (if any), may be null e.g. for Gen 1 devices
     * @param lightCapabilities the required light capabilities
     * @param rgbDataType the required RGB data type
     * @param ledOperatingMode the required LED operating mode
     * @param mirekCoolest the coolest supported color temperature in mirek
     * @param mirekWarmest the warmest supported color temperature in mirek
     * @param stepSize the step size for the light model
     * @param operatingMode the baseline COLOR/WHITE operating mode
     * @param isOperatingModeReadOnly true if the operating mode is read-only, false otherwise
     * @throws IllegalArgumentException if the parameters are invalid
     */
    private ShellyLightModel(ShellyLightHandler handler, int channelGroupNumber, @Nullable String configProfile,
            LightCapabilities lightCapabilities, RgbDataType rgbDataType, LedOperatingMode ledOperatingMode,
            Double mirekCoolest, Double mirekWarmest, Double stepSize, Mode operatingMode,
            boolean isOperatingModeReadOnly) throws IllegalArgumentException {

        super(lightCapabilities, rgbDataType, null, mirekCoolest, mirekWarmest, stepSize, null, null);
        super.setLedOperatingMode(ledOperatingMode);

        this.handler = handler;
        this.channelGroupNumber = channelGroupNumber;
        this.operatingMode = operatingMode;
        this.baselineOperatingMode = operatingMode;
        this.isOperatingModeReadOnly = isOperatingModeReadOnly;

        // initialize some flags from ThingTypeUID
        ThingTypeUID thingTypeUID = handler.getThing().getThingTypeUID();
        isVintage = THING_TYPE_SHELLYVINTAGE.equals(thingTypeUID);
        isBulb = THING_TYPE_SHELLYBULB.equals(thingTypeUID);
        isDuo = GROUP_DUO_THING_TYPES.contains(thingTypeUID);
        isRGBW2 = GROUP_RGBW2_THING_TYPES.contains(thingTypeUID);
        isG3DuoBulb = THING_TYPE_SHELLYPLUSDUOBULB.equals(thingTypeUID); // TODO mapping in #20909 is wrong
        isG3ColorBulb = THING_TYPE_SHELLYPLUSCOLORBULB.equals(thingTypeUID); // TODO mapping in #20909 is wrong

        // initialize some flags from the device configured operating profile (Generation 2/3)
        isProfileLIGHT = SHELLY2_PROFILE_LIGHT.equals(configProfile);
        isProfileRGB = SHELLY2_PROFILE_RGB.equals(configProfile);
        isProfileRGBW = SHELLY2_PROFILE_RGBW.equals(configProfile);
        isProfileRGBCCT = SHELLY2_PROFILE_RGBCCT.equals(configProfile);
        isProfileRGBX2LIGHT = SHELLY2_PROFILE_RGBX2LIGHT.equals(configProfile);
        isProfileCCTX2 = SHELLY2_PROFILE_CCTX2.equals(configProfile);

        rgbxLength = super.getRGBx().length;
        baselineRGBX = new int[rgbxLength];
        cacheRGBX = Arrays.copyOf(baselineRGBX, rgbxLength);
        super.setRGBx(Arrays.stream(cacheRGBX).mapToDouble(i -> (double) i).toArray());

        logger.debug("{}: created model from thingTypeUID:{} configProfile:{} for channelGroupNumber:{} with "
                + "capabilities:{}, rgbDataType:{}, ledOperatingMode:{}, shellyMode:{}, isModeReadOnly:{}, ct-range: [{} K..{} K]",
                handler.thingName, thingTypeUID, configProfile, channelGroupNumber, lightCapabilities, rgbDataType,
                ledOperatingMode, baselineOperatingMode, isOperatingModeReadOnly, Math.round(reciprocal(mirekWarmest)),
                Math.round(reciprocal(mirekCoolest)));
    }

    /**
     * OpenHAB light control standard main entry point:
     * Override handleCommand and set the mode and dirty flags accordingly.
     */
    @Override
    public void handleCommand(Command command) {
        logger.trace("{}: channelGroupNo:{} model handleCommand({})", handler.thingName, channelGroupNumber, command);
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
        logger.trace("{}: channelGroupNo:{} model handleColorTemperatureCommand({})", handler.thingName,
                channelGroupNumber, command);
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
        logger.trace("{}: channelGroupNo:{} model setBrightness({})", handler.thingName, channelGroupNumber,
                brightness);
        super.setBrightness(brightness);
        setMode(Mode.WHITE);
    }

    /**
     * Set the brightness (i.e. the brightness when color temperature mode).
     */
    public void setBrightness(Command command) {
        if (!(command instanceof HSBType)) {
            logger.trace("{}: channelGroupNo:{} model setBrightness({})", handler.thingName, channelGroupNumber,
                    command);
            super.handleCommand(command);
            setMode(Mode.WHITE);
        }
    }

    /**
     * Check if the brightness has been changed since lock() was called.
     */
    public boolean isBrightnessDirty() {
        return !Objects.equals(baselineBrightness, super.getBrightness(true));
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
        logger.trace("{}: channelGroupNo:{} model setColor({},{})", handler.thingName, channelGroupNumber, index,
                value);
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
        return !Arrays.equals(baselineRGBX, 0, rgbxLength, cacheRGBX, 0, rgbxLength);
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
        QuantityType<?> ct = super.getColorTemperature();
        return ct != null ? new QuantityType<>(Math.round(ct.doubleValue()), ct.getUnit()) : UnDefType.UNDEF;
    }

    /**
     * Get the color temperature as a PercentType.
     */
    public State getColorTemperaturePercentState() {
        PercentType pct = super.getColorTemperaturePercent();
        return pct != null ? new PercentType((int) Math.round(pct.doubleValue())) : UnDefType.UNDEF;
    }

    /**
     * Set the color temperature.
     */
    public void setColorTemp(double kelvin) {
        logger.trace("{}: channelGroupNo:{} model setColorTemp({})", handler.thingName, channelGroupNumber, kelvin);
        super.setMirek(reciprocal(kelvin));
        setMode(Mode.WHITE);
    }

    public void setColorTempRange(int minKelvin, int maxKelvin) {
        configSetMirekControlCoolest(reciprocal(maxKelvin));
        configSetMirekControlWarmest(reciprocal(minKelvin));
    }

    /**
     * Check if the color temperature has been changed since lock() was called.
     */
    public boolean isColorTempDirty() {
        return !Objects.equals(baselineColorTemperature, super.getColorTemperature());
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
        logger.trace("{}: channelGroupNo:{} model setEffect({})", handler.thingName, channelGroupNumber, value);
        effect = value;
    }

    /**
     * Check if the effect has been changed since lock() was called.
     */
    public boolean isEffectDirty() {
        return !Objects.equals(baselineEffect, effect);
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
        logger.trace("{}: channelGroupNo:{} model setGain({})", handler.thingName, channelGroupNumber, gain);
        super.setBrightness(gain);
        setMode(Mode.COLOR);
    }

    /**
     * Set gain (i.e. the brightness when color mode).
     */
    public void setGain(Command command) {
        if (!(command instanceof HSBType)) {
            logger.trace("{}: channelGroupNo:{} model setGain({})", handler.thingName, channelGroupNumber, command);
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
     * Get the the channel group number within the device.
     */
    public int getChannelGroupNumber() {
        return channelGroupNumber;
    }

    /**
     * Get the shelly device mode.
     */
    public Mode getMode() {
        return operatingMode;
    }

    public OnOffType getModeState() {
        return OnOffType.from(operatingMode == Mode.COLOR);
    }

    /**
     * Set the shelly device mode.
     */
    public void setMode(Mode shellyMode) {
        if (!isOperatingModeReadOnly) {
            logger.trace("{}: channelGroupNo:{} model setMode({})", handler.thingName, channelGroupNumber, shellyMode);
            this.operatingMode = shellyMode;
        }
    }

    /**
     * Check if the mode has been changed since lock() was called.
     */
    public boolean isModeDirty() {
        return baselineOperatingMode != operatingMode;
    }

    public State getOnOffState() {
        return toNonNull(super.getOnOff(true));
    }

    /**
     * Set the on/off state.
     */
    @Override
    public void setOnOff(boolean on) {
        logger.trace("{}: channelGroupNo:{} model setOnOff({})", handler.thingName, channelGroupNumber, on);
        super.setOnOff(on);
    }

    /**
     * Check if the on/off state has been changed since lock() was called.
     */
    public boolean isOnOffDirty() {
        return !Objects.equals(baselineOnOff, super.getOnOff(true));
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
        logger.trace("{}: channelGroupNo:{} model setRGBX({})", handler.thingName, channelGroupNumber, rgbx);
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
            setRGBX(rgbxLength == 4 ? new int[] { SHELLY_MAX_COLOR, 0, 0, 0 } : new int[] { SHELLY_MAX_COLOR, 0, 0 });
        } else if (color.equals(SHELLY_COLOR_GREEN)) {
            setRGBX(rgbxLength == 4 ? new int[] { 0, SHELLY_MAX_COLOR, 0, 0 } : new int[] { 0, SHELLY_MAX_COLOR, 0 });
        } else if (color.equals(SHELLY_COLOR_BLUE)) {
            setRGBX(rgbxLength == 4 ? new int[] { 0, 0, SHELLY_MAX_COLOR, 0 } : new int[] { 0, 0, SHELLY_MAX_COLOR });
        } else if (color.equals(SHELLY_COLOR_YELLOW)) {
            setRGBX(rgbxLength == 4 ? new int[] { SHELLY_MAX_COLOR, SHELLY_MAX_COLOR, 0, 0 }
                    : new int[] { SHELLY_MAX_COLOR, SHELLY_MAX_COLOR, 0 });
        } else if (color.equals(SHELLY_COLOR_WHITE)) {
            setRGBX(rgbxLength == 4 ? new int[] { 0, 0, 0, SHELLY_MAX_COLOR }
                    : new int[] { SHELLY_MAX_COLOR, SHELLY_MAX_COLOR, SHELLY_MAX_COLOR });
        } else {
            throw new IllegalArgumentException("Invalid full color selection: " + color);
        }
    }

    /**
     * Get the full color as a StringType. The color is returned as one of the predefined color names,
     * or UNDEF if the color does not match any of the predefined colors.
     */
    public State getFullColorState() {
        int[] rgbw = getRGBX();
        if (rgbw[0] == SHELLY_MAX_COLOR && rgbw[1] == SHELLY_MAX_COLOR && rgbw[2] == 0) {
            return new StringType(SHELLY_COLOR_YELLOW);
        } else if (rgbw[0] == SHELLY_MAX_COLOR && rgbw[1] == 0 && rgbw[2] == 0) {
            return new StringType(SHELLY_COLOR_RED);
        } else if (rgbw[0] == 0 && rgbw[1] == SHELLY_MAX_COLOR && rgbw[2] == 0) {
            return new StringType(SHELLY_COLOR_GREEN);
        } else if (rgbw[0] == 0 && rgbw[1] == 0 && rgbw[2] == SHELLY_MAX_COLOR) {
            return new StringType(SHELLY_COLOR_BLUE);
        } else if (rgbw.length == 4 && rgbw[0] == 0 && rgbw[1] == 0 && rgbw[2] == 0 && rgbw[3] == SHELLY_MAX_COLOR) {
            return new StringType(SHELLY_COLOR_WHITE);
        } else if (rgbw.length == 3 && rgbw[0] == SHELLY_MAX_COLOR && rgbw[1] == SHELLY_MAX_COLOR) {
            return new StringType(SHELLY_COLOR_WHITE);
        }
        return UnDefType.UNDEF;
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
        baselineSnapshot = logger.isDebugEnabled() ? toString() : null;
        baselineRGBX = getRGBX();
        baselineOnOff = super.getOnOff(true);
        baselineEffect = effect;
        baselineOperatingMode = operatingMode;
        baselineBrightness = super.getBrightness(true);
        baselineColorTemperature = super.getColorTemperature();
        logger.debug("{}: channelGroupNo:{} model acquired", handler.thingName, channelGroupNumber);
    }

    /**
     * Release the lock, and check if the model is dirty and if so update the handlers channels from this light model.
     * 
     * @return true if the model was dirty and the channels were updated, false otherwise.
     */
    public boolean release() {
        boolean updated = handler.updateChannelsFromLightModel(this);
        if (updated) {
            logger.debug("{}: channelGroupNo:{} model updated..\n => OLD: {}\n => NEW: {}", handler.thingName,
                    channelGroupNumber, baselineSnapshot, this);
        }
        logger.debug("{}: channelGroupNo:{} model released", handler.thingName, channelGroupNumber);
        lock.unlock();
        return updated;
    }

    /**
     * Returns true if the light model supports brightness channels, false otherwise.
     * 
     * NOTE: the gain channel valid when devices are in COLOR mode, and the brightness
     * channel is valid when devices are in WHITE mode.
     * 
     * @param ignoreLiveOperatingMode if true the check does not evaluate the actual mode.
     * @return true if such channels are supported, false otherwise.
     */
    public boolean supportsBrightnessChannel(boolean ignoreLiveOperatingMode) {
        return (
        // @formatter:off
            (isVintage) || 
            (isDuo) ||
            (isBulb) ||
            (isRGBW2 && Mode.WHITE == operatingMode) || // never ignore the operating mode
            (isG3DuoBulb) ||
            (isG3ColorBulb) ||
            (isProfileCCTX2) ||
            (isProfileLIGHT) ||
            (isProfileRGBCCT && channelGroupNumber > 0) ||
            (isProfileRGBX2LIGHT && channelGroupNumber > 0)
        // @formatter:on
        ) && (ignoreLiveOperatingMode || isOperatingModeReadOnly || Mode.WHITE == operatingMode);
    }

    public boolean supportsBrightnessChannel() {
        return supportsBrightnessChannel(false);
    }

    /**
     * Returns true if the light model supports color channels (RGB or RGBW), false otherwise.
     * In case of dual mode devices, the inColor flag is used to refine the check.
     * In case of multiple profile devices, the model id is used to refine the check.
     *
     * @param ignoreLiveOperatingMode if true the check does not evaluate the actual mode.
     * @return true if such channels are supported, false otherwise.
     */
    public boolean supportsColorChannel(boolean ignoreLiveOperatingMode) {
        return (
        // @formatter:off
           (isBulb) ||
           (isRGBW2 && Mode.COLOR == operatingMode) || // never ignore the operating mode
           (isG3ColorBulb) ||
           (isProfileRGB) ||
           (isProfileRGBW) ||
           (isProfileRGBCCT && channelGroupNumber == 0) || 
           (isProfileRGBX2LIGHT && channelGroupNumber == 0)
        // @formatter:on
        ) && (ignoreLiveOperatingMode || isOperatingModeReadOnly || Mode.COLOR == operatingMode);
    }

    public boolean supportsColorChannel() {
        return supportsColorChannel(false);
    }

    /**
     * Returns true if the light model supports color temperature channels, false otherwise.
     * 
     * @param ignoreLiveOperatingMode if true the check does not evaluate the actual mode.
     * @return true if such channels are supported, false otherwise.
     */
    public boolean supportsColorTempChannel(boolean ignoreLiveOperatingMode) {
        return (
        // @formatter:off
            (isDuo && !isVintage) || // Vintage bulb is white-only!
            (isBulb) ||
            (isG3DuoBulb) || 
            (isG3ColorBulb) || 
            (isProfileCCTX2) ||
            (isProfileRGBCCT && channelGroupNumber > 0) 
        // @formatter:on
        ) && (ignoreLiveOperatingMode || isOperatingModeReadOnly || Mode.WHITE == operatingMode);
    }

    public boolean supportsColorTempChannel() {
        return supportsColorTempChannel(false);
    }

    /**
     * Returns true if the light model supports effect channels, false otherwise.
     *
     * @param ignoreLiveOperatingMode if true the check does not evaluate the actual mode.
     * @return true if such channels are supported, false otherwise.
     */
    public boolean supportsEffectChannel(boolean ignoreLiveOperatingMode) {
        return supportsColorChannel(ignoreLiveOperatingMode);
    }

    public boolean supportsEffectChannel() {
        return supportsEffectChannel(false);
    }

    /**
     * Returns true if the light model supports gain channels, false otherwise.
     * 
     * NOTE: the gain channel valid when devices are in COLOR mode, and the brightness
     * channel is valid when devices are in WHITE mode.
     *
     * @param ignoreLiveOperatingMode if true the check does not evaluate the actual mode.
     * @return true if such channels are supported, false otherwise.
     */
    public boolean supportsGainChannel(boolean ignoreLiveOperatingMode) {
        return supportsColorChannel(ignoreLiveOperatingMode);
    }

    public boolean supportsGainChannel() {
        return supportsGainChannel(false);
    }

    /**
     * Returns true if the light model supports a switch channel, false otherwise.
     * All devices, except RGBW2 devices operating in light mode, support switch channels
     * .. BUT for the first light model only!
     *
     * @return true if such channels are supported, false otherwise.
     */
    public boolean supportsOnOffChannel() {
        return
        // @formatter:off
            (isDuo) || 
            (isBulb) ||
            (isRGBW2 && Mode.COLOR == operatingMode) ||
            (isG3DuoBulb) ||
            (isG3ColorBulb) ||
            // TODO isProfileCCTX2 ??
            (isProfileRGB) ||
            (isProfileRGBW) ||
            (isProfileRGBCCT && channelGroupNumber == 0) || 
            (isProfileRGBX2LIGHT && channelGroupNumber == 0)
        // @formatter:on
        ;
    }

    /**
     * Returns true if the light model supports on/off via its brightness channel, false
     * otherwise. This is a special case for devices operating in light mode, which do not
     * support an own switch channel.
     *
     * @return true if such channels are supported, false otherwise.
     */
    public boolean supportsOnOffViaBrightnessChannel() {
        return
        // @formatter:off
            (isRGBW2 && Mode.WHITE == operatingMode) ||
            // TODO isProfileCCTX2 ??
            // TODO isProfileRGBCCT && lightId == 0 ?? 
            (isProfileLIGHT) ||
            (isProfileRGBX2LIGHT && channelGroupNumber > 0)
        // @formatter:on
        ;
    }
}
