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
package org.openhab.binding.hue.internal.api.dto.clip1;

import java.util.Arrays;

/**
 * Current state of light.
 *
 * @author Q42 - Initial contribution
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding
 * @author Laurent Garnier - add few methods to update the object
 */
public class State {
    private boolean on;
    public int bri;
    public int hue;
    public int sat;
    private float[] xy;
    public int ct;
    private String alert;
    private String effect;
    public String colormode;
    private boolean reachable;

    public State() {
    }

    /**
     * Color modes of a light.
     */
    public enum ColorMode {
        /**
         * CIE color space coordinates
         */
        XY,

        /**
         * Hue and saturation
         */
        HS,

        /**
         * Color temperature in mired
         */
        CT
    }

    /**
     * Alert modes of a light.
     */
    public enum AlertMode {
        /**
         * Light is not performing alert effect
         */
        NONE,

        /**
         * Light is performing one breathe cycle
         */
        SELECT,

        /**
         * Light is performing breathe cycles for 30 seconds (unless cancelled)
         */
        LSELECT
    }

    /**
     * Effects possible for a light.
     */
    public enum Effect {
        /**
         * No effect
         */
        NONE,

        /**
         * Cycle through all hues with current saturation and brightness
         */
        COLORLOOP
    }

    /**
     * Returns the on state.
     *
     * @return true if the light is on, false if it isn't
     */
    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    /**
     * Returns the brightness.
     *
     * @return brightness
     */
    public int getBrightness() {
        return bri;
    }

    public void setBri(int bri) {
        this.bri = bri;
    }

    /**
     * Returns the hue.
     *
     * @return hue
     */
    public int getHue() {
        return hue;
    }

    public void setHue(int hue) {
        this.hue = hue;
    }

    /**
     * Returns the saturation.
     *
     * @return saturation
     */
    public int getSaturation() {
        return sat;
    }

    public void setSaturation(int sat) {
        this.sat = sat;
    }

    /**
     * Returns the coordinates in CIE color space.
     *
     * @return cie color spaces coordinates
     */
    public float[] getXY() {
        return xy;
    }

    public void setXY(float[] xy) {
        this.xy = xy;
    }

    /**
     * Returns the color temperature.
     *
     * @return color temperature
     */
    public int getColorTemperature() {
        return ct;
    }

    public void setColorTemperature(int ct) {
        this.ct = ct;
    }

    /**
     * Returns the last alert mode set.
     * Future firmware updates may change this to actually report the current alert mode.
     *
     * @return last alert mode
     */
    public AlertMode getAlertMode() {
        if (alert == null) {
            return null;
        }
        return AlertMode.valueOf(alert.toUpperCase());
    }

    /**
     * Returns the current color mode.
     *
     * @return current color mode
     */
    public ColorMode getColorMode() {
        if (colormode == null) {
            return null;
        }
        return ColorMode.valueOf(colormode.toUpperCase());
    }

    public void setColormode(ColorMode colormode) {
        this.colormode = colormode.name();
    }

    /**
     * Returns the current active effect.
     *
     * @return current active effect
     */
    public Effect getEffect() {
        if (effect == null) {
            return null;
        }
        return Effect.valueOf(effect.toUpperCase());
    }

    /**
     * Returns reachability.
     *
     * @return true if reachable, false if it isn't
     */
    public boolean isReachable() {
        return reachable;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((alert == null) ? 0 : alert.hashCode());
        result = prime * result + bri;
        result = prime * result + ((colormode == null) ? 0 : colormode.hashCode());
        result = prime * result + ct;
        result = prime * result + ((effect == null) ? 0 : effect.hashCode());
        result = prime * result + hue;
        result = prime * result + (on ? 1231 : 1237);
        result = prime * result + (reachable ? 1231 : 1237);
        result = prime * result + sat;
        result = prime * result + Arrays.hashCode(xy);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        State other = (State) obj;
        if (alert == null) {
            if (other.alert != null) {
                return false;
            }
        } else if (!alert.equals(other.alert)) {
            return false;
        }
        if (bri != other.bri) {
            return false;
        }
        if (colormode == null) {
            if (other.colormode != null) {
                return false;
            }
        } else if (!colormode.equals(other.colormode)) {
            return false;
        }
        if (ct != other.ct) {
            return false;
        }
        if (effect == null) {
            if (other.effect != null) {
                return false;
            }
        } else if (!effect.equals(other.effect)) {
            return false;
        }
        if (hue != other.hue) {
            return false;
        }
        if (on != other.on) {
            return false;
        }
        if (reachable != other.reachable) {
            return false;
        }
        if (sat != other.sat) {
            return false;
        }
        return Arrays.equals(xy, other.xy);
    }
}
