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
package org.openhab.binding.airparif.internal.discovery;

import static org.openhab.binding.airparif.internal.AirParifBindingConstants.LOCATION_THING_TYPE;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.airparif.internal.config.LocationConfiguration;
import org.openhab.binding.airparif.internal.db.DepartmentDbService;
import org.openhab.binding.airparif.internal.db.DepartmentDbService.Department;
import org.openhab.binding.airparif.internal.handler.AirParifBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AirParifDiscoveryService} creates things based on the configured location.
 *
 * @author Gaël L'hopital - Initial Contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = AirParifDiscoveryService.class)
@NonNullByDefault
public class AirParifDiscoveryService extends AbstractThingHandlerDiscoveryService<AirParifBridgeHandler> {
    private static final int DISCOVER_TIMEOUT_SECONDS = 2;

    private final Logger logger = LoggerFactory.getLogger(AirParifDiscoveryService.class);
    private final DepartmentDbService dbService = new DepartmentDbService();

    private @NonNullByDefault({}) LocationProvider locationProvider;

    public AirParifDiscoveryService() {
        super(AirParifBridgeHandler.class, Set.of(LOCATION_THING_TYPE), DISCOVER_TIMEOUT_SECONDS);
    }

    @Reference(unbind = "-")
    public void setLocationProvider(LocationProvider locationProvider) {
        this.locationProvider = locationProvider;
    }

    @Override
    public void startScan() {
        logger.debug("Starting AirParif discovery scan");

        LocationProvider localLocation = locationProvider;
        PointType location = localLocation != null ? localLocation.getLocation() : null;
        if (location == null) {
            logger.warn("LocationProvider.getLocation() is not set -> Will not provide any discovery results");
            return;
        }

        createDepartmentResults(location);
    }

    private void createDepartmentResults(PointType serverLocation) {
        List<Department> candidates = dbService.getBounding(serverLocation);
        ThingUID bridgeUID = thingHandler.getThing().getUID();
        if (!candidates.isEmpty()) {
            candidates.forEach(dep -> thingDiscovered(
                    DiscoveryResultBuilder.create(new ThingUID(LOCATION_THING_TYPE, bridgeUID, dep.id()))//
                            .withLabel("Air Quality Report: %s".formatted(dep.name())) //
                            .withProperty(LocationConfiguration.DEPARTMENT, dep.id()) //
                            .withProperty(LocationConfiguration.LOCATION, serverLocation.toFullString())//
                            .withRepresentationProperty(LocationConfiguration.DEPARTMENT) //
                            .withBridge(bridgeUID).build()));
        }
    }
}
