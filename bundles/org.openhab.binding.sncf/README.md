# SNCF Binding

The SNCF binding provides real-time data(*) for each train, bus, tramway... station in France.
This is based on live API provided by DIGITALSNCF.

Get your API key on [DIGITALSNCF web site](https://www.digital.sncf.com/startup/api/token-developpeur)

Note: SNCF Api is based on the open [API Navitia](https://doc.navitia.io/#getting-started).
This binding uses a very small subset of it, restricted to its primary purpose.

(*) According to DIGITALSNCF Transilien may only be available for schedule, maybe not real-time.

## Supported Things

Bridge: The binding supports a bridge to connect to the [DIGITALSNCF service](<https://www.digital.sncf.com/startup/api/token> developpeur).
A bridge uses the thing ID "api".

Station: Represents a given bus, train station.

Of course, you can add as many stations as needed.

## Discovery

This binding takes care of auto discovery. This method is strongly recommended as it is the only way to get proper station ID depending upon transportation type.

To enable auto-discovery, your location system setting must be defined.
Once done, at first launch, discovery will search every station in a radius of 2000 m around the system, extending it by step of 500 m until it finds a first set of results.
Every following manual successive launch will extend this radius by 500 m, increasing the number of stations discovered.

## Binding Configuration

The binding has no configuration options, all configuration is done at Thing level.

## Bridge Configuration

The bridge configuration only holds the api key:

| Parameter | Description                                            |
| --------- | ------------------------------------------------------ |
| apiID     | API ID provided by the DIGITALSNCF service. Mandatory. |

## Thing Configuration

The 'Station' thing has only one configuration parameter:

| Parameter   | Description                                           |
| ----------- | ----------------------------------------------------- |
| stopPointId | Identifier of the station in the DIGITALSNCF network. |

The thing will auto-update depending on the timestamp of the earliest event detected to trigger (arrival or departure).

## Channels

The Station thing holds two groups of channels (arrivals and departures) containing these channels:

| Channel ID | Item Type | Description                                 |
| ---------- | --------- | ------------------------------------------- |
| direction  | String    | The direction of the route                  |
| lineName   | String    | Commercial name of the line                 |
| name       | String    | Name of the line                            |
| network    | String    | Name of the network ruling the line         |
| timestamp  | DateTime  | Timestamp of the event (departure, arrival) |

## Full Example

sncf.things:

```java
Bridge sncf:api:8901d44a68 "Bridge" [apiID="xxx-yyy-zzz"] {
    station MyHouse "Krakow"[stopPointId="stop_point:SNCF:87561951:Bus"]
}
```

sncf.items:

```java
String      Arrival_Direction   { channel="sncf:station:8901d44a68:87381475_RapidTransit:arrivals#direction" }
String      Arrival_Line        { channel="sncf:station:8901d44a68:87381475_RapidTransit:arrivals#lineName" }
DateTime    Arrival_Time        { channel="sncf:station:8901d44a68:87381475_RapidTransit:arrivals#timestamp" }
String      Departure_Direction { channel="sncf:station:8901d44a68:87381475_RapidTransit:departures#direction" }
String      Departure_Line      { channel="sncf:station:8901d44a68:87381475_RapidTransit:departures#lineName" }
DateTime    Departure_Time      { channel="sncf:station:8901d44a68:87381475_RapidTransit:departures#timestamp" }
```
