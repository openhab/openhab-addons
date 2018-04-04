/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tplinksmarthome.internal.model;

import org.eclipse.smarthome.core.library.types.OnOffType;

import com.google.gson.annotations.Expose;

/**
 * Data class for setting the tp-Link Smart Plug state and retrieving the result.
 * Only setter methods as the values are set by gson based on the retrieved json.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class SetRelayState implements HasErrorResponse {

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
        system.setRelayState.state = onOff == OnOffType.ON ? 1 : 0;
    }

    @Override
    public String toString() {
        return "SetRelayState {system:{" + system + "}";
    }
}
