/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.rainsoft;

import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link RainSoftBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Ben Rosenblum - Initial contribution
 */
public class RainSoftBindingConstants {

    public static final String BINDING_ID = "rainsoft";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public final static ThingTypeUID THING_TYPE_WCS = new ThingTypeUID(BINDING_ID, "wcs");

    // List of all Channel ids
    public final static String CHANNEL_STATUS_SYSTEMSTATUS = "status#systemstatus";
    public final static String CHANNEL_STATUS_STATUSCODE = "status#statuscode";
    public final static String CHANNEL_STATUS_STATUSASOF = "status#statusasof";
    public final static String CHANNEL_STATUS_REGENTIME = "status#regentime";
    public final static String CHANNEL_STATUS_LASTREGEN = "status#lastregen";
    public final static String CHANNEL_STATUS_AIRPURGEHOUR = "status#airpurgehour";
    public final static String CHANNEL_STATUS_AIRPURGEMINUTE = "status#airpurgeminute";
    public final static String CHANNEL_STATUS_FLTREGENTIME = "status#fltregentime";
    public final static String CHANNEL_STATUS_MAXSALT = "status#maxsalt";
    public final static String CHANNEL_STATUS_SALTLBS = "status#saltlbs";
    public final static String CHANNEL_STATUS_CAPACITYREMAINING = "status#capacityremaining";
    public final static String CHANNEL_STATUS_VACATIONMODE = "status#vacationmode";
    public final static String CHANNEL_STATUS_HARDNESS = "status#hardness";
    public final static String CHANNEL_STATUS_PRESSURE = "status#pressure";
    public final static String CHANNEL_STATUS_IRONLEVEL = "status#ironlevel";
    public final static String CHANNEL_STATUS_DRAINFLOW = "status#drainflow";
    public final static String CHANNEL_STATUS_AVGMONTHSALT = "status#avgmonthsalt";
    public final static String CHANNEL_STATUS_DAILYWATERUSE = "status#dailywateruse";
    public final static String CHANNEL_STATUS_REGENS28DAY = "status#regens28day";
    public final static String CHANNEL_STATUS_WATER28DAY = "status#water28day";
    public final static String CHANNEL_STATUS_ENDOFDAY = "status#endofday";
    public final static String CHANNEL_STATUS_SALT28DAY = "status#salt28day";
    public final static String CHANNEL_STATUS_FLOWSINCEREGEN = "status#flowsinceregen";
    public final static String CHANNEL_STATUS_LIFETIMEFLOW = "status#lifetimeflow";

