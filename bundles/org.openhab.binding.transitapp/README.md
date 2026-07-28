# TransitApp Binding

This binding integrates public transit information and real-time departure details from the Transit API (v4) into openHAB.

## Supported Things

1. **TransitApp Bridge (`bridge`)**
   - Connects to the Transit API using your personal API key.
   - Validates the API key upon initialization.

1. **Transit Stop (`stop`)**
   - Polls real-time stop departures based on a global stop ID (e.g., `VVSDE:2298`).
   - Models upcoming departures using group channels (`depart1`, `depart2`, `depart3`).
   - Channels per departure group:
     - `departX#routeLongName` (String): Full route name / itinerary
     - `departX#routeShortName` (String): Short route name or line number (e.g., "43", "S1")
     - `departX#departureTime` (DateTime): Scheduled or live departure time
     - `departX#minutesUntilDeparture` (Number:Time): Countdown until departure using UoM standard (`min`)
     - `departX#delayMinutes` (Number:Time): Current delay using UoM standard (`min`)
     - `departX#platform` (String): Track or platform (e.g., "Gleis 1")
     - `departX#wheelchairAccessible` (Switch): Wheelchair accessibility (ON/OFF)
     - `departX#occupancy` (String): Vehicle occupancy status
     - `departX#isCancelled` (Switch): Indicates if the departure is cancelled (ON/OFF)

1. **Transit Route Details (`routedetails`)**
   - Retrieves comprehensive route details, colors, alerts, and start/destination locations based on a global route ID (e.g., `VVSDE:247174`).
   - Models route attributes using the group channel `route`.
   - Channels:
     - `route#routeLongName` / `route#routeShortName` (String): Full and short route names
     - `route#agencyName` / `route#routeNetworkName` (String): Transit agency and network name (e.g., "VVS|Stuttgart")
     - `route#modeName` / `route#vehicleName` (String): Transport mode and vehicle type
     - `route#routeColor` / `route#routeTextColor` (String): Official hex colors for UI badges
     - `route#startLocation` / `route#destinationLocation` (String): Start and end stop names of the route
     - `route#activeAlertsCount` (Number): Number of active service alerts and disruptions
     - `route#alertTitle` / `route#alertDescription` / `route#alertSeverity` (String): Active service alerts details
     - `route#url` (String): Web link to route schedule or information

1. **Transit Trip Details (`tripdetails`)**
   - Retrieves specific real-time trip details and monitors up to 5 upcoming stops based on a trip search key.
   - Models general trip details under `trip1` and upcoming stop previews under `stop1` to `stop5`.
   - General Trip Channels (`trip1`):
     - `trip1#tripHeadsign` (String): Destination sign on the vehicle
     - `trip1#tripStatus` (String): Current trip status (e.g., "In Transit", "On Time")
     - `trip1#rtTripId` (String): Real-time vehicle tracking ID
     - `trip1#location` (Location): GPS coordinates (latitude, longitude) for UI Map widgets
     - `trip1#timeToTarget` (Number:Time): Live countdown to the configured target destination stop (`min`)
     - `trip1#occupancy` (String): Vehicle occupancy status
     - `trip1#bikesAllowed` (Switch): Indicates if bicycles are allowed on this vehicle (ON/OFF)
     - `trip1#routeLongName` / `trip1#routeShortName` / `trip1#routeColor` / `trip1#modeName` / `trip1#vehicleName`: Route & vehicle info
   - Upcoming Stop Channels (`stop1` to `stop5`):
     - `stopX#stopName` (String): Name of the upcoming stop
     - `stopX#scheduledTime` / `stopX#realtimeTime` (DateTime): Timetable and real-time departure time
     - `stopX#minutesUntilDeparture` (Number:Time): Countdown until upcoming departure using UoM standard (`min`)
     - `stopX#delayMinutes` (Number:Time): Current delay using UoM standard (`min`)
     - `stopX#platform` (String): Platform or track for the upcoming stop

## Configuration

### Bridge Configuration

- `apiKey` (text, required): Your personal Transit API key.

### Thing Configurations

- **Stop (`stop`)**:
  - `globalStopId` (text, required): Global stop identifier (e.g., `VVSDE:2298`).
  - `refreshInterval` (integer, default: `60`): Polling interval in seconds.
- **Route Details (`routedetails`)**:
  - `routeId` (text, required): Global route identifier (e.g., `VVSDE:247174`).
  - `includeNextDeparture` (boolean, default: `false`): Include next departure for each stop.
  - `stopDetailed` (boolean, default: `false`): Return detailed stop objects.
  - `locale` (text, optional): Language locale for translated names.
  - `refreshInterval` (integer, default: `300`): Polling interval in seconds.
- **Trip Details (`tripdetails`)**:
  - `tripId` (text, required): Trip search key (e.g., `VVSDE:52245421:47:2:22`).
  - `targetStopId` (text, optional): Destination stop ID to calculate the `timeToTarget` countdown (e.g., `VVSDE:1234`).
  - `includeContinuation` (boolean, default: `false`): Append immediate next trip stops if vehicle continues in-seat.
  - `locale` (text, optional): Language locale for translated strings.
  - `refreshInterval` (integer, default: `60`): Polling interval in seconds.

