/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import java.time.temporal.ChronoUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nikohomecontrol.internal.handler.NikoHomeControlThermostatHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NhcThermostat} class represents the thermostat Niko Home Control communication object. It contains all
 * fields representing a Niko Home Control thermostat and has methods to set the thermostat in Niko Home Control and
 * receive thermostat updates.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public final class NhcThermostat {

    private final Logger logger = LoggerFactory.getLogger(NhcThermostat.class);

    @Nullable
    private NikoHomeControlCommunication nhcComm;

    private int id;
    private String name;
    private String location;
    private Integer measured = 0;
    private Integer setpoint = 0;
    private Integer mode = 0;
    private Integer overrule = 0;
    private Integer overruletime = 0;
    private Integer ecosave = 0;

    @Nullable
    private LocalDateTime overruleStart;

    @Nullable
    private NikoHomeControlThermostatHandler thingHandler;

    NhcThermostat(int id, String name, String location) {
        this.id = id;
        this.name = name;
        this.location = location;
    }

    /**
     * Update all values of the thermostat without touching the thermostat definition (id, name and location) and
     * without changing the ThingHandler callback.
     *
     * @param measured     current temperature in 0.1°C multiples
     * @param setpoint     the setpoint temperature in 0.1°C multiples
     * @param              mode: 0 = day, 1 = night, 2 = eco, 3 = off, 4 = cool, 5 = prog 1, 6 = prog 2, 7 = prog 3
     * @param overrule     the overrule temperature in 0.1°C multiples
     * @param overruletime in minutes
     * @param ecosave
     */
    public void updateState(Integer measured, Integer setpoint, Integer mode, Integer overrule, Integer overruletime,
            Integer ecosave) {
        setMeasured(measured);
        setSetpoint(setpoint);
        setMode(mode);
        setOverrule(overrule);
        setOverruletime(overruletime);
        setEcosave(ecosave);

        NikoHomeControlThermostatHandler handler = thingHandler;
        if (handler != null) {
            logger.debug("Niko Home Control: update channels for {}", id);
            handler.handleStateUpdate(this);
        }
    }

    /**
     * This method should be called if the ThingHandler for the thing corresponding to this action is initialized.
     * It keeps a record of the thing handler in this object so the thing can be updated when
     * the action receives an update from the Niko Home Control IP-interface.
     *
     * @param handler
     */
    public void setThingHandler(NikoHomeControlThermostatHandler handler) {
        this.thingHandler = handler;
    }

    /**
     * This method sets a pointer to the nhcComm object of class {@link NikoHomeControlCommuncation}.
     * This is then used to be able to call back the sendCommand method in this class to send a command to the
     * Niko Home Control IP-interface when..
     *
     * @param nhcComm
     */
    public void setNhcComm(NikoHomeControlCommunication nhcComm) {
        this.nhcComm = nhcComm;
    }

    /**
     * Get name of action.
     *
     * @return action name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get location name of action.
     *
     * @return location name
     */
    public String getLocation() {
        return this.location;
    }

    /**
     * Get measured temperature.
     *
     * @return measured temperature in 0.1°C multiples
     */
    public Integer getMeasured() {
        return this.measured;
    }

    private void setMeasured(Integer measured) {
        this.measured = measured;
    }

    /**
     * @return the setpoint temperature in 0.1°C multiples
     */
    public Integer getSetpoint() {
        return setpoint;
    }

    private void setSetpoint(Integer setpoint) {
        this.setpoint = setpoint;
    }

    /**
     * Get the thermostat mode.
     *
     * @return the mode: 0 = day, 1 = night, 2 = eco, 3 = off, 4 = cool, 5 = prog 1, 6 = prog 2, 7 = prog 3
     */
    public Integer getMode() {
        return mode;
    }

    private void setMode(Integer mode) {
        this.mode = mode;
    }

    /**
     * Get the overrule temperature.
     *
     * @return the overrule temperature in 0.1°C multiples
     */
    public Integer getOverrule() {
        return overrule;
    }

    private void setOverrule(Integer overrule) {
        this.overrule = overrule;
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
        if (overruletime <= 0) {
            stopOverrule();
        } else if (overruletime != this.overruletime) {
            startOverrule();
        }
        this.overruletime = overruletime;
    }

    /**
     * @return the ecosave mode
     */
    public Integer getEcosave() {
        return ecosave;
    }

    /**
     * @param ecosave the ecosave mode to set
     */
    private void setEcosave(Integer ecosave) {
        this.ecosave = ecosave;
    }

    /**
     * Sends thermostat mode to Niko Home Control.
     *
     * @param mode
     */
    public void executeMode(int mode) {
        logger.debug("Niko Home Control: execute thermostat mode {} for {}", mode, this.id);

        NhcMessageCmd nhcCmd = new NhcMessageCmd("executethermostat", this.id).withMode(mode);

        NikoHomeControlCommunication comm = nhcComm;
        if (comm != null) {
            comm.sendMessage(nhcCmd);
        }
    }

    /**
     * Sends thermostat setpoint to Niko Home Control.
     *
     * @param overrule temperature to overrule the setpoint in 0.1°C multiples
     * @param time     time duration in min for overrule
     */
    public void executeOverrule(int overrule, int overruletime) {
        logger.debug("Niko Home Control: execute thermostat overrule {} during {} min for {}", overrule, overruletime,
                this.id);

        String overruletimeString = String.format("%1$02d:%2$02d", overruletime / 60, overruletime % 60);
        NhcMessageCmd nhcCmd = new NhcMessageCmd("executethermostat", this.id).withOverrule(overrule)
                .withOverruletime(overruletimeString);

        NikoHomeControlCommunication comm = nhcComm;
        if (comm != null) {
            comm.sendMessage(nhcCmd);
        }
    }

    /**
     * @return remaining overrule time in minutes
     */
    public long getRemainingOverruletime() {
        if (overruleStart == null) {
            return 0;
        } else {
            return overruletime - ChronoUnit.MINUTES.between(overruleStart, LocalDateTime.now());
        }
    }

    /**
     * Start a new overrule, this method is used to be able to calculate the remaining overrule time
     */
    private void startOverrule() {
        this.overruleStart = LocalDateTime.now();
    }

    /**
     * Reset overrule start
     */
    private void stopOverrule() {
        this.overruleStart = null;
    }
}
