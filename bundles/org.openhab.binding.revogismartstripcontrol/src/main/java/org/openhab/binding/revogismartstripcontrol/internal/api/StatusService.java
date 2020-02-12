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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.revogismartstripcontrol.internal.udp.UdpSenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

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

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UdpSenderService udpSenderService;

    public StatusService(UdpSenderService udpSenderService) {
        this.udpSenderService = udpSenderService;
    }

    public Status queryStatus(String serialNumber) {
        List<String> responses = udpSenderService.broadcastUpdDatagram(String.format(UDP_DISCOVERY_QUERY, serialNumber));
        responses.forEach(response -> logger.info("Received: {}", response));
        return responses.stream()
                .filter(response -> !response.isEmpty() && response.contains(VERSION_STRING))
                .map(this::deserializeString)
                .filter(statusRaw -> statusRaw.getCode() == 200)
                .map(statusRaw -> new Status(true, statusRaw.getCode(), statusRaw.getData().getSwitchValue(), statusRaw.getData().getWatt(), statusRaw.getData().getAmp()))
                .findFirst()
                .orElse(new Status(false, 503, null, null, null));


    }

    private StatusRaw deserializeString(String response) {
        String extractedJsonResponse = response.substring(response.lastIndexOf(VERSION_STRING) + 2);
        try {
            return objectMapper.readValue(extractedJsonResponse, StatusRaw.class);
        } catch (IOException e) {
            logger.warn("Could not parse string \"{}\" to StatusRaw", response, e);
            return new StatusRaw(503, 0, new Status(false, 503, null, null, null));
        }
    }
}
