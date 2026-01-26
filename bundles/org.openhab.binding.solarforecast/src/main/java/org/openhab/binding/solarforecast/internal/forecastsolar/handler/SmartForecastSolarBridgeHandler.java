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
package org.openhab.binding.solarforecast.internal.forecastsolar.handler;

import static org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants.CHANNEL_CORRECTION_FACTOR;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solarforecast.internal.actions.SolarForecastAdjuster;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.Bridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SmartForecastSolarBridgeHandler} is a non active handler instance. It will be triggerer by the bridge.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class SmartForecastSolarBridgeHandler extends ForecastSolarBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(SmartForecastSolarBridgeHandler.class);

    public SmartForecastSolarBridgeHandler(Bridge bridge, @Nullable PointType location) {
        super(bridge, location);
    }

    /**
     * Hook into the timeseriesUpdate process and update correction factor channel
     */
    @Override
    public void updateTimeseries() {
        super.updateTimeseries();
        double energyProductionSum = 0;
        double forecastProductionSum = 0;
        boolean holdingTimeElapsed = true;

        for (SolarForecastAdjuster adjuster : getAdjusters()) {
            energyProductionSum += adjuster.getInverterEnergy();
            forecastProductionSum += adjuster.getForecastEnergy();
            holdingTimeElapsed = holdingTimeElapsed && adjuster.isHoldingTimeElapsed();
            logger.trace("factor calculation {}", adjuster.toString());
        }
        double factor = 1;
        if (holdingTimeElapsed) {
            if (forecastProductionSum > 0) {
                factor = energyProductionSum / forecastProductionSum;
            }
        }
        logger.trace("Factor calculation Inverter {}, Forecast {} factor {}", energyProductionSum,
                forecastProductionSum, factor);
        // calculate new correction factor out of each plane and their production values
        updateState(CHANNEL_CORRECTION_FACTOR, new DecimalType(factor));
    }

    private List<SolarForecastAdjuster> getAdjusters() {
        return planes.stream().map(plane -> plane.getSolarForecasts()).flatMap(List::stream)
                .filter(solarForecast -> solarForecast.getAdjuster().isPresent())
                .map(solarForecast -> solarForecast.getAdjuster().get()).toList();
    }
}
