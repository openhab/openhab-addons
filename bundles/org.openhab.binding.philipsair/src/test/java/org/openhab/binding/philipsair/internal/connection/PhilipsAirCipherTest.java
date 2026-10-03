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
package org.openhab.binding.philipsair.internal.connection;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Test cases for {@link PhilipsAirCipher}. The tests
 * verifies basics of the key exchange procedure
 *
 * @author Michał Boroński - Initial contribution
 * @author Marcel Verpaalen - Code cleanup and align with guidelines
 */
@NonNullByDefault
public class PhilipsAirCipherTest {
    private static final String FAKE_KEY = "3344160F1A200827D1AFDC14A50C48DD";

    // Known answers of the key exchange for the private exponent below, computed independently of the binding with
    // Python (pow() with the generator and modulus of the protocol). The device encrypts its session key with the
    // first 16 bytes of the shared secret, encoded as 128 byte big-endian number, as AES key with a zero iv.
    static final BigInteger EXPONENT = new BigInteger("1D2C3B4A5968778695A4B3C2D1E0F00112233445566778899AABBCCDDEEFF01",
            16);
    static final String PUBLIC_VALUE = "75a03273cfb99fea2a178aa4fb0f5ab6396b252561fe64a8adb52afefaca49c964540ce817521db5d90c62d220f7d49d7de3c9bc1a67ce658ad9b7a4d2d4163bd3320328edc53ffde70109efe9de1af518ab5087eb7f1138e4c1e3e319016e07027a8c646685822ebdbf36618a8600649a9e14e8e390b70447273f75e6e07469";
    private static final String GENERATOR = "a4d1cbd5c3fd34126765a442efb99905f8104dd258ac507fd6406cff14266d31266fea1e5c41564b777e690f5504f213160217b4b01b886a5e91547f9e2749f4d7fbd7d3b9a92ee1909d0d2263f80a76a6a24c087a091f531dbf0a0169b6a28ad662a4d18e73afa32d779d5918d08bc8858f4dcef97c2a24855e6eeb22b3b2e5";
    static final String DEVICE_SESSION_KEY = "A1B2C3D4E5F60718293A4B5C6D7E8F90";
    // the shared secret has 1023 bits, so its two's complement encoding has an additional byte for the sign
    static final String HELLMAN_LONG_SECRET = "3e196114010368a0d5bfc5ec8d48df3861a3844578ebc7cef773ab85193effb002b7c3ceff14a854ecf6fbc5603ac45f791c8633141e6d2185e0ba5d347495a25f7c27024a75416fe3aa9d05562bc546f0d02f03ed11c3741675dfb1a8999656c8972fc662ce630f28bd196a2f9f471a8e661764a59e5044ea905ad47980158f";
    static final String ENCRYPTED_KEY_LONG_SECRET = "89acc93b8339c16b74de09dd79ffdb5cbe7ddf83f15ecce3830ce7089517d2f8";
    // the shared secret has 1008 bits, so it is shorter than 128 bytes
    static final String HELLMAN_SHORT_SECRET = "53923a564baa3f244a65b4255428abb9d6534774c658f884fdad9adbc5442ba2b816345f6d4a4ba7eb60eed63b7271c7bf57a29fd13f1fed650aa4ad006319fdd54b6bc82130feab48bac5d07ec5a99b8c596c159400c1846683f18e0f65d851b1942004f1bf1c2f3e361b92e28b190aa33c069edfd3d2f2da881de282605f81";
    static final String ENCRYPTED_KEY_SHORT_SECRET = "55ac82d2fc9e67b067e6189719fcf2529169381f5724762162332670f2673a47";

    private PhilipsAirCipher cipher() throws GeneralSecurityException {
        return new PhilipsAirCipher(BigInteger.ONE);
    }

    @Test
    public void sessionKeyIsCalculatedForTrivialExponent() throws GeneralSecurityException {
        String aes = cipher().calculateKey(
                "18895f807c53c2576f344365e50e7f3f5f6f061dab736a057c07defc1f337410443ee0d4eadf8d07ff533bbcd316dbbf9cc578154ace1fd97db997db8ebf2a75c0e31c2a23b4e4774ad37e374c73a8f158f2a102f51cd0c3e0638979779a264610dd9486134047752bb8380a8afece6e4d93c22ecb4c203f8ae1e3fc7eb217b2",
                "b18e96ee433d4bbd93e38d588e51566cba2c2c95fd440131cd428b7390ef3dfd");

        assertEquals(FAKE_KEY, aes);
    }

