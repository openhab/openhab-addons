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
package org.openhab.binding.candy.internal.protocol;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The repeated-key XOR and hexadecimal framing used by simply-Fi local status.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public final class CandyCodec {
    public static final int MAX_WIRE_BYTES = 65536;

    private CandyCodec() {
    }

    public static String decode(String wire, String key) throws CandyProtocolException {
        validateKey(key);
        String hex = wire.strip();
        if (wire.length() > MAX_WIRE_BYTES || hex.isEmpty() || hex.length() % 2 != 0
                || !hex.chars().allMatch(c -> c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a' && c <= 'f')) {
            throw new CandyProtocolException("@text/protocol-error-hex");
        }
        byte[] decoded = xor(HexFormat.of().parseHex(hex), key);
        try {
            return StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(decoded)).toString();
        } catch (CharacterCodingException e) {
            throw new CandyProtocolException("@text/protocol-error-encoding");
        }
    }

    public static String encode(String plaintext, String key) throws CandyProtocolException {
        validateKey(key);
        byte[] bytes = plaintext.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > MAX_WIRE_BYTES / 2) {
            throw new CandyProtocolException("@text/protocol-error-size");
        }
        return HexFormat.of().withUpperCase().formatHex(xor(bytes, key));
    }

    /** Whether a key has the format supported by the local protocol. */
    public static boolean isSupportedKey(String key) {
        return key.length() == 16 && key.chars().allMatch(c -> c >= 33 && c <= 126);
    }

    private static void validateKey(String key) throws CandyProtocolException {
        if (!isSupportedKey(key)) {
            throw new CandyProtocolException("@text/protocol-error-key");
        }
    }

    private static byte[] xor(byte[] bytes, String key) {
        byte[] keyBytes = key.getBytes(StandardCharsets.US_ASCII);
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] ^= keyBytes[i % keyBytes.length];
        }
        return bytes;
    }
}
