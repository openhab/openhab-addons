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

import static org.openhab.binding.astro.internal.util.MathUtils.sinDeg;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.astro.internal.model.EclipseKind;

/**
 * Adjust the eclipses calculations for the moon
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Gaël L'hopital - Extracted from MoonCalc
 */
@NonNullByDefault
public class MoonEclipseCalc extends EclipseCalc {

    @Override
    protected double astroAdjust(EclipseKind eclipse, double e, double m, double m1, double g, double u, double jd) {
        if ((1.0248 - u - Math.abs(g)) / .545 <= 0) {
            return 0; // no moon eclipse
        }
        double u2 = (.4678 - u) * (.4678 - u) - g * g;
        double ug = (1.0128 - u - Math.abs(g)) / .545;
        if (EclipseKind.PARTIAL.equals(eclipse) && ug > 0 && u2 > 0) {
            return 0; // no partial moon eclipse
        }
        if (EclipseKind.TOTAL.equals(eclipse) && (ug <= 0 != u2 <= 0)) {
            return 0; // no total moon eclipse
        }
        return jd + -.4065 * sinDeg(m1) + .1727 * e * sinDeg(m);
    }

    @Override
    protected double getJDAjust() {
        return 0.5;
    }

    @Override
    protected Set<EclipseKind> validEclipses() {
        return Set.of(EclipseKind.PARTIAL, EclipseKind.TOTAL);
    }
}
