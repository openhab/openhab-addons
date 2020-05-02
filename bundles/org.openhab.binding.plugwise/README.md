# Plugwise Binding

The Plugwise binding adds support to openHAB for [Plugwise](https://www.plugwise.com) ZigBee devices using the Stick.

Users should use the Plugwise [Source](https://www.plugwise.com/source) software to define the network, reset devices or perform firmware upgrades.

Currently only "V2" of the Plugwise protocol is supported. It is adviced that users of the binding upgrade their devices to the latest firmware using the Plugwise Source software.

## Supported Things

The binding supports the following Plugwise devices:

| Device Type                                                   | Description                                                                              | Thing Type |
|---------------------------------------------------------------|------------------------------------------------------------------------------------------|------------|
| [Circle](https://www.plugwise.com/en_US/products/circle)      | A power outlet plug that provides energy measurement and switching control of appliances | circle     |
| [Circle+](https://www.plugwise.com/en_US/products/circle)     | A special Circle that coordinates the ZigBee network and acts as network gateway         | circleplus |
| [Scan](https://www.plugwise.com/en_US/products/scan)          | A wireless motion (PIR) and light sensor                                                 | scan       |
| [Sense](https://www.plugwise.com/en_US/products/sense)        | A wireless temperature and humidity sensor                                               | sense      |
| [Stealth](https://www.plugwise.com/en_US/products/stealth)    | A Circle with a more compact form factor that can be built-in                            | stealth    |
| [Stick](https://www.plugwise.com/en_US/products/start-source) | A ZigBee USB controller that openHAB uses to communicate with the Circle+                | stick      |
| [Switch](https://www.plugwise.com/en_US/products/switch)      | A wireless wall switch                                                                   | switch     |

## Discovery

Automatic device discovery runs every 3 minutes which can be sped up by starting a manual discovery.
The Stick is automatically discovered on unused serial ports.
All Circle, Circle+ and Stealth devices are discovered immediately after adding the Stick. Battery powered devices like the Scan, Sense and Switch are discovered when they are awake.
The Scan and Sense can be woken by pressing the "Wake" button.
The Switch is detected when it is awake after switching the left or right button.

## Thing Configuration

### MAC addresses

The MAC addresses are stickered to the back of Plugwise devices.
The binding uses full MAC addresses i.e. also the fine print on the sticker.
If you don't want to get off your chair, climb up ladders and unplug devices all across your home, causing all sorts of havoc; you can also find them in Source. Open `Settings > Appliances`. Then double click on an appliance.
Click on the little Circle icon to the right of the short Address to see the details of a module and the full MAC address.

Similarly the MAC addresses of the Scan, Sense and Switch can also be obtained from the Appliances screen by double clicking them in the `Sensors and other modules` list.

### Stick

| Configuration Parameter | Required | Default      | Description                                                                       |
|-------------------------|----------|--------------|-----------------------------------------------------------------------------------|
| serialPort              | X        | /dev/ttyUSB0 | The serial port of the Stick, e.g. "/dev/ttyUSB0" for Linux or "COM1" for Windows |
| messageWaitTime         |          | 150          | The time to wait between messages sent on the ZigBee network (in ms)              |

To determine the serial port in Linux, insert the Stick, then execute the `dmesg` command.
The last few lines of the output will contain the USB port of the Stick (e.g. `/dev/ttyUSB0`).
In Windows the Device Manager lists it in the `Ports (COM & LPT)` section.
On some Linux distributions (e.g. Raspbian) an OS restart may be required before the Stick is properly configured.
To access the serial port of the Stick on Linux, the user running openHAB needs to be part of the 'dialout' group. E.g. for the user 'openhab' issue the following command: `sudo adduser openhab dialout`.

### Circle(+), Stealth

| Configuration Parameter | Required | Default          | Description                                                                                                            |
|-------------------------|----------|------------------|------------------------------------------------------------------------------------------------------------------------|
| macAddress              | X        |                  | The full device MAC address e.g. "000D6F0000A1B2C3"                                                                    |
| powerStateChanging      |          | commandSwitching | Controls if the power state can be changed with commands or is always on/off (commandSwitching, alwaysOn or alwaysOff) |
| suppliesPower           |          | false            | Enables power production measurements (true or false)                                                                  |
| measurementInterval     |          | 60               | The energy measurement interval (in minutes) (5 to 60)                                                                 |
| temporarilyNotInNetwork |          | false            | Stops searching for an unplugged device on the ZigBee network traffic (true or false)                                  |


### Scan

| Configuration Parameter | Required | Default | Description                                                                                                      |
|-------------------------|----------|---------|------------------------------------------------------------------------------------------------------------------|
| macAddress              | X        |         | The full device MAC address e.g. "000D6F0000A1B2C3"                                                              |
| sensitivity             |          | medium  | The sensitivity of movement detection (off, medium or high)                                                      |
| switchOffDelay          |          | 5       | The delay the Scan waits before sending an off command when motion is no longer detected (in minutes) (1 to 240) |
| daylightOverride        |          | false   | Disables movement detection when there is daylight (true or false)                                               |
| wakeupInterval          |          | 1440    | The interval in which the Scan wakes up at least once (in minutes) (5 to 1440)                                   |
| wakeupDuration          |          | 10      | The number of seconds the Scan stays awake after it woke up (10 to 120)                                          |


### Sense

| Configuration Parameter | Required | Default         | Description                                                                                                                |
|-------------------------|----------|-----------------|----------------------------------------------------------------------------------------------------------------------------|
| macAddress              | X        |                 | The full device MAC address e.g. "000D6F0000A1B2C3"                                                                        |
| measurementInterval     |          | 15              | The interval in which the Sense measures the temperature and humidity (in minutes) (5 to 60)                               |
| boundaryType            |          | none            | The boundary type that is used for switching (none, temperature or humidity)                                               |
| boundaryAction          |          | offBelowOnAbove | The boundary switch action when the value is below/above the boundary minimum/maximum (offBelowOnAbove or onBelowOffAbove) |
| temperatureBoundaryMin  |          | 15              | The minimum boundary for the temperature boundary action (0 to 60)                                                         |
| temperatureBoundaryMax  |          | 25              | The maximum boundary for the temperature boundary action (0 to 60)                                                         |
| humidityBoundaryMin     |          | 45              | The minimum boundary for the humidity boundary action (5 to 95)                                                            |
| humidityBoundaryMax     |          | 65              | The maximum boundary for the humidity boundary action (5 to 95)                                                            |
| wakeupInterval          |          | 1440            | The interval in which the Sense wakes up at least once (in minutes) (5 to 1440)                                            |
| wakeupDuration          |          | 10              | The number of seconds the Sense stays awake after it woke up (10 to 120)                                                   |


### Switch

| Configuration Parameter | Required | Default | Description                                                                      |
|-------------------------|----------|---------|----------------------------------------------------------------------------------|
| macAddress              | X        |         | The full device MAC address e.g. "000D6F0000A1B2C3"                              |
| wakeupInterval          |          | 1440    | The interval in which the Switch wakes up at least once (in minutes) (5 to 1440) |
| wakeupDuration          |          | 10      | The number of seconds the Switch stays awake after it woke up (10 to 120)        |


## Channels

| Channel Type ID  | Item Type            | Description                                                                                                                                                                                                        | Thing Types                                      |
|------------------|----------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------|
| clock            | String               | Time as indicated by the internal clock of the device                                                                                                                                                              | circle, circleplus, stealth                      |
| energy           | Number:Energy        | Energy consumption/production during the last measurement interval                                                                                                                                                 | circle, circleplus, stealth                      |
| energystamp      | DateTime             | Timestamp of the start of the last energy measurement interval                                                                                                                                                     | circle, circleplus, stealth                      |
| humidity         | Number:Dimensionless | Current relative humidity                                                                                                                                                                                          | sense                                            |
| lastseen         | DateTime             | Timestamp of the last received message. Because there is no battery level indication this is a helpful value to determine if a battery powered device is still operating properly even when no state changes occur | circle, circleplus, scan, sense, stealth, switch |
| leftbuttonstate  | Switch               | Current state of the left button                                                                                                                                                                                   | switch                                           |
| power            | Number:Power         | Current power consumption, measured over 1 second interval                                                                                                                                                         | circle, circleplus, stealth                      |
| realtimeclock    | DateTime             | Time as indicated by the internal clock of the Circle+                                                                                                                                                             | circleplus                                       |
| rightbuttonstate | Switch               | Current state of the right button                                                                                                                                                                                  | switch                                           |
| state            | Switch               | Switches the power state on/off                                                                                                                                                                                    | circle, circleplus, stealth                      |
| temperature      | Number:Temperature   | Current temperature                                                                                                                                                                                                | sense                                            |
| triggered        | Switch               | Most recent switch action initiated by the device. When daylight override is disabled on a Scan this corresponds one to one with motion detection                                                                  | scan, sense                                      |

## Example

demo.things

```
Bridge plugwise:stick:demostick [ serialPort="/dev/ttyUSB0", messageWaitTime=150 ]

Thing plugwise:circle:fan (plugwise:stick:demostick) [ macAddress="000D6F0000A1A1A1", measurementInterval=15 ]

Thing plugwise:circleplus:lamp (plugwise:stick:demostick) [ macAddress="000D6F0000B2B2B2" ] {
    Channels:
        Type clock : clock [ updateInterval=30 ]
        Type energy : energy [ updateInterval=600 ]
        Type power : power [ updateInterval=10 ]
        Type realtimeclock : realtimeclock [ updateInterval=30 ]
        Type state : state [ updateInterval=10 ]
}

Thing plugwise:scan:motionsensor (plugwise:stick:demostick) [ macAddress="000D6F0000C3C3C3", sensitivity="high", switchOffDelay=10 ]

Thing plugwise:sense:climatesensor (plugwise:stick:demostick) [ macAddress="000D6F0000D4D4D4", measurementInterval=10, boundaryType="temperature", boundaryAction="onBelowOffAbove", temperatureBoundaryMin=15, temperatureBoundaryMax=20 ]

Thing plugwise:stealth:fridge (plugwise:stick:demostick) [ macAddress="000D6F0000E5E5E5", powerStateChanging="alwaysOn" ] {
    Channels:
        Type power : power [ updateInterval=10 ]
        Type state : state [ updateInterval=10 ]
}

Thing plugwise:switch:lightswitches (plugwise:stick:demostick) [ macAddress="000D6F0000F6F6F6", wakeupInterval=240, wakeupDuration=20 ]
```

demo.items

```
/* Circle */
Switch Fan_Switch "Switch" <switch> { channel="plugwise:circle:fan:state" }
String Fan_Clock "Clock [%s]" <clock> { channel="plugwise:circle:fan:clock" }
Number:Power Fan_Power "Power [%.1f %unit%]" <energy> { channel="plugwise:circle:fan:power" }
Number:Energy Fan_Energy "Energy [%.3f %unit%]" <chart> { channel="plugwise:circle:fan:energy" }
DateTime Fan_Energy_Stamp "Energy stamp [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]" <calendar> { channel="plugwise:circle:fan:energystamp" }
DateTime Fan_Last_Seen "Last seen [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]" <calendar> { channel="plugwise:circle:fan:lastseen" }

/* Circle+ */
Switch Lamp_Switch "Switch" <switch> { channel="plugwise:circleplus:lamp:state" }
String Lamp_Clock "Clock [%s]" <clock> { channel="plugwise:circleplus:lamp:clock" }
Number:Power Lamp_Power "Power [%.1f %unit%]" <energy> { channel="plugwise:circleplus:lamp:power" }
Number:Energy Lamp_Energy "Energy [%.3f %unit%]" <chart> { channel="plugwise:circleplus:lamp:energy" }
DateTime Lamp_Energy_Stamp "Energy stamp [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]" <calendar> { channel="plugwise:circleplus:lamp:energystamp" }
DateTime Lamp_Real_Time_Clock "Real time clock [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]" <clock> { channel="plugwise:circleplus:lamp:realtimeclock" }
DateTime Lamp_Last_Seen "Last seen [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]" <calendar> { channel="plugwise:circleplus:lamp:lastseen" }

/* Scan */
Switch Motion_Sensor_Switch "Triggered [%s]" <switch> { channel="plugwise:scan:motionsensor:triggered" }
DateTime Motion_Sensor_Last_Seen "Last seen [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]" <clock> { channel="plugwise:scan:motionsensor:lastseen" }

/* Sense */
Switch Climate_Sensor_Switch "Triggered [%s]" <switch> { channel="plugwise:sense:climatesensor:triggered" }
Number:Dimensionless Climate_Sensor_Humidity "Humidity [%.1f %unit%]" <humidity> { channel="plugwise:sense:climatesensor:humidity" }
Number:Temperature Climate_Sensor_Temperature "Temperature [%.1f %unit%]" <temperature> { channel="plugwise:sense:climatesensor:temperature" }
DateTime Climate_Sensor_Last_Seen "Last seen [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]" <clock> { channel="plugwise:sense:climatesensor:lastseen" }

/* Stealth */
Switch Fridge_Switch "Switch" <switch> { channel="plugwise:stealth:fridge:state" }
String Fridge_Clock "Clock [%s]" <clock> { channel="plugwise:stealth:fridge:clock" }
Number:Power Fridge_Power "Power [%.1f %unit%]" <energy> { channel="plugwise:stealth:fridge:power" }
Number:Energy Fridge_Energy "Energy [%.3f %unit%]" <chart> { channel="plugwise:stealth:fridge:energy" }
DateTime Fridge_Energy_Stamp "Energy stamp [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]" <calendar> { channel="plugwise:stealth:fridge:energystamp" }
DateTime Fridge_Last_Seen "Last seen [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]" <calendar> { channel="plugwise:stealth:fridge:lastseen" }

/* Switch */
Switch Light_Switches_Left_Button_State "Left button [%s]" <switch> { channel="plugwise:switch:lightswitches:leftbuttonstate" }
Switch Light_Switches_Right_Button_State "Right button [%s]" <switch> { channel="plugwise:switch:lightswitches:rightbuttonstate" }
DateTime Light_Switches_Last_Seen "Last seen [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]" <clock> { channel="plugwise:switch:lightswitches:lastseen" }
```

demo.sitemap

```
sitemap demo label="Main Menu"
{

  Frame label="Fan (Circle)" {
    Switch item=Fan_Switch
    Text item=Fan_Clock
    Text item=Fan_Power
    Text item=Fan_Energy
    Text item=Fan_Energy_Stamp
    Text item=Fan_Last_Seen
  }

  Frame label="Lamp (Circle+)" {
    Switch item=Lamp_Switch
    Text item=Lamp_Clock
    Text item=Lamp_Power
    Text item=Lamp_Energy
    Text item=Lamp_Energy_Stamp
    Text item=Lamp_Real_Time_Clock
    Text item=Lamp_Last_Seen
  }

  Frame label="Motion Sensor (Scan)" {
    Text item=Motion_Sensor_Switch
    Text item=Motion_Sensor_Last_Seen
  }

  Frame label="Climate Sensor (Sense)" {
    Text item=Climate_Sensor_Switch
    Text item=Climate_Sensor_Humidity
    Text item=Climate_Sensor_Temperature
    Text item=Climate_Sensor_Last_Seen
  }

  Frame label="Fridge (Stealth)" {
    Switch item=Fridge_Switch
    Text item=Fridge_Clock
    Text item=Fridge_Power
    Text item=Fridge_Energy
    Text item=Fridge_Energy_Stamp
    Text item=Fridge_Last_Seen
  }

  Frame label="Light Switches (Switch)" {
    Text item=Light_Switches_Left_Button_State
    Text item=Light_Switches_Right_Button_State
    Text item=Light_Switches_Last_Seen
  }

}
```
