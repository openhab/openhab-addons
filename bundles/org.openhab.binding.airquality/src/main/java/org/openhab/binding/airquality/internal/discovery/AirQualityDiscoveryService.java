/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.airquality.internal.handler.AirQualityBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AirQualityDiscoveryService} creates things based on the configured location.
 *
 * @author Gaël L'hopital - Initial Contribution
 */
@Component(service = DiscoveryService.class, configurationPid = "discovery.airquality")
@NonNullByDefault
public class AirQualityDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {
    private static final int DISCOVER_TIMEOUT_SECONDS = 2;
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_STATION);

    private final Logger logger = LoggerFactory.getLogger(AirQualityDiscoveryService.class);

    private @Nullable LocationProvider locationProvider;
    private @Nullable AirQualityBridgeHandler bridgeHandler;

    /**
     * Creates an AirQualityDiscoveryService with enabled autostart.
     */
    public AirQualityDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, DISCOVER_TIMEOUT_SECONDS, false);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof AirQualityBridgeHandler) {
            final AirQualityBridgeHandler bridgeHandler = (AirQualityBridgeHandler) handler;
            this.bridgeHandler = bridgeHandler;
            this.locationProvider = bridgeHandler.getLocationProvider();
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    protected void startScan() {
        logger.debug("Starting Air Quality discovery scan");
        LocationProvider provider = locationProvider;
        if (provider != null) {
            PointType location = provider.getLocation();
            AirQualityBridgeHandler bridge = this.bridgeHandler;
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
