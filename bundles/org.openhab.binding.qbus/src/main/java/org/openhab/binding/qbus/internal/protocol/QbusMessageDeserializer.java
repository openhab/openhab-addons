/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.qbus.internal.protocol;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Class {@link QbusMessageDeserializer} deserializes all json messages from Qbus. Various json
 * message formats are supported. The format is selected based on the content of the cmd and event json objects.
 *
 * @author Koen Schockaert - Initial Contribution
 *
 */

@NonNullByDefault
class QbusMessageDeserializer implements JsonDeserializer<QbusMessageBase> {

    @Override
    public @Nullable QbusMessageBase deserialize(final JsonElement json, final Type typeOfT,
            final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();

        try {
            String cmd = null;
            String CTD = null;

            if (jsonObject.has("cmd")) {
                cmd = jsonObject.get("cmd").getAsString();
            }

            if (jsonObject.has("CTD")) {
                CTD = jsonObject.get("CTD").getAsString();
            }

            JsonElement jsonOutputs = null;

            if (jsonObject.has("outputs")) {
                jsonOutputs = jsonObject.get("outputs");
            }

            QbusMessageBase message = null;

            if (jsonOutputs != null) {
                if (jsonOutputs.isJsonObject()) {
                    message = new QbusMessageMap();

                    Map<String, String> outputs = new HashMap<>();
                    for (Entry<String, JsonElement> entry : jsonOutputs.getAsJsonObject().entrySet()) {
                        outputs.put(entry.getKey(), entry.getValue().getAsString());
                    }
                    ((QbusMessageMap) message).setOutputs(outputs);

                } else if (jsonOutputs.isJsonArray()) {
                    JsonArray jsonOutputsArray = jsonOutputs.getAsJsonArray();

                    message = new QbusMessageListMap();

                    List<Map<String, String>> outputsList = new ArrayList<>();
                    for (int i = 0; i < jsonOutputsArray.size(); i++) {
                        JsonObject jsonOutputsObject = jsonOutputsArray.get(i).getAsJsonObject();

                        Map<String, String> outputs = new HashMap<>();
                        for (Entry<String, JsonElement> entry : jsonOutputsObject.entrySet()) {
                            outputs.put(entry.getKey(), entry.getValue().getAsString());
                        }
                        outputsList.add(outputs);
                    }
                    ((QbusMessageListMap) message).setOutputs(outputsList);
                }
            }

            if (message != null && cmd != null && CTD != null) {
                message.setCmd(cmd);
                message.setSn(CTD);
            } else {
                throw new JsonParseException("Unexpected Json type");
            }

            return message;

        } catch (IllegalStateException e) {
            throw new JsonParseException("Unexpected Json type");
        }
    }
}
