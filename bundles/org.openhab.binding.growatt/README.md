# Growatt Binding

![Growatt](doc/growatt.png)

This binding supports the integration of Growatt solar inverters.
It depends on the independent [Grott](https://github.com/johanmeijer/grott#the-growatt-inverter-monitor) proxy server application to intercept the data transmissions between the inverter and the Growatt cloud server.

## Supported Things

The binding supports two types of things:

- `bridge`: The bridge is the interface to the Grott application; it receives the data from all inverters.
- `inverter`: The inverter thing contains channels which are updated with solor production and consumption data.

## Discovery

There is no automatic discovery of the bridge or inverter things.

## Grott Application

The Grott application acts as a proxy server between your Growatt inverter and the Growatt cloud server.
It intercepts and decodes the data packets sent from the inverter to the cloud server.
And it uses the `grottext.py` application extension to send a copy of the intercepted data also to your OpenHAB system.
The data is transmitted via an HTTP POST to the 'http://openhab-ip-address:8080/growatt' end point with a JSON pay-load.

You need to install the Grott application either on the same computer as OpenHAB or on another computer.

_**NOTE**: make sure that the Grott application is fully operational for your inveter **BEFORE** you create any things in OpenHAB!_

You should configure the Grott application via its `grott.ini` file.
Configure Grott to match your inverter according to the [instructions](https://github.com/johanmeijer/grott#the-growatt-inverter-monitor).
To operate with OpenHAB the recommended Grott configuration is as follows:

- Configure Grott to run in proxy mode.
- Configure Grott to start as a service.
- Configure your inverter type in Grott.
- Install the `grottext.py` application extension in the Grott home folder.
- Configure the `grottext` extension's ip address, port, and path via `grott.ini` as follows:

| Entry   | Configuration Entry Value                        |
|---------|--------------------------------------------------|
| ip      | IP address of your OpenHab computer.             |
| port    | Port of your OpenHab core server (usually 8080). |
| path    | 'growatt' (fixed value).                         |

## Thing Configuration

The `bridge` thing requires no configuration.

The `inverter` thing requires configuration of its serial number resp. `deviceId`:

| Name      | Type    | Description                                                                               | Required |
|-----------|---------|-------------------------------------------------------------------------------------------|----------|
| deviceId  | text    | Device serial number or id as configuted in the Growatt cloud, and the Grott application. | yes      |

## Channels

The `bridge` thing has no channels.

The `inverter` thing supports many possible channels relating to solar generation and consumption.
All channels are read only.
Depending on the inverter model, and it configuration, not all of the channels will be present.
The list of all possible channels is as follows:

| Channel            | Type               | Description                                         |
|--------------------|--------------------|-----------------------------------------------------|
| pvstatus           | Number             | Status of the inverter (0=ready, 1=online, 2=fault) |
| pvpowerin          | Number:power       | Total solar input power.                            |
| pv1voltage         | Number:voltage     | Voltage from solar panel string #1.                 |
| pv1current         | Number:current     | Current from solar panel string #1.                 |
| pv1watt            | Number:power       | Power from solar panel string #1.                   |
| pv2voltage         | Number:voltage     | Voltage of solar panel string #2.                   |
| pv2current         | Number:current     | Current from solar panel string #2.                 |
| pv2watt            | Number:power       | Power from solar panel string #2.                   |
| pvpowerout         | Number:power       | Total solar output power.                           |
| pvfrequency        | Number:frequency   | Frequency of the grid supply.                       |
| pvgridvoltage      | Number:voltage     | Voltage of the grid supply.                         |
| pvgridcurrent      | Number:current     | Current delivered to the grid supply.               |
| pvgridpower        | Number:power       | Power delivered to the grid supply.                 |
| pvgridvoltage2     | Number:voltage     | Voltage of the grid supply #2.                      |
| pvgridcurrent2     | Number:current     | Current delivered to the grid supply #2.            |
| pvgridpower2       | Number:power       | Power delivered to the grid supply #2.              |
| pvgridvoltage3     | Number:voltage     | Voltage of the grid supply #3.                      |
| pvgridcurrent3     | Number:current     | Current delivered to the grid supply #3.            |
| pvgridpower3       | Number:power       | Power delivered to the grid supply #3.              |
| pvenergytoday      | Number:energy      | Solar energy collected today.                       |
| pvenergytotal      | Number:energy      | Total solar energy collected.                       |
| totworktime        | Number:time        | Total uptime of the inverter.                       |
| epv1today          | Number:energy      | Energy from solar panel string #1 today.            |
| epv1total          | Number:energy      | Total energy from solar panel string #1.            |
| epv2today          | Number:energy      | Energy from solar panel string #2 today.            |
| epv2total          | Number:energy      | Total energy from solar panel string #2.            |
| epvtotal           | Number:energy      | Total energy from all solar panels.                 |
| pvtemperature      | Number:temperature | Temperature of the solar panels.                    |
| pvipmtemperature   | Number:temperature | Temperature of the IPM.                             |
| pvboosttemperature | Number:temperature | Boost temperature.                                  |
| temp4              | Number:temperature | Temperature #4.                                     |
| Vac_RS             | Number:voltage     | AC voltage R-S.                                     |
| Vac_ST             | Number:voltage     | AC voltage S-T.                                     |
| Vac_TR             | Number:voltage     | AC voltage T-R.                                     |
| uwBatVolt_DSP      | Number:voltage     | Battery voltage DSP.                                |
| pbusvolt           | Number:voltage     | P Bus voltage.                                      |
| nbusvolt           | Number:voltage     | N Bus voltage.                                      |
| eacCharToday       | Number:energy      | AC charge energy today.                             |
| eacCharTotal       | Number:energy      | Total AC charge energy.                             |
| ebatDischarToday   | Number:energy      | Battery discharge energy today.                     |
| ebatDischarTotal   | Number:energy      | Total battery discharge energy.                     |
| eacDischarToday    | Number:energy      | AC discharge energy today.                          |
| eacDischarTotal    | Number:energy      | Total AC discharge energy.                          |
| ACCharCurr         | Number:current     | AC charge current.                                  |
| ACDischarWatt      | Number:power       | AC discharge power.                                 |
| ACDischarVA        | Number:va          | AC discharge VA.                                    |
| BatDischarWatt     | Number:power       | Battery discharge power.                            |
| BatDischarVA       | Number:va          | Battery VA.                                         |
| BatWatt            | Number:power       | Battery power.                                      |

## Full Example

### Thing Configuration

```java
Example thing configuration goes here.
```

### Item Configuration

```java
Example item configuration goes here.
```
