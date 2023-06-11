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
package org.openhab.binding.pushsafer.internal.connection;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
import org.openhab.binding.pushsafer.internal.config.PushsaferAccountConfiguration;
import org.openhab.binding.pushsafer.internal.dto.Icon;
import org.openhab.binding.pushsafer.internal.dto.Sound;
import org.openhab.core.cache.ExpiringCacheMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link PushsaferAPIConnection} is responsible for handling the connections to Pushsafer Messages API.
 *
 * @author Kevin Siml - Initial contribution, forked from Christoph Weitkamp
 */
@NonNullByDefault
public class PushsaferAPIConnection {

    private final Logger logger = LoggerFactory.getLogger(PushsaferAPIConnection.class);

    private static final String VALIDATE_URL = "https://www.pushsafer.com/api-k";
    private static final String MESSAGE_URL = "https://www.pushsafer.com/api";
    private static final String CANCEL_MESSAGE_URL = "https://www.pushsafer.com/api-m";
    private static final String SOUNDS_URL = "https://www.pushsafer.com/api-s";
    private static final String ICONS_URL = "https://www.pushsafer.com/api-i";

    private final HttpClient httpClient;
    private final PushsaferAccountConfiguration config;

    private final ExpiringCacheMap<String, String> cache = new ExpiringCacheMap<>(TimeUnit.DAYS.toMillis(1));

    public PushsaferAPIConnection(HttpClient httpClient, PushsaferAccountConfiguration config) {
        this.httpClient = httpClient;
        this.config = config;
    }

    public boolean validateUser() throws PushsaferCommunicationException, PushsaferConfigurationException {
        final String localApikey = config.apikey;
        if (localApikey == null || localApikey.isEmpty()) {
            throw new PushsaferConfigurationException("@text/offline.conf-error-missing-apikey");
        }
        final String localUser = config.user;
        if (localUser == null || localUser.isEmpty()) {
            throw new PushsaferConfigurationException("@text/offline.conf-error-missing-user");
        }

        final String content = get(buildURL(VALIDATE_URL, Map.of(PushsaferMessageBuilder.MESSAGE_KEY_TOKEN, localApikey,
                PushsaferMessageBuilder.MESSAGE_KEY_USER, localUser)));
        final JsonObject json = content == null || content.isBlank() ? null
                : JsonParser.parseString(content).getAsJsonObject();
        return json == null ? false : getMessageStatus(json);
    }

    public boolean sendPushsaferMessage(PushsaferMessageBuilder message)
            throws PushsaferCommunicationException, PushsaferConfigurationException {
        return getMessageStatus(post(MESSAGE_URL, message.build()));
    }

    public String sendPushsaferPriorityMessage(PushsaferMessageBuilder message)
            throws PushsaferCommunicationException, PushsaferConfigurationException {
        final JsonObject json = JsonParser.parseString(post(MESSAGE_URL, message.build())).getAsJsonObject();
        return getMessageStatus(json) && json.has("receipt") ? json.get("receipt").getAsString() : "";
    }

    public boolean cancelPushsaferPriorityMessage(String receipt)
            throws PushsaferCommunicationException, PushsaferConfigurationException {
        return getMessageStatus(post(CANCEL_MESSAGE_URL.replace("{receipt}", receipt),
                PushsaferMessageBuilder.getInstance(config.apikey, config.device).build()));
    }

    public List<Sound> getSounds() throws PushsaferCommunicationException, PushsaferConfigurationException {
        final String localApikey = config.apikey;
        if (localApikey == null || localApikey.isEmpty()) {
            throw new PushsaferConfigurationException("@text/offline.conf-error-missing-apikey");
        }

        final Map<String, String> params = new HashMap<>(1);
        params.put(PushsaferMessageBuilder.MESSAGE_KEY_TOKEN, localApikey);

        final String content = getFromCache(buildURL(SOUNDS_URL, params));
        final JsonObject json = content == null || content.isBlank() ? null
                : JsonParser.parseString(content).getAsJsonObject();
        final JsonObject sounds = json == null || !json.has("sounds") ? null : json.get("sounds").getAsJsonObject();

        return sounds == null ? List.of()
                : sounds.entrySet().stream().map(entry -> new Sound(entry.getKey(), entry.getValue().getAsString()))
                        .collect(Collectors.toUnmodifiableList());
    }

