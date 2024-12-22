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

import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NikoHomeControlCommunication} class is an abstract class representing the communication objects with the
 * Niko Home Control System.
 * {@link org.openhab.binding.nikohomecontrol.internal.protocol.nhc1.NikoHomeControlCommunication1} or
 * {@link org.openhab.binding.nikohomecontrol.internal.protocol.nhc2.NikoHomeControlCommunication2} should be
 * used for the respective version of Niko Home Control.
 * <ul>
 * <li>Start and stop communication with the Niko Home Control System.
 * <li>Read all setup and status information from the Niko Home Control Controller.
 * <li>Execute Niko Home Control commands.
 * <li>Listen to events from Niko Home Control.
 * </ul>
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public abstract class NikoHomeControlCommunication {

    private final Logger logger = LoggerFactory.getLogger(NikoHomeControlCommunication.class);

    protected final Map<String, NhcAction> actions = new ConcurrentHashMap<>();
    protected final Map<String, NhcThermostat> thermostats = new ConcurrentHashMap<>();
    protected final Map<String, NhcMeter> meters = new ConcurrentHashMap<>();
    protected final Map<String, NhcAccess> accessDevices = new ConcurrentHashMap<>();
    protected final Map<String, NhcVideo> videoDevices = new ConcurrentHashMap<>();
    protected final Map<String, NhcAlarm> alarmDevices = new ConcurrentHashMap<>();

    protected final NhcControllerEvent handler;

    protected final ScheduledExecutorService scheduler;

    // restart attempts
    private volatile int delay = 0;
    private volatile int attempt = 0;
    protected volatile @Nullable ScheduledFuture<?> scheduledRestart = null;

    protected NikoHomeControlCommunication(NhcControllerEvent handler, ScheduledExecutorService scheduler) {
        this.handler = handler;
        this.scheduler = scheduler;
    }

    /**
     * Start Communication with Niko Home Control system.
     */
    public abstract void startCommunication();

    /**
     * Stop Communication with Niko Home Control system.
     */
    public void stopCommunication() {
        stopScheduledRestart();

        resetCommunication();
    }

    /**
     * Stop Communication with Niko Home Control system, but keep reconnection attempts going.
     */
    public abstract void resetCommunication();

    protected synchronized void stopScheduledRestart() {
        ScheduledFuture<?> future = scheduledRestart;
        if (future != null) {
            future.cancel(true);
        }
        scheduledRestart = null;
        delay = 0;
        attempt = 0;
    }

    /**
     * Close and restart communication with Niko Home Control system.
     */
    public synchronized void restartCommunication() {
        resetCommunication();

        startCommunication();
    }

    private synchronized void checkAndRestartCommunication() {
        restartCommunication();

        // Try again if it didn't succeed
        if (!communicationActive()) {
            attempt++;
            delay = ((attempt <= 5) ? 30 : 60);
            logger.debug("schedule communication restart in {} seconds", delay);
            scheduledRestart = scheduler.schedule(this::checkAndRestartCommunication, delay, TimeUnit.SECONDS);
        } else {
            stopScheduledRestart();
        }
    }

    /**
     * Close and restart communication with Niko Home Control system. This method will keep doing multiple reconnection
     * attempts, starting immediately, then 5 times with 30 second intervals and every minute thereafter until the
     * connection is re-established.
     */
    public synchronized void scheduleRestartCommunication() {
        // Don't do this if we already scheduled to restart
        if (scheduledRestart == null) {
            delay = 0;
            attempt = 0;
            scheduledRestart = scheduler.schedule(this::checkAndRestartCommunication, 0, TimeUnit.SECONDS);
        }
    }

    /**
     * Method to check if communication with Niko Home Control is active. This method can be blocking for max 5s to wait
     * for completion of startup.
     *
     * @return True if active
     */
    public abstract boolean communicationActive();

    /**
     * Return the timezone for the system.
     *
     * @return zoneId
     */
    public ZoneId getTimeZone() {
        return handler.getTimeZone();
    }

    /**
     * Return all actions in the Niko Home Control Controller.
     *
     * @return <code>Map&lt;String, {@link NhcAction}></code>
     */
    public Map<String, NhcAction> getActions() {
        return actions;
    }

    /**
     * Return all thermostats in the Niko Home Control Controller.
     *
     * @return <code>Map&lt;String, {@link NhcThermostat}></code>
     */
    public Map<String, NhcThermostat> getThermostats() {
        return thermostats;
    }

    /**
     * Return all meters in the Niko Home Control Controller.
     *
     * @return <code>Map&ltString, {@link NhcMeter}></code>
     */
    public Map<String, NhcMeter> getMeters() {
        return meters;
    }

    /**
     * Return all access devices in the Niko Home Control Controller.
     *
     * @return <code>Map&ltString, {@link NhcAccess}></code>
     */
    public Map<String, NhcAccess> getAccessDevices() {
        return accessDevices;
    }

    /**
     * Return all video devices in the Niko Home Control Controller.
     *
     * @return <code>Map&ltString, {@link NhcVideo}></code>
     */
    public Map<String, NhcVideo> getVideoDevices() {
        return videoDevices;
    }

    /**
     * Return all alarm devices in the Niko Home Control Controller.
     *
     * @return <code>Map&lt;String, {@link NhcAlarm}></code>
     */
    public Map<String, NhcAlarm> getAlarmDevices() {
        return alarmDevices;
    }

    /**
     * Execute an action command by sending it to Niko Home Control.
     *
     * @param actionId
     * @param value
     */
    public abstract void executeAction(String actionId, String value);

    /**
     * Execute a thermostat command by sending it to Niko Home Control.
     *
     * @param thermostatId
     * @param mode
     */
    public abstract void executeThermostat(String thermostatId, String mode);

    /**
     * Execute a thermostat command by sending it to Niko Home Control.
     *
     * @param thermostatId
     * @param overruleTemp
     * @param overruleTime
     */
    public abstract void executeThermostat(String thermostatId, int overruleTemp, int overruleTime);

    /**
     * Query meter for energy, gas consumption or water production/consumption data. The query will update the total
     * production/consumption and production/consumption from the start of the day through the meterReadingEvent
     * callback in {@link NhcMeterEvent}.
     *
     * @param meterId
     */
    public abstract void executeMeter(String meterId);

    /**
     * Start retrieving energy meter data from Niko Home Control. The method is used to regularly retrigger the
     * information flow. It can be left empty in concrete classes if the power data is flowing continuously.
     *
     * @param meterId
     */
    public void startMeterLive(String meterId) {
        NhcMeter meter = getMeters().get(meterId);
        if (meter != null) {
            meter.startMeterLive();
        }
    }

    /**
     * Retrigger retrieving energy meter data from Niko Home Control. This is used if the power data does not continue
     * flowing automatically and needs to be retriggered at regular intervals.
     *
     * @param meterId
     */
    public abstract void retriggerMeterLive(String meterId);

    /**
     * Stop retrieving energy meter data from Niko Home Control. This method can be used to stop a scheduled retrigger
     * of the information flow, as scheduled in {{@link #startMeterLive(String)}.
     *
     * @param meterId
     */
    public void stopMeterLive(String meterId) {
        NhcMeter meter = getMeters().get(meterId);
        if (meter != null) {
            meter.stopMeterLive();
        }
    };

    /**
     * Start retrieving meter data from Niko Home Control at a regular interval.
     *
     * @param meterId
     * @param refresh reading frequency in minutes
     */
    public void startMeter(String meterId, int refresh) {
        NhcMeter meter = getMeters().get(meterId);
        if (meter != null) {
            meter.startMeter(refresh);
        }
    }

    /**
     * Stop retrieving meter data from Niko Home Control at a regular interval.
     */
    public void stopMeter(String meterId) {
        NhcMeter meter = getMeters().get(meterId);
        if (meter != null) {
            meter.stopMeter();
        }
    }

    /**
     * Stop retrieving meter data from Niko Home Control at a regular interval for all meters.
     */
    public void stopAllMeters() {
        for (String meterId : getMeters().keySet()) {
            stopMeter(meterId);
            stopMeterLive(meterId);
        }
    }

    /**
     * Execute a bell command on an access control device by sending it to Niko Home Control.
     *
     * @param accessId
     */
    public void executeAccessBell(String accessId) {
    }

    /**
     * Execute a bell command on video control device by sending it to Niko Home Control.
     *
     * @param accessId
     * @param buttonIndex
     */
    public void executeVideoBell(String accessId, int buttonIndex) {
    }

    /**
     * Switches state ring and come on access control device (turns on if off and off if on) by sending it to Niko Home
     * Control.
     *
     * @param accessId
     * @param ringAndComeIn status
     */
    public void executeAccessRingAndComeIn(String accessId, boolean ringAndComeIn) {
    }

    /**
     * Execute an unlock command on an access control device by sending it to Niko Home Control.
     *
     * @param accessId
     */
    public void executeAccessUnlock(String accessId) {
    }

    /**
     * Execute an arm command on an alarm control device by sending it to Niko Home Control.
     *
     * @param accessId
     */
    public void executeArm(String alarmId) {
    }

    /**
     * Execute an disarm command on an alarm control device by sending it to Niko Home Control.
     *
     * @param accessId
     */
    public void executeDisarm(String alarmId) {
    }
}
