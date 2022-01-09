/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.yioremote.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonObject;

/**
 * The {@link AuthenticationMessage} the AuthenticationMessage DTO
 *
 *
 * @author Michael Loercher - Initial contribution
 */
@NonNullByDefault
public class AuthenticationMessage {
    private String type = "auth";
    private String token = "0";

    public String getType() {
        return type;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public JsonObject getAuthenticationMessageJsonObject() {
        JsonObject authenticationMessage = new JsonObject();
        authenticationMessage.addProperty("type", type);
        authenticationMessage.addProperty("token", token);
        return authenticationMessage;
    }

    public String getAuthenticationMessageString() {
        return getAuthenticationMessageJsonObject().toString();
    }
}
