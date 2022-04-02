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
package org.openhab.binding.wemo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.ZoneId;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.wemo.internal.InsightParser;
import org.openhab.binding.wemo.internal.WemoBindingConstants;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Unit tests for {@link InsightParser}.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class InsightParserTest {

    /**
     * 'InsightParams' for subscription 'insight1'.
     */
    @Test
    public void parseUpnpInsightParams() {
        InsightParser parser = new InsightParser(
                "1|1645800647|109676|80323|1196960|1209600|44|41400|30288361|483361410|8000");
        Map<String, State> result = parser.parse();
        assertEquals(OnOffType.ON, result.get(WemoBindingConstants.CHANNEL_STATE));
        assertEquals(DateTimeType.valueOf("2022-02-25T15:50:47.000+0100").toZone(ZoneId.systemDefault()),
                result.get(WemoBindingConstants.CHANNEL_LAST_CHANGED_AT));
        assertEquals(new DecimalType(109_676), result.get(WemoBindingConstants.CHANNEL_LAST_ON_FOR));
        assertEquals(new DecimalType(80_323), result.get(WemoBindingConstants.CHANNEL_ON_TODAY));
        assertEquals(new DecimalType(1_196_960), result.get(WemoBindingConstants.CHANNEL_ON_TOTAL));
        assertEquals(new DecimalType(1_209_600), result.get(WemoBindingConstants.CHANNEL_TIMESPAN));
        assertEquals(new QuantityType<>(44, Units.WATT), result.get(WemoBindingConstants.CHANNEL_AVERAGE_POWER));
        assertEquals(new QuantityType<>(41.4, Units.WATT), result.get(WemoBindingConstants.CHANNEL_CURRENT_POWER_RAW));
        assertEquals(new QuantityType<>(505, Units.WATT_HOUR), result.get(WemoBindingConstants.CHANNEL_ENERGY_TODAY));
        assertEquals(new QuantityType<>(8056, Units.WATT_HOUR), result.get(WemoBindingConstants.CHANNEL_ENERGY_TOTAL));
        assertEquals(new QuantityType<>(8, Units.WATT), result.get(WemoBindingConstants.CHANNEL_STAND_BY_LIMIT));
    }

    /**
     * 'InsightParams' received from HTTP call. Format is a bit different: State can be non-binary,
     * e.g. 8 for ON, and energy total is formatted with decimals.
     */
    @Test
    public void parseHttpInsightParams() {
        InsightParser parser = new InsightParser("8|1645967627|0|0|0|1209600|13|0|0|0.000000|8000");
        Map<String, State> result = parser.parse();
        assertEquals(OnOffType.ON, result.get(WemoBindingConstants.CHANNEL_STATE));
        assertEquals(DateTimeType.valueOf("2022-02-27T14:13:47.000+0100").toZone(ZoneId.systemDefault()),
                result.get(WemoBindingConstants.CHANNEL_LAST_CHANGED_AT));
        assertEquals(new DecimalType(0), result.get(WemoBindingConstants.CHANNEL_LAST_ON_FOR));
        assertEquals(new DecimalType(0), result.get(WemoBindingConstants.CHANNEL_ON_TODAY));
        assertEquals(new DecimalType(0), result.get(WemoBindingConstants.CHANNEL_ON_TOTAL));
        assertEquals(new DecimalType(1_209_600), result.get(WemoBindingConstants.CHANNEL_TIMESPAN));
        assertEquals(new QuantityType<>(13, Units.WATT), result.get(WemoBindingConstants.CHANNEL_AVERAGE_POWER));
        assertEquals(new QuantityType<>(0, Units.WATT), result.get(WemoBindingConstants.CHANNEL_CURRENT_POWER_RAW));
        assertEquals(new QuantityType<>(0, Units.WATT_HOUR), result.get(WemoBindingConstants.CHANNEL_ENERGY_TODAY));
        assertEquals(new QuantityType<>(0, Units.WATT_HOUR), result.get(WemoBindingConstants.CHANNEL_ENERGY_TOTAL));
        assertEquals(new QuantityType<>(8, Units.WATT), result.get(WemoBindingConstants.CHANNEL_STAND_BY_LIMIT));
    }

    /**
     * Some devices provide 'BinaryState' for subscription 'basicevent1'. This contains
     * the same information as 'InsightParams' except last parameter (stand-by limit).
     */
    @Test
    public void parseUpnpBinaryState() {
        InsightParser parser = new InsightParser(
                "1|1645800647|109676|80323|1196960|1209600|44|41400|30288361|483361410");
        Map<String, State> result = parser.parse();
        assertEquals(OnOffType.ON, result.get(WemoBindingConstants.CHANNEL_STATE));
        assertEquals(DateTimeType.valueOf("2022-02-25T15:50:47.000+0100").toZone(ZoneId.systemDefault()),
                result.get(WemoBindingConstants.CHANNEL_LAST_CHANGED_AT));
        assertEquals(new DecimalType(109_676), result.get(WemoBindingConstants.CHANNEL_LAST_ON_FOR));
        assertEquals(new DecimalType(80_323), result.get(WemoBindingConstants.CHANNEL_ON_TODAY));
        assertEquals(new DecimalType(1_196_960), result.get(WemoBindingConstants.CHANNEL_ON_TOTAL));
        assertEquals(new DecimalType(1_209_600), result.get(WemoBindingConstants.CHANNEL_TIMESPAN));
        assertEquals(new QuantityType<>(44, Units.WATT), result.get(WemoBindingConstants.CHANNEL_AVERAGE_POWER));
        assertEquals(new QuantityType<>(41.4, Units.WATT), result.get(WemoBindingConstants.CHANNEL_CURRENT_POWER_RAW));
        assertEquals(new QuantityType<>(505, Units.WATT_HOUR), result.get(WemoBindingConstants.CHANNEL_ENERGY_TODAY));
        assertEquals(new QuantityType<>(8056, Units.WATT_HOUR), result.get(WemoBindingConstants.CHANNEL_ENERGY_TOTAL));
        assertNull(result.get(WemoBindingConstants.CHANNEL_STAND_BY_LIMIT));
    }

    @Test
    public void parseInvalidLastChangedAt() {
        InsightParser parser = new InsightParser("1|A");
        Map<String, State> result = parser.parse();
        assertEquals(UnDefType.UNDEF, result.get(WemoBindingConstants.CHANNEL_LAST_CHANGED_AT));
    }

    @Test
    public void parseInvalidLastOnFor() {
        InsightParser parser = new InsightParser("1|1645800647|A");
        Map<String, State> result = parser.parse();
        assertEquals(UnDefType.UNDEF, result.get(WemoBindingConstants.CHANNEL_LAST_ON_FOR));
    }
}
