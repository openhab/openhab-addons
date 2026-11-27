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
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Root response of {@code GET /telemetry/telemetry/energy/{unitId}}. The wire unit is measure-specific: the ATA
 * {@code cumulative_energy_consumed_since_last_upload} series reports Wh, the ATW interval series report kWh per
 * bucket. {@link org.openhab.binding.melcloud.internal.home.api.MelCloudHomeApiClient} normalizes every supported
 * measure to kWh.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MelCloudHomeTelemetryResponse {

    public List<MelCloudHomeMeasureData> measureData = List.of();

    /**
     * @return the most recent (last) sample across the first measure series in this response, with its value in the
     *         measure's own wire unit and its bucket start as reported by the endpoint
     */
    public Optional<MelCloudHomeMeasureValue> getLatestSample() {
        if (measureData.isEmpty()) {
            return Optional.empty();
        }
        List<MelCloudHomeMeasureValue> values = measureData.get(0).values;
        if (values.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(values.get(values.size() - 1));
    }
}
