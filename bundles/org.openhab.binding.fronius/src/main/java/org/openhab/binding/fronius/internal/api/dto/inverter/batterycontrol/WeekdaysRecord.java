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
 * Record representing the "Weekdays" node of {@link TimeOfUseRecord}.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public record WeekdaysRecord(@SerializedName("Mon") boolean monday, @SerializedName("Tue") boolean tuesday,
        @SerializedName("Wed") boolean wednesday, @SerializedName("Thu") boolean thursday,
        @SerializedName("Fri") boolean friday, @SerializedName("Sat") boolean saturday,
        @SerializedName("Sun") boolean sunday) {
}
