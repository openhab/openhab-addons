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
 * The {@link FinishPairingDTO} class defines the Data Transfer Object
 * for the Philips TV API /pair/grant endpoint to finish pairing.
 *
 * @author Benjamin Meyer - Initial contribution
 * @author Ben Rosenblum - Merged into AndroidTV
 */
@NonNullByDefault
public class FinishPairingDTO {

    @JsonProperty("auth")
    private AuthDTO auth;

    @JsonProperty("device")
    private DeviceDTO device;

    public FinishPairingDTO(DeviceDTO device, AuthDTO auth) {
        this.device = device;
        this.auth = auth;
    }

    public void setAuth(AuthDTO auth) {
        this.auth = auth;
    }

    public AuthDTO getAuth() {
        return auth;
    }

    public void setDevice(DeviceDTO device) {
        this.device = device;
    }

    public DeviceDTO getDevice() {
        return device;
    }

    @Override
    public String toString() {
        return "FinishPairingDTO{" + "auth = '" + auth + '\'' + ",device = '" + device + '\'' + "}";
    }
}
