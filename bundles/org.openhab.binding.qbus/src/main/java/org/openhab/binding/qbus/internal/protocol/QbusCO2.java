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
import org.openhab.binding.qbus.internal.handler.QbusCO2Handler;

/**
 * The {@link QbusCO2} class represents the action Qbus CO2 output.
 *
 * @author Koen Schockaert - Initial Contribution
 */

@NonNullByDefault
public final class QbusCO2 {

    private @Nullable Integer state;

    private @Nullable QbusCO2Handler thingHandler;

    QbusCO2() {
    }

    /**
     * This method should be called if the ThingHandler for the thing corresponding to this CO2 is initialized.
     * It keeps a record of the thing handler in this object so the thing can be updated when
     * the CO2 output receives an update from the Qbus IP-interface.
     *
     * @param handler
     */
    public void setThingHandler(QbusCO2Handler handler) {
        this.thingHandler = handler;
    }

    /**
     * This method sets a pointer to the qComm CO2 of class {@link QbusCommuncation}.
     * This is then used to be able to call back the sendCommand method in this class to send a command to the
     * Qbus client.
     *
     * @param qComm
     */
    // public void setQComm(QbusCommunication qComm) {
    // this.qComm = qComm;
    // }

    /**
     * Get state of CO2.
     *
     * @return CO2 state
     */
    public @Nullable Integer getState() {
        return this.state;
    }

    /**
     * Update the value of the CO2.
     *
     * @param CO2 value
     */
    void updateState(@Nullable Integer state) {
        this.state = state;
        QbusCO2Handler handler = this.thingHandler;
        if (handler != null) {
            handler.handleStateUpdate(this);
        }
    }
}
