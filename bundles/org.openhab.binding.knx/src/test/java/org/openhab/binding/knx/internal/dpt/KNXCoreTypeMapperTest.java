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
package org.openhab.binding.knx.internal.dpt;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;

/**
 *
 * @author Simon Kaufmann - initial contribution and API
 *
 */
@NonNullByDefault
class KNXCoreTypeMapperTest {

    @Test
    void testToDPTValueTrailingZeroesStrippedOff() {
        assertEquals("3", new KNXCoreTypeMapper().toDPTValue(new DecimalType("3"), "17.001"));
        assertEquals("3", new KNXCoreTypeMapper().toDPTValue(new DecimalType("3.0"), "17.001"));
    }

    @Test
    @SuppressWarnings("null")
    void testToDPT5ValueFromQuantityType() {
        assertEquals("80.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("80 %"), "5.001"));

        assertEquals("180.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("180 °"), "5.003"));
        assertTrue(new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("3.14 rad"), "5.003").startsWith("179."));
        assertEquals("80.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("80 %"), "5.004"));
    }

    @Test
    @SuppressWarnings("null")
    void testToDPT7ValueFromQuantityType() {
        assertEquals("1000.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1000 ms"), "7.002"));
        assertEquals("1000.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1000 ms"), "7.003"));
        assertEquals("1000.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1000 ms"), "7.004"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1000 ms"), "7.005"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("60 s"), "7.006"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("60 min"), "7.007"));

        assertEquals("1000.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 m"), "7.011"));
        assertEquals("1000.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1000 mA"), "7.012"));
        assertEquals("1000.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1000 lx"), "7.013"));

        assertEquals("3000.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("3000 K"), "7.600"));
    }

    @Test
    @SuppressWarnings("null")
    void testToDPT8ValueFromQuantityType() {
        assertEquals("1000.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1000 ms"), "8.002"));
        assertEquals("1000.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1000 ms"), "8.003"));
        assertEquals("1000.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1000 ms"), "8.004"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1000 ms"), "8.005"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("60 s"), "8.006"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("60 min"), "8.007"));

        assertEquals("180.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("180 °"), "8.011"));
        assertEquals("1000.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 km"), "8.012"));
    }

    @Test
    @SuppressWarnings("null")
    void testToDPT9ValueFromQuantityType() {
        assertEquals("23.1", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("23.1 °C"), "9.001"));
        assertEquals("5.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("41 °F"), "9.001"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("274.15 K"), "9.001"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 K"), "9.002"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1000 mK"), "9.002"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 °C"), "9.002"));
        assertTrue(new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 °F"), "9.002").startsWith("0.55"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 K/h"), "9.003"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 °C/h"), "9.003"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1000 mK/h"), "9.003"));
        assertEquals("600.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("10 K/min"), "9.003"));
        assertEquals("100.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("100 lx"), "9.004"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 m/s"), "9.005"));
        assertTrue(new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1.94 kn"), "9.005").startsWith("0.99"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("3.6 km/h"), "9.005"));
        assertEquals("456.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("456 Pa"), "9.006"));
        assertEquals("70.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("70 %"), "9.007"));
        assertEquals("8.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("8 ppm"), "9.008"));
        assertEquals("9.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("9 m³/h"), "9.009"));
        assertEquals("10.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("10 s"), "9.010"));
        assertEquals("11.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("0.011 s"), "9.011"));

        assertEquals("20.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("20 mV"), "9.020"));
        assertEquals("20.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("0.02 V"), "9.020"));
        assertEquals("21.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("21 mA"), "9.021"));
        assertEquals("21.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("0.021 A"), "9.021"));
        assertEquals("12.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("12 W/m²"), "9.022"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 K/%"), "9.023"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 °C/%"), "9.023"));
        assertTrue(new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 °F/%"), "9.023").startsWith("0.55"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 kW"), "9.024"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 l/h"), "9.025"));
        assertEquals("60.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 l/min"), "9.025"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 l/m²"), "9.026"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 °F"), "9.027"));
        assertTrue(new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("-12 °C"), "9.027").startsWith("10."));
        assertEquals("10.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("10 km/h"), "9.028"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 g/m³"), "9.029"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 µg/m³"), "9.030"));
    }

    @Test
    @SuppressWarnings("null")
    void testToDPT10ValueFromQuantityType() {
        // DateTimeTyype, not QuantityType
        assertEquals("Wed, 17:30:00",
                new KNXCoreTypeMapper().toDPTValue(new DateTimeType("2019-06-12T17:30:00Z"), "10.001"));
    }

    @Test
    @SuppressWarnings("null")
    void testToDPT11ValueFromQuantityType() {
        // DateTimeTyype, not QuantityType
        assertEquals("2019-06-12",
                new KNXCoreTypeMapper().toDPTValue(new DateTimeType("2019-06-12T17:30:00Z"), "11.001"));
    }

    @Test
    @SuppressWarnings("null")
    void testToDPT12ValueFromQuantityType() {
        // 12.001: dimensionless

        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 s"), "12.100"));
        assertEquals("2.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("2 min"), "12.101"));
        assertEquals("3.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("3 h"), "12.102"));

        assertEquals("1000.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 m^3"), "12.1200"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 l"), "12.1200"));
        assertEquals("2.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("2 m³"), "12.1201"));
    }

    @Test
    @SuppressWarnings("null")
    void testToDPT13ValueFromQuantityType() {
        // 13.001 dimensionless
        assertEquals("24.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("24 m³/h"), "13.002"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("24 m³/d"), "13.002"));

        assertEquals("42.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("42 Wh"), "13.010"));
        assertEquals("42.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("42 VAh"), "13.011"));
        assertEquals("42.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("42 varh"), "13.012"));
        assertEquals("42.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("42 kWh"), "13.013"));
        assertEquals("4.2", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("4200 VAh"), "13.014"));
        assertEquals("42.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("42 kvarh"), "13.015"));
        assertEquals("42.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("42 MWh"), "13.016"));

        assertEquals("42.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("42 s"), "13.100"));

        assertEquals("42.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("42 l"), "13.1200"));
        assertEquals("42.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("42 m³"), "13.1201"));
    }

    @Test
    @SuppressWarnings("null")
    void testToDPT14ValueFromQuantityType() {
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 m/s²"), "14.000"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 rad/s²"), "14.001"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 J/mol"), "14.002"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 /s"), "14.003"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 mol"), "14.004"));

        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 rad"), "14.006"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 °"), "14.007"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 J*s"), "14.008"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 rad/s"), "14.009"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 m²"), "14.010"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 F"), "14.011"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 C/m²"), "14.012"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 C/m³"), "14.013"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 m²/N"), "14.014"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 S"), "14.015"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 S/m"), "14.016"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 kg/m³"), "14.017"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 C"), "14.018"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 A"), "14.019"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 A/m²"), "14.020"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 C*m"), "14.021"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 C/m²"), "14.022"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 V/m"), "14.023"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 C"), "14.024"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 C/m²"), "14.025"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 C/m²"), "14.026"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 V"), "14.027"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 V"), "14.028"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 A*m²"), "14.029"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 V"), "14.030"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 J"), "14.031"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 N"), "14.032"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 Hz"), "14.033"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 rad/s"), "14.034"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 J/K"), "14.035"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 W"), "14.036"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 J"), "14.037"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 Ohm"), "14.038"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 m"), "14.039"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 J"), "14.040"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 lm*s"), "14.040"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 cd/m²"), "14.041"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 lm"), "14.042"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 cd"), "14.043"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 A/m"), "14.044"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 Wb"), "14.045"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 T"), "14.046"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 A*m²"), "14.047"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 T"), "14.048"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 A/m"), "14.049"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 A"), "14.050"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 kg"), "14.051"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 kg/s"), "14.052"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 N/s"), "14.053"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 rad"), "14.054"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 °"), "14.055"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 W"), "14.056"));
        // 14.057: dimensionless
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 Pa"), "14.058"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 Ohm"), "14.059"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 Ohm"), "14.060"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 Ohm*m"), "14.061"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 H"), "14.062"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 sr"), "14.063"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 W/m²"), "14.064"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 m/s"), "14.065"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 Pa"), "14.066"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 N/m"), "14.067"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 °C"), "14.068"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 K"), "14.069"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 K"), "14.070"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 J/K"), "14.071"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 W/m/K"), "14.072"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 V/K"), "14.073"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 s"), "14.074"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 N*m"), "14.075"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 J"), "14.075"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 m³"), "14.076"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 m³/s"), "14.077"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 N"), "14.078"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 J"), "14.079"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 VA"), "14.080"));

        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 m³/h"), "14.1200"));
        assertEquals("1.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("1 l/s"), "14.1201"));
    }

    @Test
    @SuppressWarnings("null")
    void testToDPT19ValueFromQuantityType() {
        // DateTimeTyype, not QuantityType
        assertEquals("2019-06-12 17:30:00",
                new KNXCoreTypeMapper().toDPTValue(new DateTimeType("2019-06-12T17:30:00Z"), "19.001"));
    }

    @Test
    @SuppressWarnings("null")
    void testToDPT29ValueFromQuantityType() {
        assertEquals("42.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("42 Wh"), "29.010"));
        assertEquals("42.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("42 VAh"), "29.011"));
        assertEquals("42.0", new KNXCoreTypeMapper().toDPTValue(new QuantityType<>("42 varh"), "29.012"));
    }
}
