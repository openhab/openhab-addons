/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mspa.internal;

import static org.openhab.binding.mspa.internal.MSpaConstants.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.openhab.binding.mspa.internal.MSpaConstants.ServiceRegion;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MSpaUtils} class provides several helper functions for interacting with MSpa cloud services.
 * It includes methods for generating cryptographic hashes, signing requests, decoding and validating tokens,
 * and mapping device properties for discovery and configuration.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MSpaUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(MSpaUtils.class);
    private static final int EXPIRATION_TIME_SEC = 24 * 60 * 60;

    /**
     * Gets hash value from md5 algorithm
     *
     * @param input for calculating hash value
     * @return md5 hash value
     */
    public static String getMd5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes());
            byte[] digest = md.digest();
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error(
                    "MD5 Algorithm not supported. Exception: {}. Please verify the Java runtime environment for supported algorithms.",
                    e.toString());
        }
        return UNKNOWN;
    }

    /**
     * Get signature to sign a query or command towards MSpa cloud.
     *
     * @param nonce to sign
     * @param timestamp to sign
     * @param region to sign
     * @return signature of the 3 above parameters as upper case md5 hash
     */
    public static String getSignature(String nonce, long timestamp, ServiceRegion region) {
        String appId = APP_IDS.get(region);
        String appSecret = APP_SECRETS.get(region);
        if (appId != null && appSecret != null) {
            String toSign = appId + "," + appSecret + "," + nonce + "," + timestamp;
            String signature = getMd5(toSign);
            if (!UNKNOWN.equals(signature)) {
                return signature.toUpperCase();
            }
        }
        return UNKNOWN;
    }

    /**
     * Get password hash for command or query authorization
     *
     * @param password as input
     * @return lower case md5 hash
     */
    public static String getPasswordHash(String password) {
        String passwordHash = getMd5(password);
        if (UNKNOWN.equals(passwordHash)) {
            return UNKNOWN;
        } else {
            return passwordHash.toLowerCase();
        }
    }

    /**
     * Decode token delivered by MSpa cloud. Expiration and creation set manually, no refresh token provided by MSpa
     *
     * @param content as JSON encoded String
     * @return AccessToken object
     */
    public static AccessTokenResponse decodeNewToken(String content) {
        JSONObject json = new JSONObject(content);
        if (json.has("data")) {
            JSONObject data = json.getJSONObject("data");
            if (data.has("token")) {
                AccessTokenResponse response = new AccessTokenResponse();
                // Set data manually because the MSpa cloud does not provide refresh tokens in its response.
                // This means the application must request a new token each time the current one expires,
                // potentially increasing the frequency of authentication requests to the MSpa cloud.
                // token!
                response.setCreatedOn(Instant.now());
                response.setExpiresIn(EXPIRATION_TIME_SEC);
                response.setAccessToken(data.getString("token"));
                return response;
            }
        }
        return getInvalidToken();
    }

    /**
     * Decode token from JSONObject obtained from storage. All keys must be present.
     *
     * @param json as JSONObject
     * @return AccessToken object
     */
    public static AccessTokenResponse decodeStoredToken(JSONObject json) {
        if (json.has("token") && json.has("created") && json.has("expires")) {
            AccessTokenResponse response = new AccessTokenResponse();
            response.setAccessToken(json.getString("token"));
            response.setCreatedOn(Instant.parse(json.getString("created")));
            response.setExpiresIn(json.getLong("expires"));
            return response;
        }
        return getInvalidToken();
    }

    /**
     * Converts AccessTokenResponse to JSON object for storage
     *
     * @param atr AccessTokenResponse
     * @return JSONObject with fields from atr
     */
    public static JSONObject token2Json(AccessTokenResponse atr) {
        JSONObject json = new JSONObject();
        json.put("token", atr.getAccessToken());
        json.put("created", atr.getCreatedOn().toString());
        json.put("expires", atr.getExpiresIn());
        return json;
    }

    /**
     * Gets default invalid token
     *
     * @return invalid and expired token
     */
    public static AccessTokenResponse getInvalidToken() {
        AccessTokenResponse response = new AccessTokenResponse();
        response.setAccessToken(UNKNOWN);
        return response;
    }

    /**
     * Check if token is valid and not expired
     *
     * @param token to checked
     * @return true if valid, false otherwise
     */
    public static boolean isTokenValid(AccessTokenResponse token) {
        return !token.isExpired(Instant.now(), 1) && !UNKNOWN.equals(token.getAccessToken());
    }

    /**
     * Provides property Map for discovery
     *
     * @param properties map from API call
     * @return Map<String,Object> for discovery
     */
    public static Map<String, Object> getDiscoveryProperties(Map<String, Object> properties) {
        Map<String, Object> discoveryProperties = new HashMap<>();
        DEVICE_PROPERTY_MAPPING.forEach((key, targetKey) -> {
            // Retrieve the value of the property using the key from the API call
            Object propertyValue = properties.get(key);
            if (propertyValue != null) {
                // Map the retrieved property value to the target key for discovery
                discoveryProperties.put(targetKey, propertyValue);
            }
        });

        return discoveryProperties;
    }

    /**
     * Provides property Map for Thing properties
     *
     * @param properties map from API call
     * @return Map<String,String> for Thing.setProperties
     */
    public static Map<String, String> getDeviceProperties(Map<String, Object> properties) {
        Map<String, String> deviceProperties = new HashMap<>();
        getDiscoveryProperties(properties).forEach((key, value) -> {
            deviceProperties.put(key, value.toString());
        });
        return deviceProperties;
    }

    /**
     * Get the message out of a cloud response
     *
     * @param response as String
     * @return message from JSON object, UNKNOWN otherwise
     */
    public static String checkResponse(String response) {
        JSONObject responseJson = new JSONObject(response);
        if (responseJson.has("message")) {
            return responseJson.getString("message");
        }
        return UNKNOWN;
    }
}
