/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.blebox.devices;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.binding.blebox.BleboxBindingConstants;

/**
 * The {@link SwitchBox} class defines a logic for SwitchBox device
 *
 * @author Szymon Tokarski - Initial contribution
 */
public class SwitchBox extends BaseDevice {
    public static final String SET_URL = "/s/";
    public static final String STATE_URL = "/api/relay/state";

    public SwitchBox(String ipAddress) {
        super(BleboxBindingConstants.SWITCHBOX, ipAddress);
    }

    public SwitchBox(String itemType, String ipAddress) {
        super(itemType, ipAddress);
    }

    public class StateResponse extends BaseResponse {

        public Relay[] relays;

        @Override
        public String getRootElement() {
            return null;
        }

    }

    public class Relay extends BaseResponse {
        public int relay;
        public int state;
        public int stateAfterRestart;

        @Override
        public String getRootElement() {
            return null;
        }
    }

    public void setSwitchState(OnOffType onOff) {
        String url = SET_URL + (onOff.equals(OnOffType.ON) ? "1" : "0");

        getJson(url, StateResponse.class, null);
    }

    public OnOffType getSwitchState(int switchIndex) {
        Relay[] response = getJsonArray(STATE_URL, Relay[].class, null);

        if (response != null && response.length > switchIndex) {
            return response[switchIndex].state > 0 ? OnOffType.ON : OnOffType.OFF;
        }

        return null;
    }
}
