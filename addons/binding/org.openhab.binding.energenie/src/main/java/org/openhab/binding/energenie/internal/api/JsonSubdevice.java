/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.energenie.internal.api;

import com.google.gson.annotations.SerializedName;

/**
 * JSON representation of a subdevice used in the communication with the server.
 *
 * @author Svilen Valkanov - Initial contribution
 */
public class JsonSubdevice extends JsonDevice {
    public static final String DEFAULT_LABEL = "New Subdevice";

    // ID of the gateway that the subdevice is paired to
    @SerializedName("device_id")
    int parentID;

    // For open and motion sensors
    @SerializedName("sensor_state")
    Integer sensorState;

    // Only for energy monitor
    @SerializedName("voltage")
    double voltage;

    @SerializedName("real_power")
    int realPower;

    @SerializedName("today_wh")
    int todayWh;

    public JsonSubdevice(int id, int gatewayID, EnergenieDeviceTypes type) {
        super(type, id, DEFAULT_LABEL);
        this.parentID = gatewayID;
    }

    public JsonSubdevice(int id, int gatewayID, EnergenieDeviceTypes type, Integer state) {
        super(type, id, DEFAULT_LABEL);
        this.parentID = gatewayID;
        this.sensorState = state;
    }

    public JsonSubdevice(int id, int gatewayID, EnergenieDeviceTypes type, double voltage, int power, int wh) {
        super(type, id, DEFAULT_LABEL);
        this.parentID = gatewayID;
        this.voltage = voltage;
        this.realPower = power;
        this.todayWh = wh;
    }

    public int getParentID() {
        return parentID;
    }

    public void setParentID(int parentID) {
        this.parentID = parentID;
    }

    public Integer getSensorState() {
        return sensorState;
    }

    public void setSensorState(Integer sensorState) {
        this.sensorState = sensorState;
    }

    public double getVoltage() {
        return voltage;
    }

    public void setVoltage(double voltage) {
        this.voltage = voltage;
    }

    public int getRealPower() {
        return realPower;
    }

    public void setRealPower(int realPower) {
        this.realPower = realPower;
    }

    public int getTodayWh() {
        return todayWh;
    }

    public void setTodayWh(int todayWh) {
        this.todayWh = todayWh;
    }
}
