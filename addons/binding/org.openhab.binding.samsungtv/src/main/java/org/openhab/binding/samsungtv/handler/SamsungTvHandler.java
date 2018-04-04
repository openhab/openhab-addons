/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.samsungtv.handler;

import static org.openhab.binding.samsungtv.SamsungTvBindingConstants.POWER;
import static org.openhab.binding.samsungtv.internal.config.SamsungTvConfiguration.HOST_NAME;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.DiscoveryListener;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceRegistry;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.jupnp.UpnpService;
import org.jupnp.model.meta.Device;
import org.jupnp.model.meta.LocalDevice;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.registry.Registry;
import org.jupnp.registry.RegistryListener;
import org.openhab.binding.samsungtv.internal.config.SamsungTvConfiguration;
import org.openhab.binding.samsungtv.internal.service.RemoteControllerService;
import org.openhab.binding.samsungtv.internal.service.ServiceFactory;
import org.openhab.binding.samsungtv.internal.service.api.EventListener;
import org.openhab.binding.samsungtv.internal.service.api.SamsungTvService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SamsungTvHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Pauli Anttila - Initial contribution
 * @author Martin van Wingerden - Some changes for non-UPnP configured devices
 */
public class SamsungTvHandler extends BaseThingHandler implements DiscoveryListener, RegistryListener, EventListener {

    private Logger logger = LoggerFactory.getLogger(SamsungTvHandler.class);

    /* Global configuration for Samsung TV Thing */
    private SamsungTvConfiguration configuration;

    private UpnpIOService upnpIOService;
    private DiscoveryServiceRegistry discoveryServiceRegistry;
    private UpnpService upnpService;

    private ThingUID upnpThingUID = null;

    /* Samsung TV services */
    private final List<SamsungTvService> services = new CopyOnWriteArrayList<>();

    private boolean powerState = false;

    /* Polling job for searching UPnP devices on startup */
    private ScheduledFuture<?> upnpPollingJob;

    /* Polling job for non-UPnP remote controller */
    private ScheduledFuture<?> nonUpnpRemoteControllerJob;

    public SamsungTvHandler(Thing thing, UpnpIOService upnpIOService, DiscoveryServiceRegistry discoveryServiceRegistry,
            UpnpService upnpService) {
        super(thing);

        logger.debug("Create a Samsung TV Handler for thing '{}'", getThing().getUID());

        if (upnpIOService != null) {
            this.upnpIOService = upnpIOService;
        } else {
            logger.debug("upnpIOService not set.");
        }

        if (upnpService != null) {
            this.upnpService = upnpService;
        } else {
            logger.debug("upnpService not set.");
        }

        if (discoveryServiceRegistry != null) {
            this.discoveryServiceRegistry = discoveryServiceRegistry;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received channel: {}, command: {}", channelUID, command);

        String channel = channelUID.getId();

        // Delegate command to correct service
        for (SamsungTvService service : services) {
            for (String s : service.getSupportedChannelNames()) {
                if (channel.equals(s)) {
                    service.handleCommand(channel, command);
                    return;
                }
            }
        }

        logger.warn("Channel '{}' not supported", channelUID);
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.debug("channelLinked: {}", channelUID);

        updateState(POWER, getPowerState() ? OnOffType.ON : OnOffType.OFF);

        for (SamsungTvService service : services) {
            if (service != null) {
                service.clearCache();
            }
        }
    }

    private synchronized void setPowerState(boolean state) {
        powerState = state;
    }

    private synchronized boolean getPowerState() {
        return powerState;
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.OFFLINE);

        configuration = getConfigAs(SamsungTvConfiguration.class);

        logger.debug("Initializing Samsung TV handler for uid '{}'", getThing().getUID());

        if (discoveryServiceRegistry != null) {
            discoveryServiceRegistry.addDiscoveryListener(this);
        }

        nonUpnpRemoteControllerJob = scheduler.scheduleWithFixedDelay(this::checkCreateManualConnection, 1, 1,
                TimeUnit.MINUTES);
    }

    @Override
    public void dispose() {
        if (discoveryServiceRegistry != null) {
            discoveryServiceRegistry.removeDiscoveryListener(this);
        }
        shutdown();
    }

    private void shutdown() {
        if (upnpPollingJob != null && !upnpPollingJob.isCancelled()) {
            upnpPollingJob.cancel(true);
            upnpPollingJob = null;
        }
        if (nonUpnpRemoteControllerJob != null && !nonUpnpRemoteControllerJob.isCancelled()) {
            nonUpnpRemoteControllerJob.cancel(true);
            nonUpnpRemoteControllerJob = null;
        }

        if (upnpService != null) {
            upnpService.getRegistry().removeListener(this);
        }

        stopServices();
    }

