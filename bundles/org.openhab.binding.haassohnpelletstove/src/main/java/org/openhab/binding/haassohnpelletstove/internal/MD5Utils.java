/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.haassohnpelletstove.internal;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link MD5Utils} is responsible for generating the MD5 hash
 *
 *
 * @author Christian Feininger - Initial contribution
 */
@NonNullByDefault
public class MD5Utils {

    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    private static byte[] digest(byte[] input) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
        byte[] result = md.digest(input);
        return result;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /***
     * Returns an encrypted MD5 string
     *
     * @param input nonce as input
     * @return Encrypted String
     */
    public static String getMD5String(@Nullable String input) {
        if (input != null) {
            byte[] md5InBytes = MD5Utils.digest(input.getBytes(UTF_8));
            return bytesToHex(md5InBytes);
        }
        return "";
    }
}
