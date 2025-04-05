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
public class RespStep7SessionInfo {
    @SerializedName("login_token")
    public String login_token;
    @SerializedName("expires_in")
    public String expires_in;

    public String getLogin_token() {
        return login_token;
    }

    public String getExpires_in() {
        return expires_in;
    }
}
