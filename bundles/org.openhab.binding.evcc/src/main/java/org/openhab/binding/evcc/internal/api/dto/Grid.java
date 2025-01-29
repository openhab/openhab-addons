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
package org.openhab.binding.evcc.internal.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents the grid response (/api/state).
 * This DTO was written for evcc version 0.133.0
 *
 * @author Daniel Kötting - Initial contribution
 */
public class Grid {
    // Data types from https://github.com/evcc-io/evcc/blob/master/api/api.go
    // and from https://docs.evcc.io/docs/reference/configuration/messaging/#msg

    @SerializedName("currents")
    private float[] currents;

    @SerializedName("energy")
    private float energy;

    @SerializedName("power")
    private Float power;

    /**
     * @return grid's currents
     */
    public float[] getCurrents() {
        return currents;
    }

    /**
     * @return grid's energy
     */
    public float getEnergy() {
        return energy;
    }

    /**
     * @return grid's power or {@code null} if not available
     */
    public Float getPower() {
        return power;
    }
}
