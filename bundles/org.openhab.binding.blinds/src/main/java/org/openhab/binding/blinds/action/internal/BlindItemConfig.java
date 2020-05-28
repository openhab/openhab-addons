/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.blinds.action.internal;

import org.openhab.binding.blinds.action.BlindDirection;

/**
 * The configuration of a blind item
 *
 * @author Markus Pfleger - Initial contribution
 */
public class BlindItemConfig {
    private int azimutLowerBound;
    private int azimutUpperBound;
    private boolean azimutRangeSet;

    private int elevationLowerBound;
    private int elevationUpperBound;
    private boolean elevationRangeSet;

    private double temperatureLowerBound;
    private double temperatureUpperBound;
    private boolean temperatureRangeSet;

    private int blindPositionLimit = -1;

    private boolean automaticProgramEnabled;
    private BlindDirection allowedDirection = BlindDirection.ANY;

    public BlindItemConfig() {
    }

    public void setBlindPositionLimit(int limit) {
        this.blindPositionLimit = limit;
    }

    public int getBlindPositionLimit() {
        return blindPositionLimit;
    }

    public void setTemperatureRange(double temperatureLowerBound, double temperatureUpperBound) {
        this.temperatureLowerBound = temperatureLowerBound;
        this.temperatureUpperBound = temperatureUpperBound;
        this.temperatureRangeSet = true;
    }

    public boolean isTemperatureRangeSet() {
        return this.temperatureRangeSet;
    }

    public double getTemperatureLowerBound() {
        return this.temperatureLowerBound;
    }

    public double getTemperatureUpperBound() {
        return this.temperatureUpperBound;
    }

    public void setSunRange(int azimutLowerBound, int azimutUpperBound, int elevationLowerBound,
            int elevationUpperBound) {
        this.azimutLowerBound = azimutLowerBound;
        this.azimutUpperBound = azimutUpperBound;
        this.elevationLowerBound = elevationLowerBound;
        this.elevationUpperBound = elevationUpperBound;

        if (this.azimutLowerBound >= 0 && this.azimutUpperBound >= 0) {
            azimutRangeSet = true;
        }

        if (this.elevationLowerBound >= 0 && this.elevationUpperBound >= 0) {
            elevationRangeSet = true;
        }
    }

    public boolean isAzimutRangeSet() {
        return azimutRangeSet;
    }

    public boolean isElevationRangeSet() {
        return elevationRangeSet;
    }

    public int getAzimutLowerBound() {
        return azimutLowerBound;
    }

    public int getAzimutUpperBound() {
        return azimutUpperBound;
    }

    public int getElevationLowerBound() {
        return elevationLowerBound;
    }

    public int getElevationUpperBound() {
        return elevationUpperBound;
    }

    public void setAutomaticProgramEnabled(boolean value) {
        this.automaticProgramEnabled = value;
    }

    public boolean isAutomaticProgramEnabled() {
        return this.automaticProgramEnabled;
    }

    public boolean isSunRangeSet() {
        return isAzimutRangeSet() || isElevationRangeSet();
    }

    public BlindDirection getAllowedBlindDirection() {
        return allowedDirection;
    }

    public void setAllowedBlindDirection(BlindDirection allowedDirection) {
        this.allowedDirection = allowedDirection;
    }
}
