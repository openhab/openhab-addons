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
package org.openhab.binding.qbus.internal.protocol;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.qbus.internal.handler.QbusThermostatHandler;

/**
 * The {@link QbusThermostat} class represents the thermostat Qbus communication object. It contains all
 * fields representing a Qbus thermostat and has methods to set the thermostat mode and setpoint in Qbus and
 * receive thermostat updates.
 *
 * @author Koen Schockaert - Initial Contribution
 */

@NonNullByDefault
public final class QbusThermostat {

    private @Nullable QbusCommunication qComm;

    private Integer id;
    private double measured = 0.0;
    private double setpoint = 0.0;
    private @Nullable Integer mode;

    private @Nullable QbusThermostatHandler thingHandler;

    QbusThermostat(Integer id) {
        this.id = id;
    }

    /**
     * This method should be called if the ThingHandler for the thing corresponding to the termostat is initialized.
     * It keeps a record of the thing handler in this object so the thing can be updated when
     * the thermostat receives an update from the Qbus client.
     *
     * @param handler
     */
    public void setThingHandler(QbusThermostatHandler handler) {
        this.thingHandler = handler;
    }

    /**
     * This method sets a pointer to the qComm THERMOSTAT of class {@link QbusCommuncation}.
     * This is then used to be able to call back the sendCommand method in this class to send a command to the
     * Qbus client.
     *
     * @param qComm
     */
    public void setQComm(QbusCommunication qComm) {
        this.qComm = qComm;
    }

    /**
     * Update all values of the Thermostat
     *
     * @param measured current temperature in 1°C multiples
     * @param setpoint the setpoint temperature in 1°C multiples
     * @param mode 0="Manual", 1="Freeze", 2="Economic", 3="Comfort", 4="Night"
     */
    public void updateState(Double measured, Double setpoint, Integer mode) {
        this.measured = measured;
        this.setpoint = setpoint;
        this.mode = mode;

        QbusThermostatHandler handler = this.thingHandler;
        if (handler != null) {
            handler.handleStateUpdate(this);
        }
    }

    /**
     * Get measured temperature of the Thermostat.
     *
     * @return measured temperature in 0.5°C multiples
     */
    public @Nullable Double getMeasured() {
        return this.measured;
    }

    /**
     * Get setpoint temperature of the Thermostat.
     *
     * @return the setpoint temperature in 0.5°C multiples
     */
    public @Nullable Double getSetpoint() {
        return this.setpoint;
    }

    /**
     * Get the Thermostat mode.
     *
     * @return the mode: 0="Manual", 1="Freeze", 2="Economic", 3="Comfort", 4="Night"
     */
    public @Nullable Integer getMode() {
        return this.mode;
    }

    /**
     * Sends Thermostat mode to Qbus.
     *
     * @param mode
     * @param sn
     * @throws InterruptedException
     * @throws IOException
     */
    public void executeMode(int mode, String sn) throws InterruptedException, IOException {
        QbusMessageCmd qCmd = new QbusMessageCmd(sn, "executeThermostat").withId(this.id).withMode(mode);
        QbusCommunication comm = this.qComm;
        if (comm != null) {
            comm.sendMessage(qCmd);
        }
    }

    /**
     * Sends Thermostat setpoint to Qbus.
     *
     * @param setpoint
     * @throws IOException
     * @throws InterruptedException
     */
    public void executeSetpoint(double setpoint, String sn) throws InterruptedException, IOException {
        QbusMessageCmd qCmd = new QbusMessageCmd(sn, "executeThermostat").withId(this.id).withSetPoint(setpoint);
        QbusCommunication comm = this.qComm;
        if (comm != null) {
            comm.sendMessage(qCmd);
        }
    }
}
