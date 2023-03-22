/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import org.openhab.binding.nikohomecontrol.internal.protocol.nhc1.NikoHomeControlCommunication1;
import org.openhab.binding.nikohomecontrol.internal.protocol.nhc2.NikoHomeControlCommunication2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NikoHomeControlCommunication} class is an abstract class representing the communication objects with the
 * Niko Home Control System. {@link NikoHomeControlCommunication1} or {@link NikoHomeControlCommunication2} should be
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
    protected final Map<String, NhcEnergyMeter> energyMeters = new ConcurrentHashMap<>();

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

        logger.debug("restart communication from thread {}", Thread.currentThread().getId());

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
     * @return <code>Map&ltString, {@link NhcAction}></code>
     */
    public Map<String, NhcAction> getActions() {
        return actions;
    }

    /**
     * Return all thermostats in the Niko Home Control Controller.
     *
     * @return <code>Map&ltString, {@link NhcThermostat}></code>
     */
    public Map<String, NhcThermostat> getThermostats() {
        return thermostats;
    }

    /**
     * Return all energyMeters meters in the Niko Home Control Controller.
     *
     * @return <code>Map&ltString, {@link NhcEnergyMeter}></code>
     */
    public Map<String, NhcEnergyMeter> getEnergyMeters() {
        return energyMeters;
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
     * Start retrieving energy meter data from Niko Home Control.
     *
     */
    public void startEnergyMeter(String energyMeterId) {
    };

    /**
     * Stop retrieving energy meter data from Niko Home Control.
     *
     */
    public void stopEnergyMeter(String energyMeterId) {
    };
}
