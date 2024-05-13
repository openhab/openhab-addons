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
package org.openhab.binding.onecta.internal.api.dto.authentication;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * @author Alexander Drent - Initial contribution
 */
@NonNullByDefault
public class RespAuthenticationRoot {
    @SerializedName("AuthenticationResult")
    private RespAuthenticationResult respAuthenticationResult = new RespAuthenticationResult();
    @SerializedName("ChallengeParameters")
    private RespChallengeParameters respChallengeParameters = new RespChallengeParameters();
    @SerializedName("__type")
    private String __type = "";
    @SerializedName("message")
    private String message = "";

    public RespAuthenticationResult getAuthenticationResult() {
        return respAuthenticationResult;
    }

    public RespChallengeParameters getChallengeParameters() {
        return respChallengeParameters;
    }

    public String get__type() {
        return __type;
    }

    public String getMessage() {
        return message;
    }
}
