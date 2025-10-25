/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.homekit.internal.temporary;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.core.util.ColorUtil;

/**
 * NOTE: This class is a temporary copy of the proposed OH Core Light Model. It is introduced here as a proof
 * of concept until such time as the OH Core Light Model is available to be used directly.
 *
 * The {@link LightModel} provides a state machine model for maintaining and modifying the state of a light,
 * which is intended to be used within the Thing Handler of a lighting binding.
 * <p>
 *
 * It supports lights with different capabilities, including:
 * <ul>
 * <li>On/Off only</li>
 * <li>On/Off with Brightness</li>
 * <li>On/Off with Brightness and Color Temperature</li>
 * <li>On/Off with Brightness and Color (HSB, RGB, or CIE XY)</li>
 * <li>On/Off with Brightness, Color Temperature, and Color</li>
 * </ul>
 * It maintains an internal representation of the state of the light.
 * It provides methods to handle commands from openHAB and to update the state from the remote light.
 * It also provides configuration methods to set the capabilities and parameters of the light.
 * The state machine maintains a consistent state, ensuring that the On/Off state is derived from the
 * brightness, and that the color temperature and color are only set if the capabilities are supported.
 * It also provides utility methods to convert between different color representations.
 * <p>
 * See also {@link ColorUtil} for other color conversions.
 * <p>
 * To use the model you must initialize the {@link #lightCapabilities} during initialization as follows:
 * <ul>
 * <li>ON_OFF: if the light is on-off only.</li>
 * <li>BRIGHTNESS: if the light is on-off with brightness.</li>
 * <li>BRIGHTNESS_WITH_COLOR_TEMPERATURE: if the light is on-off with color temperature control.</li>
 * <li>COLOR: if the light is on-off with brightness and full and color control.</li>
 * <li>COLOR_WITH_COLOR_TEMPERATURE: if the light is on-off with brightness, full color, and color temperature
 * control.</li>
 * </ul>
 * Also set {@link #rgbDataType} to the chosen RGB data type RGB, RGBW, RGBCW etc.
 * And optionally set the following configuration parameters:
 * <ul>
 * <li>Optionally override {@link #minimumOnBrightness} to a minimum brightness percent in the range [0.1..10.0]
 * percent, to consider as being "ON". The default is 1 percent.</li>
 * <li>Optionally override {@link #mirekControlWarmest} to a 'warmest' white color temperature in the range
 * [{@link #mirekControlCoolest}..1000.0] Mirek/Mired. The default is 500 Mirek/Mired.</li>
 * <li>Optionally override {@link #mirekControlCoolest} to a 'coolest' white color temperature in the range
 * [100.0.. {@link #mirekControlWarmest}] Mirek/Mired. The default is 153 Mirek/Mired.</li>
 * <li>Optionally override {@link #stepSize} to a step size for the IncreaseDecreaseType commands in the range
 * [1.0..50.0] percent. The default is 10.0 percent.</li>
 * </ul>
 * <p>
 * The model specifically handles the following "exotic" cases:
 * <ol>
 * <li>It handles inter relationships between the brightness PercentType state, the 'B' part of the HSBType state, and
 * the OnOffType state. Where if the brightness goes below the configured {@link #minimumOnBrightness} level the on/off
 * state changes from ON to OFF, and the brightness is clamped to 0%. And analogously if the on/off state changes from
 * OFF to ON, the brightness changes from 0% to its last non zero value.</li>
 * <li>It handles IncreaseDecreaseType commands to change the brightness up or down by the configured
 * {@link #stepSize}, and ensures that the brightness is clamped in the range [0%..100%].</li>
 * <li>It handles both color temperature PercentType states and QuantityType states (which may be either in Mirek/Mired
 * or Kelvin). Where color temperature PercentType values are internally converted to Mirek/Mired values on the
 * percentage scale between the configured {@link #mirekControlCoolest} and {@link #mirekControlWarmest} Mirek/Mired
 * values, and vice versa.</li>
 * <li>When the color temperature changes then the HS values are adapted to match the corresponding color temperature
 * point on the Planckian Locus in the CIE color chart.</li>
 * <li>It handles input/output values in RGB format in the range [0..255]. The behavior depends on the
 * {@link #rgbDataType} setting. If {@link #rgbDataType} is DEFAULT the RGB values read/write all three parts of the
 * HSBType state. Whereas if it is {@link #rgbDataType} is RGB_NO_BRIGHTNESS the RGB values read/write only
 * the 'HS' parts. NOTE: in the latter case, a 'setRGBx()' call followed by a 'getRGBx()' call do not necessarily return
 * the same values, since the values are normalized to 100%. Neverthless the ratios between the RGB values do remain
 * unchanged.</li>
 * <li>If {@link #rgbDataType} is RGB_W it handles values in RGBW format. The behavior is similar to the RGB case above
 * except that the white channel is derived from the lowest of the RGB values.</li>
 * <li>If {@link #rgbDataType} is RGB_C_W it handles values in RGBCW format. The behavior is similar to the RGBW case
 * above except that the white channel is derived from the RGB values by a custom algorithm.</li>
 * </ol>
 * <p>
 * A typical use case is within in a ThingHandler as follows:
 *
 * <pre>
 * {@code
 * public class LightModelHandler extends BaseThingHandler {
 *
 *     // initialize the light model with default capabilities and parameters
 *     private final LightModel model = new LightModel();
 *
 *     &#64;Override
 *     public void initialize() {
 *       // Set up the light state machine capabilities.
 *       model.configSetLightCapabilities(LightCapabilities.COLOR_WITH_COLOR_TEMPERATURE);
 *
 *       // Optionally: set up the light state machine configuration parameters.
 *       // These would typically be read from the thing configuration or read from the remote device.
 *       model.configSetRgbDataType(RgbDataType.RGB_NO_BRIGHTNESS); // RGB data type
 *       model.configSetMinimumOnBrightness(2); // minimum brightness level when on 2%
 *       model.configSetIncreaseDecreaseStep(10); // step size for increase/decrease commands
 *       model.configSetMirekControlCoolest(153); // color temperature control range
 *       model.configSetMirekControlWarmest(500); // color temperature control range
 *
 *       // Optionally: if the light has warm and cool white LEDS then set up their LED color temperatures.
 *       // These would typically be read from the thing configuration or read from the remote device.
 *       model.configSetMirekCoolWhiteLED(153);
 *       model.configSetMirekWarmWhiteLED(500);
 *
 *       // now set the status to UNKNOWN to indicate that we are initialized
 *       updateStatus(ThingStatus.UNKNOWN);
 *     }
 *
 *     &#64;Override
 *     public void handleCommand(ChannelUID channelUID, Command command) {
 *         // update the model state based on a command from OpenHAB
 *         model.handleCommand(command);
 *
 *         // or if it is a color temperature command
 *         model.handleColorTemperatureCommand(command);
 *
 *         sendBindingSpecificCommandToUpdateRemoteLight(
 *              .. model.getOnOff() or
 *              .. model.getBrightness() or
 *              .. model.getColor() or
 *              .. model.getColorTemperature() or
 *              .. model.getColorTemperaturePercent() or
 *              .. model.getRGBx() or
 *              .. model.getXY() or
 *         );
 *     }
 *
 *     // method that sends the updated state data to the remote light
 *     private void sendBindingSpecificCommandToUpdateRemoteLight(..) {
 *       // binding specific code
 *     }
 *
 *     // method that receives data from remote light, and updates the model, and then OH
 *     private void receiveBindingSpecificDataFromRemoteLight(double... receivedData) {
 *         // update the model state based on the data received from the remote
 *         model.setBrightness(receivedData[0]);
 *         model.setRGBx(receivedData[1], receivedData[2], receivedData[3]);
 *         model.setMirek(receivedData[4]);
 *
 *         // update the OH channels with the new state values
 *         updateState(onOffChannelUID, model.getOnOff());
 *         updateState(brightnessChannelUID, model.getBrightness());
 *         updateState(colorChannelUID, model.getColor());
 *         updateState(colorTemperatureChannelUID, model.getColorTemperature());
 *     }
 * }
 * }
 * </pre>
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class LightModel {

    /*********************************************************************************
     * SECTION: Common Enumerators for light capabilities.
     *********************************************************************************/

    /**
     * Enum for the capabilities of different types of lights
     * <p>
     * Different brands of light support different capabilities. Some only support on-off, some support
     * brightness, some support color temperature, and some support full color. This enum
     * defines the different combinations of capabilities that a light may support.
     */
    public static enum LightCapabilities {
        ON_OFF, // on-off only
        BRIGHTNESS, // on-off with brightness
        BRIGHTNESS_WITH_COLOR_TEMPERATURE, // on-off with brightness and color temperature
        COLOR, // on-off with brightness and color
        COLOR_WITH_COLOR_TEMPERATURE; // on-off with brightness, color and color temperature

        public boolean supportsBrightness() {
            return this != ON_OFF;
        }

        public boolean supportsColor() {
            return this == COLOR || this == COLOR_WITH_COLOR_TEMPERATURE;
        }

        public boolean supportsColorTemperature() {
            return this == BRIGHTNESS_WITH_COLOR_TEMPERATURE || this == COLOR_WITH_COLOR_TEMPERATURE;
        }
    }

    /**
     * Enum for the different types of RGB data
     * <p>
     * Different brands of light use different types of RGB data. Some only support plain RGB, some support RGB
     * with a single white channel, and some support RGB with both cold and warm white channels. Also some lights
     * use their RGBx values to represent only the hue and saturation (only the HS parts), and they have another
     * separate control channel for the brightness (B part). Whereby others use the RGBx values to represent the
     * hue, saturation and brightness all together (all the HSB parts).
     */
    public static enum RgbDataType {
        DEFAULT, // supports plain RGB with brightness (i.e. full HSBType)
        RGB_NO_BRIGHTNESS, // supports plain RGB but ignores brightness (i.e. only HS parts of HSBType)
        RGB_W, // supports 4-element RGB with white channel
        RGB_C_W // supports 5-element RGB with cold and warm white channels
    }

    /**
     * Enum for the LED operating mode
     * <p>
     * Some brands of light are not able to use the RGB leds and the white led(s) at the same time. So they must
     * be switched between WHITE_ONLY and RGB_ONLY mode. Whereas others lights can use any combination of RGB and
     * White leds at the same time they must be switched COMBINED mode. If the mode is changed at runtime then the
     * color and/or color temperature are updated to be consistent with the new mode, while keeping the brightness
     * the same. If the light does not support color then the mode is forced to WHITE_ONLY.
     */
    public static enum LedOperatingMode {
        RGB_ONLY, // operating with RGB LEDs only
        COMBINED, // operating with RGB and white LEDs together
        WHITE_ONLY // operating with white LED(s) only
    }

    /*********************************************************************************
     * SECTION: Default Parameters. May be modified during initialization.
     *********************************************************************************/

    /**
     * Minimum brightness percent to consider as light "ON"
     */
    private double minimumOnBrightness = 1.0;

    /**
     * The 'coolest' white color temperature in Mirek/Mired
     */
    private double mirekControlCoolest = 153;

    /**
     * The 'warmest' white color temperature in Mirek/Mired
     */
    private double mirekControlWarmest = 500;

    /*
     * Step size for IncreaseDecreaseType commands
     */
    private double stepSize = 10.0; // step size for IncreaseDecreaseType commands

    /*********************************************************************************
     * SECTION: Capabilities. May be modified during initialization.
     *********************************************************************************/

    /**
     * The capabilities supported by the light
     */
    private LightCapabilities lightCapabilities = LightCapabilities.COLOR_WITH_COLOR_TEMPERATURE;

    /**
     * The RGB data type supported
     */
    private RgbDataType rgbDataType = RgbDataType.DEFAULT;

    /**
     * The capabilities of the cool white LED
     */
    private WhiteLED coolWhiteLed = new WhiteLED(mirekControlCoolest);

    /**
     * The capabilities of warm white LED
     */
    private WhiteLED warmWhiteLed = new WhiteLED(mirekControlWarmest);

    /*********************************************************************************
     * SECTION: Light state variables. Used at run time only.
     *********************************************************************************/

    /**
     * Cached Brightness state, never null
     */
    private PercentType cachedBrightness = PercentType.ZERO;

    /**
     * Cached Color state, never null
     */
    private HSBType cachedHSB = new HSBType();

    /**
     * Cached Mirek/Mired state, may be NaN if not (yet) known
     */
    private double cachedMirek = Double.NaN;

    /**
     * Cached OnOff state, may be null if not (yet) known
     */
    private @Nullable OnOffType cachedOnOff = null;

    /**
     * The current operating mode of the light, default is WHITE only
     */
    private LedOperatingMode ledOperatingMode = LedOperatingMode.WHITE_ONLY;

    /*********************************************************************************
     * SECTION: Constructors
     *********************************************************************************/

    /**
     * Create a {@link LightModel} with default capabilities and parameters as follows:
     * <ul>
     * <li>{@link #lightCapabilities} is COLOR_WITH_COLOR_TEMPERATURE (the light supports brightness control, color
     * control, and color temperature control)</li>
     * <li>{@link #rgbDataType} is DEFAULT (the light supports plain RGB)</li>
     * <li>{@link #minimumOnBrightness} is 1.0 (the minimum brightness percent to consider as light "ON")</li>
     * <li>{@link #mirekControlCoolest} is 153 (the 'coolest' white color temperature)</li>
     * <li>{@link #mirekControlWarmest} is 500 (the 'warmest' white color temperature)</li>
     * <li>{@link #stepSize} is 10.0 (the step size for IncreaseDecreaseType commands)</li>
     * <li>coolWhiteLedMirek is 153 Mirek/Mired (the color temperature of the cool white LED)</li>
     * <li>warmWhiteLedMirek is 500 Mirek/Mired (the color temperature of the warm white LED)</li>
     * </ul>
     */
    public LightModel() {
        this(LightCapabilities.COLOR_WITH_COLOR_TEMPERATURE, RgbDataType.DEFAULT, null, null, null, null, null, null);
    }

    /**
     * Create a {@link LightModel} with the given capabilities. The parameters are set to the default.
     *
     * @param lightCapabilities the capabilities of the light
     * @param rgbDataType the type of RGB data used
     */
    public LightModel(LightCapabilities lightCapabilities, RgbDataType rgbDataType) {
        this(lightCapabilities, rgbDataType, null, null, null, null, null, null);
    }

    /**
     * Create a {@link LightModel} with the given capabilities and parameters. The parameters can be
     * null to use the default.
     *
     * @param lightCapabilities the capabilities of the light
     * @param rgbDataType the type of RGB data supported
     * @param minimumOnBrightness the minimum brightness percent to consider as light "ON"
     * @param mirekControlCoolest the 'coolest' white color temperature control value in Mirek/Mired
     * @param mirekControlWarmest the 'warmest' white color temperature control value in Mirek/Mired
     * @param stepSize the step size for IncreaseDecreaseType commands
     * @param coolWhiteLedMirek the color temperature of the cool white LED
     * @param warmWhiteLedMirek the color temperature of the warm white LED
     * @throws IllegalArgumentException if any of the parameters are out of range
     */
    public LightModel(LightCapabilities lightCapabilities, RgbDataType rgbDataType,
            @Nullable Double minimumOnBrightness, @Nullable Double mirekControlCoolest,
            @Nullable Double mirekControlWarmest, @Nullable Double stepSize, @Nullable Double coolWhiteLedMirek,
            @Nullable Double warmWhiteLedMirek) throws IllegalArgumentException {
        configSetLightCapabilities(lightCapabilities);
        configSetRgbDataType(rgbDataType);
        if (minimumOnBrightness != null) {
            configSetMinimumOnBrightness(minimumOnBrightness);
        }
        if (mirekControlWarmest != null) {
            configSetMirekControlWarmest(mirekControlWarmest);
        }
        if (mirekControlCoolest != null) {
            configSetMirekControlCoolest(mirekControlCoolest);
        }
        if (stepSize != null) {
            configSetIncreaseDecreaseStep(stepSize);
        }
        if (coolWhiteLedMirek != null) {
            configSetMirekCoolWhiteLED(coolWhiteLedMirek);
        }
        if (warmWhiteLedMirek != null) {
            configSetMirekWarmWhiteLED(warmWhiteLedMirek);
        }
    }

    /*********************************************************************************
     * SECTION: Configuration getters and setters. May be used during initialization.
     *********************************************************************************/

    /**
     * Configuration: get the step size for IncreaseDecreaseType commands.
     */
    public double configGetIncreaseDecreaseStep() {
        return stepSize;
    }

    /**
     * Configuration: get the light capabilities.
     */
    public LightCapabilities configGetLightCapabilities() {
        return lightCapabilities;
    }

    /**
     * Configuration: get the minimum brightness percent to consider as light "ON".
     */
    public double configGetMinimumOnBrightness() {
        return minimumOnBrightness;
    }

    /**
     * Configuration: get the coolest color temperature in Mirek/Mired.
     */
    public double configGetMirekControlCoolest() {
        return mirekControlCoolest;
    }

    /**
     * Configuration: get the color temperature of the cool white LED in Mirek/Mired.
     *
     * @return the color temperature of the cool white LED.
     */
    public double configGetMirekCoolWhiteLed() {
        return coolWhiteLed.getMirek();
    }

    /**
     * Configuration: get the warmest color temperature in Mirek/Mired.
     */
    public double configGetMirekControlWarmest() {
        return mirekControlWarmest;
    }

    /**
     * Configuration: get the color temperature of the warm white LED in Mirek/Mired.
     *
     * @return the color temperature of the warm white LED.
     */
    public double configGetMirekWarmWhiteLed() {
        return warmWhiteLed.getMirek();
    }

    /**
     * Configuration: get the supported RGB data type.
     */
    public RgbDataType configGetRgbDataType() {
        return rgbDataType;
    }

    /**
     * Configuration: set the step size for IncreaseDecreaseType commands.
     *
     * @param stepSize the step size in percent.
     * @throws IllegalArgumentException if the stepSize parameter is out of range.
     */
    public void configSetIncreaseDecreaseStep(double stepSize) throws IllegalArgumentException {
        if (stepSize < 1.0 || stepSize > 50.0) {
            throw new IllegalArgumentException("Step size '%.1f' out of range [1.0..50.0]".formatted(stepSize));
        }
        this.stepSize = stepSize;
    }

    /**
     * Configuration: set the light capabilities.
     */
    public void configSetLightCapabilities(LightCapabilities lightCapabilities) {
        this.lightCapabilities = lightCapabilities;
        switch (lightCapabilities) {
            case COLOR:
                ledOperatingMode = LedOperatingMode.RGB_ONLY;
                break;
            case COLOR_WITH_COLOR_TEMPERATURE:
                ledOperatingMode = LedOperatingMode.COMBINED;
                break;
            default:
                ledOperatingMode = LedOperatingMode.WHITE_ONLY;
        }
    }

    /**
     * Configuration: set the minimum brightness percent to consider as light "ON".
     *
     * @param minimumOnBrightness the minimum brightness percent.
     * @throws IllegalArgumentException if the minimumBrightness parameter is out of range.
     */
    public void configSetMinimumOnBrightness(double minimumOnBrightness) throws IllegalArgumentException {
        if (minimumOnBrightness < 0.1 || minimumOnBrightness > 10.0) {
            throw new IllegalArgumentException(
                    "Minimum brightness '%.1f' out of range [0.1..10.0]".formatted(minimumOnBrightness));
        }
        this.minimumOnBrightness = minimumOnBrightness;
    }

    /**
     * Configuration: set the coolest color temperature in Mirek/Mired.
     *
     * @param mirekControlCoolest the coolest supported color temperature in Mirek/Mired.
     * @throws IllegalArgumentException if the mirekControlCoolest parameter is out of range or not less than
     *             mirekControlWarmest.
     */
    public void configSetMirekControlCoolest(double mirekControlCoolest) throws IllegalArgumentException {
        if (mirekControlCoolest < 100.0 || mirekControlCoolest > 1000.0) {
            throw new IllegalArgumentException(
                    "Coolest Mirek/Mired '%.1f' out of range [100.0..1000.0]".formatted(mirekControlCoolest));
        }
        if (mirekControlWarmest <= mirekControlCoolest) {
            throw new IllegalArgumentException("Warmest Mirek/Mired '%.1f' must be greater than the coolest '%.1f'"
                    .formatted(mirekControlWarmest, mirekControlCoolest));
        }
        this.mirekControlCoolest = mirekControlCoolest;
    }

    /**
     * Configuration: set the warmest color temperature in Mirek/Mired.
     *
     * @param mirekControlWarmest the warmest supported color temperature in Mirek/Mired.
     * @throws IllegalArgumentException if the mirekControlWarmest parameter is out of range or not greater than
     *             mirekControlCoolest.
     */
    public void configSetMirekControlWarmest(double mirekControlWarmest) throws IllegalArgumentException {
        if (mirekControlWarmest < 100.0 || mirekControlWarmest > 1000.0) {
            throw new IllegalArgumentException(
                    "Warmest Mirek/Mired '%.1f' out of range [100.0..1000.0]".formatted(mirekControlWarmest));
        }
        if (mirekControlWarmest <= mirekControlCoolest) {
            throw new IllegalArgumentException("Warmest Mirek/Mired '%.1f' must be greater than coolest '%.1f'"
                    .formatted(mirekControlWarmest, mirekControlCoolest));
        }
        this.mirekControlWarmest = mirekControlWarmest;
    }

    /**
     * Configuration: set the color temperature of the cool white LED, and thus set the weightings of its
     * individual RGB sub- components.
     * <p>
     * NOTE: If the light has a single white LED then both the 'configSetMirekCoolWhiteLED()' and the
     * 'configSetMirekControlWarmest()' methods MUST be called with the identical color temperature.
     *
     * @param coolLedMirek the color temperature in Mirek/Mired of the cool white LED.
     * @throws IllegalArgumentException if the coolLedMirek parameter is out of range.
     */
    public void configSetMirekCoolWhiteLED(double coolLedMirek) throws IllegalArgumentException {
        if (coolLedMirek < 100.0 || coolLedMirek > 1000.0) {
            throw new IllegalArgumentException(
                    "Cool LED Mirek/Mired '%.1f' out of range [100.0..1000.0]".formatted(coolLedMirek));
        }
        coolWhiteLed = new WhiteLED(coolLedMirek);
    }

    /**
     * Configuration: set the color temperature of the warm white LED, and thus set the weightings of its
     * individual RGB sub- components.
     * <p>
     * NOTE: If the light has a single white LED then both the 'configSetMirekCoolWhiteLED()' and the
     * 'configSetMirekControlWarmest()' methods MUST be called with the identical color temperature.
     *
     * @param warmLedMirek the color temperature in Mirek/Mired of the warm white LED.
     */
    public void configSetMirekWarmWhiteLED(double warmLedMirek) {
        if (warmLedMirek < 100.0 || warmLedMirek > 1000.0) {
            throw new IllegalArgumentException(
                    "Warm LED Mirek/Mired '%.1f' out of range [100.0..1000.0]".formatted(warmLedMirek));
        }
        warmWhiteLed = new WhiteLED(warmLedMirek);
    }

    /**
     * Configuration: set the supported RGB type.
     *
     * @param rgbType the supported RGB type.
     */
    public void configSetRgbDataType(RgbDataType rgbType) {
        this.rgbDataType = rgbType;
        switch (rgbType) {
            case DEFAULT:
            case RGB_NO_BRIGHTNESS:
                ledOperatingMode = LedOperatingMode.RGB_ONLY;
            default:
        }
    }

    /*********************************************************************************
     * SECTION: Runtime State getters, setters, and handlers. Only used at runtime.
     *********************************************************************************/

    /**
     * Runtime State: get the brightness or return null if the capability is not supported.
     *
     * @return PercentType, or null if not supported.
     */
    public @Nullable PercentType getBrightness() {
        return getBrightness(false);
    }

    /**
     * Runtime State: get the brightness or return null if the capability is not supported.
     *
     * @param forceChannelVisible if true return a non-null value even when color is supported.
     * @return PercentType, or null if not supported.
     */
    public @Nullable PercentType getBrightness(boolean forceChannelVisible) {
        return lightCapabilities.supportsBrightness() && (!lightCapabilities.supportsColor() || forceChannelVisible)
                ? cachedHSB.getBrightness()
                : null;
    }

    /**
     * Runtime State: get the color or return null if the capability is not supported.
     *
     * @return HSBType, or null if not supported.
     */
    public @Nullable HSBType getColor() {
        return lightCapabilities.supportsColor() ? cachedHSB : null;
    }

    /**
     * Runtime State: get the color temperature or return null if the capability is not supported.
     * or the Mirek/Mired value is not known.
     *
     * @return QuantityType in Kelvin representing the color temperature, or null if not supported
     *         or the Mirek/Mired value is not known.
     */
    public @Nullable QuantityType<?> getColorTemperature() {
        if (lightCapabilities.supportsColorTemperature() && !Double.isNaN(cachedMirek)) {
            return Objects.requireNonNull( // Mired always converts to Kelvin
                    QuantityType.valueOf(cachedMirek, Units.MIRED).toInvertibleUnit(Units.KELVIN));
        }
        return null;
    }

    /**
     * Runtime State: get the color temperature in percent or return null if the capability is not supported
     * or the Mirek/Mired value is not known.
     *
     * @return PercentType in range [0..100] representing [coolest..warmest], or null if not supported
     *         or the Mirek/Mired value is not known.
     */
    public @Nullable PercentType getColorTemperaturePercent() {
        if (lightCapabilities.supportsColorTemperature() && !Double.isNaN(cachedMirek)) {
            double percent = 100 * (cachedMirek - mirekControlCoolest) / (mirekControlWarmest - mirekControlCoolest);
            return new PercentType(new BigDecimal(Math.min(Math.max(percent, 0.0), 100.0)));
        }
        return null;
    }

    /**
     * Runtime State: get the hue in range [0..360].
     *
     * @return double representing the hue in range [0..360].
     */
    public double getHue() {
        return cachedHSB.getHue().doubleValue();
    }

    /**
     * Runtime State: get the HSBType color.
     *
     * @return HSBType representing the color.
     */
    public HSBType getHsb() {
        return new HSBType(cachedHSB.getHue(), cachedHSB.getSaturation(), cachedHSB.getBrightness());
    }

    /**
     * Runtime State: get the color temperature in Mirek/Mired, may be NaN if not known.
     *
     * @return double representing the color temperature in Mirek/Mired.
     */
    public double getMirek() {
        return cachedMirek;
    }

    /**
     * Runtime State: get the on/off state or null if not supported.
     *
     * @return OnOffType representing the on/off state or null if not supported.
     */
    public @Nullable OnOffType getOnOff() {
        return getOnOff(false);
    }

    /**
     * Runtime State: get the on/off state or null if not supported.
     *
     * @param forceChannelVisible if true return a non-null value even if brightness or color are supported.
     * @return OnOffType representing the on/off state or null if not supported.
     */
    public @Nullable OnOffType getOnOff(boolean forceChannelVisible) {
        return (!lightCapabilities.supportsColor() && !lightCapabilities.supportsBrightness()) || forceChannelVisible
                ? OnOffType.from(cachedHSB.getBrightness().doubleValue() >= minimumOnBrightness)
                : null;
    }

    /**
     * Runtime State: get the RGB(C)(W) values as an array of doubles in range [0..255]. Depending on the value of
     * {@link #rgbDataType}, the array length is either 3 (RGB), 4 (RGBW), or 5 (RGBCW). The array is in the order [red,
     * green, blue, (cold-)(white), (warm-white)]. Depending on the value, the brightness may or may not be used as
     * follows:
     *
     * <ul>
     * <li>'RGB_NO_BRIGHTNESS': The return result does not depend on the current brightness. In other words the values
     * only relate to the 'HS' part of the {@link HSBType} state. Note: this means that in this case a round trip of
     * setRGBx() followed by getRGBx() will NOT necessarily contain identical values, although the RGB ratios will
     * certainly be the same.</li>
     *
     * <li>All other values of {@link #rgbDataType}: The return result depends on the current brightness. In other
     * words the values relate to all the 'HSB' parts of the {@link HSBType} state.</li>
     * <ul>
     *
     * @return double[] representing the RGB(C)(W) components in range [0..255.0]
     * @throws IllegalStateException if the RGB data type is not compatible with the current LED operating mode.
     */
    public double[] getRGBx() throws IllegalStateException {
        HSBType hsb = RgbDataType.RGB_NO_BRIGHTNESS == rgbDataType
                ? new HSBType(cachedHSB.getHue(), cachedHSB.getSaturation(), PercentType.HUNDRED)
                : cachedHSB;

        /*
         * In white only mode the RGB values are all zero.
         */
        if (LedOperatingMode.WHITE_ONLY == ledOperatingMode) {

            /*
             * If the light has a single white led then its value is determined by the brightness only.
             */
            if (RgbDataType.RGB_W == rgbDataType) {
                double w = cachedHSB.getBrightness().doubleValue() * 255.0 / 100.0;
                return new double[] { 0.0, 0.0, 0.0, w };
            }

            /*
             * If the light has a warm and a cool white led, the mix of white values are determined
             * by the brightness and the color temperature.
             */
            if (RgbDataType.RGB_C_W == rgbDataType) {
                double ratio = (cachedMirek - coolWhiteLed.getMirek())
                        / (warmWhiteLed.getMirek() + coolWhiteLed.getMirek());
                double bri = cachedHSB.getBrightness().doubleValue() * 255.0 / 100.0;
                double cool = bri * ratio;
                double warm = bri - cool;
                return new double[] { 0.0, 0.0, 0.0, cool, warm };
            }

            throw new IllegalStateException("LED operating mode '%s' not compatible with RGB data type '%s'"
                    .formatted(ledOperatingMode, rgbDataType));
        }

        /*
         * In RGB only mode the RGB values are determined by the HSB values and the white values are always zero.
         */
        if (LedOperatingMode.RGB_ONLY == ledOperatingMode) {

            /*
             * RGB only - convert HSB to RGB, then scale to [0..255] and pad with zeros for white values.
             */
            PercentType[] rgbP = ColorUtil.hsbToRgbPercent(hsb);
            double[] rgb = Arrays.stream(rgbP).mapToDouble(p -> p.doubleValue() * 255.0 / 100.0).toArray();
            if (RgbDataType.RGB_W == rgbDataType) {
                return new double[] { rgb[0], rgb[1], rgb[2], 0 };
            } else if (RgbDataType.RGB_C_W == rgbDataType) {
                return new double[] { rgb[0], rgb[1], rgb[2], 0, 0 };
            }
            return rgb;
        }

        /*
         * In combined mode the RGB and white values are all determined by the HSB values.
         */
        if (LedOperatingMode.COMBINED == ledOperatingMode) {

            /*
             * RGBCW - convert HSB to RGB, normalize it, then convert to RGBCW, then scale to [0..255]
             */
            if (RgbDataType.RGB_C_W == rgbDataType) {
                PercentType[] rgbP = ColorUtil.hsbToRgbPercent(hsb);
                double[] rgb = Arrays.stream(rgbP).mapToDouble(p -> p.doubleValue() / 100.0).toArray();
                double[] rgbcw = RgbcwMath.rgb2rgbcw(rgb, coolWhiteLed.getProfile(), warmWhiteLed.getProfile());
                rgbcw = Arrays.stream(rgbcw).map(d -> Math.round(d * 255 * 10) / 10).toArray(); // // round to 1
                return rgbcw;
            } else

            /*
             * RGBW - convert HSB to RGBW, then scale to [0..255]
             */
            if (RgbDataType.RGB_W == rgbDataType) {
                PercentType[] rgbwP = ColorUtil.hsbToRgbwPercent(hsb);
                double[] rgbw = Arrays.stream(rgbwP).mapToDouble(p -> p.doubleValue() * 255.0 / 100.0).toArray();
                return rgbw;
            }

            /*
             * RGB only - convert HSB to RGB, then scale to [0..255]
             */
            PercentType[] rgbP = ColorUtil.hsbToRgbPercent(hsb);
            double[] rgb = Arrays.stream(rgbP).mapToDouble(p -> p.doubleValue() * 255.0 / 100.0).toArray();
            return rgb;
        }

        throw new IllegalStateException("Unknown LED operating mode '%s'".formatted(ledOperatingMode));
    }

    /**
     * Runtime State: get the saturation in range [0..100].
     *
     * @return double representing the saturation in range [0..100].
     */
    public double getSaturation() {
        return cachedHSB.getSaturation().doubleValue();
    }

    /**
     * Runtime State: get the CIE XY values as an array of doubles in range [0.0..1.0].
     *
     * @return double[] representing the XY components in range [0.0..1.0].
     */
    public double[] getXY() {
        return ColorUtil.hsbToXY(new HSBType(cachedHSB.getHue(), cachedHSB.getSaturation(), PercentType.HUNDRED));
    }

    /**
     * Runtime State: handle a command to change the light's color temperature state. Commands may be one of:
     * <ul>
     * <li>{@link PercentType} for color temperature setting.</li>
     * <li>{@link QuantityType} for color temperature setting.</li>
     * </ul>
     * Other commands are deferred to {@link #handleCommand(Command)} for processing just-in-case.
     *
     * @param command the command to handle.
     * @throws IllegalArgumentException if the command type is not supported.
     */
    public void handleColorTemperatureCommand(Command command) throws IllegalArgumentException {
        if (command instanceof PercentType warmness) {
            zHandleColorTemperature(warmness);
        } else if (command instanceof QuantityType<?> temperature) {
            zHandleColorTemperature(temperature);
        } else {
            // defer to the main handler for other command types just-in-case
            handleCommand(command);
        }
    }

    /**
     * Runtime State: handle a command to change the light's state. Commands may be one of:
     * <ul>
     * <li>{@link HSBType} for color setting</li>
     * <li>{@link PercentType} for brightness setting</li>
     * <li>{@link OnOffType} for on/off state setting</li>
     * <li>{@link IncreaseDecreaseType} for brightness up/down setting</li>
     * <li>{@link QuantityType} for color temperature setting</li>
     * </ul>
     *
     * @param command the command to handle.
     * @throws IllegalArgumentException if the command type is not supported.
     */
    public void handleCommand(Command command) throws IllegalArgumentException {
        if (command instanceof HSBType color) {
            zHandleHSBType(color);
        } else if (command instanceof PercentType brightness) {
            zHandleBrightness(brightness);
        } else if (command instanceof OnOffType onOff) {
            zHandleOnOff(onOff);
        } else if (command instanceof IncreaseDecreaseType incDec) {
            zHandleIncreaseDecrease(incDec);
        } else if (command instanceof QuantityType<?> temperature) {
            zHandleColorTemperature(temperature);
        } else {
            throw new IllegalArgumentException(
                    "Command '%s' not supported for light states".formatted(command.getClass().getName()));
        }
    }

    /**
     * Runtime State: update the brightness from the remote light, ensuring it is in the range [0.0..100.0]
     *
     * @param brightness in the range [0..100]
     * @throws IllegalArgumentException if the value is outside the range [0.0 to 100.0]
     */
    public void setBrightness(double brightness) throws IllegalArgumentException {
        zHandleBrightness(zPercentTypeFrom(brightness));
    }

    /**
     * Runtime State: Set the current LED operating mode. Some brands of light are not able to use the RGB leds
     * and the white led(s) at the same time. So they must be switched between WHITE_ONLY and RGB_ONLY mode.
     * Whereas others lights can use any combination of RGB and White leds at the same time they must be switched
     * COMBINED mode. If the mode is changed at runtime then the color and/or color temperature are updated to be
     * consistent with the new mode, while keeping the brightness the same. If the light does not support color
     * then the mode is forced to WHITE_ONLY.
     */
    public void setLedOperatingMode(LedOperatingMode newOperatingMode) {
        switch (lightCapabilities) {
            case COLOR:
            case COLOR_WITH_COLOR_TEMPERATURE:
                // only change things if different
                if (ledOperatingMode != newOperatingMode) {
                    ledOperatingMode = newOperatingMode;
                    double newMirek;
                    switch (newOperatingMode) {
                        case RGB_ONLY:
                            /*
                             * Force the color to the point on the Planckian locus that corresponds to the color
                             * temperature. This ensures that the color changes to one that is consistent with the
                             * prior color temperature. Keeps the original brightness.
                             */
                            newMirek = Double.isNaN(cachedMirek) ? 250 : cachedMirek; // default to 4000 K
                            break;
                        case WHITE_ONLY:
                            /*
                             * Go to the XY point on the Planckian locus that is closest to the existing color, and
                             * set the color temperature to the corresponding Mirek/Mired value. Keeps the original
                             * brightness.
                             */
                            HSBType oldHsb = new HSBType(cachedHSB.getHue(), cachedHSB.getSaturation(),
                                    PercentType.HUNDRED);
                            double[] xyY = ColorUtil.hsbToXY(oldHsb);
                            newMirek = 1000000 / ColorUtil.xyToKelvin(new double[] { xyY[0], xyY[1] });
                            break;
                        case COMBINED: // no change - fall through
                        default:
                            return;
                    }
                    setMirek(newMirek);
                }
                break;
            default:
                this.ledOperatingMode = LedOperatingMode.WHITE_ONLY; // force to WHITE mode
        }
    }

    /**
     * Runtime State: update the hue from the remote light, ensuring it is in the range [0.0..360.0]
     *
     * @param hue in the range [0..360]
     * @throws IllegalArgumentException if the hue parameter is not in the range 0.0 to 360.0
     */
    public void setHue(double hue) throws IllegalArgumentException {
        HSBType hsb = new HSBType(new DecimalType(hue), cachedHSB.getSaturation(), cachedHSB.getBrightness());
        cachedHSB = hsb;
        cachedMirek = zMirekFrom(hsb);
    }

    /**
     * Runtime State: update the Mirek/Mired color temperature from the remote light, and update the cached HSB color
     * accordingly. Constrain the Mirek/Mired value to be within the warmest and coolest limits. If the Mirek/Mired
     * value is NaN then the cached color is not updated as we cannot determine what it should be.
     *
     * @param mirek the color temperature in Mirek/Mired or NaN if not known.
     * @throws IllegalArgumentException if the mirek parameter is not in the range
     *             [mirekControlCoolest..mirekControlWarmest]
     */
    public void setMirek(double mirek) throws IllegalArgumentException {
        if (mirek < mirekControlCoolest || mirek > mirekControlWarmest) { // NaN is not < or > anything // anything
            throw new IllegalArgumentException("Mirek/Mired value '%.1f' out of range [%.1f..%.1f]".formatted(mirek,
                    mirekControlCoolest, mirekControlWarmest));
        }
        if (!Double.isNaN(mirek)) { // don't update color if Mirek/Mired is not known
            HSBType hsb = ColorUtil.xyToHsb(ColorUtil.kelvinToXY(1000000 / mirek));
            cachedHSB = new HSBType(hsb.getHue(), hsb.getSaturation(), cachedHSB.getBrightness());
        }
        cachedMirek = mirek;
    }

    /**
     * Runtime State: update the on/off state from the remote light.
     *
     * @param on true for ON, false for OFF
     */
    public void setOnOff(boolean on) {
        zHandleOnOff(OnOffType.from(on));
    }

    /**
     * Runtime State: update the color with RGB(C)(W) fields from the remote light, and update the cached HSB color
     * accordingly. The array must be in the order [red, green, blue, (cold-)(white), (warm-white)]. If white is
     * present but the light does not support white channel(s) then IllegalArgumentException is thrown. Depending
     * on the value of {@link #rgbDataType} the brightness may or may not change as follows:
     *
     * <ul>
     * <li>'RGB_NO_BRIGHTNESS' both [255,0,0] and [127.5,0,0] change the color to RED without a change in brightness.
     * In other words the values only relate to the 'HS' part of the {@link HSBType} state. Note: this means that in
     * this case a round trip of 'setRGBx()' followed by 'getRGBx()' will NOT necessarily contain identical values,
     * although the RGB ratios will certainly be the same.</li>
     *
     * <li>All other values of {@link #rgbDataType}: both [255,0,0] and [127.5,0,0] change the color to RED and the
     * former changes the brightness to 100 percent, whereas the latter changes it to 50 percent. In other words the
     * values relate to all the 'HSB' parts of the {@link HSBType} state.</li>
     * <ul>
     *
     * @param rgbxParameter an array of double representing RGB or RGBW values in range [0.0..255.0]
     * @throws IllegalArgumentException if the array length is not 3, 4, or 5 depending on the light's capabilities,
     *             or if any of the values are outside the range [0.0 to 255.0]
     */
    public void setRGBx(double[] rgbxParameter) throws IllegalArgumentException {
        if (rgbxParameter.length > 5) {
            throw new IllegalArgumentException("Too many arguments in RGBx array");
        }
        if (rgbxParameter.length < 3 || (RgbDataType.RGB_W == rgbDataType && rgbxParameter.length < 4)
                || (RgbDataType.RGB_C_W == rgbDataType && rgbxParameter.length < 5)) {
            throw new IllegalArgumentException("Too few arguments in RGBx array");
        }
        if (rgbxParameter.length == 3 && ledOperatingMode != LedOperatingMode.RGB_ONLY) {
            throw new IllegalArgumentException("White channel(s) mandatory in LED mode " + ledOperatingMode);
        }
        if (rgbxParameter.length > 3 && ledOperatingMode == LedOperatingMode.RGB_ONLY) {
            throw new IllegalArgumentException("White channel(s) not allowed in LED mode " + ledOperatingMode);
        }
        if (Arrays.stream(rgbxParameter).anyMatch(d -> d < 0.0 || d > 255.0)) {
            throw new IllegalArgumentException("RGBx value out of range [0.0..255.0]");
        }

        HSBType hsb;
        PercentType brightness;
        switch (ledOperatingMode) {
            case WHITE_ONLY:
                double white;
                double mirek;
                if (rgbxParameter.length == 5) {
                    /*
                     * We have both a C and a W channel so we create a pure white whose brightness
                     * is determined by both white channels averaged. And the color temperature is
                     * determined by the ratio of the two white channels.
                     */
                    white = (rgbxParameter[3] + rgbxParameter[4]) / 2.0;
                    mirek = (coolWhiteLed.getMirek() * rgbxParameter[3] / white)
                            + (warmWhiteLed.getMirek() * rgbxParameter[4] / white);
                } else {
                    /*
                     * At this point the rgbxParameter.length can only be 4 so we create a white
                     * with brightness from the single white channel. And the color temperature
                     * is determined by the average of the two white LEDs. This is the same as
                     * having a single white LED with a color temperature equal to the average of
                     * the two LED temps.
                     */
                    white = rgbxParameter[3];
                    mirek = (coolWhiteLed.getMirek() + warmWhiteLed.getMirek()) / 2.0; // average of the two LEDs
                }
                hsb = ColorUtil.xyToHsb(ColorUtil.kelvinToXY(1000000 / mirek));
                hsb = new HSBType(hsb.getHue(), hsb.getSaturation(), PercentType.HUNDRED);
                brightness = zPercentTypeFrom(white * 100.0 / 255.0);
                break;

            case RGB_ONLY:
                /*
                 * If we got to this point the rgbxParameter.length can only have the value 3,
                 * otherwise an exception would have been thrown in the size checks above, so
                 * we can treat it the same as the COMBINED mode case.
                 */
                if (rgbxParameter.length != 3) {
                    return; // safe coding but will never happen
                }
                // fall through to COMBINED

            case COMBINED:
                double[] rgbx;
                if (RgbDataType.RGB_C_W == rgbDataType) {
                    // RGBCW - normalize, convert to RGB, then scale back to [0..255]
                    rgbx = Arrays.stream(rgbxParameter).map(d -> d / 255.0).toArray();
                    rgbx = RgbcwMath.rgbcw2rgb(rgbx, coolWhiteLed.getProfile(), warmWhiteLed.getProfile());
                    rgbx = Arrays.stream(rgbx).map(d -> Math.round(d * 255 * 10) / 10).toArray(); // round to 0.1
                } else {
                    // RGB or RGBW - pass through RGB(W) values unchanged
                    rgbx = rgbxParameter;
                }

                hsb = ColorUtil.rgbToHsb(Arrays.stream(rgbx).map(d -> d * 100.0 / 255.0)
                        .mapToObj(d -> zPercentTypeFrom(d)).toArray(PercentType[]::new));

                brightness = hsb.getBrightness();
                if (RgbDataType.RGB_NO_BRIGHTNESS == rgbDataType) {
                    hsb = new HSBType(hsb.getHue(), hsb.getSaturation(), cachedHSB.getBrightness());
                }
                break;

            default:
                return; // safe coding but will never happen
        }

        cachedHSB = hsb;
        cachedMirek = zMirekFrom(hsb);
        if (RgbDataType.RGB_NO_BRIGHTNESS == rgbDataType) {
            zHandleBrightness(brightness);
        }
    }

    /**
     * Runtime State: update the saturation from the remote light, ensuring it is in the range [0.0..100.0]
     *
     * @param saturation in the range [0..100]
     * @throws IllegalArgumentException if the value is outside the range [0.0..100.0]
     */
    public void setSaturation(double saturation) throws IllegalArgumentException {
        HSBType hsb = new HSBType(cachedHSB.getHue(), zPercentTypeFrom(saturation), cachedHSB.getBrightness());
        cachedHSB = hsb;
        cachedMirek = zMirekFrom(hsb);
    }

    /**
     * Runtime State: update the color with CIE XY fields from the remote light, and update the cached HSB color
     * accordingly.
     *
     * @param x the x field in range [0.0..1.0]
     * @param y the y field in range [0.0..1.0]
     * @throws IllegalArgumentException if any of the XY values are out of range [0.0..1.0]
     */
    public void setXY(double x, double y) throws IllegalArgumentException {
        double[] xy = new double[] { x, y };
        HSBType hsb = ColorUtil.xyToHsb(xy);
        cachedHSB = new HSBType(hsb.getHue(), hsb.getSaturation(), cachedHSB.getBrightness());
        cachedMirek = 1000000 / ColorUtil.xyToKelvin(xy);
    }

    /**
     * Runtime State: convert a nullable State to a non-null State, using {@link UnDefType}.UNDEF if the input is null.
     * <p>
     * {@code State state = xyz.toNonNull(xyz.getColor())} is a common usage.
     *
     * @param state the input State, which may be null.
     * @return the input State if it is not null, otherwise 'UnDefType.UNDEF'.
     */
    public State toNonNull(@Nullable State state) {
        return state != null ? state : UnDefType.UNDEF;
    }

    /**
     * Runtime State: create and return a copy of this LightModel. The copy has the same configuration and
     * runtime state as this instance.
     *
     * @return a copy of this LightModel.
     */
    public LightModel copy() {
        OnOffType tempOnOff = cachedOnOff;
        LightModel copy = new LightModel(lightCapabilities, rgbDataType, minimumOnBrightness, mirekControlCoolest,
                mirekControlWarmest, stepSize, coolWhiteLed.getMirek(), warmWhiteLed.getMirek());
        copy.cachedBrightness = PercentType.valueOf(cachedBrightness.toFullString());
        copy.cachedHSB = HSBType.valueOf(cachedHSB.toFullString());
        copy.cachedMirek = cachedMirek;
        copy.cachedOnOff = tempOnOff == null ? null : OnOffType.valueOf(tempOnOff.toFullString());
        copy.ledOperatingMode = ledOperatingMode;
        return copy;
    }

    /*********************************************************************************
     * SECTION: Internal private methods. Names have 'z' prefix to indicate private.
     *********************************************************************************/

    /**
     * Internal: handle a write brightness command from OH core.
     *
     * @param brightness the brightness {@link PercentType} to set.
     */
    private void zHandleBrightness(PercentType brightness) {
        if (brightness.doubleValue() >= minimumOnBrightness) {
            cachedBrightness = brightness;
            cachedHSB = new HSBType(cachedHSB.getHue(), cachedHSB.getSaturation(), brightness);
            cachedOnOff = OnOffType.ON;
        } else {
            if (OnOffType.ON == cachedOnOff) {
                cachedBrightness = cachedHSB.getBrightness(); // cache the last 'ON' state brightness
            }
            cachedHSB = new HSBType(cachedHSB.getHue(), cachedHSB.getSaturation(), PercentType.ZERO);
            cachedOnOff = OnOffType.OFF;
        }
    }

    /**
     * Internal: handle a write color temperature command from OH core.
     *
     * @param warmness the color temperature warmness {@link PercentType} to set.
     */
    private void zHandleColorTemperature(PercentType warmness) {
        setMirek(mirekControlCoolest + ((mirekControlWarmest - mirekControlCoolest) * warmness.doubleValue() / 100.0));
    }

    /**
     * Internal: handle a write color temperature command from OH core.
     *
     * @param colorTemperature the color temperature {@link QuantityType} to set.
     * @throws IllegalArgumentException if the colorTemperature parameter is not convertible to Mired.
     */
    private void zHandleColorTemperature(QuantityType<?> colorTemperature) throws IllegalArgumentException {
        QuantityType<?> mirek = colorTemperature.toInvertibleUnit(Units.MIRED);
        if (mirek == null) {
            throw new IllegalArgumentException(
                    "Parameter '%s' not convertible to Mirek/Mired".formatted(colorTemperature.toFullString()));
        }
        setMirek(mirek.doubleValue());
    }

    /**
     * Internal: handle a write color command from OH core.
     *
     * @param hsb the color {@link HSBType} to set.
     */
    private void zHandleHSBType(HSBType hsb) {
        cachedHSB = hsb;
        zHandleBrightness(hsb.getBrightness());
        cachedMirek = zMirekFrom(hsb);
    }

    /**
     * Internal: handle a write increase/decrease command from OH core, ensuring it is in the range [0.0..100.0]
     *
     * @param increaseDecrease the {@link IncreaseDecreaseType} command.
     */
    private void zHandleIncreaseDecrease(IncreaseDecreaseType increaseDecrease) {
        double bri = Math.min(Math.max(cachedHSB.getBrightness().doubleValue()
                + ((IncreaseDecreaseType.INCREASE == increaseDecrease ? 1 : -1) * stepSize), 0.0), 100.0);
        setBrightness(bri);
    }

    /**
     * Internal: handle a write on/off command from OH core.
     *
     * @param onOff the {@link OnOffType} command.
     */
    private void zHandleOnOff(OnOffType onOff) {
        if (!Objects.equals(onOff, getOnOff())) {
            zHandleBrightness(OnOffType.OFF == onOff ? PercentType.ZERO : cachedBrightness);
        }
    }

    /**
     * Internal: return the Mirek/Mired value from the given {@link HSBType} color. The Mirek/Mired value is constrained
     * to be within the warmest and coolest limits.
     *
     * @param hsb the {@link HSBType} color to use to determine the Mirek/Mired value.
     */
    private double zMirekFrom(HSBType hsb) {
        double[] xyY = ColorUtil.hsbToXY(new HSBType(hsb.getHue(), hsb.getSaturation(), PercentType.HUNDRED));
        double mirek = 1000000 / ColorUtil.xyToKelvin(new double[] { xyY[0], xyY[1] });
        return Math.min(Math.max(mirek, mirekControlCoolest), mirekControlWarmest);
    }

    /**
     * Internal: create a {@link PercentType} from a double value, ensuring it is in the range [0.0..100.0]
     *
     * @param value the input value.
     * @return a {@link PercentType} representing the input value, constrained to the range [0.0..100.0]
     * @throws IllegalArgumentException if the value is outside the range [0.0..100.0]
     */
    private PercentType zPercentTypeFrom(double value) throws IllegalArgumentException {
        if (value < 0.0 || value > 100.0) {
            throw new IllegalArgumentException("PercentType value must be in range [0.0..100.0]: " + value);
        }
        return new PercentType(new BigDecimal(value));
    }

    /*********************************************************************************
     * SECTION: Internal private classes.
     *********************************************************************************/

    /**
     * Internal: a class that models the RGB LED sub-components of a white LED light. The RGB component
     * weightings are in the range [0.0..1.0] which if scaled to 255 would produce the color temperature
     * specified in the constructor at 100% brightness.
     *
     */
    protected static class WhiteLED {

        private final double[] profile;
        private final double mirek;

        /**
         * Converts the given Mirek/Mired color temperature to RGB component weighting for the LED, so that its
         * output would have the specified color temperature. Each component is in the range [0.0..1.0]
         *
         * @param ledMirek the color temperature of the LED in Mirek/Mired.
         */
        protected WhiteLED(double ledMirek) {
            this.profile = Arrays
                    .stream(ColorUtil.hsbToRgbPercent(ColorUtil.xyToHsb(ColorUtil.kelvinToXY((1000000 / ledMirek)))))
                    .mapToDouble(p -> p.doubleValue() / 100).toArray();
            this.mirek = ledMirek;
        }

        /**
         * Get the Mirek/Mired color temperature of the LED.
         *
         * @return the Mirek/Mired color temperature of the LED.
         */
        protected double getMirek() {
            return mirek;
        }

        /**
         * Get the RGB component weighting of the LED.
         *
         * @return an array of 3 double values representing the RGB components of the LED in the range [0.0..1.0]
         *         which if scaled to 255 would produce the color temperature specified by the 'mirek' field at
         *         100% brightness.
         */
        protected double[] getProfile() {
            return profile;
        }
    }

    /**
     * Internal: a class containing mathematical utility methods that convert between RGB and RGBCW color arrays
     * based on the RGB main values and the RGB sub- component values of the cool and warm white LEDs.
     *
     * TODO it is intended to move this class to the {@link ColorUtil} utility class, but let's keep it here
     * for the time being in order to simplify testing and code review.
     */
    public static class RgbcwMath {

        // below this value no RGB -> RGBCW conversion attempted (see method rgb2rgbcw)
        private static final double CONVERSION_THRESHOLD = 0.01;

        // step size when iterating over C scalar values for RGB -> RGBCW conversion (see method rgb2rgbcw)
        private static final double CONVERSION_ITERATOR_STEP_SIZE = 0.01;

        // default cool and warm white LED RGB profiles used if nothing else is provided in the variable argument lists
        private static final double[] COOL_PROFILE = new double[] { 0.95562, 0.976449753, 1.0 }; // 153 Mirek/Mired
        private static final double[] WARM_PROFILE = new double[] { 1.0, 0.695614289308524, 0.25572 }; // 500

        /**
         * Composes an RGBCW from the given RGB. Calls {@link #rgb2rgbcw(double[], double[], double[])} with default
         * LED profiles. The result depends on the main input RGB values and the RGB sub- component contributions of
         * the cold and warm white LEDs. It solves to find the maximum usable C and W scalar values such that none of
         * the RGB' channels become negative. It solves for C and W such that:
         * <p>
         * {@code RGB ≈ C * coolProfile + W * warmProfile + RGB'} where {@code RGB'} is the remaining RGB after
         * subtracting the scaled cool and warm LED contributions.
         * <p>
         *
         * @param rgb a 3-element array of double: [R,G,B].
         *
         * @return a 5-element array of double: [R',G',B',C,W], where R', G', B' are the remaining RGB values
         *         and C and W are the calculated cold and warm white values.
         * @throws IllegalArgumentException if the input array length is not 3, or if any of its values are outside
         *             the range [0.0..1.0]
         */
        public static double[] rgb2rgbcw(double[] rgb) throws IllegalArgumentException {
            return rgb2rgbcw(rgb, COOL_PROFILE, WARM_PROFILE);
        }

        /**
         * Composes an RGBCW from the given RGB. The result depends on the main input RGB values and the RGB sub-
         * component contributions of the cold and warm white LEDs. It solves to find the maximum usable C and W
         * scalar values such that none of the RGB' channels become negative. It solves for C and W such that:
         * <p>
         * {@code RGB ≈ C * coolProfile + W * warmProfile + RGB'} where {@code RGB'} is the remaining RGB after
         * subtracting the scaled cool and warm LED contributions.
         * <p>
         *
         * @param rgb a 3-element array of double: [R,G,B].
         * @param coolProfile the cool white LED RGB profile, a normalized 3-element [R,G,B] array in the range
         *            [0.0..1.0]. For example see {@link #COOL_PROFILE}.
         * @param warmProfile the warm white LED RGB profile, a normalized 3-element [R,G,B] array in the range
         *            [0.0..1.0]. For example see {@link #WARM_PROFILE}.
         *
         * @return a 5-element array of double: [R',G',B',C,W], where R', G', B' are the remaining RGB values
         *         and C and W are the calculated cold and warm white values.
         * @throws IllegalArgumentException if the input array length is not 3, or if any of its values are outside
         *             the range [0.0..1.0]
         */
        public static double[] rgb2rgbcw(double[] rgb, double[] coolProfile, double[] warmProfile)
                throws IllegalArgumentException {
            if (rgb.length != 3 || Arrays.stream(rgb).anyMatch(d -> d < 0.0 || d > 1.0)) {
                throw new IllegalArgumentException("RGB invalid length, or value out of range");
            }

            double[] rgbcw = new double[] { rgb[0], rgb[1], rgb[2], 0.0, 0.0 };

            // cool/warm contribution is only possible if all rgb values are non- zero
            if (rgb[0] < CONVERSION_THRESHOLD || rgb[1] < CONVERSION_THRESHOLD || rgb[2] < CONVERSION_THRESHOLD) {
                return rgbcw;
            }

            double lowestDelta = 3.0; // lowest total of RGB' elements found so far; starting with the worst case

            // get maximum C scalar such that RGB' channels can't become negative
            double coolScalarMax = getMaxScalarForRgbWithProfile(rgb, coolProfile);

            // iterate downwards over C scalar values to solve for the best combination of C and W scalars
            for (double coolScalar = coolScalarMax; coolScalar >= 0.0; coolScalar -= CONVERSION_ITERATOR_STEP_SIZE) {
                // subtract cool LED profile contributions from RGB to create RGB'
                double[] rgbPrime = new double[] { //
                        rgb[0] - coolProfile[0] * coolScalar, //
                        rgb[1] - coolProfile[1] * coolScalar, //
                        rgb[2] - coolProfile[2] * coolScalar, //
                        Double.NaN, Double.NaN }; // scalar values are dropped in when a new best solution is found

                // get maximum W scalar such that RGB' channels can't become negative
                double warmScalar = getMaxScalarForRgbWithProfile(rgbPrime, warmProfile);

                // also subtract warm LED profile contributions from RGB'
                rgbPrime[0] = rgbPrime[0] - warmProfile[0] * warmScalar;
                rgbPrime[1] = rgbPrime[1] - warmProfile[1] * warmScalar;
                rgbPrime[2] = rgbPrime[2] - warmProfile[2] * warmScalar;

                // select the best solution so far that minimizes the total of the RGB' elements
                double thisDelta = rgbPrime[0] + rgbPrime[1] + rgbPrime[2];
                if (thisDelta < lowestDelta) {
                    lowestDelta = thisDelta;
                    rgbcw = rgbPrime;
                    rgbcw[3] = coolScalar; // drop in the current C and W scalar values
                    rgbcw[4] = warmScalar;
                }
            }

            return rgbcw;
        }

        /**
         * Decomposes the given RGBCW to an RGB. Calls {@link #rgbcw2rgb(double[], double[], double[])} with default
         * LED profiles. The result comprises the main input RGB values plus the RGB sub- component contributions of
         * the cold and warm white LEDs.
         *
         * @param rgbcw a 5-element array of double: [R,G,B,C,W], where R, G, B are the RGB values and C and W are
         *            the cold and warm white LED RGB profile contributions.
         *
         * @return double[] a 3-element array of double: [R,G,B].
         * @throws IllegalArgumentException if the input array length is not 5, or if any its values are
         *             outside the range [0.0..1.0]
         */
        public static double[] rgbcw2rgb(double[] rgbcw) throws IllegalArgumentException {
            return rgbcw2rgb(rgbcw, COOL_PROFILE, WARM_PROFILE);
        }

        /**
         * Decomposes the given RGBCW to an RGB. The result comprises the main input RGB values plus the RGB sub-
         * component contributions of the cold and warm white LEDs.
         *
         * @param rgbcw a 5-element array of double: [R,G,B,C,W], where R, G, B are the RGB values and C and W are
         *            the cold and warm white LED RGB profile contributions.
         * @param coolProfile the cool white LED RGB profile, a normalized 3-element [R,G,B] array in the range
         *            [0.0..1.0]. For example see {@link #COOL_PROFILE}.
         * @param warmProfile the warm white LED RGB profile, a normalized 3-element [R,G,B] array in the range
         *            [0.0..1.0]. For example see {@link #WARM_PROFILE}.
         *
         * @return double[] a 3-element array of double: [R,G,B].
         * @throws IllegalArgumentException if the input array length is not 5, or if any its values are
         *             outside the range [0.0..1.0]
         */
        public static double[] rgbcw2rgb(double[] rgbcw, double[] coolProfile, double[] warmProfile)
                throws IllegalArgumentException {
            if (rgbcw.length != 5 || Arrays.stream(rgbcw).anyMatch(d -> d < 0.0 || d > 1.0)) {
                throw new IllegalArgumentException("RGB invalid length, or value out of range");
            }

            double coolScalar = rgbcw[3], warmScalar = rgbcw[4];

            // add c/w contributions to rgb and clamp to 1.0
            return new double[] { //
                    Math.min(1, rgbcw[0] + coolProfile[0] * coolScalar + warmProfile[0] * warmScalar), //
                    Math.min(1, rgbcw[1] + coolProfile[1] * coolScalar + warmProfile[1] * warmScalar), //
                    Math.min(1, rgbcw[2] + coolProfile[2] * coolScalar + warmProfile[2] * warmScalar) };
        }

        /**
         * Internal: Returns the maximum scalar value for the given RGB and LED profile such that none of
         * the resulting RGB' channels can become negative. Used to determine how much of a given white LED
         * profile can be applied. It checks for zero profile values to avoid divide-by-zero errors.
         *
         * @param rgb a 3-element array of double: [R,G,B].
         * @param profile a 3-element array of double representing an LED profile: [R,G,B].
         * @return double representing the highest scalar value that can be applied to the given RGB LED profile values
         *         without any of the resulting RGB' channel values becoming negative.
         */
        private static double getMaxScalarForRgbWithProfile(double[] rgb, double[] profile) {
            return Math.min(Math.min( //
                    profile[0] > 0 ? rgb[0] / profile[0] : 1, //
                    profile[1] > 0 ? rgb[1] / profile[1] : 1), //
                    profile[2] > 0 ? rgb[2] / profile[2] : 1);
        }
    }
}
