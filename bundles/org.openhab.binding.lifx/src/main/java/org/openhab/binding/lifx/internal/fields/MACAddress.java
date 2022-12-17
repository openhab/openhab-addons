/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.util.HexUtils;

/**
 * @author Tim Buckley - Initial contribution
 * @author Karel Goderis - Initial contribution
 */
@NonNullByDefault
public class MACAddress {

    public static final MACAddress BROADCAST_ADDRESS = new MACAddress("000000000000");

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

    public MACAddress(String string) {
        byte[] byteArray = HexUtils.hexToBytes(string);
        this.bytes = ByteBuffer.wrap(byteArray);
        this.hex = HexUtils.bytesToHex(byteArray, ":");
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

        hex = String.join(":", byteStrings);

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
        return this.hex.equalsIgnoreCase(other.hex);
    }
}
