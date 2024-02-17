/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;

/**
 * Holds the calculated azimuth and elevation.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Gaël L'hopital - Added shade length
 * @author Christoph Weitkamp - Introduced UoM
 */
@NonNullByDefault
public class Position {

    private double azimuth;
    private double elevation;
    private double shadeLength;

    public Position() {
    }

    public Position(double azimuth, double elevation, double shadeLength) {
        this.azimuth = azimuth;
        this.elevation = elevation;
        this.shadeLength = shadeLength;
    }

    /**
     * Returns the azimuth.
     */
    public QuantityType<Angle> getAzimuth() {
        return new QuantityType<>(azimuth, Units.DEGREE_ANGLE);
    }

    /**
     * Sets the azimuth.
     */
    public void setAzimuth(double azimuth) {
        this.azimuth = azimuth;
    }

    /**
     * Returns the elevation.
     */
    public QuantityType<Angle> getElevation() {
        return new QuantityType<>(elevation, Units.DEGREE_ANGLE);
    }

    public double getElevationAsDouble() {
        return elevation;
    }

    /**
     * Sets the elevation.
     */
    public void setElevation(double elevation) {
        this.elevation = elevation;
    }

    /**
     * Returns the shade length.
     */
    public double getShadeLength() {
        return shadeLength;
    }

    /**
     * Sets the shade length.
     */
    public void setShadeLength(double shadeLength) {
        this.shadeLength = shadeLength;
    }
}
