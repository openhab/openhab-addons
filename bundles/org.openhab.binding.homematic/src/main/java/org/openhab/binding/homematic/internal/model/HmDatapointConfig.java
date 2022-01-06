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
package org.openhab.binding.homematic.internal.model;

/**
 * Configuration object for sending a datapoint.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class HmDatapointConfig {
    private Double delay;
    private Double receiveDelay;

    /**
     * Returns the delay in seconds for sending the datapoint.
     */
    public double getDelay() {
        return delay == null ? 0.0 : delay;
    }

    /**
     * Sets the delay in seconds for sending the datapoint.
     */
    public void setDelay(Double delay) {
        this.delay = delay;
    }

    /**
     * Returns the delay in seconds for receiving a new datapoint event.
     */
    public Double getReceiveDelay() {
        return receiveDelay == null ? 0.0 : receiveDelay;
    }

    /**
     * Sets the delay in seconds for receiving a datapoint event.
     */
    public void setReceiveDelay(Double receiveDelay) {
        this.receiveDelay = receiveDelay;
    }

    @Override
    public String toString() {
        return String.format("%s[delay=%f,receiveDelay=%f]", getClass().getSimpleName(), delay, receiveDelay);
    }
}
