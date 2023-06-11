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
package org.openhab.binding.neeo.internal.models;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The model representing an Neeo Device Details Timings (serialize/deserialize json use only)
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class NeeoDeviceDetailsTiming {

    /** Standby delay in ms (time to turn on device) */
    @Nullable
    private Integer standbyCommandDelay;

    /** Source switch in ms (time to switch inputs) */
    @Nullable
    private Integer sourceSwitchDelay;

    /** Shutdown delay in ms (time to shutdown device) */
    @Nullable
    private Integer shutdownDelay;

    /**
     * The time (in ms) to turn on device. May be null if not supported
     *
     * @return a possibly null time to turn on device
     */
    @Nullable
    public Integer getStandbyCommandDelay() {
        return standbyCommandDelay;
    }

    /**
     * The time (in ms) to switch inputs. May be null if not supported
     *
     * @return a possibly null time to switch inputs
     */
    @Nullable
    public Integer getSourceSwitchDelay() {
        return sourceSwitchDelay;
    }

    /**
     * The time (in ms) to shutdown device. May be null if not supported
     *
     * @return a possibly null time to shut down device
     */
    @Nullable
    public Integer getShutdownDelay() {
        return shutdownDelay;
    }

    @Override
    public String toString() {
        return "NeeoDeviceDetailsTiming [standbyCommandDelay=" + standbyCommandDelay + ", sourceSwitchDelay="
                + sourceSwitchDelay + ", shutdownDelay=" + shutdownDelay + "]";
    }
}
