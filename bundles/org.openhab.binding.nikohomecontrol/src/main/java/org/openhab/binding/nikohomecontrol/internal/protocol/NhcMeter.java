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
package org.openhab.binding.nikohomecontrol.internal.protocol;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlConstants.MeterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NhcMeter} class represents the meter Niko Home Control communication object. It contains all fields
 * representing a Niko Home Control meter and has methods to receive meter usage information.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public abstract class NhcMeter {

    private final Logger logger = LoggerFactory.getLogger(NhcMeter.class);

    protected NikoHomeControlCommunication nhcComm;

    protected final String id;
    protected String name;
    protected MeterType type;
    protected @Nullable LocalDateTime referenceDateUTC;
    protected @Nullable String location;

    // This can be null as long as we do not receive power readings
    protected volatile @Nullable Integer power;
    protected volatile int reading;
    protected volatile int dayReading;
    protected volatile @Nullable LocalDateTime lastReadingUTC;

    private @Nullable NhcMeterEvent eventHandler;

    private ScheduledExecutorService scheduler;
    private volatile @Nullable ScheduledFuture<?> restartTimer;
    private volatile @Nullable ScheduledFuture<?> readingSchedule;

    private Random r = new Random();

    protected NhcMeter(String id, String name, MeterType type, @Nullable LocalDateTime referenceDate,
            @Nullable String location, NikoHomeControlCommunication nhcComm, ScheduledExecutorService scheduler) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.referenceDateUTC = referenceDate;
        this.location = location;
        this.nhcComm = nhcComm;
        this.scheduler = scheduler;
    }

    /**
     * Update power value of the meter without touching the meter definition (id, name) and without changing the
     * ThingHandler callback.
     *
     */
    protected void updatePowerState() {
        NhcMeterEvent handler = eventHandler;
        Integer value = getPower();
        if ((handler != null) && (value != null)) {
            logger.debug("update power channel for {} with {}", id, value);
            handler.meterPowerEvent(value);
        }
    }

    /**
     * Update meter reading value of the meter without touching the meter definition (id, name) and without changing the
     * ThingHandler callback.
     *
     */
    protected void updateReadingState() {
        NhcMeterEvent handler = eventHandler;
        double reading = getReading();
        double dayReading = getDayReading();
        LocalDateTime lastReading = getLastReading();
        if ((handler != null) && (lastReading != null)) {
            logger.debug("update meter reading channels for {} with {}, day {}", id, reading, dayReading);
            handler.meterReadingEvent(reading, dayReading, lastReading);
        }
    }

    /**
     * Method called when meter is removed from the Niko Home Control Controller.
     */
    public void meterRemoved() {
        logger.debug("meter removed {}, {}", id, name);
        NhcMeterEvent eventHandler = this.eventHandler;
        if (eventHandler != null) {
            eventHandler.deviceRemoved();
            unsetEventHandler();
        }
    }

    /**
     * This method should be called when an object implementing the {@NhcMeterEvent} interface is initialized.
     * It keeps a record of the event handler in that object so it can be updated when the action receives an update
     * from the Niko Home Control IP-interface.
     *
     * @param eventHandler
     */
    public void setEventHandler(NhcMeterEvent eventHandler) {
        this.eventHandler = eventHandler;
    }

    /**
     * This method should be called when an object implementing the {@NhcMeterEvent} interface is disposed.
     * It resets the reference, so no updates go to the handler anymore.
     *
     */
    public void unsetEventHandler() {
        this.eventHandler = null;
    }

    /**
     * Get the id of the meter.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Get name of the meter.
     *
     * @return meter name
     */
    public String getName() {
        return name;
    }

    /**
     * Set name of meter.
     *
     * @param name meter name
     */
    public void setName(String name) {
        this.name = name;
    }

    public MeterType getType() {
        return type;
    }

    /**
     * Get location name of meter.
     *
     * @return location name
     */
    public @Nullable String getLocation() {
        return location;
    }

    /**
     * Set location of meter.
     *
     * @param location meter location
     */
    public void setLocation(@Nullable String location) {
        this.location = location;
    }

    /**
     * @return the power in W (positive for consumption, negative for production), return null if no reading received
     *         yet
     */
    public @Nullable Integer getPower() {
        return power;
    }

    /**
     * @param power the power to set in W (positive for consumption, negative for production), null if an empty reading
     *            was received
     */
    public void setPower(@Nullable Integer power) {
        this.power = power;
        updatePowerState();
    }

    /**
     * @return the meter reading in the base unit (m^3 for gas and water, kWh for energy)
     */
    public double getReading() {
        // For energy, readings are in W per 10 min, convert to kWh
        // For water and gas, readings are in 0.1 dm^3, convert to m^3
        return ((type == MeterType.ENERGY) || (type == MeterType.ENERGY_LIVE)) ? (reading / 6000.0)
                : (reading / 10000.0);
    }

    /**
     * @return reading without conversion, as provided by NHC
     */
    public int getReadingInt() {
        return reading;
    }

    /**
     * @return the meter reading for the current day in the base unit (m^3 for gas and water, kWh for energy)
     */
    public double getDayReading() {
        // For energy, readings are in W per 10 min, convert to kWh
        // For water and gas, readings are in 0.1 dm^3, convert to m^3
        return ((type == MeterType.ENERGY) || (type == MeterType.ENERGY_LIVE)) ? (dayReading / 6000.0)
                : (dayReading / 10000.0);
    }

    /**
     * @return day reading without conversion, as provided by NHC
     */
    public int getDayReadingInt() {
        return dayReading;
    }

    /**
     * @return last processed meter reading in UTC zone
     */
    public @Nullable LocalDateTime getLastReading() {
        return lastReadingUTC;
    }

    /**
     * @param reading the meter reading
     * @param dayReading the day meter reading
     * @param lastReading the last meter reading time in UTC zone
     */
    public void setReading(int reading, int dayReading, LocalDateTime lastReading) {
        this.reading = reading;
        this.dayReading = dayReading;
        this.lastReadingUTC = lastReading;
        updateReadingState();
    }

    /**
     * Start the flow of energy information from the energy meter. The Niko Home Control energy meter will send power
     * information every 2s for 30s. This method will retrigger every 25s to make sure the information continues
     * flowing. If the information is no longer required, make sure to use the {@link stopMeterLive} method to stop the
     * flow of information.
     */
    public void startMeterLive() {
        stopMeterLive();
        restartTimer = scheduler.scheduleWithFixedDelay(() -> {
            nhcComm.retriggerMeterLive(id);
        }, 0, 25, TimeUnit.SECONDS);
    }

    /**
     * Cancel receiving energy information from the controller. We therefore stop the automatic retriggering of the
     * subscription, see {@link startMeterLive}.
     */
    public void stopMeterLive() {
        ScheduledFuture<?> timer = restartTimer;
        if (timer != null) {
            timer.cancel(true);
            restartTimer = null;
        }
    }

    /**
     * Start receiving meter data at regular intervals. Initial delay will be random between 10 and 100s to reduce risk
     * of overloading controller during initialization.
     *
     * @param refresh interval between meter queries in minutes
     */
    public void startMeter(int refresh) {
        stopMeter();
        int firstRefreshDelay = 10 + r.nextInt(90);
        logger.debug("schedule meter data refresh for {} every {} minutes, first refresh in {}s", id, refresh,
                firstRefreshDelay);
        readingSchedule = scheduler.scheduleWithFixedDelay(() -> {
            nhcComm.executeMeter(id);
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

    /**
     * @return start date and time for meter readings in UTC zone
     */
    public @Nullable LocalDateTime getReferenceDate() {
        return referenceDateUTC;
    }
}
