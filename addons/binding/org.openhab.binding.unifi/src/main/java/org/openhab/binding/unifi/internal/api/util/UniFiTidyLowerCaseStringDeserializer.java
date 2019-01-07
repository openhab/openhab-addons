/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.unifi.internal.api.util;

import java.lang.reflect.Type;

import org.apache.commons.lang.StringUtils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 *
 * The {@link UniFiTidyLowerCaseStringDeserializer} is an implementation of {@link JsonDeserializer} that deserializes
 * strings in a tidy lower case format.
 *
 * @author Matthew Bowman - Initial contribution
 */
public class UniFiTidyLowerCaseStringDeserializer implements JsonDeserializer<String> {

    @Override
    public String deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        String s = json.getAsJsonPrimitive().getAsString();
        return StringUtils.lowerCase(StringUtils.strip(s));
    }

}
