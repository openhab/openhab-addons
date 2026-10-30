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
package org.openhab.binding.caldav.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.caldav.internal.client.CalDavUris;
import org.openhab.binding.caldav.internal.config.AccountConfiguration;
import org.openhab.binding.caldav.internal.config.CalDavConfiguration;
import org.openhab.binding.caldav.internal.config.CalendarConfiguration;
import org.openhab.core.thing.ThingUID;

/**
 * Calendar representation preserves DAV reference identity within each configured bridge.
 *
 * @author Andreas Vilippus - Initial contribution
 */
@NonNullByDefault
class CalendarIdentityTest {
    private static final ThingUID BRIDGE = new ThingUID("caldav:account:account1");
    private static final URI ACCOUNT = URI.create("https://server.example/dav/");

    /**
     * Checks that a calendar representation contains its bridge and collection reference independently of the account
     * path.
     */
    @Test
    void identityIsReadableAndIndependentOfAccountPath() {
        assertEquals("caldav:account:account1|/calendar/work/",
                CalendarIdentity.create(BRIDGE, ACCOUNT, URI.create("https://server.example/calendar/work/")));
    }

    /** Checks that equivalent manual path forms identify the same calendar within a bridge. */
    @Test
    void relativeAndAbsoluteConfigurationResolveToTheSameIdentity() {
        AccountConfiguration account = new AccountConfiguration();
        account.url = ACCOUNT.toString();
        for (String path : List.of("work/", "/dav/work/", "https://server.example/dav/work/")) {
            CalendarConfiguration config = new CalendarConfiguration();
            config.path = path;
            assertEquals("caldav:account:account1|/dav/work/",
                    CalendarIdentity.create(BRIDGE, ACCOUNT, CalDavConfiguration.validate(config, account)), path);
        }
    }

    /** Checks that canonical URI aliases produce one calendar representation. */
    @Test
    void equivalentCanonicalUrisHaveTheSameIdentity() {
        for (String uri : List.of("https://SERVER.example:443/calendar/%4a/", "HTTPS://server.example/calendar/%4A/",
                "https://server.example/calendar/J/", "https://server.example/calendar/../calendar/J/",
                "https://server.example/calendar/%2e%2e/calendar/J/")) {
            assertEquals("caldav:account:account1|/calendar/J/",
                    CalendarIdentity.create(BRIDGE, ACCOUNT, URI.create(uri)), uri);
        }
    }

    /** Checks that significant DAV path, query and escape differences remain separate representations. */
    @Test
    void meaningfulDavPathDistinctionsRemainDistinct() {
        var identities = new HashSet<String>();
        List<String> paths = List.of("/a%2Fb/", "/a/b/", "/a%2Fb", "/A%2Fb/", "/a%2Fb/?key=Value", "/a%2Fb/?key=value",
                "/a%2Fb/?", "/a//b/", "/a%2Fb/?x=1", "/a%2Fb/?x=2");
        for (String path : paths) {
            assertTrue(
                    identities
                            .add(CalendarIdentity.create(BRIDGE, ACCOUNT, URI.create("https://server.example" + path))),
                    path);
        }
    }

    /** Checks that accounts sharing a collection URL retain distinct calendar representations. */
    @Test
    void differentBridgesKeepTheSameCollectionSeparate() {
        URI collection = URI.create("https://server.example/calendar/work/");
        assertNotEquals(CalendarIdentity.create(BRIDGE, ACCOUNT, collection),
                CalendarIdentity.create(new ThingUID("caldav:account:account2"), ACCOUNT, collection));
    }

    /** Checks that a server origin change preserves the logical collection reference within the same bridge. */
    @Test
    void accountOriginChangesPreserveTheSameBridgeReference() {
        String original = CalendarIdentity.create(BRIDGE, ACCOUNT, URI.create("https://server.example/calendar/work/"));
        URI changedAccount = URI.create("https://other.example:8443/new-account/");
        assertEquals(original, CalendarIdentity.create(BRIDGE, changedAccount,
                URI.create("https://other.example:8443/calendar/work/")));
    }

    /** Checks that foreign origins, credentials and fragments cannot enter a calendar representation. */
    @Test
    void crossOriginOrUnsafeCollectionsAreRejected() {
        for (String uri : List.of("https://other.example/calendar/work/", "https://server.example:444/calendar/work/",
                "http://server.example/calendar/work/", "https://user:secret@server.example/calendar/work/",
                "https://server.example/calendar/work/#fragment")) {
            assertThrows(IllegalArgumentException.class,
                    () -> CalendarIdentity.create(BRIDGE, ACCOUNT, URI.create(uri)), uri);
        }
    }

    /** Checks that leading empty path segments survive origin-relative representation and reconstruction. */
    @Test
    void leadingEmptySegmentsKeepALosslessReference() {
        URI collection = URI.create("https://server.example//calendar/work/?");
        assertEquals("caldav:account:account1|/.//calendar/work/?",
                CalendarIdentity.create(BRIDGE, ACCOUNT, collection));
        assertEquals(collection, CalDavUris.reconstruct(ACCOUNT, "/.//calendar/work/?"));
    }
}
