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
package org.openhab.binding.pushover.internal.connection;

import static org.openhab.binding.pushover.internal.PushoverBindingConstants.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.i18n.CommunicationException;
import org.openhab.core.i18n.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link PushoverAPIConnection} is responsible for handling the connections to Pushover Messages API.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class PushoverAPIConnection {

    private static final String JSON_VALUE_ERRORS = "errors";
    private static final String JSON_VALUE_RECEIPT = "receipt";
    private static final String JSON_VALUE_SOUNDS = "sounds";
    private static final String JSON_VALUE_STATUS = "status";

    private final Logger logger = LoggerFactory.getLogger(PushoverAPIConnection.class);

    private static final String VALIDATE_URL = "https://api.pushover.net/1/users/validate.json";
    private static final String MESSAGE_URL = "https://api.pushover.net/1/messages.json";
    private static final String CANCEL_MESSAGE_URL = "https://api.pushover.net/1/receipts/%s/cancel.json";
    private static final String SOUNDS_URL = "https://api.pushover.net/1/sounds.json";

    private final HttpClient httpClient;
    private final PushoverAccountConfiguration config;

    private final ExpiringCache<List<Sound>> cache = new ExpiringCache<>(TimeUnit.DAYS.toMillis(1),
            this::getSoundsFromSource);

    public PushoverAPIConnection(HttpClient httpClient, PushoverAccountConfiguration config) {
        this.httpClient = httpClient;
        this.config = config;
    }

    public boolean validateUser() throws CommunicationException, ConfigurationException {
        return getMessageStatus(
                post(VALIDATE_URL, PushoverMessageBuilder.getInstance(config.apikey, config.user).build()));
    }

    public boolean sendMessage(PushoverMessageBuilder message) throws CommunicationException, ConfigurationException {
        return getMessageStatus(post(MESSAGE_URL, message.build()));
    }

    public String sendPriorityMessage(PushoverMessageBuilder message)
            throws CommunicationException, ConfigurationException {
        final JsonObject json = JsonParser.parseString(post(MESSAGE_URL, message.build())).getAsJsonObject();
        return getMessageStatus(json) && json.has(JSON_VALUE_RECEIPT) ? json.get(JSON_VALUE_RECEIPT).getAsString() : "";
    }

    public boolean cancelPriorityMessage(String receipt) throws CommunicationException, ConfigurationException {
        return getMessageStatus(post(String.format(CANCEL_MESSAGE_URL, receipt),
                PushoverMessageBuilder.getInstance(config.apikey, config.user).build()));
    }

    public @Nullable List<Sound> getSounds() {
        return cache.getValue();
    }

    private List<Sound> getSoundsFromSource() throws CommunicationException, ConfigurationException {
        final String localApikey = config.apikey;
        if (localApikey == null || localApikey.isBlank()) {
            throw new ConfigurationException(TEXT_OFFLINE_CONF_ERROR_MISSING_APIKEY);
        }

        try {
            final String content = get(
                    buildURL(SOUNDS_URL, Map.of(PushoverMessageBuilder.MESSAGE_KEY_TOKEN, localApikey)));
            final JsonObject json = JsonParser.parseString(content).getAsJsonObject();
            final JsonObject jsonSounds = json.has(JSON_VALUE_SOUNDS) ? json.get(JSON_VALUE_SOUNDS).getAsJsonObject()
                    : null;
            if (jsonSounds != null) {
                List<Sound> sounds = jsonSounds.entrySet().stream()
                        .map(entry -> new Sound(entry.getKey(), entry.getValue().getAsString()))
                        .collect(Collectors.toList());
                sounds.add(PushoverAccountConfiguration.SOUND_DEFAULT);
                return sounds;
            }
        } catch (JsonSyntaxException e) {
            // do nothing
        }
        return List.of();
    }

    private String buildURL(String url, Map<String, String> requestParams) {
        return requestParams.keySet().stream().map(key -> key + "=" + encodeParam(requestParams.get(key)))
                .collect(Collectors.joining("&", url + "?", ""));
    }

    private String encodeParam(@Nullable String value) {
        return value == null ? "" : URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String get(String url) throws CommunicationException, ConfigurationException {
        return executeRequest(HttpMethod.GET, url, null);
    }

    private String post(String url, ContentProvider body) throws CommunicationException, ConfigurationException {
        return executeRequest(HttpMethod.POST, url, body);
    }

    private synchronized String executeRequest(HttpMethod httpMethod, String url, @Nullable ContentProvider body)
            throws CommunicationException, ConfigurationException {
        logger.trace("Pushover request: {} - URL = '{}'", httpMethod, url);
        try {
            final Request request = httpClient.newRequest(url).method(httpMethod).timeout(config.timeout,
                    TimeUnit.SECONDS);

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
                    throw new ConfigurationException(getMessageError(content));
                default:
                    logger.debug("Pushover server responded with status code {}: {}", httpStatus, content);
                    throw new CommunicationException(content);
            }
        } catch (ExecutionException e) {
            String message = e.getMessage();
            logger.debug("ExecutionException occurred during execution: {}", message, e);
            throw new CommunicationException(message == null ? TEXT_OFFLINE_COMMUNICATION_ERROR : message,
                    e.getCause());
        } catch (TimeoutException e) {
            String message = e.getMessage();
            logger.debug("TimeoutException occurred during execution: {}", message, e);
            throw new CommunicationException(message == null ? TEXT_OFFLINE_COMMUNICATION_ERROR : message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            String message = e.getMessage();
            logger.debug("InterruptedException occurred during execution: {}", message, e);
            throw new CommunicationException(message == null ? TEXT_OFFLINE_COMMUNICATION_ERROR : message);
        }
    }

    private String getMessageError(String content) {
        final JsonObject json = JsonParser.parseString(content).getAsJsonObject();
        final JsonElement errorsElement = json.get(JSON_VALUE_ERRORS);
        if (errorsElement != null && errorsElement.isJsonArray()) {
            return errorsElement.getAsJsonArray().toString();
        }
        return TEXT_OFFLINE_CONF_ERROR_UNKNOWN;
    }

    private boolean getMessageStatus(String content) {
        final JsonObject json = JsonParser.parseString(content).getAsJsonObject();
        return json.has(JSON_VALUE_STATUS) ? json.get(JSON_VALUE_STATUS).getAsInt() == 1 : false;
    }

    private boolean getMessageStatus(JsonObject json) {
        return json.has(JSON_VALUE_STATUS) ? json.get(JSON_VALUE_STATUS).getAsInt() == 1 : false;
    }
}
