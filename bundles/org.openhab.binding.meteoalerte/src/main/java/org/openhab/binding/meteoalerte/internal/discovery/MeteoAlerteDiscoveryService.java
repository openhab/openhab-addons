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

import static org.openhab.binding.meteoalerte.internal.MeteoAlerteBindingConstants.THING_TYPE_DEPARTEMENT;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.meteoalerte.internal.config.DepartmentConfiguration;
import org.openhab.binding.meteoalerte.internal.db.DepartmentDbService;
import org.openhab.binding.meteoalerte.internal.db.DepartmentDbService.Department;
import org.openhab.binding.meteoalerte.internal.handler.MeteoAlerteBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MeteoAlerteDiscoveryService} discovers departments on the configured location.
 *
 * @author Gaël L'hopital - Initial Contribution
 */
@Component(service = ThingHandlerService.class)
@NonNullByDefault
public class MeteoAlerteDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {
    private static final int DISCOVER_TIMEOUT_SECONDS = 2;

    private final Logger logger = LoggerFactory.getLogger(MeteoAlerteDiscoveryService.class);
    private @Nullable LocationProvider locationProvider;
    private @Nullable DepartmentDbService dbService;
    private @Nullable MeteoAlerteBridgeHandler bridgeHandler;

    @Activate
    public MeteoAlerteDiscoveryService() {
        super(Set.of(THING_TYPE_DEPARTEMENT), DISCOVER_TIMEOUT_SECONDS);
    }

    @Override
    public void deactivate() {
        super.deactivate();
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

        MeteoAlerteBridgeHandler handler = bridgeHandler;
        ThingUID bridgeUID = handler != null ? handler.getThing().getUID() : null;
        if (bridgeUID == null) {
            logger.debug("No valid UID for Meteo Alerte bridge -> Will not provide any discovery results");
            return;
        }

        DepartmentDbService localDbService = dbService;
        if (localDbService != null) {
            createResults(location, localDbService, bridgeUID);
        } else {
            logger.debug("No department database available -> Will not provide any discovery results");
        }
    }

    private void createResults(PointType serverLocation, DepartmentDbService db, ThingUID bridgeUID) {
        List<Department> candidates = db.getBounding(serverLocation);

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

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof MeteoAlerteBridgeHandler bridgeHandler) {
            this.bridgeHandler = bridgeHandler;
            this.locationProvider = bridgeHandler.getLocationProvider();
            this.dbService = bridgeHandler.getDbService();
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }
}
