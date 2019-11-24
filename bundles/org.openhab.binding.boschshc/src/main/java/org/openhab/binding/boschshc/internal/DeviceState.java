/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the state of a device as reported from the Smart Home Controller
 *
 *
 * @author Stefan Kästle
 *
 */
public class DeviceState {

    /**
     * ...."state":{
     * ......"@type":"powerSwitchState",
     * ......"automaticPowerOffTime":0,
     * ......"switchState":"ON"
     * ....},
     *
     */

    public DeviceState() {

    }

    public DeviceState(String type, String switchState, int autoPowerOff) {

        this.type = type;
        this.switchState = switchState;
        this.automaticPowerOffTime = autoPowerOff;
    }

    @SerializedName("@type")
    String type;

    int automaticPowerOffTime;

    String switchState;
}
