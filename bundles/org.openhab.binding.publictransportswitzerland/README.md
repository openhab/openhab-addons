# Public Transport Switzerland Binding

Connects to the "Swiss public transport API" to provide real-time public transport information. [Link to the API](https://transport.opendata.ch/)

For example, here is a station board in HABPanel. (Download [here](https://github.com/StefanieJaeger/HABPanel-departure-board))

![Departure board in HABPanel](doc/departure_board_habpanel.png)

## Supported Things

### Stationboard

Upcoming departures for a single station.

#### Configuration 

`Station` is the station name for which to display departures.  
The name has to be one that is used by the swiss federal railways.  
Please consult their [website](https://sbb.ch/en).

#### Channels 

| channel | type   | description |
|---------|--------|-------------|
| departures#n   | String | A dynamic channel for each upcoming departure |
| tsv (advanced) | String | A tsv which contains the fields:<br />`identifier, departureTime, destination, track, delay` |

## Discovery

This binding does not support auto-discovery.
