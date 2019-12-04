# KVV Binding

Adds support for the public API of Karlsruher Verkehrsverbund (public transport system in Karlsruhe, Germany).
Enables the user to show the latest departure times for specific street car stations in openhab.

## Supported Things

Every street car station is represented by one thing. Each thing contains channels for the information referred to the next n trains.
This includes the name of the train, the final destination and the estimated time available (eta).

## Thing Configuration

Since every station is represented by a KVV-provided id, this id has to be figured out via an API call.

### Example Call for Station 'Karlsruhe Volkswohnung'

```
# Request
curl https://live.kvv.de/webapp/stops/byname/Volkswohnung\?key\=377d840e54b59adbe53608ba1aad70e8

# Response
{"stops":[{"id":"de:8212:72","name":"Karlsruhe Volkswohnung","lat":49.00381654,"lon":8.40393026}]}
```

## Full Example

### demo.things

```
Bridge kvv:bridge:1 {
	station gottesauerplatz		"Gottesauer Platz/BGV"		[ commonName="Gottesauer Platz/BGV", stationId="de:8212:6", maxTrains="3", updateInterval="10000" ]
}
```

### demo.items

```
String kvv_gottesauerplatz_train0_name      	{channel="kvv:station:1:gottesauerplatz:train0-name"}
String kvv_gottesauerplatz_train0_destination	{channel="kvv:station:1:gottesauerplatz:train0-destination"}
String kvv_gottesauerplatz_train0_eta      		{channel="kvv:station:1:gottesauerplatz:train0-eta"}

String kvv_gottesauerplatz_train1_name      	{channel="kvv:station:1:gottesauerplatz:train1-name"}
String kvv_gottesauerplatz_train1_destination   {channel="kvv:station:1:gottesauerplatz:train1-destination"}
String kvv_gottesauerplatz_train1_eta      		{channel="kvv:station:1:gottesauerplatz:train1-eta"}

String kvv_gottesauerplatz_train2_name      	{channel="kvv:station:1:gottesauerplatz:train2-name"}
String kvv_gottesauerplatz_train2_destination   {channel="kvv:station:1:gottesauerplatz:train2-destination"}
String kvv_gottesauerplatz_train2_eta      		{channel="kvv:station:1:gottesauerplatz:train2-eta"}
```
