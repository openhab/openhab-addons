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
package org.openhab.binding.androidtv.internal.protocol.googletv;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

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
    private final Logger logger = LoggerFactory.getLogger(GoogleTVUtils.class);

    Certificate clientCert;
    Certificate serverCert;

    public GoogleTVUtils(Certificate clientCert, Certificate serverCert) {
        this.clientCert = clientCert;
        this.serverCert = serverCert;
    }

    private static String processMag(final byte[] magnitude) {
        if (magnitude != null) {
            final int length = magnitude.length;
            if (length != 0) {
                final BigInteger bigInteger = new BigInteger(1, magnitude);
                final StringBuilder sb = new StringBuilder();
                sb.append("%0");
                sb.append(length + length);
                sb.append("x");
                return String.format(sb.toString(), bigInteger);
            }
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

    public final byte[] processDigest(byte[] digest) {
        logger.trace("processDigest byte[] digest {}", digest);
        final PublicKey clientPublicKey = this.clientCert.getPublicKey();
        final PublicKey serverPublicKey = this.serverCert.getPublicKey();
        processMag(digest);
        logger.trace("processDigest byte[] processMag(digest) {}", digest);
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
                logger.trace("processDigest byte[] instance {}", instance.toString());

                digest = instance.digest();
                processMag(digest);
                logger.trace("processDigest byte[] processMag(digest) {}", digest);

            } catch (NoSuchAlgorithmException e) {
                logger.debug("NoSuchAlgorithmException Exception", e);
            }
        }
        return digest;
    }

    private byte[] intToByteArray(int value) {
        return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value };
    }

    private byte[] processSecretArray(final byte[] array) {
        final int length = array.length;
        if (length >= 2) {
            int n;
            if (length < 4) {
                n = 1;
            } else {
                n = length >> 2;
            }
            final int n2 = length - n;
            final byte[] array2 = new byte[n2];
            System.arraycopy(array, n, array2, 0, n2);
            return array2;
        }
        throw new IllegalArgumentException();
    }

    private byte[] appendSecretArray(final int n, final byte[] array) {
        final byte[] digest = processDigest(array);
        final int length = array.length;
        final byte[] array2 = new byte[n + length];
        System.arraycopy(digest, 0, array2, 0, n);
        System.arraycopy(array, 0, array2, n, length);
        return array2;
    }

    public boolean validateSecret(int secret) {
        logger.trace("validateSecret int secret {}", secret);
        final byte[] secretByte = intToByteArray(secret);
        logger.trace("validateSecret byte[] secret {}", secretByte);
        if (secretByte != null) {
            try {
                final byte[] byteShifted = processSecretArray(secretByte);
                logger.trace("validateSecret byte[] byteShifted {}", byteShifted);
                final int length = secretByte.length;
                final int length2 = byteShifted.length;
                processMag(byteShifted);
                processMag(secretByte);
                final int n = length - length2;
                processMag(appendSecretArray(n, byteShifted));
                if (Arrays.equals(secretByte, appendSecretArray(n, byteShifted))) {
                    final byte[] digest = processDigest(processSecretArray(secretByte));
                    logger.trace("validateSecret byte[] digest success {}", digest);
                    return true;
                } else {
                    logger.trace("validateSecret byte[] digest failed {}", appendSecretArray(n, byteShifted));
                }
            } catch (IllegalArgumentException e) {
                logger.debug("IllegalArgumentException", e);
            }
            logger.debug("Secret failed local check.");
            return false;
        }
        logger.debug("Invalid secret.");
        return false;
    }
    /*
     * public boolean inbandSecret(int secret) {
     * final int n2 = secret / 2;
     * int n3;
     * if (secret < 4) {
     * n3 = 1;
     * } else {
     * n3 = secret >> 2;
     * }
     * final byte[] bytes = new byte[secret - n3];
     * try {
     * SecureRandom.getInstance("SHA1PRNG").nextBytes(bytes);
     * this.j.u(n3, bytes);
     * final ovl ovl = (ovl) this.f(7);
     * final byte[] t = this.j.t(bytes);
     * final byte[] a = ovl.a;
     * if (!Arrays.equals(t, a)) {
     * final String z = processMag(t);
     * final String z2 = processMag(a);
     * final StringBuilder sb = new StringBuilder();
     * sb.append("Inband secret did not match. Expected [");
     * sb.append(z);
     * sb.append("], got [");
     * sb.append(z2);
     * sb.append("]");
     * throw new ouw(sb.toString());
     * }
     * throw new ouy("Unable to store peer association");
     * } catch (NoSuchAlgorithmException ex) {
     * throw new ouy(ex);
     * }
     * }
     */
}
