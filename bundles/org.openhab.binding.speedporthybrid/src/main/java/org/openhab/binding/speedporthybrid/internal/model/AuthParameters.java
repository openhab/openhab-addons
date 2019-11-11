/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.speedporthybrid.internal.model;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.speedporthybrid.internal.handler.CryptoUtils;

/**
 * Represents the set of authentication parameters used to communicate with the SpeedPort Hybrid router.
 *
 * @author Henning Treu - initial contribution
 *
 */
@NonNullByDefault
public class AuthParameters {

    private static final String NULLTOKEN = "nulltoken";

    @Nullable
    private String challengev;

    private String csrfToken;

    @Nullable
    private String derivedKey;

    @Nullable
    private String passwordHash;

    public AuthParameters() {
        csrfToken = NULLTOKEN;
    }

    public @Nullable String getChallengev() {
        return challengev;
    }

    public void updateChallengev(String challengev, @Nullable String password) {
        this.challengev = challengev;
        this.derivedKey = CryptoUtils.INSTANCE.deriveKey(challengev, password);
        this.passwordHash = CryptoUtils.INSTANCE.hashPassword(challengev, password);
    }

    public String getCSRFToken() {
        return csrfToken;
    }

    public void updateCSRFToken(String csrfToken) {
        this.csrfToken = csrfToken;
    }

    public byte[] getDerivedKey() throws DecoderException {
        String dKey = this.derivedKey;
        if (dKey == null) {
            return new byte[0];
        }
        return Hex.decodeHex(dKey.toCharArray());
    }

    public boolean isValid() {
        return !csrfToken.equals(NULLTOKEN) && challengev != null && derivedKey != null;
    }

    public void reset() {
        csrfToken = NULLTOKEN;
        challengev = null;
        derivedKey = null;
        passwordHash = null;
    }

    public String getAuthData() {
        return "?showpw=0" + //
                "&csrf_token=" + csrfToken + //
                "&challengev=" + (challengev != null ? challengev : "") + //
                "&password=" + (challengev != null ? passwordHash : "");
    }
}
