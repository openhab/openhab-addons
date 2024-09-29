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

/**
 * Data class for setting the TP-Link Smart Plug state and retrieving the result.
 * Only setter methods as the values are set by gson based on the retrieved json.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class SetRelayState extends ContextState implements HasErrorResponse {

    public static class RelayState extends ErrorResponse {
        @Expose(deserialize = false)
        private int state;

        @Override
        public String toString() {
            return "state:" + state;
        }
    }

    public static class System {
        @Expose
        private RelayState setRelayState = new RelayState();

        public void setRelayState(OnOffType onOff) {
            setRelayState.state = onOff == OnOffType.ON ? 1 : 0;
        }

        @Override
        public String toString() {
            return "set_relay_state:{" + setRelayState + "}";
        }
    }

    @Expose
    private System system = new System();

    @Override
    public ErrorResponse getErrorResponse() {
        return system.setRelayState;
    }

    public void setRelayState(OnOffType onOff) {
        system.setRelayState(onOff);
    }

    @Override
    public String toString() {
        return "SetRelayState {system:{" + system + "}";
    }
}
