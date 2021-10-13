# Miele@home Binding

This binding integrates Miele@home appliances.
Miele@home allows controlling Miele appliances that are equipped with special communication modules. 
There are devices that communicate through ZigBee and others that use WiFi.

See [www.miele.de](https://www.miele.de) for the list of available appliances.

## Supported Things

This binding requires the XGW3000 gateway from Miele as all integration with openHAB is done through this gateway.
While users with ZigBee-enabled Miele appliances usually own such a gateway, this is often not the case for people that have only WiFi-enabled appliances.

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


## Thing Configuration

Each appliance needs the device UID as a configuration parameter.
The UID is nowhere to be found on the appliances, but since the discovery works quite reliably, a manual configuration is not needed.

Once you got hold of the IDs, a manual configuration looks like this:

```
Bridge miele:xgw3000:dilbeek [ipAddress="192.168.0.18", interface="192.168.0.5"] {
Things:
Thing fridgefreezer freezer [uid="00124b000424be44#2"]
Thing hood hood [uid="001d63fffe020685#210"]
Thing fridge fridge [uid="00124b000424bdc0#2"]
Thing oven oven [uid="001d63fffe020390#210"]
Thing oven microwave [uid="001d63fffe0206eb#210"]
Thing hob hob [uid="00124b000424bed7#2"]
Thing dishwasher dishwasher [uid="001d63fffe020683#210"]
Thing tumbledryer dryer [uid="001d63fffe0200ba#210"]
Thing washingmachine washingmachine [uid="001d63fffe020505#210"]
Thing coffeemachine coffeemachine [uid="001d63fffe020505#190"]
}
```

## Channels

See below which channels are available for each appliance type. Raw program and phase values are individual for each appliance type,
while these raw state values are unique across all appliance types:

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

### Coffee Machine

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

### Dishwasher

| Channel             | Type                 | Read/write | Description                                                         |
|---------------------|----------------------|------------|---------------------------------------------------------------------|
| state               | String               | Read       | Current status of the appliance                                     |
| rawState            | Number               | Read       | Current status of the appliance as raw number                       |
| program             | String               | Read       | Current program or function running on the appliance                |
| rawProgram          | Number               | Read       | Current program or function running on the appliance as raw number  |
| phase               | String               | Read       | Current phase of the program running on the appliance               |
| rawPhase            | Number               | Read       | Current phase of the program running on the appliance as raw number |
| start               | DateTime             | Read       | Programmed start time of the program                                |
| duration            | DateTime             | Read       | Duration of the program running on the appliance                    |
| elapsed             | DateTime             | Read       | Time elapsed in the program running on the appliance                |
| finish              | DateTime             | Read       | Time to finish the program running on the appliance                 |
| door                | Contact              | Read       | Current state of the door of the appliance                          |
| switch              | Switch               | Write      | Switch the appliance on or off                                      |
| powerConsumption    | Number:Power         | Read       | Power consumption by the currently running program on the appliance |
| waterConsumption    | Number:Volume        | Read       | Water consumption by the currently running program on the appliance |

### Fridge

| Channel             | Type                 | Read/write | Description                                                         |
|---------------------|----------------------|------------|---------------------------------------------------------------------|
| state               | String               | Read       | Current status of the appliance                                     |
| rawState            | Number               | Read       | Current status of the appliance as raw number                       |
| supercool           | Switch               | Read/Write | Start or stop Super Cooling                                         |
| current             | Number:Temperature   | Read       | Current temperature in the fridge                                   |
| target              | Number:Temperature   | Read       | Target temperature to be reached by the fridge                      |
| door                | Contact              | Read       | Current state of the door of the appliance                          |
| switch              | Switch               | Write      | Switch the appliance on or off                                      |

### Fridge/Freezer combination

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

### Hob

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

### Hood

| Channel             | Type                 | Read/write | Description                                                         |
|---------------------|----------------------|------------|---------------------------------------------------------------------|
| state               | String               | Read       | Current status of the appliance                                     |
| rawState            | Number               | Read       | Current status of the appliance as raw number                       |
| light               | Switch               | Write      | Switch the appliance on or off                                      |
| ventilation         | Number               | Read       | Current ventilation power                                           |
| stop                | Switch               | Write      | Stop the appliance                                                  |

### Oven

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
| duration            | DateTime             | Read       | Duration of the program running on the appliance                    |
| elapsed             | DateTime             | Read       | Time elapsed in the program running on the appliance                |
| finish              | DateTime             | Read       | Time to finish the program running on the appliance                 |
| target              | Number:Temperature   | Read       | Target temperature to be reached by the oven                        |
| measured            | Number:Temperature   | Read       | Actual measured temperature in the oven                             |
| temp1               | Number:Temperature   | Read       | Program temperature in the oven 1                                   |
| temp2               | Number:Temperature   | Read       | Program temperature in the oven 2                                   |
| door                | Contact              | Read       | Current state of the door of the appliance                          |
| switch              | Switch               | Write      | Switch the appliance on or off                                      |
| stop                | Switch               | Write      | Stop the appliance                                                  |

### Microwave/Oven combination

See oven.

### Tumble Dryer

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
| duration            | DateTime             | Read       | Duration of the program running on the appliance                    |
| elapsed             | DateTime             | Read       | Time elapsed in the program running on the appliance                |
| finish              | DateTime             | Read       | Time to finish the program running on the appliance                 |
| door                | Contact              | Read       | Current state of the door of the appliance                          |
| switch              | Switch               | Write      | Switch the appliance on or off                                      |
| step                | Number               | Read       | Current step in the program running on the appliance                |

### Washing Machine

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
| duration            | DateTime             | Read       | Duration of the program running on the appliance                    |
| elapsed             | DateTime             | Read       | Time elapsed in the program running on the appliance                |
| finish              | DateTime             | Read       | Time to finish the program running on the appliance                 |
| door                | Contact              | Read       | Current state of the door of the appliance                          |
| switch              | Switch               | Write      | Switch the appliance on or off                                      |
| target              | Number:Temperature   | Read       | Temperature of the selected program                                 |
| spinningspeed       | String               | Read       | Spinning speed in the program running on the appliance              |
| powerConsumption    | Number:Power         | Read       | Power consumption by the currently running program on the appliance |
| waterConsumption    | Number:Volume        | Read       | Water consumption by the currently running program on the appliance |

# Configuration Examples

## things/miele.things

```
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

```
String Dishwasher_State                                     {channel="miele:dishwasher:home:dishwasher:state"}
Number Dishwasher_RawState                                  {channel="miele:dishwasher:home:dishwasher:rawState"}
String Dishwasher_Program "Program [%s]"                    {channel="miele:dishwasher:home:dishwasher:program"}
String Dishwasher_Phase "Phase [%s]"                        {channel="miele:dishwasher:home:dishwasher:phase"}
DateTime Dishwasher_ElapsedTime "Elapsed time" <time>       {channel="miele:dishwasher:home:dishwasher:elapsed"}
DateTime Dishwasher_FinishTime "Remaining time" <time>      {channel="miele:dishwasher:home:dishwasher:finish"}
Number:Power Dishwasher_PowerConsumption                    {channel="miele:dishwasher:home:dishwasher:powerConsumption"}
Number:Volume Dishwasher_WaterConsumption                   {channel="miele:dishwasher:home:dishwasher:waterConsumption"}

String Fridge_State                                         {channel="miele:fridge:home:fridge:state"}
Contact Fridge_Door                                         {channel="miele:fridge:home:fridge:door"}
Switch Fridge_SuperCool                                     {channel="miele:fridge:home:fridge:supercool"}
Number:Temperature Fridge_CurrentTemperature <temperature>  {channel="miele:fridge:home:fridge:current"}
Number:Temperature Fridge_TargetTemperature  <temperature>  {channel="miele:fridge:home:fridge:target"}
Switch Fridge_Start                                         {channel="miele:fridge:home:fridge:start"}

String Oven_State                                           {channel="miele:oven:home:oven:state"}
Number Oven_RawState                                        {channel="miele:oven:home:oven:rawState"}
String Oven_Program "Program [%s]"                          {channel="miele:oven:home:oven:program"}
String Oven_Phase "Phase [%s]"                              {channel="miele:oven:home:oven:phase"}
DateTime Oven_ElapsedTime "Elapsed time" <time>             {channel="miele:oven:home:oven:elapsed"}
DateTime Oven_FinishTime "Remaining time" <time>            {channel="miele:oven:home:oven:finish"}
Number:Temperature Oven_CurrentTemperature <temperature>    {channel="miele:oven:home:oven:measured"}
Number:Temperature Oven_TargetTemperature <temperature>     {channel="miele:oven:home:oven:target"}
Switch Oven_Stop                                            {channel="miele:oven:home:oven:stop", autoupdate="false"}

String WashingMachine_State                                 {channel="miele:washingmachine:home:washingmachine:state"}
Number WashingMachine_RawState                              {channel="miele:washingmachine:home:washingmachine:rawState"}
String WashingMachine_Program "Program [%s]"                {channel="miele:washingmachine:home:washingmachine:program"}
String WashingMachine_Phase "Phase [%s]"                    {channel="miele:washingmachine:home:washingmachine:phase"}
Number:Temperature WashingMachine_Temperature <temperature> {channel="miele:washingmachine:home:washingmachine:target"}
String WashingMachine_SpinningSpeed                         {channel="miele:washingmachine:home:washingmachine:spinningspeed"}
DateTime WashingMachine_ElapsedTime "Elapsed time" <time>   {channel="miele:washingmachine:home:washingmachine:elapsed"}
DateTime WashingMachine_FinishTime "Remaining time" <time>  {channel="miele:washingmachine:home:washingmachine:finish"}
Number:Power WashingMachine_PowerConsumption                {channel="miele:washingmachine:home:washingmachine:powerConsumption"}
Number:Volume WashingMachine_WaterConsumption               {channel="miele:washingmachine:home:washingmachine:waterConsumption"}

String TumbleDryer_State                                    {channel="miele:tumbledryer:home:tumbledryer:state"}
Number TumbleDryer_RawState                                 {channel="miele:tumbledryer:home:tumbledryer:rawState"}
String TumbleDryer_Program "Program [%s]"                   {channel="miele:tumbledryer:home:tumbledryer:program"}
String TumbleDryer_Phase "Phase [%s]"                       {channel="miele:tumbledryer:home:tumbledryer:phase"}
DateTime TumbleDryer_ElapsedTime "Elapsed time" <time>      {channel="miele:tumbledryer:home:tumbledryer:elapsed"}
DateTime TumbleDryer_FinishTime "Remaining time" <time>     {channel="miele:tumbledryer:home:tumbledryer:finish"}
```

## sitemaps/miele.sitemap

```
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
            Text item=WashingMachine_PowerConsumption
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
            Text item=Dishwasher_PowerConsumption
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
