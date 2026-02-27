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
 * Represents the fundamental lunar arguments used in analytical
 * Moon position and phase calculations.
 * <p>
 * These angles are defined according to the classical formulation
 * described in Jean Meeus, <i>Astronomical Algorithms</i>, and are
 * functions of the time expressed in Julian centuries since J2000.0.
 * </p>
 *
 * <p>
 * All angular values are expressed in <strong>radians</strong>.
 * </p>
 *
 * <ul>
 * <li><b>D</b> – Mean elongation of the Moon from the Sun</li>
 * <li><b>M</b> – Mean anomaly of the Sun (Earth)</li>
 * <li><b>M′</b> – Mean anomaly of the Moon</li>
 * <li><b>F</b> – Argument of latitude of the Moon</li>
 * </ul>
 *
 *
 * <p>
 * This class is immutable and serves as a shared intermediate
 * representation for lunar calculations.
 * </p>
 *
 * @see <a href="https://en.wikipedia.org/wiki/Elongation_(astronomy)">Elongation (astronomy)</a>
 * @see Jean Meeus, Astronomical Algorithms, Chapter 47
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class LunarArguments {
    public final double d;
    public final double m;
    public final double m1;
    public final double f;

    public LunarArguments(double jd) {
        double t = DateTimeUtils.toJulianCenturies(jd);
        double t2 = t * t;
        double t3 = t2 * t;
        double t4 = t3 * t;

        this.d = Math.toRadians(297.8502042 + 445267.11151686 * t - 0.00163 * t2 + t3 / 545868 - t4 / 113065000);
        this.m = Math.toRadians(AstroConstants.E05_0 + 35999.0502909 * t - 0.0001536 * t2 + t3 / 24490000);
        this.m1 = Math.toRadians(134.9634114 + 477198.8676313 * t + 0.008997 * t2 + t3 / 69699 - t4 / 14712000);
        this.f = Math.toRadians(93.2720993 + 483202.0175273 * t - 0.0034029 * t2 - t3 / 3526000 + t4 / 863310000);
    }
}
