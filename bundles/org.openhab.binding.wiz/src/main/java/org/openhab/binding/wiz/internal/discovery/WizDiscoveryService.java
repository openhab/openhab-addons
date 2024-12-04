/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.wiz.internal.discovery;

import static org.openhab.binding.wiz.internal.WizBindingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wiz.internal.entities.RegistrationRequestParam;
import org.openhab.binding.wiz.internal.entities.SystemConfigResult;
import org.openhab.binding.wiz.internal.entities.WizRequest;
import org.openhab.binding.wiz.internal.entities.WizResponse;
import org.openhab.binding.wiz.internal.enums.WizMethodType;
import org.openhab.binding.wiz.internal.handler.WizMediator;
import org.openhab.binding.wiz.internal.utils.WizPacketConverter;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the {@link DiscoveryService} for the WiZ Things.
 *
 * @author Sriram Balakrishnan - Initial contribution
 * @author Joshua Freeman - use configured Broadcast address instead of guessing, discovery of plugs
 *
 */
@Component(configurationPid = "discovery.wiz", service = DiscoveryService.class, immediate = true)
@NonNullByDefault
public class WizDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(WizDiscoveryService.class);

    private final WizMediator mediator;

    private final WizPacketConverter converter = new WizPacketConverter();

    private @Nullable ScheduledFuture<?> backgroundDiscovery;

    /**
     * Constructor of the discovery service.
     *
     * @throws IllegalArgumentException if the timeout < 0
     */
    @Activate
    public WizDiscoveryService(
            @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC) WizMediator mediator)
            throws IllegalArgumentException {
        super(SUPPORTED_THING_TYPES, DISCOVERY_TIMEOUT_SECONDS, true);
        this.mediator = mediator;
        mediator.setDiscoveryService(this);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES;
    }

    /**
     * This method is called when {@link AbstractDiscoveryService#setBackgroundDiscoveryEnabled(boolean)}
     * is called with true as parameter and when the component is being activated
     * (see {@link AbstractDiscoveryService#activate()}.
     *
     * This will also serve to "re-discover" any devices that have changed to a new IP address.
     */
    @Override
    protected void startBackgroundDiscovery() {
        ScheduledFuture<?> backgroundDiscovery = this.backgroundDiscovery;
        if (backgroundDiscovery == null || backgroundDiscovery.isCancelled()) {
            this.backgroundDiscovery = scheduler.scheduleWithFixedDelay(this::startScan, 1, 60, TimeUnit.MINUTES);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        ScheduledFuture<?> backgroundDiscovery = this.backgroundDiscovery;
        if (backgroundDiscovery != null && !backgroundDiscovery.isCancelled()) {
            backgroundDiscovery.cancel(true);
            this.backgroundDiscovery = null;
        }
    }

    @Override
    protected void startScan() {
        DatagramSocket dsocket = null;
        try {
            String broadcastIp = this.mediator.getNetworkAddressService().getConfiguredBroadcastAddress();
            if (broadcastIp != null) {
                InetAddress address = InetAddress.getByName(broadcastIp);
                RegistrationRequestParam registrationRequestParam = mediator.getRegistrationParams();
                WizRequest request = new WizRequest(WizMethodType.Registration, registrationRequestParam);
                request.setId(0);

                byte[] message = this.converter.transformToByteMessage(request);

                // Initialize a datagram packet with data and address
                DatagramPacket packet = new DatagramPacket(message, message.length, address, DEFAULT_UDP_PORT);

                // Create a datagram socket, send the packet through it, close it.
                // For discovery we will "fire and forget" and let the mediator take care of the
                // responses
                dsocket = new DatagramSocket();
                dsocket.send(packet);
                logger.debug("Broadcast packet to address: {} and port {}", address, DEFAULT_UDP_PORT);
            } else {
                logger.warn("No broadcast address was configured or discovered! No broadcast sent.");
            }
        } catch (IllegalStateException e) {
            logger.debug("Unable to start background scan: {}", e.getMessage());
        } catch (IOException exception) {
            logger.debug("Something wrong happened when broadcasting the packet to port {}... msg: {}",
                    DEFAULT_UDP_PORT, exception.getMessage());
        } finally {
            if (dsocket != null) {
                dsocket.close();
            }
        }
    }

    /**
     * Method called by mediator, after receiving a packet from an unknown WiZ device
     *
     * @param macAddress the mac address from the device.
     * @param ipAddress the host address from the device.
     */
    public void discoveredLight(final String macAddress, final String ipAddress) {
        Map<String, Object> properties = new HashMap<>(2);
        properties.put(CONFIG_MAC_ADDRESS, macAddress);
        properties.put(CONFIG_IP_ADDRESS, ipAddress);
        logger.trace("New device discovered at {} with MAC {}.  Requesting configuration info from it.", ipAddress,
                macAddress);

        // Assume it is a full color bulb, unless we get confirmation otherwise.
        // This will ensure the maximum number of channels will be created so there's no
        // missing functionality.
        // There's nothing a simple dimmable bulb can do that a full color bulb can't.
        // It's easy for a user to ignore or not link anything to a non-working channel,
        // but impossible to add a new channel if it's wanted.
        // The bulbs will merely ignore or return an error for specific commands they
        // cannot carry-out (ie, setting color on a non-color bulb) and continue to
        // function as they were before the bad command.
        ThingTypeUID thisDeviceType = THING_TYPE_COLOR_BULB;
        String thisDeviceLabel = "WiZ Full Color Bulb at " + ipAddress;
        ThingUID newThingId = new ThingUID(thisDeviceType, macAddress);

        WizResponse configResponse = getDiscoveredDeviceConfig(ipAddress);
        if (configResponse != null) {
            SystemConfigResult discoveredDeviceConfig = configResponse.getSystemConfigResults();
            if (discoveredDeviceConfig != null) {
                String discoveredModel = discoveredDeviceConfig.moduleName.toUpperCase();
                logger.trace("Returned model from discovered device at {}: {}", ipAddress, discoveredModel);

                // “moduleName”:“ESP10_SOCKET_06” confirmed example module name for Wiz Smart Plug
                // Check for "SOCKET" this seems safe based on other naming conventions observed
                if (discoveredModel.contains("SOCKET")) {
                    thisDeviceType = THING_TYPE_SMART_PLUG;
                    thisDeviceLabel = "WiZ Smart Plug at " + ipAddress;
                    newThingId = new ThingUID(thisDeviceType, macAddress);
                    logger.trace("New device appears to be a smart plug and will be given the UUID: {}", newThingId);

                    // We'll try to key off "TW" for tunable white
                } else if (discoveredModel.contains("TW")) {
                    thisDeviceType = THING_TYPE_TUNABLE_BULB;
                    thisDeviceLabel = "WiZ Tunable White Bulb at " + ipAddress;
                    newThingId = new ThingUID(thisDeviceType, macAddress);
                    logger.trace("New device appears to be a tunable white bulb and will be given the UUID: {}",
                            newThingId);

                    // Check for "FANDIMS" as in confirmed example ESP03_FANDIMS_31 for Faro Barcelona Smart Fan
                } else if (discoveredModel.contains("FANDIMS")) {
                    thisDeviceType = THING_TYPE_FAN_WITH_DIMMABLE_BULB;
                    thisDeviceLabel = "WiZ Smart Fan at " + ipAddress;
                    newThingId = new ThingUID(thisDeviceType, macAddress);
                    logger.trace("New device appears to be a smart fan and will be given the UUID: {}", newThingId);

                    // We key off "RGB" for color bulbs
                } else if (!discoveredModel.contains("RGB")) {
                    thisDeviceType = THING_TYPE_DIMMABLE_BULB;
                    thisDeviceLabel = "WiZ Dimmable White Bulb at " + ipAddress;
                    newThingId = new ThingUID(thisDeviceType, macAddress);
                    logger.trace(
                            "New device appears not to be either tunable white bulb or full color and will be called a dimmable only bulb and given the UUID: {}",
                            newThingId);
                }

                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(newThingId).withProperties(properties)
                        .withLabel(thisDeviceLabel).withRepresentationProperty(CONFIG_MAC_ADDRESS).build();

                this.thingDiscovered(discoveryResult);
            }
        } else {
            logger.trace(
                    "Couldn't get or couldn't parse configuration information from discovered device.  Discovery result will not be created.");
        }
    }

    private synchronized @Nullable WizResponse getDiscoveredDeviceConfig(final String lightIpAddress) {
        DatagramSocket dsocket = null;
        try {
            WizRequest request = new WizRequest(WizMethodType.GetSystemConfig, null);
            request.setId(1);

            byte[] message = this.converter.transformToByteMessage(request);

            // Initialize a datagram packet with data and address
            InetAddress address = InetAddress.getByName(lightIpAddress);
            DatagramPacket packet = new DatagramPacket(message, message.length, address, DEFAULT_UDP_PORT);

            // Create a datagram socket, send the packet through it, close it.
            dsocket = new DatagramSocket();
            dsocket.send(packet);
            logger.debug("Sent packet to address: {} and port {}", address, DEFAULT_UDP_PORT);

            byte[] responseMessage = new byte[1024];
            packet = new DatagramPacket(responseMessage, responseMessage.length);
            dsocket.receive(packet);

            return converter.transformResponsePacket(packet);
        } catch (SocketTimeoutException e) {
            logger.trace("Socket timeout after sending command; no response from {} within 500ms", lightIpAddress);
        } catch (IOException exception) {
            logger.debug("Something wrong happened when sending the packet to address: {} and port {}... msg: {}",
                    lightIpAddress, DEFAULT_UDP_PORT, exception.getMessage());
        } finally {
            if (dsocket != null) {
                dsocket.close();
            }
        }
        return null;
    }

    // SETTERS AND GETTERS
    /**
     * Gets the {@link WizMediator} of this binding.
     *
     * @return {@link WizMediator}.
     */
    public WizMediator getMediator() {
        return this.mediator;
    }
}
