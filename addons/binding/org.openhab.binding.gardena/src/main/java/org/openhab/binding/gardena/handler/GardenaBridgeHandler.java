/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gardena.handler;

import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.gardena.discovery.GardenaDeviceDiscoveryService;
import org.openhab.binding.gardena.internal.GardenaSmart;
import org.openhab.binding.gardena.internal.GardenaSmartEventListener;
import org.openhab.binding.gardena.internal.GardenaSmartImpl;
import org.openhab.binding.gardena.internal.config.GardenaConfig;
import org.openhab.binding.gardena.internal.exception.GardenaException;
import org.openhab.binding.gardena.internal.model.Device;
import org.openhab.binding.gardena.util.UidUtils;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GardenaBridgeHandler} is the handler for a Gardena Smart Home access and connects it to the framework.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class GardenaBridgeHandler extends BaseBridgeHandler implements GardenaSmartEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(GardenaBridgeHandler.class);
    private static final long REINITIALIZE_DELAY_SECONDS = 10;

    private GardenaDeviceDiscoveryService discoveryService;
    private ServiceRegistration<?> discoveryServiceRegistration;

    private GardenaSmart gardenaSmart = new GardenaSmartImpl();

    public GardenaBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        try {
            LOGGER.debug("Initializing bridge '{}'", getThing().getUID().getId());

            GardenaConfig gardenaConfig = getThing().getConfiguration().as(GardenaConfig.class);
            LOGGER.debug(gardenaConfig.toString());

            String id = getThing().getUID().getId();
            gardenaSmart.init(id, gardenaConfig, this);
            registerDeviceDiscoveryService();
            updateStatus(ThingStatus.ONLINE);

            for (Thing thing : getThing().getThings()) {
                thing.getHandler().thingUpdated(thing);
            }

        } catch (GardenaException ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
            dispose();
            scheduleReinitialize();
            LOGGER.debug(ex.getMessage(), ex);
        }
    }

    /**
     * Schedules a reinitialization, if Gardea Smart Home is not reachable at bridge startup.
     */
    private void scheduleReinitialize() {
        scheduler.schedule(new Runnable() {

            @Override
            public void run() {
                initialize();
            }
        }, REINITIALIZE_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        LOGGER.debug("Disposing bridge '{}'", getThing().getUID().getId());
        super.dispose();

        if (discoveryService != null) {
            discoveryService.stopScan();
            unregisterDeviceDiscoveryService();
        }

        gardenaSmart.dispose();
    }

    /**
     * Registers the Gardena DeviceDiscoveryService.
     */
    private void registerDeviceDiscoveryService() {
        discoveryService = new GardenaDeviceDiscoveryService(this);
        discoveryServiceRegistration = bundleContext.registerService(DiscoveryService.class.getName(), discoveryService,
                new Hashtable<String, Object>());
        discoveryService.activate();
    }

    /**
     * Unregisters the Gardena DeviceDisoveryService.
     */
    private void unregisterDeviceDiscoveryService() {
        if (discoveryServiceRegistration != null) {
            GardenaDeviceDiscoveryService service = (GardenaDeviceDiscoveryService) bundleContext
                    .getService(discoveryServiceRegistration.getReference());
            service.deactivate();

            discoveryServiceRegistration.unregister();
            discoveryServiceRegistration = null;
            discoveryService = null;
        }
    }

    /**
     * Returns the Gardena Smart Home implementation.
     */
    public GardenaSmart getGardenaSmart() {
        return gardenaSmart;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH == command) {
            LOGGER.debug("Refreshing bridge '{}'", getThing().getUID().getId());
            dispose();
            initialize();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDeviceUpdated(Device device) {
        try {
            Thing gardenaThing = getThingByUID(UidUtils.generateThingUID(device, getThing()));
            if (gardenaThing != null) {
                GardenaThingHandler gardenaThingHandler = (GardenaThingHandler) gardenaThing.getHandler();
                gardenaThingHandler.updateProperties(device);
                for (Channel channel : gardenaThing.getChannels()) {
                    gardenaThingHandler.updateChannel(channel.getUID());
                }
            }
        } catch (GardenaException ex) {
            LOGGER.error(ex.getMessage(), ex);
        } catch (BridgeHandlerNotAvailableException ex) {
            // ignore
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNewDevice(Device device) {
        if (discoveryService != null) {
            discoveryService.deviceDiscovered(device);
        }
        onDeviceUpdated(device);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDeviceDeleted(Device device) {
        if (discoveryService != null) {
            discoveryService.deviceRemoved(device);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConnectionLost() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Connection lost");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConnectionResumed() {
        updateStatus(ThingStatus.ONLINE);
    }

}
