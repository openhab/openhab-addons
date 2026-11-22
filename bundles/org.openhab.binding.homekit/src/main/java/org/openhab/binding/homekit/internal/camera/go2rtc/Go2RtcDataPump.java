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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.camera.codec.AudioCodec;
import org.openhab.binding.homekit.internal.camera.codec.VideoCodec;
import org.openhab.binding.homekit.internal.camera.ffmpeg.FfmpegRtpTranscoder;
import org.openhab.binding.homekit.internal.camera.session.RtcpReceiverReport;
import org.openhab.binding.homekit.internal.camera.session.RtpPacketSink;
import org.openhab.binding.homekit.internal.camera.session.RtpPacketSource;
import org.openhab.binding.homekit.internal.camera.session.RtspConnection;
import org.openhab.binding.homekit.internal.camera.session.SrtpParameters;
import org.openhab.binding.homekit.internal.camera.session.SrtpSession;
import org.openhab.binding.homekit.internal.enums.AudioCodecType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Relays media between the camera and go2rtc over RTSP.
 * <p>
 * Camera forward path audio and video arrive as SRTP on UDP ports advertised in the SetupEndpoints request.
 * They are decrypted and published as interleaved RTP to go2rtc's listen stream. AAC-ELD audio is first transcoded to
 * Opus by ffmpeg, because go2rtc only accepts Opus.
 * <p>
 * The talk path is read as interleaved RTP from go2rtc's talk stream, re-labelled with the talk payload type and
 * sync source, encrypted and sent to the camera's audio port advertised in the SetupEndpoints response, using the
 * same local socket that receives camera audio. Audio RTP and RTCP share this full-duplex endpoint. When the camera's
 * audio codec is AAC-ELD, the talk audio is first transcoded from Opus to AAC-ELD by ffmpeg.
 * The SRTP receivers and talk sender form the edges of the plain RTP callback chains. Receiver Reports are generated
 * separately from statistics collected before any transcoding. The adapters, statistics and callbacks belong to one
 * camera session and are not reused when that session is replaced.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class Go2RtcDataPump implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Go2RtcDataPump.class);

    private static final String RTSP_HOST = "127.0.0.1";
    private static final int MAX_DATAGRAM_SIZE = 2048;
    private static final int RTP_HEADER_LENGTH = 12;
    private static final int SRTP_AUTH_TAG_LENGTH = 10;
    // RFC 3550 RTCP packet types 200 (SR) to 204 (APP)
    private static final int RTCP_SENDER_REPORT = 200;
    private static final int RTCP_APP = 204;
    private static final int SRTCP_TRAILER_LENGTH = 4 + SRTP_AUTH_TAG_LENGTH;
    private static final long RTCP_REPORT_INTERVAL_MS = 1000L;
    private static final int VIDEO_CLOCK_RATE = 90000;
    private static final int TALK_RETRY_DELAY_MS = 2000;
    private static final int PUBLISH_TIMEOUT_SECONDS = 60;

    public static final int AUDIO_PAYLOAD_TYPE = 110;
    public static final int VIDEO_PAYLOAD_TYPE = 99;

    // sync sources are uint which Java doesn't have so we use 4 byte long (8 hex digits)
    public static final long AUDIO_SYNC_SOURCE = 0x12345678L;
    public static final long VIDEO_SYNC_SOURCE = 0x23456789L;

    // interleaved RTP channels; the RTCP channel is always the RTP channel plus one.
    private static final int AUDIO_CHANNEL = 0;
    private static final int VIDEO_CHANNEL = 2;
    private static final int TALK_CHANNEL = 0;

    /**
     * Opus RTP timestamps always use a 48 kHz clock regardless of the output sample rate (RFC 7587).
     */
    private static final String ANNOUNCE_SDP = """
            v=0
            o=- 0 0 IN IP4 127.0.0.1
            s=HomeKit
            c=IN IP4 127.0.0.1
            t=0 0
            m=audio 0 RTP/AVP %d
            a=rtpmap:%d opus/48000/2
            a=control:trackID=0
            m=video 0 RTP/AVP %d
            a=rtpmap:%d H264/90000
            a=fmtp:%d packetization-mode=1
            a=control:trackID=1
            """.formatted(AUDIO_PAYLOAD_TYPE, AUDIO_PAYLOAD_TYPE, VIDEO_PAYLOAD_TYPE, VIDEO_PAYLOAD_TYPE,
            VIDEO_PAYLOAD_TYPE);

    /**
     * The SRTP contexts of one camera session. The camera's inbound contexts are used only by their relay thread. The
     * outbound contexts encrypt talk audio and Receiver Reports, and are shared between threads.
     */
    public record Session(SrtpSession audioSession, SrtpSession videoSession, SrtpSession talkSession,
            SrtpSession videoControlSession, InetSocketAddress talkAddress) {
    }

    private static final class RelaySource implements RtpPacketSource {
        private RtpPacketSink downstream;

        private RelaySource(RtpPacketSink downstream) {
            this.downstream = downstream;
        }

        @Override
        public void connectTo(RtpPacketSink downstream) {
            this.downstream = downstream;
        }

        private void push(byte[] packet, int length) throws IOException {
            downstream.accept(packet, length);
        }
    }

    private final class SrtpReceiver implements RtpPacketSource {
        private final SessionState owner;
        private final SrtpSession crypto;
        private final RtcpReceiverReport report;
        private RtpPacketSink downstream;

        private SrtpReceiver(SessionState owner, SrtpSession crypto, RtcpReceiverReport report, int channel) {
            this.owner = owner;
            this.crypto = crypto;
            this.report = report;
            downstream = (data, length) -> publishPacket(owner, channel, data, length);
        }

        @Override
        public void connectTo(RtpPacketSink downstream) {
            this.downstream = downstream;
        }

        private void receive(DatagramPacket datagram, byte[] rtpBuffer) throws IOException {
            byte[] data = datagram.getData();
            int length = datagram.getLength();
            if (isRtcp(data, length)) {
                return;
            }
            int rtpLength = crypto.decrypt(data, length, rtpBuffer);
            if (rtpLength >= 0 && Objects.equals(session, owner)) {
                report.setPeer(datagram.getSocketAddress());
                report.onRtp(rtpBuffer, rtpLength, nowMicros());
                downstream.accept(rtpBuffer, rtpLength);
            }
        }
    }

    private final class SrtpSender implements RtpPacketSink {
        private final SessionState owner;
        private final byte[] srtpBuffer = new byte[MAX_DATAGRAM_SIZE + SRTP_AUTH_TAG_LENGTH];

        private SrtpSender(SessionState owner) {
            this.owner = owner;
        }

        @Override
        public void accept(byte[] packet, int length) throws IOException {
            setPayloadType(packet);
            // Header bytes 8-11 hold the SSRC in network byte order.
            packet[8] = (byte) (AUDIO_SYNC_SOURCE >>> 24);
            packet[9] = (byte) (AUDIO_SYNC_SOURCE >>> 16);
            packet[10] = (byte) (AUDIO_SYNC_SOURCE >>> 8);
            packet[11] = (byte) AUDIO_SYNC_SOURCE;
            int srtpLength = owner.crypto.talkSession().encrypt(packet, length, srtpBuffer);
            synchronized (sessionMonitor) {
                if (Objects.equals(session, owner)) {
                    audioSocket.send(new DatagramPacket(srtpBuffer, srtpLength, owner.crypto.talkAddress()));
                }
            }
        }
    }

    private static final class SessionState {
        private final Session crypto;
        private final RtcpReceiverReport audioReport;
        private final RtcpReceiverReport videoReport;
        private final SrtpReceiver audioSource;
        private final SrtpReceiver videoSource;
        private @Nullable FfmpegRtpTranscoder audioTranscoder;

        private SessionState(Session crypto, int audioClockRate, Go2RtcDataPump pump) {
            this.crypto = crypto;
            audioReport = new RtcpReceiverReport(AUDIO_SYNC_SOURCE, audioClockRate);
            videoReport = new RtcpReceiverReport(VIDEO_SYNC_SOURCE, VIDEO_CLOCK_RATE);
            audioSource = pump.new SrtpReceiver(this, crypto.audioSession(), audioReport, AUDIO_CHANNEL);
            videoSource = pump.new SrtpReceiver(this, crypto.videoSession(), videoReport, VIDEO_CHANNEL);
        }
    }

    private final AudioCodec audioCodec;
    private final VideoCodec videoCodec;
    private final @Nullable String ffmpegBinary;
    private final DatagramSocket audioSocket;
    private final DatagramSocket videoSocket;
    // Serializes session changes with UDP sends, but never covers blocking receive or RTSP I/O.
    private final Object sessionMonitor = new Object();

    private volatile @Nullable SessionState session = null;
    private volatile @Nullable RtspConnection publisher = null;
    private volatile @Nullable String publishUrl = null;
    private volatile @Nullable RtspConnection talkReader = null;
    private volatile boolean closed;
    private final Object talkMonitor = new Object();
    private boolean talkActive; // guarded by talkMonitor

    /**
     * Creates a data pump for the camera's audio and video. The camera must support Opus or AAC-ELD audio and H.264
     * video.
     *
     * @param audioCodecs the audio codecs the camera supports
     * @param videoCodecs the video configurations the camera supports
     * @param ffmpegBinary the ffmpeg executable, required only when the camera's audio is AAC-ELD; otherwise may be
     *            null
     * @throws IllegalArgumentException if the camera does not support Opus or AAC-ELD audio or H.264 video, or if
     *             AAC-ELD is used without an ffmpeg executable
     * @throws IOException if the sockets cannot be created
     */
    public Go2RtcDataPump(List<AudioCodec> audioCodecs, List<VideoCodec> videoCodecs, @Nullable String ffmpegBinary)
            throws IOException {
        audioCodec = AudioCodec.preferred(audioCodecs);
        videoCodec = VideoCodec.preferred(videoCodecs);
        if (audioCodec.codecType() == AudioCodecType.AAC_ELD && ffmpegBinary == null) {
            throw new IllegalArgumentException("ffmpeg is required to transcode AAC-ELD audio");
        }
        this.ffmpegBinary = ffmpegBinary;

        audioSocket = new DatagramSocket(0);
        try {
            videoSocket = new DatagramSocket(0);
        } catch (IOException | RuntimeException e) {
            audioSocket.close();
            throw e;
        }

        startThread("audio-rtp", () -> relayListen(audioSocket, AUDIO_CHANNEL, state -> state.audioSource));
        startThread("video-rtp", () -> relayListen(videoSocket, VIDEO_CHANNEL, state -> state.videoSource));
        startThread("rtcp-rr", this::sendReceiverReports);
    }

    /**
     * @return the camera's audio codec
     */
    public AudioCodec audioCodec() {
        return audioCodec;
    }

    /**
     * @return the camera's video codec
     */
    public VideoCodec videoCodec() {
        return videoCodec;
    }

    /**
     * @return the full-duplex UDP port used for camera audio SRTP and SRTCP in both directions
     */
    public int audioPort() {
        return audioSocket.getLocalPort();
    }

    /**
     * @return the UDP port that receives the camera's video SRTP
     */
    public int videoPort() {
        return videoSocket.getLocalPort();
    }

    /**
     * Publishes the camera media to go2rtc and starts relaying the talk stream to the camera. The talk stream is
     * retried until go2rtc has it available.
     *
     * @param rtspPort the go2rtc RTSP port on the loopback interface
     * @param listenStream the name of the go2rtc stream that receives the camera media
     * @param talkStream the name of the go2rtc stream that carries the browser talk audio
     * @throws IOException if go2rtc refuses the publish
     * @throws IllegalStateException if already connected or closed
     */
    public synchronized void connect(int rtspPort, String listenStream, String talkStream) throws IOException {
        if (closed || publisher != null) {
            throw new IllegalStateException("data pump is already connected or closed");
        }
        RtspConnection connection = new RtspConnection(RTSP_HOST, rtspPort);
        String url = url(rtspPort, listenStream);
        try {
            publish(connection, url);
        } catch (IOException | RuntimeException e) {
            connection.close();
            throw e;
        }
        publishUrl = url;
        publisher = connection;
        startThread("talk-rtp", () -> relayTalk(rtspPort, talkStream));
    }

    private void publishPacket(SessionState current, int channel, byte[] data, int length) throws IOException {
        RtspConnection connection = publisher;
        if (Objects.equals(session, current) && connection != null) {
            connection.sendFrame(channel, data, length);
        }
    }

    /**
     * Starts relaying media for a camera session, replacing any session already active.
     *
     * @param audioSrtpParameters the SRTP parameters the camera uses to encrypt its audio
     * @param videoSrtpParameters the SRTP parameters the camera uses to encrypt its video
     * @param talkParameters the SRTP parameters the binding uses to encrypt talk audio towards the camera
     * @param videoControlParameters the SRTP parameters the binding uses to encrypt video Receiver Reports towards the
     *            camera
     * @param remoteHost the camera's IP address
     * @param remoteTalkPort the camera's audio port from the SetupEndpoints response
     * @return the new session
     * @throws IllegalArgumentException if a crypto suite is NONE
     * @throws IllegalStateException if the data pump is closed
     */
    public synchronized Session startSession(SrtpParameters audioParameters, SrtpParameters videoParameters,
            SrtpParameters talkParameters, SrtpParameters videoControlParameters, String remoteHost,
            int remoteTalkPort) {
        if (closed) {
            throw new IllegalStateException("data pump is closed");
        }
        Session newSession = new Session(SrtpSession.create(audioParameters), SrtpSession.create(videoParameters),
                SrtpSession.create(talkParameters), SrtpSession.create(videoControlParameters),
                new InetSocketAddress(remoteHost, remoteTalkPort));
        SessionState previous;
        synchronized (sessionMonitor) {
            previous = session;
            session = null;
        }
        stopAudioTranscoder(previous);
        stopTalk();
        SessionState state = new SessionState(newSession, audioCodec.rtpClockRate(), this);
        startAudioTranscoder(state);
        synchronized (sessionMonitor) {
            session = state;
        }
        return newSession;
    }

    /**
     * Stops relaying media. Packets received afterwards are dropped, and the browser talk relay is stopped.
     */
    public synchronized void stopSession() {
        LOGGER.debug("Stopping go2rtc relay session: publisherActive={}", publisher != null);
        SessionState previous;
        synchronized (sessionMonitor) {
            previous = session;
            session = null;
        }
        stopAudioTranscoder(previous);
        stopTalk();
    }

    /**
     * Starts a fresh AAC-ELD transcoder for the session, since ffmpeg exits when its input is idle.
     */
    private void startAudioTranscoder(SessionState current) {
        String ffmpeg = ffmpegBinary;
        if (publisher == null || audioCodec.codecType() != AudioCodecType.AAC_ELD || ffmpeg == null) {
            return;
        }
        try {
            FfmpegRtpTranscoder transcoder = FfmpegRtpTranscoder.aacEldToOpus(ffmpeg, audioCodec, AUDIO_PAYLOAD_TYPE);
            transcoder.connectTo(current.audioSource.downstream);
            current.audioSource.connectTo(transcoder);
            current.audioTranscoder = transcoder;
        } catch (IOException e) {
            LOGGER.warn("Failed to start audio transcoder: {}", e.getMessage());
        }
    }

    private void stopAudioTranscoder(@Nullable SessionState current) {
        if (current == null) {
            return;
        }
        FfmpegRtpTranscoder transcoder = current.audioTranscoder;
        current.audioTranscoder = null;
        if (transcoder != null) {
            transcoder.close();
        }
    }

    /**
     * Sends an RTSP OPTIONS request on the publisher connection. go2rtc drops an idle publisher after its timeout, and
     * each request it receives resets that timeout. The response is read here, so it does not accumulate.
     *
     * @throws IOException if the publisher connection fails
     */
    public void keepAlive() throws IOException {
        RtspConnection connection = publisher;
        String url = publishUrl;
        if (connection != null && url != null) {
            connection.request("OPTIONS", url, "", null);
        }
    }

    /**
     * Starts reading the browser talk stream from go2rtc and relaying it to the camera. Called when a browser talk
     * session begins; until then the talk relay stays idle and does not poll go2rtc.
     */
    public void startTalk() {
        synchronized (talkMonitor) {
            if (!talkActive) {
                LOGGER.debug("Talk-back session started");
            }
            talkActive = true;
            talkMonitor.notifyAll();
        }
    }

    /**
     * Stops relaying the browser talk stream, closing the go2rtc talk reader.
     */
    public void stopTalk() {
        synchronized (talkMonitor) {
            if (talkActive) {
                LOGGER.debug("Talk-back session stopped");
            }
            talkActive = false;
            talkMonitor.notifyAll();
        }
        RtspConnection reader = talkReader;
        if (reader != null) {
            reader.close();
        }
    }

    @Override
    public synchronized void close() {
        closed = true;
        SessionState previous;
        synchronized (sessionMonitor) {
            previous = session;
            session = null;
        }
        audioSocket.close();
        videoSocket.close();
        stopAudioTranscoder(previous);
        RtspConnection connection = publisher;
        if (connection != null) {
            connection.close();
        }
        stopTalk();
    }

    /**
     * Decrypts camera audio or video SRTP and publishes it as interleaved RTP to go2rtc. AAC-ELD audio is passed to the
     * transcoder instead, which publishes Opus. Camera SRTCP on the same socket is dropped and not decrypted. The
     * camera's address is learned from the first RTP packet that authenticates, and Receiver Reports are sent to it.
     */
    private void relayListen(DatagramSocket socket, int channel, Function<SessionState, SrtpReceiver> source) {
        byte[] srtpBuffer = new byte[MAX_DATAGRAM_SIZE];
        byte[] rtpBuffer = new byte[MAX_DATAGRAM_SIZE];
        DatagramPacket receivePacket = new DatagramPacket(srtpBuffer, srtpBuffer.length);
        while (!closed) {
            try {
                receivePacket.setLength(srtpBuffer.length);
                socket.receive(receivePacket);
                SessionState current = session;
                if (current != null && publisher != null) {
                    source.apply(current).receive(receivePacket, rtpBuffer);
                }
            } catch (IOException e) {
                if (closed) {
                    return;
                }
                LOGGER.debug(
                        "go2rtc RTP relay failed on channel {} ({}: {}) socketClosed={} sessionActive={} publisherActive={}",
                        channel, e.getClass().getSimpleName(), e.getMessage(), socket.isClosed(), session != null,
                        publisher != null);
            }
        }
    }

    /**
     * Reads the browser talk stream from go2rtc and sends it as SRTP to the camera. Idles until a talk session starts,
     * and reconnects after failures while the talk session remains active.
     */
    private void relayTalk(int rtspPort, String talkStream) {
        byte[] rtpBuffer = new byte[MAX_DATAGRAM_SIZE];
        while (awaitTalkActive()) {
            try {
                relayTalkSession(rtspPort, talkStream, rtpBuffer);
            } catch (IOException e) {
                if (closed) {
                    return;
                }
                LOGGER.debug("Talk-back unavailable: {}", e.getMessage());
                if (!pauseBeforeTalkRetry()) {
                    return;
                }
            }
        }
    }

    /**
     * Relays one talk session, reconnecting to go2rtc while the session stays active. The talk transcoder, if any,
     * lives
     * for the whole session.
     */
    private void relayTalkSession(int rtspPort, String talkStream, byte[] rtpBuffer) throws IOException {
        SessionState current = session;
        if (current == null) {
            return;
        }
        SrtpSender sender = new SrtpSender(current);
        RelaySource source = new RelaySource(sender);
        try (FfmpegRtpTranscoder encoder = openTalkEncoder()) {
            if (encoder != null) {
                encoder.connectTo(sender);
                source.connectTo(encoder);
            }
            while (!closed && Objects.equals(session, current) && isTalkActive()) {
                try (RtspConnection reader = openTalkReader(rtspPort, talkStream)) {
                    LOGGER.debug("Talk-back reading go2rtc stream {}", talkStream);
                    synchronized (sessionMonitor) {
                        if (!Objects.equals(session, current) || !isTalkActive()) {
                            return;
                        }
                        talkReader = reader;
                    }
                    while (!closed && Objects.equals(session, current) && isTalkActive()) {
                        RtspConnection.Frame frame = reader.readFrame(rtpBuffer);
                        if (!Objects.equals(session, current) || frame.channel() != TALK_CHANNEL
                                || frame.length() < RTP_HEADER_LENGTH) {
                            continue;
                        }
                        // Use the payload type expected by both the FFmpeg input SDP and the camera.
                        setPayloadType(rtpBuffer);
                        source.push(rtpBuffer, frame.length());
                    }
                } catch (IOException e) {
                    if (closed || !Objects.equals(session, current)) {
                        return;
                    }
                    // a reader closed by stopTalk() is expected and not a failure
                    if (isTalkActive()) {
                        LOGGER.debug("go2rtc talk stream unavailable: {}", e.getMessage());
                        if (!pauseBeforeTalkRetry()) {
                            return;
                        }
                    }
                } finally {
                    talkReader = null;
                }
            }
        }
    }

    /**
     * Creates the Opus to AAC-ELD transcoder for a talk session. Returns null when the camera takes Opus directly.
     */
    private @Nullable FfmpegRtpTranscoder openTalkEncoder() throws IOException {
        if (audioCodec.codecType() != AudioCodecType.AAC_ELD) {
            return null;
        }
        String ffmpeg = ffmpegBinary;
        if (ffmpeg == null) {
            throw new IOException("No ffmpeg binary available for AAC-ELD talk-back");
        }
        return FfmpegRtpTranscoder.opusToAacEld(ffmpeg, audioCodec, AUDIO_PAYLOAD_TYPE);
    }

    /**
     * Blocks until a talk session is active or the data pump is closed.
     *
     * @return true if a talk session is active, false if the data pump is closed or the thread was interrupted
     */
    private boolean awaitTalkActive() {
        synchronized (talkMonitor) {
            while ((!talkActive || session == null) && !closed) {
                try {
                    talkMonitor.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
            return talkActive && !closed;
        }
    }

    private boolean isTalkActive() {
        synchronized (talkMonitor) {
            return talkActive;
        }
    }

    /**
     * @return false if the thread was interrupted
     */
    private static boolean pauseBeforeTalkRetry() {
        try {
            Thread.sleep(TALK_RETRY_DELAY_MS);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private static void setPayloadType(byte[] rtpBuffer) {
        // keep marker bit (bit 7) and replace payload type
        rtpBuffer[1] = (byte) ((rtpBuffer[1] & 0x80) | AUDIO_PAYLOAD_TYPE);
    }

    /**
     * Connects a reader to the go2rtc talk stream, and starts playback of its audio on interleaved channel 0.
     */
    private static RtspConnection openTalkReader(int rtspPort, String talkStream) throws IOException {
        String url = url(rtspPort, talkStream);
        RtspConnection reader = new RtspConnection(RTSP_HOST, rtspPort);
        try {
            String sdp = reader.request("DESCRIBE", url, "Accept: application/sdp\r\n", null).body();
            reader.request("SETUP", trackUrl(url, sdp), "Transport: RTP/AVP/TCP;unicast;interleaved=0-1\r\n", null);
            reader.request("PLAY", url, "", null);
            reader.disableReadTimeout();
            return reader;
        } catch (IOException | RuntimeException e) {
            reader.close();
            throw e;
        }
    }

    /**
     * Sends the publish sequence (ANNOUNCE, SETUP per track, RECORD) that makes go2rtc accept the camera media.
     */
    private static void publish(RtspConnection connection, String url) throws IOException {
        connection.request("ANNOUNCE", url + "?timeout=" + PUBLISH_TIMEOUT_SECONDS, "Content-Type: application/sdp\r\n",
                ANNOUNCE_SDP);
        connection.request("SETUP", url + "/trackID=0",
                "Transport: RTP/AVP/TCP;unicast;mode=record;interleaved=0-1\r\n", null);
        connection.request("SETUP", url + "/trackID=1",
                "Transport: RTP/AVP/TCP;unicast;mode=record;interleaved=2-3\r\n", null);
        connection.request("RECORD", url, "", null);
    }

    /**
     * Returns the URL of the first media track in the SDP, falling back to the stream URL.
     */
    private static String trackUrl(String url, String sdp) {
        boolean inMedia = false;
        for (String line : sdp.split("\r?\n")) {
            if (line.startsWith("m=")) {
                inMedia = true;
            } else if (inMedia && line.startsWith("a=control:")) {
                String control = line.substring("a=control:".length()).trim();
                return "*".equals(control) ? url : url + "/" + control;
            }
        }
        return url;
    }

    private static String url(int rtspPort, String stream) {
        return "rtsp://%s:%d/%s".formatted(RTSP_HOST, rtspPort, stream);
    }

    /**
     * Sends a Receiver Report for each stream once per interval while a camera session is active. The audio report uses
     * the talk SRTP context, and the video report uses the video control context.
     */
    private void sendReceiverReports() {
        while (!closed) {
            try {
                Thread.sleep(RTCP_REPORT_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            SessionState current = session;
            if (current != null) {
                sendReceiverReport(current, audioSocket, current.audioReport, current.crypto.talkSession());
                sendReceiverReport(current, videoSocket, current.videoReport, current.crypto.videoControlSession());
            }
        }
    }

    /**
     * Builds, encrypts and sends one Receiver Report to the camera address learned from its media. Nothing is sent
     * until the camera's media has been authenticated.
     */
    private void sendReceiverReport(SessionState current, DatagramSocket socket, RtcpReceiverReport report,
            SrtpSession control) {
        SocketAddress peer = report.peer();
        if (peer == null) {
            return;
        }
        byte[] rr = report.buildReport();
        byte[] srtcp = new byte[rr.length + SRTCP_TRAILER_LENGTH];
        try {
            int length = control.encryptRtcp(rr, rr.length, srtcp);
            synchronized (sessionMonitor) {
                if (Objects.equals(session, current)) {
                    socket.send(new DatagramPacket(srtcp, length, peer));
                }
            }
        } catch (IOException e) {
            if (!closed) {
                LOGGER.debug("Receiver Report send failed ({}: {})", e.getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    /**
     * RFC 5761 demultiplexing: RTCP packet types 200-204 appear in the second byte, where RTP carries the marker bit
     * and payload type. The payload types used here do not collide with that range. The first eight bytes of SRTCP
     * are not encrypted, so the type is readable before decryption.
     */
    private static boolean isRtcp(byte[] packet, int length) {
        if (length < 2) {
            return false;
        }
        int packetType = packet[1] & 0xFF;
        return packetType >= RTCP_SENDER_REPORT && packetType <= RTCP_APP;
    }

    private static long nowMicros() {
        return System.nanoTime() / 1000L;
    }

    /**
     * Starts a daemon thread to run the given task.
     */
    private void startThread(String name, Runnable task) {
        Thread thread = new Thread(task, "OH-binding-homekit-go2rtc-" + name);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public String toString() {
        return "Go2RtcDataPump [session=" + session + ", closed=" + closed + ", audioCodec=" + audioCodec
                + ", videoCodec=" + videoCodec + ", audioPort=" + audioPort() + ", videoPort=" + videoPort() + "]";
    }
}
