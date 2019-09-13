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
package org.openhab.binding.heos.internal.resources;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link HeosJsonParser} parses the JSON message of the
 * HEOS bridge to the correct Deserializer
 *
 * @author Johannes Einig - Initial contribution
 */
public class HeosJsonParser {

    private HeosResponse response;
    private HeosResponseEvent eventResponse;
    private HeosResponsePayload payloadResponse;
    private Gson gson;

    public HeosJsonParser(HeosResponse response) {
        this.response = response;
        this.eventResponse = response.getEvent();
        this.payloadResponse = response.getPayload();

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(HeosResponseEvent.class, new HeosDeserializerEvent());
        gsonBuilder.registerTypeAdapter(HeosResponsePayload.class, new HeosDeserializerPayload());
        this.gson = gsonBuilder.create();
    }

    public synchronized HeosResponse parseResult(String receivedMessage) {
        response.setRawResponseMessage(receivedMessage);

        this.eventResponse = gson.fromJson(receivedMessage, HeosResponseEvent.class);
        this.payloadResponse = gson.fromJson(receivedMessage, HeosResponsePayload.class);

        this.response.setEvent(eventResponse);
        this.response.setPayload(payloadResponse);

        if (eventResponse.getMessagesMap().containsKey(HeosConstants.PID)) {
            response.setPid((eventResponse.getMessagesMap().get(HeosConstants.PID)));
        }
        return response;
    }
}
