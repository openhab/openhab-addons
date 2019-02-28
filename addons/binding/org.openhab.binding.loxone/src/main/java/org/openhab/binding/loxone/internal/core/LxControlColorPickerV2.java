/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.loxone.internal.core;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.openhab.binding.loxone.internal.core.LxJsonApp3.LxJsonControl;

/**
 * A Color Picker V2 type of control on Loxone Miniserver.
 * <p>
 * According to Loxone API documentation, a color picker control covers:
 * <ul>
 * <li>Color (Hue/Saturation/Brightness)
 * </ul>
 *
 * @author Michael Mattan - initial contribution
 *
 */
public class LxControlColorPickerV2 extends LxControl {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxWsClient client, LxUuid uuid, LxJsonControl json, LxContainer room, LxCategory category) {
            return new LxControlColorPickerV2(client, uuid, json, room, category);
        }

        @Override
        String getType() {
            return TYPE_NAME;
        }
    }

    private static final String COLOR_TYPE_TEMP = "temp";
    private static final String COLOR_TYPE_HSV = "hsv";

    /**
     * Color state
     */
    public static final String STATE_COLOR = "color";

    private static final String TYPE_NAME = "colorpickerv2";

    LxControlColorPickerV2(LxWsClient client, LxUuid uuid, LxJsonControl json, LxContainer room, LxCategory category) {
        super(client, uuid, json, room, category);
    }

    /**
     * get the current Loxone color in HSBType format
     *
     * @return the HSBType color
     */
    public HSBType getColor() {
        HSBType hsbColor = null;
        String color = getStateTextValue(STATE_COLOR);

        if (color != null) {
            hsbColor = this.mapLoxoneToOH(color);
        }

        return hsbColor;
    }

    /**
     * sets the color of the color picker
     *
     * @param hsb the color to set
     */
    public void setColor(HSBType hsb) {
        socketClient.sendAction(uuid, "hsv(" + hsb.toString() + ")");
    }

    /**
     * Sets the color picker to on
     *
     * @throws IOException
     *                         error communicating with the Miniserver
     */
    public void on() throws IOException {
        HSBType currentColor = this.getColor();
        if (currentColor != null) {
            this.setColor(new HSBType(currentColor.getHue(), currentColor.getSaturation(), PercentType.HUNDRED));
        }
    }

    /**
     * Sets the color picker to off
     *
     * @throws IOException
     *                         error communicating with the Miniserver
     */
    public void off() throws IOException {
        HSBType currentColor = this.getColor();
        if (currentColor != null) {
            this.setColor(new HSBType(currentColor.getHue(), currentColor.getSaturation(), PercentType.ZERO));
        }
    }

    /**
     * set the brightness level
     *
     * @param p the brightness percentage
     */
    public void setBrightness(PercentType p) {
        HSBType currentColor = this.getColor();
        if (currentColor != null) {
            this.setColor(new HSBType(currentColor.getHue(), currentColor.getSaturation(), p));
        }
    }

    /**
     * set the brightness level from a decimal type
     *
     * @param d the brightness in decimal
     */
    public void setBrightness(DecimalType d) {
        this.setBrightness(new PercentType(d.toBigDecimal()));
    }

    /**
     * increases/decreases the brightness with a given step
     *
     * @param step the amount to increase/decrease
     */
    public void increaseDecreaseBrightness(int step) {
        HSBType currentColor = this.getColor();
        if (currentColor != null) {
            this.setBrightness(new PercentType(currentColor.getBrightness().intValue() + step));
        }
    }

    /**
     * map the Loxone color to OpenHab HSBType
     *
     * @param color color in format hsb(h,s,v) or temp(brightness,kelvin)
     * @return HSBType
     */
    private HSBType mapLoxoneToOH(String color) {
        HSBType hsbColor = null;

        Matcher m = Pattern.compile("\\((.*?)\\)").matcher(color);
        if (m.find()) {
            String[] colorSplit = m.group(1).split(",");

            if (isTemperatureColor(color) && colorSplit.length == 2) {
                // convert temperature to a OH color
                int brightness = constrain(Integer.valueOf(colorSplit[0]), 0, 100);
                int temperature = constrain(Integer.valueOf(colorSplit[1]), 0, 65500);

                int red = map(brightness, 0, 100, 0, calculateRed(temperature));
                int green = map(brightness, 0, 100, 0, calculateGreen(temperature));
                int blue = map(brightness, 0, 100, 0, calculateBlue(temperature));

                hsbColor = HSBType.fromRGB(red, green, blue);
            } else if (isHsvColor(color) && colorSplit.length == 3) {
                hsbColor = new HSBType(m.group(1));
            } else {
                throw new IllegalArgumentException("Invalid color: " + color);
            }
        }

        return hsbColor;
    }

    /**
     * Re-maps a number from one range to another. That is, a value of fromLow would get mapped to toLow, a value of
     * fromHigh to toHigh, values in-between to values in-between, etc.
     *
     * @param x        the number to map
     * @param fromLow  the lower bound of the value's current range
     * @param fromHigh the upper bound of the value's current range
     * @param toLower  the lower bound of the value's target range
     * @param toHigh   the upper bound of the value's target range
     * @return the mapped value
     */
    private int map(int x, int fromLow, int fromHigh, int toLow, int toHigh) {
        return (x - fromLow) * (toHigh - toLow) / (fromHigh - fromLow) + toLow;
    }

    /**
     * Constrains a number to be within a range.
     *
     * @param x   the number to constrain
     * @param min the minimum value
     * @param max the maximum value
     * @return the constrained value
     */
    private int constrain(int x, int min, int max) {
        if (x >= min && x <= max) {
            return x;
        } else if (x < min) {
            return min;
        } else {
            return max;
        }
    }

    /**
     * calculates the red value based on the Kelvin temperature
     *
     * @param temp the Kelvin temperature
     * @return the red value
     */
    private int calculateRed(int temp) {
        int red = 255;
        int temperature = temp / 100;

        if (temperature > 66) {
            red = temperature - 60;
            red = ((Long) Math.round(329.698727466 * Math.pow(red, -0.1332047592))).intValue();
        }

        return constrain(red, 0, 255);
    }

    /**
     * calculates the green value based on the Kelvin temperature
     *
     * @param temp green Kelvin temperature
     * @return the red value
     */
    private int calculateGreen(int temp) {
        int green;
        int temperature = temp / 100;

        if (temperature <= 66) {
            green = temperature;
            green = ((Long) Math.round((99.4708025861 * Math.log(green)) - 161.1195681661)).intValue();
        } else {
            green = temperature - 60;
            green = ((Long) Math.round(288.1221695283 * Math.pow(green, -0.0755148492))).intValue();
        }

        return constrain(green, 0, 255);
    }

    /**
     * calculates the blue value based on the Kelvin temperature
     *
     * @param temp the Kelvin temperature
     * @return the blue value
     */
    private int calculateBlue(int temp) {
        int blue = 255;
        int temperature = temp / 100;

        if (temperature < 65) {
            if (temperature <= 19) {
                blue = 0;
            } else {
                blue = temperature - 10;
                blue = ((Long) Math.round((138.5177312231 * Math.log(blue)) - 305.0447927307)).intValue();
            }
        }

        return constrain(blue, 0, 255);
    }

    /**
     * checks whether the given Loxone color is a temperature
     *
     * @param color the loxone color
     * @return boolean
     */
    private boolean isTemperatureColor(String color) {
        return color.startsWith(COLOR_TYPE_TEMP);
    }

    /**
     * checks whether the given Loxone color is a HSV-color
     *
     * @param color the loxone color
     * @return boolean
     */
    private boolean isHsvColor(String color) {
        return color.startsWith(COLOR_TYPE_HSV);
    }

}
