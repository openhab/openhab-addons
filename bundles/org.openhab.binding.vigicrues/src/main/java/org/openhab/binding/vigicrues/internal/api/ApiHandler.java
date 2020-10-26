/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.vigicrues.internal.api;

import java.io.IOException;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpUtil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link ApiHandler} is the responsible to call a given
 * url and transform the answer in the appropriate dto class
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ApiHandler {
    private static final int TIMEOUT_MS = 30000;
    private final Gson gson;

    public ApiHandler(TimeZoneProvider timeZoneProvider) {
        this.gson = new GsonBuilder().registerTypeAdapter(ZonedDateTime.class,
                (JsonDeserializer<ZonedDateTime>) (json, type, jsonDeserializationContext) -> ZonedDateTime
                        .parse(json.getAsJsonPrimitive().getAsString())
                        .withZoneSameInstant(timeZoneProvider.getTimeZone()))
                .create();
    }

    public <T> T execute(ApiRequest request) throws VigiCruesException {
        String jsonResponse = "";
        try {
            jsonResponse = HttpUtil.executeUrl("GET", request.getUrl(), TIMEOUT_MS);
            @SuppressWarnings("unchecked")
            T fullResponse = gson.fromJson(jsonResponse, (Class<T>) request.getResponseClass());
            return fullResponse;
        } catch (IOException e) {
            throw new VigiCruesException(e);
        } catch (JsonSyntaxException e1) {
            throw new VigiCruesException(jsonResponse, e1);
        }
    }
}
