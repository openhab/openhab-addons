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

import java.io.IOException;
import java.net.DatagramSocket;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.support.http.HttpResponse;

/**
 * Base interface for a streaming protocol.
 *
 * <p>
 * Implementations are AirPlay version specific ({@link AirPlayV1}, {@link AirPlayV2}).
 * Methods are blocking (the RAOP data plane runs on dedicated virtual threads); RTSP
 * futures are joined internally.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface StreamProtocol {

    /**
     * Result of sending an audio packet: the sequence number and the full packet as it
     * appeared on the wire (used for the retransmission backlog).
     *
     * @param seqno RTP sequence number of the sent packet
     * @param packet the full packet bytes (header + payload, including any trailing nonce)
     */
    record SentAudioPacket(int seqno, byte[] packet) {
    }

    /**
     * Sets up the connection prior to starting to stream.
     *
     * @param timingServerPort local timing server port
     * @param controlClientPort local control client port
     */
    void setup(int timingServerPort, int controlClientPort);

    /** Tears down resources allocated by setup after streaming finished. */
    void teardown();

    /** Starts to send feedback (if supported and required). */
    void startFeedback();

    /**
     * Sends an audio packet to the receiver.
     *
     * @param socket UDP socket connected to the receiver's audio port
     * @param rtpHeader encoded 12-byte RTP header
     * @param audio audio payload (raw PCM frames)
     * @return sequence number and full sent packet
     * @throws IOException if the datagram cannot be sent
     */
    SentAudioPacket sendAudioPacket(DatagramSocket socket, byte[] rtpHeader, byte[] audio) throws IOException;

    /**
     * Plays media from a URL.
     *
     * @param timingServerPort local timing server port
     * @param url URL to play
     * @param position start position in seconds
     * @return the response to the play request
     */
    HttpResponse playUrl(int timingServerPort, String url, double position);
}
