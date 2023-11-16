# Miele@home Binding

This binding integrates Miele@home appliances.
Miele@home allows controlling Miele appliances that are equipped with special communication modules.
There are devices that communicate through Zigbee and others that use WiFi.

See [www.miele.de](https://www.miele.de) for the list of available appliances.

## Supported Things

This binding requires the XGW3000 gateway from Miele as all integration with openHAB is done through this gateway.
While users with Zigbee-enabled Miele appliances usually own such a gateway, this is often not the case for people that have only WiFi-enabled appliances.

The types of appliances that are supported by this binding are:

- Coffeemachine
- Dishwasher
- Fridge
- Fridge/Freezer combination
- Hob
- Hood
- Oven
- Microwave/Oven combination
- Tumbledryer
- Washingmachine

## Discovery

The binding is able to auto-discover the Miele XGW3000 gateway.
When an XGW3000 gateway is discovered, all appliances can be subsequently discovered.

### Note on Discovery

The XGW3000 gateway is sometimes a few seconds late in re-announcing itself on the network.
This means that it might repeatedly disappear from, and re-appear in, the Inbox.
To avoid this, there is a discovery configuration parameter `removalGracePeriod` which delays such Inbox disappearances.
The default value is 15 seconds.
If you want to change this value just add the following line to your `$OPENHAB_CONF/services/runtime.cfg` file.

```text
discovery.miele:removalGracePeriod=30
```

## Thing Configuration

### Thing Configuration for Miele XGW3000

| Configuration Parameter | Description   |
|-------------------------|---------------|
| ipAddress               | Network address of the Miele@home gateway |
| interface               | Network address of openHAB host interface where the binding will listen for multicast events coming from the Miele@home gateway. |
| userName                | Name of a registered Miele@home user. |
| password                | Password for the registered Miele@home user. |
| language                | Language for state, program and phase texts. Leave blank for system language. |

### Thing Configuration for appliance

| Configuration Parameter | Description   |
|-------------------------|---------------|
| uid                     | Unique identifier for specific appliance on the gateway. |

Each appliance needs the device UID as a configuration parameter.
The UID is nowhere to be found on the appliances, but since the discovery works quite reliably, a manual configuration is not needed.

## Channels

### Raw values

Some channels represent raw/numeric values for state, program and phase, namely rawState, rawProgram and rawPhase.
These channels are more reliable as rule triggers/logic than using their text-based counterparts. Raw values are
always available from the gateway even when texts are missing. Only a subset of available raw values are documented
here since no official documentation exists.

#### State

| State | Description                  | Appliances                                                      |
|-------|------------------------------|-----------------------------------------------------------------|
| 0     | Unknown                      | All                                                             |
| 1     | Off                          | All                                                             |
| 2     | Stand-By                     | All                                                             |
| 3     | Programmed                   | Coffee Machine, Dishwasher, Oven, Tumble Dryer, Washing Machine |
| 4     | Waiting to Start             | Coffee Machine, Dishwasher, Oven, Tumble Dryer, Washing Machine |
| 5     | Running                      | All                                                             |
| 6     | Paused                       | Coffee Machine, Dishwasher, Oven, Tumble Dryer, Washing Machine |
| 7     | End                          | Coffee Machine, Dishwasher, Oven, Tumble Dryer, Washing Machine |
| 8     | Failure                      | All                                                             |
| 9     | Abort                        | Coffee Machine, Dishwasher, Oven, Tumble Dryer, Washing Machine |
| 10    | Idle                         | All                                                             |
| 11    | Rinse Hold                   | Washing Machine, Tumble Dryer                                   |
| 12    | Service                      | All                                                             |
| 13    | Super Freezing               | Fridge/Freezer combination                                      |
| 14    | Super Cooling                | Fridge, Fridge/Freezer combination                              |
| 15    | Super Heating                | Hob                                                             |
| 144   | Default                      | All                                                             |
| 145   | Locked                       | All                                                             |
| 146   | Super Cooling/Super Freezing | Fridge/Freezer combination                                      |
| 255   | Not Connected                | All                                                             |

### Appliances

Channels available for each appliance type are listed below.

#### Coffee Machine

| Channel             | Type                 | Read/write | Description                                                         |
|---------------------|----------------------|------------|---------------------------------------------------------------------|
| state               | String               | Read       | Current status of the appliance                                     |
| rawState            | Number               | Read       | Current status of the appliance as raw number                       |
| program             | String               | Read       | Current program or function running on the appliance                |
| rawProgram          | Number               | Read       | Current program or function running on the appliance as raw number  |
| type                | String               | Read       | Type of the program running on the appliance                        |
| phase               | String               | Read       | Current phase of the program running on the appliance               |
| rawPhase            | Number               | Read       | Current phase of the program running on the appliance as raw number |
| door                | Contact              | Read       | Current state of the door of the appliance                          |
| switch              | Switch               | Write      | Switch the appliance on or off                                      |

#### Dishwasher

| Channel             | Type                 | Read/write | Description                                                          |
|---------------------|----------------------|------------|----------------------------------------------------------------------|
| state               | String               | Read       | Current status of the appliance                                      |
| rawState            | Number               | Read       | Current status of the appliance as raw number                        |
| program             | String               | Read       | Current program or function running on the appliance                 |
| rawProgram          | Number               | Read       | Current program or function running on the appliance as raw number   |
| phase               | String               | Read       | Current phase of the program running on the appliance                |
| rawPhase            | Number               | Read       | Current phase of the program running on the appliance as raw number  |
| start               | DateTime             | Read       | Programmed start time of the program                                 |
| end                 | DateTime             | Read       | End time of the program (programmed or running)                      |
| duration            | Number:Time          | Read       | Duration of the program running on the appliance                     |
| elapsed             | Number:Time          | Read       | Time elapsed in the program running on the appliance                 |
| finish              | Number:Time          | Read       | Time to finish the program running on the appliance                  |
| door                | Contact              | Read       | Current state of the door of the appliance                           |
| switch              | Switch               | Write      | Switch the appliance on or off                                       |
| energyConsumption   | Number:Energy        | Read       | Energy consumption by the currently running program on the appliance |
| waterConsumption    | Number:Volume        | Read       | Water consumption by the currently running program on the appliance  |

##### Programs

| Program | Description                         |
|---------|-------------------------------------|
| 26      | Intensive                           |
| 27      | Maintenance programme               |
| 28      | ECO                                 |
| 30      | Normal                              |
| 32      | Automatic                           |
| 34      | SolarSave                           |
| 35      | Gentle                              |
| 36      | Extra Quiet                         |
| 37      | Hygiene                             |
| 38      | QuickPowerWash                      |
| 42      | Tall items                          |

##### Phases

| Phase | Legacy | Description                  |
|-------|--------|------------------------------|
| 1792  | 0      | None (appliance off)         |
| 1794  | 2      | Pre-Wash                     |
| 1795  | 3      | Main Wash                    |
| 1796  | 4      | Rinses                       |
| 1798  | 6      | Final rinse                  |
| 1799  | 7      | Drying                       |
| 1800  | 8      | Finished                     |

#### Fridge

| Channel             | Type                 | Read/write | Description                                                         |
|---------------------|----------------------|------------|---------------------------------------------------------------------|
| state               | String               | Read       | Current status of the appliance                                     |
| rawState            | Number               | Read       | Current status of the appliance as raw number                       |
| supercool           | Switch               | Read/Write | Start or stop Super Cooling                                         |
| current             | Number:Temperature   | Read       | Current temperature in the fridge                                   |
| target              | Number:Temperature   | Read       | Target temperature to be reached by the fridge                      |
| door                | Contact              | Read       | Current state of the door of the appliance                          |
| switch              | Switch               | Write      | Switch the appliance on or off                                      |

#### Fridge/Freezer combination

| Channel             | Type                 | Read/write | Description                                                         |
|---------------------|----------------------|------------|---------------------------------------------------------------------|
| state               | String               | Read       | Current status of the appliance                                     |
| rawState            | Number               | Read       | Current status of the appliance as raw number                       |
| freezerstate        | String               | Read       | Current status of the freezer compartment                           |
| fridgestate         | String               | Read       | Current status of the fridge compartment                            |
| supercool           | Switch               | Read/Write | Start or stop Super Cooling                                         |
| superfreeze         | Switch               | Read/Write | Start or stop Super Freezing                                        |
| freezercurrent      | Number:Temperature   | Read       | Current temperature in the freezer compartment                      |
| freezertarget       | Number:Temperature   | Read       | Target temperature to be reached by the freezer compartment         |
| fridgecurrent       | Number:Temperature   | Read       | Current temperature in the fridge compartment                       |
| fridgetarget        | Number:Temperature   | Read       | Target temperature to be reached by the fridge compartment          |
| door                | Contact              | Read       | Current state of the door of the appliance                          |
| start               | Switch               | Write      | Switch the appliance on or off                                      |

#### Hob

| Channel             | Type                 | Read/write | Description                                                         |
|---------------------|----------------------|------------|---------------------------------------------------------------------|
| state               | String               | Read       | Current status of the appliance                                     |
| rawState            | Number               | Read       | Current status of the appliance as raw number                       |
| plate1power         | Number               | Read       | Power level of heating zone/plate 1                                 |
| plate1heat          | Number               | Read       | Remaining heat level of heating zone/plate 1                        |
| plate1time          | String               | Read       | Remaining time of heating zone/plate 1                              |
| plate2power         | Number               | Read       | Power level of heating zone/plate 2                                 |
| plate2heat          | Number               | Read       | Remaining heat level of heating zone/plate 2                        |
| plate2time          | String               | Read       | Remaining time of heating zone/plate 2                              |
| plate3power         | Number               | Read       | Power level of heating zone/plate 3                                 |
| plate3heat          | Number               | Read       | Remaining heat level of heating zone/plate 3                        |
| plate3time          | String               | Read       | Remaining time of heating zone/plate 3                              |
| plate4power         | Number               | Read       | Power level of heating zone/plate 4                                 |
| plate4heat          | Number               | Read       | Remaining heat level of heating zone/plate 4                        |
| plate4time          | String               | Read       | Remaining time of heating zone/plate 4                              |
| plate5power         | Number               | Read       | Power level of heating zone/plate 5                                 |
| plate5heat          | Number               | Read       | Remaining heat level of heating zone/plate 5                        |
| plate5time          | String               | Read       | Remaining time of heating zone/plate 5                              |
| plate6power         | Number               | Read       | Power level of heating zone/plate 6                                 |
| plate6heat          | Number               | Read       | Remaining heat level of heating zone/plate 6                        |
| plate6time          | String               | Read       | Remaining time of heating zone/plate 6                              |

#### Hood

| Channel             | Type                 | Read/write | Description                                                         |
|---------------------|----------------------|------------|---------------------------------------------------------------------|
| state               | String               | Read       | Current status of the appliance                                     |
| rawState            | Number               | Read       | Current status of the appliance as raw number                       |
| light               | Switch               | Write      | Switch the appliance on or off                                      |
| ventilation         | Number               | Read       | Current ventilation power                                           |
| stop                | Switch               | Write      | Stop the appliance                                                  |

#### Oven

| Channel             | Type                 | Read/write | Description                                                         |
|---------------------|----------------------|------------|---------------------------------------------------------------------|
| state               | String               | Read       | Current status of the appliance                                     |
| rawState            | Number               | Read       | Current status of the appliance as raw number                       |
| program             | String               | Read       | Current program or function running on the appliance                |
| rawProgram          | Number               | Read       | Current program or function running on the appliance as raw number  |
| type                | String               | Read       | Type of the program running on the appliance                        |
| phase               | String               | Read       | Current phase of the program running on the appliance               |
| rawPhase            | Number               | Read       | Current phase of the program running on the appliance as raw number |
| start               | DateTime             | Read       | Programmed start time of the program                                |
| end                 | DateTime             | Read       | End time of the program (programmed or running)                     |
| duration            | Number:Time          | Read       | Duration of the program running on the appliance                    |
| elapsed             | Number:Time          | Read       | Time elapsed in the program running on the appliance                |
| finish              | Number:Time          | Read       | Time to finish the program running on the appliance                 |
| target              | Number:Temperature   | Read       | Target temperature to be reached by the oven                        |
| measured            | Number:Temperature   | Read       | Actual measured temperature in the oven                             |
| temp1               | Number:Temperature   | Read       | Program temperature in the oven 1                                   |
| temp2               | Number:Temperature   | Read       | Program temperature in the oven 2                                   |
| door                | Contact              | Read       | Current state of the door of the appliance                          |
| switch              | Switch               | Write      | Switch the appliance on or off                                      |
| stop                | Switch               | Write      | Stop the appliance                                                  |

##### Phases

| Phase | Legacy | Description                  |
|-------|--------|------------------------------|
| 3072  | 0      | None (appliance off)         |
| 3073  | 1      | Heating                      |
| 3074  | 2      | Temp. hold                   |
| 3075  | 3      | Door Open                    |
| 3076  | 4      | Pyrolysis                    |
| 3079  | 7      | Lighting                     |
| 3080  | 8      | Searing phase                |
| 3082  | 10     | Defrost                      |
| 3083  | 11     | Cooling down                 |
| 3084  | 12     | Energy save phase            |

#### Microwave/Oven combination

See oven.

#### Tumble Dryer

| Channel             | Type                 | Read/write | Description                                                         |
|---------------------|----------------------|------------|---------------------------------------------------------------------|
| state               | String               | Read       | Current status of the appliance                                     |
| rawState            | Number               | Read       | Current status of the appliance as raw number                       |
| program             | String               | Read       | Current program or function running on the appliance                |
| rawProgram          | Number               | Read       | Current program or function running on the appliance as raw number  |
| type                | String               | Read       | Type of the program running on the appliance                        |
| phase               | String               | Read       | Current phase of the program running on the appliance               |
| rawPhase            | Number               | Read       | Current phase of the program running on the appliance as raw number |
| start               | DateTime             | Read       | Programmed start time of the program                                |
| end                 | DateTime             | Read       | End time of the program (programmed or running)                     |
| duration            | Number:Time          | Read       | Duration of the program running on the appliance                    |
| elapsed             | Number:Time          | Read       | Time elapsed in the program running on the appliance                |
| finish              | Number:Time          | Read       | Time to finish the program running on the appliance                 |
| door                | Contact              | Read       | Current state of the door of the appliance                          |
| switch              | Switch               | Write      | Switch the appliance on or off                                      |
| step                | Number               | Read       | Current step in the program running on the appliance                |

##### Programs

| Program | Description                         |
|---------|-------------------------------------|
| 10      | Automatic Plus                      |
| 20      | Cottons                             |
| 23      | Cottons hygiene                     |
| 30      | Minimum iron                        |
| 31      | Gentle minimum iron                 |
| 40      | Woollens handcare                   |
| 50      | Delicates                           |
| 60      | Warm Air                            |
| 70      | Cool air                            |
| 80      | Express                             |
| 90      | Cottons                             |
| 100     | Gentle smoothing                    |
| 120     | Proofing                            |
| 130     | Denim                               |
| 131     | Gentle denim                        |
| 140     | Shirts                              |
| 141     | Gentle shirts                       |
| 150     | Sportswear                          |
| 160     | Outerwear                           |
| 170     | Silks handcare                      |
| 190     | Standard pillows                    |
| 220     | Basket programme                    |
| 240     | Smoothing                           |
| 65000   | Cottons (auto load control)         |
| 65001   | Minimum iron (auto load control)    |

##### Phases

| Phase | Legacy | Description                  |
|-------|--------|------------------------------|
| 512   | 0      | None (appliance off)         |
| 513   | 1      | Programme running            |
| 514   | 2      | Drying                       |
| 515   | 3      | Drying Machine iron          |
| 516   | 4      | Drying Hand iron (2)         |
| 517   | 5      | Drying Normal                |
| 518   | 6      | Drying Normal+               |
| 519   | 7      | Cooling down                 |
| 520   | 8      | Drying Hand iron (1)         |
| 522   | 10     | Finished                     |

#### Washing Machine

| Channel             | Type                 | Read/write | Description                                                          |
|---------------------|----------------------|------------|----------------------------------------------------------------------|
| state               | String               | Read       | Current status of the appliance                                      |
| rawState            | Number               | Read       | Current status of the appliance as raw number                        |
| program             | String               | Read       | Current program or function running on the appliance                 |
| rawProgram          | Number               | Read       | Current program or function running on the appliance as raw number   |
| type                | String               | Read       | Type of the program running on the appliance                         |
| phase               | String               | Read       | Current phase of the program running on the appliance                |
| rawPhase            | Number               | Read       | Current phase of the program running on the appliance as raw number  |
| start               | DateTime             | Read       | Programmed start time of the program                                 |
| end                 | DateTime             | Read       | End time of the program (programmed or running)                      |
| duration            | Number:Time          | Read       | Duration of the program running on the appliance                     |
| elapsed             | Number:Time          | Read       | Time elapsed in the program running on the appliance                 |
| finish              | Number:Time          | Read       | Time to finish the program running on the appliance                  |
| door                | Contact              | Read       | Current state of the door of the appliance                           |
| switch              | Switch               | Write      | Switch the appliance on or off                                       |
| target              | Number:Temperature   | Read       | Temperature of the selected program (10 °C = cold)                   |
| spinningspeed       | String               | Read       | Spinning speed in the program running on the appliance               |
| energyConsumption   | Number:Energy        | Read       | Energy consumption by the currently running program on the appliance |
| waterConsumption    | Number:Volume        | Read       | Water consumption by the currently running program on the appliance  |

##### Programs

| Program | Description                         |
|---------|-------------------------------------|
| 1       | Cottons                             |
| 3       | Minimum iron                        |
| 4       | Delicates                           |
| 8       | Woollens                            |
| 9       | Silks                               |
| 17      | Starch                              |
| 18      | Rinse                               |
| 21      | Drain/Spin                          |
| 22      | Curtains                            |
| 23      | Shirts                              |
| 24      | Denim                               |
| 27      | Proofing                            |
| 29      | Sportswear                          |
| 31      | Automatic Plus                      |
| 37      | Outerwear                           |
| 39      | Pillows                             |
| 50      | Dark Garments                       |
| 53      | First wash                          |
| 75      | Steam care                          |
| 76      | Freshen up                          |
| 91      | Maintenance wash                    |
| 95      | Down duvets                         |
| 122     | Express 20                          |
| 129     | Down filled items                   |
| 133     | Cottons Eco                         |
| 146     | QuickPowerWash                      |
| 65532   | Mix                                 |

##### Phases

| Phase | Legacy | Description                  |
|-------|--------|------------------------------|
| 256   | 0      | None (appliance off)         |
| 257   | 1      | Pre-wash                     |
| 260   | 4      | Washing                      |
| 261   | 5      | Rinses                       |
| 263   | 7      | Clean                        |
| 265   | 9      | Drain                        |
| 266   | 10     | Spin                         |
| 267   | 11     | Anti-crease                  |
| 268   | 12     | Finished                     |

# Configuration Examples

## things/miele.things

```java
Bridge miele:xgw3000:home [ipAddress="192.168.0.18", interface="192.168.0.5"] {
    Things:
        Thing fridgefreezer freezer [uid="00124b000424be44#2"]
        Thing hood hood [uid="001d63fffe020685#210"]
        Thing fridge fridge [uid="00124b000424bdc0#2"]
        Thing oven oven [uid="001d63fffe020390#210"]
        Thing oven microwave [uid="001d63fffe0206eb#210"]
        Thing hob hob [uid="00124b000424bed7#2"]
        Thing dishwasher dishwasher [uid="001d63fffe020683#210"]
        Thing tumbledryer tumbledryer [uid="001d63fffe0200ba#210"]
        Thing washingmachine washingmachine [uid="001d63fffe020505#210"]
        Thing coffeemachine coffeemachine [uid="001d63fffe020505#190"]
}
```

## items/miele.items

```java
String Dishwasher_State                                       {channel="miele:dishwasher:home:dishwasher:state"}
Number Dishwasher_RawState                                    {channel="miele:dishwasher:home:dishwasher:rawState"}
String Dishwasher_Program "Program [%s]"                      {channel="miele:dishwasher:home:dishwasher:program"}
String Dishwasher_Phase "Phase [%s]"                          {channel="miele:dishwasher:home:dishwasher:phase"}
Number:Time Dishwasher_ElapsedTime "Elapsed time" <time>      {channel="miele:dishwasher:home:dishwasher:elapsed"}
Number:Time Dishwasher_FinishTime "Remaining time" <time>     {channel="miele:dishwasher:home:dishwasher:finish"}
Number:Energy Dishwasher_EnergyConsumption                    {channel="miele:dishwasher:home:dishwasher:energyConsumption"}
Number:Volume Dishwasher_WaterConsumption                     {channel="miele:dishwasher:home:dishwasher:waterConsumption"}

String Fridge_State                                           {channel="miele:fridge:home:fridge:state"}
Contact Fridge_Door                                           {channel="miele:fridge:home:fridge:door"}
Switch Fridge_SuperCool                                       {channel="miele:fridge:home:fridge:supercool"}
Number:Temperature Fridge_CurrentTemperature <temperature>    {channel="miele:fridge:home:fridge:current"}
Number:Temperature Fridge_TargetTemperature  <temperature>    {channel="miele:fridge:home:fridge:target"}
Switch Fridge_Start                                           {channel="miele:fridge:home:fridge:start"}

String Oven_State                                             {channel="miele:oven:home:oven:state"}
Number Oven_RawState                                          {channel="miele:oven:home:oven:rawState"}
String Oven_Program "Program [%s]"                            {channel="miele:oven:home:oven:program"}
String Oven_Phase "Phase [%s]"                                {channel="miele:oven:home:oven:phase"}
Number:Time Oven_ElapsedTime "Elapsed time" <time>            {channel="miele:oven:home:oven:elapsed"}
Number:Time Oven_FinishTime "Remaining time" <time>           {channel="miele:oven:home:oven:finish"}
Number:Temperature Oven_CurrentTemperature <temperature>      {channel="miele:oven:home:oven:measured"}
Number:Temperature Oven_TargetTemperature <temperature>       {channel="miele:oven:home:oven:target"}
Switch Oven_Stop                                              {channel="miele:oven:home:oven:stop"}

String WashingMachine_State                                   {channel="miele:washingmachine:home:washingmachine:state"}
Number WashingMachine_RawState                                {channel="miele:washingmachine:home:washingmachine:rawState"}
String WashingMachine_Program "Program [%s]"                  {channel="miele:washingmachine:home:washingmachine:program"}
String WashingMachine_Phase "Phase [%s]"                      {channel="miele:washingmachine:home:washingmachine:phase"}
Number:Temperature WashingMachine_Temperature <temperature>   {channel="miele:washingmachine:home:washingmachine:target"}
String WashingMachine_SpinningSpeed                           {channel="miele:washingmachine:home:washingmachine:spinningspeed"}
Number:Time WashingMachine_ElapsedTime "Elapsed time" <time>  {channel="miele:washingmachine:home:washingmachine:elapsed"}
Number:Time WashingMachine_FinishTime "Remaining time" <time> {channel="miele:washingmachine:home:washingmachine:finish"}
Number:Energy WashingMachine_EnergyConsumption                {channel="miele:washingmachine:home:washingmachine:energyConsumption"}
Number:Volume WashingMachine_WaterConsumption                 {channel="miele:washingmachine:home:washingmachine:waterConsumption"}

String TumbleDryer_State                                      {channel="miele:tumbledryer:home:tumbledryer:state"}
Number TumbleDryer_RawState                                   {channel="miele:tumbledryer:home:tumbledryer:rawState"}
String TumbleDryer_Program "Program [%s]"                     {channel="miele:tumbledryer:home:tumbledryer:program"}
String TumbleDryer_Phase "Phase [%s]"                         {channel="miele:tumbledryer:home:tumbledryer:phase"}
Number:Time TumbleDryer_ElapsedTime "Elapsed time" <time>     {channel="miele:tumbledryer:home:tumbledryer:elapsed"}
Number:Time TumbleDryer_FinishTime "Remaining time" <time>    {channel="miele:tumbledryer:home:tumbledryer:finish"}
```

## sitemaps/miele.sitemap

```perl
sitemap miele label="Miele" {
    Frame label="Miele" {
        Text item=Oven_State label="Oven [%s]" icon="kitchen" {
            Text item=Oven_Program visibility=[Oven_RawState>1]
            Text item=Oven_Phase visibility=[Oven_Phase!=UNDEF]
            Text item=Oven_ElapsedTime
            Text item=Oven_FinishTime
            Switch item=Oven_Stop
        }
        Text item=WashingMachine_State label="Washing Machine [%s]" icon="washingmachine" {
            Text item=WashingMachine_Program visibility=[WashingMachine_RawState>1]
            Text item=WashingMachine_Temperature visibility=[WashingMachine_Program!=UNDEF]
            Text item=WashingMachine_SpinningSpeed visibility=[WashingMachine_Program!=UNDEF]
            Text item=WashingMachine_Phase visibility=[WashingMachine_Phase!=UNDEF]
            Text item=WashingMachine_ElapsedTime
            Text item=WashingMachine_FinishTime
            Text item=WashingMachine_EnergyConsumption
            Text item=WashingMachine_WaterConsumption
        }
        Text item=TumbleDryer_State label="Tumble Dryer [%s]" icon="dryer" {
            Text item=TumbleDryer_Program visibility=[TumbleDryer_RawState>1]
            Text item=TumbleDryer_Phase visibility=[TumbleDryer_Phase!=UNDEF]
            Text item=TumbleDryer_ElapsedTime
            Text item=TumbleDryer_FinishTime
        }
        Text item=Dishwasher_State label="Dishwasher [%s]" icon="dryer" {
            Text item=Dishwasher_Program visibility=[Dishwasher_RawState>1]
            Text itemDishwasher_Phase visibility=[Dishwasher_Phase!=UNDEF]
            Text item=Dishwasher_ElapsedTime
            Text item=Dishwasher_FinishTime
            Text item=Dishwasher_EnergyConsumption
            Text item=Dishwasher_WaterConsumption
        }
        Text item=Fridge_CurrentTemperature label="Fridge" icon="climate" {
            Text item=Fridge_CurrentTemperature
            Text item=Fridge_TargetTemperature
            Switch item=Fridge_SuperCool icon="snow"
        }
    }
}
```
