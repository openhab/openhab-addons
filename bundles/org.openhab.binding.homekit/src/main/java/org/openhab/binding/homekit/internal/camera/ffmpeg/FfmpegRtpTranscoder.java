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
package org.openhab.binding.homekit.internal.camera.ffmpeg;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.IntFunction;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homekit.internal.camera.codec.AudioCodec;
import org.openhab.binding.homekit.internal.camera.session.RtpPacketSink;
import org.openhab.binding.homekit.internal.camera.session.RtpPacketSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transcodes RTP audio between a camera's AAC-ELD and Opus using an ffmpeg child process.
 * <p>
 * RTP is sent to ffmpeg on a loopback port described by a temporary SDP file, and ffmpeg sends the transcoded RTP back
 * to a loopback port owned by this class. Each received packet is passed to the downstream {@link RtpPacketSink}.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public final class FfmpegRtpTranscoder implements RtpPacketSource, RtpPacketSink, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(FfmpegRtpTranscoder.class);

    private static final int MAX_DATAGRAM_SIZE = 2048;
    private static final int SDP_DELETE_ATTEMPTS = 5;
    private static final long SDP_DELETE_RETRY_MS = 200;
    private static final int MAX_START_ATTEMPTS = 3;
    private static final long STARTUP_CHECK_MS = 2000;

    private record Launch(int inputPort, Path sdpFile, Process process) {
    }

    private final DatagramSocket inputSocket;
    private final DatagramSocket outputSocket;
    private final InetAddress loopback;
    private final int inputPort;
    private final Path sdpFile;
    private final Process process;
    private volatile boolean closed;
    private boolean connected;
    private boolean firstPacketSent;
    private int nextSequence = -1;

    /**
     * Starts ffmpeg. Connecting the downstream sink starts the output relay.
     *
     * @param ffmpeg the ffmpeg executable
     * @param inputSdp builds the SDP describing the input RTP stream, given the loopback port ffmpeg reads from
     * @param inputArgs the decoder arguments applied to the input, see {@link FfmpegHelpers#aacEldDecoderArgs}
     * @param encoderArgs the ffmpeg arguments that select the output codec
     * @param payloadType the RTP payload type of the input and output streams
     * @throws IOException if the sockets, SDP file or process cannot be created
     */
    private FfmpegRtpTranscoder(String ffmpeg, IntFunction<String> inputSdp, List<String> inputArgs,
            List<String> encoderArgs, int payloadType) throws IOException {
        loopback = InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 });
        inputSocket = new DatagramSocket();
        try {
            outputSocket = new DatagramSocket(0, loopback);
        } catch (IOException | RuntimeException e) {
            inputSocket.close();
            throw e;
        }
        Launch launch;
        try {
            launch = launch(ffmpeg, inputSdp, inputArgs, encoderArgs, payloadType, outputSocket.getLocalPort());
        } catch (IOException | RuntimeException e) {
            inputSocket.close();
            outputSocket.close();
            throw e;
        }
        inputPort = launch.inputPort();
        sdpFile = launch.sdpFile();
        process = launch.process();

        Thread logger = new Thread(() -> logOutput(process), "OH-binding-homekit-ffmpeg-log");
        logger.setDaemon(true);
        logger.start();
    }

    @Override
    public synchronized void connectTo(RtpPacketSink downstream) {
        if (connected || closed) {
            throw new IllegalStateException("RTP transcoder is already connected or closed");
        }
        connected = true;
        Thread reader = new Thread(() -> forwardOutput(downstream), "OH-binding-homekit-ffmpeg-transcoder");
        reader.setDaemon(true);
        reader.start();
    }

    /**
     * Creates a transcoder that converts the camera's AAC-ELD RTP to Opus RTP.
     *
     * @param ffmpeg the ffmpeg executable
     * @param codec the camera's AAC-ELD audio codec
     * @param payloadType the RTP payload type used for both input and output
     * @throws IOException if the transcoder cannot be started
     */
    public static FfmpegRtpTranscoder aacEldToOpus(String ffmpeg, AudioCodec codec, int payloadType)
            throws IOException {
        return new FfmpegRtpTranscoder(ffmpeg, port -> FfmpegHelpers.aacEldInputSdp(port, codec, payloadType),
                FfmpegHelpers.aacEldDecoderArgs(), FfmpegHelpers.opusEncoderArgs(), payloadType);
    }

    /**
     * Creates a transcoder that converts browser Opus RTP to the camera's AAC-ELD RTP.
     *
     * @param ffmpeg the ffmpeg executable
     * @param codec the camera's AAC-ELD audio codec
     * @param payloadType the RTP payload type used for both input and output
     * @throws IOException if the transcoder cannot be started
     */
    public static FfmpegRtpTranscoder opusToAacEld(String ffmpeg, AudioCodec codec, int payloadType)
            throws IOException {
        return new FfmpegRtpTranscoder(ffmpeg, port -> FfmpegHelpers.opusInputSdp(port, payloadType), List.of(),
                FfmpegHelpers.aacEldEncoderArgs(codec), payloadType);
    }

    /**
     * Sends one input RTP packet to ffmpeg.
     *
     * @param data the buffer holding the RTP packet
     * @param length the packet length
     * @throws IOException if the packet cannot be sent
     */
    @Override
    public void accept(byte[] data, int length) throws IOException {
        // go2rtc omits some browser RTP packets after their sequence number was assigned. ffmpeg treats the resulting
        // hole as loss, waits for the missing packet, and logs "RTP: missed". Renumber in arrival order so those
        // holes are not loss. Timestamps are left unchanged.
        nextSequence = rewriteSequence(data, length, nextSequence);
        if (!firstPacketSent) {
            firstPacketSent = true;
            LOGGER.debug("ffmpeg input: first RTP packet forwarded, {} bytes to port {}", length, inputPort);
        }
        inputSocket.send(new DatagramPacket(data, length, loopback, inputPort));
    }

    /**
     * Writes a contiguous RTP sequence number into the packet and returns the next number to use.
     *
     * @param data the buffer holding the RTP packet
     * @param length the packet length
     * @param next the next sequence number, or -1 to keep the packet's own number
     * @return the sequence number for the following packet, or {@code next} when the packet has no sequence field
     */
    static int rewriteSequence(byte[] data, int length, int next) {
        if (length < 4) {
            return next;
        }
        int sequence = next < 0 ? ((data[2] & 0xFF) << 8) | (data[3] & 0xFF) : next;
        data[2] = (byte) (sequence >>> 8);
        data[3] = (byte) sequence;
        return (sequence + 1) & 0xFFFF;
    }

    private void logOutput(Process p) {
        try (BufferedReader reader = p.inputReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    LOGGER.debug("ffmpeg: {}", line);
                }
            }
        } catch (IOException e) {
            // close() destroys ffmpeg, which closes this stream. That is a normal talk-session end, not a failure.
            if (!closed) {
                LOGGER.debug("ffmpeg output closed: {}", e.getMessage());
            }
        }
        try {
            int code = p.waitFor();
            if (!closed) {
                LOGGER.debug("ffmpeg exited with code {}", code);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void forwardOutput(RtpPacketSink sink) {
        byte[] buffer = new byte[MAX_DATAGRAM_SIZE];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        while (!closed) {
            try {
                packet.setLength(buffer.length);
                outputSocket.receive(packet);
                sink.accept(buffer, packet.getLength());
            } catch (IOException e) {
                if (!closed) {
                    LOGGER.debug("RTP transcoding output failed: {}", e.getMessage());
                }
                return;
            }
        }
    }

    @Override
    public void close() {
        closed = true;
        inputSocket.close();
        outputSocket.close();
        process.destroy();
        // destroy() is asynchronous and Windows may keep the SDP file locked briefly after ffmpeg exits, so retry
        process.onExit().thenRun(() -> {
            for (int attempt = 1; attempt <= SDP_DELETE_ATTEMPTS; attempt++) {
                try {
                    Files.deleteIfExists(sdpFile);
                    return;
                } catch (IOException e) {
                    if (attempt == SDP_DELETE_ATTEMPTS) {
                        LOGGER.debug("Cannot delete transcoder SDP file {}: {}", sdpFile, e.getMessage());
                    } else {
                        try {
                            Thread.sleep(SDP_DELETE_RETRY_MS);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                }
            }
        });
    }

    /**
     * Starts ffmpeg, retrying with a fresh input port pair if ffmpeg exits during startup because it could not bind
     * its ports. ffmpeg binds its RTP and RTCP input ports itself, so a port that was free when probed may already be
     * in use by then.
     */
    private static Launch launch(String ffmpeg, IntFunction<String> inputSdp, List<String> inputArgs,
            List<String> encoderArgs, int payloadType, int outputPort) throws IOException {
        for (int attempt = 1;; attempt++) {
            int inputPort = freeRtpPortPair();
            Path sdp = Files.createTempFile("homekit-rtp-transcoder-", ".sdp");
            boolean started = false;
            String failure;
            try {
                Files.writeString(sdp, inputSdp.apply(inputPort));
                Process process = new ProcessBuilder(FfmpegHelpers.transcodeCommandLine(ffmpeg, sdp.toString(),
                        outputPort, inputArgs, encoderArgs, payloadType)).redirectErrorStream(true).start();
                if (!exitedDuringStartup(process)) {
                    started = true;
                    return new Launch(inputPort, sdp, process);
                }
                String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                failure = "exit code " + process.exitValue() + ": " + output.strip();
            } finally {
                if (!started) {
                    Files.deleteIfExists(sdp);
                }
            }
            if (attempt == MAX_START_ATTEMPTS) {
                throw new IOException("ffmpeg failed to start after " + attempt + " attempts, " + failure);
            }
            LOGGER.debug("ffmpeg failed to start on RTP port {} (attempt {} of {}), retrying: {}", inputPort, attempt,
                    MAX_START_ATTEMPTS, failure);
        }
    }

    /**
     * Waits up to {@link #STARTUP_CHECK_MS} for ffmpeg to exit, which only happens when it fails to start.
     */
    private static boolean exitedDuringStartup(Process process) throws IOException {
        try {
            return process.waitFor(STARTUP_CHECK_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
            throw new IOException("Interrupted while starting ffmpeg", e);
        }
    }

    /**
     * Returns a UDP port whose successor is also free, so that ffmpeg can bind both its RTP port and its RTCP port
     * (port + 1) on the wildcard address. ffmpeg binds these itself, so another process could take them first; the
     * caller accepts that small risk.
     */
    private static int freeRtpPortPair() throws IOException {
        for (int attempt = 0; attempt < 20; attempt++) {
            int port;
            try (DatagramSocket probe = new DatagramSocket(0)) {
                port = probe.getLocalPort();
            }
            if (port < 65535) {
                try (DatagramSocket rtcpProbe = new DatagramSocket(port + 1)) {
                    return port;
                } catch (IOException e) {
                    // RTCP port taken, try another pair
                }
            }
        }
        throw new IOException("No free RTP/RTCP UDP port pair available");
    }
}
