/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.hue.internal.handler;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.api.dto.clip1.ColorTemperature;
import org.openhab.binding.hue.internal.api.dto.clip1.State;
import org.openhab.binding.hue.internal.api.dto.clip1.State.AlertMode;
import org.openhab.binding.hue.internal.api.dto.clip1.State.ColorMode;
import org.openhab.binding.hue.internal.api.dto.clip1.State.Effect;
import org.openhab.binding.hue.internal.api.dto.clip1.StateUpdate;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.util.ColorUtil;

/**
 * The {@link LightStateConverter} is responsible for mapping to/from jue types.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Oliver Libutzki - Adjustments
 * @author Kai Kreuzer - made code static
 * @author Andre Fuechsel - added method for brightness
 * @author Yordan Zhelev - added method for alert
 * @author Denis Dudnik - switched to internally integrated source of Jue library, minor code cleanup
 * @author Christoph Weitkamp - Added support for bulbs using CIE XY colormode only
 */
@NonNullByDefault
public class LightStateConverter {

    private static final double HUE_FACTOR = 65535 / 360.0;
    private static final double SATURATION_FACTOR = 2.54;
    private static final double BRIGHTNESS_FACTOR = 2.54;

    /**
     * {@value #ALERT_MODE_NONE}. The light is not performing an alert effect.
     */
    static final String ALERT_MODE_NONE = "NONE";
    /**
     * {@value #ALERT_MODE_SELECT}. The light is performing one breathe cycle.
     */
    static final String ALERT_MODE_SELECT = "SELECT";
    /**
     * {@value #ALERT_MODE_LONG_SELECT}. The light is performing breathe cycles
     * for 15 seconds or until an "alert": "none" command is received.
     */
    static final String ALERT_MODE_LONG_SELECT = "LSELECT";

    private static final int DIM_STEPSIZE = 30;

    /**
     * Transforms the given {@link HSBType} into a light state.
     *
     * @param hsbType HSB type
     * @return light state representing the {@link HSBType}.
     */
    public static StateUpdate toColorLightState(HSBType hsbType, State lightState) {
        // XY color is the implicit default: Use XY color mode if i) no color mode is set or ii) if the bulb is in
        // CT mode or iii) already in XY mode. Only if the bulb is in HS mode, use this one.
        StateUpdate stateUpdate = ColorMode.HS.equals(lightState.getColorMode()) ? toHSBColorLightState(hsbType)
                : toXYColorLightState(hsbType);

        int brightness = (int) Math.floor(hsbType.getBrightness().doubleValue() * BRIGHTNESS_FACTOR);
        if (brightness > 0) {
            stateUpdate.setBrightness(brightness);
        }
        return stateUpdate;
    }

    private static StateUpdate toHSBColorLightState(HSBType hsbType) {
        int hue = (int) Math.round(hsbType.getHue().doubleValue() * HUE_FACTOR);
        int saturation = (int) Math.floor(hsbType.getSaturation().doubleValue() * SATURATION_FACTOR);

        return new StateUpdate().setHue(hue).setSat(saturation);
    }

    private static StateUpdate toXYColorLightState(HSBType hsbType) {
        PercentType[] xy = hsbType.toXY();
        float x = xy[0].floatValue() / 100.0f;
        float y = xy[1].floatValue() / 100.0f;

        return new StateUpdate().setXY(x, y);
    }

    /**
     * Transforms the given {@link OnOffType} into a light state containing the
     * 'on' value.
     *
     * @param onOffType on or off state
     * @return light state containing the 'on' value
     */
    public static StateUpdate toOnOffLightState(OnOffType onOffType) {
        return new StateUpdate().setOn(OnOffType.ON.equals(onOffType));
    }

    /**
     * Transforms the given {@link PercentType} into a light state containing
     * the brightness and the 'on' value represented by {@link PercentType}.
     *
     * @param percentType brightness represented as {@link PercentType}
     * @return light state containing the brightness and the 'on' value
     */
    public static StateUpdate toBrightnessLightState(PercentType percentType) {
        boolean on = !percentType.equals(PercentType.ZERO);
        final StateUpdate stateUpdate = new StateUpdate().setOn(on);

        int brightness = (int) Math.floor(percentType.doubleValue() * BRIGHTNESS_FACTOR);
        if (brightness > 0) {
            stateUpdate.setBrightness(brightness);
        }
        return stateUpdate;
    }

