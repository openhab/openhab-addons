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

import static org.openhab.binding.heos.internal.resources.HeosConstants.*;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * The {@link HeosDeserializer} deserialize the JSON message
 * which is received by the HEOS bridge.
 *
 * @author Johannes Einig - Initial contribution
 */
public class HeosDeserializerEvent implements JsonDeserializer<HeosResponseEvent> {

    private HeosResponseEvent responseHeos = new HeosResponseEvent();

    private String rawCommand;
    private String rawResult;
    private String rawMessage;

    private String eventType;
    private String commandType;
    private Map<String, String> messages = new HashMap<>();

    @Override
    public HeosResponseEvent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        final JsonObject jsonObject;
        final JsonObject jsonHeos;

        if (json.isJsonObject()) {
            jsonObject = json.getAsJsonObject();
            jsonHeos = jsonObject.get(HEOS).getAsJsonObject();
        } else {
            return null;
        }

        // sets the Basic command String and decodes it afterwards to eventType and commandType
        this.rawCommand = jsonHeos.get(COMMAND).getAsString();
        decodeCommand(rawCommand);
        responseHeos.setCommand(rawCommand);
        responseHeos.setEventType(eventType);
        responseHeos.setCommandType(commandType);

        // not all Messages has a result field. Field is only set if check is true
        if (jsonHeos.has(RESULT)) {
            this.rawResult = jsonHeos.get(RESULT).getAsString();
            responseHeos.setResult(rawResult);
        } else {
            responseHeos.setResult("null");
        }
        // not all Messages has a message field. Field is only set if check is true
        if (jsonHeos.has(MESSAGE)) {
            if (!jsonHeos.get(MESSAGE).getAsString().isEmpty()) {
                this.rawMessage = jsonHeos.get(MESSAGE).getAsString();
                responseHeos.setMessage(rawMessage); // raw Message
                decodeMessage(rawMessage);
                responseHeos.setMessagesMap(messages);
            } else {
                this.messages.put(COM_UNDER_PROCESS, FALSE);
                responseHeos.setMessagesMap(messages);
            }
        }
        if (rawResult.equals(FAIL)) {
            responseHeos.setErrorCode(messages.get(EID));
            responseHeos.setErrorMessage(messages.get(TEXT));
        }
        return responseHeos;
    }

    private void decodeMessage(String message) {
        if (message.contains(COM_UNDER_PROCESS)) {
            this.messages.put(COM_UNDER_PROCESS, TRUE);
            return;
        }

        this.messages.put(COM_UNDER_PROCESS, FALSE);

        String input = "&" + message;
        int start = 0;
        int stop = 0;

        while (stop >= 0) {
            start = input.indexOf("&", start) + 1;
            stop = input.indexOf("=", start);
            if (stop < 0) {
                this.messages.put(MESSAGE, message);
                return;
            }
            String key = input.substring(start, stop);
            start = stop + 1;
            stop = input.indexOf("&", start);
            String value;
            if (stop < 0) {
                value = input.substring(start, input.length());
            } else {
                value = input.substring(start, stop);
            }
            this.messages.put(key, value);
        }
    }

    private void decodeCommand(String command) {
        int start = 0;
        int stop = 0;
        if (command.indexOf("/") > 0) {
            stop = command.indexOf("/", start);
            this.eventType = command.substring(start, stop);
            this.commandType = command.substring(stop + 1);
        } else {
            this.eventType = ERROR;
            this.commandType = command;
        }
    }
}
