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
package org.openhab.binding.astro.internal.calc.moon;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.astro.internal.util.AstroConstants;
import org.openhab.binding.astro.internal.util.DateTimeUtils;

/**
 * Fundamental arguments of a lunar lunation as defined by
 * Jean Meeus, Astronomical Algorithms.
 * <p>
 * These parameters are common to the computation of:
 * </p>
 * <ul>
 * <li>lunar phases</li>
 * <li>solar eclipses</li>
 * <li>lunar eclipses</li>
 * </ul>
 *
 * <p>
 * All angular values are expressed in <strong>degrees</strong>.
 * </p>
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class LunationArguments {
    public final double kMod;
    public final double t; // Time in Julian centuries since J2000.0.
    public final double t2;
    public final double m; /// Mean anomaly of the Sun (degrees).
    public final double m1; // Mean anomaly of the Moon (degrees).
    public final double f; // Argument of latitude of the Moon (degrees).
    public final double o; // Longitude of the ascending node of the Moon's orbit (degrees).
    public final double e; // Earth orbital eccentricity correction factor.
    public final double jde; // Mean Julian Ephemeris Day of the lunation.

    public LunationArguments(double jd, double adjust) {
        this.kMod = Math.floor(jd) + adjust;
        this.t = kMod / DateTimeUtils.JULIAN_CENTURY_DAYS * AstroConstants.LUNAR_SYNODIC_MONTH_DAYS;
        this.t2 = t * t;
        double t3 = t2 * t;
        double t4 = t3 * t;

        this.m = 2.5534 + 29.10535669 * kMod - 0.0000218 * t2 - 0.00000011 * t3;
        this.m1 = 201.5643 + 385.81693528 * kMod + 0.1017438 * t2 + 0.00001239 * t3 - 0.000000058 * t4;
        this.f = 160.7108 + 390.67050274 * kMod - 0.0016341 * t2 - 0.00000227 * t3 + 0.000000011 * t4;
        this.o = 124.7746 - 1.56375588 * kMod + 0.0020691 * t2 + 0.00000215 * t3;
        this.e = 1 - 0.002516 * t - 0.0000074 * t2;
        this.jde = 2451550.09765 + 29.530588853 * kMod + 0.0001337 * t2 - 0.00000015 * t3 + 0.00000000073 * t4;
    }

    public static double varK(double jd, double tz) {
        return (jd + tz - DateTimeUtils.JD_2000_01_01) / AstroConstants.LUNAR_SYNODIC_MONTH_DAYS;
    }
}
