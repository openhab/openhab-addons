/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.myenergi.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ZappiBoostTimeSlot} is a DTO class used to hold a slot of boost
 * times.
 *
 * @author Rene Scherer - Initial contribution
 *
 */
@NonNullByDefault
public class ZappiBoostTimeSlot {

    private static final String ALL_DAYS_OFF = "00000000";
    // {
    // "slt":11,
    // "bsh":1,
    // "bsm":30,
    // "bdh":4,
    // "bdm":0,
    // "bdd":"00010000"
    // }

    @SerializedName("slt")
    public int slotId;

    @SerializedName("bsh")
    public int startHour;
    @SerializedName("bsm")
    public int startMinute;

    @SerializedName("bdh")
    public int durationHour;
    @SerializedName("bdm")
    public int durationMinute;

    /**
     * a string bitmap of 8 characters (either '0' or '1'). The first character is
     * always '0', then followed by Mon, Tue, Wed, Thu, Fri, Sat and Sun bits.
     */
    @SerializedName("bdd")
    public String daysOfTheWeekMap = ALL_DAYS_OFF;

    public ZappiBoostTimeSlot() {
    }

    public ZappiBoostTimeSlot(int slotId, int startHour, int startMinute, int durationHour, int durationMinute,
            DaysOfWeekMap daysOfTheWeekMap) {
        super();
        this.slotId = slotId;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.durationHour = durationHour;
        this.durationMinute = durationMinute;
        this.daysOfTheWeekMap = daysOfTheWeekMap.getMapAsString();
    }

    @Override
    public String toString() {
        return "ZappiBoostTimeSlot [slotId=" + slotId + ", startHour=" + startHour + ", startMinute=" + startMinute
                + ", durationHour=" + durationHour + ", durationMinute=" + durationMinute + ", daysOfTheWeekMap="
                + daysOfTheWeekMap + "]";
    }
}
