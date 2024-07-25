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
package org.openhab.binding.fronius.internal.api.dto.inverter;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fronius.internal.api.dto.ValueUnit;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link InverterRealtimeBodyData} is responsible for storing
 * the "Data" node of the {@link InverterRealtimeBody}.
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
    private InverterDeviceStatus deviceStatus;

    public ValueUnit getDayEnergy() {
        return dayEnergy;
    }

    public ValueUnit getPac() {
        return pac;
    }

    public ValueUnit getTotalEnergy() {
        return totalEnergy;
    }

    public ValueUnit getYearEnergy() {
        return yearEnergy;
    }

    public @Nullable InverterDeviceStatus getDeviceStatus() {
        return deviceStatus;
    }

    public ValueUnit getFac() {
        return fac;
    }

    public ValueUnit getIac() {
        return iac;
    }

    public ValueUnit getIdc() {
        return idc;
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

    public ValueUnit getUdc() {
        return udc;
    }

    public ValueUnit getUdc2() {
        return udc2;
    }

    public ValueUnit getUdc3() {
        return udc3;
    }
}
