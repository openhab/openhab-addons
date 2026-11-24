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
 */
@NonNullByDefault
public class PostOAuth2ResponseTest {

    private PostOAuth2Response tokenExpiringIn(int seconds) {
        PostOAuth2Response token = new PostOAuth2Response();
        token.expiresIn = seconds;
        token.postProcess();
        return token;
    }

    @Test
    public void missingRefreshTokenUsesClientCredentialsWhenAccessTokenExpired() {
        assertTrue(tokenExpiringIn(0).needsNewToken());
    }

    @Test
    public void blankRefreshTokenUsesClientCredentialsWhenAccessTokenExpired() {
        PostOAuth2Response token = tokenExpiringIn(0);
        token.refreshToken = "";
        assertTrue(token.needsNewToken());
        token.refreshToken = "  ";
        assertTrue(token.needsNewToken());
    }

    @Test
    public void existingRefreshTokenIsUsedWhenAccessTokenExpired() {
        PostOAuth2Response token = tokenExpiringIn(0);
        token.refreshToken = "valid-refresh-token";
        assertFalse(token.needsNewToken());
    }

    @Test
    public void validAccessTokenIsRetainedWithoutRefreshToken() {
        assertFalse(tokenExpiringIn(3600).needsNewToken());
    }

    @Test
    public void validAccessTokenIsRetainedWithRefreshToken() {
        PostOAuth2Response token = tokenExpiringIn(3600);
        token.refreshToken = "valid-refresh-token";
        assertFalse(token.needsNewToken());
    }
}