    @Test
    public void publicValueIsCalculatedFromExponent() throws GeneralSecurityException {
        assertEquals(PUBLIC_VALUE, new PhilipsAirCipher(EXPONENT).getApow());
    }

    @Test
    public void publicValueOfTrivialExponentIsGenerator() throws GeneralSecurityException {
        assertEquals(GENERATOR, cipher().getApow());
    }

    @Test
    public void sessionKeyIsCalculatedForSecretWithSignByte() throws GeneralSecurityException {
        PhilipsAirCipher cipher = new PhilipsAirCipher(EXPONENT);

        assertEquals(DEVICE_SESSION_KEY, cipher.calculateKey(HELLMAN_LONG_SECRET, ENCRYPTED_KEY_LONG_SECRET));
    }

    @Test
    public void sessionKeyIsCalculatedForShortSecret() throws GeneralSecurityException {
        PhilipsAirCipher cipher = new PhilipsAirCipher(EXPONENT);

        assertEquals(DEVICE_SESSION_KEY, cipher.calculateKey(HELLMAN_SHORT_SECRET, ENCRYPTED_KEY_SHORT_SECRET));
    }

    @Test
    public void shortValueIsPaddedAtTheLeft() {
        byte[] fixed = PhilipsAirCipher.toFixedLength(new BigInteger("0102", 16));

        assertEquals(128, fixed.length);
        assertEquals(1, fixed[126]);
        assertEquals(2, fixed[127]);
        assertArrayEquals(new byte[126], Arrays.copyOfRange(fixed, 0, 126));
    }

    @Test
    public void valueOfExactLengthIsNotChanged() {
        byte[] expected = new byte[128];
        Arrays.fill(expected, (byte) 0x11);
        expected[0] = 0x7F;
        BigInteger value = new BigInteger(1, expected);
        assertEquals(128, value.toByteArray().length);

        assertArrayEquals(expected, PhilipsAirCipher.toFixedLength(value));
    }

    @Test
    public void leadingSignByteIsDropped() {
        byte[] expected = new byte[128];
        Arrays.fill(expected, (byte) 0x22);
        expected[0] = (byte) 0x80;
        BigInteger value = new BigInteger(1, expected);
        // a positive number with the highest bit set has a leading zero byte in its two's complement encoding
        assertEquals(129, value.toByteArray().length);

        assertArrayEquals(expected, PhilipsAirCipher.toFixedLength(value));
    }

    @Test
    public void messageIsDecrypted() throws GeneralSecurityException {
        PhilipsAirCipher cipher = cipher();
        cipher.initKey(FAKE_KEY);

        assertEquals("{\"ddp\":\"0\"}", cipher.decrypt("765kW9EGhHMhtzJ/rxeyIg=="));
    }

    @Test
    public void messageIsEncrypted() throws GeneralSecurityException {
        PhilipsAirCipher cipher = cipher();
        cipher.initKey(FAKE_KEY);

        assertEquals("765kW9EGhHMhtzJ/rxeyIg==", cipher.encrypt("{\"ddp\":\"0\"}"));
    }

    @Test
    public void nonAsciiTextSurvivesRoundTrip() throws GeneralSecurityException {
        PhilipsAirCipher cipher = cipher();
        cipher.initKey(FAKE_KEY);
        String status = "{\"name\":\"Wohnzimmer Küche\"}";

        assertEquals(status, cipher.decrypt(Objects.requireNonNull(cipher.encrypt(status))));
    }

    @Test
    public void usingTheCipherBeforeInitKeyFails() throws GeneralSecurityException {
        PhilipsAirCipher uninitialized = cipher();

        assertThrows(IllegalStateException.class, () -> uninitialized.encrypt("{}"));
        assertThrows(IllegalStateException.class, () -> uninitialized.decrypt("765kW9EGhHMhtzJ/rxeyIg=="));
    }
}
