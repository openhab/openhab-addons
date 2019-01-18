/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.io.neeo.internal.models;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This model specifies the various timings associated with the {@link NeeoDevice}
 *
 * @author Tim Roberts - Initial Contribution
 *
 */
@NonNullByDefault
public class NeeoDeviceTiming {
    // The default, min and max delays allowed by NEEO
    private static final int DEFAULT_DELAY = 5000;
    private static final int MIN_DELAY = 0;
    private static final int MAX_DELAY = 60000;

    /** Time (in ms) until the device is powered on and is ready to accept new commands */
    private final int standbyCommandDelay;

    /** Time (in ms) until the device switched input and is ready to accept new commands */
    private final int sourceSwitchDelay;

    /** Time (in ms) until the device is powered off and is ready to accept new commands */
    private final int shutdownDelay;

    /**
     * Constructs the timings from the {@link #DEFAULT_DELAY}
     */
    public NeeoDeviceTiming() {
        this(null, null, null);
    }

    /**
     * Constructs the timings from the specified delays. If the delay is < {@link #MIN_DELAY}, it is set to
     * {@link #MIN_DELAY}. If the delay is > {@link #MAX_DELAY}, the delay will be set to {@link #MAX_DELAY}. If any
     * delay is null, {@link #DEFAULT_DELAY} will be used instead
     *
     * @param standbyCommandDelay the time (in ms) for the device to power on
     * @param sourceSwitchDelay the time (in ms) to switch inputs
     * @param shutdownDelay the time (in ms) to power off
     */
    public NeeoDeviceTiming(@Nullable Integer standbyCommandDelay, @Nullable Integer sourceSwitchDelay,
            @Nullable Integer shutdownDelay) {
        this.standbyCommandDelay = constrainTime(standbyCommandDelay == null ? DEFAULT_DELAY : standbyCommandDelay);
        this.sourceSwitchDelay = constrainTime(sourceSwitchDelay == null ? DEFAULT_DELAY : sourceSwitchDelay);
        this.shutdownDelay = constrainTime(shutdownDelay == null ? DEFAULT_DELAY : shutdownDelay);
    }

    /**
     * Utility function to ensure the passed delay is withing bounds
     *
     * @param delay the delay to constrain
     * @return the constrained delay
     */
    private static int constrainTime(int delay) {
        if (delay < MIN_DELAY) {
            return MIN_DELAY;
        }
        if (delay > MAX_DELAY) {
            return MAX_DELAY;
        }
        return delay;
    }

    /**
     * The time delay for the device to be powered on
     *
     * @return the time delay
     */
    public int getStandbyCommandDelay() {
        return standbyCommandDelay;
    }

    /**
     * The time delay for the device switch inputs
     *
     * @return the time delay
     */
    public int getSourceSwitchDelay() {
        return sourceSwitchDelay;
    }

    /**
     * The time delay for the device to power off
     *
     * @return the time delay
     */
    public int getShutdownDelay() {
        return shutdownDelay;
    }

    @Override
    public String toString() {
        return "NeeoDeviceTiming [standbyCommandDelay=" + standbyCommandDelay + ", sourceSwitchDelay="
                + sourceSwitchDelay + ", shutdownDelay=" + shutdownDelay + "]";
    }
}
