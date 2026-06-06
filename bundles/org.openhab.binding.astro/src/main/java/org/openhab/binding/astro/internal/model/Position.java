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
package org.openhab.binding.astro.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.astro.internal.util.MathUtils;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Immutable class holding calculated azimuth and elevation.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Gaël L'hopital - Added shade length
 * @author Christoph Weitkamp - Introduced UoM
 */
@NonNullByDefault
public class Position {
    public static final Position NONE = new Position();
    private final double azimuth;
    private final double elevation;

    private Position() {
        this(Double.NaN, Double.NaN);
    }

    public Position(double azimuth, double elevation) {
        this.azimuth = azimuth;
        this.elevation = elevation;
    }

    /**
     * Returns the azimuth.
     */
    public State getAzimuth() {
        return Double.isNaN(azimuth) ? UnDefType.UNDEF : new QuantityType<>(azimuth, Units.DEGREE_ANGLE);
    }

    public double getAzimuthAsDouble() {
        return azimuth;
    }

    /**
     * Returns the elevation.
     */
    public State getElevation() {
        return Double.isNaN(elevation) ? UnDefType.UNDEF : new QuantityType<>(elevation, Units.DEGREE_ANGLE);
    }

    public double getElevationAsDouble() {
        return elevation;
    }

    /**
     * Returns the shade length ratio.
     */
    public State getShadeLength() {
        return Double.isNaN(elevation) ? UnDefType.UNDEF
                : elevation <= 0 ? UnDefType.NULL : new DecimalType(1d / MathUtils.tanDeg(elevation));
    }
}
