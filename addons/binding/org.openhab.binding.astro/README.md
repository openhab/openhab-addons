# Astro Binding

The Astro binding is used for calculating many DateTime and positional values for sun and moon.

## Supported Things

This binding supports two Things: Sun and Moon

## Discovery

Discovery is not necessary, because all calculations are done within the binding.

## Binding Configuration

No binding configuration required.

## Thing Configuration

A thing requires the geolocation (latitude, longitude) for which the calculation is done.
Optionally, a refresh interval (in seconds) can be defined to also calculate positional data like azimuth and elevation.

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

**Offsets:** For each event group you can optionally configure an offset in minutes.
The offset must be configured in the channel properties for the corresponding thing.
The minimum allowed offset is -1440 and the maximum allowed offset is 1440.

## Full Example

Things:

```
astro:sun:home  [ geolocation="xx.xxxxxx,xx.xxxxxx", interval=60 ]
astro:moon:home [ geolocation="xx.xxxxxx,xx.xxxxxx", interval=60 ]
```

or optionally with an offset

```
astro:sun:home [ geolocation="xx.xxxxxx,xx.xxxxxx", interval=60 ] {
    Channels:
        Type rangeEvent : rise#event [
            offset=-30
        ]
}
astro:moon:home [ geolocation="xx.xxxxxx,xx.xxxxxx", interval=60 ]
```

Items:

```
DateTime Sunrise_Time  "Sunrise [%1$tH:%1$tM]"  { channel="astro:sun:home:rise#start" }
DateTime Sunset_Time   "Sunset [%1$tH:%1$tM]"   { channel="astro:sun:home:set#start" }
Number   Azimuth       "Azimuth [%.1f °]"       { channel="astro:sun:home:position#azimuth" }
Number   Elevation     "Elevation [%.1f °]"     { channel="astro:sun:home:position#elevation" }
String   MoonPhase     "Moon Phase [%s]"        { channel="astro:moon:home:phase#name" }
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
