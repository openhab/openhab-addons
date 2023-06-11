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
package org.openhab.binding.tplinksmarthome.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.OnOffType;

import com.google.gson.annotations.SerializedName;

/**
 * Data class to setting different kind of states on Smart Home light bulbs.
 * Only setter methods as the object is only used to send values.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class TransitionLightState {

    /**
     * Color sets hue, saturation and brightness.
     * color temperature present to send with default 0 value to bulb.
     */
    public static class LightStateColor extends LightStateBrightness {
        private int colorTemp;
        private int hue;
        private int saturation;

        public void setHue(int hue) {
            this.hue = hue;
        }

        public void setSaturation(int saturation) {
            this.saturation = saturation;
        }

        @Override
        public String toString() {
            return "colorTemp" + colorTemp + ", hue:" + hue + ", saturation:" + saturation + ", " + super.toString();
        }
    }

    /**
     * Color Temperature doesn't set brightness therefore separate class.
     * hue and saturation present to send with default 0 value to bulb.
     */
    public static class LightStateColorTemperature extends LightOnOff {
        private int colorTemp;
        private int hue;
        private int saturation;

        public void setColorTemperature(int colorTemp) {
            this.colorTemp = colorTemp;
        }

        @Override
        public String toString() {
            return "colorTemp" + colorTemp + ", hue:" + hue + ", saturation:" + saturation + ", " + super.toString();
        }
    }

    public static class LightStateBrightness extends LightOnOff {
        private int brightness;

        public void setBrightness(int brightness) {
            this.brightness = brightness;
        }

        @Override
        public String toString() {
            return "brightness:" + brightness + ", " + super.toString();
        }
    }

    public static class LightOnOff {
        private int onOff;
        private int ignoreDefault = 1;
        private String mode = "normal";
        private int transitionPeriod;

        public void setOnOff(OnOffType onOff) {
            this.onOff = onOff == OnOffType.ON ? 1 : 0;
        }

        public void setTransitionPeriod(int transitionPeriod) {
            this.transitionPeriod = transitionPeriod;
        }

        @Override
        public String toString() {
            return "onOff:" + onOff + ", ignoreDefault:" + ignoreDefault + ", mode:" + mode + ", transitionPeriod:"
                    + transitionPeriod;
        }
    }

    public static class LightingService {
        private LightOnOff transitionLightState;

        @Override
        public String toString() {
            return "transitionLightState:{" + transitionLightState + "}";
        }
    }

    @NonNullByDefault
    @SerializedName("smartlife.iot.smartbulb.lightingservice")
    private LightingService service = new LightingService();

    public void setLightState(LightOnOff lightState) {
        service.transitionLightState = lightState;
    }

    @Override
    public String toString() {
        return "TransitionLightState {service:{" + service + "}";
    }
}
