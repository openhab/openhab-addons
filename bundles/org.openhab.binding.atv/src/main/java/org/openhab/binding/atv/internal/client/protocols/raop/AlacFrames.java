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

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Wraps 16-bit stereo PCM in uncompressed ALAC frames.
 *
 * <p>
 * Some AirPlay 2 receivers only accept ALAC or AAC. ALAC allows a frame to carry the samples as is, so no real
 * encoder is needed. A frame is a 23-bit header, the big-endian samples in L/R order, and a 3-bit end tag. The
 * fields are not byte aligned, so every sample byte is shifted by one bit.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
final class AlacFrames {

    private static final int CHANNEL_PAIR = 1;
    private static final int END = 7;

    private AlacFrames() {
    }

    /**
     * @param pcm 16-bit big-endian stereo samples
     * @return one uncompressed ALAC frame holding the same samples
     */
    static byte[] wrap(byte[] pcm) {
        BitWriter out = new BitWriter(pcm.length + 4);
        out.write(CHANNEL_PAIR, 3);
        out.write(0, 4); // element instance
        out.write(0, 12); // unused
        // No sample count. Every frame then holds the stream's 352 samples per channel, which is why the last
        // packet of a stream is padded to full length.
        out.write(0, 1);
        out.write(0, 2); // no extra low bits
        out.write(1, 1); // not compressed
        for (byte b : pcm) {
            out.write(b & 0xFF, 8);
        }
        out.write(END, 3);
        return out.toByteArray();
    }

    private static final class BitWriter {
        private final byte[] buf;
        private int bitPos;

        BitWriter(int capacity) {
            buf = new byte[capacity];
        }

        void write(int value, int bits) {
            for (int i = bits - 1; i >= 0; i--) {
                if (((value >> i) & 1) != 0) {
                    buf[bitPos >> 3] |= (byte) (0x80 >> (bitPos & 7));
                }
                bitPos++;
            }
        }

        byte[] toByteArray() {
            return Arrays.copyOf(buf, (bitPos + 7) >> 3);
        }
    }
}
