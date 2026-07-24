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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.melcloud.internal.exceptions.MelCloudCommException;
import org.openhab.binding.melcloud.internal.exceptions.MelCloudHomeAuthException;

/**
 * Unit tests for {@link MelCloudHomeAuthService} (see ADR-002). The underlying {@link HttpClient} is mocked so each
 * step of the PAR -> authorize -> Cognito -> token-exchange chain can be verified without any real network access.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("unchecked")
class MelCloudHomeAuthServiceTest {

    private static final String COGNITO_LOGIN_URL = "https://live-melcloudhome.auth.eu-west-1.amazoncognito.com/login"
            + "?client_id=cognitoclient&redirect_uri=https%3A%2F%2Fauth.melcloudhome.com%2Fsignin-oidc-meu";

    private HttpClient httpClient = mock(HttpClient.class);

    @BeforeEach
    void setUp() {
        httpClient = mock(HttpClient.class);
        when(httpClient.followRedirects()).thenReturn(HttpClient.Redirect.NEVER);
    }

    @Test
    void whenHttpClientDoesNotDisableRedirects_thenConstructorThrows() {
        // Arrange
        HttpClient misconfiguredClient = mock(HttpClient.class);
        when(misconfiguredClient.followRedirects()).thenReturn(HttpClient.Redirect.NORMAL);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new MelCloudHomeAuthService(misconfiguredClient));
    }

    @Test
    void whenAuthServerHasExistingSession_thenLoginSkipsCognitoAndReturnsTokens() throws Exception {
        // Arrange
        HttpResponse<String> parResponse = fakeResponse(201, "{\"request_uri\":\"urn:par:abc\"}", Map.of());
        HttpResponse<String> authorizeResponse = fakeResponse(302, "",
                Map.of("Location", List.of("melcloudhome://callback?code=EXISTING123&state=xyz")));
        HttpResponse<String> tokenResponse = fakeResponse(200,
                "{\"access_token\":\"AT1\",\"refresh_token\":\"RT1\",\"expires_in\":3600}", Map.of());
        when(httpClient.send(any(HttpRequest.class), anyBodyHandler())).thenReturn(parResponse, authorizeResponse,
                tokenResponse);
        MelCloudHomeAuthService authService = new MelCloudHomeAuthService(httpClient);

        // Act
        MelCloudHomeTokenResponse result = authService.login("user@example.com", "secret");

        // Assert
        assertEquals("AT1", result.accessToken);
        assertEquals("RT1", result.refreshToken);
        assertEquals(3600, result.expiresIn);
    }

    @Test
    void whenCognitoLoginPageIsReached_thenLoginSubmitsCredentialsAndReturnsTokens() throws Exception {
        // Arrange
        HttpResponse<String> parResponse = fakeResponse(201, "{\"request_uri\":\"urn:par:abc\"}", Map.of());
        HttpResponse<String> authorizeRedirect = fakeResponse(302, "", Map.of("Location", List.of(COGNITO_LOGIN_URL)));
        HttpResponse<String> cognitoLoginPage = fakeResponse(200,
                "<html><input name=\"_csrf\" value=\"csrf-token-123\"/></html>", Map.of());
        HttpResponse<String> cognitoCredentialsAccepted = fakeResponse(302, "",
                Map.of("Location", List.of("https://auth.melcloudhome.com/signin-oidc-meu?code=cog123&state=abc")));
        // auth.melcloudhome.com completes its own internal exchange and redirects the app to melcloudhome://.
        HttpResponse<String> signinOidcCallback = fakeResponse(302, "",
                Map.of("Location", List.of("melcloudhome://callback?code=FINALCODE&state=xyz")));
        HttpResponse<String> tokenResponse = fakeResponse(200,
                "{\"access_token\":\"AT2\",\"refresh_token\":\"RT2\",\"expires_in\":1800}", Map.of());
        when(httpClient.send(any(HttpRequest.class), anyBodyHandler())).thenReturn(parResponse, authorizeRedirect,
                cognitoLoginPage, cognitoCredentialsAccepted, signinOidcCallback, tokenResponse);
        MelCloudHomeAuthService authService = new MelCloudHomeAuthService(httpClient);

        // Act
        MelCloudHomeTokenResponse result = authService.login("user@example.com", "secret");

        // Assert
        assertEquals("AT2", result.accessToken);
        assertEquals("RT2", result.refreshToken);
    }

    @Test
    void whenCognitoAcceptanceBouncesThroughSafeRedirectPage_thenLoginFollowsItAndReturnsTokens() throws Exception {
        // Arrange
        HttpResponse<String> parResponse = fakeResponse(201, "{\"request_uri\":\"urn:par:abc\"}", Map.of());
        HttpResponse<String> authorizeRedirect = fakeResponse(302, "", Map.of("Location", List.of(COGNITO_LOGIN_URL)));
        HttpResponse<String> cognitoLoginPage = fakeResponse(200,
                "<html><input name=\"_csrf\" value=\"csrf-token-123\"/></html>", Map.of());
        // Duende's "safe redirect" helper: instead of a 3xx, it answers 200 and embeds the real next hop in its
        // own query string (RedirectUri), URL-encoded once.
        String innerTarget = "/connect/authorize/callback?request_uri=REQ123&client_id=homemobile";
        String bounceUrl = "https://auth.melcloudhome.com/Redirect?RedirectUri="
                + URLEncoder.encode(innerTarget, StandardCharsets.UTF_8);
        HttpResponse<String> cognitoCredentialsAccepted = fakeResponse(302, "", Map.of("Location", List.of(bounceUrl)));
        HttpResponse<String> safeRedirectBouncePage = fakeResponse(200, "", Map.of());
        HttpResponse<String> callbackRedirect = fakeResponse(302, "",
                Map.of("Location", List.of("melcloudhome://callback?code=FINALCODE2&state=xyz")));
        HttpResponse<String> tokenResponse = fakeResponse(200,
                "{\"access_token\":\"AT4\",\"refresh_token\":\"RT4\",\"expires_in\":1200}", Map.of());
        when(httpClient.send(any(HttpRequest.class), anyBodyHandler())).thenReturn(parResponse, authorizeRedirect,
                cognitoLoginPage, cognitoCredentialsAccepted, safeRedirectBouncePage, callbackRedirect, tokenResponse);
        MelCloudHomeAuthService authService = new MelCloudHomeAuthService(httpClient);

        // Act
        MelCloudHomeTokenResponse result = authService.login("user@example.com", "secret");

        // Assert
        assertEquals("AT4", result.accessToken);
        assertEquals("RT4", result.refreshToken);
    }

    @Test
    void whenCognitoRejectsCredentials_thenLoginThrowsAuthException() throws Exception {
        // Arrange
        HttpResponse<String> parResponse = fakeResponse(201, "{\"request_uri\":\"urn:par:abc\"}", Map.of());
        HttpResponse<String> authorizeRedirect = fakeResponse(302, "", Map.of("Location", List.of(COGNITO_LOGIN_URL)));
        HttpResponse<String> cognitoLoginPage = fakeResponse(200,
                "<html><input name=\"_csrf\" value=\"csrf-token-123\"/></html>", Map.of());
        // Wrong password: Cognito re-renders the same login page instead of redirecting onward.
        HttpResponse<String> cognitoLoginRejected = fakeResponse(200,
                "<html>Incorrect username or password.<input name=\"_csrf\" value=\"csrf-token-456\"/></html>",
                Map.of());
        when(httpClient.send(any(HttpRequest.class), anyBodyHandler())).thenReturn(parResponse, authorizeRedirect,
                cognitoLoginPage, cognitoLoginRejected);
        MelCloudHomeAuthService authService = new MelCloudHomeAuthService(httpClient);

        // Act & Assert
        assertThrows(MelCloudHomeAuthException.class, () -> authService.login("user@example.com", "wrong-password"));
    }

    @Test
    void whenNetworkFails_thenLoginThrowsCommExceptionNotAuthException() throws Exception {
        // Arrange
        when(httpClient.send(any(HttpRequest.class), anyBodyHandler())).thenThrow(new IOException("connection reset"));
        MelCloudHomeAuthService authService = new MelCloudHomeAuthService(httpClient);

        // Act & Assert
        MelCloudCommException exception = assertThrows(MelCloudCommException.class,
                () -> authService.login("user@example.com", "secret"));
        assertFalse(exception instanceof MelCloudHomeAuthException);
    }

    @Test
    void whenParResponseIsMalformed_thenLoginThrowsAuthException() throws Exception {
        // Arrange
        HttpResponse<String> parResponse = fakeResponse(201, "{ not valid json", Map.of());
        when(httpClient.send(any(HttpRequest.class), anyBodyHandler())).thenReturn(parResponse);
        MelCloudHomeAuthService authService = new MelCloudHomeAuthService(httpClient);

        // Act & Assert
        assertThrows(MelCloudHomeAuthException.class, () -> authService.login("user@example.com", "secret"));
    }

    @Test
    void whenRefreshTokenIsValid_thenRefreshTokenReturnsNewTokens() throws Exception {
        // Arrange
        HttpResponse<String> tokenResponse = fakeResponse(200,
                "{\"access_token\":\"AT3\",\"refresh_token\":\"RT3\",\"expires_in\":900}", Map.of());
        when(httpClient.send(any(HttpRequest.class), anyBodyHandler())).thenReturn(tokenResponse);
        MelCloudHomeAuthService authService = new MelCloudHomeAuthService(httpClient);

        // Act
        MelCloudHomeTokenResponse result = authService.refreshToken("old-refresh-token");

        // Assert
        assertEquals("AT3", result.accessToken);
        assertEquals("RT3", result.refreshToken);
    }

    @Test
    void whenRefreshTokenIsRejected_thenRefreshTokenThrowsAuthException() throws Exception {
        // Arrange
        HttpResponse<String> rejectedResponse = fakeResponse(401, "{\"error\":\"invalid_grant\"}", Map.of());
        when(httpClient.send(any(HttpRequest.class), anyBodyHandler())).thenReturn(rejectedResponse);
        MelCloudHomeAuthService authService = new MelCloudHomeAuthService(httpClient);

        // Act & Assert
        assertThrows(MelCloudHomeAuthException.class, () -> authService.refreshToken("expired-refresh-token"));
    }

    @Test
    void whenRefreshTokenIsBlank_thenRefreshTokenThrowsIllegalArgumentException() {
        // Arrange
        MelCloudHomeAuthService authService = new MelCloudHomeAuthService(httpClient);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> authService.refreshToken(""));
    }

    /**
     * Type witness helper: {@code any()} alone cannot infer {@code HttpClient.send}'s generic body-handler type
     * parameter as {@code String} (it resolves to {@code Object}, which the compiler then rejects against the
     * {@code HttpResponse<String>} return type expected by {@code thenReturn}/{@code thenThrow}). Returning it
     * through a method whose declared return type is {@code HttpResponse.BodyHandler<String>} fixes the inference.
     */
    private static HttpResponse.BodyHandler<String> anyBodyHandler() {
        return any();
    }

    private static HttpResponse<String> fakeResponse(int statusCode, String body, Map<String, List<String>> headers) {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(statusCode);
        when(response.body()).thenReturn(body);
        when(response.headers()).thenReturn(HttpHeaders.of(headers, (name, value) -> true));
        return response;
    }
}
