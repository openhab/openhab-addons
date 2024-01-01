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
package org.openhab.binding.fronius.internal.api;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link InverterRealtimeBodyData} is responsible for storing
 * the "data" node of the JSON response
 *
 * @author Thomas Rokohl - Initial contribution
 */
public class InverterRealtimeBodyData {

    @SerializedName("DAY_ENERGY")
    private ValueUnit dayEnergy;
    @SerializedName("FAC")
    private ValueUnit fac;
    @SerializedName("IAC")
    private ValueUnit iac;
    @SerializedName("IDC")
    private ValueUnit idc;
    @SerializedName("IDC_2")
    private ValueUnit idc2;
    @SerializedName("IDC_3")
    private ValueUnit idc3;
    @SerializedName("PAC")
    private ValueUnit pac;
    @SerializedName("TOTAL_ENERGY")
    private ValueUnit totalEnergy;
    @SerializedName("UAC")
    private ValueUnit uac;
    @SerializedName("UDC")
    private ValueUnit udc;
    @SerializedName("UDC_2")
    private ValueUnit udc2;
    @SerializedName("UDC_3")
    private ValueUnit udc3;
    @SerializedName("YEAR_ENERGY")
    private ValueUnit yearEnergy;
    @SerializedName("DeviceStatus")
    private DeviceStatus deviceStatus;

    public ValueUnit getDayEnergy() {
        return dayEnergy;
    }

    public void setDayEnergy(ValueUnit dayEnergy) {
        this.dayEnergy = dayEnergy;
    }

    public ValueUnit getPac() {
        return pac;
    }

    public void setPac(ValueUnit pac) {
        this.pac = pac;
    }

    public ValueUnit getTotalEnergy() {
        return totalEnergy;
    }

    public void setTotalEnergy(ValueUnit totalEnergy) {
        this.totalEnergy = totalEnergy;
    }

    public ValueUnit getYearEnergy() {
        return yearEnergy;
    }

    public void setYearEnergy(ValueUnit yearEnergy) {
        this.yearEnergy = yearEnergy;
    }

    public DeviceStatus getDeviceStatus() {
        if (deviceStatus == null) {
            deviceStatus = new DeviceStatus();
        }
        return deviceStatus;
    }

    public void setDeviceStatus(DeviceStatus deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    public ValueUnit getFac() {
        return fac;
    }

    public void setFac(ValueUnit fac) {
        this.fac = fac;
    }

    public ValueUnit getIac() {
        return iac;
    }

    public void setIac(ValueUnit iac) {
        this.iac = iac;
    }

    public ValueUnit getIdc() {
        return idc;
    }

    public void setIdc(ValueUnit idc) {
        this.idc = idc;
    }

    public ValueUnit getIdc2() {
        return idc2;
    }

    public ValueUnit getIdc3() {
        return idc3;
    }

    public ValueUnit getUac() {
        return uac;
    }

    public void setUac(ValueUnit uac) {
        this.uac = uac;
    }

    public ValueUnit getUdc() {
        return udc;
    }

    public void setUdc(ValueUnit udc) {
        this.udc = udc;
    }

    public ValueUnit getUdc2() {
        return udc2;
    }

    public ValueUnit getUdc3() {
        return udc3;
    }
}
