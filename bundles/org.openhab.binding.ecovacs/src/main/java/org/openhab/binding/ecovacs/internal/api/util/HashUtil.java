/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.ecovacs.internal.api.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Johannes Ptaszyk - Initial contribution
 */
@NonNullByDefault
public class HashUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(HashUtil.class);

    private HashUtil() {
        // Prevent instantiation of util class
    }

    public static String getMD5Hash(String input) {
        return calculateHash("MD5", input);
    }

    public static String getSHA256Hash(String input) {
        return calculateHash("SHA-256", input);
    }

    private static String calculateHash(String algorithm, String input) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Could not get {} MessageDigest instance", algorithm, e);
            return "";
        }
        md.update(input.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte b : md.digest()) {
            if ((b & 0xff) < 0x10) {
                hexString.append("0");
            }
            hexString.append(Integer.toHexString(b & 0xff));
        }
        return hexString.toString();
    }
}
