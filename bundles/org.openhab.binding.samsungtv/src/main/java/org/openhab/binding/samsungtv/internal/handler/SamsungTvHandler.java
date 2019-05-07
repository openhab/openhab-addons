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
package org.openhab.binding.samsungtv.internal.handler;

import static org.openhab.binding.samsungtv.internal.SamsungTvBindingConstants.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryListener;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceRegistry;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.io.net.http.WebSocketFactory;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.jupnp.UpnpService;
import org.jupnp.model.meta.Device;
import org.jupnp.model.meta.RemoteDevice;
import org.openhab.binding.samsungtv.internal.WakeOnLanUtility;
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
 * @author Arjan Mels - Remove RegistryListener, manually create RemoteService in all circumstances, add sending of WOL
 *         package to power on TV
 */
@NonNullByDefault
public class SamsungTvHandler extends BaseThingHandler implements DiscoveryListener, EventListener {

    private static final int WOL_PACKET_RETRY_COUNT = 10;
    private static final int WOL_SERVICE_CHECK_COUNT = 30;

    private Logger logger = LoggerFactory.getLogger(SamsungTvHandler.class);

    private UpnpIOService upnpIOService;
    private DiscoveryServiceRegistry discoveryServiceRegistry;
    private UpnpService upnpService;
    private WebSocketFactory webSocketFactory;

    private @Nullable ThingUID upnpThingUID = null;

    /* Samsung TV services */
    private final Set<SamsungTvService> services = new CopyOnWriteArraySet<>();

    /* Store powerState to be able to restore upon new link */
    private boolean powerState = false;

    /* Store if art mode is supported to be able to skip switching power state to ON during initialization */
    boolean artModeIsSupported = false;

