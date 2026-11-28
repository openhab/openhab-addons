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
package org.openhab.binding.homekit.internal.camera.go2rtc;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;

import org.junit.jupiter.api.Test;

/**
 * Tests for the go2rtc streams response parsing in {@link Go2RtcProcess}.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
class Go2RtcProcessTest {

    private static final String CAM = "homekit_cam";
    private static final String TALK = "homekit_cam_talk";

    @Test
    void consumerOnListenStreamMeansDemand() throws IOException {
        String json = """
                {"homekit_cam":{"producers":[],"consumers":[{"url":"webrtc"}]},"homekit_cam_talk":{"producers":[],"consumers":null}}
                """;
        assertTrue(Go2RtcProcess.hasConsumers(json, CAM, TALK));
    }

    @Test
    void consumerOnTalkStreamMeansDemand() throws IOException {
        String json = """
                {"homekit_cam":{"producers":[],"consumers":null},"homekit_cam_talk":{"producers":[],"consumers":[{}]}}
                """;
        assertTrue(Go2RtcProcess.hasConsumers(json, CAM, TALK));
    }

    @Test
    void nullAndEmptyConsumersMeanNoDemand() throws IOException {
        assertFalse(Go2RtcProcess.hasConsumers(
                "{\"homekit_cam\":{\"producers\":[],\"consumers\":null},\"homekit_cam_talk\":{\"consumers\":null}}",
                CAM, TALK));
        assertFalse(Go2RtcProcess.hasConsumers(
                "{\"homekit_cam\":{\"producers\":[],\"consumers\":[]},\"homekit_cam_talk\":{\"consumers\":[]}}", CAM,
                TALK));
    }

    @Test
    void missingConsumersFieldMeansNoDemand() throws IOException {
        assertFalse(Go2RtcProcess.hasConsumers("{\"homekit_cam\":{\"producers\":[]}}", CAM, TALK));
    }

    @Test
    void absentConfiguredStreamsAreUnknown() {
        assertThrows(IOException.class, () -> Go2RtcProcess.hasConsumers("{\"other\":{\"consumers\":[]}}", CAM, TALK));
        assertThrows(IOException.class, () -> Go2RtcProcess.hasConsumers("{}", CAM, TALK));
    }

    @Test
    void malformedResponsesAreUnknown() {
        assertThrows(IOException.class, () -> Go2RtcProcess.hasConsumers("", CAM, TALK));
        assertThrows(IOException.class, () -> Go2RtcProcess.hasConsumers("{not json", CAM, TALK));
        assertThrows(IOException.class, () -> Go2RtcProcess.hasConsumers("[]", CAM, TALK));
        assertThrows(IOException.class, () -> Go2RtcProcess.hasConsumers("null", CAM, TALK));
    }

    @Test
    void talkProducerMeansActiveTalk() throws IOException {
        assertTrue(Go2RtcProcess.hasProducers(
                "{\"homekit_cam\":{\"producers\":[],\"consumers\":[]},\"homekit_cam_talk\":{\"producers\":[{}],\"consumers\":[{}]}}",
                TALK));
    }

    @Test
    void noTalkProducerMeansIdle() throws IOException {
        assertFalse(Go2RtcProcess.hasProducers(
                "{\"homekit_cam\":{\"producers\":[],\"consumers\":[]},\"homekit_cam_talk\":{\"producers\":[],\"consumers\":[{}]}}",
                TALK));
        assertFalse(Go2RtcProcess.hasProducers("{\"homekit_cam_talk\":{\"consumers\":null}}", TALK));
    }

    @Test
    void absentOrMalformedTalkStreamIsUnknown() {
        assertThrows(IOException.class, () -> Go2RtcProcess.hasProducers("{\"other\":{}}", TALK));
        assertThrows(IOException.class, () -> Go2RtcProcess.hasProducers("[]", TALK));
        assertThrows(IOException.class, () -> Go2RtcProcess.hasProducers("{not json", TALK));
    }

    @Test
    void freeTcpPortIsBindableAfterSelection() throws IOException {
        int port = Go2RtcConfig.freeTcpPort();
        assertTrue(port > 0 && port <= 65535);
        try (ServerSocket socket = new ServerSocket(port)) {
            assertEquals(port, socket.getLocalPort());
        }
    }

    @Test
    void freeTcpAndUdpPortIsBindableForBoth() throws IOException {
        int port = Go2RtcConfig.freeTcpUdpPort();
        try (ServerSocket tcp = new ServerSocket(port); DatagramSocket udp = new DatagramSocket(port)) {
            assertEquals(port, tcp.getLocalPort());
            assertEquals(port, udp.getLocalPort());
        }
    }
}
