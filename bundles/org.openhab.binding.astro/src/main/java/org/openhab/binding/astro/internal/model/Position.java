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
 * Holds the calculated azimuth and elevation.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Christoph Weitkamp - Introduced UoM
 */
@NonNullByDefault
public class Position {
    public static final Position NULL = new Position();
    protected final double azimuth;
    protected final double elevation;

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
    public @Nullable QuantityType<Angle> getAzimuth() {
        return Double.isNaN(azimuth) ? null : new QuantityType<>(azimuth, Units.DEGREE_ANGLE);
    }

    public double getAzimuthAsDouble() {
        return azimuth;
    }

    /**
     * Returns the elevation.
     */
    public @Nullable QuantityType<Angle> getElevation() {
        return Double.isNaN(elevation) ? null : new QuantityType<>(elevation, Units.DEGREE_ANGLE);
    }

    public double getElevationAsDouble() {
        return elevation;
    }

    /**
     * Returns the shade length.
     */
    public State getShadeLength() {
        return Double.isNaN(elevation) ? UnDefType.NULL : new DecimalType(1 / MathUtils.tanDeg(elevation));
    }
}
