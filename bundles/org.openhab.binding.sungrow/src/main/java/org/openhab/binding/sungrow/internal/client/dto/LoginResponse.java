/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.sungrow.internal.client.dto;

import com.google.gson.annotations.SerializedName;

/**
 * @author Christian Kemper - Initial contribution
 */
public class LoginResponse extends BaseResponse<LoginResponse.LoginResult> {

    public boolean isSuccess() {
        return getErrorCode() != null && getErrorCode().equals("1");
    }

    public static class LoginResult {

        private String token;
        @SerializedName("login_state")
        private LoginState loginState;
        @SerializedName("msg")
        private String message;

        public String getToken() {
            return token;
        }

        public LoginState getLoginState() {
            return loginState;
        }

        public String getMessage() {
            return message;
        }
    }
}
