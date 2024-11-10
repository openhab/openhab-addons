# Astro Binding

The Astro binding is used for calculating

- many DateTime and positional values for sun and moon.
- Radiation levels (direct, diffuse and total) of the sun during the day

## Supported Things

This binding supports two Things: Sun and Moon

## Discovery

If a system location is set, "Local Sun" and a "Local Moon" will be automatically discovered for this location.

If the system location is changed, the background discovery updates the configuration of "Local Sun" and "Local Moon" automatically.

## Binding Configuration

No binding configuration required.

## Thing Configuration

All Things require the parameter `geolocation` (as `<latitude>,<longitude>[,<altitude in m>]`) for which the calculation is done.
The altitude segment is optional and sharpens results provided by the Radiation group.
Optionally, a refresh `interval` (in seconds) can be defined to also calculate positional data like azimuth and elevation.

Season calculation can be switched from equinox based calculation to meteorological based (starting on the first day of the given month).
This is done by setting `useMeteorologicalSeason` to true in the advanced setting of the sun.

## Channels

- **thing** `sun`
  - **group** `rise, set, noon, night, morningNight, astroDawn, nauticDawn, civilDawn, astroDusk, nauticDusk, civilDusk, eveningNight, daylight`
    - **channel**
      - `start, end` (DateTime)
      - `duration` (Number:Time)
  - **group** `position`
    - **channel**
      - `azimuth, elevation` (Number:Angle)
      - `shadeLength` (Number)
  - **group** `radiation`
    - **channel**
      - `direct, diffuse, total` (Number:Intensity)
  - **group** `zodiac`
    - **channel**
      - `start, end` (DateTime)
      - `sign` (String), values: `ARIES, TAURUS, GEMINI, CANCER, LEO, VIRGO, LIBRA, SCORPIO, SAGITTARIUS, CAPRICORN, AQUARIUS, PISCES`
  - **group** `season`
    - **channel**:
      - `spring, summer, autumn, winter` (DateTime)
      - `name`,`nextName` (String), values `SPRING, SUMMER, AUTUMN, WINTER`
      - `timeLeft` (Number:Time)
  - **group** `eclipse`
    - **channel**:
      - `total, partial, ring` (DateTime)
      - `totalElevation, partialElevation, ringElevation` (Number:Angle)
  - **group** `phase`
    - **channel**
      - `name` (String), values: `SUN_RISE, ASTRO_DAWN, NAUTIC_DAWN, CIVIL_DAWN, CIVIL_DUSK, NAUTIC_DUSK, ASTRO_DUSK, SUN_SET, DAYLIGHT, NOON, NIGHT`
- **thing** `moon`
  - **group** `rise, set`
    - **channel**
      - `start, end` (DateTime)
  - **group** `phase`
    - **channel**:
      - `firstQuarter, thirdQuarter, full, new` (DateTime)
      - `age` (Number:Time)
      - `ageDegree` (Number:Angle)
      - `agePercent, illumination` (Number:Dimensionless)
      - `name` (String), values: `NEW, WAXING_CRESCENT, FIRST_QUARTER, WAXING_GIBBOUS, FULL, WANING_GIBBOUS, THIRD_QUARTER, WANING_CRESCENT`
  - **group** `eclipse`
    - **channel**:
      - `total, partial` (DateTime)
      - `totalElevation, partialElevation` (Number:Angle)
  - **group** `distance`
    - **channel**:
      - `date` (DateTime)
      - `distance` (Number:Length)
  - **group** `perigee`
    - **channel**:
      - `date` (DateTime),
      - `distance` (Number:Length)
  - **group** `apogee`
    - **channel**:
      - `date` (DateTime)
      - `distance` (Number:Length)
  - **group** `zodiac`
    - **channel**
      - `sign` (String), values: `ARIES, TAURUS, GEMINI, CANCER, LEO, VIRGO, LIBRA, SCORPIO, SAGITTARIUS, CAPRICORN, AQUARIUS, PISCES`
  - **group** `position`
    - **channel**
      - `azimuth, elevation` (Number:Angle)

### Trigger Channels

Only these can be used in rule triggers as shown below. Note that they have their own offset configurations that are independent from offsets configured on the start or end times of e.g. the `rise` or `set` channels.

- **thing** `sun`
  - **group** `rise, set, noon, night, morningNight, astroDawn, nauticDawn, civilDawn, astroDusk, nauticDusk, civilDusk, eveningNight, daylight`
    - **event** `START, END`
  - **group** `eclipse`
    - **event**: `TOTAL, PARTIAL, RING`
- **thing** `moon`
  - **group** `rise`
    - **event** `START`
  - **group** `set`
    - **event** `END`
  - **group** `phase`
    - **event**: `FIRST_QUARTER, THIRD_QUARTER, FULL, NEW`
  - **group** `eclipse`
    - **event**: `TOTAL, PARTIAL`
  - **group** `perigee`
    - **event**: `PERIGEE`
  - **group** `apogee`
    - **event**: `APOGEE`

