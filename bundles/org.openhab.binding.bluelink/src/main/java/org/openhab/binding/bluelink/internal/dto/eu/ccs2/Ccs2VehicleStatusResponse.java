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
package org.openhab.binding.bluelink.internal.dto.eu.ccs2;

import java.util.List;

import org.openhab.binding.bluelink.internal.dto.BatteryStatus;
import org.openhab.binding.bluelink.internal.dto.CommonVehicleStatus;
import org.openhab.binding.bluelink.internal.dto.DoorStatus;
import org.openhab.binding.bluelink.internal.dto.DrivingRange;
import org.openhab.binding.bluelink.internal.dto.EvStatus;
import org.openhab.binding.bluelink.internal.dto.IDoorStatus;
import org.openhab.binding.bluelink.internal.dto.ITirePressureWarning;
import org.openhab.binding.bluelink.internal.dto.SeatHeaterState;
import org.openhab.binding.bluelink.internal.dto.TemperatureValue;
import org.openhab.binding.bluelink.internal.dto.TirePressureWarning;
import org.openhab.binding.bluelink.internal.model.IVehicle;
import org.openhab.binding.bluelink.internal.model.PlugType;

import com.google.gson.annotations.SerializedName;

/**
 * CCU/CCS2 vehicle status response (EU).
 *
 * @author Florian Hotze - Initial contribution
 */
