# Stiebel Eltron ISG

This extension adds support for the Stiebel Eltron Modbus protocol.

An Internet Service Gateway (ISG) with an installed Modbus extension is required in order to run this binding.
In case the Modbus extension is not yet installed on the ISG, you have to contact the Stiebel Eltron support, because the ISG Updater Tool for the update is not available anymore as described in earlier releases of the addon. But there are still documents available about the ISG (plus) under the given links:<br>
ISG web (English)
<https://www.stiebel-eltron.com/en/home/products-solutions/renewables/controller_energymanagement/isg-web/isg-web.html>
About the ISG plus (only German, French and Italian)
<https://www.stiebel-eltron.ch/de/home/produkte-loesungen/erneuerbare_energien/regelung_energiemanagement/servicewelt-und-isg.html>
<https://www.stiebel-eltron.ch/de/home/produkte-loesungen/erneuerbare_energien/regelung_energiemanagement/isg-plus/isg-plus.html>

Note about the new ISG version "*ISG connect*": it is unknown, if this binding supports it.

## Supported Things

This bundle adds the following thing types to the Modbus binding.
Note, that the things will show up under the Modbus binding.

| Thing                                     | ThingTypeID                   | Description                                                                                                         |
| ----------------------------------------- | ----------------------------- | --------------------------------------------------------------------------------------------------------------------|
| Stiebel Eltron Heat Pump                  | heatpump                      | A Stiebel Eltron Heat Pump connected through modbus to an ISG                                                       |
| Stiebel Eltron Heat Pump (WPM compatible) | stiebeleltron-heatpump-allwpm | A Stiebel Eltron Heat Pump (WPM compatible) thing with extended function support connected through Modbus to an ISG |

The first thing *Stiebel Eltron Heat Pump* and its channel IDs have been kept for compatibility reasons.
It's recommended to switch to the second thing *Stiebel Eltron Heat Pump (WPM compatible)* as it supports the retrieval of much more information from a compatible heat pump controller (WPM).
Supported controllers are WPMsystem, WPM3 and WPM3i.
If the SG Ready polling is enabled using configuration parameter *pollSgReady*, the controller id is retrieved and visible in the appropriate channel.
If the id is known, it can be set in the configuration parameter *wpmControllerId*.
The binding code works without correct controller id as it checks the retrieved values and sets unavailable channels fix to NULL.

## Discovery

This extension does not support auto-discovery. The things need to be added manually.

## Thing Configuration

You first need to set up a TCP Modbus bridge according to the Modbus documentation. A typical bridge configuration would look like this:

```java
Bridge modbus:tcp:bridge [ host="10.0.0.2", port=502, id=1 ]
```

The things in this extension will use the selected bridge to connect to the device.

The following configuration parameters are valid for the <i>stiebeleltron-heatpump-allwpm</i> thing:

| Parameter | Type    | Required | Default if omitted | Description                                                                |
| --------- | ------- | -------- | ------------------ | -------------------------------------------------------------------------- |
| refresh   | integer | no       | 5                  | Poll interval in seconds. Increase this if you encounter connection errors |
| maxTries  | integer | no       | 3                  | Number of retries before giving up reading from this thing                 |


| Parameter         | Type    | Required | Default if omitted | Description                                                          |
| ----------------- | ------- | -------- | ------------------ | -------------------------------------------------------------------- |
| stateBlockLength  | integer | no       | 3                  | Number of retries before giving up reading from this thing           |
| nrOfHps           | integer | no       | 0                  | Number of heat pumps in a WPMsystem compatible heat pump             |
| wpmControllerId   | integer | no       | WMP3               | Default WPM controller id (WPM3 = 390, WPM3I = 391, WPMsystem = 449) |
| pollSgReady       | boolean | no       | false              | NFlag to enable polling of the SG Ready registers                    |

A typcial bridge and thing setup would look like this:

```java
Bridge modbus:tcp:bridge [ host="10.0.0.2", port=502, id=1 ] {
    Thing heatpump StiebelEltronHP "Stiebel Eltron Heat Pump" (modbus:tcp:bridge) @"room"  [ ]
}
```

## Channels

Channels are grouped into channel groups.
The heat pump thing *Stiebel Eltron Heat Pump (WPM compatible)* supports more channels dependant on the available heat pump type.

### System State Groups

This groups contain general state information about the heat pump.

#### Channels supported by the legacy thing *Stiebel Eltron Heat Pump*

| Channel ID       | Item Type | Read only | Description                                                   |
| ---------------- | --------- | --------- | ------------------------------------------------------------- |
| is-heating       | Contact   | true      | OPEN in case the heat pump is currently in heating mode       |
| is-heating-water | Contact   | true      | OPEN in case the heat pump is currently in heating water mode |
| is-cooling       | Contact   | true      | OPEN in case the heat pump is currently in cooling mode       |
| is-pumping       | Contact   | true      | OPEN in case the heat pump is currently in pumping mode       |
| is-summer        | Contact   | true      | OPEN in case the heat pump is currently in summer mode        |



#### Channels supported by thing *Stiebel Eltron Heat Pump (WPM compatible)*

Note: The column WPM is for WPMsystem.

| Channel ID                     | Item Type | Read only | Description                                                            | WPM | WPM3 | WPM3i |
| ------------------------------ | --------- | --------- | ---------------------------------------------------------------------- | --- | ---- | ----- |
| hc1-pump-active                | Contact   | true      | OPEN in case the heat circuit 1 pump is currently running              |  x  |  x   |   x   |
| hc2-pump-active                | Contact   | true      | OPEN in case the heat circuit 2 pump is currently running              |  x  |  x   |   x   |
| heat-up-program-active         | Contact   | true      | OPEN in case the heat-up program is currently running                  |  x  |  x   |   x   |
| nhz-stages-running             | Contact   | true      | OPEN in case if any electric reheating stages are currently running    |  x  |  x   |   x   |
| hp-in-heating-mode             | Contact   | true      | OPEN in case the heat pump is currently in heating mode                |  x  |  x   |   x   |
| hp-in-hotwater-mode            | Contact   | true      | OPEN in case the heat pump is currently in heating water mode          |  x  |  x   |   x   |
| compressor-running             | Contact   | true      | OPEN in case the compressor is currently running                       |  x  |  x   |   x   |
| summer-mode-active             | Contact   | true      | OPEN in case the heat pump is currently in summer mode                 |  x  |  x   |   x   |
| cooling-mode-active            | Contact   | true      | OPEN in case the heat pump is currently in cooling mode                |  x  |  x   |   x   |
| min-one-iws-in-defrosting-mode | Contact   | true      | OPEN in case at least one IWS is in defrosting mode                    |  x  |  x   |   x   |
| silent-mode1-active            | Contact   | true      | OPEN in case the silent mode 1 is currently active                     |  x  |  x   |   x   |
| silent-mode2-active            | Contact   | true      | OPEN in case the silent mode 2 is currently active (heat pump off)     |  x  |  x   |   x   |
| power-off                      | Contact   | true      | OPEN in case the heat pump is currently blocked by the power company   |  x  |  x   |   x   |
| compressor1-active             | Contact   | true      | OPEN in case the compressor 1 is currently running                     |     |  x   |       |
| compressor2-active             | Contact   | true      | OPEN in case the compressor 2 is currently running                     |     |  x   |       |
| compressor3-active             | Contact   | true      | OPEN in case the compressor 3 is currently running                     |     |  x   |       |
| compressor4-active             | Contact   | true      | OPEN in case the compressor 4 is currently running                     |     |  x   |       |
| compressor5-active             | Contact   | true      | OPEN in case the compressor 5 is currently running                     |     |  x   |       |
| compressor6-active             | Contact   | true      | OPEN in case the compressor 6 is currently running                     |     |  x   |       |
| buffer-charging-pump1-active   | Contact   | true      | OPEN in case the buffer charing pump 1 is currently running            |     |  x   |       |
| buffer-charging-pump2-active   | Contact   | true      | OPEN in case the buffer charing pump 2 is currently running            |     |  x   |       |
| buffer-charging-pump3-active   | Contact   | true      | OPEN in case the buffer charing pump 3 is currently running            |     |  x   |       |
| buffer-charging-pump4-active   | Contact   | true      | OPEN in case the buffer charing pump 4 is currently running            |     |  x   |       |
| buffer-charging-pump5-active   | Contact   | true      | OPEN in case the buffer charing pump 5 is currently running            |     |  x   |       |
| buffer-charging-pump6-active   | Contact   | true      | OPEN in case the buffer charing pump 6 is currently running            |     |  x   |       |
| nhz1-active                    | Contact   | true      | OPEN in case the electric reheating stage 1 is currently running       |     |  x   |       |
| nhz2-active                    | Contact   | true      | OPEN in case the electric reheating stage 2 is currently running       |     |  x   |       |
| fault-status                   | Number    | true      | Fault Status: 0=No Fault, 1=Fault                                      |  x  |  x   |   x   |
| bus-status                     | Number    | true      | Bus Status: 0=OK, 1=ERROR, 2=Error-Passive,3=Bus-Off, 4=Physical Error |  x  |  x   |   x   |
| defrost-initiated              | Number    | true      | Defrost Initiated: 0=OFF, 1=INITIATED                                  |  x  |  x   |       |
| active-error                   | Number    | true      | Active Error Number                                                    |  x  |  x   |   x   |


