/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.freeboxos.internal.api.login;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.BaseResponse;
import org.openhab.binding.freeboxos.internal.api.BaseResponse.ErrorCode;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;

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

    public OpenSessionData(String appId, String appToken, String challenge) throws FreeboxException {
        this.appId = appId;
        try {
            // Get an hmac_sha1 key from the raw key bytes
            SecretKeySpec signingKey = new SecretKeySpec(appToken.getBytes(), ALGORITHM);

            // Get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(signingKey);

            // Compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(challenge.getBytes());
            // Convert raw bytes to Hex
            this.password = DatatypeConverter.printHexBinary(rawHmac).toLowerCase();
            //
        } catch (IllegalArgumentException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new FreeboxException("Error encoding session password", e, BaseResponse.of(ErrorCode.INVALID_TOKEN));
        }
    }

    public String getPassword() {
        return password;
    }
}
