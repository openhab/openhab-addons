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
package org.openhab.binding.fronius.internal.api.dto.inverter.batterycontrol;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Record representing an entry of {@link TimeOfUseRecords}.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public record TimeOfUseRecord(@SerializedName("Active") boolean active, @SerializedName("Power") int power,
        @SerializedName("ScheduleType") ScheduleType scheduleType,
        @SerializedName("TimeTable") TimeTableRecord timeTable, @SerializedName("Weekdays") WeekdaysRecord weekdays) {
}
