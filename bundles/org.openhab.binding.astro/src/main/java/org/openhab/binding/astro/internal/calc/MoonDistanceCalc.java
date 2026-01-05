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

import static org.openhab.binding.astro.internal.util.MathUtils.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.astro.internal.model.DistanceType;
import org.openhab.binding.astro.internal.model.MoonDistance;
import org.openhab.binding.astro.internal.util.AstroConstants;
import org.openhab.binding.astro.internal.util.DateTimeUtils;

/**
 * Moon Distance Calculator
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class MoonDistanceCalc {
    private static final double ANOMALISTIC_MONTH = 27.55454988;
    private static final double JDE_0 = 2451534.6698;
    private static final int[] KD = new int[] { 0, 2, 2, 0, 0, 0, 2, 2, 2, 2, 0, 1, 0, 2, 0, 0, 4, 0, 4, 2, 2, 1, 1, 2,
            2, 4, 2, 0, 2, 2, 1, 2, 0, 0, 2, 2, 2, 4, 0, 3, 2, 4, 0, 2, 2, 2, 4, 0, 4, 1, 2, 0, 1, 3, 4, 2, 0, 1, 2,
            2 };
    private static final int[] KM = new int[] { 0, 0, 0, 0, 1, 0, 0, -1, 0, -1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1,
            -1, 0, 0, 0, 1, 0, -1, 0, -2, 1, 2, -2, 0, 0, -1, 0, 0, 1, -1, 2, 2, 1, -1, 0, 0, -1, 0, 1, 0, 1, 0, 0, -1,
            2, 1, 0, 0 };
    private static final int[] KM1 = new int[] { 1, -1, 0, 2, 0, 0, -2, -1, 1, 0, -1, 0, 1, 0, 1, 1, -1, 3, -2, -1, 0,
            -1, 0, 1, 2, 0, -3, -2, -1, -2, 1, 0, 2, 0, -1, 1, 0, -1, 2, -1, 1, -2, -1, -1, -2, 0, 1, 4, 0, -2, 0, 2, 1,
            -2, -3, 2, 1, -1, 3, -1 };
    private static final int[] KF = new int[] { 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, -2, 2, -2, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, -2, 2, 0, 2, 0, 0, 0, 0, 0, 0, -2, 0, 0, 0, 0, -2, -2, 0, 0, 0, 0, 0, 0,
            0, -2 };
    private static final int[] KR = new int[] { -20905355, -3699111, -2955968, -569925, 48888, -3149, 246158, -152138,
            -170733, -204586, -129620, 108743, 104755, 10321, 0, 79661, -34782, -23210, -21636, 24208, 30824, -8379,
            -16675, -12831, -10445, -11650, 14403, -7003, 0, 10056, 6322, -9884, 5751, 0, -4950, 4130, 0, -3958, 0,
            3258, 2616, -1897, -2117, 2354, 0, 0, -1423, -1117, -1571, -1739, 0, -4421, 0, 0, 0, 0, 1165, 0, 0, 8752 };

    /**
     * Calculates the distance from the moon to earth in metres
     */
    public static MoonDistance calculate(double jd) {
        double t = DateTimeUtils.toJulianCenturies(jd);
        double t2 = t * t;
        double t3 = t2 * t;
        double t4 = t3 * t;
        double d = 297.8502042 + 445267.11151686 * t - .00163 * t2 + t3 / 545868 - t4 / 113065000;
        double m = AstroConstants.E05_0 + 35999.0502909 * t - .0001536 * t2 + t3 / 24490000;
        double m1 = 134.9634114 + 477198.8676313 * t + .008997 * t2 + t3 / 69699 - t4 / 14712000;
        double f = 93.27209929999999 + 483202.0175273 * t - .0034029 * t2 - t3 / 3526000 + t4 / 863310000;
        return new MoonDistance(jd, 385000560 + getCoefficient(d, m, m1, f));
    }

    public static MoonDistance get(DistanceType type, double julianDate) {
        if (DistanceType.CURRENT.equals(type)) {
            throw new IllegalArgumentException("MoonDistanceCalc.get only supports APOGEE and PERIGEE");
        }

        double moment = getApogeePerigee(type, julianDate);
        return calculate(moment);
    }

    private static double getCoefficient(double d, double m, double m1, double f) {
        double sr = 0;
        for (int t = 0; t < 60; t++) {
            sr += KR[t] * cosDeg(KD[t] * d + KM[t] * m + KM1[t] * m1 + KF[t] * f);
        }
        return sr;
    }

    /**
     * Calculates the date, where the moon is furthest away from the earth.
     */
    private static double getApogeePerigee(DistanceType type, double julianDate) {
        double k = Math.floor((julianDate - JDE_0) / ANOMALISTIC_MONTH) + (type.equals(DistanceType.APOGEE) ? 0.5 : 0)
                - 1;
        double jd = 0;
        do {
            double t = k / 1325.55;
            double t2 = t * t;
            double t3 = t2 * t;
            double t4 = t3 * t;
            double d = 171.9179 + 335.9106046 * k - .010025 * t2 - .00001156 * t3 + .000000055 * t4;
            double m = 347.3477 + 27.1577721 * k - .0008323 * t2 - .000001 * t3;
            double f = 316.6109 + 364.5287911 * k - .0125131 * t2 - .0000148 * t3;
            jd = JDE_0 + ANOMALISTIC_MONTH * k - .0006886 * t2 - .000001098 * t3 + .0000000052 * t2;
            if (DistanceType.APOGEE.equals(type)) {
                jd += .4392 * sinDeg(2 * d) + .0684 * sinDeg(4 * d) + (.0456 - .00011 * t) * sinDeg(m)
                        + (.0426 - .00011 * t) * sinDeg(2 * d - m) + .0212 * sinDeg(2 * f);
                jd += -.0189 * sinDeg(d) + .0144 * sinDeg(6 * d) + .0113 * sinDeg(4 * d - m)
                        + .0047 * sinDeg(2 * d + 2 * f) + .0036 * sinDeg(d + m) + .0035 * sinDeg(8 * d)
                        + .0034 * sinDeg(6 * d - m) - .0034 * sinDeg(2 * d - 2 * f) + .0022 * sinDeg(2 * d - 2 * m)
                        - .0017 * sinDeg(3 * d);
                jd += .0013 * sinDeg(4 * d + 2 * f) + .0011 * sinDeg(8 * d - m) + .001 * sinDeg(4 * d - 2 * m)
                        + .0009 * sinDeg(10 * d) + .0007 * sinDeg(3 * d + m) + .0006 * sinDeg(2 * m)
                        + .0005 * sinDeg(2 * d + m) + .0005 * sinDeg(2 * d + 2 * m) + .0004 * sinDeg(6 * d + 2 * f);
                jd += .0004 * sinDeg(6 * d - 2 * m) + .0004 * sinDeg(10 * d - m) - .0004 * sinDeg(5 * d)
                        - .0004 * sinDeg(4 * d - 2 * f) + .0003 * sinDeg(2 * f + m) + .0003 * sinDeg(12 * d)
                        + .0003 * sinDeg(2 * d + 2 * f - m) - .0003 * sinDeg(d - m);
            } else if (DistanceType.PERIGEE.equals(type)) {
                jd += -1.6769 * sinDeg(2 * d) + .4589 * sinDeg(4 * d) - .1856 * sinDeg(6 * d) + .0883 * sinDeg(8 * d);
                jd += -(.0773 + .00019 * t) * sinDeg(2 * d - m) + (.0502 - .00013 * t) * sinDeg(m)
                        - .046 * sinDeg(10 * d) + (.0422 - .00011 * t) * sinDeg(4 * d - m) - .0256 * sinDeg(6 * d - m)
                        + .0253 * sinDeg(12 * d) + .0237 * sinDeg(d);
                jd += .0162 * sinDeg(8 * d - m) - .0145 * sinDeg(14 * d) + .0129 * sinDeg(2 * f) - .0112 * sinDeg(3 * d)
                        - .0104 * sinDeg(10 * d - m) + .0086 * sinDeg(16 * d) + .0069 * sinDeg(12 * d - m)
                        + .0066 * sinDeg(5 * d) - .0053 * sinDeg(2 * d + 2 * f);
                jd += -.0052 * sinDeg(18 * d) - .0046 * sinDeg(14 * d - m) - .0041 * sinDeg(7 * d)
                        + .004 * sinDeg(2 * d + m) + .0032 * sinDeg(20 * d) - .0032 * sinDeg(d + m)
                        + .0031 * sinDeg(16 * d - m);
                jd += -.0029 * sinDeg(4 * d + m) - .0027 * sinDeg(2 * d - 2 * m) + .0024 * sinDeg(4 * d - 2 * m)
                        - .0021 * sinDeg(6 * d - 2 * m) - .0021 * sinDeg(22 * d) - .0021 * sinDeg(18 * d - m);
                jd += .0019 * sinDeg(6 * d + m) - .0018 * sinDeg(11 * d) - .0014 * sinDeg(8 * d + m)
                        - .0014 * sinDeg(4 * d - 2 * f) - .0014 * sinDeg(6 * d - 2 * f) + .0014 * sinDeg(3 * d + m)
                        - .0014 * sinDeg(5 * d + m) + .0013 * sinDeg(13 * d);
                jd += .0013 * sinDeg(20 * d - m) + .0011 * sinDeg(3 * d + 2 * m) - .0011 * sinDeg(4 * d + 2 * f - 2 * m)
                        - .001 * sinDeg(d + 2 * m) - .0009 * sinDeg(22 * d - m) - .0008 * sinDeg(4 * f)
                        + .0008 * sinDeg(6 * d - 2 * f) + .0008 * sinDeg(2 * d - 2 * f + m);
                jd += .0007 * sinDeg(2 * m) + .0007 * sinDeg(2 * f - m) + .0007 * sinDeg(2 * d + 4 * f)
                        - .0006 * sinDeg(2 * f - 2 * m) - .0006 * sinDeg(2 * d - 2 * f + 2 * m) + .0006 * sinDeg(24 * d)
                        + .0005 * sinDeg(4 * d - 4 * f) + .0005 * sinDeg(2 * d + 2 * m) - .0004 * sinDeg(d - m)
                        + .0027 * sinDeg(9 * d) + .0027 * sinDeg(4 * d + 2 * f);
            }
            k += 1;
        } while (jd < julianDate);
        return jd;
    }
}