public record Ccs2VehicleStatusResponse(String resCode, @SerializedName("ServiceNo") String serviceNo,
        @SerializedName("RetCode") String retCode, String lastUpdateTime, State state) {

    public CommonVehicleStatus toCommonVehicleStatus(IVehicle vehicle) {
        Vehicle v = this.state != null ? this.state.vehicle() : null;
        if (v == null)
            return null;

        return new CommonVehicleStatus() {
            @Override
            public boolean engine() {
                PowerSupply ps = v.electronics().powerSupply();
                return (ps.ignition1() != 0 || ps.ignition3() != 0);
            }

            @Override
            public boolean doorLock() {
                var r1 = v.cabin().door().row1();
                var r2 = v.cabin().door().row2();

                boolean frontLeftLocked = r1.driver() != null && r1.driver().lock() > 0;
                boolean frontRightLocked = r1.passenger() != null && r1.passenger().lock() > 0;
                boolean backLeftLocked = r2.left() != null && r2.left().lock() > 0;
                boolean backRightLocked = r2.right() != null && r2.right().lock() > 0;

                return (frontLeftLocked || frontRightLocked || backLeftLocked || backRightLocked);
            }

            @Override
            public IDoorStatus doorOpen() {
                var r1 = v.cabin().door().row1();
                var r2 = v.cabin().door().row2();

                int fl = r1.driver() != null ? r1.driver().open() : 0;
                int fr = r1.passenger() != null ? r1.passenger().open() : 0;
                int bl = r2.left() != null ? r2.left().open() : 0;
                int br = r2.right() != null ? r2.right().open() : 0;

                return new DoorStatus(fl, fr, bl, br);
            }

            @Override
            public DoorStatus windowOpen() {
                var r1 = v.cabin().door().row1();
                var r2 = v.cabin().door().row2();

                int fl = r1.driver() != null ? r1.driver().open() : 0;
                int fr = r1.passenger() != null ? r1.passenger().open() : 0;
                int bl = r2.left() != null ? r2.left().open() : 0;
                int br = r2.right() != null ? r2.right().open() : 0;

                return new DoorStatus(fl, fr, bl, br);
            }

            @Override
            public boolean trunkOpen() {
                return v.body() != null && v.body().trunk() != null && v.body().trunk().open() > 0;
            }

            @Override
            public boolean hoodOpen() {
                return v.body() != null && v.body().hood() != null && v.body().hood().open() > 0;
            }

            @Override
            public boolean airCtrlOn() {
                return v.cabin().hvac().row1().driver().temperature().value().equals("ON");
            }

            @Override
            public TemperatureValue airTemp() {
                // Note: Not available in CCS2
                return null;
            }

            @Override
            public boolean defrost() {
                return v.body().windshield().front().heat().state() > 0;
            }

            @Override
            public int steerWheelHeat() {
                return v.cabin().steeringWheel().heat().state();
            }

            @Override
            public int sideBackWindowHeat() {
                // Note: Not available in CCS2
                return 0;
            }

            @Override
            public int sideMirrorHeat() {
                // Note: Not available in CCS2
                return 0;
            }

            @Override
            public SeatHeaterState seatHeaterVentState() {
                var r1 = v.cabin().door().row1();
                var r2 = v.cabin().door().row2();

                int fl = r1.driver() != null ? r1.driver().open() : 0;
                int fr = r1.passenger() != null ? r1.passenger().open() : 0;
                int bl = r2.left() != null ? r2.left().open() : 0;
                int br = r2.right() != null ? r2.right().open() : 0;

                return new SeatHeaterState(fl, fr, bl, br);
            }

            @Override
            public BatteryStatus battery() {
                return new BatteryStatus(v.electronics().battery().level());
            }

            @Override
            public EvStatus evStatus() {
                var g = v.green();
                if (g == null) {
                    return null;
                }
                var ci = g.chargingInformation();
                var fuelSystem = v.drivetrain().fuelSystem();

                int current = ci.charging() != null ? ci.charging().remainTime() : 0;
                int fast = ci.estimatedTime() != null ? ci.estimatedTime().quick() : 0;
                int portable = ci.estimatedTime() != null ? ci.estimatedTime().iccb() : 0;
                int station = ci.estimatedTime() != null ? ci.estimatedTime().standard() : 0;

                EvStatus.ChargeRemainingTime remainTime = new EvStatus.ChargeRemainingTime(
                        new EvStatus.ChargeRemainingTime.TimeValue(current, 1),
                        new EvStatus.ChargeRemainingTime.TimeValue(fast, 1),
                        new EvStatus.ChargeRemainingTime.TimeValue(portable, 1),
                        new EvStatus.ChargeRemainingTime.TimeValue(station, 1));

                EvStatus.ReserveChargeInfo targetSoC = null;
                if (ci.targetSoC() != null && ci.dte() != null) {
                    targetSoC = new EvStatus.ReserveChargeInfo(List.of(
                            new EvStatus.ReserveChargeInfo.TargetSOC(PlugType.AC.ordinal(), ci.targetSoC().standard(),
                                    new DrivingRange(ci.dte().targetSoC().standard(), 1)),
                            new EvStatus.ReserveChargeInfo.TargetSOC(PlugType.DC.ordinal(), ci.targetSoC().quick(),
                                    new DrivingRange(ci.dte().targetSoC().quick(), 1))));
                }

                List<EvStatus.DrivingDistance> drvDistance = null;
                if (fuelSystem.dte() != null) {
                    double totalRange = fuelSystem.dte().total();
                    DrivingRange range = new DrivingRange(totalRange, 1);
                    drvDistance = List.of(new EvStatus.DrivingDistance(new EvStatus.DrivingDistance.RangeByFuel(range,
                            vehicle.isElectric() ? range : null, null)));
                }

                return new EvStatus(current > 0, g.batteryManagement().batteryRemain().ratio(),
                        ci.connectorFastening().state(), targetSoC, drvDistance, remainTime);
            }

            @Override
            public DrivingRange dte() {
                return new DrivingRange(v.drivetrain().fuelSystem().dte().total(), 1);
            }

            @Override
            public int fuelLevel() {
                return v.drivetrain().fuelSystem().fuelLevel();
            }

            @Override
            public boolean lowFuelLight() {
                return v.drivetrain().fuelSystem().lowFuelWarning() > 0;
            }

            @Override
            public boolean washerFluidStatus() {
                return v.body().windshield().front().washerFluid().low() > 0;
            }

            @Override
            public boolean brakeOilStatus() {
                return v.chassis().brake().fluid().warning() > 0;
            }

            @Override
            public ITirePressureWarning tirePressureWarning() {
                var tire = v.chassis().axle().tire();
                var r1 = v.chassis().axle().row1();
                var r2 = v.chassis().axle().row2();

                int fl = (r1.left() != null && r1.left().tire() != null) ? r1.left().tire().pressureLow() : 0;
                int fr = (r1.right() != null && r1.right().tire() != null) ? r1.right().tire().pressureLow() : 0;
                int rl = (r2.left() != null && r2.left().tire() != null) ? r2.left().tire().pressureLow() : 0;
                int rr = (r2.right() != null && r2.right().tire() != null) ? r2.right().tire().pressureLow() : 0;

                return new TirePressureWarning(tire.pressureLow(), fl, fr, rl, rr);
            }
        };
    }

    public record State(@SerializedName("Vehicle") Vehicle vehicle) {
    }

    public record Vehicle(@SerializedName("Location") VehicleLocation location,
            @SerializedName("Drivetrain") VehicleDrivetrain drivetrain, @SerializedName("Cabin") Cabin cabin,
            @SerializedName("Body") Body body, @SerializedName("Chassis") Chassis chassis,
            @SerializedName("Electronics") Electronics electronics, @SerializedName("Green") Green green) {
    }

    public record ValueUnit(@SerializedName("Unit") int unit, @SerializedName("Value") double value) {
    }

    public record FrontRow<T> (@SerializedName("Driver") T driver, @SerializedName("Passenger") T passenger) {
    }

    public record GenericRow<T> (@SerializedName("Left") T left, @SerializedName("Right") T right) {
    }

    // Location & Coordinates
    public record VehicleLocation(@SerializedName("Data") String time, @SerializedName("GeoCoord") GeoCoordinates coord,
            @SerializedName("Heading") int head, @SerializedName("Speed") ValueUnit speed) {
    }

    public record GeoCoordinates(@SerializedName("Latitude") double lat, @SerializedName("Longitude") double lon,
            @SerializedName("Altitude") double alt) {
    }

    // Drivetrain
    public record VehicleDrivetrain(@SerializedName("Odometer") double odometer,
            @SerializedName("FuelSystem") FuelSystem fuelSystem) {
    }

    public record FuelSystem(@SerializedName("DTE") Dte dte, @SerializedName("LowFuelWarning") int lowFuelWarning,
            @SerializedName("FuelLevel") int fuelLevel) {
    }

    public record Dte(@SerializedName("Unit") int unit, @SerializedName("Total") double total) {
    }

    // Body (Windshield, Hood, Trunk)
    public record Body(@SerializedName("Windshield") Windshield windshield, @SerializedName("Hood") OpenState hood,
            @SerializedName("Trunk") OpenState trunk) {
    }

    public record Windshield(@SerializedName("Front") WindshieldFront front) {
    }

    public record WindshieldFront(@SerializedName("Heat") Heat heat,
            @SerializedName("WasherFluid") WasherFluid washerFluid) {
    }

    public record WasherFluid(@SerializedName("LevelLow") int low) {
    }

    public record OpenState(@SerializedName("Open") int open) {
    }

    // Cabin (Doors, Windows, HVAC, Steering Wheel)
    public record Cabin(@SerializedName("Door") Door door, @SerializedName("Seat") Seat seat,
            @SerializedName("Window") Window window, @SerializedName("HVAC") Hvac hvac,
            @SerializedName("SteeringWheel") SteeringWheel steeringWheel) {
    }

    public record Door(@SerializedName("Row1") FrontRow<DoorState> row1,
            @SerializedName("Row2") GenericRow<DoorState> row2) {
    }

    public record DoorState(@SerializedName("Open") int open, @SerializedName("Lock") int lock) {
    }

    public record Seat(@SerializedName("Row1") FrontRow<SeatState> row1,
            @SerializedName("Row2") GenericRow<SeatState> row2) {
    }

    public record SeatState(@SerializedName("Climate") Climate climate) {
    }

    public record Climate(@SerializedName("State") int state) {
    }

    public record Window(@SerializedName("Row1") FrontRow<WindowState> row1,
            @SerializedName("Row2") GenericRow<WindowState> row2) {
    }

    public record WindowState(@SerializedName("Open") int open, @SerializedName("OpenLevel") int level) {
    }

    public record Hvac(@SerializedName("Row1") HvacRow1 row1) {
    }

    public record HvacRow1(@SerializedName("Driver") HvacDriver driver) {
    }

    public record HvacDriver(@SerializedName("Temperature") Temperature temperature) {
    }

    public record Temperature(@SerializedName("Value") String value) {
    }

    public record SteeringWheel(@SerializedName("Heat") Heat heat) {
    }

    public record Heat(@SerializedName("State") int state) {
    }

    // Chassis (Axles, Tires, Break)
    public record Chassis(@SerializedName("Axle") Axle axle, @SerializedName("Brake") Brake brake) {
    }

    public record Axle(@SerializedName("Row1") GenericRow<Wheel> row1, @SerializedName("Row2") GenericRow<Wheel> row2,
            @SerializedName("Tire") TireState tire) {
    }

    public record Wheel(@SerializedName("Tire") TireState tire) {
    }

    public record TireState(@SerializedName("PressureLow") int pressureLow) {
    }

    public record Brake(@SerializedName("Fluid") BrakeFluid fluid) {
    }

    public record BrakeFluid(@SerializedName("Warning") int warning) {
    }

    // Electronics (12V Battery, Ignition, Smart Key)
    public record Electronics(@SerializedName("PowerSupply") PowerSupply powerSupply,
            @SerializedName("Battery") Battery battery, @SerializedName("FOB") FOB smartKey) {
    }

    public record PowerSupply(@SerializedName("Ignition1") int ignition1, @SerializedName("Ignition3") int ignition3,
            @SerializedName("Accessory") int accessory) {
    }

    public record Battery(@SerializedName("Level") float level) {
    }

    public record FOB(@SerializedName("LowBattery") int batteryWarning) {
    }

    // Green (HV Battery, Charging Info)
    public record Green(@SerializedName("BatteryManagement") BatteryManagement batteryManagement,
            @SerializedName("ChargingInformation") ChargingInformation chargingInformation) {
    }

    public record BatteryManagement(@SerializedName("BatteryRemain") BatteryRemain batteryRemain) {
    }

    public record BatteryRemain(@SerializedName("Ratio") float ratio) {
    }

    public record ChargingInformation(@SerializedName("ConnectorFastening") ConnectorFastening connectorFastening,
            @SerializedName("Charging") Charging charging, @SerializedName("DTE") ChargingDTE dte,
            @SerializedName("TargetSoC") TargetSoC targetSoC,
            @SerializedName("EstimatedTime") EstimatedTime estimatedTime) {
    }

    public record ConnectorFastening(@SerializedName("State") int state) {
    }

    public record Charging(@SerializedName("RemainTime") int remainTime) {
    }

    public record ChargingDTE(@SerializedName("TargetSoC") TargetSoC targetSoC) {
    }

    public record TargetSoC(@SerializedName("Standard") int standard, @SerializedName("Quick") int quick) {
    }

    public record EstimatedTime(@SerializedName("Quick") int quick, @SerializedName("ICCB") int iccb,
            @SerializedName("Standard") int standard) {
    }
}
