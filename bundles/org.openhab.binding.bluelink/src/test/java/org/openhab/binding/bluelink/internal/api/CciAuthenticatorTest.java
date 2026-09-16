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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.bluelink.internal.BluelinkBindingConstants.HTTP_REQUEST_BUFFER_SIZE;
import static org.openhab.binding.bluelink.internal.CciLoginStubs.*;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bluelink.internal.CciLoginStubs;
import org.openhab.binding.bluelink.internal.MockApiData;
import org.openhab.core.i18n.TimeZoneProvider;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;

/**
 * @author Carlo Dischler - Initial contribution
 */
@NonNullByDefault
class CciAuthenticatorTest {

    private static final WireMockServer SERVER = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    private static final HttpClient HTTP_CLIENT = new HttpClient();
    private static final String IDP = "https://idpconnect-eu.hyundai.com";

    private final TimeZoneProvider timeZoneProvider = () -> ZoneId.of("Europe/Berlin");
    private @NonNullByDefault({}) CciLoginStubs stubs;
    private @NonNullByDefault({}) CciAuthenticator authenticator;

    @BeforeAll
    static void startServer() throws Exception {
        SERVER.start();
        HTTP_CLIENT.setRequestBufferSize(HTTP_REQUEST_BUFFER_SIZE);
        HTTP_CLIENT.start();
    }

    @AfterAll
    static void stopServer() throws Exception {
        SERVER.stop();
        HTTP_CLIENT.stop();
    }

    @BeforeEach
    void setUp() {
        SERVER.resetAll();
        stubs = new CciLoginStubs(SERVER);
        final String baseUrl = "http://localhost:" + SERVER.port();
        final CciAuthenticator.Config config = new CciAuthenticator.Config("test-client-id",
                "https://oneapp.hyundai.com/redirect", baseUrl, "com.hyundai.oneapp.eu", "hyundai", "18.7", "APNS");
        authenticator = new CciAuthenticator(HTTP_CLIENT, timeZoneProvider, config, baseUrl);
    }

    @Test
    void testLoginEncryptsPasswordAndExchangesTokens() throws Exception {
        stubs.stubLogin();

        final CciAuthenticator.CcsToken token = authenticator.login(MockApiData.TEST_USERNAME,
                MockApiData.TEST_PASSWORD);

        assertEquals(CCS_ACCESS_TOKEN, token.accessToken());
        final Duration lifetime = Duration.between(Instant.now(), token.expiry());
        assertTrue(lifetime.toSeconds() > 3500 && lifetime.toSeconds() <= 3600, "lifetime " + lifetime);
        assertTrue(authenticator.hasSession());

        final List<LoggedRequest> signIn = SERVER.findAll(postRequestedFor(urlEqualTo("/auth/account/signin")));
        assertEquals(1, signIn.size());
        SERVER.verify(postRequestedFor(urlEqualTo("/auth/account/signin"))
                .withFormParam("username", equalTo(MockApiData.TEST_USERNAME))
                .withFormParam("encryptedPassword", equalTo("true")).withFormParam("kid", equalTo("test-kid"))
                .withFormParam("client_id", equalTo("test-client-id")));
        final String encryptedPassword = signIn.getFirst().formParameter("password").firstValue();
        assertEquals(MockApiData.TEST_PASSWORD, stubs.decryptPassword(encryptedPassword));
        final String cookies = signIn.getFirst().getHeader("Cookie");
        assertEquals("csrf=test-csrf; session=test-session",
                Stream.of(cookies.split("; ")).sorted().collect(Collectors.joining("; ")));

        SERVER.verify(postRequestedFor(urlPathEqualTo("/domain/api/v1/auth/token"))
                .withHeader("client-id", equalTo("com.hyundai.oneapp.eu")).withHeader("client-name", equalTo("hyundai"))
                .withHeader("client-os-version", equalTo("18.7")).withHeader("locale", equalTo("EN"))
                .withHeader("timezone", matching("[+-]\\d\\d:\\d\\d")).withoutHeader("Authorization"));
        SERVER.verify(postRequestedFor(urlPathEqualTo("/domain/api/v1/auth/token-exchange"))
                .withHeader("Authorization", equalTo("Bearer " + CCI_ACCESS_TOKEN))
                .withHeader("Authentication", equalTo(NON_CCS_TOKEN))
                .withHeader("exchangeable-token", equalTo(EXCHANGEABLE_TOKEN))
                .withHeader("non-ccs-token", equalTo(NON_CCS_TOKEN)));
    }

