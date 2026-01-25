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

import java.time.InstantSource;
import java.time.ZoneId;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.astro.internal.model.MoonPhase;
import org.openhab.binding.astro.internal.model.MoonPhaseSet;
import org.openhab.binding.astro.internal.util.AstroConstants;
import org.openhab.binding.astro.internal.util.DateTimeUtils;

/**
 * Moon Phase Calculator
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class MoonPhaseCalc extends AstroCalc {
    public static MoonPhaseSet calculate(InstantSource instantSource, double julianDate, MoonPhaseSet previousMP,
            ZoneId zone) {
        final MoonPhaseSet result;

        if (previousMP.needsRecalc(julianDate)) {
            double julianDateMidnight = Math.floor(julianDate + 0.5) - 0.5;
            double parentNewMoon = getPhase(julianDateMidnight, MoonPhase.NEW, false);

            Map<MoonPhase, Double> comingPhases = MoonPhase.remarkables().stream()
                    .collect(Collectors.toMap(phase -> phase, phase -> getPhase(julianDateMidnight, phase, true)));

            result = new MoonPhaseSet(instantSource, parentNewMoon, comingPhases);
        } else {
            result = previousMP;
        }

        result.setIllumination(getIllumination(julianDate));
        result.updateName(julianDate, zone);

        return result;
    }

    /**
     * Calculates the illumination.
     */
    private static double getIllumination(double jd) {
        double t = DateTimeUtils.toJulianCenturies(jd);
        double t2 = t * t;
        double t3 = t2 * t;
        double t4 = t3 * t;
        double d = 297.8502042 + 445267.11151686 * t - .00163 * t2 + t3 / 545868 - t4 / 113065000;
        double m = AstroConstants.E05_0 + 35999.0502909 * t - .0001536 * t2 + t3 / 24490000;
        double m1 = 134.9634114 + 477198.8676313 * t + .008997 * t2 + t3 / 69699 - t4 / 14712000;
        double i = 180 - d - 6.289 * sinDeg(m1) + 2.1 * sinDeg(m) - 1.274 * sinDeg(2 * d - m1) - .658 * sinDeg(2 * d)
                - .241 * sinDeg(2 * m1) - .110 * sinDeg(d);
        return (1 + cosDeg(i)) / 2;
    }

    /**
     * Searches the next moon phase in a given direction
     */
    private static double getPhase(double jd, MoonPhase phase, boolean forward) {
        double tz = 0;
        double phaseJd = 0;
        do {
            double k = varK(jd, tz);
            tz += forward ? 1 : -1;
            phaseJd = calcMoonPhase(k, phase);
        } while (forward ? phaseJd <= jd : phaseJd > jd);
        return phaseJd;
    }

    /**
     * Calculates the moon phase.
     */
    private static double calcMoonPhase(double k, MoonPhase phase) {
        double kMod = Math.floor(k) + phase.cycleProgress;
        double t = kMod / 1236.85;
        double e = varE(t);
        double m = varM(kMod, t);
        double m1 = varM1(kMod, t);
        double f = varF(kMod, t);
        double o = varO(kMod, t);
        double jd = varJde(kMod, t);
        switch (phase) {
            case NEW:
                jd += -.4072 * sinDeg(m1) + .17241 * e * sinDeg(m) + .01608 * sinDeg(2 * m1) + .01039 * sinDeg(2 * f)
                        + .00739 * e * sinDeg(m1 - m) - .00514 * e * sinDeg(m1 + m) + .00208 * e * e * sinDeg(2 * m)
                        - .00111 * sinDeg(m1 - 2 * f) - .00057 * sinDeg(m1 + 2 * f);
                jd += .00056 * e * sinDeg(2 * m1 + m) - .00042 * sinDeg(3 * m1) + .00042 * e * sinDeg(m + 2 * f)
                        + .00038 * e * sinDeg(m - 2 * f) - .00024 * e * sinDeg(2 * m1 - m) - .00017 * sinDeg(o)
                        - .00007 * sinDeg(m1 + 2 * m) + .00004 * sinDeg(2 * m1 - 2 * f);
                jd += .00004 * sinDeg(3 * m) + .00003 * sinDeg(m1 + m - 2 * f) + .00003 * sinDeg(2 * m1 + 2 * f)
                        - .00003 * sinDeg(m1 + m + 2 * f) + .00003 * sinDeg(m1 - m + 2 * f)
                        - .00002 * sinDeg(m1 - m - 2 * f) - .00002 * sinDeg(3 * m1 + m);
                jd += .00002 * sinDeg(4 * m1);
                break;
            case FULL:
                jd += -.40614 * sinDeg(m1) + .17302 * e * sinDeg(m) + .01614 * sinDeg(2 * m1) + .01043 * sinDeg(2 * f)
                        + .00734 * e * sinDeg(m1 - m) - .00515 * e * sinDeg(m1 + m) + .00209 * e * e * sinDeg(2 * m)
                        - .00111 * sinDeg(m1 - 2 * f) - .00057 * sinDeg(m1 + 2 * f);
                jd += .00056 * e * sinDeg(2 * m1 + m) - .00042 * sinDeg(3 * m1) + .00042 * e * sinDeg(m + 2 * f)
                        + .00038 * e * sinDeg(m - 2 * f) - .00024 * e * sinDeg(2 * m1 - m) - .00017 * sinDeg(o)
                        - .00007 * sinDeg(m1 + 2 * m) + .00004 * sinDeg(2 * m1 - 2 * f);
                jd += .00004 * sinDeg(3 * m) + .00003 * sinDeg(m1 + m - 2 * f) + .00003 * sinDeg(2 * m1 + 2 * f)
                        - .00003 * sinDeg(m1 + m + 2 * f) + .00003 * sinDeg(m1 - m + 2 * f)
                        - .00002 * sinDeg(m1 - m - 2 * f) - .00002 * sinDeg(3 * m1 + m);
                jd += .00002 * sinDeg(4 * m1);
                break;
            default:
                jd += -.62801 * sinDeg(m1) + .17172 * e * sinDeg(m) - .01183 * e * sinDeg(m1 + m)
                        + .00862 * sinDeg(2 * m1) + .00804 * sinDeg(2 * f) + .00454 * e * sinDeg(m1 - m)
                        + .00204 * e * e * sinDeg(2 * m) - .0018 * sinDeg(m1 - 2 * f) - .0007 * sinDeg(m1 + 2 * f);
                jd += -.0004 * sinDeg(3 * m1) - .00034 * e * sinDeg(2 * m1 - m) + .00032 * e * sinDeg(m + 2 * f)
                        + .00032 * e * sinDeg(m - 2 * f) - .00028 * e * e * sinDeg(m1 + 2 * m)
                        + .00027 * e * sinDeg(2 * m1 + m) - .00017 * sinDeg(o);
                jd += -.00005 * sinDeg(m1 - m - 2 * f) + .00004 * sinDeg(2 * m1 + 2 * f)
                        - .00004 * sinDeg(m1 + m + 2 * f) + .00004 * sinDeg(m1 - 2 * m)
                        + .00003 * sinDeg(m1 + m - 2 * f) + .00003 * sinDeg(3 * m) + .00002 * sinDeg(2 * m1 - 2 * f);
                jd += .00002 * sinDeg(m1 - m + 2 * f) - .00002 * sinDeg(3 * m1 + m);
                double w = .00306 - .00038 * e * cosDeg(m) + .00026 * cosDeg(m1) - .00002 * cosDeg(m1 - m)
                        + .00002 * cosDeg(m1 + m) + .00002 * cosDeg(2 * f);
                jd += MoonPhase.FIRST_QUARTER.equals(phase) ? w : -w;
        }
        return moonCorrection(jd, t, kMod);
    }

    private static double moonCorrection(double jd, double t, double k) {
        double ret = jd;
        ret += .000325 * sinDeg(299.77 + .107408 * k - .009173 * t * t) + .000165 * sinDeg(251.88 + .016321 * k)
                + .000164 * sinDeg(251.83 + 26.651886 * k) + .000126 * sinDeg(349.42 + 36.412478 * k)
                + .00011 * sinDeg(84.66 + 18.206239 * k);
        ret += .000062 * sinDeg(141.74 + 53.303771 * k) + .00006 * sinDeg(207.14 + 2.453732 * k)
                + .000056 * sinDeg(154.84 + 7.30686 * k) + .000047 * sinDeg(34.52 + 27.261239 * k)
                + .000042 * sinDeg(207.19 + .121824 * k) + .00004 * sinDeg(291.34 + 1.844379 * k);
        ret += .000037 * sinDeg(161.72 + 24.198154 * k) + .000035 * sinDeg(239.56 + 25.513099 * k)
                + .000023 * sinDeg(331.55 + 3.592518 * k);
        return ret;
    }
}
