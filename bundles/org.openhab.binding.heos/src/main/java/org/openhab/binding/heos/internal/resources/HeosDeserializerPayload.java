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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * The {@link HeosDeserializePayload} decodes the payload part of the
 * JSON message received from the HEOS bridge
 *
 * @author Johannes Einig - Initial contribution
 */
public class HeosDeserializerPayload implements JsonDeserializer<HeosResponsePayload> {

    private HeosResponsePayload responsePayload = new HeosResponsePayload();

    private static final String PAYLOAD = "payload";

    @Override
    public HeosResponsePayload deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        boolean arrayTrue = false;
        List<Map<String, String>> mapList = new ArrayList<>();
        List<List<Map<String, String>>> overallPlayerList = new ArrayList<>();
        List<Map<String, String>> groupPlayerList = new ArrayList<>();

        JsonObject jsonObject = json.getAsJsonObject();

        if (jsonObject.has(PAYLOAD)) {
            if (jsonObject.get(PAYLOAD).isJsonArray()) {
                arrayTrue = true;
            }
        }
        if (jsonObject.has(PAYLOAD) && arrayTrue) {
            JsonArray jsonArray = jsonObject.get(PAYLOAD).getAsJsonArray();
            for (int i = 0; i < jsonArray.size(); i++) {
                Map<String, String> payload = new HashMap<>();
                JsonObject object = jsonArray.get(i).getAsJsonObject();
                for (Entry<String, JsonElement> entry : object.entrySet()) {
                    if (entry.getValue().isJsonArray()) {
                        JsonArray playerArray = entry.getValue().getAsJsonArray();
                        for (int j = 0; j < playerArray.size(); j++) {
                            Map<String, String> player = new HashMap<>();
                            JsonObject playerObj = playerArray.get(j).getAsJsonObject();
                            for (Entry<String, JsonElement> element : playerObj.entrySet()) {
                                player.put(element.getKey(), element.getValue().getAsString());
                            }
                            groupPlayerList.add(player);
                        }
                    } else {
                        payload.put(entry.getKey(), entry.getValue().getAsString());
                    }
                }
                mapList.add(payload);
                overallPlayerList.add(groupPlayerList);
            }
        } else if (jsonObject.has(PAYLOAD) && !arrayTrue) {
            Map<String, String> payload = new HashMap<>();
            JsonObject jsonPayload = jsonObject.get(PAYLOAD).getAsJsonObject();
            for (Entry<String, JsonElement> entry : jsonPayload.entrySet()) {
                if (entry.getValue().isJsonArray()) {
                    JsonArray playerArray = entry.getValue().getAsJsonArray();
                    for (int j = 0; j < playerArray.size(); j++) {
                        Map<String, String> player = new HashMap<>();
                        JsonObject playerObj = playerArray.get(j).getAsJsonObject();
                        for (Entry<String, JsonElement> element : playerObj.entrySet()) {
                            player.put(element.getKey(), element.getValue().getAsString());
                        }
                        groupPlayerList.add(player);
                    }
                } else {
                    payload.put(entry.getKey(), entry.getValue().getAsString());
                }
            }
            mapList.add(payload);
            overallPlayerList.add(groupPlayerList);
        } else {
            Map<String, String> player = new HashMap<>();
            Map<String, String> payload = new HashMap<>();
            payload.put("No Payload", "No Payload");
            player.put("No Player", "No Player");
            groupPlayerList.add(player);
            mapList.add(payload);
        }
        responsePayload.setPlayerList(overallPlayerList);
        responsePayload.setPayload(mapList);
        return responsePayload;
    }
}
