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
 * This class represents a vehicle object of the status response (/api/state).
 * This DTO was written for evcc version 0.123.1
 *
 * @author Luca Arnecke - Initial contribution
 */
public class Vehicle {
    // Data types from https://github.com/evcc-io/evcc/blob/master/api/api.go
    // and from https://docs.evcc.io/docs/reference/configuration/messaging/#msg

    @SerializedName("title")
    private String title;

    @SerializedName("minSoc")
    private float minSoC;

    @SerializedName("limitSoc")
    private float limitSoC;

    @SerializedName("capacity")
    private float capacity;

    @SerializedName("plans")
    private Plan[] plans;

    /**
     * @return vehicle name
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return minimum state of charge
     */
    public float getMinSoC() {
        return minSoC;
    }

    /**
     * @return limit state of charge
     */
    public float getLimitSoC() {
        return limitSoC;
    }

    /**
     * @return vehicle's capacity
     */
    public float getCapacity() {
        return capacity;
    }

    /**
     * @return current plan for vehicle
     */
    public Plan getPlan() {
        if (plans != null) {
            return plans[0];
        } else {
            return null;
        }
    }
}
