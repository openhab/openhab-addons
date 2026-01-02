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
package org.openhab.binding.bluelink.internal.dto.us;

import java.util.List;

import org.openhab.binding.bluelink.internal.dto.AirTemperature;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.annotations.SerializedName;

/**
 * Vehicle status response from the US API.
 *
 * @author Marcus Better - Initial contribution
 */
public record VehicleStatusUS(VehicleStatusData vehicleStatus) {

    public record VehicleStatusData(String dateTime, VehicleLocation vehicleLocation, boolean engine, boolean doorLock,
            DoorStatus doorOpen, WindowStatus windowOpen, boolean trunkOpen, boolean hoodOpen, boolean airCtrlOn,
            AirTemperature airTemp, boolean defrost, int steerWheelHeat, int sideBackWindowHeat, int sideMirrorHeat,
            SeatHeaterState seatHeaterVentState, BatteryStatus battery, EvStatus evStatus, RangeValue dte,
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
            ReserveChargeInfo reservChargeInfos, List<DrivingRange> drvDistance, ChargeRemainingTime remainTime2) {
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

    /**
     * Range by fuel type.
     */
    public record RangeByFuel(@SerializedName("totalAvailableRange") RangeValue total,
            @SerializedName("evModeRange") RangeValue ev, @SerializedName("gasModeRange") RangeValue gas) {
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

    public record ChargeRemainingTime(@SerializedName("atc") ChargeRemainingTime.TimeValue current,
            @SerializedName("etc1") ChargeRemainingTime.TimeValue fast,
            @SerializedName("etc2") ChargeRemainingTime.TimeValue portable,
            @SerializedName("etc3") ChargeRemainingTime.TimeValue station) {
        public record TimeValue(int value, int unit) {
        }
    }

    public record DrivingRange(@SerializedName("rangeByFuel") RangeByFuel rangeByFuel) {
    }

    public record TirePressureWarning(@SerializedName("tirePressureWarningLampAll") int all,
            @SerializedName("tirePressureWarningLampFrontLeft") int frontLeft,
            @SerializedName("tirePressureWarningLampFrontRight") int frontRight,
            @SerializedName("tirePressureWarningLampRearLeft") int rearLeft,
            @SerializedName("tirePressureWarningLampRearRight") int rearRight) {
    }
}
