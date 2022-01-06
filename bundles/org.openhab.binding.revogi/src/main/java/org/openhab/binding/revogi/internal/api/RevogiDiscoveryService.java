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
package org.openhab.binding.revogi.internal.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.revogi.internal.udp.UdpResponseDTO;
import org.openhab.binding.revogi.internal.udp.UdpSenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link RevogiDiscoveryService} helps to discover smart strips within your network
 *
 * @author Andi Bräu - Initial contribution
 */
@NonNullByDefault
public class RevogiDiscoveryService {
    private static final String UDP_DISCOVERY_QUERY = "00sw=all,,,;";
    private final Logger logger = LoggerFactory.getLogger(RevogiDiscoveryService.class);

    private final Gson gson = new GsonBuilder().create();
    private final UdpSenderService udpSenderService;

    public RevogiDiscoveryService(UdpSenderService udpSenderService) {
        this.udpSenderService = udpSenderService;
    }

    public CompletableFuture<List<DiscoveryRawResponseDTO>> discoverSmartStrips() {
        CompletableFuture<List<UdpResponseDTO>> responses = udpSenderService.broadcastUdpDatagram(UDP_DISCOVERY_QUERY);
        return responses.thenApply(futureList -> futureList.stream().filter(response -> !response.getAnswer().isEmpty())
                .map(this::deserializeString).filter(discoveryRawResponse -> discoveryRawResponse.getResponse() == 0)
                .collect(Collectors.toList()));
    }

    private DiscoveryRawResponseDTO deserializeString(UdpResponseDTO response) {
        try {
            DiscoveryRawResponseDTO discoveryRawResponse = gson.fromJson(response.getAnswer(),
                    DiscoveryRawResponseDTO.class);
            discoveryRawResponse.setIpAddress(response.getIpAddress());
            return discoveryRawResponse;
        } catch (JsonSyntaxException e) {
            logger.warn("Could not parse string \"{}\" to DiscoveryRawResponse", response, e);
            return new DiscoveryRawResponseDTO(503, new DiscoveryResponseDTO());
        }
    }
}
