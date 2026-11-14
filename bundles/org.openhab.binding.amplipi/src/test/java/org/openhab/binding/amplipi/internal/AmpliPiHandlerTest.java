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
package org.openhab.binding.amplipi.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.openhab.binding.amplipi.internal.model.Announcement;
import org.openhab.binding.amplipi.internal.model.Status;
import org.openhab.binding.amplipi.internal.model.Zone;
import org.openhab.core.audio.AudioHTTPServer;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Bridge;

import com.google.gson.Gson;

/**
 * Tests for the announcement payload creation of {@link AmpliPiHandler}.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class AmpliPiHandlerTest {

    @Test
    public void testAnnouncementWithoutVolumeOmitsForcedDb() {
        Announcement announcement = AmpliPiHandler.createAnnouncement("http://example.com/a.mp3", null, null);

        assertNull(announcement.getVol());
        assertFalse(new Gson().toJson(announcement).contains("\"vol\""));
    }

    @Test
    public void testAnnouncementWithVolumeSetsDb() {
        Announcement announcement = AmpliPiHandler.createAnnouncement("http://example.com/a.mp3", new PercentType(100),
                null);

        assertEquals(0, announcement.getVol());
    }

    @Test
    public void testAnnouncementZones() {
        Announcement announcement = AmpliPiHandler.createAnnouncement("http://example.com/a.mp3", null, List.of(2, 3));

        assertEquals(List.of(2, 3), announcement.getZones());
    }

    @Test
    public void testDisabledZoneIdsExtractedFromStatus() {
        Status status = new Status();
        Zone enabled = new Zone();
        enabled.setId(0);
        enabled.setDisabled(Boolean.FALSE);
        Zone disabled = new Zone();
        disabled.setId(1);
        disabled.setDisabled(Boolean.TRUE);
        Zone unknown = new Zone();
        unknown.setId(2);
        unknown.setDisabled(Boolean.FALSE);
        status.setZones(List.of(enabled, disabled, unknown));

        assertEquals(java.util.Set.of(1), AmpliPiHandler.extractDisabledZoneIds(status));
    }

    @Test
    public void testDisabledZoneIdsEmptyWithoutZones() {
        Status status = new Status();

        assertTrue(AmpliPiHandler.extractDisabledZoneIds(status).isEmpty());
    }

    @Test
    public void testZoneNotDisabledBeforeFirstPoll() {
        AmpliPiHandler handler = new AmpliPiHandler(mock(Bridge.class), mock(HttpClient.class),
                mock(AudioHTTPServer.class), null);

        assertFalse(handler.isZoneDisabled(0));
    }
}
