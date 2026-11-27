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
package org.openhab.binding.fineoffsetweatherstation.internal.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.assertj.core.api.Assertions;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.MeasuredValue;

/**
 * Verifies that a {@link Measurand}'s {@link Sensor} tag is carried onto the {@link MeasuredValue}s it produces,
 * via both the TCP byte path and the HTTP string path.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
class MeasurandTest {

    /**
     * DebugDetails requires (byte[], Command, Protocol); we pass a minimal zero-padded payload.
     * CMD_GW1000_LIVEDATA has sizeBytes=2, so the constructor accesses indices 0-4 and data.length-1;
     * a 6-byte array is sufficient.
     */
    private static DebugDetails stubDebugDetails() {
        return new DebugDetails(new byte[6], Command.CMD_GW1000_LIVEDATA, Protocol.DEFAULT);
    }

    @Test
    void tcpPathCarriesSensorTag() {
        Measurand tagged = Measurand.measurand("moisture-soil-channel", "Soil Moisture", MeasureType.PERCENTAGE)
                .sensor(Sensor.WH51);
        List<MeasuredValue> result = new ArrayList<>();
        // PERCENTAGE is a single unsigned byte; value 42 at offset 0
        tagged.extractMeasuredValues(new byte[] { 42 }, 0, 1, null, result, stubDebugDetails());
        Assertions.assertThat(result).singleElement()
                .satisfies(value -> Assertions.assertThat(value.getSensor()).isEqualTo(Sensor.WH51));
    }

    @Test
    void httpPathCarriesSensorTag() {
        Measurand tagged = Measurand.measurand("moisture-soil-channel", "Soil Moisture", MeasureType.PERCENTAGE)
                .sensor(Sensor.WH51);
        MeasuredValue value = Objects.requireNonNull(tagged.parseHttp("42", "%", 1, null));
        Assertions.assertThat(value.getSensor()).isEqualTo(Sensor.WH51);
    }

    @Test
    void untaggedMeasurandHasNoSensor() {
        Measurand untagged = Measurand.measurand("temperature-dew-point", "Dew point", MeasureType.TEMPERATURE);
        Assertions.assertThat(untagged.getSensor()).isNull();
    }
}
