# Growatt Binding

![Growatt](doc/growatt.png)

This binding supports the integration of Growatt solar inverters.

It depends on the independent [Grott](https://github.com/johanmeijer/grott#the-growatt-inverter-monitor) proxy server application.
This intercepts the logging data that the Growatt inverter data logger normally sends directly to the Growatt cloud server.
It sends the original (encoded) data onwards to the cloud server (so the cloud server will not notice anything different).
But it also sends a (decoded) copy to OpenHAB as well.

## Supported Things

The binding supports two types of things:

- `bridge`: The bridge is the interface to the Grott application; it receives the data from all inverters.
- `inverter`: The inverter thing contains channels which are updated with solor production and consumption data.

## Discovery

There is no automatic discovery of the bridge.
However if a bridge exists and it receives inverter data, then a matching inverter thing is created in the Inbox.

## Thing Configuration

The `bridge` thing requires no configuration.

The `inverter` thing requires configuration of its serial number resp. `deviceId`:

| Name      | Type    | Description                                                                              | Required |
|-----------|---------|------------------------------------------------------------------------------------------|----------|
| deviceId  | text    | Device serial number or id as configured in the Growatt cloud and the Grott application. | yes      |

## Channels

The `bridge` thing has no channels.

The `inverter` thing supports many possible channels relating to solar generation and consumption.
All channels are read only.
Depending on the inverter model, and its configuration, not all of the channels will be present.
The list of all possible channels is as follows:

| Channel                       | Type                      | Description                                          | Advanced |
|-------------------------------|---------------------------|------------------------------------------------------|----------|
| system-status                 | Number:Dimensionless      | Inverter status code.                                |          |
| pv-power-in                   | Number:Power              | Total DC solar input power.                          |          |
| pv-power-out                  | Number:Power              | Total AC solar output power.                         |          |
| pv1-voltage                   | Number:ElectricPotential  | DC voltage from solar panel string #1.               | yes      |
| pv2-voltage                   | Number:ElectricPotential  | DC voltage from solar panel string #2.               | yes      |
| pv1-current                   | Number:ElectricCurrent    | DC current from solar panel string #1.               | yes      |
| pv2-current                   | Number:ElectricCurrent    | DC current from solar panel string #2.               | yes      |
| pv1-power                     | Number:Power              | DC power from solar panel string #1.                 | yes      |
| pv2-power                     | Number:Power              | DC power from solar panel string #2.                 | yes      |
| grid-frequency                | Number:Frequency          | Frequency of the grid.                               | yes      |
| grid-voltage                  | Number:ElectricPotential  | Voltage of the grid (phase #R).                      |          |
| grid-voltage-s                | Number:ElectricPotential  | Voltage of the grid phase #S.                        | yes      |
| grid-voltage-t                | Number:ElectricPotential  | Voltage of the grid phase #T.                        | yes      |
| grid-voltage-rs               | Number:ElectricPotential  | Voltage of the grid phases #RS.                      | yes      |
| grid-voltage-st               | Number:ElectricPotential  | Voltage of the grid phases #ST.                      | yes      |
| grid-voltage-tr               | Number:ElectricPotential  | Voltage of the grid phases #TR.                      | yes      |
| solar-current                 | Number:ElectricCurrent    | AC current from solar (phase #R).                    | yes      |
| solar-current-s               | Number:ElectricCurrent    | AC current from solar phase #S.                      | yes      |
| solar-current-t               | Number:ElectricCurrent    | AC current from solar phase #T.                      | yes      |
| solar-power                   | Number:Power              | AC power from solar (phase #R).                      |          |
| solar-power-s                 | Number:Power              | AC power from solar phase #S.                        | yes      |
| solar-power-t                 | Number:Power              | AC power from solar phase #T.                        | yes      |
| solar-va                      | Number:Power              | AC VA from solar.                                    | yes      |
| charge-power                  | Number:Power              | Battery charge power.                                |          |
| charge-current                | Number:ElectricCurrent    | Battery charge current.                              | yes      |
| discharge-power               | Number:Power              | Battery discharge power.                             |          |
| discharge-va                  | Number:Power              | Battery discharge VA.                                | yes      |
| export-power                  | Number:Power              | Power exported to grid.                              |          |
| export-power-r                | Number:Power              | Power exported to grid phase #R.                     | yes      |
| export-power-s                | Number:Power              | Power exported to grid phase #S.                     | yes      |
| export-power-t                | Number:Power              | Power exported to grid phase #T.                     | yes      |
| import-power                  | Number:Power              | Power imported from grid.                            |          |
| import-power-r                | Number:Power              | Power imported from grid phase #R.                   | yes      |
| import-power-s                | Number:Power              | Power imported from grid phase #S.                   | yes      |
| import-power-t                | Number:Power              | Power imported from grid phase #T.                   | yes      |
| load-power                    | Number:Power              | Power supplied to load.                              |          |
| load-power-r                  | Number:Power              | Power supplied to load phase #R.                     | yes      |
| load-power-s                  | Number:Power              | Power supplied to load phase #S.                     | yes      |
| load-power-t                  | Number:Power              | Power supplied to load phase #T.                     | yes      |
| solar-energy-today            | Number:Energy             | Solar AC energy produced today.                      |          |
| solar-energy-total            | Number:Energy             | Total solar AC energy produced.                      |          |
| pv-energy-today               | Number:Energy             | DC energy collected by solar panels today.           |          |
| pv1-energy-today              | Number:Energy             | DC energy collected by solar panels string #1 today. | yes      |
| pv2-energy-today              | Number:Energy             | DC energy collected by solar panels string #2 today. | yes      |
| pv-energy-total               | Number:Energy             | Total DC energy collected by solar panels.           |          |
| pv1-energy-total              | Number:Energy             | Total DC energy collected by solar panels string #1. | yes      |
| pv2-energy-total              | Number:Energy             | Total DC energy collected by solar panels string #2. | yes      |
| export-energy-today           | Number:Energy             | Energy exported today.                               |          |
| export-energy-total           | Number:Energy             | Total energy exported.                               |          |
| import-energy-today           | Number:Energy             | Energy imported today.                               |          |
| import-energy-total           | Number:Energy             | Total energy imported.                               |          |
| load-energy-today             | Number:Energy             | Energy supplied to load today.                       |          |
| load-energy-total             | Number:Energy             | Total energy supplied to load.                       |          |
| import-charge-energy-today    | Number:Energy             | Energy imported to charge battery today.             |          |
| import-charge-energy-total    | Number:Energy             | Total energy imported to charge battery.             |          |
| solar-charge-energy-today     | Number:Energy             | Solar energy to charge battery today.                |          |
| solar-charge-energy-total     | Number:Energy             | Total solar energy to charge battery.                |          |
| discharge-energy-today        | Number:Energy             | Energy consumed from battery.                        |          |
| discharge-energy-total        | Number:Energy             | Total energy consumed from battery.                  |          |
| total-work-time               | Number:Time               | Total work time of the system.                       | yes      |
| p-bus-voltage                 | Number:ElectricPotential  | P Bus voltage.                                       | yes      |
| n-bus-voltage                 | Number:ElectricPotential  | N Bus voltage.                                       | yes      |
| sp-bus-voltage                | Number:ElectricPotential  | SP Bus voltage.                                      | yes      |
| pv-temperature                | Number:Temperature        | Temperature of the solar panels (string #1).         | yes      |
| pv-ipm-temperature            | Number:Temperature        | Temperature of the IPM.                              | yes      |
| pv-boost-temperature          | Number:Temperature        | Boost temperature.                                   | yes      |
| temperature-4                 | Number:Temperature        | Temperature #4.                                      | yes      |
| pv2-temperature               | Number:Temperature        | Temperature of the solar panels (string #2).         | yes      |
| battery-type                  | Number:Dimensionless      | Type code of the battery.                            | yes      |
| battery-temperature           | Number:Temperature        | Battery temperature.                                 | yes      |
| battery-voltage               | Number:ElectricPotential  | Battery voltage.                                     | yes      |
| battery-display               | Number:Dimensionless      | Battery display code.                                | yes      |
| battery-soc                   | Number:Dimensionless      | Battery State of Charge percent.                     | yes      |
| system-fault-0                | Number:Dimensionless      | System fault code #0.                                | yes      |
| system-fault-1                | Number:Dimensionless      | System fault code #1.                                | yes      |
| system-fault-2                | Number:Dimensionless      | System fault code #2.                                | yes      |
| system-fault-3                | Number:Dimensionless      | System fault code #3.                                | yes      |
| system-fault-4                | Number:Dimensionless      | System fault code #4.                                | yes      |
| system-fault-5                | Number:Dimensionless      | System fault code #5.                                | yes      |
| system-fault-6                | Number:Dimensionless      | System fault code #6.                                | yes      |
| system-fault-7                | Number:Dimensionless      | System fault code #7.                                | yes      |
| system-work-mode              | Number:Dimensionless      | System work mode code.                               | yes      |
| sp-display-status             | Number:Dimensionless      | Solar panel display status code.                     | yes      |
| constant-power-ok             | Number:Dimensionless      | Constant power OK code.                              | yes      |
| load-percent                  | Number:Dimensionless      | Percent of full load.                                | yes      |
| rac                           | Number:Dimensionless      | RAC code.                                            | yes      |
| erac-today                    | Number:Dimensionless      | ERAC count today.                                    | yes      |
| erac-total                    | Number:Dimensionless      | Total ERAC count.                                    | yes      |

## Full Example

### Example `.things` file

```java
Bridge growatt:bridge:home "Growattt Bridge" [] {
    Thing inverter sph "Growatt SPH Inverter" [deviceId="INVERTERTID"]
}
```

### Example `.items` file

```java
Number:ElectricPotential Solar_String1_Voltage "Solar String #1 PV Voltage" {channel="growatt:inverter:home:sph:pv1-voltage"}
Number:ElectricCurrent Solar_String1_Current "Solar String #1 PV Current" {channel="growatt:inverter:home:sph:pv1-current"}
Number:Power Solar_String1_Power "Solar String #1 PV Power" {channel="growatt:inverter:home:sph:pv1-power"}
Number:Energy Solar_Output_Energy "Solar Output Energy Total" {channel="growatt:inverter:home:sph:pv-energy-total"}
```

## Grott Application Installation and Setup

You can install the Grott application either on the same computer as OpenHAB or on another.
The following assumes you will be running it on the same computer.
Grott application acts as a proxy server between your Growatt inverter and the Growatt cloud server.
It intercepts data packets between the inverter and the cloud server, and it sends a copy of the intercepted data also to OpenHAB.

**NOTE**: make sure that the Grott application is **FULLY OPERATIONAL** for your inverter **BEFORE** you create any things in OpenHAB!
Otherwise the binding might create a wrong (or even empty) list of channels for the inverter thing.
(Yet if you do make that mistake you can rectify it by deleteing and recreating the thing).

You should configure the Grott application via its `grott.ini` file.
Configure Grott to match your inverter according to the [instructions](https://github.com/johanmeijer/grott#the-growatt-inverter-monitor).

### Install Python

If Python is not already installed on you computer, then install it first.
And install the following additional necessary python packages:

```bash
sudo pip3 install paho-mqtt
sudo pip3 install requests
```

### Install Grott

First install the Grott application and the Grott application extension files in a Grott specific home folder.
Note that Grott requires the `grottext.py` application extension in addition to the standard application files.
The installation is as follows:

- Create a 'home' sub-folder for Grott e.g. `/home/<username>/grott/`.
- Copy `grott.py`, `grottconf.py`, `grottdata.py`, `grottproxy.py`, `grottsniffer.py`, `grottserver.py` to the home folder.
- Copy `grottext.py` application extension to the home folder.
- Copy `grott.ini` configuration file to the home folder.
- Modify `grott.ini` to run in proxy mode; not in compatibility mode; show your inverter type; not run MQTT; not run PVOutput; enable the `grottext` extension; and set the OpenHAB `/growatt` servlet url.

A suggested Grott configuration for OpenHAB is as follows:

```php
[Generic]
mode = proxy
compat = False
invtype = sph // your inverter type

[MQTT]
nomqtt = True // disable mqtt

[PVOutput]
pvoutput = False // disable pvoutput

[extension]
extension = True
extname = grottext
extvar = {"url": "http://xxx.xxx.xxx.xxx:8080/growatt"}
```

### Start Grott as a Service

Finally you should set your computer to starts the Grott application automatically as a service when your computer starts.
For Windows see wiki: https://github.com/johanmeijer/grott/wiki/Grott-as-a-service-(Windows)
For Linux see wiki: https://github.com/johanmeijer/grott/wiki/Grott-as-a-service-(Linux)
The service configuration for Linux is summarised below:

- Copy the `grott.service` file to the `/etc/systemd/system/` folder
- Modify `grott.service` to enter your user name; the Grott settings; the path to Python; and the path to the Grott application:

```php
[Service]
SyslogIdentifier=grott
User=<username>  // your username
WorkingDirectory=/home/<username>/grott/ // your home grott folder
ExecStart=-/usr/bin/python3 -u /home/<username>/grott/grott.py -v // ditto
```

And finally enable the Grott service:

```bash
sudo systemctl enable grott
```

### Route Growatt Inverter Logging via Grott Proxy

Normally the Growatt inverter sends its logging data directly to port `5279` on the Growatt server at `server.growatt.com` (ip=47.91.67.66) on the cloud.
Grott is a proxy server that interposes itself beween the inverter and the cloud server.
i.e. it receives the inverter logging data and forwards it unchanged to the cloud server.

**WARNING**: make sure that Grott is running on a computer with a **STATIC IP ADDRESS** (and note this safely)!
Otherwise if the computer changes its ip address dynamically, it can no longer intercept the inverter data.
This means **YOU WILL NO LONGER BE ABLE TO RESET THE INVERTER** to its original settings!

You need to use the Growatt App to tell the inverter to send its logging data to the Grott proxy instead of to the cloud.
See wiki: https://github.com/johanmeijer/grott/wiki/Rerouting-Growatt-Wifi-TCPIP-data-via-your-Grott-Server for more information.
