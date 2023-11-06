/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.venstarthermostat.internal.dto;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * The {@link VenstarSystemStateSerializer} parses system state values
 * from the REST API JSON.
 *
 * @author William Welliver - Initial contribution
 * @author Matthew Davies - added IllegalArgumentException handling
 */
public class VenstarSystemStateSerializer implements JsonDeserializer<VenstarSystemState> {
    @Override
    public VenstarSystemState deserialize(JsonElement element, Type arg1, JsonDeserializationContext arg2)
            throws JsonParseException {
        int key = element.getAsInt();
        try {
            return VenstarSystemState.fromInt(key);
        } catch (IllegalArgumentException e) {
            throw new JsonParseException(e);
        }
    }
}
