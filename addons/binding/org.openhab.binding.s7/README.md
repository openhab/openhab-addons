# Siemens S7 Binding

This binding allows to connect with Siemens S7 PLCs.

## Supported Things

The binding relies on the Moka7 library which supports Siemens S7 PLCs. It also partially supports the new CPUs 1200/1500, the old S7200, the small LOGO 0BA7/0BA8 and SINAMICS Drives.

## Discovery

Autodiscovery is not supported by this binding.

## Thing Configuration

The PLC itself is seen by OpenHab as a bridge. On this bridge you can configure as many switch, light, contact or pushbutton you want.

The following parameters can be defined:
* ```server``` (The OpenHab bridge)
  * ```ipAddress```: The IP address of the PLC.
  * ```localTSAP``` and ```remoteTSAP```: Defines the endpoints. For more details, see the [details](http://snap7.sourceforge.net/plc_connection.html) on Snap7 website. 
  * ```pollingInterval```: Defines the delay between reading PLC
* ```switch```, ```light```, ```contact``` or ```pushbutton``` (The OpenHab things)
  * ```accessMode```: Defines the access mode to the things
    * ```ToggleMode```: Means that a pulse is sent to the input area/address to toggle the thing state.
    * ```ReadWrite```: Means that the new state is written to the input area/address to set the thing state.
    * ```ReadOnly```: Means the thing cannot be manipulated (default access mode for thing of type ```contact```).
    * ```Pushbutton```: Means that the state cannot be readen (default access for thing of type ```pushbutton```). 

## Channels

The things has only one channel called ```state```, which is of type "switch" for switches, lights and pushbuttons and of type "contact" for contacts.

The server also has a channel called ```refreshDuration``` that gives the number of milliseconds between two refreshes of the data.

## Full Example

Here under is an example thing file.
```
Bridge s7:server:PLC     [ ipAddress="192.168.1.30", localTSAP=256, remoteTSAP=512, pollingInterval=200 ] {
    Thing switch     MySwitch         [ accessMode="ToggleMode", inputDBArea=132, inputAddress=81, outputDBArea=130, outputAddress=15 ]
    Thing light      MyLight          [ accessMode="ToggleMode", inputDBArea=132, inputAddress=39, outputDBArea=132, outputAddress=63 ]
    Thing contact    MyContact        [ outputDBArea=132, outputAddress=12 ]
    Thing pushbutton MyPushButton     [ inputDBArea=132, inputAddress=36 ]
}
```
