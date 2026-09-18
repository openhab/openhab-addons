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
package org.openhab.binding.bluelink.internal.api;

import static org.openhab.binding.bluelink.internal.api.AbstractBluelinkApi.TOKEN_EXPIRY_MARGIN;
import static org.openhab.binding.bluelink.internal.api.AbstractBluelinkApi.isServerError;
import static org.openhab.binding.bluelink.internal.api.AbstractBluelinkApi.operationFailed;
import static org.openhab.binding.bluelink.internal.api.AbstractBluelinkApi.send;

import java.math.BigInteger;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

import javax.crypto.Cipher;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.Fields;
import org.openhab.binding.bluelink.internal.api.LoginRejectedException.Reason;
import org.openhab.binding.bluelink.internal.dto.eu.CciTokens;
import org.openhab.binding.bluelink.internal.dto.eu.CcsTokenResponse;
import org.openhab.binding.bluelink.internal.dto.eu.SigningKeyResponse;
import org.openhab.core.i18n.TimeZoneProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Password login for the EU region via the OneApp/CCI API used by the current Kia/Hyundai/Genesis apps.
 * <p>
 * The CCI access token is exchanged for a CCS token that the legacy API accepts as bearer token. This avoids the
 * legacy authorize endpoint, which is blocked for third-party clients since August 2026.
 *
 * @author Carlo Dischler - Initial contribution
 */
@NonNullByDefault
class CciAuthenticator {

    record Config(String clientId, String redirectUri, String apiBaseUrl, String packageId, String clientName,
            String osVersion, String notificationProvider) {

        Config withApiBaseUrl(final String url) {
            return new Config(clientId, redirectUri, url, packageId, clientName, osVersion, notificationProvider);
        }
    }

    record CcsToken(String accessToken, Instant expiry) {
    }

    private record Session(String deviceId, CciTokens tokens) {
    }

    private static final String CLIENT_VERSION = "1.3.3";
    private static final String LANGUAGE = "en";
    private static final String USER_AGENT = "Mozilla/5.0 (Linux; Android 4.1.1; Galaxy Nexus Build/JRO03C) "
            + "AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Mobile Safari/535.19_CCS_APP_AOS";
    private static final Duration DEFAULT_TOKEN_LIFETIME = Duration.ofHours(1);
    private static final Duration MAX_TOKEN_LIFETIME = Duration.ofHours(24);
    private static final CciTokens EMPTY = new CciTokens("", "", "", "", "", "", "");

    private final Logger logger = LoggerFactory.getLogger(CciAuthenticator.class);
    private final Gson gson = new Gson();
    private final HttpClient httpClient;
    private final TimeZoneProvider timeZoneProvider;
    private final Config config;
    private final String loginBaseUrl;

    private @Nullable Session session;

    CciAuthenticator(final HttpClient httpClient, final TimeZoneProvider timeZoneProvider, final Config config,
            final String loginBaseUrl) {
        this.httpClient = httpClient;
        this.timeZoneProvider = timeZoneProvider;
        this.config = config;
        this.loginBaseUrl = loginBaseUrl;
    }

    boolean hasSession() {
        return session != null;
    }

    CcsToken login(final String username, final String password) throws BluelinkApiException {
        final String deviceId = UUID.randomUUID().toString();
        clearLoginCookies();
        authorize();
        final SigningKeyResponse.Key key = fetchSigningKey();
        final String code = signIn(username, encryptPassword(password, key), key.kid());
        final CciTokens tokens = merge(EMPTY, exchangeCode(deviceId, code), null);
        if (tokens.accessToken().isEmpty()) {
            throw new BluelinkApiException("Invalid token response");
        }
        final CcsToken ccsToken = exchangeCcsToken(deviceId, tokens);
        session = new Session(deviceId, tokens);
        logger.debug("CCI login successful");
        return ccsToken;
    }