### System Parameters Group

This group contains system paramters of the heat pump.

#### Channels supported by thing *Stiebel Eltron Heat Pump*

| Channel ID                  | Item Type          | Read only | Description                                                                                      |
| --------------------------- | ------------------ | --------- | ------------------------------------------------------------------------------------------------ |
| operation-mode              | Number             | false     | The current operation mode of the heat pump                                                      |
|                             |                    |           | 0=emergency mode, 1=ready mode, 2=program mode, 3=comfort mode, 4=eco mode, 5=heating water mode |
| comfort-temperature-heating | Number:Temperature | false     | The current heating comfort temperature                                                          |
| eco-temperature-heating     | Number:Temperature | false     | The current heating eco temperature                                                              |
| comfort-temperature-water   | Number:Temperature | false     | The current hot water comfort temperature                                                        |
| eco-temperature-water       | Number:Temperature | false     | The current hot water eco temperature                                                            |

#### Channels supported by thing *Stiebel Eltron Heat Pump (WPM compatible)*

Note: The column WPM is for WPMsystem.

| Channel ID                                | Item Type          | Read only | Description                                                        | WPM | WPM3 | WPM3i |
| ----------------------------------------- | ------------------ | --------- | ------------------------------------------------------------------ | --- | ---- | ----- |
| operating-mode                            | Number             | false     | The current operation mode of the heat pump                        |  x  |  x   |   x   |
|                                           |                    |           | 0=emergency, 1=ready, 2=program, 3=comfort, 4=eco, 5=heating water |  x  |  x   |   x   |
| hc1-comfort-temperature                   | Number:Temperature | false     | The current heating comfort temperature of heat circuit 1          |  x  |  x   |   x   |
| hc1-eco-temperature                       | Number:Temperature | false     | The current heating eco temperature of heat circuit 1              |  x  |  x   |   x   |
| hc1-heating-curve-rise                    | Number             | false     | The current heating curve rise of heat circuit 1                   |  x  |  x   |   x   |
| hc2-comfort-temperature                   | Number:Temperature | false     | The current heating comfort temperature of heat circuit 2          |  x  |  x   |   x   |
| hc2-eco-temperature                       | Number:Temperature | false     | The current heating eco temperature of heat circuit 2              |  x  |  x   |   x   |
| hc2-heating-curve-rise                    | Number             | false     | The current heating curve rise of heat circuit 2                   |  x  |  x   |   x   |
| fixed-value-operation                     | Number:Temperature | false     | The current heating temperature of the fixed value operation       |  x  |  x   |   x   |
| heating-dual-mode-temperature             | Number:Temperature | false     | The current heating temperature of the dual mode operation         |     |  x   |   x   |
| hotwater-comfort-temperature              | Number:Temperature | false     | The current hot water comfort temperature                          |  x  |  x   |   x   |
| hotwater-eco-temperature                  | Number:Temperature | false     | The current hot water eco temperature                              |  x  |  x   |   x   |
| hotwater-stages                           | Number             | false     | The current number of active hot water stages                      |  x  |  x   |   x   |
| hotwater-dual-mode-temperature            | Number:Temperature | false     | The current hot water temperature of the dual mode operation       |  x  |  x   |   x   |
| area-cooling-flow-temperature-setpoint    | Number:Temperature | false     | The current area cooling flow setpoint temperature                 |  x  |  x   |   x   |
| area-cooling-flow-temperature-hysteresis  | Number:Temperature | false     | The current area cooling flow temperature hysteresis               |     |  x   |   x   |
| area-cooling-room-temperature-setpoint    | Number:Temperature | false     | The current area cooling room setoint temperature                  |  x  |  x   |   x   |
| fan-cooling-flow-temperature-setpoint     | Number:Temperature | false     | The current fan cooling flow setpoint temperature                  |  x  |  x   |   x   |
| fan-cooling-flow-temperature-hysteresis   | Number:Temperature | false     | The current fan cooling flow temperature hysteresis                |  x  |  x   |   x   |
| fan-cooling-room-temperature-setpoint     | Number:Temperature | false     | The current fan cooling room temperature hysteresis                |  x  |  x   |   x   |
| reset                                     | Number             | false     | Reset heat pump / 0=off, 1=system reset (factory reset),           |  x  |  x   |   x   |
|                                           |                    |           |   2=reset fault list, 3=reset heat pump                            |  x  |  x   |   x   |
| restart-isg                               | Number             | false     | Restart ISG command / 0=off, 1=restart, 2=service key              |  x  |  x   |   x   |

##### Note

Channel 'reset': The binding only accepts commands 2 and 3 - command 1 (system reset) is ignored to prevent factory reset by accident.
Channel 'restart-isg': The binding only accepts command 1 - command 2 (service key) is ignored as impact of command is not known to developer.


### System Information Group

This group contains general operational information about the device.

#### Channels supported by thing *Stiebel Eltron Heat Pump*

| Channel ID                 | Item Type            | Read only | Description                                           |
| -------------------------- | -------------------- | --------- | ----------------------------------------------------- |
| fek-temperature            | Number:Temperature   | true      | The current temperature measured by the FEK           |
| fek-temperature-setpoint   | Number:Temperature   | true      | The current setpoint of the FEK temperature           |
| fek-humidity               | Number:Dimensionless | true      | The current humidity measured by the FEK              |
| fek-dewpoint               | Number:Temperature   | true      | The current dew point temperature measured by the FEK |
| outdoor-temperature        | Number:Temperature   | true      | The current outdoor temperature                       |
| hk1-temperature            | Number:Temperature   | true      | The current temperature of the HK1                    |
| hk1-temperature-setpoint   | Number:Temperature   | true      | The current temperature setpoint of the HK1           |
| supply-temperature         | Number:Temperature   | true      | The current supply temperature                        |
| return-temperature         | Number:Temperature   | true      | The current return temperature                        |
| source-temperature         | Number:Temperature   | true      | The current source temperature                        |
| water-temperature          | Number:Temperature   | true      | The current water temperature                         |
| water-temperature-setpoint | Number:Temperature   | true      | The current water temperature setpoint                |

#### Channels supported by thing *Stiebel Eltron Heat Pump (WPM compatible)*

Note: The column WPM is for WPMsystem.

