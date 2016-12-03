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
    Channel 'astro:sun:home:event#event' triggered RISE_START 
then
    ...
end
```

Available events:
* **thing** `sun`
    * `RISE_START, RISE_END, SET_START, SET_END, NOON_START, NOON_END, NIGHT_START, NIGHT_END, MORNING_NIGHT_START, MORNING_NIGHT_END, ASTRO_DAWN_START, ASTRO_DAWN_END, NAUTIC_DAWN_START, NAUTIC_DAWN_END, CIVIL_DAWN_START, CIVIL_DAWN_END, ASTRO_DUSK_START, ASTRO_DUSK_END, NAUTIC_DUSK_START, NAUTIC_DUSK_END, CIVIL_DUSK_START, CIVIL_DUSK_END, EVENING_NIGHT_START, EVENING_NIGHT_END, DAYLIGHT_START, DAYLIGHT_END, TOTAL_ECLIPSE, PARTIAL_ECLIPSE, RING_ECLIPSE, ZODIAC_ARIES_START, ZODIAC_ARIES_END, ZODIAC_TAURUS_START, ZODIAC_TAURUS_END, ZODIAC_GEMINI_START, ZODIAC_GEMINI_END, ZODIAC_CANCER_START, ZODIAC_CANCER_END, ZODIAC_LEO_START, ZODIAC_LEO_END, ZODIAC_VIRGO_START, ZODIAC_VIRGO_END, ZODIAC_LIBRA_START, ZODIAC_LIBRA_END, ZODIAC_SCORPIO_START, ZODIAC_SCORPIO_END, ZODIAC_SAGITTARIUS_START, ZODIAC_SAGITTARIUS_END, ZODIAC_CAPRICORN_START, ZODIAC_CAPRICORN_END, ZODIAC_AQUARIUS_START, ZODIAC_AQUARIUS_END, ZODIAC_PISCES_START, ZODIAC_PISCES_END, SEASON_SPRING, SEASON_SUMMER, SEASON_AUTUMN, SEASON_WINTER`
* **thing** `moon`
    * `RISE_START, SET_END, FIRST_QUARTER, THIRD_QUARTER, FULL, NEW, TOTAL_ECLIPSE, PARTIAL_ECLIPSE, PERIGEE, APOGEE`

**note**: delayed events not available, currently not supported by the openHab 2 framework

