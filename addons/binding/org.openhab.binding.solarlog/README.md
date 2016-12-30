The [SolarLog Family](http://www.solar-log.com/en/home.html) of monitoring devices for PV installations provide a MODBUS (TCP) and JSON-based API to access a number of internal data related to power generation and consumption. 

This binding is an attempt to simplify the reading of these values to be displayed in OpenHAB2. It was born out of the "frustration" that setting up SolarLog monitoring via MODBUS is almost as tedious as writing an OpenHAB2 binding and that using the HTTP binding is not an option since the JSON interface requires a POST with a payload. Which the HTTP binding so far does not support.

The SolarLog is exposed a one thing with a number of channels that can be used to read the values.

You can either configure the Thing via the OpenHAB2 UI or via a `*.things` file, according to the following example:

`Thing solarlog:solarlog_js:pv "SolarLog 300" @ "Utility Room" [ url="http://solar-log" refreshInterval="15" ]`

The parameters to be used are simple:

* `url` denotes the URL of your SolarLog. If you have not changed anything, this defaults to `http://solar-log`.
* `refreshInterval` is the interval to fetch new data. SolarLog updates its data every 15 - 60 seconds. `15` is the default value and no value lower than this can be used.

More information about the SolarLog Data interfaces can be found in the [Installation Manual](http://www.solar-log.com/fileadmin/BENUTZERDATEN/Downloads/Handbuecher/EN/SolarLog_Installation_manual_EN.pdf)