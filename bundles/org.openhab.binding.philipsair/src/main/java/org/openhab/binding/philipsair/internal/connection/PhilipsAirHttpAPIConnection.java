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
package org.openhab.binding.philipsair.internal.connection;

import static org.eclipse.jetty.http.HttpMethod.PUT;
import static org.eclipse.jetty.http.HttpStatus.*;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.philipsair.internal.PhilipsAirConfiguration;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierDataDTO;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierDeviceDTO;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierFiltersDTO;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierWritableDataDTO;
import org.openhab.core.cache.ExpiringCacheMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * Handles communication with Philips Air purifiers AC2729 and AC2889 and others
 *
 * @author Michał Boroński - Initial contribution
 * @author Marcel Verpaalen - Fix key exchange and error response handling
 *
 */

@NonNullByDefault
public class PhilipsAirHttpAPIConnection extends PhilipsAirAPIConnection {
    private final Logger logger = LoggerFactory.getLogger(PhilipsAirHttpAPIConnection.class);
    private static final String BASE_UPNP_URL = "http://%HOST%/upnp/description.xml";
    private static final String STATUS_URL = "http://%HOST%/di/v1/products/1/air";
    private static final String DEVICE_URL = "http://%HOST%/di/v1/products/1/device";
    private static final String USERINFO_URL = "http://%HOST%/di/v1/products/0/userinfo";
    private static final String KEY_URL = "http://%HOST%/di/v1/products/0/security";
    private static final String FILTERS_URL = "http://%HOST%/di/v1/products/1/fltsts";
    private static final String FIRMWARE_URL = "http://%HOST%/di/v1/products/0/firmware";
    private static final long REQUEST_TIMEOUT_SECONDS = 10;

    private final ExpiringCacheMap<String, String> cache;
    private final Gson gson = new Gson();
    private long cooldownTimer = 0;

    private @Nullable PhilipsAirCipher cipher = null;
    private HttpClient httpClient;

    public PhilipsAirHttpAPIConnection(PhilipsAirConfiguration config, HttpClient httpClient) {
        super(config);
        this.httpClient = httpClient;
        cache = new ExpiringCacheMap<>(TimeUnit.SECONDS.toMillis(config.getRefreshInterval()));
        this.config = config;
        initCipher();
    }

    private void initCipher() {
        this.cipher = null;
        try {
            final PhilipsAirCipher cipher = new PhilipsAirCipher();
            if (config.getKey().isEmpty()) {
                exchangeKeys(cipher);
            }
            cipher.initKey(config.getKey());
            this.cipher = cipher;
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            logger.debug("Could not initialize the encryption key: {}", e.getMessage());
        } catch (PhilipsAirAPIException e) {
            logger.debug("Key exchange with {} failed: {}", config.getHost(), e.getMessage());
        }
    }

    public synchronized @Nullable String getAirPurifierInfo(String host)
            throws JsonSyntaxException, PhilipsAirAPIException {
        return getResponseFromCache(buildURL(BASE_UPNP_URL, host), false);
    }

    @Override
    public synchronized @Nullable PhilipsAirPurifierDataDTO getAirPurifierStatus(String host)
            throws JsonSyntaxException, PhilipsAirAPIException {
        return gson.fromJson(getResponseFromCache(buildURL(STATUS_URL, host), true), PhilipsAirPurifierDataDTO.class);
    }

    public synchronized @Nullable String getAirPurifierKey(String host)
            throws JsonSyntaxException, PhilipsAirAPIException {
        return getResponseFromCache(buildURL(KEY_URL, host), true);
    }

    public synchronized @Nullable String getAirPurifierFirmware(String host)
            throws JsonSyntaxException, PhilipsAirAPIException {
        return getResponseFromCache(buildURL(FIRMWARE_URL, host), true);
    }

    @Override
    public synchronized @Nullable PhilipsAirPurifierDeviceDTO getAirPurifierDevice(String host)
            throws JsonSyntaxException, PhilipsAirAPIException {
        return gson.fromJson(getResponseFromCache(buildURL(DEVICE_URL, host), true), PhilipsAirPurifierDeviceDTO.class);
    }

    private synchronized @Nullable String getAirPurifierUserinfo(String host)
            throws JsonSyntaxException, PhilipsAirAPIException {
        return getResponseFromCache(buildURL(USERINFO_URL, host), true);
    }

    @Override
    public synchronized @Nullable PhilipsAirPurifierFiltersDTO getAirPurifierFiltersStatus(String host)
            throws JsonSyntaxException, PhilipsAirAPIException {
        return gson.fromJson(getResponseFromCache(buildURL(FILTERS_URL, host), true),
                PhilipsAirPurifierFiltersDTO.class);
    }

    private static String buildURL(String url, String host) {
        return url.replaceFirst("%HOST%", host);
    }

    private @Nullable String getResponseFromCache(String url, boolean decrypt) {
        return cache.putIfAbsentAndGet(url, () -> getResponse(url, HttpMethod.GET, null, decrypt));
    }

    private String getResponse(String url, HttpMethod method, @Nullable String content, boolean decode)
            throws PhilipsAirAPIException {
        return getResponse(url, method, content, decode, false);
    }

