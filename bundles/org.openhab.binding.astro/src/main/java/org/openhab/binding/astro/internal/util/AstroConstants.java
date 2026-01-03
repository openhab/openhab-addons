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
package org.openhab.binding.astro.internal.util;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Common constants used across the binding
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AstroConstants {
    public static final double SECONDS_PER_DAY = 60 * 60 * 24;
    public static final double MILLISECONDS_PER_DAY = 1000 * SECONDS_PER_DAY;
    public static final double TROPICAL_YEAR_DAYS = 365.242189;
    public static final double TROPICAL_YEAR_SECONDS = TROPICAL_YEAR_DAYS * SECONDS_PER_DAY;
    public static final double SOLAR_MEAN_MOTION_PER_SECOND = MathUtils.TWO_PI / AstroConstants.TROPICAL_YEAR_SECONDS;

    /** Constructor */
    private AstroConstants() {
        throw new IllegalAccessError("Non-instantiable");
    }
}
