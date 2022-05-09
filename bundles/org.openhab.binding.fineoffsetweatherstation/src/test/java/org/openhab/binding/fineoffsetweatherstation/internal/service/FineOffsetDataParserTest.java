/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.fineoffsetweatherstation.internal.service;

import java.time.ZoneOffset;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.bouncycastle.util.encoders.Hex;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.ConversionContext;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.MeasuredValue;

/**
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
class FineOffsetDataParserTest {
    private final FineOffsetDataParser parser = new FineOffsetDataParser();

    @Test
    void testLiveDataWH45() {
        List<MeasuredValue> data = parser.getLiveData(Hex.decode(
                "FFFF2700510100D306280827EF0927EF020045074F0A00150B00000C0000150000000016000117001900000E0000100000110021120000002113000005850D00007000D12E0060005A005B005502AE028F0633"),
                new ConversionContext(ZoneOffset.UTC));
        Assertions.assertThat(data)
                .extracting(MeasuredValue::getChannelId, measuredValue -> measuredValue.getState().toString())
                .containsExactly(new Tuple("temperature-indoor", "21.1 °C"), new Tuple("humidity-indoor", "40 %"),
                        new Tuple("pressure-absolute", "10223 hPa"), new Tuple("pressure-relative", "10223 hPa"),
                        new Tuple("temperature-outdoor", "6.9 °C"), new Tuple("humidity-outdoor", "79 %"),
                        new Tuple("direction-wind", "21 °"), new Tuple("speed-wind", "0 m/s"),
                        new Tuple("speed-gust", "0 m/s"), new Tuple("illumination", "0 lx"),
                        new Tuple("irradiation-uv", "1 µW/cm²"), new Tuple("uv-index", "0"),
                        new Tuple("wind-max-day", "0 m/s"), new Tuple("rain-rate", "0 mm/h"),
                        new Tuple("rain-day", "0 mm"), new Tuple("rain-week", "3.3 mm"),
                        new Tuple("rain-month", "3.3 mm"), new Tuple("rain-year", "141.3 mm"),
                        new Tuple("rain-event", "0 mm"), new Tuple("sensor-co2-temperature", "20.9 °C"),
                        new Tuple("sensor-co2-humidity", "46 %"), new Tuple("sensor-co2-pm10", "9.6 µg/m³"),
                        new Tuple("sensor-co2-pm10-24-hour-average", "9 µg/m³"),
                        new Tuple("sensor-co2-pm25", "9.1 µg/m³"),
                        new Tuple("sensor-co2-pm25-24-hour-average", "8.5 µg/m³"),
                        new Tuple("sensor-co2-co2", "686 ppm"), new Tuple("sensor-co2-co2-24-hour-average", "655 ppm"));
    }
}