    /**
     * Adjusts the given brightness using the {@link IncreaseDecreaseType} and
     * returns the updated value.
     *
     * @param command The {@link IncreaseDecreaseType} to be used
     * @param currentBrightness The current brightness
     * @return The adjusted brightness value
     */
    public static int toAdjustedBrightness(IncreaseDecreaseType command, int currentBrightness) {
        int newBrightness;
        if (command == IncreaseDecreaseType.DECREASE) {
            newBrightness = Math.max(currentBrightness - DIM_STEPSIZE, 0);
        } else {
            newBrightness = Math.min(currentBrightness + DIM_STEPSIZE, (int) (BRIGHTNESS_FACTOR * 100));
        }
        return newBrightness;
    }

    /**
     * Transforms the given {@link PercentType} into a light state containing
     * the color temperature represented by {@link PercentType}.
     *
     * @param percentType color temperature represented as {@link PercentType}
     * @return light state containing the color temperature
     */
    public static StateUpdate toColorTemperatureLightStateFromPercentType(PercentType percentType,
            ColorTemperature capabilities) {
        int colorTemperature = capabilities.min
                + Math.round(((capabilities.max - capabilities.min) * percentType.floatValue()) / 100);
        return new StateUpdate().setColorTemperature(colorTemperature, capabilities);
    }

    public static int kelvinToMired(int kelvinValue) {
        return (int) (1000000.0 / kelvinValue);
    }

    /**
     * Transforms the given color temperature in Kelvin into a Hue Light {@link State}.
     *
     * @param kelvinValue color temperature in Kelvin
     * @param capabilities color temperature capabilities (e.g. min and max values)
     * @return light state containing the color temperature
     */
    public static StateUpdate toColorTemperatureLightState(int kelvinValue, ColorTemperature capabilities) {
        return new StateUpdate().setColorTemperature(kelvinToMired(kelvinValue), capabilities);
    }

    /**
     * Adjusts the given color temperature using the {@link IncreaseDecreaseType} and returns the updated value.
     *
     * @param type The {@link IncreaseDecreaseType} to be used
     * @param currentColorTemp The current color temperature
     * @return The adjusted color temperature value
     */
    public static int toAdjustedColorTemp(IncreaseDecreaseType type, int currentColorTemp,
            ColorTemperature capabilities) {
        int newColorTemp;
        if (type == IncreaseDecreaseType.DECREASE) {
            newColorTemp = Math.max(currentColorTemp - DIM_STEPSIZE, capabilities.min);
        } else {
            newColorTemp = Math.min(currentColorTemp + DIM_STEPSIZE, capabilities.max);
        }
        return newColorTemp;
    }

    /**
     * Transforms Hue Light {@link State} into {@link PercentType} representing
     * the color temperature.
     *
     * @param lightState light state
     * @return percent type representing the color temperature
     */
    public static PercentType toColorTemperaturePercentType(State lightState, ColorTemperature capabilities) {
        int percent = (int) Math.round(((lightState.getColorTemperature() - capabilities.min) * 100.0)
                / (capabilities.max - capabilities.min));
        return new PercentType(restrictToBounds(percent));
    }

    public static int miredToKelvin(int miredValue) {
        return (int) (1000000.0 / miredValue);
    }

    /**
     * Transforms Hue Light {@link State} into {@link QuantityType} representing
     * the color temperature in Kelvin.
     *
     * @param lightState light state
     * @return quantity type representing the color temperature in Kelvin
     */
    public static QuantityType<Temperature> toColorTemperature(State lightState) {
        return new QuantityType<>(miredToKelvin(lightState.getColorTemperature()), Units.KELVIN);
    }

    /**
     * Transforms Hue Light {@link State} into {@link PercentType} representing
     * the brightness.
     *
     * @param lightState light state
     * @return percent type representing the brightness
     */
    public static PercentType toBrightnessPercentType(State lightState) {
        int percent = (int) Math.ceil(lightState.getBrightness() / BRIGHTNESS_FACTOR);
        return new PercentType(restrictToBounds(percent));
    }

