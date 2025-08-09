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
package org.openhab.binding.sungrow.internal.impl;

import org.openhab.binding.sungrow.internal.SungrowBindingConstants;
import org.openhab.binding.sungrow.internal.SungrowConfiguration;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SungrowBridgeHandler} is responsible for creating Plant Things which
 * are handled by {@link SungrowPlantHandler}
 *
 * @author Christian Kemper - Initial contribution
 */
public class SungrowBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(SungrowBridgeHandler.class);

    private ThingRegistry thingRegistry;

    private ApiClient apiClient;

    public SungrowBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        logger.info("Bridge is initializing: {}", getThing().getUID());

        try {
            thingRegistry = fetchThingRegistry();
            apiClient = new ApiClient(getConfigAs(SungrowConfiguration.class));
            updateStatus(ThingStatus.UNKNOWN);
            scheduler.execute(this::createPlants);
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, e.getMessage());
            logger.error("Unable to fetch ThingRegistry.", e);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.warn("Received a command {} for channel {}; this should not happen in bridge.", command, channelUID);
    }

    @Override
    public void dispose() {
        super.dispose();
        logger.info("Bridge disposed.");
    }

    ApiClient getApiClient() {
        return apiClient;
    }

    private void createPlants() {
        ThingUID thingUID = null;
        try {
            apiClient.initialize();
            updateStatus(ThingStatus.ONLINE);

            String plantId = "id-from-sungrow";
            thingUID = new ThingUID(SungrowBindingConstants.THING_TYPE_PLANT.getBindingId(), getThing().getUID(),
                    plantId);

            if (thingRegistry.get(thingUID) != null) {
                logger.warn("Plant Thing with UID {} already exists. Skipping creation.", thingUID);
                return;
            }

            Thing plant = ThingBuilder.create(SungrowBindingConstants.THING_TYPE_PLANT, thingUID)
                    .withBridge(getThing().getUID()).withLabel("Label for Plant").build();

            thingRegistry.add(plant);
            logger.info("Successfully registered new Plant Thing: {}", thingUID);

        } catch (Exception e) {
            logger.error("Failed to register Plant Thing: {}", thingUID, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, e.getMessage());
        }
    }

    private ThingRegistry fetchThingRegistry() {
        BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        ServiceReference<ThingRegistry> reference = bundleContext.getServiceReference(ThingRegistry.class);
        if (reference != null) {
            ThingRegistry thingRegistry = bundleContext.getService(reference);
            logger.info("Successfully retrieved ThingRegistry.");
            return thingRegistry;
        } else {
            logger.error("Failed to retrieve ThingRegistry.");
            throw new RuntimeException("Failed to retrieve ThingRegistry.");
        }
    }
}
