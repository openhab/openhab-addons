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

    public static String getPasswordHash(String password) {
        String passwordHash = getMd5(password);
        if (UNKNOWN.equals(password)) {
            return UNKNOWN;
        } else {
            return passwordHash.toLowerCase();
        }
    }

    public static AccessTokenResponse decodeNewToken(String content) {
        JSONObject json = new JSONObject(content);
        if (json.has("data")) {
            JSONObject data = json.getJSONObject("data");
            if (data.has("token")) {
                AccessTokenResponse response = new AccessTokenResponse();
                response.setCreatedOn(Instant.now());
                response.setExpiresIn(24 * 60 * 60);
                response.setAccessToken(data.getString("token"));
                return response;
            }
        }
        return getInvalidToken();
    }

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

    public static AccessTokenResponse getInvalidToken() {
        AccessTokenResponse response = new AccessTokenResponse();
        response.setAccessToken(UNKNOWN);
        return response;
    }

    public static boolean isTokenValid(AccessTokenResponse response) {
        return !UNKNOWN.equals(response.getAccessToken());
    }
}
