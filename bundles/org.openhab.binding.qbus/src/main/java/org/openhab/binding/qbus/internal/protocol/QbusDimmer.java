/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link QbusDimmer} class represents the action Qbus Dimmer output.
 *
 * @author Koen Schockaert - Initial Contribution
 */

@NonNullByDefault
public final class QbusDimmer {

    private final Logger logger = LoggerFactory.getLogger(QbusDimmer.class);

    @Nullable
    private QbusCommunication QComm;

    private String id = "";
    private Integer state = 0;

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
            logger.info("Qbus: update channels for {}", id);
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
    public Integer getState() {
        return this.state;
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
            logger.info("Update channel state for {} with {}", id, state);
            handler.handleStateUpdate(this);
        }
    }

    /**
     * Sends Dimmer state to Qbus.
     */
    public void execute(int percent, String sn) {
        logger.info("Execute dimmer for {} ", this.id);

        QbusMessageCmd QCmd = new QbusMessageCmd(sn, "executeDimmer").withId(this.id).withState(percent);

        QbusCommunication comm = QComm;
        if (comm != null) {
            comm.sendMessage(QCmd);
        }
    }
}
