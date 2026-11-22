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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.binding.homekit.internal.camera.codec.AudioCodec;
import org.openhab.binding.homekit.internal.camera.codec.VideoCodec;
import org.openhab.binding.homekit.internal.camera.session.RtcpReceiverReport;
import org.openhab.binding.homekit.internal.camera.session.SrtpParameters;
import org.openhab.binding.homekit.internal.camera.session.SrtpSession;
import org.openhab.binding.homekit.internal.enums.AudioCodecType;
import org.openhab.binding.homekit.internal.enums.VideoCodecType;

/**
 * Tests for {@link Go2RtcDataPump} codec selection, port allocation and full-duplex audio.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 * @author Andrew Fiddian-Green - Symmetric audio regression coverage
 * @author Andrew Fiddian-Green - Session transition regression coverage
 */
class Go2RtcDataPumpTest {

    private static final AudioCodec OPUS = new AudioCodec(AudioCodecType.OPUS, 1, 16, 20);
    private static final AudioCodec PCMA = new AudioCodec(AudioCodecType.PCMA, 1, 8, 20);
    private static final AudioCodec AAC_ELD = new AudioCodec(AudioCodecType.AAC_ELD, 1, 16, 30);
    private static final VideoCodec H264 = new VideoCodec(VideoCodecType.H_264, 0, 0, 640, 480, 30);

    @Test
    void testSelectsOpusAudioAndH264Video() throws IOException {
        try (Go2RtcDataPump pump = new Go2RtcDataPump(List.of(PCMA, OPUS), List.of(H264), null)) {
            assertEquals(AudioCodecType.OPUS, pump.audioCodec().codecType());
            assertEquals(VideoCodecType.H_264, pump.videoCodec().codecType());
        }
    }

