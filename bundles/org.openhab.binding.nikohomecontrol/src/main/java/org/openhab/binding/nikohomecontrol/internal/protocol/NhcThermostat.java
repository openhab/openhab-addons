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

import static org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlConstants.THERMOSTATMODES;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nikohomecontrol.internal.protocol.nhc1.NhcThermostat1;
import org.openhab.binding.nikohomecontrol.internal.protocol.nhc2.NhcThermostat2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NhcThermostat} class represents the thermostat Niko Home Control communication object. It contains all
 * fields representing a Niko Home Control thermostat and has methods to set the thermostat in Niko Home Control and
 * receive thermostat updates. Specific implementation are {@link NhcThermostat1} and {@link NhcThermostat2}.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public abstract class NhcThermostat {

    private final Logger logger = LoggerFactory.getLogger(NhcThermostat.class);

    protected NikoHomeControlCommunication nhcComm;

    protected String id;
    protected String name;
    protected @Nullable String location;
    protected volatile int measured;
    protected volatile int setpoint;
    protected volatile int mode;
    protected volatile int overrule;
    protected volatile int overruletime;
    protected volatile int ecosave;
    protected volatile int demand;

    private @Nullable volatile ZonedDateTime overruleStart;

    private @Nullable NhcThermostatEvent eventHandler;

    protected NhcThermostat(String id, String name, @Nullable String location, NikoHomeControlCommunication nhcComm) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.nhcComm = nhcComm;
    }

    /**
     * Update all values of the thermostat without touching the thermostat definition (id, name and location) and
     * without changing the ThingHandler callback.
     *
     * @param measured current temperature in 0.1°C multiples
     * @param setpoint the setpoint temperature in 0.1°C multiples
     * @param mode thermostat mode 0 = day, 1 = night, 2 = eco, 3 = off, 4 = cool, 5 = prog1, 6 = prog2, 7 =
     *            prog3
     * @param overrule the overrule temperature in 0.1°C multiples
     * @param overruletime in minutes
     * @param ecosave
     * @param demand 0 if no demand, > 0 if heating, < 0 if cooling
     */
    public void updateState(int measured, int setpoint, int mode, int overrule, int overruletime, int ecosave,
            int demand) {
        setMeasured(measured);
        setSetpoint(setpoint);
        setMode(mode);
        setOverrule(overrule);
        setOverruletime(overruletime);
        setEcosave(ecosave);
        setDemand(demand);

        updateChannels();
    }

    /**
     * Method called when thermostat is removed from the Niko Home Control Controller.
     */
    public void thermostatRemoved() {
        logger.debug("action removed {}, {}", id, name);
        NhcThermostatEvent eventHandler = this.eventHandler;
        if (eventHandler != null) {
            eventHandler.thermostatRemoved();
            unsetEventHandler();
        }
    }

    private void updateChannels() {
        NhcThermostatEvent handler = eventHandler;
        if (handler != null) {
            logger.debug("update channels for {}", id);
            handler.thermostatEvent(measured, setpoint, mode, overrule, demand);
        }
    }

    /**
     * This method should be called when an object implementing the {@link NhcThermostatEvent} interface is initialized.
     * It keeps a record of the event handler in that object so it can be updated when the action receives an update
     * from the Niko Home Control IP-interface.
     *
     * @param eventHandler
     */
    public void setEventHandler(NhcThermostatEvent eventHandler) {
        this.eventHandler = eventHandler;
    }

    /**
     * This method should be called when an object implementing the {@link NhcThermostatEvent} interface is disposed.
     * It resets the reference, so no updates go to the handler anymore.
     *
     */
    public void unsetEventHandler() {
        this.eventHandler = null;
    }

    /**
     * Get id of the thermostat.
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Get name of thermostat.
     *
     * @return thermostat name
     */
    public String getName() {
        return name;
    }

    /**
     * Set name of thermostat.
     *
     * @param name thermostat name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get location name of thermostat.
     *
     * @return location name
     */
    public @Nullable String getLocation() {
        return location;
    }

    /**
     * Set location name of thermostat.
     *
     * @param location thermostat location name
     */
    public void setLocation(@Nullable String location) {
        this.location = location;
    }

    /**
     * Get measured temperature.
     *
     * @return measured temperature in 0.1°C multiples
     */
    public int getMeasured() {
        return measured;
    }

    private void setMeasured(int measured) {
        this.measured = measured;
    }

    /**
     * @return the setpoint temperature in 0.1°C multiples
     */
    public int getSetpoint() {
        return setpoint;
    }

    private void setSetpoint(int setpoint) {
        this.setpoint = setpoint;
    }

    /**
     * Get the thermostat mode.
     *
     * @return the mode:
     *         0 = day, 1 = night, 2 = eco, 3 = off, 4 = cool, 5 = prog 1, 6 = prog 2, 7 = prog 3
     */
    public int getMode() {
        return mode;
    }

    private void setMode(int mode) {
        this.mode = mode;
    }

    /**
     * Get the overrule temperature.
     *
     * @return the overrule temperature in 0.1°C multiples
     */
    public int getOverrule() {
        if (overrule > 0) {
            return overrule;
        } else {
            return setpoint;
        }
    }

    private void setOverrule(int overrule) {
        this.overrule = overrule;
        if (overrule <= 0) {
            stopOverrule();
        }
    }

    /**
     * Get the duration for an overrule temperature
     *
     * @return the overruletime in minutes
     */
    public int getOverruletime() {
        return overruletime;
    }

    /**
     * Set the duration for an overrule temperature
     *
     * @param overruletime the overruletime in minutes
     */
    private void setOverruletime(int overruletime) {
        if (overruletime != this.overruletime) {
            if (overruletime <= 0) {
                stopOverrule();
            } else {
                startOverrule();
            }
            this.overruletime = overruletime;
        }
    }

    /**
     * @return the ecosave mode
     */
    public int getEcosave() {
        return ecosave;
    }

    /**
     * @param ecosave the ecosave mode to set
     */
    private void setEcosave(int ecosave) {
        this.ecosave = ecosave;
    }

    /**
     * @return the heating/cooling demand: 0 if no demand, 1 if heating, -1 if cooling
     */
    public int getDemand() {
        return demand;
    }

    /**
     * @param demand set the heating/cooling demand
     */
    private void setDemand(int demand) {
        this.demand = demand;
    }

    /**
     * Sends thermostat mode to Niko Home Control. This method is implemented in {@link NhcThermostat1} and
     * {@link NhcThermostat2}.
     *
     * @param mode
     */
    public abstract void executeMode(int mode);

    /**
     * Sends thermostat mode to Niko Home Control.
     *
     * @param mode allowed values are Day, Night, Eco, Off, Cool, Prog1, Prog2, Prog3
     */
    public void executeMode(String mode) {
        int intMode = Arrays.asList(THERMOSTATMODES).indexOf(mode);
        if (intMode < 0) { // if not recognized, default to Day
            intMode = 0;
            logger.debug("Thermostat mode {} not recognized, default to Day mode", mode);
        }
        executeMode(intMode);
    };

    /**
     * Sends thermostat setpoint to Niko Home Control. This method is implemented in {@link NhcThermostat1} and
     * {@link NhcThermostat2}.
     *
     * @param overrule temperature to overrule the setpoint in 0.1°C multiples
     * @param overruletime time duration in min for overrule
     */
    public abstract void executeOverrule(int overrule, int overruletime);

    /**
     * @return remaining overrule time in minutes, 0 or positive
     */
    public int getRemainingOverruletime() {
        int remainingTime = 0;
        if (overruleStart != null) {
            // overruletime time max 23h59min, therefore can safely cast to int
            remainingTime = Math.max(0, overruletime - (int) ChronoUnit.MINUTES.between(overruleStart,
                    LocalDateTime.now().atZone(nhcComm.getTimeZone())));
        }
        logger.trace("Getting remaining overrule time, remaining: {}", remainingTime);
        return remainingTime;
    }

    /**
     * Start a new overrule, this method is used to be able to calculate the remaining overrule time
     */
    private void startOverrule() {
        overruleStart = LocalDateTime.now().atZone(nhcComm.getTimeZone());
    }

    /**
     * Reset overrule start
     */
    private void stopOverrule() {
        overruleStart = null;
        overruletime = 0;
        overrule = 0;
    }
}