    public List<Icon> getIcons() throws PushsaferCommunicationException, PushsaferConfigurationException {
        final String localApikey = config.apikey;
        if (localApikey == null || localApikey.isEmpty()) {
            throw new PushsaferConfigurationException("@text/offline.conf-error-missing-apikey");
        }

        final Map<String, String> params = new HashMap<>(1);
        params.put(PushsaferMessageBuilder.MESSAGE_KEY_TOKEN, localApikey);

        final String content = getFromCache(buildURL(ICONS_URL, params));
        final JsonObject json = content == null || content.isBlank() ? null
                : JsonParser.parseString(content).getAsJsonObject();
        final JsonObject icons = json == null || !json.has("icons") ? null : json.get("icons").getAsJsonObject();

        return icons == null ? List.of()
                : icons.entrySet().stream().map(entry -> new Icon(entry.getKey(), entry.getValue().getAsString()))
                        .collect(Collectors.toUnmodifiableList());
    }

    private String buildURL(String url, Map<String, String> requestParams) {
        return requestParams.keySet().stream().map(key -> key + "=" + encodeParam(requestParams.get(key)))
                .collect(Collectors.joining("&", url + "?", ""));
    }

    private String encodeParam(@Nullable String value) {
        return value == null ? "" : URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private @Nullable String getFromCache(String url)
            throws PushsaferCommunicationException, PushsaferConfigurationException {
        return cache.putIfAbsentAndGet(url, () -> get(url));
    }

    private String get(String url) throws PushsaferCommunicationException, PushsaferConfigurationException {
        return executeRequest(HttpMethod.GET, url, null);
    }

    private String post(String url, ContentProvider body)
            throws PushsaferCommunicationException, PushsaferConfigurationException {
        return executeRequest(HttpMethod.POST, url, body);
    }

    private String executeRequest(HttpMethod httpMethod, String url, @Nullable ContentProvider body)
            throws PushsaferCommunicationException, PushsaferConfigurationException {
        logger.trace("Pushsafer request: {} - URL = '{}'", httpMethod, uglifyApikey(url));
        try {
            final Request request = httpClient.newRequest(url).method(httpMethod).timeout(10, TimeUnit.SECONDS);

            if (body != null) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Pushsafer request body: '{}'", body);
                }
                request.content(body);
            }

            final ContentResponse contentResponse = request.send();

            final int httpStatus = contentResponse.getStatus();
            final String content = contentResponse.getContentAsString();
            logger.trace("Pushsafer response: status = {}, content = '{}'", httpStatus, content);
            switch (httpStatus) {
                case HttpStatus.OK_200:
                    return content;
                case 250:
                case HttpStatus.BAD_REQUEST_400:
                    logger.debug("Pushsafer server responded with status code {}: {}", httpStatus, content);
                    throw new PushsaferConfigurationException(getMessageError(content));
                default:
                    logger.debug("Pushsafer server responded with status code {}: {}", httpStatus, content);
                    throw new PushsaferCommunicationException(content);
            }
        } catch (ExecutionException e) {
            logger.debug("Exception occurred during execution: {}", e.getLocalizedMessage(), e);
            throw new PushsaferCommunicationException(e.getLocalizedMessage(), e.getCause());
        } catch (InterruptedException | TimeoutException e) {
            logger.debug("Exception occurred during execution: {}", e.getLocalizedMessage(), e);
            throw new PushsaferCommunicationException(e.getLocalizedMessage());
        }
    }

    private String uglifyApikey(String url) {
        return url.replaceAll("(k=)+\\w+", "k=*****");
    }

    private String getMessageError(String content) {
        final JsonObject json = JsonParser.parseString(content).getAsJsonObject();
        final JsonElement errorsElement = json.get("errors");
        if (errorsElement != null && errorsElement.isJsonArray()) {
            return errorsElement.getAsJsonArray().toString();
        }
        return "@text/offline.conf-error-unknown";
    }

    private boolean getMessageStatus(String content) {
        return getMessageStatus(JsonParser.parseString(content).getAsJsonObject());
    }

    private boolean getMessageStatus(JsonObject json) {
        return json.has("status") ? json.get("status").getAsInt() == 1 : false;
    }
}
