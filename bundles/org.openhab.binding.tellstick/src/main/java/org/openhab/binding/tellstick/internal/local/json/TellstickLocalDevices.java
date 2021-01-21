/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.tellstick.internal.local.json;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * Class used to deserialize JSON from Telldus local API.
 *
 * @author Jan Gustafsson - Initial contribution
 */
public class TellstickLocalDevices {

    @SerializedName("device")
    private List<TellstickLocalDevice> devices = null;

    public List<TellstickLocalDevice> getDevices() {
        return devices;
    }

    public void setDevices(List<TellstickLocalDevice> devices) {
        this.devices = devices;
    }
}
