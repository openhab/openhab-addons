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
package org.openhab.binding.pushover.internal.connection;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.pushover.internal.config.PushoverAccountConfiguration;
import org.openhab.binding.pushover.internal.dto.Sound;
import org.openhab.core.cache.ExpiringCacheMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link PushoverAPIConnection} is responsible for handling the connections to Pushover Messages API.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class PushoverAPIConnection {

    private final Logger logger = LoggerFactory.getLogger(PushoverAPIConnection.class);

    private static final String VALIDATE_URL = "https://api.pushover.net/1/users/validate.json";
    private static final String MESSAGE_URL = "https://api.pushover.net/1/messages.json";
    private static final String CANCEL_MESSAGE_URL = "https://api.pushover.net/1/receipts/{receipt}/cancel.json";
    private static final String SOUNDS_URL = "https://api.pushover.net/1/sounds.json";

    private final HttpClient httpClient;
    private final PushoverAccountConfiguration config;

    private final ExpiringCacheMap<String, String> cache = new ExpiringCacheMap<>(TimeUnit.DAYS.toMillis(1));

    private final JsonParser parser = new JsonParser();

    public PushoverAPIConnection(HttpClient httpClient, PushoverAccountConfiguration config) {
        this.httpClient = httpClient;
        this.config = config;
    }

    public boolean validateUser() throws PushoverCommunicationException, PushoverConfigurationException {
        return getMessageStatus(
                post(VALIDATE_URL, PushoverMessageBuilder.getInstance(config.apikey, config.user).build()));
    }

    public boolean sendMessage(PushoverMessageBuilder message)
            throws PushoverCommunicationException, PushoverConfigurationException {
        return getMessageStatus(post(MESSAGE_URL, message.build()));
    }

    public String sendPriorityMessage(PushoverMessageBuilder message)
            throws PushoverCommunicationException, PushoverConfigurationException {
        final JsonObject json = parser.parse(post(MESSAGE_URL, message.build())).getAsJsonObject();
        return getMessageStatus(json) && json.has("receipt") ? json.get("receipt").getAsString() : "";
    }

    public boolean cancelPriorityMessage(String receipt)
            throws PushoverCommunicationException, PushoverConfigurationException {
        return getMessageStatus(post(CANCEL_MESSAGE_URL.replace("{receipt}", receipt),
                PushoverMessageBuilder.getInstance(config.apikey, config.user).build()));
    }

    public List<Sound> getSounds() throws PushoverCommunicationException, PushoverConfigurationException {
        final String localApikey = config.apikey;
        if (localApikey == null || localApikey.isEmpty()) {
            throw new PushoverConfigurationException("@text/offline.conf-error-missing-apikey");
        }

        final Map<String, String> params = new HashMap<>(1);
        params.put(PushoverMessageBuilder.MESSAGE_KEY_TOKEN, localApikey);

        // TODO do not cache the response, cache the parsed list of sounds
        final JsonObject json = parser.parse(getFromCache(buildURL(SOUNDS_URL, params))).getAsJsonObject();
        if (json.has("sounds")) {
            final JsonObject sounds = json.get("sounds").getAsJsonObject();
            if (sounds != null) {
                return Collections.unmodifiableList(sounds.entrySet().stream()
                        .map(entry -> new Sound(entry.getKey(), entry.getValue().getAsString()))
                        .collect(Collectors.toList()));
            }
        }
        return Collections.emptyList();
    }

    private String buildURL(String url, Map<String, String> requestParams) {
        return requestParams.keySet().stream().map(key -> key + "=" + encodeParam(requestParams.get(key)))
                .collect(Collectors.joining("&", url + "?", ""));
    }

    private String encodeParam(@Nullable String value) {
        return value == null ? "" : URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private @Nullable String getFromCache(String url) {
        return cache.putIfAbsentAndGet(url, () -> get(url));
    }

    private String get(String url) throws PushoverCommunicationException, PushoverConfigurationException {
        return executeRequest(HttpMethod.GET, url, null);
    }

    private String post(String url, ContentProvider body)
            throws PushoverCommunicationException, PushoverConfigurationException {
        return executeRequest(HttpMethod.POST, url, body);
    }

    private String executeRequest(HttpMethod httpMethod, String url, @Nullable ContentProvider body)
            throws PushoverCommunicationException, PushoverConfigurationException {
        logger.trace("Pushover request: {} - URL = '{}'", httpMethod, url);
        try {
            final Request request = httpClient.newRequest(url).method(httpMethod).timeout(10, TimeUnit.SECONDS);

            if (body != null) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Pushover request body: '{}'", body);
                }
                request.content(body);
            }

            final ContentResponse contentResponse = request.send();

            final int httpStatus = contentResponse.getStatus();
            final String content = contentResponse.getContentAsString();
            logger.trace("Pushover response: status = {}, content = '{}'", httpStatus, content);
            switch (httpStatus) {
                case HttpStatus.OK_200:
                    return content;
                case HttpStatus.BAD_REQUEST_400:
                    logger.debug("Pushover server responded with status code {}: {}", httpStatus, content);
                    throw new PushoverConfigurationException(getMessageError(content));
                default:
                    logger.debug("Pushover server responded with status code {}: {}", httpStatus, content);
                    throw new PushoverCommunicationException(content);
            }
        } catch (ExecutionException e) {
            logger.debug("Exception occurred during execution: {}", e.getLocalizedMessage(), e);
            throw new PushoverCommunicationException(e.getLocalizedMessage(), e.getCause());
        } catch (InterruptedException | TimeoutException e) {
            logger.debug("Exception occurred during execution: {}", e.getLocalizedMessage(), e);
            throw new PushoverCommunicationException(e.getLocalizedMessage());
        }
    }

    private String getMessageError(String content) {
        final JsonObject json = parser.parse(content).getAsJsonObject();
        if (json.has("errors")) {
            final JsonArray errors = json.get("errors").getAsJsonArray();
            if (errors != null) {
                return errors.toString();
            }
        }
        return "Unknown error occured.";
    }

    private boolean getMessageStatus(String content) {
        final JsonObject json = parser.parse(content).getAsJsonObject();
        return json.has("status") ? json.get("status").getAsInt() == 1 : false;
    }

    private boolean getMessageStatus(JsonObject json) {
        return json.has("status") ? json.get("status").getAsInt() == 1 : false;
    }
}