| Channel ID                                | Item Type                 | Read only | Description                                                          | WPM | WPM3 | WPM3i |
| ----------------------------------------- | ------------------------- | --------- | -------------------------------------------------------------------- | --- | ---- | ----- |
| fe7-temperature                           | Number:Temperature        | true      | The current temperature measured by the Remote Control FE7           |  x  |  x   |   x   |
| fe7-temperature-setpoint                  | Number:Temperature        | true      | The current setpoint of the Remote Control FE7 temperature           |  x  |  x   |   x   |
| fek-temperature                           | Number:Temperature        | true      | The current temperature measured by the Remote Control FEK           |     |  x   |   x   |
| fek-temperature-setpoint                  | Number:Temperature        | true      | The current setpoint of the Remote Control FEK temperature           |     |  x   |   x   |
| fek-humidity                              | Number:Dimensionless      | true      | The current humidity measured by the Remote Control FEK              |     |  x   |   x   |
| fek-dewpoint                              | Number:Temperature        | true      | The current dew point temperature measured by the Remote Control FEK |     |  x   |   x   |
| outdoor-temperature                       | Number:Temperature        | true      | The current outdoor temperature                                      |  x  |  x   |   x   |
| hc1-temperature                           | Number:Temperature        | true      | The current heat circuit 1 temperature                               |  x  |  x   |   x   |
| hc1-temperature-setpoint                  | Number:Temperature        | true      | The current heat circuit 1 temperature setpoint                      |  x  |  x   |   x   |
| hc2-temperature                           | Number:Temperature        | true      | The current heat circuit 2 temperature                               |  x  |  x   |   x   |
| hc2-temperature-setpoint                  | Number:Temperature        | true      | The current heat circuit 2 temperature setpoint                      |  x  |  x   |   x   |
| hp-flow-temperature                       | Number:Temperature        | true      | The current heat pump flow temperature                               |  x  |  x   |   x   |
| nhz-flow-temperature                      | Number:Temperature        | true      | The current electric reheating flow temperature                      |  x  |  x   |   x   |
| flow-temperature                          | Number:Temperature        | true      | The current flow temperature                                         |     |  x   |       |
| return-temperature                        | Number:Temperature        | true      | The current return temperature                                       |  x  |  x   |   x   |
| fixed-temperature-setpoint                | Number:Temperature        | true      | The current fixed temperature setpoint                               |  x  |  x   |   x   |
| buffer-temperature                        | Number:Temperature        | true      | The current buffer temperature                                       |  x  |  x   |   x   |
| buffer-temperature-setpoint               | Number:Temperature        | true      | The current buffer temperature setpoint                              |  x  |  x   |   x   |
| heating-pressure                          | Number:Pressure           | true      | The current heating pressure                                         |  x  |  x   |   x   |
| flow-rate                                 | Number:VolumetricFlowRate | true      | The current flow rate                                                |  x  |  x   |   x   |
| hotwater-temperature                      | Number:Temperature        | true      | The current hotwater temperature                                     |  x  |  x   |   x   |
| hotwater-temperature-setpoint             | Number:Temperature        | true      | The current hotwater temperature setpoint                            |  x  |  x   |   x   |
| fan-cooling-temperature                   | Number:Temperature        | true      | The current fan cooling temperature                                  |  x  |  x   |   x   |
| fan-cooling-temperature-setpoint          | Number:Temperature        | true      | The current fan cooling temperature setpoint                         |  x  |  x   |   x   |
| area-cooling-temperature                  | Number:Temperature        | true      | The current area cooling temperature                                 |  x  |  x   |   x   |
| area-cooling-temperature-setpoint         | Number:Temperature        | true      | The current area cooling temperature setpoint                        |  x  |  x   |   x   |
| solar-thermal-collector-temperature       | Number:Temperature        | true      | The current solar thermal collector temperature                      |     |  x   |       |
| solar-thermal-cylinder-temperature        | Number:Temperature        | true      | The current solar thermal collector temperature setpoint             |     |  x   |       |
| solar-thermal-runtime                     | Number:Time               | true      | The current solar thermal runtime                                    |     |  x   |       |
| external-heat-source-temperature          | Number:Temperature        | true      | The current external heat source temperature                         |  x  |  x   |       |
| external-heat-source-temperature-setpoint | Number:Temperature        | true      | The current external heat source temperature setpoint                |  x  |  x   |       |
| external-heat-source-runtime              | Number:Time               | true      | The current external heat source runtime                             |  x  |  x   |       |
| lower-application-limit-heating           | Number:Temperature        | true      | The current heating lower application limit temperature              |  x  |  x   |   x   |
| lower-application-limit-hotwater          | Number:Temperature        | true      | The current hotwater lower application limit temperature             |  x  |  x   |   x   |
| source-temperature                        | Number:Temperature        | true      | The current source temperature                                       |  x  |  x   |   x   |
| min-source-temperature                    | Number:Temperature        | true      | The current minimal source temperature                               |  x  |  x   |   x   |
| source-pressure                           | Number:Pressure           | true      | The current source pressure                                          |  x  |  x   |   x   |
| hotgas-temperature                        | Number:Temperature        | true      | The current hot gas temperature                                      |     |      |   x   |
| high-pressure                             | Number:Pressure           | true      | The current high pressure                                            |     |      |   x   |
| low-pressure                              | Number:Pressure           | true      | The current low pressure                                             |     |      |   x   |
| hp1-return-temperature                    | Number:Temperature        | true      | Heat Pump 1 current return temperature                               |  x  |  x   |       |
| hp1-flow-temperature                      | Number:Temperature        | true      | Heat Pump 1 current flow temperature                                 |  x  |  x   |       |
| hp1-hotgas-temperature                    | Number:Temperature        | true      | Heat Pump 1 current hot gas temperature                              |  x  |  x   |       |
| hp1-low-pressure                          | Number:Pressure           | true      | Heat Pump 1 current low pressure                                     |  x  |  x   |       |
| hp1-mean-pressure                         | Number:Pressure           | true      | Heat Pump 1 current mean pressure                                    |  x  |  x   |       |
| hp1-high-pressure                         | Number:Pressure           | true      | Heat Pump 1 current high pressure                                    |  x  |  x   |       |
| hp1-flow-rate                             | Number:VolumetricFlowRate | true      | Heat Pump 1 current flow rate                                        |  x  |  x   |       |

#### Note
The last block can be available for up to 6 heat pumps. The number of available heat pumps shall be set with the configuration paramaeter *nrOfHps*.


### Energy Information Group

This group contains information about the energy consumption and delivery of the heat pump.

#### Channels supported by things *Stiebel Eltron Heat Pump*

| Channel ID                         | Item Type     | Read only | Description                                            |
| -----------------------------------| ------------- | --------- | -------------------------------------------------------|
| production-heat-today              | Number:Energy | true      | The compressor heat quantity delivered today           |
| production-heat-total              | Number:Energy | true      | The compressor heat quantity delivered in total        |
| production-water-today             | Number:Energy | true      | The compressor water heat quantity delivered today     |
| production-water-total             | Number:Energy | true      | The compressor water heat quantity delivered in total  |
| consumption-heat-today             | Number:Energy | true      | The power consumption for heating today                |
| consumption-heat-total             | Number:Energy | true      | The power consumption for heating in total             |
| consumption-water-today            | Number:Energy | true      | The power consumption for water heating today          |
| consumption-water-total            | Number:Energy | true      | The power consumption for water heating in total       |


### Energy und Runtime Information Group

This group contains information about the energy consumption and delivery of the heat pump as well as the runtime of compressor and electric reheating stages for heating, cooling and hot water production.

#### Channels supported by thing *Stiebel Eltron Heat Pump (WPM compatible)*

Note: The column WPM is for WPMsystem.

