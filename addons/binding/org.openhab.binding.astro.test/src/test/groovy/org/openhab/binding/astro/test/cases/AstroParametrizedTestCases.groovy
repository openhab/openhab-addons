/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.astro.test.cases

import static org.openhab.binding.astro.test.AstroOSGiTest.TEST_MOON_THING_ID
import static org.openhab.binding.astro.test.AstroOSGiTest.TEST_SUN_THING_ID

import org.openhab.binding.astro.test.AstroOSGiTest.AcceptedItemType

/**
 * All the channels of the astro thing, used in the parametrized tests.
 *
 * @author Petar Valchev
 *
 */
public class AstroParametrizedTestCases {
    public def cases = new Object[76][3]

    AstroParametrizedTestCases(){
        cases[0][0] = TEST_SUN_THING_ID
        cases[0][1] = "rise#start"
        cases[0][2] = AcceptedItemType.DATE_TIME

        cases[1][0] = TEST_SUN_THING_ID
        cases[1][1] = "rise#end"
        cases[1][2] = AcceptedItemType.DATE_TIME

        cases[2][0] = TEST_SUN_THING_ID
        cases[2][1] = "rise#duration"
        cases[2][2] = AcceptedItemType.NUMBER

        cases[3][0] = TEST_SUN_THING_ID
        cases[3][1] = "set#start"
        cases[3][2] = AcceptedItemType.DATE_TIME

        cases[4][0] = TEST_SUN_THING_ID
        cases[4][1] = "set#end"
        cases[4][2] = AcceptedItemType.DATE_TIME

        cases[5][0] = TEST_SUN_THING_ID
        cases[5][1] = "set#duration"
        cases[5][2] = AcceptedItemType.NUMBER

        cases[6][0] = TEST_SUN_THING_ID
        cases[6][1] = "noon#start"
        cases[6][2] = AcceptedItemType.DATE_TIME

        cases[7][0] = TEST_SUN_THING_ID
        cases[7][1] = "noon#end"
        cases[7][2] = AcceptedItemType.DATE_TIME

        cases[8][0] = TEST_SUN_THING_ID
        cases[8][1] = "noon#duration"
        cases[8][2] = AcceptedItemType.NUMBER

        cases[9][0] = TEST_SUN_THING_ID
        cases[9][1] = "night#start"
        cases[9][2] = AcceptedItemType.DATE_TIME

        cases[10][0] = TEST_SUN_THING_ID
        cases[10][1] = "night#end"
        cases[10][2] = AcceptedItemType.DATE_TIME

        cases[11][0] = TEST_SUN_THING_ID
        cases[11][1] = "night#duration"
        cases[11][2] = AcceptedItemType.NUMBER

        cases[12][0] = TEST_SUN_THING_ID
        cases[12][1] = "morningNight#start"
        cases[12][2] = AcceptedItemType.DATE_TIME

        cases[13][0] = TEST_SUN_THING_ID
        cases[13][1] = "morningNight#end"
        cases[13][2] = AcceptedItemType.DATE_TIME

        cases[14][0] = TEST_SUN_THING_ID
        cases[14][1] = "morningNight#duration"
        cases[14][2] = AcceptedItemType.NUMBER

        cases[15][0] = TEST_SUN_THING_ID
        cases[15][1] = "astroDawn#start"
        cases[15][2] = AcceptedItemType.DATE_TIME

        cases[16][0] = TEST_SUN_THING_ID
        cases[16][1] = "astroDawn#end"
        cases[16][2] = AcceptedItemType.DATE_TIME

        cases[17][0] = TEST_SUN_THING_ID
        cases[17][1] = "astroDawn#duration"
        cases[17][2] = AcceptedItemType.NUMBER

        cases[18][0] = TEST_SUN_THING_ID
        cases[18][1] = "nauticDawn#start"
        cases[18][2] = AcceptedItemType.DATE_TIME

        cases[19][0] = TEST_SUN_THING_ID
        cases[19][1] = "nauticDawn#end"
        cases[19][2] = AcceptedItemType.DATE_TIME

        cases[20][0] = TEST_SUN_THING_ID
        cases[20][1] = "nauticDawn#duration"
        cases[20][2] = AcceptedItemType.NUMBER

        cases[21][0] = TEST_SUN_THING_ID
        cases[21][1] = "civilDawn#start"
        cases[21][2] = AcceptedItemType.DATE_TIME

        cases[22][0] = TEST_SUN_THING_ID
        cases[22][1] = "civilDawn#end"
        cases[22][2] = AcceptedItemType.DATE_TIME

        cases[23][0] = TEST_SUN_THING_ID
        cases[23][1] = "civilDawn#duration"
        cases[23][2] = AcceptedItemType.NUMBER

        cases[24][0] = TEST_SUN_THING_ID
        cases[24][1] = "astroDusk#start"
        cases[24][2] = AcceptedItemType.DATE_TIME

        cases[25][0] = TEST_SUN_THING_ID
        cases[25][1] = "astroDusk#end"
        cases[25][2] = AcceptedItemType.DATE_TIME

        cases[26][0] = TEST_SUN_THING_ID
        cases[26][1] = "astroDusk#duration"
        cases[26][2] = AcceptedItemType.NUMBER

        cases[27][0] = TEST_SUN_THING_ID
        cases[27][1] = "nauticDusk#start"
        cases[27][2] = AcceptedItemType.DATE_TIME

        cases[28][0] = TEST_SUN_THING_ID
        cases[28][1] = "nauticDusk#end"
        cases[28][2] = AcceptedItemType.DATE_TIME

        cases[29][0] = TEST_SUN_THING_ID
        cases[29][1] = "nauticDusk#duration"
        cases[29][2] = AcceptedItemType.NUMBER

        cases[30][0] = TEST_SUN_THING_ID
        cases[30][1] = "civilDusk#start"
        cases[30][2] = AcceptedItemType.DATE_TIME

        cases[31][0] = TEST_SUN_THING_ID
        cases[31][1] = "civilDusk#end"
        cases[31][2] = AcceptedItemType.DATE_TIME

        cases[32][0] = TEST_SUN_THING_ID
        cases[32][1] = "civilDusk#duration"
        cases[32][2] = AcceptedItemType.NUMBER

        cases[33][0] = TEST_SUN_THING_ID
        cases[33][1] = "eveningNight#start"
        cases[33][2] = AcceptedItemType.DATE_TIME

        cases[34][0] = TEST_SUN_THING_ID
        cases[34][1] = "eveningNight#end"
        cases[34][2] = AcceptedItemType.DATE_TIME

        cases[35][0] = TEST_SUN_THING_ID
        cases[35][1] = "eveningNight#duration"
        cases[35][2] = AcceptedItemType.NUMBER

        cases[36][0] = TEST_SUN_THING_ID
        cases[36][1] = "daylight#start"
        cases[36][2] = AcceptedItemType.DATE_TIME

        cases[37][0] = TEST_SUN_THING_ID
        cases[37][1] = "daylight#end"
        cases[37][2] = AcceptedItemType.DATE_TIME

        cases[38][0] = TEST_SUN_THING_ID
        cases[38][1] = "daylight#duration"
        cases[38][2] = AcceptedItemType.NUMBER

        cases[39][0] = TEST_SUN_THING_ID
        cases[39][1] = "position#azimuth"
        cases[39][2] = AcceptedItemType.NUMBER

        cases[40][0] = TEST_SUN_THING_ID
        cases[40][1] = "position#elevation"
        cases[40][2] = AcceptedItemType.NUMBER

        cases[41][0] = TEST_SUN_THING_ID
        cases[41][1] = "zodiac#start"
        cases[41][2] = AcceptedItemType.DATE_TIME

        cases[42][0] = TEST_SUN_THING_ID
        cases[42][1] = "zodiac#end"
        cases[42][2] = AcceptedItemType.DATE_TIME

        cases[43][0] = TEST_SUN_THING_ID
        cases[43][1] = "zodiac#sign"
        cases[43][2] = AcceptedItemType.STRING

        cases[44][0] = TEST_SUN_THING_ID
        cases[44][1] = "season#spring"
        cases[44][2] = AcceptedItemType.DATE_TIME

        cases[45][0] = TEST_SUN_THING_ID
        cases[45][1] = "season#summer"
        cases[45][2] = AcceptedItemType.DATE_TIME

        cases[46][0] = TEST_SUN_THING_ID
        cases[46][1] = "season#autumn"
        cases[46][2] = AcceptedItemType.DATE_TIME

        cases[47][0] = TEST_SUN_THING_ID
        cases[47][1] = "season#winter"
        cases[47][2] = AcceptedItemType.DATE_TIME

        cases[48][0] = TEST_SUN_THING_ID
        cases[48][1] = "season#name"
        cases[48][2] = AcceptedItemType.STRING

        cases[49][0] = TEST_SUN_THING_ID
        cases[49][1] = "eclipse#total"
        cases[49][2] = AcceptedItemType.DATE_TIME

        cases[50][0] = TEST_SUN_THING_ID
        cases[50][1] = "eclipse#partial"
        cases[50][2] = AcceptedItemType.DATE_TIME

        cases[51][0] = TEST_SUN_THING_ID
        cases[51][1] = "eclipse#ring"
        cases[51][2] = AcceptedItemType.DATE_TIME

        cases[52][0] = TEST_MOON_THING_ID
        cases[52][1] = "rise#start"
        cases[52][2] = AcceptedItemType.DATE_TIME

        cases[53][0] = TEST_MOON_THING_ID
        cases[53][1] = "rise#end"
        cases[53][2] = AcceptedItemType.DATE_TIME

        cases[54][0] = TEST_MOON_THING_ID
        cases[54][1] = "rise#duration"
        cases[54][2] = AcceptedItemType.NUMBER

        cases[55][0] = TEST_MOON_THING_ID
        cases[55][1] = "phase#firstQuarter"
        cases[55][2] = AcceptedItemType.DATE_TIME

        cases[56][0] = TEST_MOON_THING_ID
        cases[56][1] = "phase#thirdQuarter"
        cases[56][2] = AcceptedItemType.DATE_TIME

        cases[57][0] = TEST_MOON_THING_ID
        cases[57][1] = "phase#full"
        cases[57][2] = AcceptedItemType.DATE_TIME

        cases[58][0] = TEST_MOON_THING_ID
        cases[58][1] = "phase#new"
        cases[58][2] = AcceptedItemType.DATE_TIME

        cases[59][0] = TEST_MOON_THING_ID
        cases[59][1] = "phase#age"
        cases[59][2] = AcceptedItemType.NUMBER

        cases[60][0] = TEST_MOON_THING_ID
        cases[60][1] = "phase#illumination"
        cases[60][2] = AcceptedItemType.NUMBER

        cases[61][0] = TEST_MOON_THING_ID
        cases[61][1] = "phase#name"
        cases[61][2] = AcceptedItemType.STRING

        cases[62][0] = TEST_MOON_THING_ID
        cases[62][1] = "eclipse#total"
        cases[62][2] = AcceptedItemType.DATE_TIME

        cases[63][0] = TEST_MOON_THING_ID
        cases[63][1] = "eclipse#partial"
        cases[63][2] = AcceptedItemType.DATE_TIME

        cases[64][0] = TEST_MOON_THING_ID
        cases[64][1] = "distance#date"
        cases[64][2] = AcceptedItemType.DATE_TIME

        cases[65][0] = TEST_MOON_THING_ID
        cases[65][1] = "distance#kilometer"
        cases[65][2] = AcceptedItemType.NUMBER

        cases[66][0] = TEST_MOON_THING_ID
        cases[66][1] = "distance#miles"
        cases[66][2] = AcceptedItemType.NUMBER

        cases[67][0] = TEST_MOON_THING_ID
        cases[67][1] = "perigee#date"
        cases[67][2] = AcceptedItemType.DATE_TIME

        cases[68][0] = TEST_MOON_THING_ID
        cases[68][1] = "perigee#kilometer"
        cases[68][2] = AcceptedItemType.NUMBER

        cases[69][0] = TEST_MOON_THING_ID
        cases[69][1] = "perigee#miles"
        cases[69][2] = AcceptedItemType.NUMBER

        cases[70][0] = TEST_MOON_THING_ID
        cases[70][1] = "apogee#date"
        cases[70][2] = AcceptedItemType.DATE_TIME

        cases[71][0] = TEST_MOON_THING_ID
        cases[71][1] = "apogee#kilometer"
        cases[71][2] = AcceptedItemType.NUMBER

        cases[72][0] = TEST_MOON_THING_ID
        cases[72][1] = "apogee#miles"
        cases[72][2] = AcceptedItemType.NUMBER

        cases[73][0] = TEST_MOON_THING_ID
        cases[73][1] = "zodiac#sign"
        cases[73][2] = AcceptedItemType.STRING

        cases[74][0] = TEST_MOON_THING_ID
        cases[74][1] = "position#azimuth"
        cases[74][2] = AcceptedItemType.NUMBER

        cases[75][0] = TEST_MOON_THING_ID
        cases[75][1] = "position#elevation"
        cases[75][2] = AcceptedItemType.NUMBER
    }

    public List getCases(){
        return Arrays.asList(cases)
    }
}
