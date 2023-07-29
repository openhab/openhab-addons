/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.windcentrale.internal.dto;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests (de)serialization of AWS Cognito requests/responses to/from JSON.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class CognitoGsonTest {

    private static final DataUtil DATA_UTIL = new DataUtil(CognitoGson.GSON);

    @Test
    public void serializeInitiateAuthRequestSrp() throws IOException {
        String json = DATA_UTIL.toJson(InitiateAuthRequest.userSrpAuth("clientId123", "username456", "srpA789"));
        assertThat(json, is(DATA_UTIL.fromFile("initiate-auth-request-srp.json")));
    }

    @Test
    public void deserializeChallengeResponseSrp() throws IOException {
        ChallengeResponse response = DATA_UTIL.fromJson("challenge-response-srp.json", ChallengeResponse.class);
        assertThat(response, is(notNullValue()));

        assertThat(response.challengeName, is("PASSWORD_VERIFIER"));
        assertThat(response.getSalt(), is("salt123"));
        assertThat(response.getSecretBlock(), is("secretBlock456"));
        assertThat(response.getSrpB(), is("srpB789"));
        assertThat(response.getUsername(), is("username@acme.com"));
        assertThat(response.getUserIdForSrp(), is("userid@acme.com"));
    }

    @Test
    public void serializeInitiateAuthRequestRefresh() throws IOException {
        String json = DATA_UTIL.toJson(InitiateAuthRequest.refreshTokenAuth("clientId123", "refreshToken123"));
        assertThat(json, is(DATA_UTIL.fromFile("initiate-auth-request-refresh.json")));
    }

    @Test
    public void deserializeInitiateAuthResponseRefresh() throws IOException {
        AuthenticationResultResponse response = DATA_UTIL.fromJson("authentication-result-response-refresh.json",
                AuthenticationResultResponse.class);
        assertThat(response, is(notNullValue()));

        assertThat(response.getAccessToken(), is("accessToken123"));
        assertThat(response.getExpiresIn(), is(3600));
        assertThat(response.getIdToken(), is("idToken456"));
        assertThat(response.getRefreshToken(), is(""));
        assertThat(response.getTokenType(), is("Bearer"));
    }

    @Test
    public void serializeRespondToAuthChallengeRequest() throws IOException {
        String json = DATA_UTIL.toJson(new RespondToAuthChallengeRequest("clientId123", "username@acme.com",
                "passwordClaimSecretBlock456", "passwordClaimSignature789", "Thu Apr 6 07:16:19 UTC 2023"));
        assertThat(json, is(DATA_UTIL.fromFile("respond-to-auth-challenge-request.json")));
    }

    @Test
    public void deserializeRespondToAuthChallengeResponse() throws IOException {
        AuthenticationResultResponse response = DATA_UTIL.fromJson("authentication-result-response-challenge.json",
                AuthenticationResultResponse.class);
        assertThat(response, is(notNullValue()));

        assertThat(response.getAccessToken(), is("accessToken123"));
        assertThat(response.getExpiresIn(), is(3600));
        assertThat(response.getIdToken(), is("idToken456"));
        assertThat(response.getRefreshToken(), is("refreshToken789"));
        assertThat(response.getTokenType(), is("Bearer"));
    }

    @Test
    public void deserializeErrorResponseInvalidParameter() throws IOException {
        CognitoError response = DATA_UTIL.fromJson("cognito-error-response-invalid-parameter.json", CognitoError.class);
        assertThat(response, is(notNullValue()));

        assertThat(response.type, is("InvalidParameterException"));
        assertThat(response.message, is("Missing required parameter REFRESH_TOKEN"));
    }

    @Test
    public void deserializeErrorResponseNotAuthorized() throws IOException {
        CognitoError response = DATA_UTIL.fromJson("cognito-error-response-not-authorized.json", CognitoError.class);
        assertThat(response, is(notNullValue()));

        assertThat(response.type, is("NotAuthorizedException"));
        assertThat(response.message, is("Incorrect username or password."));
    }
}