    public SamsungTvHandler(Thing thing, UpnpIOService upnpIOService, DiscoveryServiceRegistry discoveryServiceRegistry,
            UpnpService upnpService, WebSocketFactory webSocketFactory) {
        super(thing);

        logger.debug("Create a Samsung TV Handler for thing '{}'", getThing().getUID());

        this.upnpIOService = upnpIOService;
        this.upnpService = upnpService;
        this.discoveryServiceRegistry = discoveryServiceRegistry;
        this.webSocketFactory = webSocketFactory;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received channel: {}, command: {}", channelUID, command);

        String channel = channelUID.getId();

        // if power on command try WOL for good measure:
        if ((channel.equals(POWER) || channel.equals(ART_MODE)) && OnOffType.ON.equals(command)) {
            sendWOLandResendCommand(channel, command);
        }

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
        logger.trace("channelLinked: {}", channelUID);

        updateState(POWER, getPowerState() ? OnOffType.ON : OnOffType.OFF);

        for (SamsungTvService service : services) {
            service.clearCache();
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
        updateStatus(ThingStatus.UNKNOWN);

        logger.debug("Initializing Samsung TV handler for uid '{}'", getThing().getUID());

        discoveryServiceRegistry.addDiscoveryListener(this);

        checkAndCreateServices();

        SamsungTvConfiguration configuration = getConfigAs(SamsungTvConfiguration.class);
        if (StringUtils.isEmpty(configuration.macAddress) && configuration.hostName != null) {
            String macAddress = WakeOnLanUtility.getMACAddress(configuration.hostName);
            if (macAddress != null) {
                getConfig().put(SamsungTvConfiguration.MAC_ADDRESS, macAddress);
            }
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing SamsungTvHandler");

        discoveryServiceRegistry.removeDiscoveryListener(this);
        shutdown();
        putOffline();
    }

    private void shutdown() {
        logger.debug("Shutdown all Samsung services");
        for (SamsungTvService service : services) {
            stopService(service);
        }
        services.clear();
    }

    private synchronized void putOnline() {
        setPowerState(true);
        updateStatus(ThingStatus.ONLINE);

        if (!artModeIsSupported) {
            updateState(POWER, OnOffType.ON);
        }
    }

    private synchronized void putOffline() {
        setPowerState(false);
        updateStatus(ThingStatus.OFFLINE);
        updateState(ART_MODE, OnOffType.OFF);
        updateState(POWER, OnOffType.OFF);
        updateState(SOURCE_APP, new StringType(""));
    }

    @Override
    public synchronized void valueReceived(String variable, State value) {
        logger.debug("Received value '{}':'{}' for thing '{}'", variable, value, this.getThing().getUID());

        if (POWER.equals(variable)) {
            setPowerState(OnOffType.ON.equals(value));
        } else if (ART_MODE.equals(variable)) {
            artModeIsSupported = true;
        }
        updateState(variable, value);
    }

    @Override
    public void reportError(@Nullable ThingStatusDetail statusDetail, @Nullable String message, @Nullable Throwable e) {
        logger.info("Error was reported: {}", message, e);
    }

    /**
     * One Samsung TV contains several UPnP devices. Samsung TV is discovered by
     * Media Renderer UPnP device. This function tries to find another UPnP
     * devices related to same Samsung TV and create handler for those.
     */
    private void checkAndCreateServices() {
        logger.debug("Check and create missing UPnP services");

        for (Device<?, ?, ?> device : upnpService.getRegistry().getDevices()) {
            createService((RemoteDevice) device);
        }

        checkCreateManualConnection();
    }

    private synchronized void createService(RemoteDevice device) {
        SamsungTvConfiguration configuration = getConfigAs(SamsungTvConfiguration.class);
        if (configuration.hostName != null
                && configuration.hostName.equals(device.getIdentity().getDescriptorURL().getHost())) {
            String modelName = device.getDetails().getModelDetails().getModelName();
            String udn = device.getIdentity().getUdn().getIdentifierString();
            String type = device.getType().getType();

            SamsungTvService existingService = findServiceInstance(type);

            if (existingService == null || !existingService.isUpnp()) {
                SamsungTvService newService = ServiceFactory.createService(type, upnpIOService, udn,
                        configuration.refreshInterval, configuration.hostName, configuration.port);

                if (newService != null) {
                    if (existingService != null) {
                        stopService(existingService);
                        startService(newService);
                        logger.debug("Restarting service in UPnP mode for: {}, {} ({})", modelName, type, udn);
                    } else {
                        startService(newService);
                        logger.debug("Started service for: {}, {} ({})", modelName, type, udn);
                    }
                } else {
                    logger.trace("Skipping unknown UPnP service: {}, {} ({})", modelName, type, udn);
                }
            } else {
                logger.debug("Service rediscovered, clearing caches: {}, {} ({})", modelName, type, udn);
                existingService.clearCache();
            }
            putOnline();
        }
    }

    private @Nullable SamsungTvService findServiceInstance(String serviceName) {
        Class<? extends SamsungTvService> cl = ServiceFactory.getClassByServiceName(serviceName);

        for (SamsungTvService service : services) {
            if (service.getClass() == cl) {
                return service;
            }
        }
        return null;
    }

    private synchronized void checkCreateManualConnection() {
        try {
            // create remote service manually if it does not yet exist

            RemoteControllerService service = (RemoteControllerService) findServiceInstance(
                    RemoteControllerService.SERVICE_NAME);
            if (service == null) {
                putOffline();
                SamsungTvConfiguration configuration = getConfigAs(SamsungTvConfiguration.class);
                service = RemoteControllerService.createNonUpnpService(configuration.hostName, configuration.port);
                startService(service);
            } else {
                // open connection again if needed
                if (!service.checkConnection()) {
                    putOffline();
                    service.start();
                }
            }
        } catch (RuntimeException e) {
            logger.warn("Catching all exceptions because otherwise the thread would silently fail", e);
        }
    }

    private synchronized void startService(SamsungTvService service) {
        service.addEventListener(this);
        service.start();
        services.add(service);
    }

    private synchronized void stopService(SamsungTvService service) {
        service.stop();
        service.removeEventListener(this);
        services.remove(service);
    }

    @Override
    public void thingDiscovered(DiscoveryService source, DiscoveryResult result) {
        SamsungTvConfiguration configuration = getConfigAs(SamsungTvConfiguration.class);

        if (configuration.hostName.equals(result.getProperties().get(SamsungTvConfiguration.HOST_NAME))) {
            logger.debug("thingDiscovered: {}, {}", result.getProperties().get(SamsungTvConfiguration.HOST_NAME),
                    result);
            /*
             * SamsungTV discovery services creates thing UID from UPnP UDN.
             * When thing is generated manually, thing UID may not match UPnP UDN, so store it for later use (e.g.
             * thingRemoved).
             */
            upnpThingUID = result.getThingUID();
            logger.debug("thingDiscovered, thingUID={}, discoveredUID={}", this.getThing().getUID(), upnpThingUID);
            checkAndCreateServices();
        }
    }

    @Override
    public void thingRemoved(DiscoveryService source, ThingUID thingUID) {
        if (thingUID.equals(upnpThingUID)) {
            logger.debug("Thing Removed: {}", thingUID);
            shutdown();
            putOffline();
        }
    }

    @Override
    public @Nullable Collection<ThingUID> removeOlderResults(DiscoveryService source, long timestamp,
            @Nullable Collection<ThingTypeUID> thingTypeUIDs, @Nullable ThingUID bridgeUID) {
        return Collections.emptyList();
    }

    /**
     * Send multiple WOL packets spaced with 100ms intervals and resend command
     *
     * @param channel Channel to resend command on
     * @param command Command to resend
     */
    private void sendWOLandResendCommand(String channel, Command command) {
        SamsungTvConfiguration configuration = getConfigAs(SamsungTvConfiguration.class);

        if (configuration.macAddress == null || configuration.macAddress.isEmpty()) {
            logger.warn("Cannot send WOL packet to {} MAC address unknown", configuration.hostName);
            return;
        } else {
            logger.info("Send WOL packet to {} ({})", configuration.hostName, configuration.macAddress);

            // send max 10 WOL packets with 100ms intervals
            scheduler.schedule(new Runnable() {
                int count = 0;

                @Override
                public void run() {
                    count++;
                    if (count < WOL_PACKET_RETRY_COUNT) {
                        WakeOnLanUtility.sendWOLPacket(configuration.macAddress);
                        scheduler.schedule(this, 100, TimeUnit.MILLISECONDS);
                    }
                }
            }, 1, TimeUnit.MILLISECONDS);

            // after RemoteService up again to ensure state is properly set
            scheduler.schedule(new Runnable() {
                int count = 0;

                @Override
                public void run() {
                    count++;
                    if (count < WOL_SERVICE_CHECK_COUNT) {
                        RemoteControllerService service = (RemoteControllerService) findServiceInstance(
                                RemoteControllerService.SERVICE_NAME);
                        if (service != null) {
                            logger.info("Service found after {} attempts: resend command {} to channel {}", count,
                                    command, channel);
                            service.handleCommand(channel, command);
                        } else {
                            scheduler.schedule(this, 1000, TimeUnit.MILLISECONDS);
                        }
                    } else {
                        logger.info("Service NOT found after {} attempts", count);
                    }
                }

            }, 1000, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void putConfig(@Nullable String key, @Nullable Object value) {
        getConfig().put(key, value);
    }

    @Override
    public Object getConfig(@Nullable String key) {
        return getConfig().get(key);
    }

    @Override
    public WebSocketFactory getWebSocketFactory() {
        return webSocketFactory;
    }
}
