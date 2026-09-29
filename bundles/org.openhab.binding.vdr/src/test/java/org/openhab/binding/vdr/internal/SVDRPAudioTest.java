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
package org.openhab.binding.vdr.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.vdr.internal.svdrp.SVDRPAudio;
import org.openhab.binding.vdr.internal.svdrp.SVDRPException;
import org.openhab.binding.vdr.internal.svdrp.SVDRPParseResponseException;

/**
 * Specific unit tests to check if {@link SVDRPAudio} parses SVDRP responses correctly
 *
 * @author Matthias Klocke - Initial contribution
 *
 */
@NonNullByDefault
public class SVDRPAudioTest {

    private final String audioResponseOk = "1 deu stereo\r\n" + "2 mis mit Audiodeskription\r\n"
            + "3 qks Klare Sprache\r\n" + "33 deu *Dolby Digital 5.1\r\n";
    private final String audioResponseOkMultiLang = "1 deu *stereo\r\n" + "2 fra französisch\r\n"
            + "3 mul mit Audiodeskription\r\n" + "4 mis Klare Sprache (wenn kein Origin\r\n";
    private final String audioResponseParseError = "deu1stereo\r\n" + "mis2mitAudiodeskription\r\n"
            + "qks3KlareSprache\r\n" + "deu33*Dolby Digital 5.1\r\n";
    private final String audioResponseNoActiveTrack = "1 deu stereo\r\n" + "2 mis mit Audiodeskription\r\n"
            + "3 qks Klare Sprache\r\n" + "33 deu Dolby Digital 5.1\r\n";
    private final String audioResponseLanguageNotSet = "1 --- *stereo\r\n";
    private final String audioResponseLanguageDualLang = "1 deu+eng *stereo\r\n";

    @Test
    public void testParseAudioData() throws SVDRPException {
        SVDRPAudio audio = SVDRPAudio.parse(audioResponseOk);
        assertEquals("stereo", audio.getAudioTracks().get(0).getDescription());
        assertEquals("deu", audio.getAudioTracks().get(0).getLanguage());
        assertEquals(33, audio.getActiveTrackNumber());
        assertEquals("Dolby Digital 5.1", audio.getAudioTracks().get(3).getDescription());

        audio = SVDRPAudio.parse(audioResponseOkMultiLang);
        assertEquals("fra", audio.getAudioTracks().get(1).getLanguage());
        assertEquals("französisch", audio.getAudioTracks().get(1).getDescription());
        assertEquals(1, audio.getActiveTrackNumber());

        audio = SVDRPAudio.parse(audioResponseLanguageNotSet);
        assertEquals("---", audio.getAudioTracks().get(0).getLanguage());

        audio = SVDRPAudio.parse(audioResponseLanguageDualLang);
        assertEquals("deu+eng", audio.getAudioTracks().get(0).getLanguage());
    }

    @Test
    public void testParseExceptionAudioData() {
        assertThrows(SVDRPParseResponseException.class, () -> {
            SVDRPAudio.parse(audioResponseParseError);
        });

        assertThrows(SVDRPParseResponseException.class, () -> {
            SVDRPAudio audio = SVDRPAudio.parse(audioResponseNoActiveTrack);
            audio.getActiveTrackNumber();
        });
    }
}
