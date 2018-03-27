/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.model;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

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
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("delay", delay)
                .append("receiveDelay", receiveDelay).toString();
    }

}
