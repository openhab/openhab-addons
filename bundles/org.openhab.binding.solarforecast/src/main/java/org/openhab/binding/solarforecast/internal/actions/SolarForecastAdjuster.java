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
package org.openhab.binding.solarforecast.internal.actions;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SolarForecastAdjuster} provides the adjustment parameters which are used to calculate the correction
 * factor
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class SolarForecastAdjuster {

    private final String identifier;
    private final double correctionFactor;
    private final double inverterEnergy;
    private final double forecastEnergy;
    private final boolean isHoldingTimeElapsed;

    public SolarForecastAdjuster(String identifier, double correctionFactor, double inverterEnergy,
            double forecastEnergy, boolean isHoldingTimeElapsed) {
        this.identifier = identifier;
        this.correctionFactor = correctionFactor;
        this.inverterEnergy = inverterEnergy;
        this.forecastEnergy = forecastEnergy;
        this.isHoldingTimeElapsed = isHoldingTimeElapsed;
    }

    /**
     * Returns the correction factor used to adjust the forecast
     *
     * @return correction factor
     */
    public double getCorrectionFactor() {
        return correctionFactor;
    }

    /**
     * Returns the inverter energy used for adjustment calculation at the time forecast is created
     *
     * @return inverter energy as double in kWh
     */
    public double getInverterEnergy() {
        return inverterEnergy;
    }

    /**
     * Returns the forecast energy used for adjustment calculation at the time forecast is created
     *
     * @return forecast energy as double in kWh
     */
    public double getForecastEnergy() {
        return forecastEnergy;
    }

    public boolean isHoldingTimeElapsed() {
        return isHoldingTimeElapsed;
    }

    @Override
    public String toString() {
        return identifier + " SolarForecastAdjuster [correctionFactor=" + correctionFactor + ", inverterEnergy="
                + inverterEnergy + ", forecastEnergy=" + forecastEnergy + ", isHoldingTimeElapsed="
                + isHoldingTimeElapsed + "]";
    }
}
