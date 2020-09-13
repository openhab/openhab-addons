/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.lifx.internal.fields;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tim Buckley
 * @author Karel Goderis
 */
@NonNullByDefault
public class MACAddress {

    public static final MACAddress BROADCAST_ADDRESS = new MACAddress("000000000000", true);

    private final Logger logger = LoggerFactory.getLogger(MACAddress.class);

    private ByteBuffer bytes;
    private String hex = "";

    public ByteBuffer getBytes() {
        return bytes;
    }

    public String getHex() {
        return hex;
    }

    public MACAddress(ByteBuffer bytes) {
        this.bytes = bytes;

        createHex();
    }

    public MACAddress(String string, boolean isHex) {
        if (!isHex) {
            this.bytes = ByteBuffer.wrap(string.getBytes());
            createHex();
        } else {
            this.bytes = ByteBuffer.wrap(parseHexBinary(string));

            try {
                formatHex(string, 2, ":");
            } catch (IOException e) {
                logger.error("An exception occurred while formatting an HEX string : '{}'", e.getMessage());
            }
        }
    }

    private byte[] parseHexBinary(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public MACAddress() {
        this(ByteBuffer.allocate(6));
    }

    private void createHex() {
        bytes.rewind();

        List<String> byteStrings = new LinkedList<>();
        while (bytes.hasRemaining()) {
            byteStrings.add(String.format("%02X", bytes.get()));
        }

        hex = StringUtils.join(byteStrings, ':');

        bytes.rewind();
    }

    public String getAsLabel() {
        bytes.rewind();

        StringBuilder hex = new StringBuilder();
        while (bytes.hasRemaining()) {
            hex.append(String.format("%02X", bytes.get()));
        }

        bytes.rewind();

        return hex.toString();
    }

    private void formatHex(String original, int length, String separator) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(original.getBytes());
        byte[] buffer = new byte[length];
        String result = "";
        while (bis.read(buffer) > 0) {
            for (byte b : buffer) {
                result += (char) b;
            }
            Arrays.fill(buffer, (byte) 0);
            result += separator;
        }

        hex = StringUtils.left(result, result.length() - 1);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.hex);
        return hash;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final MACAddress other = (MACAddress) obj;
        if (!this.hex.equalsIgnoreCase(other.hex)) {
            return false;
        }

        return true;
    }
}
