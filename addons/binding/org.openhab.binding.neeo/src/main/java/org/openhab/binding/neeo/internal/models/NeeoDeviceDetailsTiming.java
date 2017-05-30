/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo.internal.models;

/**
 * The model representing an Neeo Device Details Timings (serialize/deserialize json use only)
 *
 * @author Tim Roberts - Initial contribution
 */
public class NeeoDeviceDetailsTiming {

    /** Standby delay in ms (time to turn on device) */
    private final Integer standbyCommandDelay;

    /** Source switch in ms (time to switch inputs) */
    private final Integer sourceSwitchDelay;

    /** Shutdown delay in ms (tiem to shutdown device) */
    private final Integer shutdownDelay;

    /**
     * Constructs the timings option
     * 
     * @param standbyCommandDelay the standby delay (in ms)
     * @param sourceSwitchDelay the source switch delay (in ms)
     * @param shutdownDelay the shutdown delay (in ms)
     */
    public NeeoDeviceDetailsTiming(Integer standbyCommandDelay, Integer sourceSwitchDelay, Integer shutdownDelay) {
        this.standbyCommandDelay = standbyCommandDelay;
        this.sourceSwitchDelay = sourceSwitchDelay;
        this.shutdownDelay = shutdownDelay;
    }

    /**
     * The time (in ms) to turn on device. May be null if not supported
     * 
     * @return a possibly null time to turn on device
     */
    public Integer getStandbyCommandDelay() {
        return standbyCommandDelay;
    }

    /**
     * The time (in ms) to switch inputs. May be null if not supported
     * 
     * @return a possibly null time to switch inputs
     */
    public Integer getSourceSwitchDelay() {
        return sourceSwitchDelay;
    }

    /**
     * The time (in ms) to shutdown device. May be null if not supported
     * 
     * @return a possibly null time to shut down device
     */
    public Integer getShutdownDelay() {
        return shutdownDelay;
    }

    @Override
    public String toString() {
        return "NeeoDeviceDetailsTiming [standbyCommandDelay=" + standbyCommandDelay + ", sourceSwitchDelay="
                + sourceSwitchDelay + ", shutdownDelay=" + shutdownDelay + "]";
    }

}
