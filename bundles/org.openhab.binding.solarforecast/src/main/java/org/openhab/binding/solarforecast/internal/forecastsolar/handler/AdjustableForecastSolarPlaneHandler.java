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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.solarforecast.internal.utils.Utils;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.PersistenceServiceRegistry;
import org.openhab.core.persistence.QueryablePersistenceService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
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

    protected Optional<QueryablePersistenceService> persistenceService = Optional.empty();

    public AdjustableForecastSolarPlaneHandler(Thing thing, HttpClient hc, PersistenceServiceRegistry psr) {
        super(thing, hc);
        persistenceRegistry = psr;
    }

    @Override
    public void initialize() {
        if (super.doInitialize()) {
            if (!configuration.calculationItemName.isBlank()) {
                PersistenceService service = persistenceRegistry.get(configuration.calculationItemPersistence);
                if (service != null) {
                    if (service instanceof QueryablePersistenceService queryService) {
                        if (Utils.checkPersistence(configuration.calculationItemName, queryService)) {
                            persistenceService = Optional.of(queryService);
                            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE,
                                    "@text/solarforecast.plane.status.await-feedback");
                            bridgeHandler.ifPresentOrElse(handler -> {
                                handler.addPlane(this);
                            }, () -> {
                                // bridge handler is not available, so we cannot add the plane
                                configErrorStatus("@text/solarforecast.plane.status.bridge-handler-not-found");
                            });
                        } else {
                            // item not found in persistence
                            configErrorStatus("@text/solarforecast.plane.status.item-not-in-persistence" + " [\""
                                    + configuration.calculationItemName + "\", \""
                                    + configuration.calculationItemPersistence + "\"]");
                        }
                    } else {
                        // persistence service not queryable
                        configErrorStatus("@text/solarforecast.plane.status.persistence-not-queryable" + " [\""
                                + configuration.calculationItemPersistence + "\"]");
                    }
                } else {
                    // persistence service not found
                    configErrorStatus("@text/solarforecast.plane.status.persistence-not-found" + " [\""
                            + configuration.calculationItemPersistence + "\"]");
                }
            } else {
                // power item not configured
                configErrorStatus("@text/solarforecast.plane.status.item-not-found" + " [\""
                        + configuration.calculationItemName + "\"]");
            }
        }
        // else initialization failed already in super.doInitialize()
    }

    @Override
    /**
     * Adds actual energy production to the query parameters if holding time has passed.
     */
    protected Map<String, String> queryParameters() {
        Map<String, String> parameters = super.queryParameters();

        if (isHoldingTimeElapsed()) {
            if (!configuration.calculationItemName.isBlank() && persistenceService.isPresent() && apiKey.isPresent()) {
                // https://doc.forecast.solar/actual
                parameters.put("actual", String
                        .valueOf(Utils.getEnergyTillNow(configuration.calculationItemName, persistenceService.get())));
            } else {
                logger.debug("Add reset parameters - config missing calculationItem, persistence or API key");
                parameters.put("actual", "0");
            }
        } else {
            logger.debug("Holding time not elapsed, no adjustment of forecast");
        }
        return parameters;
    }

    public boolean isHoldingTimeElapsed() {
        Optional<Instant> firstMeasure = forecast.getFirstPowerTimestamp();
        if (firstMeasure.isPresent()) {
            return Instant.now(Utils.getClock())
                    .isAfter(firstMeasure.get().plus(configuration.holdingTime, ChronoUnit.MINUTES));
        }
        if (!forecast.isEmpty()) {
            logger.warn("No adjustment possible: Unable to find first measure in forecast {}", forecast.getRaw());
        } else {
            logger.debug("Forecast is empty, no first measure available");
        }
        return false;
    }
}
