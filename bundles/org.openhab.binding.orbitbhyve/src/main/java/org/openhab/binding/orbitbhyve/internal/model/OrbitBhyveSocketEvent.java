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
package org.openhab.binding.orbitbhyve.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link OrbitBhyveSocketEvent} holds information about a B-Hyve
 * event received on web socket.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class OrbitBhyveSocketEvent {
    String event = "";
    String mode = "";
    JsonElement program = new JsonObject();
    int delay = 0;

    @SerializedName("device_id")
    String deviceId = "";

    @SerializedName("current_station")
    int station = 0;

    public String getEvent() {
        return event;
    }

    public String getMode() {
        return mode;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public int getStation() {
        return station;
    }

    public JsonElement getProgram() {
        return program;
    }

    public int getDelay() {
        return delay;
    }
}
