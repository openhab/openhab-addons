# Astro Binding

The Astro binding is used for calculating 
    * many DateTime and positional values for sun and moon.
    * Radiation levels (direct, diffuse and total) of the sun during the day

## Supported Things

This binding supports two Things: Sun and Moon

## Discovery

If a system location is set, "Local Sun" and a "Local Moon" will be automatically discovered for this location.

If the system location is changed, the background discovery updates the configuration of "Local Sun" and "Local Moon" automatically.

## Binding Configuration

No binding configuration required.

## Thing Configuration

All Things require the parameter `geolocation` (as `<latitude>,<longitude>,[<altitude in m>]`) for which the calculation is done. 
The altitude segment is optional and sharpens results provided by the Radiation group.
Optionally, a refresh `interval` (in seconds) can be defined to also calculate positional data like azimuth and elevation.


## Channels

* **thing** `sun`
    * **group** `rise, set, noon, night, morningNight, astroDawn, nauticDawn, civilDawn, astroDusk, nauticDusk, civilDusk, eveningNight, daylight`
        * **channel** 
            * `start, end` (DateTime)
            * `duration` (Number:Time)
    * **group** `position`
        * **channel** 
            * `azimuth, elevation` (Number:Angle)
            * `shadeLength` (Number)
    * **group** `radiation`
        * **channel** 
            * `direct, diffuse, total` (Number:Intensity)
    * **group** `zodiac`
        * **channel** 
            * `start, end` (DateTime) 
            * `sign` (String), values: `ARIES, TAURUS, GEMINI, CANCER, LEO, VIRGO, LIBRA, SCORPIO, SAGITTARIUS, CAPRICORN, AQUARIUS, PISCES`
    * **group** `season`
        * **channel**: 
            * `spring, summer, autumn, winter` (DateTime)
            * `name` (String), values `SPRING, SUMMER, AUTUMN, WINTER`
    * **group** `eclipse`
        * **channel**: 
            * `total, partial, ring` (DateTime)
    * **group** `phase`
        * **channel** 
            * `name` (String), values: `SUN_RISE, ASTRO_DAWN, NAUTIC_DAWN, CIVIL_DAWN, CIVIL_DUSK, NAUTIC_DUSK, ASTRO_DUSK, SUN_SET, DAYLIGHT, NIGHT`
* **thing** `moon`
    * **group** `rise, set`
        * **channel** 
            * `start, end` (DateTime)
    * **group** `phase`
        * **channel**: 
            * `firstQuarter, thirdQuarter, full, new` (DateTime)
            * `age` (Number:Time)
            * `ageDegree` (Number:Angle)
            * `agePercent, illumination` (Number:Dimensionless)
            * `name` (String), values: `NEW, WAXING_CRESCENT, FIRST_QUARTER, WAXING_GIBBOUS, FULL, WANING_GIBBOUS, THIRD_QUARTER, WANING_CRESCENT`
    * **group** `eclipse`
        * **channel**: 
            * `total, partial` (DateTime)
    * **group** `distance`
        * **channel**: 
            * `date` (DateTime)
            * `distance` (Number:Length)
    * **group** `perigee`
        * **channel**: 
            * `date` (DateTime), 
            * `distance` (Number:Length)
    * **group** `apogee`
        * **channel**: 
            * `date` (DateTime)
            * `distance` (Number:Length)
    * **group** `zodiac`
        * **channel** 
            * `sign` (String), values: `ARIES, TAURUS, GEMINI, CANCER, LEO, VIRGO, LIBRA, SCORPIO, SAGITTARIUS, CAPRICORN, AQUARIUS, PISCES`
    * **group** `position`
        * **channel** 
            * `azimuth, elevation` (Number:Angle)

### Trigger Channels

* **thing** `sun`
    * **group** `rise, set, noon, night, morningNight, astroDawn, nauticDawn, civilDawn, astroDusk, nauticDusk, civilDusk, eveningNight, daylight`
        * **event** `START, END`
    * **group** `eclipse`
        * **event**: `TOTAL, PARTIAL, RING`
* **thing** `moon`
    * **group** `rise`
        * **event** `START`
    * **group** `set`
        * **event** `END`
    * **group** `phase`
        * **event**: `FIRST_QUARTER, THIRD_QUARTER, FULL, NEW`
    * **group** `eclipse`
        * **event**: `TOTAL, PARTIAL`
    * **group** `perigee`
        * **event**: `PERIGEE`
    * **group** `apogee`
        * **event**: `APOGEE`

### Channel config

**Offsets:** For each event group you can optionally configure an `offset` in minutes.
The `offset` must be configured in the channel properties for the corresponding thing.

The minimum allowed offset is -1440 and the maximum allowed offset is 1440.

**Earliest/Latest:** For each trigger channel and `start`, `end` datetime value, you can optionally configure the `earliest` and `latest` time of the day.

e.g `sun#set earliest=18:00, latest=20:00`

sunset is 17:40, but `earliest` is set to 18:00 so the event/datetime value is moved to 18:00.

OR

sunset is 22:10 but `latest` is set to 20:00 so the event/datetime value is moved 20:00.

## Full Example

Things:

```
astro:sun:home  [ geolocation="52.5200066,13.4049540,100", interval=60 ]
astro:moon:home [ geolocation="52.5200066,13.4049540", interval=60 ]
```

or optionally with an event offset

```
astro:sun:home [ geolocation="52.5200066,13.4049540,100", interval=60 ] {
    Channels:
        Type rangeEvent : rise#event [
            offset=-30
        ]
}
astro:moon:home [ geolocation="52.5200066,13.4049540", interval=60 ]
```

or a datetime offset

```
astro:sun:home [ geolocation="52.5200066,13.4049540,100", interval=60 ] {
    Channels:
        Type start : rise#start [
            offset=5
        ]
        Type end : rise#end [
            offset=5
        ]
}
```

or a offset and latest

```
astro:sun:home [ geolocation="52.5200066,13.4049540,100", interval=60 ] {
    Channels:
        Type rangeEvent : rise#event [
            offset=-10,
            latest="08:00"
        ]
}
```

Items:

```
DateTime         Sunrise_Time       "Sunrise [%1$tH:%1$tM]"                   { channel="astro:sun:home:rise#start" }
DateTime         Sunset_Time        "Sunset [%1$tH:%1$tM]"                    { channel="astro:sun:home:set#start" }
Number:Angle     Azimuth            "Azimuth"                                 { channel="astro:sun:home:position#azimuth" }
Number:Angle     Elevation          "Elevation"                               { channel="astro:sun:home:position#elevation" }
String           MoonPhase          "MoonPhase"                               { channel="astro:moon:home:phase#name" }
Number:Length    MoonDistance       "MoonDistance [%.1f %unit%]"              { channel="astro:moon:home:distance#distance" }
Number:Intensity Total_Radiation    "Radiation [%.2f %unit%]"                 { channel="astro:sun:home:radiation#total" }
Number:Intensity Diffuse_Radiation  "Diffuse Radiation [%.2f %unit%]"         { channel="astro:sun:home:radiation#diffuse" }
```

Events:

```
rule "example trigger rule"
when
    Channel 'astro:sun:home:rise#event' triggered START
then
    ...
end
```

## Tips

Do not worry if for example the "astro dawn" is undefined at your location.
The reason might be that you live in a northern country and it is summer, such that the sun is not 18 degrees below the horizon in the morning.
For details see [this Wikipedia article](https://en.wikipedia.org/wiki/Dawn).
The "civil dawn" event might often be the better choice.
