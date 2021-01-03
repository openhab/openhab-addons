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
import org.openhab.binding.qbus.internal.handler.QbusCO2Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link QbusCO2} class represents the action Qbus CO2 output.
 *
 * @author Koen Schockaert - Initial Contribution
 */

@NonNullByDefault
public final class QbusCO2 {

    private final Logger logger = LoggerFactory.getLogger(QbusCO2.class);

    private String co2Id;
    private Integer co2State = 0;

    @Nullable
    private QbusCO2Handler thingHandler;

    QbusCO2(String co2Id) {
        this.co2Id = co2Id;
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
     * Get state of CO2.
     *
     * @return CO2 state
     */
    public Integer getState() {
        return this.co2State;
    }

    /**
     * Sets state of CO2.
     *
     * @param CO2 state
     */
    public void setState(Integer co2) {
        this.co2State = co2;
        QbusCO2Handler handler = thingHandler;
        if (handler != null) {
            logger.info("Update channel state for {} with {}", co2Id, co2State);
            handler.handleStateUpdate(this);
        }
    }
}
