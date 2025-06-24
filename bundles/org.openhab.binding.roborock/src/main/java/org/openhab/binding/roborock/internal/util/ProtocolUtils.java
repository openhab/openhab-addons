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
package org.openhab.binding.roborock.internal.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Johannes Ptaszyk - Initial contribution
 */
@NonNullByDefault
public class ProtocolUtils {
    private ProtocolUtils() {
        // Prevent instantiation of util class
    }

    public static String md5Hex(String data) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            // should never occur
            return "";
        }
        byte[] array = md.digest(data.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : array) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
