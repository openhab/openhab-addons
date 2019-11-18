# Pixometer Binding

This binding connects to the pixometer API, which can manage your meter readings through a native smartphone app.

## Supported Things

This binding supports:

- Apiservice (bridge)
- Energymeter (thing)
- Gasmeter (thing)
- Watermeter (thing)

## Thing Configuration

### Apiservice (bridge)

| Parameter    | Description                                                        | Required | Default Value    | Comment                                                       |
|--------------|--------------------------------------------------------------------|----------|------------------|---------------------------------------------------------------|
| user     |                                                                    | Yes      | -                |                                                               |
| password     |                                                                    | Yes      | -                |                                                               |
| refresh | Sets the refresh time. Minimum is 60 Minutes.                      | Yes      | 240              |                                                               |
| scope        | The scope (read or write) you want to grant openHAB for api usage. | Yes      | Read only access | Currently not used. Just prepared for a possible later usage. |
| access_token   | The currently used auth token.                                     | -        | -                |                                                               |

### Meter Things

| Parameter        | Description                                                                                                                                                                                         | Required |
|------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|
| resource_id     | The ID which represents the current meter. You can find it in the pixometer browser app while editing a specific meter. It should look like this: "https://pixometer.io/portal/#/meters/XXXXX/edit" | Yes      |

## Channels

All meter things have the following channels:

| Channel ID         | Channel Description                                    | Supported item type | Advanced |
|--------------------|--------------------------------------------------------|---------------------|----------|
| last_reading_value | The last value that has been read for this meter.      | Number              | false    |
| last_reading_date  | The time at which the last reading value was recorded. | DateTime            | false    |
| last_refresh_date  | The last time that the current thing has been updated. | DateTime            | false    |

## Full Example

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
