# Tankerkönig Binding

The binding uses the Tankerkönig API <https://www.tankerkoenig.de> for collecting gas price data of German gas stations.
Special thanks to the creators of Tankerkönig for providing an easy way to get data from the &lsqb;MTS-K&rsqb; (Markttransparenzstelle für Kraftstoffe).

Tankerkönig is providing this service for free, however they request to prevent overloading of their server by reducing the number of web-requests.
This binding handles those requests (minimum Refresh Interval is 5 minutes, a webserver does handle a maximum of 10 stations).
The data will be updated for each Station individually after the initialization and after each Refresh Interval for all (open) stations (Note: changing the Webservice will cause the Refresh Interval to restart).
Additionally one may select the mode Opening-Times in which only those Stations get polled which are actually open.
For a correct usage of opening times the binding needs the information if the actual day is a holiday.

Note:
While using the mode Opening-Times the channel "station_open" will NOT show "close" because during such times no update is being requested from that Station!

## Preparation

In order to use this binding one needs to prepare:

- minimal Java Version is 1.8.0_101-b13 (otherwise the https request will not produce a usable return)

- a personal API-Key

Request a free Tankerkönig API key from: <https://creativecommons.tankerkoenig.de/> (Select the tab "API-Key").

- LocationIDs of the selected gas stations

Search for the gas station IDs via the [finder tool](https://creativecommons.tankerkoenig.de/TankstellenFinder/index.html) (Select tab "Tools" -> "Tankstellenfinder").
Drag the red marker on the map to the rough location of desired gas stations.
Select the gas stations and click "Tankstellen übernehmen" on the right.
This will download a file holding the location IDs.
For example: `a7cdd9cf-b467-4aac-8eab-d662f082511e`

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

The Webservice (bridge) needs to be configured with the personal API-Key, the desired Refresh Interval (the time interval between price-updates, default 60 minutes, minimum 5 minutes) and the Opening-Times mode selection (in this mode price-updates are only requested from stations that are actually open).
A single Webservice can handle up to 10 Stations.

Each Station needs to be configured with a LocationID and the Webservice to which it is linked.

## Channels

The binding introduces the channel `holiday` for the Webservice and the channels `e10`, `e5`, `diesel` and `station_open` for the Stations:

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

```java
Bridge tankerkoenig:webservice:WebserviceName "MyWebserviceName" [ apikey="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx", refresh= 60, modeOpeningTime =false ] {
        Thing station StationName1 "MyStationName1" @ "GasStations"[ locationid = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx" ]
        Thing station StationName2 "MyStationName2" @ "GasStations"[ locationid = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx" ]
}
```

tankerkoenig.items:

```java
Switch Station_Holidays "Today is holiday: [%s]" { channel="tankerkoenig:webservice:WebserviceName:holiday"}
Number E10_1 "E10 [%.3f €]" { channel="tankerkoenig:station:WebserviceName:StationName1:e10" }
Number E5_1 "E5 [%.3f €]"  { channel="tankerkoenig:station:WebserviceName:StationName1:e5" }
Number Diesel_1 "Diesel [%.3f €]" { channel="tankerkoenig:station:WebserviceName:StationName1:diesel"}
Contact Station_Open_1 "Station is [%s]" { channel="tankerkoenig:station:WebserviceName:StationName1:station_open"}
Number E10_2 "E10 [%.3f €]" { channel="tankerkoenig:station:WebserviceName:StationName2:e10"}
Number E5_2 "E5 [%.3f €]" { channel="tankerkoenig:station:WebserviceName:StationName2:e5"}
Number Diesel_2 "Diesel [%.3f €]" { channel="tankerkoenig:station:WebserviceName:StationName2:diesel"}
Contact Station_Open_2 "Station is [%s]" { channel="tankerkoenig:station:WebserviceName:StationName2:station_open"}
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

```text
 2017-06-25 16:02:12.679 [DEBUG] [ig.internal.data.TankerkoenigService] - getTankerkoenigDetailResult IOException:
java.io.IOException: java.util.concurrent.ExecutionException: javax.net.ssl.SSLHandshakeException: General SSLEngine problem
......
```

This indicates a missing certificate of a certification authority (CA) in the certificate-store of the Java JDK under which openHAB is running.
In most cases, updating to the latest version of JDK solves this because the store of cacerts are maintained and updated in Java releases.

Note: You must restart openHAB after a Java update.

If you receive the error because you are running an old Linux installation which does not have the latest java-versions available in its package-repositories, you may be able to fix the issue using one of the three options below:

   1. Update the Linux system and install the latest Java version

   1. Download the most recent JDK and install it directly on to your system without using a pre-composed package

   1. Update the cacerts store by importing the missing certificate

Check which CA has validated the certificate

Navigate to <https://creativecommons.tankerkoenig.de/>

Check which CA has validated the certificate

Export the certificate of the certificate authority

Import the certificate to the CA-store which you have found

```java
>> cd /usr/lib/jvm/jdk-11-oracle-arm32-vfp-hflt/jre/lib/security
>> keytool -import -keystore cacerts -alias LetsEncrypt -file ca.crt
```

The required password is "changeit".

Restart your server

-The Station(s) and Webservice go to OFFLINE after being ONLINE

Either the web-request to Tankerkönig returned a failure or no valid response was received (this could be caused by a banned API-key).
In both cases the Webservice and the Station(s) go OFFLINE.
If the Tankerkönig return indicates an error a descriptive message (in German) is added next to the OFFLINE which will be displayed on the Webservice and Station(s) pages on UI. For further investigation the API Explorer can be used to show all data-fields of the things.
On the next receipt of a valid message Webservice and Station(s) will go ONLINE again.
The scheduled polling of price-data is canceled in case of no valid response.
Users should check the log for any reports to solve the reason for the OFFLINE status.
In order to restart the polling a change of the Webservice has to be saved (for example a change in the Refresh Interval).

Note: If the API-key is banned by Tankerkönig, the reason has to be cleared with Tankerkönig!

-How to set the switch item for the channel holiday?

The correct usage of opening times needs the information if the actual day is a holiday.
The binding expects a switch item linked to the Webservice channel holiday.
This switch can be set either manually (only suggested for testing!) or by a rule which uses the [ephemeris action](https://www.openhab.org/docs/configuration/actions.html#ephemeris) to set that switch.

## Tankerkönig API

- <https://creativecommons.tankerkoenig.de/>  (sorry, only available in German)

- &lsqb;MTS-K&rsqb;: <https://www.bundeskartellamt.de/DE/Wirtschaftsbereiche/Mineral%C3%B6l/MTS-Kraftstoffe/Verbraucher/verbraucher_node.html>
