/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.revogi.internal.udp.UdpResponseDTO;
import org.openhab.binding.revogi.internal.udp.UdpSenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link SwitchService} enables the binding to actually switch plugs on and of
 *
 * @author Andi Bräu - Initial contribution
 */
@NonNullByDefault
public class SwitchService {

    private static final String UDP_DISCOVERY_QUERY = "V3{\"sn\":\"%s\", \"cmd\": 20, \"port\": %d, \"state\": %d}";
    private static final String VERSION_STRING = "V3";
    private final Logger logger = LoggerFactory.getLogger(SwitchService.class);

    private final Gson gson = new GsonBuilder().create();
    private final UdpSenderService udpSenderService;

    public SwitchService(UdpSenderService udpSenderService) {
        this.udpSenderService = udpSenderService;
    }

    public CompletableFuture<SwitchResponseDTO> switchPort(String serialNumber, String ipAddress, int port, int state) {
        if (state < 0 || state > 1) {
            throw new IllegalArgumentException("state has to be 0 or 1");
        }
        if (port < 0) {
            throw new IllegalArgumentException("Given port doesn't exist");
        }

        CompletableFuture<List<UdpResponseDTO>> responses;
        if (ipAddress.trim().isEmpty()) {
            responses = udpSenderService
                    .broadcastUdpDatagram(String.format(UDP_DISCOVERY_QUERY, serialNumber, port, state));
        } else {
            responses = udpSenderService.sendMessage(String.format(UDP_DISCOVERY_QUERY, serialNumber, port, state),
                    ipAddress);
        }

        return responses.thenApply(this::getSwitchResponse);
    }

    private SwitchResponseDTO getSwitchResponse(final List<UdpResponseDTO> singleResponse) {
        return singleResponse.stream().filter(response -> !response.getAnswer().isEmpty())
                .map(response -> deserializeString(response.getAnswer()))
                .filter(switchResponse -> switchResponse.getCode() == 200 && switchResponse.getResponse() == 20)
                .findFirst().orElse(new SwitchResponseDTO(0, 503));
    }

    private SwitchResponseDTO deserializeString(String response) {
        String extractedJsonResponse = response.substring(response.lastIndexOf(VERSION_STRING) + 2);
        try {
            return Objects.requireNonNull(gson.fromJson(extractedJsonResponse, SwitchResponseDTO.class));
        } catch (JsonSyntaxException e) {
            logger.warn("Could not parse string \"{}\" to SwitchResponse", response);
            return new SwitchResponseDTO(0, 503);
        }
    }
}
