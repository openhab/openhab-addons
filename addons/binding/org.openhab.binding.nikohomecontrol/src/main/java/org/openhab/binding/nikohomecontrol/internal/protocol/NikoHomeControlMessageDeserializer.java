/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nikohomecontrol.internal.protocol;

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
 * Class {@link NikoHomeControlMessageDeserializer} deserializes all json messages from Niko Home Control. Various json
 * message formats are supported. The format is selected based on the content of the cmd and event json objects.
 *
 * @author Mark Herwege
 *
 */
class NikoHomeControlMessageDeserializer implements JsonDeserializer<NhcMessageBase> {

    @Override
    public NhcMessageBase deserialize(final JsonElement json, final Type typeOfT,
            final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();

        try {
            String cmd = null;
            String event = null;
            if (jsonObject.has("cmd")) {
                cmd = jsonObject.get("cmd").getAsString();
            }
            if (jsonObject.has("event")) {
                event = jsonObject.get("event").getAsString();
            }

            JsonElement jsonData = null;
            if (jsonObject.has("data")) {
                jsonData = jsonObject.get("data");
            }

            NhcMessageBase message = null;

            if (jsonData != null) {

                if (jsonData.isJsonObject()) {

                    message = new NhcMessageMap();

                    Map<String, String> data = new HashMap<>();
                    for (Entry<String, JsonElement> entry : jsonData.getAsJsonObject().entrySet()) {
                        data.put(entry.getKey(), entry.getValue().getAsString());
                    }
                    ((NhcMessageMap) message).setData(data);

                } else if (jsonData.isJsonArray()) {

                    JsonArray jsonDataArray = jsonData.getAsJsonArray();

                    message = new NhcMessageListMap();

                    List<Map<String, String>> dataList = new ArrayList<>();
                    for (int i = 0; i < jsonDataArray.size(); i++) {
                        JsonObject jsonDataObject = jsonDataArray.get(i).getAsJsonObject();

                        Map<String, String> data = new HashMap<>();
                        for (Entry<String, JsonElement> entry : jsonDataObject.entrySet()) {
                            data.put(entry.getKey(), entry.getValue().getAsString());
                        }
                        dataList.add(data);
                    }
                    ((NhcMessageListMap) message).setData(dataList);
                }

            }

            if (message != null) {
                message.setCmd(cmd);
                message.setEvent(event);
            } else {
                throw new JsonParseException("Unexpected Json type");
            }

            return message;

        } catch (IllegalStateException | ClassCastException e) {
            throw new JsonParseException("Unexpected Json type");
        }
    }
}
