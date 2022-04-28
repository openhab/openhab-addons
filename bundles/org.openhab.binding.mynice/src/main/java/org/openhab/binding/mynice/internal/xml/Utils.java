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
package org.openhab.binding.mynice.internal.xml;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class Utils {
    public static byte[] invertArray(byte[] data) {
        byte[] result = new byte[data.length];
        int i = data.length - 1;
        int c = 0;
        while (i >= 0) {
            int c2 = c + 1;
            result[c] = data[i];
            i--;
            c = c2;
        }
        return result;
    }

    public static byte[] sha256(byte[]... values) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        for (byte[] data : values) {
            digest.update(data);
        }
        return digest.digest();
    }
}
