/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
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
    private Boolean forceUpdate;
    private Double delay;

    public HmDatapointConfig() {
    }

    public HmDatapointConfig(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    /**
     * Returns true, if the cache of the datapoint should be ignored.
     */
    public boolean isForceUpdate() {
        return forceUpdate == null ? false : forceUpdate;
    }

    /**
     * Sets the forcedUpdate flag.
     */
    public void setForceUpdate(Boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

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
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("forceUpdate", forceUpdate)
                .append("delay", delay).toString();
    }

}
