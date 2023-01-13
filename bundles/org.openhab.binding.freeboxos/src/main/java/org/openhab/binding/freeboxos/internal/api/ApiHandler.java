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
package org.openhab.binding.freeboxos.internal.api;

import static org.openhab.binding.freeboxos.internal.api.ApiConstants.*;

import java.net.URI;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpStatus.Code;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.ErrorCode;
import org.openhab.binding.freeboxos.internal.api.player.PlayerStatusForegroundApp;
import org.openhab.core.i18n.TimeZoneProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link ApiHandler} is responsible for sending requests toward a given url and transform the answer in appropriate
 * DTO.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ApiHandler {
    private final Logger logger = LoggerFactory.getLogger(ApiHandler.class);
    private final HttpClient httpClient;
    private final Gson gson;

    private long timeoutInMs = TimeUnit.SECONDS.toMillis(8);

    public ApiHandler(HttpClient httpClient, TimeZoneProvider timeZoneProvider) {
        this.httpClient = httpClient;
        this.gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(ZonedDateTime.class,
                        (JsonDeserializer<ZonedDateTime>) (json, type, jsonDeserializationContext) -> {
                            long timestamp = json.getAsJsonPrimitive().getAsLong();
                            Instant i = Instant.ofEpochSecond(timestamp);
                            return ZonedDateTime.ofInstant(i, timeZoneProvider.getTimeZone());
                        })
                .registerTypeAdapter(PlayerStatusForegroundApp.class, new ForegroundAppDeserializer())
                .registerTypeAdapter(List.class, new ArrayListDeserializer())
                .registerTypeAdapterFactory(new StrictEnumTypeAdapterFactory()).create();
    }

    public synchronized <T> T executeUri(URI uri, HttpMethod method, Class<T> clazz, @Nullable String sessionToken,
            @Nullable Object payload) throws FreeboxException {
        logger.debug("executeUrl {} : {} ", method, uri);

        Request request = httpClient.newRequest(uri).method(method).timeout(timeoutInMs, TimeUnit.MILLISECONDS)
                .header(HttpHeader.CONTENT_TYPE, CONTENT_TYPE);

        if (sessionToken != null) {
            request.header(AUTH_HEADER, sessionToken);
        }

        if (payload != null) {
            request.content(serialize(payload), null);
        }

        try {
            ContentResponse response = request.send();
            Code statusCode = HttpStatus.getCode(response.getStatus());
            String content = new String(response.getContent(), DEFAULT_CHARSET);
            logger.trace("executeUrl {} - {} returned {}", method, uri, content);
            if (statusCode == Code.OK) {
                return deserialize(clazz, content);
            } else if (statusCode == Code.FORBIDDEN) {
                logger.debug("Fobidden, serviceReponse was {}, ", content);
                throw new FreeboxException(ErrorCode.AUTH_REQUIRED);
            }
            throw new FreeboxException("Error '%s' requesting : %s", statusCode.getMessage(), uri.toString());
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new FreeboxException(e, "Exception while calling %s", request.getURI());
        }
    }

    public <T> T deserialize(Class<T> clazz, String json) throws FreeboxException {
        try {
            @Nullable
            T result = gson.fromJson(json, clazz);
            if (result != null) {
                return result;
            }
            throw new FreeboxException("Deserialization of '%s' resulted in null value", json);
        } catch (JsonSyntaxException e) {
            throw new FreeboxException(e, "Unexpected error deserializing '%s'", json);
        }
    }

    public <T> T deserialize(Class<T> clazz, JsonElement json) throws FreeboxException {
        try {
            @Nullable
            T result = gson.fromJson(json, clazz);
            if (result != null) {
                return result;
            }
            throw new FreeboxException("Deserialization of '%s' resulted in null value", json);
        } catch (JsonSyntaxException e) {
            throw new FreeboxException(e, "Unexpected error deserializing '%s'", json);
        }
    }

    public ContentProvider serialize(Object payload) {
        return new StringContentProvider(gson.toJson(payload), DEFAULT_CHARSET);
    }

    public void setTimeout(long millis) {
        timeoutInMs = millis;
        logger.debug("Timeout set to {} ms", timeoutInMs);
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }
}
