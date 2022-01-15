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
package org.openhab.binding.squeezebox.internal.dto;

import java.lang.reflect.Type;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * The {@link ButtonDTODeserializer} is responsible for deserializing a button object, which
 * can either be an Integer, or a custom button specification.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class ButtonDTODeserializer implements JsonDeserializer<ButtonDTO> {

    @Override
    public ButtonDTO deserialize(JsonElement jsonElement, Type tyoeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        ButtonDTO button = null;
        if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber()) {
            Integer value = jsonElement.getAsInt();
            button = new ButtonDTO();
            button.custom = false;
            button.enabled = value != 0;
        } else if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            button = new ButtonDTO();
            button.custom = true;
            button.icon = jsonObject.get("icon").getAsString();
            button.jiveStyle = jsonObject.get("jiveStyle").getAsString();
            button.toolTip = jsonObject.get("tooltip").getAsString();
            button.command = StreamSupport.stream(jsonObject.getAsJsonArray("command").spliterator(), false)
                    .map(JsonElement::getAsString).collect(Collectors.joining(" "));
        }
        return button;
    }
}
