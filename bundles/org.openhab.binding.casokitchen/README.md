# CasoKitchen Binding

Provides access towards CASO Smart Kitchen devices which are connected within the [CASO Control App](https://www.casocontrol.de/).

## Supported Things

- `winecooler-2z`: Wine cooler with two zones

## Discovery

There's no automatic discovery.

## Thing Configuration

You need a [CASO Account](https://www.casoapp.com/Account/Create) to get configuration parameters.
After register you'll get the

- API key
- Device ID

## Wine Cooler with 2 Zones

### Configuration winecooler-2z

| Name            | Type    | Description                                          | Default |
|-----------------|---------|------------------------------------------------------|---------|
| apiKey          | text    | API obtained from thing configuration                | N/A     |
| deviceId        | text    | Device Id obtained from thing configuration          | N/A     |
| refreshInterval | integer | Interval the device is polled in minutes             | 5       |

### Channels winecooler-2z

Channels are separated in 3 groups

- `generic` group covering states for the whole device
- `top` and `bottom` group covering states related to top or bottom zone

#### Generic Group

Group name `generic`.

| Channel       | Type     | Read/Write | Description                  |
|---------------|----------|------------|------------------------------|
| light-switch  | Switch   | RW         | Control lights for all zones |
| last-update   | DateTime | R          | Date and Time of last update |
| hint          | String   | R          | General command description  |

#### Zone Groups

Group `top` and `bottom`.

The `set-temperature` channel is holding the desired temperature controlled via buttons on the wine cooler device.
Currently it cannot be changed using the API.

| Channel          | Type                  | Read/Write | Description                  |
|------------------|-----------------------|------------|------------------------------|
| power            | Switch                | R          | Zone Power                   |
| temperature      | Number:Temperature    | R          | Current Zone Temperature     |
| set-temperature  | Number:Temperature    | R          | Desired Zone Temperature     |
| light-switch     | Switch                | RW         | Control lights for this zone |

## Full Example

### Example Thing Configuration

```java
Thing       casokitchen:winecooler-2z:whiny           "Whiny Wine Cooler"        [ apiKey="ABC", deviceId="XYZ" ]
```

### Item Configuration

```java
Switch                  Whiny_Generic_LightSwitch           {channel="casokitchen:winecooler-2z:whiny:generic#light-switch" }
DateTime                Whiny_Generic_LastUpdate            {channel="casokitchen:winecooler-2z:whiny:generic#last-update" }
String                  Whiny_Generic_Hint                  {channel="casokitchen:winecooler-2z:whiny:generic#hint" }

Switch                  Whiny_Top_Power                     {channel="casokitchen:winecooler-2z:whiny:top#power" }
Number:Temperature      Whiny_Top_CurrentTemperature        {channel="casokitchen:winecooler-2z:whiny:top#temperature" }
Number:Temperature      Whiny_Top_DesiredTemperature        {channel="casokitchen:winecooler-2z:whiny:top#set-temperature" }
Switch                  Whiny_Top_LightSwitch               {channel="casokitchen:winecooler-2z:whiny:top#light-switch" }

Switch                  Whiny_Bottom_Power                  {channel="casokitchen:winecooler-2z:whiny:bottom#power" }
Number:Temperature      Whiny_Bottom_CurrentTemperature     {channel="casokitchen:winecooler-2z:whiny:bottom#temperature" }
Number:Temperature      Whiny_Bottom_DesiredTemperature     {channel="casokitchen:winecooler-2z:whiny:bottom#set-temperature" }
Switch                  Whiny_Bottom_LightSwitch            {channel="casokitchen:winecooler-2z:whiny:bottom#light-switch" }
```
