# Tankerkönig Binding

The binding uses the Tankerkönig API (https://www.tankerkoenig.de) for collecting gas price data of german gas stations. 
Special thanks to the creators of Tankerkönig for providing an easy way to get data from  the [MTS-K]  (Markttransparenzstelle für Kraftstoffe).

Tankerkönig is providing this service for free, however they request to prevent overloading of their server by reducing the number of web-requests. This binding handles those requests (minimum Refresh Interval is 10 minutes, a webserver does handle maximum of 10 stations.
The data will be updated for each Station individually after the initialization and after each Refresh Interval for all (open) stations (Note: changing the Webservice will cause the Refresh Interval to restart).
Additionally one may select the mode Opening-Times in which only those Stations get polled which are actually open.


## Preparation

In order to use this binding one needs to prepare:

-a personal API-Key

Demand free Tankerkönig API key from: https://creativecommons.tankerkoenig.de/  Select the tab "API-Key".

-LocationIDs of the selected gas stations

Search for the gas station IDs here: https://creativecommons.tankerkoenig.de/configurator/index.html 
Drag the red marker on the map to the rough location of desired gas stations. Select the gas stations and click "Tankstellen übernehmen" on the right. This will download a file holding the location IDs. For example: a7cdd9cf-b467-4aac-8eab-d662f082511e

## Supported Things

This binding supports:

-Webservice (bridge)

-Station (thing)

## Discovery

The binding provides no discovery. The desired Webservice and Stations must be configured manually or via a things file.

## Binding configuration

The binding has no configuration options itself, all configuration is done at 'Bridge' and 'Things' level.

## Thing configuration

The Webservice (bridge) needs to be configured with the personal API-Key, the desired Refresh Interval (the time interval between price-updates, default 60 minutes, minimum 10 minutes) and the Opening-Times mode selection (in this mode price-updates are only requested from stations that are actually open). 
A single Webservice can handle up to 10 Stations.
 
Each Station needs to be configured with a LocationID and the Webservice to which it is linked.

## Channels

The binding introduces the following channels:

| Channel ID                                      | Channel Description                                          | Supported item type | Advanced |
|-------------------------------------------------|--------------------------------------------------------------|---------------------|----------|
| e10                                             | price of e10                                                 | Number              | False    |
| e5                                              | price of e5                                                  | Number              | False    |
| diesel                                          | price of diesel                                              | Number              | False    |


## Full example

Note: All apikeys and locationids are only examples!

tankerkoenig.things:

```
Bridge tankerkoenig:webservice:WebserviceName "MyWebserviceName" [ apikey="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx", refresh= 60, modeOpeningTime =false ] {
        Thing station StationName1 "MyStationName1" @ "GasStations"[ locationid = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx" ]
        Thing station StationName2 "MyStationName2" @ "GasStations"[ locationid = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx" ]
}
```


tankerkoenig.items:

```
Number E10_1 "E10 [%.3f €]" { channel="tankerkoenig:station:StationName1:e10" }
Number E5_1 "E5 [%.3f €]"  { channel="tankerkoenig:station:StationName1:e5" }
Number Diesel_1 "Diesel [%.3f €]" { channel="tankerkoenig:station:StationName1:diesel"}
Number E10_2 "E10 [%.3f €]" { channel="tankerkoenig:station:StationName2:e10"}
Number E5_2 "E5 [%.3f €]" { channel="tankerkoenig:station:StationName2:e5"}
Number Diesel_2 "Diesel [%.3f €]" { channel="tankerkoenig:station:StationName2:diesel"}
```

## Tankerkönig API

*  https://creativecommons.tankerkoenig.de/  (sorry, only available in german)

   [MTS-K]: <https://www.bundeskartellamt.de/DE/Wirtschaftsbereiche/Mineral%C3%B6l/MTS-Kraftstoffe/Verbraucher/verbraucher_node.html>


