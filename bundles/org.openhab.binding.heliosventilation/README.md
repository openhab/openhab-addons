# HeliosVentilation Binding

This is the binding for Helios Ventilation Systems KWL EC 200/300/500 Pro. It requires a connection to the RS485 bus used by the original remote controls KWL-FB (9417) and does not use the Modbus/TCP interface of the newer EasyControl devices.

For electrical connection it is recommended to use an USB-RS485 interface, but any RS485 interface that shows up as a serial port will do.Setup the device as described in https://www.openhab.org/docs/administration/serial.html.

The binding will use the remote control address 15 for communication, so make sure that this is not assigned to a physically present remote control.

## Supported Things

There is only one thing type supported by this binding: a Helios Ventilation System KWL EC 200/300/500 Pro from Helios. The binding was developed and test on a KWL EC 200 Pro device.


## Discovery

No discovery implemented.


## Binding Configuration

The binding requires access to the serial device connecting to the RS485 bus as described in https://www.openhab.org/docs/administration/serial.html. Otherwise only thing configuration is needed.


## Thing Configuration

The binding supports only one thing and requires the configuration of the serial port (typically /dev/ttyUSB0 on Linux and COM3 on Windows) and the polling time which is the cycle time after which the binding tries to reconnect to the bus and requests data updates.


## Channels

_Here you should provide information about available channel types, what their meaning is and how they can be used._

_Note that it is planned to generate some part of this based on the XML files within ```src/main/resources/ESH-INF/thing``` of your binding._

| channel  | type   | description                  |
|----------|--------|------------------------------|
| control  | Switch | This is the control channel  |

## Full Example

_Provide a full usage example based on textual configuration files (*.things, *.items, *.sitemap)._
