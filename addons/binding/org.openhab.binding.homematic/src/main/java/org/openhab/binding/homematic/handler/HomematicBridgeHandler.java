/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.handler;

import java.io.IOException;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
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
import org.openhab.binding.homematic.internal.common.HomematicConfig;
import org.openhab.binding.homematic.internal.communicator.HomematicGateway;
import org.openhab.binding.homematic.internal.communicator.HomematicGatewayAdapter;
import org.openhab.binding.homematic.internal.communicator.HomematicGatewayFactory;
import org.openhab.binding.homematic.internal.discovery.HomematicDeviceDiscoveryService;
import org.openhab.binding.homematic.internal.misc.HomematicClientException;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDatapointConfig;
import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.binding.homematic.internal.type.HomematicTypeGenerator;
import org.openhab.binding.homematic.internal.type.UidUtils;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link HomematicBridgeHandler} is the handler for a Homematic gateway and connects it to the framework.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class HomematicBridgeHandler extends BaseBridgeHandler implements HomematicGatewayAdapter {
    private final Logger logger = LoggerFactory.getLogger(HomematicBridgeHandler.class);
    private static final long REINITIALIZE_DELAY_SECONDS = 10;
    private static SimplePortPool portPool = new SimplePortPool();

    private HomematicConfig config;
    private HomematicGateway gateway;
    private HomematicTypeGenerator typeGenerator;

    private HomematicDeviceDiscoveryService discoveryService;
    private ServiceRegistration<?> discoveryServiceRegistration;

    private String ipv4Address;

    public HomematicBridgeHandler(@NonNull Bridge bridge, HomematicTypeGenerator typeGenerator, String ipv4Address) {
        super(bridge);
        this.typeGenerator = typeGenerator;
        this.ipv4Address = ipv4Address;
    }

    @Override
    public void initialize() {
        config = createHomematicConfig();
        registerDeviceDiscoveryService();
        final HomematicBridgeHandler instance = this;
        scheduler.execute(() -> {
            try {
                String id = getThing().getUID().getId();
                gateway = HomematicGatewayFactory.createGateway(id, config, instance);
                gateway.initialize();

                discoveryService.startScan(null);
                discoveryService.waitForScanFinishing();
                updateStatus(ThingStatus.ONLINE);
                if (!config.getGatewayInfo().isHomegear()) {
                    try {
                        gateway.loadRssiValues();
                    } catch (IOException ex) {
                        logger.warn("Unable to load RSSI values from bridge '{}'", getThing().getUID().getId());
                        logger.error("{}", ex.getMessage(), ex);
                    }
                }
                gateway.startWatchdogs();
            } catch (IOException ex) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
                dispose();
                scheduleReinitialize();
            }
        });
    }

    /**
     * Schedules a reinitialization, if the Homematic gateway is not reachable at bridge startup.
     */
    private void scheduleReinitialize() {
        scheduler.schedule(() -> {
            initialize();
        }, REINITIALIZE_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing bridge '{}'", getThing().getUID().getId());
        super.dispose();
        if (discoveryService != null) {
            discoveryService.stopScan();
            unregisterDeviceDiscoveryService();
        }
        if (gateway != null) {
            gateway.dispose();
        }
        if (config != null) {
            portPool.release(config.getXmlCallbackPort());
            portPool.release(config.getBinCallbackPort());
        }
    }

    /**
     * Registers the DeviceDiscoveryService.
     */
    private void registerDeviceDiscoveryService() {
        if (bundleContext != null) {
            logger.trace("Registering HomematicDeviceDiscoveryService for bridge '{}'", getThing().getUID().getId());
            discoveryService = new HomematicDeviceDiscoveryService(this);
            discoveryServiceRegistration = bundleContext.registerService(DiscoveryService.class.getName(),
                    discoveryService, new Hashtable<String, Object>());
            discoveryService.activate();
        }
    }

    /**
     * Unregisters the DeviceDisoveryService.
     */
    private void unregisterDeviceDiscoveryService() {
        if (discoveryServiceRegistration != null && bundleContext != null) {
            HomematicDeviceDiscoveryService service = (HomematicDeviceDiscoveryService) bundleContext
                    .getService(discoveryServiceRegistration.getReference());
            if (service != null) {
                service.deactivate();
            }

            discoveryServiceRegistration.unregister();
            discoveryServiceRegistration = null;
            discoveryService = null;
        }
    }

    /**
     * Sets the OFFLINE status for all things of this bridge that has been removed from the gateway.
     */
    public void setOfflineStatus() {
        for (Thing hmThing : getThing().getThings()) {
            try {
                gateway.getDevice(UidUtils.getHomematicAddress(hmThing));
            } catch (HomematicClientException e) {
                if (hmThing.getHandler() != null) {
                    ((HomematicThingHandler) hmThing.getHandler()).handleRemoval();
                }
            }
        }
    }

    /**
     * Creates the configuration for the HomematicGateway.
     */
    private HomematicConfig createHomematicConfig() {
        HomematicConfig homematicConfig = getThing().getConfiguration().as(HomematicConfig.class);
        if (homematicConfig.getCallbackHost() == null) {
            homematicConfig.setCallbackHost(this.ipv4Address);
        }
        if (homematicConfig.getXmlCallbackPort() == 0) {
            homematicConfig.setXmlCallbackPort(portPool.getNextPort());
        } else {
            portPool.setInUse(homematicConfig.getXmlCallbackPort());
        }
        if (homematicConfig.getBinCallbackPort() == 0) {
            homematicConfig.setBinCallbackPort(portPool.getNextPort());
        } else {
            portPool.setInUse(homematicConfig.getBinCallbackPort());
        }
        logger.debug("{}", homematicConfig);
        return homematicConfig;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH == command) {
            logger.debug("Refreshing bridge '{}'", getThing().getUID().getId());
            reloadAllDeviceValues();
        }
    }

    /**
     * Returns the TypeGenerator.
     */
    public HomematicTypeGenerator getTypeGenerator() {
        return typeGenerator;
    }

    /**
     * Returns the HomematicGateway.
     */
    public HomematicGateway getGateway() {
        return gateway;
    }

    /**
     * Updates the thing for the given Homematic device.
     */
    private void updateThing(HmDevice device) {
        Thing hmThing = getThingByUID(UidUtils.generateThingUID(device, getThing()));
        if (hmThing != null && hmThing.getHandler() != null) {
            HomematicThingHandler thingHandler = (HomematicThingHandler) hmThing.getHandler();
            thingHandler.thingUpdated(hmThing);
            for (Channel channel : hmThing.getChannels()) {
                thingHandler.handleRefresh(channel.getUID());
            }
        }
    }

    @Override
    public void onStateUpdated(HmDatapoint dp) {
        Thing hmThing = getThingByUID(UidUtils.generateThingUID(dp.getChannel().getDevice(), getThing()));
        if (hmThing != null && hmThing.getHandler() != null) {
            final ThingStatus status = hmThing.getStatus();
            if (status == ThingStatus.ONLINE || status == ThingStatus.OFFLINE) {
                HomematicThingHandler thingHandler = (HomematicThingHandler) hmThing.getHandler();
                thingHandler.updateDatapointState(dp);
            }
        }
    }

    @Override
    public HmDatapointConfig getDatapointConfig(HmDatapoint dp) {
        Thing hmThing = getThingByUID(UidUtils.generateThingUID(dp.getChannel().getDevice(), getThing()));
        if (hmThing != null && hmThing.getHandler() != null) {
            HomematicThingHandler thingHandler = (HomematicThingHandler) hmThing.getHandler();
            return thingHandler.getChannelConfig(dp);
        }
        return new HmDatapointConfig();
    }

    @Override
    public void onNewDevice(HmDevice device) {
        onDeviceLoaded(device);
        updateThing(device);
    }

    @Override
    public void onDeviceDeleted(HmDevice device) {
        discoveryService.deviceRemoved(device);
        updateThing(device);
    }

    @Override
    public void onConnectionLost() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Connection lost");
    }

    @Override
    public void onConnectionResumed() {
        updateStatus(ThingStatus.ONLINE);
        reloadAllDeviceValues();
    }

    @Override
    public void onDeviceLoaded(HmDevice device) {
        typeGenerator.generate(device);
        if (discoveryService != null) {
            discoveryService.deviceDiscovered(device);
        }
    }

    @Override
    public void reloadDeviceValues(HmDevice device) {
        updateThing(device);
        if (device.isGatewayExtras()) {
            typeGenerator.generate(device);
        }
    }

    @Override
    public void reloadAllDeviceValues() {
        for (Thing hmThing : getThing().getThings()) {
            try {
                HmDevice device = gateway.getDevice(UidUtils.getHomematicAddress(hmThing));
                gateway.triggerDeviceValuesReload(device);
            } catch (HomematicClientException ex) {
                logger.warn("{}", ex.getMessage());
            }
        }
    }

}