    CcsToken refresh() throws BluelinkApiException {
        final Session current = session;
        if (current == null) {
            throw new IllegalStateException("not logged in");
        }
        final String url = config.apiBaseUrl() + "/domain/api/v2/auth/token-refresh";
        final Request request = cciRequest(url, current.deviceId(), current.tokens())
                .content(new StringContentProvider(gson.toJson(current.tokens())), "application/json");
        final ContentResponse response = send(request, "CCI token refresh");
        final CciTokens tokens = merge(current.tokens(), parseJson(response, CciTokens.class, "CCI token refresh"),
                cookieValue(response, "t"));
        // the refresh token has been rotated at this point, keep it even if the exchange below fails
        session = new Session(current.deviceId(), tokens);
        final CcsToken ccsToken = exchangeCcsToken(current.deviceId(), tokens);
        logger.debug("CCI token refresh successful");
        return ccsToken;
    }

    // the account's HTTP client keeps the login session cookies, stale ones from an earlier login must not be sent
    private void clearLoginCookies() {
        final CookieStore cookieStore = httpClient.getCookieStore();
        final URI uri = URI.create(loginBaseUrl);
        for (final HttpCookie cookie : cookieStore.get(uri)) {
            cookieStore.remove(uri, cookie);
        }
    }

    private void authorize() throws BluelinkApiException {
        final String url = loginBaseUrl + "/auth/api/v2/user/oauth2/authorize?response_type=code&client_id="
                + config.clientId() + "&redirect_uri=" + config.redirectUri() + "&lang=en&state=ccsp&country=de";
        // a blocked login only shows on the page behind the redirect
        final ContentResponse response = send(httpClient.newRequest(url).method(HttpMethod.GET)
                .header(HttpHeader.USER_AGENT, USER_AGENT).followRedirects(true), "authorize");
        if (response.getContentAsString().toLowerCase(Locale.ROOT).contains("abusing")
                || response.getRequest().getURI().toString().contains("/error?status=400")) {
            throw new LoginRejectedException(Reason.BLOCKED, "Login blocked by the Bluelink server");
        }
        if (response.getStatus() != HttpStatus.OK_200) {
            throw failure(response, "authorize");
        }
    }

    private SigningKeyResponse.Key fetchSigningKey() throws BluelinkApiException {
        final Request request = httpClient.newRequest(loginBaseUrl + "/auth/api/v1/accounts/certs")
                .method(HttpMethod.GET).header(HttpHeader.USER_AGENT, USER_AGENT)
                .header(HttpHeader.ACCEPT, "application/json");
        final SigningKeyResponse.Key key = sendJson(request, SigningKeyResponse.class, "fetch signing key").retValue();
        if (key == null || key.kid() == null || key.n() == null || key.e() == null) {
            throw new BluelinkApiException("fetch signing key: invalid response");
        }
        return key;
    }

    private static String encryptPassword(final String password, final SigningKeyResponse.Key key)
            throws BluelinkApiException {
        try {
            final Base64.Decoder decoder = Base64.getUrlDecoder();
            final RSAPublicKeySpec spec = new RSAPublicKeySpec(new BigInteger(1, decoder.decode(key.n())),
                    new BigInteger(1, decoder.decode(key.e())));
            final PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(spec);
            final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return HexFormat.of().formatHex(cipher.doFinal(password.getBytes(StandardCharsets.UTF_8)));
        } catch (final GeneralSecurityException | IllegalArgumentException e) {
            throw new BluelinkApiException("Failed to encrypt password", e);
        }
    }

