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

    public boolean isLeftSteering() {
        return isLeftSteering;
    }

    public void setLeftSteering(boolean isLeftSteering) {
        this.isLeftSteering = isLeftSteering;
    }

    public String getLastFetched() {
        return lastFetched;
    }

    public void setLastFetched(String lastFetched) {
        this.lastFetched = lastFetched;
    }

    public String getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(String lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public boolean isLscSupported() {
        return isLscSupported;
    }

    public void setLscSupported(boolean isLscSupported) {
        this.isLscSupported = isLscSupported;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public VehicleDoorsState getDoorsState() {
        return doorsState;
    }

    public void setDoorsState(VehicleDoorsState doorsState) {
        this.doorsState = doorsState;
    }

    public VehicleWindowsState getWindowsState() {
        return windowsState;
    }

    public void setWindowsState(VehicleWindowsState windowsState) {
        this.windowsState = windowsState;
    }

    public VehicleRoofState getRoofState() {
        return roofState;
    }

    public void setRoofState(VehicleRoofState roofState) {
        this.roofState = roofState;
    }

    public VehicleTireStates getTireState() {
        return tireState;
    }

    public void setTireState(VehicleTireStates tireState) {
        this.tireState = tireState;
    }

    public VehicleLocation getLocation() {
        return location;
    }

    public void setLocation(VehicleLocation location) {
        this.location = location;
    }

    public int getCurrentMileage() {
        return currentMileage;
    }

    public void setCurrentMileage(int currentMileage) {
        this.currentMileage = currentMileage;
    }

    public ClimateControlState getClimateControlState() {
        return climateControlState;
    }

    public void setClimateControlState(ClimateControlState climateControlState) {
        this.climateControlState = climateControlState;
    }

    public List<RequiredService> getRequiredServices() {
        return requiredServices;
    }

    public void setRequiredServices(List<RequiredService> requiredServices) {
        this.requiredServices = requiredServices;
    }

    public List<CheckControlMessage> getCheckControlMessages() {
        return checkControlMessages;
    }

    public void setCheckControlMessages(List<CheckControlMessage> checkControlMessages) {
        this.checkControlMessages = checkControlMessages;
    }

    public CombustionFuelLevel getCombustionFuelLevel() {
        return combustionFuelLevel;
    }

    public void setCombustionFuelLevel(CombustionFuelLevel combustionFuelLevel) {
        this.combustionFuelLevel = combustionFuelLevel;
    }

    public DriverPreferences getDriverPreferences() {
        return driverPreferences;
    }

    public void setDriverPreferences(DriverPreferences driverPreferences) {
        this.driverPreferences = driverPreferences;
    }

    public boolean isDeepSleepModeActive() {
        return isDeepSleepModeActive;
    }

    public void setDeepSleepModeActive(boolean isDeepSleepModeActive) {
        this.isDeepSleepModeActive = isDeepSleepModeActive;
    }

    public List<ClimateTimer> getClimateTimers() {
        return climateTimers;
    }

    public void setClimateTimers(List<ClimateTimer> climateTimers) {
        this.climateTimers = climateTimers;
    }

    public ChargingProfile getChargingProfile() {
        return chargingProfile;
    }

    public void setChargingProfile(ChargingProfile chargingProfile) {
        this.chargingProfile = chargingProfile;
    }

    /**
     * @return the electricChargingState
     */
    public ElectricChargingState getElectricChargingState() {
        return electricChargingState;
    }

    /**
     * @param electricChargingState the electricChargingState to set
     */
    public void setElectricChargingState(ElectricChargingState electricChargingState) {
        this.electricChargingState = electricChargingState;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */

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
