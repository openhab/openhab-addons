/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.heos.internal.resources;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    @Override
    public HeosResponsePayload deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        boolean arrayTrue = false;
        List<HashMap<String, String>> mapList = new ArrayList<HashMap<String, String>>();
        List<List<HashMap<String, String>>> overallPlayerList = new ArrayList<List<HashMap<String, String>>>();
        List<HashMap<String, String>> groupPlayerList = new ArrayList<HashMap<String, String>>();

        JsonObject jsonObject = json.getAsJsonObject();

        if (jsonObject.has("payload")) {
            if (jsonObject.get("payload").isJsonArray()) {
                arrayTrue = true;
            }
        }

        if (jsonObject.has("payload") && arrayTrue) {
            JsonArray jsonArray = jsonObject.get("payload").getAsJsonArray();

            for (int i = 0; i < jsonArray.size(); i++) {
                HashMap<String, String> payload = new HashMap<String, String>();

                JsonObject object = jsonArray.get(i).getAsJsonObject();

                for (Entry<String, JsonElement> entry : object.entrySet()) {
                    if (entry.getValue().isJsonArray()) {
                        JsonArray playerArray = entry.getValue().getAsJsonArray();
                        for (int j = 0; j < playerArray.size(); j++) {
                            HashMap<String, String> player = new HashMap<String, String>();
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
        } else if (jsonObject.has("payload") && !arrayTrue) {
            HashMap<String, String> payload = new HashMap<String, String>();
            JsonObject jsonPayload = jsonObject.get("payload").getAsJsonObject();
            for (Entry<String, JsonElement> entry : jsonPayload.entrySet()) {
                if (entry.getValue().isJsonArray()) {
                    JsonArray playerArray = entry.getValue().getAsJsonArray();
                    for (int j = 0; j < playerArray.size(); j++) {
                        HashMap<String, String> player = new HashMap<String, String>();
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
            HashMap<String, String> player = new HashMap<String, String>();
            HashMap<String, String> payload = new HashMap<String, String>();
            payload.put("No Payload", "No Payload");
            player.put("No Player", "No Player");
            groupPlayerList.add(player);
            mapList.add(payload);
        }

        responsePayload.setPlayerList(overallPlayerList);
        responsePayload.setPayload(mapList);
        return responsePayload;
    }

    public void itterateValues() {
    }
}
