/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.toon.internal.api;

/**
 * The {@link ToonState} class defines the json object as received by the api.
 *
 * @author Jorg de Jong - Initial contribution
 */
public class ToonState {
    private ThermostatInfo thermostatInfo;
    private PowerUsage powerUsage;
    private GasUsage gasUsage;
    private Boolean success;
    private DeviceStatusInfo deviceStatusInfo;
    private DeviceConfigInfo deviceConfigInfo;

    public ThermostatInfo getThermostatInfo() {
        return thermostatInfo;
    }

    public void setThermostatInfo(ThermostatInfo thermostatInfo) {
        this.thermostatInfo = thermostatInfo;
    }

    public PowerUsage getPowerUsage() {
        return powerUsage;
    }

    public void setPowerUsage(PowerUsage powerUsage) {
        this.powerUsage = powerUsage;
    }

    public GasUsage getGasUsage() {
        return gasUsage;
    }

    public void setGasUsage(GasUsage gasUsage) {
        this.gasUsage = gasUsage;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public DeviceStatusInfo getDeviceStatusInfo() {
        return deviceStatusInfo;
    }

    public void setDeviceStatusInfo(DeviceStatusInfo deviceStatusInfo) {
        this.deviceStatusInfo = deviceStatusInfo;
    }

    public DeviceConfigInfo getDeviceConfigInfo() {
        return deviceConfigInfo;
    }

    public void setDeviceConfigInfo(DeviceConfigInfo deviceConfigInfo) {
        this.deviceConfigInfo = deviceConfigInfo;
    }

}
