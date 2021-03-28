/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import org.apache.commons.lang3.StringUtils;
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
 *
 */

@NonNullByDefault
public class PhilipsAirAPIConnection {
    private final Logger logger = LoggerFactory.getLogger(PhilipsAirAPIConnection.class);
    private static final String BASE_UPNP_URL = "http://%HOST%/upnp/description.xml";
    private static final String STATUS_URL = "http://%HOST%/di/v1/products/1/air";
    private static final String DEVICE_URL = "http://%HOST%/di/v1/products/1/device";
    private static final String USERINFO_URL = "http://%HOST%/di/v1/products/0/userinfo";
    private static final String KEY_URL = "http://%HOST%/di/v1/products/0/security";
    private static final String FILTERS_URL = "http://%HOST%/di/v1/products/1/fltsts";
    private static final String FIRMWARE_URL = "http://%HOST%/di/v1/products/0/firmware";

    private final HttpClient httpClient;
    private final ExpiringCacheMap<String, String> cache;
    private final Gson gson = new Gson();
    private long cooldownTimer = 0;

    private @Nullable PhilipsAirCipher cipher = null;

    private PhilipsAirConfiguration config;

    public PhilipsAirAPIConnection(PhilipsAirConfiguration config, HttpClient httpClient) {
        this.httpClient = httpClient;
        cache = new ExpiringCacheMap<>(TimeUnit.SECONDS.toMillis(config.getRefreshInterval()));
        this.config = config;
        initCipher();
    }

    private void initCipher() {

        try {
            this.cipher = new PhilipsAirCipher();
            if (StringUtils.isEmpty(config.getKey())) {
                exchangeKeys();
            }

            cipher.initKey(config.getKey());

        } catch (GeneralSecurityException | PhilipsAirAPIException | InterruptedException e) {
            logger.warn("An exception occured", e);
            this.cipher = null;
        }
    }

    public synchronized @Nullable String getAirPurifierInfo(String host)
            throws JsonSyntaxException, PhilipsAirAPIException {
        return getResponseFromCache(buildURL(BASE_UPNP_URL, host), false);
    }

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

    public synchronized @Nullable PhilipsAirPurifierDeviceDTO getAirPurifierDevice(String host)
            throws JsonSyntaxException, PhilipsAirAPIException {
        return gson.fromJson(getResponseFromCache(buildURL(DEVICE_URL, host), true), PhilipsAirPurifierDeviceDTO.class);
    }

    public synchronized @Nullable String getAirPurifierUserinfo(String host)
            throws JsonSyntaxException, PhilipsAirAPIException {
        return getResponseFromCache(buildURL(USERINFO_URL, host), true);
    }

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
        try {
            if (decode && cipher == null) {
                logger.warn("Cipher not initialized");
                config.setKey("");
                initCipher();
            }

            if (cooldownTimer > System.currentTimeMillis()) {
                logger.debug(
                        "Cooldown period is active, waiting Philips Air Purifier device responded with status code");
                throw new PhilipsAirAPIException(
                        "Cooldown period is active, waiting Philips Air Purifier device responded with status code");
            }

            Request request = httpClient.newRequest(url).method(method);
            if (method == PUT && StringUtils.isNotEmpty(content)) {
                request.content(new StringContentProvider(content));
            }

            ContentResponse contentResponse = request.timeout(config.getRefreshInterval(), TimeUnit.SECONDS).send();
            int httpStatus = contentResponse.getStatus();
            String finalcontent = contentResponse.getContentAsString();
            logger.trace("Philips Air Purifier device encrypted response: '{}'", finalcontent);
            if (decode) {
                try {
                    finalcontent = this.cipher.decrypt(finalcontent);
                } catch (BadPaddingException bexp) {
                    // retry for once with a new key
                    config.setKey("");
                    initCipher();
                    finalcontent = this.cipher.decrypt(finalcontent);
                }
            }

            logger.debug("Philips Air Purifier device response: status = {}, content = '{}'", httpStatus, finalcontent);
            switch (httpStatus) {
                case OK_200:
                    if (finalcontent != null) {
                        return finalcontent;
                    }
                case BAD_REQUEST_400:
                case UNAUTHORIZED_401:
                case NOT_FOUND_404:
                    logger.debug("Philips Air Purifier device responded with status code {}", httpStatus);
                    throw new PhilipsAirAPIException(String.format("Error with status %d", httpStatus));
                case TOO_MANY_REQUESTS_429:
                    cooldownTimer = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5);
                default:
                    logger.debug("Philips Air Purifier device responded with status code {}", httpStatus);
                    throw new PhilipsAirAPIException(String.format("Error with status %d", httpStatus));
            }
        } catch (ExecutionException e) {
            String errorMessage = e.getLocalizedMessage() != null ? e.getLocalizedMessage() : e.getMessage();
            logger.trace("Exception occurred during execution: {}", errorMessage, e);
            throw new PhilipsAirAPIException(errorMessage, e);
        } catch (InterruptedException | TimeoutException e) {
            String errorMessage = e.getLocalizedMessage() != null ? e.getLocalizedMessage() : e.getMessage();
            logger.debug("Exception occurred during execution: {}", errorMessage, e);
            throw new PhilipsAirAPIException(errorMessage, e);
        } catch (Exception e) {
            String errorMessage = e.getLocalizedMessage() != null ? e.getLocalizedMessage() : e.getMessage();
            logger.warn("Unexpected exception occurred during execution: {}", errorMessage, e);
            throw new PhilipsAirAPIException(errorMessage, e);
        }
    }

    public @Nullable String exchangeKeys() throws PhilipsAirAPIException, InterruptedException {
        if (this.cipher == null) {
            return null;
        }

        String url = buildURL(KEY_URL, config.getHost());
        String data = "{\"diffie\":\"" + this.cipher.getApow() + "\"}";
        String encodedContent = getResponse(url, PUT, data, false);
        JsonObject encodedJson = gson.fromJson(encodedContent, JsonObject.class);
        String key = encodedJson.get("key").getAsString();
        String hellman = encodedJson.get("hellman").getAsString();
        String aesKey;
        try {
            aesKey = this.cipher.calculateKey(hellman, key);
        } catch (GeneralSecurityException | TimeoutException | ExecutionException e) {
            throw new PhilipsAirAPIException(e);
        }

        config.setKey(aesKey);
        return aesKey;
    }

    public @Nullable PhilipsAirPurifierDataDTO sendCommand(String parameter, PhilipsAirPurifierWritableDataDTO value)
            throws IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {

        String commandValue = gson.toJson(value);
        logger.debug("{}", commandValue);
        commandValue = this.cipher.encrypt(commandValue.toString());
        if (commandValue == null || commandValue.isEmpty()) {
            return null;
        }

        String response = getResponse(buildURL(STATUS_URL, config.getHost()), PUT, commandValue.toString(), true);
        logger.debug("{}", response);
        return gson.fromJson(response, PhilipsAirPurifierDataDTO.class);
    }

    public PhilipsAirConfiguration getConfig() {
        return this.config;
    }
}
