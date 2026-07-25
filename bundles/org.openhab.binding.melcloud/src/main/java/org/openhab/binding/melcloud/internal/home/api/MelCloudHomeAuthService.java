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
package org.openhab.binding.melcloud.internal.home.api;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.melcloud.internal.exceptions.MelCloudCommException;
import org.openhab.binding.melcloud.internal.exceptions.MelCloudHomeAuthException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Implements the real MELCloud Home login: OAuth 2.0 Authorization Code + PKCE, fronted by a Pushed Authorization
 * Request (PAR, RFC 9126) and federated to an AWS Cognito Hosted UI for credential entry.
 *
 * <p>
 * This class runs the entire flow headlessly, with no interactive browser step:
 * <ol>
 * <li>{@code POST connect/par} with the PKCE challenge, obtaining an opaque {@code request_uri}.
 * <li>{@code GET connect/authorize?client_id=...&request_uri=...}, following redirects manually. If
 * {@code auth.melcloudhome.com} already has a live session, this redirects straight to the {@code melcloudhome://}
 * callback with an authorization code; otherwise it redirects to a Cognito Hosted UI login page.
 * <li>If a Cognito login page was reached: scrape the page's CSRF token and {@code POST} the username/password to
 * it directly (matching the reference implementation confirmed in {@code reversed.md}), then follow the resulting
 * redirect chain the same way.
 * <li>Capture the authorization {@code code} from the final redirect to the {@code melcloudhome://} custom scheme —
 * intercepted by inspecting the {@code Location} header manually, since the injected {@link HttpClient} does not
 * follow redirects automatically.
 * <li>{@code POST connect/token} to exchange the code (or, for {@link #refreshToken(String)}, a stored refresh
 * token) for an access/refresh token pair.
 * </ol>
 *
 * <p>
 * The injected {@link HttpClient} <b>must</b> be configured with {@link HttpClient.Redirect#NEVER} and a
 * {@link java.net.CookieHandler} (so the session cookie set by {@code auth.melcloudhome.com}/Cognito survives
 * across requests); see {@code MelCloudHandlerFactory} for how it is constructed.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MelCloudHomeAuthService {

    private static final String AUTH_BASE_URL = "https://auth.melcloudhome.com";
    private static final String PAR_URL = AUTH_BASE_URL + "/connect/par";
    private static final String AUTHORIZE_URL = AUTH_BASE_URL + "/connect/authorize";
    private static final String TOKEN_URL = AUTH_BASE_URL + "/connect/token";

    private static final String CLIENT_ID = "homemobile";
    private static final String REDIRECT_URI = "melcloudhome://";
    private static final String SCOPES = "openid profile email offline_access IdentityServerApi";

    private static final String COGNITO_DOMAIN_SUFFIX = ".amazoncognito.com";
    // Matches the mobile Safari UA the reference implementation uses when submitting credentials to Cognito's
    // hosted login page; the page's behavior appears to be tuned for a mobile browser.
    private static final String COGNITO_LOGIN_USER_AGENT = "Mozilla/5.0 (iPhone; CPU iPhone OS 18_5 like Mac OS X) "
            + "AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/22F76";

    private static final int MAX_REDIRECT_HOPS = 10;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);

    private static final Pattern CSRF_PATTERN_NAME_FIRST = Pattern.compile("name=\"_csrf\"[^>]*value=\"([^\"]+)\"");
    private static final Pattern CSRF_PATTERN_VALUE_FIRST = Pattern.compile("value=\"([^\"]+)\"[^>]*name=\"_csrf\"");
    private static final Pattern CODE_PATTERN = Pattern.compile("[?&]code=([^&\"' ]+)");
    // Duende IdentityServer's "safe redirect" bounce page (used to avoid header-based open-redirect issues when
    // returning from the external Cognito login): it answers HTTP 200 with the real next hop embedded in its own
    // query string rather than issuing a 3xx, so it needs to be detected and followed explicitly.
    private static final Pattern REDIRECT_URI_PARAM_PATTERN = Pattern.compile("[?&]RedirectUri=([^&]+)");

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final Logger logger = LoggerFactory.getLogger(MelCloudHomeAuthService.class);
    private final HttpClient httpClient;
    private final Gson gson = new Gson();

    /**
     * @param httpClient an {@link HttpClient} configured with {@link HttpClient.Redirect#NEVER} and a cookie
     *            handler
     * @throws IllegalArgumentException if {@code httpClient} does not have {@link HttpClient.Redirect#NEVER}
     *             configured
     */
    public MelCloudHomeAuthService(HttpClient httpClient) {
        if (httpClient.followRedirects() != HttpClient.Redirect.NEVER) {
            throw new IllegalArgumentException("httpClient must be configured with HttpClient.Redirect.NEVER so "
                    + "redirects to the " + REDIRECT_URI + " custom scheme can be intercepted");
        }
        this.httpClient = httpClient;
    }

    /**
     * Logs in to MELCloud Home with an email/password pair, running the full PAR + Cognito-federated OAuth flow.
     *
     * @param username the MELCloud Home account email address
     * @param password the MELCloud Home account password; never logged
     * @return the resulting access/refresh token pair
     * @throws MelCloudHomeAuthException if the login is rejected (e.g. invalid credentials) or the flow cannot be
     *             completed
     * @throws MelCloudCommException if a network/server error occurs
     * @throws IllegalArgumentException if {@code username} or {@code password} is blank
     */
    public MelCloudHomeTokenResponse login(String username, String password) throws MelCloudCommException {
        if (username.isBlank()) {
            throw new IllegalArgumentException("username is null");
        }
        if (password.isBlank()) {
            throw new IllegalArgumentException("password is null");
        }

        logger.debug("Starting MELCloud Home login flow");
        String codeVerifier = generateCodeVerifier();
        String codeChallenge = generateCodeChallenge(codeVerifier);
        String state = generateRandomToken();

        String requestUri = pushedAuthorizationRequest(state, codeChallenge);
        AuthorizeOutcome outcome = authorize(requestUri);

        String authorizationCode = switch (outcome) {
            case AuthorizationCode authorizationCodeOutcome -> {
                logger.debug("Existing MELCloud Home session detected, skipping Cognito credential submission");
                yield authorizationCodeOutcome.code();
            }
            case CognitoLoginPage loginPage -> submitCognitoCredentials(loginPage, username, password);
        };

        return exchangeCodeForTokens(authorizationCode, codeVerifier);
    }

    /**
     * Exchanges a previously obtained refresh token for a new access/refresh token pair.
     *
     * @param refreshToken a refresh token obtained from a prior {@link #login(String, String)} or
     *            {@link #refreshToken(String)} call
     * @return the resulting access/refresh token pair
     * @throws MelCloudHomeAuthException if the refresh token is rejected
     * @throws MelCloudCommException if a network/server error occurs
     * @throws IllegalArgumentException if {@code refreshToken} is blank
     */
    public MelCloudHomeTokenResponse refreshToken(String refreshToken) throws MelCloudCommException {
        if (refreshToken.isBlank()) {
            throw new IllegalArgumentException("refreshToken is null");
        }

        logger.debug("Refreshing MELCloud Home access token");
        Map<String, String> formParams = new LinkedHashMap<>();
        formParams.put("grant_type", "refresh_token");
        formParams.put("refresh_token", refreshToken);
        formParams.put("client_id", CLIENT_ID);

        HttpResponse<String> response = sendFormPost(TOKEN_URL, formParams, Map.of());
        if (response.statusCode() != 200) {
            throw new MelCloudHomeAuthException("Refresh token rejected: HTTP " + response.statusCode());
        }
        return toTokenResponse(bodyOf(response));
    }

    private String pushedAuthorizationRequest(String state, String codeChallenge) throws MelCloudCommException {
        logger.debug("Step 1: pushed authorization request (PAR)");
        Map<String, String> formParams = new LinkedHashMap<>();
        formParams.put("response_type", "code");
        formParams.put("state", state);
        formParams.put("code_challenge", codeChallenge);
        formParams.put("code_challenge_method", "S256");
        formParams.put("client_id", CLIENT_ID);
        formParams.put("scope", SCOPES);
        formParams.put("redirect_uri", REDIRECT_URI);

        HttpResponse<String> response = sendFormPost(PAR_URL, formParams, Map.of());
        if (response.statusCode() != 201) {
            throw new MelCloudHomeAuthException("Pushed authorization request failed: HTTP " + response.statusCode());
        }

        MelCloudHomeParResponse parResponse = parseJson(bodyOf(response), MelCloudHomeParResponse.class,
                "pushed authorization response");
        String requestUri = parResponse.requestUri;
        if (requestUri == null || requestUri.isBlank()) {
            throw new MelCloudHomeAuthException("Pushed authorization response did not contain a request_uri");
        }
        return requestUri;
    }

    private AuthorizeOutcome authorize(String requestUri) throws MelCloudCommException {
        logger.debug("Step 2: following the authorize redirect chain");
        String authorizeUrl = AUTHORIZE_URL + "?client_id=" + urlEncode(CLIENT_ID) + "&request_uri="
                + urlEncode(requestUri);
        return followRedirectsToOutcome(authorizeUrl);
    }

    private String submitCognitoCredentials(CognitoLoginPage loginPage, String username, String password)
            throws MelCloudCommException {
        logger.debug("Step 3: submitting credentials to Cognito");
        Map<String, String> formParams = new LinkedHashMap<>();
        formParams.put("_csrf", loginPage.csrfToken());
        formParams.put("username", username);
        formParams.put("password", password);
        formParams.put("cognitoAsfData", "");

        URI loginUri = URI.create(loginPage.loginUrl());
        Map<String, String> extraHeaders = new LinkedHashMap<>();
        extraHeaders.put("User-Agent", COGNITO_LOGIN_USER_AGENT);
        extraHeaders.put("Origin", loginUri.getScheme() + "://" + loginUri.getHost());
        extraHeaders.put("Referer", loginPage.loginUrl());

        HttpResponse<String> response = sendFormPost(loginPage.loginUrl(), formParams, extraHeaders);
        int status = response.statusCode();

        if (status == 200 && isCognitoLoginPage(loginPage.loginUrl())) {
            // Cognito re-rendered the same login page instead of redirecting onward — the standard shape of an
            // "incorrect username or password" response.
            throw new MelCloudHomeAuthException("MELCloud Home login rejected: invalid username or password");
        }

        if (status >= 300 && status < 400) {
            String location = response.headers().firstValue("Location").orElseThrow(() -> new MelCloudHomeAuthException(
                    "Cognito login response (HTTP " + status + ") without a Location header"));
            if (location.startsWith(REDIRECT_URI)) {
                return extractCode(location);
            }
            String resolvedLocation = resolveLocation(loginPage.loginUrl(), location);
            if (isCognitoLoginPage(resolvedLocation)) {
                throw new MelCloudHomeAuthException("MELCloud Home login rejected: invalid username or password");
            }
            return switch (followRedirectsToOutcome(resolvedLocation)) {
                case AuthorizationCode authorizationCode -> authorizationCode.code();
                // The pattern variable is required by switch syntax but unused in the arrow body — Java 21 has no
                // unnamed-pattern syntax outside preview (JEP 443/456), so it cannot simply be omitted.
                case CognitoLoginPage rejected -> // NOPMD - UnusedLocalVariable: binding required, see comment above
                    throw new MelCloudHomeAuthException("MELCloud Home login rejected: invalid username or password");
            };
        }

        throw new MelCloudHomeAuthException("Unexpected HTTP status while submitting Cognito credentials: " + status);
    }

    private MelCloudHomeTokenResponse exchangeCodeForTokens(String code, String codeVerifier)
            throws MelCloudCommException {
        logger.debug("Step 4: exchanging the authorization code for tokens");
        Map<String, String> formParams = new LinkedHashMap<>();
        formParams.put("grant_type", "authorization_code");
        formParams.put("code", code);
        formParams.put("redirect_uri", REDIRECT_URI);
        formParams.put("code_verifier", codeVerifier);
        formParams.put("client_id", CLIENT_ID);

        HttpResponse<String> response = sendFormPost(TOKEN_URL, formParams, Map.of());
        if (response.statusCode() != 200) {
            throw new MelCloudHomeAuthException("Token exchange failed: HTTP " + response.statusCode());
        }
        return toTokenResponse(bodyOf(response));
    }

    private MelCloudHomeTokenResponse toTokenResponse(String body) throws MelCloudCommException {
        MelCloudHomeTokenResponse tokenResponse = parseJson(body, MelCloudHomeTokenResponse.class, "token response");
        String accessToken = tokenResponse.accessToken;
        if (accessToken == null || accessToken.isBlank()) {
            throw new MelCloudHomeAuthException("Token response did not contain an access_token");
        }
        logger.debug("MELCloud Home authentication successful");
        return tokenResponse;
    }

    /**
     * Follows a chain of HTTP redirects starting at {@code startUrl}, stopping as soon as either the
     * {@code melcloudhome://} callback or a Cognito login page is reached.
     */
    private AuthorizeOutcome followRedirectsToOutcome(String startUrl) throws MelCloudCommException {
        String currentUrl = startUrl;
        for (int hop = 0; hop < MAX_REDIRECT_HOPS; hop++) {
            HttpResponse<String> response = sendGet(currentUrl);
            int status = response.statusCode();

            if (status >= 300 && status < 400) {
                String location = response.headers().firstValue("Location")
                        .orElseThrow(() -> new MelCloudHomeAuthException(
                                "Redirect response (HTTP " + status + ") without a Location header"));
                if (location.startsWith(REDIRECT_URI)) {
                    return new AuthorizationCode(extractCode(location));
                }
                currentUrl = resolveLocation(currentUrl, location);
                continue;
            }

            if (status == 200) {
                Optional<String> redirectUriParam = extractRedirectUriParam(currentUrl);
                if (redirectUriParam.isPresent()) {
                    // Duende's "safe redirect" bounce page (GET .../Redirect?RedirectUri=<url-encoded next hop>) —
                    // it never issues a 3xx itself, precisely to avoid header-based open-redirect issues; the real
                    // next hop is embedded in its own query string instead.
                    currentUrl = resolveLocation(currentUrl, redirectUriParam.get());
                    continue;
                }

                String body = bodyOf(response);
                if (isCognitoLoginPage(currentUrl)) {
                    String csrfToken = extractCsrfToken(body).orElseThrow(() -> new MelCloudHomeAuthException(
                            "Failed to extract the CSRF token from the Cognito login page"));
                    return new CognitoLoginPage(currentUrl, csrfToken);
                }
                // auth.melcloudhome.com already had a live session and returned the callback body directly
                // instead of issuing a redirect; the authorization code is embedded in the page body.
                Matcher matcher = CODE_PATTERN.matcher(body);
                if (matcher.find()) {
                    return new AuthorizationCode(matcher.group(1));
                }
                throw new MelCloudHomeAuthException("Unexpected HTTP 200 response while authorizing: " + currentUrl);
            }

            throw new MelCloudHomeAuthException(
                    "Unexpected HTTP status " + status + " while authorizing: " + currentUrl);
        }
        throw new MelCloudHomeAuthException("Exceeded " + MAX_REDIRECT_HOPS + " redirect hops while authorizing");
    }

    private String resolveLocation(String currentUrl, String location) throws MelCloudHomeAuthException {
        try {
            return URI.create(currentUrl).resolve(location).toString();
        } catch (IllegalArgumentException e) {
            throw new MelCloudHomeAuthException("Malformed redirect Location header: " + location, e);
        }
    }

    private boolean isCognitoLoginPage(String url) {
        try {
            URI uri = URI.create(url);
            String host = uri.getHost();
            String path = uri.getPath();
            return host != null && host.endsWith(COGNITO_DOMAIN_SUFFIX) && path != null && path.contains("/login");
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private Optional<String> extractCsrfToken(String html) {
        Matcher matcher = CSRF_PATTERN_NAME_FIRST.matcher(html);
        if (matcher.find()) {
            return Optional.of(matcher.group(1));
        }
        matcher = CSRF_PATTERN_VALUE_FIRST.matcher(html);
        if (matcher.find()) {
            return Optional.of(matcher.group(1));
        }
        return Optional.empty();
    }

    private Optional<String> extractRedirectUriParam(String url) {
        Matcher matcher = REDIRECT_URI_PARAM_PATTERN.matcher(url);
        if (matcher.find()) {
            return Optional.of(URLDecoder.decode(matcher.group(1), StandardCharsets.UTF_8));
        }
        return Optional.empty();
    }

    private String extractCode(String url) throws MelCloudHomeAuthException {
        Matcher matcher = CODE_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new MelCloudHomeAuthException(
                "Redirect to " + REDIRECT_URI + " did not contain a 'code' parameter: " + url);
    }

    private <T> T parseJson(String body, Class<T> type, String context) throws MelCloudHomeAuthException {
        try {
            @Nullable
            T parsed = gson.fromJson(body, type);
            if (parsed == null) {
                throw new MelCloudHomeAuthException("Received an empty " + context);
            }
            return parsed;
        } catch (JsonSyntaxException e) {
            throw new MelCloudHomeAuthException("Failed to parse " + context, e);
        }
    }

    /**
     * Reads {@link HttpResponse#body()}. {@link HttpResponse} is not designed with null type annotations in mind
     * (unlike this class, which is {@code @NonNullByDefault}), so the compiler can only produce an "unsafe
     * interpretation" advisory here rather than a genuine null-safety guarantee. Centralizing the call in this one
     * helper — instead of adding a redundant, always-false null check at every call site, which the compiler flags
     * as dead code once it has made that same "unsafe interpretation" — keeps the advisory confined to a single,
     * documented spot.
     *
     * @param response the HTTP response to read the body from
     * @return the response body
     */
    @SuppressWarnings("null")
    private static String bodyOf(HttpResponse<String> response) {
        return response.body();
    }

    private HttpResponse<String> sendGet(String url) throws MelCloudCommException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(REQUEST_TIMEOUT).GET().build();
        return send(request);
    }

    private HttpResponse<String> sendFormPost(String url, Map<String, String> formParams,
            Map<String, String> extraHeaders) throws MelCloudCommException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url)).timeout(REQUEST_TIMEOUT)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(encodeForm(formParams)));
        extraHeaders.forEach(builder::header);
        return send(builder.build());
    }

    private HttpResponse<String> send(HttpRequest request) throws MelCloudCommException {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new MelCloudCommException("Network error while communicating with " + request.uri(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MelCloudCommException("Interrupted while communicating with " + request.uri(), e);
        }
    }

    private static String encodeForm(Map<String, String> params) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!builder.isEmpty()) {
                builder.append('&');
            }
            builder.append(urlEncode(entry.getKey())).append('=').append(urlEncode(entry.getValue()));
        }
        return builder.toString();
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String generateCodeVerifier() {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private static String generateCodeChallenge(String codeVerifier) throws MelCloudHomeAuthException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is a mandatory algorithm for every Java platform implementation; unreachable in practice.
            throw new MelCloudHomeAuthException("SHA-256 is not available in this JVM", e);
        }
    }

    private static String generateRandomToken() {
        byte[] randomBytes = new byte[16];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Outcome of following the {@code connect/authorize} redirect chain: either an authorization code was obtained
     * directly (an existing {@code auth.melcloudhome.com} session was reused), or a Cognito login page needs
     * credentials submitted to it.
     */
    private sealed interface AuthorizeOutcome permits AuthorizationCode, CognitoLoginPage {
    }

    private record AuthorizationCode(String code) implements AuthorizeOutcome {
    }

    private record CognitoLoginPage(String loginUrl, String csrfToken) implements AuthorizeOutcome {
    }
}
