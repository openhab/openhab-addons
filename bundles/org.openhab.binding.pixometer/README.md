# Pixometer Binding

This binding connects to the pixometer API, which can manage your meter readings through a native smartphone app.

## Supported Things

This binding supports the following thing types according to the capabilities of pixometer:

| Name        | Type   | Description                                                                 |
| ----------- | ------ | --------------------------------------------------------------------------- |
| Account     | Bridge | Representation of a pixometer account, which connects to the pixometer API. |
| Energymeter | Thing  | Provides access to the readings of configured energy meters.                |
| Gasmeter    | Thing  | Provides access to the readings of configured gas meters.                   |
| Watermeter  | Thing  | Provides access to the readings of configured water meters.                 |

The different meter types are pretty similar in basic, but are implemented in parallel to provide Units of Measurement support.

## Thing Configuration

### Account (bridge)

| Parameter | Description                                   | Required | Default Value | Comment |
| --------- | --------------------------------------------- | -------- | ------------- | ------- |
| user      |                                               | Yes      | -             |         |
| password  |                                               | Yes      | -             |         |
| refresh   | Sets the refresh time. Minimum is 60 Minutes. | Yes      | 240           |         |

### Meter Things

| Parameter  | Description                                                                                                                                                                                         | Required |
| ---------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------- |
| resourceId | The ID which represents the current meter. You can find it in the pixometer browser app while editing a specific meter. It should look like this: `https://pixometer.io/portal/#/meters/XXXXX/edit` | Yes      |

## Channels

All meter things have the following channels:

| Channel ID         | Channel Description                                    | Supported item type | Advanced |
| ------------------ | ------------------------------------------------------ | ------------------- | -------- |
| last_reading_value | The last value that has been read for this meter.      | Number              | false    |
| last_reading_date  | The time at which the last reading value was recorded. | DateTime            | false    |
| last_refresh_date  | The last time that the current thing has been updated. | DateTime            | false    |

## Full Example

pixometer.things:

```java
Bridge pixometer:account:AccountName "MyAccountName" [ user="xxxxxxxx@xxxx.xx", password="xxxxxxxxxxxx", refresh= 60 ] {
        Thing energymeter   MeterName1 "MyMeterName1" [ resourceId = "xxxxxxxx" ]
        Thing gasmeter      MeterName2 "MyMeterName2" [ resourceId = "xxxxxxxx" ]
        Thing watermeter    MeterName3 "MyMeterName3" [ resourceId = "xxxxxxxx" ]
}
```

pixometer.items:

```java
Number:Volume   Meter_Gas_ReadingValue              "[%.3f %unit%]"                     []  {channel="pixometer:gasmeter:accountname:metername1:last_reading_value"}
DateTime        Meter_Gas_LastReadingDate           "[%1$td.%1$tm.%1$tY %1$tH:%1$tM]"   []  {channel="pixometer:gasmeter:accountname:metername1:last_reading_date"}
Number:Energy   Meter_Electricity_ReadingValue      "[%.1f %unit%]"                     []  {channel="pixometer:energymeter:accountname:metername2:last_reading_value"}
DateTime        Meter_Electricity_LastReadingDate   "[%1$td.%1$tm.%1$tY %1$tH:%1$tM]"   []  {channel="pixometer:energymeter:accountname:metername2:last_reading_date"}
Number:Volume   Meter_Water_ReadingValue            "[%.3f %unit%]"                     []  {channel="pixometer:watermeter:accountname:metername3:last_reading_value"}
DateTime        Meter_Water_LastReadingDate         "[%1$td.%1$tm.%1$tY %1$tH:%1$tM]"   []  {channel="pixometer:watermeter:accountname:metername3:last_reading_date"}
```
