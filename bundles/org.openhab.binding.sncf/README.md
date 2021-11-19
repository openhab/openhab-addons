# SNCF Binding

The SNCF binding provides real-time data for each train, bus, tramway... station in France.
This is based on live API provided by DIGITALSNCF.

Get you API key at https://www.digital.sncf.com/startup/api/token-developpeur

Note : SNCF Api is based on the open [API Navitia](https://doc.navitia.io/#getting-started). This binding uses a very small subset of it, restricted to its primary purpose.

## Supported Things

Bridge: The binding supports a bridge to connect to the [DIGITALSNCF service](https://www.digital.sncf.com/startup/api/token-developpeur). A bridge uses the thing ID "api".

Station: Represents a given bus, train station.

Of course, you can add multiple as many stations as needed.


## Discovery

This binding takes care of auto discovery. This method is strongly recommended as its the only way to get proper station ID depending upon transportation type.

To enable auto-discovery, your location system setting must be defined. Once done, at first launch discovery will search every station in a radius of 2500 m around the system, extending it by step of 500 m until it finds a first set of results.
Every following manual successive launch will extend this radius by 500 m, increasing the number of stations discovered.


## Binding Configuration

The binding has no configuration options, all configuration is done at Thing level.

## Bridge Configuration

The bridge configuration only holds the api key : 

| Parameter | Description                                                    |
|-----------|----------------------------------------------------------------|
| apiID     | API ID provided by the DIGITALSNCF service. Mandatory.         |

## Thing Configuration

The 'Station' thing has only one configuration parameter:

| Parameter   | Description                                                  |
|-------------|--------------------------------------------------------------|
| stopPointId | Identifier of the station in the DIGITALSNCF network.        |

The thing will auto-update depending on the timestamp of the earliest event detected to trigger (arrival or departure).

## Channels

The Station thing holds two groups of channels (arrivals and departures) containing these channels:

| Channel ID            | Item Type | Description                                      |
|-----------------------|-----------|--------------------------------------------------|
| direction             | String    | The direction of the route                       |
| code                  | String    | Code name of the line                            |
| commercialMode        | String    | Commercial name of the line (RER, Transilien...) |
| name                  | String    | Name of the line                                 |
| network               | String    | Name of the network ruling the line              |
| timestamp             | DateTime  | Timestamp of the event (departure, arrival)      |

## Full Example

sncf.things:

```
Bridge sncf:api:main "Bridge" [apiID="xxx-yyy-zzz"] {
    station MyHouse "Krakow"[stopPointId="stop_point:SNCF:87561951:Bus"]
}
```

sncf.items:

```
```
