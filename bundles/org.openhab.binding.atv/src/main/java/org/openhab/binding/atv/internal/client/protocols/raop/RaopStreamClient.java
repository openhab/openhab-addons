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
package org.openhab.binding.atv.internal.client.protocols.raop;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Clock;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.LongSupplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.dto.MediaMetadata;
import org.openhab.binding.atv.internal.client.exceptions.AuthenticationError;
import org.openhab.binding.atv.internal.client.exceptions.InvalidStateError;
import org.openhab.binding.atv.internal.client.exceptions.ProtocolError;
import org.openhab.binding.atv.internal.client.protocols.raop.RaopPackets.AudioPacketHeader;
import org.openhab.binding.atv.internal.client.protocols.raop.RaopParsers.EncryptionType;
import org.openhab.binding.atv.internal.client.protocols.raop.RaopParsers.MetadataType;
import org.openhab.binding.atv.internal.client.settings.RaopSettings;
import org.openhab.binding.atv.internal.client.support.DmapTags;
import org.openhab.binding.atv.internal.client.support.http.RtspSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client for AirPlay audio streaming.
 *
 * <p>
 * Generic across AirPlay versions; delegates protocol specific bits (v1/v2) to a
 * {@link StreamProtocol}.
 *
 * <p>
 * Threading: the whole data plane is blocking code intended to run on a dedicated
 * virtual thread (the {@code RaopStream.streamFile} thread); sync packets and protocol
 * keep-alives run on their own virtual threads. Pacing uses absolute time against the
 * injectable monotonic clock of {@link StreamTiming}: the sender parks until
 * {@code playbackStart + totalFrames * 1e9 / sampleRate}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class RaopStreamClient {

    /** When being late, compensate by sending at most these many packets to catch up. */
    public static final int MAX_PACKETS_COMPENSATE = 3;

    /** Number of packets stored in case retransmission is requested. */
    public static final int PACKET_BACKLOG_SIZE = 1000;

    /**
     * Metadata instance used when there is no metadata.
     */
    public static final MediaMetadata EMPTY_METADATA = new MediaMetadata(null, null, null, null, null);

    /**
     * Metadata shown to the receiver when nothing else is available.
     */
    public static final MediaMetadata MISSING_METADATA = new MediaMetadata("Streaming with openHAB", "openHAB",
            "AirPlay", null, null);

    private static final int SLOW_WARNING_THRESHOLD = 5;

    private static final Logger LOGGER = LoggerFactory.getLogger(RaopStreamClient.class);

    private final RtspSession rtsp;
    private final StreamContext context;
    private final StreamProtocol protocol;
    private final RaopSettings settings;
    private final StreamTiming timing;
    private final Clock clock;
    private final Random rng;

    private @Nullable ControlServer controlServer;
    private @Nullable TimingServer timingServer;
    private EnumSet<EncryptionType> encryptionTypes = EnumSet.noneOf(EncryptionType.class);
    private EnumSet<MetadataType> metadataTypes = EnumSet.noneOf(MetadataType.class);
    private MediaMetadata metadata = EMPTY_METADATA;
    private Map<String, String> properties = Map.of();
    private final Map<String, Object> info = new LinkedHashMap<>();

    private volatile @Nullable RaopListener listener;
    private volatile boolean isPlaying;
    private volatile @Nullable Thread syncThread;

    /**
     * Creates a new stream client.
     *
     * @param rtsp RTSP session to the receiver
     * @param context shared stream context
     * @param protocol AirPlay version specific protocol logic
     * @param settings RAOP settings (local control/timing ports)
     * @param timing timing knobs (injectable for tests)
     * @param clock wall clock used for NTP timestamps
     * @param rng randomness for the initial sequence number (injectable for tests)
     */
    public RaopStreamClient(RtspSession rtsp, StreamContext context, StreamProtocol protocol, RaopSettings settings,
            StreamTiming timing, Clock clock, Random rng) {
        this.rtsp = rtsp;
        this.context = context;
        this.protocol = protocol;
        this.settings = settings;
        this.timing = timing;
        this.clock = clock;
        this.rng = rng;
    }

    /**
     * Sets the listener notified about playback state changes (may be {@code null}).
     */
    public void setListener(@Nullable RaopListener listener) {
        this.listener = listener;
    }

    /**
     * Returns the current playback information.
     */
    public PlaybackInfo playbackInfo() {
        MediaMetadata current = EMPTY_METADATA.equals(metadata) ? MISSING_METADATA : metadata;
        return new PlaybackInfo(current, context.position());
    }

    /**
     * Returns value mappings for the receiver's {@code /info} values.
     */
    public Map<String, Object> info() {
        return info;
    }

    /**
     * Returns the shared stream context.
     */
    public StreamContext context() {
        return context;
    }

    /**
     * Closes the session and frees up resources.
     */
    public void close() {
        protocol.teardown();
        stopSync();
        ControlServer control = controlServer;
        if (control != null) {
            control.close();
        }
        TimingServer timingSrv = timingServer;
        if (timingSrv != null) {
            timingSrv.close();
        }
    }

    /**
     * Initializes the session: parses receiver properties, binds the local control and
     * timing endpoints, fetches {@code /info}, performs auth-setup when needed and runs
     * the protocol setup.
     *
     * @param properties Zeroconf properties of the RAOP service
     */
    public void initialize(Map<String, String> properties) {
        this.properties = properties;
        this.encryptionTypes = RaopParsers.getEncryptionTypes(properties);
        this.metadataTypes = RaopParsers.getMetadataTypes(properties);

        LOGGER.debug("Initializing RTSP with encryption={}, metadata={}", encryptionTypes, metadataTypes);

        // This only logs; streaming continues regardless of the encryption check result
        EnumSet<EncryptionType> intersection = EnumSet.copyOf(encryptionTypes);
        intersection.retainAll(RaopParsers.SUPPORTED_ENCRYPTIONS);
        if (intersection.isEmpty()) {
            LOGGER.debug("No supported encryption type, continuing anyway");
        }

        RaopParsers.AudioProperties audioProperties = RaopParsers.getAudioProperties(properties);
        context.sampleRate = audioProperties.sampleRate();
        context.channels = audioProperties.channels();
        context.bytesPerChannel = audioProperties.sampleSize();
        LOGGER.debug("Update play settings to {}/{}/{}bit", context.sampleRate, context.channels,
                context.bytesPerChannel * 8);

        ControlServer control;
        TimingServer timingSrv;
        try {
            control = new ControlServer(new PacketFifo<>(PACKET_BACKLOG_SIZE), settings.controlPort(), null);
            timingSrv = new TimingServer(clock, settings.timingPort(), null);
        } catch (IOException e) {
            throw new ProtocolError("failed to bind local UDP endpoints", e);
        }
        controlServer = control;
        timingServer = timingSrv;

        LOGGER.debug("Local ports: control={}, timing={}", control.port(), timingSrv.port());

        info.putAll(RaopFutures.await(rtsp.info()));
        LOGGER.debug("Updated info parameters to: {}", info);

        // Handle some special authentication cases
        if (requiresAuthSetup()) {
            RaopFutures.await(rtsp.authSetup());
        }

        // Set up the streaming session
        protocol.setup(timingSrv.port(), control.port());
    }

    /**
     * Returns if auth-setup shall be performed: MFiSAP encryption supported by the
     * receiver and the receiver is an AirPort Express.
     */
    private boolean requiresAuthSetup() {
        String modelName = properties.getOrDefault("am", "");
        return encryptionTypes.contains(EncryptionType.MFISAP) && modelName.startsWith("AirPort");
    }

    /**
     * Stops what is currently playing.
     */
    public void stop() {
        LOGGER.debug("Stopping audio playback");
        isPlaying = false;
    }

    /**
     * Changes volume on the receiver.
     *
     * @param volume new volume in dBFS
     */
    public void setVolume(double volume) {
        RaopFutures.await(rtsp.setParameter("volume", formatFloat(volume)));
        context.volume = volume;
    }

    /** Formats a volume level as a plain decimal string. */
    private static String formatFloat(double value) {
        return Double.toString(value);
    }

    /**
     * Sends an audio stream to the device.
     *
     * @param source audio source to stream
     * @param streamMetadata metadata to publish ({@code null} for no metadata)
     * @param volume volume in percent to set once streaming has started (deferred set), or
     *            {@code null} when the volume has already been set
     */
    public void sendAudio(AudioSource source, @Nullable MediaMetadata streamMetadata, @Nullable Double volume) {
        ControlServer control = controlServer;
        if (control == null || timingServer == null) {
            throw new InvalidStateError("not initialized");
        }

        context.reset(rng, clock);

        DatagramSocket socket = null;
        try {
            // Create a socket used for writing audio packets
            socket = new DatagramSocket();
            socket.connect(InetAddress.getByName(rtsp.connection().remoteIp()), context.serverPort);

            // Start sending sync packets
            startSync(rtsp.connection().remoteIp());

            // Send progress if supported by receiver
            if (metadataTypes.contains(MetadataType.PROGRESS)) {
                long start = context.rtptime();
                long now = context.rtptime();
                long end = start + (long) source.duration() * context.sampleRate;
                RaopFutures.await(rtsp.setParameter("progress", start + "/" + now + "/" + end));
            }

            // Apply text metadata if it is supported
            this.metadata = streamMetadata == null ? EMPTY_METADATA : streamMetadata;
            if (metadataTypes.contains(MetadataType.TEXT)) {
                LOGGER.debug("Playing with metadata: {}", playbackInfo().metadata());
                setMetadata(playbackInfo().metadata());
            }

            // Send artwork if that is supported
            if (metadataTypes.contains(MetadataType.ARTWORK) && this.metadata.artwork() != null) {
                LOGGER.debug("Sending {} bytes artwork", this.metadata.artwork().length);
                setArtwork(this.metadata.artwork());
            }

            // Start keep-alive task to ensure connection is not closed by remote device
            protocol.startFeedback();

            RaopListener currentListener = listener;
            if (currentListener != null) {
                currentListener.playing(playbackInfo());
            }

            // Start playback
            RaopFutures.await(rtsp.record(null, null));

            RaopFutures.await(rtsp.flush(Map.of("Range", "npt=0-", "Session", context.rtspSession, "RTP-Info",
                    "seq=" + context.rtpseq + ";rtptime=" + context.rtptime()), null));

            if (volume != null) {
                setVolume(RaopVolume.pctToDbfs(volume));
            }

            streamData(source, socket);
        } catch (ProtocolError | AuthenticationError e) {
            throw e; // Re-raise internal exceptions to maintain a proper stack trace
        } catch (Exception e) {
            throw new ProtocolError("an error occurred during streaming", e);
        } finally {
            control.packetBacklog().clear(); // Don't keep old packets around (big!)
            if (socket != null) {
                // TODO: teardown should not be done here so the connection could be reused
                // for streaming more audio files. Refactor when supported.
                try {
                    RaopFutures.await(rtsp.teardown(context.rtspSession));
                } catch (RuntimeException e) {
                    LOGGER.debug("Teardown failed: {}", e.toString());
                }
                socket.close();
            }
            protocol.teardown();
            close();

            RaopListener currentListener = listener;
            if (currentListener != null) {
                currentListener.stopped();
            }
        }
    }

    /** Changes metadata for what is playing. */
    private void setMetadata(MediaMetadata streamMetadata) throws IOException {
        ByteArrayOutputStream payload = new ByteArrayOutputStream();
        if (streamMetadata.title() != null) {
            payload.write(DmapTags.stringTag("minm", streamMetadata.title()));
        }
        if (streamMetadata.album() != null) {
            payload.write(DmapTags.stringTag("asal", streamMetadata.album()));
        }
        if (streamMetadata.artist() != null) {
            payload.write(DmapTags.stringTag("asar", streamMetadata.artist()));
        }
        RaopFutures.await(rtsp.exchange("SET_PARAMETER", null, "application/x-dmap-tagged", rtpInfoHeaders(),
                DmapTags.containerTag("mlit", payload.toByteArray()), false, "RTSP/1.0"));
    }

    /** Changes artwork for what is playing. */
    private void setArtwork(byte[] artwork) {
        RaopFutures.await(
                rtsp.exchange("SET_PARAMETER", null, "image/jpeg", rtpInfoHeaders(), artwork, false, "RTSP/1.0"));
    }

    private Map<String, Object> rtpInfoHeaders() {
        return Map.of("Session", context.rtspSession, "RTP-Info",
                "seq=" + context.rtpseq + ";rtptime=" + context.rtptime());
    }

    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    private void startSync(String remoteAddr) {
        ControlServer control = controlServer;
        if (control == null) {
            throw new InvalidStateError("not initialized");
        }
        LOGGER.debug("Starting periodic sync task");
        InetSocketAddress dest = new InetSocketAddress(remoteAddr, context.controlPort);
        syncThread = Thread.ofVirtual().name("raop-sync").start(() -> {
            boolean first = true;
            try {
                while (syncThread == Thread.currentThread()) {
                    control.sendSync(dest, first, context.rtptime(), context.latency,
                            RaopTiming.ts2ntp(context.headTs, context.sampleRate));
                    first = false;
                    Thread.sleep(timing.syncInterval());
                }
            } catch (InterruptedException e) {
                // Stopped
            } catch (IOException e) {
                LOGGER.error("control task failure: {}", e.toString());
            }
            LOGGER.debug("Periodic sync task ended");
        });
    }

    private void stopSync() {
        Thread task = syncThread;
        if (task != null) {
            syncThread = null;
            task.interrupt();
        }
    }

    private void streamData(AudioSource source, DatagramSocket socket) throws IOException {
        LongSupplier nanoClock = timing.nanoClock();
        Statistics stats = new Statistics(context.sampleRate, nanoClock);

        long initialTime = nanoClock.getAsLong();
        isPlaying = true;
        int prevSlowSeqno = -1;
        int numberSlowSeqno = 0;
        while (isPlaying) {
            int currentSeqno = context.rtpseq - 1;

            int numSent = sendPacket(source, stats.totalFrames() == 0, socket);
            if (numSent == 0) {
                break;
            }

            stats.tick(numSent);
            long framesBehind = stats.framesBehind();

            // If we are late, send some additional frames with hopes of catching up
            if (framesBehind >= AudioSource.FRAMES_PER_PACKET) {
                int maxPackets = (int) Math.min(framesBehind / AudioSource.FRAMES_PER_PACKET, MAX_PACKETS_COMPENSATE);
                LOGGER.debug("Compensating with {} packets ({} frames behind)", maxPackets, framesBehind);
                long[] result = sendNumberOfPackets(source, socket, maxPackets);
                stats.tick((int) result[0]);
                if (result[1] == 0) {
                    break;
                }
            }

            // Log how long it took to send sample_rate amount of frames (should be 1s)
            if (stats.intervalCompleted()) {
                long[] interval = stats.newInterval();
                LOGGER.debug("Sent {} frames in {}s (current frames: {}, expected: {})", interval[1], interval[0] / 1e9,
                        stats.totalFrames(), stats.expectedFrameCount());
            }

            // Calculate the actual absolute position in stream and where we actually are
            // (from when we initially started to stream). The diff is the time we need to
            // sleep until the next lap.
            double absTimeStream = stats.totalFrames() / (double) context.sampleRate;
            double relToStart = (nanoClock.getAsLong() - initialTime) / 1e9;
            double diff = absTimeStream - relToStart;
            if (diff > 0) {
                numberSlowSeqno = 0;
                timing.sleeper().accept((long) (diff * 1e9));
            } else {
                // Increase number of consecutive frames that we are late
                if (prevSlowSeqno == currentSeqno - 1) {
                    numberSlowSeqno += 1;
                }

                if (numberSlowSeqno >= SLOW_WARNING_THRESHOLD) {
                    LOGGER.debug("Too slow to keep up for seqno {} ({} vs {} => {})", currentSeqno, absTimeStream,
                            relToStart, diff);
                } else {
                    LOGGER.debug("Too slow to keep up for seqno {} ({} vs {} => {})", currentSeqno, absTimeStream,
                            relToStart, diff);
                }
                prevSlowSeqno = currentSeqno;
            }
        }

        LOGGER.debug("Audio finished sending in {}s", (nanoClock.getAsLong() - stats.startTimeNs()) / 1e9);
    }

    private int sendPacket(AudioSource source, boolean firstPacket, DatagramSocket socket) throws IOException {
        ControlServer control = controlServer;
        if (control == null) {
            throw new InvalidStateError("not initialized");
        }

        // Once all frames in the audio stream have been sent, we are still "latency"
        // behind and will start sending padding (empty audio) until we catch up. This is
        // needed to keep the sync packets in line with real time.
        if (context.paddingSent >= context.latency) {
            return 0;
        }

        byte[] frames = source.readFrames(AudioSource.FRAMES_PER_PACKET);
        if (frames.length == 0) {
            // No more frames to send means we send padding packets (just zeros) to keep
            // sync packets accurate
            frames = new byte[context.packetSize()];
            context.paddingSent += frames.length / context.frameSize();
        } else if (frames.length != context.packetSize()) {
            // The audio stream length seldom aligns with number of frames per packet, so
            // pad the last packet with zeros
            frames = Arrays.copyOf(frames, context.packetSize());
        }

        byte[] header = new AudioPacketHeader(0x80, firstPacket ? 0xE0 : 0x60, context.rtpseq, context.rtptime(),
                rtsp.sessionId()).encode();

        if (socket.isClosed()) {
            LOGGER.debug("Connection closed while streaming audio");
            return 0;
        }

        // Send packet and add it to backlog
        StreamProtocol.SentAudioPacket sent = protocol.sendAudioPacket(socket, header, frames);
        control.packetBacklog().put(sent.seqno(), sent.packet());

        context.rtpseq = (context.rtpseq + 1) % (1 << 16);
        context.headTs += frames.length / context.frameSize();

        return frames.length / context.frameSize();
    }

    /**
     * Sends a specific number of packets, returning {@code [totalSentFrames,
     * hasMorePackets]} ({@code hasMorePackets} encoded as 1/0).
     */
    private long[] sendNumberOfPackets(AudioSource source, DatagramSocket socket, int count) throws IOException {
        long totalFrames = 0;
        for (int i = 0; i < count; i++) {
            int sent = sendPacket(source, false, socket);
            totalFrames += sent;
            if (sent == 0) {
                return new long[] { totalFrames, 0 };
            }
        }
        return new long[] { totalFrames, 1 };
    }

    /** Maintains statistics of frames during a streaming session. */
    static final class Statistics {

        private final int sampleRate;
        private final LongSupplier nanoClock;
        private final long startTimeNs;
        private long intervalTimeNs;
        private long totalFrames;
        private long intervalFrames;

        Statistics(int sampleRate, LongSupplier nanoClock) {
            this.sampleRate = sampleRate;
            this.nanoClock = nanoClock;
            this.startTimeNs = nanoClock.getAsLong();
            this.intervalTimeNs = startTimeNs;
        }

        long startTimeNs() {
            return startTimeNs;
        }

        long totalFrames() {
            return totalFrames;
        }

        /** Number of frames expected to be sent at the current time. */
        long expectedFrameCount() {
            return (long) ((nanoClock.getAsLong() - startTimeNs) / (1e9 / sampleRate));
        }

        /** Number of frames behind until being in sync. */
        long framesBehind() {
            return expectedFrameCount() - totalFrames;
        }

        /** Returns if sample_rate amount of frames has been sent since the interval start. */
        boolean intervalCompleted() {
            return intervalFrames >= sampleRate;
        }

        /** Adds newly sent frames to statistics. */
        void tick(int sentFrames) {
            totalFrames += sentFrames;
            intervalFrames += sentFrames;
        }

        /** Starts a new interval, returning {@code [elapsedNanos, frames]} of the previous one. */
        long[] newInterval() {
            long endTime = nanoClock.getAsLong();
            long diff = endTime - intervalTimeNs;
            intervalTimeNs = endTime;

            long frames = intervalFrames;
            intervalFrames = 0;

            return new long[] { diff, frames };
        }
    }
}
