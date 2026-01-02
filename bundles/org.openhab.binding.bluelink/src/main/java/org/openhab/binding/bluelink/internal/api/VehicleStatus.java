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
package org.openhab.binding.bluelink.internal.api;

import java.time.Instant;
import java.util.List;

import org.openhab.binding.bluelink.internal.dto.us.VehicleStatusUS.RangeValue;
import org.openhab.binding.bluelink.internal.dto.us.VehicleStatusUS.SeatHeaterState;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * A unified abstraction for Vehicle Status across different API regions.
 *
 * @author Florian Hotze - Initial contribution
 */
public record VehicleStatus(Instant lastUpdate, VehicleLocation location, boolean engineOn, boolean doorsLocked,
        DoorState doorOpen, WindowState windowOpen, boolean trunkOpen, boolean hoodOpen, boolean airControlOn,
        State airTemp, boolean defrost, boolean steerWheelHeat, boolean sideBackWindowHeat, Boolean sideMirrorHeat,
        SeatHeaterState seatHeaterVent, int batterySoC, EvStatus evStatus, State fuelRange, Integer fuelLevel,
        Boolean lowFuelWarning, boolean washerFluidLow, boolean brakeOilWarning, boolean smartKeyBatteryWarning,
        TirePressureWarning tirePressureWarning, State odometer) {
    public record VehicleLocation(double latitude, double longitude, double altitude) {
    }

    public record DoorState(boolean frontLeft, boolean frontRight, boolean rearLeft, boolean rearRight) {
    }

    public record WindowState(boolean frontLeft, boolean frontRight, boolean rearLeft, boolean rearRight) {
    }

    public record EvStatus(boolean isCharging, int batterySoC, boolean isPluggedIn, List<TargetSoC> targetSoCs,
            RangeByFuel range, ChargeRemainingTime chargeRemainingTime) {
    }

    public record TargetSoC(String plugType, int level) {
    }

    public record RangeByFuel(State total, State ev, State gas) {
        public static RangeByFuel from(RangeValue total, RangeValue ev, RangeValue gas) {
            return new RangeByFuel(total != null ? total.getRange() : UnDefType.UNDEF,
                    ev != null ? ev.getRange() : UnDefType.UNDEF, gas != null ? gas.getRange() : UnDefType.UNDEF);
        }
    }

    public record ChargeRemainingTime(int currentMinutes, int fastMinutes, int portableMinutes, int stationMinutes) {
    }

    public record TirePressureWarning(boolean all, boolean frontLeft, boolean frontRight, boolean rearLeft,
            boolean rearRight) {
    }
}
