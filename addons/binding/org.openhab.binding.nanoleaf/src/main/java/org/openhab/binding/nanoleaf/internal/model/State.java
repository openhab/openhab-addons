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
package org.openhab.binding.nanoleaf.internal.model;

import com.google.gson.annotations.SerializedName;

/**
 * Represents overall state settings of the light panels
 *
 * @author Martin Raepple - Initial contribution
 */
public class State {

    private On on;
    private Brightness brightness;
    private Hue hue;
    @SerializedName("sat")
    private Sat saturation;
    @SerializedName("ct")
    private Ct colorTemperature;
    private String colorMode;

    public On getOn() {
        return on;
    }

    public void setOn(On on) {
        this.on = on;
    }

    public Brightness getBrightness() {
        return brightness;
    }

    public void setBrightness(Brightness brightness) {
        this.brightness = brightness;
    }

    public Hue getHue() {
        return hue;
    }

    public void setHue(Hue hue) {
        this.hue = hue;
    }

    public Sat getSaturation() {
        return saturation;
    }

    public void setSaturation(Sat sat) {
        this.saturation = sat;
    }

    public Ct getColorTemperature() {
        return colorTemperature;
    }

    public void setColorTemperature(Ct ct) {
        this.colorTemperature = ct;
    }

    public String getColorMode() {
        return colorMode;
    }

    public void setColorMode(String colorMode) {
        this.colorMode = colorMode;
    }

    public void setState(IntegerState value) {
        if (value instanceof Brightness) {
            this.setBrightness((Brightness) value);
        } else if (value instanceof Hue) {
            this.setHue((Hue) value);
        } else if (value instanceof Sat) {
            this.setSaturation((Sat) value);
        } else if (value instanceof Ct) {
            this.setColorTemperature((Ct) value);
        }
    }

    public void setState(BooleanState value) {
        if (value instanceof On) {
            this.setOn((On) value);
        }
    }
}
