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
 */
@NonNullByDefault
public enum MoonPhaseName {
    NEW(0),
    WAXING_CRESCENT(Double.NaN),
    FIRST_QUARTER(0.25),
    WAXING_GIBBOUS(Double.NaN),
    FULL(0.5),
    WANING_GIBBOUS(Double.NaN),
    THIRD_QUARTER(0.75), // also called last quarter
    WANING_CRESCENT(Double.NaN);

    public final double mode;

    MoonPhaseName(double mode) {
        this.mode = mode;
    }
}
