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
import java.util.Calendar;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

/**
 * The {@link UniFiTimestampDeserializer} is an implementation of {@link JsonDeserializer} that deserializes timestamps
 * returned in the JSON responses of the UniFi controller.
 *
 * @author Matthew Bowman - Initial contribution
 */
public class UniFiTimestampDeserializer implements JsonDeserializer<Calendar> {

    @Override
    public Calendar deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
        String text = json.getAsJsonPrimitive().getAsString();
        long millis = Long.valueOf(text) * 1000;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        return cal;
    }

}
