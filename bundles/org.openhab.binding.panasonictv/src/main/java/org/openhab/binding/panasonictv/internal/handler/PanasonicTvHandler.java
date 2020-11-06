/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.panasonictv.internal.handler;

import static org.openhab.binding.panasonictv.internal.PanasonicTvBindingConstants.POWER;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jupnp.UpnpService;
import org.jupnp.model.meta.Device;
import org.jupnp.model.meta.LocalDevice;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.registry.Registry;
import org.jupnp.registry.RegistryListener;
import org.openhab.binding.panasonictv.internal.config.PanasonicTvConfiguration;
import org.openhab.binding.panasonictv.internal.discovery.DeviceInformation;
import org.openhab.binding.panasonictv.internal.event.PanasonicEventListener;
import org.openhab.binding.panasonictv.internal.event.PanasonicEventListenerService;
import org.openhab.binding.panasonictv.internal.service.MediaRendererService;
import org.openhab.binding.panasonictv.internal.service.PanasonicTvService;
import org.openhab.binding.panasonictv.internal.service.ServiceFactory;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PanasonicTvHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Prakashbabu Sidaraddi - Initial contribution
 */
@NonNullByDefault
public class PanasonicTvHandler extends BaseThingHandler implements RegistryListener, PanasonicEventListener {
    private Logger logger = LoggerFactory.getLogger(PanasonicTvHandler.class);

    /* Global configuration for Panasonic TV Thing */
    private PanasonicTvConfiguration configuration = new PanasonicTvConfiguration();

    private UpnpIOService upnpIOService;
    private UpnpService upnpService;
    private PanasonicEventListenerService eventListenerService;

    /* Panasonic TV services */
    private final List<PanasonicTvService> services = new CopyOnWriteArrayList<>();

    private boolean powerState = false;

    /* Polling job for searching UPnP devices on startup */
    private @Nullable ScheduledFuture<?> upnpPollingJob;

    public PanasonicTvHandler(Thing thing, UpnpIOService upnpIOService, UpnpService upnpService,
            PanasonicEventListenerService eventListenerService) {
        super(thing);

        this.upnpIOService = upnpIOService;
        this.upnpService = upnpService;
        this.eventListenerService = eventListenerService;
        logger.debug("Create a Panasonic TV Handler for thing '{}'", getThing().getUID());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received channel: {}, command: {}", channelUID, command);

        String channel = channelUID.getId();
        // Delegate command to correct service
        services.stream().filter(service -> service.getSupportedChannelNames().contains(channel)).findAny()
                .ifPresent(service -> service.handleCommand(channel, command));
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.debug("channelLinked: {}", channelUID);

        updateState(POWER, OnOffType.from(powerState));
        services.forEach(PanasonicTvService::clearCache);
    }

    @Override
    public void initialize() {
        configuration = getConfigAs(PanasonicTvConfiguration.class);

        logger.debug("Initializing Panasonic TV handler for uid '{}'", getThing().getUID());
        updateStatus(ThingStatus.UNKNOWN);

        upnpService.getRegistry().getDevices().forEach(this::createService);
        upnpService.getRegistry().addListener(this);
    }

    @Override
    public void dispose() {
        shutdown();
    }

    private void shutdown() {
        ScheduledFuture<?> upnpPollingJob = this.upnpPollingJob;
        if (upnpPollingJob != null) {
            upnpPollingJob.cancel(true);
            this.upnpPollingJob = null;
        }

        upnpService.getRegistry().removeListener(this);
        stopServices();
    }

    @Override
    public void remoteDeviceAdded(@Nullable Registry registry, @Nullable RemoteDevice device) {
        if (device != null) {
            logger.debug("remoteDeviceAdded: device={}", device);
            createService(device);
        }
    }

    @Override
    public void remoteDeviceUpdated(@Nullable Registry registry, @Nullable RemoteDevice device) {
    }

    @Override
    public void remoteDeviceRemoved(@Nullable Registry registry, @Nullable RemoteDevice device) {
        if (device != null) {
            logger.debug("remoteDeviceRemoved: device={}", device);
            removeService(device);
        }
    }

    @Override
    public void localDeviceAdded(@Nullable Registry registry, @Nullable LocalDevice device) {
    }

    @Override
    public void localDeviceRemoved(@Nullable Registry registry, @Nullable LocalDevice device) {
    }

    @Override
    public void beforeShutdown(@Nullable Registry registry) {
    }

