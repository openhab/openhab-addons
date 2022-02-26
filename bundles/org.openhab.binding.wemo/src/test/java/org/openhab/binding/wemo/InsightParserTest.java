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
    public void parseInsightParams() {
        InsightParser parser = new InsightParser(
                "1|1645800647|109676|80323|1196960|1209600|44|41400|30288361|483361410|8000");
        Map<String, State> result = parser.parse();
        assertEquals(result.get(WemoBindingConstants.CHANNEL_STATE), OnOffType.ON);
        assertEquals(result.get(WemoBindingConstants.CHANNEL_LASTCHANGEDAT),
                DateTimeType.valueOf("2022-02-25T15:50:47.000+0100").toLocaleZone());
        assertEquals(result.get(WemoBindingConstants.CHANNEL_LASTONFOR), new DecimalType(109_676));
        assertEquals(result.get(WemoBindingConstants.CHANNEL_ONTODAY), new DecimalType(80_323));
        assertEquals(result.get(WemoBindingConstants.CHANNEL_ONTOTAL), new DecimalType(1_196_960));
        assertEquals(result.get(WemoBindingConstants.CHANNEL_TIMESPAN), new DecimalType(1_209_600));
        assertEquals(result.get(WemoBindingConstants.CHANNEL_AVERAGEPOWER), new QuantityType<>(44, Units.WATT));
        assertEquals(result.get(WemoBindingConstants.CHANNEL_CURRENTPOWER), new QuantityType<>(41, Units.WATT));
        assertEquals(result.get(WemoBindingConstants.CHANNEL_ENERGYTODAY), new QuantityType<>(505, Units.WATT_HOUR));
        assertEquals(result.get(WemoBindingConstants.CHANNEL_ENERGYTOTAL), new QuantityType<>(8056, Units.WATT_HOUR));
        assertEquals(result.get(WemoBindingConstants.CHANNEL_STANDBYLIMIT), new QuantityType<>(8, Units.WATT));
    }

    /**
     * Some devices provide 'BinaryState' for subscription 'basicevent1'. This contains
     * the same information as 'InsightParams' except last parameter (stand-by limit).
     */
    @Test
    public void parseBinaryState() {
        InsightParser parser = new InsightParser(
                "1|1645800647|109676|80323|1196960|1209600|44|41400|30288361|483361410");
        Map<String, State> result = parser.parse();
        assertEquals(result.get(WemoBindingConstants.CHANNEL_STATE), OnOffType.ON);
        assertEquals(result.get(WemoBindingConstants.CHANNEL_LASTCHANGEDAT),
                DateTimeType.valueOf("2022-02-25T15:50:47.000+0100").toLocaleZone());
        assertEquals(result.get(WemoBindingConstants.CHANNEL_LASTONFOR), new DecimalType(109_676));
        assertEquals(result.get(WemoBindingConstants.CHANNEL_ONTODAY), new DecimalType(80_323));
        assertEquals(result.get(WemoBindingConstants.CHANNEL_ONTOTAL), new DecimalType(1_196_960));
        assertEquals(result.get(WemoBindingConstants.CHANNEL_TIMESPAN), new DecimalType(1_209_600));
        assertEquals(result.get(WemoBindingConstants.CHANNEL_AVERAGEPOWER), new QuantityType<>(44, Units.WATT));
        assertEquals(result.get(WemoBindingConstants.CHANNEL_CURRENTPOWER), new QuantityType<>(41, Units.WATT));
        assertEquals(result.get(WemoBindingConstants.CHANNEL_ENERGYTODAY), new QuantityType<>(505, Units.WATT_HOUR));
        assertEquals(result.get(WemoBindingConstants.CHANNEL_ENERGYTOTAL), new QuantityType<>(8056, Units.WATT_HOUR));
        assertNull(result.get(WemoBindingConstants.CHANNEL_STANDBYLIMIT));
    }

    @Test
    public void parseInvalidLastChangedAt() {
        InsightParser parser = new InsightParser("1|A");
        Map<String, State> result = parser.parse();
        assertEquals(result.get(WemoBindingConstants.CHANNEL_LASTCHANGEDAT), UnDefType.UNDEF);
    }

    @Test
    public void parseInvalidLastOnFor() {
        InsightParser parser = new InsightParser("1|1645800647|A");
        Map<String, State> result = parser.parse();
        assertEquals(result.get(WemoBindingConstants.CHANNEL_LASTONFOR), UnDefType.UNDEF);
    }
}
