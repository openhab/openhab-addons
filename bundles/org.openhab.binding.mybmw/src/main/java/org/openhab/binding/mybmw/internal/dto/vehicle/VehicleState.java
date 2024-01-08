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
package org.openhab.binding.mybmw.internal.dto.vehicle;

import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.mybmw.internal.dto.charge.ChargingProfile;

/**
 * 
 * derived from the API responses
 * 
 * @author Martin Grassl - initial contribution
 */
public class VehicleState {

    public static final String CHECK_CONTROL_OVERALL_MESSAGE_OK = "No Issues";

    private boolean isLeftSteering = false;
    private String lastFetched = ""; // 2022-12-21T17:31:26.560Z,
    private String lastUpdatedAt = ""; // 2022-12-21T15:41:23Z,
    private boolean isLscSupported = false; // true,
    private int range = -1; // 435,
    private VehicleDoorsState doorsState = new VehicleDoorsState();
    private VehicleWindowsState windowsState = new VehicleWindowsState();
    private VehicleRoofState roofState = new VehicleRoofState();
    private VehicleTireStates tireState = new VehicleTireStates();

    private VehicleLocation location = new VehicleLocation();
    private int currentMileage = -1;
    private ClimateControlState climateControlState = new ClimateControlState();
    private List<RequiredService> requiredServices = new ArrayList<>();
    private List<CheckControlMessage> checkControlMessages = new ArrayList<>();
    private CombustionFuelLevel combustionFuelLevel = new CombustionFuelLevel();
    private DriverPreferences driverPreferences = new DriverPreferences();
    private ElectricChargingState electricChargingState = new ElectricChargingState();
    private boolean isDeepSleepModeActive = false; // false
    private List<ClimateTimer> climateTimers = new ArrayList<>();
    private ChargingProfile chargingProfile = new ChargingProfile();

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */

    /**
     * @return the isLeftSteering
     */
    public boolean isLeftSteering() {
        return isLeftSteering;
    }

    /**
     * @return the lastFetched
     */
    public String getLastFetched() {
        return lastFetched;
    }

    /**
     * @return the lastUpdatedAt
     */
    public String getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    /**
     * @return the isLscSupported
     */
    public boolean isLscSupported() {
        return isLscSupported;
    }

    /**
     * @return the range
     */
    public int getRange() {
        return range;
    }

    /**
     * @return the doorsState
     */
    public VehicleDoorsState getDoorsState() {
        return doorsState;
    }

    /**
     * @return the windowsState
     */
    public VehicleWindowsState getWindowsState() {
        return windowsState;
    }

    /**
     * @return the roofState
     */
    public VehicleRoofState getRoofState() {
        return roofState;
    }

    /**
     * @return the tireState
     */
    public VehicleTireStates getTireState() {
        return tireState;
    }

    /**
     * @return the location
     */
    public VehicleLocation getLocation() {
        return location;
    }

    /**
     * @return the currentMileage
     */
    public int getCurrentMileage() {
        return currentMileage;
    }

    /**
     * @return the climateControlState
     */
    public ClimateControlState getClimateControlState() {
        return climateControlState;
    }

    /**
     * @return the requiredServices
     */
    public List<RequiredService> getRequiredServices() {
        return requiredServices;
    }

    /**
     * @return the checkControlMessages
     */
    public List<CheckControlMessage> getCheckControlMessages() {
        return checkControlMessages;
    }

    /**
     * @return the combustionFuelLevel
     */
    public CombustionFuelLevel getCombustionFuelLevel() {
        return combustionFuelLevel;
    }

    /**
     * @return the driverPreferences
     */
    public DriverPreferences getDriverPreferences() {
        return driverPreferences;
    }

    /**
     * @return the electricChargingState
     */
    public ElectricChargingState getElectricChargingState() {
        return electricChargingState;
    }

    /**
     * @return the isDeepSleepModeActive
     */
    public boolean isDeepSleepModeActive() {
        return isDeepSleepModeActive;
    }

    /**
     * @return the climateTimers
     */
    public List<ClimateTimer> getClimateTimers() {
        return climateTimers;
    }

    /**
     * @return the chargingProfile
     */
    public ChargingProfile getChargingProfile() {
        return chargingProfile;
    }

    @Override
    public String toString() {
        return "VehicleState [isLeftSteering=" + isLeftSteering + ", lastFetched=" + lastFetched + ", lastUpdatedAt="
                + lastUpdatedAt + ", isLscSupported=" + isLscSupported + ", range=" + range + ", doorsState="
                + doorsState + ", windowsState=" + windowsState + ", roofState=" + roofState + ", tireState="
                + tireState + ", location=" + location + ", currentMileage=" + currentMileage + ", climateControlState="
                + climateControlState + ", requiredServices=" + requiredServices + ", checkControlMessages="
                + checkControlMessages + ", combustionFuelLevel=" + combustionFuelLevel + ", driverPreferences="
                + driverPreferences + ", electricChargingState=" + electricChargingState + ", isDeepSleepModeActive="
                + isDeepSleepModeActive + ", climateTimers=" + climateTimers + ", chargingProfile=" + chargingProfile
                + "]";
    }

    /**
     * helper methods
     */
    public String getOverallCheckControlStatus() {
        StringBuilder overallMessage = new StringBuilder();

        for (CheckControlMessage checkControlMessage : checkControlMessages) {
            if (checkControlMessage.getId() > 0) {
                overallMessage.append(checkControlMessage.getName() + "; ");
            }
        }

        String overallMessageString = overallMessage.toString();

        if (overallMessageString.isEmpty()) {
            overallMessageString = CHECK_CONTROL_OVERALL_MESSAGE_OK;
        }

        return overallMessageString;
    }
}
