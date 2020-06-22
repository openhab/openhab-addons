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
package org.openhab.binding.wizlighting.internal.discovery;

import static org.openhab.binding.wizlighting.internal.WizLightingBindingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.wizlighting.internal.entities.RegistrationRequestParam;
import org.openhab.binding.wizlighting.internal.entities.SystemConfigResult;
import org.openhab.binding.wizlighting.internal.entities.WizLightingRequest;
import org.openhab.binding.wizlighting.internal.entities.WizLightingResponse;
import org.openhab.binding.wizlighting.internal.enums.WizLightingMethodType;
import org.openhab.binding.wizlighting.internal.handler.WizLightingMediator;
import org.openhab.binding.wizlighting.internal.utils.WizLightingPacketConverter;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the {@link DiscoveryService} for the Wizlighting Items.
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
@Component(configurationPid = "discovery.wizlighting", service = DiscoveryService.class, immediate = true)
@NonNullByDefault
public class WizLightingDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(WizLightingDiscoveryService.class);

    private final WizLightingMediator mediator;

    private final WizLightingPacketConverter converter = new WizLightingPacketConverter();

    /**
     * Constructor of the discovery service.
     *
     * @throws IllegalArgumentException if the timeout < 0
     */
    @Activate
    public WizLightingDiscoveryService(
            @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC) WizLightingMediator mediator)
            throws IllegalArgumentException {
        super(SUPPORTED_THING_TYPES, DISCOVERY_TIMEOUT_SECONDS, true);
        this.mediator = mediator;
        mediator.setDiscoveryService(this);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES;
    }

    @Override
    protected void startScan() {
        DatagramSocket dsocket = null;
        try {
            RegistrationRequestParam outParam = this.mediator.getRegistrationParams();
            String outIpReal = outParam.getPhoneIp();
            String broadcastIp = outIpReal.substring(0, outIpReal.lastIndexOf(".")) + "255";
            InetAddress address = InetAddress.getByName(broadcastIp);
            WizLightingRequest request = new WizLightingRequest(WizLightingMethodType.Registration,
                    this.mediator.getRegistrationParams());
            request.setId(0);

            byte[] message = this.converter.transformToByteMessage(request);
            // logger.trace("Raw packet to send: {}", message);

            // Initialize a datagram packet with data and address
            DatagramPacket packet = new DatagramPacket(message, message.length, address, DEFAULT_BULB_UDP_PORT);

            // Create a datagram socket, send the packet through it, close it.
            // For discovery we will "fire and forget" and let the mediator take care of the
            // responses
            dsocket = new DatagramSocket();
            dsocket.send(packet);
            logger.debug("Sent packet to address: {} and port {}", address, DEFAULT_BULB_UDP_PORT);
        } catch (IOException exception) {
            logger.debug("Something wrong happened when broadcasting the packet to port {}... msg: {}",
                    DEFAULT_BULB_UDP_PORT, exception.getMessage());
        } finally {
            if (dsocket != null) {
                dsocket.close();
            }
        }
    }

    /**
     * Method called by mediator, after receiving a packet from an unknown WiZ bulb
     *
     * @param bulbMacAddress the mac address from the device.
     * @param bulbIpAddress the host address from the device.
     */
    public void discoveredLight(final String lightMacAddress, final String lightIpAddress) {
        Map<String, Object> properties = new HashMap<>(2);
        properties.put(CONFIG_MAC_ADDRESS, lightMacAddress);
        properties.put(CONFIG_IP_ADDRESS, lightIpAddress);
        logger.trace("New bulb discovered at {} with MAC {}.  Requesting configuration info from the bulb.",
                lightIpAddress, lightMacAddress);

        // Assume it is a full color bulb, unless we get confirmation otherwise.
        // This will ensure the maximum number of channels will be created so there's no
        // missing functionality.
        // There's nothing a simple dimmable bulb can do that a full color bulb can't.
        // It's easy for a user to ignore or not link anything to a non-working channel,
        // but impossible to add a new channel if it's wanted.
        // The bulbs will merely ignore or return an error for specific commands they
        // cannot carry-out (ie, setting color on a non-color bulb) and continue to
        // function as they were before the bad command.
        ThingTypeUID thisBulbType = THING_TYPE_WIZ_COLOR_BULB;
        String thisBulbLabel = "WiZ Full Color Bulb at " + lightIpAddress;
        ThingUID newThingId = new ThingUID(thisBulbType, lightMacAddress);

        WizLightingResponse configResponse = getDiscoveredBulbConfig(lightIpAddress);
        if (configResponse != null) {
            SystemConfigResult discoveredBulbConfig = configResponse.getSystemConfigResults();
            if (discoveredBulbConfig != null) {
                String discoveredModel = discoveredBulbConfig.moduleName;
                logger.trace("Returned model from discovered bulb at {}: {}", lightIpAddress, discoveredModel);

                // We'll try to key off "TW" for tunable white
                if (discoveredModel.contains("TW")) {
                    thisBulbType = THING_TYPE_WIZ_TUNABLE_BULB;
                    thisBulbLabel = "WiZ Tunable White Bulb at " + lightIpAddress;
                    newThingId = new ThingUID(thisBulbType, lightMacAddress);
                    logger.trace("New bulb appears to be a tunable white bulb and will be given the UUID: {}",
                            newThingId);

                    // We key off "RGB" for color bulbs
                } else if (!discoveredModel.contains("RBG")) {
                    thisBulbType = THING_TYPE_WIZ_DIMMABLE_BULB;
                    thisBulbLabel = "WiZ Dimmable White Bulb at " + lightIpAddress;
                    newThingId = new ThingUID(thisBulbType, lightMacAddress);
                    logger.trace(
                            "New bulb appears not to be either tunable white bulb or full color and will be called dimmable only and given the UUID: {}",
                            newThingId);
                }

                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(newThingId).withProperties(properties)
                        .withLabel(thisBulbLabel).withRepresentationProperty(CONFIG_MAC_ADDRESS).build();

                this.thingDiscovered(discoveryResult);
            }
        } else {
            logger.trace(
                    "Couldn't get or couldn't parse configuration information from discovered bulb.  Discovery result will not be created.");
        }
    }

    private synchronized @Nullable WizLightingResponse getDiscoveredBulbConfig(final String lightIpAddress) {
        DatagramSocket dsocket = null;
        try {
            WizLightingRequest request = new WizLightingRequest(WizLightingMethodType.GetSystemConfig, null);
            request.setId(1);

            byte[] message = this.converter.transformToByteMessage(request);
            // logger.trace("Raw packet to send: {}", message);

            // Initialize a datagram packet with data and address
            InetAddress address = InetAddress.getByName(lightIpAddress);
            DatagramPacket packet = new DatagramPacket(message, message.length, address, DEFAULT_BULB_UDP_PORT);

            // Create a datagram socket, send the packet through it, close it.
            dsocket = new DatagramSocket();
            dsocket.send(packet);
            logger.debug("Sent packet to address: {} and port {}", address, DEFAULT_BULB_UDP_PORT);

            byte[] responseMessage = new byte[1024];
            packet = new DatagramPacket(responseMessage, responseMessage.length);
            dsocket.receive(packet);

            return converter.transformResponsePacket(packet);
        } catch (SocketTimeoutException e) {
            logger.trace("Socket timeout after sending command; no response from {} within 500ms", lightIpAddress);
        } catch (IOException exception) {
            logger.debug("Something wrong happened when sending the packet to address: {} and port {}... msg: {}",
                    lightIpAddress, DEFAULT_BULB_UDP_PORT, exception.getMessage());
        } finally {
            if (dsocket != null) {
                dsocket.close();
            }
        }
        return null;
    }

    // SETTERS AND GETTERS
    /**
     * Gets the {@link WizLightingMediator} of this binding.
     *
     * @return {@link WizLightingMediator}.
     */
    public WizLightingMediator getMediator() {
        return this.mediator;
    }
}
