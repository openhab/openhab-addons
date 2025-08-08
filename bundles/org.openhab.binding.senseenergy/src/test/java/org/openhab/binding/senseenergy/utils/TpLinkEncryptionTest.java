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
package org.openhab.binding.senseenergy.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.UnsupportedEncodingException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * {@link TpLinkEncryptionTest }
 *
 * @author Jeff James - Initial contribution
 *
 */
@NonNullByDefault
class TpLinkEncryptionTest {

    @Test
    void test() throws UnsupportedEncodingException {
        String input = new String("Hello, this is a great day!");

        byte[] encrypted = TpLinkEncryption.encrypt(input.getBytes("UTF-8"), 0);

        byte[] unencrypted = TpLinkEncryption.decrypt(encrypted, 0);

        assertEquals(input, new String(unencrypted, "UTF-8"));
    }
}
