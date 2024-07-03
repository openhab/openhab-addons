/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.meteofrance.internal.deserialization;

import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.meteofrance.internal.dto.MeteoFrance.Period;
import org.openhab.binding.meteofrance.internal.dto.MeteoFrance.Periods;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * The {@link PeriodsDeserializer} is a specialized deserializer aimed to transform
 * a list of `Period` into a map identified by the Period Term.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
class PeriodsDeserializer implements JsonDeserializer<Periods> {
    @Override
    public @NonNull Periods deserialize(JsonElement json, Type clazz, JsonDeserializationContext context)
            throws JsonParseException {
        Periods result = new Periods();
        if (json instanceof JsonArray jsonArray) {
            jsonArray.forEach(item -> {
                Period period = context.deserialize(item, Period.class);
                result.put(period.echeance(), period);
            });
        }
        return result;
    }
}
