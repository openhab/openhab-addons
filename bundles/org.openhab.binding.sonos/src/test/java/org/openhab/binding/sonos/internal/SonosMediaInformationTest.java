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

    @Test
    public void parseTuneInMediaInfoWithStreamContent() {
        SonosMetaData trackMetaData = new SonosMetaData("yyy", "yyy", "yyy", "Mroning Live", "yyy", "yyy", "yyy", "yyy",
                "yyy", "yyy");
        SonosMediaInformation result = SonosMediaInformation.parseTuneInMediaInfo(null, "Radio One", trackMetaData);
        assertNull(result.getArtist());
        assertNull(result.getAlbum());
        assertEquals("Radio One", result.getTitle());
        assertEquals("Radio One - Mroning Live", result.getCombinedInfo());
        assertEquals(true, result.needsUpdate());
    }

    @Test
    public void parseTuneInMediaInfoWithoutStreamContent() {
        SonosMetaData trackMetaData = new SonosMetaData("yyy", "yyy", "yyy", "", "yyy", "yyy", "yyy", "yyy", "yyy",
                "yyy");
        SonosMediaInformation result = SonosMediaInformation.parseTuneInMediaInfo(null, "Radio One", trackMetaData);
        assertNull(result.getArtist());
        assertNull(result.getAlbum());
        assertEquals("Radio One", result.getTitle());
        assertEquals("Radio One", result.getCombinedInfo());
        assertEquals(true, result.needsUpdate());
    }

    @Test
    public void parseTuneInMediaInfoWithoutTitle() {
        SonosMetaData trackMetaData = new SonosMetaData("yyy", "yyy", "yyy", "Mroning Live", "yyy", "yyy", "yyy", "yyy",
                "yyy", "yyy");
        SonosMediaInformation result = SonosMediaInformation.parseTuneInMediaInfo(null, "", trackMetaData);
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
    public void parseRadioAppMediaInfoWithSongTitle() {
        SonosMetaData trackMetaData = new SonosMetaData("yyy", "yyy", "yyy",
                "TYPE=SNG|TITLE Green Day - Time Of Your Life (Good Riddance)|ARTIST |ALBUM ", "yyy", "yyy", "yyy",
                "yyy", "yyy", "yyy");
        SonosMediaInformation result = SonosMediaInformation.parseRadioAppMediaInfo("Radio Two", trackMetaData);
        assertEquals("Green Day", result.getArtist());
        assertEquals("", result.getAlbum());
        assertEquals("Time Of Your Life (Good Riddance)", result.getTitle());
        assertEquals("Radio Two - Green Day - Time Of Your Life (Good Riddance)", result.getCombinedInfo());
        assertEquals(true, result.needsUpdate());
    }

    @Test
    public void parseRadioAppMediaInfoWithSongTitleArtistAlbum() {
        SonosMetaData trackMetaData = new SonosMetaData("yyy", "yyy", "yyy",
                "TYPE=SNG|TITLE Time Of Your Life (Good Riddance)|ARTIST Green Day|ALBUM Nimrod", "yyy", "yyy", "yyy",
                "yyy", "yyy", "yyy");
        SonosMediaInformation result = SonosMediaInformation.parseRadioAppMediaInfo("Radio Two", trackMetaData);
        assertEquals("Green Day", result.getArtist());
        assertEquals("Nimrod", result.getAlbum());
        assertEquals("Time Of Your Life (Good Riddance)", result.getTitle());
        assertEquals("Radio Two - Green Day - Nimrod - Time Of Your Life (Good Riddance)", result.getCombinedInfo());
        assertEquals(true, result.needsUpdate());
    }

    @Test
    public void parseRadioAppMediaInfoWithdvertisement() {
        SonosMetaData trackMetaData = new SonosMetaData("yyy", "yyy", "yyy",
                "TYPE=SNG|TITLE Advertisement_Stop|ARTIST |ALBUM ", "yyy", "yyy", "yyy", "yyy", "yyy", "yyy");
        SonosMediaInformation result = SonosMediaInformation.parseRadioAppMediaInfo("Radio Two", trackMetaData);
        assertEquals("", result.getArtist());
        assertEquals("", result.getAlbum());
        assertEquals("Radio Two", result.getTitle());
        assertEquals("Radio Two", result.getCombinedInfo());
        assertEquals(true, result.needsUpdate());
    }

    @Test
    public void parseRadioAppMediaInfoWithoutStreamContent() {
        SonosMetaData trackMetaData = new SonosMetaData("yyy", "yyy", "yyy", "", "yyy", "yyy", "yyy", "yyy", "yyy",
                "yyy");
        SonosMediaInformation result = SonosMediaInformation.parseRadioAppMediaInfo("Radio Two", trackMetaData);
        assertNull(result.getArtist());
        assertNull(result.getAlbum());
        assertEquals("Radio Two", result.getTitle());
        assertEquals("Radio Two", result.getCombinedInfo());
        assertEquals(true, result.needsUpdate());
    }

    @Test
    public void parseRadioAppMediaInfoWithoutTitle() {
        SonosMetaData trackMetaData = new SonosMetaData("yyy", "yyy", "yyy",
                "TYPE=SNG|TITLE Green Day - Time Of Your Life (Good Riddance)|ARTIST |ALBUM ", "yyy", "yyy", "yyy",
                "yyy", "yyy", "yyy");
        SonosMediaInformation result = SonosMediaInformation.parseRadioAppMediaInfo("", trackMetaData);
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
        SonosMetaData trackMetaData = new SonosMetaData("xxx", "xxx", "xxx", "xxx", "xxx",
                "Time Of Your Life (Good Riddance)", "xxx", "xxx", "Nimrod", "Green Day");
        SonosMediaInformation result = SonosMediaInformation.parseTrack(trackMetaData);
        assertEquals("Green Day", result.getArtist());
        assertEquals("Nimrod", result.getAlbum());
        assertEquals("Time Of Your Life (Good Riddance)", result.getTitle());
        assertEquals("Green Day - Nimrod - Time Of Your Life (Good Riddance)", result.getCombinedInfo());
        assertEquals(true, result.needsUpdate());
    }

    @Test
    public void parseTrackWithoutArtist() {
        SonosMetaData trackMetaData = new SonosMetaData("xxx", "xxx", "xxx", "xxx", "xxx",
                "Time Of Your Life (Good Riddance)", "xxx", "", "Nimrod", "");
        SonosMediaInformation result = SonosMediaInformation.parseTrack(trackMetaData);
        assertEquals("", result.getArtist());
        assertEquals("Nimrod", result.getAlbum());
        assertEquals("Time Of Your Life (Good Riddance)", result.getTitle());
        assertEquals("Nimrod - Time Of Your Life (Good Riddance)", result.getCombinedInfo());
        assertEquals(true, result.needsUpdate());
    }

    @Test
    public void parseTrackWithoutAlbum() {
        SonosMetaData trackMetaData = new SonosMetaData("xxx", "xxx", "xxx", "xxx", "xxx",
                "Time Of Your Life (Good Riddance)", "xxx", "Green Day", "", "");
        SonosMediaInformation result = SonosMediaInformation.parseTrack(trackMetaData);
        assertEquals("Green Day", result.getArtist());
        assertEquals("", result.getAlbum());
        assertEquals("Time Of Your Life (Good Riddance)", result.getTitle());
        assertEquals("Green Day - Time Of Your Life (Good Riddance)", result.getCombinedInfo());
        assertEquals(true, result.needsUpdate());
    }

    @Test
    public void parseTrackWithoutTitle() {
        SonosMetaData trackMetaData = new SonosMetaData("xxx", "xxx", "xxx", "xxx", "xxx", "", "xxx", "xxx", "Nimrod",
                "Green Day");
        SonosMediaInformation result = SonosMediaInformation.parseTrack(trackMetaData);
        assertEquals("Green Day", result.getArtist());
        assertEquals("Nimrod", result.getAlbum());
        assertEquals("", result.getTitle());
        assertEquals("Green Day - Nimrod", result.getCombinedInfo());
        assertEquals(true, result.needsUpdate());
    }

    @Test
    public void parseTrackWithOnlyTitle() {
        SonosMetaData trackMetaData = new SonosMetaData("", "", "", "", "", "Time Of Your Life (Good Riddance)", "", "",
                "", "");
        SonosMediaInformation result = SonosMediaInformation.parseTrack(trackMetaData);
        assertEquals("", result.getArtist());
        assertEquals("", result.getAlbum());
        assertEquals("Time Of Your Life (Good Riddance)", result.getTitle());
        assertEquals("Time Of Your Life (Good Riddance)", result.getCombinedInfo());
        assertEquals(true, result.needsUpdate());
    }

    @Test
    public void parseTrackWithEmptyMetaData() {
        SonosMetaData trackMetaData = new SonosMetaData("", "", "", "", "", "", "", "", "", "");
        SonosMediaInformation result = SonosMediaInformation.parseTrack(trackMetaData);
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
        SonosMetaData trackMetaData = new SonosMetaData("xxx", "xxx", "xxx", "xxx", "xxx",
                "Time Of Your Life (Good Riddance)", "xxx", "xxx", "Nimrod", "Green Day");
        SonosMediaInformation result = SonosMediaInformation.parseTrackTitle(trackMetaData);
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
