/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.sonos.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.sonos.internal.handler.SonosMediaInformation;

/**
 * Test for class SonosMediaInformation
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class SonosMediaInformationTest {

    private static final SonosMetaData METADATA_STREAM_CONTENT = new SonosMetaData("yyy", "yyy", "yyy", "Morning Live",
            "yyy", "yyy", "yyy", "yyy", "yyy", "yyy");
    private static final SonosMetaData METADATA_EMPTY_STREAM_CONTENT = new SonosMetaData("yyy", "yyy", "yyy", "", "yyy",
            "yyy", "yyy", "yyy", "yyy", "yyy");

    private static final SonosMetaData METADATA_RADIOAPP_1 = new SonosMetaData("yyy", "yyy", "yyy",
            "TYPE=SNG|TITLE Green Day - Time Of Your Life (Good Riddance)|ARTIST |ALBUM ", "yyy", "yyy", "yyy", "yyy",
            "yyy", "yyy");
    private static final SonosMetaData METADATA_RADIOAPP_2 = new SonosMetaData("yyy", "yyy", "yyy",
            "TYPE=SNG|TITLE Time Of Your Life (Good Riddance)|ARTIST Green Day|ALBUM Nimrod", "yyy", "yyy", "yyy",
            "yyy", "yyy", "yyy");
    private static final SonosMetaData METADATA_RADIOAPP_ADVERTISEMENT = new SonosMetaData("yyy", "yyy", "yyy",
            "TYPE=SNG|TITLE Advertisement_Stop|ARTIST |ALBUM ", "yyy", "yyy", "yyy", "yyy", "yyy", "yyy");

    private static final SonosMetaData METADATA_ARTIST_ALBUM_TITLE = new SonosMetaData("xxx", "xxx", "xxx", "xxx",
            "xxx", "Time Of Your Life (Good Riddance)", "xxx", "xxx", "Nimrod", "Green Day");
    private static final SonosMetaData METADATA_EMPTY_CREATOR_ARTIST = new SonosMetaData("xxx", "xxx", "xxx", "xxx",
            "xxx", "Time Of Your Life (Good Riddance)", "xxx", "", "Nimrod", "");
    private static final SonosMetaData METADATA_EMPTY_ALBUM = new SonosMetaData("xxx", "xxx", "xxx", "xxx", "xxx",
            "Time Of Your Life (Good Riddance)", "xxx", "Green Day", "", "");
    private static final SonosMetaData METADATA_EMPTY_TITLE = new SonosMetaData("xxx", "xxx", "xxx", "xxx", "xxx", "",
            "xxx", "xxx", "Nimrod", "Green Day");
    private static final SonosMetaData METADATA_ONLY_TITLE = new SonosMetaData("", "", "", "", "",
            "Time Of Your Life (Good Riddance)", "", "", "", "");
    private static final SonosMetaData METADATA_EMPTY = new SonosMetaData("", "", "", "", "", "", "", "", "", "");

    @Test
    public void parseTuneInMediaInfoWithStreamContent() {
        SonosMediaInformation result = SonosMediaInformation.parseTuneInMediaInfo(null, "Radio One",
                METADATA_STREAM_CONTENT);
        assertNull(result.getArtist());
        assertNull(result.getAlbum());
        assertEquals("Radio One", result.getTitle());
        assertEquals("Radio One - Morning Live", result.getCombinedInfo());
        assertEquals(true, result.needsUpdate());
    }

    @Test
    public void parseTuneInMediaInfoWithoutStreamContent() {
        SonosMediaInformation result = SonosMediaInformation.parseTuneInMediaInfo(null, "Radio One",
                METADATA_EMPTY_STREAM_CONTENT);
        assertNull(result.getArtist());
        assertNull(result.getAlbum());
        assertEquals("Radio One", result.getTitle());
        assertEquals("Radio One", result.getCombinedInfo());
        assertEquals(true, result.needsUpdate());
    }

    @Test
    public void parseTuneInMediaInfoWithoutTitle() {
        SonosMediaInformation result = SonosMediaInformation.parseTuneInMediaInfo(null, "", METADATA_STREAM_CONTENT);
        assertNull(result.getArtist());
        assertNull(result.getAlbum());
        assertNull(result.getTitle());
        assertNull(result.getCombinedInfo());
        assertEquals(false, result.needsUpdate());
    }

    @Test
    public void parseTuneInMediaInfoWithNullParams() {
        SonosMediaInformation result = SonosMediaInformation.parseTuneInMediaInfo(null, null, null);
        assertNull(result.getArtist());
        assertNull(result.getAlbum());
        assertNull(result.getTitle());
        assertNull(result.getCombinedInfo());
        assertEquals(false, result.needsUpdate());
    }

    @Test
    public void parseTuneInMediaInfoWithOPMLRequest() throws IOException {
        InputStream resourceStream = getClass().getResourceAsStream("/OPML.xml");
        assertNotNull(resourceStream);
        final String opmlResult = new String(resourceStream.readAllBytes(), StandardCharsets.UTF_8);
        SonosMediaInformation result = SonosMediaInformation.parseTuneInMediaInfo(opmlResult, "Radio One",
                METADATA_STREAM_CONTENT);
        assertNull(result.getArtist());
        assertNull(result.getAlbum());
        assertEquals("RTL2 105.9", result.getTitle());
        assertEquals("RTL2 105.9 - Le Son Pop-Rock - Paris, France", result.getCombinedInfo());
        assertEquals(true, result.needsUpdate());
    }

    @Test
    public void parseRadioAppMediaInfoWithSongTitle() {
        SonosMediaInformation result = SonosMediaInformation.parseRadioAppMediaInfo("Radio Two", METADATA_RADIOAPP_1);
        assertEquals("Green Day", result.getArtist());
        assertEquals("", result.getAlbum());
        assertEquals("Time Of Your Life (Good Riddance)", result.getTitle());
        assertEquals("Radio Two - Green Day - Time Of Your Life (Good Riddance)", result.getCombinedInfo());
        assertEquals(true, result.needsUpdate());
    }

    @Test
    public void parseRadioAppMediaInfoWithSongTitleArtistAlbum() {
        SonosMediaInformation result = SonosMediaInformation.parseRadioAppMediaInfo("Radio Two", METADATA_RADIOAPP_2);
        assertEquals("Green Day", result.getArtist());
        assertEquals("Nimrod", result.getAlbum());
        assertEquals("Time Of Your Life (Good Riddance)", result.getTitle());
        assertEquals("Radio Two - Green Day - Nimrod - Time Of Your Life (Good Riddance)", result.getCombinedInfo());
        assertEquals(true, result.needsUpdate());
    }

    @Test
    public void parseRadioAppMediaInfoWithdvertisement() {
        SonosMediaInformation result = SonosMediaInformation.parseRadioAppMediaInfo("Radio Two",
                METADATA_RADIOAPP_ADVERTISEMENT);
        assertEquals("", result.getArtist());
        assertEquals("", result.getAlbum());
        assertEquals("Radio Two", result.getTitle());
        assertEquals("Radio Two", result.getCombinedInfo());
        assertEquals(true, result.needsUpdate());
    }

    @Test
    public void parseRadioAppMediaInfoWithoutStreamContent() {
        SonosMediaInformation result = SonosMediaInformation.parseRadioAppMediaInfo("Radio Two",
                METADATA_EMPTY_STREAM_CONTENT);
        assertNull(result.getArtist());
        assertNull(result.getAlbum());
        assertEquals("Radio Two", result.getTitle());
        assertEquals("Radio Two", result.getCombinedInfo());
        assertEquals(true, result.needsUpdate());
    }

    @Test
    public void parseRadioAppMediaInfoWithoutTitle() {
        SonosMediaInformation result = SonosMediaInformation.parseRadioAppMediaInfo("", METADATA_RADIOAPP_1);
        assertNull(result.getArtist());
        assertNull(result.getAlbum());
        assertNull(result.getTitle());
        assertNull(result.getCombinedInfo());
        assertEquals(false, result.needsUpdate());
    }

    @Test
    public void parseRadioAppMediaInfoWithNullParams() {
        SonosMediaInformation result = SonosMediaInformation.parseRadioAppMediaInfo(null, null);
        assertNull(result.getArtist());
        assertNull(result.getAlbum());
        assertNull(result.getTitle());
        assertNull(result.getCombinedInfo());
        assertEquals(false, result.needsUpdate());
    }

    @Test
    public void parseTrack() {
        SonosMediaInformation result = SonosMediaInformation.parseTrack(METADATA_ARTIST_ALBUM_TITLE);
        assertEquals("Green Day", result.getArtist());
        assertEquals("Nimrod", result.getAlbum());
        assertEquals("Time Of Your Life (Good Riddance)", result.getTitle());
        assertEquals("Green Day - Nimrod - Time Of Your Life (Good Riddance)", result.getCombinedInfo());
        assertEquals(true, result.needsUpdate());
    }

    @Test
    public void parseTrackWithoutArtist() {
        SonosMediaInformation result = SonosMediaInformation.parseTrack(METADATA_EMPTY_CREATOR_ARTIST);
        assertEquals("", result.getArtist());
        assertEquals("Nimrod", result.getAlbum());
        assertEquals("Time Of Your Life (Good Riddance)", result.getTitle());
        assertEquals("Nimrod - Time Of Your Life (Good Riddance)", result.getCombinedInfo());
        assertEquals(true, result.needsUpdate());
    }

    @Test
    public void parseTrackWithoutAlbum() {
        SonosMediaInformation result = SonosMediaInformation.parseTrack(METADATA_EMPTY_ALBUM);
        assertEquals("Green Day", result.getArtist());
        assertEquals("", result.getAlbum());
        assertEquals("Time Of Your Life (Good Riddance)", result.getTitle());
        assertEquals("Green Day - Time Of Your Life (Good Riddance)", result.getCombinedInfo());
        assertEquals(true, result.needsUpdate());
    }

    @Test
    public void parseTrackWithoutTitle() {
        SonosMediaInformation result = SonosMediaInformation.parseTrack(METADATA_EMPTY_TITLE);
        assertEquals("Green Day", result.getArtist());
        assertEquals("Nimrod", result.getAlbum());
        assertEquals("", result.getTitle());
        assertEquals("Green Day - Nimrod", result.getCombinedInfo());
        assertEquals(true, result.needsUpdate());
    }

    @Test
    public void parseTrackWithOnlyTitle() {
        SonosMediaInformation result = SonosMediaInformation.parseTrack(METADATA_ONLY_TITLE);
        assertEquals("", result.getArtist());
        assertEquals("", result.getAlbum());
        assertEquals("Time Of Your Life (Good Riddance)", result.getTitle());
        assertEquals("Time Of Your Life (Good Riddance)", result.getCombinedInfo());
        assertEquals(true, result.needsUpdate());
    }

    @Test
    public void parseTrackWithEmptyMetaData() {
        SonosMediaInformation result = SonosMediaInformation.parseTrack(METADATA_EMPTY);
        assertEquals("", result.getArtist());
        assertEquals("", result.getAlbum());
        assertEquals("", result.getTitle());
        assertEquals("", result.getCombinedInfo());
        assertEquals(true, result.needsUpdate());
    }

    @Test
    public void parseTrackWithNullParam() {
        SonosMediaInformation result = SonosMediaInformation.parseTrack(null);
        assertNull(result.getArtist());
        assertNull(result.getAlbum());
        assertNull(result.getTitle());
        assertNull(result.getCombinedInfo());
        assertEquals(false, result.needsUpdate());
    }

    @Test
    public void parseTrackTitle() {
        SonosMediaInformation result = SonosMediaInformation.parseTrackTitle(METADATA_ARTIST_ALBUM_TITLE);
        assertNull(result.getArtist());
        assertNull(result.getAlbum());
        assertEquals("Time Of Your Life (Good Riddance)", result.getTitle());
        assertEquals("Time Of Your Life (Good Riddance)", result.getCombinedInfo());
        assertEquals(true, result.needsUpdate());
    }

    @Test
    public void parseTrackTitleWithNullParam() {
        SonosMediaInformation result = SonosMediaInformation.parseTrackTitle(null);
        assertNull(result.getArtist());
        assertNull(result.getAlbum());
        assertNull(result.getTitle());
        assertNull(result.getCombinedInfo());
        assertEquals(false, result.needsUpdate());
    }
}
