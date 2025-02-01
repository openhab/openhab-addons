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

| Thing                                | ThingTypeID                      | Description                                                                                                 |
| ------------------------------------ | -------------------------------- | ----------------------------------------------------------------------------------------------------------- |
| Stiebel Eltron Heat Pump             | heatpump                         | A Stiebel Eltron Heat Pump connected through CAN to an ISG                                                  |
| Stiebel Eltron Heat Pump (WPMsystem) | stiebeleltron-heatpump-wpmsystem | A Stiebel Eltron Heat Pump (WPMsystem compatible) connected through CAN to an ISG                           |
| Stiebel Eltron Heat Pump (WPM3)      | stiebeleltron-heatpump-wpm3      | A Stiebel Eltron Heat Pump (WPM3 compatible) connected through CAN to an ISG                                |
| Stiebel Eltron Heat Pump (WPM3i)     | stiebeleltron-heatpump-wpm3i     | A Stiebel Eltron Heat Pump (WPM3i compatible) connected through CAN to an ISG                               |
| Stiebel Eltron ISG plus SG Ready EM  | stiebeleltron-isg-sg-ready-em    | The Stiebel Eltron ISG plus SG Ready Energy Management                                                      |

The thing *Stiebel Eltron Heat Pump* and its channel IDs have been kept for compatibility reasons.
It's recommended to switch to one of the new things as they support much more channels.
The thing *Stiebel Eltron ISG SG Ready EM* is for users that have an ISGplus with SG Ready Energy Management. Please add this thing additionally to the heat pump.

## Discovery

This extension does not support auto-discovery. The things need to be added manually.

## Thing Configuration

You need first to set up a TCP Modbus bridge according to the Modbus documentation.
A typical bridge configuration would look like this:

```java
Bridge modbus:tcp:bridge [ host="10.0.0.2", port=502, id=1 ]
```

Things in this extension will use the selected bridge to connect to the device.

The following parameters are valid for all thing types:

| Parameter | Type    | Required | Default if omitted | Description                                                                |
| --------- | ------- | -------- | ------------------ | -------------------------------------------------------------------------- |
| refresh   | integer | no       | 5                  | Poll interval in seconds. Increase this if you encounter connection errors |
| maxTries  | integer | no       | 3                  | Number of retries when before giving up reading from this thing.           |

A typcial bridge and thing setup would look like this:

```java
Bridge modbus:tcp:bridge [ host="10.0.0.2", port=502, id=1 ] {
    Thing heatpump StiebelEltronHP "Stiebel Eltron Heat Pump" (modbus:tcp:bridge) @"room"  [ ]
}
```

## Channels

Channels are grouped into channel groups.
The heat pump things *Stiebel Eltron Heat Pump (WPMsystem)*, *Stiebel Eltron Heat Pump (WPM3)* and *Stiebel Eltron Heat Pump (WPM3i)* support more channels.

### System State Groups

This groups contain general state information about the heat pump.

#### Channels supported by thing *Stiebel Eltron Heat Pump*

| Channel ID       | Item Type | Read only | Description                                                   |
| ---------------- | --------- | --------- | ------------------------------------------------------------- |
| is-heating       | Contact   | true      | OPEN in case the heat pump is currently in heating mode       |
| is-heating-water | Contact   | true      | OPEN in case the heat pump is currently in heating water mode |
| is-cooling       | Contact   | true      | OPEN in case the heat pump is currently in cooling mode       |
| is-pumping       | Contact   | true      | OPEN in case the heat pump is currently in pumping mode       |
| is-summer        | Contact   | true      | OPEN in case the heat pump is currently in summer mode        |

#### Channels supported by things *Stiebel Eltron Heat Pump (WPMsystem)*, *Stiebel Eltron Heat Pump (WPM3)* and *Stiebel Eltron Heat Pump (WPM3i)*

Note: The column WPM is for WPMsystem.

| Channel ID                     | Item Type | Read only | Description                                                            | WPM | WPM3 | WPM3i |
| ------------------------------ | --------- | --------- | ---------------------------------------------------------------------- | --- | ---- | ----- |
| hc1-pump-active                | Contact   | true      | OPEN in case the heat circuit 1 pump is currently running              |  x  |  x   |   x   |
| hc2-pump-active                | Contact   | true      | OPEN in case the heat circuit 2 pump is currently running              |  x  |  x   |   x   |
| heat-up-program-active         | Contact   | true      | OPEN in case the heat-up program is currently running                  |  x  |  x   |   x   |
| nhz-stages-running             | Contact   | true      | OPEN in case if any emergency heat stage is currently running          |  x  |  x   |   x   |
| hp-in-heating-mode             | Contact   | true      | OPEN in case the heat pump is currently in heating mode                |  x  |  x   |   x   |
| hp-in-hotwater-mode            | Contact   | true      | OPEN in case the heat pump is currently in heating water mode          |  x  |  x   |   x   |
| compressor-running             | Contact   | true      | OPEN in case the compressor is currently running                       |  x  |  x   |   x   |
| summer-mode-active             | Contact   | true      | OPEN in case the heat pump is currently in summer mode                 |  x  |  x   |   x   |
| cooling-mode-active            | Contact   | true      | OPEN in case the heat pump is currently in cooling mode                |  x  |  x   |   x   |
| min-one-iws-in-defrosting-mode | Contact   | true      | OPEN in case at least one IWS is in defrosting mode                    |  x  |  x   |   x   |
| silent-mode1-active            | Contact   | true      | OPEN in case the silent mode 1 is currently active                     |  x  |  x   |   x   |
| silent-mode2-active            | Contact   | true      | OPEN in case the silent mode 2 is currently active (heat pump off)     |  x  |  x   |   x   |
| power-off                      | Contact   | true      | OPEN in case the heat pump is currently blocked by the power company   |  x  |  x   |   x   |
| compressor1-active             | Contact   | true      | OPEN in case the compressor 1 is currently running                     |  x  |  x   |       |
| compressor2-active             | Contact   | true      | OPEN in case the compressor 2 is currently running                     |  x  |  x   |       |
| compressor3-active             | Contact   | true      | OPEN in case the compressor 3 is currently running                     |  x  |  x   |       |
| compressor4-active             | Contact   | true      | OPEN in case the compressor 4 is currently running                     |  x  |  x   |       |
| compressor5-active             | Contact   | true      | OPEN in case the compressor 5 is currently running                     |  x  |  x   |       |
| compressor6-active             | Contact   | true      | OPEN in case the compressor 6 is currently running                     |  x  |  x   |       |
| buffer-charging-pump1-active   | Contact   | true      | OPEN in case the buffer charing pump 1 is currently running            |  x  |  x   |       |
| buffer-charging-pump2-active   | Contact   | true      | OPEN in case the buffer charing pump 2 is currently running            |  x  |  x   |       |
| buffer-charging-pump3-active   | Contact   | true      | OPEN in case the buffer charing pump 3 is currently running            |  x  |  x   |       |
| buffer-charging-pump4-active   | Contact   | true      | OPEN in case the buffer charing pump 4 is currently running            |  x  |  x   |       |
| buffer-charging-pump5-active   | Contact   | true      | OPEN in case the buffer charing pump 5 is currently running            |  x  |  x   |       |
| buffer-charging-pump6-active   | Contact   | true      | OPEN in case the buffer charing pump 6 is currently running            |  x  |  x   |       |
| nhz1-active                    | Contact   | true      | OPEN in case the emergency heat stage 1 is currently running           |  x  |  x   |       |
| nhz2-active                    | Contact   | true      | OPEN in case the emergency heat stage 2 is currently running           |  x  |  x   |       |
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

#### Channels supported by things *Stiebel Eltron Heat Pump (WPMsystem)*, *Stiebel Eltron Heat Pump (WPM3)* and *Stiebel Eltron Heat Pump (WPM3i)*

Note: The column WPM is for WPMsystem.

