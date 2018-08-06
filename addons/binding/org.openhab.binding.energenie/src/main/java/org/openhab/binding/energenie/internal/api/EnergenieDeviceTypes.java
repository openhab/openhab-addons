/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.energenie.internal.api;

import com.google.gson.annotations.SerializedName;

/**
 * Contains all Energenie Mi|Home JSON keys associated with the device types.
 * They are used in the communication with the server
 *
 * @author Mihaela Memova - Initial contribution
 *
 */
public enum EnergenieDeviceTypes {
    @SerializedName("gateway")
    GATEWAY("gateway"),

    @SerializedName("open")
    OPEN_SENSOR("open"),

    @SerializedName("motion")
    MOTION_SENSOR("motion"),

    @SerializedName("house")
    HOUSE_MONITOR("house");

    private String name;

    private EnergenieDeviceTypes(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
