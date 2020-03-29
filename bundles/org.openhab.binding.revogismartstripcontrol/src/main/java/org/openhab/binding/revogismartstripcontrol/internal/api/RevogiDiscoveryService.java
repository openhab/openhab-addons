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
package org.openhab.binding.revogismartstripcontrol.internal.api;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.revogismartstripcontrol.internal.udp.UdpResponse;
import org.openhab.binding.revogismartstripcontrol.internal.udp.UdpSenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The {@link RevogiDiscoveryService} helps to discover smart strips within your network
 *
 * @author Andi Bräu - Initial contribution
 */
@NonNullByDefault
public class RevogiDiscoveryService {
    private static final String UDP_DISCOVERY_QUERY = "00sw=all,,,;";
    private final Logger logger = LoggerFactory.getLogger(RevogiDiscoveryService.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UdpSenderService udpSenderService;

    public RevogiDiscoveryService(UdpSenderService udpSenderService) {
        this.udpSenderService = udpSenderService;
    }

    public List<DiscoveryRawResponse> discoverSmartStrips() {
        List<UdpResponse> responses = udpSenderService.broadcastUpdDatagram(UDP_DISCOVERY_QUERY);
        responses.forEach(response -> logger.info("Received: {}", response));
        return responses.stream().filter(response -> !response.getAnswer().isEmpty()).map(this::deserializeString)
                .filter(discoveryRawResponse -> discoveryRawResponse.getResponse() == 0).collect(Collectors.toList());
    }

    private DiscoveryRawResponse deserializeString(UdpResponse response) {
        try {
            DiscoveryRawResponse discoveryRawResponse = objectMapper.readValue(response.getAnswer(),
                    DiscoveryRawResponse.class);
            discoveryRawResponse.setIpAddress(response.getIpAddress());
            return discoveryRawResponse;
        } catch (IOException e) {
            logger.warn("Could not parse string \"{}\" to DiscoveryRawResponse", response, e);
            return new DiscoveryRawResponse(503, new DiscoveryResponse());
        }
    }
}
