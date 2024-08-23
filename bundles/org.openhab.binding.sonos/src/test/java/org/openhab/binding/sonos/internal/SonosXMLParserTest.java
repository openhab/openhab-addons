/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class SonosXMLParserTest {

    @Test
    public void buildThingTypeIdFromModelWithoutSpace() {
        assertEquals("Move", SonosXMLParser.buildThingTypeIdFromModelName("Sonos Move"));
    }

    @Test
    public void buildThingTypeIdFromModelWithSpace() {
        assertEquals("RoamSL", SonosXMLParser.buildThingTypeIdFromModelName("Sonos Roam SL"));
    }

    @Test
    public void buildThingTypeIdFromModelWithColon() {
        assertEquals("PLAY5", SonosXMLParser.buildThingTypeIdFromModelName("Sonos PLAY:5"));
    }

    @Test
    public void buildThingTypeIdFromSymfoniskModel() {
        assertEquals("SYMFONISK", SonosXMLParser.buildThingTypeIdFromModelName("SYMFONISK Table lamp"));
        assertEquals("SYMFONISK", SonosXMLParser.buildThingTypeIdFromModelName("Symfonisk Table lamp"));
        assertEquals("SYMFONISK", SonosXMLParser.buildThingTypeIdFromModelName("Sonos Symfonisk"));
    }

    @Test
    public void buildThingTypeIdFromZP80Model() {
        assertEquals("CONNECT", SonosXMLParser.buildThingTypeIdFromModelName("Sonos ZP80"));
    }

    @Test
    public void buildThingTypeIdFromZP100Model() {
        assertEquals("CONNECTAMP", SonosXMLParser.buildThingTypeIdFromModelName("Sonos ZP100"));
    }

    @Test
    public void buildThingTypeIdFromModelWithAdditionalTextInParenthesis() {
        assertEquals("OneSL", SonosXMLParser.buildThingTypeIdFromModelName("Sonos One SL (OpenHome)"));
    }

    @Test
    public void getRadioTimeFromXML() throws IOException {
        InputStream resourceStream = getClass().getResourceAsStream("/OPML.xml");
        assertNotNull(resourceStream);
        final String opmlResult = new String(resourceStream.readAllBytes(), StandardCharsets.UTF_8);
        List<String> result = SonosXMLParser.getRadioTimeFromXML(opmlResult);
        assertEquals(3, result.size());
        if (result.size() == 3) {
            assertEquals("RTL2 105.9", result.get(0));
            assertEquals("Le Son Pop-Rock", result.get(1));
            assertEquals("Paris, France", result.get(2));
        }
    }

    @Test
    public void getMetaDataFromXML() throws IOException {
        InputStream resourceStream = getClass().getResourceAsStream("/MetaData.xml");
        assertNotNull(resourceStream);
        final String xml = new String(resourceStream.readAllBytes(), StandardCharsets.UTF_8);
        SonosMetaData sonosMetaData = SonosXMLParser.getMetaDataFromXML(xml);
        assertEquals("-1", sonosMetaData.getId());
        assertEquals("-1", sonosMetaData.getParentId());
        assertEquals("Turn Down for What - Single", sonosMetaData.getAlbum());
        assertEquals("DJ Snake & Lil Jon", sonosMetaData.getCreator());
        assertEquals("Turn Down for What", sonosMetaData.getTitle());
        assertEquals("object.item.audioItem.musicTrack", sonosMetaData.getUpnpClass());
        assertEquals("x-sonosapi-hls-static:librarytrack%3ai.eoD8VQ5SZOB8QX7?sid=204&flags=8232&sn=9",
                sonosMetaData.getResource());
        assertEquals(
                "/getaa?s=1&u=x-sonosapi-hls-static%3alibrarytrack%253ai.eoD8VQ5SZOB8QX7%3fsid%3d204%26flags%3d8232%26sn%3d9",
                sonosMetaData.getAlbumArtUri());
    }

    @Test
    public void compileMetadataString() {
        SonosEntry sonosEntry = new SonosEntry("1", "Can't Buy Me Love", "0", "A Hard Day's Night", "", "",
                "object.item.audioItem.musicTrack", "");
        String expected = """
                <DIDL-Lite xmlns:dc="http://purl.org/dc/elements/1.1/" \
                xmlns:upnp="urn:schemas-upnp-org:metadata-1-0/upnp/" \
                xmlns:r="urn:schemas-rinconnetworks-com:metadata-1-0/" \
                xmlns="urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/">\
                <item id="1" parentID="0" restricted="true">\
                <dc:title>Can&apos;t Buy Me Love</dc:title>\
                <upnp:class>object.item.audioItem.musicTrack</upnp:class>\
                <desc id="cdudn" nameSpace="urn:schemas-rinconnetworks-com:metadata-1-0/">RINCON_AssociatedZPUDN</desc>\
                </item>\
                </DIDL-Lite>\
                """;
        String actual = SonosXMLParser.compileMetadataString(sonosEntry);
        assertEquals(expected, actual);
    }
}
