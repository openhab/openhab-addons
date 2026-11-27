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
package org.openhab.binding.lgthinq.lgservices.api;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.openhab.binding.lgthinq.lgservices.LGServicesConstants.LG_API_CLIENT_ID;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.binding.lgthinq.lgservices.api.LGThinqOauthEmpAuthenticator.LoginAccountResult;
import org.openhab.binding.lgthinq.lgservices.api.model.GatewayResult;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;

/**
 * Tests for the EMP session token request of {@link LGThinqOauthEmpAuthenticator}.
 *
 * @author Nemer Daud - Initial contribution
 */
@WireMockTest
@NonNullByDefault
class LGThinqOauthEmpAuthenticatorTest {
    private static final String SIGNATURE_PATH = "/spx/signature";
    private static final String EMP_SESSION_PATH = "/emp/oauth2/token/empsession";
    private static final String SIGNATURE = "ZW1wLXNlc3Npb24tc2lnbmF0dXJl";
    private static final String TOKEN_RESPONSE = """
            {
              "status": 1,
              "access_token": "fake-access-token",
              "expires_in": "3600",
              "refresh_token": "fake-refresh-token",
              "oauth2_backend_url": "http://localhost/"
            }
            """;
    private static final LoginAccountResult ACCOUNT = new LoginAccountResult("LGE", "someone@some.url", "BR",
            "emp;11111111;222222222");

    private final HttpClient httpClient = new HttpClient();
    private LGThinqGateway gateway = new LGThinqGateway();

    @BeforeEach
    void setUp(WireMockRuntimeInfo wireMock) throws Exception {
        httpClient.start();
        String baseUrl = wireMock.getHttpBaseUrl();
        GatewayResult gatewayResult = new GatewayResult("0000", "", baseUrl, baseUrl + "/api", baseUrl + "/v1", baseUrl,
                baseUrl, baseUrl, baseUrl + "/spx");
        gateway = new LGThinqGateway(gatewayResult, "en-US", "BR", baseUrl);
    }

    @AfterEach
    void tearDown() throws Exception {
        httpClient.stop();
    }

    @Test
    void getTokenSignsEmpSessionWithSignatureFromServer() throws Exception {
        stubFor(get(urlPathEqualTo(SIGNATURE_PATH)).willReturn(ok(SIGNATURE + "\n")));
        stubFor(post(EMP_SESSION_PATH).withHeader("lgemp-x-signature", equalTo(SIGNATURE))
                .willReturn(okJson(TOKEN_RESPONSE)));

        TokenResult token = new LGThinqOauthEmpAuthenticator(httpClient).getToken(gateway, ACCOUNT);

        assertEquals("fake-access-token", token.getAccessToken());
        List<LoggedRequest> empSessionRequests = findAll(postRequestedFor(urlEqualTo(EMP_SESSION_PATH)));
        assertEquals(1, empSessionRequests.size());
        String date = Objects.requireNonNull(empSessionRequests.get(0).getHeader("lgemp-x-date"));
        Map<String, String> empData = new LinkedHashMap<>();
        empData.put("account_type", ACCOUNT.userIdType());
        empData.put("client_id", LG_API_CLIENT_ID);
        empData.put("country_code", ACCOUNT.country());
        empData.put("username", ACCOUNT.userId());
        String expectedMessage = RestUtils.getTokenSignatureMessage(gateway.getTokenSessionEmpUrl(), empData, date);
        verify(getRequestedFor(urlPathEqualTo(SIGNATURE_PATH)).withQueryParam("kind", equalTo("oauth2"))
                .withQueryParam("key_name", equalTo("OAUTH_SECRETKEY")).withQueryParam("sever_type", equalTo("OP"))
                .withQueryParam("timestamp", equalTo(expectedMessage)));
        verify(0, getRequestedFor(urlPathMatching(".*/searchKey")));
    }

    @Test
    void getTokenFailsWhenSignatureEndpointIsUnavailable() {
        stubFor(get(urlPathEqualTo(SIGNATURE_PATH)).willReturn(notFound().withBody("<!DOCTYPE html><html></html>")));

        assertThrows(IllegalStateException.class,
                () -> new LGThinqOauthEmpAuthenticator(httpClient).getToken(gateway, ACCOUNT));
        verify(0, postRequestedFor(urlEqualTo(EMP_SESSION_PATH)));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " \n", "<!DOCTYPE html><html></html>" })
    void getTokenRejectsInvalidSignature(String signatureResponse) {
        stubFor(get(urlPathEqualTo(SIGNATURE_PATH)).willReturn(ok(signatureResponse)));

        assertThrows(IllegalStateException.class,
                () -> new LGThinqOauthEmpAuthenticator(httpClient).getToken(gateway, ACCOUNT));
        verify(0, postRequestedFor(urlEqualTo(EMP_SESSION_PATH)));
    }
}
