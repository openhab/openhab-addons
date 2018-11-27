# Vitotronic Binding

Viessmann heating systems with Vitotronic has a optolink Interface for maintenance.
This interface can use for get/set data in the heating system. [see on openv](http://openv.wikispaces.com)

The Vitotronic binding is a solution to bind this interface into openHAB2.
It supports the separation of the heating adaption from the integration in [openHAB2](http://www.openhab.org/).

![Architectur](doc/architecture_vitotronic.jpg)

The adapter transform the address oriented raw interface of the Vitotronic to a abstract format.
The adapter itself is not a part of the openhab2 binding.
[A alpha version is available here](https://github.com/steand/optolink)
[More Information about the adapter](https://github.com/steand/optolink/wiki)   

## Supported Things

For easy using are the main things of a heating system are already define in this binding:

*   heating (Vitotronic core system)
*   pelletburner (Pellet Fireplace, works for wood also)
*   oilburner (Oil Fireplace)
*   storagetank (Storage Tank, stores heat in a water tank on 3 levels: bottom, middle, top=hot water)
*   circuit (Heating circuit controls the flow between the heating system and the radiators in the rooms)
*   solar (Solar water heating (SWH): Convert sunlight into energy for water heating)

For advanced used 3 basic things of a headingsystem define also.

*   temperaturesensor (Single temperature sensor)
*   pump (Single pump)
*   valve (Single valve)

Note: The mapping of things and channels to the heating system addresses must be done in the adapter.

## Discovery

The binding discovers the adapter with broadcast and put the found `vitotronic:bridge` into the inbox. For automatic detection the adapter and **openHAB** must be on the same LAN. The discovery itself must be start in the Paper-UI.
If the bridge isn't on the same LAN the bridge can also add manually. In this case the `IP-Address` and the `adapterID` is required.
Íf the `vitotronic:bridge` added a second discovery will be start. It discovers all things, define in the adapter and put found things into the inbox.

## Binding Configuration

Binding itself has 4 configuration parameters:   

*   ipAddress (The IP address of the Optolink adapter)
*   port (Port of the LAN gateway. Default: 31113)
*   adapterID (The ID/Name of the adapter)
*   refreshInterval (Refresh time for data in seconds. Default: 600 seconds)

If the adapter is automatic discovered the ipAddress, and adapterID will be set by discovery.
The rereshInterval can be set between 60 and 600 seconds. The minimal setting is dependent of the performance of the adapter.



## Thing Configuration

There is no configuration of Things necessary. Only some channels are set active by default. If this channels are defined in the adapter and will be used in **openHAB**  it must set active manually.
Don't change the Thing Name. It is the reference to the name in the adapter.  

## Channels

The follow channels are implemented:   

| Channel Type ID  | Item Type | Description                                           |
|------------------|-----------|-------------------------------------------------------|
| systemtime       | DateTime  | DateTime of the heating system                        |
| outside_temp     | Number    | Outside temperature sensor                            |
| boiler_temp      | Number    | Temperature sensor of boiler (fireplace)              |
| flowuprating     | Switch    | Pump state of flow up rating                          |
| flame_temp       | Number    | Temperature of flame                                  |
| airshutter_prim  | Number    | Position of the primary air shutter                   |
| airshutter_sec   | Number    | Position of the secondary air shutter                 |
| lambdasensor     | Number    | Oxygen content of the exhaust air                     |
| fanspeed         | Number    | Fan Speed in rpm                                      |
| fanspeed_target  | Number    | Fan Speed in rpm                                      |
| error            | Switch    |                                                       |
| starts           | Number    | Count of starts                                       |
| ontime           | Number    | Ontime in hours                                       |
| consumedpellets  | Number    | Consumed Pellets since start of heating in tons       |
| power            | Number    | Power of the pellet burner                            |
| powerlevel       | Number    | Power of the oil burner                               |
| actualpower      | Number    | Actual power of the burner                            |
| ontimelevel1     | Number    | Ontime in hours                                       |
| ontimelevel2     | Number    | Ontime in hours                                       |
| consumedoil      | Number    | Consumed Oil since start of heating in Liter          |
| hotwater_temp    | Number    | Temperature sensor of the hot water                   |
| middle_temp      | Number    | Temperature sensor in the middle of the storage tank  |
| bottom_temp      | Number    | Temperature sensor at the bottom of the storage tank  |
| circuitpump      | Switch    | Circuit pump state                                    |
| flowtemperature  | Number    | Temperature sensor of the ciruit flow                 |
| pump             | Switch    | Pump state                                            |
| operationmode    | Number    | Operationmode                                         |
| savemode         | Switch    | Savemode on/off                                       |
| partymode        | Switch    | Partymode on/off                                      |
| party_temp       | Number    | Target temperature of party mode                      |
| room_temp        | Number    | Target temperature of rooms                           |
| save_temp        | Number    | Target temperature of save mode                       |
| gradient         | Number    | The gradient relativ to outside temperature           |
| niveau           | Number    | The niveau relativ to outside temperature             |
| collector_temp   | Number    | Actual temperature of the collector                   |
| storagetank_temp | Number    | Actual temperature of the storage tank (solar sensor) |
| bufferload       | Switch    | State of the pump (on/off)                            |
| loadsuppression  | Switch    | State of the load suppression (on/off)                |
| producedheat     | Number    | Produced heat since starting solar system             |
| temperature      | Number    | Generic temperature sensor                            |
| valve            | Number    | Value of a generic valve                              |

## Example

t.b.d
