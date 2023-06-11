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
package org.openhab.binding.nanoleaf.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.OnOffType;

import com.google.gson.annotations.SerializedName;

/**
 * Represents overall state settings of the light panels
 *
 * @author Martin Raepple - Initial contribution
 */
@NonNullByDefault
public class State {

    private @Nullable On on;
    private @Nullable Brightness brightness;
    private @Nullable Hue hue;
    @SerializedName("sat")
    private @Nullable Sat saturation;
    @SerializedName("ct")
    private @Nullable Ct colorTemperature;

    private @Nullable String colorMode;

    public @Nullable On getOn() {
        return on;
    }

    public OnOffType getOnOff() {
        On localOn = on;
        return (localOn != null && localOn.getValue()) ? OnOffType.ON : OnOffType.OFF;
    }

    public void setOn(On on) {
        this.on = on;
    }

    public @Nullable Brightness getBrightness() {
        return brightness;
    }

    public void setBrightness(Brightness brightness) {
        this.brightness = brightness;
    }

    public @Nullable Hue getHue() {
        return hue;
    }

    public void setHue(Hue hue) {
        this.hue = hue;
    }

    public @Nullable Sat getSaturation() {
        return saturation;
    }

    public void setSaturation(Sat sat) {
        this.saturation = sat;
    }

    public @Nullable Ct getColorTemperature() {
        return colorTemperature;
    }

    public void setColorTemperature(Ct ct) {
        this.colorTemperature = ct;
    }

    public @Nullable String getColorMode() {
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
