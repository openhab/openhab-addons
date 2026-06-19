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

import org.assertj.core.api.Assertions;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;

/**
 * Verifies the reverse {@code (Sensor, channel) -> SensorGatewayBinding} lookup used to route measured values
 * onto sensor Things.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
class SensorGatewayBindingTest {

    @Test
    void channelledSensorResolvesByChannel() {
        Assertions.assertThat(SensorGatewayBinding.forSensorAndChannel(Sensor.WH51, 1))
                .isEqualTo(SensorGatewayBinding.WH51_CH1);
        Assertions.assertThat(SensorGatewayBinding.forSensorAndChannel(Sensor.WH51, 16))
                .isEqualTo(SensorGatewayBinding.WH51_CH16);
        Assertions.assertThat(SensorGatewayBinding.forSensorAndChannel(Sensor.WH31, 8))
                .isEqualTo(SensorGatewayBinding.WH31_CH8);
    }

    @Test
    void channellessSensorResolvesWithNullChannel() {
        Assertions.assertThat(SensorGatewayBinding.forSensorAndChannel(Sensor.WH57, null))
                .isEqualTo(SensorGatewayBinding.WH57);
        Assertions.assertThat(SensorGatewayBinding.forSensorAndChannel(Sensor.WH45, null))
                .isEqualTo(SensorGatewayBinding.WH45);
    }

    @Test
    void soilMoistureAndTemperatureConvergeOnTheSameBinding() {
        // WH51 soil moisture and soil temperature of channel 1 are the same physical sensor: both must resolve
        // to the single WH51_CH1 binding so they land on one sensor Thing.
        @Nullable
        SensorGatewayBinding fromMoisture = SensorGatewayBinding.forSensorAndChannel(Sensor.WH51, 1);
        @Nullable
        SensorGatewayBinding fromTemperature = SensorGatewayBinding.forSensorAndChannel(Sensor.WH51, 1);
        Assertions.assertThat(fromMoisture).isEqualTo(fromTemperature).isEqualTo(SensorGatewayBinding.WH51_CH1);
    }

    @Test
    void unknownCombinationsResolveToNull() {
        // WH57 has no channels -> a channelled query must miss
        Assertions.assertThat(SensorGatewayBinding.forSensorAndChannel(Sensor.WH57, 1)).isNull();
        // WH31 is channelled -> a null-channel query must miss
        Assertions.assertThat(SensorGatewayBinding.forSensorAndChannel(Sensor.WH31, null)).isNull();
        // out-of-range channel
        Assertions.assertThat(SensorGatewayBinding.forSensorAndChannel(Sensor.WH41, 9)).isNull();
    }
}
