/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.scalarweb.gson;

import java.lang.reflect.Type;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebResult;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * This class represents the deserializer to deserialize a json element to a {@link ScalarWebResult}
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class ScalarWebResultDeserializer implements JsonDeserializer<ScalarWebResult> {
    @Override
    public ScalarWebResult deserialize(final @Nullable JsonElement je, final @Nullable Type type,
            final @Nullable JsonDeserializationContext context) throws JsonParseException {
        Objects.requireNonNull(je, "je cannot be null");
        Objects.requireNonNull(type, "type cannot be null");
        Objects.requireNonNull(context, "context cannot be null");

        if (je instanceof JsonObject) {
            final JsonObject jo = je.getAsJsonObject();

            int id = -1;

            final JsonElement idElm = jo.get("id");
            if (idElm != null && idElm.isJsonPrimitive()) {
                id = idElm.getAsInt();
            }

            return new ScalarWebResult(id, GsonUtilities.getArray(jo, "result"), GsonUtilities.getArray(jo, "error"));
        }
        throw new JsonParseException("The json element isn't a JsonObject and cannot be deserialized");
    }
}
