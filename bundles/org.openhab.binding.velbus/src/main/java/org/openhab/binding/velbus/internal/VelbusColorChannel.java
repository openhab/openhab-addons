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
package org.openhab.binding.velbus.internal;

import static org.openhab.binding.velbus.internal.VelbusBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.util.ColorUtil;

/**
 * The {@link VelbusDALIConverter} represents a class with properties that manage DALI color channel.
 *
 * @author Daniel Rosengarten - Initial contribution
 */
@NonNullByDefault
public class VelbusColorChannel {
    protected final int BRIGHTNESS_MIN_VALUE = 0;
    protected final int BRIGHTNESS_MAX_VALUE = 100;

    protected final int COLOR_MIN_VALUE = 0;
    protected final int COLOR_MAX_VALUE = 255;

    protected final int WHITE_MIN_VALUE = 0;
    protected final int WHITE_MAX_VALUE = 100;

    private int brightness = 100;
    private int[] color = { 255, 255, 255 };
    private int white = 100;

    /**
     * @param brightness the brightness to set
     */
    public void setBrightness(int brightness) {
        this.brightness = (brightness < BRIGHTNESS_MIN_VALUE) ? BRIGHTNESS_MIN_VALUE : brightness;
        this.brightness = (brightness > BRIGHTNESS_MAX_VALUE) ? BRIGHTNESS_MAX_VALUE : brightness;
    }

    /**
     * @param brightnessState the brightness to set
     */
    public void setBrightness(PercentType brightnessState) {
        setBrightness(brightnessState.intValue());
    }

    /**
     * @param brightnessState the brightness to set
     */
    public void setBrightness(HSBType brightnessState) {
        setBrightness(brightnessState.getBrightness());
    }

    /**
     * @param brightness the brightness to set from velbus packet
     */
    public void setBrightness(byte brightness) {
        if (brightness != VALUE_UNCHANGED) {
            this.brightness = convertFromVelbus(Byte.toUnsignedInt(brightness), Byte.toUnsignedInt(DALI_MAX_VALUE),
                    BRIGHTNESS_MAX_VALUE);
        }
    }

    /**
     * @return the brightness
     */
    public int getBrightness() {
        return this.brightness;
    }

    /**
     * @return the brightness in PercentType format
     */
    public PercentType getBrightnessPercent() {
        return new PercentType(getBrightness());
    }

    /**
     * @return the brightness for velbus packet
     */
    public byte getBrightnessVelbus() {
        return convertToVelbus(this.brightness, BRIGHTNESS_MAX_VALUE, Byte.toUnsignedInt(DALI_MAX_VALUE));
    }

    /**
     * @param rgb the color to set in RGB
     */
    public void setColor(int[] rgb) {
        if (rgb.length == 3) {
            this.color[0] = (rgb[0] < COLOR_MIN_VALUE) ? COLOR_MIN_VALUE : rgb[0];
            this.color[0] = (rgb[0] > COLOR_MAX_VALUE) ? COLOR_MAX_VALUE : rgb[0];
            this.color[1] = (rgb[1] < COLOR_MIN_VALUE) ? COLOR_MIN_VALUE : rgb[1];
            this.color[1] = (rgb[1] > COLOR_MAX_VALUE) ? COLOR_MAX_VALUE : rgb[1];
            this.color[2] = (rgb[2] < COLOR_MIN_VALUE) ? COLOR_MIN_VALUE : rgb[2];
            this.color[2] = (rgb[2] > COLOR_MAX_VALUE) ? COLOR_MAX_VALUE : rgb[2];
        }
    }

    /**
     * @param hsbState the color to set in HSB
     */
    public void setColor(HSBType hsbState) {
        setColor(ColorUtil.hsbToRgb(hsbState));
    }

    /**
     * @param rgb the color to set in RGB from velbus packet
     */
    public void setColor(byte[] rgb) {
        if (rgb.length == 3) {
            if (rgb[0] != VALUE_UNCHANGED) {
                this.color[0] = (rgb[0] == DALI_MAX_VALUE) ? COLOR_MAX_VALUE : Byte.toUnsignedInt(rgb[0]);
            }
            if (rgb[1] != VALUE_UNCHANGED) {
                this.color[1] = (rgb[1] == DALI_MAX_VALUE) ? COLOR_MAX_VALUE : Byte.toUnsignedInt(rgb[1]);
            }
            if (rgb[2] != VALUE_UNCHANGED) {
                this.color[2] = (rgb[2] == DALI_MAX_VALUE) ? COLOR_MAX_VALUE : Byte.toUnsignedInt(rgb[2]);
            }
        }
    }

