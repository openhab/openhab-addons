/**
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
package org.openhab.binding.squeezebox.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SqueezeBoxNotificationListener}.
 *
 * @author Stefan Triller - Initial contribution
 */
@NonNullByDefault
class SqueezeBoxNotificationListenerTest {

    private static final String MAC = "aa:bb:cc:dd:ee:ff";

    @Test
    void preAddEventIsNotAcknowledgedByBaselineMode() {
        SqueezeBoxNotificationListener listener = new SqueezeBoxNotificationListener(MAC, true);

        // A stale in-flight event with the pre-add count must only establish the
        // baseline, not be treated as the playlist change.
        listener.numberPlaylistTracksEvent(MAC, 2);

        assertTrue(listener.isBaselineEstablished());
        assertFalse(listener.isPlaylistUpdated());

        // The post-add count differs from the baseline and is acknowledged.
        listener.numberPlaylistTracksEvent(MAC, 3);

        assertTrue(listener.isPlaylistUpdated());
        assertEquals(3, listener.getNewTrackCount());
        // The notification is at index newCount - 1, i.e. the added track, not N - 1.
        assertEquals(2, listener.getNewTrackCount() - 1);
    }

    @Test
    void baselineIsEstablishedByFirstEvent() {
        SqueezeBoxNotificationListener listener = new SqueezeBoxNotificationListener(MAC, true);

        assertFalse(listener.isBaselineEstablished());

        listener.numberPlaylistTracksEvent(MAC, 5);

        assertTrue(listener.isBaselineEstablished());
        assertFalse(listener.isPlaylistUpdated());
    }

    @Test
    void deletionIsAlsoAcknowledged() {
        SqueezeBoxNotificationListener listener = new SqueezeBoxNotificationListener(MAC, true);

        listener.numberPlaylistTracksEvent(MAC, 4);
        listener.numberPlaylistTracksEvent(MAC, 3);

        assertTrue(listener.isPlaylistUpdated());
        assertEquals(3, listener.getNewTrackCount());
    }

    @Test
    void unchangedCountIsNotAcknowledged() {
        SqueezeBoxNotificationListener listener = new SqueezeBoxNotificationListener(MAC, true);

        listener.numberPlaylistTracksEvent(MAC, 4);
        listener.numberPlaylistTracksEvent(MAC, 4);

        assertFalse(listener.isPlaylistUpdated());
    }

    @Test
    void legacyModeAcknowledgesAnyEvent() {
        SqueezeBoxNotificationListener listener = new SqueezeBoxNotificationListener(MAC);

        assertFalse(listener.isBaselineEstablished());

        listener.numberPlaylistTracksEvent(MAC, 2);

        assertTrue(listener.isPlaylistUpdated());
        assertEquals(2, listener.getNewTrackCount());
    }

    @Test
    void eventsForOtherPlayersAreIgnored() {
        SqueezeBoxNotificationListener listener = new SqueezeBoxNotificationListener(MAC, true);

        listener.numberPlaylistTracksEvent("00:00:00:00:00:00", 2);

        assertFalse(listener.isBaselineEstablished());
        assertFalse(listener.isPlaylistUpdated());
    }
}