    private String signIn(final String username, final String encryptedPassword, final String kid)
            throws BluelinkApiException {
        final Fields fields = new Fields();
        fields.add("client_id", config.clientId());
        fields.add("encryptedPassword", "true");
        fields.add("password", encryptedPassword);
        fields.add("redirect_uri", config.redirectUri());
        fields.add("scope", "");
        fields.add("nonce", "");
        fields.add("state", "ccsp");
        fields.add("username", username);
        fields.add("connector_session_key", "");
        fields.add("kid", kid);
        fields.add("_csrf", "");

        // the authorization code is passed in the redirect location
        final Request request = httpClient.newRequest(loginBaseUrl + "/auth/account/signin").method(HttpMethod.POST)
                .header(HttpHeader.USER_AGENT, USER_AGENT).content(new FormContentProvider(fields))
                .followRedirects(false);
        final ContentResponse response = send(request, "sign in");
        if (response.getStatus() != HttpStatus.FOUND_302) {
            throw failure(response, "sign in");
        }
        final String location = response.getHeaders().get(HttpHeader.LOCATION);
        if (location == null) {
            throw new BluelinkApiException("sign in: missing redirect");
        }
        // split by hand, error redirects may contain characters that java.net.URI rejects
        final int queryStart = location.indexOf('?');
        final String path = queryStart < 0 ? location : location.substring(0, queryStart);
        final String query = queryStart < 0 ? "" : location.substring(queryStart + 1);
        final String code = queryParameter(query, "code");
        if (code != null && !code.isEmpty()) {
            return code;
        }
        if (path.contains("/web/v1/user/authorization")) {
            throw new LoginRejectedException(Reason.CONSENT_REQUIRED, "Account consent required");
        }
        final String error = queryParameter(query, "error_description");
        if (error != null) {
            throw new LoginRejectedException(Reason.REJECTED, "Login failed: " + error, error);
        }
        if (path.contains("authorize")) {
            throw new LoginRejectedException(Reason.INVALID_CREDENTIALS, "Login failed: invalid username or password");
        }
        throw new BluelinkApiException("sign in: unexpected redirect");
    }

    private static @Nullable String cookieValue(final ContentResponse response, final String name) {
        for (final String header : response.getHeaders().getValuesList(HttpHeader.SET_COOKIE)) {
            try {
                for (final HttpCookie cookie : HttpCookie.parse(header)) {
                    if (name.equals(cookie.getName()) && !cookie.getValue().isEmpty()) {
                        return cookie.getValue();
                    }
                }
            } catch (final IllegalArgumentException e) {
                // malformed cookie, not the one we are looking for
            }
        }
        return null;
    }

    private CciTokens exchangeCode(final String deviceId, final String code) throws BluelinkApiException {
        final String url = config.apiBaseUrl() + "/domain/api/v1/auth/token?code="
                + URLEncoder.encode(code, StandardCharsets.UTF_8);
        return sendJson(cciRequest(url, deviceId, null), CciTokens.class, "CCI token exchange");
    }

