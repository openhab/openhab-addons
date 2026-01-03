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
package org.openhab.binding.astro.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * All moon phases.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Gaël L'hopital - added age equivalence & mode
 */
@NonNullByDefault
public enum MoonPhaseName {
    NEW(1, 0),
    WAXING_CRESCENT(4, Double.NaN),
    FIRST_QUARTER(8, 0.25),
    WAXING_GIBBOUS(11, Double.NaN),
    FULL(15, 0.5),
    WANING_GIBBOUS(18, Double.NaN),
    THIRD_QUARTER(22, 0.75), // also called last quarter
    WANING_CRESCENT(26, Double.NaN);

    public final double mode;
    public final int ageDays;

    MoonPhaseName(int ageDays, double mode) {
        this.mode = mode;
        this.ageDays = ageDays;
    }
}
