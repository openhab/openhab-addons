/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.evcc.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents a loadpoint object of the status response (/api/state).
 *
 * @author Florian Hotze - Initial contribution
 */
public class Loadpoint {

    @SerializedName("activePhases")
    private int activePhases;

    @SerializedName("chargeCurrent")
    private float chargeCurrent;

    @SerializedName("chargeDuration")
    private long chargeDuration;

    @SerializedName("chargePower")
    private float chargePower;

    @SerializedName("chargeRemainingDuration")
    private long chargeRemainingDuration;

    @SerializedName("chargedEnergy")
    private double chargedEnergy;

    @SerializedName("charging")
    private boolean charging;

    @SerializedName("connected")
    private boolean connected;

    @SerializedName("connectedDuration")
    private long connectedDuration;

    @SerializedName("enabled")
    private boolean enabled;

    @SerializedName("hasVehicle")
    private boolean hasVehicle;

    @SerializedName("loadpoint")
    private int loadpoint;

    @SerializedName("maxCurrent")
    private int maxCurrent;

    @SerializedName("minCurrent")
    private int minCurrent;

    @SerializedName("minSoC")
    private int minSoC;

    @SerializedName("mode")
    private String mode;

    @SerializedName("phases")
    private int phases;

    @SerializedName("pvAction")
    private String pvAction;

    @SerializedName("pvRemaining")
    private long pvRemaining;

    @SerializedName("targetSoC")
    private int targetSoC;

    @SerializedName("targetTime")
    private String targetTime;

    @SerializedName("targetTimeActive")
    private boolean targetTimeActive;

    @SerializedName("targetTimeHourSuggestion")
    private int targetTimeHourSuggestion;

    @SerializedName("title")
    private String title;

    @SerializedName("vehicleCapacity")
    private int vehicleCapacity;

    @SerializedName("vehicleOdometer")
    private long vehicleOdometer;

    @SerializedName("vehiclePresent")
    private Boolean vehiclePresent;

    @SerializedName("vehicleRange")
    private int vehicleRange;

    @SerializedName("vehicleSoC")
    private int vehicleSoC;

    @SerializedName("vehicleTitle")
    private String vehicleTitle;

    /**
     * @return the active phases
     */
    public int getActivePhases() {
        return activePhases;
    }

    /**
     * @return the charge current
     */
    public float getChargeCurrent() {
        return chargeCurrent;
    }

    /**
     * @return the charge duration
     */
    public long getChargeDuration() {
        return chargeDuration;
    }

    /**
     * @return the charge power
     */
    public float getChargePower() {
        return chargePower;
    }

    /**
     * @return the charge remaining duration
     */
    public long getChargeRemainingDuration() {
        return chargeRemainingDuration;
    }

    /**
     * @return the charged energy
     */
    public double getChargedEnergy() {
        return chargedEnergy;
    }

    /**
     * @return wtether loadpoint is charging a vehicle
     */
    public Boolean getCharging() {
        return charging;
    }

    /**
     * @return whether a vehicle is connected to the loadpoint
     */
    public Boolean getConnected() {
        return connected;
    }

    /**
     * @return the connected duration
     */
    public long getConnectedDuration() {
        return connectedDuration;
    }

    /**
     * @return whether loadpoint is enabled
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * @return whether loadpoint has vehicle
     */
    public Boolean getHasVehicle() {
        return hasVehicle;
    }

    /**
     * @return the loadpoint
     */
    public int getLoadpoint() {
        return loadpoint;
    }

    /**
     * @return the maximum current
     */
    public int getMaxCurrent() {
        return maxCurrent;
    }

    /**
     * @return the minimum current
     */
    public int getMinCurrent() {
        return minCurrent;
    }

    /**
     * @return the minimum state of charge
     */
    public int getMinSoC() {
        return minSoC;
    }

    /**
     * @return the mode: off, now, minpv, pv
     */
    public String getMode() {
        return mode;
    }

    /**
     * @return the phases
     */
    public int getPhases() {
        return phases;
    }

    /**
     * @return the pv action
     */
    public String getPvAction() {
        return pvAction;
    }

    /**
     * @return the pv remaining
     */
    public long getPvRemaining() {
        return pvRemaining;
    }

    /**
     * @return the target state of charge
     */
    public int getTargetSoC() {
        return targetSoC;
    }

    /**
     * @return the target time for the target state of charge
     */
    public String getTargetTime() {
        return targetTime;
    }

    /**
     * @return whether the target time is active
     */
    public Boolean getTargetTimeActive() {
        return targetTimeActive;
    }

    /**
     * @return the target time hour suggestion
     */
    public int getTargetTimeHourSuggestion() {
        return targetTimeHourSuggestion;
    }

    /**
     * @return the loadpoint's title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the vehicle's capacity
     */
    public int getVehicleCapacity() {
        return vehicleCapacity;
    }

    /**
     * @return the vehicle's odometer
     */
    public long getVehicleOdometer() {
        return vehicleOdometer;
    }

    /**
     * @return whether a vehicle is present
     */
    public Boolean getVehiclePresent() {
        return vehiclePresent;
    }

    /**
     * @return the vehicle's range
     */
    public int getVehicleRange() {
        return vehicleRange;
    }

    /**
     * @return the vehicle's state of charge
     */
    public int getVehicleSoC() {
        return vehicleSoC;
    }

    /**
     * @return the vehicle's title
     */
    public String getVehicleTitle() {
        return vehicleTitle;
    }
}
