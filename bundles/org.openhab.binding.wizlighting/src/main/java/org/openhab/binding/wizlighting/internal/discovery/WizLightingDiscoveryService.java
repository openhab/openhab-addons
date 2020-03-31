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
import org.openhab.binding.wizlighting.internal.entities.WizLightingRequest;
import org.openhab.binding.wizlighting.internal.enums.WizLightingMethodType;
import org.openhab.binding.wizlighting.internal.handler.WizLightingMediator;
import org.openhab.binding.wizlighting.internal.utils.WizLightingPacketConverter;
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

    @NonNullByDefault({})
    private WizLightingMediator mediator;

    private final WizLightingPacketConverter converter = new WizLightingPacketConverter();

    /**
     * Used by OSGI to inject the mediator in the discovery service.
     *
     * @param mediator the mediator
     */
    @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC)
    public void setMediator(final WizLightingMediator mediator) {
        logger.trace("Mediator has been injected on discovery service.");

        this.mediator = mediator;
        mediator.setDiscoveryService(this);
    }

    /**
     * Used by OSGI to unset the mediator in the discovery service.
     *
     * @param mediator the mediator
     */
    public void unsetMediator(final WizLightingMediator mitsubishiMediator) {
        logger.trace("Mediator has been unsetted from discovery service.");
        WizLightingMediator mediator = this.mediator;
        if (mediator != null) {
            mediator.setDiscoveryService(null);
            this.mediator = null;
        }
    }

    /**
     * Constructor of the discovery service.
     *
     * @throws IllegalArgumentException if the timeout < 0
     */
    public WizLightingDiscoveryService() throws IllegalArgumentException {
        super(SUPPORTED_THING_TYPES, DISCOVERY_TIMEOUT_SECONDS);
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
            WizLightingRequest request = new WizLightingRequest(WizLightingMethodType.registration,
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

        // NOTE: Only full color bulbs supported at this time
        ThingUID newThingId = new ThingUID(THING_TYPE_WIZ_COLOR_BULB, lightMacAddress);
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(newThingId).withProperties(properties)
                .withLabel("WiZ Full Color Bulb at " + lightIpAddress).withRepresentationProperty(lightMacAddress)
                .build();

        this.thingDiscovered(discoveryResult);
    }

    // SETTERS AND GETTERS
    /**
     * Gets the {@link WizLightingMediator} of this binding.
     *
     * @return {@link WizLightingMediator}.
     */
    public @Nullable WizLightingMediator getMediator() {
        return this.mediator;
    }
}
