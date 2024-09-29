/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpStatus.Code;
import org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants;
import org.openhab.binding.freeboxos.internal.api.deserialization.ForegroundAppDeserializer;
import org.openhab.binding.freeboxos.internal.api.deserialization.ListDeserializer;
import org.openhab.binding.freeboxos.internal.api.deserialization.StrictEnumTypeAdapterFactory;
import org.openhab.binding.freeboxos.internal.api.rest.PlayerManager.ForegroundApp;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import inet.ipaddr.MACAddressString;
import inet.ipaddr.mac.MACAddress;

/**
 * The {@link ApiHandler} is responsible for sending requests toward a given url and transform the answer in appropriate
 * DTO.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ApiHandler {
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private static final String CONTENT_TYPE = "application/json; charset=" + DEFAULT_CHARSET.name();
    private static final int RESPONSE_BUFFER_SIZE = 65536;
    public static final String AUTH_HEADER = "X-Fbx-App-Auth";

    private final Logger logger = LoggerFactory.getLogger(ApiHandler.class);
    private final HttpClient httpClient;
    private final Gson gson;

    private long timeoutInMs = TimeUnit.SECONDS.toMillis(10);

    public ApiHandler(HttpClientFactory httpClientFactory, TimeZoneProvider timeZoneProvider) {
        this.gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(ZonedDateTime.class,
                        (JsonDeserializer<ZonedDateTime>) (json, type, jsonDeserializationContext) -> {
                            long timestamp = json.getAsJsonPrimitive().getAsLong();
                            Instant i = Instant.ofEpochSecond(timestamp);
                            return ZonedDateTime.ofInstant(i, timeZoneProvider.getTimeZone());
                        })
                .registerTypeAdapter(MACAddress.class,
                        (JsonDeserializer<MACAddress>) (json, type,
                                jsonDeserializationContext) -> new MACAddressString(json.getAsString()).getAddress())
                .registerTypeAdapter(IPAddress.class,
                        (JsonDeserializer<IPAddress>) (json, type,
                                jsonDeserializationContext) -> new IPAddressString(json.getAsString()).getAddress())
                .registerTypeAdapter(ForegroundApp.class, new ForegroundAppDeserializer())
                .registerTypeAdapter(List.class, new ListDeserializer()).serializeNulls()
                .registerTypeAdapterFactory(new StrictEnumTypeAdapterFactory()).create();
        httpClient = httpClientFactory.createHttpClient(FreeboxOsBindingConstants.BINDING_ID);
        httpClient.setResponseBufferSize(RESPONSE_BUFFER_SIZE);
        try {
            httpClient.start();
        } catch (Exception e) {
            logger.warn("Unable to start httpClient: {}", e.getMessage());
        }
    }

    public void dispose() {
        try {
            httpClient.stop();
        } catch (Exception e) {
            logger.warn("Unable to stop httpClient: {}", e.getMessage());
        }
    }

    public synchronized <T> T executeUri(URI uri, HttpMethod method, Class<T> clazz, @Nullable String sessionToken,
            @Nullable Object payload) throws FreeboxException, InterruptedException {
        logger.debug("executeUrl {}: {} ", method, uri);

        Request request = httpClient.newRequest(uri).method(method).timeout(timeoutInMs, TimeUnit.MILLISECONDS)
                .header(HttpHeader.CONTENT_TYPE, CONTENT_TYPE);

        if (sessionToken != null) {
            request.header(AUTH_HEADER, sessionToken);
        }

        if (payload != null) {
            request.content(new StringContentProvider(serialize(payload), DEFAULT_CHARSET), null);
        }

        try {
            ContentResponse response = request.send();

            Code statusCode = HttpStatus.getCode(response.getStatus());

            if (statusCode != Code.OK && statusCode != Code.FORBIDDEN) {
                throw new FreeboxException(statusCode.getMessage());
            }

            String content = new String(response.getContent(), DEFAULT_CHARSET);
            T result = deserialize(clazz, content);
            logger.trace("executeUrl {} - {} returned {}", method, uri, content);

            if (statusCode == Code.OK) {
                return result;
            } else if (statusCode == Code.FORBIDDEN) {
                logger.debug("Fobidden, serviceReponse was {}, ", content);
                if (result instanceof Response<?> errorResponse) {
                    if (errorResponse.getErrorCode() == Response.ErrorCode.INSUFFICIENT_RIGHTS) {
                        throw new PermissionException(errorResponse.getMissingRight(), errorResponse.getMsg());
                    } else {
                        throw new FreeboxException(errorResponse.getErrorCode(), errorResponse.getMsg());
                    }
                }
            }

            throw new FreeboxException("Error '%s' requesting: %s", statusCode.getMessage(), uri.toString());
        } catch (TimeoutException | ExecutionException e) {
            throw new FreeboxException(e, "Exception while calling %s", request.getURI());
        }
    }

    public <T> T deserialize(Class<T> clazz, String json) {
        @Nullable
        T result = gson.fromJson(json, clazz);
        if (result != null) {
            return result;
        }
        throw new IllegalArgumentException("Null result deserializing '%s', please file a bug report.".formatted(json));
    }

    public String serialize(Object payload) {
        return gson.toJson(payload);
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public void setTimeout(long millis) {
        timeoutInMs = millis;
        logger.debug("Timeout set to {} ms", timeoutInMs);
    }
}
