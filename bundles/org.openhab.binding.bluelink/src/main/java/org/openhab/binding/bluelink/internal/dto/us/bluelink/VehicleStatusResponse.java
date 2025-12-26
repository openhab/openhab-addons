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
package org.openhab.binding.bluelink.internal.dto.us.bluelink;

import org.openhab.binding.bluelink.internal.dto.BatteryStatus;
import org.openhab.binding.bluelink.internal.dto.CommonVehicleStatus;
import org.openhab.binding.bluelink.internal.dto.DoorStatus;
import org.openhab.binding.bluelink.internal.dto.DrivingRange;
import org.openhab.binding.bluelink.internal.dto.EvStatus;
import org.openhab.binding.bluelink.internal.dto.SeatHeaterState;
import org.openhab.binding.bluelink.internal.dto.TirePressureWarnings;

import com.google.gson.annotations.SerializedName;

/**
 * Vehicle status response from the Bluelink API.
 *
 * @author Marcus Better - Initial contribution
 */
public record VehicleStatusResponse(VehicleStatus result) {

    public record VehicleStatus(VehicleStatusData vehicleStatus) {

        public record VehicleStatusData(String dateTime, VehicleLocation vehicleLocation, @Override boolean engine,
                @Override boolean doorLock, @Override DoorStatus doorOpen, @Override DoorStatus windowOpen,
                @Override boolean trunkOpen, @Override boolean hoodOpen, @Override boolean airCtrlOn,
                @Override AirTemperature airTemp, @Override boolean defrost, @Override int steerWheelHeat,
                @Override int sideBackWindowHeat, @Override int sideMirrorHeat,
                @Override SeatHeaterState seatHeaterVentState, @Override BatteryStatus battery,
                @Override EvStatus evStatus, @Override DrivingRange dte, @Override int fuelLevel,
                @Override boolean lowFuelLight, @Override boolean washerFluidStatus,
                @Override @SerializedName("breakOilStatus") boolean brakeOilStatus, boolean smartKeyBatteryWarning,
                @Override @SerializedName("tirePressureLamp") TirePressureWarning tirePressureWarning)
                implements
                    CommonVehicleStatus {

            public record TirePressureWarning(@Override @SerializedName("tirePressureWarningLampAll") int all,
                    @Override @SerializedName("tirePressureWarningLampFrontLeft") int frontLeft,
                    @Override @SerializedName("tirePressureWarningLampFrontRight") int frontRight,
                    @Override @SerializedName("tirePressureWarningLampRearLeft") int rearLeft,
                    @Override @SerializedName("tirePressureWarningLampRearRight") int rearRight)
                    implements
                        TirePressureWarnings {
            }
        }
    }
}