    /**
     * @param red the red value to set from velbus packet
     */
    public void setRedColor(byte red) {
        if (red != VALUE_UNCHANGED) {
            this.color[0] = convertFromVelbus(Byte.toUnsignedInt(red), DALI_MAX_VALUE, COLOR_MAX_VALUE);
        }
    }

    /**
     * @param green the green value to set from velbus packet
     */
    public void setGreenColor(byte green) {
        if (green != VALUE_UNCHANGED) {
            this.color[1] = convertFromVelbus(Byte.toUnsignedInt(green), DALI_MAX_VALUE, COLOR_MAX_VALUE);
        }
    }

    /**
     * @param blue the blue value to set from velbus packet
     */
    public void setBlueColor(byte blue) {
        if (blue != VALUE_UNCHANGED) {
            this.color[2] = convertFromVelbus(Byte.toUnsignedInt(blue), DALI_MAX_VALUE, COLOR_MAX_VALUE);
        }
    }

    /**
     * @return the color in RGB
     */
    public int[] getColor() {
        return this.color;
    }

    /**
     * @return the color in HSBType format
     */
    public HSBType getColorHSB() {
        return ColorUtil.rgbToHsb(this.color);
    }

    /**
     * @return the color for velbus packet
     */
    public byte[] getColorVelbus() {
        byte[] rgb = { VALUE_UNCHANGED, VALUE_UNCHANGED, VALUE_UNCHANGED };

        rgb[0] = convertToVelbus(this.color[0], COLOR_MAX_VALUE, DALI_MAX_VALUE);
        rgb[1] = convertToVelbus(this.color[1], COLOR_MAX_VALUE, DALI_MAX_VALUE);
        rgb[2] = convertToVelbus(this.color[2], COLOR_MAX_VALUE, DALI_MAX_VALUE);

        return rgb;
    }

    /**
     * @param white the white to set
     */
    public void setWhite(int white) {
        this.white = (white < WHITE_MIN_VALUE) ? WHITE_MIN_VALUE : white;
        this.white = (white > WHITE_MAX_VALUE) ? WHITE_MAX_VALUE : white;
    }

    /**
     * @param whiteState the white to set
     */
    public void setWhite(PercentType whiteState) {
        setWhite(whiteState.intValue());
    }

    /**
     * @param white the white to set from velbus packet
     */
    public void setWhite(byte white) {
        if (white != VALUE_UNCHANGED) {
            this.white = convertFromVelbus(Byte.toUnsignedInt(white), Byte.toUnsignedInt(DALI_MAX_VALUE),
                    WHITE_MAX_VALUE);
        }
    }

    /**
     * @return the white
     */
    public int getWhite() {
        return this.white;
    }

    /**
     * @return the white in PercentType format
     */
    public PercentType getWhitePercent() {
        return new PercentType(this.white);
    }

    /**
     * @return the white value for velbus packet
     */
    public byte getWhiteVelbus() {
        return convertToVelbus(this.white, WHITE_MAX_VALUE, Byte.toUnsignedInt(DALI_MAX_VALUE));
    }

    /**
     * @param value the value to rescale
     * @param from_max the maximum value of the first parameter
     * @param to_max the maximum value supported by the returned value
     * @return the value rescaled
     */
    private int rescale(int value, int from_max, int to_max) {
        return value * to_max / from_max;
    }

    /**
     * @param value the value to convert
     * @param from_max the maximum value of the first parameter
     * @param to_max the maximum value supported by the Velbus module
     * @return the value rescaled for the velbus packet
     */
    private byte convertToVelbus(int value, int from_max, int to_max) {
        return (value >= from_max) ? Integer.valueOf(to_max).byteValue()
                : Integer.valueOf(rescale(value, from_max, to_max)).byteValue();
    }

    /**
     * @param value the value to convert
     * @param from_max the maximum value supported by the Velbus module
     * @param to_max the maximum value supported by the returned value
     * @return the value rescaled from the packet
     */
    private int convertFromVelbus(int value, int from_max, int to_max) {
        return (value >= from_max) ? to_max : rescale(value, from_max, to_max);
    }
}
