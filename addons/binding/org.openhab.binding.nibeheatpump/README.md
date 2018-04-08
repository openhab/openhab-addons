# Nibe Heatpump Binding

The Nibe Heatpump binding is used to get live data from from Nibe heat pumps without using expensive MODBUS40 adapter. This binding should be compatible with at least the F1145 and F1245 heat pump models.

The binding support data telegrams (contains max 20 registers) from the heat pump, but binding can also read other registers from the pump.
It's recommend to add most changed variables to telegram, binding will then read all other registers automatically from the pump when channels are linked to item.
Register reading need to be enabled from the thing configuration.
Binding have also experimental support for register writing.
This can be used to configure heat pump.
Write mode need to enabled from thing configuration and for safety reason all register identifiers need to explicitly defined.

## Supported Things

This binding supports direct serial port connection (RS-485 adapter needed) to heat pump but also UDP connection via NibeGW software.

| Thing type      | Description                                      |
|-----------------|--------------------------------------------------|
| f1x45-serial    | Serial port connected F1145 and F1245 Heat Pumps |
| f1x45-udp       | UDP connected Nibe F1145 and F1245 Heat Pumps    |
| f1x45-simulator | Simulator for Nibe F1145 and F1245 Heat Pumps    |

## Discovery

Discovery is not supported, therefore binding and things need to be configured via Paper UI or thing files. 

## Prerequisites

When Modbus adapter support is enabled from the heat pump UI, the heat pump will start to send telegrams every now and then. A telegram contains a maximum of 20 registers.
Those 20 registers can be configured via the Nibe ModbusManager application.

Unfortunately Nibe has made this tricky: telegram from heat pump should be acknowledged, otherwise heat pump will raise an alarm and go in alarm state.
Acknowledge (ACK or NAK) should be sent accurately.
Binding support also direct serial port connections to heat pump, but heat pump will raise an alarm when openHAB binding not running e.g. during openHAB updates.
This problem can be resolved by using the `nibegw` program, which can be run on Unix/Linux (such as a Raspberry Pi) or Arduino-based boards.
If you are running openHAB on Raspberry Pi `nibegw` program can run also on the same machine, then when openHAB is not running `nibegw` will still acknowledge packets to heat pump.
Obviously, this doesn't solve the problem when whole Raspberry Pi is down, therefore Arduino based solution is recommended.

`nibegw` is an application that read telegrams from a serial port (which requires an RS-485 adapter), sends ACK/NAK to the heat pump and relays untouched telegrams to openHAB via UDP packets.
The Nibe Heat Pump binding will listen to a UDP port and parse register data from UDP telegrams.

### Arduino

