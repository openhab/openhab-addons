/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.tellstick.device.iface.Device;
import org.tellstick.enums.DeviceType;

import com.google.gson.annotations.SerializedName;

/**
 * Class used to deserialize JSON from Telldus local API.
 *
 * @author Jan Gustafsson - Initial contribution
 */
public class TellstickLocalDeviceDTO implements Device {

    @SerializedName("id")
    private int deviceId;
    private int methods;
    private String name;
    private int state;
    private String statevalue;
    private String type;
    private String protocol;
    private String model;
    private boolean updated;

    public void setUpdated(boolean b) {
        this.updated = b;
    }

    public boolean isUpdated() {
        return updated;
    }

    @Override
    public int getId() {
        return deviceId;
    }

    public void setId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getMethods() {
        return methods;
    }

    public void setMethods(int methods) {
        this.methods = methods;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getUUId() {
        return Integer.toString(deviceId);
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public String getModel() {
        return model;
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.DEVICE;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getStatevalue() {
        return statevalue;
    }

    public void setStatevalue(String statevalue) {
        this.statevalue = statevalue;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