    @Override
    public void afterShutdown() {
    }

    @Override
    public void remoteDeviceDiscoveryStarted(@Nullable Registry registry, @Nullable RemoteDevice device) {
    }

    @Override
    public void remoteDeviceDiscoveryFailed(@Nullable Registry registry, @Nullable RemoteDevice device,
            @Nullable Exception ex) {
    }

    private void setThingAndPowerState(boolean powerState) {
        if (this.powerState != powerState) {
            this.powerState = powerState;
            updateState(POWER, OnOffType.from(powerState));
            updateStatus(powerState ? ThingStatus.ONLINE : ThingStatus.OFFLINE);
        }
    }

    @Override
    public void valueReceived(String variable, State value) {
        logger.debug("Received value '{}':'{}' for thing '{}'", variable, value, this.getThing().getUID());
        updateState(variable, value);
        setThingAndPowerState(true);
    }

    @Override
    public void reportError(ThingStatusDetail statusDetail, String message, Throwable e) {
        logger.debug("Error was reported: {}", message, e);
        updateStatus(ThingStatus.OFFLINE, statusDetail, message);
        stopServices();
    }

    /**
     * checks if the device is a RemoteDevice and belongs to this thing
     *
     * @param deviceInformation the device to check
     * @return Optional DeviceInformation (empty if no RemoteDevice or not for us)
     */
    private boolean checkDeviceBelongsToThing(DeviceInformation deviceInformation) {
        logger.debug("Checking modelName={}, udn={}, service={} for thing {}", deviceInformation.modelName,
                deviceInformation.udn, deviceInformation.serviceType, thing.getUID());

        String configHostname = configuration.hostName;
        if (configHostname == null || !configHostname.equals(deviceInformation.host)) {
            logger.debug("Ignoring device={} (hostname not matching) in thing {}", deviceInformation, thing.getUID());
            return false;
        }

        return true;
    }

    /**
     * find a service device in our services
     * 
     * @param deviceInformation the device information of the service to find
     * @return Optional of the service
     */
    private Optional<PanasonicTvService> findService(DeviceInformation deviceInformation) {
        return services.stream().filter(service -> service.getServiceName().equals(deviceInformation.serviceType))
                .findAny();
    }

    private void createService(Device<?, ?, ?> device) {
        // if service exists, clear cache, otherwise try to create and start it
        DeviceInformation.fromDevice(device).filter(this::checkDeviceBelongsToThing).ifPresent(deviceInformation -> {
            logger.trace("Creating service for {}", deviceInformation);
            findService(deviceInformation).ifPresentOrElse(PanasonicTvService::clearCache, () -> {
                ServiceFactory.createService(deviceInformation.serviceType, upnpIOService, upnpService,
                        deviceInformation.udn, configuration.refreshInterval).ifPresent(this::startService);
                if (MediaRendererService.SERVICE_NAME.equalsIgnoreCase(deviceInformation.serviceType)) {
                    eventListenerService.subscribeListener(deviceInformation.udn, this, deviceInformation.host);
                }
            });
        });
    }

    private void removeService(Device<?, ?, ?> device) {
        DeviceInformation.fromDevice(device).filter(this::checkDeviceBelongsToThing)
                .ifPresent(deviceInformation -> findService(deviceInformation).ifPresentOrElse(service -> {
                    logger.trace("Found service, stopping and removing: {}", deviceInformation);
                    stopService(service);
                    if (service.getServiceName().equalsIgnoreCase(MediaRendererService.SERVICE_NAME)) {
                        eventListenerService.unsubscribeListener(deviceInformation.udn);
                    }
                }, () -> {
                    logger.debug("Could not find service in list, but service belongs to this device: {}",
                            deviceInformation);
                }));
    }

    private void startService(PanasonicTvService service) {
        service.addEventListener(this);
        service.start();
        services.add(service);
        setThingAndPowerState(true);
    }

    private void stopService(PanasonicTvService service) {
        logger.trace("Before removing {} the service list size is {}", service, services.size());
        service.stop();
        service.removeEventListener(this);
        services.remove(service);
        logger.trace("After removing {} the service list size is {}", service, services.size());
        if (services.size() == 0) {
            // last service removed, device seems to be offline
            setThingAndPowerState(false);
        }
    }

    private void stopServices() {
        logger.debug("Shutdown all Panasonic TV services");
        services.forEach(this::stopService);
    }
}
