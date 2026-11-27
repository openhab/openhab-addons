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
package org.openhab.binding.volvocars.internal.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;

/**
 * Mock of core's {@link OAuthClientService} that keeps the imported token and returns it, like the real service
 * while the token has not expired.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
class OAuthServiceMock {
    final OAuthClientService service = mock(OAuthClientService.class);
    @Nullable
    AccessTokenResponse token;

    OAuthServiceMock() throws Exception {
        doAnswer(invocation -> {
            token = invocation.getArgument(0);
            return null;
        }).when(service).importAccessTokenResponse(any());
        when(service.getAccessTokenResponse()).thenAnswer(invocation -> token);
    }

    /**
     * Lets the next refresh store and return a new token, as the real service does.
     */
    void refreshReturns(String accessToken, String refreshToken) throws Exception {
        when(service.refreshToken()).thenAnswer(invocation -> {
            AccessTokenResponse refreshed = token(accessToken, refreshToken);
            token = refreshed;
            return refreshed;
        });
    }

    static AccessTokenResponse token(String accessToken, String refreshToken) {
        AccessTokenResponse response = new AccessTokenResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(1799);
        response.setCreatedOn(Instant.now());
        return response;
    }
}
