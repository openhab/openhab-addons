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

## Example

demo.items:

```
String MieleFridgeState  (gMiele,gMieleFridge) {channel="miele:fridge:dilbeek:fridge:state"}
Switch MieleFridgeSuperCool (gMiele,gMieleFridge) {channel="miele:fridge:dilbeek:fridge:supercool"}
Number:Temperature MieleFridgeCurrent (gMiele,gMieleFridge) {channel="miele:fridge:dilbeek:fridge:current"}
Number:Temperature MieleFridgeTarget (gMiele,gMieleFridge) {channel="miele:fridge:dilbeek:fridge:target"}
Contact MieleFridgeDoor (gMiele,gMieleFridge) {channel="miele:fridge:dilbeek:fridge:door"}
Switch MieleFridgeStart (gMiele,gMieleFridge) {channel="miele:fridge:dilbeek:fridge:start"}
```
