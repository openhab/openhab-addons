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
import org.openhab.binding.astro.internal.calc.moon.LunarArguments;
import org.openhab.binding.astro.internal.calc.moon.LunationArguments;
import org.openhab.binding.astro.internal.model.MoonPhase;
import org.openhab.binding.astro.internal.model.MoonPhaseSet;

/**
 * Moon Phase Calculator
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class MoonPhaseCalc {
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
        LunarArguments la = new LunarArguments(jd);
        double i = Math.PI - la.d - Math.toRadians(6.289) * Math.sin(la.m1) + Math.toRadians(2.1) * Math.sin(la.m)
                - Math.toRadians(1.274) * Math.sin(2 * la.d - la.m1) - Math.toRadians(0.658) * Math.sin(2 * la.d)
                - Math.toRadians(0.241) * Math.sin(2 * la.m1) - Math.toRadians(0.110) * Math.sin(la.d);

        return (1 + Math.cos(i)) / 2;
    }

    /**
     * Searches the next moon phase in a given direction
     */
    private static double getPhase(double jd, MoonPhase phase, boolean forward) {
        double tz = 0;
        double phaseJd = 0;
        do {
            double k = LunationArguments.varK(jd, tz);
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
        LunationArguments la = new LunationArguments(kMod);
        double t = kMod / 1236.85;
        double jd = la.jde;
        switch (phase) {
            case NEW:
                jd += -.4072 * sinDeg(la.m1) + .17241 * la.e * sinDeg(la.m) + .01608 * sinDeg(2 * la.m1)
                        + .01039 * sinDeg(2 * la.f) + .00739 * la.e * sinDeg(la.m1 - la.m)
                        - .00514 * la.e * sinDeg(la.m1 + la.m) + .00208 * la.e * la.e * sinDeg(2 * la.m)
                        - .00111 * sinDeg(la.m1 - 2 * la.f) - .00057 * sinDeg(la.m1 + 2 * la.f);
                jd += .00056 * la.e * sinDeg(2 * la.m1 + la.m) - .00042 * sinDeg(3 * la.m1)
                        + .00042 * la.e * sinDeg(la.m + 2 * la.f) + .00038 * la.e * sinDeg(la.m - 2 * la.f)
                        - .00024 * la.e * sinDeg(2 * la.m1 - la.m) - .00017 * sinDeg(la.o)
                        - .00007 * sinDeg(la.m1 + 2 * la.m) + .00004 * sinDeg(2 * la.m1 - 2 * la.f);
                jd += .00004 * sinDeg(3 * la.m) + .00003 * sinDeg(la.m1 + la.m - 2 * la.f)
                        + .00003 * sinDeg(2 * la.m1 + 2 * la.f) - .00003 * sinDeg(la.m1 + la.m + 2 * la.f)
                        + .00003 * sinDeg(la.m1 - la.m + 2 * la.f) - .00002 * sinDeg(la.m1 - la.m - 2 * la.f)
                        - .00002 * sinDeg(3 * la.m1 + la.m);
                jd += .00002 * sinDeg(4 * la.m1);
                break;
            case FULL:
                jd += -.40614 * sinDeg(la.m1) + .17302 * la.e * sinDeg(la.m) + .01614 * sinDeg(2 * la.m1)
                        + .01043 * sinDeg(2 * la.f) + .00734 * la.e * sinDeg(la.m1 - la.m)
                        - .00515 * la.e * sinDeg(la.m1 + la.m) + .00209 * la.e * la.e * sinDeg(2 * la.m)
                        - .00111 * sinDeg(la.m1 - 2 * la.f) - .00057 * sinDeg(la.m1 + 2 * la.f);
                jd += .00056 * la.e * sinDeg(2 * la.m1 + la.m) - .00042 * sinDeg(3 * la.m1)
                        + .00042 * la.e * sinDeg(la.m + 2 * la.f) + .00038 * la.e * sinDeg(la.m - 2 * la.f)
                        - .00024 * la.e * sinDeg(2 * la.m1 - la.m) - .00017 * sinDeg(la.o)
                        - .00007 * sinDeg(la.m1 + 2 * la.m) + .00004 * sinDeg(2 * la.m1 - 2 * la.f);
                jd += .00004 * sinDeg(3 * la.m) + .00003 * sinDeg(la.m1 + la.m - 2 * la.f)
                        + .00003 * sinDeg(2 * la.m1 + 2 * la.f) - .00003 * sinDeg(la.m1 + la.m + 2 * la.f)
                        + .00003 * sinDeg(la.m1 - la.m + 2 * la.f) - .00002 * sinDeg(la.m1 - la.m - 2 * la.f)
                        - .00002 * sinDeg(3 * la.m1 + la.m);
                jd += .00002 * sinDeg(4 * la.m1);
                break;
            default:
                jd += -.62801 * sinDeg(la.m1) + .17172 * la.e * sinDeg(la.m) - .01183 * la.e * sinDeg(la.m1 + la.m)
                        + .00862 * sinDeg(2 * la.m1) + .00804 * sinDeg(2 * la.f) + .00454 * la.e * sinDeg(la.m1 - la.m)
                        + .00204 * la.e * la.e * sinDeg(2 * la.m) - .0018 * sinDeg(la.m1 - 2 * la.f)
                        - .0007 * sinDeg(la.m1 + 2 * la.f);
                jd += -.0004 * sinDeg(3 * la.m1) - .00034 * la.e * sinDeg(2 * la.m1 - la.m)
                        + .00032 * la.e * sinDeg(la.m + 2 * la.f) + .00032 * la.e * sinDeg(la.m - 2 * la.f)
                        - .00028 * la.e * la.e * sinDeg(la.m1 + 2 * la.m) + .00027 * la.e * sinDeg(2 * la.m1 + la.m)
                        - .00017 * sinDeg(la.o);
                jd += -.00005 * sinDeg(la.m1 - la.m - 2 * la.f) + .00004 * sinDeg(2 * la.m1 + 2 * la.f)
                        - .00004 * sinDeg(la.m1 + la.m + 2 * la.f) + .00004 * sinDeg(la.m1 - 2 * la.m)
                        + .00003 * sinDeg(la.m1 + la.m - 2 * la.f) + .00003 * sinDeg(3 * la.m)
                        + .00002 * sinDeg(2 * la.m1 - 2 * la.f);
                jd += .00002 * sinDeg(la.m1 - la.m + 2 * la.f) - .00002 * sinDeg(3 * la.m1 + la.m);
                double w = .00306 - .00038 * la.e * cosDeg(la.m) + .00026 * cosDeg(la.m1)
                        - .00002 * cosDeg(la.m1 - la.m) + .00002 * cosDeg(la.m1 + la.m) + .00002 * cosDeg(2 * la.f);
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
