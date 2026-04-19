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
package org.openhab.binding.homekit.internal;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.homekit.internal.crypto.CryptoConstants.*;
import static org.openhab.binding.homekit.internal.crypto.CryptoUtils.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.homekit.internal.crypto.SRPclient;
import org.openhab.binding.homekit.internal.crypto.Tlv8Codec;
import org.openhab.binding.homekit.internal.enums.PairingMethod;
import org.openhab.binding.homekit.internal.enums.PairingState;
import org.openhab.binding.homekit.internal.enums.TlvType;
import org.openhab.binding.homekit.internal.hapservices.PairSetupClient;
import org.openhab.binding.homekit.internal.transport.IpTransport;
import org.openhab.core.util.HexUtils;

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
    void testBareCrypto() throws InvalidCipherTextException {
        byte[] plainText0 = "the quick brown dog".getBytes(StandardCharsets.UTF_8);
        byte[] key = new byte[32]; // 256 bits = 32 bytes
        byte[] nonce64 = generateNonce64(123);
        new SecureRandom().nextBytes(key);
        byte[] cipherText = encrypt(key, nonce64, plainText0, new byte[0]);
        byte[] plainText1 = decrypt(key, nonce64, cipherText, new byte[0]);
        assertArrayEquals(plainText0, plainText1);
    }

    @Test
    void testSrpClient() throws InvalidCipherTextException, NoSuchAlgorithmException {
        byte[] plainText0 = "the quick brown dog".getBytes(StandardCharsets.UTF_8);
        SRPclient client = new SRPclient("password123", HexUtils.hexBlockToBytes(SALT_HEX),
                HexUtils.hexBlockToBytes(SERVER_PRIVATE_HEX));
        byte[] sharedKey = generateHkdfKey(client.K, PAIR_SETUP_ENCRYPT_SALT, PAIR_SETUP_ENCRYPT_INFO);
        byte[] cipherText = encrypt(sharedKey, PS_M5_NONCE, plainText0, new byte[0]);
        byte[] plainText1 = decrypt(sharedKey, PS_M5_NONCE, cipherText, new byte[0]);
        assertArrayEquals(plainText0, plainText1);
    }

    @Test
    void testPairSetup() throws NoSuchAlgorithmException, SecurityException, InvalidCipherTextException, IOException,
            InterruptedException, TimeoutException, ExecutionException, IllegalArgumentException {
        // initialize test parameters
        String password = "password123";
        byte[] iOSDeviceId = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };
        byte[] accessoryId = new byte[] { 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 };
        byte[] serverSalt = HexUtils.hexBlockToBytes(SALT_HEX);

        // initialize signing keys
        Ed25519PrivateKeyParameters controllerLongTermSecretKey = new Ed25519PrivateKeyParameters(
                HexUtils.hexBlockToBytes(CLIENT_PRIVATE_HEX));
        Ed25519PrivateKeyParameters accessoryLongTermSecretKey = new Ed25519PrivateKeyParameters(
                HexUtils.hexBlockToBytes(SERVER_PRIVATE_HEX));

        // create mock
        IpTransport mockTransport = mock(IpTransport.class);

        // create SRP client and server
        SRPserver server = new SRPserver(password, serverSalt, accessoryId, accessoryLongTermSecretKey, null, null);
        PairSetupClient client = new PairSetupClient(mockTransport, iOSDeviceId, controllerLongTermSecretKey, password,
                false);

        // mock the HTTP transport to simulate the SRP exchange
        doAnswer(invocation -> {
            byte[] arg = invocation.getArgument(2);

            // decode and validate the incoming TLV
            Map<Integer, byte[]> tlv = Tlv8Codec.decode(arg);
            PairSetupClient.Validator.validate(PairingMethod.SETUP, tlv);
            byte[] state = tlv.get(TlvType.STATE.value);
            if (state == null || state.length != 1) {
                throw new IllegalArgumentException("State missing or invalid");
            }

            // process the message based on the pairing process Mx state
            return switch (state[0]) {
                case 1 -> m1GetAccessoryResponse(server, serverSalt);
                case 3 -> m3GetAccessoryResponse(server, tlv, client);
                case 5 -> m5GetAccessoryResponse(server, tlv);
                default -> throw new IllegalArgumentException("Unexpected state");
            };

        }).when(mockTransport).post(anyString(), anyString(), any(byte[].class));

        // execute the pairing setup
        client.pair();
    }

    private byte[] m1GetAccessoryResponse(SRPserver server, byte[] serverSalt) {
        Map<Integer, byte[]> tlv = Map.of( //
                TlvType.STATE.value, new byte[] { PairingState.M2.value }, //
                TlvType.SALT.value, serverSalt, // salt
                TlvType.PUBLIC_KEY.value, toUnsigned(server.B, 384) // server public key
        );
        PairSetupClient.Validator.validate(PairingMethod.SETUP, tlv);
        return Tlv8Codec.encode(tlv);
    }

    private byte[] m3GetAccessoryResponse(SRPserver server, Map<Integer, byte[]> tlv2, PairSetupClient client)
            throws NoSuchAlgorithmException {
        clientPublicKey = tlv2.get(TlvType.PUBLIC_KEY.value);
        byte[] serverProof = server.m3CreateServerProof(Objects.requireNonNull(clientPublicKey));
        Map<Integer, byte[]> tlv3 = Map.of( //
                TlvType.STATE.value, new byte[] { PairingState.M4.value }, //
                TlvType.PROOF.value, serverProof // server proof
        );
        PairSetupClient.Validator.validate(PairingMethod.SETUP, tlv3);
        return Tlv8Codec.encode(tlv3);
    }

    private byte[] m5GetAccessoryResponse(SRPserver server, Map<Integer, byte[]> tlv5)
            throws InvalidCipherTextException {
        server.m5DecodeControllerInfoAndVerify(tlv5);
        byte[] cipherText = server.m6EncodeAccessoryInfoAndSign();
        Map<Integer, byte[]> tlv6 = Map.of( //
                TlvType.STATE.value, new byte[] { PairingState.M6.value }, //
                TlvType.ENCRYPTED_DATA.value, cipherText);
        PairSetupClient.Validator.validate(PairingMethod.SETUP, tlv6);
        return Tlv8Codec.encode(tlv6);
    }
}
