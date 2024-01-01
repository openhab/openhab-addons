/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.Command;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.ConversionContext;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.DebugDetails;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.Protocol;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.MeasuredValue;
import org.openhab.core.util.HexUtils;

/**
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
class FineOffsetDataParserTest {
    @Test
    void testLiveDataWH45() {
        byte[] bytes = HexUtils.hexToBytes(
                "FFFF2700510100D306280827EF0927EF020045074F0A00150B00000C0000150000000016000117001900000E0000100000110021120000002113000005850D00007000D12E0060005A005B005502AE028F0633");
        DebugDetails debugDetails = new DebugDetails(bytes, Command.CMD_GW1000_LIVEDATA, Protocol.DEFAULT);
        List<MeasuredValue> data = new FineOffsetDataParser(Protocol.DEFAULT).getMeasuredValues(bytes,
                new ConversionContext(ZoneOffset.UTC), debugDetails);
        Assertions.assertThat(data)
                .extracting(MeasuredValue::getChannelId, measuredValue -> measuredValue.getState().toString())
                .containsExactly(new Tuple("temperature-indoor", "21.1 °C"), new Tuple("humidity-indoor", "40 %"),
                        new Tuple("pressure-absolute", "1022.3 hPa"), new Tuple("pressure-relative", "1022.3 hPa"),
                        new Tuple("temperature-outdoor", "6.9 °C"), new Tuple("humidity-outdoor", "79 %"),
                        new Tuple("direction-wind", "21 °"), new Tuple("speed-wind", "0 m/s"),
                        new Tuple("speed-gust", "0 m/s"), new Tuple("illumination", "0 lx"),
                        new Tuple("irradiation-uv", "0.1 mW/m²"), new Tuple("uv-index", "0"),
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

    @Test
    void testLiveDataWithManySensors() {
        byte[] bytes = HexUtils.hexToBytes(
                "FFFF27007B0100D206240826CC0926CC02004907450A00760B00160C001F150001C00C16000017002A00144D00372C381A0085223E1B00A72333580059005A00620000000061FFFFFFFF60FF1900380E000010002D1100A012000000A013000000A00D009F63004D417000CF2C00250020001B0018020B021E06722164");
        DebugDetails debugDetails = new DebugDetails(bytes, Command.CMD_GW1000_LIVEDATA, Protocol.DEFAULT);
        List<MeasuredValue> data = new FineOffsetDataParser(Protocol.DEFAULT).getMeasuredValues(bytes,
                new ConversionContext(ZoneOffset.UTC), debugDetails);
        Assertions.assertThat(data)
                .extracting(MeasuredValue::getChannelId, measuredValue -> measuredValue.getState().toString())
                .containsExactly(new Tuple("temperature-indoor", "21 °C"), new Tuple("humidity-indoor", "36 %"),
                        new Tuple("pressure-absolute", "993.2 hPa"), new Tuple("pressure-relative", "993.2 hPa"),
                        new Tuple("temperature-outdoor", "7.3 °C"), new Tuple("humidity-outdoor", "69 %"),
                        new Tuple("direction-wind", "118 °"), new Tuple("speed-wind", "2.2 m/s"),
                        new Tuple("speed-gust", "3.1 m/s"), new Tuple("illumination", "11470 lx"),
                        new Tuple("irradiation-uv", "0 mW/m²"), new Tuple("uv-index", "0"),
                        new Tuple("air-quality-channel-1", "2 µg/m³"),
                        new Tuple("air-quality-24-hour-average-channel-1", "5.5 µg/m³"),
                        new Tuple("moisture-soil-channel-1", "56 %"), new Tuple("temperature-channel-1", "13.3 °C"),
                        new Tuple("humidity-channel-1", "62 %"), new Tuple("temperature-channel-2", "16.7 °C"),
                        new Tuple("humidity-channel-2", "51 %"), new Tuple("water-leak-channel-1", "OFF"),
                        new Tuple("water-leak-channel-2", "OFF"), new Tuple("water-leak-channel-3", "OFF"),
                        new Tuple("lightning-counter", "0"), new Tuple("lightning-time", "UNDEF"),
                        new Tuple("lightning-distance", "UNDEF"), new Tuple("wind-max-day", "5.6 m/s"),
                        new Tuple("rain-rate", "0 mm/h"), new Tuple("rain-day", "4.5 mm"),
                        new Tuple("rain-week", "16 mm"), new Tuple("rain-month", "16 mm"),
                        new Tuple("rain-year", "16 mm"), new Tuple("rain-event", "15.9 mm"),
                        new Tuple("temperature-external-channel-1", "7.7 °C"),
                        new Tuple("sensor-co2-temperature", "20.7 °C"), new Tuple("sensor-co2-humidity", "44 %"),
                        new Tuple("sensor-co2-pm10", "3.7 µg/m³"),
                        new Tuple("sensor-co2-pm10-24-hour-average", "3.2 µg/m³"),
                        new Tuple("sensor-co2-pm25", "2.7 µg/m³"),
                        new Tuple("sensor-co2-pm25-24-hour-average", "2.4 µg/m³"),
                        new Tuple("sensor-co2-co2", "523 ppm"), new Tuple("sensor-co2-co2-24-hour-average", "542 ppm"),
                        new Tuple("leaf-wetness-channel-1", "33 %"));
    }

    @Test
    void testLiveDataWH34AndWh45() {
        byte[] bytes = HexUtils.hexToBytes(
                "FFFF2700540100CA063E0826EC0926EC02007A074C0A002F0B001F0C0023150000032016000017001A0086225558005A00620000000661654A5AF1601B1900266300884B7000CE3F001D00240016001E041A037B0695");
        DebugDetails debugDetails = new DebugDetails(bytes, Command.CMD_GW1000_LIVEDATA, Protocol.DEFAULT);
        List<MeasuredValue> data = new FineOffsetDataParser(Protocol.DEFAULT).getMeasuredValues(bytes,
                new ConversionContext(ZoneOffset.UTC), debugDetails);
        Assertions.assertThat(data)
                .extracting(MeasuredValue::getChannelId, measuredValue -> measuredValue.getState().toString())
                .containsExactly(new Tuple("temperature-indoor", "20.2 °C"), new Tuple("humidity-indoor", "62 %"),
                        new Tuple("pressure-absolute", "996.4 hPa"), new Tuple("pressure-relative", "996.4 hPa"),
                        new Tuple("temperature-outdoor", "12.2 °C"), new Tuple("humidity-outdoor", "76 %"),
                        new Tuple("direction-wind", "47 °"), new Tuple("speed-wind", "3.1 m/s"),
                        new Tuple("speed-gust", "3.5 m/s"), new Tuple("illumination", "80 lx"),
                        new Tuple("irradiation-uv", "0 mW/m²"), new Tuple("uv-index", "0"),
                        new Tuple("temperature-channel-1", "13.4 °C"), new Tuple("humidity-channel-1", "85 %"),
                        new Tuple("water-leak-channel-1", "OFF"), new Tuple("water-leak-channel-3", "OFF"),
                        new Tuple("lightning-counter", "6"),
                        new Tuple("lightning-time", "2023-11-07T15:42:41.000+0000"),
                        new Tuple("lightning-distance", "27 km"), new Tuple("wind-max-day", "3.8 m/s"),
                        new Tuple("temperature-external-channel-1", "13.6 °C"),
                        new Tuple("sensor-co2-temperature", "20.6 °C"), new Tuple("sensor-co2-humidity", "63 %"),
                        new Tuple("sensor-co2-pm10", "2.9 µg/m³"),
                        new Tuple("sensor-co2-pm10-24-hour-average", "3.6 µg/m³"),
                        new Tuple("sensor-co2-pm25", "2.2 µg/m³"),
                        new Tuple("sensor-co2-pm25-24-hour-average", "3 µg/m³"),
                        new Tuple("sensor-co2-co2", "1050 ppm"),
                        new Tuple("sensor-co2-co2-24-hour-average", "891 ppm"));
    }

    @Test
    void testLiveDataELV() {
        byte[] data = HexUtils.hexToBytes(
                "FFFF0B00500401010B0201120300620401120501120629072108254B09254B0A01480B00040C000A0E000000001000000021110000002E120000014F130000100714000012FD15000B4BB816086917056D35");
        DebugDetails debugDetails = new DebugDetails(data, Command.CMD_WS980_LIVEDATA, Protocol.ELV);
        List<MeasuredValue> measuredValues = new FineOffsetDataParser(Protocol.ELV).getMeasuredValues(data,
                new ConversionContext(ZoneOffset.UTC), debugDetails);
        Assertions.assertThat(measuredValues)
                .extracting(MeasuredValue::getChannelId, measuredValue -> measuredValue.getState().toString())
                .containsExactly(new Tuple("temperature-indoor", "26.7 °C"),
                        new Tuple("temperature-outdoor", "27.4 °C"), new Tuple("temperature-dew-point", "9.8 °C"),
                        new Tuple("temperature-wind-chill", "27.4 °C"), new Tuple("temperature-heat-index", "27.4 °C"),
                        new Tuple("humidity-indoor", "41 %"), new Tuple("humidity-outdoor", "33 %"),
                        new Tuple("pressure-absolute", "954.7 hPa"), new Tuple("pressure-relative", "954.7 hPa"),
                        new Tuple("direction-wind", "328 °"), new Tuple("speed-wind", "0.4 m/s"),
                        new Tuple("speed-gust", "1 m/s"), new Tuple("rain-rate", "0 mm/h"),
                        new Tuple("rain-day", "3.3 mm"), new Tuple("rain-week", "4.6 mm"),
                        new Tuple("rain-month", "33.5 mm"), new Tuple("rain-year", "410.3 mm"),
                        new Tuple("rain-total", "486.1 mm"), new Tuple("illumination", "74028 lx"),
                        new Tuple("irradiation-uv", "215.3 mW/m²"), new Tuple("uv-index", "5"));
    }

    @Test
    void testRainData() {
        byte[] data = HexUtils
                .hexToBytes("FFFF5700290E000010000000001100000024120000003113000005030D00000F0064880000017A017B0030");
        DebugDetails debugDetails = new DebugDetails(data, Command.CMD_READ_RAIN, Protocol.DEFAULT);
        List<MeasuredValue> measuredValues = new FineOffsetDataParser(Protocol.DEFAULT).getRainData(data,
                new ConversionContext(ZoneOffset.UTC), debugDetails);
        Assertions.assertThat(measuredValues)
                .extracting(MeasuredValue::getChannelId, measuredValue -> measuredValue.getState().toString())
                .containsExactly(new Tuple("rain-rate", "0 mm/h"), new Tuple("rain-day", "0 mm"),
                        new Tuple("rain-week", "3.6 mm"), new Tuple("rain-month", "4.9 mm"),
                        new Tuple("rain-year", "128.3 mm"), new Tuple("rain-event", "0 mm"),
                        new Tuple("rain-hour", "10 mm"));
    }

    @Test
    void testRainDataW90() {
        byte[] data = HexUtils.hexToBytes(
                "FFFF5700398000008300000009840000000985000000C786000000C7810000870064006400640064006400640064006400640064880900007A02BF");
        Assertions.assertThat(Command.CMD_READ_RAIN.isResponseValid(data)).isTrue();
        DebugDetails debugDetails = new DebugDetails(data, Command.CMD_READ_RAIN, Protocol.DEFAULT);
        List<MeasuredValue> measuredValues = new FineOffsetDataParser(Protocol.DEFAULT).getRainData(data,
                new ConversionContext(ZoneOffset.UTC), debugDetails);
        Assertions.assertThat(measuredValues)
                .extracting(MeasuredValue::getChannelId, measuredValue -> measuredValue.getState().toString())
                .containsExactly(new Tuple("piezo-rain-rate", "0 mm/h"), new Tuple("piezo-rain-day", "0.9 mm"),
                        new Tuple("piezo-rain-week", "0.9 mm"), new Tuple("piezo-rain-month", "19.9 mm"),
                        new Tuple("piezo-rain-year", "19.9 mm"), new Tuple("piezo-rain-event", "0 mm"));
    }

    @Test
    void testFirmware() {
        byte[] data = HexUtils.hexToBytes("FFFF501511456173795765617468657256312E362E3400");
        String firmware = new FineOffsetDataParser(Protocol.ELV).getFirmwareVersion(data);
        Assertions.assertThat(firmware).isEqualTo("EasyWeatherV1.6.4");
    }
}
