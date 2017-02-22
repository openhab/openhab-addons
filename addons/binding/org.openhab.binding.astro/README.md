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

**Offsets:** For each event group you can optionally configure an offset in minutes.
The offset must be configured in the channel properties for the corresponding thing.
The minimum allowed offset is -1440 and the maximum allowed offset is 1440.

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

## Full Example II (German)
Same Things as above.

Item:
```
//Astro
//Sun
DateTime ASTRO_SUNRISE_TIME              "Aufgang [%1$tH:%1$tM]"                                    <sunrise> { channel="astro:sun:home:rise#start" }
DateTime ASTRO_SUNSET_TIME               "Untergang [%1$tH:%1$tM]"                                  <sunset>  { channel="astro:sun:home:set#start" }
Number   ASTRO_AZIMUTH                   "Azimut"                                                   <sun>     { channel="astro:sun:home:position#azimuth" }
Number   ASTRO_ELEVATION                 "Höhe"                                                     <sun>     { channel="astro:sun:home:position#elevation" }
Number   ASTRO_TOTAL_RADIATION           "Radiation[%.2f]"                                          <sun>     { channel="astro:sun:home:radiation#total" }
DateTime ASTRO_SUNECLIPSE_TOTAL          "Totale Finsternis [%1$td.%1$tm.%1$ty %1$tH:%1$tM]"        <sun>     { channel="astro:sun:home:eclipse#total" }
DateTime ASTRO_SUNECLIPSE_PARTIAL        "Partielle Finsternis [%1$td.%1$tm.%1$ty %1$tH:%1$tM]"     <sun>     { channel="astro:sun:home:eclipse#partial" }
DateTime ASTRO_SUNECLIPSE_RING           "Ring Finsternis [%1$td.%1$tm.%1$ty %1$tH:%1$tM]"          <sun>     { channel="astro:sun:home:eclipse#ring" }

//Moon
String   ASTRO_MOONPHASE                 "Phase[MAP(moon.map):%s]"                                  <moon>    { channel="astro:moon:home:phase#name" }
DateTime ASTRO_MOONRISE_TIME             "Aufgang[%1$tH:%1$tM]"                                     <moon>    { channel="astro:moon:home:rise#start" }
DateTime ASTRO_MOONSET_TIME              "Untergang[%1$tH:%1$tM]"                                   <moon>    { channel="astro:moon:home:set#start" }
Number   ASTRO_MOON_AZIMUTH              "Azimut"                                                   <moon>    { channel="astro:moon:home:position#azimuth" }
Number   ASTRO_MOON_ELEVATION            "Höhe"                                                     <moon>    { channel="astro:moon:home:position#elevation" }
DateTime ASTRO_MOONECLIPSE_TOTAL         "Totale Finsternis[%1$td.%1$tm.%1$ty %1$tH:%1$tM]"         <moon>    { channel="astro:moon:home:eclipse#total" }
DateTime ASTRO_MOONECLIPSE_PARTIAL       "Partielle Finsternis[%1$td.%1$tm.%1$ty %1$tH:%1$tM]"      <moon>    { channel="astro:moon:home:eclipse#partial" }
Number   ASTRO_MOONPERIGEE_KM            "Perigäum[%.2f KM]"                                        <moon>    { channel="astro:moon:home:perigee#kilometer" }
DateTime ASTRO_MOONPERIGEE_DATE          "Perigäum[%1$td.%1$tm.%1$ty %1$tH:%1$tM]"                  <moon>    { channel="astro:moon:home:perigee#date" }
Number   ASTRO_MOONAPOGEE_KM             "Apogäum[%.2f KM]"                                         <moon>    { channel="astro:moon:home:apogee#kilometer" }
DateTime ASTRO_MOONAPOGEE_DATE           "Apogäum[%1$td.%1$tm.%1$ty %1$tH:%1$tM]"                   <moon>    { channel="astro:moon:home:apogee#date" }

//General
String   ASTRO_DAYTIME                   "Tageszeit [%s]"                                           <sun>     { channel="astro:sun:home:phase#name" }
String   ASTRO_ZODIAC                    "Sternzeichen[MAP(zodiac.map):%s]"                         <none>    { channel="astro:sun:home:zodiac#sign" }
DateTime ASTRO_ZODIAC_START              "Von[%1$td.%1$tm.%1$ty]"                                   <none>    { channel="astro:sun:home:zodiac#start" }
DateTime ASTRO_ZODIAC_END                "Bis[%1$td.%1$tm.%1$ty]"                                   <none>    { channel="astro:sun:home:zodiac#end" }
String   ASTRO_SEASON                    "Jahreszeit[MAP(season.map):%s]"                           <none>    { channel="astro:sun:home:season#name" }
```

Sitemap:
```
Text label="Astronomische Daten" icon="sun" {
    Frame label="Sonne" {
        Text item=ASTRO_SUNRISE_TIME
        Text item=ASTRO_SUNSET_TIME
        Text item=ASTRO_AZIMUTH
        Text item=ASTRO_ELEVATION
        Text item=ASTRO_TOTAL_RADIATION
        Text item=ASTRO_SUNECLIPSE_RING
        Text item=ASTRO_SUNECLIPSE_PARTIAL
        Text item=ASTRO_SUNECLIPSE_TOTAL
    }
    Frame label="Mond" {
        Text item=ASTRO_MOONRISE_TIME
        Text item=ASTRO_MOONSET_TIME
        Text item=ASTRO_MOON_AZIMUTH
        Text item=ASTRO_MOON_ELEVATION
        Text item=ASTRO_MOONPERIGEE_DATE
        Text item=ASTRO_MOONPERIGEE_KM
        Text item=ASTRO_MOONAPOGEE_DATE
        Text item=ASTRO_MOONAPOGEE_KM
        Text item=ASTRO_MOONECLIPSE_PARTIAL
        Text item=ASTRO_MOONECLIPSE_TOTAL
        Text item=ASTRO_MOONPHASE
    }
    Frame label="Sonstiges" {
        Text item=ASTRO_DAYTIME
        Text item=ASTRO_ZODIAC
        Text item=ASTRO_ZODIAC_START
        Text item=ASTRO_ZODIAC_END
        Text item=ASTRO_SEASON
    }
}
```

Transformation definitons:

season.map
```
SPRING=Frühling
SUMMER=Sommer
AUTUMN=Herbst
WINTER=Winter
```

zodiac.map
```
ARIES=Widder
TAURUS=Stier
GEMINI=Zwilling
CANCER=Krebs
LEO=Löwe
VIRGO=Jungfrau
LIBRA=Waage
SCORPIO=Skorpion
SAGITTARIUS=Schütze
CAPRICORN=Steinbock
AQUARIUS=Wassermann
PISCES=Fisch
```
