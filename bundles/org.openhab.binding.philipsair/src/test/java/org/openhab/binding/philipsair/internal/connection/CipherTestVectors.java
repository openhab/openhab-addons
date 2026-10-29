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

import java.math.BigInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Known answers of the key exchange, shared by the tests of the cipher and of the connection that uses it.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
final class CipherTestVectors {

    private CipherTestVectors() {
    }

    // Known answers of the key exchange for the private exponent below, computed independently of the binding with
    // Python (pow() with the generator and modulus of the protocol). The device encrypts its session key with the
    // first 16 bytes of the shared secret, encoded as 128 byte big-endian number, as AES key with a zero iv.
    static final BigInteger EXPONENT = new BigInteger("1D2C3B4A5968778695A4B3C2D1E0F00112233445566778899AABBCCDDEEFF01",
            16);
    static final String PUBLIC_VALUE = "75a03273cfb99fea2a178aa4fb0f5ab6396b252561fe64a8adb52afefaca49c964540ce817521db5d90c62d220f7d49d7de3c9bc1a67ce658ad9b7a4d2d4163bd3320328edc53ffde70109efe9de1af518ab5087eb7f1138e4c1e3e319016e07027a8c646685822ebdbf36618a8600649a9e14e8e390b70447273f75e6e07469";
    static final String DEVICE_SESSION_KEY = "A1B2C3D4E5F60718293A4B5C6D7E8F90";
    // the shared secret has 1023 bits, so its two's complement encoding has an additional byte for the sign
    static final String HELLMAN_LONG_SECRET = "3e196114010368a0d5bfc5ec8d48df3861a3844578ebc7cef773ab85193effb002b7c3ceff14a854ecf6fbc5603ac45f791c8633141e6d2185e0ba5d347495a25f7c27024a75416fe3aa9d05562bc546f0d02f03ed11c3741675dfb1a8999656c8972fc662ce630f28bd196a2f9f471a8e661764a59e5044ea905ad47980158f";
    static final String ENCRYPTED_KEY_LONG_SECRET = "89acc93b8339c16b74de09dd79ffdb5cbe7ddf83f15ecce3830ce7089517d2f8";
}
