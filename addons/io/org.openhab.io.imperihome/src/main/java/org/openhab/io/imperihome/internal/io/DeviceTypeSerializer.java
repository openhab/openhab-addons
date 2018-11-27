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

import org.openhab.io.imperihome.internal.model.device.DeviceType;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Serializer for {@link DeviceType}.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class DeviceTypeSerializer implements JsonSerializer<DeviceType> {

    @Override
    public JsonElement serialize(DeviceType deviceType, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(deviceType.getApiString());
    }

}
