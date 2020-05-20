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
package org.openhab.binding.freebox.internal.api.model;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link OpenSessionData} holds and handle data needed to
 * be sent to API in order to open a new session
 * https://dev.freebox.fr/sdk/os/login/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class OpenSessionData {
    private static final String ALGORITHM = "HmacSHA1";
    protected final String appId;
    protected final String password;

    public OpenSessionData(String appId, String appToken, String challenge)
            throws InvalidKeyException, NoSuchAlgorithmException {
        this.appId = appId;

        SecretKeySpec signingKey = new SecretKeySpec(appToken.getBytes(), ALGORITHM); // Get an hmac_sha1 key from the
                                                                                      // raw key bytes
        Mac mac = Mac.getInstance(ALGORITHM); // Get an hmac_sha1 Mac instance and initialize with the signing key
        mac.init(signingKey);
        byte[] rawHmac = mac.doFinal(challenge.getBytes()); // Compute the hmac on input data bytes
        byte[] hexBytes = new Hex().encode(rawHmac); // Convert raw bytes to Hex
        this.password = new String(hexBytes, StandardCharsets.UTF_8).toLowerCase(); // Convert array of Hex bytes to
                                                                                    // String
    }

    public String getPassword() {
        return password;
    }
}
