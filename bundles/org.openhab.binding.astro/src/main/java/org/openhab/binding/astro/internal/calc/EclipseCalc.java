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

import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.astro.internal.model.EclipseKind;
import org.openhab.binding.astro.internal.model.Position;
import org.openhab.binding.astro.internal.util.DateTimeUtils;

/**
 * Calculates the eclipses for the astro object
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Gaël L'hopital - Extracted from MoonCalc
 */
@NonNullByDefault
public abstract class EclipseCalc extends AstroCalc {
    public record Eclipse(EclipseKind kind, double when) {
        public LocalizedEclipse withPosition(Position position) {
            return new LocalizedEclipse(this, position.getElevationAsDouble());
        }
    }

    public record LocalizedEclipse(Eclipse eclipse, double elevation) {
        public Instant when() {
            return DateTimeUtils.jdToInstant(eclipse.when);
        }

        public EclipseKind kind() {
            return eclipse.kind;
        }

        public boolean matches(EclipseKind otherKind) {
            return eclipse.kind.equals(otherKind);
        }
    }

    public List<Eclipse> getNextEclipses(double startJd) {
        return validEclipses().stream().map(ek -> new Eclipse(ek, calculate(startJd, ek))).toList();
    }

    public double calculate(double midnightJd, EclipseKind eclipse) {
        double tz = 0;
        double eclipseJd = 0;
        do {
            double k = varK(midnightJd, tz);
            tz += 1;
            eclipseJd = getEclipse(Math.floor(k) + getJDAjust(), eclipse);
        } while (eclipseJd <= midnightJd);
        return eclipseJd;
    }

    protected abstract double getJDAjust();

    protected abstract double astroAdjust(EclipseKind ek, double e, double m, double m1, double g, double u, double jd);

    protected abstract Set<EclipseKind> validEclipses();

    /**
     * Calculates the eclipse.
     */
    protected double getEclipse(double kMod, EclipseKind eclipse) {
        double t = kMod / 1236.85;
        double f = varF(kMod, t);

        if (sinDeg(Math.abs(f)) > .36) {
            return 0;
        }

        double o = varO(kMod, t);
        double f1 = f - .02665 * sinDeg(o);
        double a1 = 299.77 + .107408 * kMod - .009173 * t * t;
        double e = varE(t);
        double m = varM(kMod, t);
        double m1 = varM1(kMod, t);
        double p = .207 * e * sinDeg(m) + .0024 * e * sinDeg(2 * m) - .0392 * sinDeg(m1) + .0116 * sinDeg(2 * m1)
                - .0073 * e * sinDeg(m1 + m) + .0067 * e * sinDeg(m1 - m) + .0118 * sinDeg(2 * f1);
        double q = 5.2207 - .0048 * e * cosDeg(m) + .002 * e * cosDeg(2 * m) - .3299 * cosDeg(m1)
                - .006 * e * cosDeg(m1 + m) + .0041 * e * cosDeg(m1 - m);
        double g = (p * cosDeg(f1) + q * sinDeg(f1)) * (1 - .0048 * cosDeg(Math.abs(f1)));
        double u = .0059 + .0046 * e * cosDeg(m) - .0182 * cosDeg(m1) + .0004 * cosDeg(2 * m1) - .0005 * cosDeg(m + m1);
        double jd = varJde(kMod, t);
        jd += .0161 * sinDeg(2 * m1) - .0097 * sinDeg(2 * f1) + .0073 * e * sinDeg(m1 - m) - .005 * e * sinDeg(m1 + m)
                - .0023 * sinDeg(m1 - 2 * f1) + .0021 * e * sinDeg(2 * m);
        jd += .0012 * sinDeg(m1 + 2 * f1) + .0006 * e * sinDeg(2 * m1 + m) - .0004 * sinDeg(3 * m1)
                - .0003 * e * sinDeg(m + 2 * f1) + .0003 * sinDeg(a1) - .0002 * e * sinDeg(m - 2 * f1)
                - .0002 * e * sinDeg(2 * m1 - m) - .0002 * sinDeg(o);
        return astroAdjust(eclipse, e, m, m1, g, u, jd);
    }
}