### Channel config

**Offsets:** For each event group you can optionally configure an `offset` in minutes.
The `offset` must be configured in the channel properties for the corresponding thing.

The minimum allowed offset is -1440 and the maximum allowed offset is 1440.

**Earliest/Latest:** For each trigger channel and `start`, `end` datetime value, you can optionally configure the `earliest` and `latest` time of the day.

e.g `sun#set earliest=18:00, latest=20:00`

sunset is 17:40, but `earliest` is set to 18:00 so the event/datetime value is moved to 18:00.

OR

sunset is 22:10 but `latest` is set to 20:00 so the event/datetime value is moved 20:00.

**Force event:** For each trigger channel and `start`, `end` datetime value, you can force the `earliest`, `latest` time of the day, when the event is actually not taking place (e.g. astronomic dawn during summer in Sweden)
e.g `sun#astroDawn earliest=6:00, latest=20:00 forceEvent=true`

astronomic dawn start is null but `earliest` is set to 06:00 so the event/datetime value is set to 06:00.

## Full Example

Things:

```java
astro:sun:home  [ geolocation="52.5200066,13.4049540,100", interval=60 ]
astro:moon:home [ geolocation="52.5200066,13.4049540", interval=60 ]
```

or optionally with an event offset

```java
astro:sun:home [ geolocation="52.5200066,13.4049540,100", interval=60 ] {
    Channels:
        Type rangeEvent : rise#event [
            offset=-30
        ]
}
astro:moon:home [ geolocation="52.5200066,13.4049540", interval=60 ]
```

or a datetime offset

```java
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

or an offset and latest

```java
astro:sun:home [ geolocation="52.5200066,13.4049540,100", interval=60 ] {
    Channels:
        Type rangeEvent : rise#event [
            offset=-10,
            latest="08:00"
        ]
}
```

Items:

```java
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

```java
rule "example trigger rule"
when
    Channel 'astro:sun:home:rise#event' triggered START
then
    ...
end
```

## Rule Actions

Multiple actions are supported by this binding. In classic rules these are accessible as shown in the example below:

Getting sunActions variable in scripts

```java
 val sunActions = getActions("astro","astro:sun:local")
 if(null === sunActions) {
        logInfo("actions", "sunActions not found, check thing ID")
        return
 } else {
        // do something with sunActions
 }
```

### getEventTime(phaseName, date, moment)

Retrieves date and time (ZonedDateTime) of the requested phase name.
Thing method only applies to Sun thing type.

- `phaseName` (String), values: `SUN_RISE, ASTRO_DAWN, NAUTIC_DAWN, CIVIL_DAWN, CIVIL_DUSK, NAUTIC_DUSK, ASTRO_DUSK, SUN_SET, DAYLIGHT, NIGHT`. Mandatory.

- `date` (ZonedDateTime), only the date part of this parameter will be considered - defaulted to now() if null.

- `moment` (String), values: `START, END` - defaulted to `START` if null.

Example :

```java
 val sunEvent = "SUN_SET"
 val today = ZonedDateTime.now;
 val sunEventTime = sunActions.getEventTime(sunEvent,today,"START")
 logInfo("AstroActions","{} will happen at : {}", sunEvent, sunEventTime.toString)
```

### getElevation(timeStamp)

Retrieves the elevation (QuantityType\<Angle\>) of the sun at the requested instant.
Thing method applies to Sun and Moon.

- `timeStamp` (ZonedDateTime) - defaulted to now() if null.

### getAzimuth(timeStamp)

Retrieves the azimuth (QuantityType\<Angle\>) of the sun at the requested instant.
Thing method applies to Sun and Moon.

- `timeStamp` (ZonedDateTime) - defaulted to now() if null.

Example :

```java
 val azimuth = sunActions.getAzimuth(sunEventTime)
 val elevation = sunActions.getElevation(sunEventTime)
 logInfo("AstroActions", "{} will be positioned at elevation {} - azimuth {}",sunEvent, elevation.toString,azimuth.toString)
```

### getTotalRadiation(timeStamp)

Retrieves the total radiation (QuantityType\<Intensity\>) of the sun at the requested instant.
Thing method only applies to Sun thing type.

```java
 val totalRadiation = sunActions.getTotalRadiation(ZonedDateTime.now)
 logInfo("AstroActions", "Currently, the total sun radiation is {}", totalRadiation.toString)
```

## Tips

Do not worry if for example the "astro dawn" is undefined at your location.
The reason might be that you live in a northern country and it is summer, such that the sun is not 18 degrees below the horizon in the morning.
For details see [this Wikipedia article](https://en.wikipedia.org/wiki/Dawn).
The "civil dawn" event might often be the better choice.
