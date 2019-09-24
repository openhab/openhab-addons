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
package org.openhab.binding.surepetcare.internal;

import java.lang.reflect.Type;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * The {@link GsonColonDateTypeAdapter} is a GSON java.utilDate serializer which deals with the colon support in the
 * timezone offset.
 *
 * @author Rene Scherer - Initial contribution
 */
public class GsonColonDateTypeAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {

    public class ColonDateFormat extends SimpleDateFormat {

        private static final long serialVersionUID = -6266537452934495628L;

        public ColonDateFormat(String string) {
            super(string);
        }

        @Override
        public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition pos) {
            StringBuffer rfcFormat = super.format(date, toAppendTo, pos);
            return rfcFormat.insert(rfcFormat.length() - 2, ":");
        }

        @Override
        public Date parse(String text, ParsePosition pos) {
            String fixedText = text;
            if (text.length() > 3) {
                fixedText = text.substring(0, text.length() - 3) + text.substring(text.length() - 2);
            }
            return super.parse(fixedText, pos);
        }

    }

    private final ColonDateFormat dateFormat;

    public GsonColonDateTypeAdapter() {
        dateFormat = new ColonDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public synchronized JsonElement serialize(Date date, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(dateFormat.format(date));
    }

    @Override
    public synchronized Date deserialize(JsonElement jsonElement, Type type,
            JsonDeserializationContext jsonDeserializationContext) {
        try {
            return dateFormat.parse(jsonElement.getAsString());
        } catch (ParseException e) {
            throw new JsonParseException(e);
        }
    }
}
