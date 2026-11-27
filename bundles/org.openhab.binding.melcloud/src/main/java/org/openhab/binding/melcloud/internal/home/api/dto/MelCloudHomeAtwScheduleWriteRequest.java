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
package org.openhab.binding.melcloud.internal.home.api.dto;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Request body for {@code POST /monitor/atwcloudschedule/{unitId}}, used for both creating and updating a schedule
 * entry. Provisional: the shape is not confirmed against real ATW traffic.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MelCloudHomeAtwScheduleWriteRequest {

    /**
     * Client-generated UUID on create; the existing entry's id on update.
     */
    public @Nullable String id;
    /**
     * A create call must supply a non-null, non-empty list; an update carries the entry's complete current value.
     */
    public @Nullable List<Integer> days;
    /** A create call must supply a value; an update carries the entry's current value. See {@link #days}. */
    public @Nullable String time;
    public @Nullable Boolean power;
    public @Nullable Double setTemperatureZone1;
    public @Nullable Double setTemperatureZone2;
    public @Nullable Double setTankWaterTemperature;
    public @Nullable Boolean forcedHotWaterMode;
    public @Nullable Integer operationModeZone1;
    public @Nullable Integer operationModeZone2;
}