    @Test
    void testRefreshKeepsUnchangedTokensAndFallsBackOnBadExpiry() throws Exception {
        stubs.stubLogin();
        authenticator.login(MockApiData.TEST_USERNAME, MockApiData.TEST_PASSWORD);

        SERVER.stubFor(post(urlEqualTo("/domain/api/v2/auth/token-refresh")).willReturn(json("""
                {"accessToken": "cci-access-2", "refreshToken": "cci-refresh-2"}
                """).withHeader("Set-Cookie", "t=rotated-exchangeable; Path=/; HttpOnly")));
        stubs.stubTokenExchange("ccs-2", Instant.now().plusSeconds(30).getEpochSecond());

        final CciAuthenticator.CcsToken token = authenticator.refresh();

        assertEquals("ccs-2", token.accessToken());
        final Duration lifetime = Duration.between(Instant.now(), token.expiry());
        assertTrue(lifetime.toSeconds() > 3500 && lifetime.toSeconds() <= 3600, "lifetime " + lifetime);

        SERVER.verify(postRequestedFor(urlEqualTo("/domain/api/v2/auth/token-refresh"))
                .withHeader("Content-Type", containing("application/json"))
                .withHeader("Authorization", equalTo("Bearer " + CCI_ACCESS_TOKEN))
                .withRequestBody(matchingJsonPath("$.refreshToken", equalTo(CCI_REFRESH_TOKEN)))
                .withRequestBody(matchingJsonPath("$.idToken", equalTo("test-id-token"))));
        SERVER.verify(postRequestedFor(urlPathEqualTo("/domain/api/v1/auth/token-exchange"))
                .withHeader("Authorization", equalTo("Bearer cci-access-2"))
                .withHeader("Authentication", equalTo(NON_CCS_TOKEN))
                .withHeader("exchangeable-token", equalTo("rotated-exchangeable")));

        // the rotated refresh token is used next time
        authenticator.refresh();
        SERVER.verify(postRequestedFor(urlEqualTo("/domain/api/v2/auth/token-refresh"))
                .withRequestBody(matchingJsonPath("$.refreshToken", equalTo("cci-refresh-2"))));
    }

    @Test
    void testLoginFailsWhenAuthorizeIsBlocked() {
        stubs.stubLogin();
        SERVER.stubFor(get(urlPathEqualTo("/auth/api/v2/user/oauth2/authorize"))
                .willReturn(aResponse().withStatus(200).withBody("Your request looks like abusing our system")));

        final LoginRejectedException e = assertThrows(LoginRejectedException.class,
                () -> authenticator.login(MockApiData.TEST_USERNAME, MockApiData.TEST_PASSWORD));
        assertEquals(LoginRejectedException.Reason.BLOCKED, e.getReason());
        assertEquals("@text/account-handler.login.blocked", e.getStatusDescription());
        assertFalse(authenticator.hasSession());
        SERVER.verify(0, postRequestedFor(urlEqualTo("/auth/account/signin")));
    }

    @Test
    void testLoginFailsWhenConsentIsRequired() {
        stubs.stubLogin();
        stubs.stubSignInRedirect(IDP + "/web/v1/user/authorization?client_id=test");

        final LoginRejectedException e = assertThrows(LoginRejectedException.class,
                () -> authenticator.login(MockApiData.TEST_USERNAME, MockApiData.TEST_PASSWORD));
        assertEquals(LoginRejectedException.Reason.CONSENT_REQUIRED, e.getReason());
    }

    @Test
    void testLoginFailsOnWrongCredentials() {
        stubs.stubLogin();
        stubs.stubSignInRedirect(IDP + "/auth/api/v2/user/oauth2/authorize?error_description=Invalid+password");

        final LoginRejectedException e = assertThrows(LoginRejectedException.class,
                () -> authenticator.login(MockApiData.TEST_USERNAME, MockApiData.TEST_PASSWORD));
        assertEquals(LoginRejectedException.Reason.REJECTED, e.getReason());
        assertEquals("@text/account-handler.login.rejected [\"Invalid password\"]", e.getStatusDescription());
        SERVER.verify(0, postRequestedFor(urlPathEqualTo("/domain/api/v1/auth/token")));
    }

    @Test
    void testServerErrorIsRetryable() {
        stubs.stubLogin();
        SERVER.stubFor(get(urlEqualTo("/auth/api/v1/accounts/certs")).willReturn(aResponse().withStatus(503)));

        final BluelinkApiException e = assertThrows(RetryableRequestException.class,
                () -> authenticator.login(MockApiData.TEST_USERNAME, MockApiData.TEST_PASSWORD));
        assertEquals("operation failed: fetch signing key - 503", e.getMessage());
    }

