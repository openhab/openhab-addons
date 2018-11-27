/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.imperihome.internal.io;

import java.lang.reflect.Type;

import org.openhab.io.imperihome.internal.model.param.DeviceParam;
import org.openhab.io.imperihome.internal.model.param.DeviceParameters;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Serializer for {@link DeviceParameters}.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class DeviceParametersSerializer implements JsonSerializer<DeviceParameters> {

    @Override
    public JsonElement serialize(DeviceParameters params, Type type,
            JsonSerializationContext jsonSerializationContext) {
        JsonArray result = new JsonArray();
        for (DeviceParam param : params.values()) {
            result.add(jsonSerializationContext.serialize(param));
        }
        return result;
    }

}
