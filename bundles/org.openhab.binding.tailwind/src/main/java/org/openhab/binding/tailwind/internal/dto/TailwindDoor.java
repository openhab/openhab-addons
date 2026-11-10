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
 * The {@link TailwindDoor} is used to define the JSON door subset in the
 * status response from the TailWind controller API or UDP server.
 *
 * @author Dave J. Schoepel - Initial contribution
 */
public class TailwindDoor implements Serializable {

    private static final long serialVersionUID = 4331180371949634469L;

    @SerializedName("index")
    private long index; // Door index number (0-door1, 1-door2, 2-door3)

    @SerializedName("status")
    private String status; // Door events (open, close, lock, enable, disable, reboot)

    @SerializedName("lockup")
    private long lockup; // TailWind controller locked door (0-unlocked, 1-locked) to prevent injury

    @SerializedName("disabled")
    private long disabled; // Door is disabled via APP (0-not disabled, 1- disabled)

    public long getIndex() {
        return index;
    }

    public void setIndex(long value) {
        this.index = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        this.status = value;
    }

    public long getLockup() {
        return lockup;
    }

    public void setLockup(long value) {
        this.lockup = value;
    }

    public long getDisabled() {
        return disabled;
    }

    public void setDisabled(long value) {
        this.disabled = value;
    }

    @Override
    public String toString() {
        return "Door [index=" + index + ", status=" + status + ", lockup=" + lockup + ", disabled=" + disabled + "]";
    }
}
