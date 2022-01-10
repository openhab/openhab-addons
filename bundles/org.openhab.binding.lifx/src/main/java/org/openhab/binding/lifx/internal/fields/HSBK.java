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
package org.openhab.binding.lifx.internal.fields;

import static org.openhab.binding.lifx.internal.util.LifxMessageUtil.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.PercentType;

/**
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class HSBK {

    private static final String DEFAULT_PROPERTY_NAME = "hsbk";

    private int hue;
    private int saturation;
    private int brightness;
    private int kelvin;

    public HSBK(int hue, int saturation, int brightness, int kelvin) {
        this.hue = hue;
        this.saturation = saturation;
        this.brightness = brightness;
        this.kelvin = kelvin;
    }

    public HSBK(HSBK other) {
        this(other.hue, other.saturation, other.brightness, other.kelvin);
    }

    public HSBK(HSBType hsb, int kelvin) {
        setHSB(hsb);
        this.kelvin = kelvin;
    }

    public int getHue() {
        return hue;
    }

    public int getSaturation() {
        return saturation;
    }

    public int getBrightness() {
        return brightness;
    }

    public int getKelvin() {
        return kelvin;
    }

    public HSBType getHSB() {
        DecimalType hue = hueToDecimalType(this.hue);
        PercentType saturation = saturationToPercentType(this.saturation);
        PercentType brightness = brightnessToPercentType(this.brightness);
        return new HSBType(hue, saturation, brightness);
    }

    public void setHSB(HSBType hsb) {
        setHue(hsb.getHue());
        setSaturation(hsb.getSaturation());
        setBrightness(hsb.getBrightness());
    }

    public void setHue(DecimalType hue) {
        this.hue = decimalTypeToHue(hue);
    }

    public void setSaturation(PercentType saturation) {
        this.saturation = percentTypeToSaturation(saturation);
    }

    public void setBrightness(PercentType brightness) {
        this.brightness = percentTypeToBrightness(brightness);
    }

    public void setKelvin(int kelvin) {
        this.kelvin = kelvin;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + hue;
        result = prime * result + saturation;
        result = prime * result + brightness;
        result = prime * result + kelvin;
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        HSBK other = (HSBK) obj;
        if (hue != other.hue) {
            return false;
        }
        if (saturation != other.saturation) {
            return false;
        }
        if (brightness != other.brightness) {
            return false;
        }
        if (kelvin != other.kelvin) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return toString(DEFAULT_PROPERTY_NAME);
    }

    public String toString(String propertyName) {
        return String.format("%s=%d,%d,%d,%d", propertyName, hue, saturation, brightness, kelvin);
    }
}
