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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.homekit.internal.crypto.CryptoConstants.PS_M5_NONCE;
import static org.openhab.binding.homekit.internal.crypto.CryptoUtils.*;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Objects;

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.homekit.internal.crypto.SRPclient;
import org.openhab.binding.homekit.internal.crypto.Tlv8Codec;
import org.openhab.binding.homekit.internal.enums.PairingMethod;
import org.openhab.binding.homekit.internal.enums.PairingState;
import org.openhab.binding.homekit.internal.enums.TlvType;
import org.openhab.binding.homekit.internal.hap_services.PairSetupClient;
import org.openhab.binding.homekit.internal.transport.IpTransport;

/**
 * Test cases for the {@link PairSetupClient} class.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class TestPairSetup {

    public static final String SALT_HEX = """
            BEB25379 D1A8581E B5A72767 3A2441EE
            """;

    public static final String CLIENT_PRIVATE_HEX = """
            60975527 035CF2AD 1989806F 0407210B C81EDC04 E2762A56 AFD529DD DA2D4393
            """;

    public static final String SERVER_PRIVATE_HEX = """
            E487CB59 D31AC550 471E81F0 0F6928E0 1DDA08E9 74A004F4 9E61F5D1 05284D20
            """;

    private @NonNullByDefault({}) byte[] clientPublicKey;

    @Test
    void testBareCrypto() throws Exception {
        byte[] plainText0 = "the quick brown dog".getBytes(StandardCharsets.UTF_8);
        byte[] key = new byte[32]; // 256 bits = 32 bytes
        byte[] nonce = generateNonce(123);
        new SecureRandom().nextBytes(key);
        byte[] cipherText = encrypt(key, nonce, plainText0);
        byte[] plainText1 = decrypt(key, nonce, cipherText);
        assertArrayEquals(plainText0, plainText1);
    }

    @Test
    void testSrpClient() throws Exception {
        byte[] plainText0 = "the quick brown dog".getBytes(StandardCharsets.UTF_8);
        SRPclient client = new SRPclient("password123", toBytes(SALT_HEX), toBytes(SERVER_PRIVATE_HEX));
        byte[] key = client.getSymmetricKey();
        byte[] cipherText = encrypt(key, PS_M5_NONCE, plainText0);
        byte[] plainText1 = decrypt(key, PS_M5_NONCE, cipherText);
        assertArrayEquals(plainText0, plainText1);
    }

    @Test
    void testPairSetup() throws Exception {
        // initialize test parameters
        String password = "password123";
        String clientPairingIdentifier = "11:22:33:44:55:66";
        String serverPairingIdentifier = "66:55:44:33:22:11";
        byte[] serverSalt = toBytes(SALT_HEX);
        byte[] serverPairingId = serverPairingIdentifier.getBytes(StandardCharsets.UTF_8);

        // initialize signing keys
        Ed25519PrivateKeyParameters clientPrivateSigningKey = new Ed25519PrivateKeyParameters(
                toBytes(CLIENT_PRIVATE_HEX));
        Ed25519PrivateKeyParameters serverPrivateSigningKey = new Ed25519PrivateKeyParameters(
                toBytes(SERVER_PRIVATE_HEX));

        // create mock
        IpTransport mockTransport = mock(IpTransport.class);

        // create SRP client and server
        SRPserver server = new SRPserver(password, serverSalt, serverPairingId, serverPrivateSigningKey, null, null);
        PairSetupClient client = new PairSetupClient(mockTransport, clientPairingIdentifier, clientPrivateSigningKey,
                password);

        // mock the HTTP transport to simulate the SRP exchange
        doAnswer(invocation -> {
            byte[] arg = invocation.getArgument(2);

            // decode and validate the incoming TLV
            Map<Integer, byte[]> tlv = Tlv8Codec.decode(arg);
            PairSetupClient.Validator.validate(PairingMethod.SETUP, tlv);
            byte[] state = tlv.get(TlvType.STATE.key);
            if (state == null || state.length != 1) {
                throw new IllegalArgumentException("State missing or invalid");
            }

            // process the message based on the pairing process Mx state
            return switch (state[0]) {
                case 1 -> m1GetServerResponse(server, serverSalt);
                case 3 -> m3GetServerResponse(server, tlv, client);
                case 5 -> m5GetServerResponse(server);
                default -> throw new IllegalArgumentException("Unexpected state");
            };

        }).when(mockTransport).post(anyString(), anyString(), any(byte[].class));

        // execute the pairing setup
        client.pair();
    }

    private byte[] m1GetServerResponse(SRPserver server, byte[] serverSalt) {
        Map<Integer, byte[]> tlv = Map.of( //
                TlvType.STATE.key, new byte[] { PairingState.M2.value }, //
                TlvType.SALT.key, serverSalt, // salt
                TlvType.PUBLIC_KEY.key, toUnsigned(server.B, 384) // server public key
        );
        PairSetupClient.Validator.validate(PairingMethod.SETUP, tlv);
        return Tlv8Codec.encode(tlv);
    }

    private byte[] m3GetServerResponse(SRPserver server, Map<Integer, byte[]> tlv2, PairSetupClient client)
            throws Exception {
        clientPublicKey = tlv2.get(TlvType.PUBLIC_KEY.key);
        byte[] serverProof = server.m3CreateServerProof(Objects.requireNonNull(clientPublicKey));
        Map<Integer, byte[]> tlv3 = Map.of( //
                TlvType.STATE.key, new byte[] { PairingState.M4.value }, //
                TlvType.PROOF.key, serverProof // server proof
        );
        PairSetupClient.Validator.validate(PairingMethod.SETUP, tlv3);
        return Tlv8Codec.encode(tlv3);
    }

    private byte[] m5GetServerResponse(SRPserver server) throws Exception {
        byte[] cipherText = server.m5EncodeServerInfoAndSign();
        Map<Integer, byte[]> tlv = Map.of( //
                TlvType.STATE.key, new byte[] { PairingState.M6.value }, //
                TlvType.ENCRYPTED_DATA.key, cipherText);
        PairSetupClient.Validator.validate(PairingMethod.SETUP, tlv);
        return Tlv8Codec.encode(tlv);
    }
}