| Channel ID                         | Item Type     | Read only | Description                                                          | WPM | WPM3 | WPM3i |
| ---------------------------------- | ------------- | --------- | -------------------------------------------------------------------- | --- | ---- | ----- |
| production-heat-today              | Number:Energy | true      | The compressor heat quantity delivered today                         |  x  |  x   |   x   |
| production-heat-total              | Number:Energy | true      | The compressor heat quantity delivered in total                      |  x  |  x   |   x   |
| production-water-today             | Number:Energy | true      | The compressor water heat quantity delivered today                   |  X  |  x   |   x   |
| production-water-total             | Number:Energy | true      | The compressor water heat quantity delivered in total                |  X  |  x   |   x   |
| production-nhz-heat-total          | Number:Energy | true      | The electric reheating heat quantity delivered in total              |  X  |  X   |   x   |
| production-nhz-water-total         | Number:Energy | true      | The electric reheating hot water quantity delivered in total         |  X  |  X   |   x   |
| consumption-heat-today             | Number:Energy | true      | The power consumption for heating today                              |  x  |  x   |   x   |
| consumption-heat-total             | Number:Energy | true      | The power consumption for heating in total                           |  x  |  x   |   x   |
| consumption-water-today            | Number:Energy | true      | The power consumption for water heating today                        |  x  |  x   |   x   |
| consumption-water-total            | Number:Energy | true      | The power consumption for water heating in total                     |  x  |  x   |   x   |
| heating-runtime                    | Number:Time   | true      | The compressor runtime for heating in total                          |     |      |   x   |
| hotwater-runtime                   | Number:Time   | true      | The compressor runtime for heating in total                          |     |      |   x   |
| cooling-runtime                    | Number:Time   | true      | The compressor runtime for cooling in total                          |     |      |   x   |
| nhz1-runtime                       | Number:Time   | true      | The electric reheating stage 1 runtime in total                      |  x  |  x   |   x   |
| nhz2-runtime                       | Number:Time   | true      | The electric reheating stage 2 runtime in total                      |  x  |  x   |   x   |
| nhz12-runtime                      | Number:Time   | true      | The electric reheating stages 1+2 runtime in total                   |  x  |  x   |   x   |
| hp1-production-heat-today          | Number:Energy | true      | Heat Pump 1 compressor heat quantity delivered today                 |  x  |  x   |       |
| hp1-production-heat-total          | Number:Energy | true      | Heat Pump 1 compressor heat quantity delivered in total              |  x  |  x   |       |
| hp1-production-water-today         | Number:Energy | true      | Heat Pump 1 compressor water heat quantity delivered today           |  x  |  x   |       |
| hp1-production-water-total         | Number:Energy | true      | Heat Pump 1 compressor water heat quantity delivered in total        |  x  |  x   |       |
| hp1-production-nhz-heat-total      | Number:Energy | true      | Heat Pump 1 electric reheating heat quantity delivered in total      |  x  |  x   |       |
| hp1-production-nhz-water-total     | Number:Energy | true      | Heat Pump 1 electric reheating hot water quantity delivered in total |  x  |  x   |       |
| hp1-consumption-heat-today         | Number:Energy | true      | Heat Pump 1 power consumption for heating today                      |  x  |  x   |       |
| hp1-consumption-heat-total         | Number:Energy | true      | Heat Pump 1 power consumption for heating in total                   |  x  |  x   |       |
| hp1-consumption-water-today        | Number:Energy | true      | Heat Pump 1 power consumption for water heating today                |  x  |  x   |       |
| hp1-consumption-water-total        | Number:Energy | true      | Heat Pump 1 power consumption for water heating in total             |  x  |  x   |       |
| hp1-cp1-heating-runtime            | Number:Time   | true      | Heat Pump 1 compressor 1 runtime for heating in total                |  X  |  x   |       |
| hp1-cp2-heating-runtime            | Number:Time   | true      | Heat Pump 1 compressor 2 runtime for heating in total                |  X  |  x   |       |
| hp1-cp12-heating-runtime           | Number:Time   | true      | Heat Pump 1 compressor 1/2 runtime for heating in total              |  x  |  x   |       |
| hp1-cp1-hotwater-runtime           | Number:Time   | true      | Heat Pump 1 compressor 1 runtime for heating in total                |  x  |  x   |       |
| hp1-cp2-hotwater-runtime           | Number:Time   | true      | Heat Pump 1 compressor 2 runtime for heating in total                |  x  |  x   |       |
| hp1-cp12-hotwater-runtime          | Number:Time   | true      | Heat Pump 1 compressor 1/2 runtime for heating in total              |  x  |  x   |       |
| hp1-cooling-runtime                | Number:Time   | true      | Heat Pump 1 compressor runtime for cooling in total                  |  x  |  x   |       |

#### Note
The last block can be available for up to 6 heat pumps. The number of available heat pumps shall be set with the configuration paramaeter *nrOfHps*.


### SG Ready - Energy Management Settings
The following channels are only available for the thing *Stiebel Eltron Heat Pump (WPM compatible)*

| Channel ID             | Item Type | Read only | Description            |
| -----------------------| ----------| --------- | -----------------------|
| sg-ready-on-off-switch | Number    | false     | SG Ready On/Off Switch |
| sg-ready-input-lines   | Number    | false     | SG Ready Input Lines   |


### SG Ready - Energy Management System Information
The following channels are only available for the thing *Stiebel Eltron Heat Pump (WPM compatible)*

| Channel ID                         | Item Type | Read only | Description                        |
| ---------------------------------- | ----------| --------- | -----------------------------------|
| sg-ready-operating-state           | Number    | true      | SG Ready Operating State           |
| sg-ready-controller-identification | Number    | true      | SG Ready Controller Identification |


## Full Example for the thing *Stiebel Eltron Heat Pump*

### Thing Configuration

```java
Bridge modbus:tcp:bridge "Stiebel Modbus TCP"[ host="hostname|ip", port=502, id=1 ]
Thing modbus:heatpump:stiebelEltron "StiebelEltron" (modbus:tcp:bridge) @"Room" [ ]
```
#### Note
If using the notation with braces, the thing UID gets automatically a bridge label inbetween (like modbus:heatpump:bridge:stiebelEltron).
Therefore, the notation without braces is used.

### Item Configuration

