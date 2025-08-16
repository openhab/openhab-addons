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

import java.io.IOException;
import java.net.URI;

import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.sungrow.internal.SungrowBindingConstants;
import org.openhab.binding.sungrow.internal.SungrowConfiguration;
import org.openhab.binding.sungrow.internal.client.SungrowClient;
import org.openhab.binding.sungrow.internal.client.SungrowClientFactory;
import org.openhab.binding.sungrow.internal.client.operations.ApiOperationsFactory;
import org.openhab.binding.sungrow.internal.client.operations.PlantList;
import org.openhab.core.config.core.Configuration;
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

    private final HttpClient commonHttpClient;

    private volatile SungrowConfiguration configuration = new SungrowConfiguration();

    private ThingRegistry thingRegistry;

    private SungrowClient sungrowClient;

    public SungrowBridgeHandler(Bridge bridge, HttpClient commonHttpClient) {
        super(bridge);
        this.commonHttpClient = commonHttpClient;
    }

    @Override
    public void initialize() {
        logger.info("Bridge is initializing: {}", getThing().getUID());
        try {
            updateStatus(ThingStatus.UNKNOWN);
            configuration = getConfigAs(SungrowConfiguration.class);
            if (!configuration.isValid()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid configuration.");
                return;
            }
            thingRegistry = fetchThingRegistry();
            URI baseUrl = configuration.getRegion().getBaseUrl();
            if (configuration.getHostname() != null && !configuration.getHostname().isEmpty()) {
                baseUrl = new URI(configuration.getHostname());
            }
            sungrowClient = SungrowClientFactory.createSungrowClient(commonHttpClient, baseUrl,
                    configuration.getAppKey(), configuration.getAppSecret());
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

    SungrowClient getSungrowClient() {
        return sungrowClient;
    }

    SungrowConfiguration getConfiguration() {
        return configuration;
    }

    private void createPlants() {
        try {
            sungrowClient.login(configuration.getUsername(), configuration.getPassword());
            updateStatus(ThingStatus.ONLINE);

            PlantList plantList = ApiOperationsFactory.getPlantList();
            sungrowClient.execute(plantList);
            plantList.getResponse().getPlants().forEach(this::handlePlants);
        } catch (IOException e) {
            logger.error("Failed to create Plants.", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, e.getMessage());
        }
    }

    private void handlePlants(PlantList.Plant plant) {
        logger.debug("Create Handler for plant: {}", plant);
        ThingUID thingUID = new ThingUID(SungrowBindingConstants.THING_TYPE_PLANT.getBindingId(), getThing().getUID(),
                plant.getPlantId());

        if (thingRegistry.get(thingUID) != null) {
            logger.warn("Plant Thing with UID {} already exists. Skipping creation.", thingUID);
            return;
        }

        Configuration configuration = new Configuration();
        configuration.put("plantId", plant.getPlantId());

        Thing plantThing = ThingBuilder.create(SungrowBindingConstants.THING_TYPE_PLANT, thingUID)
                .withBridge(getThing().getUID()).withLabel(plant.getPlantName()).withConfiguration(configuration)
                .build();

        thingRegistry.add(plantThing);
        logger.info("Successfully registered new Plant Thing: {}", thingUID);
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
