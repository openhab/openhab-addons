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

import javax.measure.quantity.Angle;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
 * @author Christoph Weitkamp - Introduced UoM
 */
@NonNullByDefault
<<<<<<< Upstream, based on main
<<<<<<< Upstream, based on main
public class Position {
    public static final Position NONE = new Position();
    private final double azimuth;
    private final double elevation;
=======
public abstract class Position {
>>>>>>> 48a7069 Reworked sun and moon position Reworked eclipse calculations Transitioned these to Instant Added unit tests for eclipses

<<<<<<< Upstream, based on main
    private Position() {
        this(Double.NaN, Double.NaN);
    }
=======
=======
public class Position {
    public static final Position NULL = new Position();
>>>>>>> 385bae1 Rebased. Corrected moon_day dynamic icons Reworked sun and moon position Reworked eclipse calculations Transitioned these to Instant Added unit tests for eclipses
    protected final double azimuth;
    protected final double elevation;
>>>>>>> 48a7069 Reworked sun and moon position Reworked eclipse calculations Transitioned these to Instant Added unit tests for eclipses

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
<<<<<<< Upstream, based on main
    public State getAzimuth() {
<<<<<<< Upstream, based on main
        return Double.isNaN(azimuth) ? UnDefType.UNDEF : new QuantityType<>(azimuth, Units.DEGREE_ANGLE);
=======
        return Double.isNaN(azimuth) ? UnDefType.NULL : new QuantityType<>(azimuth, Units.DEGREE_ANGLE);
>>>>>>> 48a7069 Reworked sun and moon position Reworked eclipse calculations Transitioned these to Instant Added unit tests for eclipses
=======
    public @Nullable QuantityType<Angle> getAzimuth() {
        return Double.isNaN(azimuth) ? null : new QuantityType<>(azimuth, Units.DEGREE_ANGLE);
>>>>>>> 0c5383c Reverting contract modification in Action. lspiel code review adressed.
    }

    public double getAzimuthAsDouble() {
        return azimuth;
    }

    /**
     * Returns the elevation.
     */
<<<<<<< Upstream, based on main
    public State getElevation() {
<<<<<<< Upstream, based on main
        return Double.isNaN(elevation) ? UnDefType.UNDEF : new QuantityType<>(elevation, Units.DEGREE_ANGLE);
=======
        return Double.isNaN(elevation) ? UnDefType.NULL : new QuantityType<>(elevation, Units.DEGREE_ANGLE);
>>>>>>> 48a7069 Reworked sun and moon position Reworked eclipse calculations Transitioned these to Instant Added unit tests for eclipses
=======
    public @Nullable QuantityType<Angle> getElevation() {
        return Double.isNaN(elevation) ? null : new QuantityType<>(elevation, Units.DEGREE_ANGLE);
>>>>>>> 0c5383c Reverting contract modification in Action. lspiel code review adressed.
    }

    public double getElevationAsDouble() {
        return elevation;
    }
<<<<<<< Upstream, based on main
<<<<<<< Upstream, based on main

<<<<<<< Upstream, based on main
    /**
     * Returns the shade length ratio.
     */
    public State getShadeLength() {
        return Double.isNaN(elevation) ? UnDefType.UNDEF
                : elevation <= 0 ? UnDefType.NULL : new DecimalType(1d / MathUtils.tanDeg(elevation));
=======

    /**
     * Returns the shade length.
     */
    public State getShadeLength() {
        return Double.isNaN(elevation) ? UnDefType.NULL : new DecimalType(1 / MathUtils.tanDeg(elevation));
>>>>>>> 385bae1 Rebased. Corrected moon_day dynamic icons Reworked sun and moon position Reworked eclipse calculations Transitioned these to Instant Added unit tests for eclipses
    }
}
=======
}
>>>>>>> 48a7069 Reworked sun and moon position Reworked eclipse calculations Transitioned these to Instant Added unit tests for eclipses
=======
}
>>>>>>> d82040e Applied spotless
