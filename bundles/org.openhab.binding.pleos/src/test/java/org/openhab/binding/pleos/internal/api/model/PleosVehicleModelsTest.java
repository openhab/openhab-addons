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

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.BatteryResponse;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.BatteryResponse.BatteryData;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.BatteryResponse.ChargeInfo;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.BatteryResponse.TargetSoC;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.DrivingResponse;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.DrivingResponse.DrivingData;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.DrivingResponse.IgnitionInfo;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.DrivingResponse.OdometerInfo;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.DrivingResponse.TransmissionInfo;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.FreezerResponse;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.FreezerResponse.FreezerData;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.FreezerResponse.FreezerInfo;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.LeftRightRow;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.LocationResponse;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.LocationResponse.GpsLocation;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.LocationResponse.LocationData;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.PowertrainResponse;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.PowertrainResponse.DistanceInfo;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.PowertrainResponse.PowertrainData;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.VehicleStatusResponse;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.VehicleStatusResponse.ClimateControl;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.VehicleStatusResponse.TireRow;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.VehicleStatusResponse.ValueWithUnit;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.VehicleStatusResponse.VehicleStatusData;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.VehiclesResponse;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.VehiclesResponse.VehicleInfo;
import org.openhab.binding.pleos.internal.api.model.PleosVehicleModels.VehiclesResponse.VehiclesData;

import com.google.gson.Gson;

/**
 * Unit tests for {@link PleosVehicleModels} JSON deserialization.
 *
 * @author Stephan Sandmann - Initial contribution
 */
@NonNullByDefault
public class PleosVehicleModelsTest {

    private final Gson gson = new Gson();

    @Test
    public void testVehiclesResponseDeserialization() {
        String json = """
                {
                    "data": {
                        "vehicles": [
                            {
                                "vin": "KMHKN81AFNU123456",
                                "nickname": "My IONIQ",
                                "name": "NE_EV",
                                "type": "EV",
                                "sellname": "IONIQ 5"
                            }
                        ]
                    }
                }
                """;

        VehiclesResponse response = gson.fromJson(json, VehiclesResponse.class);
        assertNotNull(response);
        VehiclesData data = response.data();
        assertNotNull(data);
        List<VehicleInfo> list = data.vehicles();
        assertNotNull(list);
        assertEquals(1, list.size());

        VehicleInfo vehicle = list.get(0);
        assertEquals("KMHKN81AFNU123456", vehicle.vin());
        assertEquals("My IONIQ", vehicle.nickname());
        assertEquals("NE_EV", vehicle.name());
        assertEquals("EV", vehicle.type());
        assertEquals("IONIQ 5", vehicle.sellname());
    }

    @Test
    public void testBatteryResponseDeserialization() {
        String json = """
                {
                    "data": {
                        "charge": {
                            "stateOfCharge": "80",
                            "charging": true,
                            "plugin": "PLUGGED",
                            "status": "CHARGING_AC",
                            "remainTime": "45",
                            "batteryCapacity": "77400",
                            "targetStateOfCharge": {
                                "standard": "90",
                                "quick": "80"
                            }
                        }
                    }
                }
                """;

        BatteryResponse response = gson.fromJson(json, BatteryResponse.class);
        assertNotNull(response);
        BatteryData data = response.data();
        assertNotNull(data);
        ChargeInfo charge = data.charge();
        assertNotNull(charge);
        assertEquals("80", charge.stateOfCharge());
        assertEquals(Boolean.TRUE, charge.charging());
        assertEquals("PLUGGED", charge.plugin());
        assertEquals("CHARGING_AC", charge.status());
        assertEquals("45", charge.remainTime());
        assertEquals("77400", charge.batteryCapacity());
        TargetSoC target = charge.targetStateOfCharge();
        assertNotNull(target);
        assertEquals("90", target.standard());
        assertEquals("80", target.quick());
    }

    @Test
    public void testPowertrainResponseDeserialization() {
        String json = """
                {
                    "data": {
                        "distanceToEmpties": [
                            {
                                "type": "EV",
                                "value": "342.5"
                            }
                        ]
                    }
                }
                """;

        PowertrainResponse response = gson.fromJson(json, PowertrainResponse.class);
        assertNotNull(response);
        PowertrainData data = response.data();
        assertNotNull(data);
        List<DistanceInfo> dtes = data.distanceToEmpties();
        assertNotNull(dtes);
        assertEquals(1, dtes.size());
        assertEquals("EV", dtes.get(0).type());
        assertEquals("342.5", dtes.get(0).value());
    }