| Channel ID                                | Item Type          | Read only | Description                                                                                      | WPM | WPM3 | WPM3i |
| ----------------------------------------- | ------------------ | --------- | ------------------------------------------------------------------------------------------------ | --- | ---- | ----- |
| operating-mode                            | Number             | false     | The current operation mode of the heat pump                                                      |  x  |  x   |   x   |
|                                           |                    |           | 0=emergency mode, 1=ready mode, 2=program mode, 3=comfort mode, 4=eco mode, 5=heating water mode |  x  |  x   |   x   |
| hc1-comfort-temperature                   | Number:Temperature | false     | The current heating comfort temperature of heat circuit 1                                        |  x  |  x   |   x   |
| hc1-eco-temperature                       | Number:Temperature | false     | The current heating eco temperature of heat circuit 1                                            |  x  |  x   |   x   |
| hc1-heating-curve-rise                    | Number             | false     | The current heating curve rise of heat circuit 1                                                 |  x  |  x   |   x   |
| hc2-comfort-temperature                   | Number:Temperature | false     | The current heating comfort temperature of heat circuit 2                                        |  x  |  x   |   x   |
| hc2-eco-temperature                       | Number:Temperature | false     | The current heating eco temperature of heat circuit 2                                            |  x  |  x   |   x   |
| hc2-heating-curve-rise                    | Number             | false     | The current heating curve rise of heat circuit 2                                                 |  x  |  x   |   x   |
| fixed-value-operation                     | Number:Temperature | false     | The current heating temperature of the fixed value operation                                     |  x  |  x   |   x   |
| heating-dual-mode-temperature             | Number:Temperature | false     | The current heating temperature of the dual mode operation                                       |     |  x   |   x   |
| hotwater-comfort-temperature              | Number:Temperature | false     | The current hot water comfort temperature                                                        |  x  |  x   |   x   |
| hotwater-eco-temperature                  | Number:Temperature | false     | The current hot water eco temperature                                                            |  x  |  x   |   x   |
| hotwater-stages                           | Number             | false     | The current number of active hot water stages                                                    |  x  |  x   |   x   |
| hotwater-dual-mode-temperature            | Number:Temperature | false     | The current hot water temperature of the dual mode operation                                     |  x  |  x   |   x   |
| area-cooling-flow-temperature-setpoint    | Number:Temperature | false     | The current area cooling flow setpoint temperature                                               |  x  |  x   |   x   |
| area-cooling-flow-temperature-hysteresis  | Number:Temperature | false     | The current area cooling flow temperature hysteresis                                             |     |  x   |   x   |
| area-cooling-room-temperature-setpoint    | Number:Temperature | false     | The current area cooling room setoint temperature                                                |  x  |  x   |   x   |
| fan-cooling-flow-temperature-setpoint     | Number:Temperature | false     | The current fan cooling flow setpoint temperature                                                |  x  |  x   |   x   |
| fan-cooling-flow-temperature-hysteresis   | Number:Temperature | false     | The current fan cooling flow temperature hysteresis                                              |  x  |  x   |   x   |
| fan-cooling-room-temperature-setpoint     | Number:Temperature | false     | The current fan cooling room temperature hysteresis                                              |  x  |  x   |   x   |
| reset                                     | Number             | false     | Reset heat pump / 0=off, 1=system reset (factory reset), 2=reset fault list, 3=reset heat pump   |  x  |  x   |   x   |
| restart-isg                               | Number             | false     | Restart ISG command / 0=off, 1=restart, 2=service key                                            |  x  |  x   |   x   |

##### Note

Channel 'reset': The binding only accepts commands 2 and 3 - command 1 (system reset) is ignored to prevent factory reset by accident.
Channel 'restart-isg': The binding only accepts command 1 - command 2 (service key) is ignored as impact of command is not known to developer.


### System Information Group

This group contains general operational information about the device.

#### Channels supported by thing *Stiebel Eltron Heat Pump*

| Channel ID                 | Item Type            | Read only | Description                                           |
| -------------------------- | -------------------- | --------- | ----------------------------------------------------- |
| fek-temperature            | Number:Temperature   | true      | The current temperature measured by the FEK           |
| fek-temperature-setpoint   | Number:Temperature   | true      | The current set point of the FEK temperature          |
| fek-humidity               | Number:Dimensionless | true      | The current humidity measured by the FEK              |
| fek-dewpoint               | Number:Temperature   | true      | The current dew point temperature measured by the FEK |
| outdoor-temperature        | Number:Temperature   | true      | The current outdoor temperature                       |
| hk1-temperature            | Number:Temperature   | true      | The current temperature of the HK1                    |
| hk1-temperature-setpoint   | Number:Temperature   | true      | The current temperature set point of the HK1          |
| supply-temperature         | Number:Temperature   | true      | The current supply temperature                        |
| return-temperature         | Number:Temperature   | true      | The current return temperature                        |
| source-temperature         | Number:Temperature   | true      | The current source temperature                        |
| water-temperature          | Number:Temperature   | true      | The current water temperature                         |
| water-temperature-setpoint | Number:Temperature   | true      | The current water temperature set point               |

#### Channels supported by things *Stiebel Eltron Heat Pump (WPMsystem)*, *Stiebel Eltron Heat Pump (WPM3)* and *Stiebel Eltron Heat Pump (WPM3i)*

Note: The column WPM is for WPMsystem.

| Channel ID                                | Item Type                 | Read only | Description                                                          | WPM | WPM3 | WPM3i |
| ----------------------------------------- | ------------------------- | --------- | -------------------------------------------------------------------- | --- | ---- | ----- |
| fe7-temperature                           | Number:Temperature        | true      | The current temperature measured by the Remote Control FE7           |  x  |  x   |   x   |
| fe7-temperature-setpoint                  | Number:Temperature        | true      | The current set point of the Remote Control FE7 temperature          |  x  |  x   |   x   |
| fek-temperature                           | Number:Temperature        | true      | The current temperature measured by the Remote Control FEK           |     |  x   |   x   |
| fek-temperature-setpoint                  | Number:Temperature        | true      | The current set point of the Remote Control FEK temperature          |     |  x   |   x   |
| fek-humidity                              | Number:Dimensionless      | true      | The current humidity measured by the Remote Control FEK              |     |  x   |   x   |
| fek-dewpoint                              | Number:Temperature        | true      | The current dew point temperature measured by the Remote Control FEK |     |  x   |   x   |
| outdoor-temperature                       | Number:Temperature        | true      | The current outdoor temperature                                      |  x  |  x   |   x   |
| hc1-temperature                           | Number:Temperature        | true      | The current heat circuit 1 temperature                               |  x  |  x   |   x   |
| hc1-temperature-setpoint                  | Number:Temperature        | true      | The current heat circuit 1 temperature set point                     |  x  |  x   |   x   |
| hc2-temperature                           | Number:Temperature        | true      | The current heat circuit 2 temperature                               |  x  |  x   |   x   |
| hc2-temperature-setpoint                  | Number:Temperature        | true      | The current heat circuit 2 temperature set point                     |  x  |  x   |   x   |
| hp-flow-temperature                       | Number:Temperature        | true      | The current heat pump flow temperature                               |  x  |  x   |   x   |
| nhz-flow-temperature                      | Number:Temperature        | true      | The current emergency heating flow temperature                       |  x  |  x   |   x   |
| flow-temperature                          | Number:Temperature        | true      | The current flow temperature                                         |  x  |  x   |   x   |
| return-temperature                        | Number:Temperature        | true      | The current return temperature                                       |  x  |  x   |   x   |
| fixed-temperature-setpoint                | Number:Temperature        | true      | The current fixed temperature set point                              |  x  |  x   |   x   |
| buffer-temperature                        | Number:Temperature        | true      | The current buffer temperature                                       |  x  |  x   |   x   |
| buffer-temperature-setpoint               | Number:Temperature        | true      | The current buffer temperature set point                             |  x  |  x   |   x   |
| heating-pressure                          | Number:Pressure           | true      | The current heating pressure                                         |  x  |  x   |   x   |
| flow-rate                                 | Number:VolumetricFlowRate | true      | The current flow rate                                                |  x  |  x   |   x   |
| hotwater-temperature                      | Number:Temperature        | true      | The current hotwater temperature                                     |  x  |  x   |   x   |
| hotwater-temperature-setpoint             | Number:Temperature        | true      | The current hotwater temperature set point                           |  x  |  x   |   x   |
| fan-cooling-temperature                   | Number:Temperature        | true      | The current fan cooling temperature                                  |  x  |  x   |   x   |
| fan-cooling-temperature-setpoint          | Number:Temperature        | true      | The current fan cooling temperature set point                        |  x  |  x   |   x   |
| area-cooling-temperature                  | Number:Temperature        | true      | The current area cooling temperature                                 |  x  |  x   |   x   |
| area-cooling-temperature-setpoint         | Number:Temperature        | true      | The current area cooling temperature set point                       |  x  |  x   |   x   |
| solar-thermal-collector-temperature       | Number:Temperature        | true      | The current solar thermal collector temperature                      |     |  x   |       |
| solar-thermal-cylinder-temperature        | Number:Temperature        | true      | The current solar thermal collector temperature set point            |     |  x   |       |
| solar-thermal-runtime                     | Number:Time               | true      | The current solar thermal runtime                                    |     |  x   |       |
| external-heat-source-temperature          | Number:Temperature        | true      | The current external heat source temperature                         |  x  |  x   |       |
| external-heat-source-temperature-setpoint | Number:Temperature        | true      | The current external heat source temperature set point               |  x  |  x   |       |
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

### Energy Information Group

This group contains information about the energy consumption and delivery of the heat pump.

#### Channels supported by things *Stiebel Eltron Heat Pump*, *Stiebel Eltron Heat Pump (WPMsystem)* and *Stiebel Eltron Heat Pump (WPM3)*

Note: The column WPM is for WPMsystem.

| Channel ID                 | Item Type     | Read only | Description                                           | heatpump | WPM | WPM3 |
| -------------------------- | ------------- | --------- | ----------------------------------------------------- | -------- | --- | ---- |
| production-heat-today      | Number:Energy | true      | The compressor heat quantity delivered today          |    x     |  x  |   x  |
| production-heat-total      | Number:Energy | true      | The compressor heat quantity delivered in total       |    x     |  x  |   x  |
| production-water-today     | Number:Energy | true      | The compressor water heat quantity delivered today    |    x     |  x  |   x  |
| production-water-total     | Number:Energy | true      | The compressor water heat quantity delivered in total |    x     |  x  |   x  |
| production-nhz-heat-total  | Number:Energy | true      | The emergency heat quantity delivered in total        |          |  x  |   x  |
| production-nhz-water-total | Number:Energy | true      | The emergency water heat quantity delivered in total  |          |  x  |   x  |
| consumption-heat-today     | Number:Energy | true      | The power consumption for heating today               |    x     |  x  |   x  |
| consumption-heat-total     | Number:Energy | true      | The power consumption for heating in total            |    x     |  x  |   x  |
| consumption-water-today    | Number:Energy | true      | The power consumption for water heating today         |    x     |  x  |   x  |
| consumption-water-total    | Number:Energy | true      | The power consumption for water heating in total      |    x     |  x  |   x  |