    public final static String CHANNEL_HISTORY_DAY0DATE = "history#day0date";
    public final static String CHANNEL_HISTORY_DAY0WATER = "history#day0water";
    public final static String CHANNEL_HISTORY_DAY0SALT = "history#day0salt";
    public final static String CHANNEL_HISTORY_DAY1DATE = "history#day01date";
    public final static String CHANNEL_HISTORY_DAY1WATER = "history#day1water";
    public final static String CHANNEL_HISTORY_DAY1SALT = "history#day1salt";
    public final static String CHANNEL_HISTORY_DAY2DATE = "history#day2date";
    public final static String CHANNEL_HISTORY_DAY2WATER = "history#day2water";
    public final static String CHANNEL_HISTORY_DAY2SALT = "history#day2salt";
    public final static String CHANNEL_HISTORY_DAY3DATE = "history#day3date";
    public final static String CHANNEL_HISTORY_DAY3WATER = "history#day3water";
    public final static String CHANNEL_HISTORY_DAY3SALT = "history#day3salt";
    public final static String CHANNEL_HISTORY_DAY4DATE = "history#day4date";
    public final static String CHANNEL_HISTORY_DAY4WATER = "history#day4water";
    public final static String CHANNEL_HISTORY_DAY4SALT = "history#day4salt";
    public final static String CHANNEL_HISTORY_DAY5DATE = "history#day5date";
    public final static String CHANNEL_HISTORY_DAY5WATER = "history#day5water";
    public final static String CHANNEL_HISTORY_DAY5SALT = "history#day5salt";
    public final static String CHANNEL_HISTORY_DAY6DATE = "history#day6date";
    public final static String CHANNEL_HISTORY_DAY6WATER = "history#day6water";
    public final static String CHANNEL_HISTORY_DAY6SALT = "history#day6salt";
    public final static String CHANNEL_HISTORY_DAY7DATE = "history#day7date";
    public final static String CHANNEL_HISTORY_DAY7WATER = "history#day7water";
    public final static String CHANNEL_HISTORY_DAY7SALT = "history#day7salt";
    public final static String CHANNEL_HISTORY_DAY8DATE = "history#day8date";
    public final static String CHANNEL_HISTORY_DAY8WATER = "history#day8water";
    public final static String CHANNEL_HISTORY_DAY8SALT = "history#day8salt";
    public final static String CHANNEL_HISTORY_DAY9DATE = "history#day9date";
    public final static String CHANNEL_HISTORY_DAY9WATER = "history#day9water";
    public final static String CHANNEL_HISTORY_DAY9SALT = "history#day9salt";
    public final static String CHANNEL_HISTORY_DAY10DATE = "history#day10date";
    public final static String CHANNEL_HISTORY_DAY10WATER = "history#day10water";
    public final static String CHANNEL_HISTORY_DAY10SALT = "history#day10salt";
    public final static String CHANNEL_HISTORY_DAY11DATE = "history#day101date";
    public final static String CHANNEL_HISTORY_DAY11WATER = "history#day11water";
    public final static String CHANNEL_HISTORY_DAY11SALT = "history#day11salt";
    public final static String CHANNEL_HISTORY_DAY12DATE = "history#day12date";
    public final static String CHANNEL_HISTORY_DAY12WATER = "history#day12water";
    public final static String CHANNEL_HISTORY_DAY12SALT = "history#day12salt";
    public final static String CHANNEL_HISTORY_DAY13DATE = "history#day13date";
    public final static String CHANNEL_HISTORY_DAY13WATER = "history#day13water";
    public final static String CHANNEL_HISTORY_DAY13SALT = "history#day13salt";
    public final static String CHANNEL_HISTORY_DAY14DATE = "history#day14date";
    public final static String CHANNEL_HISTORY_DAY14WATER = "history#day14water";
    public final static String CHANNEL_HISTORY_DAY14SALT = "history#day14salt";
    public final static String CHANNEL_HISTORY_DAY15DATE = "history#day15date";
    public final static String CHANNEL_HISTORY_DAY15WATER = "history#day15water";
    public final static String CHANNEL_HISTORY_DAY15SALT = "history#day15salt";
    public final static String CHANNEL_HISTORY_DAY16DATE = "history#day16date";
    public final static String CHANNEL_HISTORY_DAY16WATER = "history#day16water";
    public final static String CHANNEL_HISTORY_DAY16SALT = "history#day16salt";
    public final static String CHANNEL_HISTORY_DAY17DATE = "history#day17date";
    public final static String CHANNEL_HISTORY_DAY17WATER = "history#day17water";
    public final static String CHANNEL_HISTORY_DAY17SALT = "history#day17salt";
    public final static String CHANNEL_HISTORY_DAY18DATE = "history#day18date";
    public final static String CHANNEL_HISTORY_DAY18WATER = "history#day18water";
    public final static String CHANNEL_HISTORY_DAY18SALT = "history#day18salt";
    public final static String CHANNEL_HISTORY_DAY19DATE = "history#day19date";
    public final static String CHANNEL_HISTORY_DAY19WATER = "history#day19water";
    public final static String CHANNEL_HISTORY_DAY19SALT = "history#day19salt";
    public final static String CHANNEL_HISTORY_DAY20DATE = "history#day20date";
    public final static String CHANNEL_HISTORY_DAY20WATER = "history#day20water";
    public final static String CHANNEL_HISTORY_DAY20SALT = "history#day20salt";
    public final static String CHANNEL_HISTORY_DAY21DATE = "history#day201date";
    public final static String CHANNEL_HISTORY_DAY21WATER = "history#day21water";
    public final static String CHANNEL_HISTORY_DAY21SALT = "history#day21salt";
    public final static String CHANNEL_HISTORY_DAY22DATE = "history#day22date";
    public final static String CHANNEL_HISTORY_DAY22WATER = "history#day22water";
    public final static String CHANNEL_HISTORY_DAY22SALT = "history#day22salt";
    public final static String CHANNEL_HISTORY_DAY23DATE = "history#day23date";
    public final static String CHANNEL_HISTORY_DAY23WATER = "history#day23water";
    public final static String CHANNEL_HISTORY_DAY23SALT = "history#day23salt";
    public final static String CHANNEL_HISTORY_DAY24DATE = "history#day24date";
    public final static String CHANNEL_HISTORY_DAY24WATER = "history#day24water";
    public final static String CHANNEL_HISTORY_DAY24SALT = "history#day24salt";
    public final static String CHANNEL_HISTORY_DAY25DATE = "history#day25date";
    public final static String CHANNEL_HISTORY_DAY25WATER = "history#day25water";
    public final static String CHANNEL_HISTORY_DAY25SALT = "history#day25salt";
    public final static String CHANNEL_HISTORY_DAY26DATE = "history#day26date";
    public final static String CHANNEL_HISTORY_DAY26WATER = "history#day26water";
    public final static String CHANNEL_HISTORY_DAY26SALT = "history#day26salt";
    public final static String CHANNEL_HISTORY_DAY27DATE = "history#day27date";
    public final static String CHANNEL_HISTORY_DAY27WATER = "history#day27water";
    public final static String CHANNEL_HISTORY_DAY27SALT = "history#day27salt";

