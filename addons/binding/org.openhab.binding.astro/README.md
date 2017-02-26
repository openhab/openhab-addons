# Astro Binding

The Astro binding is used for calculating 
    * many DateTime and positional values for sun and moon.
    * Radiation levels (direct, diffuse and total) of the sun during the day

## Supported Things

This binding supports two Things: Sun and Moon

## Discovery

Discovery is not necessary, because all calculations are done within the binding.

## Binding Configuration

No binding configuration required.

## Thing Configuration

A thing requires the geolocation (latitude, longitude) for which the calculation is done.
Optionally, a refresh interval (in seconds) can be defined to also calculate positional data like azimuth and elevation.
An complementary altitude (optional) configuration item can also be specified to sharpen results provided by Radiation group.

## Channels

* **thing** `sun`
    * **group** `rise, set, noon, night, morningNight, astroDawn, nauticDawn, civilDawn, astroDusk, nauticDusk, civilDusk, eveningNight, daylight`
        * **channel** 
            * `start, end` (DateTime)
            * `duration` (Number)
    * **group** `position`
        * **channel** 
            * `azimuth, elevation` (Number)
    * **group** `radiation`
        * **channel** 
            * `direct, diffuse, total` (Number)
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
            * `name` (String), values: `SUN_RISE, ASTRO_DAWN, NAUTIC_DAWN, CIVIL_DAWN, CIVIL_DUSK, NAUTIC_DUSK, ASTRO_DUSK, SUN_SET, DAYLIGHT, NOON, NIGHT`
* **thing** `moon`
    * **group** `rise, set`
        * **channel** 
            * `start, end` (DateTime)
            * `duration` (Number), **Note:** start and end is always equal, duration always 0.
    * **group** `phase`
        * **channel**: 
            * `firstQuarter, thirdQuarter, full, new` (DateTime)
            * `age, illumination` (Number)
            * `name` (String), values: `NEW, WAXING_CRESCENT, FIRST_QUARTER, WAXING_GIBBOUS, FULL, WANING_GIBBOUS, THIRD_QUARTER, WANING_CRESCENT`
    * **group** `eclipse`
        * **channel**: 
            * `total, partial` (DateTime)
    * **group** `distance`
        * **channel**: 
            * `date` (DateTime)
            * `kilometer, miles` (Number)
    * **group** `perigee`
        * **channel**: 
            * `date` (DateTime), 
            * `kilometer, miles` (Number)
    * **group** `apogee`
        * **channel**: 
            * `date` (DateTime)
            * `kilometer, miles` (Number)
    * **group** `zodiac`
        * **channel** 
            * `sign` (String), values: `ARIES, TAURUS, GEMINI, CANCER, LEO, VIRGO, LIBRA, SCORPIO, SAGITTARIUS, CAPRICORN, AQUARIUS, PISCES`
    * **group** `position`
        * **channel** 
            * `azimuth, elevation` (Number)

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

**Offsets:** For each event group you can optionally configure an `offset` in minutes. The `offset` must be configured in the channel properties for the corresponding thing.

The minimum allowed offset is -1440 and the maximum allowed offset is 1440.

**Earliest/Latest:** For each trigger channel and `start`, `end` datetime value, you can optionally configure the `earliest` and `latest` time of the day.  

e.g `sun#rise earliest=18:00, latest=20:00`

sunrise is 17:40, but `earliest` is set to 18:00 so the event/datetime value is moved to 18:00.

OR

sunrise is 22:10 but `latest` is set to 20:00 so the event/datetime value is moved 20:00.

## Full Example

Things:

```
astro:sun:home  [ geolocation="xx.xxxxxx,xx.xxxxxx", altitude=100, interval=60 ]
astro:moon:home [ geolocation="xx.xxxxxx,xx.xxxxxx", interval=60 ]
```

or optionally with an event offset

```
astro:sun:home [ geolocation="xx.xxxxxx,xx.xxxxxx", altitude=100, interval=60 ] {
    Channels:
        Type rangeEvent : rise#event [
            offset=-30
        ]
}
astro:moon:home [ geolocation="xx.xxxxxx,xx.xxxxxx", interval=60 ]
```

or a datetime offset

```
astro:sun:home [ geolocation="xx.xxxxxx,xx.xxxxxx", altitude=100, interval=60 ] {
    Channels:
        Type start : rise#start [
            offset=5
        ]
        Type end : rise#end [
            offset=5
        ]
}
```

Items:

```
DateTime Sunrise_Time       "Sunrise [%1$tH:%1$tM]"  { channel="astro:sun:home:rise#start" }
DateTime Sunset_Time        "Sunset [%1$tH:%1$tM]"   { channel="astro:sun:home:set#start" }
Number   Azimuth            "Azimuth"                { channel="astro:sun:home:position#azimuth" }
Number   Elevation          "Elevation"              { channel="astro:sun:home:position#elevation" }
String   MoonPhase          "MoonPhase"              { channel="astro:moon:home:phase#name" }
Number   Total_Radiation    "Radiation"              { channel="astro:sun:home:radiation#total" }
Number   Total_Radiation    "Radiation"              { channel="astro:sun:home:radiation#total" }
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
