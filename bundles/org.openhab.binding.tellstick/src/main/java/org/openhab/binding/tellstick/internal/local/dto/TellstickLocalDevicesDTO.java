/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.tellstick.internal.local.dto;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * Class used to deserialize JSON from Telldus local API.
 *
 * @author Jan Gustafsson - Initial contribution
 */
public class TellstickLocalDevicesDTO {

    @SerializedName("device")
    private List<TellstickLocalDeviceDTO> devices = null;

    public List<TellstickLocalDeviceDTO> getDevices() {
        return devices;
    }

    public void setDevices(List<TellstickLocalDeviceDTO> devices) {
        this.devices = devices;
    }
}
