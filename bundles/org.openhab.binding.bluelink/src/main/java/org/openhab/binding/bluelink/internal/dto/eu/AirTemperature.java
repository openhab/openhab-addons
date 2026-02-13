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
package org.openhab.binding.bluelink.internal.dto.eu;

import static org.openhab.core.library.unit.SIUnits.CELSIUS;

import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.bluelink.internal.dto.TemperatureValue;
import org.openhab.binding.bluelink.internal.model.IVehicle;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Air temperature value for the EU API.
 *
 * @author Florian Hotze - Initial contribution
 */
public record AirTemperature(@Override String value, @Override int unit, int hvacTempType) implements TemperatureValue {

    private static final Logger LOGGER = LoggerFactory.getLogger(AirTemperature.class);
    private static final int UNIT_CELSIUS = 0;
    // Fixed range 14.0 to 30.0 in 0.5 steps (indices 28..60 mapped to 0..32)
    private static final double[] TEMP_RANGE_C = IntStream.range(28, 61).mapToDouble(x -> (double) x / 2).toArray();

    @Override
    public State getTemperature(final @NonNull IVehicle vehicle) {
        if (unit != UNIT_CELSIUS || value == null || !value.endsWith("H")) {
            return UnDefType.UNDEF;
        }
        final int idx;
        try {
            idx = Integer.parseInt(value.replace("H", ""), 16);
        } catch (final NumberFormatException e) {
            LOGGER.debug("unexpected temperature value {}", value);
            return UnDefType.UNDEF;
        }
        if (idx < 0 || idx >= TEMP_RANGE_C.length) {
            return UnDefType.UNDEF;
        }
        return new QuantityType<>(TEMP_RANGE_C[idx], CELSIUS);
    }
}