    private CcsToken exchangeCcsToken(final String deviceId, final CciTokens tokens) throws BluelinkApiException {
        final String url = config.apiBaseUrl() + "/domain/api/v1/auth/token-exchange?serviceType=CCS";
        final CcsTokenResponse response = sendJson(cciRequest(url, deviceId, tokens), CcsTokenResponse.class,
                "CCS token exchange");
        final String accessToken = response.accessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            throw new BluelinkApiException("Invalid token response");
        }
        return new CcsToken(accessToken, tokenExpiry(response.expiresTime()));
    }

    // expiresTime is a unix timestamp; fall back to a fixed lifetime if it looks wrong
    private static Instant tokenExpiry(final @Nullable Long expiresTime) {
        final Instant now = Instant.now();
        if (expiresTime != null) {
            final Instant expiry = Instant.ofEpochSecond(expiresTime);
            if (expiry.isAfter(now.plus(TOKEN_EXPIRY_MARGIN)) && expiry.isBefore(now.plus(MAX_TOKEN_LIFETIME))) {
                return expiry;
            }
        }
        return now.plus(DEFAULT_TOKEN_LIFETIME);
    }

    private Request cciRequest(final String url, final String deviceId, final @Nullable CciTokens tokens) {
        final Request request = httpClient.newRequest(url).method(HttpMethod.POST)
                .content(new StringContentProvider("")).header("client-id", config.packageId())
                .header("client-name", config.clientName()).header("client-version", CLIENT_VERSION)
                .header("client-os-code", "ios").header("client-os-version", config.osVersion())
                .header("client-device-id", deviceId).header("client-device-model", "iPhone")
                .header("client-notification-provider-type", config.notificationProvider())
                .header("locale", LANGUAGE.toUpperCase(Locale.ROOT)).header("timezone", timeZoneOffset())
                .header(HttpHeader.ACCEPT, "application/json").header(HttpHeader.ACCEPT_LANGUAGE, LANGUAGE)
                .header(HttpHeader.USER_AGENT, USER_AGENT);
        if (tokens != null) {
            if (!tokens.accessToken().isEmpty()) {
                request.header(HttpHeader.AUTHORIZATION, "Bearer " + tokens.accessToken());
            }
            header(request, "Authentication", tokens.nonCcsToken());
            header(request, "exchangeable-token", tokens.exchangeableAccessToken());
            header(request, "non-ccs-token", tokens.nonCcsToken());
        }
        return request;
    }

    private static void header(final Request request, final String name, final String value) {
        if (!value.isEmpty()) {
            request.header(name, value);
        }
    }

    private String timeZoneOffset() {
        final int seconds = timeZoneProvider.getTimeZone().getRules().getOffset(Instant.now()).getTotalSeconds();
        final int abs = Math.abs(seconds);
        return String.format(Locale.ROOT, "%s%02d:%02d", seconds < 0 ? "-" : "+", abs / 3600, abs % 3600 / 60);
    }

    // the refresh response may omit unchanged tokens, a rotated exchangeable token may arrive as cookie instead
    private static CciTokens merge(final CciTokens current, final CciTokens update,
            final @Nullable String exchangeableToken) {
        return new CciTokens(pick(update.accessToken(), current.accessToken()),
                pick(update.refreshToken(), current.refreshToken()), pick(update.nonCcsToken(), current.nonCcsToken()),
                pick(exchangeableToken, pick(update.exchangeableAccessToken(), current.exchangeableAccessToken())),
                pick(update.exchangeableRefreshToken(), current.exchangeableRefreshToken()),
                pick(update.nonCcsRefreshToken(), current.nonCcsRefreshToken()),
                pick(update.idToken(), current.idToken()));
    }

    private static String pick(final @Nullable String update, final String current) {
        return update != null && !update.isEmpty() ? update : current;
    }

    private static @Nullable String queryParameter(final String query, final String name) {
        for (final String pair : query.split("&")) {
            final int idx = pair.indexOf('=');
            if (name.equals(decode(idx < 0 ? pair : pair.substring(0, idx)))) {
                return idx < 0 ? "" : decode(pair.substring(idx + 1));
            }
        }
        return null;
    }

    private static String decode(final String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (final IllegalArgumentException e) {
            return value;
        }
    }

    private <T> T sendJson(final Request request, final Class<T> type, final String op) throws BluelinkApiException {
        return parseJson(send(request, op), type, op);
    }

    private <T> T parseJson(final ContentResponse response, final Class<T> type, final String op)
            throws BluelinkApiException {
        if (response.getStatus() != HttpStatus.OK_200) {
            throw failure(response, op);
        }
        try {
            final @Nullable T result = gson.fromJson(response.getContentAsString(), type);
            if (result == null) {
                throw new BluelinkApiException(op + ": empty response");
            }
            return result;
        } catch (final JsonSyntaxException e) {
            throw new BluelinkApiException(op + ": invalid response", e);
        }
    }

    private BluelinkApiException failure(final ContentResponse response, final String op) {
        final int status = response.getStatus();
        logger.debug("operation failed ({}): {} - {}", op, status, response.getContentAsString());
        return operationFailed(op, status, isServerError(status));
    }
}
