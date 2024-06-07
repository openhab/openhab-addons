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
package org.openhab.binding.evcc.internal.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents a battery object of the status response (/api/state).
 * This DTO was written for evcc version 0.123.1
 * 
 * @author MikeTheTux - Initial contribution
 */
public class Battery {
    // Data types from https://github.com/evcc-io/evcc/blob/master/api/api.go
    // and from https://docs.evcc.io/docs/reference/configuration/messaging/#msg

    @SerializedName("power")
    private float power;

    @SerializedName("energy")
    private float energy;

    @SerializedName("soc")
    private float soc;

    @SerializedName("capacity")
    private float capacity;

    @SerializedName("controllable")
    private boolean controllable;

    /**
     * @return battery's capacity
     */
    public float getCapacity() {
        return capacity;
    }

    /**
     * @return battery's power
     */
    public float getPower() {
        return power;
    }

    /**
     * @return battery's state of charge
     */
    public float getSoC() {
        return soc;
    }

    /**
     * @return battery discharge controlable
     */
    public boolean getControllable() {
        return controllable;
    }
}
