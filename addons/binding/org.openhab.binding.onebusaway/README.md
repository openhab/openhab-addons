# OneBusAway Binding

[OneBusAway](https://onebusaway.org/) is an open source, real-time, transit-information service.  This binding allows you to get events based on transit arrival and departures, so you can create rules to do something based on that data.

## Preparation

You'll need to obtain an API key from the transit provider you want to load data from.
Different providers of the service have different policies, so you'll have to figure this part out for each [deployment](https://github.com/OneBusAway/onebusaway/wiki/OneBusAway-Deployments).

## Supported Things

This binding supports route arrival and departure times for all stops provided from a OneBusAway deployment.

## Binding Configuration

The following configuration options are available for the API binding:

| Parameter   | Name       | Description                                                                         | Required |
|-------------|------------|-------------------------------------------------------------------------------------|----------|
| `apiKey`    | API Key    | The API key given to you by a transit provider for their deployment.                | yes      |
| `apiServer` | API Server | The domain name of the deployment to talk to, e.g. `api.pugetsound.onebusaway.org`. | yes      |


The following configuration options are available for the Stop binding (which requires an API binding):

| Parameter | Name | Description | Required |
|-----------|------|-------------|----------|
| `stopId` | Stop ID | The OneBusAway ID of the stop to obtain data for, e.g. `1_26860`. | yes |
| `interval` | Update Interval | The number of seconds between updates. | no |

## Thing Configuration

The following configuration options are available for a Route (which requires a Stop binding):

| Parameter | Name     | Description                                                         | Required |
|-----------|----------|---------------------------------------------------------------------|----------|
| `routeId` | Route ID | The OneBusAway ID of the route to obtain data for, e.g. `1_102574`. | yes      |


## Channels

The Route Thing supports the following state channels:

| Channel Type ID  | Channel Kind | Item Type | Description                                                                                              |
|------------------|--------------|-----------|----------------------------------------------------------------------------------------------------------|
| arrival          | state        | DateTime  | The arrival time of a Route at a Stop.                                                                   |
| departure        | state        | DateTime  | The departure time of a Route at a Stop.                                                                 |
| update           | state        | DateTime  | The last time this data was updated (per the data provider, not the last time OpenHAB updated the data). |
| arrivalDeparture | trigger      | DateTime  | Triggered when a Route arrives or departs a Stop.                                                        |


### Channel Configurations

The `arrival`, `departure`, and `arrivalDeparture` channels can be configured with an `offset` specifying the number of seconds to move an event back in time.

## Full Example

Here's an example of a configuration for a bus stop in Seattle, WA, USA that has three routes configured.

`demo.things`:

```
Bridge onebusaway:api:pugentsound [apiKey="your-api-key", apiServer="api.pugetsound.onebusaway.org"] {
  Bridge onebusaway:stop:1_26860 [stopId="1_26860"] {
    Thing onebusaway:route:1_100193 [routeId="1_100193"]
    Thing onebusaway:route:1_102574 [routeId="1_102574"]
    Thing onebusaway:route:1_100252 [routeId="1_100252"]
  }
}
```

`demo.items`:

```
// Route 1_100193 (#32)
DateTime Fremont_32_Arrival "32 - University District" { channel="onebusaway:route:1_100193:arrival" }
DateTime Fremont_32_Departure "32 - University District" { channel="onebusaway:route:1_100193:departure" }

// Route 1_102574 (#40)
DateTime Fremont_40_Arrival "40 - Ballard" { channel="onebusaway:route:1_102574:arrival" }
DateTime Fremont_40_Departure "40 - Ballard" { channel="onebusaway:route:1_102574:departure" }

// Route 1_100252 (#62)
DateTime Fremont_62_Arrival "62 - Sand Point East Green Lake" { channel="onebusaway:route:1_100252:arrival" }
DateTime Fremont_62_Departure "62 - Sand Point East Green Lake" { channel="onebusaway:route:1_100252:departure" }
```
