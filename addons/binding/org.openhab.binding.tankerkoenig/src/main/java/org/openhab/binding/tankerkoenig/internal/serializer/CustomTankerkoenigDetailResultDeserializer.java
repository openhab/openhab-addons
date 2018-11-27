/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tankerkoenig.internal.serializer;

import java.lang.reflect.Type;

import org.openhab.binding.tankerkoenig.internal.config.LittleStation;
import org.openhab.binding.tankerkoenig.internal.config.OpeningTime;
import org.openhab.binding.tankerkoenig.internal.config.OpeningTimes;
import org.openhab.binding.tankerkoenig.internal.config.TankerkoenigDetailResult;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/***
 * Custom Deserializer for the detail result of tankerkoenigs api response
 *
 * @author Jürgen Baginski - Initial contribution
 */
public class CustomTankerkoenigDetailResultDeserializer implements JsonDeserializer<TankerkoenigDetailResult> {

    @Override
    public TankerkoenigDetailResult deserialize(final JsonElement json, final Type typeOfT,
            final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();
        final Boolean isOK = jsonObject.get("ok").getAsBoolean();
        TankerkoenigDetailResult result = new TankerkoenigDetailResult();
        if (isOK) {
            final JsonObject jsonStation = jsonObject.get("station").getAsJsonObject();
            final Boolean isWholeDay = jsonStation.get("wholeDay").getAsBoolean();
            final LittleStation littleStation = new LittleStation();
            if (!jsonStation.get("e10").isJsonNull()) {
                final String e10 = jsonStation.get("e10").getAsString();
                littleStation.setE10(e10);
            }
            if (!jsonStation.get("e5").isJsonNull()) {
                final String e5 = jsonStation.get("e5").getAsString();
                littleStation.setE5(e5);
            }
            if (!jsonStation.get("diesel").isJsonNull()) {
                final String diesel = jsonStation.get("diesel").getAsString();
                littleStation.setDiesel(diesel);
            }
            final Boolean isOpen = jsonStation.get("isOpen").getAsBoolean();
            final String stationID = jsonStation.get("id").getAsString();
            OpeningTime[] openingTime = context.deserialize(jsonStation.get("openingTimes"), OpeningTime[].class);
            littleStation.setOpen(isOpen);
            littleStation.setID(stationID);
            final OpeningTimes openingTimes = new OpeningTimes(stationID, isWholeDay, openingTime);
            result.setLittleStation(littleStation);
            result.setOk(isOK);
            result.setwholeDay(isWholeDay);
            result.setOpeningTimes(openingTimes);
        } else {
            final String message = jsonObject.get("message").getAsString();
            result.setOk(isOK);
            result.setMessage(message);
        }
        return result;
    }
}
