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
package org.openhab.binding.sleepiq.internal.api.impl.typeadapters;

import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sleepiq.internal.api.dto.SleepNumberRequest;
import org.openhab.binding.sleepiq.internal.api.enums.Side;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * The {@link SleepNumberRequestAdapter} serializes the request to set the sleep number.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
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
