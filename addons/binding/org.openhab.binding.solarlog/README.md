# openHAB Binding for SolarLog 

The [SolarLog Family](http://www.solar-log.com/en/home.html) of monitoring devices for PV installations provide a MODBUS (TCP) and JSON-based API to access a number of internal data related to power generation and consumption. 

This binding is an attempt to simplify the reading of these values to be displayed in openHAB. It was born out of the "frustration" that setting up SolarLog monitoring via MODBUS is almost as tedious as writing an entirely new (albeit simple) openHAB binding. And using the HTTP binding is not an option since the JSON interface requires a POST with a payload, which the HTTP binding so far does not support.

## Use of the binding

The SolarLog is exposed as one thing with a number of channels that can be used to read the values for different aspects of your SolarLog installation. 

## Setup of the binding

You can either configure the Thing via the openHAB UI or via a `*.things` file, according to the following example:

`Thing solarlog:meter:pv "SolarLog 300" @ "Utility Room" [ url="http://solar-log" ]`

The parameters to be used are simple:

* `url` denotes the URL of your SolarLog. If you have not changed anything, this defaults to `http://solar-log`.
* `refreshInterval` is the interval to fetch new data. SolarLog updates its data every 15 - 60 seconds. `15` is the default value. Values lower than this will return the result from the last 15 seconds period. No value lower than this can be set in the UI.

Currently, the binding does not support authenticated access to the SolarLog JSON API (which was iuntroduced with newer firmwares). If must set the API access to `Open` in the SolarLog configuration in order for the binding to work.

## More information

More information about the SolarLog Data interfaces and the exact meaning of the various channels can be found in the [Installation Manual](http://www.solar-log.com/fileadmin/BENUTZERDATEN/Downloads/Handbuecher/EN/SolarLog_Installation_manual_EN.pdf)
