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
package org.openhab.binding.ventaair.internal.message.dto;

import org.openhab.binding.ventaair.internal.message.action.AllActions;

import com.google.gson.annotations.SerializedName;

/**
 * Message send by the device, containing information about its current state
 *
 * @author Stefan Triller - Initial contribution
 *
 */
public class DeviceInfoMessage extends Message {

    public DeviceInfoMessage(Header header) {
        super(header);
    }

    @SerializedName(value = "Action")
    private AllActions currentActions;

    @SerializedName(value = "Info")
    private Info info;

    @SerializedName(value = "Measure")
    private Measurements measurements;

    public AllActions getCurrentActions() {
        return currentActions;
    }

    public Info getInfo() {
        return info;
    }

    public Measurements getMeasurements() {
        return measurements;
    }
}
