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

import org.openhab.core.library.types.OnOffType;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Data class for setting the TP-Link Smart Dimmer (HS220) state and retrieving the result.
 * Only setter methods as the values are set by gson based on the retrieved json.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class SetSwitchState implements HasErrorResponse {

    public static class State extends ErrorResponse {
        @Expose(deserialize = false)
        private int state;

        @Override
        public String toString() {
            return "state:" + state;
        }
    }

    public static class Dimmer {
        @Expose
        private State setSwitchState = new State();

        @Override
        public String toString() {
            return "set_switch_state:{" + setSwitchState + "}";
        }
    }

    @Expose
    @SerializedName("smartlife.iot.dimmer")
    private Dimmer dimmer = new Dimmer();

    @Override
    public ErrorResponse getErrorResponse() {
        return dimmer.setSwitchState;
    }

    public void setSwitchState(OnOffType onOff) {
        dimmer.setSwitchState.state = onOff == OnOffType.ON ? 1 : 0;
    }

    @Override
    public String toString() {
        return "SetSwitchState {smartlife.iot.dimmer:{" + dimmer + "}";
    }
}
