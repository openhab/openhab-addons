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
import org.openhab.binding.qbus.internal.handler.QbusDimmerHandler;

/**
 * The {@link QbusDimmer} class represents the action Qbus Dimmer output.
 *
 * @author Koen Schockaert - Initial Contribution
 */

@NonNullByDefault
public final class QbusDimmer {

    @Nullable
    private QbusCommunication QComm;

    private String id;

    @Nullable
    private Integer state;

    @Nullable
    private QbusDimmerHandler thingHandler;

    QbusDimmer(String id) {
        this.id = id;
    }

    /**
     * Update all values of the dimmer
     *
     * @param state
     */
    public void updateState(Integer state) {
        setState(state);

        QbusDimmerHandler handler = thingHandler;
        if (handler != null) {
            handler.handleStateUpdate(this);
        }
    }

    /**
     * This method should be called if the ThingHandler for the thing corresponding to this dimmer is initialized.
     * It keeps a record of the thing handler in this object so the thing can be updated when
     * the dimmer receives an update from the Qbus IP-interface.
     *
     * @param handler
     */
    public void setThingHandler(QbusDimmerHandler handler) {
        this.thingHandler = handler;
    }

    /**
     * This method sets a pointer to the QComm Dimmer of class {@link QbusCommuncation}.
     * This is then used to be able to call back the sendCommand method in this class to send a command to the
     * Qbus IP-interface when..
     *
     * @param QComm
     */
    public void setQComm(QbusCommunication QComm) {
        this.QComm = QComm;
    }

    /**
     * Get state of dimmer.
     *
     * @return dimmer state
     */
    public @Nullable Integer getState() {
        if (this.state != null) {
            return this.state;
        } else {
            return null;
        }
    }

    /**
     * Sets state of Dimmer.
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
     * Sends Dimmer state to Qbus.
     */
    public void execute(int percent, String sn) {
        QbusMessageCmd QCmd = new QbusMessageCmd(sn, "executeDimmer").withId(this.id).withState(percent);
        QbusCommunication comm = QComm;
        if (comm != null) {
            comm.sendMessage(QCmd);
        }
    }
}