    public final static String CHANNEL_HISTORY_MONTH0DATE = "history#month0date";
    public final static String CHANNEL_HISTORY_MONTH0WATER = "history#month0water";
    public final static String CHANNEL_HISTORY_MONTH0SALT = "history#month0salt";
    public final static String CHANNEL_HISTORY_MONTH1DATE = "history#month01date";
    public final static String CHANNEL_HISTORY_MONTH1WATER = "history#month1water";
    public final static String CHANNEL_HISTORY_MONTH1SALT = "history#month1salt";
    public final static String CHANNEL_HISTORY_MONTH2DATE = "history#month2date";
    public final static String CHANNEL_HISTORY_MONTH2WATER = "history#month2water";
    public final static String CHANNEL_HISTORY_MONTH2SALT = "history#month2salt";
    public final static String CHANNEL_HISTORY_MONTH3DATE = "history#month3date";
    public final static String CHANNEL_HISTORY_MONTH3WATER = "history#month3water";
    public final static String CHANNEL_HISTORY_MONTH3SALT = "history#month3salt";
    public final static String CHANNEL_HISTORY_MONTH4DATE = "history#month4date";
    public final static String CHANNEL_HISTORY_MONTH4WATER = "history#month4water";
    public final static String CHANNEL_HISTORY_MONTH4SALT = "history#month4salt";
    public final static String CHANNEL_HISTORY_MONTH5DATE = "history#month5date";
    public final static String CHANNEL_HISTORY_MONTH5WATER = "history#month5water";
    public final static String CHANNEL_HISTORY_MONTH5SALT = "history#month5salt";
    public final static String CHANNEL_HISTORY_MONTH6DATE = "history#month6date";
    public final static String CHANNEL_HISTORY_MONTH6WATER = "history#month6water";
    public final static String CHANNEL_HISTORY_MONTH6SALT = "history#month6salt";
    public final static String CHANNEL_HISTORY_MONTH7DATE = "history#month7date";
    public final static String CHANNEL_HISTORY_MONTH7WATER = "history#month7water";
    public final static String CHANNEL_HISTORY_MONTH7SALT = "history#month7salt";
    public final static String CHANNEL_HISTORY_MONTH8DATE = "history#month8date";
    public final static String CHANNEL_HISTORY_MONTH8WATER = "history#month8water";
    public final static String CHANNEL_HISTORY_MONTH8SALT = "history#month8salt";
    public final static String CHANNEL_HISTORY_MONTH9DATE = "history#month9date";
    public final static String CHANNEL_HISTORY_MONTH9WATER = "history#month9water";
    public final static String CHANNEL_HISTORY_MONTH9SALT = "history#month9salt";
    public final static String CHANNEL_HISTORY_MONTH10DATE = "history#month10date";
    public final static String CHANNEL_HISTORY_MONTH10WATER = "history#month10water";
    public final static String CHANNEL_HISTORY_MONTH10SALT = "history#month10salt";
    public final static String CHANNEL_HISTORY_MONTH11DATE = "history#month101date";
    public final static String CHANNEL_HISTORY_MONTH11WATER = "history#month11water";
    public final static String CHANNEL_HISTORY_MONTH11SALT = "history#month11salt";

    public final static String CHANNEL_CONTROL_REMOTEREGEN = "control#remoteregen";
}
