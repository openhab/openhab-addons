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

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Represents overall state settings of the light panels
 *
 * @author Martin Raepple - Initial contribution
 */
public class State {

    @SerializedName("on")
    @Expose
    private On on;
    @SerializedName("brightness")
    @Expose
    private Brightness brightness;
    @SerializedName("hue")
    @Expose
    private Hue hue;
    @SerializedName("sat")
    @Expose
    private Sat sat;
    @SerializedName("ct")
    @Expose
    private Ct ct;
    @SerializedName("colorMode")
    @Expose
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

    public Sat getSat() {
        return sat;
    }

    public void setSat(Sat sat) {
        this.sat = sat;
    }

    public Ct getCt() {
        return ct;
    }

    public void setCt(Ct ct) {
        this.ct = ct;
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
            this.setSat((Sat) value);
        } else if (value instanceof Ct) {
            this.setCt((Ct) value);
        }
    }

    public void setState(BooleanState value) {
        if (value instanceof On) {
            this.setOn((On) value);
        }
    }
}
