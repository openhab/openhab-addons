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
 * URI resolution, canonical identity, compact references and XML trust-boundary regressions.
 * 
 * @author Andreas Vilippus - Initial contribution
 * @author Andreas Vilippus - RFC 3986 URI resolution regressions
 * @author Andreas Vilippus - Canonical identity and compact reference regressions
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
    void canonicalizesEquivalentAbsoluteIdentities() {
        URI expected = URI.create("https://example.org/cal/family/~event.ics?name=A%2FB");
        for (String value : new String[] { "HTTPS://EXAMPLE.ORG:443/cal/./family/%7eevent.ics?name=%41%2fB",
                "https://example.org/cal/other/../family/~event.ics?name=A%2FB", expected.toString() }) {
            assertEquals(expected, CalDavUris.canonicalize(URI.create(value)), value);
        }
        assertEquals(URI.create("https://example.org/"),
                CalDavUris.canonicalize(URI.create("https://EXAMPLE.org:443")));
        assertEquals(URI.create("https://example.org/"), CalDavUris.canonicalize(URI.create("https://example.org:/")));
    }

    @Test
    void canonicalizationPreservesDistinctPathAndQueryIdentities() {
        URI base = CalDavUris.canonicalize(URI.create("https://example.org/cal/a/"));
        for (String value : new String[] { "/cal/A/", "/cal/a", "/cal//a/", "/cal%2Fa/", "/cal/a/?", "/cal/a/?name=A",
                "/cal/a/?name=a" }) {
            assertNotEquals(base, CalDavUris.canonicalize(URI.create("https://example.org" + value)), value);
        }
        assertEquals("https://example.org/cal//A%2Fb/?x=/%2F//./",
                CalDavUris.canonicalize(URI.create("https://example.org/cal//A%2fb/?x=/%2f//./")).toString());
        assertNotEquals(CalDavUris.canonicalize(URI.create("https://example.org/cal/a/?x=%26")),
                CalDavUris.canonicalize(URI.create("https://example.org/cal/a/?x=&")));
    }

    @Test
    void canonicalizationRemovesEncodedDotSegmentsAndIsIdempotent() {
        Map.of("/cal/%2e/a/", "/cal/a/", "/cal/a/%2E%2e/b/", "/cal/b/", "/cal/%2E/../b/", "/b/", "/cal//%2e/a/",
                "/cal//a/", "/cal/%252E/a/", "/cal/%252E/a/").forEach((path, expected) -> {
                    URI canonical = CalDavUris.canonicalize(URI.create("https://example.org" + path));
                    assertEquals("https://example.org" + expected, canonical.toString(), path);
                    assertEquals(canonical, CalDavUris.canonicalize(canonical), path);
                });
    }

    @Test
    void sameOriginUsesSchemeHostAndEffectivePortOnly() {
        URI base = URI.create("https://example.org/caldav/");
        assertTrue(CalDavUris.sameOrigin(base, URI.create("HTTPS://EXAMPLE.ORG:443/calendars/user/9/")));
        assertFalse(CalDavUris.sameOrigin(base, URI.create("https://other.example.org/caldav/")));
        assertFalse(CalDavUris.sameOrigin(base, URI.create("https://example.org:444/caldav/")));
        assertFalse(CalDavUris.sameOrigin(URI.create("https://localhost/"), URI.create("http://localhost/")));
        assertTrue(CalDavUris.sameOrigin(URI.create("http://localhost/"), URI.create("http://LOCALHOST:80/cal/")));
        assertTrue(CalDavUris.sameOrigin(URI.create("https://[2001:db8::1]/"),
                URI.create("HTTPS://[2001:DB8::1]:443/cal/")));
    }

    @Test
    void collectionReferenceIsIndependentOfAccountPath() {
        URI account = URI.create("https://server.example/caldav/");
        URI collection = URI.create("HTTPS://SERVER.EXAMPLE:443/calendars/users/9/family/");
        String reference = CalDavUris.originRelative(account, collection);
        assertEquals("/calendars/users/9/family/", reference);
        assertEquals(CalDavUris.canonicalize(collection), CalDavUris.reconstruct(account, reference));
        assertEquals(reference, CalDavUris.compact(account, collection));
    }

    @Test
    void compactsResourceReferencesInsideCollection() {
        URI collection = URI.create("https://example.org/cal/family/");
        for (String reference : new String[] { "event123.ics", "sub/event123.ics", "sub//event123.ics", "a%2Fb.ics",
                "A%20B.ics?x=%2F", "event123.ics?" }) {
            URI resource = URI.create(collection + reference);
            assertEquals(reference, CalDavUris.compact(collection, resource), reference);
            assertEquals(resource, CalDavUris.reconstruct(collection, reference), reference);
        }
    }

    @Test
    void compactionComparesCanonicalSegments() {
        URI parent = URI.create("HTTPS://EXAMPLE.ORG:443/cal/%66amily/");
        URI resource = URI.create("https://example.org/cal/family/%7Eevent.ics");
        assertEquals("~event.ics", CalDavUris.compact(parent, resource));
        assertEquals(CalDavUris.canonicalize(resource), CalDavUris.reconstruct(parent, "~event.ics"));
    }

    @Test
    void outsideResourcesAndFalsePrefixesUseOriginFallback() {
        URI collection = URI.create("https://example.org/cal/a/");
        for (String path : new String[] { "/cal/abc/event.ics", "/cal/A/event.ics", "/cal/a", "/cal/a%2Fb/event.ics",
                "/cal/other/event.ics", "/other/event.ics?x=1", "/cal/a/../other/event.ics",
                "/cal/a/%2e%2e/other/event.ics" }) {
            URI resource = URI.create("https://example.org" + path);
            URI canonical = CalDavUris.canonicalize(resource);
            String reference = CalDavUris.compact(collection, resource);
            assertEquals(
                    canonical.getRawPath() + (canonical.getRawQuery() == null ? "" : "?" + canonical.getRawQuery()),
                    reference, path);
            assertEquals(canonical, CalDavUris.reconstruct(collection, reference), path);
        }
    }

    @Test
    void parentWithoutTrailingSlashDoesNotBecomeADirectory() {
        URI parent = URI.create("https://example.org/cal/a");
        URI target = URI.create("https://example.org/cal/a/event.ics");
        assertEquals("/cal/a/event.ics", CalDavUris.compact(parent, target));
        assertEquals(target, CalDavUris.reconstruct(parent, CalDavUris.compact(parent, target)));
    }

    @Test
    void relativeNamesWithColonAreProtectedFromSchemeParsing() {
        URI collection = URI.create("https://example.org/cal/a/");
        URI target = URI.create("https://example.org/cal/a/name:123.ics");
        assertEquals("./name:123.ics", CalDavUris.compact(collection, target));
        assertEquals(target, CalDavUris.reconstruct(collection, "./name:123.ics"));
        assertThrows(IllegalArgumentException.class, () -> CalDavUris.reconstruct(collection, "name:123.ics"));
        assertEquals("sub/name:123.ics",
                CalDavUris.compact(collection, URI.create("https://example.org/cal/a/sub/name:123.ics")));
    }

    @Test
    void leadingEmptySegmentsCannotBecomeAuthoritiesOrAbsolutePaths() {
        URI parent = URI.create("https://example.org/cal/a/");
        URI child = URI.create("https://example.org/cal/a//event.ics");
        assertEquals(".//event.ics", CalDavUris.compact(parent, child));
        assertEquals(child, CalDavUris.reconstruct(parent, ".//event.ics"));
        for (String path : new String[] { "//cal/a/event.ics", "///cal/a/", "//", "///" }) {
            URI target = URI.create("https://example.org" + path);
            String reference = CalDavUris.originRelative(parent, target);
            assertEquals("/." + path, reference);
            assertEquals(target, CalDavUris.reconstruct(parent, reference));
            assertEquals(reference, CalDavUris.compact(parent, target));
        }
        assertEquals(".//event.ics",
                CalDavUris.compact(URI.create("https://example.org"), URI.create("https://example.org//event.ics")));
    }

    @Test
    void queryPresenceSurvivesCompactionWithoutInheritingParentQuery() {
        URI parent = URI.create("https://example.org/cal/a/?old=value");
        Map.of("/cal/a/", "./", "/cal/a/?", "./?", "/cal/a/?new=value", "./?new=value", "/cal/a/event.ics", "event.ics",
                "/other/event.ics?x=/./%2f", "/other/event.ics?x=/./%2F").forEach((path, expected) -> {
                    URI target = URI.create("https://example.org" + path);
                    assertEquals(expected, CalDavUris.compact(parent, target), path);
                    assertEquals(CalDavUris.canonicalize(target), CalDavUris.reconstruct(parent, expected), path);
                });
        assertEquals(CalDavUris.canonicalize(parent), CalDavUris.reconstruct(parent, ""));
        assertEquals(URI.create("https://example.org/cal/a/?new=value"), CalDavUris.reconstruct(parent, "?new=value"));
    }

    @Test
    void rawParentWhoseCanonicalHierarchyDiffersUsesSafeFallback() {
        URI parent = URI.create("https://example.org/cal/%2E%2E");
        URI target = URI.create("https://example.org/event.ics");
        assertEquals("/event.ics", CalDavUris.compact(parent, target));
        assertEquals(target, CalDavUris.reconstruct(parent, CalDavUris.compact(parent, target)));
    }

    @Test
    void reconstructsAbsoluteAndSameOriginNetworkReferences() {
        URI parent = URI.create("https://example.org/cal/a/");
        URI expected = URI.create("https://example.org/cal/a/~event.ics");
        for (String reference : new String[] { "HTTPS://EXAMPLE.ORG:443/cal/a/%7Eevent.ics",
                "//EXAMPLE.ORG:443/cal/a/%7eevent.ics", "/cal/a/~event.ics", "~event.ics" }) {
            assertEquals(expected, CalDavUris.reconstruct(parent, reference), reference);
        }
    }

    @Test
    void canonicalReconstructionNormalizesMixedDotSegmentsBeforeRemovingThem() {
        URI parent = URI.create("https://example.org/cal/a/");
        URI target = URI.create("https://example.org/event.ics");
        for (String reference : new String[] { "https://example.org/cal/%2E/../event.ics",
                "//example.org/cal/%2E/../event.ics", "/cal/%2E/../event.ics", "/cal/./../event.ics" }) {
            assertEquals(target, CalDavUris.reconstruct(parent, reference), reference);
        }
        assertEquals(URI.create("https://example.org/cal/event.ics"),
                CalDavUris.reconstruct(parent, "%2E/../event.ics"));
        // Raw RFC resolution differs here; ambiguous references are not safe compact outputs.
        String ambiguous = "https://example.org/cal/%2E/../event.ics";
        assertEquals(URI.create("https://example.org/cal/event.ics"), CalDavUris.resolve(parent, ambiguous));
        assertFalse(CalDavUris.isRoundTrip(parent, target, ambiguous));
        String compact = CalDavUris.compact(parent, URI.create(ambiguous));
        assertEquals("/event.ics", compact);
        assertTrue(CalDavUris.isRoundTrip(parent, target, compact));
    }

    @Test
    void canonicalReconstructionUsesTheCanonicalParentHierarchy() {
        URI parent = URI.create("https://example.org/cal/%2E%2E?old=value");
        assertEquals(URI.create("https://example.org/event.ics"), CalDavUris.reconstruct(parent, "event.ics"));
        assertEquals(URI.create("https://example.org/?new=value"), CalDavUris.reconstruct(parent, "?new=value"));
        assertEquals(URI.create("https://example.org/?old=value"), CalDavUris.reconstruct(parent, ""));
        URI target = URI.create("https://example.org/event.ics");
        assertFalse(CalDavUris.isRoundTrip(parent, target, "event.ics"));
        assertTrue(CalDavUris.isRoundTrip(parent, target, CalDavUris.compact(parent, target)));
    }

    @Test
    void referenceOperationsRetainIpv6AndNonDefaultPorts() {
        for (String origin : new String[] { "https://[2001:db8::1]:443", "https://[2001:db8::1]:8443",
                "https://example.org:8443", "http://[::1]:8080", "http://localhost:80" }) {
            URI parent = URI.create(origin + "/cal/");
            URI target = URI.create(origin + "/cal/event.ics");
            assertEquals("event.ics", CalDavUris.compact(parent, target), origin);
            assertEquals("/cal/event.ics", CalDavUris.originRelative(parent, target), origin);
            assertEquals(CalDavUris.canonicalize(target), CalDavUris.reconstruct(parent, "event.ics"), origin);
        }
    }

    @Test
    void compactReferencesRejectUnsafeCalendarAndResourceTargets() {
        URI parent = URI.create("https://localhost/cal/");
        for (String value : new String[] { "https://evil.example/cal/", "https://evil.example/cal/event.ics",
                "http://localhost/cal/", "https://localhost:444/cal/", "https://user:secret@localhost/cal/",
                "https://localhost/cal/#fragment", "file:///tmp/event.ics", "https:cal" }) {
            URI target = URI.create(value);
            assertThrows(IllegalArgumentException.class, () -> CalDavUris.compact(parent, target), value);
            assertThrows(IllegalArgumentException.class, () -> CalDavUris.originRelative(parent, target), value);
            assertThrows(IllegalArgumentException.class, () -> CalDavUris.reconstruct(parent, value), value);
            assertFalse(CalDavUris.isRoundTrip(parent, target, value), value);
        }
    }

    @Test
    void invalidReferencesCannotPassRoundTripVerification() {
        URI parent = URI.create("https://example.org/cal/");
        URI target = URI.create("https://example.org/cal/event.ics");
        for (String reference : new String[] { "event%.ics", "event%2.ics", "event%GG.ics", "//evil.example/event.ics",
                "//user:secret@example.org/event.ics", "event.ics#fragment", "../event.ics", "other.ics", "event.ics?",
                "event%2F.ics", "EVENT.ics" }) {
            assertFalse(CalDavUris.isRoundTrip(parent, target, reference), reference);
        }
        for (String reference : new String[] { "event%.ics", "event%2.ics", "event%GG.ics" }) {
            assertThrows(IllegalArgumentException.class, () -> CalDavUris.reconstruct(parent, reference), reference);
        }
        assertTrue(CalDavUris.isRoundTrip(parent, target, "event.ics"));
        assertTrue(CalDavUris.isRoundTrip(parent, target, "/cal/./event.ics"));
    }

    @Test
    void referenceRoundTripsAcrossPathAndQueryBoundaries() {
        for (String parentPath : new String[] { "", "/", "//", "/cal/a/", "/cal/a", "/cal//a/", "/cal/%61/",
                "/cal/%2E%2E/a/", "/cal/%2E%2E" }) {
            URI parent = URI.create("HTTPS://EXAMPLE.ORG:443" + parentPath + "?old=value");
            for (String targetPath : new String[] { "/", "//", "/cal/a/", "/cal/a", "/cal/a//event.ics",
                    "/cal/a/name:123.ics", "/cal/a/%2F.ics", "/cal/a/%252F.ics", "/cal/a/%7E.ics",
                    "/cal/a/%2E%2E/event.ics", "/cal/abc/event.ics", "/cal//a/event.ics", "/CAL/a/event.ics" }) {
                for (String query : new String[] { "", "?", "?name=A%2fb&next=/../", "?name=%7E" }) {
                    URI target = URI.create("https://example.org" + targetPath + query);
                    URI canonical = CalDavUris.canonicalize(target);
                    String compact = CalDavUris.compact(parent, target);
                    String originRelative = CalDavUris.originRelative(parent, target);
                    String context = parent + " -> " + target;
                    assertEquals(canonical, CalDavUris.reconstruct(parent, compact), context);
                    assertEquals(canonical, CalDavUris.reconstruct(parent, originRelative), context);
                    assertTrue(CalDavUris.isRoundTrip(parent, target, compact), context);
                    assertEquals(canonical, CalDavUris.canonicalize(canonical), context);
                    assertEquals(compact, CalDavUris.compact(parent, canonical), context);
                }
            }
        }
    }

    @Test
    void xmlDepthIsBounded() {
        assertThrows(Exception.class, () -> CalDavXml.parse("<x>".repeat(100) + "</x>".repeat(100)));
    }
}
