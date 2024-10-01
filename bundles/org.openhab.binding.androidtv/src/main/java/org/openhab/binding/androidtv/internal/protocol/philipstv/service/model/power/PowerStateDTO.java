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
package org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.power;

import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVBindingConstants.POWER_ON;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVBindingConstants.STANDBY;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVBindingConstants.STANDBYKEEP;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link PowerStateDTO} class defines the Data Transfer Object
 * for the Philips TV API /powerstate endpoint to retrieve or set the current power state.
 *
 * @author Benjamin Meyer - Initial contribution
 * @author Ben Rosenblum - Merged into AndroidTV
 */
public class PowerStateDTO {

    @JsonProperty("powerstate")
    private String powerState = "";

    public PowerStateDTO() {
    }

    public String getPowerState() {
        return powerState;
    }

    public void setPowerState(String powerState) {
        this.powerState = powerState;
    }

    @JsonIgnore
    public boolean isPoweredOn() {
        return powerState.equalsIgnoreCase(POWER_ON);
    }

    @JsonIgnore
    public boolean isStandby() {
        return (powerState.equalsIgnoreCase(STANDBY) || powerState.equalsIgnoreCase(STANDBYKEEP));
    }

    @JsonIgnore
    public boolean isStandbyKeep() {
        return powerState.equalsIgnoreCase(STANDBYKEEP);
    }
}
