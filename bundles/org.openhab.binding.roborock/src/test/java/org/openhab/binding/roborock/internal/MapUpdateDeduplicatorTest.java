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
package org.openhab.binding.roborock.internal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

@NonNullByDefault({})
class MapUpdateDeduplicatorTest {

    @Test
    void shouldPublishReturnsFalseForUnchangedMapImageBytes() {
        MapUpdateDeduplicator deduplicator = new MapUpdateDeduplicator();
        byte[] firstMap = new byte[] { 1, 2, 3, 4 };
        byte[] sameMapContent = new byte[] { 1, 2, 3, 4 };

        assertTrue(deduplicator.shouldPublish(firstMap));
        assertFalse(deduplicator.shouldPublish(sameMapContent));
    }

    @Test
    void shouldPublishReturnsTrueWhenMapImageBytesChange() {
        MapUpdateDeduplicator deduplicator = new MapUpdateDeduplicator();
        byte[] firstMap = new byte[] { 1, 2, 3, 4 };
        byte[] changedMap = new byte[] { 1, 2, 3, 5 };

        assertTrue(deduplicator.shouldPublish(firstMap));
        assertTrue(deduplicator.shouldPublish(changedMap));
    }

    @Test
    void resetAllowsRepublishingSameMapAfterUndefinedTransition() {
        MapUpdateDeduplicator deduplicator = new MapUpdateDeduplicator();
        byte[] mapImage = new byte[] { 9, 8, 7, 6 };

        assertTrue(deduplicator.shouldPublish(mapImage));
        assertFalse(deduplicator.shouldPublish(mapImage));

        deduplicator.reset();

        assertTrue(deduplicator.shouldPublish(mapImage));
    }
}
