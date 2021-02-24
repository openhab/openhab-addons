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
import org.openhab.binding.qbus.internal.handler.QbusSceneHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link QbusScene} class represents the action Qbus Scene output.
 *
 * @author Koen Schockaert - Initial Contribution
 */

@NonNullByDefault
public final class QbusScene {

    private final Logger logger = LoggerFactory.getLogger(QbusScene.class);

    private @Nullable QbusCommunication qComm;

    public @Nullable QbusSceneHandler thingHandler;

    private @Nullable Integer state;

    private String id;

    QbusScene(String id) {
        this.id = id;
    }

    /**
     * This method sets a pointer to the qComm Scene of class {@link QbusCommuncation}.
     * This is then used to be able to call back the sendCommand method in this class to send a command to the
     * Qbus IP-interface when..
     *
     * @param qComm
     */
    public void setQComm(QbusCommunication qComm) {
        this.qComm = qComm;
    }

    /**
     * Sends action to Qbus.
     */
    public void execute(int value, String sn) {
        QbusMessageCmd qCmd = new QbusMessageCmd(sn, "executeScene").withId(this.id).withState(value);
        QbusCommunication comm = qComm;
        if (comm != null) {
            try {
                comm.sendMessage(qCmd);
            } catch (InterruptedException e) {
                logger.warn("Could not send command for scene {}, {}", this.id, e.getMessage());
            }
        }
    }

    public void setThingHandler(QbusSceneHandler handler) {
        this.thingHandler = handler;
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
}
