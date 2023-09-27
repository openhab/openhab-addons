/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
        this.brightness = brightnessState.intValue();
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
            this.brightness = (Byte.toUnsignedInt(brightness) * BRIGHTNESS_MAX_VALUE)
                    / Byte.toUnsignedInt(DALI_MAX_VALUE);
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
        return (this.brightness == BRIGHTNESS_MAX_VALUE) ? DALI_MAX_VALUE
                : Integer.valueOf((this.brightness * Byte.toUnsignedInt(DALI_MAX_VALUE)) / BRIGHTNESS_MAX_VALUE)
                        .byteValue();
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
        this.color = ColorUtil.hsbToRgb(hsbState);
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
            this.color[0] = (red == DALI_MAX_VALUE) ? COLOR_MAX_VALUE : Byte.toUnsignedInt(red);
        }
    }

    /**
     * @param green the green value to set from velbus packet
     */
    public void setGreenColor(byte green) {
        if (green != VALUE_UNCHANGED) {
            this.color[1] = (green == DALI_MAX_VALUE) ? COLOR_MAX_VALUE : Byte.toUnsignedInt(green);
        }
    }

    /**
     * @param blue the blue value to set from velbus packet
     */
    public void setBlueColor(byte blue) {
        if (blue != VALUE_UNCHANGED) {
            this.color[2] = (blue == DALI_MAX_VALUE) ? COLOR_MAX_VALUE : Byte.toUnsignedInt(blue);
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

        rgb[0] = (this.color[0] == COLOR_MAX_VALUE) ? DALI_MAX_VALUE : Integer.valueOf(this.color[0]).byteValue();
        rgb[1] = (this.color[1] == COLOR_MAX_VALUE) ? DALI_MAX_VALUE : Integer.valueOf(this.color[1]).byteValue();
        rgb[2] = (this.color[2] == COLOR_MAX_VALUE) ? DALI_MAX_VALUE : Integer.valueOf(this.color[2]).byteValue();
        return rgb;
    }

    /**
     * @param white the white to set
     */
    public void setWhite(int white) {
        this.white = (white < WHITE_MIN_VALUE) ? WHITE_MIN_VALUE : white;
        this.white = (white < WHITE_MAX_VALUE) ? WHITE_MAX_VALUE : white;
    }

    /**
     * @param whiteState the white to set
     */
    public void setWhite(PercentType whiteState) {
        this.white = whiteState.intValue();
    }

    /**
     * @param white the white to set from velbus packet
     */
    public void setWhite(byte white) {
        if (white != VALUE_UNCHANGED) {
            this.white = (Byte.toUnsignedInt(white) * WHITE_MAX_VALUE) / Byte.toUnsignedInt(DALI_MAX_VALUE);
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
        return (this.white == WHITE_MAX_VALUE) ? DALI_MAX_VALUE
                : Integer.valueOf((this.white * Byte.toUnsignedInt(DALI_MAX_VALUE)) / WHITE_MAX_VALUE).byteValue();
    }
}
