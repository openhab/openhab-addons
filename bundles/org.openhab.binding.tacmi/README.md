# TA C.M.I. Binding

This binding makes use of the CAN over Ethernet feature of the C.M.I. from Technische Alternative. Since I only have the new UVR16x2, it has only been tested with this controller.

The binding currently supports the following functions:

* Receive data from analog outputs defined in TAPPS2
* Receive data from digital outputs defined in TAPPS2
* Send ON/OFF to digital inputs defined in TAPPS2

## Prerequisites

### Configure CAN outputs in TAPPS2

You need to configure CAN outputs in your Functional data on the UVR16x2. This can be done by using the TAPPS2 application from TA. Follow the user guide on how to do this.

### Configure your CMI for COE

Now follow the User Guide of the CMI on how to setup CAN over Ethernet (COE). Here you will map your outputs that you configured in the previous step. This can be acomplished via the GUI on the CMI or via the coe.csv file. As the target device you need to put the IP of your OpenHAB server. Don’t forget to reboot the CMI after you uploaded the coe.csv file.

## Supported Bridge and Things

* TA C.M.I. CoE Bridge

In order to get the CAN over Ethernet (COE) envionment working a `coe-bridge` has to be created. The bridge itself opens the UDP port 5441 for communication with the C.M.I. devices. The bridge could be used for multiple C.M.I. devices.

* TA C.M.I. CoE Connection - Thing

This thing reflects a connection to a node behind a specific C.M.I.. This node could be every CAN-Capable device from TA which allows to define an CAN-Input.

## Discovery

Autodiscovering is not supported. We have to define the things manually.

## Thing Configuration

The _TA C.M.I. CoE Connection_ has to be manually configured, either through the (Paper) UI or via a thing-file. 

This thing reflects a connection to a node behind a specific C.M.I.. This node could be every CAN-Capable device from TA which allows to define an CAN-Input.

It takes the C.M.I.'s IP-Address and the CAN Node-ID from the defice behind the C.M.I. where the data is sent to / received for. The thing has no channels by default - they have to be added manually matching the configured inputs / outputs for the related CAN Node. Digital and Analog channels are supported. Please read TA's documentation related to the CAN-protocol - multiple analog (4) and digital (16) channels are combined so please be aware of this design limitation.

## Channels

Supported channels are:

| channel  | type   | description                  |
|----------|--------|------------------------------|
| coe-digital-in  | Switch (RO) | Digital input channel for digital state data received from the node  |
| coe-digital-out | Switch      | Digital output channel for digital state data sent to the node  |
| coe-analog-in   | Number (RO) | Analog input channel for numeric values received from the node  |
| coe-analog-out  | Number      | Analog output channel for numeric values sent to the node       |

Each channel has an _output id_ as configuration. Output ID's are in range from 1 to 64. For `coe-analog-out` also a measurment type has to be configured so the C.M.I. / Receiving node know's how to handle / interpret the value. The binding will also do some conversion depending on the measurment type. For `coe-analog-in` channels the measurement type is received with the value and so the conversion is automatically applied.

The binding supports all 21 measure types that exist according to the TA documentation. Unfortunately, the documentation is not consistent here, so most of the types are supported only by generic names. The known measure types are:

| id  | type   | description                  |
|----------|--------|------------------------------|
| 1  | Temperature | Tempeature value. Value is multiplied by 0.1  |
| 2  | Unknown2 |   |
| 3  | Unknown3 |   |
| 4  | Seconds |   |
| 5...9 | Unknown5..9 |   |
| 10 | Kilowatt |   |
| 11 | Kilowatthours |   |
| 12 | Megawatthours |   |
| 13..21 | Unknown | |


## Full Example

As there is no common configuration as everything depends on the configuration of the TA devices. So we just can provide some samples providing the basics so you can build the configuration matching your system.

Example of a _.thing_ file:

```
Bridge tacmi:coe-bridge:coe-bridge "TA C.M.I. Bridge"
{

    Thing cmi cmiTest "Test-CMI"@"lab" [ host="192.168.178.33", node=54 ] {
    Channels:
        Type coe-digital-in : digitalInput1 "Digital input 1" [ output=1 ]
        Type coe-digital-out : digitalOutput1 "Digital output 1" [ output=1 ]
        Type coe-analog-in : analogInput1 "Analog input 1" [ output=1 ]
        Type coe-analog-out : analogOutput1 "Analog output 1" [ output=1, type=1 ]
    }

}
```

Sample _.items_-File:

```
Number TACMI_Analog_In_1     "TA input value 1 [%.1f]"  <temperature> {channel="tacmi:cmi:coe-bridge:cmiTest:analogInput1"}
Number TACMI_Analog_Out_1    "TA output value 1 [%.1f]" <temperature> {channel="tacmi:cmi:coe-bridge:cmiTest:analogOutput1"}
Switch TACMI_Digital_In_1    "TA input switch 1 [%s]" <temperature> {channel="tacmi:cmi:coe-bridge:cmiTest:digitalInput1"}
Switch TACMI_Digital_Out_1   "TA output switch 1 [%s]" <temperature> {channel="tacmi:cmi:coe-bridge:cmiTest:digitalOutput1"}
```

Sample _.sitemap_ snipplet

```
sitemap heatingTA label="heatingTA"
{
    Text item=TACMI_Analog_In_1
	Setpoint item=TACMI_Analog_Out_1 step=5 minValue=15 maxValue=45
    Switch item=TACMI_Digital_In_1
    Switch item=TACMI_Digital_Out_1
}
```

## Any custom content here!

It would be great to hear from your projects and ideas / solutions!