```java
Number:Temperature stiebel_eltron_temperature_ffk    "Temperature FFK [%.1f °C]" <temperature>           { channel="modbus:heatpump:stiebelEltron:systemInformation#fek-temperature" }
Number:Temperature stiebel_eltron_setpoint_ffk       "Setpoint FFK [%.1f °C]" <temperature>              { channel="modbus:heatpump:stiebelEltron:systemInformation#fek-temperature-setpoint" }
Number:Dimensionless stiebel_eltron_humidity_ffk     "Humidity FFK [%.1f %%]" <humidity>                 { channel="modbus:heatpump:stiebelEltron:systemInformation#fek-humidity" }
Number:Temperature stiebel_eltron_dewpoint_ffk       "Dew point FFK [%.1f °C]" <temperature>             { channel="modbus:heatpump:stiebelEltron:systemInformation#fek-dewpoint" }

Number:Temperature stiebel_eltron_outdoor_temp       "Outdoor temperature [%.1f °C]"                     { channel="modbus:heatpump:stiebelEltron:systemInformation#outdoor-temperature" }
Number:Temperature stiebel_eltron_temp_hk1           "Temperature HK1 [%.1f °C]"                         { channel="modbus:heatpump:stiebelEltron:systemInformation#hk1-temperature" }
Number:Temperature stiebel_eltron_setpoint_hk1       "Setpoint HK1 [%.1f °C]"                            { channel="modbus:heatpump:stiebelEltron:systemInformation#hk1-temperature-setpoint" }
Number:Temperature stiebel_eltron_temp_water         "Water temperature  [%.1f °C]"                      { channel="modbus:heatpump:stiebelEltron:systemInformation#water-temperature" }
Number:Temperature stiebel_eltron_setpoint_water     "Water setpoint [%.1f °C]"                          { channel="modbus:heatpump:stiebelEltron:systemInformation#water-temperature-setpoint" }
Number:Temperature stiebel_eltron_source_temp        "Source temperature [%.1f °C]"                      { channel="modbus:heatpump:stiebelEltron:systemInformation#source-temperature" }
Number:Temperature stiebel_eltron_supply_temp        "Supply tempertature [%.1f °C]"                     { channel="modbus:heatpump:stiebelEltron:systemInformation#supply-temperature" }
Number:Temperature stiebel_eltron_return_temp        "Return temperature  [%.1f °C]"                     { channel="modbus:heatpump:stiebelEltron:systemInformation#return-temperature" }

Number stiebel_eltron_heating_comfort_temp           "Heating Comfort Temperature [%.1f °C]"             { channel="modbus:heatpump:stiebelEltron:systemParameter#comfort-temperature-heating" }
Number stiebel_eltron_heating_eco_temp               "Heating Eco Temperature [%.1f °C]"                 { channel="modbus:heatpump:stiebelEltron:systemParameter#eco-temperature-heating" }
Number stiebel_eltron_water_comfort_temp             "Water Comfort Temperature [%.1f °C]"               { channel="modbus:heatpump:stiebelEltron:systemParameter#comfort-temperature-water" }
Number stiebel_eltron_water_eco_temp                 "Water Eco Temperature [%.1f °C]"                   { channel="modbus:heatpump:stiebelEltron:systemParameter#eco-temperature-water" }
Number stiebel_eltron_operation_mode                 "Operation Mode"                                    { channel="modbus:heatpump:stiebelEltron:systemParameter#operation-mode" }

Contact stiebel_eltron_mode_pump                     "Pump [%d]"                                         { channel="modbus:heatpump:stiebelEltron:systemState#is-pumping" }
Contact stiebel_eltron_mode_heating                  "Heating [%d]"                                      { channel="modbus:heatpump:stiebelEltron:systemState#is-heating" }
Contact stiebel_eltron_mode_water                    "Heating Water [%d]"                                { channel="modbus:heatpump:stiebelEltron:systemState#is-heating-water" }
Contact stiebel_eltron_mode_cooling                  "Cooling [%d]"                                      { channel="modbus:heatpump:stiebelEltron:systemState#is-cooling" }
Contact stiebel_eltron_mode_summer                   "Summer Mode [%d]"                                  { channel="modbus:heatpump:stiebelEltron:systemState#is-summer" }


Number:Energy stiebel_eltron_production_heat_today   "Heat quantity today [%.0f kWh]"                    { channel="modbus:heatpump:stiebelEltron:energyInformation#production_heat_today" }
Number:Energy stiebel_eltron_production_heat_total   "Heat quantity total  [%.3f MWh]"                   {channel="modbus:heatpump:stiebelEltron:energyInformation#production_heat_total"}
Number:Energy stiebel_eltron_production_water_today  "Water heat quantity today  [%.0f kWh]"             { channel="modbus:heatpump:stiebelEltron:energyInformation#production_water_today" }
Number:Energy stiebel_eltron_production_water_total  "Water heat quantity total  [%.3f MWh]"             {channel="modbus:heatpump:stiebelEltron:energyInformation#production_water_total"}
Number:Energy stiebel_eltron_consumption_heat_total  "Heating power consumption total [%.3f MWh]"        {channel="modbus:heatpump:stiebelEltron:energyInformation#consumption_heat_total"}
Number:Energy stiebel_eltron_consumption_heat_today  "Heating power consumption today [%.0f kWh]"        { channel="modbus:heatpump:stiebelEltron:energyInformation#consumption_heat_today" }
Number:Energy stiebel_eltron_consumption_water_today "Water heating power consumption today  [%.0f kWh]" { channel="modbus:heatpump:stiebelEltron:energyInformation#consumption_water_today" }
Number:Energy stiebel_eltron_consumption_water_total "Water heating power consumption total [%.3f MWh]"  {channel="modbus:heatpump:stiebelEltron:energyInformation#consumption_water_total"}

```

### Sitemap Configuration

```perl
Text label="Heat pump" icon="temperature" {
 Frame label="Operation Mode" {
  Default item=stiebel_eltron_mode_pump
  Default item=stiebel_eltron_mode_heating
  Default item=stiebel_eltron_mode_water
  Default item=stiebel_eltron_mode_cooling
  Default item=stiebel_eltron_mode_summer
 }
 Frame label= "State" {
  Default item=stiebel_eltron_operation_mode icon="settings"
  Default item=stiebel_eltron_outdoor_temp  icon="temperature"
  Default item=stiebel_eltron_temp_hk1  icon="temperature"
  Default item=stiebel_eltron_setpoint_hk1  icon="temperature"
  Default item=stiebel_eltron_supply_temp  icon="temperature"
  Default item=stiebel_eltron_return_temp  icon="temperature"
  Default item=stiebel_eltron_temp_water  icon="temperature"
  Default item=stiebel_eltron_setpoint_water icon="temperature"
  Default item=stiebel_eltron_temperature_ffk  icon="temperature"
  Default item=stiebel_eltron_setpoint_ffk icon="temperature"
  Default item=stiebel_eltron_humidity_ffk icon="humidity"
  Default item=stiebel_eltron_dewpoint_ffk icon="temperature"
  Default item=stiebel_eltron_source_temp icon="temperature"
 }
 Frame label="Paramters" {
  Setpoint item=stiebel_eltron_heating_comfort_temp icon="temperature" step=1 minValue=5 maxValue=30
  Setpoint item=stiebel_eltron_heating_eco_temp icon="temperature" step=1 minValue=5 maxValue=30
  Setpoint item=stiebel_eltron_water_comfort_temp icon="temperature" step=1 minValue=10 maxValue=60
  Setpoint item=stiebel_eltron_water_eco_temp icon="temperature" step=1 minValue=10 maxValue=60
 }
 Frame label="Energy consumption" {
  Default item=stiebel_eltron_consumption_heat_today icon="energy"
  Default item=stiebel_eltron_consumption_heat_total icon="energy"
  Default item=stiebel_eltron_consumption_water_today icon="energy"
  Default item=stiebel_eltron_consumption_water_total icon="energy"
 }
 Frame label="Heat quantity" {
  Default item=stiebel_eltron_production_heat_today icon="radiator"
  Default item=stiebel_eltron_production_heat_total icon="radiator"
  Default item=stiebel_eltron_production_water_today icon="water"
  Default item=stiebel_eltron_production_water_total icon="water"
 }

}

```


## Full Example for the thing *Stiebel Eltron Heat Pump (WPM compatible)*

### Thing Configuration

```java
Bridge modbus:tcp:bridge "Stiebel Modbus TCP"[ host="hostname|ip", port=502, id=1 ]
Thing modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp "Stiebel Eltron Heat Pump (WPM compatible)" (modbus:tcp:bridge) @"Room" [ ]
```

### Item Configuration *Stiebel Eltron Heat Pump (WPM compatible)*

