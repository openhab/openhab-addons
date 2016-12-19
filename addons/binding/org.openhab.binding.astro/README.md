# Astro Binding

The Astro binding is used for calculating many DateTime and positional values for sun and moon.

## Supported Things

This binding supports two Things: Sun and Moon

## Discovery

Discovery is not necessary, because all calculations are done within the binding.

## Binding Configuration

No binding configuration required.

## Thing Configuration

The things requires the geolocation (latitude, longitude) for which the calculation is done. Optionally, a refresh interval (in seconds) can be defined to also calculate positional data like azimuth and elevation.

## Channels

* **thing** `sun`
    * **group** `rise, set, noon, night, morningNight, astroDawn, nauticDawn, civilDawn, astroDusk, nauticDusk, civilDusk, eveningNight, daylight`
        * **channel** `start, end` (DateTime), `duration` (Number)
    * **group** `position`
        * **channel** `azimuth, elevation` (Number)
    * **group** `zodiac`
        * **channel** `start, end` (DateTime), `sign` (String)
    * **group** `season`
        * **channel**: `spring, summer, autumn, winter` (DateTime), `name` (String)
    * **group** `eclipse`
        * **channel**: `total, partial, ring` (DateTime)
* **thing** `moon`
    * **group** `rise, set`
        * **channel** `start, end` (DateTime), `duration` (Number), **Note:** start and end is always equal, duration always 0.
    * **group** `phase`
        * **channel**: `firstQuarter, thirdQuarter, full, new` (DateTime), `age, illumination` (Number), `name` (String)
    * **group** `eclipse`
        * **channel**: `total, partial` (DateTime)
    * **group** `distance`
        * **channel**: `date` (DateTime), `kilometer, miles` (Number)
    * **group** `perigee`
        * **channel**: `date` (DateTime), `kilometer, miles` (Number)
    * **group** `apogee`
        * **channel**: `date` (DateTime), `kilometer, miles` (Number)
    * **group** `zodiac`
        * **channel** `sign` (String)
    * **group** `position`
        * **channel** `azimuth, elevation` (Number)

## Full Example

Things:

```
astro:sun:home  [ geolocation="xx.xxxxxx,xx.xxxxxx", interval=60]
astro:moon:home [ geolocation="xx.xxxxxx,xx.xxxxxx", interval=60]
```

Items:

```
DateTime Sunrise_Time  "Sunrise [%1$tH:%1$tM]"  { channel="astro:sun:home:rise#start" }
DateTime Sunset_Time   "Sunset [%1$tH:%1$tM]"   { channel="astro:sun:home:set#start" }
Number   Azimuth       "Azimuth"                { channel="astro:sun:home:position#azimuth" }
Number   Elevation     "Elevation"              { channel="astro:sun:home:position#elevation" }
String   MoonPhase     "MoonPhase"              { channel="astro:moon:home:phase#name" }
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

Available events:
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

**Note**: delayed events not available, currently not supported by the openHab 2 framework

