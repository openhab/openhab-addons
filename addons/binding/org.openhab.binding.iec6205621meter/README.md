# IEC 62056-21 Meter Binding

This binding is used to communicate to metering devices supporting serial communication according IEC 62056-21 mode C master. It can be used to read metering data from slaves such as gas, water, heat, or electricity meters.

For further information read Wiki page of [IEC 62056-21](http://en.wikipedia.org/wiki/IEC_62056#IEC_62056-21).

Information received from the meter device are structured according IEC 62056-6-1:2013, Object identification system (OBIS). For further information read Wiki page of [OBIS ("Object Identification System")](http://de.wikipedia.org/wiki/OBIS-Kennzahlen).

## Binding Configuration

The binding requires no special configuration

## Thing Configuration

This binding provides a IEC 62056-21 C ,mode meter thing type, which can be configured in PaperUI or in Thing file.
The configuration allows the definition of multiple meter devices.

thing definition
iec6205621meter:meter:mymeter [port="/dev/tts0", baudRateChangeDelay=false, initMessage="" refresh=60, ]


| Property | Default | Required | Description |
|----------|---------|:--------:|-------------|
| `Portname |/dev/ttyS0 | Yes | the serial port to use for connecting to the metering device e.g. COM1 for Windows and /dev/ttyS0 or /dev/ttyUSB0 for Linux |
| `Baud rate change delay | 0 | No | Delay of baud rate change in ms. Default is 0. USB to serial converters often require a delay of up to 250ms |
| `Echo handling | true | No | Enable handling of echos caused by some optical tranceivers |
| `Extra pre init bytes |  | No | extra pre init bytes. Default value is empty. |
| `Meter refresh rate | 600 | No | Refresh rate in [s] to query the meter data. |


## Item Configuration

The syntax of an item configuration is shown in the following line in general:

```
String test "meter type [%s]" { channel="iec6205621meter:meter:mymeter:meterType" }
```

Where `<mymeter>` matches one of the defined things defined in your Thing file.

If you do not know the available OBIS on your meter device, you can probably find them on the local HMI of you meter device. Please review you manual of the meter device or read the instruction of your utility.

You can also start openHAB in debug mode, the binding will then dump all available OBIS it receives from the meter device in the osgi console.

## Channels

All devices support some of the generic  channels types:

| Channel Type ID         | Item Type    | Description  |
|-------------------------|------------------------|--------------|----------------- |------------- |
| meterType                   | Textt       | Type Designator Meter |
| obisNumber                    | Number       | OBIS Number data channel of IEC 62056-21 Meter |
| obisString                  | String       | OBIS String data channel of IEC 62056-21 Meter |

Based on these channel types the connected meter channels are created dynamically within the initialization of the thing.
You can see the generated channels in the thing configuration in PaperUI or in the REST API.

## Tested Hardware

The binding has been successfully tested with below hardware configuration:

* Landis & Gyr meter [ZMD120AR](http://www.landisgyr.ch/product/landisgyr-zmd120ar/)  connected with [IR-Reader RS232](http://wiki.volkszaehler.org/hardware/controllers/ir-schreib-lesekopf) from open hardware project in [volkszaehler](http://volkszaehler.org/)
* Landis & Gyr meter [E350](http://www.landisgyr.ch/product/landisgyr-e350-electricity-meter-new-generation/)  connected with [IR-Reader RS232](http://wiki.volkszaehler.org/hardware/controllers/ir-schreib-lesekopf) from open hardware project in [volkszaehler](http://volkszaehler.org/)
* Landis & Gyr meter connected the [IR-Reader USB](http://wiki.volkszaehler.org/hardware/controllers/ir-schreib-lesekopf-usb-ausgang) from open hardware project in [volkszaehler](http://volkszaehler.org/)
