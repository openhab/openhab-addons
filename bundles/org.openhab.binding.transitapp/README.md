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

To find the correct IDs for your configuration:

1. **Global Stop ID (`globalStopId`) & Target Stop ID (`targetStopId`)**: Use the GTFS / operator stop code (e.g., `VVSDE:2298` for Charlottenplatz in Stuttgart).
1. **Global Route ID (`routeId`)**: Found in the JSON response of a stop under the field `"global_route_id"` (e.g., `VVSDE:247174`).
1. **Trip Search Key (`tripId`)**: Found in the JSON response of stop departures under the schedule items as `"trip_search_key"` (e.g., `VVSDE:52245421:47:2:22`).

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
