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
package org.openhab.binding.meteoalerte.internal.discovery;

import static org.openhab.binding.meteoalerte.internal.MeteoAlerteBindingConstants.*;
import static org.openhab.binding.meteoalerte.internal.config.ForecastConfiguration.LOCATION;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.meteoalerte.internal.config.DepartmentConfiguration;
import org.openhab.binding.meteoalerte.internal.db.DepartmentDbService;
import org.openhab.binding.meteoalerte.internal.db.DepartmentDbService.Department;
import org.openhab.binding.meteoalerte.internal.handler.MeteoAlerteBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MeteoAlerteDiscoveryService} discovers departments on the configured location.
 *
 * @author Gaël L'hopital - Initial Contribution
 */
@Component(service = ThingHandlerService.class)
@NonNullByDefault
public class MeteoAlerteDiscoveryService extends AbstractThingHandlerDiscoveryService<MeteoAlerteBridgeHandler> {
    private static final int DISCOVER_TIMEOUT_SECONDS = 2;
    private @NonNullByDefault({}) LocationProvider locationProvider;
    private @NonNullByDefault({}) DepartmentDbService dbService;

    private final Logger logger = LoggerFactory.getLogger(MeteoAlerteDiscoveryService.class);

    public MeteoAlerteDiscoveryService() {
        super(MeteoAlerteBridgeHandler.class, DISCOVERABLE_THING_TYPES_UIDS, DISCOVER_TIMEOUT_SECONDS);
    }

    @Reference(unbind = "-")
    public void bindTranslationProvider(TranslationProvider translationProvider) {
        this.i18nProvider = translationProvider;
    }

    @Reference(unbind = "-")
    public void bindLocaleProvider(LocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
    }

    @Reference(unbind = "-")
    public void bindLocationProvider(LocationProvider locationProvider) {
        this.locationProvider = locationProvider;
    }

    @Reference(unbind = "-")
    public void bindDepartmentDbService(DepartmentDbService dbService) {
        this.dbService = dbService;
    }

    @Override
    public void startScan() {
        logger.debug("Starting Meteo Alerte department discovery scan");

        LocationProvider localLocation = locationProvider;
        PointType location = localLocation != null ? localLocation.getLocation() : null;
        if (location == null) {
            logger.debug("LocationProvider.getLocation() is not set -> Will not provide any discovery results");
            return;
        }

        createDepartmentResults(location);
        createForecastResults(location);
    }

    private void createForecastResults(PointType location) {
        thingDiscovered(DiscoveryResultBuilder.create(new ThingUID(THING_TYPE_RAIN_FORECAST, LOCAL))
                .withLabel("@text/discovery.meteoalerte.rain-forecast.local.label")
                .withProperty(LOCATION, location.toString()).withRepresentationProperty(LOCATION).build());
    }

    private void createDepartmentResults(PointType serverLocation) {
        List<Department> candidates = dbService.getBounding(serverLocation);
        ThingUID bridgeUID = thingHandler.getThing().getUID();
        if (!candidates.isEmpty()) {
            candidates.forEach(dep -> thingDiscovered(
                    DiscoveryResultBuilder.create(new ThingUID(THING_TYPE_DEPARTEMENT, bridgeUID, dep.id()))//
                            .withLabel("Vigilance Météo: %s".formatted(dep.name())) //
                            .withProperty(DepartmentConfiguration.DEPARTMENT, dep.id()) //
                            .withRepresentationProperty(DepartmentConfiguration.DEPARTMENT) //
                            .withBridge(bridgeUID).build()));
        } else {
            logger.info("No department could be discovered matching server location");
        }
    }
}
