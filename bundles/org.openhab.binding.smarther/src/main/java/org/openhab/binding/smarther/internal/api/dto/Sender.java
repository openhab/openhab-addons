/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.smarther.internal.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Smarther API Sender DTO class.
 *
 * @author Fabio Possieri - Initial contribution
 */
public class Sender {

    @SerializedName("addressType")
    private String addressType;
    private String system;
    private PlantRef plant;

    public String getAddressType() {
        return addressType;
    }

    public String getSystem() {
        return system;
    }

    public PlantRef getPlant() {
        return plant;
    }

    @Override
    public String toString() {
        return String.format("addressType=%s, system=%s, plant=[%s]", addressType, system, plant);
    }

}
