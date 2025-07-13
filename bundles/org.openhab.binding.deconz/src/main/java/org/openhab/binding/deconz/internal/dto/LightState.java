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
package org.openhab.binding.deconz.internal.dto;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link LightState} is send by the websocket connection as well as the Rest API.
 * It is part of a {@link LightMessage}.
 *
 * This should be in sync with the supported lights from
 * https://github.com/dresden-elektronik/deconz-rest-plugin/wiki/Supported-Devices.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class LightState {
    public @Nullable Boolean reachable;
    public @Nullable Boolean on;
    public @Nullable Integer bri;

    public @Nullable String alert;
    public @Nullable String colormode;
    public @Nullable String effect;
    public @Nullable Integer effectSpeed;
    public @Nullable Integer ontime;

    // depending on the type of light
    public @Nullable Integer hue;
    public @Nullable Integer sat;
    public @Nullable Integer ct;
    public double @Nullable [] xy;

    // for window covering
    public @Nullable Boolean open;
    public @Nullable Boolean stop;
    public @Nullable Integer lift;

    public @Nullable Integer transitiontime;

    private static enum ColorMode {
        CT,
        HS,
        XY
    }

    private @Nullable ColorMode colorModeFrom(@Nullable String value) {
        if (value != null) {
            try {
                return ColorMode.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                // fall through
            }
        }
        return null;
    }

    private boolean compareXYWithTolerance(double @Nullable [] a, double @Nullable [] b, double tolerance) {
        return a == null || b == null || ((a.length > 1) && (b.length > 1) && (Math.abs(a[0] - b[0]) < tolerance)
                && (Math.abs(a[1] - b[1]) < tolerance));
    }

    /**
     * compares two light states and ignore all fields that are null in either state
     *
     * @param other state to compare with
     * @return true if equal
     */
    public boolean equalsIgnoreNull(LightState other) {
        boolean colorsEqual = true;
        ColorMode thisMode = colorModeFrom(this.colormode);
        ColorMode otherMode = colorModeFrom(other.colormode);
        if (thisMode == ColorMode.CT || otherMode == ColorMode.CT) {
            colorsEqual = equalsIgnoreNull(this.ct, other.ct);
        } else if (thisMode == ColorMode.HS || otherMode == ColorMode.HS) {
            colorsEqual = equalsIgnoreNull(this.hue, other.hue) && equalsIgnoreNull(this.sat, other.sat);
        } else if (thisMode == ColorMode.XY || otherMode == ColorMode.XY) {
            colorsEqual = compareXYWithTolerance(this.xy, other.xy, 0.01);
        }
        return colorsEqual && equalsIgnoreNull(this.on, other.on) && equalsIgnoreNull(this.bri, other.bri);
    }

    /**
     * clear this light state
     */
    public void clear() {
        reachable = null;
        on = null;
        bri = null;

        alert = null;
        colormode = null;
        effect = null;
        effectSpeed = null;
        ontime = null;

        hue = null;
        sat = null;
        ct = null;
        xy = null;

        transitiontime = null;
    }

    private <T> boolean equalsIgnoreNull(T o1, T o2) {
        return (o1 != null && o2 != null) ? o1.equals(o2) : true;
    }

    @Override
    public String toString() {
        return "LightState{" + "reachable=" + reachable + ", on=" + on + ", bri=" + bri + ", alert='" + alert + '\''
                + ", colormode='" + colormode + '\'' + ", effect='" + effect + '\'' + ", effectSpeed=" + effectSpeed
                + ", ontime=" + ontime + ", hue=" + hue + ", sat=" + sat + ", ct=" + ct + ", xy=" + Arrays.toString(xy)
                + ", transitiontime=" + transitiontime + '}';
    }
}
