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

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Data class to set the Brightness of a Smart Home dimmer (HS220).
 * Setter methods for data and getter for error response.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class SetBrightness implements HasErrorResponse {

    public static class Brightness extends ErrorResponse {
        @Expose(deserialize = false)
        private int brightness;

        @Override
        public String toString() {
            return "brightness:" + brightness + super.toString();
        }
    }

    public static class Dimmer {
        @Expose
        private Brightness setBrightness = new Brightness();

        @Override
        public String toString() {
            return "set_brightness:{" + setBrightness + "}";
        }
    }

    @Expose
    @SerializedName("smartlife.iot.dimmer")
    private Dimmer dimmer = new Dimmer();

    @Override
    public ErrorResponse getErrorResponse() {
        return dimmer.setBrightness;
    }

    public void setBrightness(int brightness) {
        dimmer.setBrightness.brightness = brightness;
    }

    @Override
    public String toString() {
        return "smartlife.iot.dimmer:{" + dimmer + "}";
    }
}
