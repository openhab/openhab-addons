/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.*;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;

/**
 * The {@link ShellyColorUtils} provides some utility functions around RGBW handling.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyColorUtils {
    OnOffType power = OnOffType.OFF;
    String mode = "";

    int red = 0;
    int green = 0;
    int blue = 0;
    int white = 0;
    PercentType percentRed = new PercentType(0);
    PercentType percentGreen = new PercentType(0);
    PercentType percentBlue = new PercentType(0);
    PercentType percentWhite = new PercentType(0);

    int gain = 0;
    int brightness = 0;
    int temp = 0;
    int minTemp = 0;
    int maxTemp = 0;
    PercentType percentGain = new PercentType(0);
    PercentType percentBrightness = new PercentType(0);
    PercentType percentTemp = new PercentType(0);
    Integer effect = 0;

    public ShellyColorUtils() {
    }

    public ShellyColorUtils(ShellyColorUtils col) {
        minTemp = col.minTemp;
        maxTemp = col.maxTemp;
        setRed(col.red);
        setGreen(col.green);
        setBlue(col.blue);
        setWhite(col.white);
        setGain(col.gain);
        setBrightness(col.brightness);
        setTemp(col.temp);
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setMinMaxTemp(int min, int max) {
        minTemp = min;
        maxTemp = max;
    }

    public boolean setRGBW(int red, int green, int blue, int white) {
        setRed(red);
        setGreen(green);
        setBlue(blue);
        setWhite(white);
        return true;
    }

    public boolean setRed(int value) {
        boolean changed = red != value;
        red = value;
        percentRed = toPercent(red);
        return changed;
    }

    public boolean setGreen(int value) {
        boolean changed = green != value;
        green = value;
        percentGreen = toPercent(green);
        return changed;
    }

    public boolean setBlue(int value) {
        boolean changed = blue != value;
        blue = value;
        percentBlue = toPercent(blue);
        return changed;
    }

    public boolean setWhite(int value) {
        boolean changed = white != value;
        white = value;
        percentWhite = toPercent(white);
        return changed;
    }

    public boolean setBrightness(int value) {
        boolean changed = brightness != value;
        brightness = value;
        percentBrightness = toPercent(brightness, SHELLY_MIN_BRIGHTNESS, SHELLY_MAX_BRIGHTNESS);
        return changed;
    }

    public boolean setGain(int value) {
        boolean changed = gain != value;
        gain = value;
        percentGain = toPercent(gain, SHELLY_MIN_GAIN, SHELLY_MAX_GAIN);
        return changed;
    }

    public boolean setTemp(int value) {
        boolean changed = temp != value;
        temp = value;
        percentTemp = toPercent(temp, minTemp, maxTemp);
        return changed;
    }

    public boolean setEffect(int value) {
        boolean changed = effect != value;
        effect = value;
        return changed;
    }

    public HSBType toHSB() {
        return HSBType.fromRGB(red, green, blue);
    }

    public Integer[] fromRGBW(String rgbwString) {
        Integer values[] = new Integer[4];
        values[0] = values[1] = values[2] = values[3] = -1;
        try {
            String rgbw[] = rgbwString.split(",");
            for (int i = 0; i < rgbw.length; i++) {
                values[i] = Integer.parseInt(rgbw[i]);
            }
        } catch (NullPointerException e) { // might be a format problem
            throw new IllegalArgumentException(
                    "Unable to convert fullColor value: " + rgbwString + ", " + e.getMessage());
        }
        if (values[0] != -1) {
            setRed(values[0]);
        }
        if (values[1] != -1) {
            setGreen(values[1]);
        }
        if (values[2] != -1) {
            setBlue(values[2]);
        }
        if (values[3] != -1) {
            setWhite(values[3]);
        }
        return values;
    }

    public boolean isRgbValid() {
        return (red != -1) && (blue != -1) && (green != -1);
    }

    public static PercentType toPercent(Integer value) {
        return toPercent(value, 0, SHELLY_MAX_COLOR);
    }

    public static PercentType toPercent(Integer _value, Integer min, Integer max) {
        double range = max.doubleValue() - min.doubleValue();
        double value = _value.doubleValue();
        value = value < min ? min.doubleValue() : value;
        value = value > max ? max.doubleValue() : value;
        double percent = 0.0;
        if (range > 0) {
            percent = Math.round((value - min) / range * 100);
        }
        return new PercentType(new BigDecimal(percent));
    }
}
