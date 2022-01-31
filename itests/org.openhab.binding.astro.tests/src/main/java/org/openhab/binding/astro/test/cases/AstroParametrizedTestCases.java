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
package org.openhab.binding.astro.test.cases;

import static org.openhab.binding.astro.test.cases.AstroBindingTestsData.*;

import java.util.Arrays;
import java.util.List;

import org.openhab.binding.astro.test.AstroStateTest;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;

/**
 * Test cases used in the {@link AstroStateTest}
 *
 * @author Petar Valchev - Initial contribution
 * @author Svilen Valakanov - Added test data from
 *         <a href="http://www.suncalc.net">http://www.suncalc.net</a> and
 *         <a href="http://www.mooncalc.org">http://www.mooncalc.org</a>
 * @author Erdoan Hadzhiyusein - Adapted the class to work with the new DateTimeType
 * @author Christoph Weitkamp - Introduced UoM and migrated tests to pure Java
 */
public final class AstroParametrizedTestCases {

    public static final double TEST_LATITUDE = 22.4343;
    public static final double TEST_LONGITUDE = 54.3225;
    public static final int TEST_YEAR = 2016;
    public static final int TEST_MONTH = 2;
    public static final int TEST_DAY = 29;

    public Object[][] cases = new Object[42][3];

    public AstroParametrizedTestCases() {
        cases[0][0] = TEST_SUN_THING_ID;
        cases[0][1] = "rise#start";
        cases[0][2] = new DateTimeType("2016-02-29T05:46:00+03:00");

        cases[1][0] = TEST_SUN_THING_ID;
        cases[1][1] = "rise#end";
        cases[1][2] = new DateTimeType("2016-02-29T05:48:00+03:00");

        cases[2][0] = TEST_SUN_THING_ID;
        cases[2][1] = "rise#duration";
        cases[2][2] = new QuantityType<>(2, Units.MINUTE);

        cases[3][0] = TEST_SUN_THING_ID;
        cases[3][1] = "set#start";
        cases[3][2] = new DateTimeType("2016-02-29T17:25:00+03:00");

        cases[4][0] = TEST_SUN_THING_ID;
        cases[4][1] = "set#end";
        cases[4][2] = new DateTimeType("2016-02-29T17:27:00+03:00");

        cases[5][0] = TEST_SUN_THING_ID;
        cases[5][1] = "set#duration";
        cases[5][2] = new QuantityType<>(2, Units.MINUTE);

        cases[6][0] = TEST_SUN_THING_ID;
        cases[6][1] = "noon#start";
        cases[6][2] = new DateTimeType("2016-02-29T11:37:00+03:00");

        cases[7][0] = TEST_SUN_THING_ID;
        cases[7][1] = "noon#end";
        cases[7][2] = new DateTimeType("2016-02-29T11:38:00+03:00");

        cases[8][0] = TEST_SUN_THING_ID;
        cases[8][1] = "noon#duration";
        cases[8][2] = new QuantityType<>(1, Units.MINUTE);

        cases[9][0] = TEST_SUN_THING_ID;
        cases[9][1] = "night#start";
        cases[9][2] = new DateTimeType("2016-02-29T18:42:00+03:00");

        cases[10][0] = TEST_SUN_THING_ID;
        cases[10][1] = "night#end";
        cases[10][2] = new DateTimeType("2016-03-01T04:31:00+03:00");

        cases[11][0] = TEST_SUN_THING_ID;
        cases[11][1] = "night#duration";
        cases[11][2] = new QuantityType<>(589, Units.MINUTE);

        cases[12][0] = TEST_SUN_THING_ID;
        cases[12][1] = "morningNight#start";
        cases[12][2] = new DateTimeType("2016-02-29T00:00:00+03:00");

        cases[13][0] = TEST_SUN_THING_ID;
        cases[13][1] = "morningNight#end";
        cases[13][2] = new DateTimeType("2016-02-29T04:32:00+03:00");

        cases[14][0] = TEST_SUN_THING_ID;
        cases[14][1] = "morningNight#duration";
        cases[14][2] = new QuantityType<>(272, Units.MINUTE);

        cases[15][0] = TEST_SUN_THING_ID;
        cases[15][1] = "astroDawn#start";
        cases[15][2] = new DateTimeType("2016-02-29T04:32:00+03:00");

        cases[16][0] = TEST_SUN_THING_ID;
        cases[16][1] = "astroDawn#end";
        cases[16][2] = new DateTimeType("2016-02-29T04:58:00+03:00");

        cases[17][0] = TEST_SUN_THING_ID;
        cases[17][1] = "astroDawn#duration";
        cases[17][2] = new QuantityType<>(26, Units.MINUTE);

        cases[18][0] = TEST_SUN_THING_ID;
        cases[18][1] = "nauticDawn#start";
        cases[18][2] = new DateTimeType("2016-02-29T04:58:00+03:00");

        cases[19][0] = TEST_SUN_THING_ID;
        cases[19][1] = "nauticDawn#end";
        cases[19][2] = new DateTimeType("2016-02-29T05:24:00+03:00");

        cases[20][0] = TEST_SUN_THING_ID;
        cases[20][1] = "nauticDawn#duration";
        cases[20][2] = new QuantityType<>(26, Units.MINUTE);

        cases[21][0] = TEST_SUN_THING_ID;
        cases[21][1] = "civilDawn#start";
        cases[21][2] = new DateTimeType("2016-02-29T05:24:00+03:00");

        cases[22][0] = TEST_SUN_THING_ID;
        cases[22][1] = "civilDawn#end";
        cases[22][2] = new DateTimeType("2016-02-29T05:46:00+03:00");

        cases[23][0] = TEST_SUN_THING_ID;
        cases[23][1] = "civilDawn#duration";
        cases[23][2] = new QuantityType<>(22, Units.MINUTE);

        cases[24][0] = TEST_SUN_THING_ID;
        cases[24][1] = "astroDusk#start";
        cases[24][2] = new DateTimeType("2016-02-29T18:16:00+03:00");

        cases[25][0] = TEST_SUN_THING_ID;
        cases[25][1] = "astroDusk#end";
        cases[25][2] = new DateTimeType("2016-02-29T18:42:00+03:00");

        cases[26][0] = TEST_SUN_THING_ID;
        cases[26][1] = "astroDusk#duration";
        cases[26][2] = new QuantityType<>(26, Units.MINUTE);

        cases[27][0] = TEST_SUN_THING_ID;
        cases[27][1] = "nauticDusk#start";
        cases[27][2] = new DateTimeType("2016-02-29T17:50:00+03:00");

        cases[28][0] = TEST_SUN_THING_ID;
        cases[28][1] = "nauticDusk#end";
        cases[28][2] = new DateTimeType("2016-02-29T18:16:00+03:00");

        cases[29][0] = TEST_SUN_THING_ID;
        cases[29][1] = "nauticDusk#duration";
        cases[29][2] = new QuantityType<>(26, Units.MINUTE);

        cases[30][0] = TEST_SUN_THING_ID;
        cases[30][1] = "civilDusk#start";
        cases[30][2] = new DateTimeType("2016-02-29T17:27:00+03:00");

        cases[31][0] = TEST_SUN_THING_ID;
        cases[31][1] = "civilDusk#end";
        cases[31][2] = new DateTimeType("2016-02-29T17:50:00+03:00");

        cases[32][0] = TEST_SUN_THING_ID;
        cases[32][1] = "civilDusk#duration";
        cases[32][2] = new QuantityType<>(23, Units.MINUTE);

        cases[33][0] = TEST_SUN_THING_ID;
        cases[33][1] = "eveningNight#start";
        cases[33][2] = new DateTimeType("2016-02-29T18:42:00+03:00");

        cases[34][0] = TEST_SUN_THING_ID;
        cases[34][1] = "eveningNight#end";
        cases[34][2] = new DateTimeType("2016-03-01T00:00:00+03:00");

        cases[35][0] = TEST_SUN_THING_ID;
        cases[35][1] = "eveningNight#duration";
        cases[35][2] = new QuantityType<>(318, Units.MINUTE);

        cases[36][0] = TEST_SUN_THING_ID;
        cases[36][1] = "daylight#start";
        cases[36][2] = new DateTimeType("2016-02-29T05:48:00+03:00");

        cases[37][0] = TEST_SUN_THING_ID;
        cases[37][1] = "daylight#end";
        cases[37][2] = new DateTimeType("2016-02-29T17:25:00+03:00");

        cases[38][0] = TEST_SUN_THING_ID;
        cases[38][1] = "daylight#duration";
        cases[38][2] = new QuantityType<>(697, Units.MINUTE);

        cases[39][0] = TEST_MOON_THING_ID;
        cases[39][1] = "rise#start";
        cases[39][2] = new DateTimeType("2016-02-29T23:00:00+03:00");

        cases[40][0] = TEST_MOON_THING_ID;
        cases[40][1] = "rise#end";
        cases[40][2] = new DateTimeType("2016-02-29T23:00:00+03:00");

        cases[41][0] = TEST_MOON_THING_ID;
        cases[41][1] = "rise#duration";
        cases[41][2] = new QuantityType<>(0, Units.MINUTE);
    }

    public List<Object[]> getCases() {
        return Arrays.asList(cases);
    }
}
