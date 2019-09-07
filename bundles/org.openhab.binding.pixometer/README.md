# Pixometer Binding

_This binding connects to the pixometer api, which can manage your meter readings through a native smartphone app._

## Supported Things

This binding supports:

- Apiservice (bridge)

- Energymeter (thing)

- Gasmeter (thing)

- Watermeter (thing)

## Discovery

Currently the binding provides no discovery.
The desired Apiservice and Meters must be configured manually or via a things file.

## Binding Configuration

The binding has no configuration options itself, all configuration is done at 'Bridge' and 'Things' level.

## Thing Configuration

The Apiservice (bridge) needs to be configured with the personal User-Data (Username and Password) and the desired Refresh Interval (the time interval between meter-updates, default 12 hours, minimum 1 hour).

Each Meter needs to be configured with a ressource ID and the Apiservice to which it is linked.

## Channels

This binding introduces the channels *last_reading_value* and *last_reading_date* for the Meters.

| Channel ID         | Channel Description                                    | Supported item type | Advanced |
|--------------------|--------------------------------------------------------|---------------------|----------|
| last_reading_value | The last value that has been read for this meter.      | Number              | false    |
| last_reading_date  | The time at which the last reading value was recorded. | DateTime            | false    |
| last_refresh_date  | The last time that the current thing has been updated. | DateTime            | false    |

## Full Example

> Note: All usedata values and ressourceIDs are only examples!

pixometer.things:

```java
Bridge pixometer:apiservice:ApiserviceName "MyApiserviceName" [ user="xxxxxxxx@xxxx.xx", password="xxxxxxxxxxxx", refresh= 12 ] {
        Thing energymeter   MeterName1 "MyMeterName1" [ resource_id = "xxxxxxxx" ]
        Thing gasmeter      MeterName2 "MyMeterName2" [ resource_id = "xxxxxxxx" ]
        Thing watermeter    MeterName3 "MyMeterName3" [ resource_id = "xxxxxxxx" ]
}
```

pixometer.items:

```java
Number:Volume   Meter_Gas_ReadingValue              "[.3%f %unit%]"                     []  {channel="pixometer:gasmeter:apiservicename:metername1:last_reading_value"}
DateTime        Meter_Gas_LastReadingDate           "[%1$td.%1$tm.%1$tY %1$tH:%1$tM]"   []  {channel="pixometer:gasmeter:apiservicename:metername1:last_reading_date"}
Number:Energy   Meter_Electricity_ReadingValue      "[.3%f %unit%]"                     []  {channel="pixometer:energymeter:apiservicename:metername2:last_reading_value"}
DateTime        Meter_Electricity_LastReadingDate   "[%1$td.%1$tm.%1$tY %1$tH:%1$tM]"   []  {channel="pixometer:energymeter:apiservicename:metername2:last_reading_date"}
Number:Volume   Meter_Water_ReadingValue            "[.3%f %unit%]"                     []  {channel="pixometer:watermeter:apiservicename:metername3:last_reading_value"}
DateTime        Meter_Water_LastReadingDate         "[%1$td.%1$tm.%1$tY %1$tH:%1$tM]"   []  {channel="pixometer:watermeter:apiservicename:metername3:last_reading_date"}
```
