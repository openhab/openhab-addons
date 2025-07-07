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
package org.openhab.binding.meross.internal.api;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link MD5Util} is responsible for generating the MD5 hash
 *
 *
 * @author Christian Feininger - Initial contribution
 */
@NonNullByDefault
public class MD5Util {
    private static byte[] digest(byte[] input) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
        return md.digest(input);
    }

    private static String bytesToString(byte[] bytes) {
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
    public static String getMD5String(String input) {
        byte[] md5InBytes = MD5Util.digest(input.getBytes(StandardCharsets.UTF_8));
        return bytesToString(md5InBytes);
    }
}