    /**
     * Transforms {@link State} into {@link StringType} representing the {@link AlertMode}.
     *
     * @param lightState light state.
     * @return string type representing the alert mode.
     */
    public static StringType toAlertStringType(State lightState) {
        AlertMode alertMode = lightState.getAlertMode();
        if (alertMode == null) {
            return new StringType("NULL");
        } else {
            return new StringType(alertMode.toString());
        }
    }

    /**
     * Transforms Hue Light {@link State} into {@link HSBType} representing the
     * color.
     *
     * @param lightState light state
     * @return HSB type representing the color
     */
    public static HSBType toHSBType(State lightState) {
        // even if color mode is reported to be XY, xy field of lightState might be null, while hsb is available
        boolean isInXYMode = ColorMode.XY.equals(lightState.getColorMode()) && lightState.getXY() != null;
        return isInXYMode ? fromXYtoHSBType(lightState) : fromHSBtoHSBType(lightState);
    }

    private static HSBType fromHSBtoHSBType(State lightState) {
        int hue = (int) Math.round(lightState.getHue() / HUE_FACTOR) % 360;

        int saturationInPercent = (int) Math.ceil(lightState.getSaturation() / SATURATION_FACTOR);
        saturationInPercent = restrictToBounds(saturationInPercent);

        int brightnessInPercent = (int) Math.ceil(lightState.getBrightness() / BRIGHTNESS_FACTOR);
        brightnessInPercent = restrictToBounds(brightnessInPercent);

        return new HSBType(new DecimalType(hue), new PercentType(saturationInPercent),
                new PercentType(brightnessInPercent));
    }

    private static HSBType fromXYtoHSBType(State lightState) {
        float[] xy = lightState.getXY();
        HSBType hsb = ColorUtil.xyToHsb(new double[] { xy[0], xy[1] });

        int brightnessInPercent = (int) Math.ceil(lightState.getBrightness() / BRIGHTNESS_FACTOR);
        brightnessInPercent = restrictToBounds(brightnessInPercent);

        return new HSBType(hsb.getHue(), hsb.getSaturation(), new PercentType(brightnessInPercent));
    }

    /**
     * Transforms the given {@link StringType} into a light state containing the {@link AlertMode} to be triggered.
     *
     * @param alertType {@link StringType} representing the required {@link AlertMode} . <br>
     *            Supported values are:
     *            <ul>
     *            <li>{@value #ALERT_MODE_NONE}.
     *            <li>{@value #ALERT_MODE_SELECT}.
     *            <li>{@value #ALERT_MODE_LONG_SELECT}.
     *            </ul>
     * @return light state containing the {@link AlertMode} or <b><code>null </code></b> if the provided
     *         {@link StringType} represents unsupported mode.
     */
    public static @Nullable StateUpdate toAlertState(StringType alertType) {
        AlertMode alertMode;

        switch (alertType.toString()) {
            case ALERT_MODE_NONE:
                alertMode = State.AlertMode.NONE;
                break;
            case ALERT_MODE_SELECT:
                alertMode = State.AlertMode.SELECT;
                break;
            case ALERT_MODE_LONG_SELECT:
                alertMode = State.AlertMode.LSELECT;
                break;
            default:
                return null;
        }
        return new StateUpdate().setAlert(alertMode);
    }

    /**
     * Transforms the given {@link OnOffType} into a light state containing the {@link Effect} value.
     * {@link OnOffType#ON} will result in {@link Effect#COLORLOOP}. {@link OnOffType#OFF} will result in
     * {@link Effect#NONE}.
     *
     * @param onOffType on or off state
     * @return light state containing the {@link Effect} value
     */
    public static StateUpdate toOnOffEffectState(OnOffType onOffType) {
        StateUpdate stateUpdate;

        if (OnOffType.ON.equals(onOffType)) {
            stateUpdate = new StateUpdate().setEffect(Effect.COLORLOOP);
        } else {
            stateUpdate = new StateUpdate().setEffect(Effect.NONE);
        }

        return stateUpdate;
    }

    private static int restrictToBounds(int percentValue) {
        if (percentValue < 0) {
            return 0;
        } else if (percentValue > 100) {
            return 100;
        }
        return percentValue;
    }
}
