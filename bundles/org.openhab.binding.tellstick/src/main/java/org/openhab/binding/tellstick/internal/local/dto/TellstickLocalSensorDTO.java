/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.tellstick.internal.local.dto;

import java.util.List;

import org.openhab.binding.tellstick.internal.live.xml.LiveDataType;
import org.tellstick.device.iface.Device;
import org.tellstick.enums.DeviceType;

import com.google.gson.annotations.SerializedName;

/**
 * Class used to deserialize JSON from Telldus local API.
 *
 * @author Jan Gustafsson - Initial contribution
 */
public class TellstickLocalSensorDTO implements Device {

    private int battery;
    private boolean updated;
    private List<LocalDataTypeValueDTO> data = null;
    @SerializedName("id")
    private int deviceId;
    private String model;
    private String name;
    private String protocol;
    private int sensorId;

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public List<LocalDataTypeValueDTO> getData() {
        return data;
    }

    public void setData(List<LocalDataTypeValueDTO> data) {
        this.data = data;
    }

    @Override
    public int getId() {
        return deviceId;
    }

    public void setId(int id) {
        this.deviceId = id;
    }

    @Override
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setUpdated(boolean b) {
        this.updated = b;
    }

    public boolean isUpdated() {
        return updated;
    }

    public boolean isSensorOfType(LiveDataType type) {
        boolean res = false;
        if (data != null) {
            for (LocalDataTypeValueDTO val : data) {
                if (val.getName() == type) {
                    res = true;
                    break;
                }
            }
        }
        return res;
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.SENSOR;
    }

    public int getSensorId() {
        return sensorId;
    }

    public void setSensorId(int sensorId) {
        this.sensorId = sensorId;
    }

    @Override
    public String getUUId() {
        return Integer.toString(deviceId);
    }
}
