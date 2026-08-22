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
package org.openhab.binding.ocpp.internal.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests that a charge point id is reduced to a valid ThingUID segment without letting two distinct ids
 * collide onto one Thing.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class OcppDiscoveryServiceTest {

    @Test
    void aCleanIdIsUsedAsTheSegmentUnchanged() {
        assertEquals("CP001", OcppDiscoveryService.sanitize("CP001"));
        assertEquals("charger_3-A", OcppDiscoveryService.sanitize("charger_3-A"));
    }

    @Test
    void twoIdsThatDifferOnlyByAnUnsupportedCharacterDoNotCollide() {
        // "a/b" and "a_b" both become "a_b" under a plain character-to-underscore replacement, giving
        // one ThingUID for two chargers; the reversible encoding keeps them distinct.
        assertNotEquals(OcppDiscoveryService.sanitize("a/b"), OcppDiscoveryService.sanitize("a_b"));
    }

    @Test
    void anEncodedIdIsAValidSegmentAndDecodesBackToTheOriginal() {
        String id = "CP/x:1 ÿ";
        String segment = OcppDiscoveryService.sanitize(id);
        assertTrue(segment.matches("[A-Za-z0-9_-]+"), "must be a valid ThingUID segment");
        String decoded = new String(Base64.getUrlDecoder().decode(segment.substring("b64-".length())),
                StandardCharsets.UTF_8);
        assertEquals(id, decoded);
    }
}
