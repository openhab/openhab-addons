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
import org.openhab.binding.freebox.internal.api.FreeboxException;
import org.openhab.binding.freebox.internal.api.RequestAnnotation;

/**
 * The {@link OpenSessionRequest} is the Java class used to map the
 * structure used by the request of the open session API
 * https://dev.freebox.fr/sdk/os/login/#
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("unused")
@RequestAnnotation(responseClass = OpenSessionResponse.class, relativeUrl = "login/session/", retryAuth = false, method = "POST")
public class OpenSessionRequest {
    private static final String ALGORITHM = "HmacSHA1";

    private String appId;
    private String password;

    public OpenSessionRequest(String appId, String appToken, String challenge) throws FreeboxException {
        this.appId = appId;
        this.password = hmacSha1(appToken, challenge);
    }

    private String hmacSha1(String appToken, String challenge) throws FreeboxException {
        try {
            // Get an hmac_sha1 key from the raw key bytes
            SecretKeySpec signingKey = new SecretKeySpec(appToken.getBytes(), ALGORITHM);

            // Get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(signingKey);

            // Compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(challenge.getBytes());

            // Convert raw bytes to Hex
            byte[] hexBytes = new Hex().encode(rawHmac);

            // Covert array of Hex bytes to a String
            return new String(hexBytes, StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new FreeboxException("Computing the hmac-sha1 of the challenge and the app token failed", e);
        }
    }
}
