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
package org.openhab.binding.mspa.internal;

import static org.openhab.binding.mspa.internal.MSpaConstants.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MSpaUtils} providing several helper functions
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MSpaUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(MSpaUtils.class);

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
            return DatatypeConverter.printHexBinary(digest);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("MD5 Algorithm not supported");
        }
        return UNKNOWN;
    }

    /**
     * Get signature to sign a query or command towards MSpa cloud.
     *
     * @param nonce to sgin
     * @param timestamp to sign
     * @param region to sign
     * @return signature of the 3 above parameters as upper case md5 hash
     */
    public static String getSignature(String nonce, long timestamp, String region) {
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
        if (UNKNOWN.equals(password)) {
            return UNKNOWN;
        } else {
            return passwordHash.toLowerCase();
        }
    }

    /**
     * Decode token delivered by MSpa cloud
     *
     * @param content as JSIN encoded String
     * @return AccessToken object
     */
    public static AccessTokenResponse decodeNewToken(String content) {
        JSONObject json = new JSONObject(content);
        if (json.has("data")) {
            JSONObject data = json.getJSONObject("data");
            if (data.has("token")) {
                AccessTokenResponse response = new AccessTokenResponse();
                // set data manually cause they aren't delivered. Also no refresh token available - simply get a new
                // token!
                response.setCreatedOn(Instant.now());
                response.setExpiresIn(24 * 60 * 60);
                response.setAccessToken(data.getString("token"));
                return response;
            }
        }
        return getInvalidToken();
    }

    /**
     * Decode token from JSONObject
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

    public static JSONObject token2Json(AccessTokenResponse atr) {
        JSONObject json = new JSONObject();
        json.put("token", atr.getAccessToken());
        json.put("created", atr.getCreatedOn());
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
     * Check if token is valid
     *
     * @param token to checked
     * @return true if valid, false otherwise
     */
    public static boolean isTokenValid(AccessTokenResponse token) {
        return !UNKNOWN.equals(token.getAccessToken());
    }

    public static Map<String, Object> getDiscoveryProperties(Map<String, Object> properties) {
        Map<String, Object> discoveryProperties = new HashMap<>();
        DEVICE_PROPERTY_MAPPING.forEach((key, targetKey) -> {
            Object propertyValue = properties.get(key);
            if (propertyValue != null) {
                discoveryProperties.put(targetKey, propertyValue);
            }
        });
        return discoveryProperties;
    }

    public static Map<String, String> getDeviceProperties(Map<String, Object> properties) {
        Map<String, String> deviceProperties = new HashMap<>();
        getDiscoveryProperties(properties).forEach((key, value) -> {
            deviceProperties.put(key, value.toString());
        });
        return deviceProperties;
    }
}
