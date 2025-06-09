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
            if (!configuration.powerItemName.isBlank()) {
                PersistenceService service = persistenceRegistry.get(configuration.powerItemPersistence);
                if (service != null) {
                    if (service instanceof QueryablePersistenceService queryService) {
                        if (Utils.checkPersistence(configuration.powerItemName, queryService)) {
                            persistenceService = Optional.of(queryService);
                            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE,
                                    "@text/solarforecast.plane.status.await-feedback");
                            bridgeHandler.ifPresentOrElse(handler -> {
                                handler.addPlane(this);
                            }, () -> {
                                // bridge handler is not available, so we cannot add the plane
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                        "@text/solarforecast.plane.status.bridge-handler-not-found");
                            });
                        } else {
                            // item not found in persistence
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                    "@text/solarforecast.plane.status.item-not-in-persistence" + " [\""
                                            + configuration.powerItemName + "\", \""
                                            + configuration.powerItemPersistence + "\"]");
                        }
                    } else {
                        // persistence service not queryable
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                "@text/solarforecast.plane.status.persistence-not-queryable" + " [\""
                                        + configuration.powerItemPersistence + "\"]");
                    }
                } else {
                    // persistence service not found
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/solarforecast.plane.status.persistence-not-found" + " [\""
                                    + configuration.powerItemPersistence + "\"]");
                }
            } else {
                // power item not configured
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/solarforecast.plane.status.item-not-found" + " [\"" + configuration.powerItemName
                                + "\"]");
            }
        }
        // else initialization failed already in super.doInitialize()
    }

    @Override
    protected String queryParameters() {
        String parameters = super.queryParameters();

        Instant firstMeasure = forecast.getFirstPowerTimestamp();
        if (Instant.MAX.equals(firstMeasure)) {
            logger.debug("Unable to determine first measure of forecast");
        } else {
            Instant startCorrectionTime = firstMeasure.plus(configuration.holdingTime, ChronoUnit.MINUTES);
            if (Instant.now(Utils.getClock()).isAfter(startCorrectionTime)) {
                if (!configuration.powerItemName.isBlank() && persistenceService.isPresent() && apiKey.isPresent()) {
                    logger.debug("Add real parameters");
                    // https://doc.forecast.solar/actual
                    parameters += "&actual="
                            + Utils.getEnergyTillNow(configuration.powerItemName, persistenceService.get());
                } else {
                    logger.debug("Add reset parameters - config missing powerItem, persistence or API key");
                    parameters += "&actual=0"; // no power item configured or persistence service not available
                }
            } else {
                logger.debug("Holding time, first correction starts {}", startCorrectionTime);
            }
        }
        logger.debug("Parameters for query {}", parameters);
        return parameters;
    }
}
