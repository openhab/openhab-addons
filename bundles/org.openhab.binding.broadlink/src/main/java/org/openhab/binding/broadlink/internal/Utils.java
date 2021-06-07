/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.broadlink.internal;

import java.io.IOException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for working with the Broadlink devices.
 *
 * @author John Marshall/Cato Sognen - Initial contribution
 */
@NonNullByDefault
public class Utils {

    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    public static boolean isOnline(Thing thing) {
        return thing.getStatus().equals(ThingStatus.ONLINE);
    }

    public static boolean isOffline(Thing thing) {
        return thing.getStatus().equals(ThingStatus.OFFLINE);
    }

    public static byte[] slice(byte source[], int from, int to) throws IllegalArgumentException {
        if (from > to) {
            throw new IllegalArgumentException("Can't slice; from: " + from + " is larger than to: " + to);
        }
        if (to - from > source.length) {
            throw new IllegalArgumentException(
                    "Can't slice; from: " + from + " - to: " + to + " is longer than source length: " + source.length);
        }
        if (to == from) {
            byte sliced[] = new byte[1];
            sliced[0] = source[from];
            return sliced;
        } else {
            byte sliced[] = new byte[to - from];
            System.arraycopy(source, from, sliced, 0, to - from);
            return sliced;
        }
    }

    public static byte[] padTo(byte[] source, int quotient) {
        int modulo = source.length % quotient;
        if (modulo == 0) {
            return source;
        }

        int requiredNewSize = source.length + (quotient - modulo);
        byte[] padded = new byte[requiredNewSize];
        System.arraycopy(source, 0, padded, 0, source.length);

        return padded;
    }

    public static byte[] encrypt(byte key[], IvParameterSpec ivSpec, byte data[]) throws IOException {
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(1, secretKey, ivSpec);
            return cipher.doFinal(data);
        } catch (Exception e) {
            LOGGER.error("Exception while encrypting", e);
            throw new IOException(e);
        }
    }

    public static byte[] decrypt(byte key[], IvParameterSpec ivSpec, byte data[]) throws IOException {
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(2, secretKey, ivSpec);
            return cipher.doFinal(data);
        } catch (Exception e) {
            LOGGER.error("Exception while decrypting", e);
            throw new IOException(e);
        }
    }
}
