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
import static org.openhab.binding.homekit.internal.crypto.CryptoConstants.*;
import static org.openhab.binding.homekit.internal.crypto.CryptoUtils.*;

import java.util.Map;
import java.util.Objects;

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.homekit.internal.crypto.Tlv8Codec;
import org.openhab.binding.homekit.internal.enums.PairingMethod;
import org.openhab.binding.homekit.internal.enums.PairingState;
import org.openhab.binding.homekit.internal.enums.TlvType;
import org.openhab.binding.homekit.internal.hap_services.PairVerifyClient;
import org.openhab.binding.homekit.internal.transport.IpTransport;

/**
 * Test cases for the {@link PairVerifyClient} class.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class TestPairVerify {

    public static final String CLIENT_PRIVATE_HEX = """
            60975527 035CF2AD 1989806F 0407210B C81EDC04 E2762A56 AFD529DD DA2D4393
            """;

    public static final String SERVER_PRIVATE_HEX = """
            E487CB59 D31AC550 471E81F0 0F6928E0 1DDA08E9 74A004F4 9E61F5D1 05284D20
            """;

    byte[] clientPairingId = new byte[] { 11, 22, 33, 44, 55, 66, 77, 88 };
    byte[] serverPairingId = new byte[] { 88, 77, 66, 55, 44, 33, 22, 11 };

    private final Ed25519PrivateKeyParameters clientLongTermPrivateKey = new Ed25519PrivateKeyParameters(
            toBytes(CLIENT_PRIVATE_HEX));

    private final Ed25519PrivateKeyParameters serverLongTermPrivateKey = new Ed25519PrivateKeyParameters(
            toBytes(SERVER_PRIVATE_HEX));

    private @NonNullByDefault({}) X25519PrivateKeyParameters serverEphemeralSecretKey;
    private @NonNullByDefault({}) X25519PublicKeyParameters clientEphemeralPublicKey;
    private @NonNullByDefault({}) byte[] sharedKey;

    @Test
    void testPairVerify() throws Exception {
        serverEphemeralSecretKey = generateX25519KeyPair();

        // create mock
        IpTransport mockTransport = mock(IpTransport.class);

        // create SRP client and server
        PairVerifyClient client = new PairVerifyClient(mockTransport, clientPairingId, clientLongTermPrivateKey,
                serverLongTermPrivateKey.generatePublicKey());

        // mock the HTTP transport to simulate the SRP exchange
        doAnswer(invocation -> {
            byte[] arg = invocation.getArgument(2);

            // decode and validate the incoming TLV
            Map<Integer, byte[]> tlv = Tlv8Codec.decode(arg);
            PairVerifyClient.Validator.validate(PairingMethod.VERIFY, tlv);
            byte[] state = tlv.get(TlvType.STATE.key);
            if (state == null || state.length != 1) {
                throw new IllegalArgumentException("State missing or invalid");
            }

            // process the message based on the pair verification process Mx state
            return switch (state[0]) {
                case 1 -> m1GetServerResponse(tlv);
                case 3 -> m3GetServerResponse(tlv);
                default -> throw new IllegalArgumentException("Unexpected state");
            };

        }).when(mockTransport).post(anyString(), anyString(), any(byte[].class));

        // execute the pairing verification process
        client.verify();
    }

    private byte[] m1GetServerResponse(Map<Integer, byte[]> tlv) throws Exception {
        byte[] clientEphemeralPublicKey = tlv.get(TlvType.PUBLIC_KEY.key);
        byte[] serverEphemeralPublicKey = this.serverEphemeralSecretKey.generatePublicKey().getEncoded();
        if (clientEphemeralPublicKey == null) {
            throw new SecurityException("Client public key missing");
        }
        byte[] serverSignature = signMessage(serverLongTermPrivateKey,
                concat(serverEphemeralPublicKey, serverPairingId, clientEphemeralPublicKey));

        Map<Integer, byte[]> tlvInner = Map.of( //
                TlvType.IDENTIFIER.key, serverPairingId, //
                TlvType.SIGNATURE.key, serverSignature);

        this.clientEphemeralPublicKey = new X25519PublicKeyParameters(clientEphemeralPublicKey);

        byte[] sharedSecret = generateSharedSecret(serverEphemeralSecretKey, this.clientEphemeralPublicKey);
        sharedKey = generateHkdfKey(sharedSecret, PAIR_VERIFY_ENCRYPT_SALT, PAIR_VERIFY_ENCRYPT_INFO);

        byte[] plainText = Tlv8Codec.encode(tlvInner);
        byte[] cipherText = encrypt(sharedKey, PV_M2_NONCE, plainText, new byte[0]);

        Map<Integer, byte[]> tlvOut = Map.of( //
                TlvType.STATE.key, new byte[] { PairingState.M2.value }, //
                TlvType.PUBLIC_KEY.key, serverEphemeralPublicKey, //
                TlvType.ENCRYPTED_DATA.key, cipherText);

        return Tlv8Codec.encode(tlvOut);
    }

    private byte[] m3GetServerResponse(Map<Integer, byte[]> tlv) throws Exception {
        if (sharedKey.length == 0) {
            throw new IllegalStateException("Session key not established");
        }
        byte[] cipherText = tlv.get(TlvType.ENCRYPTED_DATA.key);
        if (cipherText == null) {
            throw new SecurityException("Server cipher text missing");
        }
        byte[] plainText = decrypt(sharedKey, PV_M3_NONCE, Objects.requireNonNull(cipherText), new byte[0]);

        Map<Integer, byte[]> subTlv = Tlv8Codec.decode(plainText);
        byte[] clientPairingId = subTlv.get(TlvType.IDENTIFIER.key);
        byte[] clientSignature = subTlv.get(TlvType.SIGNATURE.key);
        if (clientPairingId == null || clientSignature == null) {
            throw new SecurityException("Client pairing Id or signature missing");
        }

        if (!verifySignature(clientLongTermPrivateKey.generatePublicKey(), clientSignature,
                concat(clientEphemeralPublicKey.getEncoded(), clientPairingId,
                        serverEphemeralSecretKey.generatePublicKey().getEncoded()))) {
            throw new SecurityException("Client signature invalid");
        }

        Map<Integer, byte[]> tlvOut = Map.of(TlvType.STATE.key, new byte[] { PairingState.M4.value });
        PairVerifyClient.Validator.validate(PairingMethod.VERIFY, tlvOut);

        // no further messages from server
        return Tlv8Codec.encode(tlvOut);
    }
}
