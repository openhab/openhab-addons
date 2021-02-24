/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.qbus.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.qbus.internal.handler.QbusThermostatHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link QbusThermostat} class represents the thermostat Qbus communication object. It contains all
 * fields representing a Qbus thermostat and has methods to set the thermostat mode and setpoint in Qbus and
 * receive thermostat updates.
 *
 * @author Koen Schockaert - Initial Contribution
 */

@NonNullByDefault
public final class QbusThermostat {

    private final Logger logger = LoggerFactory.getLogger(QbusThermostat.class);

    private @Nullable QbusCommunication qComm;

    private String id;
    private double measured = 0.0;
    private double setpoint = 0.0;
    private Integer mode = 0;

    private @Nullable QbusThermostatHandler thingHandler;

    QbusThermostat(String id) {
        this.id = id;
    }

    /**
     * Update all values of the thermostat
     *
     * @param measured current temperature in 1°C multiples
     * @param setpoint the setpoint temperature in 1°C multiples
     * @param mode 0="Manueel", 1="Vorst", 2="Economisch", 3="Comfort", 4="Nacht"
     */
    public void updateState(Double measured, Double setpoint, Integer mode) {
        setMeasured(measured);
        setSetpoint(setpoint);
        setMode(mode);

        QbusThermostatHandler handler = thingHandler;
        if (handler != null) {
            handler.handleStateUpdate(this);
        }
    }

    /**
     * This method should be called if the ThingHandler for the thing corresponding to the termostat is initialized.
     * It keeps a record of the thing handler in this object so the thing can be updated when
     * the thermostat receives an update from the Qbus IP-interface.
     *
     * @param handler
     */
    public void setThingHandler(QbusThermostatHandler handler) {
        this.thingHandler = handler;
    }

    /**
     * This method sets a pointer to the qComm object of class {@link QbusCommuncation}.
     * This is then used to be able to call back the sendCommand method in this class to send a command to the
     * Qbus IP-interface when..
     *
     * @param qComm
     */
    public void setQComm(QbusCommunication qComm) {
        this.qComm = qComm;
    }

    /**
     * Get measured temperature.
     *
     * @return measured temperature in 0.5°C multiples
     */
    public double getMeasured() {
        return this.measured;
    }

    /**
     * Set measured temperature.
     *
     * @param measured
     */
    private void setMeasured(Double measured) {
        this.measured = measured;
    }

    /**
     * Get setpoint
     *
     * @return the setpoint temperature in 0.5°C multiples
     */
    public double getSetpoint() {
        return this.setpoint;
    }

    /**
     * Set setpoint temperature.
     *
     * @param setpoint
     */
    private void setSetpoint(Double setpoint) {
        this.setpoint = setpoint;
    }

    /**
     * Get the thermostat mode.
     *
     * @return the mode: 0="Manueel", 1="Vorst", 2="Economisch", 3="Comfort", 4="Nacht"
     */
    public Integer getMode() {
        return mode;
    }

    /**
     * Set the thermostat
     *
     * mode: 0="Manueel", 1="Vorst", 2="Economisch", 3="Comfort", 4="Nacht"
     */
    private void setMode(Integer mode) {
        this.mode = mode;
    }

    /**
     * Sends thermostat mode to Qbus.
     *
     * @param mode
     * @param sn
     */
    public void executeMode(int mode, String sn) {
        QbusMessageCmd qCmd = new QbusMessageCmd(sn, "executeThermostat").withId(this.id).withMode(mode);
        QbusCommunication comm = qComm;
        if (comm != null) {
            try {
                comm.sendMessage(qCmd);
            } catch (InterruptedException e) {
                logger.warn("Could not send command mode for thermostat {}, {}", this.id, e.getMessage());
            }
        }
    }

    /**
     * Sends setpoint to Qbus.
     *
     * @param d
     */
    public void executeSetpoint(double d, String sn) {
        QbusMessageCmd qCmd = new QbusMessageCmd(sn, "executeThermostat").withId(this.id).withSetPoint(setpoint);
        QbusCommunication comm = qComm;
        if (comm != null) {
            try {
                comm.sendMessage(qCmd);
            } catch (InterruptedException e) {
                logger.warn("Could not send command setpoitn for thermostat {}, {}", this.id, e.getMessage());
            }
        }
    }
}
