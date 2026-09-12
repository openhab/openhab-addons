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
package org.openhab.binding.spotify.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SpotifyAuthServlet}, focused on how it determines the scheme (http/https) to use for the Spotify
 * redirect_uri from a request that may have passed through one or more reverse proxies.
 *
 * @author Claude Sonnet 5 - Initial contribution
 */
@NonNullByDefault
public class SpotifyAuthServletTest {

    private @NonNullByDefault({}) SpotifyAuthService spotifyAuthService;
    private @NonNullByDefault({}) SpotifyAuthServlet servlet;
    private @NonNullByDefault({}) HttpServletRequest req;

    @BeforeEach
    public void setUp() {
        spotifyAuthService = mock(SpotifyAuthService.class);
        servlet = new SpotifyAuthServlet(spotifyAuthService, "", "");
        req = mock(HttpServletRequest.class);
        when(req.getScheme()).thenReturn("http");
    }

    @Test
    public void noHeadersFallsBackToRequestScheme() {
        assertEquals("http", servlet.determineScheme(req));
    }

    @Test
    public void validForwardedProtoIsUsed() {
        when(req.getHeader("X-Forwarded-Proto")).thenReturn("https");

        assertEquals("https", servlet.determineScheme(req));
    }

    @Test
    public void forwardedProtoIsCaseInsensitiveAndTrimmed() {
        when(req.getHeader("X-Forwarded-Proto")).thenReturn("  HTTPS  ");

        assertEquals("https", servlet.determineScheme(req));
    }

    @Test
    public void chainedForwardedProtoUsesFirstEntry() {
        when(req.getHeader("X-Forwarded-Proto")).thenReturn("https, http");

        assertEquals("https", servlet.determineScheme(req));
    }

    @Test
    public void invalidForwardedProtoFallsThroughToRequestScheme() {
        when(req.getHeader("X-Forwarded-Proto")).thenReturn("ftp");

        assertEquals("http", servlet.determineScheme(req));
    }

    @Test
    public void invalidForwardedProtoFallsThroughToLowerPriorityValidHeader() {
        // Regression test: an unrecognized value on the highest-priority header must not shadow a valid,
        // lower-priority one.
        when(req.getHeader("X-Forwarded-Proto")).thenReturn("ftp");
        when(req.getHeader("Forwarded")).thenReturn("for=1.2.3.4;proto=https;by=203.0.113.43");

        assertEquals("https", servlet.determineScheme(req));
    }

    @Test
    public void forwardedSslOnMapsToHttps() {
        when(req.getHeader("X-Forwarded-Ssl")).thenReturn("on");

        assertEquals("https", servlet.determineScheme(req));
    }

    @Test
    public void forwardedSslOffMapsToHttp() {
        when(req.getScheme()).thenReturn("https");
        when(req.getHeader("X-Forwarded-Ssl")).thenReturn("off");

        assertEquals("http", servlet.determineScheme(req));
    }

    @Test
    public void forwardedSslUnrecognizedValueFallsThrough() {
        when(req.getHeader("X-Forwarded-Ssl")).thenReturn("maybe");

        assertEquals("http", servlet.determineScheme(req));
    }

    @Test
    public void frontEndHttpsOnMapsToHttps() {
        when(req.getHeader("Front-End-Https")).thenReturn("On");

        assertEquals("https", servlet.determineScheme(req));
    }

    @Test
    public void frontEndHttpsUnrecognizedValueFallsThrough() {
        when(req.getHeader("Front-End-Https")).thenReturn("yes");

        assertEquals("http", servlet.determineScheme(req));
    }

    @Test
    public void forwardedHeaderValidProtoIsUsed() {
        when(req.getHeader("Forwarded")).thenReturn("for=1.2.3.4;proto=https;by=203.0.113.43");

        assertEquals("https", servlet.determineScheme(req));
    }

    @Test
    public void forwardedHeaderInvalidProtoFallsThroughToRequestScheme() {
        when(req.getHeader("Forwarded")).thenReturn("for=1.2.3.4;proto=ftp;by=203.0.113.43");

        assertEquals("http", servlet.determineScheme(req));
    }

    @Test
    public void precedenceForwardedProtoWinsOverAllOthersWhenValid() {
        when(req.getHeader("X-Forwarded-Proto")).thenReturn("https");
        when(req.getHeader("X-Forwarded-Ssl")).thenReturn("off");
        when(req.getHeader("Front-End-Https")).thenReturn("off");
        when(req.getHeader("Forwarded")).thenReturn("proto=http");

        assertEquals("https", servlet.determineScheme(req));
    }

    @Test
    public void precedenceForwardedSslWinsOverFrontEndHttpsAndForwarded() {
        when(req.getHeader("X-Forwarded-Ssl")).thenReturn("on");
        when(req.getHeader("Front-End-Https")).thenReturn("off");
        when(req.getHeader("Forwarded")).thenReturn("proto=http");

        assertEquals("https", servlet.determineScheme(req));
    }

    @Test
    public void precedenceFrontEndHttpsWinsOverForwarded() {
        when(req.getHeader("Front-End-Https")).thenReturn("on");
        when(req.getHeader("Forwarded")).thenReturn("proto=http");

        assertEquals("https", servlet.determineScheme(req));
    }

    @Test
    public void forceHttpsOverrideIgnoresAllHeadersAndRequestScheme() {
        when(spotifyAuthService.isForceHttps()).thenReturn(true);
        when(req.getHeader("X-Forwarded-Proto")).thenReturn("http");
        when(req.getRequestURL()).thenReturn(new StringBuffer("http://example.com/connectspotify"));

        assertEquals("https://example.com/connectspotify", servlet.extractServletBaseURL(req));
    }

    @Test
    public void extractServletBaseURLAppliesDeterminedScheme() {
        when(req.getHeader("X-Forwarded-Proto")).thenReturn("https");
        when(req.getRequestURL()).thenReturn(new StringBuffer("http://example.com/connectspotify"));

        assertEquals("https://example.com/connectspotify", servlet.extractServletBaseURL(req));
    }
}