### Energy und Runtime Information Group

This group contains information about the energy consumption and delivery of the heat pump as well as the runtime
of compressor and emergency heating for heating, cooling and hot water production.

#### Channels supported by thing *Stiebel Eltron Heat Pump (WPM3i)*

| Channel ID                 | Item Type     | Read only | Description                                           |
| -------------------------- | ------------- | --------- | ----------------------------------------------------- |
| production-heat-today      | Number:Energy | true      | The compressor heat quantity delivered today          |
| production-heat-total      | Number:Energy | true      | The compressor heat quantity delivered in total       |
| production-water-today     | Number:Energy | true      | The compressor water heat quantity delivered today    |
| production-water-total     | Number:Energy | true      | The compressor water heat quantity delivered in total |
| production-nhz-heat-total  | Number:Energy | true      | The emergency heat quantity delivered in total        |
| production-nhz-water-total | Number:Energy | true      | The emergency water heat quantity delivered in total  |
| consumption-heat-today     | Number:Energy | true      | The power consumption for heating today               |
| consumption-heat-total     | Number:Energy | true      | The power consumption for heating in total            |
| consumption-water-today    | Number:Energy | true      | The power consumption for water heating today         |
| consumption-water-total    | Number:Energy | true      | The power consumption for water heating in total      |
| heating-runtime            | Number:Time   | true      | The compressor runtime for heating in total           |
| hotwater-runtime           | Number:Time   | true      | The compressor runtime for heating in total           |
| cooling-runtime            | Number:Time   | true      | The compressor runtime for heating in total           |
| nhz1-runtime               | Number:Time   | true      | The emergency heat stage 1 runtime in total           |
| nhz2-runtime               | Number:Time   | true      | The emergency heat stage 2 runtime in total           |
| nhz12-runtime              | Number:Time   | true      | The emergency heat stages 1+2 runtime in total        |


### SG Ready - Energy Management Settings

| Channel ID             | Item Type | Read only | Description            |
| -----------------------| ----------| --------- | -----------------------|
| sg-ready-on-off-switch | Number    | false     | SG Ready On/Off Switch |
| sg-ready-input-lines   | Number    | false     | SG Ready Input Lines   |


### SG Ready - Energy Management System Information

| Channel ID                         | Item Type | Read only | Description                        |
| ---------------------------------- | ----------| --------- | -----------------------------------|
| sg-ready-operating-state           | Number    | true      | SG Ready Operating State           |
| sg-ready-controller-identification | Number    | true      | SG Ready Controller Identification |



## Full Example for the thing *Stiebel Eltron Heat Pump*

### Thing Configuration

```java
Bridge modbus:tcp:bridge "Stiebel Modbus TCP"[ host="hostname|ip", port=502, id=1 ] {
 Thing modbus:heatpump:stiebelEltron "StiebelEltron" (modbus:tcp:bridge) @"Room"  [ ]
}
```

### Item Configuration

