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
package org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.ambilight;

import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVBindingConstants.POWER_ON;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link AmbilightPowerDTO} class defines the Data Transfer Object
 * for the Philips TV API /ambilight/power endpoint to retrieve or set the current power state.
 *
 * @author Benjamin Meyer - Initial contribution
 * @author Ben Rosenblum - Merged into AndroidTV
 */

public class AmbilightPowerDTO {

    @JsonProperty("power")
    private String power = "";

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }

    @JsonIgnore
    public boolean isPoweredOn() {
        return power.equalsIgnoreCase(POWER_ON);
    }
}
