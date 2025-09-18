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
package org.openhab.binding.homekit.internal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.homekit.internal.crypto.CryptoUtils;
import org.openhab.binding.homekit.internal.crypto.Tlv8Codec;
import org.openhab.binding.homekit.internal.enums.PairingMethod;
import org.openhab.binding.homekit.internal.enums.PairingState;
import org.openhab.binding.homekit.internal.enums.TlvType;
import org.openhab.binding.homekit.internal.hap_services.PairVerifyClient;
import org.openhab.binding.homekit.internal.hap_services.PairVerifyClient.Validator;
import org.openhab.binding.homekit.internal.transport.HttpTransport;

/**
 * Test cases for the {@link PairVerifyClient} class.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class TestPairVerify {

    private static final String PAIR_VERIFY_ENCRYPT_INFO = "Pair-Verify-Encrypt-Info";
    private static final String PAIR_VERIFY_ENCRYPT_SALT = "Pair-Verify-Encrypt-Salt";
    private static final byte[] VERIFY_NONCE_M2 = CryptoUtils.generateNonce("PV-Msg02");
    private static final byte[] VERIFY_NONCE_M3 = CryptoUtils.generateNonce("PV-Msg03");

    public static final String CLIENT_PRIVATE_HEX = """
            60975527 035CF2AD 1989806F 0407210B C81EDC04 E2762A56 AFD529DD DA2D4393
            """;

    public static final String SERVER_PRIVATE_HEX = """
            E487CB59 D31AC550 471E81F0 0F6928E0 1DDA08E9 74A004F4 9E61F5D1 05284D20
            """;

    private byte[] sessionKey = new byte[0];

    @Test
    void testPairVerify() throws Exception {
        // initialize test parameters
        String baseUrl = "http://example.com";
        String clientIdentifier = "11:22:33:44:55:66";
        String serverIdentifier = "AA:BB:CC:DD:EE:FF";

        // initialize signing keys
        Ed25519PrivateKeyParameters clientPrivateSigningKey = new Ed25519PrivateKeyParameters(
                hexBlockToByteArray(CLIENT_PRIVATE_HEX));
        Ed25519PrivateKeyParameters serverPrivateSigningKey = new Ed25519PrivateKeyParameters(
                hexBlockToByteArray(SERVER_PRIVATE_HEX));

        // create mock
        HttpTransport mockTransport = mock(HttpTransport.class);

        // create SRP client and server
        PairVerifyClient client = new PairVerifyClient(mockTransport, baseUrl, clientIdentifier,
                clientPrivateSigningKey, serverPrivateSigningKey.generatePublicKey());

        // mock the HTTP transport to simulate the SRP exchange
        doAnswer(invocation -> {
            byte[] arg = invocation.getArgument(3);

            // decode and validate the incoming TLV
            Map<Integer, byte[]> tlv = Tlv8Codec.decode(arg);
            PairVerifyClient.Validator.validate(PairingMethod.VERIFY, tlv);
            byte[] state = tlv.get(TlvType.STATE.key);
            if (state == null || state.length != 1) {
                throw new IllegalArgumentException("State missing or invalid");
            }

            // process the message based on the pair verification process Mx state
            return switch (state[0]) {
                case 1 -> getServerResponseM1(tlv, serverIdentifier, serverPrivateSigningKey);
                case 3 -> getServerResponseM3(tlv);
                default -> throw new IllegalArgumentException("Unexpected state");
            };

        }).when(mockTransport).post(anyString(), anyString(), anyString(), any(byte[].class));

        // execute the pairing verification process
        client.verify();
    }

    private byte[] getServerResponseM1(Map<Integer, byte[]> tlv, String serverIdentifier,
            Ed25519PrivateKeyParameters serverPrivateSigningKey) throws Exception {
        X25519PrivateKeyParameters serverKey = CryptoUtils.generateX25519KeyPair();

        byte[] pairingId = serverIdentifier.getBytes(StandardCharsets.UTF_8);
        byte[] clientKeyBytes = tlv.get(TlvType.PUBLIC_KEY.key);
        byte[] payload = concat(serverKey.generatePublicKey().getEncoded(), pairingId,
                Objects.requireNonNull(clientKeyBytes));

        byte[] signature = CryptoUtils.signVerifyMessage(serverPrivateSigningKey, payload);
        Map<Integer, byte[]> tlvInner = Map.of( //
                TlvType.IDENTIFIER.key, pairingId, //
                TlvType.SIGNATURE.key, signature);

        X25519PublicKeyParameters clientKey = new X25519PublicKeyParameters(clientKeyBytes);

        byte[] sharedSecret = CryptoUtils.computeSharedSecret(serverKey, clientKey);
        this.sessionKey = CryptoUtils.hkdf(sharedSecret, PAIR_VERIFY_ENCRYPT_SALT, PAIR_VERIFY_ENCRYPT_INFO);
        byte[] plaintext = Tlv8Codec.encode(tlvInner); // TODO ?? authTag see page 40
        byte[] encrypted = CryptoUtils.encrypt(sessionKey, VERIFY_NONCE_M2, plaintext);

        Map<Integer, byte[]> tlvOut = Map.of( //
                TlvType.STATE.key, new byte[] { PairingState.M2.value }, //
                TlvType.PUBLIC_KEY.key, serverKey.generatePublicKey().getEncoded(), //
                TlvType.ENCRYPTED_DATA.key, encrypted);

        return Tlv8Codec.encode(tlvOut);
    }

    private byte[] getServerResponseM3(Map<Integer, byte[]> tlv) throws Exception {
        if (sessionKey.length == 0) {
            throw new IllegalStateException("Session key not established");
        }
        byte[] encrypted = tlv.get(TlvType.ENCRYPTED_DATA.key);
        byte[] plaintext = CryptoUtils.decrypt(sessionKey, VERIFY_NONCE_M3, Objects.requireNonNull(encrypted));

        System.out.println("Decrypted M3: " + Arrays.toString(plaintext)); // TODO

        Map<Integer, byte[]> tlvOut = Map.of(TlvType.STATE.key, new byte[] { PairingState.M4.value });
        Validator.validate(PairingMethod.VERIFY, tlvOut);

        // no further messages from server
        return Tlv8Codec.encode(tlvOut);
    }

    private static byte[] hexBlockToByteArray(String hexBlock) {
        String normalized = hexBlock.replaceAll("\\s+", "");
        if (normalized.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string must have even length");
        }
        int len = normalized.length();
        byte[] result = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            int high = Character.digit(normalized.charAt(i), 16);
            int low = Character.digit(normalized.charAt(i + 1), 16);
            if (high == -1 || low == -1) {
                throw new IllegalArgumentException(
                        "Invalid hex character: " + normalized.charAt(i) + normalized.charAt(i + 1));
            }
            result[i / 2] = (byte) ((high << 4) + low);
        }
        return result;
    }

    private static byte[] concat(byte[]... parts) {
        int total = Arrays.stream(parts).mapToInt(p -> p.length).sum();
        byte[] out = new byte[total];
        int pos = 0;
        for (byte[] p : parts) {
            System.arraycopy(p, 0, out, pos, p.length);
            pos += p.length;
        }
        return out;
    }
}
