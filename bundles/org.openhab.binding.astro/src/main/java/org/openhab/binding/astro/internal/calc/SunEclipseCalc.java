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
 * Adjust the eclipses calculations for the sun
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Gaël L'hopital - Extracted from MoonCalc
 */
@NonNullByDefault
public class SunEclipseCalc extends EclipseCalc {

    @Override
    protected double astroAdjust(EclipseKind eclipse, double e, double m, double m1, double g, double u, double jd) {
        if (Math.abs(g) > 1.5433 + u) {
            return 0; // no sun eclipse
        }
        if (!EclipseKind.PARTIAL.equals(eclipse)) {
            if ((g < -.9972 || g > .9972) || (Math.abs(g) < .9972 && Math.abs(g) > .9972 + Math.abs(u))) {
                return 0; // no ring or total sun eclipse
            }
            double ringTest = u > .0047 || u >= .00464 * Math.sqrt(1 - g * g) ? 1 : 0;
            if (ringTest == 1 && EclipseKind.TOTAL.equals(eclipse)) {
                return 0;
            }
            if (ringTest == 0 && EclipseKind.RING.equals(eclipse)) {
                return 0;
            }
        } else if ((g >= -.9972 && g <= .9972) || (Math.abs(g) >= .9972 && Math.abs(g) < .9972 + Math.abs(u))) {
            return 0; // no partial sun eclipse
        }
        return jd + -.4075 * sinDeg(m1) + .1721 * e * sinDeg(m);
    }

    @Override
    protected double getJDAjust() {
        return 0;
    }

    @Override
    protected Set<EclipseKind> validEclipses() {
        return Set.of(EclipseKind.PARTIAL, EclipseKind.TOTAL, EclipseKind.RING);
    }
}
