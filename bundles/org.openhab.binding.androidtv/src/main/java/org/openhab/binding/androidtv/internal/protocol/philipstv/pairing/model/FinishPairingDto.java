/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.androidtv.internal.protocol.philipstv.internal.pairing.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link FinishPairingDto} class defines the Data Transfer Object
 * for the Philips TV API /pair/grant endpoint to finish pairing.
 *
 * @author Benjamin Meyer - Initial contribution
 */
public class FinishPairingDto {

    @JsonProperty("auth")
    private AuthDto auth;

    @JsonProperty("device")
    private DeviceDto device;

    public void setAuth(AuthDto auth) {
        this.auth = auth;
    }

    public AuthDto getAuth() {
        return auth;
    }

    public void setDevice(DeviceDto device) {
        this.device = device;
    }

    public DeviceDto getDevice() {
        return device;
    }

    @Override
    public String toString() {
        return "FinishPairingDto{" + "auth = '" + auth + '\'' + ",device = '" + device + '\'' + "}";
    }
}
