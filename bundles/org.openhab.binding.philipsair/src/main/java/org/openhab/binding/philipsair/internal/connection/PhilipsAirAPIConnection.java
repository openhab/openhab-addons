/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import static org.eclipse.jetty.http.HttpStatus.BAD_REQUEST_400;
import static org.eclipse.jetty.http.HttpStatus.NOT_FOUND_404;
import static org.eclipse.jetty.http.HttpStatus.OK_200;
import static org.eclipse.jetty.http.HttpStatus.TOO_MANY_REQUESTS_429;
import static org.eclipse.jetty.http.HttpStatus.UNAUTHORIZED_401;

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

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.smarthome.core.cache.ExpiringCacheMap;
import org.openhab.binding.philipsair.internal.PhilipsAirConfiguration;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierData;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierDevice;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierFilters;
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
    private PhilipsAirCipher cipher;

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

            this.cipher.initKey(config.getKey());
        } catch (GeneralSecurityException | ExecutionException | TimeoutException | InterruptedException e) {
            logger.error("An exception occured", e);
            this.cipher = null;
        }
    }

    public synchronized @Nullable String getAirPurifierInfo(String host)
            throws JsonSyntaxException, PhilipsAirAPIException {
        return getResponseFromCache(buildURL(BASE_UPNP_URL, host));
    }

    public synchronized @Nullable PhilipsAirPurifierData getAirPurifierStatus(String host)
            throws JsonSyntaxException, PhilipsAirAPIException {
        return gson.fromJson(getResponseFromCache(buildURL(STATUS_URL, host)), PhilipsAirPurifierData.class);
    }

    public synchronized @Nullable String getAirPurifierKey(String host)
            throws JsonSyntaxException, PhilipsAirAPIException {
        return getResponseFromCache(buildURL(KEY_URL, host));
    }

    public synchronized @Nullable String getAirPurifierFirmware(String host)
            throws JsonSyntaxException, PhilipsAirAPIException {
        return getResponseFromCache(buildURL(FIRMWARE_URL, host));
    }

    public synchronized @Nullable PhilipsAirPurifierDevice getAirPurifierDevice(String host)
            throws JsonSyntaxException, PhilipsAirAPIException {
        return gson.fromJson(getResponseFromCache(buildURL(DEVICE_URL, host)), PhilipsAirPurifierDevice.class);
    }

    public synchronized @Nullable String getAirPurifierUserinfo(String host)
            throws JsonSyntaxException, PhilipsAirAPIException {
        return getResponseFromCache(buildURL(USERINFO_URL, host));
    }

    public synchronized @Nullable PhilipsAirPurifierFilters getAirPurifierFiltersStatus(String host)
            throws JsonSyntaxException, PhilipsAirAPIException {
        return gson.fromJson(getResponseFromCache(buildURL(FILTERS_URL, host)), PhilipsAirPurifierFilters.class);
    }

    private static String buildURL(String url, String host) {
        return url.replaceFirst("%HOST%", host);
    }

    private @Nullable String getResponseFromCache(String url) {
        return cache.putIfAbsentAndGet(url, () -> getResponse(url, HttpMethod.GET, null, true));
    }

    private String getResponse(String url, HttpMethod method, String content, boolean decode) {
        try {
            if (decode && cipher == null) {
                logger.error("Cipher not initialized");
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
                return finalcontent;
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
            String errorMessage = e.getLocalizedMessage();
            logger.trace("Exception occurred during execution: {}", errorMessage, e);
            throw new PhilipsAirAPIException(errorMessage, e.getCause());
        } catch (InterruptedException | TimeoutException e) {
            logger.debug("Exception occurred during execution: {}", e.getLocalizedMessage(), e);
            throw new PhilipsAirAPIException(e.getLocalizedMessage(), e.getCause());
        } catch (Exception e) {
            logger.error("Unexpected exception occurred during execution: {}", e.getLocalizedMessage(), e);
            throw new PhilipsAirAPIException(e.getLocalizedMessage(), e.getCause());
        }
    }

    public String exchangeKeys() throws GeneralSecurityException, InterruptedException, TimeoutException,
            ExecutionException, InvalidAlgorithmParameterException {
        if (this.cipher == null) {
            return null;
        }

        String url = buildURL(KEY_URL, config.getHost());
        String data = "{\"diffie\":\"" + this.cipher.getApow() + "\"}";

        String encodedContent = getResponse(url, PUT, data, false);
        JsonObject encodedJson = gson.fromJson(encodedContent, JsonObject.class);
        String key = encodedJson.get("key").getAsString();
        String hellman = encodedJson.get("hellman").getAsString();
        String aesKey = this.cipher.calculateKey(hellman, key);
        config.setKey(aesKey);
        return aesKey;
    }

    public PhilipsAirPurifierData sendCommand(String parameter, Object value)
            throws IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        String commandValue = null;
        if (parameter.equals("om")) {
            commandValue = "{\"om\":\"" + value + "\",\"mode\":\"M\"}";
        } else if ((value instanceof String)) {
            commandValue = "{\"" + parameter + "\":\"" + value + "\"}";
        } else {
            commandValue = "{\"" + parameter + "\":" + value + "}";
        }

        logger.info("{}", commandValue.toString());
        commandValue = this.cipher.encrypt(commandValue.toString());
        String response = getResponse(buildURL(STATUS_URL, config.getHost()), PUT, commandValue.toString(), true);
        logger.info("{}", response);
        return gson.fromJson(response, PhilipsAirPurifierData.class);
    }
}
