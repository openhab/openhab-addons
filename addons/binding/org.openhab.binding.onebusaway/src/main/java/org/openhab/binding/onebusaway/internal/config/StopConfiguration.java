/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.onebusaway.internal.config;

import static org.openhab.binding.onebusaway.OneBusAwayBindingConstants.*;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * The {@link StopConfiguration} defines the model for a stop bridge configuration.
 *
 * @author Shawn Wilsher - Initial contribution
 */
public class StopConfiguration {

    private Integer interval;
    private String stopId;

    /**
     * @return the update interval (in seconds).
     */
    public Integer getInterval() {
        return interval;
    }

    /**
     * Sets the update interval (in seconds).
     */
    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    /**
     * @return the stop ID.
     */
    public String getStopId() {
        return stopId;
    }

    /**
     * Sets the stop ID.
     */
    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append(STOP_CONFIG_INTERVAL, this.getInterval())
                .append(STOP_CONFIG_ID, this.getStopId()).toString();
    }
}
