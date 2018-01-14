# Bosch Indego Binding

This is the Binding for Bosch Indego Connect lawn mowers.
Thank´s to zazaz-de who found out how the API works. His [Java Library](https://github.com/zazaz-de/iot-device-bosch-indego-controller) made this Binding possible.

## Configuration of the Thing

Currently the binding supports  ***indego***  mowers as a thing type with this parameters:

| parameter | datatype | required                       |
|-----------|----------|--------------------------------|
| username  | String   | yes                            |
| password  | String   | yes                            |
| refresh   | integer  | no (default: 180, minimum: 60) |

The refresh interval is specified in seconds.

A possible entry in your thing file could be:

```
boschindego:indego:lawnmower [username="myname@myhost.tld", password="idontneedtocutthelawnagain", refresh=120]
```


## Channels

| item-type    | description |                                                                                                                                     |
|--------------|-------------|-------------------------------------------------------------------------------------------------------------------------------------|
| state        | Number      | You can send commands to this channel to control the mower and read the simplified state from it (1=mow, 2=return to dock, 3=pause) |
| errorcode    | Number      | Errorcode of the mower (0=no error, readonly)                                                                                       |
| statecode    | Number      | Detailed state of the mower. I included English and German map-files to read the state easier (readonly)                            |
| textualstate | String      | State as a text. (readonly)                                                                                                         |
| ready        | Number      | Shows if the mower is ready to mow (1=ready, 0=not ready, readonly)                                                                 |
| mowed        | Dimmer      | Cut grass in percent (readonly)                                                                                                     |

For example you can use this sitemap entry to control the mower manually:

```
Switch item=indegostate  mappings=[ 1="Mow", 2="Return",3="Pause" ]
```

## Meaning of the numeric statecodes

You can use this as .map file

```
0=Reading status
257=Charging
258=Docked
259=Docked - Software update
260=Docked
261=Docked
262=Docked - Loading map
263=Docked - Saving map
513=Mowing
514=Relocalising
515=Loading map
516=Learning lawn
517=Paused
518=Border cut
519=Idle in lawn
769=Returning to Dock
770=Returning to Dock
771=Returning to Dock - Battery low
772=Returning to dock - Calendar timeslot ended
773=Returning to dock - Battery temp range
774=Returning to dock
775=Returning to dock - Lawn complete
776=Returning to dock - Relocalising
```
