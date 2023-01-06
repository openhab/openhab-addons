/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.unifi.internal.api.util;

import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.unifi.internal.api.dto.UnfiPortOverrideJsonObject;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Serializer for {@link UnfiPortOverrideJsonObject}. Returns the content of the jsonObject in the class.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class UnfiPortOverrideJsonElementDeserializer implements JsonSerializer<UnfiPortOverrideJsonObject> {

    @Override
    public JsonElement serialize(final UnfiPortOverrideJsonObject src, final Type typeOfSrc,
            final JsonSerializationContext context) {
        return src.getJsonObject();
    }
}
