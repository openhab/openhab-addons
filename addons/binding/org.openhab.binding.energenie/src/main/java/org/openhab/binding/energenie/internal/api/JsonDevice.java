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
 * JSON representation of a device (gateway or subdevice) used in the communication with the server.
 *
 * @author Mihaela Memova - Initial contribution
 *
 */
public class JsonDevice {

    @SerializedName("device_type")
    private EnergenieDeviceTypes type;
    @SerializedName("id")
    private int id;
    @SerializedName("label")
    private String label;

    public JsonDevice() {

    }

    public JsonDevice(EnergenieDeviceTypes type, int id, String label) {
        this.type = type;
        this.id = id;
        this.label = label;
    }

    public EnergenieDeviceTypes getType() {
        return type;
    }

    public void setType(EnergenieDeviceTypes type) {
        this.type = type;
    }

    public int getID() {
        return id;
    }

    public void setID(int id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }
}
