/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

/**
 * Represents a device status update as represented by the Smart Home
 * Controller.
 *
 * @author Stefan Kästle - Initial contribution
 * @author Christian Oeing - refactorings of e.g. server registration 
 */
public class DeviceStatusUpdate {
    /**
     * Url path of the service the update came from.
     */
    String path;

    /**
     * The type of message.
     */
    @SerializedName("@type")
    String type;

    /**
     * Name of service the update came from.
     */
    String id;

    /**
     * Current state of device. Serialized as JSON.
     */
    JsonElement state;

    /**
     * Id of device the update is for.
     */
    String deviceId;

    @Override
    public String toString() {

        return this.deviceId + "state: " + this.type;
    }
}
