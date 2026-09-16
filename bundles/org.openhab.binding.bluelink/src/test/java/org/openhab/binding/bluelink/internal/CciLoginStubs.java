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
package org.openhab.binding.bluelink.internal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;

import javax.crypto.Cipher;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;

/**
 * WireMock stubs for the OneApp/CCI password login.
 *
 * @author Carlo Dischler - Initial contribution
 */
@NonNullByDefault
public final class CciLoginStubs {

    public static final String AUTH_CODE = "test-auth-code";
    public static final String CCI_ACCESS_TOKEN = "test-cci-access-token";
    public static final String CCI_REFRESH_TOKEN = "test-cci-refresh-token";
    public static final String NON_CCS_TOKEN = "test-non-ccs-token";
    public static final String EXCHANGEABLE_TOKEN = "test-exchangeable-token";
    public static final String CCS_ACCESS_TOKEN = "test-ccs-access-token";

    private static final KeyPair KEY_PAIR = generateKeyPair();

    private final WireMockServer server;

    public CciLoginStubs(final WireMockServer server) {
        this.server = server;
    }

    private static KeyPair generateKeyPair() {
        try {
            final KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (final GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    public void stubLogin() {
        final RSAPublicKey publicKey = (RSAPublicKey) KEY_PAIR.getPublic();
        server.stubFor(get(urlPathEqualTo("/auth/api/v2/user/oauth2/authorize"))
                .willReturn(aResponse().withStatus(302).withHeader("Location", "/auth/login-page?lang=en")
                        .withHeader("Set-Cookie", "session=test-session; Path=/; Max-Age=1800; HttpOnly")));
        server.stubFor(get(urlPathEqualTo("/auth/login-page")).willReturn(aResponse().withStatus(200)
                .withHeader("Set-Cookie", "csrf=test-csrf; Path=/").withBody("<html>login</html>")));
        server.stubFor(get(urlEqualTo("/auth/api/v1/accounts/certs"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("{\"retValue\":{\"kid\":\"test-kid\",\"n\":\"" + jwkParam(publicKey.getModulus())
                                + "\",\"e\":\"" + jwkParam(publicKey.getPublicExponent()) + "\"}}")));
        stubSignInRedirect("https://oneapp.hyundai.com/redirect?code=" + AUTH_CODE + "&state=ccsp");
        server.stubFor(post(urlPathEqualTo("/domain/api/v1/auth/token")).withQueryParam("code", equalTo(AUTH_CODE))
                .willReturn(json(cciTokens(CCI_ACCESS_TOKEN, CCI_REFRESH_TOKEN))));
        stubTokenExchange(CCS_ACCESS_TOKEN, Instant.now().plusSeconds(3600).getEpochSecond());
    }

    public void stubTokenExchange(final String ccsAccessToken, final long expiresTime) {
        final String body = """
                {"accessToken": "%s", "expiresTime": %d}
                """.formatted(ccsAccessToken, expiresTime);
        server.stubFor(post(urlPathEqualTo("/domain/api/v1/auth/token-exchange"))
                .withQueryParam("serviceType", equalTo("CCS")).willReturn(json(body)));
    }

    public ResponseDefinitionBuilder stubTokenRefresh(final String accessToken, final String refreshToken) {
        final ResponseDefinitionBuilder response = json(cciTokens(accessToken, refreshToken));
        server.stubFor(post(urlEqualTo("/domain/api/v2/auth/token-refresh")).willReturn(response));
        return response;
    }

    public void stubSignInRedirect(final String location) {
        server.stubFor(post(urlEqualTo("/auth/account/signin"))
                .willReturn(aResponse().withStatus(302).withHeader("Location", location)));
    }

    public static ResponseDefinitionBuilder json(final String body) {
        return aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(body);
    }

    public static String cciTokens(final String accessToken, final String refreshToken) {
        return """
                {
                  "accessToken": "%s",
                  "refreshToken": "%s",
                  "nonCcsToken": "%s",
                  "exchangeableAccessToken": "%s",
                  "exchangeableRefreshToken": "test-exchangeable-refresh-token",
                  "nonCcsRefreshToken": "test-non-ccs-refresh-token",
                  "idToken": "test-id-token",
                  "expiresIn": 3599
                }
                """.formatted(accessToken, refreshToken, NON_CCS_TOKEN, EXCHANGEABLE_TOKEN);
    }

    public String decryptPassword(final String hex) {
        try {
            final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, KEY_PAIR.getPrivate());
            return new String(cipher.doFinal(HexFormat.of().parseHex(hex)), StandardCharsets.UTF_8);
        } catch (final GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String jwkParam(final BigInteger value) {
        byte[] bytes = value.toByteArray();
        if (bytes.length > 1 && bytes[0] == 0) {
            bytes = Arrays.copyOfRange(bytes, 1, bytes.length);
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
