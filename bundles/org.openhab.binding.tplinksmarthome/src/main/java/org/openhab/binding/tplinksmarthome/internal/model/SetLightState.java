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
package org.openhab.binding.tplinksmarthome.internal.model;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.OnOffType;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Data class for setting the TP-Link Smart Light Strip state and retrieving the result.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class SetLightState implements HasErrorResponse {

    private static final int GROUPS_INDEX_HUE = 2;
    private static final int GROUPS_INDEX_SATURATION = 3;
    private static final int GROUPS_INDEX_BRIGHTNESS = 4;
    private static final int GROUPS_INDEX_COLOR_TEMPERATURE = 5;

    public static class ColorTemperature extends LightOnOff {
        @Expose(deserialize = false)
        private int colorTemp;
        @Expose(deserialize = false)
        private int hue = 0;
        @Expose(deserialize = false)
        private int saturation = 0;

        public void setColorTemp(final int colorTemperature) {
            this.colorTemp = colorTemperature;
        }
    }

    public static class Color extends Brightness {
        @Expose(deserialize = false)
        private int colorTemp;
        @Expose(deserialize = false)
        private int hue;
        @Expose(deserialize = false)
        private int saturation;

        public void setHue(final int hue) {
            this.hue = hue;
        }

        public void setSaturation(final int saturation) {
            this.saturation = saturation;
        }
    }

    public static class Brightness extends LightOnOff {
        @Expose(deserialize = false)
        private int brightness;

        public void setBrightness(final int brightness) {
            this.brightness = brightness;
        }
    }

    public static class LightOnOff extends ErrorResponse {
        @Expose
        private int onOff;
        @Expose(serialize = false)
        private String mode;
        @Expose(deserialize = false)
        private int transition;
        /**
         * groups contain status: [[0,31,0,0,73,5275]]
         * [?,?,hue,saturation,brightness,color_temp]
         */
        @Expose(serialize = false)
        protected int[][] groups;

        public OnOffType getOnOff() {
            return OnOffType.from(onOff == 1);
        }

        public void setOnOff(final OnOffType onOff) {
            this.onOff = onOff == OnOffType.ON ? 1 : 0;
        }

        public void setTransition(final int transition) {
            this.transition = transition;
        }

        public int getHue() {
            return groups[0][GROUPS_INDEX_HUE];
        }

        public int getSaturation() {
            return groups[0][GROUPS_INDEX_SATURATION];
        }

        public int getBrightness() {
            return groups[0][GROUPS_INDEX_BRIGHTNESS];
        }

        public int getColorTemperature() {
            return groups[0][GROUPS_INDEX_COLOR_TEMPERATURE];
        }

        public int[][] getGroups() {
            return groups;
        }

        @Override
        public String toString() {
            return "onOff:" + onOff + ", mode:" + mode + ", transition:" + transition + ", groups:"
                    + Arrays.toString(groups);
        }
    }

    public static class Context {
        @Expose
        private String source = "12345668-1234-1234-1234-123456789012";

        public String getSource() {
            return source;
        }

        public void setSource(final String source) {
            this.source = source;
        }
    }

    public static class LightingStrip {
        @Expose
        private LightOnOff setLightState;

        @Override
        public String toString() {
            return "setLightState:{" + setLightState + "}";
        }
    }

    @NonNullByDefault
    @SerializedName("smartlife.iot.lightStrip")
    @Expose
    private final LightingStrip strip = new LightingStrip();

    @Expose(deserialize = false)
    private Context context;

    public void setLightState(final LightOnOff lightState) {
        strip.setLightState = lightState;
    }

    public void setContext(final Context context) {
        this.context = context;
    }

    @Override
    public ErrorResponse getErrorResponse() {
        return strip.setLightState;
    }

    @Override
    public String toString() {
        return "SetLightState {strip:{" + strip + "}";
    }
}
