# Windcentrale Binding

This Binding is used to display the details of a Windcentrale windmill.

## Supported Things

This Binding supports Windcentrale mill devices.

## Discovery

There is no discovery available for this binding.

## Binding Configuration

No binding configuration required.

## Thing Configuration

| Configuration Parameter | Required | Default | Description                                         |
|-------------------------|----------|---------|-----------------------------------------------------|
| millId                  | X        | 131     | Identifies the windmill (see table below)           |
| wd                      |          | 1       | Number of wind shares ("Winddelen")                 |
| refreshInterval         |          | 30      | Refresh interval for refreshing the data in seconds |

| millId | Windmill name     |
|--------|-------------------|
| 1      | De Grote Geert    |
| 2      | De Jonge Held     |
| 31     | Het Rode Hert     |
| 41     | De Ranke Zwaan    |
| 51     | De Witte Juffer   |
| 111    | De Bonte Hen      |
| 121    | De Trouwe Wachter |
| 131    | De Blauwe Reiger  |
| 141    | De Vier Winden    |
| 201    | De Boerenzwaluw   |

## Channels

| Channel Type ID | Item Type            | Description                         |
|-----------------|----------------------|-------------------------------------|
| kwh             | Number:Energy        | Current energy                      |
| kwhForecast     | Number:Energy        | Energy forecast                     |
| powerAbsTot     | Number:Power         | Total power                         |
| powerAbsWd      | Number:Power         | Power provided for your wind shares |
| powerRel        | Number:Dimensionless | Relative power                      |
| runPercentage   | Number:Dimensionless | Run percentage this year            |
| runTime         | Number:Time          | Run time this year                  |
| timestamp       | DateTime             | Timestamp of the last update        |
| windDirection   | String               | Current wind direction              |
| windSpeed       | Number               | Measured current wind speed (Bft)   |

## Example

### demo.things

```
Thing windcentrale:mill:geert  [ millId=1 ]
Thing windcentrale:mill:reiger [ millId=131, wd=3, refreshInterval=60 ]
```

### demo.items

```
Group                 gReiger                 "Windcentrale Reiger"              <wind>

Number                ReigerWindSpeed         "Wind speed [%d Bft]"              <wind>  (gReiger) { channel="windcentrale:mill:reiger:windSpeed" }
String                ReigerWindDirection     "Wind direction [%s]"              <wind>  (gReiger) { channel="windcentrale:mill:reiger:windDirection" }
Number:Power          ReigerPowerAbsTot       "Total mill power [%.1f %unit%]"   <wind>  (gReiger) { channel="windcentrale:mill:reiger:powerAbsTot" }
Number:Power          ReigerPowerAbsWd        "Wind shares power [%.1f %unit%]"  <wind>  (gReiger) { channel="windcentrale:mill:reiger:powerAbsWd" }
Number:Dimensionless  ReigerPowerRel          "Relative power [%.1f %unit%]"     <wind>  (gReiger) { channel="windcentrale:mill:reiger:powerRel" }
Number:Energy         ReigerKwh               "Current energy [%.0f %unit%]"     <wind>  (gReiger) { channel="windcentrale:mill:reiger:kwh" }
Number:Energy         ReigerKwhForecast       "Energy forecast [%.0f %unit%]"    <wind>  (gReiger) { channel="windcentrale:mill:reiger:kwhForecast" }
Number:Dimensionless  ReigerRunPercentage     "Run percentage [%.1f %unit%]"     <wind>  (gReiger) { channel="windcentrale:mill:reiger:runPercentage" }
Number:Time           ReigerRunTime           "Run time [%.0f %unit%]"           <wind>  (gReiger) { channel="windcentrale:mill:reiger:runTime" }
DateTime              ReigerTimestamp         "Update timestamp [%1$ta %1$tR]"   <wind>  (gReiger) { channel="windcentrale:mill:reiger:timestamp" }
```
