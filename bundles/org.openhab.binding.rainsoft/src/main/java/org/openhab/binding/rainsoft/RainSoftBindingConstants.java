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

    public final static String CHANNEL_USAGE_DAY1DATE = "usage#day01date";
    public final static String CHANNEL_USAGE_DAY1WATER = "usage#day1water";
    public final static String CHANNEL_USAGE_DAY1SALT = "usage#day1salt";
    public final static String CHANNEL_USAGE_DAY2DATE = "usage#day2date";
    public final static String CHANNEL_USAGE_DAY2WATER = "usage#day2water";
    public final static String CHANNEL_USAGE_DAY2SALT = "usage#day2salt";
    public final static String CHANNEL_USAGE_DAY3DATE = "usage#day3date";
    public final static String CHANNEL_USAGE_DAY3WATER = "usage#day3water";
    public final static String CHANNEL_USAGE_DAY3SALT = "usage#day3salt";
    public final static String CHANNEL_USAGE_DAY4DATE = "usage#day4date";
    public final static String CHANNEL_USAGE_DAY4WATER = "usage#day4water";
    public final static String CHANNEL_USAGE_DAY4SALT = "usage#day4salt";
    public final static String CHANNEL_USAGE_DAY5DATE = "usage#day5date";
    public final static String CHANNEL_USAGE_DAY5WATER = "usage#day5water";
    public final static String CHANNEL_USAGE_DAY5SALT = "usage#day5salt";
    public final static String CHANNEL_USAGE_DAY6DATE = "usage#day6date";
    public final static String CHANNEL_USAGE_DAY6WATER = "usage#day6water";
    public final static String CHANNEL_USAGE_DAY6SALT = "usage#day6salt";
    public final static String CHANNEL_USAGE_DAY7DATE = "usage#day7date";
    public final static String CHANNEL_USAGE_DAY7WATER = "usage#day7water";
    public final static String CHANNEL_USAGE_DAY7SALT = "usage#day7salt";
    public final static String CHANNEL_USAGE_DAY8DATE = "usage#day8date";
    public final static String CHANNEL_USAGE_DAY8WATER = "usage#day8water";
    public final static String CHANNEL_USAGE_DAY8SALT = "usage#day8salt";
    public final static String CHANNEL_USAGE_DAY9DATE = "usage#day9date";
    public final static String CHANNEL_USAGE_DAY9WATER = "usage#day9water";
    public final static String CHANNEL_USAGE_DAY9SALT = "usage#day9salt";
    public final static String CHANNEL_USAGE_DAY10DATE = "usage#day10date";
    public final static String CHANNEL_USAGE_DAY10WATER = "usage#day10water";
    public final static String CHANNEL_USAGE_DAY10SALT = "usage#day10salt";
    public final static String CHANNEL_USAGE_DAY11DATE = "usage#day101date";
    public final static String CHANNEL_USAGE_DAY11WATER = "usage#day11water";
    public final static String CHANNEL_USAGE_DAY11SALT = "usage#day11salt";
    public final static String CHANNEL_USAGE_DAY12DATE = "usage#day12date";
    public final static String CHANNEL_USAGE_DAY12WATER = "usage#day12water";
    public final static String CHANNEL_USAGE_DAY12SALT = "usage#day12salt";
    public final static String CHANNEL_USAGE_DAY13DATE = "usage#day13date";
    public final static String CHANNEL_USAGE_DAY13WATER = "usage#day13water";
    public final static String CHANNEL_USAGE_DAY13SALT = "usage#day13salt";
    public final static String CHANNEL_USAGE_DAY14DATE = "usage#day14date";
    public final static String CHANNEL_USAGE_DAY14WATER = "usage#day14water";
    public final static String CHANNEL_USAGE_DAY14SALT = "usage#day14salt";
    public final static String CHANNEL_USAGE_DAY15DATE = "usage#day15date";
    public final static String CHANNEL_USAGE_DAY15WATER = "usage#day15water";
    public final static String CHANNEL_USAGE_DAY15SALT = "usage#day15salt";
    public final static String CHANNEL_USAGE_DAY16DATE = "usage#day16date";
    public final static String CHANNEL_USAGE_DAY16WATER = "usage#day16water";
    public final static String CHANNEL_USAGE_DAY16SALT = "usage#day16salt";
    public final static String CHANNEL_USAGE_DAY17DATE = "usage#day17date";
    public final static String CHANNEL_USAGE_DAY17WATER = "usage#day17water";
    public final static String CHANNEL_USAGE_DAY17SALT = "usage#day17salt";
    public final static String CHANNEL_USAGE_DAY18DATE = "usage#day18date";
    public final static String CHANNEL_USAGE_DAY18WATER = "usage#day18water";
    public final static String CHANNEL_USAGE_DAY18SALT = "usage#day18salt";
    public final static String CHANNEL_USAGE_DAY19DATE = "usage#day19date";
    public final static String CHANNEL_USAGE_DAY19WATER = "usage#day19water";
    public final static String CHANNEL_USAGE_DAY19SALT = "usage#day19salt";
    public final static String CHANNEL_USAGE_DAY20DATE = "usage#day20date";
    public final static String CHANNEL_USAGE_DAY20WATER = "usage#day20water";
    public final static String CHANNEL_USAGE_DAY20SALT = "usage#day20salt";
    public final static String CHANNEL_USAGE_DAY21DATE = "usage#day201date";
    public final static String CHANNEL_USAGE_DAY21WATER = "usage#day21water";
    public final static String CHANNEL_USAGE_DAY21SALT = "usage#day21salt";
    public final static String CHANNEL_USAGE_DAY22DATE = "usage#day22date";
    public final static String CHANNEL_USAGE_DAY22WATER = "usage#day22water";
    public final static String CHANNEL_USAGE_DAY22SALT = "usage#day22salt";
    public final static String CHANNEL_USAGE_DAY23DATE = "usage#day23date";
    public final static String CHANNEL_USAGE_DAY23WATER = "usage#day23water";
    public final static String CHANNEL_USAGE_DAY23SALT = "usage#day23salt";
    public final static String CHANNEL_USAGE_DAY24DATE = "usage#day24date";
    public final static String CHANNEL_USAGE_DAY24WATER = "usage#day24water";
    public final static String CHANNEL_USAGE_DAY24SALT = "usage#day24salt";
    public final static String CHANNEL_USAGE_DAY25DATE = "usage#day25date";
    public final static String CHANNEL_USAGE_DAY25WATER = "usage#day25water";
    public final static String CHANNEL_USAGE_DAY25SALT = "usage#day25salt";
    public final static String CHANNEL_USAGE_DAY26DATE = "usage#day26date";
    public final static String CHANNEL_USAGE_DAY26WATER = "usage#day26water";
    public final static String CHANNEL_USAGE_DAY26SALT = "usage#day26salt";
    public final static String CHANNEL_USAGE_DAY27DATE = "usage#day27date";
    public final static String CHANNEL_USAGE_DAY27WATER = "usage#day27water";
    public final static String CHANNEL_USAGE_DAY27SALT = "usage#day27salt";
    public final static String CHANNEL_USAGE_DAY28DATE = "usage#day28date";
    public final static String CHANNEL_USAGE_DAY28WATER = "usage#day28water";
    public final static String CHANNEL_USAGE_DAY28SALT = "usage#day28salt";

    public final static String CHANNEL_USAGE_MONTH0DATE = "usage#month0date";
    public final static String CHANNEL_USAGE_MONTH0WATER = "usage#month0water";
    public final static String CHANNEL_USAGE_MONTH0SALT = "usage#month0salt";
    public final static String CHANNEL_USAGE_MONTH1DATE = "usage#month01date";
    public final static String CHANNEL_USAGE_MONTH1WATER = "usage#month1water";
    public final static String CHANNEL_USAGE_MONTH1SALT = "usage#month1salt";
    public final static String CHANNEL_USAGE_MONTH2DATE = "usage#month2date";
    public final static String CHANNEL_USAGE_MONTH2WATER = "usage#month2water";
    public final static String CHANNEL_USAGE_MONTH2SALT = "usage#month2salt";
    public final static String CHANNEL_USAGE_MONTH3DATE = "usage#month3date";
    public final static String CHANNEL_USAGE_MONTH3WATER = "usage#month3water";
    public final static String CHANNEL_USAGE_MONTH3SALT = "usage#month3salt";
    public final static String CHANNEL_USAGE_MONTH4DATE = "usage#month4date";
    public final static String CHANNEL_USAGE_MONTH4WATER = "usage#month4water";
    public final static String CHANNEL_USAGE_MONTH4SALT = "usage#month4salt";
    public final static String CHANNEL_USAGE_MONTH5DATE = "usage#month5date";
    public final static String CHANNEL_USAGE_MONTH5WATER = "usage#month5water";
    public final static String CHANNEL_USAGE_MONTH5SALT = "usage#month5salt";
    public final static String CHANNEL_USAGE_MONTH6DATE = "usage#month6date";
    public final static String CHANNEL_USAGE_MONTH6WATER = "usage#month6water";
    public final static String CHANNEL_USAGE_MONTH6SALT = "usage#month6salt";
    public final static String CHANNEL_USAGE_MONTH7DATE = "usage#month7date";
    public final static String CHANNEL_USAGE_MONTH7WATER = "usage#month7water";
    public final static String CHANNEL_USAGE_MONTH7SALT = "usage#month7salt";
    public final static String CHANNEL_USAGE_MONTH8DATE = "usage#month8date";
    public final static String CHANNEL_USAGE_MONTH8WATER = "usage#month8water";
    public final static String CHANNEL_USAGE_MONTH8SALT = "usage#month8salt";
    public final static String CHANNEL_USAGE_MONTH9DATE = "usage#month9date";
    public final static String CHANNEL_USAGE_MONTH9WATER = "usage#month9water";
    public final static String CHANNEL_USAGE_MONTH9SALT = "usage#month9salt";
    public final static String CHANNEL_USAGE_MONTH10DATE = "usage#month10date";
    public final static String CHANNEL_USAGE_MONTH10WATER = "usage#month10water";
    public final static String CHANNEL_USAGE_MONTH10SALT = "usage#month10salt";
    public final static String CHANNEL_USAGE_MONTH11DATE = "usage#month101date";
    public final static String CHANNEL_USAGE_MONTH11WATER = "usage#month11water";
    public final static String CHANNEL_USAGE_MONTH11SALT = "usage#month11salt";

    public final static String CHANNEL_CONTROL_REMOTEREGEN = "control#remoteregen";
}
