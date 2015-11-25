# RFXCOM Binding

This binding integrates large number of sensors and actuators from several different manufactures throug the [RFXCOM transceivers](http://www.rfxcom.com).

RFXCOM transceivers supports RF 433 Mhz protocols like: 
* HomeEasy 
* Cresta 
* X10 
* La Crosse
* OWL
* CoCo (KlikAanKlikUit), 
* PT2262
* Oregon etc.

See RFXtrx User Guide for the complete list of supported sensors and devices from [RFXCOM](http://www.rfxcom.com) and firmware update announcements.


## Supported Things

Binding should support RFXtrx433E and RFXtrx315 transceivers and RFXrec433 receiver as bridge for accessing different sensors and actuators.

RFXCOM binding currently supports following packet types:

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

If you meet any problems with JD2XX or you don't want to disable FTDI driver on OS X, you can also configure RFXCOM transceivers/receivers manually.

After bridge is configured and transceiver receives message from any sensor and actuator, device is put in the Inbox. Because RFXCOM communication is one way protocol, receiver actuators can't be discovered automatically.

Both bridges and sensor/actuators are easy to configure from the Paper UI. However, a manual configuration looks (thing file) e.g. like

```
Bridge rfxcom:bridge:usb0 [ serialPort="/dev/tty.usbserial-06VVEG1Y" ] {
    Thing lighting2 100001_1 [deviceId="100001.1", subType="AC"]
}
```

## Channels

Currently supported  channels:

| Channel Type ID | Item Type    | Description  |
|-----------------|------------------------|--------------|
| command | Switch | Command channel. |
| contact | Contact | Contact channel. |
| dimminglevel | Dimmer | Dimming level channel. |
| mood | Number | Mood channel. |
| status | String | Status channel. |
| setpoint | Number | Requested temperature. |
| motion | Switch | Motion detection sensor state. |
| rainrate | Number | Rain fall rate in millimeters per hour. |
| raintotal | Number | Total rain in millimeters. |
| shutter | Rollershutter | Shutter channel. |
| instantpower | Number | Instant power consumption in Watts. |
| totalusage | Number | Used energy in Watt hours. |
| instantamp | Number | Instant current in Amperes. |
| totalamphours | Number | Used "energy" in ampere-hours. |
| temperature | Number | Current temperature in degree Celsius. |
| humidity | Number | Relative humidity level in percentages. |
| humiditystatus | String | Current humidity status. |
| signallevel | Number | Received signal strength level. |
| batterylevel | Number | Battery level. |
| windspeed | Number | Average wind speed in meters per second. |
| winddirection | Number | Wind direction in degrees. |
