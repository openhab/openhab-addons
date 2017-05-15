/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.tankerkoenig.internal.serializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;

import org.openhab.binding.tankerkoenig.internal.config.LittleStation;
import org.openhab.binding.tankerkoenig.internal.config.Prices;
import org.openhab.binding.tankerkoenig.internal.config.TankerkoenigListResult;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/***
 * Custom Deserializer fopr the list result of tankerkoenigs api response
 *
 * @author Dennis Dollinger
 *
 */
public class CustomTankerkoenigListResultDeserializer implements JsonDeserializer<TankerkoenigListResult> {

    @Override
    public TankerkoenigListResult deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        final JsonObject jsonObject = json.getAsJsonObject();

        TankerkoenigListResult result = new TankerkoenigListResult();
        result.setOk(jsonObject.get("ok").getAsBoolean());
        JsonObject jsonPrices = jsonObject.get("prices").getAsJsonObject();
        Set<Entry<String, JsonElement>> objects = jsonPrices.entrySet();
        Gson gson = new Gson();
        Prices p = new Prices();
        result.setPrices(p);
        ArrayList<LittleStation> list = new ArrayList<>();
        for (Entry<String, JsonElement> entry : objects) {
            JsonElement jsonElement = entry.getValue();
            LittleStation station = gson.fromJson(jsonElement, LittleStation.class);
            station.setID(entry.getKey());
            list.add(station);
        }
        result.getPrices().setStations(list);
        return result;
    }
}
