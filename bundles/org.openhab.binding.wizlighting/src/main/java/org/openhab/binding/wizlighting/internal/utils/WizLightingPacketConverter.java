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
package org.openhab.binding.wizlighting.internal.utils;

import static java.nio.charset.StandardCharsets.*;

import java.net.DatagramPacket;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wizlighting.internal.entities.WizLightingRequest;
import org.openhab.binding.wizlighting.internal.entities.WizLightingResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

/**
 * Transforms the datagram packet to request/response
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
@NonNullByDefault
public class WizLightingPacketConverter {

    private final Logger logger = LoggerFactory.getLogger(WizLightingPacketConverter.class);

    private Gson wizlightingGsonBuilder;

    /**
     * Default constructor of the packet converter.
     */
    public WizLightingPacketConverter() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(WizLightingResponse.class, new WizResponseDeserializer());
        gsonBuilder.excludeFieldsWithoutExposeAnnotation();
        // gsonBuilder.setLenient();
        Gson gson = gsonBuilder.create();
        this.wizlightingGsonBuilder = gson;
    }

    /**
     * Method that transforms one {@link WizLightingRequest} to json requst
     *
     * @param requestPacket the {@link WizLightingRequest}.
     * @return the byte array with the message.
     */
    public byte[] transformToByteMessage(final WizLightingRequest requestPacket) {
        byte[] requestDatagram = null;

        // {"id":20,"method":"setPilot","params":{"sceneId":18}}
        String jsonCmd = this.wizlightingGsonBuilder.toJson(requestPacket);

        requestDatagram = jsonCmd.getBytes(UTF_8);
        return requestDatagram;
    }

    /**
     * Method that transforms {@link DatagramPacket} to a
     * {@link WizLightingResponse} Object
     *
     * @param packet the {@link DatagramPacket}
     * @return the {@link WizLightingResponse}
     */
    public @Nullable WizLightingResponse transformResponsePacket(final DatagramPacket packet) {
        String responseJson = new String(packet.getData(), 0, packet.getLength(), UTF_8);
        logger.debug("Incoming packet from {} to convert -> {}", packet.getAddress().getHostAddress(), responseJson);

        @Nullable
        WizLightingResponse response = null;
        try {
            response = this.wizlightingGsonBuilder.fromJson(responseJson, WizLightingResponse.class);
            response.setWizResponseIpAddress(packet.getAddress().getHostAddress());
        } catch (JsonParseException e) {
            logger.error("Error parsing json! {}", e.getMessage());
        }
        return response;
    }
}