```java
Contact                   stiebel_eltron_heat_pump_allwpm_hc1_pump_active                            "HC1 Pump Active"                                <pump>        { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemStateAllWpm#hc1-pump-active" }
Contact                   stiebel_eltron_heat_pump_allwpm_hc2_pump_active                            "HC2 Pump Active"                                <pump>        { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemStateAllWpm#hc2-pump-active" }
Contact                   stiebel_eltron_heat_pump_allwpm_heatup_program_active                      "Heat-Up Program Active"                         <status>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemStateAllWpm#heat-up-program-active" }
Contact                   stiebel_eltron_heat_pump_allwpm_nhz_stages_active                          "NHZ Stages Active"                              <status>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemStateAllWpm#nhz-stages-running" }
Contact                   stiebel_eltron_heat_pump_allwpm_currently_heating                          "Currently Heating"                              <status>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemStateAllWpm#hp-in-heating-mode" }
Contact                   stiebel_eltron_heat_pump_allwpm_currently_heating_hot_water                "Currently Heating Hot Water"                    <status>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemStateAllWpm#hp-in-hotwater-mode" }
Contact                   stiebel_eltron_heat_pump_allwpm_compressor_running                         "Compressor Running"                             <status>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemStateAllWpm#compressor-running" }
Contact                   stiebel_eltron_heat_pump_allwpm_summer_mode_active                         "Summer Mode Active"                             <status>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemStateAllWpm#summer-mode-active" }
Contact                   stiebel_eltron_heat_pump_allwpm_currently_cooling                          "Currently Cooling"                              <status>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemStateAllWpm#cooling-mode-active" }
Contact                   stiebel_eltron_heat_pump_allwpm_min_one_iws_in_defrosting_mode             "Min. one IWS In Defrosting Mode"                <status>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemStateAllWpm#min-one-iws-in-defrosting-mode" }
Contact                   stiebel_eltron_heat_pump_allwpm_silent_mode_1_active                       "Silent Mode 1 Active"                           <status>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemStateAllWpm#silent-mode1-active" }
Contact                   stiebel_eltron_heat_pump_allwpm_silent_mode_2_active                       "Silent Mode 2 Active"                           <status>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemStateAllWpm#silent-mode2-active" }
Contact                   stiebel_eltron_heat_pump_allwpm_power_off                                  "Power Off"                                      <status>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemStateAllWpm#power-off" }
Contact                   stiebel_eltron_heat_pump_allwpm_comp1_active                               "Compressor 1 Active"                            <status>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemStateAllWpm#compressor1-active" }
Contact                   stiebel_eltron_heat_pump_allwpm_comp2_active                               "Compressor 2 Active"                            <status>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemStateAllWpm#compressor2-active" }
Contact                   stiebel_eltron_heat_pump_allwpm_comp3_active                               "Compressor 3 Active"                            <status>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemStateAllWpm#compressor3-active" }
Contact                   stiebel_eltron_heat_pump_allwpm_comp4_active                               "Compressor 4 Active"                            <status>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemStateAllWpm#compressor4-active" }
Contact                   stiebel_eltron_heat_pump_allwpm_comp5_active                               "Compressor 5 Active"                            <status>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemStateAllWpm#compressor5-active" }
Contact                   stiebel_eltron_heat_pump_allwpm_comp6_active                               "Compressor 6 Active"                            <status>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemStateAllWpm#compressor6-active" }
Contact                   stiebel_eltron_heat_pump_allwpm_bc_pump1_active                            "Buffer Charging Pump 1 Active"                  <status>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemStateAllWpm#buffer-charging-pump1-active" }
Contact                   stiebel_eltron_heat_pump_allwpm_bc_pump2_active                            "Buffer Charging Pump 2 Active"                  <status>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemStateAllWpm#buffer-charging-pump2-active" }
Contact                   stiebel_eltron_heat_pump_allwpm_bc_pump3_active                            "Buffer Charging Pump 3 Active"                  <status>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemStateAllWpm#buffer-charging-pump3-active" }
Contact                   stiebel_eltron_heat_pump_allwpm_bc_pump4_active                            "Buffer Charging Pump 4 Active"                  <status>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemStateAllWpm#buffer-charging-pump4-active" }
Contact                   stiebel_eltron_heat_pump_allwpm_bc_pump5_active                            "Buffer Charging Pump 5 Active"                  <status>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemStateAllWpm#buffer-charging-pump5-active" }
Contact                   stiebel_eltron_heat_pump_allwpm_bc_pump6_active                            "Buffer Charging Pump 6 Active"                  <status>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemStateAllWpm#buffer-charging-pump6-active" }
Contact                   stiebel_eltron_heat_pump_allwpm_nhz1_active                                "Electric Heating Stage 1 Active"                <status>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemStateAllWpm#nhz1-active" }
Contact                   stiebel_eltron_heat_pump_allwpm_nhz2_active                                "Electric Heating Stage 2 Active"                <status>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemStateAllWpm#nhz2-active" }
Number                    stiebel_eltron_heat_pump_allwpm_fault_status                               "Fault Status"                                   <status>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemStateAllWpm#fault-status" }
Number                    stiebel_eltron_heat_pump_allwpm_bus_status                                 "Bus Status"                                     <status>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemStateAllWpm#bus-status" }
Number                    stiebel_eltron_heat_pump_allwpm_defrost_initiated                          "Defrost Initiated"                              <status>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemStateAllWpm#defrost-initiated" }
Number                    stiebel_eltron_heat_pump_allwpm_active_error                               "Active Error"                                   <status>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemStateAllWpm#active-error" }

Number                    stiebel_eltron_heat_pump_allwpm_operating_mode                             "Operating Mode"                                 <heating>     { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemParameterAllWpm#operating-mode" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_hc1_comfort_temperature                    "HC1 Comfort Temperature"                        <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemParameterAllWpm#hc1-comfort-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_hc1_eco_temperature                        "HC1 Eco Temperature"                            <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemParameterAllWpm#hc1-eco-temperature" }
Number                    stiebel_eltron_heat_pump_allwpm_hc1_heating_curve_rise                     "HC1 Heating Curve Rise"                         <line>        { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemParameterAllWpm#hc1-heating-curve-rise" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_hc2_comfort_temperature                    "HC2 Comfort Temperature"                        <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemParameterAllWpm#hc2-comfort-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_hc2_eco_temperature                        "HC2 Eco Temperature"                            <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemParameterAllWpm#hc2-eco-temperature" }
Number                    stiebel_eltron_heat_pump_allwpm_hc2_heating_curve_rise                     "HC2 Heating Curve Rise"                         <line>        { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemParameterAllWpm#hc2-heating-curve-rise" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_fixed_value_operation                      "Fixed Value Operation"                          <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemParameterAllWpm#fixed-value-operation" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_heating_dualmode_temperature_bivalence     "Heating DualMode Temperature (bivalence)"       <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemParameterAllWpm#heating-dual-mode-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_hot_water_comfort_temperature              "Hot Water Comfort Temperature"                  <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemParameterAllWpm#hotwater-comfort-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_hot_water_eco_temperature                  "Hot Water Eco Temperature"                      <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemParameterAllWpm#hotwater-eco-temperature" }
Number                    stiebel_eltron_heat_pump_allwpm_hot_water_stages                           "Hot Water Stages"                               <status>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemParameterAllWpm#hotwater-stages" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_hot_water_dualmode_temperature_bivalence   "Hot Water DualMode Temperature (bivalence)"     <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemParameterAllWpm#hotwater-dual-mode-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_area_cooling_flow_temperature_setpoint     "Area Cooling Flow Temperature Setpoint"         <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemParameterAllWpm#area-cooling-flow-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_area_cooling_flow_temperature_hysteresis   "Area Cooling Flow Temperature Hysteresis"       <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemParameterAllWpm#area-cooling-flow-temperature-hysteresis" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_area_cooling_room_temperature_setpoint     "Area Cooling Room Temperature Setpoint"         <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemParameterAllWpm#area-cooling-room-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_fan_cooling_flow_temperature_setpoint      "Fan Cooling Flow Temperature Setpoint"          <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemParameterAllWpm#fan-cooling-flow-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_fan_cooling_flow_temperature_hysteresis    "Fan Cooling Flow Temperature Hysteresis"        <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemParameterAllWpm#fan-cooling-flow-temperature-hysteresis" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_fan_cooling_room_temperature_setpoint      "Fan Cooling Room Temperature Setpoint"          <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemParameterAllWpm#fan-cooling-room-temperature-setpoint" }
Number                    stiebel_eltron_heat_pump_allwpm_reset                                      "Reset"                                          <settings>    { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemParameterAllWpm#reset" }
Number                    stiebel_eltron_heat_pump_allwpm_restart_isg                                "Restart ISG"                                    <settings>    { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemParameterAllWpm#restart-isg" }

Number:Temperature        stiebel_eltron_heat_pump_allwpm_fe7_temperature                            "FE7 Temperature"                                <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#fe7-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_fe7_temperature_setpoint                   "FE7 Temperature Setpoint"                       <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#fe7-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_ffktemperature                             "FFK Temperature"                                <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#fek-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_ffk_temperature_setpoint                   "FFK Temperature Setpoint"                       <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#fek-temperature-setpoint" }
Number:Dimensionless      stiebel_eltron_heat_pump_allwpm_ffk_humidity                               "FFK Humidity"                                   <humidity>    { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#fek-humidity" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_ffk_dewpoint                               "FFK Dewpoint"                                   <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#fek-dewpoint" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_outdoor_temperature                        "Outdoor Temperature"                            <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#outdoor-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_hc1_temperature                            "HC1 Temperature"                                <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#hc1-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_hc1_temperature_setpoint                   "HC1 Temperature Setpoint"                       <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#hc1-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_hc2_temperature                            "HC2 Temperature"                                <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#hc2-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_hc2_temperature_setpoint                   "HC2 Temperature Setpoint"                       <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#hc2-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_heat_pump_flow_temperature                 "Heat Pump Flow Temperature"                     <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#hp-flow-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_electric_reheating_flow_temperature        "Electric Rehating Flow Temperature"             <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#nhz-flow-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_flow_temperature                           "Flow Temperature"                               <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#flow-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_return_temperature                         "Return Temperature"                             <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#return-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_fixed_temperature_setpoint                 "Fixed Temperature Setpoint"                     <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#fixed-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_buffer_temperature                         "Buffer Temperature"                             <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#buffer-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_buffer_temperature_setpoint                "Buffer Temperature Setpoint"                    <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#buffer-temperature-setpoint" }
Number:Pressure           stiebel_eltron_heat_pump_allwpm_heating_pressure                           "Heating Pressure"                               <pressure>    { unit="bar", channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#heating-pressure" }
Number:VolumetricFlowRate stiebel_eltron_heat_pump_allwpm_flow_rate                                  "Flow Rate"                                      <flow>        { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#flow-rate" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_hot_water_temperature                      "Hot Water Temperature"                          <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#hotwater-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_hot_water_temperature_setpoint             "Hot Water Temperature Setpoint"                 <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#hotwater-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_fan_cooling_temperature                    "Fan Cooling Temperature"                        <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#fan-cooling-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_fan_cooling_temperature_setpoint           "Fan Cooling Temperature Setpoint"               <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#fan-cooling-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_area_cooling_temperature                   "Area Cooling Temperature"                       <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#area-cooling-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_area_cooling_temperature_setpoint          "Area Cooling Temperature Setpoint"              <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#area-cooling-temperature-setpoint" }
Number                    stiebel_eltron_heat_pump_allwpm_solar_thermal_collector_temperature        "Solar Thermal Collector Temperature"            <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#solar-thermal-collector-temperature" }
Number                    stiebel_eltron_heat_pump_allwpm_solar_thermal_cylinder_runtime             "Solar Thermal Cylinder Runtime"                 <time>        { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#solar-thermal-runtime" }
Number                    stiebel_eltron_heat_pump_allwpm_solar_thermal_cylinder_temperature         "Solar Thermal Cylinder Temperature"             <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#solar-thermal-cylinder-temperature" }
Number                    stiebel_eltron_heat_pump_allwpm_external_heat_source_runtime               "External Heat Source Runtime"                   <time>        { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#external-heat-source-runtime" }
Number                    stiebel_eltron_heat_pump_allwpm_external_heat_source_temperature           "External Heat Source Temperature"               <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#external-heat-source-temperature" }
Number                    stiebel_eltron_heat_pump_allwpm_external_heat_source_temperature_setpoint  "External Heat Source Temperature Setpoint"      <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#external-heat-source-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_lower_application_limit_heating            "Lower Application Limit Heating"                <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#lower-application-limit-heating" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_lower_application_limit_hot_water          "Lower Application Limit Hot Water"              <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#lower-application-limit-hotwater" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_source_temperature                         "Source Temperature"                             <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#source-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_minimal_source_temperature                 "Minimal Source Temperature"                     <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#min-source-temperature" }
Number:Pressure           stiebel_eltron_heat_pump_allwpm_source_pressure                            "Source Pressure"                                <pressure>    { unit="bar", channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#source-pressure" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_hot_gas_temperature                        "Hot Gas Temperature"                            <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#hotgas-temperature" }
Number:Pressure           stiebel_eltron_heat_pump_allwpm_high_pressure                              "High Pressure"                                  <pressure>    { unit="bar", channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#high-pressure" }
Number:Pressure           stiebel_eltron_heat_pump_allwpm_low_pressure                               "Low Pressure"                                   <pressure>    { unit="bar", channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#low-pressure" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_hp1_return_temperature                     "HP1 Return Temperature"                         <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#hp1-return-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_hp1_flow_temperature                       "HP1 Flow Temperature"                           <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#hp1-flow-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_allwpm_hp1_hot_gas_temperature                    "HP1 Hot Gas Temperature"                        <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#hp1-hotgas-temperature" }
Number:Pressure           stiebel_eltron_heat_pump_allwpm_hp1_logw_pressure                          "HP1 Low Pressure"                               <temperature> { unit="bar", channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#hp1-low-pressure" }
Number:Pressure           stiebel_eltron_heat_pump_allwpm_hp1_mean_pressure                          "HP1 Mean Pressure"                              <temperature> { unit="bar", channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#hp1-mean-pressure" }
Number:Pressure           stiebel_eltron_heat_pump_allwpm_hp1_high_pressure                          "HP1 High Pressure"                              <temperature> { unit="bar", channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#hp1-high-pressure" }
Number:VolumetricFlowRate stiebel_eltron_heat_pump_allwpm_hp1_flow_rate                              "HP1 Flow Rate"                                  <temperature> { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:systemInformationAllWpm#hp1-flow-rate" }
...

Number:Energy             stiebel_eltron_heat_pump_allwpm_heat_quantity_today                      "Heat Quantity Today [%.0f kWh]"                      <energy>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:energyRuntimeInformationAllWpm#production-heat-today" }
Number:Energy             stiebel_eltron_heat_pump_allwpm_heat_quantity_total                      "Heat Quantity Total [%.3f MWh]"                      <energy>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:energyRuntimeInformationAllWpm#production-heat-total" }
Number:Energy             stiebel_eltron_heat_pump_allwpm_water_heat_quantity_today                "Water Heat Quantity Today [%.0f kWh]"                <energy>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:energyRuntimeInformationAllWpm#production-water-today" }
Number:Energy             stiebel_eltron_heat_pump_allwpm_water_heat_quantity_total                "Water Heat Quantity Total [%.3f MWh]"                <energy>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:energyRuntimeInformationAllWpm#production-water-total" }
Number:Energy             stiebel_eltron_heat_pump_allwpm_nhz_heating_quantity_total               "NHZ Heating Quantity Total [%.3f MWh]"               <energy>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:energyRuntimeInformationAllWpm#production-nhz-heat-total" }
Number:Energy             stiebel_eltron_heat_pump_allwpm_nhz_hot_water_quantity_total             "NHZ Hot Water Quantity Total [%.3f MWh]"             <energy>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:energyRuntimeInformationAllWpm#production-nhz-water-total" }
Number:Energy             stiebel_eltron_heat_pump_allwpm_power_consumption_for_heating_today      "Power Consumption for Heating Today [%.0f kWh]"      <energy>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:energyRuntimeInformationAllWpm#consumption-heat-today" }
Number:Energy             stiebel_eltron_heat_pump_allwpm_power_consumption_for_heating_total      "Power Consumption for Heating Total [%.0f kWh]"      <energy>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:energyRuntimeInformationAllWpm#consumption-heat-total" }
Number:Energy             stiebel_eltron_heat_pump_allwpm_power_consumption_for_water_today        "Power Consumption for Water Today [%.0f kWh]"        <energy>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:energyRuntimeInformationAllWpm#consumption-water-today" }
Number:Energy             stiebel_eltron_heat_pump_allwpm_power_consumption_for_water_total        "Power Consumption for Water Total [%.0f kWh]"        <energy>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:energyRuntimeInformationAllWpm#consumption-water-total" }
Number:Time               stiebel_eltron_heat_pump_allwpm_heating_compressor_runtime               "Heating Compressor Runtime"                          <time>        { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:energyRuntimeInformationAllWpm#heating-runtime" }
Number:Time               stiebel_eltron_heat_pump_allwpm_hot_water_runtime                        "Hot Water Compressor Runtime"                        <time>        { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:energyRuntimeInformationAllWpm#hotwater-runtime" }
Number:Time               stiebel_eltron_heat_pump_allwpm_cooling_runtime                          "Cooling Compressor Runtime"                          <time>        { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:energyRuntimeInformationAllWpm#cooling-runtime" }
Number:Time               stiebel_eltron_heat_pump_allwpm_electric_reheating_stage_1_runtime       "Electric Rehating Stage 1 Runtime"                   <time>        { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:energyRuntimeInformationAllWpm#nhz1-runtime" }
Number:Time               stiebel_eltron_heat_pump_allwpm_electric_reheating_stage_2_runtime       "Electric Rehating Stage 2 Runtime"                   <time>        { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:energyRuntimeInformationAllWpm#nhz2-runtime" }
Number:Time               stiebel_eltron_heat_pump_allwpm_electric_reheating_stages_12_runtime     "Electric Rehating Stages 1+2 Runtime"                <time>        { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:energyRuntimeInformationAllWpm#nhz12-runtime" }
Number:Energy             stiebel_eltron_heat_pump_allwpm_hp1_heat_quantity_today                  "HP 1 Heat Quantity Today [%.0f kWh]"                 <energy>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:energyRuntimeInformationAllWpm#hp1-production-heat-today" }
Number:Energy             stiebel_eltron_heat_pump_allwpm_hp1_heat_quantity_total                  "HP 1 Heat Quantity Total [%.3f MWh]"                 <energy>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:energyRuntimeInformationAllWpm#hp1-production-heat-total" }
Number:Energy             stiebel_eltron_heat_pump_allwpm_hp1_water_heat_quantity_today            "HP 1 Water Heat Quantity Today [%.0f kWh]"           <energy>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:energyRuntimeInformationAllWpm#hp1-production-water-today" }
Number:Energy             stiebel_eltron_heat_pump_allwpm_hp1_water_heat_quantity_total            "HP 1 Water Heat Quantity Total [%.3f MWh]"           <energy>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:energyRuntimeInformationAllWpm#hp1-production-water-total" }
Number:Energy             stiebel_eltron_heat_pump_allwpm_hp1_nhz_heating_quantity_total           "HP 1 NHZ Heating Quantity Total [%.3f MWh]"          <energy>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:energyRuntimeInformationAllWpm#hp1-production-nhz-heat-total" }
Number:Energy             stiebel_eltron_heat_pump_allwpm_hp1_nhz_hot_water_quantity_total         "HP 1 NHZ Hot Water Quantity Total [%.3f MWh]"        <energy>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:energyRuntimeInformationAllWpm#hp1-production-nhz-water-total" }
Number:Energy             stiebel_eltron_heat_pump_allwpm_hp1_power_consumption_for_heating_today  "HP 1 Power Consumption for Heating Today [%.0f kWh]" <energy>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:energyRuntimeInformationAllWpm#hp1-consumption-heat-today" }
Number:Energy             stiebel_eltron_heat_pump_allwpm_hp1_power_consumption_for_heating_total  "HP 1 Power Consumption for Heating Total [%.0f kWh]" <energy>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:energyRuntimeInformationAllWpm#hp1-consumption-heat-total" }
Number:Energy             stiebel_eltron_heat_pump_allwpm_hp1_power_consumption_for_water_today    "HP 1 Power Consumption for Water Today [%.0f kWh]"   <energy>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:energyRuntimeInformationAllWpm#hp1-consumption-water-today" }
Number:Energy             stiebel_eltron_heat_pump_allwpm_hp1_power_consumption_for_water_total    "HP 1 Power Consumption for Water Total [%.0f kWh]"   <energy>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:energyRuntimeInformationAllWpm#hp1-consumption-water-total" }
Number:Time               stiebel_eltron_heat_pump_allwpm_hp1_heating_compressor1_runtime          "HP 1 Heating Compressor 1 Runtime"                   <time>        { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:energyRuntimeInformationAllWpm#hp1-cp1-heating-runtime" }
Number:Time               stiebel_eltron_heat_pump_allwpm_hp1_heating_compressor2_runtime          "HP 1 Heating Compressor 2 Runtime"                   <time>        { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:energyRuntimeInformationAllWpm#hp1-cp2-heating-runtime" }
Number:Time               stiebel_eltron_heat_pump_allwpm_hp1_heating_compressor12_runtime         "HP 1 Heating Compressor 1+2 Runtime"                 <time>        { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:energyRuntimeInformationAllWpm#hp1-cp12-heating-runtime" }
Number:Time               stiebel_eltron_heat_pump_allwpm_hp1_hot_water_comp1_runtime              "HP 1 Hot Water Compressor 1 Runtime"                 <time>        { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:energyRuntimeInformationAllWpm#hp1-cp1-hotwater-runtime" }
Number:Time               stiebel_eltron_heat_pump_allwpm_hp1_hot_water_comp2_runtime              "HP 1 Hot Water Compressor 2 Runtime"                 <time>        { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:energyRuntimeInformationAllWpm#hp1-cp2-hotwater-runtime" }
Number:Time               stiebel_eltron_heat_pump_allwpm_hp1_hot_water_comp12_runtime             "HP 1 Hot Water Compressor 1+2 Runtime"               <time>        { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:energyRuntimeInformationAllWpm#hp1-cp12-hotwater-runtime" }
Number:Time               stiebel_eltron_heat_pump_allwpm_hp1_cooling_runtime                      "HP 1 Cooling Compressor Runtime"                     <time>        { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:energyRuntimeInformationAllWpm#hp1-cooling-runtime" }
Number:Time               stiebel_eltron_heat_pump_allwpm_hp1_electric_reheating_stage_1_runtime   "HP 1 Electric Rehating Stage 1 Runtime"              <time>        { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:energyRuntimeInformationAllWpm#hp1-nhz1-runtime" }
Number:Time               stiebel_eltron_heat_pump_allwpm_hp1_electric_reheating_stage_2_runtime   "HP 1 Electric Rehating Stage 2 Runtime"              <time>        { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:energyRuntimeInformationAllWpm#hp1-nhz2-runtime" }
Number:Time               stiebel_eltron_heat_pump_allwpm_hp1_electric_reheating_stages_12_runtime "HP 1 Electric Rehating Stages 1+2 Runtime"           <time>        { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:energyRuntimeInformationAllWpm#hp1-nhz12-runtime" }
...

Number                    stiebel_eltron_sg_ready_on_off_switch                                   "SG Ready Energy Management Operating State"     <settings>    { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:sgReadyEnergyManagementSettings#sg-ready-on-off-switch" }
Number                    stiebel_eltron_sg_ready_input_lines                                     "SG Ready Input Lines"                           <settings>    { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:sgReadyEnergyManagementSettings#sg-ready-input-lines" }

Number                    stiebel_eltron_sg_ready_operating_state                                 "SG Ready Energy Management Operating State"     <status>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:sgReadyEnergyManagementSystemInformation#sg-ready-operating-state" }
Number                    stiebel_eltron_sg_ready_controller_identification                       "SG Ready Controller Identification"             <status>      { channel="modbus:stiebeleltron-heatpump-allwpm:stiebelEltronWpmComp:sgReadyEnergyManagementSystemInformation#sg-ready-controller-identification" }
```
