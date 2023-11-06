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
package org.openhab.binding.nest.internal.sdm.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Type of the SDM device.
 *
 * @author Wouter Born - Initial contribution
 */
public enum SDMDeviceType {
    @SerializedName("sdm.devices.types.CAMERA")
    CAMERA,

    @SerializedName("sdm.devices.types.DISPLAY")
    DISPLAY,

    @SerializedName("sdm.devices.types.DOORBELL")
    DOORBELL,

    @SerializedName("sdm.devices.types.THERMOSTAT")
    THERMOSTAT;

    public String toLabel() {
        return name().charAt(0) + name().toLowerCase().substring(1);
    }
}
