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
package org.openhab.binding.androidtv.internal.protocol.philipstv.pairing.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Part of {@link FinishPairingDTO}
 *
 * @author Benjamin Meyer - Initial contribution
 * @author Ben Rosenblum - Merged into AndroidTV
 */
@NonNullByDefault
public class AuthDTO {

    @JsonProperty("auth_signature")
    private String authSignature = "";

    @JsonProperty("auth_timestamp")
    private String authTimestamp = "";

    @JsonProperty("pin")
    private String pin = "";

    @JsonProperty("auth_AppId")
    private String authAppId = "";

    public void setAuthSignature(String authSignature) {
        this.authSignature = authSignature;
    }

    public String getAuthSignature() {
        return authSignature;
    }

    public void setAuthTimestamp(String authTimestamp) {
        this.authTimestamp = authTimestamp;
    }

    public String getAuthTimestamp() {
        return authTimestamp;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getPin() {
        return pin;
    }

    public void setAuthAppId(String authAppId) {
        this.authAppId = authAppId;
    }

    public String getAuthAppId() {
        return authAppId;
    }

    @Override
    public String toString() {
        return "Auth{" + "auth_signature = '" + authSignature + '\'' + ",auth_timestamp = '" + authTimestamp + '\''
                + ",pin = '" + pin + '\'' + ",auth_AppId = '" + authAppId + '\'' + "}";
    }
}
