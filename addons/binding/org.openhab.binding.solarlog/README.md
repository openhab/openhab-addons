# openHAB Binding for SolarLog 

The [SolarLog Family](http://www.solar-log.com/en/home.html) of monitoring devices for PV installations provide a MODBUS (TCP) and JSON-based API to access a number of internal data related to power generation and consumption. This binding implements access to the data via the JSON API.

## Use of the binding

The SolarLog is exposed as one thing with a number of channels that can be used to read the values for different aspects of your SolarLog installation. 

## Setup of the binding

You can either configure the Thing via the openHAB UI or via a `*.things` file, according to the following example:

`Thing solarlog:meter:pv "SolarLog 300" @ "Utility Room" [ url="http://solar-log" refreshInterval=15]`

The parameters to be used are simple:

* `url` denotes the URL of your SolarLog. If you have not changed anything, this defaults to `http://solar-log`.
* `refreshInterval` is the interval to fetch new data. SolarLog updates its data every 15 - 60 seconds. `15` is the default value. Values lower than this will return the result from the last 15 seconds period. No value lower than this can be set in the UI.

Currently, the binding does not support authenticated access to the SolarLog JSON API (which was iuntroduced with newer firmwares). If must set the API access to `Open` in the SolarLog configuration in order for the binding to work.

## Available channels

The following table is taken from the official manual and contains all available channels. If you want to manually define Items, this can for example be done as follows:

`Number solarlog_meter_pv_yieldday "Yield Day [% W]" (gSolarLog, gUtilityRoom) { channel="solarlog:meter:pv:yieldday" }` 

Data point          | Unit                  | Index Description
------------------- | --------------------- | -----------------   
lastUpdateTime      | Time in the format dd.mm.yy; hh.minmin, secsec | 100 Time
Pac                 | W  | 101 Total output PAC from all of the inverters and meters in inverter mode
Pdc                 | W  | 102 Total output PAC from all of the inverters
Uac                 | V  | 103 Average voltage UAC from the inverter
Udc                 | V  | 104 Average voltage UDC from the inverter
yieldDay            | Wh | 105 Total yield for the day from all of the inverters
yieldYesterday      | Wh | 106 Total yield for the previous day from all of the inverters
yieldMonth          | Wh | 107 Total yield for the month from all of the inverters
yieldYear           | Wh | 108 Total yield for the year from all of the inverters
yieldTotal          | Wh | 109 Total yield from all of the inverters
consPac             | W  | 110 Current total consumption PAC from all of the consumption meters
consYieldDay        | Wh | 111 Total consumption from all of the consumption meters
consYieldYesterday  | Wh | 112 Total consumption for the previous day; all of the consumption meters
consYieldMonth      | Wh | 113 Total consumption for the month; all of the consumption meters
consYieldYear       | Wh | 114 Total consumption for the year; all of the consumption meters
consYieldTotal      | Wh | 115 Accumulated total consumption, all Consumption meter
totalPower          | Wp | 116 Installed generator power
------------------- | --------------------- | -----------------   

## More information

More information about the SolarLog Data interfaces and the exact meaning of the various channels and the documentation of the JSON API can be found in the [Manual](https://www.solar-log.com/manuals/manuals/en_GB/SolarLog_Manual_3x_EN.pdf). 
