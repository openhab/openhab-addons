/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.bluelink.internal.dto.ca;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Requests of the login verification (MFA) for Canada API.
 *
 * @author Carlo Dischler - Initial contribution
 */
@NonNullByDefault
public final class MfaRequest {

    // API code of the login verification in the web app
    public static final String API_CODE = "0107";

    public record SelectMethod(String mfaApiCode, String userAccount) {
    }

    public record SendCode(String otpMethod, String mfaApiCode, String userAccount, String userPhone,
            String userInfoUuid) {
    }

    public record ValidateCode(String otpNo, String userAccount, String otpKey, String mfaApiCode) {
    }

    public record GenerateToken(String userAccount, String otpEmail, String mfaApiCode, String otpValidationKey,
            String mfaYn) {
    }

    private MfaRequest() {
    }
}
