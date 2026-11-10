/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.tailwind.internal.dto;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link TailwindNotification} is used to define the JSON notify subset in the
 * status response from the TailWind controller UDP server.
 *
 * @author Dave J. Schoepel - Initial contribution
 */
public class TailwindNotification implements Serializable {

    private static final long serialVersionUID = -7087921117659647215L;

    /* ----- Notification details follow --------- */
    @SerializedName("door_idx")
    private Integer doorIdx; // Door index number (0-door1, 1-door2, 2-door3)

    @SerializedName("event")
    private String event; // Event type ((open, close, lock, enable, disable, reboot)

    public Integer getDoorIdx() {
        return doorIdx;
    }

    public void setDoorIdx(Integer doorIdx) {
        this.doorIdx = doorIdx;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    @Override
    public String toString() {
        return "Notify [doorIdx=" + doorIdx + ", event=" + event + "]";
    }
}
