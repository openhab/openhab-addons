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
 * Root response of {@code GET /telemetry/telemetry/energy/{unitId}} (see ADR-003). Values are in Wh; callers divide
 * by 1000 for kWh.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MelCloudHomeTelemetryResponse {

    public List<MelCloudHomeMeasureData> measureData = List.of();

    /**
     * @return the most recent (last) value across the first measure series in this response, in Wh
     */
    public Optional<Double> getLatestValueWh() {
        if (measureData.isEmpty()) {
            return Optional.empty();
        }
        List<MelCloudHomeMeasureValue> values = measureData.get(0).values;
        if (values.isEmpty()) {
            return Optional.empty();
        }
        String raw = values.get(values.size() - 1).value;
        try {
            return Optional.of(Double.parseDouble(raw));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
