/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test cases for {@link PhilipsAirCipher}. The tests
 * verifies basics of the key exchange procedure
 *
 * @author michalboronski - Initial contribution
 */
public class PhilipsAirCipherTest {
    private static PhilipsAirCipher cipher = null;
    private static final String FAKE_KEY = "3344160F1A200827D1AFDC14A50C48DD";

    @BeforeAll
    public static void init() throws GeneralSecurityException {
        cipher = new PhilipsAirCipher(BigInteger.ONE);
    }

    @BeforeEach
    public void setUp() throws Exception {
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    @Test
    public void testCalculateKey() throws InvalidAlgorithmParameterException, GeneralSecurityException,
            InterruptedException, TimeoutException, ExecutionException, UnsupportedEncodingException {

        String aes = cipher.calculateKey(
                "18895f807c53c2576f344365e50e7f3f5f6f061dab736a057c07defc1f337410443ee0d4eadf8d07ff533bbcd316dbbf9cc578154ace1fd97db997db8ebf2a75c0e31c2a23b4e4774ad37e374c73a8f158f2a102f51cd0c3e0638979779a264610dd9486134047752bb8380a8afece6e4d93c22ecb4c203f8ae1e3fc7eb217b2",
                "b18e96ee433d4bbd93e38d588e51566cba2c2c95fd440131cd428b7390ef3dfd");

        assertEquals(FAKE_KEY, aes);
    }

    @Test
    public void testDecrypt() throws GeneralSecurityException {
        cipher.initKey(FAKE_KEY);
        assertEquals("{\"ddp\":\"0\"}", cipher.decrypt("765kW9EGhHMhtzJ/rxeyIg=="));
    }

    @Test
    public void testEncrypt() throws GeneralSecurityException, UnsupportedEncodingException {
        cipher.initKey(FAKE_KEY);
        assertEquals("765kW9EGhHMhtzJ/rxeyIg==", cipher.encrypt("{\"ddp\":\"0\"}"));
    }
}