    @Test
    void testLoginWithLongTokens() throws Exception {
        stubs.stubLogin();
        final String longToken = "x".repeat(1500);
        SERVER.stubFor(post(urlPathEqualTo("/domain/api/v1/auth/token")).willReturn(json("""
                {"accessToken": "%1$s", "refreshToken": "r", "nonCcsToken": "%1$s", "exchangeableAccessToken": "%1$s"}
                """.formatted(longToken))));

        assertEquals(CCS_ACCESS_TOKEN,
                authenticator.login(MockApiData.TEST_USERNAME, MockApiData.TEST_PASSWORD).accessToken());
    }

    @Test
    void testLoginReportsErrorFromUnencodedRedirect() {
        stubs.stubLogin();
        stubs.stubSignInRedirect(
                IDP + "/auth/api/v2/user/oauth2/authorize?error_description=Invalid user name or password");

        final LoginRejectedException e = assertThrows(LoginRejectedException.class,
                () -> authenticator.login(MockApiData.TEST_USERNAME, MockApiData.TEST_PASSWORD));
        assertEquals("@text/account-handler.login.rejected [\"Invalid user name or password\"]",
                e.getStatusDescription());
    }

    @Test
    void testLoginFailsWithoutCciAccessToken() {
        stubs.stubLogin();
        SERVER.stubFor(post(urlPathEqualTo("/domain/api/v1/auth/token")).willReturn(json("{\"errCode\":\"4002\"}")));

        final BluelinkApiException e = assertThrows(BluelinkApiException.class,
                () -> authenticator.login(MockApiData.TEST_USERNAME, MockApiData.TEST_PASSWORD));
        assertTrue(String.valueOf(e.getMessage()).contains("Invalid token response"), e.getMessage());
        SERVER.verify(0, postRequestedFor(urlPathEqualTo("/domain/api/v1/auth/token-exchange")));
    }

    @Test
    void testLoginsDoNotShareCookies() throws Exception {
        stubs.stubLogin();
        authenticator.login(MockApiData.TEST_USERNAME, MockApiData.TEST_PASSWORD);
        SERVER.stubFor(get(urlPathEqualTo("/auth/login-page"))
                .willReturn(aResponse().withStatus(200).withBody("<html>login</html>")));
        SERVER.resetRequests();

        authenticator.login(MockApiData.TEST_USERNAME, MockApiData.TEST_PASSWORD);

        final String cookies = SERVER.findAll(postRequestedFor(urlEqualTo("/auth/account/signin"))).getFirst()
                .getHeader("Cookie");
        assertFalse(cookies.contains("csrf"), cookies);
    }

    @Test
    void testRefreshKeepsRotatedTokensWhenExchangeFails() throws Exception {
        stubs.stubLogin();
        authenticator.login(MockApiData.TEST_USERNAME, MockApiData.TEST_PASSWORD);
        stubs.stubTokenRefresh("cci-access-2", "cci-refresh-2");
        SERVER.stubFor(
                post(urlPathEqualTo("/domain/api/v1/auth/token-exchange")).willReturn(aResponse().withStatus(503)));

        assertThrows(RetryableRequestException.class, () -> authenticator.refresh());

        stubs.stubTokenExchange("ccs-2", Instant.now().plusSeconds(3600).getEpochSecond());
        assertEquals("ccs-2", authenticator.refresh().accessToken());
        SERVER.verify(postRequestedFor(urlEqualTo("/domain/api/v2/auth/token-refresh"))
                .withRequestBody(matchingJsonPath("$.refreshToken", equalTo("cci-refresh-2"))));
    }

    @Test
    void testConnectionFailureIsRetryable() {
        stubs.stubLogin();
        SERVER.stubFor(get(urlEqualTo("/auth/api/v1/accounts/certs"))
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

        assertThrows(RetryableRequestException.class,
                () -> authenticator.login(MockApiData.TEST_USERNAME, MockApiData.TEST_PASSWORD));
    }

    @Test
    void testLoginFailsOnInvalidCredentialsWithoutDescription() {
        stubs.stubLogin();
        stubs.stubSignInRedirect(IDP + "/auth/api/v2/user/oauth2/authorize?client_id=test");

        final LoginRejectedException e = assertThrows(LoginRejectedException.class,
                () -> authenticator.login(MockApiData.TEST_USERNAME, MockApiData.TEST_PASSWORD));
        assertEquals("@text/account-handler.login.invalid-credentials", e.getStatusDescription());
    }

    @Test
    void testStatusDescriptionArgumentIsSanitized() {
        final LoginRejectedException e = new LoginRejectedException(LoginRejectedException.Reason.REJECTED, "msg",
                "Wrong \"password\", [try again]");
        assertEquals("@text/account-handler.login.rejected [\"Wrong password try again\"]", e.getStatusDescription());
    }
}
