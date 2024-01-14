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
package org.openhab.binding.tankerkoenig.internal.serializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.openhab.binding.tankerkoenig.internal.dto.LittleStation;
import org.openhab.binding.tankerkoenig.internal.dto.Prices;
import org.openhab.binding.tankerkoenig.internal.dto.TankerkoenigListResult;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/***
 * Custom Deserializer fopr the list result of tankerkoenigs api response
 *
 * @author Dennis Dollinger - Initial contribution
 */
public class CustomTankerkoenigListResultDeserializer implements JsonDeserializer<TankerkoenigListResult> {

    private final Gson gson = new Gson();

    @Override
    public TankerkoenigListResult deserialize(final JsonElement json, final Type typeOfT,
            final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();

        final Boolean isOK = jsonObject.get("ok").getAsBoolean();
        TankerkoenigListResult result = new TankerkoenigListResult();
        if (isOK) {
            result.setOk(jsonObject.get("ok").getAsBoolean());
            JsonObject jsonPrices = jsonObject.get("prices").getAsJsonObject();
            Set<Entry<String, JsonElement>> objects = jsonPrices.entrySet();
            Prices p = new Prices();
            result.setPrices(p);
            List<LittleStation> list = new ArrayList<>();
            for (Entry<String, JsonElement> entry : objects) {
                JsonElement jsonElement = entry.getValue();
                LittleStation station = gson.fromJson(jsonElement, LittleStation.class);
                station.setID(entry.getKey());
                list.add(station);
            }
            result.getPrices().setStations(list);
        } else {
            result.setOk(jsonObject.get("ok").getAsBoolean());
            result.setMessage(jsonObject.get("message").getAsString());
        }
        return result;
    }
}
