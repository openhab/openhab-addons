# KVV Binding

Adds support for the public API of Karlsruher Verkehrsverbund (public transport system in Karlsruhe, Germany).
Enables the user to show the latest departure times for specific street car stops in openHAB.

## Supported Things

Every street car stop is represented by one thing.
Each thing contains channels for the information referred to the next n trains.
This includes the name of the train, the final destination and the estimated time available (eta).

## Thing Configuration

Since every stop is represented by a KVV-provided id, this id has to be figured out via an API call.

### Example Call for Stop 'Gottesauer Platz/BGV'

```bash
export QUERY="gottesauer"
curl https://www.kvv.de/tunnelEfaDirect.php?action=XSLT_STOPFINDER_REQUEST&name_sf=${QUERY}&outputFormat=JSON&type_sf=any
```

The exact `id` may be extracted from the JSON-encoded reponse. E.g.

```json
"points": [
{
    "usage": "sf",
    "type": "any",
    "name": "Karlsruhe, Gottesauer Platz/BGV",
    "stateless": "7000006",
    "anyType": "stop",
    "sort": "2",
    "quality": "949",
    "best": "0",
    "object": "Gottesauer Platz/BGV",
    "mainLoc": "Karlsruhe",
    "modes": "1,4,5",
    "ref": {
        "id": "7000006",
        "gid": "de:08212:6",
        "omc": "8212000",
        "placeID": "15",
        "place": "Karlsruhe",
        "coords": "937855.00000,5723868.00000"
    }
}
```

## Channel Configuration

Each stop automatically creates a set of channels depending on the configuration.
If `maxTrains` is set to three, each stop creates three sets of channels.
Each set consists of the following three channels:

| Channel                                                    | Type   | Description                                                   |
| :--------------------------------------------------------- | :----- | :------------------------------------------------------------ |
| kvv:stop:${bridgeId}:${stopId}:train${trainId}-name        | String | Name of the line, e.g. _S5_                                   |
| kvv:stop:${bridgeId}:${stopId}:train${trainId}-destination | String | Name of the stop the train is heading to                      |
| kvv:stop:${bridgeId}:${stopId}:train${trainId}-eta         | String | Duration until the train arrives. Can be relative or absolute |

## Full Example

### Things

```things
Bridge kvv:bridge:1 "Bridge" @ "Wohnzimmer" [ maxTrains="3", updateInterval="10" ] {
    stop gottesauerplatz        "Gottesauer Platz/BGV"      [ stopId="7000006" ]
}
```

### Items

```items
String kvv_gottesauerplatz_train0_name          "Name [%s]"         {channel="kvv:stop:1:gottesauerplatz:train0-name"}
String kvv_gottesauerplatz_train0_destination   "Destination [%s]"  {channel="kvv:stop:1:gottesauerplatz:train0-destination"}
String kvv_gottesauerplatz_train0_eta           "ETA [%s]"          {channel="kvv:stop:1:gottesauerplatz:train0-eta"}

String kvv_gottesauerplatz_train1_name          "Name [%s]"         {channel="kvv:stop:1:gottesauerplatz:train1-name"}
String kvv_gottesauerplatz_train1_destination   "Destination [%s]"  {channel="kvv:stop:1:gottesauerplatz:train1-destination"}
String kvv_gottesauerplatz_train1_eta           "ETA [%s]"          {channel="kvv:stop:1:gottesauerplatz:train1-eta"}

String kvv_gottesauerplatz_train2_name          "Name [%s]"         {channel="kvv:stop:1:gottesauerplatz:train2-name"}
String kvv_gottesauerplatz_train2_destination   "Destination [%s]"  {channel="kvv:stop:1:gottesauerplatz:train2-destination"}
String kvv_gottesauerplatz_train2_eta           "ETA [%s]"          {channel="kvv:stop:1:gottesauerplatz:train2-eta"}
```

### Template for HABPanel

```html
<style>
.kvvtable > tbody > tr > td {padding: 3px 20px; text-align: left;}
</style>
<table class="kvvtable">
<tr>
  <td>{{itemValue('kvv_gottesauerplatz_train0_name')}}</td>
  <td>{{itemValue('kvv_gottesauerplatz_train0_destination')}}</td>
  <td>{{itemValue('kvv_gottesauerplatz_train0_eta')}}</td>
</tr>
<tr>
  <td>{{itemValue('kvv_gottesauerplatz_train1_name')}}</td>
  <td>{{itemValue('kvv_gottesauerplatz_train1_destination')}}</td>
  <td>{{itemValue('kvv_gottesauerplatz_train1_eta')}}</td>
</tr><tr>
  <td>{{itemValue('kvv_gottesauerplatz_train2_name')}}</td>
  <td>{{itemValue('kvv_gottesauerplatz_train2_destination')}}</td>
  <td>{{itemValue('kvv_gottesauerplatz_train2_eta')}}</td>
</tr>
</table>
```

### Sitemap for Basic UI

```sitemap
sitemap kvv label="KVV" {
    Frame label="Gottesauer Platz/BGV" {
        Group item=kvv_gottesauerplatz_train0_destination label="Train #1" {
            Text item=kvv_gottesauerplatz_train0_name
            Text item=kvv_gottesauerplatz_train0_destination
            Text item=kvv_gottesauerplatz_train0_eta
        }
        Group item=kvv_gottesauerplatz_train1_destination label="Train #2" {
            Text item=kvv_gottesauerplatz_train1_name
            Text item=kvv_gottesauerplatz_train1_destination
            Text item=kvv_gottesauerplatz_train1_eta
        }
        Group item=kvv_gottesauerplatz_train2_destination label="Train #3" {
            Text item=kvv_gottesauerplatz_train2_name
            Text item=kvv_gottesauerplatz_train2_destination
            Text item=kvv_gottesauerplatz_train2_eta
        }
    }
}
```
