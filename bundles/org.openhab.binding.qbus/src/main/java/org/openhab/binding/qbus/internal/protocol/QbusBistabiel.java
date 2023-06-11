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
import org.openhab.binding.qbus.internal.handler.QbusBistabielHandler;

/**
 * The {@link QbusBistabiel} class represents the Qbus BISTABIEL output.
 *
 * @author Koen Schockaert - Initial Contribution
 */

@NonNullByDefault
public final class QbusBistabiel {

    private @Nullable QbusCommunication qComm;

    private Integer id;

    private @Nullable Integer state;

    private @Nullable QbusBistabielHandler thingHandler;

    QbusBistabiel(Integer id) {
        this.id = id;
    }

    /**
     * This method should be called if the ThingHandler for the thing corresponding to this bistabiel is initialized.
     * It keeps a record of the thing handler in this object so the thing can be updated when
     * the bistable output receives an update from the Qbus client.
     *
     * @param handler
     */
    public void setThingHandler(QbusBistabielHandler handler) {
        this.thingHandler = handler;
    }

    /**
     * This method sets a pointer to the qComm BISTABIEL of class {@link QbusCommuncation}.
     * This is then used to be able to call back the sendCommand method in this class to send a command to the
     * Qbus client.
     *
     * @param qComm
     */
    public void setQComm(QbusCommunication qComm) {
        this.qComm = qComm;
    }

    /**
     * Update the value of the Bistabiel.
     *
     * @param state
     */
    void updateState(@Nullable Integer state) {
        this.state = state;
        QbusBistabielHandler handler = this.thingHandler;
        if (handler != null) {
            handler.handleStateUpdate(this);
        }
    }

    /**
     * Get the value of the Bistabiel.
     *
     * @return
     */
    public @Nullable Integer getState() {
        return this.state;
    }

    /**
     * Sends Bistabiel state to Qbus.
     *
     * @param value
     * @param sn
     * @throws InterruptedException
     * @throws IOException
     */
    public void execute(int value, String sn) throws InterruptedException, IOException {
        QbusMessageCmd qCmd = new QbusMessageCmd(sn, "executeBistabiel").withId(this.id).withState(value);
        QbusCommunication comm = this.qComm;
        if (comm != null) {
            comm.sendMessage(qCmd);
        }
    }
}