    @Override
    public void thingDiscovered(DiscoveryService source, DiscoveryResult result) {
        logger.debug("thingDiscovered: {}", result);

        if (configuration.hostName.equals(result.getProperties().get(HOST_NAME))) {
            /*
             * SamsungTV discovery services creates thing UID from UPnP UDN.
             * When thing is generated manually, thing UID may not match UPnP UDN, so store it for later use (e.g.
             * thingRemoved).
             */
            upnpThingUID = result.getThingUID();
            logger.debug("thingDiscovered, thingUID={}, discoveredUID={}", this.getThing().getUID(), upnpThingUID);
            upnpPollingJob = scheduler.schedule(this::checkAndCreateServices, 0, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void thingRemoved(DiscoveryService source, ThingUID thingUID) {
        logger.debug("thingRemoved: {}", thingUID);

        if (thingUID.equals(upnpThingUID)) {
            shutdown();
            putOffline();
        }
    }

    @Override
    public Collection<ThingUID> removeOlderResults(DiscoveryService source, long timestamp,
            Collection<ThingTypeUID> thingTypeUIDs, ThingUID bridgeUID) {
        return Collections.emptyList();
    }

    @Override
    public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
        logger.debug("remoteDeviceAdded: device={}", device);
        createService(device);
    }

    @Override
    public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
    }

    @Override
    public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
        logger.debug("remoteDeviceRemoved: device={}", device);
    }

    @Override
    public void localDeviceAdded(Registry registry, LocalDevice device) {
    }

    @Override
    public void localDeviceRemoved(Registry registry, LocalDevice device) {
    }

    @Override
    public void beforeShutdown(Registry registry) {
    }

    @Override
    public void afterShutdown() {
    }

    @Override
    public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
    }

    @Override
    public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {
    }

    private void putOnline() {
        setPowerState(true);
        updateStatus(ThingStatus.ONLINE);
        updateState(POWER, OnOffType.ON);
    }

    private synchronized void putOffline() {
        setPowerState(false);
        updateStatus(ThingStatus.OFFLINE);
        updateState(POWER, OnOffType.OFF);
    }

    @Override
    public synchronized void valueReceived(String variable, State value) {
        logger.debug("Received value '{}':'{}' for thing '{}'", variable, value, this.getThing().getUID());

        updateState(variable, value);
        updateState(POWER, OnOffType.ON);
        setPowerState(true);
    }

    @Override
    public void reportError(ThingStatusDetail statusDetail, String message, Throwable e) {
        logger.info("Error was reported: {}", message, e);
        updateStatus(ThingStatus.OFFLINE, statusDetail, message);
        stopServices();
    }

    /**
     * One Samsung TV contains several UPnP devices. Samsung TV is discovered by
     * Media Renderer UPnP device. This polling job tries to find another UPnP
     * devices related to same Samsung TV and create handler for those.
     */
    private void checkAndCreateServices() {
        logger.debug("Check and create missing UPnP services");

        for (Device device : upnpService.getRegistry().getDevices()) {
            createService((RemoteDevice) device);
        }

        if (upnpService != null) {
            upnpService.getRegistry().addListener(this);
        }
    }

    private synchronized void createService(RemoteDevice device) {
        if (configuration != null) {
            if (configuration.hostName.equals(device.getIdentity().getDescriptorURL().getHost())) {
                String modelName = device.getDetails().getModelDetails().getModelName();
                String udn = device.getIdentity().getUdn().getIdentifierString();
                String type = device.getType().getType();

                logger.debug(" modelName={}, udn={}, type={}", modelName, udn, type);

                SamsungTvService existingService = findServiceInstance(type);

                if (existingService == null || !existingService.isUpnp()) {
                    SamsungTvService newService = ServiceFactory.createService(type, upnpIOService, udn,
                            configuration.refreshInterval, configuration.hostName, configuration.port);

                    if (newService != null) {
                        if (existingService != null) {
                            stopService(existingService);
                        }

                        startService(newService);
                    }
                } else {
                    logger.debug("Device rediscovered, clear caches");
                    existingService.clearCache();
                }
                putOnline();
            } else {
                logger.debug("Ignore device={}", device);
            }
        } else {
            logger.debug("Thing not yet initialized");
        }
    }

    private SamsungTvService findServiceInstance(String serviceName) {
        Class<? extends SamsungTvService> cl = ServiceFactory.getClassByServiceName(serviceName);

        for (SamsungTvService service : services) {
            if (service.getClass() == cl) {
                return service;
            }
        }
        return null;
    }

    private void checkCreateManualConnection() {
        try {
            if (services.isEmpty()) {
                RemoteControllerService service = RemoteControllerService.createNonUpnpService(configuration.hostName,
                        configuration.port);

                if (service.checkConnection()) {
                    startService(service);
                    putOnline();
                } else {
                    stopService(service);
                }
            } else {
                logger.trace("One or more services are already registered, not checking for new ones");
            }
        } catch (RuntimeException e) {
            logger.warn("Catching all exceptions because otherwise the thread would silently fail", e);
        }
    }

    private void startService(SamsungTvService service) {
        if (service != null) {
            service.addEventListener(this);
            service.start();
            services.add(service);
        }
    }

    private void stopService(SamsungTvService service) {
        if (service != null) {
            service.stop();
            service.removeEventListener(this);
            services.remove(service);
        }
    }

    private void stopServices() {
        logger.debug("Shutdown all Samsung services");
        for (SamsungTvService service : services) {
            stopService(service);
        }
        services.clear();
    }
}