    private String getResponse(String url, HttpMethod method, @Nullable String content, boolean decode, boolean isRetry)
            throws PhilipsAirAPIException {
        try {
            PhilipsAirCipher cipher = this.cipher;
            if (decode && cipher == null) {
                logger.debug("Cipher not initialized, exchanging keys");
                config.setKey("");
                initCipher();
                cipher = getCipher();
            }

            if (cooldownTimer > System.currentTimeMillis()) {
                logger.debug(
                        "Cooldown period is active, waiting Philips Air Purifier device responded with status code");
                throw new PhilipsAirAPIException(
                        "Cooldown period is active, waiting Philips Air Purifier device responded with status code");
            }

            Request request = httpClient.newRequest(url).method(method);
            if (method == PUT && (content != null && !content.isEmpty())) {
                request.content(new StringContentProvider(content));
            }

            ContentResponse contentResponse = request.timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS).send();
            int httpStatus = contentResponse.getStatus();
            String finalcontent = contentResponse.getContentAsString();
            logger.trace("Philips Air Purifier device response: status = {}, content = '{}'", httpStatus, finalcontent);

            switch (httpStatus) {
                case OK_200:
                    break;
                case TOO_MANY_REQUESTS_429:
                    cooldownTimer = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5);
                    // fall through
                default:
                    logger.debug("Philips Air Purifier device responded with status code {}", httpStatus);
                    throw new PhilipsAirAPIException(String.format("Error with status %d", httpStatus));
            }

            // only successful responses are encrypted
            if (decode && cipher != null) {
                try {
                    finalcontent = cipher.decrypt(finalcontent);
                } catch (BadPaddingException bexp) {
                    logger.debug("Could not decrypt response, exchanging keys");
                    config.setKey("");
                    initCipher();
                    getCipher();
                    // the response was encrypted with the previous key. A command is not repeated, as its content
                    // was also encrypted with the previous key.
                    if (isRetry || method != HttpMethod.GET) {
                        throw new PhilipsAirAPIException("Could not decrypt response, encryption key renewed");
                    }
                    return getResponse(url, method, content, decode, true);
                }
            }
            if (finalcontent == null) {
                throw new PhilipsAirAPIException("Empty response");
            }
            logger.debug("Philips Air Purifier device response: '{}'", finalcontent);
            return finalcontent;
        } catch (PhilipsAirAPIException e) {
            throw e;
        } catch (ExecutionException e) {
            String errorMessage = e.getLocalizedMessage() != null ? e.getLocalizedMessage() : e.getMessage();
            logger.trace("Exception occurred during execution: {}", errorMessage, e);
            throw new PhilipsAirAPIException(errorMessage, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PhilipsAirAPIException(e.getMessage(), e);
        } catch (TimeoutException e) {
            String errorMessage = e.getLocalizedMessage() != null ? e.getLocalizedMessage() : e.getMessage();
            logger.debug("Exception occurred during execution: {}", errorMessage, e);
            throw new PhilipsAirAPIException(errorMessage, e);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            logger.debug("Could not decrypt response: {}", e.getMessage());
            throw new PhilipsAirAPIException(e.getMessage(), e);
        }
    }

    private PhilipsAirCipher getCipher() throws PhilipsAirAPIException {
        PhilipsAirCipher cipher = this.cipher;
        if (cipher == null) {
            throw new PhilipsAirAPIException("Encryption key not available");
        }
        return cipher;
    }

    /**
     * Exchanges a new session key with the device using the not yet initialized cipher and stores it in the
     * configuration.
     */
    private void exchangeKeys(PhilipsAirCipher cipher) throws PhilipsAirAPIException {
        String url = buildURL(KEY_URL, config.getHost());
        String data = "{\"diffie\":\"" + cipher.getApow() + "\"}";
        String encodedContent = getResponse(url, PUT, data, false);
        JsonObject encodedJson;
        try {
            encodedJson = gson.fromJson(encodedContent, JsonObject.class);
        } catch (JsonSyntaxException e) {
            throw new PhilipsAirAPIException("Invalid key exchange response", e);
        }
        if (encodedJson == null || !encodedJson.has("key") || !encodedJson.has("hellman")) {
            throw new PhilipsAirAPIException("Invalid key exchange response: " + encodedContent);
        }
        try {
            config.setKey(cipher.calculateKey(encodedJson.get("hellman").getAsString(),
                    encodedJson.get("key").getAsString()));
        } catch (GeneralSecurityException | IllegalArgumentException | UnsupportedOperationException
                | IllegalStateException e) {
            throw new PhilipsAirAPIException(e);
        }
    }

    @Override
    public synchronized @Nullable PhilipsAirPurifierDataDTO sendCommand(String parameter,
            PhilipsAirPurifierWritableDataDTO value) throws PhilipsAirAPIException {
        final PhilipsAirCipher cipher = this.cipher;
        if (cipher == null) {
            return null;
        }
        try {
            String commandValue = gson.toJson(value);
            logger.debug("{}", commandValue);
            commandValue = cipher.encrypt(commandValue.toString());
            if (commandValue == null || commandValue.isEmpty()) {
                return null;
            }
            String statusUrl = buildURL(STATUS_URL, config.getHost());
            String response;
            try {
                response = getResponse(statusUrl, PUT, commandValue.toString(), true);
            } finally {
                // the cached status no longer reflects the device state, even if the command failed
                cache.invalidate(statusUrl);
            }
            logger.debug("{}", response);
            return gson.fromJson(response, PhilipsAirPurifierDataDTO.class);
        } catch (InvalidKeyException | JsonSyntaxException | IllegalBlockSizeException | BadPaddingException
                | UnsupportedEncodingException | NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidAlgorithmParameterException | PhilipsAirAPIException e) {
            throw new PhilipsAirAPIException(e);
        }
    }

    @Override
    public PhilipsAirConfiguration getConfig() {
        return this.config;
    }
}
