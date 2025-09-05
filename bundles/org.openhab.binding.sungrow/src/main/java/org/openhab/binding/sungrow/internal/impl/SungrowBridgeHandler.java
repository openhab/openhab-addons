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

import java.net.URI;
import java.time.Duration;

import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.sungrow.internal.SungrowBindingConstants;
import org.openhab.binding.sungrow.internal.SungrowConfiguration;
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

import de.afrouper.server.sungrow.api.SungrowClient;
import de.afrouper.server.sungrow.api.SungrowClientFactory;
import de.afrouper.server.sungrow.api.dto.*;

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
            sungrowClient = SungrowClientFactory.createSungrowClient(baseUrl, configuration.getAppKey(),
                    configuration.getAppSecret(), Duration.ofSeconds(10), Duration.ofSeconds(30));
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

            PlantList plants = sungrowClient.getPlants();
            plants.plants().forEach(this::handlePlants);
        } catch (SungrowApiException e) {
            logger.error("Failed to create Plants.", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, e.getMessage());
        }
    }

    private void handlePlants(Plant plant) {
        logger.debug("Create Handler for plant: {}", plant);
        ThingUID thingUID = new ThingUID(SungrowBindingConstants.THING_TYPE_PLANT.getBindingId(), getThing().getUID(),
                plant.plantId());

        if (thingRegistry.get(thingUID) != null) {
            logger.warn("Plant Thing with UID {} already exists. Skipping creation.", thingUID);
            return;
        }

        Configuration plantConfiguration = new Configuration();
        plantConfiguration.put("plant", plant);

        Thing plantThing = ThingBuilder.create(SungrowBindingConstants.THING_TYPE_PLANT, thingUID)
                .withBridge(getThing().getUID()).withLabel(plant.plantName()).withConfiguration(plantConfiguration)
                .build();

        thingRegistry.add(plantThing);

        DeviceList devices = sungrowClient.getDevices(plant.plantId());
        devices.devices().forEach(this::addDeviceThing);

        logger.info("Successfully registered new Plant Thing: {}", thingUID);
    }

    private void addDeviceThing(Device device) {
        ThingUID thingUID = new ThingUID(SungrowBindingConstants.THING_TYPE_DEVICE.getBindingId(), getThing().getUID(),
                device.plantDeviceId());

        if (thingRegistry.get(thingUID) != null) {
            logger.warn("Device Thing with UID {} already exists. Skipping creation.", thingUID);
            return;
        }

        Configuration deviceConfiguration = new Configuration();
        deviceConfiguration.put("device", device);

        Thing plantThing = ThingBuilder.create(SungrowBindingConstants.THING_TYPE_DEVICE, thingUID)
                .withBridge(getThing().getUID()).withLabel(device.deviceName()).withConfiguration(deviceConfiguration)
                .build();

        thingRegistry.add(plantThing);
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
