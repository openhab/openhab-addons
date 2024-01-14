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
package org.openhab.binding.tplinksmarthome.internal.model;

import com.google.gson.annotations.SerializedName;

/**
 * Data class for getting the response from the Light bulb. This class is similar to {@link TransitionLightState} but
 * has no subclasses for the light state. The other class has and makes it difficult to use to deserialize by gson.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class TransitionLightStateResponse implements HasErrorResponse {

    public static class LightingService {
        private LightState transitionLightState;

        @Override
        public String toString() {
            return "LightingService:{" + transitionLightState + "}";
        }
    }

    @SerializedName("smartlife.iot.smartbulb.lightingservice")
    private LightingService service = new LightingService();

    @Override
    public ErrorResponse getErrorResponse() {
        return service.transitionLightState;
    }

    @Override
    public String toString() {
        return "TransitionLightStateResponse {service:{" + service + "}";
    }
}
