# Hayward Omnilogic Binding

The Hayward Omnilogic binding integrates the Omnilogic pool controller using the Hayward API.

The Hayward Omnilogic API interacts with Hayward's cloud server requiring a connection with the Internet for sending and receiving information.

## Supported Things

The table below lists the Hayward OmniLogic binding thing types:

| Things                       | Description                                                                     | Thing Type    |
|------------------------------|---------------------------------------------------------------------------------|---------------|
| Hayward OmniLogix Connection | Connection to Hayward's Server                                                  | bridge        |
| Backyard                     | Backyard                                                                        | backyard      |
| Body of Water                | Body of Water                                                                   | bow           |
| Chlorinator                  | Chlorinator                                                                     | chlorinator   |
| Colorlogic Light             | Colorlogic Light                                                                | colorlogic    |
| Filter                       | Filter control                                                                  | filter        |
| Heater Equipment             | Actual heater (i.e. gas, solar, electric)                                       | heater        |
| Pump                         | Auxillary pump control (i.e. spillover)                                         | pump          |
| Relay                        | Accessory relay control (deck jet sprinklers, lights, etc.)                     | relay         |
| Virtaul Heater               | A Virtual Heater that can control all of the heater equipment based on priority | virtualHeater |

## Discovery

The binding will automatically discover the Omnilogic pool things from the cloud server using your Hayward Omnilogic credentials.

## Thing Configuration

Hayward OmniLogic Connection Parameters:

| Property             | Default                                                          | Required | Description                                  |
|----------------------|------------------------------------------------------------------|----------|----------------------------------------------|
| Host Name            | <https://app1.haywardomnilogic.com/HAAPI/HomeAutomation/API.ash> | Yes      | Host name of the Hayward API server          |
| User Name            | None                                                             | Yes      | Your Hayward User Name (not email address)   |
| Password             | None                                                             | Yes      | Your Hayward User Password                   |
| Telemetry Poll Delay | 3                                                                | Yes      | Telemetry Poll Delay (2-60 seconds)          |
| Alarm Poll Delay     | 10                                                               | Yes      | Alarm Poll Delay (0-120 seconds, 0 disabled) |

## Channels

### Backyard Channels

| Channel Type ID | Item Type          | Description                      | Read Write |
|-----------------|--------------------|----------------------------------|:----------:|
| backyardAirTemp | Number:Temperature | Backyard air temp sensor reading |      R     |
| backyardStatus  | String             | Backyard status                  |      R     |
| backyardState   | String             | Backyard state                   |      R     |
| backyardAlarm1  | String             | Backyard alarm #1                |      R     |
| backyardAlarm2  | String             | Backyard alarm #2                |      R     |
| backyardAlarm3  | String             | Backyard alarm #3                |      R     |
| backyardAlarm4  | String             | Backyard alarm #4                |      R     |
| backyardAlarm5  | String             | Backyard alarm #5                |      R     |

### Body of Water Channels

| Channel Type ID | Item Type          | Description                        | Read Write |
|-----------------|--------------------|------------------------------------|:----------:|
| bowFlow         | Switch             | Body of Water flow sensor feedback |      R     |
| bowWaterTemp    | Number:Temperature | Body of Water temperature          |      R     |

### Chlorinator Channels

| Channel Type ID       | Item Type            | Description                                              | Read Write |
|-----------------------|----------------------|----------------------------------------------------------|:----------:|
| chlorEnable           | Switch               | Chlorinator enable                                       |     R/W    |
| chlorOperatingMode    | String               | Chlorinator operating mode                               |      R     |
| chlorTimedPercent     | Number:Dimensionless | Chlorinator salt output (%)                              |     R/W    |
| chlorOperatingState   | Number               | Chlorinator operating state                              |      R     |
| chlorScMode           | String               | Chlorinator super chlorinate mode                        |      R     |
| chlorError            | String               | Chlorinator error bit array                              |      R     |
| chlorAlert            | String               | Chlorinator alert bit array                              |      R     |
| chlorAvgSaltLevel     | Number:Dimensionless | Chlorinator average salt level in Part per Million (ppm) |      R     |
| chlorInstantSaltLevel | Number:Dimensionless | Chlorinator instant salt level in Part per Million (ppm) |      R     |
| chlorStatus           | String               | Chlorinator status bit array                             |      R     |

### Chlorinator Error Bit Array

|Bits  |Value                                                               |Description                    |
|------|--------------------------------------------------------------------|-------------------------------|
|1:0   |00 = OK<br>  01 = Short<br> 10 = Open                               |Current Sensor                 |
|3:2   |00 = OK<br>  01 = Short<br> 10 = Open                               |Voltage Sensor                 |
|5:4   |00 = OK<br>  01 = Short<br> 10 = Open                               |Cell Temp Sensor               |
|7:6   |00 = OK<br>  01 = Short<br> 10 = Open                               |Board Temp Sensor              |
|9:8   |00 = OK<br>  01 = Short<br> 10 = Open                               |K1 Relay                       |
|11:10 |00 = OK<br>  01 = Short<br> 10 = Open                               |K2 Relay                       |
|13:12 |00 = OK<br>  01 = Type<br> 10 = Authentication <br> 11 = Comm Loss  |Cell Errors                    |
|14    |0                                                                   |Aquarite PCB Error             |

