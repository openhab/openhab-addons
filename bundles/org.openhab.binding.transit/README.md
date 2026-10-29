# Transit Binding

This binding integrates public transit information and real-time departure details from the Transit API (v4) into openHAB.

> **Powered by Transit** (<https://transit.app>)

## Supported Things

1. **Transit Bridge (`bridge`)**: Connects to the Transit API using your personal API key and validates it upon initialization.
1. **Transit Stop (`stop`)**: Polls real-time stop departures based on a global stop ID (e.g., `VVSDE:2298`) or a comma-separated list of stop IDs. Models upcoming departures using group channels (`depart1` to `depart10`).
1. **Transit Route Details (`routedetails`)**: Retrieves comprehensive route details, colors, and alerts based on a global route ID (e.g., `VVSDE:247174`).
1. **Transit Trip Details (`tripdetails`)**: Retrieves specific real-time trip details and monitors up to 10 upcoming stops based on a trip search key.
1. **Transit Network Alerts (`networkalerts`)**: Shows the active service alerts of one or more transit networks.
1. **Transit Journey (`journey`)**: Plans a trip between two places (stop IDs or coordinates) and shows the itinerary that arrives first.
1. **Transit Vehicles (`vehicles`)**: Shows the real-time positions of up to 10 vehicles of a route (`vehicle1` to `vehicle10`).

## Discovery

The bridge can discover the stops near the location configured in openHAB (_Settings_ → _Regional Settings_).
Each scan uses one API request, so there is no background discovery: start a scan manually from the Inbox.
Discovered stops are added as `stop` Things with their `globalStopId`.

---

## Thing Configuration

### Bridge (`bridge`)

| Parameter           | Type    | Default | Description                                                           |
| :------------------ | :------ | :------ | :-------------------------------------------------------------------- |
| `apiKey`            | TEXT    |         | Your personal Transit API key (required).                             |
| `cacheTimeMs`       | INTEGER | `30000` | How long to cache API responses in milliseconds (5000–300000).        |
| `retryAfterSeconds` | INTEGER | `60`    | Default retry-after value when API rate limit is hit (1–600 seconds). |
| `maxDepartures`     | INTEGER | `10`    | Maximum number of departures to display per stop (1–10).              |

### Stop (`stop`)

| Parameter         | Type    | Default | Description                                                                                                       |
| :---------------- | :------ | :------ | :---------------------------------------------------------------------------------------------------------------- |
| `globalStopId`    | TEXT    |         | Global stop identifier (e.g., `VVSDE:2298`) or a comma-separated list (e.g., `VVSDE:2298,VVSDE:2394`) (required). |
| `refreshInterval` | INTEGER | `60`    | Polling interval in seconds.                                                                                      |

With several stop IDs, the departures of all stops are merged and sorted by departure time; one API request is used for all stops.
The channel `departX#global-stop-id` shows the stop a departure belongs to.

### Route Details (`routedetails`)

| Parameter         | Type    | Default | Description                                                |
| :---------------- | :------ | :------ | :--------------------------------------------------------- |
| `routeId`         | TEXT    |         | Global route identifier (e.g., `VVSDE:247174`) (required). |
| `refreshInterval` | INTEGER | `300`   | Polling interval in seconds.                               |

### Trip Details (`tripdetails`)

| Parameter         | Type    | Default | Description                                                                 |
| :---------------- | :------ | :------ | :-------------------------------------------------------------------------- |
| `tripSearchKey`   | TEXT    |         | Trip search key (e.g., `VVSDE:52245421:47:2:22`) (required).                |
| `targetStopId`    | TEXT    |         | Destination stop ID to calculate the `time-to-target` countdown (optional). |
| `refreshInterval` | INTEGER | `60`    | Polling interval in seconds.                                                |

### Network Alerts (`networkalerts`)

| Parameter         | Type    | Default | Description                                                     |
| :---------------- | :------ | :------ | :-------------------------------------------------------------- |
| `networkIds`      | TEXT    |         | Network ID or a comma-separated list of network IDs (required). |
| `refreshInterval` | INTEGER | `3600`  | Polling interval in seconds.                                    |

The network ID of a route is the `route_network_id` field of the `getDepartures` action result, e.g. `VVS|Stuttgart`.

### Journey (`journey`)

| Parameter         | Type    | Default | Description                                                             |
| :---------------- | :------ | :------ | :---------------------------------------------------------------------- |
| `fromStopId`      | TEXT    |         | Global stop ID of the start. Has priority over `fromLocation`.          |
| `fromLocation`    | TEXT    |         | Location of the start as `latitude,longitude` (e.g., `48.7784,9.1800`). |
| `toStopId`        | TEXT    |         | Global stop ID of the destination. Has priority over `toLocation`.      |
| `toLocation`      | TEXT    |         | Location of the destination as `latitude,longitude`.                    |
| `refreshInterval` | INTEGER | `900`   | Polling interval in seconds.                                            |

Either the stop ID or the location is required for the start and for the destination.
The Transit API returns several itineraries; the binding shows the one that arrives first among those that have not started yet.

### Vehicles (`vehicles`)

| Parameter         | Type    | Default | Description                                                      |
| :---------------- | :------ | :------ | :--------------------------------------------------------------- |
| `routeId`         | TEXT    |         | Global route identifier (e.g., `VVSDE:247174`) (required).       |
| `directionId`     | INTEGER | `-1`    | `0` = outbound only, `1` = inbound only, `-1` = both directions. |
| `refreshInterval` | INTEGER | `300`   | Polling interval in seconds.                                     |

Vehicles are sorted by their vehicle ID, so a vehicle keeps its channel group as long as the set of reported vehicles does not change.

### API Request Limits and Polling Intervals

The free tier of the Transit API is limited to 1,500 requests per month per API key (at the time of writing, check the terms of your own plan).
Every Thing polls the API on its own, so the limit is shared by all Things of a bridge.
With the default refresh interval of 60 seconds, a single Stop Thing uses about 1,440 requests per day and exhausts the monthly limit in roughly one day.

Requests per Thing when polling around the clock:

| `refreshInterval`   | Requests per day | Requests per 31 days |
| :------------------ | :--------------- | :------------------- |
| `60` (1 minute)     | 1,440            | 44,640               |
| `300` (5 minutes)   | 288              | 8,928                |
| `900` (15 minutes)  | 96               | 2,976                |
| `1800` (30 minutes) | 48               | 1,488                |
| `3600` (1 hour)     | 24               | 744                  |

Guidance for the free tier:

- For one Thing polling around the clock, use a `refreshInterval` of at least `1800` seconds.
  With several Things, multiply the interval accordingly (e.g. `3600` seconds for two Things).
- If you need frequent updates only at certain times (e.g. on weekday mornings before leaving the house), disable the Things outside these times with a rule instead of polling all day.
  For example, a Stop Thing enabled for 2 hours per day with a `refreshInterval` of `300` uses 24 requests per day (about 750 per month).
- Thing initialization, `REFRESH` commands, the Thing actions and discovery scans also count as requests.
- Combine several stops in one Stop Thing (comma-separated `globalStopId`) instead of creating one Thing per stop: this uses one request per update for all stops.
- When the limit is reached, the API answers with HTTP 429 and the Things go `OFFLINE` with a communication error until requests are possible again.

---

## Channels

### Stop Channels (`depart1` to `depart10`)

| Channel                           | Type        | Description                                                                                               |
| :-------------------------------- | :---------- | :-------------------------------------------------------------------------------------------------------- |
| `departX#route-long-name`         | String      | Full route name / itinerary                                                                               |
| `departX#route-short-name`        | String      | Short route name or line number (e.g., "43", "S1")                                                        |
| `departX#departure-time`          | DateTime    | Scheduled or live departure time                                                                          |
| `departX#minutes-until-departure` | Number:Time | Countdown until departure using UoM standard (`min`)                                                      |
| `departX#delay-minutes`           | Number:Time | Current delay using UoM standard (`min`)                                                                  |
| `departX#wheelchair-accessible`   | Switch      | Wheelchair accessibility of the trip: `ON` = accessible, `OFF` = not accessible, `UNDEF` = no information |
| `departX#is-cancelled`            | Switch      | Indicates if the departure is cancelled                                                                   |
| `departX#headsign`                | String      | Destination shown on the vehicle (e.g., "Feuersee")                                                       |
| `departX#is-real-time`            | Switch      | `ON` if the departure time is based on real-time data, `OFF` if it is the scheduled time                  |
| `departX#is-last`                 | Switch      | `ON` if this is the last departure of the day for this itinerary                                          |
| `departX#transport-mode`          | String      | Mode of transport (e.g., "Bus", "Subway")                                                                 |
| `departX#route-color`             | String      | Official hex color of the route                                                                           |
| `departX#global-stop-id`          | String      | Stop the departure belongs to (useful with several stop IDs) (advanced)                                   |
| `departX#global-route-id`         | String      | Global route ID for a Route Details or Vehicles Thing (advanced)                                          |
| `departX#trip-search-key`         | String      | Trip search key for a Trip Details Thing (advanced)                                                       |

### Route Details Channels (`route`)

| Channel                     | Type   | Description                                         |
| :-------------------------- | :----- | :-------------------------------------------------- |
| `route#route-long-name`     | String | Full route name                                     |
| `route#route-short-name`    | String | Short route name or line number                     |
| `route#route-color`         | String | Official hex color for UI badges                    |
| `route#transport-mode`      | String | Mode of transport (e.g., "Bus", "Subway")           |
| `route#active-alerts-count` | Number | Number of active service alerts and disruptions     |
| `route#alert-title`         | String | Title of the most severe active alert               |
| `route#alert-description`   | String | Description of the most severe active alert         |
| `route#alert-severity`      | String | `Unknown`, `Info`, `Warning` or `Severe`            |
| `route#alert-effect`        | String | Effect on the service, e.g. `NO_SERVICE` (advanced) |
| `route#alert-cause`         | String | Cause, e.g. `CONSTRUCTION` (advanced)               |

An alert counts as active if one of its active periods contains the current time or if it has no active periods.
If several alerts are active, the alert channels show the most severe one (the newest one if the severity is the same).

### Trip Details Channels (`trip` and `stop1` to `stop10`)

| Channel                         | Type        | Description                                                      |
| :------------------------------ | :---------- | :--------------------------------------------------------------- |
| `trip#time-to-target`           | Number:Time | Live countdown to the configured target destination stop (`min`) |
| `trip#route-short-name`         | String      | Route short name / line number                                   |
| `stopX#stop-name`               | String      | Name of the upcoming stop                                        |
| `stopX#minutes-until-departure` | Number:Time | Countdown until upcoming departure (`min`)                       |

### Network Alerts Channels (`alerts`)

The `alerts` group has the same alert channels as the Route Details Thing: `alerts#active-alerts-count`, `alerts#alert-title`, `alerts#alert-description`, `alerts#alert-severity`, `alerts#alert-effect` and `alerts#alert-cause`.

### Journey Channels (`journey`)

| Channel                           | Type        | Description                                                       |
| :-------------------------------- | :---------- | :---------------------------------------------------------------- |
| `journey#departure-time`          | DateTime    | Start of the journey (including the walk to the first stop)       |
| `journey#arrival-time`            | DateTime    | Arrival at the destination                                        |
| `journey#duration`                | Number:Time | Duration of the journey (`min`)                                   |
| `journey#minutes-until-departure` | Number:Time | Time until the start of the journey (`min`)                       |
| `journey#transfers`               | Number      | Number of transfers between transit vehicles                      |
| `journey#lines`                   | String      | Lines used by the journey (e.g., "U6, 43")                        |
| `journey#route-short-name`        | String      | First line of the journey                                         |
| `journey#delay-minutes`           | Number:Time | Delay of the first transit departure (`min`)                      |
| `journey#is-real-time`            | Switch      | `ON` if the first transit departure is based on real-time data    |
| `journey#is-cancelled`            | Switch      | `ON` if one of the transit departures of the journey is cancelled |
| `journey#global-route-id`         | String      | Global route ID of the first line (advanced)                      |
| `journey#trip-search-key`         | String      | Trip search key of the first transit departure (advanced)         |

A transit leg can be served by several lines (e.g., U6, U7 and U12 on the same track); the channels show the line of the first departure.

### Vehicles Channels (`vehicles` and `vehicle1` to `vehicle10`)

| Channel                          | Type     | Description                                                                         |
| :------------------------------- | :------- | :---------------------------------------------------------------------------------- |
| `vehicles#vehicle-count`         | Number   | Number of vehicles reported for the route                                           |
| `vehicles#last-update`           | DateTime | Newest position update of all vehicles                                              |
| `vehicleX#location`              | Location | Position of the vehicle                                                             |
| `vehicleX#last-update`           | DateTime | Time of the last position update                                                    |
| `vehicleX#direction-id`          | Number   | `0` = outbound, `1` = inbound (advanced)                                            |
| `vehicleX#occupancy-status`      | Number   | `1` = not crowded, `2` = some crowding, `3` = crowded (if provided by the operator) |
| `vehicleX#wheelchair-accessible` | Switch   | `ON` = accessible, `OFF` = not accessible, `UNDEF` = no information                 |
| `vehicleX#vehicle-label`         | String   | Vehicle identifier as provided by the operator (advanced)                           |

---

## Actions

| Thing  | Action                                                 | Result                                                            |
| :----- | :----------------------------------------------------- | :---------------------------------------------------------------- |
| bridge | `getNearbyStops(lat, lon)`                             | Raw JSON of the stops near a coordinate                           |
| bridge | `searchStops(lat, lon, query)`                         | Raw JSON of the stops matching a name near a coordinate           |
| bridge | `getTripPlan(fromLat, fromLon, toLat, toLon)`          | Raw JSON of the trip plan between two coordinates                 |
| bridge | `estimatePlanDuration(fromLat, fromLon, toLat, toLon)` | Estimated duration of a trip in seconds (`Long`), `null` on error |
| stop   | `getDepartures()`                                      | Raw JSON of the upcoming departures of the configured stop(s)     |
| stop   | `getNextDepartureForLine(lineName)`                    | Next departure time of a line (e.g., `"12:34"`) or `N/A`          |

Each action call is one API request.

---

## Finding Parameters (Stop ID, Route ID, Trip ID)

You can find the required IDs for your openHAB configuration directly within the openHAB MainUI using native Thing Actions, or by testing endpoints interactively online in the [Official Transit API v4 Documentation](https://api-doc.transitapp.com/v4.html#GET/v4).

### Option 1: UI Actions (Recommended)

The Transit binding provides built-in Rule Actions that you can trigger easily from the **Developer Sidebar** or the **Rule Action Tester** in openHAB MainUI. These return the raw JSON API responses allowing you to extract `global_stop_id`, route IDs, and real-time trip IDs (`trip_search_key`).

#### 1. Finding Stop IDs (`globalStopId` / `targetStopId`)

Use the `getNearbyStops` action on your `transit:bridge` Thing.

- **Input:** Provide your latitude (`lat`) and longitude (`lon`).
- **Result:** Returns a JSON array of all nearby stations. Look for the `global_stop_id` and `stop_name` fields.

If you know the name of the stop, use the `searchStops` action with a `query` (e.g., `Hauptbahnhof`) instead.
Alternatively, use the discovery (see above).

#### 2. Finding Route IDs (`routeId`) & Trip Search Keys (`tripSearchKey`)

Once you have your `globalStopId`, create a `transit:stop` Thing with it. Then use the `getDepartures` action on this newly created Stop Thing.

- **Result:** Returns a JSON array of all upcoming departures for this stop. Each route entry contains `route_short_name` and `global_route_id`; the `trip_search_key` for tracking a specific vehicle is found in `merged_itineraries[].schedule_items[]`.

### Option 2: Online API Explorer

Alternatively, you can execute all queries directly in your browser:

1. Open the [Transit API v4 Online Documentation](https://api-doc.transitapp.com/v4.html#GET/v4).
1. Enter your API Key in the request header / authorization block.
1. Select an endpoint such as `/v4/public/nearby_stops` or `/v4/public/stop_departures`.
1. Enter your query parameters (e.g., latitude/longitude or stop ID) and click **Send / Execute** to inspect the raw JSON response.

---

## Full Example

### Thing Configuration

Create a `.things` file (e.g., `transit.things`) with the following configuration:

```text
Bridge transit:bridge:mybridge [ apiKey="YOUR_API_KEY_HERE" ] {
    Thing stop mystop [ globalStopId="VVSDE:2298", refreshInterval=60 ]
    Thing routedetails myroute [ routeId="VVSDE:247174", refreshInterval=300 ]
    Thing tripdetails mytrip [ tripSearchKey="VVSDE:52245421:47:2:22", targetStopId="VVSDE:1234", refreshInterval=60 ]
    Thing networkalerts myalerts [ networkIds="VVS|Stuttgart", refreshInterval=3600 ]
    Thing journey mywork [ fromLocation="48.7784,9.1800", toStopId="VVSDE:2394", refreshInterval=900 ]
    Thing vehicles myvehicles [ routeId="VVSDE:247174", directionId=0, refreshInterval=300 ]
}
```

### Item Configuration

```java
String   Stop_Line      "Line [%s]"                 { channel="transit:stop:mybridge:mystop:depart1#route-short-name" }
String   Stop_Headsign  "Direction [%s]"            { channel="transit:stop:mybridge:mystop:depart1#headsign" }
Number:Time Stop_Minutes "Departure in [%d %unit%]" { channel="transit:stop:mybridge:mystop:depart1#minutes-until-departure" }
Switch   Stop_RealTime  "Real-time"                 { channel="transit:stop:mybridge:mystop:depart1#is-real-time" }
String   Alert_Title    "Alert [%s]"                { channel="transit:networkalerts:mybridge:myalerts:alerts#alert-title" }
DateTime Work_Leave     "Leave at [%1$tH:%1$tM]"    { channel="transit:journey:mybridge:mywork:journey#departure-time" }
DateTime Work_Arrival   "Arrival [%1$tH:%1$tM]"     { channel="transit:journey:mybridge:mywork:journey#arrival-time" }
Location Bus1_Position  "Bus 1"                     { channel="transit:vehicles:mybridge:myvehicles:vehicle1#location" }
```

### Rule Example

```javascript
// JavaScript Scripting
const transit = actions.thingActions("transit", "transit:bridge:mybridge");
const seconds = transit.estimatePlanDuration(48.7784, 9.1800, 48.7832, 9.1812);
console.info("Estimated trip duration: " + seconds + " s");
```
