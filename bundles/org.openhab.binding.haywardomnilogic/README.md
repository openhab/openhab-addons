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

| Property             | Default                                                        | Required | Description                                  |
|----------------------|----------------------------------------------------------------|----------|----------------------------------------------|
| Host Name            | https://app1.haywardomnilogic.com/HAAPI/HomeAutomation/API.ash | Yes      | Host name of the Hayward API server          |
| User Name            | None                                                           | Yes      | Your Hayward User Name (not email address)   |
| Password             | None                                                           | Yes      | Your Hayward User Password                   |
| Telemetry Poll Delay | 12                                                             | Yes      | Telemetry Poll Delay (10-60 seconds)         |
| Alarm Poll Delay     | 60                                                             | Yes      | Alarm Poll Delay (0-120 seconds, 0 disabled) |

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
| chlorTimedPercent     | Number:Dimensionless | Chlorinator timed percent                                |     R/W    |
| chlorOperatingState   | Number               | Chlorinator operating state                              |      R     |
| chlorScMode           | String               | Chlorinator super chlorinate mode                        |      R     |
| chlorError            | Number               | Chlorinator error                                        |      R     |
| chlorAlert            | String               | Chlorinator alert                                        |      R     |
| chlorAvgSaltLevel     | Number:Dimensionless | Chlorinator average salt level in Part per Million (ppm) |      R     |
| chlorInstantSaltLevel | Number:Dimensionless | Chlorinator instant salt level in Part per Million (ppm) |      R     |
| chlorStatus           | Number               | Chlorinator K1/K2 relay status                           |      R     |

### Colorlogic Light Channels

| Channel Type ID            | Item Type | Description                   | Read Write |
|----------------------------|-----------|-------------------------------|:----------:|
| colorLogicLightEnable      | Switch    | Colorlogic Light enable       |     R/W    |
| colorLogicLightState       | String    | Colorlogic Light state        |      R     |
| colorLogicLightCurrentShow | String    | Colorlogic Light current show |     R/W    |

### Filter Channels

| Channel Type ID     | Item Type            | Description            | Read Write |
|---------------------|----------------------|------------------------|:----------:|
| filterEnable        | Switch               | Filter enable          |     R/W    |
| filterValvePosition | String               | Filter valve position  |      R     |
| filterSpeedPercent  | Number:Dimensionless | Filter speed in %      |     R/W    |
| filterSpeedRpm      | Number               | Filter speed in RPM    |     R/W    |
| filterSpeedSelect   | String               | Filter speed presets   |     R/W    |
| filterState         | String               | Filter state           |      R     |
| filterLastSpeed     | Number:Dimensionless | Filter last speed in % |      R     |

### Heater Channels

| Channel Type ID | Item Type | Description   | Read Write |
|-----------------|-----------|---------------|:----------:|
| heaterState     | String    | Heater state  |      R     |
| heaterEnable    | Switch    | Heater enable |      R     |

### Pump Channels

| Channel Type ID  | Item Type            | Description          | Read Write |
|------------------|----------------------|----------------------|:----------:|
| pumpEnable       | Switch               | Pump enable          |     R/W    |
| pumpSpeedPercent | Number:Dimensionless | Pump speed in %      |     R/W    |
| pumpSpeedRpm     | Number               | Pump speed in RPM    |     R/W    |
| pumpSpeedSelect  | String               | Pump speed presets   |     R/W    |
| pumpState        | String               | Pump state           |      R     |
| pumpLastSpeed    | Number:Dimensionless | Pump last speed in % |      R     |

### Relay Channels

| Channel Type ID | Item Type | Description | Read Write |
|-----------------|-----------|-------------|:----------:|
| relayState      | Switch    | Relay state |     R/W    |

### Virtual Heater Channels

| Channel Type ID       | Item Type          | Description             | Read Write |
|-----------------------|--------------------|-------------------------|:----------:|
| heaterEnable          | Switch             | Heater enable           |      R     |
| heaterCurrentSetpoint | Number:Temperature | Heater Current Setpoint |     R/W    |

## Full Example

After installing the binding, you will need to manually add the Hayward Connection thing and enter your credentials.
All pool items can be automatically discovered by scanning the bridge.  
Goto the inbox and add the things.
