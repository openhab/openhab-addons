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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Request body for {@code PUT /monitor/ataunit/{id}} (see ADR-003).
 *
 * <p>
 * The server requires every one of these fields to be present on every call; fields left {@code null} here must be
 * serialized as JSON {@code null} (not omitted) — see {@code MelCloudHomeApiClient} for the {@code Gson} instance
 * this requires. Callers should only set the one or two fields being changed and leave the rest {@code null}.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MelCloudHomeAtaControlRequest {

    public @Nullable Boolean power;
    public @Nullable String operationMode;
    public @Nullable String setFanSpeed;
    public @Nullable String vaneHorizontalDirection;
    public @Nullable String vaneVerticalDirection;
    public @Nullable Double setTemperature;
    public @Nullable Double temperatureIncrementOverride;
    public @Nullable Boolean inStandbyMode;
}
