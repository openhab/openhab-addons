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
package org.openhab.binding.qbus.internal.protocol;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.qbus.internal.handler.QbusRolHandler;

/**
 * The {@link QbusRol} class represents the action Qbus Shutter/Slats output.
 *
 * @author Koen Schockaert - Initial Contribution
 */

@NonNullByDefault
public final class QbusRol {

    private @Nullable QbusCommunication qComm;

    private Integer id;

    private @Nullable Integer state;

    private @Nullable Integer slats;

    private @Nullable QbusRolHandler thingHandler;

    QbusRol(Integer id) {
        this.id = id;
    }

    /**
     * This method should be called if the ThingHandler for the thing corresponding to this Shutter/Slats is
     * initialized.
     * It keeps a record of the thing handler in this object so the thing can be updated when
     * the shutter/slat receives an update from the Qbus client.
     *
     * @param qbusRolHandler
     */
    public void setThingHandler(QbusRolHandler qbusRolHandler) {
        this.thingHandler = qbusRolHandler;
    }

    /**
     * This method sets a pointer to the qComm Shutter/Slats of class {@link QbusCommuncation}.
     * This is then used to be able to call back the sendCommand method in this class to send a command to the
     * Qbus IP-interface when..
     *
     * @param qComm
     */
    public void setQComm(QbusCommunication qComm) {
        this.qComm = qComm;
    }

    /**
     * Update the value of the Shutter.
     *
     * @param Shutter value
     */
    public void updateState(@Nullable Integer state) {
        this.state = state;
        QbusRolHandler handler = this.thingHandler;
        if (handler != null) {
            handler.handleStateUpdate(this);
        }
    }

    /**
     * Update the value of the Slats.
     *
     * @param Slat value
     */
    public void updateSlats(@Nullable Integer Slats) {
        this.slats = Slats;
        QbusRolHandler handler = this.thingHandler;
        if (handler != null) {
            handler.handleStateUpdate(this);
        }
    }

    /**
     * Get the value of the Shutter.
     *
     * @return shutter value
     */
    public @Nullable Integer getState() {
        return this.state;
    }

    /**
     * Get the value of the Slats.
     *
     * @return slats value
     */
    public @Nullable Integer getStateSlats() {
        return this.slats;
    }

    /**
     * Sends shutter state to Qbus.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void execute(int value, String sn) throws InterruptedException, IOException {
        QbusMessageCmd qCmd = new QbusMessageCmd(sn, "executeStore").withId(this.id).withState(value);
        QbusCommunication comm = qComm;
        if (comm != null) {
            comm.sendMessage(qCmd);
        }
    }

    /**
     * Sends slats state to Qbus.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void executeSlats(int value, String sn) throws InterruptedException, IOException {
        QbusMessageCmd qCmd = new QbusMessageCmd(sn, "executeSlats").withId(this.id).withState(value);
        QbusCommunication comm = qComm;
        if (comm != null) {
            comm.sendMessage(qCmd);
        }
    }
}
