# TransitApp Binding

This binding integrates public transit information and real-time departure details from the Transit API (v4) into openHAB.

## Supported Things

1. **TransitApp Bridge (`bridge`)**
   - Connects to the Transit API using your personal API key.
   - Validates the API key upon initialization.

1. **Transit Stop (`stop`)**
   - Polls real-time stop departures based on a global stop ID (e.g., `VVSDE:2298`).
   - Channels: Route Long Name, Route Short Name, Departure Time, Is Cancelled.

1. **Transit Route Details (`routedetails`)**
   - Retrieves route details and itineraries based on a global route ID (e.g., `VVSDE:247174`).
   - Channels: Route Long Name, Route Short Name, Agency Name.

1. **Transit Trip Details (`tripdetails`)**
   - Retrieves specific trip details based on a trip search key.
   - Channels: Trip Headsign, Trip Status.

## Configuration

### Bridge Configuration

- `apiKey`: Your personal Transit API key.

### Thing Configurations

- `refreshInterval`: Polling interval in seconds (default: 60s for stops/trips, 300s for route details).
- `globalStopId`: Global stop identifier (for Stop things).
- `routeId`: Global route identifier (for Route Details things).
- `tripId`: Trip search key (for Trip Details things).

## Finding Parameters (Stop ID, Route ID, Trip ID)

To find the correct IDs for your configuration:

1. **Global Stop ID (`globalStopId`)**: Use the GTFS / operator stop code (e.g., `VVSDE:2298` for Charlottenplatz in Stuttgart).
1. **Global Route ID (`routeId`)**: Found in the JSON response of a stop under the field `"global_route_id"` (e.g., `VVSDE:247174`).
1. **Trip Search Key (`tripId`)**: Found in the JSON response of stop departures under the schedule items as `"trip_search_key"` (e.g., `VVSDE:52245421:47:2:22`).

## Logging & Debugging

To enable full TRACE and DEBUG logging (including raw JSON responses) in the Karaf console:

```bash
log:set DEBUG org.openhab.binding.transitapp
log:set TRACE org.openhab.binding.transitapp
```
