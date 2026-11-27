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
package org.openhab.binding.pleos.internal.api.model;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonElement;

/**
 * Data model for a Pleos vehicle.
 *
 * @author Stephan Sandmann - Initial contribution
 */
@NonNullByDefault
public class PleosVehicleModels {

    // Reusable component for doors and seats ([0]=Front, [1]=Rear)
    public record LeftRightRow<T>(@Nullable T left, @Nullable T right) {
    }

    public record VehiclesResponse(@Nullable VehiclesData data) {
        public record VehiclesData(@Nullable List<VehicleInfo> vehicles) {
        }

        public record VehicleInfo(@Nullable String vin, @Nullable String nickname, @Nullable String name,
                @Nullable String type, @Nullable String sellname) {
        }
    }

    public record BatteryResponse(@Nullable BatteryData data) {
        public record BatteryData(@Nullable ChargeInfo charge) {
        }

        public record ChargeInfo(@Nullable String stateOfCharge, @Nullable Boolean charging, @Nullable String plugin,
                @Nullable String status, @Nullable String remainTime, @Nullable String batteryCapacity,
                @Nullable TargetSoC targetStateOfCharge) {
        }

        public record TargetSoC(@Nullable String standard, @Nullable String quick) {
        }
    }

    public record PowertrainResponse(@Nullable PowertrainData data) {
        public record PowertrainData(@Nullable EngineInfo engine, @Nullable List<DistanceInfo> distanceToEmpties) {
        }

        public record EngineInfo(@Nullable String fuelLevel) {
        }

        public record DistanceInfo(@Nullable String type, @Nullable String unit, @Nullable String value) {
        }
    }

    public record LocationResponse(@Nullable LocationData data) {
        public record LocationData(@Nullable String timestamp, @Nullable GpsLocation location) {
        }

        public record GpsLocation(@Nullable String latitude, @Nullable String longitude, @Nullable String altitude,
                @Nullable JsonElement speed, @Nullable String heading) {

            /**
             * Extracts the speed regardless of whether the API returns an object ({"value": "50"}) or a plain string
             * ("50").
             */
            public @Nullable String extractRawSpeed() {
                JsonElement speedElement = this.speed;
                if (speedElement == null || speedElement.isJsonNull()) {
                    return null;
                }
                if (speedElement.isJsonObject()) {
                    var obj = speedElement.getAsJsonObject();
                    return (obj.has("value") && !obj.get("value").isJsonNull()) ? obj.get("value").getAsString() : null;
                }
                return speedElement.isJsonPrimitive() ? speedElement.getAsString() : null;
            }
        }
    }

    public record DrivingResponse(@Nullable DrivingData data) {
        public record DrivingData(@Nullable IgnitionInfo ignition, @Nullable String sleepMode,
                @Nullable OdometerInfo odometer, @Nullable TransmissionInfo transmission, @Nullable String timestamp) {
        }

        public record IgnitionInfo(@Nullable String status, @Nullable List<IgnitionDetail> ignitionDetails) {
        }

        public record IgnitionDetail(@Nullable String type, @Nullable String status) {
        }

        public record OdometerInfo(@Nullable Double value, @Nullable String unit) {
        }

        public record TransmissionInfo(@Nullable Boolean parkingPosition) {
        }
    }

    public record VehicleStatusResponse(@Nullable VehicleStatusData data) {

        public record VehicleStatusData(@Nullable List<LeftRightRow<String>> doors, @Nullable String hood,
                @Nullable String trunk, @Nullable String sunroof, @Nullable List<TireRow> tire,
                @Nullable List<String> warnings, @Nullable ClimateControl climateControl,
                @Nullable WindshieldInfo windshield, @Nullable String steeringWheelHeat,
                @Nullable ValueWithUnit outsideTemperature, @Nullable List<LeftRightRow<SeatInfo>> seats) {
        }

        // Specific row-level record for tires, since "unit" is at the row level
        public record TireRow(@Nullable Double left, @Nullable Double right, @Nullable String unit) {
        }

        public record ClimateControl(@Nullable String mode, @Nullable String status, @Nullable Integer speed,
                @Nullable ValueWithUnit temperature) {
        }

        public record WindshieldInfo(@Nullable String defrost) {
        }

        public record ValueWithUnit(@Nullable Double value, @Nullable String unit) {
        }

        public record SeatInfo(@Nullable JsonElement heat, @Nullable JsonElement ventilation) {
        }
    }

    public record FreezerResponse(@Nullable FreezerData data) {
        public record FreezerData(@Nullable FreezerInfo freezer) {
        }

        public record FreezerInfo(@Nullable String status, @Nullable Double temperature) {
        }
    }
}
