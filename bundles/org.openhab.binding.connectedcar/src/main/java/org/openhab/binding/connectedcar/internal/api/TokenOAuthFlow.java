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
package org.openhab.binding.connectedcar.internal.api;

import static org.openhab.binding.connectedcar.internal.CarUtils.substringBetween;
import static org.openhab.binding.connectedcar.internal.api.ApiHttpClient.getUrlParm;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TokenOAuthFlow} implements some helpers for the oauth flow
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class TokenOAuthFlow {
    private final Logger logger = LoggerFactory.getLogger(TokenOAuthFlow.class);

    public String location = "";
    public String relayState = "", csrf = "", hmac = "", state = "";
    public String userId = "", idToken = "", accessToken = "", expiresIn = "0";
    public String code = "", codeVerifier = "", codeChallenge = "";
    public String action = "";
    public ApiResult res = new ApiResult();
    private final ApiHttpClient http;

    public TokenOAuthFlow(ApiHttpClient http) {
        this.http = http;
        http.clearCookies();
    }

    ApiHttpMap map = new ApiHttpMap();

    public TokenOAuthFlow header(String header, String value) {
        map.header(header, value);
        return this;
    }

    public TokenOAuthFlow header(HttpHeader header, String value) {
        map.header(header.toString(), value);
        return this;
    }

    public TokenOAuthFlow data(String attribute, String value) {
        map.data(attribute, value);
        return this;
    }

    public ApiResult get(String url) throws ApiException {
        res = http.get(url, map.getHeaders(), false);
        return update();
    }

    public ApiResult post(String url, boolean json) throws ApiException {
        res = http.post(url, map.getHeaders(), map.getData(), json);
        return update();
    }

    public ApiResult post(String url) throws ApiException {
        return post(url, false);
    }

    public ApiResult follow() throws ApiException {
        if (location.isEmpty()) {
            throw new ApiException("Missing localtion on redirect");
        }
        return get(location);
    }

    private ApiResult update() {
        location = res.getLocation();
        action = substringBetween(res.response, "action=\"", "\">");
        if (!res.response.isEmpty()) {
            if (res.response.contains("name=\"_csrf\"")) {
                csrf = substringBetween(res.response, "name=\"_csrf\" value=\"", "\"/>");
            }
            if (res.response.contains("name=\"relayState\" value=")) {
                relayState = substringBetween(res.response, "name=\"relayState\" value=\"", "\"/>");
            }
            if (res.response.contains("name=\"hmac\" value=")) {
                hmac = substringBetween(res.response, "name=\"hmac\" value=\"", "\"/>");
            }
        }

        if (!location.isEmpty()) {
            if (location.contains("relayState=")) {
                relayState = getUrlParm(location, "relayState");
            }
            if (location.contains("hmac=")) {
                hmac = getUrlParm(location, "hmac");
            }

            if (location.contains("&code=")) {
                code = getUrlParm(location, "code");
            }
            if (location.contains("&userId")) {
                userId = getUrlParm(location, "userId");
            }
            if (location.contains("&id_token=")) {
                idToken = getUrlParm(location, "id_token");
            }
            if (location.contains("&expires_in=")) {
                expiresIn = getUrlParm(location, "expires_in");
            }
            if (location.contains("&access_token=")) {
                accessToken = getUrlParm(location, "access_token");
            }
            if (location.contains("#state=")) {
                state = getUrlParm(location, "state", "#");
            }

        }
        return res;
    }

    public String addCodeChallenge(String url) {
        try {
            if (url.contains("code_challenge")) {
                codeVerifier = generateCodeVerifier();
                codeChallenge = generateCodeChallange(codeVerifier);
                return url + "&code_challenge=" + codeChallenge;
            }
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            logger.debug("Unable to generate Code Challenge", e);
        }

        return url;
    }

    public static String generateCodeVerifier() throws UnsupportedEncodingException {
        SecureRandom secureRandom = new SecureRandom();
        byte[] codeVerifier = new byte[32];
        secureRandom.nextBytes(codeVerifier);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifier);
    }

    public static String generateCodeChallange(String codeVerifier)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {
        byte[] bytes = codeVerifier.getBytes("US-ASCII");
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(bytes, 0, bytes.length);
        byte[] digest = messageDigest.digest();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }

    public TokenOAuthFlow clearHeader() {
        map.clearHeader();
        return this;
    }

    public TokenOAuthFlow clearData() {
        map.clearData();
        return this;
    }
}
