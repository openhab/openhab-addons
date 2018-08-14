/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
    @SerializedName("PAC")
    private ValueUnit pac;
    @SerializedName("TOTAL_ENERGY")
    private ValueUnit totalEnergy;
    @SerializedName("UAC")
    private ValueUnit uac;
    @SerializedName("UDC")
    private ValueUnit udc;
    @SerializedName("YEAR_ENERGY")
    private ValueUnit yearEnergy;
    @SerializedName("DeviceStatus")
    private DeviceStatus deviceStatus;

    public ValueUnit getDayEnergy() {
        if (dayEnergy == null) {
            dayEnergy = new ValueUnit();
        }
        return dayEnergy;
    }

    public void setDayEnergy(ValueUnit dayEnergy) {
        this.dayEnergy = dayEnergy;
    }

    public ValueUnit getPac() {
        if (pac == null) {
            pac = new ValueUnit();
        }
        return pac;
    }

    public void setPac(ValueUnit pac) {
        this.pac = pac;
    }

    public ValueUnit getTotalEnergy() {
        if (totalEnergy == null) {
            totalEnergy = new ValueUnit();
        }
        return totalEnergy;
    }

    public void setTotalEnergy(ValueUnit totalEnergy) {
        this.totalEnergy = totalEnergy;
    }

    public ValueUnit getYearEnergy() {
        if (yearEnergy == null) {
            yearEnergy = new ValueUnit();
        }
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
        if (fac == null) {
            fac = new ValueUnit();
        }
        return fac;
    }

    public void setFac(ValueUnit fac) {
        this.fac = fac;
    }

    public ValueUnit getIac() {
        if (iac == null) {
            iac = new ValueUnit();
        }
        return iac;
    }

    public void setIac(ValueUnit iac) {
        this.iac = iac;
    }

    public ValueUnit getIdc() {
        if (idc == null) {
            idc = new ValueUnit();
        }
        return idc;
    }

    public void setIdc(ValueUnit idc) {
        this.idc = idc;
    }

    public ValueUnit getUac() {
        if (uac == null) {
            uac = new ValueUnit();
        }
        return uac;
    }

    public void setUac(ValueUnit uac) {
        this.uac = uac;
    }

    public ValueUnit getUdc() {
        if (udc == null) {
            udc = new ValueUnit();
        }
        return udc;
    }

    public void setUdc(ValueUnit udc) {
        this.udc = udc;
    }

}
