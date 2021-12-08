/*
 * Copyright 2021 Mark Hilbush
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openhab.binding.sleepiq.api.impl.typeadapters;

import java.lang.reflect.Type;

import org.openhab.binding.sleepiq.api.enums.Side;
import org.openhab.binding.sleepiq.api.model.SleepNumberRequest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class SleepNumberRequestAdapter implements JsonSerializer<SleepNumberRequest> {

    @Override
    public JsonElement serialize(SleepNumberRequest src, Type typeOfSrc, JsonSerializationContext context) {
        // Sleep number must be >= 5 and <= 100, and be a multiple of 5
        int sleepNumber = src.getSleepNumber();
        sleepNumber = Math.min(100, sleepNumber);
        sleepNumber = Math.max(5, sleepNumber);
        sleepNumber = 5 * (int) (Math.round(sleepNumber / 5.0));

        JsonObject obj = new JsonObject();
        obj.addProperty("bedId", src.getBedId());
        obj.addProperty("sleepNumber", sleepNumber);
        obj.addProperty("side", src.getSide().equals(Side.LEFT) ? "L" : "R");
        return obj;
    }
}