    @Test
    void testAllocatesDistinctPorts() throws IOException {
        try (Go2RtcDataPump pump = new Go2RtcDataPump(List.of(OPUS), List.of(H264), null)) {
            assertTrue(pump.audioPort() > 0);
            assertTrue(pump.videoPort() > 0);
            assertNotEquals(pump.audioPort(), pump.videoPort());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    @Timeout(30)
    void testAudioListenAndTalkShareEndpoints(boolean replaceSession) throws Exception {
        InetAddress loopback = InetAddress.getByName("127.0.0.1");
        byte[] cameraRtp = { (byte) 0x80, 110, 0, 1, 0, 0, 0, 0, 0x12, 0x34, 0x56, 0x79, 1, 2, 3 };
        byte[] talkRtp = { (byte) 0x80, 110, 0, 1, 0, 0, 0, 0, 0x12, 0x34, 0x56, 0x78, 4, 5, 6 };
        SrtpParameters cameraParameters = SrtpParameters.random();
        SrtpParameters talkParameters = SrtpParameters.random();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor();
                ServerSocket server = new ServerSocket(0, 1, loopback);
                DatagramSocket camera = new DatagramSocket(0, loopback);
                Go2RtcDataPump pump = new Go2RtcDataPump(List.of(OPUS), List.of(H264), null)) {
            server.setSoTimeout(10000);
            camera.setSoTimeout(10000);
            var relay = executor.submit(() -> {
                try (Socket publisher = server.accept()) {
                    publisher.setSoTimeout(10000);
                    respond(publisher, "ANNOUNCE", "SETUP", "SETUP", "RECORD");
                    for (int i = 0; i < (replaceSession ? 2 : 1); i++) {
                        try (Socket talk = server.accept()) {
                            talk.setSoTimeout(10000);
                            respond(talk, "DESCRIBE", "SETUP", "PLAY");
                            var out = talk.getOutputStream();
                            out.write(new byte[] { '$', 0, 0, (byte) talkRtp.length });
                            out.write(talkRtp);
                            out.flush();

                            if (!replaceSession || i == 1) {
                                var in = new DataInputStream(publisher.getInputStream());
                                assertEquals('$', in.readUnsignedByte());
                                assertEquals(0, in.readUnsignedByte());
                                assertEquals(cameraRtp.length, in.readUnsignedShort());
                                assertArrayEquals(cameraRtp, in.readNBytes(cameraRtp.length));
                            }
                        }
                    }
                }
                return null;
            });

            pump.connect(server.getLocalPort(), "listen", "talk");
            Go2RtcDataPump.Session initial = pump.startSession(cameraParameters, SrtpParameters.random(),
                    talkParameters, SrtpParameters.random(), loopback.getHostAddress(), camera.getLocalPort());
            synchronized (initial.talkSession()) {
                pump.startTalk();
                if (replaceSession) {
                    awaitBlockedOn(initial.talkSession());
                    talkParameters = SrtpParameters.random();
                    pump.startSession(cameraParameters, SrtpParameters.random(), talkParameters,
                            SrtpParameters.random(), loopback.getHostAddress(), camera.getLocalPort());
                    pump.startTalk();
                }
            }

            byte[] received = new byte[2048];
            DatagramPacket packet = new DatagramPacket(received, received.length);
            camera.receive(packet);
            assertEquals(loopback, packet.getAddress());
            assertEquals(pump.audioPort(), packet.getPort(), "Talk-back must use the advertised audio source port");
            byte[] decoded = new byte[2048];
            int decodedLength = SrtpSession.create(talkParameters).decrypt(received, packet.getLength(), decoded);
            assertEquals(talkRtp.length, decodedLength);
            assertArrayEquals(talkRtp, Arrays.copyOf(decoded, decodedLength));

            byte[] encrypted = new byte[2048];
            int encryptedLength = SrtpSession.create(cameraParameters).encrypt(cameraRtp, cameraRtp.length, encrypted);
            camera.send(new DatagramPacket(encrypted, encryptedLength, packet.getSocketAddress()));
            relay.get(10, TimeUnit.SECONDS);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    @Timeout(40)
    void testSessionReplacementDiscardsInFlightPackets(boolean receiverReport) throws Exception {
        InetAddress loopback = InetAddress.getByName("127.0.0.1");
        byte[] oldRtp = { (byte) 0x80, 110, 0, 1, 0, 0, 0, 0, 0x12, 0x34, 0x56, 0x79, 1, 2, 3 };
        byte[] newRtp = { (byte) 0x80, 110, 0, 7, 0, 0, 0, 0, 0x12, 0x34, 0x56, 0x7A, 4, 5, 6 };
        SrtpParameters oldParameters = SrtpParameters.random();
        SrtpParameters newParameters = SrtpParameters.random();
        SrtpParameters newTalkParameters = SrtpParameters.random();
        var frames = new LinkedBlockingQueue<byte[]>();
        var finish = new CountDownLatch(1);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor();
                ServerSocket server = new ServerSocket(0, 1, loopback);
                DatagramSocket camera = new DatagramSocket(0, loopback);
                Go2RtcDataPump pump = new Go2RtcDataPump(List.of(OPUS), List.of(H264), null)) {
            server.setSoTimeout(10000);
            camera.setSoTimeout(10000);
            var relay = executor.submit(() -> {
                try (Socket publisher = server.accept()) {
                    publisher.setSoTimeout(10000);
                    respond(publisher, "ANNOUNCE", "SETUP", "SETUP", "RECORD");
                    var in = new DataInputStream(publisher.getInputStream());
                    for (int i = 0; i < (receiverReport ? 2 : 1); i++) {
                        assertEquals('$', in.readUnsignedByte());
                        assertEquals(0, in.readUnsignedByte());
                        frames.add(in.readNBytes(in.readUnsignedShort()));
                    }
                    assertTrue(finish.await(10, TimeUnit.SECONDS));
                }
                return null;
            });
            try {
                pump.connect(server.getLocalPort(), "listen", "talk");
                Go2RtcDataPump.Session old = pump.startSession(oldParameters, SrtpParameters.random(),
                        SrtpParameters.random(), SrtpParameters.random(), loopback.getHostAddress(),
                        camera.getLocalPort());
                SrtpSession blocked = receiverReport ? old.talkSession() : old.audioSession();
                synchronized (blocked) {
                    sendCameraAudio(camera, pump.audioPort(), oldParameters, oldRtp);
                    if (receiverReport) {
                        assertArrayEquals(oldRtp, frames.poll(10, TimeUnit.SECONDS));
                    }
                    awaitBlockedOn(blocked);
                    pump.startSession(newParameters, SrtpParameters.random(), newTalkParameters,
                            SrtpParameters.random(), loopback.getHostAddress(), camera.getLocalPort());
                }
                sendCameraAudio(camera, pump.audioPort(), newParameters, newRtp);
                assertArrayEquals(newRtp, frames.poll(10, TimeUnit.SECONDS),
                        "A packet from the replaced session must not be published");

                if (receiverReport) {
                    DatagramPacket packet = new DatagramPacket(new byte[2048], 2048);
                    camera.receive(packet);
                    var report = new RtcpReceiverReport(Go2RtcDataPump.AUDIO_SYNC_SOURCE, OPUS.rtpClockRate());
                    report.onRtp(newRtp, newRtp.length, 0);
                    byte[] rr = report.buildReport();
                    byte[] expected = new byte[rr.length + 14];
                    SrtpSession.create(newTalkParameters).encryptRtcp(rr, rr.length, expected);
                    assertArrayEquals(expected, Arrays.copyOf(packet.getData(), packet.getLength()),
                            "Receiver Reports must use the current session's statistics and keys");
                }
            } finally {
                finish.countDown();
            }
            relay.get(10, TimeUnit.SECONDS);
        }
    }

    @Test
    void testCannotRestartClosedPump() throws IOException {
        try (Go2RtcDataPump pump = new Go2RtcDataPump(List.of(OPUS), List.of(H264), null)) {
            pump.close();
            assertThrows(IllegalStateException.class, () -> pump.startSession(SrtpParameters.random(),
                    SrtpParameters.random(), SrtpParameters.random(), SrtpParameters.random(), "127.0.0.1", 5000));
        }
    }

    @Test
    void testRejectsCameraWithoutOpus() {
        assertThrows(IllegalArgumentException.class, () -> new Go2RtcDataPump(List.of(PCMA), List.of(H264), null));
    }

    @Test
    void testRejectsCameraWithoutH264() {
        assertThrows(IllegalArgumentException.class, () -> new Go2RtcDataPump(List.of(OPUS), List.of(), null));
    }

    @Test
    void testAacEldRequiresFfmpegBinary() {
        assertThrows(IllegalArgumentException.class, () -> new Go2RtcDataPump(List.of(AAC_ELD), List.of(H264), null));
    }

    private static void sendCameraAudio(DatagramSocket camera, int port, SrtpParameters parameters, byte[] rtp)
            throws IOException {
        byte[] encrypted = new byte[rtp.length + 10];
        SrtpSession.create(parameters).encrypt(rtp, rtp.length, encrypted);
        camera.send(new DatagramPacket(encrypted, encrypted.length, InetAddress.getByName("127.0.0.1"), port));
    }

    private static void awaitBlockedOn(Object monitor) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            for (ThreadInfo thread : ManagementFactory.getThreadMXBean().dumpAllThreads(false, false)) {
                var lock = thread.getLockInfo();
                if (thread.getThreadState() == Thread.State.BLOCKED && lock != null
                        && lock.getIdentityHashCode() == System.identityHashCode(monitor)) {
                    return;
                }
            }
            Thread.sleep(10);
        }
        fail("No relay worker blocked on the session crypto context");
    }

    private static void respond(Socket socket, String... methods) throws IOException {
        var reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
        for (String method : methods) {
            String request = reader.readLine();
            assertNotNull(request);
            assertTrue(request.startsWith(method + " "));
            int contentLength = 0;
            String cseq = "";
            for (String header = reader.readLine(); header != null && !header.isEmpty(); header = reader.readLine()) {
                if (header.startsWith("Content-Length: ")) {
                    contentLength = Integer.parseInt(header.substring("Content-Length: ".length()));
                } else if (header.startsWith("CSeq: ")) {
                    cseq = header;
                }
            }
            for (int i = 0; i < contentLength; i++) {
                assertNotEquals(-1, reader.read());
            }
            socket.getOutputStream().write(("RTSP/1.0 200 OK\r\n" + cseq + "\r\nContent-Length: 0\r\n\r\n")
                    .getBytes(StandardCharsets.US_ASCII));
            socket.getOutputStream().flush();
        }
    }
}
