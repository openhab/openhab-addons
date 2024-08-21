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
package org.openhab.binding.bticinosmarther.internal.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@code Sender} class defines the dto for Smarther API sender object.
 *
 * @author Fabio Possieri - Initial contribution
 */
public class Sender {

    @SerializedName("addressType")
    private String addressType;
    private String system;
    private PlantRef plant;

    /**
     * Returns the sender address type.
     *
     * @return a string containing the sender address type
     */
    public String getAddressType() {
        return addressType;
    }

    /**
     * Returns the sender system.
     *
     * @return a string containing the sender system
     */
    public String getSystem() {
        return system;
    }

    /**
     * Returns the sender plant reference.
     *
     * @return a {@link PlantRef} object representing the sender plant reference
     */
    public PlantRef getPlant() {
        return plant;
    }

    @Override
    public String toString() {
        return String.format("addressType=%s, system=%s, plant=[%s]", addressType, system, plant);
    }
}
