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
package org.openhab.binding.caldav.internal.client;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * URL and XML trust-boundary regressions.
 * 
 * @author Andreas Vilippus - Initial contribution
 * @author Andreas Vilippus - RFC 3986 URI resolution regressions
 */
@NonNullByDefault
class CalDavUrisTest {
    @Test
    void resolvesRelativeReferencesWithoutLosingEscaping() {
        assertEquals("https://example.org/a%20b.ics",
                CalDavUris.resolve(URI.create("https://example.org/calendar/"), "../a%20b.ics").toString());
    }

    @Test
    void validationPreservesEmptySegmentsAndEscapedSeparators() {
        for (String value : new String[] { "https://example.org/a//b/", "https://example.org/a///b/",
                "https://example.org//a/", "https://example.org/a%2fb/%2E%2E/",
                "https://example.org/a//b/?next=/./x" }) {
            assertEquals(value, CalDavUris.validate(URI.create(value)).toString(), value);
        }
        assertNotEquals(CalDavUris.validate(URI.create("https://example.org/a//b/")),
                CalDavUris.validate(URI.create("https://example.org/a/b/")));
    }

    @Test
    void removesOnlyCompleteDotSegmentsWithoutCollapsingEmptySegments() {
        Map.of("/a//./b/", "/a//b/", "/a//../b/", "/a/b/", "/../b/", "/b/", "/a/../../b/", "/b/", "/a/.hidden/../b..",
                "/a/b..")
                .forEach((path, expected) -> assertEquals("https://example.org" + expected,
                        CalDavUris.validate(URI.create("https://example.org" + path)).toString(), path));
    }

    @Test
    void relativeReferencesRetainEmptyPathSegments() {
        URI base = URI.create("https://example.org/base//collection/?old=value");
        Map.of("item//calendar/", "/base//collection/item//calendar/", "../calendar//", "/base//calendar//",
                "./item//calendar/", "/base//collection/item//calendar/", "/root//calendar/", "/root//calendar/",
                "../../../root//calendar/", "/root//calendar/")
                .forEach((reference, expected) -> assertEquals("https://example.org" + expected,
                        CalDavUris.resolve(base, reference).toString(), reference));
        assertEquals("https://example.org/root//calendar/",
                CalDavUris.resolve(base, "https://example.org/root//calendar/").toString());
    }

    @Test
    void emptyAndQueryOnlyReferencesRetainTheCompleteBasePath() {
        URI base = URI.create("https://example.org/base//calendar;p?old=value");
        assertEquals(base, CalDavUris.resolve(base, ""));
        assertEquals("https://example.org/base//calendar;p?new=value",
                CalDavUris.resolve(base, "?new=value").toString());
        assertEquals("https://example.org/base//calendar;p?", CalDavUris.resolve(base, "?").toString());
        assertEquals("https://example.org/calendar//",
                CalDavUris.resolve(URI.create("https://example.org"), "calendar//").toString());
    }

    @Test
    void resolvesRfc3986NormalAndAbnormalPathExamples() {
        URI base = URI.create("https://example.org/b/c/d;p?q");
        // RFC 3986 section 5.4 examples use this hierarchy; foreign schemes/authorities are rejected by policy.
        Map.ofEntries(Map.entry("g", "/b/c/g"), Map.entry("./g", "/b/c/g"), Map.entry("g/", "/b/c/g/"),
                Map.entry("/g", "/g"), Map.entry("?y", "/b/c/d;p?y"), Map.entry("g?y", "/b/c/g?y"),
                Map.entry(";x", "/b/c/;x"), Map.entry("g;x", "/b/c/g;x"), Map.entry("", "/b/c/d;p?q"),
                Map.entry(".", "/b/c/"), Map.entry("./", "/b/c/"), Map.entry("..", "/b/"), Map.entry("../", "/b/"),
                Map.entry("../g", "/b/g"), Map.entry("../..", "/"), Map.entry("../../", "/"),
                Map.entry("../../g", "/g"), Map.entry("../../../g", "/g"), Map.entry("../../../../g", "/g"),
                Map.entry("/./g", "/g"), Map.entry("/../g", "/g"), Map.entry("g.", "/b/c/g."),
                Map.entry(".g", "/b/c/.g"), Map.entry("g..", "/b/c/g.."), Map.entry("..g", "/b/c/..g"),
                Map.entry("./../g", "/b/g"), Map.entry("./g/.", "/b/c/g/"), Map.entry("g/./h", "/b/c/g/h"),
                Map.entry("g/../h", "/b/c/h"), Map.entry("g;x=1/./y", "/b/c/g;x=1/y"),
                Map.entry("g;x=1/../y", "/b/c/y"), Map.entry("g?y/./x", "/b/c/g?y/./x"),
                Map.entry("g?y/../x", "/b/c/g?y/../x"))
                .forEach((reference, expected) -> assertEquals("https://example.org" + expected,
                        CalDavUris.resolve(base, reference).toString(), reference));
    }

    @Test
    void sameOriginNetworkReferencesPreserveIpv6AuthorityAndDefaultPort() {
        assertEquals("https://EXAMPLE.org:443/a//b/",
                CalDavUris.resolve(URI.create("https://example.org/"), "//EXAMPLE.org:443/a//b/").toString());
        assertEquals("https://[2001:db8::1]:443/a//b/",
                CalDavUris.resolve(URI.create("https://[2001:db8::1]/"), "//[2001:db8::1]:443/a//b/").toString());
    }

    @Test
    void rejectsCredentialLeaksAndDowngrades() {
        URI base = URI.create("https://example.org/");
        for (String reference : new String[] { "https://evil.example/calendar/", "http://example.org/",
                "https://example.org:444/", "https://user:secret@example.org/", "file:///tmp/calendar",
                "https://example.org/#secret", "//evil.example/calendar//", "//user:secret@example.org/",
                "?query=value#secret", "https:g" }) {
            assertThrows(IllegalArgumentException.class, () -> CalDavUris.resolve(base, reference));
        }
    }

    @Test
    void xmlDepthIsBounded() {
        assertThrows(Exception.class, () -> CalDavXml.parse("<x>".repeat(100) + "</x>".repeat(100)));
    }
}
