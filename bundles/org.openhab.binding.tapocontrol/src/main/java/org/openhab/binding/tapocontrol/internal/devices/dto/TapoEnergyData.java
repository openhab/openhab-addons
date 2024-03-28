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
package org.openhab.binding.tapocontrol.internal.devices.dto;

import java.time.ZonedDateTime;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Tapo-Energy-Monitor Structure Class
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoEnergyData {
    @SerializedName("local_time")
    @Expose(serialize = false, deserialize = true)
    private String localTime = "";

    @SerializedName("current_power")
    @Expose(serialize = false, deserialize = true)
    private int currentPower = 0;

    @SerializedName("electricity_charge")
    @Expose(serialize = false, deserialize = true)
    private List<Integer> electricityCharge = List.of();

    @SerializedName("today_runtime")
    @Expose(serialize = false, deserialize = true)
    private int todayRuntime = 0;

    @SerializedName("today_energy")
    @Expose(serialize = false, deserialize = true)
    private int todayEnergy = 0;

    @SerializedName("month_energy")
    @Expose(serialize = false, deserialize = true)
    private int monthEnergy = 0;

    @SerializedName("month_runtime")
    @Expose(serialize = false, deserialize = true)
    private int monthRuntime = 0;

    /***********************************
     *
     * GET VALUES
     *
     ************************************/

    public ZonedDateTime getLocalDate() {
        return ZonedDateTime.parse(localTime);
    }

    public double getCurrentPower() {
        return (double) currentPower / 1000;
    }

    public List<Integer> getElectricityCharge() {
        return electricityCharge;
    }

    public int getTodayEnergy() {
        return todayEnergy;
    }

    public int getMonthEnergy() {
        return monthEnergy;
    }

    public int getTodayRuntime() {
        return todayRuntime;
    }

    public int getMonthRuntime() {
        return monthRuntime;
    }
}
