/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.samsungtv.handler;

import static org.openhab.binding.samsungtv.SamsungTvBindingConstants.POWER;
import static org.openhab.binding.samsungtv.config.SamsungTvConfiguration.HOST_NAME;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
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
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.jupnp.UpnpService;
import org.jupnp.model.meta.LocalDevice;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.registry.Registry;
import org.jupnp.registry.RegistryListener;
import org.openhab.binding.samsungtv.config.SamsungTvConfiguration;
import org.openhab.binding.samsungtv.internal.service.ServiceFactory;
import org.openhab.binding.samsungtv.internal.service.api.SamsungTvService;
import org.openhab.binding.samsungtv.internal.service.api.ValueReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SamsungTvHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class SamsungTvHandler extends BaseThingHandler implements DiscoveryListener, RegistryListener, ValueReceiver {

    private Logger logger = LoggerFactory.getLogger(SamsungTvHandler.class);

    /** Polling job for searching UPnP devices on startup */
    private ScheduledFuture<?> upnpPollingJob;

    /** Global configuration for Samsung TV Thing */
    private SamsungTvConfiguration configuration;

    private UpnpIOService upnpIOService;
    private DiscoveryServiceRegistry discoveryServiceRegistry;
    private UpnpService upnpService;

    private ThingUID upnpThingUID = null;

    /** Samsung TV services */
    private List<SamsungTvService> services;

    private boolean powerOn = false;

    public SamsungTvHandler(Thing thing, UpnpIOService upnpIOService, DiscoveryServiceRegistry discoveryServiceRegistry,
            UpnpService upnpService) {

        super(thing);

        logger.debug("Create a Samsung TV Handler for thing '{}'", getThing().getUID());

        if (upnpIOService != null) {
            this.upnpIOService = upnpIOService;
        } else {
            logger.debug("upnpIOService not set.");
        }

        if (discoveryServiceRegistry != null) {
            this.discoveryServiceRegistry = discoveryServiceRegistry;
        }

        if (upnpService != null) {
            this.upnpService = upnpService;
        } else {
            logger.debug("upnpService not set.");
        }

        services = new ArrayList<>();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received channel: {}, command: {}", channelUID, command);

        if (getThing().getStatus() == ThingStatus.ONLINE) {

            // Delegate command to correct service

            String channel = channelUID.getId();

            for (SamsungTvService service : services) {
                if (service != null) {
                    List<String> supportedCommands = service.getSupportedChannelNames();
                    for (String s : supportedCommands) {
                        if (channel.equals(s)) {
                            service.handleCommand(channel, command);
                            return;
                        }
                    }
                }
            }

            logger.warn("Channel '{}' not supported", channelUID);
        } else {
            logger.debug("Samsung TV '{}' is OFFLINE", getThing().getUID());
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.debug("channelLinked: {}", channelUID);

        updateState(new ChannelUID(getThing().getUID(), POWER), getPowerState() ? OnOffType.ON : OnOffType.OFF);

        for (SamsungTvService service : services) {
            if (service != null) {
                service.clearCache();
            }
        }
    }

    private synchronized void updatePowerState(boolean state) {
        powerOn = state;
    }

    private synchronized boolean getPowerState() {
        return powerOn;
    }

    /*
     * One Samsung TV contains several UPnP devices. Samsung TV is discovered by
     * Media Renderer UPnP device. This polling job tries to find another UPnP
     * devices related to same Samsung TV and create handler for those.
     */
    private Runnable scanUPnPDevicesRunnable = new Runnable() {

        @Override
        public void run() {
            checkAndCreateServices();
        }
    };

    @Override
    public void initialize() {
        updateStatus(ThingStatus.OFFLINE);

        configuration = getConfigAs(SamsungTvConfiguration.class);

        logger.debug("Initializing Samsung TV handler for uid '{}'", getThing().getUID());

        if (discoveryServiceRegistry != null) {
            discoveryServiceRegistry.addDiscoveryListener(this);
        }
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
            upnpPollingJob = scheduler.schedule(scanUPnPDevicesRunnable, 0, TimeUnit.MILLISECONDS);
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
            Collection<ThingTypeUID> thingTypeUIDs) {
        return null;
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

    public void putOnline() {
        if (this.thing.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
            updatePowerState(true);
            updateState(new ChannelUID(getThing().getUID(), POWER), OnOffType.ON);
        }
    }

    public synchronized void putOffline() {
        if (this.thing.getStatus() != ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE);
            updateState(new ChannelUID(getThing().getUID(), POWER), OnOffType.OFF);
            updatePowerState(false);
        }
    }

    @Override
    public synchronized void valueReceived(String variable, State value) {
        logger.debug("Received value '{}':'{}' for thing '{}'",
                new Object[] { variable, value, this.getThing().getUID() });

        updateState(new ChannelUID(getThing().getUID(), variable), value);

        if (!getPowerState()) {
            updatePowerState(true);
            updateState(new ChannelUID(getThing().getUID(), POWER), OnOffType.ON);
        }
    }

    private void checkAndCreateServices() {
        logger.debug("Check and create missing UPnP services");
        Iterator<?> itr = upnpService.getRegistry().getDevices().iterator();

        while (itr.hasNext()) {
            RemoteDevice device = (RemoteDevice) itr.next();
            createService(device);
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

                SamsungTvService service = findServiceInstance(type);
                if (service == null) {
                    SamsungTvService newService = ServiceFactory.createService(type, upnpIOService, udn,
                            configuration.refreshInterval, configuration.hostName, configuration.port);

                    if (newService != null) {
                        startService(newService);
                        services.add(newService);
                    }
                } else {
                    logger.debug("Device rediscovered, clear caches");
                    service.clearCache();
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
        Class<?> cl = ServiceFactory.getClassByServiceName(serviceName);

        if (cl != null) {
            for (SamsungTvService service : services) {
                if (service != null) {
                    if (service.getClass() == cl) {
                        return service;
                    }
                }
            }
        }
        return null;
    }

    private void startService(SamsungTvService service) {
        if (service != null) {
            service.addEventListener(this);
            service.start();
        }
    }

    private void stopService(SamsungTvService service) {
        if (service != null) {
            service.stop();
            service.removeEventListener(this);
            service = null;
        }
    }

    private void stopServices() {
        logger.debug("Shutdown all UPnP services");
        for (SamsungTvService service : services) {
            stopService(service);
        }
        services.clear();
    }
}