Arduino-based solution is tested with Arduino uno + RS485 and Ethernet shields.
Also [ProDiNo](https://www.kmpelectronics.eu/en-us/products/prodinoethernet.aspx) NetBoards are supported.
ProDiNo have both Ethernet and RS-485 ports on the board.

Arduino code is available [here](https://github.com/openhab/openhab2-addons/tree/master/addons/binding/org.openhab.binding.nibeheatpump/NibeGW/Arduino/)

Arduino code can be builded via Arduino IDE. See more details from [www.arduino.cc](https://www.arduino.cc/en/Main/Software). 
NibeGW configuration(such IP addresses, ports, etc) can be adapted directly editing the code files.

### Raspberry Pi (or other Linux/Unix based boards)

C code is available on [here](https://github.com/openhab/openhab2-addons/tree/master/addons/binding/org.openhab.binding.nibeheatpump/NibeGW/RasPi/)

build C code: 

```shell
gcc -std=gnu99 -o nibegw nibegw.c
```


help:

```shell
nibegw -h

./nibegw usage:

    -h                 Print help
    -v                 Print debug information
    -d <device name>   Serial port device (default: /dev/ttyS0)
    -a <address>       Remote host address (default: 127.0.0.1)
    -p <port>          Remote UDP port (default: 9999)
    -f                 Disable flow control (default: HW)
    -r <address>       RS-485 address to listen (default: 0x20)
    -i                 Send all messages by UDP (default: only modbus data)
    -n                 Don't send acknowledge at all
    -o                 Send acknowledge to all addresses
    -t                 Test mode
    -l <port>          Local UDP port for read commands (default: 9999)
    -w <port>          Local UDP port for write commands (default: 10000)
    -q                 Print data in log format

```

run example:

```shell
nibegw -v -d /dev/ttyUSB0 -a 192.168.1.10
```

## Binding Configuration

No binding configuration required.

## Thing Configuration

Things can be fully configured via Paper Ui, but following information is usefull if you want to configure things via thing configuration files.

### UDP connection

Thing examples:

```
nibeheatpump:f1x45-udp:myPump [hostName="192.168.1.50", port=9999]
```

```
nibeheatpump:f1x45-udp:myPump [hostName="192.168.1.50", port=9999, readCommandsPort=10000, writeCommandsPort=10001, refreshInterval=30, enableReadCommands=true, enableWriteCommands=true, enableRegistersForWriteCommands="44266, 47004"]
```

All supported configuration parameters for UDP connection:

| Property                        | Type    | Default | Required | Description |
|---------------------------------|---------|---------|----------|-------------|
| hostName                        | String  |         | Yes      | Network address of the Nibe heat pump |
| port                            | Integer | 9999    | No       | UDP port to listening data packets from the NibeGW |
| readCommandsPort                | Integer | 9999    | No       | UDP port to send read commands to the NibeGW |
| writeCommandsPort               | Integer | 10000   | No       | UDP port to send write commands to the NibeGW |
| refreshInterval                 | Integer | 60      | No       | States how often a refresh shall occur in seconds |
| enableReadCommands              | Boolean | false   | No       | Enable read commands to read additional variable from Nibe heat pump which are not included to data readout messages. This is experimental feature, use it at your own risk! |
| enableWriteCommands             | Boolean | false   | No       | Enable write commands to change Nibe heat pump settings. This is experimental feature, use it at your own risk! |
| enableRegistersForWriteCommands | String  |         | No       | Comma separated list of registers, which are allowed to write to Nibe heat pump. E.g. 44266, 47004 |


### Serial port connection

Thing example:

```
nibeheatpump:f1x45-serial:myPump [serialPort="/dev/ttyUSB0"]
```


All supported configuration parameters for serial port connection:

| Property                        | Type    | Default | Required | Description |
|---------------------------------|---------|---------|----------|-------------|
| serialPort                      | String  |         | Yes      | Network address of the Nibe heat pump |
| refreshInterval                 | Integer | 60      | No       | States how often a refresh shall occur in seconds |
| enableReadCommands              | Boolean | false   | No       | Enable read commands to read additional variable from Nibe heat pump which are not included to data readout messages. This is experimental feature, use it at your own risk! |
| enableWriteCommands             | Boolean | false   | No       | Enable write commands to change Nibe heat pump settings. This is experimental feature, use it at your own risk! |
| enableRegistersForWriteCommands | String  |         | No       | Comma separated list of registers, which are allowed to write to Nibe heat pump. E.g. 44266, 47004 |
| sendAckToMODBUS40               | Boolean | true    | No       | Binding emulates MODBUS40 device and send protocol acknowledges to heat pump |
| sendAckToRMU40                  | Boolean | false   | No       | Binding emulates RMU40 device and send protocol acknowledges to heat pump |
| sendAckToSMS40                  | Boolean | false   | No       | Binding emulates SMS40 device and send protocol acknowledges to heat pump |


## Channels

This binding currently supports following channels for F1x45 pump models:

| Channel Type ID | Item Type | Min | Max | Type | Description | Values |
|-----------------|-----------|-----|-----|------|-------------|--------|
| 40004 | Number | -32767 | 32767 | Setting | BT1 Outdoor temp |  |
| 40005 | Number | -32767 | 32767 | Setting | EP23-BT2 Supply temp S4 |  |
| 40006 | Number | -32767 | 32767 | Setting | EP22-BT2 Supply temp S3 |  |
| 40007 | Number | -32767 | 32767 | Setting | EP21-BT2 Supply temp S2 |  |
| 40008 | Number | -32767 | 32767 | Setting | BT2 Supply temp S1 |  |
| 40012 | Number | -32767 | 32767 | Setting | EB100-EP14-BT3 Return temp |  |
| 40013 | Number | -32767 | 32767 | Setting | BT7 Hot Water top |  |
| 40014 | Number | -32767 | 32767 | Setting | BT6 Hot Water load |  |
| 40015 | Number | -32767 | 32767 | Setting | EB100-EP14-BT10 Brine in temp |  |
| 40016 | Number | -32767 | 32767 | Setting | EB100-EP14-BT11 Brine out temp |  |
| 40017 | Number | -32767 | 32767 | Setting | EB100-EP14-BT12 Cond. out |  |
| 40018 | Number | -32767 | 32767 | Setting | EB100-EP14-BT14 Hot gas temp |  |
| 40019 | Number | -32767 | 32767 | Setting | EB100-EP14-BT15 Liquid line |  |
| 40022 | Number | -32767 | 32767 | Setting | EB100-EP14-BT17 Suction |  |
| 40025 | Number | -32767 | 32767 | Setting | EB100-BT20 Exhaust air temp. 1 |  |
| 40026 | Number | -32767 | 32767 | Setting | EB100-BT21 Vented air temp. 1 |  |
| 40028 | Number | -32767 | 32767 | Setting | AZ1-BT26 Temp Collector in FLM 1 |  |
| 40029 | Number | -32767 | 32767 | Setting | AZ1-BT27 Temp Collector out FLM 1 |  |
| 40030 | Number | -32767 | 32767 | Setting | EP23-BT50 Room Temp S4 |  |
| 40031 | Number | -32767 | 32767 | Setting | EP22-BT50 Room Temp S3 |  |
| 40032 | Number | -32767 | 32767 | Setting | EP21-BT50 Room Temp S2 |  |
| 40033 | Number | -32767 | 32767 | Setting | BT50 Room Temp S1 |  |
| 40042 | Number | -32767 | 32767 | Setting | CL11-BT51 Pool 1 Temp |  |
| 40043 | Number | -32767 | 32767 | Setting | EP8-BT53 Solar Panel Temp |  |
| 40044 | Number | -32767 | 32767 | Setting | EP8-BT54 Solar Load Temp |  |
| 40045 | Number | -32767 | 32767 | Setting | EQ1-BT64 Cool Supply Temp |  |
| 40046 | Number | -32767 | 32767 | Setting | EQ1-BT65 Cool Return Temp |  |
| 40054 | Number | -32767 | 32767 | Setting | EB100-FD1 Temperature limiter |  |
| 40067 | Number | -32767 | 32767 | Setting | BT1 Average |  |
| 40070 | Number | -32767 | 32767 | Setting | EM1-BT52 Boiler temperature |  |
| 40071 | Number | -32767 | 32767 | Setting | BT25 external supply temp |  |
| 40072 | Number | -32767 | 32767 | Setting | BF1 Flow |  |
| 40074 | Number | -32767 | 32767 | Setting | EB100-FR1 Anode Status |  |
| 40079 | Number | -2147483648 | 2147483647 | Setting | EB100-BE3 Current Phase 3 |  |
| 40081 | Number | -2147483648 | 2147483647 | Setting | EB100-BE2 Current Phase 2 |  |
| 40083 | Number | -2147483648 | 2147483647 | Setting | EB100-BE1 Current Phase 1 |  |
| 40106 | Number | -32767 | 32767 | Setting | CL12-BT51 Pool 2 Temp |  |
| 40107 | Number | -32767 | 32767 | Setting | EB100-BT20 Exhaust air temp. 4 |  |
| 40108 | Number | -32767 | 32767 | Setting | EB100-BT20 Exhaust air temp. 3 |  |
| 40109 | Number | -32767 | 32767 | Setting | EB100-BT20 Exhaust air temp. 2 |  |
| 40110 | Number | -32767 | 32767 | Setting | EB100-BT21 Vented air temp. 4 |  |
| 40111 | Number | -32767 | 32767 | Setting | EB100-BT21 Vented air temp. 3 |  |
| 40112 | Number | -32767 | 32767 | Setting | EB100-BT21 Vented air temp. 2 |  |
| 40113 | Number | -32767 | 32767 | Setting | AZ4-BT26 Temp Collector in FLM 4 |  |
| 40114 | Number | -32767 | 32767 | Setting | AZ3-BT26 Temp Collector in FLM 3 |  |
| 40115 | Number | -32767 | 32767 | Setting | AZ2-BT26 Temp Collector in FLM 2 |  |
| 40116 | Number | -32767 | 32767 | Setting | AZ4-BT27 Temp Collector out FLM 4 |  |
| 40117 | Number | -32767 | 32767 | Setting | AZ3-BT27 Temp Collector out FLM 3 |  |
| 40118 | Number | -32767 | 32767 | Setting | AZ2-BT27 Temp Collector out FLM 2 |  |
| 40127 | Number | -32767 | 32767 | Setting | EP23-BT3 Return temp S4 |  |
| 40128 | Number | -32767 | 32767 | Setting | EP22-BT3 Return temp S3 |  |
| 40129 | Number | -32767 | 32767 | Setting | EP21-BT3 Return temp S2 |  |
| 40155 | Number | -32767 | 32767 | Setting | EQ1-BT57 Collector temp. |  |
| 40156 | Number | -32767 | 32767 | Setting | EQ1-BT75 Heatdump temp. |  |
| 43001 | Number | 0 | 65535 | Setting | Software version |  |
| 43005 | Number | -30000 | 30000 | Setting | Degree Minutes |  |
| 43006 | Number | -32767 | 32767 | Setting | Calculated Supply Temperature S4 |  |
| 43007 | Number | -32767 | 32767 | Setting | Calculated Supply Temperature S3 |  |
| 43008 | Number | -32767 | 32767 | Setting | Calculated Supply Temperature S2 |  |
| 43009 | Number | -32767 | 32767 | Setting | Calculated Supply Temperature S1 |  |
| 43013 | String | 0 | 255 | Setting | Freeze Protection Status | 1=Freeze protection active |
| 43024 | String | 0 | 255 | Setting | Status Cooling | 0=Off, 1=On |
| 43081 | Number | 0 | 9999999 | Setting | Tot. op.time add. |  |
| 43084 | Number | -32767 | 32767 | Setting | Int. el.add. Power |  |
| 43086 | String | 0 | 255 | Setting | Prio | 10=Off, 20=Hot Water, 30=Heat, 40=Pool, 41=Pool 2, 50=Transfer, 60=Cooling |
| 43091 | Number | 0 | 255 | Setting | Int. el.add. State |  |
| 43097 | String | 0 | 255 | Setting | Status of the shunt controlled additional heat accessory | 10=Off, 20=Running, 30=Passive |
| 43103 | Number | 0 | 255 | Setting | HPAC state |  |
| 43108 | Number | 0 | 255 | Setting | Fan speed current |  |
| 43152 | Number | 0 | 255 | Setting | Internal cooling blocked |  |
| 43158 | Number | 0 | 255 | Setting | External adjustment activated via input S4 |  |
| 43159 | Number | 0 | 255 | Setting | External adjustment activated via input S3 |  |
| 43160 | Number | 0 | 255 | Setting | External adjustment activated via input S2 |  |
| 43161 | Number | 0 | 255 | Setting | External adjustment activated via input S1 |  |
| 43163 | String | 0 | 255 | Setting | Blocking status of the shunt controlled add heat acc | 0=Unblocked, 1=Blocked |
| 43164 | Number | 0 | 255 | Setting | Cooling blocked |  |
| 43171 | String | 0 | 255 | Setting | Blocking status of the step controlled add heat acc | 0=Unblocked, 1=Blocked |
| 43189 | String | 0 | 255 | Setting | Ext. Heat Medium Pump | 0=Off, 1=On |
| 43230 | Number | 0 | 9999999 | Setting | Accumulated energy |  |
| 43239 | Number | 0 | 9999999 | Setting | Tot. HW op.time add. |  |
| 43395 | Number | 0 | 255 | Setting | HPAC Relays |  |
| 43416 | Number | 0 | 9999999 | Setting | Compressor starts EB100-EP14 |  |
| 43420 | Number | 0 | 9999999 | Setting | Tot. op.time compr. EB100-EP14 |  |
| 43424 | Number | 0 | 9999999 | Setting | Tot. HW op.time compr. EB100-EP14 |  |
| 43427 | String | 0 | 255 | Setting | Compressor State EP14 | 20=Stopped, 40=Starting, 60=Running, 100=Stopping |
| 43431 | Number | 0 | 255 | Setting | Supply Pump State EP14 |  |
| 43433 | Number | 0 | 255 | Setting | Brine pump state EP14 |  |
| 43435 | String | 0 | 255 | Setting | Compressor status EP14 | 0=Off, 1=On |
| 43437 | Number | 0 | 255 | Setting | HM-pump Status EP14 |  |
| 43439 | Number | 0 | 255 | Setting | Brinepump Status EP14 |  |
| 43473 | Number | 0 | 255 | Setting | Heat Compressors |  |
| 43474 | Number | 0 | 255 | Setting | Hot Water Compressors |  |
| 43475 | Number | 0 | 255 | Setting | Pool 1 Compressors |  |
| 43484 | Number | 0 | 255 | Setting | FLM Cooling Activated |  |
| 43485 | Number | 0 | 255 | Setting | FLM Cooling Activated |  |
| 43486 | Number | 0 | 255 | Setting | FLM Cooling Activated |  |
| 43487 | Number | 0 | 255 | Setting | FLM Cooling Activated |  |
| 43514 | Number | 0 | 255 | Setting | PCA-Base Relays EP14 |  |
| 43516 | Number | 0 | 255 | Setting | PCA-Power Relays EP14 |  |
| 43560 | Number | 0 | 255 | Setting | Pool 2 blocked |  |
| 43561 | Number | 0 | 255 | Setting | Pool 1 blocked |  |
| 43563 | Number | 0 | 255 | Setting | Pool 2 valve |  |
| 43564 | Number | 0 | 255 | Setting | Pool 1 valve |  |
| 43577 | Number | 0 | 255 | Setting | Pool 2 Compressors |  |
| 43580 | Number | 0 | 65535 | Setting | EB108 Version |  |
| 43598 | Number | 0 | 255 | Setting | EB108 Slave Type |  |
| 43599 | Number | 0 | 255 | Setting | EB108 Compressor Size |  |
| 43600 | Number | -32767 | 32767 | Setting | EB108-EP15-BT3 Return temp. |  |
| 43601 | Number | -32767 | 32767 | Setting | EB108-EP15-BT10 Brine in temp |  |
| 43602 | Number | -32767 | 32767 | Setting | EB108-EP15-BT11 Brine out temp |  |
| 43603 | Number | -32767 | 32767 | Setting | EB108-EP15-BT12 Cond. out |  |
| 43604 | Number | -32767 | 32767 | Setting | EB108-EP15-BT14 Hot gas temp |  |
| 43605 | Number | -32767 | 32767 | Setting | EB108-EP15-BT15 Liquid line |  |
| 43606 | Number | -32767 | 32767 | Setting | EB108-EP15-BT17 Suction |  |
| 43607 | Number | -32767 | 32767 | Setting | EB108-EP15-BT29 Compr. Oil. temp. |  |
| 43608 | Number | -32767 | 32767 | Setting | EB108-EP15-BP8 Pressure transmitter |  |
| 43609 | Number | 0 | 255 | Setting | EB108-EP15 Compressor State |  |
| 43610 | Number | 0 | 255 | Setting | EB108-EP15 Compr. time to start |  |
| 43611 | Number | 0 | 65535 | Setting | EB108-EP15 Relay status |  |
| 43612 | Number | 0 | 255 | Setting | EB108-EP15 Heat med. pump status |  |
| 43613 | Number | 0 | 255 | Setting | EB108-EP15 Brine pump status |  |
| 43614 | Number | 0 | 4294967295 | Setting | EB108-EP15 Compressor starts |  |
| 43616 | Number | 0 | 4294967295 | Setting | EB108-EP15 Tot. op.time compr |  |
| 43618 | Number | 0 | 4294967295 | Setting | EB108-EP15 Tot. HW op.time compr |  |
| 43620 | Number | 0 | 65535 | Setting | EB108-EP15 Alarm number |  |
| 43621 | Number | -32767 | 32767 | Setting | EB108-EP14-BT3 Return temp. |  |
| 43622 | Number | -32767 | 32767 | Setting | EB108-EP14-BT10 Brine in temp |  |
| 43623 | Number | -32767 | 32767 | Setting | EB108-EP14-BT11 Brine out temp |  |
| 43624 | Number | -32767 | 32767 | Setting | EB108-EP14-BT12 Cond. out |  |
| 43625 | Number | -32767 | 32767 | Setting | EB108-EP14-BT14 Hot gas temp |  |
| 43626 | Number | -32767 | 32767 | Setting | EB108-EP14-BT15 Liquid line |  |
| 43627 | Number | -32767 | 32767 | Setting | EB108-EP14-BT17 Suction |  |
| 43628 | Number | -32767 | 32767 | Setting | EB108-EP14-BT29 Compr. Oil. temp. |  |
| 43629 | Number | -32767 | 32767 | Setting | EB108-EP14-BP8 Pressure transmitter |  |
| 43630 | Number | 0 | 255 | Setting | EB108-EP14 Compressor State |  |
| 43631 | Number | 0 | 255 | Setting | EB108-EP14 Compr. time to start |  |
| 43632 | Number | 0 | 65535 | Setting | EB108-EP14 Relay status |  |
| 43633 | Number | 0 | 255 | Setting | EB108-EP14 Heat med. pump status |  |
| 43634 | Number | 0 | 255 | Setting | EB108-EP14 Brine pump status |  |
| 43635 | Number | 0 | 9999999 | Setting | EB108-EP14 Compressor starts |  |
| 43637 | Number | 0 | 9999999 | Setting | EB108-EP14 Tot. op.time compr |  |
| 43639 | Number | 0 | 9999999 | Setting | EB108-EP14 Tot. HW op.time compr |  |
| 43641 | Number | 0 | 65535 | Setting | EB108-EP14 Alarm number |  |
| 43642 | Number | 0 | 65535 | Setting | EB107 Version |  |
| 43660 | Number | 0 | 255 | Setting | EB107 Slave Type |  |
| 43661 | Number | 0 | 255 | Setting | EB107 Compressor Size |  |
| 43662 | Number | -32767 | 32767 | Setting | EB107-EP15-BT3 Return temp. |  |
| 43663 | Number | -32767 | 32767 | Setting | EB107-EP15-BT10 Brine in temp |  |
| 43664 | Number | -32767 | 32767 | Setting | EB107-EP15-BT11 Brine out temp |  |
| 43665 | Number | -32767 | 32767 | Setting | EB107-EP15-BT12 Cond. out |  |
| 43666 | Number | -32767 | 32767 | Setting | EB107-EP15-BT14 Hot gas temp |  |
| 43667 | Number | -32767 | 32767 | Setting | EB107-EP15-BT15 Liquid line |  |
| 43668 | Number | -32767 | 32767 | Setting | EB107-EP15-BT17 Suction |  |
| 43669 | Number | -32767 | 32767 | Setting | EB107-EP15-BT29 Compr. Oil. temp. |  |
| 43670 | Number | -32767 | 32767 | Setting | EB107-EP15-BP8 Pressure transmitter |  |
| 43671 | Number | 0 | 255 | Setting | EB107-EP15 Compressor State |  |
| 43672 | Number | 0 | 255 | Setting | EB107-EP15 Compr. time to start |  |
| 43673 | Number | 0 | 65535 | Setting | EB107-EP15 Relay status |  |
| 43674 | Number | 0 | 255 | Setting | EB107-EP15 Heat med. pump status |  |
| 43675 | Number | 0 | 255 | Setting | EB107-EP15 Brine pump status |  |
| 43676 | Number | 0 | 4294967295 | Setting | EB107-EP15 Compressor starts |  |
| 43678 | Number | 0 | 4294967295 | Setting | EB107-EP15 Tot. op.time compr |  |
| 43680 | Number | 0 | 4294967295 | Setting | EB107-EP15 Tot. HW op.time compr |  |
| 43682 | Number | 0 | 65535 | Setting | EB107-EP15 Alarm number |  |
| 43683 | Number | -32767 | 32767 | Setting | EB107-EP14-BT3 Return temp. |  |
| 43684 | Number | -32767 | 32767 | Setting | EB107-EP14-BT10 Brine in temp |  |
| 43685 | Number | -32767 | 32767 | Setting | EB107-EP14-BT11 Brine out temp |  |
| 43686 | Number | -32767 | 32767 | Setting | EB107-EP14-BT12 Cond. out |  |
| 43687 | Number | -32767 | 32767 | Setting | EB107-EP14-BT14 Hot gas temp |  |
| 43688 | Number | -32767 | 32767 | Setting | EB107-EP14-BT15 Liquid line |  |
| 43689 | Number | -32767 | 32767 | Setting | EB107-EP14-BT17 Suction |  |
| 43690 | Number | -32767 | 32767 | Setting | EB107-EP14-BT29 Compr. Oil. temp. |  |
| 43691 | Number | -32767 | 32767 | Setting | EB107-EP14-BP8 Pressure transmitter |  |
| 43692 | Number | 0 | 255 | Setting | EB107-EP14 Compressor State |  |
| 43693 | Number | 0 | 255 | Setting | EB107-EP14 Compr. time to start |  |
| 43694 | Number | 0 | 65535 | Setting | EB107-EP14 Relay status |  |
| 43695 | Number | 0 | 255 | Setting | EB107-EP14 Heat med. pump status |  |
| 43696 | Number | 0 | 255 | Setting | EB107-EP14 Brine pump status |  |
| 43697 | Number | 0 | 9999999 | Setting | EB107-EP14 Compressor starts |  |
| 43699 | Number | 0 | 9999999 | Setting | EB107-EP14 Tot. op.time compr |  |
| 43701 | Number | 0 | 9999999 | Setting | EB107-EP14 Tot. HW op.time compr |  |
| 43703 | Number | 0 | 65535 | Setting | EB107-EP14 Alarm number |  |
| 43704 | Number | 0 | 65535 | Setting | EB106 Version |  |
| 43722 | Number | 0 | 255 | Setting | EB106 Slave Type |  |
| 43723 | Number | 0 | 255 | Setting | EB106 Compressor Size |  |
| 43724 | Number | -32767 | 32767 | Setting | EB106-EP15-BT3 Return temp. |  |
| 43725 | Number | -32767 | 32767 | Setting | EB106-EP15-BT10 Brine in temp |  |
| 43726 | Number | -32767 | 32767 | Setting | EB106-EP15-BT11 Brine out temp |  |
| 43727 | Number | -32767 | 32767 | Setting | EB106-EP15-BT12 Cond. out |  |
| 43728 | Number | -32767 | 32767 | Setting | EB106-EP15-BT14 Hot gas temp |  |
| 43729 | Number | -32767 | 32767 | Setting | EB106-EP15-BT15 Liquid line |  |
| 43730 | Number | -32767 | 32767 | Setting | EB106-EP15-BT17 Suction |  |
| 43731 | Number | -32767 | 32767 | Setting | EB106-EP15-BT29 Compr. Oil. temp. |  |
| 43732 | Number | -32767 | 32767 | Setting | EB106-EP15-BP8 Pressure transmitter |  |
| 43733 | Number | 0 | 255 | Setting | EB106-EP15 Compressor State |  |
| 43734 | Number | 0 | 255 | Setting | EB106-EP15 Compr. time to start |  |
| 43735 | Number | 0 | 65535 | Setting | EB106-EP15 Relay status |  |
| 43736 | Number | 0 | 255 | Setting | EB106-EP15 Heat med. pump status |  |
| 43737 | Number | 0 | 255 | Setting | EB106-EP15 Brine pump status |  |
| 43738 | Number | 0 | 4294967295 | Setting | EB106-EP15 Compressor starts |  |
| 43740 | Number | 0 | 4294967295 | Setting | EB106-EP15 Tot. op.time compr |  |
| 43742 | Number | 0 | 4294967295 | Setting | EB106-EP15 Tot. HW op.time compr |  |
| 43744 | Number | 0 | 65535 | Setting | EB106-EP15 Alarm number |  |
| 43745 | Number | -32767 | 32767 | Setting | EB106-EP14-BT3 Return temp. |  |
| 43746 | Number | -32767 | 32767 | Setting | EB106-EP14-BT10 Brine in temp |  |
| 43747 | Number | -32767 | 32767 | Setting | EB106-EP14-BT11 Brine out temp |  |
| 43748 | Number | -32767 | 32767 | Setting | EB106-EP14-BT12 Cond. out |  |
| 43749 | Number | -32767 | 32767 | Setting | EB106-EP14-BT14 Hot gas temp |  |
| 43750 | Number | -32767 | 32767 | Setting | EB106-EP14-BT15 Liquid line |  |
| 43751 | Number | -32767 | 32767 | Setting | EB106-EP14-BT17 Suction |  |
| 43752 | Number | -32767 | 32767 | Setting | EB106-EP14-BT29 Compr. Oil. temp. |  |
| 43753 | Number | -32767 | 32767 | Setting | EB106-EP14-BP8 Pressure transmitter |  |
| 43754 | Number | 0 | 255 | Setting | EB106-EP14 Compressor State |  |
| 43755 | Number | 0 | 255 | Setting | EB106-EP14 Compr. time to start |  |
| 43756 | Number | 0 | 65535 | Setting | EB106-EP14 Relay status |  |
| 43757 | Number | 0 | 255 | Setting | EB106-EP14 Heat med. pump status |  |
| 43758 | Number | 0 | 255 | Setting | EB106-EP14 Brine pump status |  |
| 43759 | Number | 0 | 9999999 | Setting | EB106-EP14 Compressor starts |  |
| 43761 | Number | 0 | 9999999 | Setting | EB106-EP14 Tot. op.time compr |  |
| 43763 | Number | 0 | 9999999 | Setting | EB106-EP14 Tot. HW op.time compr |  |
| 43765 | Number | 0 | 65535 | Setting | EB106-EP14 Alarm number |  |
| 43766 | Number | 0 | 65535 | Setting | EB105 Version |  |
| 43784 | Number | 0 | 255 | Setting | EB105 Slave Type |  |
| 43785 | Number | 0 | 255 | Setting | EB105 Compressor Size |  |
| 43786 | Number | -32767 | 32767 | Setting | EB105-EP15-BT3 Return temp. |  |
| 43787 | Number | -32767 | 32767 | Setting | EB105-EP15-BT10 Brine in temp |  |
| 43788 | Number | -32767 | 32767 | Setting | EB105-EP15-BT11 Brine out temp |  |
| 43789 | Number | -32767 | 32767 | Setting | EB105-EP15-BT12 Cond. out |  |
| 43790 | Number | -32767 | 32767 | Setting | EB105-EP15-BT14 Hot gas temp |  |
| 43791 | Number | -32767 | 32767 | Setting | EB105-EP15-BT15 Liquid line |  |
| 43792 | Number | -32767 | 32767 | Setting | EB105-EP15-BT17 Suction |  |
| 43793 | Number | -32767 | 32767 | Setting | EB105-EP15-BT29 Compr. Oil. temp. |  |
| 43794 | Number | -32767 | 32767 | Setting | EB105-EP15-BP8 Pressure transmitter |  |
| 43795 | Number | 0 | 255 | Setting | EB105-EP15 Compressor State |  |
| 43796 | Number | 0 | 255 | Setting | EB105-EP15 Compr. time to start |  |
| 43797 | Number | 0 | 65535 | Setting | EB105-EP15 Relay status |  |
| 43798 | Number | 0 | 255 | Setting | EB105-EP15 Heat med. pump status |  |
| 43799 | Number | 0 | 255 | Setting | EB105-EP15 Brine pump status |  |
| 43800 | Number | 0 | 4294967295 | Setting | EB105-EP15 Compressor starts |  |
| 43802 | Number | 0 | 4294967295 | Setting | EB105-EP15 Tot. op.time compr |  |
| 43804 | Number | 0 | 4294967295 | Setting | EB105-EP15 Tot. HW op.time compr |  |
| 43806 | Number | 0 | 65535 | Setting | EB105-EP15 Alarm number |  |
| 43807 | Number | -32767 | 32767 | Setting | EB105-EP14-BT3 Return temp. |  |
| 43808 | Number | -32767 | 32767 | Setting | EB105-EP14-BT10 Brine in temp |  |
| 43809 | Number | -32767 | 32767 | Setting | EB105-EP14-BT11 Brine out temp |  |
| 43810 | Number | -32767 | 32767 | Setting | EB105-EP14-BT12 Cond. out |  |
| 43811 | Number | -32767 | 32767 | Setting | EB105-EP14-BT14 Hot gas temp |  |
| 43812 | Number | -32767 | 32767 | Setting | EB105-EP14-BT15 Liquid line |  |
| 43813 | Number | -32767 | 32767 | Setting | EB105-EP14-BT17 Suction |  |
| 43814 | Number | -32767 | 32767 | Setting | EB105-EP14-BT29 Compr. Oil. temp. |  |
| 43815 | Number | -32767 | 32767 | Setting | EB105-EP14-BP8 Pressure transmitter |  |
| 43816 | Number | 0 | 255 | Setting | EB105-EP14 Compressor State |  |
| 43817 | Number | 0 | 255 | Setting | EB105-EP14 Compr. time to start |  |
| 43818 | Number | 0 | 65535 | Setting | EB105-EP14 Relay status |  |
| 43819 | Number | 0 | 255 | Setting | EB105-EP14 Heat med. pump status |  |
| 43820 | Number | 0 | 255 | Setting | EB105-EP14 Brine pump status |  |
| 43821 | Number | 0 | 9999999 | Setting | EB105-EP14 Compressor starts |  |
| 43823 | Number | 0 | 9999999 | Setting | EB105-EP14 Tot. op.time compr |  |
| 43825 | Number | 0 | 9999999 | Setting | EB105-EP14 Tot. HW op.time compr |  |
| 43827 | Number | 0 | 65535 | Setting | EB105-EP14 Alarm number |  |
| 43828 | Number | 0 | 65535 | Setting | EB104 Version |  |
| 43846 | Number | 0 | 255 | Setting | EB104 Slave Type |  |
| 43847 | Number | 0 | 255 | Setting | EB104 Compressor Size |  |
| 43848 | Number | -32767 | 32767 | Setting | EB104-EP15-BT3 Return temp. |  |
| 43849 | Number | -32767 | 32767 | Setting | EB104-EP15-BT10 Brine in temp |  |
| 43850 | Number | -32767 | 32767 | Setting | EB104-EP15-BT11 Brine out temp |  |
| 43851 | Number | -32767 | 32767 | Setting | EB104-EP15-BT12 Cond. out |  |
| 43852 | Number | -32767 | 32767 | Setting | EB104-EP15-BT14 Hot gas temp |  |
| 43853 | Number | -32767 | 32767 | Setting | EB104-EP15-BT15 Liquid line |  |
| 43854 | Number | -32767 | 32767 | Setting | EB104-EP15-BT17 Suction |  |
| 43855 | Number | -32767 | 32767 | Setting | EB104-EP15-BT29 Compr. Oil. temp. |  |
| 43856 | Number | -32767 | 32767 | Setting | EB104-EP15-BP8 Pressure transmitter |  |
| 43857 | Number | 0 | 255 | Setting | EB104-EP15 Compressor State |  |
| 43858 | Number | 0 | 255 | Setting | EB104-EP15 Compr. time to start |  |
| 43859 | Number | 0 | 65535 | Setting | EB104-EP15 Relay status |  |
| 43860 | Number | 0 | 255 | Setting | EB104-EP15 Heat med. pump status |  |
| 43861 | Number | 0 | 255 | Setting | EB104-EP15 Brine pump status |  |
| 43862 | Number | 0 | 4294967295 | Setting | EB104-EP15 Compressor starts |  |
| 43864 | Number | 0 | 4294967295 | Setting | EB104-EP15 Tot. op.time compr |  |
| 43866 | Number | 0 | 4294967295 | Setting | EB104-EP15 Tot. HW op.time compr |  |
| 43868 | Number | 0 | 65535 | Setting | EB104-EP15 Alarm number |  |
| 43869 | Number | -32767 | 32767 | Setting | EB104-EP14-BT3 Return temp. |  |
| 43870 | Number | -32767 | 32767 | Setting | EB104-EP14-BT10 Brine in temp |  |
| 43871 | Number | -32767 | 32767 | Setting | EB104-EP14-BT11 Brine out temp |  |
| 43872 | Number | -32767 | 32767 | Setting | EB104-EP14-BT12 Cond. out |  |
| 43873 | Number | -32767 | 32767 | Setting | EB104-EP14-BT14 Hot gas temp |  |
| 43874 | Number | -32767 | 32767 | Setting | EB104-EP14-BT15 Liquid line |  |
| 43875 | Number | -32767 | 32767 | Setting | EB104-EP14-BT17 Suction |  |
| 43876 | Number | -32767 | 32767 | Setting | EB104-EP14-BT29 Compr. Oil. temp. |  |
| 43877 | Number | -32767 | 32767 | Setting | EB104-EP14-BP8 Pressure transmitter |  |
| 43878 | Number | 0 | 255 | Setting | EB104-EP14 Compressor State |  |
| 43879 | Number | 0 | 255 | Setting | EB104-EP14 Compr. time to start |  |
| 43880 | Number | 0 | 65535 | Setting | EB104-EP14 Relay status |  |
| 43881 | Number | 0 | 255 | Setting | EB104-EP14 Heat med. pump status |  |
| 43882 | Number | 0 | 255 | Setting | EB104-EP14 Brine pump status |  |
| 43883 | Number | 0 | 9999999 | Setting | EB104-EP14 Compressor starts |  |
| 43885 | Number | 0 | 9999999 | Setting | EB104-EP14 Tot. op.time compr |  |
| 43887 | Number | 0 | 9999999 | Setting | EB104-EP14 Tot. HW op.time compr |  |
| 43889 | Number | 0 | 65535 | Setting | EB104-EP14 Alarm number |  |
| 43890 | Number | 0 | 65535 | Setting | EB103 Version |  |
| 43908 | Number | 0 | 255 | Setting | EB103 Slave Type |  |
| 43909 | Number | 0 | 255 | Setting | EB103 Compressor Size |  |
| 43910 | Number | -32767 | 32767 | Setting | EB103-EP15-BT3 Return temp. |  |
| 43911 | Number | -32767 | 32767 | Setting | EB103-EP15-BT10 Brine in temp |  |
| 43912 | Number | -32767 | 32767 | Setting | EB103-EP15-BT11 Brine out temp |  |
| 43913 | Number | -32767 | 32767 | Setting | EB103-EP15-BT12 Cond. out |  |
| 43914 | Number | -32767 | 32767 | Setting | EB103-EP15-BT14 Hot gas temp |  |
| 43915 | Number | -32767 | 32767 | Setting | EB103-EP15-BT15 Liquid line |  |
| 43916 | Number | -32767 | 32767 | Setting | EB103-EP15-BT17 Suction |  |
| 43917 | Number | -32767 | 32767 | Setting | EB103-EP15-BT29 Compr. Oil. temp. |  |
| 43918 | Number | -32767 | 32767 | Setting | EB103-EP15-BP8 Pressure transmitter |  |
| 43919 | Number | 0 | 255 | Setting | EB103-EP15 Compressor State |  |
| 43920 | Number | 0 | 255 | Setting | EB103-EP15 Compr. time to start |  |
| 43921 | Number | 0 | 65535 | Setting | EB103-EP15 Relay status |  |
| 43922 | Number | 0 | 255 | Setting | EB103-EP15 Heat med. pump status |  |
| 43923 | Number | 0 | 255 | Setting | EB103-EP15 Brine pump status |  |
| 43924 | Number | 0 | 4294967295 | Setting | EB103-EP15 Compressor starts |  |
| 43926 | Number | 0 | 4294967295 | Setting | EB103-EP15 Tot. op.time compr |  |
| 43928 | Number | 0 | 4294967295 | Setting | EB103-EP15 Tot. HW op.time compr |  |
| 43930 | Number | 0 | 65535 | Setting | EB103-EP15 Alarm number |  |
| 43931 | Number | -32767 | 32767 | Setting | EB103-EP14-BT3 Return temp. |  |
| 43932 | Number | -32767 | 32767 | Setting | EB103-EP14-BT10 Brine in temp |  |
| 43933 | Number | -32767 | 32767 | Setting | EB103-EP14-BT11 Brine out temp |  |
| 43934 | Number | -32767 | 32767 | Setting | EB103-EP14-BT12 Cond. out |  |
| 43935 | Number | -32767 | 32767 | Setting | EB103-EP14-BT14 Hot gas temp |  |
| 43936 | Number | -32767 | 32767 | Setting | EB103-EP14-BT15 Liquid line |  |
| 43937 | Number | -32767 | 32767 | Setting | EB103-EP14-BT17 Suction |  |
| 43938 | Number | -32767 | 32767 | Setting | EB103-EP14-BT29 Compr. Oil. temp. |  |
| 43939 | Number | -32767 | 32767 | Setting | EB103-EP14-BP8 Pressure transmitter |  |
| 43940 | Number | 0 | 255 | Setting | EB103-EP14 Compressor State |  |
| 43941 | Number | 0 | 255 | Setting | EB103-EP14 Compr. time to start |  |
| 43942 | Number | 0 | 65535 | Setting | EB103-EP14 Relay status |  |
| 43943 | Number | 0 | 255 | Setting | EB103-EP14 Heat med. pump status |  |
| 43944 | Number | 0 | 255 | Setting | EB103-EP14 Brine pump status |  |
| 43945 | Number | 0 | 9999999 | Setting | EB103-EP14 Compressor starts |  |
| 43947 | Number | 0 | 9999999 | Setting | EB103-EP14 Tot. op.time compr |  |
| 43949 | Number | 0 | 9999999 | Setting | EB103-EP14 Tot. HW op.time compr |  |
| 43951 | Number | 0 | 65535 | Setting | EB103-EP14 Alarm number |  |
| 43952 | Number | 0 | 65535 | Setting | EB102 Version |  |
| 43970 | Number | 0 | 255 | Setting | EB102 Slave Type |  |
| 43971 | Number | 0 | 255 | Setting | EB102 Compressor Size |  |
| 43972 | Number | -32767 | 32767 | Setting | EB102-EP15-BT3 Return temp. |  |
| 43973 | Number | -32767 | 32767 | Setting | EB102-EP15-BT10 Brine in temp |  |
| 43974 | Number | -32767 | 32767 | Setting | EB102-EP15-BT11 Brine out temp |  |
| 43975 | Number | -32767 | 32767 | Setting | EB102-EP15-BT12 Cond. out |  |
| 43976 | Number | -32767 | 32767 | Setting | EB102-EP15-BT14 Hot gas temp |  |
| 43977 | Number | -32767 | 32767 | Setting | EB102-EP15-BT15 Liquid line |  |
| 43978 | Number | -32767 | 32767 | Setting | EB102-EP15-BT17 Suction |  |
| 43979 | Number | -32767 | 32767 | Setting | EB102-EP15-BT29 Compr. Oil. temp. |  |
| 43980 | Number | -32767 | 32767 | Setting | EB102-EP15-BP8 Pressure transmitter |  |
| 43981 | Number | 0 | 255 | Setting | EB102-EP15 Compressor State |  |
| 43982 | Number | 0 | 255 | Setting | EB102-EP15 Compr. time to start |  |
| 43983 | Number | 0 | 65535 | Setting | EB102-EP15 Relay status |  |
| 43984 | Number | 0 | 255 | Setting | EB102-EP15 Heat med. pump status |  |
| 43985 | Number | 0 | 255 | Setting | EB102-EP15 Brine pump status |  |
| 43986 | Number | 0 | 4294967295 | Setting | EB102-EP15 Compressor starts |  |
| 43988 | Number | 0 | 4294967295 | Setting | EB102-EP15 Tot. op.time compr |  |
| 43990 | Number | 0 | 4294967295 | Setting | EB102-EP15 Tot. HW op.time compr |  |
| 43992 | Number | 0 | 65535 | Setting | EB102-EP15 Alarm number |  |
| 43993 | Number | -32767 | 32767 | Setting | EB102-EP14-BT3 Return temp. |  |
| 43994 | Number | -32767 | 32767 | Setting | EB102-EP14-BT10 Brine in temp |  |
| 43995 | Number | -32767 | 32767 | Setting | EB102-EP14-BT11 Brine out temp |  |
| 43996 | Number | -32767 | 32767 | Setting | EB102-EP14-BT12 Cond. out |  |
| 43997 | Number | -32767 | 32767 | Setting | EB102-EP14-BT14 Hot gas temp |  |
| 43998 | Number | -32767 | 32767 | Setting | EB102-EP14-BT15 Liquid line |  |
| 43999 | Number | -32767 | 32767 | Setting | EB102-EP14-BT17 Suction |  |
| 44000 | Number | -32767 | 32767 | Setting | EB102-EP14-BT29 Compr. Oil. temp. |  |
| 44001 | Number | -32767 | 32767 | Setting | EB102-EP14-BP8 Pressure transmitter |  |
| 44002 | Number | 0 | 255 | Setting | EB102-EP14 Compressor State |  |
| 44003 | Number | 0 | 255 | Setting | EB102-EP14 Compr. time to start |  |
| 44004 | Number | 0 | 65535 | Setting | EB102-EP14 Relay status |  |
| 44005 | Number | 0 | 255 | Setting | EB102-EP14 Heat med. pump status |  |
| 44006 | Number | 0 | 255 | Setting | EB102-EP14 Brine pump status |  |
| 44007 | Number | 0 | 9999999 | Setting | EB102-EP14 Compressor starts |  |
| 44009 | Number | 0 | 9999999 | Setting | EB102-EP14 Tot. op.time compr |  |
| 44011 | Number | 0 | 9999999 | Setting | EB102-EP14 Tot. HW op.time compr |  |
| 44013 | Number | 0 | 65535 | Setting | EB102-EP14 Alarm number |  |
| 44014 | Number | 0 | 65535 | Setting | EB101 Version |  |
| 44032 | Number | 0 | 255 | Setting | EB101 Slave Type |  |
| 44033 | Number | 0 | 255 | Setting | EB101 Compressor Size |  |
| 44034 | Number | -32767 | 32767 | Setting | EB101-EP15-BT3 Return temp. |  |
| 44035 | Number | -32767 | 32767 | Setting | EB101-EP15-BT10 Brine in temp |  |
| 44036 | Number | -32767 | 32767 | Setting | EB101-EP15-BT11 Brine out temp |  |
| 44037 | Number | -32767 | 32767 | Setting | EB101-EP15-BT12 Cond. out |  |
| 44038 | Number | -32767 | 32767 | Setting | EB101-EP15-BT14 Hot gas temp |  |
| 44039 | Number | -32767 | 32767 | Setting | EB101-EP15-BT15 Liquid line |  |
| 44040 | Number | -32767 | 32767 | Setting | EB101-EP15-BT17 Suction |  |
| 44041 | Number | -32767 | 32767 | Setting | EB101-EP15-BT29 Compr. Oil. temp. |  |
| 44042 | Number | -32767 | 32767 | Setting | EB101-EP15-BP8 Pressure transmitter |  |
| 44043 | Number | 0 | 255 | Setting | EB101-EP15 Compressor State |  |
| 44044 | Number | 0 | 255 | Setting | EB101-EP15 Compr. time to start |  |
| 44045 | Number | 0 | 65535 | Setting | EB101-EP15 Relay status |  |
| 44046 | Number | 0 | 255 | Setting | EB101-EP15 Heat med. pump status |  |
| 44047 | Number | 0 | 255 | Setting | EB101-EP15 Brine pump status |  |
| 44048 | Number | 0 | 4294967295 | Setting | EB101-EP15 Compressor starts |  |
| 44050 | Number | 0 | 4294967295 | Setting | EB101-EP15 Tot. op.time compr |  |
| 44052 | Number | 0 | 4294967295 | Setting | EB101-EP15 Tot. HW op.time compr |  |
| 44054 | Number | 0 | 65535 | Setting | EB101-EP15 Alarm number |  |
| 44055 | Number | -32767 | 32767 | Setting | EB101-EP14-BT3 Return temp. |  |
| 44056 | Number | -32767 | 32767 | Setting | EB101-EP14-BT10 Brine in temp |  |
| 44057 | Number | -32767 | 32767 | Setting | EB101-EP14-BT11 Brine out temp |  |
| 44058 | Number | -32767 | 32767 | Setting | EB101-EP14-BT12 Cond. out |  |
| 44059 | Number | -32767 | 32767 | Setting | EB101-EP14-BT14 Hot gas temp |  |
| 44060 | Number | -32767 | 32767 | Setting | EB101-EP14-BT15 Liquid line |  |
| 44061 | Number | -32767 | 32767 | Setting | EB101-EP14-BT17 Suction |  |
| 44062 | Number | -32767 | 32767 | Setting | EB101-EP14-BT29 Compr. Oil. temp. |  |
| 44063 | Number | -32767 | 32767 | Setting | EB101-EP14-BP8 Pressure transmitter |  |
| 44064 | Number | 0 | 255 | Setting | EB101-EP14 Compressor State |  |
| 44065 | Number | 0 | 255 | Setting | EB101-EP14 Compr. time to start |  |
| 44066 | Number | 0 | 65535 | Setting | EB101-EP14 Relay status |  |
| 44067 | Number | 0 | 255 | Setting | EB101-EP14 Heat med. pump status |  |
| 44068 | Number | 0 | 255 | Setting | EB101-EP14 Brine pump status |  |
| 44069 | Number | 0 | 9999999 | Setting | EB101-EP14 Compressor starts |  |
| 44071 | Number | 0 | 9999999 | Setting | EB101-EP14 Tot. op.time compr |  |
| 44073 | Number | 0 | 9999999 | Setting | EB101-EP14 Tot. HW op.time compr |  |
| 44075 | Number | 0 | 65535 | Setting | EB101-EP14 Alarm number |  |
| 44138 | String | 0 | 255 | Setting | EB108-EP15 Prio | 0=Off, 1=Heat, 2=Hot water, 3=Pool 1, 4=Pool2 |
| 44139 | String | 0 | 255 | Setting | EB108-EP14 Prio | 0=Off, 1=Heat, 2=Hot water, 3=Pool 1, 4=Pool2 |
| 44151 | String | 0 | 255 | Setting | EB107-EP15 Prio | 0=Off, 1=Heat, 2=Hot water, 3=Pool 1, 4=Pool2 |
| 44152 | String | 0 | 255 | Setting | EB107-EP14 Prio | 0=Off, 1=Heat, 2=Hot water, 3=Pool 1, 4=Pool2 |
| 44164 | String | 0 | 255 | Setting | EB106-EP15 Prio | 0=Off, 1=Heat, 2=Hot water, 3=Pool 1, 4=Pool2 |
| 44165 | String | 0 | 255 | Setting | EB106-EP14 Prio | 0=Off, 1=Heat, 2=Hot water, 3=Pool 1, 4=Pool2 |
| 44177 | String | 0 | 255 | Setting | EB105-EP15 Prio | 0=Off, 1=Heat, 2=Hot water, 3=Pool 1, 4=Pool2 |
| 44178 | String | 0 | 255 | Setting | EB105-EP14 Prio | 0=Off, 1=Heat, 2=Hot water, 3=Pool 1, 4=Pool2 |
| 44190 | String | 0 | 255 | Setting | EB104-EP15 Prio | 0=Off, 1=Heat, 2=Hot water, 3=Pool 1, 4=Pool2 |
| 44191 | String | 0 | 255 | Setting | EB104-EP14 Prio | 0=Off, 1=Heat, 2=Hot water, 3=Pool 1, 4=Pool2 |
| 44203 | String | 0 | 255 | Setting | EB103-EP15 Prio | 0=Off, 1=Heat, 2=Hot water, 3=Pool 1, 4=Pool2 |
| 44204 | String | 0 | 255 | Setting | EB103-EP14 Prio | 0=Off, 1=Heat, 2=Hot water, 3=Pool 1, 4=Pool2 |
| 44216 | String | 0 | 255 | Setting | EB102-EP15 Prio | 0=Off, 1=Heat, 2=Hot water, 3=Pool 1, 4=Pool2 |
| 44217 | String | 0 | 255 | Setting | EB102-EP14 Prio | 0=Off, 1=Heat, 2=Hot water, 3=Pool 1, 4=Pool2 |
| 44229 | String | 0 | 255 | Setting | EB101-EP15 Prio | 0=Off, 1=Heat, 2=Hot water, 3=Pool 1, 4=Pool2 |
| 44230 | String | 0 | 255 | Setting | EB101-EP14 Prio | 0=Off, 1=Heat, 2=Hot water, 3=Pool 1, 4=Pool2 |
| 44242 | String | 0 | 255 | Setting | EB100-EP15 Prio | 0=Off, 1=Heat, 2=Hot water, 3=Pool 1, 4=Pool2 |
| 44243 | String | 0 | 255 | Setting | EB100-EP14 Prio | 0=Off, 1=Heat, 2=Hot water, 3=Pool 1, 4=Pool2 |
| 44266 | Number | -30000 | 30000 | Setting | Cool Degree Minutes |  |
| 44267 | Number | -32767 | 32767 | Setting | Calc. Cooling Supply Temperature S4 |  |
| 44268 | Number | -32767 | 32767 | Setting | Calc. Cooling Supply Temperature S3 |  |
| 44269 | Number | -32767 | 32767 | Setting | Calc. Cooling Supply Temperature S2 |  |
| 44270 | Number | -32767 | 32767 | Setting | Calc. Cooling Supply Temperature S1 |  |
| 44276 | Number | 0 | 255 | Setting | State ACS |  |
| 44277 | Number | 0 | 255 | Setting | State ACS heatdump |  |
| 44278 | Number | 0 | 255 | Setting | State ACS cooldump |  |
| 44282 | Number | 0 | 255 | Setting | Used cprs. HW |  |
| 44283 | Number | 0 | 255 | Setting | Used cprs. heat |  |
| 44284 | Number | 0 | 255 | Setting | Used cprs. pool 1 |  |
| 44285 | Number | 0 | 255 | Setting | Used cprs. pool 2 |  |
| 44298 | Number | 0 | 9999999 | Setting | Accumulated Energy HW Cpr and Add |  |
| 44300 | Number | 0 | 9999999 | Setting | Accumulated Energy Heat Cpr and Add |  |
| 44302 | Number | 0 | 9999999 | Setting | Accumulated Energy Cooling Cpr |  |
| 44304 | Number | 0 | 9999999 | Setting | Accumulated Energy Pool Cpr |  |
| 44306 | Number | 0 | 9999999 | Setting | Accumulated Energy HW Cpr |  |
| 44308 | Number | 0 | 9999999 | Setting | Accumulated Energy Heat Cpr |  |
| 44320 | Number | 0 | 255 | Setting | Used cprs. cool |  |
| 44331 | Number | 0 | 255 | Setting | Software release |  |
| 44380 | Number | 0 | 255 | Setting | External Compressors |  |
| 44410 | Number | 0 | 255 | Setting | EB108 Own Hot Water |  |
| 44411 | Number | -32767 | 32767 | Setting | EB108-BT6 Hot water load temp. |  |
| 44412 | Number | -32767 | 32767 | Setting | EB108-BT7 Hot water top temp. |  |
| 44413 | Number | -32767 | 32767 | Setting | EB108-BT2 Supply temp. |  |
| 44416 | Number | 0 | 255 | Setting | EB107 Own Hot Water |  |
| 44417 | Number | -32767 | 32767 | Setting | EB107-BT6 Hot water load temp. |  |
| 44418 | Number | -32767 | 32767 | Setting | EB107-BT7 Hot water top temp. |  |
| 44419 | Number | -32767 | 32767 | Setting | EB107-BT2 Supply temp. |  |
| 44422 | Number | 0 | 255 | Setting | EB106 Own Hot Water |  |
| 44423 | Number | -32767 | 32767 | Setting | EB106-BT6 Hot water load temp. |  |
| 44424 | Number | -32767 | 32767 | Setting | EB106-BT7 Hot water top temp. |  |
| 44425 | Number | -32767 | 32767 | Setting | EB106-BT2 Supply temp. |  |
| 44428 | Number | 0 | 255 | Setting | EB105 Own Hot Water |  |
| 44429 | Number | -32767 | 32767 | Setting | EB105-BT6 Hot water load temp. |  |
| 44430 | Number | -32767 | 32767 | Setting | EB105-BT7 Hot water top temp. |  |
| 44431 | Number | -32767 | 32767 | Setting | EB105-BT2 Supply temp. |  |
| 44434 | Number | 0 | 255 | Setting | EB104 Own Hot Water |  |
| 44435 | Number | -32767 | 32767 | Setting | EB104-BT6 Hot water load temp. |  |
| 44436 | Number | -32767 | 32767 | Setting | EB104-BT7 Hot water top temp. |  |
| 44437 | Number | -32767 | 32767 | Setting | EB104-BT2 Supply temp. |  |
| 44440 | Number | 0 | 255 | Setting | EB103 Own Hot Water |  |
| 44441 | Number | -32767 | 32767 | Setting | EB103-BT6 Hot water load temp. |  |
| 44442 | Number | -32767 | 32767 | Setting | EB103-BT7 Hot water top temp. |  |
| 44443 | Number | -32767 | 32767 | Setting | EB103-BT2 Supply temp. |  |
| 44446 | Number | 0 | 255 | Setting | EB102 Own Hot Water |  |
| 44447 | Number | -32767 | 32767 | Setting | EB102-BT6 Hot water load temp. |  |
| 44448 | Number | -32767 | 32767 | Setting | EB102-BT7 Hot water top temp. |  |
| 44449 | Number | -32767 | 32767 | Setting | EB102-BT2 Supply temp. |  |
| 44452 | Number | 0 | 255 | Setting | EB101 Own Hot Water |  |
| 44453 | Number | -32767 | 32767 | Setting | EB101-BT6 Hot water load temp. |  |
| 44454 | Number | -32767 | 32767 | Setting | EB101-BT7 Hot water top temp. |  |
| 44455 | Number | -32767 | 32767 | Setting | EB101-BT2 Supply temp. |  |
| 44487 | Number | 0 | 255 | Setting | Cool Compressors |  |
| 44744 | Number | 0 | 255 | Setting | Extra heating system pump S4 |  |
| 44745 | Number | 0 | 255 | Setting | Extra heating system pump S3 |  |
| 44746 | Number | 0 | 255 | Setting | Extra heating system pump S2 |  |
| 44748 | Number | 0 | 255 | Setting | Pool 2 pump |  |
| 44749 | Number | 0 | 255 | Setting | Pool 1 pump |  |
| 44753 | Number | 0 | 255 | Setting | Passiv cool shunt |  |
| 44754 | Number | 0 | 255 | Setting | Passiv cool pool |  |
| 44756 | Number | 0 | 255 | Setting | State ground water pump |  |
| 44874 | Number | 0 | 255 | Setting | State SG Ready |  |
| 44878 | Number | 0 | 255 | Setting | SG Ready input A |  |
| 44879 | Number | 0 | 255 | Setting | SG Ready input B |  |
| 44910 | Number | -32767 | 32767 | Setting | Brine pump  dT act. |  |
| 44911 | Number | -32767 | 32767 | Setting | Brine pump  dT act. |  |
| 44912 | Switch | 0 | 1 | Sensor | Brine pump auto controlled |  |
| 45001 | Number | -32767 | 32767 | Sensor | Alarm Number |  |
| 47291 | Number | 0 | 10000 | Sensor | Floor drying timer |  |
| 47325 | Number | 0 | 7 | Sensor | Step controlled add. max. step |  |
| 47004 | Number | 0 | 15 | Sensor | Heat curve S4 |  |
| 47005 | Number | 0 | 15 | Sensor | Heat curve S3 |  |
| 47006 | Number | 0 | 15 | Sensor | Heat curve S2 |  |
| 47007 | Number | 0 | 15 | Sensor | Heat curve S1 |  |
| 47008 | Number | -10 | 10 | Sensor | Offset S4 |  |
| 47009 | Number | -10 | 10 | Sensor | Offset S3 |  |
| 47010 | Number | -10 | 10 | Sensor | Offset S2 |  |
| 47011 | Number | -10 | 10 | Sensor | Offset S1 |  |
| 47012 | Number | 50 | 700 | Sensor | Min Supply System 4 |  |
| 47013 | Number | 50 | 700 | Sensor | Min Supply System 3 |  |
| 47014 | Number | 50 | 700 | Sensor | Min Supply System 2 |  |
| 47015 | Number | 50 | 700 | Sensor | Min Supply System 1 |  |
| 47016 | Number | 50 | 700 | Sensor | Max Supply System 4 |  |
| 47017 | Number | 50 | 700 | Sensor | Max Supply System 3 |  |
| 47018 | Number | 50 | 700 | Sensor | Max Supply System 2 |  |
| 47019 | Number | 50 | 700 | Sensor | Max Supply System 1 |  |
| 47020 | Number | 0 | 80 | Sensor | Own Curve P7 |  |
| 47021 | Number | 0 | 80 | Sensor | Own Curve P6 |  |
| 47022 | Number | 0 | 80 | Sensor | Own Curve P5 |  |
| 47023 | Number | 0 | 80 | Sensor | Own Curve P4 |  |
| 47024 | Number | 0 | 80 | Sensor | Own Curve P3 |  |
| 47025 | Number | 0 | 80 | Sensor | Own Curve P2 |  |
| 47026 | Number | 0 | 80 | Sensor | Own Curve P1 |  |
| 47027 | Number | -40 | 30 | Sensor | Point offset outdoor temp. |  |
| 47028 | Number | -10 | 10 | Sensor | Point offset |  |
| 47029 | Number | -10 | 10 | Sensor | External adjustment S4 |  |
| 47030 | Number | -10 | 10 | Sensor | External adjustment S3 |  |
| 47031 | Number | -10 | 10 | Sensor | External adjustment S2 |  |
| 47032 | Number | -10 | 10 | Sensor | External adjustment S1 |  |
| 47033 | Number | 50 | 300 | Sensor | External adjustment with room sensor S4 |  |
| 47034 | Number | 50 | 300 | Sensor | External adjustment with room sensor S3 |  |
| 47035 | Number | 50 | 300 | Sensor | External adjustment with room sensor S2 |  |
| 47036 | Number | 50 | 300 | Sensor | External adjustment with room sensor S1 |  |
| 47041 | String | 0 | 2 | Sensor | Hot water mode | 0=Economy, 1=Normal, 2=Luxury |
| 47043 | Number | 50 | 700 | Sensor | Start temperature HW Luxury |  |
| 47044 | Number | 50 | 700 | Sensor | Start temperature HW Normal |  |
| 47045 | Number | 50 | 700 | Sensor | Start temperature HW Economy |  |
| 47046 | Number | 550 | 700 | Sensor | Stop temperature Periodic HW |  |
| 47047 | Number | 50 | 700 | Sensor | Stop temperature HW Luxury |  |
| 47048 | Number | 50 | 700 | Sensor | Stop temperature HW Normal |  |
| 47049 | Number | 50 | 700 | Sensor | Stop temperature HW Economy |  |
| 47050 | Switch | 0 | 1 | Setting | Periodic HW |  |
| 47051 | Number | 1 | 90 | Setting | Periodic HW Interval |  |
| 47054 | Number | 1 | 60 | Setting | Run time HWC |  |
| 47055 | Number | 0 | 60 | Setting | Still time HWC |  |
| 47131 | String | 0 | 21 | Setting | Language | 0=English, 1=Svenska, 2=Deutsch, 3=Francais, 4=Espanol, 5=Suomi, 6=Lietuviu, 7=Cesky, 8=Polski, 9=Nederlands, 10=Norsk, 11=Dansk, 12=Eesti, 13=Latviesu, 16=Magyar |
| 47134 | Number | 0 | 180 | Setting | Period HW |  |
| 47135 | Number | 0 | 180 | Setting | Period Heat |  |
| 47136 | Number | 0 | 180 | Setting | Period Pool |  |
| 47138 | String | 10 | 40 | Setting | Operational mode heat medium pump | 10=Intermittent, 20=Continous, 30=Economy, 40=Auto |
| 47139 | String | 10 | 30 | Setting | Operational mode brine medium pump | 10=Intermittent, 20=Continuous, 30=Economy, 40=Auto |
| 47206 | Number | -1000 | -30 | Setting | DM start heating |  |
| 47207 | Number | -32767 | 32767 | Setting | DM start cooling |  |
| 47208 | Number | -32767 | 32767 | Setting | DM start add. |  |
| 47209 | Number | -32767 | 32767 | Setting | DM between add. steps |  |
| 47210 | Number | -2000 | -30 | Setting | DM start add. with shunt |  |
| 47212 | Number | 0 | 4500 | Setting | Max int add. power |  |
| 47214 | Number | 1 | 200 | Setting | Fuse |  |
| 47261 | Number | 0 | 100 | Setting | Exhaust Fan speed 4 |  |
| 47262 | Number | 0 | 100 | Setting | Exhaust Fan speed 3 |  |
| 47263 | Number | 0 | 100 | Setting | Exhaust Fan speed 2 |  |
| 47264 | Number | 0 | 100 | Setting | Exhaust Fan speed 1 |  |
| 47265 | Number | 0 | 100 | Setting | Exhaust Fan speed normal |  |
| 47271 | Number | 1 | 99 | Setting | Fan return time 4 |  |
| 47272 | Number | 1 | 99 | Setting | Fan return time 3 |  |
| 47273 | Number | 1 | 99 | Setting | Fan return time 2 |  |
| 47274 | Number | 1 | 99 | Setting | Fan return time 1 |  |
| 47275 | Number | 1 | 24 | Setting | Filter Reminder period |  |
| 47276 | Switch | 0 | 1 | Setting | Floor drying |  |
| 47277 | Number | 0 | 30 | Setting | Floor drying period 7 |  |
| 47278 | Number | 0 | 30 | Setting | Floor drying period 6 |  |
| 47279 | Number | 0 | 30 | Setting | Floor drying period 5 |  |
| 47280 | Number | 0 | 30 | Setting | Floor drying period 4 |  |
| 47281 | Number | 0 | 30 | Setting | Floor drying period 3 |  |
| 47282 | Number | 0 | 30 | Setting | Floor drying period 2 |  |
| 47283 | Number | 0 | 30 | Setting | Floor drying period 1 |  |
| 47284 | Number | 15 | 70 | Setting | Floor drying temp. 7 |  |
| 47285 | Number | 15 | 70 | Setting | Floor drying temp. 6 |  |
| 47286 | Number | 15 | 70 | Setting | Floor drying temp. 5 |  |
| 47287 | Number | 15 | 70 | Setting | Floor drying temp. 4 |  |
| 47288 | Number | 15 | 70 | Setting | Floor drying temp. 3 |  |
| 47289 | Number | 15 | 70 | Setting | Floor drying temp. 2 |  |
| 47290 | Number | 15 | 70 | Setting | Floor drying temp. 1 |  |
| 47302 | Switch | 0 | 1 | Setting | Climate system 2 accessory |  |
| 47303 | Switch | 0 | 1 | Setting | Climate system 3 accessory |  |
| 47304 | Switch | 0 | 1 | Setting | Climate system 4 accessory |  |
| 47305 | Number | 1 | 100 | Setting | Climate system 4 mixing valve amp. |  |
| 47306 | Number | 1 | 100 | Setting | Climate system 3 mixing valve amp. |  |
| 47307 | Number | 1 | 100 | Setting | Climate system 2 mixing valve amp. |  |
| 47308 | Number | 10 | 300 | Setting | Climate system 4 shunt wait |  |
| 47309 | Number | 10 | 300 | Setting | Climate system 3 shunt wait |  |
| 47310 | Number | 10 | 300 | Setting | Climate system 2 shunt wait |  |
| 47312 | Switch | 0 | 1 | Setting | FLM pump |  |
| 47313 | Number | 1 | 30 | Setting | FLM defrost |  |
| 47317 | Switch | 0 | 1 | Setting | Shunt controlled add. accessory |  |
| 47318 | Number | 5 | 90 | Setting | Shunt controlled add. min. temp. |  |
| 47319 | Number | 0 | 48 | Setting | Shunt controlled add. min. runtime |  |
| 47320 | Number | 1 | 100 | Setting | Shunt controlled add. mixing valve amp. |  |
| 47321 | Number | 10 | 300 | Setting | Shunt controlled add. mixing valve wait |  |
| 47322 | Switch | 0 | 1 | Setting | Step controlled add. accessory |  |
| 47323 | Number | -2000 | -30 | Setting | Step controlled add. start DM |  |
| 47324 | Number | 0 | 1000 | Setting | Step controlled add. diff. DM |  |
| 47326 | Switch | 0 | 1 | Setting | Step controlled add. mode |  |
| 47327 | Switch | 0 | 1 | Setting | Ground water pump accessory |  |
| 47329 | Switch | 0 | 1 | Setting | Cooling 2-pipe accessory |  |
| 47330 | Switch | 0 | 1 | Setting | Cooling 4-pipe accessory |  |
| 47335 | Number | 0 | 48 | Setting | Time betw. switch heat/cool |  |
| 47336 | Number | 5 | 100 | Setting | Heat at room under temp. |  |
| 47337 | Number | 5 | 100 | Setting | Cool at room over temp. |  |
| 47338 | Number | 1 | 100 | Setting | Cooling mix. valve amp. |  |
| 47339 | Number | 10 | 300 | Setting | Cooling mix. valve step delay |  |
| 47340 | Switch | 0 | 1 | Setting | Cooling with room sensor |  |
| 47341 | Switch | 0 | 1 | Setting | HPAC accessory |  |
| 47342 | Number | 10 | 500 | Setting | Start Passive Cooling DM |  |
| 47343 | Number | 30 | 300 | Setting | Start Active Cooling DM |  |
| 47352 | Switch | 0 | 1 | Setting | SMS40 accessory |  |
| 47365 | Switch | 0 | 1 | Setting | RMU System 1 |  |
| 47366 | Switch | 0 | 1 | Setting | RMU System 2 |  |
| 47367 | Switch | 0 | 1 | Setting | RMU System 3 |  |
| 47368 | Switch | 0 | 1 | Setting | RMU System 4 |  |
| 47370 | Switch | 0 | 1 | Setting | Allow Additive Heating |  |
| 47371 | Switch | 0 | 1 | Setting | Allow Heating |  |
| 47372 | Switch | 0 | 1 | Setting | Allow Cooling |  |
| 47378 | Number | 10 | 250 | Setting | Max diff. comp. |  |
| 47379 | Number | 10 | 240 | Setting | Max diff. add. |  |
| 47380 | Switch | 0 | 1 | Setting | Low brine out autoreset |  |
| 47381 | Number | -120 | 150 | Setting | Low brine out temp. |  |
| 47382 | Switch | 0 | 1 | Setting | High brine in |  |
| 47383 | Number | 100 | 300 | Setting | High brine in temp. |  |
| 47384 | String | 1 | 2 | Setting | Date format | 1=DD-MM-YY, 2=YY-MM-DD |
| 47385 | String | 12 | 24 | Setting | Time format | 12=hours 24, 24=Hours |
| 47387 | Switch | 0 | 1 | Setting | HW production | 12=hours 24, 24=Hours |
| 47388 | Switch | 0 | 1 | Setting | Alarm lower room temp. | 12=hours 24, 24=Hours |
| 47389 | Switch | 0 | 1 | Setting | Alarm lower HW temp. | 12=hours 24, 24=Hours |
| 47391 | Switch | 0 | 1 | Setting | Use room sensor S4 | 12=hours 24, 24=Hours |
| 47392 | Switch | 0 | 1 | Setting | Use room sensor S3 | 12=hours 24, 24=Hours |
| 47393 | Switch | 0 | 1 | Setting | Use room sensor S2 | 12=hours 24, 24=Hours |
| 47394 | Switch | 0 | 1 | Setting | Use room sensor S1 | 12=hours 24, 24=Hours |
| 47395 | Number | 50 | 300 | Setting | Room sensor setpoint S4 |  |
| 47396 | Number | 50 | 300 | Setting | Room sensor setpoint S3 |  |
| 47397 | Number | 50 | 300 | Setting | Room sensor setpoint S2 |  |
| 47398 | Number | 50 | 300 | Setting | Room sensor setpoint S1 |  |
| 47399 | Number | 0 | 60 | Setting | Room sensor factor S4 |  |
| 47400 | Number | 0 | 60 | Setting | Room sensor factor S3 |  |
| 47401 | Number | 0 | 60 | Setting | Room sensor factor S2 |  |
| 47402 | Number | 0 | 60 | Setting | Room sensor factor S1 |  |
| 47413 | Number | 1 | 100 | Setting | Speed circ.pump HW |  |
| 47414 | Number | 1 | 100 | Setting | Speed circ.pump Heat |  |
| 47415 | Number | 70 | 70 | Setting | Speed circ.pump Pool |  |
| 47416 | Number | 0 | 255 | Setting | Speed circ.pump Economy |  |
| 47417 | Number | 1 | 100 | Setting | Speed circ.pump Cooling |  |
| 47418 | Number | 1 | 100 | Setting | Speed brine pump |  |
| 47537 | Switch | 0 | 1 | Setting | Night cooling |  |
| 47538 | Number | 20 | 30 | Setting | Start room temp. night cooling |  |
| 47539 | Number | 3 | 10 | Setting | Night Cooling Min. diff. |  |
| 47540 | Number | 10 | 2000 | Setting | Heat DM diff |  |
| 47543 | Number | 10 | 150 | Setting | Cooling DM diff |  |
| 47570 | String | 0 | 255 | Setting | Operational mode | 0=Auto, 1=Manual, 2=Add. heat only |
| 48053 | Number | 0 | 100 | Setting | FLM 2 speed 4 |  |
| 48054 | Number | 0 | 100 | Setting | FLM 2 speed 3 |  |
| 48055 | Number | 0 | 100 | Setting | FLM 2 speed 2 |  |
| 48056 | Number | 0 | 100 | Setting | FLM 2 speed 1 |  |
| 48057 | Number | 0 | 100 | Setting | FLM 2 speed normal |  |
| 48058 | Number | 0 | 100 | Setting | FLM 3 speed 4 |  |
| 48059 | Number | 0 | 100 | Setting | FLM 3 speed 3 |  |
| 48060 | Number | 0 | 100 | Setting | FLM 3 speed 2 |  |
| 48061 | Number | 0 | 100 | Setting | FLM 3 speed 1 |  |
| 48062 | Number | 0 | 100 | Setting | FLM 3 speed normal |  |
| 48063 | Number | 0 | 100 | Setting | FLM 4 speed 4 |  |
| 48064 | Number | 0 | 100 | Setting | FLM 4 speed 3 |  |
| 48065 | Number | 0 | 100 | Setting | FLM 4 speed 2 |  |
| 48066 | Number | 0 | 100 | Setting | FLM 4 speed 1 |  |
| 48067 | Number | 0 | 100 | Setting | FLM 4 speed normal |  |
| 48068 | Switch | 0 | 1 | Setting | FLM 4 accessory |  |
| 48069 | Switch | 0 | 1 | Setting | FLM 3 accessory |  |
| 48070 | Switch | 0 | 1 | Setting | FLM 2 accessory |  |
| 48071 | Switch | 0 | 1 | Setting | FLM 1 accessory |  |
| 48072 | Number | -32767 | 32767 | Setting | DM diff start add. |  |
| 48073 | Switch | 0 | 1 | Setting | FLM cooling |  |
| 48074 | Number | 50 | 400 | Setting | Set point for BT74 |  |
| 48087 | Switch | 0 | 1 | Setting | Pool 2 accessory |  |
| 48088 | Switch | 0 | 1 | Setting | Pool 1 accessory |  |
| 48089 | Number | 50 | 800 | Setting | Pool 2 start temp. |  |
| 48090 | Number | 50 | 800 | Setting | Pool 1 start temp. |  |
| 48091 | Number | 50 | 800 | Setting | Pool 2 stop temp. |  |
| 48092 | Number | 50 | 800 | Setting | Pool 1 stop temp. |  |
| 48093 | Switch | 0 | 1 | Setting | Pool 2 Activated |  |
| 48094 | Switch | 0 | 1 | Setting | Pool 1 Activated |  |
| 48133 | Number | 0 | 180 | Setting | Period Pool 2 |  |
| 48174 | Number | 5 | 30 | Setting | Min cooling supply temp S4 |  |
| 48175 | Number | 5 | 30 | Setting | Min cooling supply temp S3 |  |
| 48176 | Number | 5 | 30 | Setting | Min cooling supply temp S2 |  |
| 48177 | Number | 5 | 30 | Setting | Min cooling supply temp S1 |  |
| 48178 | Number | 5 | 30 | Setting | Cooling supply temp. at 20°C |  |
| 48179 | Number | 5 | 30 | Setting | Cooling supply temp. at 20°C |  |
| 48180 | Number | 5 | 30 | Setting | Cooling supply temp. at 20°C |  |
| 48181 | Number | 5 | 30 | Setting | Cooling supply temp. at 20°C |  |
| 48182 | Number | 5 | 30 | Setting | Cooling supply temp. at 40°C |  |
| 48183 | Number | 5 | 30 | Setting | Cooling supply temp. at 40°C |  |
| 48184 | Number | 5 | 30 | Setting | Cooling supply temp. at 40°C |  |
| 48185 | Number | 5 | 30 | Setting | Cooling supply temp. at 40°C |  |
| 48186 | Switch | 0 | 1 | Setting | Cooling use mix. valves |  |
| 48187 | Switch | 0 | 1 | Setting | Cooling use mix. valves |  |
| 48188 | Switch | 0 | 1 | Setting | Cooling use mix. valves |  |
| 48189 | Number | 0 | 255 | Setting | Cooling use mix. valves |  |
| 48190 | Number | 10 | 500 | Setting | Heatdump mix. valve delay |  |
| 48191 | Number | 1 | 100 | Setting | Heatdump mix. valve amp. |  |
| 48192 | Number | 10 | 500 | Setting | Cooldump mix. valve delay |  |
| 48193 | Number | 1 | 100 | Setting | Cooldump mix. valve amp. |  |
| 48194 | Switch | 0 | 1 | Setting | ACS accessory |  |
| 48195 | Switch | 0 | 1 | Setting | ACS heat dump 24h-function |  |
| 48196 | Switch | 0 | 1 | Setting | ACS run brinepump in wait mode |  |
| 48197 | Number | 0 | 100 | Setting | ACS closingtime for cool dump |  |
| 48226 | Number | 80 | 100 | Setting | Max charge pump reg speed |  |
| 48282 | Switch | 0 | 1 | Setting | SG Ready heating |  |
| 48283 | Switch | 0 | 1 | Setting | SG Ready cooling |  |
| 48284 | Switch | 0 | 1 | Setting | SG Ready hot water |  |
| 48285 | Switch | 0 | 1 | Setting | SG Ready pool |  |
| 48452 | Switch | 0 | 1 | Setting | Auto heat medium pump speed, hw |  |
| 48453 | Switch | 0 | 1 | Setting | Auto heat medium pump speed, heat |  |
| 48454 | Switch | 0 | 1 | Setting | Auto heat medium pump speed, pool |  |
| 48455 | Switch | 0 | 1 | Setting | Auto heat medium pump speed, cool |  |
| 48456 | Number | 10 | 20 | Setting | Operational mode heat medium pump, cooling |  |
| 48458 | Number | 50 | 100 | Setting | Max speed circ.pump Heat |  |
| 48459 | Number | 0 | 100 | Setting | Speed brine pump cooling |  |
| 48487 | Number | 1 | 100 | Setting | Speed circ.pump Cooling |  |


