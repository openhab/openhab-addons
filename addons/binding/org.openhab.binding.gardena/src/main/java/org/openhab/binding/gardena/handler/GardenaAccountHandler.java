/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.gardena.internal.GardenaSmart;
import org.openhab.binding.gardena.internal.GardenaSmartEventListener;
import org.openhab.binding.gardena.internal.GardenaSmartImpl;
import org.openhab.binding.gardena.internal.config.GardenaConfig;
import org.openhab.binding.gardena.internal.discovery.GardenaDeviceDiscoveryService;
import org.openhab.binding.gardena.internal.exception.GardenaException;
import org.openhab.binding.gardena.internal.model.Device;
import org.openhab.binding.gardena.internal.util.UidUtils;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GardenaAccountHandler} is the handler for a Gardena Smart Home access and connects it to the framework.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class GardenaAccountHandler extends BaseBridgeHandler implements GardenaSmartEventListener {

    private final Logger logger = LoggerFactory.getLogger(GardenaAccountHandler.class);
    private static final long REINITIALIZE_DELAY_SECONDS = 10;

    private GardenaDeviceDiscoveryService discoveryService;
    private ServiceRegistration<?> discoveryServiceRegistration;

    private GardenaSmart gardenaSmart = new GardenaSmartImpl();
    private GardenaConfig gardenaConfig;

    public GardenaAccountHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Gardena account '{}'", getThing().getUID().getId());

        gardenaConfig = getThing().getConfiguration().as(GardenaConfig.class);
        logger.debug("{}", gardenaConfig);

        initializeGardena();
    }

    /**
     * Initializes the GardenaSmart account.
     */
    private void initializeGardena() {
        final GardenaAccountHandler instance = this;
        scheduler.execute(() -> {
            try {
                String id = getThing().getUID().getId();
                gardenaSmart.init(id, gardenaConfig, instance, scheduler);
                registerDeviceDiscoveryService();
                discoveryService.startScan(null);
                discoveryService.waitForScanFinishing();
                updateStatus(ThingStatus.ONLINE);
            } catch (GardenaException ex) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
                disposeGardena();
                scheduleReinitialize();
                logger.debug("{}", ex.getMessage(), ex);
            }
        });
    }

    /**
     * Schedules a reinitialization, if Gardea Smart Home account is not reachable at startup.
     */
    private void scheduleReinitialize() {
        scheduler.schedule(() -> {
            initializeGardena();
        }, REINITIALIZE_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        super.dispose();
        disposeGardena();
    }

    /**
     * Disposes the GardenaSmart account.
     */
    private void disposeGardena() {
        logger.debug("Disposing Gardena account '{}'", getThing().getUID().getId());

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
            if (bundleContext != null) {
                GardenaDeviceDiscoveryService service = (GardenaDeviceDiscoveryService) bundleContext
                        .getService(discoveryServiceRegistration.getReference());
                service.deactivate();
            }
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

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH == command) {
            logger.debug("Refreshing Gardena account '{}'", getThing().getUID().getId());
            disposeGardena();
            initializeGardena();
        }
    }

    @Override
    public void onDeviceUpdated(Device device) {
        for (ThingUID thingUID : UidUtils.getThingUIDs(device, getThing())) {
            Thing gardenaThing = getThingByUID(thingUID);
            try {
                GardenaThingHandler gardenaThingHandler = (GardenaThingHandler) gardenaThing.getHandler();
                gardenaThingHandler.updateProperties(device);
                for (Channel channel : gardenaThing.getChannels()) {
                    gardenaThingHandler.updateChannel(channel.getUID());
                }
                gardenaThingHandler.updateSettings(device);
                gardenaThingHandler.updateStatus(device);
            } catch (GardenaException ex) {
                logger.error("There is something wrong with your thing '{}', please check or recreate it: {}",
                        gardenaThing.getUID(), ex.getMessage());
                logger.debug("Gardena exception caught on device update.", ex);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, ex.getMessage());
            } catch (AccountHandlerNotAvailableException ignore) {
            }
        }
    }

    @Override
    public void onNewDevice(Device device) {
        if (discoveryService != null) {
            discoveryService.deviceDiscovered(device);
        }
        onDeviceUpdated(device);
    }

    @Override
    public void onDeviceDeleted(Device device) {
        if (discoveryService != null) {
            discoveryService.deviceRemoved(device);
        }
    }

    @Override
    public void onConnectionLost() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Connection lost");
    }

    @Override
    public void onConnectionResumed() {
        updateStatus(ThingStatus.ONLINE);
    }

}
