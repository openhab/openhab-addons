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
import org.openhab.binding.qbus.internal.handler.QbusRolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link QbusRol} class represents the action Qbus Shutter/Slats output.
 *
 * @author Koen Schockaert - Initial Contribution
 */

@NonNullByDefault
public final class QbusRol {

    private final Logger logger = LoggerFactory.getLogger(QbusRol.class);

    @Nullable
    private QbusCommunication QComm;

    private String id = "";
    private Integer state = 0;
    private Integer slats = 0;

    @Nullable
    private QbusRolHandler thingHandler;

    QbusRol(String id) {
        this.id = id;
    }

    /**
     * This method should be called if the ThingHandler for the thing corresponding to this Shutter/Slats is
     * initialized.
     * It keeps a record of the thing handler in this object so the thing can be updated when
     * the shutter/slat receives an update from the Qbus IP-interface.
     *
     * @param qbusRolHandler
     */
    public void setThingHandler(QbusRolHandler qbusRolHandler) {
        this.thingHandler = qbusRolHandler;
    }

    /**
     * This method sets a pointer to the QComm Shutter/Slats of class {@link QbusCommuncation}.
     * This is then used to be able to call back the sendCommand method in this class to send a command to the
     * Qbus IP-interface when..
     *
     * @param QComm
     */
    public void setQComm(QbusCommunication QComm) {
        this.QComm = QComm;
    }

    /**
     * Get state of shutter.
     *
     * @return shutter state
     */
    public Integer getState() {
        return this.state;
    }

    /**
     * Get state of slats.
     *
     * @return slats state
     */
    public Integer getStateSlats() {
        return this.slats;
    }

    /**
     * Sets state of Shutter.
     *
     * @param shutter state
     */
    public void setState(Integer Slats) {
        this.state = Slats;
        QbusRolHandler handler = thingHandler;
        if (handler != null) {
            logger.info("Update channel shutter for {} with {}", id, state);
            handler.handleStateUpdate(this);
        }
    }

    /**
     * Sets state of Slats.
     *
     * @param slats state
     */
    public void setSlats(Integer Slats) {
        this.slats = Slats;
        QbusRolHandler handler = thingHandler;
        if (handler != null) {
            logger.info("Update channel slats for {} with {}", id, slats);
            handler.handleStateUpdate(this);
        }
    }

    /**
     * Sends shutter to Qbus.
     */
    public void execute(int value, String sn) {
        logger.info("Execute position for {}", this.id);

        QbusMessageCmd QCmd = new QbusMessageCmd(sn, "executeStore").withId(this.id).withState(value);

        QbusCommunication comm = QComm;
        if (comm != null) {
            comm.sendMessage(QCmd);
        }
    }

    /**
     * Sends slats to Qbus.
     */
    public void executeSlats(int value, String sn) {
        logger.info("Execute slats for {}", this.id);

        QbusMessageCmd QCmd = new QbusMessageCmd(sn, "executeStore").withId(this.id).withSlatState(value);

        QbusCommunication comm = QComm;
        if (comm != null) {
            comm.sendMessage(QCmd);
        }
    }
}
