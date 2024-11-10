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
package org.openhab.binding.evcc.internal.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents a loadpoint object of the status response (/api/state).
 * This DTO was written for evcc version 0.123.1
 *
 * @author Florian Hotze - Initial contribution
 * @author Luca Arnecke - Update to evcc version 0.123.1
 */
public class Loadpoint {
    // Data types from https://github.com/evcc-io/evcc/blob/master/api/api.go
    // and from https://docs.evcc.io/docs/reference/configuration/messaging/#msg

    @SerializedName("phasesActive")
    private int activePhases;

    @SerializedName("chargeCurrent")
    private float chargeCurrent;

    @SerializedName("chargeDuration")
    private long chargeDuration;

    @SerializedName("chargePower")
    private float chargePower;

    @SerializedName("chargeRemainingDuration")
    private long chargeRemainingDuration;

    @SerializedName("chargeRemainingEnergy")
    private float chargeRemainingEnergy;

    @SerializedName("chargedEnergy")
    private float chargedEnergy;

    @SerializedName("charging")
    private boolean charging;

    @SerializedName("connected")
    private boolean connected;

    @SerializedName("connectedDuration")
    private long connectedDuration;

    @SerializedName("enabled")
    private boolean enabled;

    @SerializedName("maxCurrent")
    private float maxCurrent;

    @SerializedName("minCurrent")
    private float minCurrent;

    @SerializedName("mode")
    private String mode;

    @SerializedName("phasesEnabled")
    private int phases;

    @SerializedName("limitEnergy")
    private float limitEnergy;

    @SerializedName("limitSoc")
    private float limitSoC;

    @SerializedName("targetTime")
    private String targetTime;

    @SerializedName("title")
    private String title;

    @SerializedName("vehicleOdometer")
    private float vehicleOdometer;

    @SerializedName("vehiclePresent")
    private boolean vehiclePresent;

    @SerializedName("vehicleRange")
    private float vehicleRange;

    @SerializedName("vehicleSoc")
    private float vehicleSoC;

    @SerializedName("vehicleName")
    private String vehicleName;

    @SerializedName("effectiveLimitSoc")
    private float effectiveLimitSoC;

    @SerializedName("chargerFeatureHeating")
    private boolean chargerFeatureHeating;

    @SerializedName("chargerFeatureIntegratedDevice")
    private boolean chargerFeatureIntegratedDevice;

    /**
     * @return number of active phases
     */
    public int getActivePhases() {
        return activePhases;
    }

    /**
     * @return charge current
     */
    public float getChargeCurrent() {
        return chargeCurrent;
    }

    /**
     * @return charge duration
     */
    public long getChargeDuration() {
        return chargeDuration;
    }

    /**
     * @return charge power
     */
    public float getChargePower() {
        return chargePower;
    }

    /**
     * @return charge remaining duration until the target SoC is reached
     */
    public long getChargeRemainingDuration() {
        return chargeRemainingDuration;
    }

    /**
     * @return charge remaining energy until the target SoC is reached
     */
    public float getChargeRemainingEnergy() {
        return chargeRemainingEnergy;
    }

    /**
     * @return charged energy
     */
    public float getChargedEnergy() {
        return chargedEnergy;
    }

    /**
     * @return whether loadpoint is charging a vehicle
     */
    public boolean getCharging() {
        return charging;
    }

    /**
     * @return whether a vehicle is connected to the loadpoint
     */
    public boolean getConnected() {
        return connected;
    }

    /**
     * @return vehicle connected duration
     */
    public long getConnectedDuration() {
        return connectedDuration;
    }

    /**
     * @return whether loadpoint is enabled
     */
    public boolean getEnabled() {
        return enabled;
    }

    /**
     * @return maximum current
     */
    public float getMaxCurrent() {
        return maxCurrent;
    }

    /**
     * @return minimum current
     */
    public float getMinCurrent() {
        return minCurrent;
    }

    /**
     * @return charging mode: off, now, minpv, pv
     */
    public String getMode() {
        return mode;
    }

    /**
     * @return number of enabled phases
     */
    public int getPhases() {
        return phases;
    }

    /**
     * @return limit energy
     */
    public float getLimitEnergy() {
        return limitEnergy;
    }

    /**
     * @return limit state of charge (SoC)
     */
    public float getLimitSoC() {
        return limitSoC;
    }

    /**
     * @return target time for the target state of charge
     */
    public String getTargetTime() {
        return targetTime;
    }

    /**
     * @return loadpoint's title/name
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return vehicle's odometer
     */
    public float getVehicleOdometer() {
        return vehicleOdometer;
    }

    /**
     * @return whether evcc is able to get data from vehicle
     */
    public boolean getVehiclePresent() {
        return vehiclePresent;
    }

    /**
     * @return vehicle's range
     */
    public float getVehicleRange() {
        return vehicleRange;
    }

    /**
     * @return vehicle's state of charge (SoC)
     */
    public float getVehicleSoC() {
        return vehicleSoC;
    }

    /**
     * @return vehicle's title/name
     */
    public String getVehicleName() {
        return vehicleName != null ? vehicleName.replace(":", "-") : vehicleName;
    }

    /**
     * @return effective limit state of charge
     */
    public float getEffectiveLimitSoC() {
        return effectiveLimitSoC;
    }

    /**
     * @return Charger Feature: Heating
     */
    public boolean getChargerFeatureHeating() {
        return chargerFeatureHeating;
    }

    /**
     * @return Charger Feature: Integrated Device
     */
    public boolean getChargerFeatureIntegratedDevice() {
        return chargerFeatureIntegratedDevice;
    }
}
