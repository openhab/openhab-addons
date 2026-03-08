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

import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.solarforecast.internal.forecastsolar.config.ForecastSolarPlaneConfiguration;
import org.openhab.binding.solarforecast.internal.utils.Utils;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.PersistenceServiceRegistry;
import org.openhab.core.persistence.QueryablePersistenceService;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AdjustableForecastSolarPlaneHandler} is a subclass of {@link ForecastSolarPlaneHandler} which adds energy
 * values to the forecast query. forecast.solar API uses them to adjust the forecast result.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class AdjustableForecastSolarPlaneHandler extends ForecastSolarPlaneHandler {
    private final Logger logger = LoggerFactory.getLogger(AdjustableForecastSolarPlaneHandler.class);
    protected final PersistenceServiceRegistry persistenceRegistry;

    protected @Nullable QueryablePersistenceService persistenceService;

    public AdjustableForecastSolarPlaneHandler(Thing thing, HttpClient hc, PersistenceServiceRegistry psr) {
        super(thing, hc);
        persistenceRegistry = psr;
    }

    @Override
    public void initialize() {
        configuration = getConfigAs(ForecastSolarPlaneConfiguration.class);
        if (!configuration.calculationItemName.isBlank()) {
            PersistenceService service = persistenceRegistry.get(configuration.calculationItemPersistence);
            if (service != null) {
                if (service instanceof QueryablePersistenceService queryService) {
                    if (Utils.checkPersistence(configuration.calculationItemName, queryService)) {
                        persistenceService = queryService;
                        super.initialize();
                    } else {
                        // item not found in persistence
                        configErrorStatus("@text/solarforecast.plane.status.item-not-in-persistence" + " [\""
                                + configuration.calculationItemName + "\", \""
                                + configuration.calculationItemPersistence + "\"]");
                    }
                } else {
                    // persistence service cannot be queried
                    configErrorStatus("@text/solarforecast.plane.status.persistence-not-queryable" + " [\""
                            + configuration.calculationItemPersistence + "\"]");
                }
            } else {
                // persistence service not found
                configErrorStatus("@text/solarforecast.plane.status.persistence-not-found" + " [\""
                        + configuration.calculationItemPersistence + "\"]");
            }
        } else {
            // calculation item not configured
            configErrorStatus("@text/solarforecast.plane.status.item-not-found" + " [\""
                    + configuration.calculationItemName + "\"]");
        }
    }

    @Override
    /**
     * Adds actual energy production to the query parameters if holding time has passed.
     */
    protected void queryParameters(Map<String, String> parameters) {
        // add parameters from super class
        super.queryParameters(parameters);
        // add parameters from config
        if (isHoldingTimeElapsed()) {
            if (!configuration.calculationItemName.isBlank() && persistenceService != null) {
                // https://doc.forecast.solar/actual
                Optional<Double> energyCalculation = Utils.getEnergyTillNow(configuration.calculationItemName,
                        persistenceService);
                energyCalculation.ifPresentOrElse(value -> {
                    parameters.put("actual", String.valueOf(value));
                }, () -> {
                    logger.debug("Add reset parameters - failed to calculate energy from item {} in persistence {}",
                            configuration.calculationItemName, persistenceService);
                    parameters.put("actual", "0");
                });
            } else {
                logger.debug("Add reset parameters - config missing for item {} in persistence {}",
                        configuration.calculationItemName, persistenceService);
                parameters.put("actual", "0");
            }
        } else {
            logger.debug("Holding time not elapsed, no adjustment of forecast");
        }
    }

    public boolean isHoldingTimeElapsed() {
        return Utils.isHoldingTimeElapsed(getForecast(), configuration.holdingTime);
    }
}
