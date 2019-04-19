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
