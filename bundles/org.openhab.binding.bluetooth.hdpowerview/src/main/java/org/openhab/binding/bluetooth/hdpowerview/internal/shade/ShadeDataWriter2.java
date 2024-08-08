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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Parser for data sent to an HD PowerView Generation 3 Shade.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ShadeDataWriter2 implements ShadeDataWriter {

    // real position values 0% to 100% scale to internal values 0 to 10000
    private static final double SCALE = 100;

    // byte array for a blank 'no-op' write command
    private static final byte[] BLANK_WRITE_COMMAND_FRAME = HexFormat.ofDelimiter(":")
            .parseHex("f7:01:00:09:00:80:00:80:00:80:00:80:00");

    // index to data field positions in the outgoing bytes
    private static final int INDEX_SEQUENCE = 2;
    private static final int INDEX_PRIMARY = 4;
    private static final int INDEX_SECONDARY = 6;
    private static final int INDEX_TILT = 8;

    private final byte[] bytes;

    public ShadeDataWriter2() {
        bytes = BLANK_WRITE_COMMAND_FRAME.clone();
    }

    @Override
    public byte[] primaryPositionToBytes(double percent) throws IllegalArgumentException {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException(String.format("Number '%0.1f' out of range (0% to 100%)", percent));
        }
        int position = ((int) Math.round(percent * SCALE));
        return new byte[] { (byte) (position & 0xff), (byte) ((position & 0xff00) >> 8) };
    }

    @Override
    public byte[] secondaryPositionToBytes(double percent) throws IllegalArgumentException {
        return primaryPositionToBytes(0);
    }

    @Override
    public byte[] tiltPositionToBytes(double percent) throws IllegalArgumentException {
        return primaryPositionToBytes(percent);
    }

    @Override
    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public ShadeDataWriter2 withPrimary(double percent) {
        byte[] bytes = primaryPositionToBytes(percent);
        System.arraycopy(bytes, 0, this.bytes, INDEX_PRIMARY, bytes.length);
        return this;
    }

    @Override
    public ShadeDataWriter2 withSecondary(double percent) {
        byte[] bytes = secondaryPositionToBytes(percent);
        System.arraycopy(bytes, 0, this.bytes, INDEX_SECONDARY, bytes.length);
        return this;
    }

    @Override
    public ShadeDataWriter2 withSequence(byte sequence) {
        this.bytes[INDEX_SEQUENCE] = sequence;
        return this;
    }

    @Override
    public ShadeDataWriter2 withTilt(double percent) {
        byte[] bytes = tiltPositionToBytes(percent);
        System.arraycopy(bytes, 0, this.bytes, INDEX_TILT, bytes.length);
        return this;
    }
}
