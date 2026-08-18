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
 * Request body for {@code POST /monitor/atwcloudschedule/{unitId}}, used for both creating a new schedule entry and
 * updating an existing one (the community {@code andrew-blake/melcloudhome} project's reference documents a single
 * shared endpoint for ATW, unlike the ATA equivalent which splits into separate create/update endpoints).
 *
 * <p>
 * <b>Provisional shape (ADR-012).</b> This class is not independently confirmed against real ATW traffic — see
 * ADR-012's Status section. In particular: {@code days} as day-number integers and
 * {@code operationModeZone1}/{@code operationModeZone2} as integers are taken from the {@code melcloudhome}
 * project's documentation, whose own "Known Limitations" section flags the schedule integer mapping as unconfirmed.
 * This class deliberately has no {@code zone1Active}/{@code zone2Active}/{@code hotWaterActive} fields, since the
 * documented write body doesn't include them — if a live capture ever shows otherwise, this class needs revision.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MelCloudHomeAtwScheduleWriteRequest {

    /**
     * Client-generated UUID on create; the existing entry's id on update. Per {@code MelCloudHomeApiClient}'s shared
     * control-request convention, this class is expected to be serialized with {@code serializeNulls()} so unset
     * fields go over the wire as JSON {@code null}, matching the control endpoints' full-payload contract — this is
     * an assumption carried over from the confirmed control endpoints, not independently confirmed for schedules.
     */
    public @Nullable String id;
    /**
     * {@code null} to leave unchanged on an update. A create call must supply a non-null, non-empty list — the
     * caller ({@code MelCloudHomeAtwUnitHandler}) is responsible for that validation; this class itself allows
     * {@code null} for both create and update since it doesn't distinguish the two calls.
     */
    public @Nullable List<Integer> days;
    /** {@code null} to leave unchanged on an update; a create call must supply a value. See {@link #days}. */
    public @Nullable String time;
    public @Nullable Boolean power;
    public @Nullable Double setTemperatureZone1;
    public @Nullable Double setTemperatureZone2;
    public @Nullable Double setTankWaterTemperature;
    public @Nullable Boolean forcedHotWaterMode;
    public @Nullable Integer operationModeZone1;
    public @Nullable Integer operationModeZone2;
}
