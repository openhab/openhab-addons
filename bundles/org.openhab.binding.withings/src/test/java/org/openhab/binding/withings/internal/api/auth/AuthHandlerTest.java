/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.withings.internal.api.auth;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.withings.internal.api.AbstractAPIHandlerTest;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;

/**
 * @author Sven Strohschein - Initial contribution
 */
public class AuthHandlerTest extends AbstractAPIHandlerTest {

    private AuthHandler authHandler;

    @BeforeEach
    public void before() {
        authHandler = new AuthHandler(accessTokenServiceMock, httpClientMock);
    }

    @Test
    public void testRedeemAuthCode() {
        mockRequest("{\n" + "    \"status\": 0,\n" + "    \"body\": {\n" + "        \"userid\": \"98765432\",\n"
                + "        \"access_token\": \"85201fa237e6bd8c3ae2c672e87a965733x04571\",\n"
                + "        \"refresh_token\": \"f68ff938a3745351f3eb38f0bbfa57ca60f4e9xx\",\n"
                + "        \"scope\": \"user.info,user.metrics,user.activity\",\n" + "        \"expires_in\": 10800,\n"
                + "        \"token_type\": \"Bearer\"\n" + "    }\n" + "}");

        Optional<WithingsAccessTokenResponse> tokenResponse = authHandler.redeemAuthCode("clientId", "clientSecret",
                "authCode");
        assertTrue(tokenResponse.isPresent());

        AccessTokenResponse accessTokenResponse = tokenResponse.get().createAccessTokenResponse();
        assertEquals("85201fa237e6bd8c3ae2c672e87a965733x04571", accessTokenResponse.getAccessToken());
        assertEquals("f68ff938a3745351f3eb38f0bbfa57ca60f4e9xx", accessTokenResponse.getRefreshToken());
        assertEquals("user.info,user.metrics,user.activity", accessTokenResponse.getScope());
        assertEquals("98765432", accessTokenResponse.getState()); // the userId is used as the state
        assertEquals("Bearer", accessTokenResponse.getTokenType());
        assertEquals(10800, accessTokenResponse.getExpiresIn());
        assertNotNull(accessTokenResponse.getCreatedOn());
    }

    @Test
    public void testRefreshAccessToken() {
        mockRequest("{\n" + "    \"status\": 0,\n" + "    \"body\": {\n" + "        \"userid\": 98765432,\n"
                + "        \"access_token\": \"85201fa237e6bd8c3ae2c672e87a965733x04571\",\n"
                + "        \"refresh_token\": \"f68ff938a3745351f3eb38f0bbfa57ca60f4e9xx\",\n"
                + "        \"scope\": \"user.info,user.metrics,user.activity\",\n" + "        \"expires_in\": 10800,\n"
                + "        \"token_type\": \"Bearer\"\n" + "    }\n" + "}");

        Optional<WithingsAccessTokenResponse> tokenResponse = authHandler.refreshAccessToken("clientId", "clientSecret",
                "refreshToken");
        assertTrue(tokenResponse.isPresent());

        AccessTokenResponse accessTokenResponse = tokenResponse.get().createAccessTokenResponse();
        assertEquals("85201fa237e6bd8c3ae2c672e87a965733x04571", accessTokenResponse.getAccessToken());
        assertEquals("f68ff938a3745351f3eb38f0bbfa57ca60f4e9xx", accessTokenResponse.getRefreshToken());
        assertEquals("user.info,user.metrics,user.activity", accessTokenResponse.getScope());
        assertEquals("98765432", accessTokenResponse.getState()); // the userId is used as the state
        assertEquals("Bearer", accessTokenResponse.getTokenType());
        assertEquals(10800, accessTokenResponse.getExpiresIn());
        assertNotNull(accessTokenResponse.getCreatedOn());
    }
}
