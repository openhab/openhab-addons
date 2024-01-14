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
package org.openhab.binding.airquality.internal.discovery;

import static org.openhab.binding.airquality.internal.AirQualityBindingConstants.*;
import static org.openhab.binding.airquality.internal.config.AirQualityConfiguration.LOCATION;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.airquality.internal.handler.AirQualityBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AirQualityDiscoveryService} creates things based on the configured location.
 *
 * @author Gaël L'hopital - Initial Contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = AirQualityDiscoveryService.class, configurationPid = "discovery.airquality")
@NonNullByDefault
public class AirQualityDiscoveryService extends AbstractThingHandlerDiscoveryService<AirQualityBridgeHandler>
        implements ThingHandlerService {
    private static final int DISCOVER_TIMEOUT_SECONDS = 2;
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_STATION);

    private final Logger logger = LoggerFactory.getLogger(AirQualityDiscoveryService.class);

    private @NonNullByDefault({}) LocationProvider locationProvider;

    /**
     * Creates an AirQualityDiscoveryService with enabled autostart.
     */
    @Activate
    public AirQualityDiscoveryService() {
        super(AirQualityBridgeHandler.class, SUPPORTED_THING_TYPES_UIDS, DISCOVER_TIMEOUT_SECONDS, false);
    }

    @Reference(unbind = "-")
    public void bindLocationProvider(LocationProvider locationProvider) {
        this.locationProvider = locationProvider;
    }

    @Override
    protected void startScan() {
        logger.debug("Starting Air Quality discovery scan");
        LocationProvider provider = locationProvider;
        if (provider != null) {
            PointType location = provider.getLocation();
            AirQualityBridgeHandler bridge = this.thingHandler;
            if (location == null || bridge == null) {
                logger.info("openHAB server location is not defined, will not provide any discovery results");
                return;
            }
            createResults(location, bridge.getThing().getUID());
        }
    }

    private void createResults(PointType location, ThingUID bridgeUID) {
        ThingUID localAirQualityThing = new ThingUID(THING_TYPE_STATION, bridgeUID, LOCAL);
        thingDiscovered(DiscoveryResultBuilder.create(localAirQualityThing).withLabel("Local Air Quality")
                .withProperty(LOCATION, String.format("%s,%s", location.getLatitude(), location.getLongitude()))
                .withBridge(bridgeUID).build());
    }
}
