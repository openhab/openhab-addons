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
package org.openhab.binding.ddwrt.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ClientNameResolver}.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
class ClientNameResolverTest {

    @Test
    void resolvesNameByNormalizedMac() {
        ClientNameResolver resolver = new ClientNameResolver();
        resolver.addIdentity("Kitchen Lamp", Map.of("macAddress", "AA:BB:CC:DD:EE:FF"));

        assertThat(resolver.resolve("aa-bb-cc-dd-ee-ff").orElseThrow(), is("Kitchen Lamp"));
    }

    @Test
    void prefersAliasPropertyToPresentationLabel() {
        ClientNameResolver resolver = new ClientNameResolver();
        resolver.addIdentity("Tapo HS200 Light-Switch", Map.of("mac", "D8-07-B6-AC-65-5A", "alias", "Jack's fan"));

        assertThat(resolver.resolve("d8:07:b6:ac:65:5a").orElseThrow(), is("Jack's fan"));
    }

    @Test
    void acceptsDuplicateCaseInsensitiveName() {
        ClientNameResolver resolver = new ClientNameResolver();
        resolver.addIdentity("Porch Light", Map.of("mac", "aa:bb:cc:dd:ee:ff"));
        resolver.addIdentity("porch light", Map.of("macAddress", "aa-bb-cc-dd-ee-ff"));

        assertThat(resolver.resolve("aabbccddeeff").orElseThrow().toLowerCase(), is("porch light"));
    }

    @Test
    void rejectsAmbiguousNamesForSameMac() {
        ClientNameResolver resolver = new ClientNameResolver();
        resolver.addIdentity("Current Device", Map.of("mac", "aa:bb:cc:dd:ee:ff"));
        resolver.addIdentity("Stale Device", Map.of("macAddress", "aabbccddeeff"));

        assertThat(resolver.resolve("aa:bb:cc:dd:ee:ff").isEmpty(), is(true));
    }

    @Test
    void ignoresGenericMacAndUncorrelatedLabels() {
        ClientNameResolver resolver = new ClientNameResolver();
        resolver.addIdentity("Unknown Device", Map.of("mac", "aa:bb:cc:dd:ee:ff"));
        resolver.addIdentity("AA-BB-CC-DD-EE-FF", Map.of("mac", "aa:bb:cc:dd:ee:ff"));
        resolver.addIdentity("Named but uncorrelated", Map.of("serialNumber", "1234"));

        assertThat(resolver.resolve("aa:bb:cc:dd:ee:ff").isEmpty(), is(true));
    }

    @Test
    void rejectsMalformedMac() {
        ClientNameResolver resolver = new ClientNameResolver();
        resolver.addIdentity("Kitchen Lamp", Map.of("mac", "not-a-mac"));

        assertThat(resolver.resolve("not-a-mac").isEmpty(), is(true));
    }
}
