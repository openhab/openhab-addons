/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.fronius.internal.api.dto.storage;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link StorageController} is responsible for storing the "Controller" node
 * of the {@link StorageRealtimeResponse}.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class StorageController {
    @SerializedName("Capacity_Maximum")
    private double capacityMaximum;

    @SerializedName("Current_DC")
    private double currentDC;

    @SerializedName("DesignedCapacity")
    private double designedCapacity;

    @SerializedName("Details")
    private @Nullable StorageDetails details;

    @SerializedName("Enable")
    private int enable;

    @SerializedName("StateOfCharge_Relative")
    private double stateOfChargeRelative;

    @SerializedName("Status_BatteryCell")
    private @Nullable String statusBatteryCell;

    @SerializedName("Temperature_Cell")
    private double temperatureCell;

    @SerializedName("TimeStamp")
    private int timeStamp;

    @SerializedName("Voltage_DC")
    private double voltageDC;

    public double getCapacityMaximum() {
        return capacityMaximum;
    }

    public double getCurrentDC() {
        return currentDC;
    }

    public double getDesignedCapacity() {
        return designedCapacity;
    }

    public @Nullable StorageDetails getDetails() {
        return details;
    }

    public int getEnable() {
        return enable;
    }

    public double getStateOfChargeRelative() {
        return stateOfChargeRelative;
    }

    public @Nullable String getStatusBatteryCell() {
        return statusBatteryCell;
    }

    public double getTemperatureCell() {
        return temperatureCell;
    }

    public int getTimeStamp() {
        return timeStamp;
    }

    public double getVoltageDC() {
        return voltageDC;
    }
}
