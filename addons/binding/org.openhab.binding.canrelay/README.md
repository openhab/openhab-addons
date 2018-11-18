# CanRelay Binding

This binding is for a DIY home automation system controlling lights using custom protocol transmitted over CANBUS. The firmware code, protocol and other details are described at [Github project](https://github.com/PoJD/can). If you are into trying to build this out for yourself, you may also try to ask a PCB manufacturer to print out the PCBs using Eagle project at [Github](https://github.com/PoJD/can-pcb). Then solder all the components and chips and off you go.

The above solution consists of 2 CanRelay devices (acting as termination of the CANBUS), in the current solution each controlling lights on one floor in the house. It can be also used to control just one floor, the other CanRelay would simply only serve the purpose of the CANBUS termination. If more floors are needed, the maximum number of light circuits should not exceed 60 and could be then split across both CanRelay output pins. 

The other part of the physical layer is a set of devices called CanSwitch, that are to be installed in the wall switches sending CAN messages upon a user pressing a toggle switch in the wall connected to them. All these devices contain RJ45 connectors that you can simply wire them together with. You may want to look at [Documentation](https://github.com/PoJD/can/tree/master/Doc) that contains a slide deck with the solution, a diagram and also 2 pictures of both CanRelay and CanSwitch examples as they have been soldered by the author of this binding.

In order for openHAB to talk to these devices over CANBUS, a physical device is needed to send CANBUS traffic across and listen for incoming traffic too. This solution was tested with [USBTin](https://www.fischl.de/usbtin/) and it is at the moment also the only device supported by the binding (plugs into USB and reports itself as CDC ACM Device in Linux). This binding implements protocol described on this page using serial port implementation present in openHAB and can be extended to support other devices too. That protocol allows sending CANBUS traffic across as well as listening for traffic. It is not fully implemented, at the moment supports only standard CAN protocol (e.g. 11 bits for canID only). But this implementation fulfills the requirements for this solution.

In reality majority of the traffic goes on between this device and both CanRelays that keep runtime mapping information to know how to translate specific CANBUS traffic to individual physical lights connected to CanRelay. On the image above you can see the 30 output pins that each of the CanRelays support, so you can in general have up to 60 lights being controlled with this binding. This binding also listens for individual CanSwitch messages being sent to indicate individual lights being toggled (switched on or off a particular light).

## Supported Things

'hwBridge' - the bridge device that talks to the CANBUS. Currently supports only USBTin. This binding communicates to the bridge via serial port in order to send traffic over CANBUS and listen for incoming traffic. 

'light' - individual light thing as detected for all CanRelays.

## Discovery

Discovery auto discovers the hwBridge using all serial ports available at openHAB runtime and not currently in use by any other binding. Feel free to visit [OpenHaB WIKI](https://www.openhab.org/addons/bindings/serial1/) in order to troubleshoot issues with detecting the port or look at the end of this README for some more concrete tips. When you start discovery for this binding, initially only the hwBridge would be discovered. Upon the bridge being added by the user to the system, another discovery would automatically start and discover all the lights available and exposed on the CANBUS from CanRelay(s) (it would be able to work with just 1 CanRelay too). There is a timeout of 2minutes that the light discovery waits for the bridge to be ready, so if the user takes longer than that to add the bridge to the system, new discovery has to be invoked in order to detect the lights. 

No configuration is needed, all should work out of the box. If no port is discovered though (e.g. openHAB not configured to see this port too), user may manually go and add new hwBridge using the respective serialPort. The rest would then work automatically too. See more in the Configuration section.

WHen a bridge is being added to the system, 2 main timers would be automatically started by the binding.

### Cache refresh

This timer would be run every 10sec and would ensure consistency among state of the lights in all CanRelays on physical CANBUS, state of the internal cache in this binding and the actual light switch states in the openHAB UI. That assures everything is kept up to date (e.g. in case of CANBUS issues or this device being disconnected from the CANBUS for some time, etc).

### hwBridge status

A regular check is run every 10sec to see if the connected hwBridge is able to serve commands ran against it from within openHAB. If an issue is detected, the hwBridge is automatically moved to error state and all child lights are disabled as a result. 

### Limitations

At the moment this binding is limited to support only 1 instance of the hwBridge at runtime. The binding would actually control that and any attempts to add any extra hwBridges at runtime would result in those bridges being defaulted to configuration error.

## Configuration

If auto discovery does not work for some reason, user may opt out to using manual configuration of the hwBridge. The configuration for the hwBridge consists of the following parameters:

| Parameter           | Description                                                                                                                         |
|---------------------|-------------------------------------------------------------------------------------------------------------------------------------|
| serialPort          | The serial port where the CanBusDevice is connected to (e.g. Linux: `/dev/ttyACM0`, Windows: `COM1`) (mandatory if set manually) |

While the system allows the user to manually configure a light too, it is not recommended since it cannot possibly work. Only lights auto discovered through the hwBridge would be able to control real lights in the CanRelays. The binding does internally store supported lights detected at CanRelays in a cache, so if no lights are auto discovered, configuring them manually would have no effect (user would be able to control them via UI, but no real physical thing would be represented nor controlled by this).

If new lights were added to an existing solution, CanRelays configured over CANBUS to use them and that the user does not see the new lights in the openHAB system yet, the user can simply invoke discovery again and the internal cached lights would get refreshed and any new lights would be discovered and would appear in the Inbox.

## Channels

'lightSwitch' is the only channel supported by the light thing. As mentioned above though, all lights should be auto discovered and would use this channel to switch the respective light ON or OFF. So no real value in trying to configure a light or a channel manually.

## Implementation notes for developers

CanBusDevice API wraps up communication to the hwBridge. Currently only USBTinDevice implements that interface, but a base class AbstractCanBusDevice is provided for other potential devices to be supported too. USBTinDevice wraps the custom protocol of USBTin simply utilizing its ability to be controlled over sending ASCII commands over the virtual serial port as it reports itself to the host OS.

CanRelayAccess API wraps up the custom protocol of CanRelay. It provides facade for the openHAB bridges, discovery services and other parts of openHAB framework in order to use the custom protocol.

See individual javadoc sections for more details.

## Example preparations needed

This section just lists some examples you may need to run on your system in order to be able to use this solution. It simply follows the [OpenHaB WIKI](https://www.openhab.org/addons/bindings/serial1/) mentioned above.

### Linux OS preparation in order to see the port

Add this into /etc/udev/rules.d/99-tty.rules when using udev to see the device with the same name all the time (change below IDs accordingly if you are not using USBTin)

```
KERNEL=="tty[A-Z]*[0-9]", GROUP="dialout"
SUBSYSTEM=="tty", ATTRS{idVendor}=="04d8", ATTRS{idProduct}=="000a", ATTRS{serial}=="0", SYMLINK+="ttyACM0"
```

### Running the binding in IDE

Add -Dgnu.io.rxtx.SerialPorts=/dev/ttyACM0 into runtime VM args of openHAB (or change accordingly to match your device detected symlink in /dev).
