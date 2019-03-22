/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.synopanalyser.internal.synop;

/**
 * The {@link Constants} class defines common constants, which are
 * used across the whole package.
 *
 * @author Jonarzz - Initial contribution
 */
public class Constants {

    public static final int INITIAL_VALUE = -1000;
    public static final String UNKNOWN_VALUE = "/";

    public static final String CHAPTER_3_CODE = "333";
    public static final String CHAPTER_4_CODE = "444";
    public static final String CHAPTER_5_CODE = "555";

    public static final char PLUS_SIGN_TEMPERATURE = '0';
    public static final char MINUS_SIGN_TEMPERATURE = '1';

    public static final String LAND_STATION_CODE = "AAXX";
    public static final String SHIP_STATION_CODE = "BBXX";
    public static final String MOBILE_LAND_STATION_CODE = "OOXX";

    ////// BEGIN YEAR-HOUR-WIND GROUP [YYHHW]
    //
    // WS - WIND SPEED
    //
    public static final int WS_WILDTYPE_IN_MPS = 0;
    public static final int WS_ANEMOMETER_IN_MPS = 1;
    public static final int WS_WILDTYPE_IN_KNOT = 3;
    public static final int WS_ANEMOMETER_IN_KNOT = 4;
    //
    ////// END YEAR-HOUR-WIND GROUP [YYHHW]

    ////// BEGIN DOWNFALL-STATION-CLOUD-VISABILITY [xyzXX]
    //
    // DFG - DOWNFALL GROUP
    // VALUES FROM '0' TO '4'
    //
    public static final char DFG_IN_CHAPTER_1_AND_3 = '0';
    public static final char DFG_IN_CHAPTER_1 = '1';
    public static final char DFG_IN_CHAPTER_3 = '2';
    public static final char DFG_NO_DF = '3';
    public static final char DFG_NO_MEASUREMENT = '4';
    //
    // ST - STATION TYPE
    // A - AUTOMATIC
    // N - NON-AUTOMATIC
    // VALUES FROM '1' TO '6'
    //
    public static final char ST_N_GROUP_7_ON = '1';
    public static final char ST_N_GROUP_7_OFF_NO_PHENOMENON = '2';
    public static final char ST_N_GROUP_7_OFF_NO_DATA = '3';
    public static final char ST_A_GROUP_7_ON = '4';
    public static final char ST_A_GROUP_7_OFF_NO_PHENOMENON = '5';
    public static final char ST_A_GROUP_7_OFF_NO_DATA = '6';
    //
    // CH - CLOUD HEIGH [IN METERS]
    // VALUES FROM '0' TO '9'
    // '/' ACCEPTABLE
    //
    public static final char CH_0_50 = '0';
    public static final char CH_50_100 = '1';
    public static final char CH_100_200 = '2';
    public static final char CH_200_300 = '3';
    public static final char CH_300_600 = '4';
    public static final char CH_600_1000 = '5';
    public static final char CH_1000_1500 = '6';
    public static final char CH_1500_2000 = '7';
    public static final char CH_2000_2500 = '8';
    public static final char CH_OVER_2500 = '9';
    //
    // HV - HORIZONTAL VISIBILITY [IN KILOMETERS]
    // VALUES FROM "00" TO "50" AND FROM "56" TO "99"
    // 00 MEANS HV = BELOW 0,1
    // DECIMAL SCOPE MEANS HV = XX / 10
    // UNIT SCOPE MEANS HV = XX - 50
    // 89 MEANS HV = OVER 70
    // 90-99 ROUGHLY NUMBERING // TODO
    /*
     * 90 - < 0,05 km
     * 91 � 0,05 � 0,2 km
     * 92 � 0,2 � 0,5 km
     * 93 � 0,5 � 1,0 km
     * 94 � 1,0 � 2,0 km
     * 95 � 2,0 � 4,0 km
     * 96 � 4,0 � 10,0 km
     * 97 � 10,0 � 20,0 km
     * 98 � 20,0 � 50,0 km
     * 99 - > 50 km
     */
    // HP - high precision
    public static final int HV_LESS_THAN_1_LIMIT = 10;
    public static final int HV_LESS_THAN_10_LIMIT = 60;
    public static final int HV_LESS_THAN_50_LIMIT = 84;
    public static final int HV_HP_LIMIT = 90;
    public static final int HV_LESS_THAN_1_HP_LIMIT = 93;
    public static final int HV_LESS_THAN_10_HP_LIMIT = 96;
    public static final int HV_LESS_THAN_50_HP_LIMIT = 98;

    public static final String HV_LESS_THAN_1_STRING = "<1";
    public static final String HV_LESS_THAN_10_STRING = "1-10";
    public static final String HV_LESS_THAN_50_STRING = "10-50";
    public static final String HV_MORE_THAN_50_STRING = ">50";
    //
    ////// END DOWNFALL-STATION-CLOUD-VISABILITY [xyzXX]

