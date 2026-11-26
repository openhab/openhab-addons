/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.gardena;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.gardena.internal.model.dto.api.PostOAuth2Response;

/**
 * Tests for Gardena OAuth token renewal decisions.
 *
 * @author Kovács László - Initial contribution
 */
@NonNullByDefault
public class PostOAuth2ResponseTest {

    private PostOAuth2Response tokenExpiringIn(int seconds) {
        PostOAuth2Response token = new PostOAuth2Response();
        token.expiresIn = seconds;
        token.postProcess();
        return token;
    }

    private boolean needsNewToken(PostOAuth2Response token) {
        return token.needsNewToken(token.isAccessTokenExpired());
    }

    @Test
    public void expiredAccessTokenSnapshotWithoutRefreshTokenAlwaysUsesClientCredentials() {
        PostOAuth2Response token = tokenExpiringIn(3600);
        // Simulate an expired access-token decision passed to the renewal selector.
        assertTrue(token.needsNewToken(true));
    }

    @Test
    public void missingRefreshTokenUsesClientCredentialsWhenAccessTokenExpired() {
        assertTrue(needsNewToken(tokenExpiringIn(0)));
    }

    @Test
    public void blankRefreshTokenUsesClientCredentialsWhenAccessTokenExpired() {
        PostOAuth2Response token = tokenExpiringIn(0);
        token.refreshToken = "";
        assertTrue(needsNewToken(token));
        token.refreshToken = "  ";
        assertTrue(needsNewToken(token));
    }

    @Test
    public void existingRefreshTokenIsUsedWhenAccessTokenExpired() {
        PostOAuth2Response token = tokenExpiringIn(0);
        token.refreshToken = "valid-refresh-token";
        assertFalse(needsNewToken(token));
    }

    @Test
    public void validAccessTokenIsRetainedWithoutRefreshToken() {
        assertFalse(needsNewToken(tokenExpiringIn(3600)));
    }

    @Test
    public void validAccessTokenIsRetainedWithRefreshToken() {
        PostOAuth2Response token = tokenExpiringIn(3600);
        token.refreshToken = "valid-refresh-token";
        assertFalse(needsNewToken(token));
    }
}
