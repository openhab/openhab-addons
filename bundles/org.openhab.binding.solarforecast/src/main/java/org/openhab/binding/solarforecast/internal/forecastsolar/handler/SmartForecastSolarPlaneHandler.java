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

import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.solarforecast.internal.actions.SolarForecastAdjuster;
import org.openhab.binding.solarforecast.internal.forecastsolar.ForecastSolarObject;
import org.openhab.binding.solarforecast.internal.utils.Utils;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.persistence.PersistenceServiceRegistry;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SmartForecastSolarPlaneHandler} is a subclass of {@link AdjustableForecastSolarPlaneHandler} that adjust
 * forecast data according to current production values.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class SmartForecastSolarPlaneHandler extends AdjustableForecastSolarPlaneHandler {
    private final Logger logger = LoggerFactory.getLogger(SmartForecastSolarPlaneHandler.class);

    public SmartForecastSolarPlaneHandler(Thing thing, HttpClient hc, PersistenceServiceRegistry psr) {
        super(thing, hc, psr);
    }

    @Override
    protected void queryParameters(Map<String, String> parameters) {
        super.queryParameters(parameters);
        // remove actual key if present - smart does calculate adjustment itself
        parameters.remove("actual");
    }

    /**
     * Hook into the forecast update process to adjust the forecast based on current energy production.
     * It calculates the correction factor based on the current energy production and the forecasted energy production.
     * The factor is applied to the forecast, and the adjusted power and energy time series are sent to the channel.
     *
     * @param newForecast forecast object containing the forecast data
     */
    @Override
    protected void updateForecast(ForecastSolarObject newForecast) {
        ForecastSolarObject adjustedForecast = newForecast;
        if (persistenceService != null) {
            // Get inverter energy production till now and predicted energy production from forecast
            Optional<Double> energyCalculation = Utils.getEnergyTillNow(configuration.calculationItemName,
                    persistenceService);
            double energyProduction = energyCalculation.isPresent() ? energyCalculation.get() : 0;

            // calculate correction factor if holding time elapsed
            boolean holdingTimeElapsed = Utils.isHoldingTimeElapsed(adjustedForecast, configuration.holdingTime);
            adjustedForecast = new ForecastSolarObject(newForecast, energyProduction, holdingTimeElapsed);
            Optional<SolarForecastAdjuster> adjuster = adjustedForecast.getAdjuster();
            double factor = 1;
            if (adjuster.isPresent()) {
                if (holdingTimeElapsed) {
                    factor = adjuster.get().getCorrectionFactor();
                }
                logger.debug("{}", adjuster.get().toString());
            } else {
                logger.debug("No adjuster available for forecast adjustment");
            }

            // factor is applied to the forecast so new adapted values are available
            updateState(CHANNEL_CORRECTION_FACTOR, new DecimalType(factor));
        } else {
            logger.debug("No persistence service available, no adjustment of forecast");
        }
        // finally call superclass to set the adjusted forecast
        super.updateForecast(adjustedForecast);
    }
}
