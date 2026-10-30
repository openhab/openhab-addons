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

import java.util.List;

/**
 * Response of the login verification (MFA) endpoints for Canada API. Each endpoint fills only its own fields.
 *
 * @author Carlo Dischler - Initial contribution
 */
public record MfaResponse(Result result, ResponseError error) {

    public record Result(String userInfoUuid, List<String> emailList, String otpKey, Boolean verifiedOtp,
            String otpValidationKey, TokenResponse.Result.Token token) {
    }
}
