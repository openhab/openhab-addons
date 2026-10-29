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
package org.openhab.binding.icalendar.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests of {@link DigestAuthentication} using the examples of RFC 2617 and RFC 7616.
 *
 * @author Contributors - Initial contribution
 */
@NonNullByDefault
public class DigestAuthenticationTest {

    private static final String RFC7616_CNONCE = "f2/wE4q74E6zIJEtWaHKaf5wv/H5QzzpXusqGemxURZJ";
    private static final String RFC7616_CHALLENGE = "realm=\"http-auth@example.org\", qop=\"auth, auth-int\", "
            + "algorithm=%s, nonce=\"7ypf/xlj9XXwfDPEoM4URrv/xwf94BcCAzFZH4GiTo0v\", "
            + "opaque=\"FQhe/qaU925kfnzjCev0ciny7QMkPqMAFRtzCUYo5tdS\"";

    private static String authorize(String challenge, String method, String requestTarget, String username,
            String password, String cnonce) {
        return Objects.requireNonNull(
                DigestAuthentication.createAuthorization(challenge, method, requestTarget, username, password, cnonce));
    }

    private static String authorize(String challenge, String method, URI uri, String username, String password) {
        return Objects
                .requireNonNull(DigestAuthentication.createAuthorization(challenge, method, uri, username, password));
    }

    @Test
    public void testRfc2617Md5WithoutAlgorithm() {
        String challenge = "realm=\"testrealm@host.com\", qop=\"auth,auth-int\", "
                + "nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"";
        String header = authorize(challenge, "GET", "/dir/index.html", "Mufasa", "Circle Of Life", "0a4f113b");
        assertTrue(header.startsWith("Digest username=\"Mufasa\", realm=\"testrealm@host.com\""));
        assertTrue(header.contains("uri=\"/dir/index.html\""));
        assertTrue(header.contains("response=\"6629fae49393a05397450978507c4ef1\""));
        assertTrue(header.contains("qop=auth, nc=00000001, cnonce=\"0a4f113b\""));
        assertTrue(header.contains("opaque=\"5ccc069c403ebaf9f0171e9517f40e41\""));
    }

    @Test
    public void testRfc7616Sha256() {
        String header = authorize(String.format(RFC7616_CHALLENGE, "SHA-256"), "GET", "/dir/index.html", "Mufasa",
                "Circle of Life", RFC7616_CNONCE);
        assertTrue(header.contains("algorithm=SHA-256"));
        assertTrue(header.contains("response=\"753927fa0e85d155564e2e272a28d1802ca10daf4496794697cf8db5856cb6c1\""));
    }

    @Test
    public void testRfc7616Md5() {
        String header = authorize(String.format(RFC7616_CHALLENGE, "MD5"), "GET", "/dir/index.html", "Mufasa",
                "Circle of Life", RFC7616_CNONCE);
        assertTrue(header.contains("response=\"8ca523f5e9506fed4657c9700eebdbec\""));
    }

    @Test
    public void testUnsupportedChallenges() {
        // only auth-int
        assertNull(DigestAuthentication.createAuthorization("realm=\"r\", nonce=\"n\", qop=\"auth-int\"", "GET", "/",
                "u", "p", "c"));
        // unsupported algorithm
        assertNull(DigestAuthentication.createAuthorization("realm=\"r\", nonce=\"n\", algorithm=SHA-512-256", "GET",
                "/", "u", "p", "c"));
        // missing nonce
        assertNull(DigestAuthentication.createAuthorization("realm=\"r\"", "GET", "/", "u", "p", "c"));
    }

    @Test
    public void testWithoutQop() {
        String header = authorize("realm=\"r\", nonce=\"n\"", "GET", "/", "u", "p", "c");
        assertTrue(!header.contains("qop="));
        assertTrue(!header.contains("cnonce="));
    }

    @Test
    public void testRequestTargetContainsQuery() {
        String header = authorize("realm=\"r\", nonce=\"n\", qop=auth", "GET",
                URI.create("https://example.org/cal/a.ics?token=a%20b"), "u", "p");
        assertTrue(header.contains("uri=\"/cal/a.ics?token=a%20b\""));
    }

    @Test
    public void testFindChallenge() {
        assertEquals("realm=\"r\", nonce=\"n\"",
                DigestAuthentication.findChallenge(List.of("Basic realm=\"x\"", "Digest realm=\"r\", nonce=\"n\"")));
        assertEquals("realm=\"r2\", nonce=\"n\"",
                DigestAuthentication.findChallenge(List.of("Basic realm=\"x\", digest realm=\"r2\", nonce=\"n\"")));
        assertNull(DigestAuthentication.findChallenge(List.of("Basic realm=\"x\"")));
        assertNull(DigestAuthentication.findChallenge(List.of()));
    }

    @Test
    public void testIsSameOrigin() {
        URI configured = URI.create("https://example.org/cal.ics");
        assertTrue(DigestAuthentication.isSameOrigin(configured, URI.create("https://example.org/other/cal.ics")));
        assertTrue(DigestAuthentication.isSameOrigin(configured, URI.create("HTTPS://EXAMPLE.ORG:443/cal.ics")));
        assertTrue(DigestAuthentication.isSameOrigin(URI.create("http://example.org/a"),
                URI.create("http://example.org:80/b")));
        assertFalse(DigestAuthentication.isSameOrigin(configured, URI.create("http://example.org/cal.ics")));
        assertFalse(DigestAuthentication.isSameOrigin(configured, URI.create("https://evil.example.com/cal.ics")));
        assertFalse(DigestAuthentication.isSameOrigin(configured, URI.create("https://example.org:8443/cal.ics")));
        assertFalse(DigestAuthentication.isSameOrigin(configured, URI.create("/relative")));
    }
}
