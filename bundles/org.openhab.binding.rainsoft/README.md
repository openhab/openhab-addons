# <bindingName> RainSoft

This is an experimental binding to the RainSoft API. It currently supports a RainSoft account
and is able to discover RainSoft Water Conditioning Systems. They need to be registered in
the RainSoft account before they will be detected.

## Supported Things

The binding currently supports RainSoft EC5 Water Conditioning Systems.

## Discovery

Auto-discovery is applicable to this binding. After (manually) adding a RainSoft Account thing, 
registered water conditioning systems will be auto discovered.

## Binding Configuration

This binding requires an account thing to be created prior to any device things.

## Thing Configuration

There are two required fields to connect to the RainSoft Account.

| Name             | Type    | Description                           | Default | Required | Advanced |
|------------------|---------|---------------------------------------|---------|----------|----------|
| username         | text    | Account Username                      | N/A     | yes      | no       |
| password         | text    | Account Password                      | N/A     | yes      | no       |
| refreshInterval  | text    | Periodicity of API Calls              | N/A     | no       | no       |

There are no required fields for the wcs.

## Channels


## Full Example
NOTE 1: Replace <ring_device_id> with a valid ring device ID when manually configuring. The easiest way to currently get that is to define the account thing and pull the device ID from the last event channel.

NOTE 2: Text configuration for the Things ONLY works if you DO NOT have 2 factor authentication enabled. If you are using 2 factor authentication, Things MUST be set up through PaperUI

rainsoft.things:

```java
rainsoft:account:rainsoftAccount  "RainSoft Account"   [ username="my@email.com", password="secret", refreshInterval=900 ]
rainsoft:wcs:12345 "WCS" 
```

rainsoft.items:

