# Tankerkönig Binding

The binding uses the Tankerkönig API <https://www.tankerkoenig.de> for collecting gas price data of german gas stations.
Special thanks to the creators of Tankerkönig for providing an easy way to get data from  the &lsqb;MTS-K&rsqb; (Markttransparenzstelle für Kraftstoffe).

Tankerkönig is providing this service for free, however they request to prevent overloading of their server by reducing the number of web-requests.
This binding handles those requests (minimum Refresh Interval is 10 minutes, a webserver does handle a maximum of 10 stations).
The data will be updated for each Station individually after the initialization and after each Refresh Interval for all (open) stations (Note: changing the Webservice will cause the Refresh Interval to restart).
Additionally one may select the mode Opening-Times in which only those Stations get polled which are actually open.
For a correct usage of opening times the binding needs the information if the actual day is a holiday.

Note:
While using the mode Opening-Times the channel "station_open" will NOT show "close" because during such times no update is being requested from that Station!

## Preparation

In order to use this binding one needs to prepare:

-minimal Java Varsion is 1.8.0_101-b13 (otherwise the https request will not produce a usable return)

-a personal API-Key

Request a free Tankerkönig API key from: <https://creativecommons.tankerkoenig.de/>  Select the tab "API-Key".

-LocationIDs of the selected gas stations

Search for the gas station IDs here: <https://creativecommons.tankerkoenig.de/configurator/index.html>
Drag the red marker on the map to the rough location of desired gas stations.
Select the gas stations and click "Tankstellen übernehmen" on the right.
This will download a file holding the location IDs. For example: a7cdd9cf-b467-4aac-8eab-d662f082511e

## Supported Things

This binding supports:

-Webservice (bridge)

-Station (thing)

## Discovery

The binding provides no discovery.
The desired Webservice and Stations must be configured manually or via a things file.

## Binding configuration

The binding has no configuration options itself, all configuration is done at 'Bridge' and 'Things' level.

## Thing configuration

The Webservice (bridge) needs to be configured with the personal API-Key, the desired Refresh Interval (the time interval between price-updates, default 60 minutes, minimum 10 minutes) and the Opening-Times mode selection (in this mode price-updates are only requested from stations that are actually open).
A single Webservice can handle up to 10 Stations.

Each Station needs to be configured with a LocationID and the Webservice to which it is linked.

## Channels

The binding introduces the channel holiday for the Webservice and the channels e10, e5 ,diesel and station_open for the Stations:

| Channel ID   | Channel Description                   | Supported item type | Advanced |
|--------------|---------------------------------------|---------------------|----------|
| holiday      | ON if today is a holiday              | Switch              | False    |
| e10          | price of e10                          | Number              | False    |
| e5           | price of e5                           | Number              | False    |
| diesel       | price of diesel                       | Number              | False    |
| station_open | reported opening-state of the station | Contact             | False    |

## Example

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
Number E10_1 "E10 [%.3f €]" { channel="tankerkoenig:station:WebserviceName:StationName1:e10" }
Number E5_1 "E5 [%.3f €]"  { channel="tankerkoenig:station:WebserviceName:StationName1:e5" }
Number Diesel_1 "Diesel [%.3f €]" { channel="tankerkoenig:station:WebserviceName:StationName1:diesel"}
Number E10_2 "E10 [%.3f €]" { channel="tankerkoenig:station:WebserviceName:StationName2:e10"}
Number E5_2 "E5 [%.3f €]" { channel="tankerkoenig:station:WebserviceName:StationName2:e5"}
Number Diesel_2 "Diesel [%.3f €]" { channel="tankerkoenig:station:WebserviceName:StationName2:diesel"}
```

## FAQ

-The Webservice stays OFFLINE

If only a Webservice is configured, it will remain OFFLINE until a Station is configured as well.
Each Station schedules a daily job to update detail-data, on completion of that job the Station and the Webservice will change to ONLINE.
The further price-updates for all Stations are scheduled by the Webservice using the Refresh Interval.

-The Station(s) and Webservice stay OFFLINE

Set the logging level for the binding to DEBUG (Karaf-Console command: "log:set DEBUG org.openhab.binding.tankerkoenig".
Create a new Station (in order to start the "initialize" routine).
Check the openhab.log for entries like:

```
 2017-06-25 16:02:12.679 [DEBUG] [ig.internal.data.TankerkoenigService] - getTankerkoenigDetailResult IOException:
java.io.IOException: java.util.concurrent.ExecutionException: javax.net.ssl.SSLHandshakeException: General SSLEngine problem
......
```

That indicates a missing certificate for the https-connection on the system.
In order to get the required certificate on a Linux-system one needs to perform these steps:

```
sudo wget http://www.startssl.com/certs/ca.crt
keytool -import -keystore cacerts -alias startssl -file ca.crt
```

The required password is "changeit".

-The Station(s) and Webservice go to OFFLINE after being ONLINE

The web-request to Tankerkönig did either return a failure or no valid response was received (which could be caused by a banned API-key).
In both cases the Webservice and the Station(s) go OFFLINE.
If the Tankerkönig return indicates an error a descriptive message (in German) is added next to the OFFLINE which will be displayed on the Webservice and Station(s) pages on PaperUI.
On the next receipt of a valid message Webservice and Station(s) will go ONLINE again.
The scheduled polling of price-data is canceled in case of no valid response.
Users should check the log for any reports to solve the reason for the OFFLINE status.
In order to restart the polling a change of the Webservice has to be saved (for example a change in the Refresh Interval).

Note: If the API-key is banned by Tankerkönig, the reason has to be cleared with Tankerkönig!

-How to set the switch item for the channel holiday?

The correct usage of opening times needs the information if the actual day is a holiday.
The binding expects a switch item linked to the Webservice channel holiday.
This switch can be set either manually (only suggested for testing!), by a rule &lsqb;openHAB1-addons rules&rsqb; or by the usage of the CALDAV binding with a calendar.

## Tankerkönig API

*   <https://creativecommons.tankerkoenig.de/>  (sorry, only available in German)

*   &lsqb;MTS-K&rsqb;: <https://www.bundeskartellamt.de/DE/Wirtschaftsbereiche/Mineral%C3%B6l/MTS-Kraftstoffe/Verbraucher/verbraucher_node.html>
*   &lsqb;openhab1-addons rules&rsqb;: <https://github.com/openhab/openhab1-addons/wiki/Samples-Rules#how-to-calculate-public-holidays>
