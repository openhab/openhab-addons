/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.vdr.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.text.ParseException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.vdr.internal.svdrp.SVDRPEpgEvent;
import org.openhab.binding.vdr.internal.svdrp.SVDRPException;
import org.openhab.binding.vdr.internal.svdrp.SVDRPParseResponseException;

/**
 * Specific unit tests to check if {@link SVDRPEpgEvent} parses SVDRP responses correctly
 *
 * @author Matthias Klocke - Initial contribution
 *
 */
@NonNullByDefault
public class SVDRPEpgEventTest {
    private final String epgResponseComplete = """
            C S19.2E-1-1201-28326 WDR HD Bielefeld
            E 9886 1610391600 900 4E F
            T Tagesschau
            S Aktuelle Nachrichten aus der Welt
            D Themen u.a.:|* Corona-Pandemie in Deutschland: Verschärfter Lockdown bundesweit in Kraft|* Entmachtung des US-Präsidenten: Demokraten planen Schritte gegen Trump|* Wintereinbruch in Bosnien-Herzegowina: Dramatische Lage der Flüchtlinge an der Grenze zu Kroatien
            G 20 80
            X 2 03 deu stereo
            X 2 03 deu ohne Audiodeskription
            X 3 01 deu Teletext-Untertitel
            X 3 20 deu mit DVB-Untertitel
            X 5 0B deu HD-Video
            V 1610391600
            e
            c
            End of EPG data\
            """;
    private final String epgMissingSubtitle = """
            C S19.2E-1-1201-28326 WDR HD Bielefeld
            E 9886 1610391600 900 4E F
            T Tagesschau
            D Themen u.a.:|* Corona-Pandemie in Deutschland: Verschärfter Lockdown bundesweit in Kraft|* Entmachtung des US-Präsidenten: Demokraten planen Schritte gegen Trump|* Wintereinbruch in Bosnien-Herzegowina: Dramatische Lage der Flüchtlinge an der Grenze zu Kroatien
            G 20 80
            X 2 03 deu stereo
            X 2 03 deu ohne Audiodeskription
            X 3 01 deu Teletext-Untertitel
            X 3 20 deu mit DVB-Untertitel
            X 5 0B deu HD-Video
            V 1610391600
            e
            c
            End of EPG data\
            """;
    private final String epgParseError = "E 9999999999999999999999999";
    private final String epgCorruptDate = """
            C S19.2E-1-1201-28326 WDR HD Bielefeld
            E 9886 2a10391600 900 4E F
            T Tagesschau
            D Themen u.a.:|* Corona-Pandemie in Deutschland: Verschärfter Lockdown bundesweit in Kraft|* Entmachtung des US-Präsidenten: Demokraten planen Schritte gegen Trump|* Wintereinbruch in Bosnien-Herzegowina: Dramatische Lage der Flüchtlinge an der Grenze zu Kroatien
            G 20 80
            X 2 03 deu stereo
            X 2 03 deu ohne Audiodeskription
            X 3 01 deu Teletext-Untertitel
            X 3 20 deu mit DVB-Untertitel
            X 5 0B deu HD-Video
            V 1610391600
            e
            c
            End of EPG data\
            """;

    private final String epgMissingEnd = """
            C S19.2E-1-1201-28326 WDR HD Bielefeld
            E 9886 1610391600 900 4E F
            T Tagesschau
            D Themen u.a.:|* Corona-Pandemie in Deutschland: Verschärfter Lockdown bundesweit in Kraft|* Entmachtung des US-Präsidenten: Demokraten planen Schritte gegen Trump|* Wintereinbruch in Bosnien-Herzegowina: Dramatische Lage der Flüchtlinge an der Grenze zu Kroatien
            G 20 80
            X 2 03 deu stereo
            X 2 03 deu ohne Audiodeskription
            X 3 01 deu Teletext-Untertitel
            X 3 20 deu mit DVB-Untertitel
            X 5 0B deu HD-Video
            V 1610391600
            e
            c
            """;

    @Test
    public void testParseEpgEventComplete() throws SVDRPException, ParseException {
        SVDRPEpgEvent event = SVDRPEpgEvent.parse(epgResponseComplete);
        assertEquals("Tagesschau", event.getTitle());
        assertEquals("Aktuelle Nachrichten aus der Welt", event.getSubtitle());
        assertEquals(15, event.getDuration());
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
        assertEquals(ZonedDateTime.parse("2021-01-11 19:00:00 UTC", dtf).toInstant(), event.getBegin());
        assertEquals(ZonedDateTime.parse("2021-01-11 19:15:00 UTC", dtf).toInstant(), event.getEnd());
    }

    @Test
    public void testParseEpgEventMissingSubtitle() throws SVDRPException {
        SVDRPEpgEvent event = SVDRPEpgEvent.parse(epgMissingSubtitle);
        assertEquals("Tagesschau", event.getTitle());
        assertEquals("", event.getSubtitle());
    }

    @Test
    public void testParseEpgEventCorruptDate() {
        assertThrows(SVDRPParseResponseException.class, () -> {
            SVDRPEpgEvent.parse(epgCorruptDate);
        });
    }

    @Test
    public void testParseEpgEventMissingEnd() throws SVDRPException, ParseException {
        SVDRPEpgEvent event = SVDRPEpgEvent.parse(epgMissingEnd);
        assertEquals("Tagesschau", event.getTitle());
        assertEquals("", event.getSubtitle());
        assertEquals(15, event.getDuration());
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
        assertEquals(ZonedDateTime.parse("2021-01-11 19:00:00 UTC", dtf).toInstant(), event.getBegin());
        assertEquals(ZonedDateTime.parse("2021-01-11 19:15:00 UTC", dtf).toInstant(), event.getEnd());
    }

    @Test
    public void testParseExceptionVolumeData() {
        assertThrows(SVDRPParseResponseException.class, () -> {
            SVDRPEpgEvent.parse(epgParseError);
        });
    }
}
