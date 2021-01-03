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
import org.openhab.binding.qbus.internal.handler.QbusBistabielHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link QbusBistabiel} class represents the Qbus BISTABIEL output.
 *
 * @author Koen Schockaert - Initial Contribution
 */

@NonNullByDefault
public final class QbusBistabiel {

    private final Logger logger = LoggerFactory.getLogger(QbusBistabiel.class);

    @Nullable
    private QbusCommunication QComm;

    private String id = "";
    private Integer state = 0;

    @Nullable
    private QbusBistabielHandler thingHandler;

    QbusBistabiel(String id) {
        this.id = id;
    }

    /**
     * This method should be called if the ThingHandler for the thing corresponding to this bistabiel is initialized.
     * It keeps a record of the thing handler in this object so the thing can be updated when
     * the bistable output receives an update from the Qbus IP-interface.
     *
     * @param handler
     */
    public void setThingHandler(QbusBistabielHandler handler) {
        this.thingHandler = handler;
    }

    /**
     * This method sets a pointer to the QComm BISTABIEL of class {@link QbusCommuncation}.
     * This is then used to be able to call back the sendCommand method in this class to send a command to the
     * Qbus IP-interface when..
     *
     * @param QComm
     */
    public void setQComm(QbusCommunication QComm) {
        this.QComm = QComm;
    }

    /**
     * Get state of bistabiel.
     *
     * @return bistabiel state
     */
    public Integer getState() {
        return this.state;
    }

    /**
     * Sets state of bistabiel.
     *
     * @param bistabiel state
     */
    void setState(int state) {
        this.state = state;
        QbusBistabielHandler handler = this.thingHandler;
        if (handler != null) {
            logger.info("Update bistabiel channel state for {} with {}", id, state);
            handler.handleStateUpdate(this);
        }
    }

    /**
     * Sends bistabiel to Qbus.
     */
    public void execute(int value, String sn) {

        logger.info("Execute bistabiel for {}", this.id);

        QbusMessageCmd QCmd = new QbusMessageCmd(sn, "executeBistabiel").withId(this.id).withState(value);

        QbusCommunication comm = QComm;
        if (comm != null) {
            comm.sendMessage(QCmd);
        }
    }
}
