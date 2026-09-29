/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.evcc.internal.handler.routing;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Extracts a nested field from a JSON object.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class ObjectFieldExtraction implements ExtractionStrategy {

    private final String fieldName;

    public ObjectFieldExtraction(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public @Nullable JsonElement extract(JsonElement source) {
        if (!source.isJsonObject()) {
            return null;
        }
        JsonObject object = source.getAsJsonObject();
        return object.get(fieldName);
    }

    @Override
    public String describe() {
        return "ObjectField{" + fieldName + "}";
    }
}
