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
import org.openhab.binding.qbus.internal.handler.QbusDimmerHandler;

/**
 * The {@link QbusDimmer} class represents the action Qbus Dimmer output.
 *
 * @author Koen Schockaert - Initial Contribution
 */

@NonNullByDefault
public final class QbusDimmer {

    private @Nullable QbusCommunication qComm;

    private Integer id;

    private @Nullable Integer state;

    private @Nullable QbusDimmerHandler thingHandler;

    QbusDimmer(Integer id) {
        this.id = id;
    }

    /**
     * This method should be called if the ThingHandler for the thing corresponding to this dimmer is initialized.
     * It keeps a record of the thing handler in this object so the thing can be updated when
     * the dimmer receives an update from the Qbus client.
     *
     * @param handler
     */
    public void setThingHandler(QbusDimmerHandler handler) {
        this.thingHandler = handler;
    }

    /**
     * This method sets a pointer to the qComm Dimmer of class {@link QbusCommuncation}.
     * This is then used to be able to call back the sendCommand method in this class to send a command to the
     * Qbus client.
     *
     * @param qComm
     */
    public void setQComm(QbusCommunication qComm) {
        this.qComm = qComm;
    }

    /**
     * Update the value of the dimmer
     *
     * @param state
     */
    public void updateState(@Nullable Integer state) {
        this.state = state;
        QbusDimmerHandler handler = this.thingHandler;
        if (handler != null) {
            handler.handleStateUpdate(this);
        }
    }

    /**
     * Get the state of dimmer.
     *
     * @return dimmer state
     */
    public @Nullable Integer getState() {
        return this.state;
    }

    /**
     * Sets the state of Dimmer.
     *
     * @param dimmer state
     */
    void setState(int state) {
        this.state = state;
        QbusDimmerHandler handler = thingHandler;
        if (handler != null) {
            handler.handleStateUpdate(this);
        }
    }

    /**
     * Sends the dimmer state to Qbus.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void execute(int percent, String sn) throws InterruptedException, IOException {
        QbusMessageCmd qCmd = new QbusMessageCmd(sn, "executeDimmer").withId(this.id).withState(percent);
        QbusCommunication comm = this.qComm;
        if (comm != null) {
            comm.sendMessage(qCmd);
        }
    }
}
