/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.wiz.internal.utils;

import static java.nio.charset.StandardCharsets.*;

import java.net.DatagramPacket;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wiz.internal.entities.WizRequest;
import org.openhab.binding.wiz.internal.entities.WizResponse;
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
public class WizPacketConverter {

    private final Logger logger = LoggerFactory.getLogger(WizPacketConverter.class);

    private Gson wizGsonBuilder;

    /**
     * Default constructor of the packet converter.
     */
    public WizPacketConverter() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(WizResponse.class, new WizResponseDeserializer());
        gsonBuilder.excludeFieldsWithoutExposeAnnotation();
        Gson gson = gsonBuilder.create();
        this.wizGsonBuilder = gson;
    }

    /**
     * Method that transforms one {@link WizRequest} to json requst
     *
     * @param requestPacket the {@link WizRequest}.
     * @return the byte array with the message.
     */
    public byte[] transformToByteMessage(final WizRequest requestPacket) {
        byte[] requestDatagram = null;

        // {"id":20,"method":"setPilot","params":{"sceneId":18}}
        String jsonCmd = this.wizGsonBuilder.toJson(requestPacket);

        requestDatagram = jsonCmd.getBytes(UTF_8);
        return requestDatagram;
    }

    /**
     * Method that transforms {@link DatagramPacket} to a
     * {@link WizResponse} Object
     *
     * @param packet the {@link DatagramPacket}
     * @return the {@link WizResponse}
     */
    public @Nullable WizResponse transformResponsePacket(final DatagramPacket packet) {
        String responseJson = new String(packet.getData(), 0, packet.getLength(), UTF_8);
        logger.debug("Incoming packet from {} to convert -> {}", packet.getAddress().getHostAddress(), responseJson);

        @Nullable
        WizResponse response = null;
        try {
            response = this.wizGsonBuilder.fromJson(responseJson, WizResponse.class);
            if (response == null) {
                throw new JsonParseException("JSON is empty");
            }
            response.setWizResponseIpAddress(packet.getAddress().getHostAddress());
        } catch (JsonParseException e) {
            logger.debug("Error parsing json! {}", e.getMessage());
        }
        return response;
    }
}
