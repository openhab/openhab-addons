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
package org.openhab.binding.netatmo.internal.deserialization;

import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.dto.NetatmoAccessTokenResponse;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * Specialized deserializer for {@link NetatmoAccessTokenResponse}
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class AccessTokenResponseDeserializer implements JsonDeserializer<AccessTokenResponse> {

    private final Gson gson = new GsonBuilder().create();

    @Override
    public @Nullable AccessTokenResponse deserialize(JsonElement element, Type arg1, JsonDeserializationContext arg2)
            throws JsonParseException {
        NetatmoAccessTokenResponse response = gson.fromJson(element, NetatmoAccessTokenResponse.class);
        if (response == null) {
            return null;
        }
        return response.toStandard();
    }
}
