/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.onebusaway.internal.config;

import static org.openhab.binding.onebusaway.internal.OneBusAwayBindingConstants.*;

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
        return getClass().getSimpleName() + "{ " + STOP_CONFIG_INTERVAL + "=" + this.getInterval() + ", "
                + STOP_CONFIG_ID + "=" + this.getStopId() + "}";
    }
}
