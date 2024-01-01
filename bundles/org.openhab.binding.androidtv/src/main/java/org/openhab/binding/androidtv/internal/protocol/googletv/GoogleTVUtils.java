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
package org.openhab.binding.androidtv.internal.protocol.googletv;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPublicKey;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GoogleTVCommand represents a GoogleTV protocol command
 *
 * @author Ben Rosenblum - Initial contribution
 */
@NonNullByDefault
public class GoogleTVUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleTVUtils.class);

    private static String processMag(final byte[] magnitude) {
        final int length = magnitude.length;
        if (length != 0) {
            final BigInteger bigInteger = new BigInteger(1, magnitude);
            final StringBuilder sb = new StringBuilder();
            sb.append("%0");
            sb.append(length + length);
            sb.append("x");
            return String.format(sb.toString(), bigInteger);
        }
        return "";
    }

    private static final byte[] processDigestArray(final byte[] array) {
        int n = 0;
        int length;
        while (true) {
            length = array.length;
            if (n >= length || array[n] != 0) {
                break;
            }
            ++n;
        }
        final int n2 = length - n;
        final byte[] array2 = new byte[n2];
        System.arraycopy(array, n, array2, 0, n2);
        return array2;
    }

    public static final byte[] processDigest(byte[] digest, Certificate clientCert, Certificate serverCert) {
        final PublicKey clientPublicKey = clientCert.getPublicKey();
        final PublicKey serverPublicKey = serverCert.getPublicKey();
        processMag(digest);
        if (clientPublicKey instanceof RSAPublicKey && serverPublicKey instanceof RSAPublicKey) {
            final RSAPublicKey clientRSAPublicKey = (RSAPublicKey) clientPublicKey;
            final RSAPublicKey serverRSAPublicKey = (RSAPublicKey) serverPublicKey;
            try {
                final MessageDigest instance = MessageDigest.getInstance("SHA-256");
                final byte[] byteArray1 = clientRSAPublicKey.getModulus().abs().toByteArray();
                final byte[] byteArray2 = clientRSAPublicKey.getPublicExponent().abs().toByteArray();
                final byte[] byteArray3 = serverRSAPublicKey.getModulus().abs().toByteArray();
                final byte[] byteArray4 = serverRSAPublicKey.getPublicExponent().abs().toByteArray();
                final byte[] r1 = processDigestArray(byteArray1);
                final byte[] r2 = processDigestArray(byteArray2);
                final byte[] r3 = processDigestArray(byteArray3);
                final byte[] r4 = processDigestArray(byteArray4);
                processMag(r1);
                processMag(r2);
                processMag(r3);
                processMag(r4);
                processMag(digest);
                instance.update(r1);
                instance.update(r2);
                instance.update(r3);
                instance.update(r4);
                instance.update(digest);
                digest = instance.digest();
                processMag(digest);
            } catch (NoSuchAlgorithmException e) {
                LOGGER.warn("NoSuchAlgorithmException Exception", e);
            }
        }
        return digest;
    }

    public static String byteArrayToString(byte[] array) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            sb.append((char) (array[i] & 0xFF));
        }
        return sb.toString();
    }

    public static String validatePIN(String pin, Certificate clientCert, Certificate serverCert) {
        char[] charArray = pin.toCharArray();

        String s1 = "" + charArray[0] + charArray[1];
        String s2 = "" + charArray[2] + charArray[3];
        String s3 = "" + charArray[4] + charArray[5];
        int si1 = Integer.parseInt(s1, 16);
        int si2 = Integer.parseInt(s2, 16);
        int si3 = Integer.parseInt(s3, 16);

        byte[] sb123 = new byte[] { (byte) si1, (byte) si2, (byte) si3 };
        byte[] sb23 = new byte[] { (byte) si2, (byte) si3 };
        byte[] digest = processDigest(sb23, clientCert, serverCert);
        String digestString = GoogleTVRequest.decodeMessage(byteArrayToString(digest));

        byte[] validPinB = new byte[] { digest[0], (byte) si2, (byte) si3 };
        String validPin = GoogleTVRequest.decodeMessage(byteArrayToString(validPinB));
        LOGGER.trace("validatePIN {} {} {} {} {} {}", sb123, digest[0], sb23, validPinB, validPin, digestString);

        return digestString;
    }
}