    @Test
    public void testLocationResponseDeserialization() {
        String json = """
                {
                    "data": {
                        "timestamp": "2026-11-16T12:00:00Z",
                        "location": {
                            "latitude": "52.5200",
                            "longitude": "13.4050",
                            "altitude": "34.0",
                            "speed": "0.0",
                            "heading": "180.0"
                        }
                    }
                }
                """;

        LocationResponse response = gson.fromJson(json, LocationResponse.class);
        assertNotNull(response);
        LocationData data = response.data();
        assertNotNull(data);
        assertEquals("2026-11-16T12:00:00Z", data.timestamp());
        GpsLocation loc = data.location();
        assertNotNull(loc);
        assertEquals("52.5200", loc.latitude());
        assertEquals("13.4050", loc.longitude());
        assertEquals("34.0", loc.altitude());
        assertEquals("0.0", loc.extractRawSpeed());
        assertEquals("180.0", loc.heading());
    }

    @Test
    public void testDrivingResponseDeserialization() {
        String json = """
                {
                    "data": {
                        "timestamp": "2026-11-16T12:05:00Z",
                        "sleepMode": "OFF",
                        "odometer": {
                            "value": 12345.6,
                            "unit": "km"
                        },
                        "transmission": {
                            "parkingPosition": true
                        },
                        "ignition": {
                            "status": "OFF",
                            "ignitionDetails": [
                                { "type": "ACC", "status": "OFF" }
                            ]
                        }
                    }
                }
                """;

        DrivingResponse response = gson.fromJson(json, DrivingResponse.class);
        assertNotNull(response);
        DrivingData data = response.data();
        assertNotNull(data);
        assertEquals("2026-11-16T12:05:00Z", data.timestamp());
        assertEquals("OFF", data.sleepMode());
        OdometerInfo odo = data.odometer();
        assertNotNull(odo);
        assertEquals(12345.6, odo.value());
        TransmissionInfo trans = data.transmission();
        assertNotNull(trans);
        assertEquals(Boolean.TRUE, trans.parkingPosition());
        IgnitionInfo ign = data.ignition();
        assertNotNull(ign);
        assertEquals("OFF", ign.status());
    }

    @Test
    public void testVehicleStatusResponseDeserialization() {
        String json = """
                {
                    "data": {
                        "doors": [
                            { "left": "CLOSED", "right": "CLOSED" },
                            { "left": "CLOSED", "right": "CLOSED" }
                        ],
                        "hood": "CLOSED",
                        "trunk": "CLOSED",
                        "sunroof": "CLOSED",
                        "tire": [
                            { "left": 2.4, "right": 2.4, "unit": "bar" },
                            { "left": 2.5, "right": 2.5, "unit": "bar" }
                        ],
                        "warnings": [
                            "low-washer-fluid",
                            "tire-pressure-fl"
                        ],
                        "climateControl": {
                            "mode": "AUTO",
                            "status": "OFF",
                            "speed": 2,
                            "temperature": { "value": 21.5, "unit": "C" }
                        },
                        "windshield": { "defrost": "OFF" },
                        "steeringWheelHeat": "OFF"
                    }
                }
                """;

        VehicleStatusResponse response = gson.fromJson(json, VehicleStatusResponse.class);
        assertNotNull(response);
        VehicleStatusData data = response.data();
        assertNotNull(data);

        // Doors
        List<LeftRightRow<String>> doors = data.doors();
        assertNotNull(doors);
        assertEquals(2, doors.size());
        assertEquals("CLOSED", doors.get(0).left());

        // Tires
        List<TireRow> tires = data.tire();
        assertNotNull(tires);
        assertEquals(2, tires.size());
        assertEquals(2.4, tires.get(0).left());
        assertEquals("bar", tires.get(0).unit());

        // Warnings
        List<String> warnings = data.warnings();
        assertNotNull(warnings);
        assertTrue(warnings.contains("low-washer-fluid"));

        // Climate
        ClimateControl climate = data.climateControl();
        assertNotNull(climate);
        assertEquals("AUTO", climate.mode());
        assertEquals(2, climate.speed());
        ValueWithUnit temp = climate.temperature();
        assertNotNull(temp);
        assertEquals(21.5, temp.value());
        assertEquals("C", temp.unit());
    }

    @Test
    public void testFreezerResponseDeserialization() {
        String json = """
                {
                    "data": {
                        "freezer": {
                            "status": "ON",
                            "temperature": -5.0
                        }
                    }
                }
                """;

        FreezerResponse response = gson.fromJson(json, FreezerResponse.class);
        assertNotNull(response);
        FreezerData data = response.data();
        assertNotNull(data);
        FreezerInfo freezer = data.freezer();
        assertNotNull(freezer);
        assertEquals("ON", freezer.status());
        assertEquals(-5.0, freezer.temperature());
    }
}
