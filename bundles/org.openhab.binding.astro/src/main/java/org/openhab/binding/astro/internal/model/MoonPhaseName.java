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
import org.openhab.binding.astro.internal.util.AstroConstants;

/**
 * All moon phases.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Gaël L'hopital - added age equivalence in days & cycleProgressPercentage
 */
@NonNullByDefault
public enum MoonPhaseName {
    NEW(0),
    WAXING_CRESCENT(0.125),
    FIRST_QUARTER(0.25),
    WAXING_GIBBOUS(0.375),
    FULL(0.5),
    WANING_GIBBOUS(0.625),
    THIRD_QUARTER(0.75), // also called last quarter
    WANING_CRESCENT(0.875);

    public final double cycleProgressPercentage;

    MoonPhaseName(double cycleProgressPercentage) {
        this.cycleProgressPercentage = cycleProgressPercentage;
    }

    public int getAgeDays() {
        return (int) ((AstroConstants.LUNAR_SYNODIC_MONTH_DAYS - 1) * cycleProgressPercentage + 1);
    }
}
