/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.openhab.binding.connectedcar.internal.BindingConstants.*;
import static org.openhab.binding.connectedcar.internal.api.ApiHttpClient.getUrlParm;
import static org.openhab.binding.connectedcar.internal.util.Helpers.substringBetween;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link IdentityOAuthFlow} implements some helpers for the oauth flow
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
@NonNullByDefault
public class IdentityOAuthFlow extends ApiHttpMap {
    private final Logger logger = LoggerFactory.getLogger(IdentityOAuthFlow.class);

    public String location = "";
    public String relayState = "", csrf = "", hmac = "", state = "";
    public String userId = "", idToken = "", accessToken = "", expiresIn = "0";
    public String code = "", codeVerifier = "", codeChallenge = "";
    public String action = "";
    public ApiResult res = new ApiResult();
    private final ApiHttpClient http;

    public IdentityOAuthFlow(ApiHttpClient http) {
        super();
        this.http = http;
        http.clearCookies();
    }

    public IdentityOAuthFlow(ApiHttpClient http, ApiHttpMap map) {
        this.http = http;
        http.clearCookies();
        init(map);
    }

    public IdentityOAuthFlow init(ApiHttpMap map) {
        super.headers(map.getHeaders());
        super.datas(map.getData());
        return this;
    }

    @Override
    public IdentityOAuthFlow header(String header, String value) {
        super.header(header, value);
        return this;
    }

    @Override
    public IdentityOAuthFlow header(HttpHeader header, String value) {
        super.header(header.toString(), value);
        return this;
    }

    @Override
    public IdentityOAuthFlow headers(Map<String, String> headers) {
        super.headers(headers);
        return this;
    }

    @Override
    public IdentityOAuthFlow data(String attribute, String value) {
        super.data(attribute, value);
        return this;
    }

    @Override
    public IdentityOAuthFlow datas(Map<String, String> data) {
        super.datas(data);
        return this;
    }

    @Override
    public IdentityOAuthFlow body(String body) {
        super.body(body);
        return this;
    }

    @Override
    public IdentityOAuthFlow clearHeader() {
        super.clearHeader();
        return this;
    }

    @Override
    public IdentityOAuthFlow clearData() {
        super.clearData();
        return this;
    }

    public ApiResult get(String url) throws ApiException {
        res = http.get(url, headers, false);
        return update();
    }

    public ApiResult post(String url, boolean json) throws ApiException {
        if (!getHeaders().containsKey(HttpHeaders.CONTENT_TYPE.toString())) {
            header(HttpHeader.CONTENT_TYPE, json ? CONTENT_TYPE_JSON : CONTENT_TYPE_FORM_URLENC);
        }
        return post(url, getRequestData(json));
    }

    public ApiResult post(String url, String data) throws ApiException {
        res = http.post(url, headers, data);
        return update();
    }

    public ApiResult put(String url, boolean json) throws ApiException {
        res = http.put(url, headers, getRequestData(json));
        return update();
    }

    public ApiResult follow() throws ApiException {
        if (location.isEmpty()) {
            throw new ApiException("Missing localtion on redirect");
        }
        return get(location);
    }

    private ApiResult update() throws ApiException {
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
            if (res.response.contains("\"hmac\":")) {
                hmac = substringBetween(res.response, "\"hmac\":\"", "\",\"");
            }
        }

        if (!location.isEmpty()) {
            if (location.contains("error=login.errors.password_invalid")) {
                throw new ApiSecurityException("Login failed due to invalid password or locked account!");
            }
            if (location.contains("error=login.errors.throttled")) {
                throw new ApiSecurityException(
                        "Login failed due to invalid password, locked account or API throtteling!");
            }
            if (location.contains("&updated=dataprivacy")) {
                throw new ApiSecurityException(
                        "Login failed: New Data Privacy Policy has to be accepted, login to Web portal");
            }
            if (location.contains("terms-and-conditions")) {
                throw new ApiException(
                        "Consent to terms&conditions required, login to the Web portal and give consent");
            }

            if (location.contains("relayState=")) {
                relayState = getUrlParm(location, "relayState");
            }
            if (location.contains("hmac=")) {
                hmac = getUrlParm(location, "hmac");
            }

            if (location.contains("code=")) {
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
}
