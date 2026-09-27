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
package org.openhab.binding.millheat.internal.dto;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Response of {@code GET /rooms/&#123;roomId&#125;/devices}. Despite the path this returns the
 * room's own state rather than just its devices: the three program setpoints, the active mode and
 * the aggregate measurements. Only the fields the binding surfaces are mapped.
 *
 * @author Petter L. H. Eide - Initial contribution
 */
public record RoomInfoDTO(String id, @Nullable String name, @Nullable String houseId,
        @Nullable Double roomComfortTemperature, @Nullable Double roomSleepTemperature,
        @Nullable Double roomAwayTemperature, @Nullable String mode, @Nullable String activeModeFromWeeklyProgram,
        @Nullable String overrideModeType, @Nullable Long overrideEndDate, @Nullable String roomProgramName,
        @Nullable Double averageTemperature, @Nullable Boolean roomHeatStatus, @Nullable Boolean roomOpenWindowStatus,
        @Nullable Boolean isRoomOnline, @Nullable Double roomEnergyUsage) {
}
