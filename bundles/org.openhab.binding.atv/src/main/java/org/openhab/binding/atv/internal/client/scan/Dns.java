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
package org.openhab.binding.atv.internal.client.scan;

import java.net.IDN;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.support.CaseInsensitiveMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processing functions for raw DNS messages.
 *
 * <p>
 * Buffers are {@link ByteBuffer}s so that name compression can seek within a message.
 * All multi-byte fields are big-endian (network order, the {@link ByteBuffer} default).
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class Dns {

    private static final Logger LOGGER = LoggerFactory.getLogger(Dns.class);

    private Dns() {
    }

    /**
     * Encodes a QNAME without using name compression.
     *
     * <p>
     * Labels are encoded using UTF-8 after NFC normalization (RFC 6763, section
     * 4.1.3), as that is what the Apple TV has been observed to use for all domain
     * names. If the name parses as a service instance name, dots inside the instance
     * label are preserved as part of a single label.
     *
     * @param name domain name with labels separated by dots
     * @return the encoded name, always ending with the root (null) label
     */
    public static byte[] qnameEncode(String name) {
        List<String> labels;
        try {
            labels = ServiceInstanceName.splitName(name).labels();
        } catch (IllegalArgumentException e) {
            labels = java.util.Arrays.asList(name.split("\\.", -1));
        }
        return qnameEncode(labels);
    }

    /**
     * Encodes a QNAME from a list of labels without using name compression. Each
     * element is treated as a single label; a null (empty) label for the root domain
     * is added if missing.
     *
     * @param name the labels
     * @return the encoded name
     */
    public static byte[] qnameEncode(List<String> name) {
        java.util.ArrayList<String> labels = new java.util.ArrayList<>(name);
        // Ensure there's always an empty label for the root domain
        if (labels.isEmpty() || !labels.get(labels.size() - 1).isEmpty()) {
            labels.add("");
        }
        java.io.ByteArrayOutputStream encoded = new java.io.ByteArrayOutputStream();
        for (String label : labels) {
            String normalized = Normalizer.normalize(label, Normalizer.Form.NFC);
            byte[] encodedLabel = normalized.getBytes(StandardCharsets.UTF_8);
            // When truncating the label, we can't just stop at 63 bytes as that might
            // be splitting a multi-byte Unicode codepoint.
            boolean truncated = false;
            String truncatedLabel = normalized;
            while (encodedLabel.length > 63) {
                truncated = true;
                truncatedLabel = truncatedLabel.substring(0, truncatedLabel.length() - 1);
                encodedLabel = truncatedLabel.getBytes(StandardCharsets.UTF_8);
            }
            if (truncated) {
                LOGGER.debug("A label ({}) is being truncated (to {}) in the DNS name '{}' "
                        + "as it is over 63 bytes long.", label, truncatedLabel, name);
            }
            encoded.write(encodedLabel.length);
            if (encodedLabel.length == 0) {
                // If we've reached an empty label, assume this is the last component.
                break;
            }
            encoded.writeBytes(encodedLabel);
        }
        return encoded.toByteArray();
    }

    /**
     * Unpacks a DNS character string: a single length byte followed by up to that many
     * bytes of data. This is distinct from "domain-name" encoding; use
     * {@link #parseDomainName(ByteBuffer)} for that.
     *
     * @param buffer buffer positioned at the string
     * @return the string data
     */
    public static byte[] parseString(ByteBuffer buffer) {
        int length = buffer.get() & 0xFF;
        byte[] data = new byte[length];
        buffer.get(data);
        return data;
    }

    /**
     * Unpacks a domain name, handling any name compression encountered (RFC 1035,
     * sections 3.1 and 4.1.4).
     *
     * <p>
     * Labels starting with the ACE prefix ({@code xn--}) are decoded with IDNA,
     * everything else as UTF-8 (what DNS-SD and Apple use).
     *
     * @param buffer buffer positioned at the name; left positioned after the name
     * @return the parsed domain name
     * @throws IllegalArgumentException on reserved compression flags or invalid UTF-8
     */
    public static String parseDomainName(ByteBuffer buffer) {
        StringBuilder name = new StringBuilder();
        int compressionOffset = -1;
        while (buffer.hasRemaining()) {
            int length = buffer.get() & 0xFF;
            if (length == 0) {
                break;
            }
            // The two high bits of the length are a flag for DNS name compression
            int lengthFlags = (length & 0xC0) >> 6;
            if (lengthFlags != 0 && lengthFlags != 0b11) {
                // The 10 and 01 flags are reserved
                throw new IllegalArgumentException("Reserved name compression flags: " + lengthFlags);
            }
            if (lengthFlags == 0b11) {
                int newOffset = ((length & 0x3F) << 8) | (buffer.get() & 0xFF);
                // It's technically possible to have multiple levels of name compression,
                // so make sure we don't lose the original place we need to go back to.
                if (compressionOffset < 0) {
                    compressionOffset = buffer.position();
                }
                buffer.position(newOffset);
            } else {
                byte[] label = new byte[length];
                buffer.get(label);
                String decodedLabel;
                if (label.length >= 4 && label[0] == 'x' && label[1] == 'n' && label[2] == '-' && label[3] == '-') {
                    decodedLabel = IDN.toUnicode(new String(label, StandardCharsets.US_ASCII));
                } else {
                    decodedLabel = decodeUtf8Strict(label);
                }
                if (name.length() > 0) {
                    name.append('.');
                }
                name.append(decodedLabel);
            }
        }
        if (compressionOffset >= 0) {
            buffer.position(compressionOffset);
        }
        return name.toString();
    }

    /**
     * Parses DNS-SD TXT records into a map. Keys are ASCII strings compared
     * case-insensitively; values are opaque binary blobs. Chunks without {@code =} are
     * stored with an empty value; empty or non-ASCII keys are skipped.
     *
     * @param buffer buffer positioned at the TXT RDATA
     * @param length RDATA length in bytes
     * @return parsed properties
     */
    public static CaseInsensitiveMap<byte[]> parseTxtDict(ByteBuffer buffer, int length) {
        CaseInsensitiveMap<byte[]> output = new CaseInsensitiveMap<>();
        int stopPosition = buffer.position() + length;
        while (buffer.position() < stopPosition) {
            byte[] chunk = parseString(buffer);
            int equals = indexOf(chunk, (byte) '=');
            if (equals < 0) {
                // missing "=" means it's just present with no value.
                output.put(new String(chunk, StandardCharsets.US_ASCII), new byte[0]);
            } else {
                byte[] key = java.util.Arrays.copyOfRange(chunk, 0, equals);
                byte[] value = java.util.Arrays.copyOfRange(chunk, equals + 1, chunk.length);
                if (key.length == 0) {
                    // Missing keys are skipped
                    continue;
                }
                if (!isAscii(key)) {
                    LOGGER.debug("Non-ASCII DNS-SD key encountered: {}", new String(key, StandardCharsets.ISO_8859_1));
                    continue;
                }
                output.put(new String(key, StandardCharsets.US_ASCII), value);
            }
        }
        return output;
    }

    /**
     * Parses a DNS SRV record. Name compression isn't allowed by the RFC for the
     * target, but is accepted anyway.
     *
     * @param buffer buffer positioned at the SRV RDATA
     * @return the parsed record
     */
    public static SrvRecord parseSrvRecord(ByteBuffer buffer) {
        int priority = buffer.getShort() & 0xFFFF;
        int weight = buffer.getShort() & 0xFFFF;
        int port = buffer.getShort() & 0xFFFF;
        String target = parseDomainName(buffer);
        return new SrvRecord(priority, weight, port, target);
    }

    private static String decodeUtf8Strict(byte[] data) {
        try {
            return StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(data)).toString();
        } catch (CharacterCodingException e) {
            throw new IllegalArgumentException("Invalid UTF-8 in DNS label", e);
        }
    }

    private static int indexOf(byte[] data, byte b) {
        for (int i = 0; i < data.length; i++) {
            if (data[i] == b) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isAscii(byte[] data) {
        for (byte b : data) {
            if ((b & 0x80) != 0) {
                return false;
            }
        }
        return true;
    }
}
