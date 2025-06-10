/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import static org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants.*;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants;
import org.openhab.binding.solarforecast.internal.forecastsolar.ForecastSolarObject;
import org.openhab.binding.solarforecast.internal.utils.Utils;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.persistence.PersistenceServiceRegistry;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SmartForecastSolarPlaneHandler} is a subclass of {@link AdjustableForecastSolarPlaneHandler} that adjust
 * forecast
 * data according to current production values.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class SmartForecastSolarPlaneHandler extends AdjustableForecastSolarPlaneHandler {
    private final Logger logger = LoggerFactory.getLogger(SmartForecastSolarPlaneHandler.class);
    private double energyProduction = 0;
    private double forecastProduction = 0;

    public SmartForecastSolarPlaneHandler(Thing thing, HttpClient hc, PersistenceServiceRegistry psr) {
        super(thing, hc, psr);
    }

    @Override
    protected String queryParameters() {
        if (!SolarForecastBindingConstants.EMPTY.equals(configuration.horizon)) {
            return "&horizon=" + configuration.horizon;
        } else {
            return EMPTY;
        }
    }

    /**
     * Hook into the forecast update process to adjust the forecast based on current energy production.
     * It calculates the correction factor based on the current energy production and the forecasted energy production.
     * The factor is applied to the forecast, and the adjusted power and energy time series are sent to the channel.
     *
     * @param f The forecast object containing the forecast data
     */
    @Override
    protected synchronized void setForecast(ForecastSolarObject f) {
        forecast = f;
        energyProduction = Utils.getEnergyTillNow(configuration.powerItemName, persistenceService.get());
        forecastProduction = forecast.getActualEnergyValue(ZonedDateTime.now(Utils.getClock()));

        double factor = 1;
        Instant firstMeasure = forecast.getFirstPowerTimestamp();
        if (Instant.MAX.equals(firstMeasure)) {
            logger.debug("SmartForecastSolarPlaneHandler: Unable to determine first measure of forecast");
        } else {
            Instant startCorrectionTime = firstMeasure.plus(configuration.holdingTime, ChronoUnit.MINUTES);
            if (Instant.now(Utils.getClock()).isAfter(startCorrectionTime)) {
                if (forecastProduction > 0) {
                    factor = energyProduction / forecastProduction;
                }
                forecast.setCorrectionFactor(factor);
            } else {
                logger.debug("Holding time, first correction starts {}", startCorrectionTime);
            }
            logger.debug("E3DC {}, Forecast {} factor {}", energyProduction, forecastProduction, factor);
        }

        // factor is applied to the forecast so new adapted values are available
        updateState(CHANNEL_CORRECTION_FACTOR, new DecimalType(factor));
        super.setForecast(forecast);
    }

    public double getEnergyProduction() {
        return energyProduction;
    }

    public double getForecastProduction() {
        return forecastProduction;
    }
}
