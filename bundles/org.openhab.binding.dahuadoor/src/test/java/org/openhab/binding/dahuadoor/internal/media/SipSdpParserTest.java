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
package org.openhab.binding.dahuadoor.internal.media;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SipSdpParser}.
 *
 * @author Sven Schad - Initial contribution
 */
@NonNullByDefault
class SipSdpParserTest {

    private final SipSdpParser parser = new SipSdpParser();

    @Test
    void parseAudioOfferSelectsSupportedCodecFromOfferedPayloads() {
        String sdp = "v=0\r\n" + "o=- 1 1 IN IP4 192.0.2.10\r\n" + "s=-\r\n" + "c=IN IP4 192.0.2.10\r\n" + "t=0 0\r\n"
                + "m=audio 5004 RTP/AVP 96 97 8 0\r\n" + "a=rtpmap:96 opus/48000/2\r\n" + "a=rtpmap:97 PCM/16000\r\n"
                + "a=ptime:20\r\n";

        Optional<SipAudioOffer> parsed = parser.parseAudioOffer(sdp);
        assertTrue(parsed.isPresent());

        SipAudioOffer offer = parsed.get();
        assertEquals("192.0.2.10", offer.getRemoteHost());
        assertEquals(5004, offer.getRemotePort());
        assertEquals(97, offer.getPayloadType());
        assertEquals("PCM", offer.getCodecName());
        assertEquals(16000, offer.getClockRate());
        assertEquals(20, offer.getPtimeMs());
    }

    @Test
    void parseAudioOfferPrefersPcm16000OverPcmuWhenBothAreOffered() {
        String sdp = "v=0\r\n" + "o=- 1 1 IN IP4 198.51.100.20\r\n" + "s=Dahua VT 1.5\r\n"
                + "c=IN IP4 198.51.100.20\r\n" + "t=0 0\r\n" + "m=audio 20000 RTP/AVP 0 97 101\r\n"
                + "a=rtpmap:97 PCM/16000\r\n" + "a=rtpmap:0 PCMU/8000\r\n" + "a=rtpmap:101 telephone-event/8000\r\n"
                + "a=ptime:20\r\n";

        Optional<SipAudioOffer> parsed = parser.parseAudioOffer(sdp);
        assertTrue(parsed.isPresent());

        SipAudioOffer offer = parsed.get();
        assertEquals(97, offer.getPayloadType());
        assertEquals("PCM", offer.getCodecName());
        assertEquals(16000, offer.getClockRate());
    }

    @Test
    void parseAudioOfferReturnsEmptyForPcmaOnly() {
        String sdp = "v=0\r\n" + "o=- 1 1 IN IP4 203.0.113.30\r\n" + "s=-\r\n" + "c=IN IP4 203.0.113.30\r\n"
                + "t=0 0\r\n" + "m=audio 6000 RTP/AVP 101\r\n" + "a=rtpmap:101 PCMA/8000\r\n" + "a=ptime:30\r\n";

        Optional<SipAudioOffer> parsed = parser.parseAudioOffer(sdp);
        assertTrue(parsed.isEmpty());
    }

    @Test
    void parseAudioOfferReturnsEmptyForUnsupportedCodecsOnly() {
        String sdp = "v=0\r\n" + "o=- 1 1 IN IP4 192.0.2.40\r\n" + "s=-\r\n" + "c=IN IP4 192.0.2.40\r\n" + "t=0 0\r\n"
                + "m=audio 4000 RTP/AVP 96\r\n" + "a=rtpmap:96 opus/48000/2\r\n";

        assertTrue(parser.parseAudioOffer(sdp).isEmpty());
    }

    @Test
    void parseAudioOfferUsesMediaLevelConnectionWhenPresent() {
        String sdp = "v=0\r\n" + "o=- 1 1 IN IP4 198.51.100.40\r\n" + "s=-\r\n" + "c=IN IP4 198.51.100.40\r\n"
                + "t=0 0\r\n" + "m=audio 4500 RTP/AVP 97\r\n" + "a=rtpmap:97 PCM/16000\r\n"
                + "c=IN IP4 203.0.113.41\r\n";

        Optional<SipAudioOffer> parsed = parser.parseAudioOffer(sdp);
        assertTrue(parsed.isPresent());
        assertEquals("203.0.113.41", parsed.get().getRemoteHost());
    }

    @Test
    void buildAnswerSdpContainsNegotiatedAudioAttributes() {
        String sdp = "v=0\r\n" + "o=- 1 1 IN IP4 192.0.2.50\r\n" + "s=-\r\n" + "c=IN IP4 192.0.2.50\r\n" + "t=0 0\r\n"
                + "m=audio 5004 RTP/AVP 97\r\n" + "a=rtpmap:97 PCM/16000\r\n" + "a=ptime:20\r\n";

        Optional<String> answer = parser.buildAnswerSdp(sdp, "192.168.1.10", 6000);
        assertTrue(answer.isPresent());

        String answerText = answer.get();
        assertTrue(answerText.contains("c=IN IP4 192.168.1.10\r\n"));
        // Answer must only advertise payload types that were offered (offer had only PT 97 = PCM)
        assertTrue(answerText.contains("m=audio 6000 RTP/AVP 97\r\n"));
        assertTrue(answerText.contains("a=rtpmap:97 PCM/16000\r\n"));
        assertTrue(answerText.contains("a=sendrecv\r\n"));
        assertTrue(answerText.contains("m=video 30000 RTP/AVP 96\r\n"));
        assertTrue(answerText.contains("a=framerate:25.000000\r\n"));
        assertTrue(answerText.contains("a=rtpmap:96 H264/90000\r\n"));
        assertTrue(answerText.contains("a=recvonly\r\n"));
    }

    @Test
    void buildAnswerSdpUsesPreferredPcm16000WhenAvailable() {
        String sdp = "v=0\r\n" + "o=- 1 1 IN IP4 198.51.100.60\r\n" + "s=Dahua VT 1.5\r\n"
                + "c=IN IP4 198.51.100.60\r\n" + "t=0 0\r\n" + "m=audio 20000 RTP/AVP 0 97 101\r\n"
                + "a=rtpmap:97 PCM/16000\r\n" + "a=rtpmap:0 PCMU/8000\r\n" + "a=rtpmap:101 telephone-event/8000\r\n"
                + "a=ptime:20\r\n";

        Optional<String> answer = parser.buildAnswerSdp(sdp, "192.0.2.61", 20000);
        assertTrue(answer.isPresent());

        String answerText = answer.get();
        assertTrue(answerText.contains("m=audio 20000 RTP/AVP 97\r\n"));
        assertTrue(answerText.contains("a=rtpmap:97 PCM/16000\r\n"));
    }
}
