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
package org.openhab.binding.mqtt.internal.ssl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.util.HexUtils;

/**
 * Encapsulates a {@link MessageDigest} with a specific Hash method. Extracts the digest data of
 * a certificate.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class PinMessageDigest {
    protected final MessageDigest messageDigest;
    private final String method;

    /**
     * Creates a message digest for a certificate/public key pinning.
     *
     * @param method The hash method to use
     * @throws NoSuchAlgorithmException
     */
    public PinMessageDigest(String method) throws NoSuchAlgorithmException {
        this.method = method;
        this.messageDigest = MessageDigest.getInstance(method);
    }

    /**
     * Outputs a string like "SHA-256:83F9171E06A313118889F7D79302BD1B7A2042EE0CFD029ABF8DD06FFA6CD9D3"
     *
     * @param digestData Digest data
     */
    public String toHexString(byte[] digestData) {
        return getMethod() + ":" + HexUtils.bytesToHex(digestData);
    }

    byte[] digest(byte[] origData) {
        synchronized (messageDigest) {
            return messageDigest.digest(origData);
        }
    }

    /**
     * @return Return the digest method for instance SHA-256
     */
    public String getMethod() {
        return method;
    }
}
