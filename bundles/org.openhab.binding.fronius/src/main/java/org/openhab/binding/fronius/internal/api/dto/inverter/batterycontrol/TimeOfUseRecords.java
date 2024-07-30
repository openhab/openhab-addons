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
 * Record representing the data received from or sent to the <code>/config/timeofuse</code> HTTP endpoint of Fronius
 * hybrid inverters.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public record TimeOfUseRecords(@SerializedName("timeofuse") TimeOfUseRecord[] records) {
}
