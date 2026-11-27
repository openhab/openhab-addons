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
package org.openhab.binding.veluxactive.internal.api.dto.serialization;

import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.veluxactive.internal.VeluxActiveTokenResponse;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * Maps the provider token response to the core OAuth response.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public final class VeluxActiveTokenResponseDeserializer implements JsonDeserializer<AccessTokenResponse> {

    private final Gson gson = new GsonBuilder().create();

    @Override
    public @Nullable AccessTokenResponse deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        try {
            VeluxActiveTokenResponse response = gson.fromJson(json, VeluxActiveTokenResponse.class);
            return response == null ? null : response.toStandard();
        } catch (RuntimeException e) {
            // Parser exceptions can contain token values; do not propagate their messages or causes.
            throw new JsonParseException("Invalid VELUX ACTIVE token response");
        }
    }
}
