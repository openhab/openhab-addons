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
```
