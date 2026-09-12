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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.provider.ShellyChannelDefinitions;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(ShellyLightModel.class);

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
     * operating mode is fixed or changeable.
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
    private final int channelGroupSuffix;
    private final int apiLightIndex;
    private final ShellyLightHandler handler;
    private final ReentrantLock lock = new ReentrantLock();
    private final boolean isOperatingModeReadOnly;

    // essential fields copied from profile
    private final boolean isBulb;
    private final boolean isDuo;
    private final boolean isDuoRGBW;
    private final boolean isVintage;
    private final boolean isG3ColorTempBulb;
    private final boolean isG3FullColorBulb;
    private final boolean isProfileLIGHT;
    private final boolean isProfileRGB;
    private final boolean isProfileRGBW;
    private final boolean isProfileRGBCCT;
    private final boolean isProfileRGBX2LIGHT;
    private final boolean isProfileCCTX2;
    private final boolean isRGBW2White;
    private final boolean isRGBW2Color;
    private final boolean isGen2;
    private final int minKelvin;
    private final int maxKelvin;

    private Mode operatingMode = Mode.WHITE;
    private int effect = 0;

    // initial values used to determine if the model dirty state changes
    private volatile @Nullable Mode baselineOperatingMode = null;
    private volatile @Nullable Integer baselineEffect = null;
    private volatile int[] baselineRGBX = new int[0];
    private volatile @Nullable PercentType baselineBrightness = null;
    private volatile @Nullable OnOffType baselineOnOff = null;
    private volatile @Nullable QuantityType<?> baselineColorTemperature = null;

    /**
     * Public static class factory that creates a {@link ShellyLightModel} with the correct parameters based on the
     * given {@link ThingTypeUID},the component index, and the {@link ShellyDeviceProfile}.
     * 
     * @param handler the ShellyLightHandler that owns this model
     * @param channelGroupSuffix the channel group suffix number
     * @param deviceProfile the ShellyDeviceProfile for the device
     * @param stepSize the step size for the light model
     * @return a new ShellyLightModel with the correct parameters
     */
    public static ShellyLightModel create(ShellyLightHandler handler, int channelGroupSuffix,
            ShellyDeviceProfile deviceProfile, double stepSize) {
        Parameters required = getRequiredParameters(handler, channelGroupSuffix, deviceProfile.device.profile);
        return new ShellyLightModel(handler, channelGroupSuffix, required.lightCapabilities, required.rgbDataType,
                required.ledOperatingMode, required.modelOperatingMode, required.isOperatingModeReadOnly, deviceProfile,
                stepSize);
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
    private static Parameters getRequiredParameters(ShellyLightHandler handler, int componentIndex,
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
                return componentIndex == 0 ? new Parameters(COLOR, RGB_NO_BRIGHTNESS, RGB_ONLY, Mode.COLOR, true)
                        : new Parameters(BRIGHTNESS_WITH_COLOR_TEMPERATURE, RGB_W_NO_BRIGHTNESS, WHITE_ONLY, Mode.WHITE,
                                true);
            }
            if (SHELLY2_PROFILE_RGBX2LIGHT.equals(configProfile)) {
                return componentIndex == 0 ? new Parameters(COLOR, RGB_NO_BRIGHTNESS, RGB_ONLY, Mode.COLOR, true)
                        : new Parameters(BRIGHTNESS, RGB_W_NO_BRIGHTNESS, WHITE_ONLY, Mode.WHITE, true);
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
            return new Parameters(COLOR_WITH_COLOR_TEMPERATURE, RGB_NO_BRIGHTNESS, RGB_ONLY, Mode.COLOR, false);
        }

        LOGGER.warn("{}: Error creating Light Model for {} - using default", handler.getThing().getLabel(),
                thingTypeUID);
        return new Parameters(ON_OFF, DEFAULT, WHITE_ONLY, Mode.WHITE, false);
    }

    /**
     * Private constructor to create a ShellyLightModel with the given parameters.
     * 
     * @param handler the ShellyLightHandler that owns this model
     * @param channelGroupSuffix the channel group number of the light within the device
     * @param lightCapabilities the required light capabilities
     * @param rgbDataType the required RGB data type
     * @param ledOperatingMode the required LED operating mode
     * @param operatingMode the baseline COLOR/WHITE operating mode
     * @param isOperatingModeReadOnly true if the operating mode is read-only, false otherwise
     * @param stepSize the step size for the light model
     * @throws IllegalArgumentException if the parameters are invalid
     */
    private ShellyLightModel(ShellyLightHandler handler, int channelGroupSuffix, LightCapabilities lightCapabilities,
            RgbDataType rgbDataType, LedOperatingMode ledOperatingMode, Mode operatingMode,
            boolean isOperatingModeReadOnly, ShellyDeviceProfile profile, Double stepSize)
            throws IllegalArgumentException {

        super(lightCapabilities, rgbDataType, 0.0, null, null, stepSize, null, null);
        super.setLedOperatingMode(ledOperatingMode);

        this.handler = handler;
        this.channelGroupSuffix = channelGroupSuffix;
        this.operatingMode = operatingMode;
        this.baselineOperatingMode = operatingMode;
        this.isOperatingModeReadOnly = isOperatingModeReadOnly;

        // initialize some flags from ThingTypeUID
        ThingTypeUID thingTypeUID = handler.getThing().getThingTypeUID();
        isVintage = THING_TYPE_SHELLYVINTAGE.equals(thingTypeUID);
        isBulb = THING_TYPE_SHELLYBULB.equals(thingTypeUID);
        isDuo = GROUP_DUO_THING_TYPES.contains(thingTypeUID);
        isDuoRGBW = THING_TYPE_SHELLYDUORGBW.equals(thingTypeUID);
        isRGBW2White = THING_TYPE_SHELLYRGBW2_WHITE.equals(thingTypeUID);
        isRGBW2Color = THING_TYPE_SHELLYRGBW2_COLOR.equals(thingTypeUID);
        isG3ColorTempBulb = THING_TYPE_SHELLYPLUSDUOBULB.equals(thingTypeUID);
        isG3FullColorBulb = THING_TYPE_SHELLYPLUSCOLORBULB.equals(thingTypeUID);
        isGen2 = profile.isGen2;

        // initialize some flags from the thing type and device operating profile
        String configProfile = profile.device.profile;
        isProfileLIGHT = SHELLY2_PROFILE_LIGHT.equals(configProfile);
        isProfileRGB = SHELLY2_PROFILE_RGB.equals(configProfile);
        isProfileRGBW = SHELLY2_PROFILE_RGBW.equals(configProfile);
        isProfileRGBCCT = SHELLY2_PROFILE_RGBCCT.equals(configProfile);
        isProfileRGBX2LIGHT = SHELLY2_PROFILE_RGBX2LIGHT.equals(configProfile);
        isProfileCCTX2 = SHELLY2_PROFILE_CCTX2.equals(configProfile);

        apiLightIndex = ShellyChannelDefinitions.deviceHasMainLight(handler.getThing(), profile) //
                ? channelGroupSuffix
                : channelGroupSuffix - 1;

        rgbxLength = super.getRGBx().length;
        cacheRGBX = new int[rgbxLength];
        super.setRGBx(Arrays.stream(cacheRGBX).mapToDouble(i -> (double) i).toArray());

        minKelvin = profile.getMinTemp(apiLightIndex);
        maxKelvin = profile.getMaxTemp(apiLightIndex);
        this.setColorTempRange(minKelvin, maxKelvin);

        LOGGER.debug(
                "{}: created model from thingTypeUID:{} configProfile:{} with capabilities:{}, rgbDataType:{}, "
                        + "ledOperatingMode:{}, shellyMode:{}, isModeReadOnly:{}, ct-range: [{} K..{} K] => "
                        + "ShellyLightModel(apiIndex:{}, groupSuffix:{}) <= {}",
                handler.thingName, thingTypeUID, configProfile, lightCapabilities, rgbDataType, ledOperatingMode,
                baselineOperatingMode, isOperatingModeReadOnly, minKelvin, maxKelvin, channelGroupSuffix, apiLightIndex,
                this);
    }

    /**
     * OpenHAB light control standard main entry point:
     * Override handleCommand and set the mode and dirty flags accordingly.
     */
    @Override
    public void handleCommand(Command command) {
        LOGGER.trace("{}: ShellyLightModel(apiIndex:{}, groupSuffix:{}) => handleCommand({})", handler.thingName,
                apiLightIndex, channelGroupSuffix, command);
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
        LOGGER.trace("{}: ShellyLightModel(apiIndex:{}, groupSuffix:{}) => handleColorTemperatureCommand({})",
                handler.thingName, apiLightIndex, channelGroupSuffix, command);
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
        LOGGER.trace("{}: ShellyLightModel(apiIndex:{}, groupSuffix:{}) => setBrightness({})", handler.thingName,
                apiLightIndex, channelGroupSuffix, brightness);
        super.setBrightness(brightness);
    }

    /**
     * Set the brightness (i.e. the brightness when color temperature mode).
     */
    public void setBrightness(Command command) {
        if (!(command instanceof HSBType)) {
            LOGGER.trace("{}: ShellyLightModel(apiIndex:{}, groupSuffix:{}) => setBrightness({})", handler.thingName,
                    apiLightIndex, channelGroupSuffix, command);
            super.handleCommand(command);
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
        LOGGER.trace("{}: ShellyLightModel(apiIndex:{}, groupSuffix:{}) => setColor({},{})", handler.thingName,
                apiLightIndex, channelGroupSuffix, index, value);
        int[] rgbx = getRGBX();
        rgbx[index.ordinal()] = value;
        setRGBX(rgbx);
    }

    /**
     * Check if the color has been changed since lock() was called.
     * Note: a change in color temperature or in brightness is also considered a color change.
     */
    public boolean isColorDirty() {
        return !Arrays.equals(baselineRGBX, cacheRGBX)
                || !Objects.equals(baselineColorTemperature, super.getColorTemperature())
                || !Objects.equals(baselineBrightness, super.getBrightness(true));
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
     * Get the minimum color temperature in Kelvin.
     */
    public BigDecimal getColorTemperatureMinimumKelvin() {
        return BigDecimal.valueOf(minKelvin);
    }

    /**
     * Get the maximum color temperature in Kelvin.
     */
    public BigDecimal getColorTemperatureMaximumKelvin() {
        return BigDecimal.valueOf(maxKelvin);
    }

    /**
     * Set the color temperature.
     */
    public void setColorTemp(double kelvin) {
        LOGGER.trace("{}: ShellyLightModel(apiIndex:{}, groupSuffix:{}) => setColorTemp({})", handler.thingName,
                apiLightIndex, channelGroupSuffix, kelvin);
        super.setMirek(reciprocal(kelvin));
        setMode(Mode.WHITE);
    }

    public void setColorTempRange(int minKelvin, int maxKelvin) {
        super.configSetMirekControlCoolest(reciprocal(maxKelvin) - 0.001); // avoid rounding issues
        super.configSetMirekControlWarmest(reciprocal(minKelvin) + 0.001); // avoid rounding issues
    }

    /**
     * Check if the color temperature has been changed since lock() was called.
     * Note: a change in color is also considered a color temperature change.
     */
    public boolean isColorTempDirty() {
        return !Objects.equals(baselineColorTemperature, super.getColorTemperature())
                || !Arrays.equals(baselineRGBX, cacheRGBX);
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
        LOGGER.trace("{}: ShellyLightModel(apiIndex:{}, groupSuffix:{}) => setEffect({})", handler.thingName,
                apiLightIndex, channelGroupSuffix, value);
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
        LOGGER.trace("{}: ShellyLightModel(apiIndex:{}, groupSuffix:{}) => setGain({})", handler.thingName,
                apiLightIndex, channelGroupSuffix, gain);
        super.setBrightness(gain);
        setMode(Mode.COLOR);
    }

    /**
     * Set gain (i.e. the brightness when color mode).
     */
    public void setGain(Command command) {
        if (!(command instanceof HSBType)) {
            LOGGER.trace("{}: ShellyLightModel(apiIndex:{}, groupSuffix:{}) => setGain({})", handler.thingName,
                    apiLightIndex, channelGroupSuffix, command);
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
    public int getChannelGroupSuffix() {
        return channelGroupSuffix;
    }

    /**
     * Get the light index used by the API.
     */
    public int getApiLightIndex() {
        return apiLightIndex;
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
            LOGGER.trace("{}: ShellyLightModel(apiIndex:{}, groupSuffix:{}) => setMode({})", handler.thingName,
                    apiLightIndex, channelGroupSuffix, shellyMode);
            this.operatingMode = shellyMode;
        }
    }

    /**
     * Check if the mode has been changed since lock() was called.
     */
    public boolean isModeDirty() {
        return !Objects.equals(baselineOperatingMode, operatingMode);
    }

    public State getOnOffState() {
        return toNonNull(super.getOnOff(true));
    }

    /**
     * Set the on/off state.
     */
    @Override
    public void setOnOff(boolean offOn) {
        LOGGER.trace("{}: ShellyLightModel(apiIndex:{}, groupSuffix:{}) => setOnOff({})", handler.thingName,
                apiLightIndex, channelGroupSuffix, offOn);
        super.setOnOff(offOn);
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
        LOGGER.trace("{}: ShellyLightModel(apiIndex:{}, groupSuffix:{}) => setRGBX({})", handler.thingName,
                apiLightIndex, channelGroupSuffix, rgbx);
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
        } else if (rgbw.length == 3 && rgbw[0] == SHELLY_MAX_COLOR && rgbw[1] == SHELLY_MAX_COLOR
                && rgbw[2] == SHELLY_MAX_COLOR) {
            return new StringType(SHELLY_COLOR_WHITE);
        }
        return new StringType(SHELLY_COLOR_UNDEFINED);
    }

    @Override
    public String toString() {
        return "mode:%s, power:%s, gain/bri:%s, rgbw:%s, temperature-pct:%s, temperature:%s, effect:%s".formatted(
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
        baselineRGBX = getRGBX();
        baselineOnOff = super.getOnOff(true);
        baselineEffect = effect;
        baselineOperatingMode = operatingMode;
        baselineBrightness = super.getBrightness(true);
        baselineColorTemperature = super.getColorTemperature();
        LOGGER.debug("{}: ShellyLightModel(apiIndex:{}, groupSuffix:{}) => acquired", handler.thingName, apiLightIndex,
                channelGroupSuffix);
    }

    /**
     * Release the lock, and check if the model is dirty and if so update the handlers channels from this light model.
     * 
     * @param forceUpdate if true, the channels will be updated even if the model is not dirty.
     * @return true if the model was dirty and the channels were updated, false otherwise.
     */
    public boolean release(boolean forceUpdate) {
        try {
            boolean updated = handler.updateChannelsFromLightModel(this, forceUpdate);
            LOGGER.debug("{}: ShellyLightModel(apiIndex:{}, groupSuffix:{}) => released ({}modified)",
                    handler.thingName, apiLightIndex, channelGroupSuffix, isDirty() ? "" : "un");
            return updated;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Always returns true as all light models support brightness channels.
     * Used to align with the semantics of the other supportsXyz methods.
     * 
     * @return true as such channels are always supported.
     */
    public boolean supportsBrightnessChannel() {
        return true;
    }

    /**
     * Returns true if the light model supports color channels (RGB or RGBW), false otherwise.
     * In case of multiple profile devices, the model id is used to refine the check.
     *
     * @return true if such channels are supported, false otherwise.
     */
    public boolean supportsColorChannel() {
        return (
        // @formatter:off
           (isBulb) ||
           (isDuoRGBW) ||
           (isRGBW2Color) || 
           (isG3FullColorBulb) ||
           (isProfileRGB) ||
           (isProfileRGBW) ||
           (isProfileRGBCCT && channelGroupSuffix == 0) || 
           (isProfileRGBX2LIGHT && channelGroupSuffix == 0)
        // @formatter:on
        );
    }

    /**
     * Returns true if the light model supports color temperature channels, false otherwise.
     * 
     * @return true if such channels are supported, false otherwise.
     */
    public boolean supportsColorTempChannel() {
        return (
        // @formatter:off
            (isDuo && !isVintage) || // Vintage bulb is white-only!
            (isBulb) ||
            (isG3ColorTempBulb) || 
            (isG3FullColorBulb) || 
            (isProfileCCTX2) ||
            (isProfileRGBCCT && channelGroupSuffix > 0) 
        // @formatter:on
        );
    }

    /**
     * Returns true if the light model supports effect channels, false otherwise.
     *
     * @return true if such channels are supported, false otherwise.
     */
    public boolean supportsEffectChannel() {
        return supportsColorChannel() && !isGen2;
    }

    /**
     * Returns true if the light model supports gain channels, false otherwise.
     * 
     * NOTE: the gain channel valid when devices are in COLOR mode, and the brightness
     * channel is valid when devices are in WHITE mode.
     *
     * @return true if such channels are supported, false otherwise.
     */
    public boolean supportsGainChannel() {
        return supportsColorChannel() && !isGen2;
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
            (isDuo && !isGen2) || 
            (isBulb) ||
            (isRGBW2Color) ||
            (isProfileRGB) ||
            (isProfileRGBW) ||
            (isProfileRGBCCT && channelGroupSuffix == 0) || 
            (isProfileRGBX2LIGHT && channelGroupSuffix == 0)
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
            (isRGBW2White) ||
            (isProfileCCTX2) ||
            (isProfileRGBCCT && channelGroupSuffix > 0) || 
            (isProfileLIGHT) ||
            (isProfileRGBX2LIGHT && channelGroupSuffix > 0) ||
            (isG3ColorTempBulb) ||
            (isG3FullColorBulb)
        // @formatter:on
        ;
    }
}
