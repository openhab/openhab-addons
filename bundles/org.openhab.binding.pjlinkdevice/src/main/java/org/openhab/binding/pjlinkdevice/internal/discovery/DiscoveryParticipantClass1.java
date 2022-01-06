/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.pjlinkdevice.internal.discovery;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.net.util.SubnetUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.pjlinkdevice.internal.PJLinkDeviceBindingConstants;
import org.openhab.binding.pjlinkdevice.internal.device.PJLinkDevice;
import org.openhab.binding.pjlinkdevice.internal.device.command.AuthenticationException;
import org.openhab.binding.pjlinkdevice.internal.device.command.ResponseException;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.osgi.service.component.annotations.Component;

/**
 * Implementation of {@link AbstractDiscoveryParticipant} for IPv4 address ranges and PJLink Class 1 devices.
 * 
 * @author Nils Schnabel - Initial contribution
 */

@Component(service = DiscoveryService.class, configurationPid = "org.openhab.binding.pjlinkdevice.internal.discovery.DiscoveryParticipantClass1")
@NonNullByDefault
public class DiscoveryParticipantClass1 extends AbstractDiscoveryParticipant {
    public DiscoveryParticipantClass1() throws IllegalArgumentException {
        super(Collections.singleton(PJLinkDeviceBindingConstants.THING_TYPE_PJLINK), 60, true);

        logger.trace("PJLinkProjectorDiscoveryParticipant constructor");
    }

    @Override
    protected void collectAddressesToScan(Set<InetAddress> addressesToScan, InterfaceAddress i) {
        // only scan IPv4
        if (!(i.getAddress() instanceof Inet4Address)) {
            return;
        }
        // only scan Class C networks
        if (i.getNetworkPrefixLength() < 24) {
            return;
        }

        SubnetUtils utils = new SubnetUtils(i.getAddress().getHostAddress() + "/" + i.getNetworkPrefixLength());
        for (String addressToScan : utils.getInfo().getAllAddresses()) {
            try {
                logger.debug("Add address to scan: {}", addressToScan);
                addressesToScan.add(InetAddress.getByName(addressToScan));
            } catch (UnknownHostException e) {
                logger.debug("Unknown Host", e);
            }
        }
    }

    @Override
    protected void checkAddress(InetAddress ip, int tcpPort, int timeout) {
        PJLinkDevice device = new PJLinkDevice(tcpPort, ip, null, timeout);
        try {
            Map<String, Object> properties = new HashMap<>();
            properties.put(PJLinkDeviceBindingConstants.PARAMETER_HOSTNAME, ip.getHostAddress());
            properties.put(PJLinkDeviceBindingConstants.PARAMETER_PORT, tcpPort);
            String description = "Unknown PJLink Device";
            try {
                device.checkAvailability();

                try {
                    description = device.getFullDescription();
                    logger.debug("got name {}", description);
                } catch (ResponseException e) {
                    logger.debug("Could not find a name for PJLink device", e);
                    // okay, no name
                }
            } catch (AuthenticationException e) {
                properties.put(PJLinkDeviceBindingConstants.PROPERTY_AUTHENTICATION_REQUIRED, true);
            }
            logger.debug("Adding thing");
            thingDiscovered(DiscoveryResultBuilder.create(createServiceUID(ip.getHostAddress(), tcpPort))
                    .withTTL(PJLinkDeviceBindingConstants.DISCOVERY_RESULT_TTL_SECONDS).withProperties(properties)
                    .withLabel(description).build());
            logger.debug("Added thing");
        } catch (ResponseException | IOException e) {
            logger.debug("No PJLinkDevice here {} {}", ip, e.getStackTrace());
            // no device here
        }
    }
}
