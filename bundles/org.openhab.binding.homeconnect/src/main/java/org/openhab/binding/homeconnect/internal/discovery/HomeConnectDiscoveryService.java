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
package org.openhab.binding.homeconnect.internal.discovery;

import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.*;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homeconnect.internal.client.HomeConnectApiClient;
import org.openhab.binding.homeconnect.internal.client.exception.AuthorizationException;
import org.openhab.binding.homeconnect.internal.client.exception.CommunicationException;
import org.openhab.binding.homeconnect.internal.client.model.HomeAppliance;
import org.openhab.binding.homeconnect.internal.handler.HomeConnectBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HomeConnectDiscoveryService} is responsible for discovering new devices.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = HomeConnectDiscoveryService.class)
@NonNullByDefault
public class HomeConnectDiscoveryService extends AbstractThingHandlerDiscoveryService<HomeConnectBridgeHandler> {

    private static final int SEARCH_TIME_SEC = 20;

    private final Logger logger = LoggerFactory.getLogger(HomeConnectDiscoveryService.class);

    /**
     * Construct a {@link HomeConnectDiscoveryService}.
     *
     */
    public HomeConnectDiscoveryService() {
        super(HomeConnectBridgeHandler.class, DISCOVERABLE_DEVICE_THING_TYPES_UIDS, SEARCH_TIME_SEC, true);
    }

    @Override
    protected void startScan() {
        logger.debug("Starting device scan.");

        HomeConnectApiClient apiClient = thingHandler.getApiClient();

        try {
            List<HomeAppliance> appliances = apiClient.getHomeAppliances();
            logger.debug("Scan found {} devices.", appliances.size());

            // add found devices
            for (HomeAppliance appliance : appliances) {
                @Nullable
                ThingTypeUID thingTypeUID = getThingTypeUID(appliance);

                if (thingTypeUID != null) {
                    logger.debug("Found {} ({}).", appliance.getHaId(), appliance.getType().toUpperCase());

                    Map<String, Object> properties = Map.of(HA_ID, appliance.getHaId());
                    String name = appliance.getBrand() + " " + appliance.getName() + " (" + appliance.getHaId() + ")";

                    DiscoveryResult discoveryResult = DiscoveryResultBuilder
                            .create(new ThingUID(BINDING_ID, appliance.getType(),
                                    thingHandler.getThing().getUID().getId(), appliance.getHaId()))
                            .withThingType(thingTypeUID).withProperties(properties).withRepresentationProperty(HA_ID)
                            .withBridge(thingHandler.getThing().getUID()).withLabel(name).build();
                    thingDiscovered(discoveryResult);
                } else {
                    logger.debug("Ignoring unsupported device {} of type {}.", appliance.getHaId(),
                            appliance.getType());
                }
            }
        } catch (CommunicationException | AuthorizationException e) {
            logger.debug("Exception during scan.", e);
        }

        logger.debug("Finished device scan.");
    }

    @Override
    public void dispose() {
        super.dispose();
        removeOlderResults(System.currentTimeMillis(), thingHandler.getThing().getUID());
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan(), thingHandler.getThing().getUID());
    }

    private @Nullable ThingTypeUID getThingTypeUID(HomeAppliance appliance) {
        @Nullable
        ThingTypeUID thingTypeUID = null;

        if (THING_TYPE_DISHWASHER.getId().equalsIgnoreCase(appliance.getType())) {
            thingTypeUID = THING_TYPE_DISHWASHER;
        } else if (THING_TYPE_OVEN.getId().equalsIgnoreCase(appliance.getType())) {
            thingTypeUID = THING_TYPE_OVEN;
        } else if (THING_TYPE_FRIDGE_FREEZER.getId().equalsIgnoreCase(appliance.getType())) {
            thingTypeUID = THING_TYPE_FRIDGE_FREEZER;
        } else if (THING_TYPE_DRYER.getId().equalsIgnoreCase(appliance.getType())) {
            thingTypeUID = THING_TYPE_DRYER;
        } else if (THING_TYPE_COFFEE_MAKER.getId().equalsIgnoreCase(appliance.getType())) {
            thingTypeUID = THING_TYPE_COFFEE_MAKER;
        } else if (THING_TYPE_HOOD.getId().equalsIgnoreCase(appliance.getType())) {
            thingTypeUID = THING_TYPE_HOOD;
        } else if (THING_TYPE_WASHER_DRYER.getId().equalsIgnoreCase(appliance.getType())) {
            thingTypeUID = THING_TYPE_WASHER_DRYER;
        } else if (THING_TYPE_COOKTOP.getId().equalsIgnoreCase(appliance.getType())) {
            thingTypeUID = THING_TYPE_COOKTOP;
        } else if (THING_TYPE_WASHER.getId().equalsIgnoreCase(appliance.getType())) {
            thingTypeUID = THING_TYPE_WASHER;
        }

        return thingTypeUID;
    }
}
