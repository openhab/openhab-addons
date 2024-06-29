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
package org.openhab.binding.teslascope.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Class for holding the set of parameters used to read the controller variables.
 *
 * @author Paul Smedley - Initial Contribution
 *
 */
@NonNullByDefault
public class DetailedInformation {
    public String vin = "";
    public String vehicleName = "";
    public String vehicleState = "";
    public double odometer;

    // charge_state
    public int batteryLevel;
    public int usableBatteryLevel;
    public float batteryRange;
    public float estBatteryRange;
    public boolean chargeEnableRequest = false;
    public float chargeEnergyAdded;
    public int chargeLimitSoc;
    public boolean chargePortDoorOpen = false;
    public float chargeRate;
    public int chargerPower;
    public int chargerVoltage;
    public String chargingState = "";
    public float timeToFullCharge;
    public boolean scheduledChargingPending = false;
    public String scheduledChargingStartTime = " ";

    // climate_state
    public boolean isAutoConditioningOn;
    public boolean isClimateOn;
    public boolean isFrontDefrosterOn;
    public boolean isPreconditioning;
    public boolean isRearDefrosterOn;
    public int seatHeaterLeft;
    public int seatHeaterRearCenter;
    public int seatHeaterRearLeft;
    public int seatHeaterRearRight;
    public int seatHeaterRight;
    public boolean sideMirrorHeaters;
    public boolean smartPreconditioning;
    public boolean steeringWheelHeater;
    public boolean wiperBladeHeater;
    public float driverTempSetting;
    public float insideTemp;
    public float outsideTemp;
    public float passengerTempSetting;
    public float fanStatus;
    public int leftTempDirection;
    public float maxAvailTemp;
    public float minAvailTemp;
    public int rightTempDirection;

    // drive state
    public int heading;
    public float latitude;
    public float longitude;
    public String shiftState = "";
    public float power;
    public float speed = 0;

    // vehicle_state
    public boolean locked;
    public boolean sentryMode;
    public boolean valetMode;
    public String softwareUpdateStatus = "";
    public String softwareUpdateVersion = "";
    public boolean fdWindow;
    public boolean fpWindow;
    public boolean rdWindow;
    public boolean rpWindow;
    public String sunRoofState = "";
    public int sunRoofPercentOpen;
    public boolean homelinkNearby;
    public double tpmsPressureFL;
    public double tpmsPressureFR;
    public double tpmsPressureRL;
    public double tpmsPressureRR;
    public boolean tpmsSoftWarningFL;
    public boolean tpmsSoftWarningFR;
    public boolean tpmsSoftWarningRL;
    public boolean tpmsSoftWarningRR;
    public boolean df;
    public boolean dr;
    public boolean pf;
    public boolean pr;
    public boolean ft;
    public boolean rt;

    private DetailedInformation() {
    }

