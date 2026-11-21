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
package org.openhab.binding.atv.internal.client.auth;

import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.atv.internal.client.exceptions.AuthenticationError;
import org.openhab.binding.atv.internal.client.exceptions.InvalidCredentialsError;
import org.openhab.binding.atv.internal.client.support.Tlv8;
import org.openhab.binding.atv.internal.client.support.Tlv8.TlvValue;

/**
 * Tests for the M4 check in {@link HapPairVerify} and for keeping secrets out of credential
 * errors.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class HapPairVerifyTest {

    private static final HapCredentials CREDENTIALS = new HapCredentials(new byte[32], new byte[32], new byte[] { 1 },
            new byte[] { 2 });

    @Test
    public void acceptsM4WithoutError() {
        HapPairVerify verifier = new HapPairVerify(CREDENTIALS, new byte[32]);

        assertDoesNotThrow(() -> verifier.verify3(m4(null)));
    }

    @Test
    public void rejectsM4WithError() {
        HapPairVerify verifier = new HapPairVerify(CREDENTIALS, new byte[32]);

        // 0x02 is kTLVError_Authentication
        assertThrows(AuthenticationError.class, () -> verifier.verify3(m4((byte) 0x02)));
    }

    @Test
    public void credentialErrorsDoNotContainSecret() {
        String secret = "00112233445566778899aabbccddeeff";

        InvalidCredentialsError wrongFields = assertThrows(InvalidCredentialsError.class,
                () -> HapCredentials.parse(secret + ":" + secret + ":" + secret));
        InvalidCredentialsError badHex = assertThrows(InvalidCredentialsError.class,
                () -> HapCredentials.parse("zz" + secret + ":" + secret));

        assertFalse(String.valueOf(wrongFields.getMessage()).contains(secret));
        assertFalse(String.valueOf(badHex.getMessage()).contains(secret));
        assertNull(badHex.getCause());
    }

    private static byte[] m4(@Nullable Byte error) {
        Map<Integer, byte[]> tlv = new LinkedHashMap<>();
        tlv.put(TlvValue.SeqNo.value(), new byte[] { (byte) Tlv8.State.M4.value() });
        if (error != null) {
            tlv.put(TlvValue.Error.value(), new byte[] { error });
        }
        return Tlv8.write(tlv);
    }
}
