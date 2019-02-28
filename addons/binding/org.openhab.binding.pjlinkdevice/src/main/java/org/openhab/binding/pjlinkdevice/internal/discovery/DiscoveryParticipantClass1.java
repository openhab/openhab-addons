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
package org.openhab.binding.pjlinkdevice.internal.discovery;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.net.util.SubnetUtils;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.openhab.binding.pjlinkdevice.internal.PJLinkDeviceBindingConstants;
import org.openhab.binding.pjlinkdevice.internal.device.PJLinkDevice;
import org.openhab.binding.pjlinkdevice.internal.device.command.AuthenticationException;
import org.openhab.binding.pjlinkdevice.internal.device.command.ResponseException;
import org.osgi.service.component.annotations.Component;

import com.google.common.collect.ImmutableSet;

/**
 * @author Nils Schnabel - Initial contribution
 *
 */

@Component(service = DiscoveryService.class, immediate = true, configurationPid = "org.openhab.binding.pjlinkdevice.internal.discovery.DiscoveryParticipantClass1")
public class DiscoveryParticipantClass1 extends AbstractDiscoveryParticipant {

    public DiscoveryParticipantClass1() throws IllegalArgumentException {
        super(ImmutableSet.of(PJLinkDeviceBindingConstants.THING_TYPE_PJLINK), 60, true);
        logger.trace("PJLinkProjectorDiscoveryParticipant constructor");
    }

    @Override
    protected void collectAddressesToScan(Set<InetAddress> addressesToScan, InterfaceAddress i) {
        // only scan IPv4
        if (!java.net.Inet4Address.class.isInstance(i.getAddress())) {
            return;
        }
        // only scan Class C networks
        if (i.getNetworkPrefixLength() < 24) {
            return;
        }

        SubnetUtils utils = new SubnetUtils(i.getAddress().getHostAddress() + "/" + i.getNetworkPrefixLength());
        for (String addressToScan : utils.getInfo().getAllAddresses()) {
            try {
                logger.info("Add address to scan: {}", addressToScan);
                addressesToScan.add(InetAddress.getByName(addressToScan));
            } catch (UnknownHostException e) {
                logger.warn("Unknown Host", e);
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
                    logger.warn("got name {}", description);
                } catch (ResponseException e) {
                    logger.warn("Could not find a name for PJLink device", e);
                    // okay, no name
                }
            } catch (AuthenticationException e) {
                properties.put(PJLinkDeviceBindingConstants.PARAMETER_AUTHENTICATION_REQUIRED, true);
            }
            logger.warn("Adding thing");
            thingDiscovered(DiscoveryResultBuilder.create(createServiceUID(ip.getHostAddress(), tcpPort))
                    .withTTL(PJLinkDeviceBindingConstants.DISCOVERY_RESULT_TTL).withProperties(properties)
                    .withLabel(description).build());
            logger.warn("Added thing");
        } catch (ResponseException | IOException e) {
            logger.warn("No PJLinkDevice here {} {}", ip, e.getStackTrace());
            // no device here
        }
    }

}
