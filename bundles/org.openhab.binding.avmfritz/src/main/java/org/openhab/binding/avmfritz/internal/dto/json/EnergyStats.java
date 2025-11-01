/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.avmfritz.internal.dto.json;

import com.google.gson.annotations.SerializedName;

/**
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class EnergyStats {
    public static final double VOLTAGE_FACTOR = 0.01;
    public static final double POWER_FACTOR = 0.01;
    public static final double ENERGY_FACTOR = 0.001;

    @SerializedName("DeviceConnectState")
    public String deviceConnectState;
    @SerializedName("VoltageStat")
    public Values voltageStat;
    @SerializedName("MM_Value_Amp")
    public int mMValueAmp;
    @SerializedName("sum_Year")
    public int sumYear;
    @SerializedName("sum_Day")
    public int sumDay;
    @SerializedName("MM_Value_Volt")
    public int mMValueVolt;
    @SerializedName("MM_Value_Power")
    public int mMValuePower;
    @SerializedName("MM_Value_Energy")
    public int mMValueEnergy;
    @SerializedName("tabType")
    public String tabType;
    @SerializedName("DeviceID")
    public String deviceID;
    @SerializedName("DeviceSwitchState")
    public String deviceSwitchState;
    @SerializedName("EnergyStat")
    public Values energyStat;
    @SerializedName("CurrentDateInSec")
    public String currentDateInSec;
    @SerializedName("sum_Month")
    public int sumMonth;
    @SerializedName("RequestResult")
    public boolean requestResult;

    /**
     * Gets the scaled voltage value.
     *
     * @return the scaled voltage value
     */
    public double getScaledVoltage() {
        return mMValueVolt * VOLTAGE_FACTOR;
    }

    /**
     * Gets the scaled power value.
     *
     * @return the scaled power value
     */
    public double getScaledPower() {
        return mMValuePower * POWER_FACTOR;
    }

    /**
     * Gets the scaled energy value.
     *
     * @return the scaled energy value
     */
    public double getScaledEnergy() {
        return mMValueEnergy * ENERGY_FACTOR;
    }
}
