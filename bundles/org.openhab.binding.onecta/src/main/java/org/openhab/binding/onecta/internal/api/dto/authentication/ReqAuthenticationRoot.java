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

import com.google.gson.annotations.SerializedName;

/**
 * @author Alexander Drent - Initial contribution
 */
public class ReqAuthenticationRoot {
    @SerializedName("ClientId")
    private String clientId;
    @SerializedName("AuthFlow")
    private String authFlow;
    @SerializedName("AuthParameters")
    private ReqAuthParameters authParameters;

    public ReqAuthenticationRoot(String clientId, String refreshToken) {
        this.clientId = clientId;
        this.authFlow = "REFRESH_TOKEN_AUTH";
        this.authParameters = new ReqAuthParameters(refreshToken);
    }

    public String getClientId() {
        return clientId;
    }

    public String getAuthFlow() {
        return authFlow;
    }

    public ReqAuthParameters getAuthParameters() {
        return authParameters;
    }
}
