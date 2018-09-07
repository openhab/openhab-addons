/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.robonect.internal.model;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * Gson deserializer for deserializing timer mode values to the corresponding enum.
 *
 * @author Marco Meyer - Initial contribution
 */
public class TimerModeDeserializer implements JsonDeserializer<Timer.TimerMode> {
    

    @Override
    public Timer.TimerMode deserialize(JsonElement jsonElement, Type type,
                                       JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        int code = jsonElement.getAsInt();
        return Timer.TimerMode.fromCode(code);
    }
} 

