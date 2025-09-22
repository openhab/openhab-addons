/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.homekit.internal.crypto;

import static org.openhab.binding.homekit.internal.crypto.CryptoUtils.*;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Constants for cryptographic operations used in HomeKit communication.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class CryptoConstants {

    public static final BigInteger N = new BigInteger("""
            FFFFFFFF FFFFFFFF C90FDAA2 2168C234 C4C6628B 80DC1CD1 29024E08 8A67CC74
            020BBEA6 3B139B22 514A0879 8E3404DD EF9519B3 CD3A431B 302B0A6D F25F1437
            4FE1356D 6D51C245 E485B576 625E7EC6 F44C42E9 A637ED6B 0BFF5CB6 F406B7ED
            EE386BFB 5A899FA5 AE9F2411 7C4B1FE6 49286651 ECE45B3D C2007CB8 A163BF05
            98DA4836 1C55D39A 69163FA8 FD24CF5F 83655D23 DCA3AD96 1C62F356 208552BB
            9ED52907 7096966D 670C354E 4ABC9804 F1746C08 CA18217C 32905E46 2E36CE3B
            E39E772C 180E8603 9B2783A2 EC07A28F B5C55DF0 6F4C52C9 DE2BCBF6 95581718
            3995497C EA956AE5 15D22618 98FA0510 15728E5A 8AAAC42D AD33170D 04507A33
            A85521AB DF1CBA64 ECFB8504 58DBEF0A 8AEA7157 5D060C7D B3970F85 A6E1E4C7
            ABF5AE8C DB0933D7 1E8C94E0 4A25619D CEE3D226 1AD2EE6B F12FFA06 D98A0864
            D8760273 3EC86A64 521F2B18 177B200C BBE11757 7A615D6C 770988C0 BAD946E2
            08E24FA0 74E5AB31 43DB5BFC E0FD108E 4B82D120 A93AD2CA FFFFFFFF FFFFFFFF
            """.replaceAll("\\s+", ""), 16);

    public static final BigInteger g = BigInteger.valueOf(5);
    public static final BigInteger k = computeK();

    // @formatter:off
    public static final String PAIR_SETUP = "Pair-Setup";
    public static final byte[] PAIR_SETUP_ENCRYPT_INFO = "Pair-Setup-Encrypt-Info".getBytes(StandardCharsets.UTF_8);
    public static final byte[] PAIR_SETUP_ENCRYPT_SALT = "Pair-Setup-Encrypt-Salt".getBytes(StandardCharsets.UTF_8);

    public static final byte[] PS_M5_NONCE = CryptoUtils.generateNonce("PS-Msg05");
    public static final byte[] PS_M6_NONCE = CryptoUtils.generateNonce("PS-Msg06");

    public static final byte[] PAIR_CONTROLLER_SIGN_SALT = "Pair-Setup-Controller-Sign-Salt".getBytes(StandardCharsets.UTF_8);
    public static final byte[] PAIR_CONTROLLER_SIGN_INFO = "Pair-Setup-Controller-Sign-Info".getBytes(StandardCharsets.UTF_8);

    public static final byte[] PAIR_ACCESSORY_SIGN_SALT = "Pair-Setup-Accessory-Sign-Salt".getBytes(StandardCharsets.UTF_8);
    public static final byte[] PAIR_ACCESSORY_SIGN_INFO = "Pair-Setup-Accessory-Sign-Info".getBytes(StandardCharsets.UTF_8);

    public static final byte[] CONTROL_SALT = "Control-Salt".getBytes(StandardCharsets.UTF_8);
    public static final byte[] CONTROL_READ_ENCRYPTION_KEY = "Control-Read-Encryption-Key".getBytes(StandardCharsets.UTF_8);
    public static final byte[] CONTROL_WRITE_ENCRYPTION_KEY = "Control-Write-Encryption-Key".getBytes(StandardCharsets.UTF_8);

    public static final byte[] PAIR_VERIFY_ENCRYPT_INFO = "Pair-Verify-Encrypt-Info".getBytes(StandardCharsets.UTF_8);
    public static final byte[] PAIR_VERIFY_ENCRYPT_SALT = "Pair-Verify-Encrypt-Salt".getBytes(StandardCharsets.UTF_8);

    public static final byte[] PV_M2_NONCE = CryptoUtils.generateNonce("PV-Msg02");
    public static final byte[] PV_M3_NONCE = CryptoUtils.generateNonce("PV-Msg03");
    // @formatter:on

    private static BigInteger computeK() {
        try {
            byte[] paddedN = toUnsigned(N, N);
            byte[] paddedG = toUnsigned(g, N);
            byte[] hash = sha512(CryptoUtils.concat(paddedN, paddedG));
            return new BigInteger(1, hash);
        } catch (Exception e) {
            throw new SecurityException("Failed to compute k", e);
        }
    }
}
