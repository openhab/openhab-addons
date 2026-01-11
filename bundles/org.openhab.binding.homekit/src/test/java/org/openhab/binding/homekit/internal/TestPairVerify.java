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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.homekit.internal.crypto.CryptoConstants.*;
import static org.openhab.binding.homekit.internal.crypto.CryptoUtils.*;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.homekit.internal.crypto.Tlv8Codec;
import org.openhab.binding.homekit.internal.enums.PairingMethod;
import org.openhab.binding.homekit.internal.enums.PairingState;
import org.openhab.binding.homekit.internal.enums.TlvType;
import org.openhab.binding.homekit.internal.hapservices.PairVerifyClient;
import org.openhab.binding.homekit.internal.transport.IpTransport;
import org.openhab.core.util.HexUtils;

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

    byte[] controllerId = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };
    byte[] accessoryId = new byte[] { 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 };

    private final Ed25519PrivateKeyParameters controllerLongTermPrivateKey = new Ed25519PrivateKeyParameters(
            HexUtils.hexBlockToBytes(CLIENT_PRIVATE_HEX));

    private final Ed25519PrivateKeyParameters accessoryLongTermPrivateKey = new Ed25519PrivateKeyParameters(
            HexUtils.hexBlockToBytes(SERVER_PRIVATE_HEX));

    private @NonNullByDefault({}) X25519PrivateKeyParameters accessoryEphemeralSecretKey;
    private @NonNullByDefault({}) X25519PublicKeyParameters controllerEphemeralPublicKey;
    private @NonNullByDefault({}) byte[] cryptoKey;

    @Test
    void testPairVerify() throws InvalidCipherTextException, IOException, InterruptedException, TimeoutException,
            ExecutionException, NoSuchAlgorithmException, NoSuchProviderException, IllegalArgumentException {
        accessoryEphemeralSecretKey = generateX25519KeyPair();

        // create mock
        IpTransport mockTransport = mock(IpTransport.class);

        // create SRP client and server
        PairVerifyClient client = new PairVerifyClient(mockTransport, controllerId, controllerLongTermPrivateKey,
                accessoryLongTermPrivateKey.generatePublicKey());

        // mock the HTTP transport to simulate the SRP exchange
        doAnswer(invocation -> {
            byte[] arg = invocation.getArgument(2);

            // decode and validate the incoming TLV
            Map<Integer, byte[]> tlv = Tlv8Codec.decode(arg);
            PairVerifyClient.Validator.validate(PairingMethod.VERIFY, tlv);
            byte[] state = tlv.get(TlvType.STATE.value);
            if (state == null || state.length != 1) {
                throw new IllegalArgumentException("State missing or invalid");
            }

            // process the message based on the pair verification process Mx state
            return switch (state[0]) {
                case 1 -> m1GetAccessoryResponse(tlv);
                case 3 -> m3GetAccessoryResponse(tlv);
                default -> throw new IllegalArgumentException("Unexpected state");
            };

        }).when(mockTransport).post(anyString(), anyString(), any(byte[].class));

        // execute the pairing verification process
        client.verify();
    }

    private byte[] m1GetAccessoryResponse(Map<Integer, byte[]> tlv) throws InvalidCipherTextException {
        byte[] controllerEphemeralPublicKey = tlv.get(TlvType.PUBLIC_KEY.value);
        byte[] accessoryEphemeralPublicKey = accessoryEphemeralSecretKey.generatePublicKey().getEncoded();
        if (controllerEphemeralPublicKey == null) {
            throw new SecurityException("Client public key missing");
        }
        byte[] accessorySignature = signMessage(accessoryLongTermPrivateKey,
                concat(accessoryEphemeralPublicKey, accessoryId, controllerEphemeralPublicKey));

        Map<Integer, byte[]> tlvInner = Map.of( //
                TlvType.IDENTIFIER.value, accessoryId, //
                TlvType.SIGNATURE.value, accessorySignature);

        this.controllerEphemeralPublicKey = new X25519PublicKeyParameters(controllerEphemeralPublicKey);

        byte[] sharedSecret = generateSharedSecret(accessoryEphemeralSecretKey, this.controllerEphemeralPublicKey);
        cryptoKey = generateHkdfKey(sharedSecret, PAIR_VERIFY_ENCRYPT_SALT, PAIR_VERIFY_ENCRYPT_INFO);

        byte[] plainText = Tlv8Codec.encode(tlvInner);
        byte[] cipherText = encrypt(cryptoKey, PV_M2_NONCE, plainText, new byte[0]);

        Map<Integer, byte[]> tlvOut = Map.of( //
                TlvType.STATE.value, new byte[] { PairingState.M2.value }, //
                TlvType.PUBLIC_KEY.value, accessoryEphemeralPublicKey, //
                TlvType.ENCRYPTED_DATA.value, cipherText);

        return Tlv8Codec.encode(tlvOut);
    }

    private byte[] m3GetAccessoryResponse(Map<Integer, byte[]> tlv) throws InvalidCipherTextException {
        if (cryptoKey.length == 0) {
            throw new IllegalStateException("Session key not established");
        }
        byte[] cipherText = tlv.get(TlvType.ENCRYPTED_DATA.value);
        if (cipherText == null) {
            throw new SecurityException("Server cipher text missing");
        }
        byte[] plainText = decrypt(cryptoKey, PV_M3_NONCE, Objects.requireNonNull(cipherText), new byte[0]);

        Map<Integer, byte[]> subTlv = Tlv8Codec.decode(plainText);
        byte[] controllerId = subTlv.get(TlvType.IDENTIFIER.value);
        byte[] controllerSignature = subTlv.get(TlvType.SIGNATURE.value);
        if (controllerId == null || controllerSignature == null) {
            throw new SecurityException("Controller Id or signature missing");
        }

        byte[] controllerInfo = concat(controllerEphemeralPublicKey.getEncoded(), controllerId,
                accessoryEphemeralSecretKey.generatePublicKey().getEncoded());
        verifySignature(controllerLongTermPrivateKey.generatePublicKey(), controllerSignature, controllerInfo);

        Map<Integer, byte[]> tlvOut = Map.of(TlvType.STATE.value, new byte[] { PairingState.M4.value });
        PairVerifyClient.Validator.validate(PairingMethod.VERIFY, tlvOut);

        // no further messages from server
        return Tlv8Codec.encode(tlvOut);
    }
}
