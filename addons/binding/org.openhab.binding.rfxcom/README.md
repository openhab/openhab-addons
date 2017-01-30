# RFXCOM Binding

This binding integrates large number of sensors and actuators from several different manufactures through [RFXCOM transceivers](http://www.rfxcom.com).

RFXCOM transceivers support RF 433 Mhz protocols like: 
* HomeEasy 
* Cresta 
* X10 
* La Crosse
* OWL
* CoCo (KlikAanKlikUit), 
* PT2262
* Oregon etc.

See the RFXtrx User Guide from [RFXCOM](http://www.rfxcom.com) for the complete list of supported sensors and devices as well as firmware update announcements.


## Supported Things

This binding supports the RFXtrx433E and RFXtrx315 transceivers and the RFXrec433 receiver as bridges for accessing different sensors and actuators.

This binding currently supports following packet types:

* Blinds1
* Curtain1 
* Energy
* Humidity
* Lighting1
* Lighting2
* Lighting4
* Lighting5
* Lighting6
* Rain
* Rfy
* Security1
* Temperature
* TemperatureHumidity
* Thermostat1
* Undecoded
* Wind


## Discovery

The transceivers/receivers are automatically discovered by the JD2XX library and put in the Inbox. 

Apple OS X note:

Apple provides build-in FTDI drivers for OS X, which need to be disabled to get JD2XX work properly.

FTDI driver disabling can be done by the following command

```
sudo kextload -b com.apple.driver.AppleUSBFTDI
```

FTDI driver can be enabled by the following command

```
sudo kextunload -b com.apple.driver.AppleUSBFTDI
```

If you have any problems with JD2XX or you don't want to disable FTDI driver on OS X, you can also configure RFXCOM transceivers/receivers manually.

You can also use an RFXCOM device over TCP/IP. To start a TCP server for an RFXCOM device, you can use socat:
```
socat tcp-listen:10001,fork,reuseaddr file:/dev/ttyUSB0,raw
``` 

After the bridge is configured and the transceiver receives a message from any sensor or actuator, the device is put in the Inbox. Because RFXCOM communication is a one way protocol, receiver actuators can't be discovered automatically.

Both bridges and sensor/actuators are easy to configure from the Paper UI. However, you can configure things manually in the thing file, for example:

```
Bridge rfxcom:bridge:usb0 [ serialPort="/dev/tty.usbserial-06VVEG1Y" ] {
    Thing lighting2 100001_1 [deviceId="100001.1", subType="AC"]
}
```

A TCP bridge, for use with socat on a remote host, can be configured like this:

```
Bridge rfxcom:tcpbridge:sunflower [ host="sunflower", port=10001 ] {
    Thing lighting2 100001_1 [deviceId="100001.1", subType="AC"]
}
```

## Channels

This binding currently supports following channels:

| Channel Type ID | Item Type    | Description  |
|-----------------|------------------------|--------------|
| batteryLevel | Number | Battery level. |
| command | Switch | Command channel. |
| contact | Contact | Contact channel. |
| dimmingLevel | Dimmer | Dimming level channel. |
| humidity | Number | Relative humidity level in percentages. |
| humidityStatus | String | Current humidity status. |
| instantamp | Number | Instant current in Amperes. |
| instantpower | Number | Instant power consumption in Watts. |
| status | String | Status channel. |
| setpoint | Number | Requested temperature. |
| mood | Number | Mood channel. |
| motion | Switch | Motion detection sensor state. |
| rainRate | Number | Rain fall rate in millimeters per hour. |
| rainTotal | Number | Total rain in millimeters. |
| rawMessage | String | Hexadecimal string of the raw RF message. |
| rawPayload | String | Hexadecimal string of the message payload, without header. |
| shutter | Rollershutter | Shutter channel. |
| signalLevel | Number | Received signal strength level. |
| temperature | Number | Current temperature in degree Celsius. |
| totalUsage | Number | Used energy in Watt hours. |
| totalAmpHour | Number | Used "energy" in ampere-hours. |
| windDirection | Number | Wind direction in degrees. |
| windSpeed | Number | Average wind speed in meters per second. |
