# Windcentrale Binding

This Binding is used to display the details of Windcentrale windmills.

## Supported Things

The binding supports the following Windcentrale Things:

| Thing Type | Description                               |
|------------|-------------------------------------------|
| account    | An account for using the Windcentrale API |
| windmill   | Windcentrale Windmill                     |

## Discovery

After creating an account Thing the Binding can discover windmills based on the participations linked to the account.

## Binding Configuration

No binding configuration required.

## Thing Configuration

### Account

| Configuration Parameter | Required |
|-------------------------|----------|
| username                | X        |
| password                | X        |

### Windmill

| Configuration Parameter | Required | Default          | Description                                         |
|-------------------------|----------|------------------|-----------------------------------------------------|
| name                    | X        | De Blauwe Reiger | Identifies the windmill (see names list below)      |
| shares                  |          | 1                | Number of wind shares ("Winddelen")                 |
| refreshInterval         |          | 30               | Refresh interval for refreshing the data in seconds |

The following windmill names are supported:

- De Blauwe Reiger
- De Boerenzwaluw
- De Bonte Hen
- De Grote Geert
- De Jonge Held
- De Ranke Zwaan
- De Trouwe Wachter
- De Vier Winden
- De Witte Juffer
- Het Rode Hert
- Het Vliegend Hert

## Channels

| Channel ID     | Item Type            | Description                         |
|----------------|----------------------|-------------------------------------|
| energy-total   | Number:Energy        | Total energy this year              |
| power-relative | Number:Dimensionless | Relative power                      |
| power-shares   | Number:Power         | Power provided for your wind shares |
| power-total    | Number:Power         | Total power                         |
| run-percentage | Number:Dimensionless | Run percentage this year            |
| run-time       | Number:Time          | Run time this year                  |
| timestamp      | DateTime             | Timestamp of the last update        |
| wind-direction | String               | Current wind direction              |
| wind-speed     | Number               | Measured current wind speed (Bft)   |

## Example

### `demo.things` Example

```java
Bridge windcentrale:account:demo-account [ username="johndoe@acme.com", password="Mf!BU45LTF6X2Cf36zxt" ] {
    Thing windmill    de-grote-geert      [ name="De Grote Geert" ]
    Thing windmill    de-blauwe-reiger    [ name="De Blauwe Reiger", shares=3, refreshInterval=60 ]
}
```

### `demo.items` Example

```java
Group                 gReiger                "Windcentrale Reiger"

Number                ReigerWindSpeed        "Wind speed [%d Bft]"                  (gReiger) { channel="windcentrale:windmill:demo-account:de-blauwe-reiger:wind-speed" }
String                ReigerWindDirection    "Wind direction [%s]"                  (gReiger) { channel="windcentrale:windmill:demo-account:de-blauwe-reiger:wind-direction" }
Number:Power          ReigerPowerTotal       "Total windmill power [%.1f %unit%]"   (gReiger) { channel="windcentrale:windmill:demo-account:de-blauwe-reiger:power-total" }
Number:Power          ReigerPowerShares      "Wind shares power [%.1f %unit%]"      (gReiger) { channel="windcentrale:windmill:demo-account:de-blauwe-reiger:power-shares" }
Number:Dimensionless  ReigerPowerRelative    "Relative power [%.1f %unit%]"         (gReiger) { channel="windcentrale:windmill:demo-account:de-blauwe-reiger:power-relative" }
Number:Energy         ReigerEnergyTotal      "Total windmill energy [%.0f %unit%]"  (gReiger) { channel="windcentrale:windmill:demo-account:de-blauwe-reiger:energy-total" }
Number:Dimensionless  ReigerRunPercentage    "Run percentage [%.1f %unit%]"         (gReiger) { channel="windcentrale:windmill:demo-account:de-blauwe-reiger:run-percentage" }
Number:Time           ReigerRunTime          "Run time [%.0f %unit%]"               (gReiger) { channel="windcentrale:windmill:demo-account:de-blauwe-reiger:run-time" }
DateTime              ReigerTimestamp        "Update timestamp [%1$ta %1$tR]"       (gReiger) { channel="windcentrale:windmill:demo-account:de-blauwe-reiger:timestamp" }
```