    public static DetailedInformation parse(String response) {
        /* parse json string */
        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
        JsonObject chargeStateJsonObject = jsonObject.get("charge_state").getAsJsonObject();
        JsonObject climateStateJsonObject = jsonObject.get("climate_state").getAsJsonObject();
        JsonObject driveStateJsonObject = jsonObject.get("drive_state").getAsJsonObject();
        JsonObject vehicleStateJsonObject = jsonObject.get("vehicle_state").getAsJsonObject();
        DetailedInformation detailedInformation = new DetailedInformation();

        detailedInformation.vin = jsonObject.get("vin").getAsString();
        detailedInformation.vehicleName = jsonObject.get("name").getAsString();
        detailedInformation.vehicleState = jsonObject.get("state").getAsString();
        detailedInformation.odometer = jsonObject.get("odometer").getAsDouble();

        // data from chargeState
        detailedInformation.batteryLevel = chargeStateJsonObject.get("battery_level").getAsInt();
        detailedInformation.usableBatteryLevel = chargeStateJsonObject.get("usable_battery_level").getAsInt();
        detailedInformation.batteryRange = chargeStateJsonObject.get("battery_range").getAsFloat();
        detailedInformation.estBatteryRange = chargeStateJsonObject.get("est_battery_range").getAsFloat();
        detailedInformation.chargeEnableRequest = "1"
                .equals(chargeStateJsonObject.get("charge_enable_request").getAsString());
        detailedInformation.chargeEnergyAdded = chargeStateJsonObject.get("charge_energy_added").getAsFloat();
        detailedInformation.chargeLimitSoc = chargeStateJsonObject.get("charge_limit_soc").getAsInt();
        detailedInformation.chargePortDoorOpen = "1"
                .equals(chargeStateJsonObject.get("charge_port_door_open").getAsString());
        // if car is not charging, these might be NULL
        if (!chargeStateJsonObject.get("charge_rate").isJsonNull()) {
            detailedInformation.chargeRate = chargeStateJsonObject.get("charge_rate").getAsFloat();
            detailedInformation.chargerPower = chargeStateJsonObject.get("charger_power").getAsInt();
        } else {
            detailedInformation.chargeRate = 0;
            detailedInformation.chargerPower = 0;
        }
        detailedInformation.chargerVoltage = chargeStateJsonObject.get("charger_voltage").getAsInt();
        detailedInformation.chargingState = chargeStateJsonObject.get("charging_state").getAsString();
        detailedInformation.timeToFullCharge = chargeStateJsonObject.get("time_to_full_charge").getAsFloat();
        detailedInformation.scheduledChargingPending = "1"
                .equals(chargeStateJsonObject.get("scheduled_charging_pending").getAsString());
        // if car has no scheduled charge, these might be NULL
        if (!chargeStateJsonObject.get("scheduled_charging_start_time").isJsonNull()) {
            detailedInformation.scheduledChargingStartTime = chargeStateJsonObject.get("scheduled_charging_start_time")
                    .getAsString();
        }

        // data from climateState
        detailedInformation.isAutoConditioningOn = "1"
                .equals(climateStateJsonObject.get("is_auto_conditioning_on").getAsString());
        detailedInformation.isClimateOn = "1".equals(climateStateJsonObject.get("is_climate_on").getAsString());
        detailedInformation.isFrontDefrosterOn = "1"
                .equals(climateStateJsonObject.get("is_front_defroster_on").getAsString());
        detailedInformation.isPreconditioning = "1"
                .equals(climateStateJsonObject.get("is_preconditioning").getAsString());
        detailedInformation.isRearDefrosterOn = "1"
                .equals(climateStateJsonObject.get("is_rear_defroster_on").getAsString());
        detailedInformation.seatHeaterLeft = climateStateJsonObject.get("seat_heater_left").getAsInt();
        detailedInformation.seatHeaterRearCenter = climateStateJsonObject.get("seat_heater_rear_center").getAsInt();
        detailedInformation.seatHeaterRearLeft = climateStateJsonObject.get("seat_heater_rear_left").getAsInt();
        detailedInformation.seatHeaterRearRight = climateStateJsonObject.get("seat_heater_rear_right").getAsInt();
        detailedInformation.seatHeaterRight = climateStateJsonObject.get("seat_heater_right").getAsInt();
        detailedInformation.sideMirrorHeaters = "1"
                .equals(climateStateJsonObject.get("side_mirror_heaters").getAsString());
        detailedInformation.smartPreconditioning = "1"
                .equals(climateStateJsonObject.get("smart_preconditioning").getAsString());
        detailedInformation.steeringWheelHeater = "1"
                .equals(climateStateJsonObject.get("steering_wheel_heater").getAsString());
        detailedInformation.wiperBladeHeater = "1"
                .equals(climateStateJsonObject.get("wiper_blade_heater").getAsString());
        detailedInformation.driverTempSetting = climateStateJsonObject.get("driver_temp_setting").getAsFloat();
        detailedInformation.insideTemp = climateStateJsonObject.get("inside_temp").getAsFloat();
        detailedInformation.outsideTemp = climateStateJsonObject.get("outside_temp").getAsFloat();
        detailedInformation.passengerTempSetting = climateStateJsonObject.get("passenger_temp_setting").getAsFloat();
        detailedInformation.fanStatus = climateStateJsonObject.get("fan_status").getAsFloat();
        detailedInformation.leftTempDirection = climateStateJsonObject.get("left_temp_direction").getAsInt();
        detailedInformation.maxAvailTemp = climateStateJsonObject.get("max_avail_temp").getAsFloat();
        detailedInformation.minAvailTemp = climateStateJsonObject.get("min_avail_temp").getAsFloat();
        detailedInformation.rightTempDirection = climateStateJsonObject.get("right_temp_direction").getAsInt();

        // data from driveState
        detailedInformation.heading = driveStateJsonObject.get("heading").getAsInt();
        detailedInformation.latitude = driveStateJsonObject.get("latitude").getAsFloat();
        detailedInformation.longitude = driveStateJsonObject.get("longitude").getAsFloat();
        detailedInformation.power = driveStateJsonObject.get("power").getAsFloat();
        // if car is parked, these will be NULL
        if (!driveStateJsonObject.get("shift_state").isJsonNull()) {
            detailedInformation.shiftState = driveStateJsonObject.get("shift_state").getAsString();
        } else {
            detailedInformation.shiftState = "N/A";
        }
        if (!driveStateJsonObject.get("speed").isJsonNull()) {
            detailedInformation.speed = driveStateJsonObject.get("speed").getAsFloat();
        } else {
            detailedInformation.speed = 0;
        }

        // data from vehicleState
        detailedInformation.locked = "1".equals(vehicleStateJsonObject.get("locked").getAsString());
        detailedInformation.sentryMode = "1".equals(vehicleStateJsonObject.get("sentry_mode").getAsString());
        detailedInformation.valetMode = "1".equals(vehicleStateJsonObject.get("valet_mode").getAsString());
        detailedInformation.softwareUpdateStatus = vehicleStateJsonObject.get("software_update_status").getAsString();
        detailedInformation.softwareUpdateVersion = vehicleStateJsonObject.get("software_update_version").getAsString();
        detailedInformation.fdWindow = "1".equals(vehicleStateJsonObject.get("fd_window").getAsString());
        detailedInformation.fpWindow = "1".equals(vehicleStateJsonObject.get("fp_window").getAsString());
        detailedInformation.rdWindow = "1".equals(vehicleStateJsonObject.get("rd_window").getAsString());
        detailedInformation.rpWindow = "1".equals(vehicleStateJsonObject.get("rp_window").getAsString());
        // not all cars have a sun roof
        if (!vehicleStateJsonObject.get("sun_roof_state").isJsonNull()) {
            detailedInformation.sunRoofState = vehicleStateJsonObject.get("sun_roof_state").getAsString();
            detailedInformation.sunRoofPercentOpen = vehicleStateJsonObject.get("sun_roof_percent_open").getAsInt();
        } else {
            detailedInformation.sunRoofState = "Not fitted";
            detailedInformation.sunRoofPercentOpen = 0;
        }
        detailedInformation.homelinkNearby = "1".equals(vehicleStateJsonObject.get("homelink_nearby").getAsString());
        detailedInformation.tpmsPressureFL = vehicleStateJsonObject.get("tpms_pressure_fl").getAsDouble();
        detailedInformation.tpmsPressureFR = vehicleStateJsonObject.get("tpms_pressure_fr").getAsDouble();
        detailedInformation.tpmsPressureRL = vehicleStateJsonObject.get("tpms_pressure_rl").getAsDouble();
        detailedInformation.tpmsPressureRR = vehicleStateJsonObject.get("tpms_pressure_rr").getAsDouble();
        detailedInformation.tpmsSoftWarningFL = "1"
                .equals(vehicleStateJsonObject.get("tpms_soft_warning_fl").getAsString());
        detailedInformation.tpmsSoftWarningFR = "1"
                .equals(vehicleStateJsonObject.get("tpms_soft_warning_fr").getAsString());
        detailedInformation.tpmsSoftWarningRL = "1"
                .equals(vehicleStateJsonObject.get("tpms_soft_warning_rl").getAsString());
        detailedInformation.tpmsSoftWarningRR = "1"
                .equals(vehicleStateJsonObject.get("tpms_soft_warning_rr").getAsString());
        detailedInformation.df = "1".equals(vehicleStateJsonObject.get("df").getAsString());
        detailedInformation.dr = "1".equals(vehicleStateJsonObject.get("dr").getAsString());
        detailedInformation.pf = "1".equals(vehicleStateJsonObject.get("pf").getAsString());
        detailedInformation.pr = "1".equals(vehicleStateJsonObject.get("pr").getAsString());
        detailedInformation.ft = "1".equals(vehicleStateJsonObject.get("ft").getAsString());
        detailedInformation.rt = "1".equals(vehicleStateJsonObject.get("rt").getAsString());
        return detailedInformation;
    }
}
