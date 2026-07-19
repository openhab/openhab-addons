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
package org.openhab.binding.atv.internal.client.support;

import java.nio.charset.StandardCharsets;

import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * HKDF-SHA512 key derivation (RFC 5869, extract-then-expand).
 *
 * <p>
 * Used throughout HAP pairing and MRP/AirPlay/Companion session key derivation, always
 * with a fixed output length of 32 bytes.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class Hkdf {

    /** Output length in bytes used by all key derivations. */
    public static final int OUTPUT_LENGTH = 32;

    private Hkdf() {
    }

    /**
     * Derives a 32-byte key from a shared secret.
     *
     * @param salt HKDF salt, encoded as UTF-8 (e.g. {@code "Pair-Setup-Encrypt-Salt"})
     * @param info HKDF info, encoded as UTF-8
     * @param inputKeyMaterial the shared secret (IKM)
     * @return 32 bytes of output key material
     */
    public static byte[] expand(String salt, String info, byte[] inputKeyMaterial) {
        return expand(salt.getBytes(StandardCharsets.UTF_8), info.getBytes(StandardCharsets.UTF_8), inputKeyMaterial,
                OUTPUT_LENGTH);
    }

    /**
     * Derives key material with HKDF-SHA512.
     *
     * @param salt HKDF salt; an empty array is treated as a hash-length block of zeros
     *            per RFC 5869
     * @param info HKDF info (context)
     * @param inputKeyMaterial the shared secret (IKM)
     * @param length number of output bytes
     * @return {@code length} bytes of output key material
     */
    public static byte[] expand(byte[] salt, byte[] info, byte[] inputKeyMaterial, int length) {
        HKDFBytesGenerator generator = new HKDFBytesGenerator(new SHA512Digest());
        generator.init(new HKDFParameters(inputKeyMaterial, salt, info));
        byte[] output = new byte[length];
        generator.generateBytes(output, 0, length);
        return output;
    }
}
