/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A boundary-wire sensor measurement returned by the Robonect API.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public class Sensor {
    private @Nullable String description;
    private @Nullable Integer quality;
    private @Nullable Integer a;
    private @Nullable Integer n;
    private @Nullable Integer f;
    private @Nullable Integer g1;
    private @Nullable Integer g2;

    /**
     * Returns the sensor label.
     *
     * @return the sensor label, or {@code null} if not reported
     */
    public @Nullable String getDescription() {
        return description;
    }

    /**
     * Returns the signal quality in percent.
     *
     * @return the signal quality in percent, or {@code null} if not reported
     */
    public @Nullable Integer getQuality() {
        return quality;
    }

    /**
     * Returns the A wire-sensor measurement.
     *
     * @return the A wire-sensor measurement, or {@code null} if not reported
     */
    public @Nullable Integer getA() {
        return a;
    }

    /**
     * Returns the N wire-sensor measurement.
     *
     * @return the N wire-sensor measurement, or {@code null} if not reported
     */
    public @Nullable Integer getN() {
        return n;
    }

    /**
     * Returns the F wire-sensor measurement.
     *
     * @return the F wire-sensor measurement, or {@code null} if not reported
     */
    public @Nullable Integer getF() {
        return f;
    }

    /**
     * Returns the G1 wire-sensor measurement.
     *
     * @return the G1 wire-sensor measurement, or {@code null} if not reported
     */
    public @Nullable Integer getG1() {
        return g1;
    }

    /**
     * Returns the G2 wire-sensor measurement.
     *
     * @return the G2 wire-sensor measurement, or {@code null} if not reported
     */
    public @Nullable Integer getG2() {
        return g2;
    }
}
