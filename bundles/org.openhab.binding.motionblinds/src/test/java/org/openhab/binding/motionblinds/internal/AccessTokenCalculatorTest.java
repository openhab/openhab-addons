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
package org.openhab.binding.motionblinds.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link AccessTokenCalculator}.
 *
 * @author Oleksandr Mishchuk - Initial contribution
 */
@NonNullByDefault
public class AccessTokenCalculatorTest {

    @Test
    public void calculateMatchesOpenSslReference() {
        // printf '%s' "$TOKEN" | openssl enc -aes-128-ecb -nopad -K <hex of key>
        assertEquals("4D098FF506E81A40DB823D3F60333214",
                AccessTokenCalculator.calculate("12ab345c-d67e-8f", "5Yx9TSv3kUq7Hn2a"));
    }

    @Test
    public void calculateRejectsInvalidLengths() {
        assertThrows(IllegalArgumentException.class,
                () -> AccessTokenCalculator.calculate("12ab345cd67e8f", "5Yx9TSv3kUq7Hn2a"));
        assertThrows(IllegalArgumentException.class,
                () -> AccessTokenCalculator.calculate("12ab345c-d67e-8f", "short"));
    }
}
