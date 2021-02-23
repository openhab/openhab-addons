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

    private @Nullable QbusCommunication QComm;

    private String id;

    private @Nullable Integer state;

    private @Nullable Integer slats;

    private @Nullable QbusRolHandler thingHandler;

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
    public @Nullable Integer getState() {
        if (this.state != null) {
            return this.state;
        } else {
            return null;
        }
    }

    /**
     * Get state of slats.
     *
     * @return slats state
     */
    public @Nullable Integer getStateSlats() {
        if (this.slats != null) {
            return this.slats;
        } else {
            return null;
        }
    }

    /**
     * Sets state of Shutter.
     *
     * @param shutter state
     */
    public void setState(Integer stat) {
        this.state = stat;
        QbusRolHandler handler = this.thingHandler;
        if (handler != null) {
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
        QbusRolHandler handler = this.thingHandler;
        if (handler != null) {
            handler.handleStateUpdate(this);
        }
    }

    /**
     * Sends shutter to Qbus.
     */
    public void execute(int value, String sn) {
        QbusMessageCmd QCmd = new QbusMessageCmd(sn, "executeStore").withId(this.id).withState(value);
        QbusCommunication comm = QComm;
        if (comm != null) {
            try {
                comm.sendMessage(QCmd);
            } catch (InterruptedException e) {
                logger.warn("Could not send command for store {}", this.id);
            }
        }
    }

    /**
     * Sends slats to Qbus.
     */
    public void executeSlats(int value, String sn) {
        QbusMessageCmd QCmd = new QbusMessageCmd(sn, "executeSlats").withId(this.id).withSlatState(value);
        QbusCommunication comm = QComm;
        if (comm != null) {
            try {
                comm.sendMessage(QCmd);
            } catch (InterruptedException e) {
                logger.warn("Could not send command for slat {}", this.id);
            }
        }
    }
}
