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

import static org.openhab.binding.astro.internal.util.AstroConstants.LUNAR_SYNODIC_MONTH_DAYS;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * All moon phases.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Gaël L'hopital - added age equivalence in days & cycleProgressPercentage
 */
@NonNullByDefault
public enum MoonPhaseName {
    NEW(0.0),
    WAXING_CRESCENT(0.125),
    FIRST_QUARTER(0.25),
    WAXING_GIBBOUS(0.375),
    FULL(0.5),
    WANING_GIBBOUS(0.625),
    THIRD_QUARTER(0.75), // also called last quarter
    WANING_CRESCENT(0.875);

    public final double cycleProgress;

    MoonPhaseName(double cycleProgress) {
        this.cycleProgress = cycleProgress;
    }

    public int getAgeDays() {
        return (int) ((LUNAR_SYNODIC_MONTH_DAYS - 1) * cycleProgress + 1);
    }

    public static MoonPhaseName fromAgePercent(double agePercent) {
        if (agePercent < 0.0 || agePercent > 1.0) {
            throw new IllegalArgumentException("agePercent must be in [0,1]");
        }

        if (agePercent == NEW.cycleProgress) {
            return NEW;
        } else if (agePercent < FIRST_QUARTER.cycleProgress) {
            return WAXING_CRESCENT;
        } else if (agePercent == FIRST_QUARTER.cycleProgress) {
            return FIRST_QUARTER;
        } else if (agePercent < FULL.cycleProgress) {
            return WAXING_GIBBOUS;
        } else if (agePercent == FULL.cycleProgress) {
            return FULL;
        } else if (agePercent < THIRD_QUARTER.cycleProgress) {
            return WANING_GIBBOUS;
        } else if (agePercent == THIRD_QUARTER.cycleProgress) {
            return THIRD_QUARTER;
        }
        return WANING_CRESCENT;
    }

    public static List<MoonPhaseName> remarkables() {
        return Arrays.stream(values()).sorted(Comparator.comparing(mpn -> mpn.cycleProgress)).toList();
    }
}
