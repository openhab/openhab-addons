# KVV Binding

Adds support for the public API of Karlsruher Verkehrsverbund (public transport system in Karlsruhe, Germany).
Enables the user to show the latest departure times for specific street car stations in openhab.

## Supported Things

Every street car station is represented by one thing. Each thing contains channels for the information referred to the next ten trains.
This includes the name of the train, the final destination and the estimated time available (eta).


## Thing Configuration

Since every station is represented by a KVV-provided id, the initially has to find out the corresponding id of the station in questions.
The binding does not include an interface, but this can easily done per API call:
```
curl https://live.kvv.de/webapp/stops/byname/Volkswohnung\?key\=377d840e54b59adbe53608ba1aad70e8
```

## Full Example

The configuration files can be found in `/openhab2-addons/addons/binding/org.openhab.binding.kvv/cfg`.

1. Install *KVV Binding* as a new binding via *Paper UI*
1. Add one new thing per street car station:
    1. Station id: the id of the station found via the API call
    1. Update interval: update interval in milliseconds
    1. Name of the station: give the station a human-readable name
1. Define the iteams. `/openhab2-addons/addons/binding/org.openhab.binding.kvv/cfg/kvv.items` contains a complete example.
1. Add a new template to *habpanel*. See `/openhab2-addons/addons/binding/org.openhab.binding.kvv/cfg/kvv.template.txt` for an example.