```java
String RAINSOFT_SYSTEMSTATUS        "RainSoft System Status [%s]"       { channel="rainsoft:wcs:12345:status#systemstatus" }
Number RAINSOFT_STATUSCODE          "RainSoft System Status Code"       { channel="rainsoft:wcs:12345:status#statuscode" }
DateTime RAINSOFT_STATUSASOF        "RainSoft Last Update"              { channel="rainsoft:wcs:12345:status#statusasof" }
DateTime RAINSOFT_REGENTIME         "RainSoft Scheduled Regen Time"     { channel="rainsoft:wcs:12345:status#regentime" }
DateTime RAINSOFT_LASTREGEN         "RainSoft Last Regen Time"          { channel="rainsoft:wcs:12345:status#lastregen" }
Number RAINSOFT_AIRPURGEHOUR        "RainSoft Air Purge Hour"           { channel="rainsoft:wcs:12345:status#airpurgehour" }
Number RAINSOFT_AIRPURGEMINUTE      "RainSoft Air Purge Minute"         { channel="rainsoft:wcs:12345:status#airpurgeminute" }
DateTime RAINSOFT_FLTREGENTIME      "RainSoft Last Flt Regen Time"      { channel="rainsoft:wcs:12345:status#fltregentime" }
Number RAINSOFT_MAXSALT             "RainSoft Max Salt Level"           { channel="rainsoft:wcs:12345:status#maxsalt" }
Number RAINSOFT_SALTLBS             "RainSoft Remaining Salt Level"     { channel="rainsoft:wcs:12345:status#saltlbs" }
Number RAINSOFT_CAPACITYREMAINING   "RainSoft Brine Capacity Remaining" { channel="rainsoft:wcs:12345:status#capacityremaining" }
Switch RAINSOFT_VACATIONMODE        "RainSoft Vacation Mode"            { channel="rainsoft:wcs:12345:status#vacationmode" }
Number RAINSOFT_HARDNESS            "RainSoft Water Hardness"           { channel="rainsoft:wcs:12345:status#hardness" }
Number RAINSOFT_PRESSURE            "RainSoft Water Pressure"           { channel="rainsoft:wcs:12345:status#pressure" }
Number RAINSOFT_IRONLEVEL           "RainSoft Iron Level"               { channel="rainsoft:wcs:12345:status#ironlevel" }
Number RAINSOFT_DRAINFLOW           "RainSoft Drain Flow"               { channel="rainsoft:wcs:12345:status#drainflow" }
Number RAINSOFT_AVGMONTHSALT        "RainSoft Average Monthly Salt"     { channel="rainsoft:wcs:12345:status#avgmonthsalt" }
Number RAINSOFT_DAILYWATERUSE       "RainSoft Daily Water Usage"        { channel="rainsoft:wcs:12345:status#dailywateruse" }
Number RAINSOFT_REGENS28DAY         "RainSoft Regens Last 28 Days"      { channel="rainsoft:wcs:12345:status#regens28day" }
Number RAINSOFT_WATER28DAY          "RainSoft Water Usage Last 28 Days" { channel="rainsoft:wcs:12345:status#water28day" }
Number RAINSOFT_ENDOFDAY            "RainSoft End of Day"               { channel="rainsoft:wcs:12345:status#endofday" }
Number RAINSOFT_SALT28DAY           "RainSoft Salt Usage Last 28 Days"  { channel="rainsoft:wcs:12345:status#salt28day" }
Number RAINSOFT_FLOWSINCEREGEN      "RainSoft Water Usage Since Regen"  { channel="rainsoft:wcs:12345:status#flowsinceregen" }
Number RAINSOFT_LIFETIMEFLOW        "RainSoft Lifetime Water Usage"     { channel="rainsoft:wcs:12345:status#lifetimeflow" }

String RAINSOFT_USAGE_DAY1DATE        "RainSoft Usage Day 1 Date"     { channel="rainsoft:wcs:12345:usage#day1date" }
Number RAINSOFT_USAGE_DAY1WATER       "RainSoft Usage Day 1 Water"    { channel="rainsoft:wcs:12345:usage#day1water" }
Number RAINSOFT_USAGE_DAY1SALT        "RainSoft Usage Day 1 Salt"     { channel="rainsoft:wcs:12345:usage#day1salt" }
String RAINSOFT_USAGE_DAY2DATE        "RainSoft Usage Day 2 Date"     { channel="rainsoft:wcs:12345:usage#day2date" }
Number RAINSOFT_USAGE_DAY2WATER       "RainSoft Usage Day 2 Water"    { channel="rainsoft:wcs:12345:usage#day2water" }
Number RAINSOFT_USAGE_DAY2SALT        "RainSoft Usage Day 2 Salt"     { channel="rainsoft:wcs:12345:usage#day2salt" }
String RAINSOFT_USAGE_DAY3DATE        "RainSoft Usage Day 3 Date"     { channel="rainsoft:wcs:12345:usage#day3date" }
Number RAINSOFT_USAGE_DAY3WATER       "RainSoft Usage Day 3 Water"    { channel="rainsoft:wcs:12345:usage#day3water" }
Number RAINSOFT_USAGE_DAY3SALT        "RainSoft Usage Day 3 Salt"     { channel="rainsoft:wcs:12345:usage#day3salt" }
String RAINSOFT_USAGE_DAY4DATE        "RainSoft Usage Day 4 Date"     { channel="rainsoft:wcs:12345:usage#day4date" }
Number RAINSOFT_USAGE_DAY4WATER       "RainSoft Usage Day 4 Water"    { channel="rainsoft:wcs:12345:usage#day4water" }
Number RAINSOFT_USAGE_DAY4SALT        "RainSoft Usage Day 4 Salt"     { channel="rainsoft:wcs:12345:usage#day4salt" }
String RAINSOFT_USAGE_DAY5DATE        "RainSoft Usage Day 5 Date"     { channel="rainsoft:wcs:12345:usage#day5date" }
Number RAINSOFT_USAGE_DAY5WATER       "RainSoft Usage Day 5 Water"    { channel="rainsoft:wcs:12345:usage#day5water" }
Number RAINSOFT_USAGE_DAY5SALT        "RainSoft Usage Day 5 Salt"     { channel="rainsoft:wcs:12345:usage#day5salt" }
String RAINSOFT_USAGE_DAY6DATE        "RainSoft Usage Day 6 Date"     { channel="rainsoft:wcs:12345:usage#day6date" }
Number RAINSOFT_USAGE_DAY6WATER       "RainSoft Usage Day 6 Water"    { channel="rainsoft:wcs:12345:usage#day6water" }
Number RAINSOFT_USAGE_DAY6SALT        "RainSoft Usage Day 6 Salt"     { channel="rainsoft:wcs:12345:usage#day6salt" }
String RAINSOFT_USAGE_DAY7DATE        "RainSoft Usage Day 7 Date"     { channel="rainsoft:wcs:12345:usage#day7date" }
Number RAINSOFT_USAGE_DAY7WATER       "RainSoft Usage Day 7 Water"    { channel="rainsoft:wcs:12345:usage#day7water" }
Number RAINSOFT_USAGE_DAY7SALT        "RainSoft Usage Day 7 Salt"     { channel="rainsoft:wcs:12345:usage#day7salt" }
String RAINSOFT_USAGE_DAY8DATE        "RainSoft Usage Day 8 Date"     { channel="rainsoft:wcs:12345:usage#day8date" }
Number RAINSOFT_USAGE_DAY8WATER       "RainSoft Usage Day 8 Water"    { channel="rainsoft:wcs:12345:usage#day8water" }
Number RAINSOFT_USAGE_DAY8SALT        "RainSoft Usage Day 8 Salt"     { channel="rainsoft:wcs:12345:usage#day8salt" }
String RAINSOFT_USAGE_DAY9DATE        "RainSoft Usage Day 9 Date"     { channel="rainsoft:wcs:12345:usage#day9date" }
Number RAINSOFT_USAGE_DAY9WATER       "RainSoft Usage Day 9 Water"    { channel="rainsoft:wcs:12345:usage#day9water" }
Number RAINSOFT_USAGE_DAY9SALT        "RainSoft Usage Day 9 Salt"     { channel="rainsoft:wcs:12345:usage#day9salt" }
String RAINSOFT_USAGE_DAY10DATE        "RainSoft Usage Day 10 Date"     { channel="rainsoft:wcs:12345:usage#day10date" }
Number RAINSOFT_USAGE_DAY10WATER       "RainSoft Usage Day 10 Water"    { channel="rainsoft:wcs:12345:usage#day10water" }
Number RAINSOFT_USAGE_DAY10SALT        "RainSoft Usage Day 10 Salt"     { channel="rainsoft:wcs:12345:usage#day10salt" }
String RAINSOFT_USAGE_DAY11DATE        "RainSoft Usage Day 11 Date"     { channel="rainsoft:wcs:12345:usage#day11date" }
Number RAINSOFT_USAGE_DAY11WATER       "RainSoft Usage Day 11 Water"    { channel="rainsoft:wcs:12345:usage#day11water" }
Number RAINSOFT_USAGE_DAY11SALT        "RainSoft Usage Day 11 Salt"     { channel="rainsoft:wcs:12345:usage#day11salt" }
String RAINSOFT_USAGE_DAY12DATE        "RainSoft Usage Day 12 Date"     { channel="rainsoft:wcs:12345:usage#day12date" }
Number RAINSOFT_USAGE_DAY12WATER       "RainSoft Usage Day 12 Water"    { channel="rainsoft:wcs:12345:usage#day12water" }
Number RAINSOFT_USAGE_DAY12SALT        "RainSoft Usage Day 12 Salt"     { channel="rainsoft:wcs:12345:usage#day12salt" }
String RAINSOFT_USAGE_DAY13DATE        "RainSoft Usage Day 13 Date"     { channel="rainsoft:wcs:12345:usage#day13date" }
Number RAINSOFT_USAGE_DAY13WATER       "RainSoft Usage Day 13 Water"    { channel="rainsoft:wcs:12345:usage#day13water" }
Number RAINSOFT_USAGE_DAY13SALT        "RainSoft Usage Day 13 Salt"     { channel="rainsoft:wcs:12345:usage#day13salt" }
String RAINSOFT_USAGE_DAY14DATE        "RainSoft Usage Day 14 Date"     { channel="rainsoft:wcs:12345:usage#day14date" }
Number RAINSOFT_USAGE_DAY14WATER       "RainSoft Usage Day 14 Water"    { channel="rainsoft:wcs:12345:usage#day14water" }
Number RAINSOFT_USAGE_DAY14SALT        "RainSoft Usage Day 14 Salt"     { channel="rainsoft:wcs:12345:usage#day14salt" }
String RAINSOFT_USAGE_DAY15DATE        "RainSoft Usage Day 15 Date"     { channel="rainsoft:wcs:12345:usage#day15date" }
Number RAINSOFT_USAGE_DAY15WATER       "RainSoft Usage Day 15 Water"    { channel="rainsoft:wcs:12345:usage#day15water" }
Number RAINSOFT_USAGE_DAY15SALT        "RainSoft Usage Day 15 Salt"     { channel="rainsoft:wcs:12345:usage#day15salt" }
String RAINSOFT_USAGE_DAY16DATE        "RainSoft Usage Day 16 Date"     { channel="rainsoft:wcs:12345:usage#day16date" }
Number RAINSOFT_USAGE_DAY16WATER       "RainSoft Usage Day 16 Water"    { channel="rainsoft:wcs:12345:usage#day16water" }
Number RAINSOFT_USAGE_DAY16SALT        "RainSoft Usage Day 16 Salt"     { channel="rainsoft:wcs:12345:usage#day16salt" }
String RAINSOFT_USAGE_DAY17DATE        "RainSoft Usage Day 17 Date"     { channel="rainsoft:wcs:12345:usage#day17date" }
Number RAINSOFT_USAGE_DAY17WATER       "RainSoft Usage Day 17 Water"    { channel="rainsoft:wcs:12345:usage#day17water" }
Number RAINSOFT_USAGE_DAY17SALT        "RainSoft Usage Day 17 Salt"     { channel="rainsoft:wcs:12345:usage#day17salt" }
String RAINSOFT_USAGE_DAY18DATE        "RainSoft Usage Day 18 Date"     { channel="rainsoft:wcs:12345:usage#day18date" }
Number RAINSOFT_USAGE_DAY18WATER       "RainSoft Usage Day 18 Water"    { channel="rainsoft:wcs:12345:usage#day18water" }
Number RAINSOFT_USAGE_DAY18SALT        "RainSoft Usage Day 18 Salt"     { channel="rainsoft:wcs:12345:usage#day18salt" }
String RAINSOFT_USAGE_DAY19DATE        "RainSoft Usage Day 19 Date"     { channel="rainsoft:wcs:12345:usage#day19date" }
Number RAINSOFT_USAGE_DAY19WATER       "RainSoft Usage Day 19 Water"    { channel="rainsoft:wcs:12345:usage#day19water" }
Number RAINSOFT_USAGE_DAY19SALT        "RainSoft Usage Day 19 Salt"     { channel="rainsoft:wcs:12345:usage#day19salt" }
String RAINSOFT_USAGE_DAY20DATE        "RainSoft Usage Day 20 Date"     { channel="rainsoft:wcs:12345:usage#day20date" }
Number RAINSOFT_USAGE_DAY20WATER       "RainSoft Usage Day 20 Water"    { channel="rainsoft:wcs:12345:usage#day20water" }
Number RAINSOFT_USAGE_DAY20SALT        "RainSoft Usage Day 20 Salt"     { channel="rainsoft:wcs:12345:usage#day20salt" }
String RAINSOFT_USAGE_DAY21DATE        "RainSoft Usage Day 21 Date"     { channel="rainsoft:wcs:12345:usage#day21date" }
Number RAINSOFT_USAGE_DAY21WATER       "RainSoft Usage Day 21 Water"    { channel="rainsoft:wcs:12345:usage#day21water" }
Number RAINSOFT_USAGE_DAY21SALT        "RainSoft Usage Day 21 Salt"     { channel="rainsoft:wcs:12345:usage#day21salt" }
String RAINSOFT_USAGE_DAY22DATE        "RainSoft Usage Day 22 Date"     { channel="rainsoft:wcs:12345:usage#day22date" }
Number RAINSOFT_USAGE_DAY22WATER       "RainSoft Usage Day 22 Water"    { channel="rainsoft:wcs:12345:usage#day22water" }
Number RAINSOFT_USAGE_DAY22SALT        "RainSoft Usage Day 22 Salt"     { channel="rainsoft:wcs:12345:usage#day22salt" }
String RAINSOFT_USAGE_DAY23DATE        "RainSoft Usage Day 23 Date"     { channel="rainsoft:wcs:12345:usage#day23date" }
Number RAINSOFT_USAGE_DAY23WATER       "RainSoft Usage Day 23 Water"    { channel="rainsoft:wcs:12345:usage#day23water" }
Number RAINSOFT_USAGE_DAY23SALT        "RainSoft Usage Day 23 Salt"     { channel="rainsoft:wcs:12345:usage#day23salt" }
String RAINSOFT_USAGE_DAY24DATE        "RainSoft Usage Day 24 Date"     { channel="rainsoft:wcs:12345:usage#day24date" }
Number RAINSOFT_USAGE_DAY24WATER       "RainSoft Usage Day 24 Water"    { channel="rainsoft:wcs:12345:usage#day24water" }
Number RAINSOFT_USAGE_DAY24SALT        "RainSoft Usage Day 24 Salt"     { channel="rainsoft:wcs:12345:usage#day24salt" }
String RAINSOFT_USAGE_DAY25DATE        "RainSoft Usage Day 25 Date"     { channel="rainsoft:wcs:12345:usage#day25date" }
Number RAINSOFT_USAGE_DAY25WATER       "RainSoft Usage Day 25 Water"    { channel="rainsoft:wcs:12345:usage#day25water" }
Number RAINSOFT_USAGE_DAY25SALT        "RainSoft Usage Day 25 Salt"     { channel="rainsoft:wcs:12345:usage#day25salt" }
String RAINSOFT_USAGE_DAY26DATE        "RainSoft Usage Day 26 Date"     { channel="rainsoft:wcs:12345:usage#day26date" }
Number RAINSOFT_USAGE_DAY26WATER       "RainSoft Usage Day 26 Water"    { channel="rainsoft:wcs:12345:usage#day26water" }
Number RAINSOFT_USAGE_DAY26SALT        "RainSoft Usage Day 26 Salt"     { channel="rainsoft:wcs:12345:usage#day26salt" }
String RAINSOFT_USAGE_DAY27DATE        "RainSoft Usage Day 27 Date"     { channel="rainsoft:wcs:12345:usage#day27date" }
Number RAINSOFT_USAGE_DAY27WATER       "RainSoft Usage Day 27 Water"    { channel="rainsoft:wcs:12345:usage#day27water" }
Number RAINSOFT_USAGE_DAY27SALT        "RainSoft Usage Day 27 Salt"     { channel="rainsoft:wcs:12345:usage#day27salt" }
String RAINSOFT_USAGE_DAY28DATE        "RainSoft Usage Day 28 Date"     { channel="rainsoft:wcs:12345:usage#day28date" }
Number RAINSOFT_USAGE_DAY28WATER       "RainSoft Usage Day 28 Water"    { channel="rainsoft:wcs:12345:usage#day28water" }
Number RAINSOFT_USAGE_DAY28SALT        "RainSoft Usage Day 28 Salt"     { channel="rainsoft:wcs:12345:usage#day28salt" }
```
