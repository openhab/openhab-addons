# TransitApp Binding

This binding integrates public transit data from the [Transit API](https://transitapp.com/partners/apis) into openHAB, allowing you to monitor real-time vehicle departures, schedules, and active transit service alerts for specific stops.

---

## Attribution & Requirements

According to the Transit API partnership guidelines, you must visibly display the **“Powered by Transit”** branding/logo within your user interface or service when utilizing this data.

---

## Supported Things

### Bridge (`bridge`)

The bridge acts as the primary connection point to the Transit API. It manages global HTTP settings and validates your API key against the server.

### Stop (`stop`)

The stop thing represents a specific public transit stop. It queries upcoming departures dynamically based on your configuration parameters and creates corresponding departure channels.

---

## Discovery

Automatic discovery is not supported. Both the **Bridge** and **Stop** things must be added manually through the openHAB UI or via a `.things` configuration file.

---

## Getting an API Key

To use this binding, you need a valid API key for the Transit API:

1. Visit the [Transit API Partner Portal](https://transitapp.com/partners/apis).
1. Request or register for an API key according to their developer partnership guidelines.
1. Use the obtained API key in the Bridge configuration.

---

## Bridge Configuration

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `apiKey` | Text | **Yes** | — | Your personal API key obtained from the [Transit API Partner Portal](https://transitapp.com/partners/apis). |
| `refreshInterval` | Integer | No | `60` | Polling interval in seconds to update departures. |

---

## Stop Configuration

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `globalStopId` | Text | **Yes** | — | The global stop ID provided by the Transit feed (e.g., `VVSDE:2298`). |
| `time` | Integer | **Yes** | `0` | UNIX timestamp representing the time for departures (`0` uses current time). |
| `removeCancelled` | Boolean | **Yes** | `false` | Remove cancelled schedule items from the results. |
| `locale` | Text | **Yes** | `en` | Language locale for translated names and strings. |
| `shouldUpdateRealtime` | Boolean | **Yes** | `true` | Tells the system whether to fetch real-time updates or schedule info only. |
| `maxNumDepartures` | Integer | **Yes** | `3` | Number of departures to return per merged itinerary (1-10). Determines how many departure channels get updated. |
| `includeStopsAndShapes` | Boolean | **Yes** | `false` | Control whether intermediate stops and shape polylines are included. |
| `stopDetailed` | Boolean | **Yes** | `false` | When true, returns detailed stop objects. Only applies if `includeStopsAndShapes=true`. |

---

## Channels

The Stop thing provides general information channels as well as indexed departure channels generated up to 10 times based on the `maxNumDepartures` parameter (using the naming convention `name#number`, e.g. `#1` to `#10`).

| Channel ID | Type | Description |
|------------|------|-------------|
| `jsonResponse` | String | Raw JSON response payload returned by the API. |
| `stopName` | String | Name of the transit stop parsed directly from the API response. |
| `routeLongName#X` | String | Long display name of the route for departure #X (where X is 1 to 10). |
| `routeShortName#X` | String | Short display name or route number for departure #X. |
| `departureTime#X` | DateTime | Scheduled or real-time departure time for departure #X. |
| `isCancelled#X` | Switch | Indicates whether departure #X is cancelled (`ON` / `OFF`). |

---

## Full Example

### `transitapp.things`

```transit
Bridge transitapp:bridge:myBridge "TransitApp Bridge" [ apiKey="YOUR_API_KEY_HERE", refreshInterval=60 ] {
    thing stop mystop "Charlottenplatz Stop" [
        globalStopId="VVSDE:2298",
        time=0,
        removeCancelled=false,
        locale="en",
        shouldUpdateRealtime=true,
        maxNumDepartures=3,
        includeStopsAndShapes=false,
        stopDetailed=false
    ]
}
```

### `transitapp.items`

```items
String Transit_RawJson        "Raw JSON Response [%s]"        { channel="transitapp:stop:myBridge:mystop:jsonResponse" }
String Transit_StopName       "Stop Name [%s]"                { channel="transitapp:stop:myBridge:mystop:stopName" }
String Transit_Route1         "Next Route Name [%s]"          { channel="transitapp:stop:myBridge:mystop:routeLongName#1" }
DateTime Transit_Time1         "Next Departure [%1$tH:%1$tM]"  { channel="transitapp:stop:myBridge:mystop:departureTime#1" }
Switch Transit_Cancelled1     "Trip Cancelled [%s]"           { channel="transitapp:stop:myBridge:mystop:isCancelled#1" }
```
