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
package org.openhab.binding.semsportal.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * @author Julio Vilmar Gesser - Initial contribution
 */
@NonNullByDefault
public class SEMSSecurityTest {

    @Test
    public void testHashPassword() {
        // Base64 of the hexadecimal MD5 hash, so of '5f4dcc3b5aa765d61d8327deb882cf99'
        assertEquals("NWY0ZGNjM2I1YWE3NjVkNjFkODMyN2RlYjg4MmNmOTk=", SEMSSecurity.hashPassword("password"));
    }

    @Test
    public void testSignature() {
        String signature = SEMSSecurity.signature("u", "t", 1700000000000L);
        assertEquals(
                "YzNjMzU2ZTlmNzlmMDI4NjA2YWM3M2FjMzY5NzVjNTA3NGYwOTQ4MWMwYjY1NDBiOTcyYzk2ZGY0NmI2MTdiM0AxNzAwMDAwMDAwMDAw",
                signature);
        // SHA-256 of '1700000000000@u@t' in hexadecimal, followed by the timestamp
        assertEquals("c3c356e9f79f028606ac73ac36975c5074f09481c0b6540b972c96df46b617b3@1700000000000",
                new String(Base64.getDecoder().decode(signature), StandardCharsets.UTF_8));
    }

    @Test
    public void testSignatureWithoutSession() {
        // the login is signed with an empty token
        String signature = new String(Base64.getDecoder().decode(SEMSSecurity.signature("", "", 1L)),
                StandardCharsets.UTF_8);
        assertEquals(64 + "@1".length(), signature.length());
    }
}
