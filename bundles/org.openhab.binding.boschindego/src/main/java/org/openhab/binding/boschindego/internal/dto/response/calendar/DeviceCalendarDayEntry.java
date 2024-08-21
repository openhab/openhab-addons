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
package org.openhab.binding.boschindego.internal.dto.response.calendar;

import com.google.gson.annotations.SerializedName;

/**
 * Device calendar day entry.
 * 
 * @author Jacob Laursen - Initial contribution
 */
public class DeviceCalendarDayEntry {

    @SerializedName("day")
    public int number;

    @SerializedName("slots")
    public DeviceCalendarDaySlot[] slots;
}
