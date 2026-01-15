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
package org.openhab.binding.astro.internal.calc;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.astro.internal.util.DateTimeUtils;

/**
 * Calculates the eclipses for the astro object
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Gaël L'hopital - Extracted from MoonCalc
 */
@NonNullByDefault
public class AstroCalc {
    protected static double varF(double k, double t) {
        return 160.7108 + 390.67050274 * k - .0016341 * t * t - .00000227 * t * t * t + .000000011 * t * t * t * t;
    }

    protected static double varO(double k, double t) {
        return 124.7746 - 1.5637558 * k + .0020691 * t * t + .00000215 * t * t * t;
    }

    protected static double varE(double t) {
        return 1 - .002516 * t - .0000074 * t * t;
    }

    protected static double varM1(double k, double t) {
        return 201.5643 + 385.81693528 * k + .1017438 * t * t + .00001239 * t * t * t - .000000058 * t * t * t * t;
    }

    protected static double varM(double k, double t) {
        return 2.5534 + 29.10535669 * k - .0000218 * t * t - .00000011 * t * t * t;
    }

    protected static double varJde(double k, double t) {
        return 2451550.09765 + 29.530588853 * k + .0001337 * t * t - .00000015 * t * t * t
                + .00000000073 * t * t * t * t;
    }

    protected static double varK(double jd, double tz) {
        return ((jd + tz - DateTimeUtils.JD_2000_01_01) / 365.0) * 12.3685;
    }
}
