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

    private @Nullable QbusCommunication qComm;

    private String id;

    private @Nullable Integer state;

    private @Nullable QbusBistabielHandler thingHandler;

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
     * This method sets a pointer to the qComm BISTABIEL of class {@link QbusCommuncation}.
     * This is then used to be able to call back the sendCommand method in this class to send a command to the
     * Qbus IP-interface when..
     *
     * @param qComm
     */
    public void setQComm(QbusCommunication qComm) {
        this.qComm = qComm;
    }

    /**
     * Get state of bistabiel.
     *
     * @return bistabiel state
     */
    public @Nullable Integer getState() {
        if (this.state != null) {
            return this.state;
        } else {
            return null;
        }
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
            handler.handleStateUpdate(this);
        }
    }

    /**
     * Sends bistabiel to Qbus.
     *
     * @throws InterruptedException
     */
    public void execute(int value, String sn) {
        QbusMessageCmd qCmd = new QbusMessageCmd(sn, "executeBistabiel").withId(this.id).withState(value);
        QbusCommunication comm = qComm;
        if (comm != null) {
            try {
                comm.sendMessage(qCmd);
            } catch (InterruptedException e) {
                logger.warn("Could not send command for bistabiel {}, {}", this.id, e.getMessage());

            }
        }
    }
}
