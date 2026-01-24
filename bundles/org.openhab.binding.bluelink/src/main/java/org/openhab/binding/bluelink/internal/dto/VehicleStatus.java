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
package org.openhab.binding.bluelink.internal.dto;

import java.util.List;

import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.annotations.SerializedName;

/**
 * Vehicle status response from the Bluelink API.
 *
 * @author Marcus Better - Initial contribution
 */
public record VehicleStatus(VehicleStatusData vehicleStatus) {

    public record VehicleStatusData(String dateTime, VehicleLocation vehicleLocation, boolean engine, boolean doorLock,
            DoorStatus doorOpen, WindowStatus windowOpen, boolean trunkOpen, boolean hoodOpen, boolean airCtrlOn,
            AirTemperature airTemp, boolean defrost, int steerWheelHeat, int sideBackWindowHeat, int sideMirrorHeat,
            SeatHeaterState seatHeaterVentState, BatteryStatus battery, EvStatus evStatus, DrivingRange dte,
            int fuelLevel, boolean lowFuelLight, boolean washerFluidStatus,
            @SerializedName("breakOilStatus") boolean brakeOilStatus, boolean smartKeyBatteryWarning,
            @SerializedName("tirePressureLamp") TirePressureWarning tirePressureWarning) {
    }

    public record DoorStatus(int frontLeft, int frontRight, int backLeft, int backRight) {
    }

    public record WindowStatus(int frontLeft, int frontRight, int backLeft, int backRight) {
    }

    public record SeatHeaterState(@SerializedName("flSeatHeatState") int frontLeft,
            @SerializedName("frSeatHeatState") int frontRight, @SerializedName("rlSeatHeatState") int rearLeft,
            @SerializedName("rrSeatHeatState") int rearRight) {
    }

    /**
     * 12V battery status.
     */
    public record BatteryStatus(@SerializedName("batSoc") int stateOfCharge) {
    }

    /**
     * EV-specific status.
     */
    public record EvStatus(boolean batteryCharge, int batteryStatus, int batteryPlugin,
            ReserveChargeInfo reservChargeInfos, List<DrivingDistance> drvDistance, ChargeRemainingTime remainTime2) {
    }

    /**
     * Charge limit and scheduling info.
     */
    public record ReserveChargeInfo(@SerializedName("targetSOCList") List<TargetSOC> targetSocList) {
    }

    /**
     * Target state of charge setting.
     */
    public record TargetSOC(int plugType, // 0 = DC, 1 = AC
            @SerializedName("targetSOCLevel") int targetSocLevel) {
    }

    public record DrivingDistance(RangeByFuel rangeByFuel) {
    }

    /**
     * Range by fuel type.
     */
    public record RangeByFuel(RangeValue totalAvailableRange, RangeValue evModeRange, RangeValue gasModeRange) {
    }

    /**
     * Range value with unit.
     */
    public record RangeValue(double value, int unit) {

        public State getRange() {
            return switch (unit) {
                case 1 -> new QuantityType<>(value * 1000, SIUnits.METRE);
                case 2, 3 -> new QuantityType<>(value, ImperialUnits.MILE);
                default -> UnDefType.UNDEF;
            };
        }
    }

    public record ChargeRemainingTime(
            // Current
            TimeValue atc,
            // Fast
            TimeValue etc1,
            // Portable
            TimeValue etc2,
            // Station
            TimeValue etc3) {
    }

    public record TimeValue(int value, int unit) {
    }

    /**
     * DTE (Distance to Empty) for non-EV.
     */
    public record DrivingRange(double value, int unit) {
    }

    public record TirePressureWarning(@SerializedName("tirePressureWarningLampAll") int all,
            @SerializedName("tirePressureWarningLampFrontLeft") int frontLeft,
            @SerializedName("tirePressureWarningLampFrontRight") int frontRight,
            @SerializedName("tirePressureWarningLampRearLeft") int rearLeft,
            @SerializedName("tirePressureWarningLampRearRight") int rearRight) {
    }
}