```java
Number:Temperature stiebel_eltron_temperature_ffk    "Temperature FFK [%.1f °C]" <temperature>           { channel="modbus:heatpump:stiebelEltron:systemInformation#fek-temperature" }
Number:Temperature stiebel_eltron_setpoint_ffk       "Set point FFK [%.1f °C]" <temperature>             { channel="modbus:heatpump:stiebelEltron:systemInformation#fek-temperature-setpoint" }
Number:Dimensionless stiebel_eltron_humidity_ffk     "Humidity FFK [%.1f %%]" <humidity>                 { channel="modbus:heatpump:stiebelEltron:systemInformation#fek-humidity" }
Number:Temperature stiebel_eltron_dewpoint_ffk       "Dew point FFK [%.1f °C]" <temperature>             { channel="modbus:heatpump:stiebelEltron:systemInformation#fek-dewpoint" }

Number:Temperature stiebel_eltron_outdoor_temp       "Outdoor temperature [%.1f °C]"                     { channel="modbus:heatpump:stiebelEltron:systemInformation#outdoor-temperature" }
Number:Temperature stiebel_eltron_temp_hk1           "Temperature HK1 [%.1f °C]"                         { channel="modbus:heatpump:stiebelEltron:systemInformation#hk1-temperature" }
Number:Temperature stiebel_eltron_setpoint_hk1       "Set point HK1 [%.1f °C]"                           { channel="modbus:heatpump:stiebelEltron:systemInformation#hk1-temperature-setpoint" }
Number:Temperature stiebel_eltron_temp_water         "Water temperature  [%.1f °C]"                      { channel="modbus:heatpump:stiebelEltron:systemInformation#water-temperature" }
Number:Temperature stiebel_eltron_setpoint_water     "Water setpoint [%.1f °C]"                          { channel="modbus:heatpump:stiebelEltron:systemInformation#water-temperature-setpoint" }
Number:Temperature stiebel_eltron_source_temp        "Source temperature [%.1f °C]"                      { channel="modbus:heatpump:stiebelEltron:systemInformation#source-temperature" }
Number:Temperature stiebel_eltron_vorlauf_temp       "Supply tempertature [%.1f °C]"                     { channel="modbus:heatpump:stiebelEltron:systemInformation#supply-temperature" }
Number:Temperature stiebel_eltron_ruecklauf_temp     "Return temperature  [%.1f °C]"                     { channel="modbus:heatpump:stiebelEltron:systemInformation#return-temperature" }

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
Text label="Heat pumpt" icon="temperature" {
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
  Default item=stiebel_eltron_vorlauf_temp  icon="temperature"
  Default item=stiebel_eltron_ruecklauf_temp  icon="temperature"
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


## Full Example for the things things *Stiebel Eltron Heat Pump (WPM3)*, *Stiebel Eltron Heat Pump (WPM3)*, *Stiebel Eltron Heat Pump (WPM3i)* and *Stiebel Eltron ISG SG Ready EM*

### Thing Configuration

Just use one of the heat pump things between the curly braces.

```java
Bridge modbus:tcp:bridge "Stiebel Modbus TCP"[ host="hostname|ip", port=502, id=1 ] {
    Thing modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem "Stiebel Eltron Heat Pump (WPMsystem)"          (modbus:tcp:bridge) @"Room" [ ]
    Thing modubs:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3          "Stiebel Eltron Heat Pump (WPM3)"               (modbus:tcp:bridge) @"Room" [ ]
    Thing modubs:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i        "Stiebel Eltron Heat Pump (WPM3i)"              (modbus:tcp:bridge) @"Room" [ ]
    Thing modbus:stiebeleltron-isg-sg-ready-em:se-isg-sg-ready-em       "Stiebel Eltron ISG SG Ready Energy Management" (modbus:tcp:bridge) @"Room" []
}
```


### Item Configuration *Stiebel Eltron Heat Pump (WPM)*

```java
Contact                   stiebel_eltron_heat_pump_wpm_hc1_pump_active                            "HC1 Pump Active"                                <pump>        { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemStateWpm3#hc1-pump-active" }
Contact                   stiebel_eltron_heat_pump_wpm_hc2_pump_active                            "HC2 Pump Active"                                <pump>        { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemStateWpm3#hc2-pump-active" }
Contact                   stiebel_eltron_heat_pump_wpm_heatup_program_active                      "Heat-Up Program Active"                         <status>      { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemStateWpm3#heat-up-program-active" }
Contact                   stiebel_eltron_heat_pump_wpm_nhz_stages_active                          "NHZ Stages Active"                              <status>      { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemStateWpm3#nhz-stages-running" }
Contact                   stiebel_eltron_heat_pump_wpm_currently_heating                          "Currently Heating"                              <status>      { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemStateWpm3#hp-in-heating-mode" }
Contact                   stiebel_eltron_heat_pump_wpm_currently_heating_hot_water                "Currently Heating Hot Water"                    <status>      { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemStateWpm3#hp-in-hotwater-mode" }
Contact                   stiebel_eltron_heat_pump_wpm_compressor_running                         "Compressor Running"                             <status>      { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemStateWpm3#compressor-running" }
Contact                   stiebel_eltron_heat_pump_wpm_summer_mode_active                         "Summer Mode Active"                             <status>      { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemStateWpm3#summer-mode-active" }
Contact                   stiebel_eltron_heat_pump_wpm_currently_cooling                          "Currently Cooling"                              <status>      { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemStateWpm3#cooling-mode-active" }
Contact                   stiebel_eltron_heat_pump_wpm_min_one_iws_in_defrosting_mode             "Min. one IWS In Defrosting Mode"                <status>      { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemStateWpm3#min-one-iws-in-defrosting-mode" }
Contact                   stiebel_eltron_heat_pump_wpm_silent_mode_1_active                       "Silent Mode 1 Active"                           <status>      { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemStateWpm3#silent-mode1-active" }
Contact                   stiebel_eltron_heat_pump_wpm_silent_mode_2_active                       "Silent Mode 2 Active"                           <status>      { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemStateWpm3#silent-mode2-active" }
Contact                   stiebel_eltron_heat_pump_wpm_power_off                                  "Power Off"                                      <status>      { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemStateWpm3#power-off" }
Number                    stiebel_eltron_heat_pump_wpm_fault_status                               "Fault Status"                                   <status>      { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemStateWpm3#fault-status" }
Number                    stiebel_eltron_heat_pump_wpm_bus_status                                 "Bus Status"                                     <status>      { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemStateWpm3#bus-status" }
Number                    stiebel_eltron_heat_pump_wpm_defrost_initiated                          "Defrost Initiated"                              <status>      { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemStateWpm3#defrost-initiated" }
Number                    stiebel_eltron_heat_pump_wpm_active_error                               "Active Error"                                   <status>      { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemStateWpm3#active-error" }

Number                    stiebel_eltron_heat_pump_wpm_operating_mode                             "Operating Mode"                                 <heating>     { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemParameterWpm3Wpm3i#operating-mode" }
Number:Temperature        stiebel_eltron_heat_pump_wpm_hc1_comfort_temperature                    "HC1 Comfort Temperature"                        <temperature> { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemParameterWpm3Wpm3i#hc1-comfort-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm_hc1_eco_temperature                        "HC1 Eco Temperature"                            <temperature> { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemParameterWpm3Wpm3i#hc1-eco-temperature" }
Number                    stiebel_eltron_heat_pump_wpm_hc1_heating_curve_rise                     "HC1 Heating Curve Rise"                         <line>        { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemParameterWpm3Wpm3i#hc1-heating-curve-rise" }
Number:Temperature        stiebel_eltron_heat_pump_wpm_hc2_comfort_temperature                    "HC2 Comfort Temperature"                        <temperature> { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemParameterWpm3Wpm3i#hc2-comfort-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm_hc2_eco_temperature                        "HC2 Eco Temperature"                            <temperature> { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemParameterWpm3Wpm3i#hc2-eco-temperature" }
Number                    stiebel_eltron_heat_pump_wpm_hc2_heating_curve_rise                     "HC2 Heating Curve Rise"                         <line>        { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemParameterWpm3Wpm3i#hc2-heating-curve-rise" }
Number:Temperature        stiebel_eltron_heat_pump_wpm_fixed_value_operation                      "Fixed Value Operation"                          <temperature> { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemParameterWpm3Wpm3i#fixed-value-operation" }
Number:Temperature        stiebel_eltron_heat_pump_wpm_hot_water_comfort_temperature              "Hot Water Comfort Temperature"                  <temperature> { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemParameterWpm3Wpm3i#hotwater-comfort-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm_hot_water_eco_temperature                  "Hot Water Eco Temperature"                      <temperature> { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemParameterWpm3Wpm3i#hotwater-eco-temperature" }
Number                    stiebel_eltron_heat_pump_wpm_hot_water_stages                           "Hot Water Stages"                               <status>      { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemParameterWpm3Wpm3i#hotwater-stages" }
Number:Temperature        stiebel_eltron_heat_pump_wpm_hot_water_dualmode_temperature_bivalence   "Hot Water DualMode Temperature (bivalence)"     <temperature> { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemParameterWpm3Wpm3i#hotwater-dual-mode-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm_area_cooling_flow_temperature_set_point    "Area Cooling Flow Temperature Set Point"        <temperature> { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemParameterWpm3Wpm3i#area-cooling-flow-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_wpm_area_cooling_room_temperature_set_point    "Area Cooling Room Temperature Set Point"        <temperature> { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemParameterWpm3Wpm3i#area-cooling-room-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_wpm_fan_cooling_flow_temperature_set_point     "Fan Cooling Flow Temperature Set Point"         <temperature> { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemParameterWpm3Wpm3i#fan-cooling-flow-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_wpm_fan_cooling_flow_temperature_hysteresis    "Fan Cooling Flow Temperature Hysteresis"        <temperature> { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemParameterWpm3Wpm3i#fan-cooling-flow-temperature-hysteresis" }
Number:Temperature        stiebel_eltron_heat_pump_wpm_fan_cooling_room_temperature_set_point     "Fan Cooling Room Temperature Set Point"         <temperature> { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemParameterWpm3Wpm3i#fan-cooling-room-temperature-setpoint" }
Number                    stiebel_eltron_heat_pump_wpm_reset                                      "Reset"                                          <settings>    { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemParameterWpm3Wpm3i#reset" }
Number                    stiebel_eltron_heat_pump_wpm_restart_isg                                "Restart ISG"                                    <settings>    { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemParameterWpm3Wpm3i#restart-isg" }

Number:Temperature        stiebel_eltron_heat_pump_wpm_fe7_temperature                            "FE7 Temperature"                                <temperature> { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemInformationWpm3#fe7-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm_fe7_temperature_set_point                  "FE7 Temperature Set Point"                      <temperature> { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemInformationWpm3#fe7-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_wpm_outdoor_temperature                        "Outdoor Temperature"                            <temperature> { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemInformationWpm3#outdoor-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm_hc1_temperature                            "HC1 Temperature"                                <temperature> { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemInformationWpm3#hc1-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm_hc1_temperature_set_point                  "HC1 Temperature Set Point"                      <temperature> { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemInformationWpm3#hc1-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_wpm_hc2_temperature                            "HC2 Temperature"                                <temperature> { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemInformationWpm3#hc2-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm_hc2_temperature_set_point                  "HC2 Temperature Set Point"                      <temperature> { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemInformationWpm3#hc2-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_wpm_heat_pump_flow_temperature                 "Heat Pump Flow Temperature"                     <temperature> { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemInformationWpm3#hp-flow-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm_emergency_heating_flow_temperature         "Emergency Heating Flow Temperature"             <temperature> { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemInformationWpm3#nhz-flow-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm_flow_temperature                           "Flow Temperature"                               <temperature> { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemInformationWpm3#flow-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm_return_temperature                         "Return Temperature"                             <temperature> { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemInformationWpm3#return-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm_fixed_temperature_set_point                "Fixed Temperature Set Point"                    <temperature> { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemInformationWpm3#fixed-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_wpm_buffer_temperature                         "Buffer Temperature"                             <temperature> { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemInformationWpm3#buffer-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm_buffer_temperature_set_point               "Buffer Temperature Set Point"                   <temperature> { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemInformationWpm3#buffer-temperature-setpoint" }
Number:Pressure           stiebel_eltron_heat_pump_wpm_heating_pressure                           "Heating Pressure"                               <pressure>    { unit="bar", channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemInformationWpm3#heating-pressure" }
Number:VolumetricFlowRate stiebel_eltron_heat_pump_wpm_flow_rate                                  "Flow Rate"                                      <flow>        { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemInformationWpm3#flow-rate" }
Number:Temperature        stiebel_eltron_heat_pump_wpm_hot_water_temperature                      "Hot Water Temperature"                          <temperature> { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemInformationWpm3#hotwater-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm_hot_water_temperature_set_point            "Hot Water Temperature Set Point"                <temperature> { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemInformationWpm3#hotwater-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_wpm_fan_cooling_temperature                    "Fan Cooling Temperature"                        <temperature> { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemInformationWpm3#fan-cooling-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm_fan_cooling_temperature_set_point          "Fan Cooling Temperature Set Point"              <temperature> { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemInformationWpm3#fan-cooling-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_wpm_area_cooling_temperature                   "Area Cooling Temperature"                       <temperature> { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemInformationWpm3#area-cooling-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm_area_cooling_temperature_set_point         "Area Cooling Temperature Set Point"             <temperature> { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemInformationWpm3#area-cooling-temperature-setpoint" }
Number                    stiebel_eltron_heat_pump_wpm_external_heat_source_runtime               "External Heat Source Runtime"                   <time>        { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemInformationWpm3#external-heat-source-runtime" }
Number                    stiebel_eltron_heat_pump_wpm_external_heat_source_temperature           "External Heat Source Temperature"               <temperature> { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemInformationWpm3#external-heat-source-temperature" }
Number                    stiebel_eltron_heat_pump_wpm_external_heat_source_temperature_set_point "External Heat Source Temperature Set Point"     <temperature> { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemInformationWpm3#external-heat-source-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_wpm_lower_application_limit_heating            "Lower Application Limit Heating"                <temperature> { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemInformationWpm3#lower-application-limit-heating" }
Number:Temperature        stiebel_eltron_heat_pump_wpm_lower_application_limit_hot_water          "Lower Application Limit Hot Water"              <temperature> { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemInformationWpm3#lower-application-limit-hotwater" }
Number:Temperature        stiebel_eltron_heat_pump_wpm_source_temperature                         "Source Temperature"                             <temperature> { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemInformationWpm3#source-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm_minimal_source_temperature                 "Minimal Source Temperature"                     <temperature> { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemInformationWpm3#min-source-temperature" }
Number:Pressure           stiebel_eltron_heat_pump_wpm_source_pressure                            "Source Pressure"                                <pressure>    { unit="bar", channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:systemInformationWpm3#source-pressure" }
Number:Temperature        stiebel_eltron_heat_pump_wpm_hp1_return_temperature                     "HP1 Return Temperature"                         <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPMsytem:systemInformationWpm3#hp1-return-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm_hp1_flow_temperature                       "HP1 Flow Temperature"                           <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPMsytem:systemInformationWpm3#hp1-flow-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm_hp1_flow_temperature                       "HP1 Hot Gas Temperature"                        <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPMsytem:systemInformationWpm3#hp1-hotgas-temperature" }
Number:Pressure           stiebel_eltron_heat_pump_wpm_hp1_logw_pressure                          "HP1 Low Pressure"                               <temperature> { unit="bar", channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPMsytem:systemInformationWpm3#hp1-low-pressure" }
Number:Pressure           stiebel_eltron_heat_pump_wpm_hp1_mean_pressure                          "HP1 Mean Pressure"                              <temperature> { unit="bar", channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPMsytem:systemInformationWpm3#hp1-mean-pressure" }
Number:Pressure           stiebel_eltron_heat_pump_wpm_hp1_high_pressure                          "HP1 High Pressure"                              <temperature> { unit="bar", channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPMsytem:systemInformationWpm3#hp1-high-pressure" }
Number:VolumetricFlowRate stiebel_eltron_heat_pump_wpm_hp1_flow_rate                              "HP1 Flow Rate"                                  <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPMsytem:systemInformationWpm3#hp1-flow-rate" }

Number:Energy             stiebel_eltron_heat_pump_wpm_heat_quantity_today                        "Heat Quantity Today [%.0f kWh]"                 <energy>      { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:energyInformationWpm3#production-heat-today" }
Number:Energy             stiebel_eltron_heat_pump_wpm_heat_quantity_total                        "Heat Quantity Total [%.3f MWh]"                 <energy>      { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:energyInformationWpm3#production-heat-total" }
Number:Energy             stiebel_eltron_heat_pump_wpm_water_heat_quantity_today                  "Water Heat Quantity Today [%.0f kWh]"           <energy>      { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:energyInformationWpm3#production-water-today" }
Number:Energy             stiebel_eltron_heat_pump_wpm_water_heat_quantity_total                  "Water Heat Quantity Total [%.3f MWh]"           <energy>      { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:energyInformationWpm3#production-water-total" }
Number:Energy             stiebel_eltron_heat_pump_wpm_nhz_heating_quantity_total                 "NHZ Heating Quantity Total [%.3f MWh]"          <energy>      { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:energyInformationWpm3#production-nhz-heat-total" }
Number:Energy             stiebel_eltron_heat_pump_wpm_nhz_hot_water_quantity_total               "NHZ Hot Water Quantity Total [%.3f MWh]"        <energy>      { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:energyInformationWpm3#production-nhz-water-total" }
Number:Energy             stiebel_eltron_heat_pump_wpm_power_consumption_for_heating_today        "Power Consumption for Heating Today [%.0f kWh]" <energy>      { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:energyInformationWpm3#consumption-heat-today" }
Number:Energy             stiebel_eltron_heat_pump_wpm_power_consumption_for_heating_total        "Power Consumption for Heating Total [%.0f kWh]" <energy>      { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:energyInformationWpm3#consumption-heat-total" }
Number:Energy             stiebel_eltron_heat_pump_wpm_power_consumption_for_water_today          "Power Consumption for Water Today [%.0f kWh]"   <energy>      { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:energyInformationWpm3#consumption-water-today" }
Number:Energy             stiebel_eltron_heat_pump_wpm_power_consumption_for_water_total          "Power Consumption for Water Total [%.0f kWh]"   <energy>      { channel="modubs:stiebeleltron-heatpump-wpmsystem:stiebelEltronWPMsytem:energyInformationWpm3#consumption-water-total" }
```


### Item Configuration *Stiebel Eltron Heat Pump (WPM3)*

```java
Contact                   stiebel_eltron_heat_pump_wpm3_hc1_pump_active                            "HC1 Pump Active"                                <pump>        { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemStateWpm3#hc1-pump-active" }
Contact                   stiebel_eltron_heat_pump_wpm3_hc2_pump_active                            "HC2 Pump Active"                                <pump>        { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemStateWpm3#hc2-pump-active" }
Contact                   stiebel_eltron_heat_pump_wpm3_heatup_program_active                      "Heat-Up Program Active"                         <status>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemStateWpm3#heat-up-program-active" }
Contact                   stiebel_eltron_heat_pump_wpm3_nhz_stages_active                          "NHZ Stages Active"                              <status>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemStateWpm3#nhz-stages-running" }
Contact                   stiebel_eltron_heat_pump_wpm3_currently_heating                          "Currently Heating"                              <status>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemStateWpm3#hp-in-heating-mode" }
Contact                   stiebel_eltron_heat_pump_wpm3_currently_heating_hot_water                "Currently Heating Hot Water"                    <status>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemStateWpm3#hp-in-hotwater-mode" }
Contact                   stiebel_eltron_heat_pump_wpm3_compressor_running                         "Compressor Running"                             <status>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemStateWpm3#compressor-running" }
Contact                   stiebel_eltron_heat_pump_wpm3_summer_mode_active                         "Summer Mode Active"                             <status>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemStateWpm3#summer-mode-active" }
Contact                   stiebel_eltron_heat_pump_wpm3_currently_cooling                          "Currently Cooling"                              <status>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemStateWpm3#cooling-mode-active" }
Contact                   stiebel_eltron_heat_pump_wpm3_min_one_iws_in_defrosting_mode             "Min. one IWS In Defrosting Mode"                <status>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemStateWpm3#min-one-iws-in-defrosting-mode" }
Contact                   stiebel_eltron_heat_pump_wpm3_silent_mode_1_active                       "Silent Mode 1 Active"                           <status>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemStateWpm3#silent-mode1-active" }
Contact                   stiebel_eltron_heat_pump_wpm3_silent_mode_2_active                       "Silent Mode 2 Active"                           <status>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemStateWpm3#silent-mode2-active" }
Contact                   stiebel_eltron_heat_pump_wpm3_power_off                                  "Power Off"                                      <status>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemStateWpm3#power-off" }
Contact                   stiebel_eltron_heat_pump_wpm3_compressor_1_active                        "Compressor 1 Active"                            <status>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemStateWpm3#compressor1-active" }
Contact                   stiebel_eltron_heat_pump_wpm3_compressor_2_active                        "Compressor 2 Active"                            <status>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemStateWpm3#compressor2-active" }
Contact                   stiebel_eltron_heat_pump_wpm3_compressor_3_active                        "Compressor 3 Active"                            <status>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemStateWpm3#compressor3-active" }
Contact                   stiebel_eltron_heat_pump_wpm3_compressor_4_active                        "Compressor 4 Active"                            <status>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemStateWpm3#compressor4-active" }
Contact                   stiebel_eltron_heat_pump_wpm3_compressor_5_active                        "Compressor 5 Active"                            <status>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemStateWpm3#compressor5-active" }
Contact                   stiebel_eltron_heat_pump_wpm3_compressor_6_active                        "Compressor 6 Active"                            <status>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemStateWpm3#compressor6-active" }
Contact                   stiebel_eltron_heat_pump_wpm3_buffer_charging_pump_1_active              "Buffer Charging Pump 1 Active"                  <status>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemStateWpm3#buffer-charging-pump1-active" }
Contact                   stiebel_eltron_heat_pump_wpm3_buffer_charging_pump_2_active              "Buffer Charging Pump 2 Active"                  <status>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemStateWpm3#buffer-charging-pump2-active" }
Contact                   stiebel_eltron_heat_pump_wpm3_buffer_charging_pump_3_active              "Buffer Charging Pump 3 Active"                  <status>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemStateWpm3#buffer-charging-pump3-active" }
Contact                   stiebel_eltron_heat_pump_wpm3_buffer_charging_pump_4_active              "Buffer Charging Pump 4 Active"                  <status>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemStateWpm3#buffer-charging-pump4-active" }
Contact                   stiebel_eltron_heat_pump_wpm3_buffer_charging_pump_5_active              "Buffer Charging Pump 5 Active"                  <status>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemStateWpm3#buffer-charging-pump5"-active }
Contact                   stiebel_eltron_heat_pump_wpm3_buffer_charging_pump_6_active              "Buffer Charging Pump 6 Active"                  <status>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemStateWpm3#buffer-charging-pump6-active" }
Contact                   stiebel_eltron_heat_pump_wpm3_nhz1_active                                "NHZ1 Active"                                    <status>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemStateWpm3#nhz1-active" }
Contact                   stiebel_eltron_heat_pump_wpm3_nhz2_active                                "NHZ2 Active"                                    <status>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemStateWpm3#nhz2-active" }
Number                    stiebel_eltron_heat_pump_wpm3_fault_status                               "Fault Status"                                   <status>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemStateWpm3#fault-status" }
Number                    stiebel_eltron_heat_pump_wpm3_bus_status                                 "Bus Status"                                     <status>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemStateWpm3#bus-status" }
Number                    stiebel_eltron_heat_pump_wpm3_defrost_initiated                          "Defrost Initiated"                              <status>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemStateWpm3#defrost-initiated" }
Number                    stiebel_eltron_heat_pump_wpm3_active_error                               "Active Error"                                   <status>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemStateWpm3#active-error" }

Number                    stiebel_eltron_heat_pump_wpm3_operating_mode                             "Operating Mode"                                 <heating>     { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemParameterWpm3Wpm3i#operating-mode" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_hc1_comfort_temperature                    "HC1 Comfort Temperature"                        <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemParameterWpm3Wpm3i#hc1-comfort-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_hc1_eco_temperature                        "HC1 Eco Temperature"                            <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemParameterWpm3Wpm3i#hc1-eco-temperature" }
Number                    stiebel_eltron_heat_pump_wpm3_hc1_heating_curve_rise                     "HC1 Heating Curve Rise"                         <line>        { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemParameterWpm3Wpm3i#hc1-heating-curve-rise" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_hc2_comfort_temperature                    "HC2 Comfort Temperature"                        <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemParameterWpm3Wpm3i#hc2-comfort-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_hc2_eco_temperature                        "HC2 Eco Temperature"                            <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemParameterWpm3Wpm3i#hc2-eco-temperature" }
Number                    stiebel_eltron_heat_pump_wpm3_hc2_heating_curve_rise                     "HC2 Heating Curve Rise"                         <line>        { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemParameterWpm3Wpm3i#hc2-heating-curve-rise" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_fixed_value_operation                      "Fixed Value Operation"                          <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemParameterWpm3Wpm3i#fixed-value-operation" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_heating_dualmode_temperature_bivalence     "Heating DualMode Temperature (bivalence)"       <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemParameterWpm3Wpm3i#heating-dual-mode-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_hot_water_comfort_temperature              "Hot Water Comfort Temperature"                  <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemParameterWpm3Wpm3i#hotwater-comfort-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_hot_water_eco_temperature                  "Hot Water Eco Temperature"                      <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemParameterWpm3Wpm3i#hotwater-eco-temperature" }
Number                    stiebel_eltron_heat_pump_wpm3_hot_water_stages                           "Hot Water Stages"                               <status>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemParameterWpm3Wpm3i#hotwater-stages" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_hot_water_dualmode_temperature_bivalence   "Hot Water DualMode Temperature (bivalence)"     <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemParameterWpm3Wpm3i#hotwater-dual-mode-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_area_cooling_flow_temperature_set_point    "Area Cooling Flow Temperature Set Point"        <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemParameterWpm3Wpm3i#area-cooling-flow-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_area_cooling_flow_temperature_hysteresis   "Area Cooling Flow Temperature Hysteresis"       <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemParameterWpm3Wpm3i#area-cooling-flow-temperature-hysteresis" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_area_cooling_room_temperature_set_point    "Area Cooling Room Temperature Set Point"        <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemParameterWpm3Wpm3i#area-cooling-room-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_fan_cooling_flow_temperature_set_point     "Fan Cooling Flow Temperature Set Point"         <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemParameterWpm3Wpm3i#fan-cooling-flow-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_fan_cooling_flow_temperature_hysteresis    "Fan Cooling Flow Temperature Hysteresis"        <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemParameterWpm3Wpm3i#fan-cooling-flow-temperature-hysteresis" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_fan_cooling_room_temperature_set_point     "Fan Cooling Room Temperature Set Point"         <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemParameterWpm3Wpm3i#fan-cooling-room-temperature-setpoint" }
Number                    stiebel_eltron_heat_pump_wpm3_reset                                      "Reset"                                          <settings>    { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemParameterWpm3Wpm3i#reset" }
Number                    stiebel_eltron_heat_pump_wpm3_restart_isg                                "Restart ISG"                                    <settings>    { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemParameterWpm3Wpm3i#restart-isg" }

Number:Temperature        stiebel_eltron_heat_pump_wpm3_fe7_temperature                            "FE7 Temperature"                                <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#fe7-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_fe7_temperature_set_point                  "FE7 Temperature Set Point"                      <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#fe7-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_ffktemperature                             "FFK Temperature"                                <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#fek-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_ffk_temperature_set_point                  "FFK Temperature Set Point"                      <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#fek-temperature-setpoint" }
Number:Dimensionless      stiebel_eltron_heat_pump_wpm3_ffk_humidity                               "FFK Humidity"                                   <humidity>    { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#fek-humidity" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_ffk_dewpoint                               "FFK Dewpoint"                                   <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#fek-dewpoint" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_outdoor_temperature                        "Outdoor Temperature"                            <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#outdoor-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_hc1_temperature                            "HC1 Temperature"                                <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#hc1-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_hc1_temperature_set_point                  "HC1 Temperature Set Point"                      <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#hc1-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_hc2_temperature                            "HC2 Temperature"                                <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#hc2-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_hc2_temperature_set_point                  "HC2 Temperature Set Point"                      <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#hc2-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_heat_pump_flow_temperature                 "Heat Pump Flow Temperature"                     <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#hp-flow-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_emergency_heating_flow_temperature         "Emergency Heating Flow Temperature"             <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#nhz-flow-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_flow_temperature                           "Flow Temperature"                               <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#flow-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_return_temperature                         "Return Temperature"                             <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#return-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_fixed_temperature_set_point                "Fixed Temperature Set Point"                    <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#fixed-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_buffer_temperature                         "Buffer Temperature"                             <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#buffer-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_buffer_temperature_set_point               "Buffer Temperature Set Point"                   <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#buffer-temperature-setpoint" }
Number:Pressure           stiebel_eltron_heat_pump_wpm3_heating_pressure                           "Heating Pressure"                               <pressure>    { unit="bar", channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#heating-pressure" }
Number:VolumetricFlowRate stiebel_eltron_heat_pump_wpm3_flow_rate                                  "Flow Rate"                                      <flow>        { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#flow-rate" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_hot_water_temperature                      "Hot Water Temperature"                          <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#hotwater-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_hot_water_temperature_set_point            "Hot Water Temperature Set Point"                <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#hotwater-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_fan_cooling_temperature                    "Fan Cooling Temperature"                        <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#fan-cooling-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_fan_cooling_temperature_set_point          "Fan Cooling Temperature Set Point"              <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#fan-cooling-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_area_cooling_temperature                   "Area Cooling Temperature"                       <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#area-cooling-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_area_cooling_temperature_set_point         "Area Cooling Temperature Set Point"             <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#area-cooling-temperature-setpoint" }
Number                    stiebel_eltron_heat_pump_wpm3_solar_thermal_collector_temperature        "Solar Thermal Collector Temperature"            <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#solar-thermal-collector-temperature" }
Number                    stiebel_eltron_heat_pump_wpm3_solar_thermal_cylinder_runtime             "Solar Thermal Cylinder Runtime"                 <time>        { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#solar-thermal-runtime" }
Number                    stiebel_eltron_heat_pump_wpm3_solar_thermal_cylinder_temperature         "Solar Thermal Cylinder Temperature"             <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#solar-thermal-cylinder-temperature" }
Number                    stiebel_eltron_heat_pump_wpm3_external_heat_source_runtime               "External Heat Source Runtime"                   <time>        { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#external-heat-source-runtime" }
Number                    stiebel_eltron_heat_pump_wpm3_external_heat_source_temperature           "External Heat Source Temperature"               <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#external-heat-source-temperature" }
Number                    stiebel_eltron_heat_pump_wpm3_external_heat_source_temperature_set_point "External Heat Source Temperature Set Point"     <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#external-heat-source-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_lower_application_limit_heating            "Lower Application Limit Heating"                <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#lower-application-limit-heating" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_lower_application_limit_hot_water          "Lower Application Limit Hot Water"              <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#lower-application-limit-hotwater" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_source_temperature                         "Source Temperature"                             <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#source-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_minimal_source_temperature                 "Minimal Source Temperature"                     <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#min-source-temperature" }
Number:Pressure           stiebel_eltron_heat_pump_wpm3_source_pressure                            "Source Pressure"                                <pressure>    { unit="bar", channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#source-pressure" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_hp1_return_temperature                     "HP1 Return Temperature"                         <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#hp1-return-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_hp1_flow_temperature                       "HP1 Flow Temperature"                           <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#hp1-flow-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3_hp1_flow_temperature                       "HP1 Hot Gas Temperature"                        <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#hp1-hotgas-temperature" }
Number:Pressure           stiebel_eltron_heat_pump_wpm3_hp1_logw_pressure                          "HP1 Low Pressure"                               <temperature> { unit="bar", channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#hp1-low-pressure" }
Number:Pressure           stiebel_eltron_heat_pump_wpm3_hp1_mean_pressure                          "HP1 Mean Pressure"                              <temperature> { unit="bar", channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#hp1-mean-pressure" }
Number:Pressure           stiebel_eltron_heat_pump_wpm3_hp1_high_pressure                          "HP1 High Pressure"                              <temperature> { unit="bar", channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#hp1-high-pressure" }
Number:VolumetricFlowRate stiebel_eltron_heat_pump_wpm3_hp1_flow_rate                              "HP1 Flow Rate"                                  <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:systemInformationWpm3#hp1-flow-rate" }

Number:Energy             stiebel_eltron_heat_pump_wpm3_heat_quantity_today                        "Heat Quantity Today [%.0f kWh]"                 <energy>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:energyInformationWpm3#production-heat-today" }
Number:Energy             stiebel_eltron_heat_pump_wpm3_heat_quantity_total                        "Heat Quantity Total [%.3f MWh]"                 <energy>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:energyInformationWpm3#production-heat-total" }
Number:Energy             stiebel_eltron_heat_pump_wpm3_water_heat_quantity_today                  "Water Heat Quantity Today [%.0f kWh]"           <energy>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:energyInformationWpm3#production-water-today" }
Number:Energy             stiebel_eltron_heat_pump_wpm3_water_heat_quantity_total                  "Water Heat Quantity Total [%.3f MWh]"           <energy>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:energyInformationWpm3#production-water-total" }
Number:Energy             stiebel_eltron_heat_pump_wpm3_nhz_heating_quantity_total                 "NHZ Heating Quantity Total [%.3f MWh]"          <energy>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:energyInformationWpm3#production-nhz-heat-total" }
Number:Energy             stiebel_eltron_heat_pump_wpm3_nhz_hot_water_quantity_total               "NHZ Hot Water Quantity Total [%.3f MWh]"        <energy>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:energyInformationWpm3#production-nhz-water-total" }
Number:Energy             stiebel_eltron_heat_pump_wpm3_power_consumption_for_heating_today        "Power Consumption for Heating Today [%.0f kWh]" <energy>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:energyInformationWpm3#consumption-heat-today" }
Number:Energy             stiebel_eltron_heat_pump_wpm3_power_consumption_for_heating_total        "Power Consumption for Heating Total [%.0f kWh]" <energy>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:energyInformationWpm3#consumption-heat-total" }
Number:Energy             stiebel_eltron_heat_pump_wpm3_power_consumption_for_water_today          "Power Consumption for Water Today [%.0f kWh]"   <energy>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:energyInformationWpm3#consumption-water-today" }
Number:Energy             stiebel_eltron_heat_pump_wpm3_power_consumption_for_water_total          "Power Consumption for Water Total [%.0f kWh]"   <energy>      { channel="modbus:stiebeleltron-heatpump-wpm3:stiebelEltronWPM3:energyInformationWpm3#consumption-water-total" }
```


### Item Configuration *Stiebel Eltron Heat Pump (WPM3i)*

```java
Contact                   stiebel_eltron_heat_pump_wpm3i_hc1_pump_active                          "HC1 Pump Active"                                <pump>        { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemStateWpm3i#hc1-pump-active" }
Contact                   stiebel_eltron_heat_pump_wpm3i_hc2_pump_active                          "HC2 Pump Active"                                <pump>        { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemStateWpm3i#hc2-pump-active" }
Contact                   stiebel_eltron_heat_pump_wpm3i_heatup_program_active                    "Heat-Up Program Active"                         <status>      { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemStateWpm3i#heat-up-program-active" }
Contact                   stiebel_eltron_heat_pump_wpm3i_nhz_stages_active                        "NHZ Stages Active"                              <status>      { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemStateWpm3i#nhz-stages-running" }
Contact                   stiebel_eltron_heat_pump_wpm3i_currently_heating                        "Currently Heating"                              <status>      { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemStateWpm3i#hp-in-heating-mode" }
Contact                   stiebel_eltron_heat_pump_wpm3i_currently_heating_hot_water              "Currently Heating Hot Water"                    <status>      { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemStateWpm3i#hp-in-hotwater-mode" }
Contact                   stiebel_eltron_heat_pump_wpm3i_compressor_running                       "Compressor Running"                             <status>      { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemStateWpm3i#compressor-running" }
Contact                   stiebel_eltron_heat_pump_wpm3i_summer_mode_active                       "Summer Mode Active"                             <status>      { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemStateWpm3i#summer-mode-active" }
Contact                   stiebel_eltron_heat_pump_wpm3i_currently_cooling                        "Currently Cooling"                              <status>      { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemStateWpm3i#cooling-mode-active" }
Contact                   stiebel_eltron_heat_pump_wpm3i_min_one_iws_in_defrosting_mode           "Min. one IWS In Defrosting Mode"                <status>      { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemStateWpm3i#min-one-iws-in-defrosting-mode" }
Contact                   stiebel_eltron_heat_pump_wpm3i_silent_mode_1_active                     "Silent Mode 1 Active"                           <status>      { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemStateWpm3i#silent-mode1-active" }
Contact                   stiebel_eltron_heat_pump_wpm3i_silent_mode_2_active                     "Silent Mode 2 Active"                           <status>      { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemStateWpm3i#silent-mode2-active" }
Contact                   stiebel_eltron_heat_pump_wpm3i_power_off                                "Power Off"                                      <status>      { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemStateWpm3i#power-off" }
Number                    stiebel_eltron_heat_pump_wpm3i_fault_status                             "Fault Status"                                   <status>      { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemStateWpm3i#fault-status" }
Number                    stiebel_eltron_heat_pump_wpm3i_bus_status                               "Bus Status"                                     <status>      { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemStateWpm3i#bus-status" }
Number                    stiebel_eltron_heat_pump_wpm3i_active_error                             "Active Error"                                   <status>      { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemStateWpm3i#active-error" }

Number                    stiebel_eltron_heat_pump_wpm3i_operating_mode                           "Operating Mode"                                 <heating>     { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemParameterWpm3Wpm3i#operating-mode" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_hc1_comfort_temperature                  "HC1 Comfort Temperature"                        <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemParameterWpm3Wpm3i#hc1-comfort-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_hc1_eco_temperature                      "HC1 Eco Temperature"                            <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemParameterWpm3Wpm3i#hc1-eco-temperature" }
Number                    stiebel_eltron_heat_pump_wpm3i_hc1_heating_curve_rise                   "HC1 Heating Curve Rise"                         <line>        { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemParameterWpm3Wpm3i#hc1-heating-curve-rise" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_hc2_comfort_temperature                  "HC2 Comfort Temperature"                        <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemParameterWpm3Wpm3i#hc2-comfort-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_hc2_eco_temperature                      "HC2 Eco Temperature"                            <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemParameterWpm3Wpm3i#hc2-eco-temperature" }
Number                    stiebel_eltron_heat_pump_wpm3i_hc2_heating_curve_rise                   "HC2 Heating Curve Rise"                         <line>        { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemParameterWpm3Wpm3i#hc2-heating-curve-rise" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_fixed_value_operation                    "Fixed Value Operation"                          <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemParameterWpm3Wpm3i#fixed-value-operation" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_heating_dualmode_temperature_bivalence   "Heating DualMode Temperature (bivalence)"       <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemParameterWpm3Wpm3i#heating-dual-mode-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_hot_water_comfort_temperature            "Hot Water Comfort Temperature"                  <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemParameterWpm3Wpm3i#hotwater-comfort-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_hot_water_eco_temperature                "Hot Water Eco Temperature"                      <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemParameterWpm3Wpm3i#hotwater-eco-temperature" }
Number                    stiebel_eltron_heat_pump_wpm3i_hot_water_stages                         "Hot Water Stages"                               <status>      { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemParameterWpm3Wpm3i#hotwater-stages" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_hot_water_dualmode_temperature_bivalence "Hot Water DualMode Temperature (bivalence)"     <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemParameterWpm3Wpm3i#hotwater-dual-mode-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_area_cooling_flow_temperature_set_point  "Area Cooling Flow Temperature Set Point"        <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemParameterWpm3Wpm3i#area-cooling-flow-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_area_cooling_flow_temperature_hysteresis "Area Cooling Flow Temperature Hysteresis"       <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemParameterWpm3Wpm3i#area-cooling-flow-temperature-hysteresis" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_area_cooling_room_temperature_set_point  "Area Cooling Room Temperature Set Point"        <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemParameterWpm3Wpm3i#area-cooling-room-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_fan_cooling_flow_temperature_set_point   "Fan Cooling Flow Temperature Set Point"         <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemParameterWpm3Wpm3i#fan-cooling-flow-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_fan_cooling_flow_temperature_hysteresis  "Fan Cooling Flow Temperature Hysteresis"        <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemParameterWpm3Wpm3i#fan-cooling-flow-temperature-hysteresis" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_fan_cooling_room_temperature_set_point   "Fan Cooling Room Temperature Set Point"         <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemParameterWpm3Wpm3i#fan-cooling-room-temperature-setpoint" }
Number                    stiebel_eltron_heat_pump_wpm3i_reset                                    "Reset"                                          <settings>    { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemParameterWpm3Wpm3i#reset" }
Number                    stiebel_eltron_heat_pump_wpm3i_restart_isg                              "Restart ISG"                                    <settings>    { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemParameterWpm3Wpm3i#restart-isg" }

Number:Temperature        stiebel_eltron_heat_pump_wpm3i_fe7_temperature                          "FE7 Temperature"                                <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemInformationWpm3i#fe7-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_fe7_temperature_set_point                "FE7 Temperature Set Point"                      <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemInformationWpm3i#fe7-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_ffktemperature                           "FFK Temperature"                                <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemInformationWpm3i#fek-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_ffk_temperature_set_point                "FFK Temperature Set Point"                      <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemInformationWpm3i#fek-temperature-setpoint" }
Number:Dimensionless      stiebel_eltron_heat_pump_wpm3i_ffk_humidity                             "FFK Humidity"                                   <humidity>    { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemInformationWpm3i#fek-humidity" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_ffk_dewpoint                             "FFK Dewpoint"                                   <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemInformationWpm3i#fek-dewpoint" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_outdoor_temperature                      "Outdoor Temperature"                            <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemInformationWpm3i#outdoor-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_hc1_temperature                          "HC1 Temperature"                                <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemInformationWpm3i#hc1-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_hc1_temperature_set_point                "HC1 Temperature Set Point"                      <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemInformationWpm3i#hc1-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_hc2_temperature                          "HC2 Temperature"                                <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemInformationWpm3i#hc2-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_hc2_temperature_set_point                "HC2 Temperature Set Point"                      <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemInformationWpm3i#hc2-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_heat_pump_flow_temperature               "Heat Pump Flow Temperature"                     <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemInformationWpm3i#hp-flow-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_emergency_heating_flow_temperature       "Emergency Heating Flow Temperature"             <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemInformationWpm3i#nhz-flow-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_flow_temperature                         "Flow Temperature"                               <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemInformationWpm3i#flow-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_return_temperature                       "Return Temperature"                             <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemInformationWpm3i#return-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_fixed_temperature_set_point              "Fixed Temperature Set Point"                    <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemInformationWpm3i#fixed-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_buffer_temperature                       "Buffer Temperature"                             <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemInformationWpm3i#buffer-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_buffer_temperature_set_point             "Buffer Temperature Set Point"                   <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemInformationWpm3i#buffer-temperature-setpoint" }
Number:Pressure           stiebel_eltron_heat_pump_wpm3i_heating_pressure                         "Heating Pressure"                               <pressure>    { unit="bar", channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemInformationWpm3i#heating-pressure" }
Number:VolumetricFlowRate stiebel_eltron_heat_pump_wpm3i_flow_rate                                "Flow Rate"                                      <flow>        { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemInformationWpm3i#flow-rate" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_hot_water_temperature                    "Hot Water Temperature"                          <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemInformationWpm3i#hotwater-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_hot_water_temperature_set_point          "Hot Water Temperature Set Point"                <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemInformationWpm3i#hotwater-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_fan_cooling_temperature                  "Fan Cooling Temperature"                        <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemInformationWpm3i#fan-cooling-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_fan_cooling_temperature_set_point        "Fan Cooling Temperature Set Point"              <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemInformationWpm3i#fan-cooling-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_area_cooling_temperature                 "Area Cooling Temperature"                       <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemInformationWpm3i#area-cooling-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_area_cooling_temperature_set_point       "Area Cooling Temperature Set Point"             <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemInformationWpm3i#area-cooling-temperature-setpoint" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_lower_application_limit_heating          "Lower Application Limit Heating"                <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemInformationWpm3i#lower-application-limit-heating" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_lower_application_limit_hot_water        "Lower Application Limit Hot Water"              <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemInformationWpm3i#lower-application-limit-hotwater" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_source_temperature                       "Source Temperature"                             <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemInformationWpm3i#source-temperature" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_minimal_source_temperature               "Minimal Source Temperature"                     <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemInformationWpm3i#min-source-temperature" }
Number:Pressure           stiebel_eltron_heat_pump_wpm3i_source_pressure                          "Source Pressure"                                <pressure>    { unit="bar", channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemInformationWpm3i#source-pressure" }
Number:Temperature        stiebel_eltron_heat_pump_wpm3i_hot_gas_temperature                      "Hot Gas Temperature"                            <temperature> { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemInformationWpm3i#hotgas-temperature" }
Number:Pressure           stiebel_eltron_heat_pump_wpm3i_high_pressure                            "High Pressure"                                  <pressure>    { unit="bar", channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemInformationWpm3i#high-pressure" }
Number:Pressure           stiebel_eltron_heat_pump_wpm3i_low_pressure                             "Low Pressure"                                   <pressure>    { unit="bar", channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:systemInformationWpm3i#low-pressure" }

Number:Energy             stiebel_eltron_heat_pump_wpm3i_heat_quantity_today                      "Heat Quantity Today [%.0f kWh]"                 <energy>      { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:energyRuntimeInformationWpm3i#production-heat-today" }
Number:Energy             stiebel_eltron_heat_pump_wpm3i_heat_quantity_total                      "Heat Quantity Total [%.3f MWh]"                 <energy>      { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:energyRuntimeInformationWpm3i#production-heat-total" }
Number:Energy             stiebel_eltron_heat_pump_wpm3i_water_heat_quantity_today                "Water Heat Quantity Today [%.0f kWh]"           <energy>      { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:energyRuntimeInformationWpm3i#production-water-today" }
Number:Energy             stiebel_eltron_heat_pump_wpm3i_water_heat_quantity_total                "Water Heat Quantity Total [%.3f MWh]"           <energy>      { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:energyRuntimeInformationWpm3i#production-water-total" }
Number:Energy             stiebel_eltron_heat_pump_wpm3i_nhz_heating_quantity_total               "NHZ Heating Quantity Total [%.3f MWh]"          <energy>      { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:energyRuntimeInformationWpm3i#production-nhz-heat-total" }
Number:Energy             stiebel_eltron_heat_pump_wpm3i_nhz_hot_water_quantity_total             "NHZ Hot Water Quantity Total [%.3f MWh]"        <energy>      { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:energyRuntimeInformationWpm3i#production-nhz-water-total" }
Number:Energy             stiebel_eltron_heat_pump_wpm3i_power_consumption_for_heating_today      "Power Consumption for Heating Today [%.0f kWh]" <energy>      { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:energyRuntimeInformationWpm3i#consumption-heat-today" }
Number:Energy             stiebel_eltron_heat_pump_wpm3i_power_consumption_for_heating_total      "Power Consumption for Heating Total [%.0f kWh]" <energy>      { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:energyRuntimeInformationWpm3i#consumption-heat-total" }
Number:Energy             stiebel_eltron_heat_pump_wpm3i_power_consumption_for_water_today        "Power Consumption for Water Today [%.0f kWh]"   <energy>      { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:energyRuntimeInformationWpm3i#consumption-water-today" }
Number:Energy             stiebel_eltron_heat_pump_wpm3i_power_consumption_for_water_total        "Power Consumption for Water Total [%.0f kWh]"   <energy>      { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:energyRuntimeInformationWpm3i#consumption-water-total" }
Number:Time               stiebel_eltron_heat_pump_wpm3i_heating_compressor_runtime               "Heating Compressor Runtime"                     <time>        { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:energyRuntimeInformationWpm3i#heating-runtime" }
Number:Time               stiebel_eltron_heat_pump_wpm3i_hot_water_runtime                        "Hot Water Compressor Runtime"                   <time>        { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:energyRuntimeInformationWpm3i#hotwater-runtime" }
Number:Time               stiebel_eltron_heat_pump_wpm3i_cooling_runtime                          "Cooling Compressor Runtime"                     <time>        { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:energyRuntimeInformationWpm3i#cooling-runtime" }
Number:Time               stiebel_eltron_heat_pump_wpm3i_emergency_heating_stage_1_runtime        "Emergency Heating Stage 1 Runtime"              <time>        { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:energyRuntimeInformationWpm3i#nhz1-runtime" }
Number:Time               stiebel_eltron_heat_pump_wpm3i_emergency_heating_stage_2_runtime        "Emergency Heating Stage 2 Runtime"              <time>        { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:energyRuntimeInformationWpm3i#nhz2-runtime" }
Number:Time               stiebel_eltron_heat_pump_wpm3i_emergency_heating_stages_12_runtime      "Emergency Heating Stages 1+2 Runtime"           <time>        { channel="modbus:stiebeleltron-heatpump-wpm3i:stiebelEltronWPM3i:energyRuntimeInformationWpm3i#nhz12-runtime" }
```


### Item Configuration *Stiebel Eltron ISG SG Ready Energy Management*

```java
Number                    stiebel_eltron_sg_ready_on_off_switch                                   "SG Ready Energy Management Operating State"     <settings>    { channel="modbus:stiebeleltron-isg-sg-ready-em:se-isg-sg-ready-em:sgReadyEnergyManagementSettings#sg-ready-on-off-switch" }
Number                    stiebel_eltron_sg_ready_input_lines                                     "SG Ready Input Lines"                           <settings>    { channel="modbus:stiebeleltron-isg-sg-ready-em:se-isg-sg-ready-em:sgReadyEnergyManagementSettings#sg-ready-input-lines" }

Number                    stiebel_eltron_sg_ready_operating_state                                 "SG Ready Energy Management Operating State"     <status>      { channel="modbus:stiebeleltron-isg-sg-ready-em:se-isg-sg-ready-em:sgReadyEnergyManagementSystemInformation#sg-ready-operating-state" }
Number                    stiebel_eltron sg_ready_controller_identification                       "SG Ready Controller Identification"             <status>      { channel="modbus:stiebeleltron-isg-sg-ready-em:se-isg-sg-ready-em:sgReadyEnergyManagementSystemInformation#sg-ready-controller-identification" }
```