### Chlorinator Alert Bit Array

|Bits  |Value                                                               |Description                    |
|------|--------------------------------------------------------------------|-------------------------------|
|1:0   |00 = OK<br>  01 = Salt Low<br> 10 = Salt too Low                    |Low salt                       |
|2     |0 = OK<br>  1 = High                                                |High Current                   |
|3     |0 = OK<br>  1 = Low                                                 |Low Voltage                    |
|5:4   |00 = OK<br>  01 = Low<br> 10 = Scaleback<br> 11 = High              |Cell Water Temp                |
|7:6   |00 = OK<br>  01 = High<br> 10 = Clearing                            |Board Temp                     |
|8     |0                                                                   |Not Used                       |
|10:9  |0                                                                   |Not Used                       |
|12:11 |00 = OK<br>  01 = Clean                                             |Cell Cleaning/Runtime          |

### Chlorinator Status Bit Array

|Bits  |Value                                                           |Description                    |
|------|----------------------------------------------------------------|-------------------------------|
|0     |0 = OK<br>1 = Error Present                                     |Error Present                  |
|1     |0 = OK<br>1 = Alert Present                                     |Alert Present                  |
|2     |0 = Standy<br>1 = Generating                                    |Generating                     |
|3     |0 = Not Paused<br>1 = Paused<br>                                |Paused                         |
|4     |0 = Local Not Paused<br>1 = Local Paused<br>                    |Local Pause                    |
|5     |0 = Not Authenticated<><BR>1 = Authenticated                    |T-Cell Authenticated           |
|6     |0 = K1 Relay Off<br> 1 = K1 Relay On                            |K1 Relay Active                |
|7     |0 = K2 Relay Off<br> 1 = K2 Relay On                            |K2 Relay Active                |

### Colorlogic Light Channels

| Channel Type ID            | Item Type | Description                   | Read Write |
|----------------------------|-----------|-------------------------------|:----------:|
| colorLogicLightEnable      | Switch    | Colorlogic Light enable       |     R/W    |
| colorLogicLightState       | String    | Colorlogic Light state        |      R     |
| colorLogicLightCurrentShow | String    | Colorlogic Light current show |     R/W    |
| colorLogicLightBrightness  | String    | Colorlogic Light brightness   |     R/W    |
| colorLogicLightSpeed       | String    | Colorlogic Light speed        |     R/W    |
**Brightness and speed channels only available on Hayward V2 lights

### Filter Channels

| Channel Type ID     | Item Type            | Description            | Read Write |
|---------------------|----------------------|------------------------|:----------:|
| filterEnable        | Switch               | Filter enable          |     R/W    |
| filterValvePosition | String               | Filter valve position  |      R     |
| filterSpeedPercent  | Number:Dimensionless | Filter speed (%)       |     R/W    |
| filterSpeedRpm      | Number:Frequency     | Filter speed (rpm)     |     R/W    |
| filterSpeedSelect   | String               | Filter speed presets   |     R/W    |
| filterState         | String               | Filter state           |      R     |
| filterLastSpeed     | Number:Dimensionless | Filter last speed (%)  |      R     |

### Heater Channels

| Channel Type ID | Item Type | Description   | Read Write |
|-----------------|-----------|---------------|:----------:|
| heaterState     | String    | Heater state  |      R     |
| heaterEnable    | Switch    | Heater enable |      R     |

### Pump Channels

| Channel Type ID  | Item Type            | Description          | Read Write |
|------------------|----------------------|----------------------|:----------:|
| pumpEnable       | Switch               | Pump enable          |     R/W    |
| pumpSpeedPercent | Number:Dimensionless | Pump speed (%)       |     R/W    |
| pumpSpeedRpm     | Number: Frequency    | Pump speed in rpm    |     R/W    |
| pumpSpeedSelect  | String               | Pump speed presets   |     R/W    |
| pumpState        | String               | Pump state           |      R     |
| pumpLastSpeed    | Number:Dimensionless | Pump last speed (%)  |      R     |

### Relay Channels

| Channel Type ID | Item Type | Description | Read Write |
|-----------------|-----------|-------------|:----------:|
| relayState      | Switch    | Relay state |     R/W    |

### Virtual Heater Channels

| Channel Type ID       | Item Type          | Description             | Read Write |
|-----------------------|--------------------|-------------------------|:----------:|
| heaterEnable          | Switch             | Heater enable           |      R     |
| heaterCurrentSetpoint | Number:Temperature | Heater Current Setpoint |     R/W    |

**Item Types Number:Dimensionless should have the units (i.e. %, ppm) defined in the Unit metadata

## Full Example

After installing the binding, you will need to manually add the Hayward Connection thing and enter your credentials.
All pool items can be automatically discovered by scanning the bridge.
Goto the inbox and add the things.
