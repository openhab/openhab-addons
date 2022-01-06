/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.io.imperihome.internal.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Digest utility.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public final class DigestUtil {

    public static String sha1(String input) {
        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(input.getBytes(StandardCharsets.UTF_8));

            return new BigInteger(1, crypt.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Could not generate SHA-1 hash", e);
        }
    }

    // Hidden constructor
    private DigestUtil() {
    }
}