    // BEGIN CLOUDINESS-WIND-DIRECTION-SPEED [xyyzz]
    //
    // CLOUDINESS
    // CLOUDINESS = x/8
    // VALUES FROM '0' TO '9'
    // '/' ACCEPTABLE
    //
    public static final String CLEAR_SKY = "NO CLOUDS";
    public static final String CLOUDY = "CLOUDY";
    public static final String SKY_NOT_VISIBLE = "SKY NOT VISIBLE";
    //
    // WD - WIND DIRECTION
    // WD = FROM { 5 + (yy - 1) * 9 } TO { 5 + yy * 9 } OR:
    // VALUES FROM "00" TO "36" AND "99"
    // "//" ACCEPTABLE
    //
    public static final String WD_NO_WIND = "00";
    public static final String WD_VARIABLE = "99";
    //
    // WIND SPEED
    // WS = zz [IN WD UNIT]
    //
    // END CLOUDINESS-WIND-DIRECTION-SPEED [xyyzz]

    ////// BEGIN 00-WIND GROUP [00xxx]
    //
    // WHEN WIND SPEED IS BIGGER THAN 99 THEN:
    // WS = xxx IN WD UNIT
    //
    ////// END 00-WIND GROUP [00xxx]

    ////// BEGIN 1-SIGN-TEMPERATURE GROUP [1sTTT]
    //
    // TEMPERATURE = (SIGN) TTT / 10
    //
    ////// END 1-SIGN-TEMPERATURE GROUP [1sTTT]

    ////// BEGIN 2-SIGN-DEW GRUPE [2sDDD]
    //
    // DEW POINT = DDD / 10
    //
    ////// END 2-SIGN-DEW GRUPE [2sDDD]
    //
    // OR
    //
    ////// BEGIN 2-9-HUMIDITY GRUPE [29HHH]
    //
    // HUMIDITY = HHH [IN PER CENT]
    // VALUES FROM "000" TO "100"
    //
    ////// END 2-9-HUMIDITY GRUPE [29HHH]

    ////// BEGIN 3-PRESSURE GRUPE [3PPPP]
    //
    // PRESSURE = PPPP / 10 [IN HECTOPASCALS]
    // BUT 0125 MEANS PRESSURE = 1012,5 hPa
    //
    ////// END 3-PRESSURE GRUPE [3PPPP]

    ////// BEGIN 4-PRESSURE GRUPE [4PPPP]
    //
    // OLA� !!!
    // NIE ROZUMIEM, CO TO ZA CINIENIE
    //
    ////// END 4-PRESSURE GRUPE [4PPPP]

    ////// BEGIN 5-CHANGE-PRESSURE [5xPPP]
    //
    // PCT - PRESSURE CHANGE TYPE
    // VALUES FROM '0' TO '8'
    //
    public static final char PCT_INCREASE_LOWER_LIMIT = '0';
    public static final char PCT_INCREASE_UPPER_LIMIT = '3';
    public static final char PCT_NO_CHANGE = '4';
    public static final char PCT_DECREASE_LOWER_LIMIT = '5';
    public static final char PCT_DECREASE_UPPER_LIMIT = '8';
    //
    // CHANGE VALUE = PPP / 10 IN HECTOPASCALS
    //
    ////// END 5-CHANGE-PRESSURE [5xPPP]

    ////// BEGIN 6-DOWNFALL-TIME [6fffT]
    //
    // TD - TOTAL DOWNFALL
    // SOP - SCOPE OF PROPORTIONALITY
    // SOP MEANS TD = fff [IN MILIMETERS]
    // SOD - SCOPE OF DECIMALS
    // SOD MEANS TD = //f
    // VALUES FROM "000" TO "999"
    // fff = "989" MEANS 989 AND OVER
    //
    public static final String TD_SOP_LOWER_LIMIT = "000";
    public static final String TD_SOP_UPPER_LIMIT = "989";
    public static final String TD_TRACE = "990";
    public static final String TD_SOD_LOWER_LIMIT = "991";
    public static final String TD_SOD_UPPER_LIMIT = "999";
    //
    // DURATION TIME = // TODO
    /*
     * 1 � 6 h 4 � 24 h 7 � 3 h
     * 2 � 12 h 5 � 1 h 8 � 9 h
     * 3 � 18 h 6 � 2 h 9 � 15 h
     */
    //
    ////// END 6-DOWNFALL-TIME [6fffT]

    ////// BEGIN 7-TYPE GROUP [7xxyy]
    //
    // DU�O TEGO // TODO
    //
    ////// END 7-TYPE GROUP [7xxyy]

    ////// BEGIN 8-CLOUDINESS-LOW-MEDIUM-HIGH GROUP [8clmh]
    //
    // RODZAJE CHMUR // TODO
    //
    ///// END 8-CLOUDINESS-LOW-MEDIUM-HIGH GROUP [8clmh]

    ////// BEGIN 9-TIME [HHMM]
    //
    // TIME OF OBSERVATION = HH:MM
    //
    ////// END 9-TIME [HHMM]
}