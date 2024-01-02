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
package org.openhab.binding.boschshc.internal.devices.bridge.dto;

import java.util.ArrayList;

import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;

/**
 * Response of the Controller for a Long Poll API call.
 *
 * @author Stefan Kästle - Initial contribution
 */
public class LongPollResult {

    /**
     * {"result":[
     * ..{
     * ...."path":"/devices/hdm:HomeMaticIP:3014F711A0001916D859A8A9/services/PowerSwitch",
     * ...."@type":"DeviceServiceData",
     * ...."id":"PowerSwitch",
     * ...."state":{
     * ......"@type":"powerSwitchState",
     * ......"switchState":"ON"
     * ....},
     * ...."deviceId":"hdm:HomeMaticIP:3014F711A0001916D859A8A9"}
     * ],"jsonrpc":"2.0"}
     */

    public ArrayList<BoschSHCServiceState> result;
    public String jsonrpc;
}
