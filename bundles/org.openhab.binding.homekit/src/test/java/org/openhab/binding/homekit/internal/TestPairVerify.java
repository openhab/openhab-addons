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

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;
import org.bouncycastle.util.Arrays;
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

    private final String clientPairingIdentifier = "11:22:33:44:55:66";
    private final byte[] clientPairingId = clientPairingIdentifier.getBytes(StandardCharsets.UTF_8);
    private final String serverPairingIdentifier = "66:55:44:33:22:11";
    private final byte[] serverPairingId = serverPairingIdentifier.getBytes(StandardCharsets.UTF_8);

    private final Ed25519PrivateKeyParameters clientLongTermPrivateKey = new Ed25519PrivateKeyParameters(
            fromHex(CLIENT_PRIVATE_HEX));

    private final Ed25519PrivateKeyParameters serverLongTermPrivateKey = new Ed25519PrivateKeyParameters(
            fromHex(SERVER_PRIVATE_HEX));

    private @NonNullByDefault({}) X25519PrivateKeyParameters serverKey;
    private @NonNullByDefault({}) X25519PublicKeyParameters clientKey;
    private @NonNullByDefault({}) byte[] sessionKey;

    @Test
    void testPairVerify() throws Exception {
        serverKey = generateX25519KeyPair();

        // create mock
        IpTransport mockTransport = mock(IpTransport.class);

        // create SRP client and server
        PairVerifyClient client = new PairVerifyClient(mockTransport, clientPairingIdentifier, clientLongTermPrivateKey,
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
                case 1 -> getServerResponseM1(tlv);
                case 3 -> getServerResponseM3(tlv);
                default -> throw new IllegalArgumentException("Unexpected state");
            };

        }).when(mockTransport).post(anyString(), anyString(), any(byte[].class));

        // execute the pairing verification process
        client.verify();
    }

    private byte[] getServerResponseM1(Map<Integer, byte[]> tlv) throws Exception {
        byte[] clientKeyBytes = tlv.get(TlvType.PUBLIC_KEY.key);
        byte[] serverKeyBytes = serverKey.generatePublicKey().getEncoded();
        byte[] payload = concat(serverKeyBytes, serverPairingId, Objects.requireNonNull(clientKeyBytes));
        byte[] signature = signMessage(serverLongTermPrivateKey, payload);

        Map<Integer, byte[]> tlvInner = Map.of( //
                TlvType.IDENTIFIER.key, serverPairingId, //
                TlvType.SIGNATURE.key, signature);

        clientKey = new X25519PublicKeyParameters(clientKeyBytes);

        byte[] sharedSecret = generateSharedSecret(serverKey, clientKey);
        sessionKey = generateHkdfKey(sharedSecret, PAIR_VERIFY_ENCRYPT_SALT, PAIR_VERIFY_ENCRYPT_INFO);

        byte[] plaintext = Tlv8Codec.encode(tlvInner);
        byte[] ciphertext = encrypt(sessionKey, PV_M2_NONCE, plaintext);

        Map<Integer, byte[]> tlvOut = Map.of( //
                TlvType.STATE.key, new byte[] { PairingState.M2.value }, //
                TlvType.PUBLIC_KEY.key, serverKey.generatePublicKey().getEncoded(), //
                TlvType.ENCRYPTED_DATA.key, ciphertext);

        return Tlv8Codec.encode(tlvOut);
    }

    private byte[] getServerResponseM3(Map<Integer, byte[]> tlv) throws Exception {
        if (sessionKey.length == 0) {
            throw new IllegalStateException("Session key not established");
        }
        byte[] ciphertext = tlv.get(TlvType.ENCRYPTED_DATA.key);
        if (ciphertext == null) {
            throw new SecurityException("Missing ciphertext in M3");
        }
        byte[] plaintext = decrypt(sessionKey, PV_M3_NONCE, Objects.requireNonNull(ciphertext));

        Map<Integer, byte[]> subTlv = Tlv8Codec.decode(plaintext);
        byte[] information = subTlv.get(TlvType.IDENTIFIER.key);
        byte[] signature = subTlv.get(TlvType.SIGNATURE.key);
        if (information == null || signature == null) {
            throw new SecurityException("Client pairing ID or signature missing");
        }

        verifySignature(clientLongTermPrivateKey.generatePublicKey(), plaintext, Objects.requireNonNull(signature));
        byte[] pairingId = Arrays.copyOfRange(information, 32, information.length - 32);
        if (!Arrays.areEqual(clientPairingId, pairingId)) {
            throw new SecurityException("Client pairing ID does not match");
        }

        Map<Integer, byte[]> tlvOut = Map.of(TlvType.STATE.key, new byte[] { PairingState.M4.value });
        PairVerifyClient.Validator.validate(PairingMethod.VERIFY, tlvOut);

        // no further messages from server
        return Tlv8Codec.encode(tlvOut);
    }
}
