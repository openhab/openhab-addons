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

import java.util.Map;

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.homekit.internal.crypto.Tlv8Codec;
import org.openhab.binding.homekit.internal.enums.PairingMethod;
import org.openhab.binding.homekit.internal.enums.PairingState;
import org.openhab.binding.homekit.internal.enums.TlvType;
import org.openhab.binding.homekit.internal.hap_services.PairSetupClient;
import org.openhab.binding.homekit.internal.transport.HttpTransport;

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

    @Test
    void testPairSetup() throws Exception {
        // initialize test parameters
        String baseUrl = "http://example.com";
        String username = "alice";
        String password = "password123";
        String clientIdentifier = "11:22:33:44:55:66";
        String serverIdentifier = "AA:BB:CC:DD:EE:FF";
        byte[] serverSalt = hexBlockToByteArray(SALT_HEX);

        // initialize signing keys
        Ed25519PrivateKeyParameters clientPrivateSigningKey = new Ed25519PrivateKeyParameters(
                hexBlockToByteArray(CLIENT_PRIVATE_HEX));
        Ed25519PrivateKeyParameters serverPrivateSigningKey = new Ed25519PrivateKeyParameters(
                hexBlockToByteArray(SERVER_PRIVATE_HEX));

        // create mock
        HttpTransport mockTransport = mock(HttpTransport.class);

        // create SRP client and server
        SRPserver server = new SRPserver(username, password, serverSalt);
        PairSetupClient client = new PairSetupClient(mockTransport, baseUrl, clientIdentifier, clientPrivateSigningKey, username,
                password);

        // mock the HTTP transport to simulate the SRP exchange
        doAnswer(invocation -> {
            byte[] arg = invocation.getArgument(3);

            // decode and validate the incoming TLV
            Map<Integer, byte[]> tlv = Tlv8Codec.decode(arg);
            PairSetupClient.Validator.validate(PairingMethod.SETUP, tlv);
            byte[] state = tlv.get(TlvType.STATE.key);
            if (state == null || state.length != 1) {
                throw new IllegalArgumentException("State missing or invalid");
            }

            // process the message based on the pairing process Mx state
            return switch (state[0]) {
                case 1 -> getServerResponseM1(server, serverSalt);
                case 3 -> getServerResponseM3(server, client);
                case 5 -> getServerResponseM5(server, serverIdentifier, serverPrivateSigningKey);
                default -> throw new IllegalArgumentException("Unexpected state");
            };

        }).when(mockTransport).post(anyString(), anyString(), anyString(), any(byte[].class));

        // execute the pairing setup
        client.pair();
    }

    private byte[] getServerResponseM1(SRPserver server, byte[] serverSalt) {
        Map<Integer, byte[]> tlv = Map.of( //
                TlvType.STATE.key, new byte[] { PairingState.M2.value }, //
                TlvType.SALT.key, serverSalt, // salt
                TlvType.PUBLIC_KEY.key, server.getPublicKey() // server public key
        );

        PairSetupClient.Validator.validate(PairingMethod.SETUP, tlv);
        return Tlv8Codec.encode(tlv);
    }

    private byte[] getServerResponseM3(SRPserver server, PairSetupClient client) throws Exception {
        byte[] serverProof = server.computeServerProof(client.getPublicKey());

        Map<Integer, byte[]> tlv = Map.of( //
                TlvType.STATE.key, new byte[] { PairingState.M4.value }, //
                TlvType.PROOF.key, serverProof // server proof
        );

        PairSetupClient.Validator.validate(PairingMethod.SETUP, tlv);
        return Tlv8Codec.encode(tlv);
    }

    private byte[] getServerResponseM5(SRPserver server, String serverIdentifier,
            Ed25519PrivateKeyParameters serverPrivateSigningKey) throws Exception {
        byte[] serverEncyptedData = server.createEncryptedData(serverIdentifier, server.getPublicKey(),
                serverPrivateSigningKey);

        Map<Integer, byte[]> tlv = Map.of( //
                TlvType.STATE.key, new byte[] { PairingState.M6.value }, //
                TlvType.ENCRYPTED_DATA.key, serverEncyptedData);

        PairSetupClient.Validator.validate(PairingMethod.SETUP, tlv);
        return Tlv8Codec.encode(tlv);
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
}
