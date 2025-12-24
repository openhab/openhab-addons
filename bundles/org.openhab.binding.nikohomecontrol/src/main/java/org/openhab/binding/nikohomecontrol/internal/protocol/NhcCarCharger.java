/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.nikohomecontrol.internal.protocol;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NhcCarCharger} class represents the charging station Niko Home Control II communication object. It
 * contains all fields representing a Niko Home Control charging station and has methods to control car charging in Niko
 * Home Control and receive charging station updates. The specific implementation is
 * {@link org.openhab.binding.nikohomecontrol.internal.protocol.nhc2.NhcCarCharger2}.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public abstract class NhcCarCharger {

    private final Logger logger = LoggerFactory.getLogger(NhcCarCharger.class);

    protected NikoHomeControlCommunication nhcComm;

    protected String id;
    protected String name;
    protected @Nullable String location;

    protected volatile boolean status;
    protected volatile @Nullable String chargingStatus;
    protected volatile @Nullable String evStatus;
    protected volatile @Nullable String couplingStatus;
    protected volatile @Nullable Integer electricalPower;
    protected volatile @Nullable String chargingMode;
    protected volatile float targetDistance;
    protected volatile @Nullable String targetTime;
    protected volatile boolean boost;
    protected volatile float reachableDistance;
    protected volatile @Nullable String nextChargingTime;
    protected volatile double reading;
    protected volatile double dayReading;
    protected volatile @Nullable LocalDateTime lastReadingUTC;

    private @Nullable NhcCarChargerEvent eventHandler;

    private ScheduledExecutorService scheduler;
    private volatile @Nullable ScheduledFuture<?> readingSchedule;

    private Random r = new Random();

    protected NhcCarCharger(String id, String name, @Nullable String location, NikoHomeControlCommunication nhcComm,
            ScheduledExecutorService scheduler) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.nhcComm = nhcComm;
        this.scheduler = scheduler;
    }

    /**
     * This method should be called when an object implementing the {@link NhcCarChargerEvent} interface is initialized.
     * It keeps a record of the event handler in that object so it can be updated when the car charger receives an
     * update
     * from the Niko Home Control IP-interface.
     *
     * @param eventHandler
     */
    public void setEventHandler(NhcCarChargerEvent eventHandler) {
        this.eventHandler = eventHandler;
    }

    /**
     * This method should be called when an object implementing the {@link NhcCarChargerEvent} interface is disposed.
     * It resets the reference, so no updates go to the handler anymore.
     *
     */
    public void unsetEventHandler() {
        this.eventHandler = null;
    }

    /**
     * Get id of car charger.
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Get name of car charger.
     *
     * @return car charger name
     */
    public String getName() {
        return name;
    }

    /**
     * Set name of car charger.
     *
     * @param name car charger name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get location name of car charger.
     *
     * @return location name
     */
    public @Nullable String getLocation() {
        return location;
    }

    /**
     * Set location name of car charger.
     *
     * @param location action location name
     */
    public void setLocation(@Nullable String location) {
        this.location = location;
    }

    /**
     * Returns the current status of the car charger.
     *
     * @return {@code true} if the car charger is active; {@code false} otherwise.
     */
    public boolean getStatus() {
        return status;
    }

    /**
     * Returns the current charging mode as a {@link String}.
     * Possible charging modes are SOLAR, NORMAL or SMART.
     * If the charging mode is not set, returns an empty string.
     *
     * @return the charging mode, or an empty string if not available
     */
    public String getChargingMode() {
        String chargingMode = this.chargingMode;
        return chargingMode != null ? chargingMode : "";
    }

    /**
     * Returns the extra target distance for charging in SMART mode.
     *
     * @return the target distance in km
     */
    public float getTargetDistance() {
        return targetDistance;
    }

    /**
     * Returns the target time for charging in SMART mode.
     * If the target time is not set, returns an empty string.
     *
     * @return the target time, or an empty string if not set
     */
    public String getTargetTime() {
        String targetTime = this.targetTime;
        return targetTime != null ? targetTime : "";
    }

    /**
     * Checks if the car charger is currently in SMART mode boost.
     * In boost mode, the car charger will not respect the current capacity limit.
     *
     * @return {@code true} if boost mode is active; {@code false} otherwise.
     */
    public boolean isBoost() {
        return boost;
    }

    /**
     * Returns the current charging status of the car charger.
     * Possible charging status is ACTIVE, INACTIVE, BATTERY FULL or ERROR.
     *
     * @return the charging status, or {@code null} if the status is unavailable.
     */
    public @Nullable String getChargingStatus() {
        return chargingStatus;
    }

    /**
     * Returns the current status of the electric vehicle (EV) connected to the charger.
     * Possible EV status is IDLE, CONNECTED or CHARGING.
     *
     * @return the EV charger status, or {@code null} if the status is unavailable.
     */
    public @Nullable String getEvStatus() {
        return evStatus;
    }

    /**
     * Returns the current coupling status of the car charger.
     * Possible coupling status is OK, NO INTERNET, NO CREDENTIALS, INVALID CREDENTIALS,
     * CONNECTION ERROR, CONNECTION TIMEOUT, API ERROR or UNKNOWN ERROR.
     *
     * @return the coupling status, or {@code null} if the status is unavailable.
     */
    public @Nullable String getCouplingStatus() {
        return couplingStatus;
    }

    /**
     * Returns the reachable distance when charging in SMART mode.
     *
     * @return the reachable distance in km
     */
    public float getReachableDistance() {
        return reachableDistance;
    }

    /**
     * Returns the next scheduled charging time.
     * If the next charging time is not set, returns an empty string.
     *
     * @return the next charging time, or an empty string if not available
     */
    public String getNextChargingTime() {
        String nextChargingTime = this.nextChargingTime;
        return nextChargingTime != null ? nextChargingTime : "";
    }

    /**
     * Returns the current charging electrical power value.
     * If the value is not set (i.e., {@code electricalPower} is {@code null}),
     * this method returns {@code 0}.
     *
     * @return the electrical power value, or {@code 0} if not available
     */
    public int getElectricalPower() {
        Integer electricalPower = this.electricalPower;
        return electricalPower != null ? electricalPower : 0;
    }

    /**
     * Updates the status and related channels of the car charger thing through the event handler.
     * If a parameter is {@code null}, the corresponding channel remains unchanged.
     *
     * @param status the new status of the car charger (true if active, false otherwise)
     * @param chargingStatus the new charging status, or {@code null} to keep the current value
     * @param evStatus the new EV status, or {@code null} to keep the current value
     * @param couplingStatus the new coupling status, or {@code null} to keep the current value
     * @param electricalPower the new electrical power value, or {@code null} to keep the current value
     */
    public void setStatus(@Nullable Boolean status, @Nullable String chargingStatus, @Nullable String evStatus,
            @Nullable String couplingStatus, @Nullable Integer electricalPower) {
        this.status = (status != null) ? status : this.status;
        this.chargingStatus = (chargingStatus != null) ? chargingStatus : this.chargingStatus;
        this.evStatus = (evStatus != null) ? evStatus : this.evStatus;
        this.couplingStatus = (couplingStatus != null) ? couplingStatus : this.couplingStatus;
        this.electricalPower = (electricalPower != null) ? electricalPower : this.electricalPower;

        NhcCarChargerEvent eventHandler = this.eventHandler;
        if (eventHandler != null) {
            logger.debug(
                    "update charger status for {} with status {}, charging status {}, EV status {}, coupling status {}, power {}",
                    id, this.status, this.chargingStatus, this.evStatus, this.couplingStatus, this.electricalPower);
            eventHandler.chargingStatusEvent(this.status, this.chargingStatus, this.evStatus, this.couplingStatus,
                    this.electricalPower);
        }
    }

    /**
     * @return the meter reading in kWh
     */
    public double getReading() {
        return reading;
    }

    /**
     * @return the meter reading for the current day in kWh
     */
    public double getDayReading() {
        return dayReading;
    }

    /**
     * @return last processed meter reading in UTC zone
     */
    public @Nullable LocalDateTime getLastReading() {
        return lastReadingUTC;
    }

    public void setReading(double reading, double dayReading, LocalDateTime lastReading) {
        this.reading = reading;
        this.dayReading = dayReading;
        this.lastReadingUTC = lastReading;
        updateReadingState();
    }

    /**
     * Updates the charging mode and related channels for the car charger thing.
     * If a parameter is {@code null}, the existing value is retained.
     * After updating, notifies the event handler (if present) with the new states.
     *
     * @param chargingMode the desired charging mode, or {@code null} to keep the current mode
     * @param targetDistance the target distance to charge for, or {@code null} to keep the current value
     * @param targetTime the target time for charging, or {@code null} or empty to keep the current value
     * @param boost whether boost mode is enabled, or {@code null} to keep the current value
     * @param reachableDistance the currently reachable distance, or {@code null} to keep the current value
     * @param nextChargingTime the next scheduled charging time, or {@code null} or empty to keep the current value
     */
    public void setChargingMode(@Nullable String chargingMode, @Nullable Float targetDistance,
            @Nullable String targetTime, @Nullable Boolean boost, @Nullable Float reachableDistance,
            @Nullable String nextChargingTime) {
        this.chargingMode = (chargingMode != null) ? chargingMode : this.chargingMode;
        this.targetDistance = (targetDistance != null) ? targetDistance : this.targetDistance;
        this.targetTime = (targetTime != null && !targetTime.isEmpty()) ? targetTime : this.targetTime;
        this.boost = (boost != null) ? boost : this.boost;
        this.reachableDistance = (reachableDistance != null) ? reachableDistance : this.reachableDistance;
        this.nextChargingTime = (nextChargingTime != null && !nextChargingTime.isEmpty()) ? nextChargingTime
                : this.nextChargingTime;
        NhcCarChargerEvent eventHandler = this.eventHandler;
        if (eventHandler != null) {
            logger.debug(
                    "update charging mode channel states for {} with charging mode {}, target distance {}, target time {}, boost {}, reachable distance {}, next charging time {}",
                    id, this.chargingMode, this.targetDistance, this.targetTime, this.boost, this.reachableDistance,
                    this.nextChargingTime);
            eventHandler.chargingModeEvent(this.chargingMode, this.targetDistance, this.targetTime, this.boost,
                    this.reachableDistance, this.nextChargingTime);
        }
    }

    /**
     * Update meter reading value of the meter without touching the meter definition (id, name) and without changing the
     * ThingHandler callback.
     *
     */
    protected void updateReadingState() {
        NhcCarChargerEvent handler = eventHandler;
        double reading = getReading();
        double dayReading = getDayReading();
        LocalDateTime lastReading = getLastReading();
        if ((handler != null) && (lastReading != null)) {
            logger.debug("update meter reading channels for {} with {}, day {}", id, reading, dayReading);
            handler.meterReadingEvent(reading, dayReading, lastReading);
        }
    }

    /**
     * Method called when car charger is removed from the Niko Home Control Controller.
     */
    public void carChargerRemoved() {
        logger.debug("car charger removed {}, {}", id, name);
        NhcCarChargerEvent eventHandler = this.eventHandler;
        if (eventHandler != null) {
            eventHandler.deviceRemoved();
            unsetEventHandler();
        }
    }

    /**
     * Changes the status of the car charger in the Niko Home Control system.
     * <p>
     * This method sends a command to update the car charger status for the specified device ID.
     *
     * @param status {@code true} to turn the car charger on, {@code false} to turn it off.
     */
    public void executeCarChargerStatus(boolean status) {
        logger.debug("change car charger status for {} to {}", id, status);
        nhcComm.executeCarChargerStatus(id, status);
    }

    /**
     * Changes the charging mode of the car charger in the Niko Home Control system.
     *
     * @param chargingMode The desired charging mode to set (SOLAR, NORMAL or SMART).
     * @param targetDistance The target distance (in kilometers) to be achieved during charging for SMART mode..
     * @param targetTime The target time (in ISO 8601 format or HH:mm) by which charging should be completed for SMART
     *            mode.
     */
    public void executeCarChargerChargingMode(String chargingMode, float targetDistance, String targetTime) {
        logger.debug("change car charger charging mode for {} to {}, target {} at {}", id, chargingMode, targetDistance,
                targetTime);
        nhcComm.executeCarChargerChargingMode(id, chargingMode, targetDistance, targetTime);
    }

    /**
     * Executes a command to change the charging boost state for SMART charging of the car charger in the Niko Home
     * Control system.
     * When boost is true, the capacity limit may not be respected.
     *
     * @param boost true to enable charging boost, false to disable it
     */
    public void executeCarChargerChargingBoost(boolean boost) {
        logger.debug("change car charger boost for {} to {}", id, boost);
        nhcComm.executeCarChargerChargingBoost(id, boost);
    }

    /**
     * Start receiving meter data at regular intervals. Initial delay will be random between 10 and 100s to reduce risk
     * of overloading controller during initialization.
     *
     * @param refresh interval between meter queries in minutes
     */
    public void startMeter(int refresh, String startDate) {
        stopMeter();
        int firstRefreshDelay = 10 + r.nextInt(90);
        logger.debug("schedule meter data refresh for {} every {} minutes, first refresh in {}s", id, refresh,
                firstRefreshDelay);
        readingSchedule = scheduler.scheduleWithFixedDelay(() -> {
            nhcComm.executeMeter(id, startDate);
        }, firstRefreshDelay, refresh * 60, TimeUnit.SECONDS);
    }

    /**
     * Stop receiving meter data.
     */
    public void stopMeter() {
        ScheduledFuture<?> schedule = readingSchedule;
        if (schedule != null) {
            schedule.cancel(true);
            readingSchedule = null;
        }
    }
}
