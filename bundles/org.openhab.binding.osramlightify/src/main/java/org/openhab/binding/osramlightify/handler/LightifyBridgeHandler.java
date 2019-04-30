/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.osramlightify.handler;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;

import org.openhab.binding.osramlightify.internal.LightifyConnector;
import org.openhab.binding.osramlightify.internal.discovery.LightifyDeviceDiscoveryService;

import org.openhab.binding.osramlightify.internal.config.LightifyBridgeConfiguration;

import org.openhab.binding.osramlightify.internal.messages.LightifyActivateSceneMessage;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openhab.binding.osramlightify.internal.messages.LightifyMessage;

import org.openhab.binding.osramlightify.internal.util.IEEEAddress;

/**
 * The {@link org.eclipse.smarthome.core.thing.binding.BridgeHandler} implementation to handle commands
 * and status of the OSRAM Lightify gateway device. This handler uses a native implementation of the
 * Lightify proprietary protocol to communicate with the Lightify gateway, therefore no internet connection
 * is necessary to utilize the OSRAM public API.
 *
 * @author Mike Jagdis - Initial contribution
 */
@NonNullByDefault
public final class LightifyBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(LightifyBridgeHandler.class);

    private @Nullable ServiceRegistration<?> serviceRegistration = null;
    private LightifyDeviceDiscoveryService discoveryService = new LightifyDeviceDiscoveryService(this);

    private @Nullable ThingRegistry globalThingRegistry = null;

    private LightifyBridgeConfiguration configuration = getConfigAs(LightifyBridgeConfiguration.class);
    private LightifyConnector connector = new LightifyConnector(this);

    public final HashMap<IEEEAddress, @Nullable Object> knownDevices = new HashMap<>();
    public final HashMap<Short, @Nullable Object> knownGroups = new HashMap<>();

    public LightifyBridgeHandler(Bridge bridge, BundleContext bundleContext, @Nullable ThingRegistry thingRegistry) {
        super(bridge);
        serviceRegistration = bundleContext.registerService(DiscoveryService.class, discoveryService, null);
        globalThingRegistry = thingRegistry;
    }

    @Override
    public void initialize() {
        thingUpdated(getThing());
        updateStatus(ThingStatus.UNKNOWN);
        connector.start();
    }

    @Override
    public void dispose() {
        logger.debug("Interrupt Lightify connector");
        connector.interrupt();
        try {
            connector.join();
        } catch (InterruptedException ie) {
        }

        discoveryService.removeResults();

        if (serviceRegistration != null) {
            serviceRegistration.unregister();
            serviceRegistration = null;
        }

        super.dispose();
    }

    @Override
    public void thingUpdated(Thing thing) {
        configuration = getConfigAs(LightifyBridgeConfiguration.class);

        configuration.discoveryIntervalNanos = TimeUnit.MILLISECONDS.toNanos((long) (configuration.discoveryInterval * 1000 + 0.5));
        configuration.minPollIntervalNanos = TimeUnit.MILLISECONDS.toNanos((long) (configuration.minPollInterval * 1000 + 0.5));
        configuration.maxPollIntervalNanos = TimeUnit.MILLISECONDS.toNanos((long) (configuration.maxPollInterval * 1000 + 0.5));

        for (Thing child : ((Bridge) thing).getThings()) {
            ThingHandler thingHandler = child.getHandler();
            if (thingHandler != null) {
                thingHandler.thingUpdated(child);
            }
        }
    }

    public void setStatusOnline() {
        updateStatus(ThingStatus.ONLINE);
    }

    public void setStatusOffline(ThingStatusDetail detail) {
        updateStatus(ThingStatus.OFFLINE, detail);
    }

    public void sendMessage(LightifyMessage message) {
        connector.sendMessage(message);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("{}, Command: {} {}", channelUID, command.getClass().getSimpleName(), command);

        if (command instanceof RefreshType) {
            /* Ignore it - we have nothing useful to say. */

        } else if (command instanceof DecimalType) {
            DecimalType sceneNo = (DecimalType) command;

            logger.debug("{}: activate scene: {}", channelUID, sceneNo);

            sendMessage(new LightifyActivateSceneMessage(sceneNo));
        }
    }

    public LightifyBridgeConfiguration getConfiguration() {
        return configuration;
    }

    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    public @Nullable Thing getThingByUIDGlobally(ThingUID thingUID) {
        return globalThingRegistry.get(thingUID);
    }

    public void modifyProperty(String name, String value) {
        updateProperty(name, value);
    }

    public LightifyDeviceDiscoveryService getDiscoveryService() {
        return discoveryService;
    }
}
