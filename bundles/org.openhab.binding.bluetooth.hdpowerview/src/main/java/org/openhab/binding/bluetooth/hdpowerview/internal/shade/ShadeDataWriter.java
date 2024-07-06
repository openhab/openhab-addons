/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.hdpowerview.internal.shade;

import java.util.HexFormat;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Parser for data sent to an HD PowerView Generation 3 Shade.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ShadeDataWriter {

    // real position values 0% to 100% scale to internal values 0 to 10000
    private static final double SCALE = 100;

    // byte array for a blank 'no-op' write command
    private static final byte[] BLANK_WRITE_COMMAND_FRAME = HexFormat.ofDelimiter(":")
            .parseHex("1f:70:80:7e:5c:07:ad:03:10:0e:0f:b3:da");

    // index to data field positions in the outgoing bytes
    private static final int INDEX_SEQUENCE = 2;
    private static final int INDEX_PRIMARY = 4;
    private static final int INDEX_SECONDARY = 6;
    private static final int INDEX_TILT = 10;
    @SuppressWarnings("unused")
    private static final int INDEX_VELOCITY = 8;

    // primary position nibble value substitution tables
    private static final List<Integer> PRIMARY_NIBBLE_0_SUBSTITUTES = //
            List.of(12, 13, 14, 15, 8, 9, 10, 11, 4, 5, 6, 7, 0, 1, 2, 3);
    private static final List<Integer> PRIMARY_NIBBLE_1_SUBSTITUTES = //
            List.of(5, 4, 7, 6, 1, 0, 3, 2, 13, 12, 15, 14, 9, 8, 11, 10);

    // no-op command for secondary channel position
    private static final byte SECONDARY_DEFAULT_LOW = (byte) 0xad;
    private static final byte SECONDARY_DEFAULT_HIGH = (byte) 0x03;

    // tilt flag (set when sending a tilt command)
    private static final byte TILT_FLAG = (byte) 0x33;

    private final byte[] bytes;

    private ShadeDataWriter() {
        bytes = BLANK_WRITE_COMMAND_FRAME.clone();
    }

    /**
     * Calculate primary position nibble #0 from a position in the range 0 to 10000.
     * Reverse engineered from observing Bluetooth traffic between the HD App and a shade.
     */
    private static int calculatePrimaryNibble0(int position) {
        return PRIMARY_NIBBLE_0_SUBSTITUTES.get(position % 16);
    }

    /**
     * Calculate primary position nibble #1 from a position in the range 0 to 10000.
     * Reverse engineered from observing Bluetooth traffic between the HD App and a shade.
     */
    private static int calculatePrimaryNibble1(int position) {
        return PRIMARY_NIBBLE_1_SUBSTITUTES.get((position / 16) % 16);
    }

    /**
     * Calculate primary position nibble #2 from a position in the range 0 to 10000.
     * Reverse engineered from observing Bluetooth traffic between the HD App and a shade.
     */
    private static int calculatePrimaryNibble2(int position) {
        return (((position / 2048) * 16) + 7 - (position / 256)) & 0xf;
    }

    /**
     * Calculate primary position nibble #3 from a position in the range 0 to 10000.
     * Reverse engineered from observing Bluetooth traffic between the HD App and a shade.
     */
    private static int calculatePrimaryNibble3(int position) {
        return 8 + (position / 4096);
    }

    /**
     * Create and initialize a {@code ShadeDataWriter} for example as {@code
     * create().withPrimary(position).withSecondary(position).withSequence(sequence).withTilt(position)}
     */
    public static ShadeDataWriter create() {
        return new ShadeDataWriter();
    }

    /**
     * Convert the given primary position percentage to a byte array containing the values to write to the shade.
     * Reverse engineered from observing Bluetooth traffic between the HD App and a shade.
     *
     * @param percent the position in percent
     * @return a byte array in little endian format
     *
     * @throws IllegalArgumentException if the position is out of range 0..100
     */
    public static byte[] primaryPositionToBytes(double percent) throws IllegalArgumentException {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException(String.format("Number '%0.1f' out of range (0% to 100%)", percent));
        }
        int position = ((int) Math.round(percent * SCALE));
        return new byte[] { (byte) ((calculatePrimaryNibble1(position) << 4) | calculatePrimaryNibble0(position)),
                (byte) ((calculatePrimaryNibble3(position) << 4) | calculatePrimaryNibble2(position)) };
    }

    /**
     * Convert the given secondary position percentage to a byte array containing the values to write to the shade.
     *
     * Secondary is not yet implemented due to lack of a device to test on. I intend to implement this as soon as I
     * get appropriate inputs from a user via the community forum or a GitHub issue. Currently it just returns the
     * default command value.
     *
     * @param percent the position in percent
     * @return a byte array in little endian format
     *
     * @throws IllegalArgumentException if the position is out of range 0..100
     */
    public static byte[] secondaryPositionToBytes(double percent) throws IllegalArgumentException {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException(String.format("Number '%0.1f' out of range (0% to 100%)", percent));
        }
        return new byte[] { SECONDARY_DEFAULT_LOW, SECONDARY_DEFAULT_HIGH };
    }

    /**
     * Convert the given tilt position percent to a byte array containing the values to write to the shade. . Reverse
     * engineered from observing Bluetooth traffic between the HD App and a shade.
     *
     * @param percent the position in percent
     * @return a byte array in little endian format
     *
     * @throws IllegalArgumentException if the position is out of range 0..100
     */
    public static byte[] tiltPositionToBytes(double percent) throws IllegalArgumentException {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException(String.format("Number '%0.1f' out of range (0% to 100%)", percent));
        }
        int position = (int) Math.round(percent);

        // reverse engineered encoding
        int bitsHi = position & 0xf0; // mask off high order bits 7..4
        int bitsLo = position & 0xf; // mask off low order bits 3..0
        bitsLo = (0xf - bitsLo) & 0xf; // apply HD custom adjustment

        return new byte[] { (byte) (bitsHi | bitsLo), TILT_FLAG };
    }

    /**
     * Return the byte array.
     */
    public byte[] getBytes() {
        return bytes;
    }

    /**
     * Overwrite the primary position command bytes.
     */
    public ShadeDataWriter withPrimary(double percent) {
        byte[] bytes = primaryPositionToBytes(percent);
        System.arraycopy(bytes, 0, this.bytes, INDEX_PRIMARY, bytes.length);
        return this;
    }

    /**
     * Overwrite the secondary position command bytes.
     */
    public ShadeDataWriter withSecondary(double percent) {
        byte[] bytes = secondaryPositionToBytes(percent);
        System.arraycopy(bytes, 0, this.bytes, INDEX_SECONDARY, bytes.length);
        return this;
    }

    /**
     * Overwrite the sequence number command byte.
     */
    public ShadeDataWriter withSequence(byte sequence) {
        this.bytes[INDEX_SEQUENCE] = sequence;
        return this;
    }

    /**
     * Overwrite the tilt position command bytes.
     */
    public ShadeDataWriter withTilt(double percent) {
        byte[] bytes = tiltPositionToBytes(percent);
        System.arraycopy(bytes, 0, this.bytes, INDEX_TILT, bytes.length);
        return this;
    }
}
