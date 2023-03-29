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
package org.openhab.binding.sleepiq.internal.api.impl.typeadapters;

import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sleepiq.internal.api.enums.FoundationActuatorSpeed;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * The {@link FoundationActuatorSpeedTypeAdapter} converts the FoundationActuatorSpeed enum
 * to the format expected by the sleepiq API.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class FoundationActuatorSpeedTypeAdapter implements JsonSerializer<FoundationActuatorSpeed> {

    @Override
    public JsonElement serialize(FoundationActuatorSpeed speed, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(speed.value());
    }
}