## Finding Parameters (Stop ID, Route ID, Trip ID)

You can find the required IDs for your openHAB configuration either directly in your terminal using `curl` and `jq`, or by testing endpoints interactively online in the [Official Transit API v4 Documentation](https://api-doc.transitapp.com/v4.html#GET/v4).

### Option 1: Terminal Commands (curl & jq)

#### 1. Finding Stop IDs (`globalStopId` / `targetStopId`)

To search for stops and stations around your location, query the `nearby_stops` endpoint with your GPS coordinates (`lat` and `lon`). This command formats the JSON output into a clean table:

```bash
curl -s 'https://external.transitapp.com/v4/public/nearby_stops?lat=48.8753&lon=9.3958' \
  --header 'Accept: application/json' \
  --header 'apiKey: YOUR_API_KEY_HERE' \
  | jq -r '
    ["GLOBAL STOP ID", "CITY", "STOP NAME", "TYPE", "DISTANCE", "LAT", "LON"],
    (.stops[] |
    [.global_stop_id,
     (.city_name // "-"),
     .stop_name,
     (if .route_type == 1 then "U-/Stadtbahn" elif .route_type == 2 then "Zug/S-Bahn" elif .route_type == 3 then "Bus" else .route_type|tostring end),
     ("\(.distance) m"),
     .stop_lat,
     .stop_lon])
    | @tsv
  ' | column -t -s $'\t'
```

_Tip: To filter for a specific station name (e.g., "Bahnhof" or "Winnenden"), replace `.stops[] |` with `.stops[] | select(.stop_name | test("Bahnhof"; "i")) |` inside the jq query._

#### 2. Finding Route IDs (`routeId`) & Trip IDs (`tripId`)

Once you have your `global_stop_id`, query the `stop_departures` endpoint to list all upcoming departures, lines, and their exact real-time Trip IDs (`trip_search_key`):

```bash
curl -s 'https://external.transitapp.com/v4/public/stop_departures?global_stop_id=YOUR_STOP_ID_HERE' \
  --header 'Accept: application/json' \
  --header 'apiKey: YOUR_API_KEY_HERE' \
  | jq -r '
    ["LINE / ROUTE", "HEADSIGN / DESTINATION", "TRIP SEARCH KEY (ID)"],
    (.route_departures[]? | .route_short_name as $route | .merged_itineraries[]? | .itineraries[]? |
    [$route, .headsign, .trip_search_key])
    | @tsv
  ' | column -t -s $'\t'
```

### Option 2: Online API Explorer

Alternatively, you can execute all queries directly in your browser without terminal commands:

1. Open the [Transit API v4 Online Documentation](https://api-doc.transitapp.com/v4.html#GET/v4).
1. Enter your API Key in the request header / authorization block.
1. Select an endpoint such as `/v4/public/nearby_stops` or `/v4/public/stop_departures`.
1. Enter your query parameters (e.g., latitude/longitude or stop ID) and click **Send / Execute** to inspect the raw JSON response.

## Item Example Configuration

Using the `#` group channel syntax in your `.items` file or UI:

```openhab
String      Stop1_Route         "Linie [%s]"             { channel="transitapp:stop:mybridge:mystop:depart1#routeShortName" }
Number:Time Stop1_Countdown     "In [%d %unit%]"         { channel="transitapp:stop:mybridge:mystop:depart1#minutesUntilDeparture" }
Number:Time Stop1_Delay         "Verspätung [%d %unit%]" { channel="transitapp:stop:mybridge:mystop:depart1#delayMinutes" }
String      Stop1_Platform      "Gleis [%s]"             { channel="transitapp:stop:mybridge:mystop:depart1#platform" }
Switch      Stop1_Cancelled     "Fällt aus [%s]"         { channel="transitapp:stop:mybridge:mystop:depart1#isCancelled" }

Number      Route_AlertsCount   "Störungen [%d]"         { channel="transitapp:routedetails:mybridge:myroute:route#activeAlertsCount" }
String      Route_AlertTitle    "Störung [%s]"           { channel="transitapp:routedetails:mybridge:myroute:route#alertTitle" }

Location    Trip_LiveLocation   "Fahrzeug Position"      { channel="transitapp:tripdetails:mybridge:mytrip:trip1#location" }
Number:Time Trip_CountdownZiel  "Ankunft am Ziel in [%d %unit%]" { channel="transitapp:tripdetails:mybridge:mytrip:trip1#timeToTarget" }
String      Trip_NextStop       "Nächster Halt [%s]"     { channel="transitapp:tripdetails:mybridge:mytrip:stop1#stopName" }
```

## Logging & Debugging

To enable full TRACE and DEBUG logging (including raw JSON responses) in the Karaf console:

```bash
log:set DEBUG org.openhab.binding.transitapp
log:set TRACE org.openhab.binding.transitapp
```
