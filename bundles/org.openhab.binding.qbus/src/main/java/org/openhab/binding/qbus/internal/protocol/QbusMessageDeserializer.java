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

        String ctd = null;
        String cmd = null;
        Integer id = null;
        Integer state = null;
        Integer mode = null;
        Double measured = null;
        Double setpoint = null;
        Integer slats = null;

        QbusMessageBase message = null;

        JsonElement jsonOutputs = null;
        try {
            if (jsonObject.has("CTD")) {
                ctd = jsonObject.get("CTD").getAsString();
            }

            if (jsonObject.has("cmd")) {
                cmd = jsonObject.get("cmd").getAsString();
            }

            if (jsonObject.has("id")) {
                id = jsonObject.get("id").getAsInt();
            }

            if (jsonObject.has("state")) {
                state = jsonObject.get("state").getAsInt();
            }

            if (jsonObject.has("mode")) {
                mode = jsonObject.get("mode").getAsInt();
            }

            if (jsonObject.has("measured")) {
                measured = jsonObject.get("measured").getAsDouble();
            }

            if (jsonObject.has("setpoint")) {
                setpoint = jsonObject.get("setpoint").getAsDouble();
            }

            if (jsonObject.has("slats")) {
                slats = jsonObject.get("slats").getAsInt();
            }

            if (jsonObject.has("outputs")) {
                jsonOutputs = jsonObject.get("outputs");

            }

            if (ctd != null && cmd != null) {
                if (jsonOutputs != null) {
                    if (jsonOutputs.isJsonArray()) {
                        JsonArray jsonOutputsArray = jsonOutputs.getAsJsonArray();
                        message = new QbusMessageListMap();
                        message.setCmd(cmd);
                        message.setSn(ctd);

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

                } else {
                    message = new QbusMessageMap();

                    message.setCmd(cmd);
                    message.setSn(ctd);

                    if (id != null) {
                        message.setId(id);
                    }

                    if (state != null) {
                        message.setState(state);
                    }

                    if (slats != null) {
                        message.setSlatState(slats);
                    }

                    if (mode != null) {
                        message.setMode(mode);
                    }

                    if (measured != null) {
                        message.setMeasured(measured);
                    }

                    if (setpoint != null) {
                        message.setSetPoint(setpoint);
                    }

                }
            }
            return message;
        } catch (IllegalStateException e) {
            String mess = e.getMessage();
            throw new JsonParseException("Unexpected Json format  " + mess + " for " + jsonObject.toString());
        }
    }
}
