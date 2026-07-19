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

import java.time.Clock;
import java.util.Random;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.auth.HapCredentials;

/**
 * Data used for one RAOP session.
 *
 * <p>
 * A plain mutable data holder shared between the stream client and the stream protocol;
 * fields are public on purpose. Confine mutation to the streaming thread (plus the RTSP
 * callers that populate ports during setup).
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class StreamContext {

    /** Extra latency in frames added on top of one second of audio. */
    public static final int EXTRA_LATENCY = 22050;

    /** Credentials used for authentication (never {@code null}). */
    public HapCredentials credentials = HapCredentials.NO_CREDENTIALS;

    /** Device password, or {@code null} when the device is not password protected. */
    public @Nullable String password;

    /** Sample rate in Hz. */
    public int sampleRate = 44100;

    /** Number of audio channels. */
    public int channels = 2;

    /** Bytes per audio channel (2 = 16-bit). */
    public int bytesPerChannel = 2;

    /** Latency in frames ({@code 22050 + sample_rate}). */
    public long latency = EXTRA_LATENCY + sampleRate;

    /** Next RTP sequence number (unsigned 16-bit). */
    public int rtpseq;

    /** Timestamp of the first frame of the session. */
    public long startTs;

    /** Timestamp of the next frame to send. */
    public long headTs;

    /** Number of padding (silence) frames sent after the audio ended. */
    public long paddingSent;

    /** Receiver port audio packets are sent to. */
    public int serverPort;

    /** Receiver event channel port (AirPlay 2). */
    public int eventPort;

    /** Receiver control channel port. */
    public int controlPort;

    /** Receiver timing port. */
    public int timingPort;

    /** RTSP session id from the SETUP response (AirPlay 1). */
    public long rtspSession;

    /** Current volume in dBFS, or {@code null} when never changed from the default. */
    public @Nullable Double volume;

    /**
     * Resets the session; must be done when the sample rate changes.
     *
     * @param rng source of randomness for the initial sequence number (injectable for
     *            deterministic tests)
     * @param clock wall clock used to derive the initial timestamp
     */
    public void reset(Random rng, Clock clock) {
        rtpseq = rng.nextInt(1 << 16);
        startTs = RaopTiming.ntp2ts(RaopTiming.ntpNow(clock), sampleRate);
        headTs = startTs;
        latency = EXTRA_LATENCY + sampleRate;
        paddingSent = 0;
    }

    /**
     * Returns the current RTP time with latency.
     */
    public long rtptime() {
        return headTs - (startTs - latency);
    }

    /**
     * Returns the current position in the stream in seconds (with fraction).
     */
    public double position() {
        // Do not consider latency here (so do not use rtptime)
        return RaopTiming.ts2ms(headTs - startTs, sampleRate) / 1000.0;
    }

    /**
     * Returns the size of a single audio frame in bytes.
     */
    public int frameSize() {
        return channels * bytesPerChannel;
    }

    /**
     * Returns the size of a full audio packet payload in bytes.
     */
    public int packetSize() {
        return AudioSource.FRAMES_PER_PACKET * frameSize();
    }
}
