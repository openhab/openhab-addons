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
 * The {@link StatusService} contains methods to get a status of a Revogi SmartStrip
 *
 * @author Andi Bräu - Initial contribution
 */
@NonNullByDefault
public class StatusService {

    private static final String UDP_DISCOVERY_QUERY = "V3{\"sn\":\"%s\", \"cmd\": 90}";
    public static final String VERSION_STRING = "V3";
    private final Logger logger = LoggerFactory.getLogger(StatusService.class);

    private final Gson gson = new GsonBuilder().create();
    private final UdpSenderService udpSenderService;

    public StatusService(UdpSenderService udpSenderService) {
        this.udpSenderService = udpSenderService;
    }

    public CompletableFuture<StatusDTO> queryStatus(String serialNumber, String ipAddress) {
        CompletableFuture<List<UdpResponseDTO>> responses;
        if (ipAddress.trim().isEmpty()) {
            responses = udpSenderService.broadcastUdpDatagram(String.format(UDP_DISCOVERY_QUERY, serialNumber));
        } else {
            responses = udpSenderService.sendMessage(String.format(UDP_DISCOVERY_QUERY, serialNumber), ipAddress);
        }
        return responses.thenApply(this::getStatus);
    }

    private StatusDTO getStatus(final List<UdpResponseDTO> singleResponse) {
        return singleResponse.stream()
                .filter(response -> !response.getAnswer().isEmpty() && response.getAnswer().contains(VERSION_STRING))
                .map(response -> deserializeString(response.getAnswer()))
                .filter(statusRaw -> statusRaw.getCode() == 200 && statusRaw.getResponse() == 90)
                .map(statusRaw -> new StatusDTO(true, statusRaw.getCode(), statusRaw.getData().getSwitchValue(),
                        statusRaw.getData().getWatt(), statusRaw.getData().getAmp()))
                .findFirst().orElse(new StatusDTO(false, 503, null, null, null));
    }

    private StatusRawDTO deserializeString(String response) {
        String extractedJsonResponse = response.substring(response.lastIndexOf(VERSION_STRING) + 2);
        try {
            return Objects.requireNonNull(gson.fromJson(extractedJsonResponse, StatusRawDTO.class));
        } catch (JsonSyntaxException e) {
            logger.warn("Could not parse string \"{}\" to StatusRaw", response, e);
            return new StatusRawDTO(503, 0, new StatusDTO(false, 503, null, null, null));
        }
    }
}
