/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mihome.test

import com.google.gson.annotations.SerializedName

/**
 * JSON representation of a subdevice used in the communication with the server.
 *
 * @author Svilen Valkanov
 */
public class JsonSubdevice extends JsonDevice {

    public static final String DEFAULT_LABEL = "New Subdevice"

    // ID of the gateway that the subdevice is paired to
    @SerializedName("device_id")
    int deviceID

    //For open and motion sensors
    @SerializedName("sensor_state")
    Integer sensorState

    //Only for energy monitor
    @SerializedName("voltage")
    double voltage

    @SerializedName("real_power")
    int realPower

    @SerializedName("today_wh")
    int todayWh

    JsonSubdevice(int id, int gatewayID, String type) {
        super(type, id, DEFAULT_LABEL);
        this.deviceID = gatewayID
    }

    JsonSubdevice(int id, int gatewayID, String type, Integer state) {
        super(type, id, DEFAULT_LABEL)
        this.deviceID = gatewayID
        this.sensorState = state
    }

    JsonSubdevice(int id, int gatewayID ,String type, double voltage, int power, int wh) {
        super(type, id, DEFAULT_LABEL)
        this.deviceID = gatewayID
        this.voltage = voltage
        this.realPower = power
        this.todayWh = wh
    }
}
