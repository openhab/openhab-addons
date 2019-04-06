/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wizlighting.internal.utils;

import java.net.DatagramPacket;

import org.openhab.binding.wizlighting.internal.entities.WizLightingRequest;
import org.openhab.binding.wizlighting.internal.entities.WizLightingResponse;
import org.openhab.binding.wizlighting.internal.entities.WizLightingSyncResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Transforms the datagram packet to request/response
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
public class WizLightingPacketConverter {

    private final Logger logger = LoggerFactory.getLogger(WizLightingPacketConverter.class);

    private Gson wizlightingGsonBuilder;

    /**
     * Default constructor of the packet converter.
     */
    public WizLightingPacketConverter() {
        this.wizlightingGsonBuilder = new GsonBuilder().create();
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
        logger.debug("JsonCmd={{}}", jsonCmd);

        requestDatagram = jsonCmd.getBytes();
        return requestDatagram;
    }

    /**
     * Method that transforms {@link DatagramPacket} to Json object.
     *
     * @param packet the {@link DatagramPacket}
     * @return the {@link WizLightingResponse} is successfully transformed.
     */
    public WizLightingResponse transformResponsePacket(final DatagramPacket packet) {
        String responseJson = new String(packet.getData(), 0, packet.getLength());
        logger.debug("Response Json={{}}", responseJson);

        WizLightingResponse response = this.wizlightingGsonBuilder.fromJson(responseJson, WizLightingResponse.class);
        return response;
    }

    /**
     * Method that transforms {@link DatagramPacket} to a {@link WizLightingSyncResponse} Object
     *
     * @param packet the {@link DatagramPacket}
     * @return the {@link WizLightingSyncResponse}
     */
    public WizLightingSyncResponse transformSyncResponsePacket(final DatagramPacket packet) {
        String responseJson = new String(packet.getData(), 0, packet.getLength());
        logger.debug("Sync Response Json={{}}", responseJson);

        WizLightingSyncResponse response = this.wizlightingGsonBuilder.fromJson(responseJson,
                WizLightingSyncResponse.class);
        response.setHostAddress(packet.getAddress().getHostAddress());
        return response;
    }
}