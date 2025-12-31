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
public abstract class Position {

    protected final double azimuth;
    protected final double elevation;

    public Position(double azimuth, double elevation) {
        this.azimuth = azimuth;
        this.elevation = elevation;
    }

    /**
     * Returns the azimuth.
     */
    public State getAzimuth() {
        return Double.isNaN(azimuth) ? UnDefType.NULL : new QuantityType<>(azimuth, Units.DEGREE_ANGLE);
    }

    public double getAzimuthAsDouble() {
        return azimuth;
    }

    /**
     * Returns the elevation.
     */
    public State getElevation() {
        return Double.isNaN(elevation) ? UnDefType.NULL : new QuantityType<>(elevation, Units.DEGREE_ANGLE);
    }

    public double getElevationAsDouble() {
        return elevation;
    }
}
