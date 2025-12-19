/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.astro.internal.util;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Common used DateTime functions.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class MathUtils {
    public static final double TWO_PI = 2 * Math.PI;

    /** Constructor */
    private MathUtils() {
        throw new IllegalAccessError("Non-instantiable");
    }

    public static double mod2Pi(double x) {
        return mod(x, TWO_PI);
    }

    public static double mod(double a, double b) {
        return a - Math.floor(a / b) * b;
    }

    /**
     * Cosinus of a degree value.
     */
    public static double cosDeg(double deg) {
        return Math.cos(Math.toRadians(deg));
    }

    /**
     * Tangent of a degree value.
     */
    public static double tanDeg(double deg) {
        return Math.tan(Math.toRadians(deg));
    }

    /**
     * Sinus of a degree value.
     */
    public static double sinDeg(double deg) {
        return Math.sin(Math.toRadians(deg));
    }
}
