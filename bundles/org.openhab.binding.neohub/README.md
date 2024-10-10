# NeoHub Binding

This is a binding for integrating [Heatmiser](https://www.heatmiser.com) room and underfloor heating control products.
The NeoHub (bridge) binding allows you to connect openHAB via TCP/IP to Heatmiser's NeoHub and integrate your Heatmiser smart thermostats, smart plugs, and accessories.

![NeoHub](doc/neohub-2.jpg)

## Supported Things

The binding supports the following types of Thing..

| Thing Type           | Description                                                                               |
|----------------------|-------------------------------------------------------------------------------------------|
| NeoHub               | The Heatmiser NeoHub bridge which is used to communicate with NeoStat and NeoPlug devices |
| NeoStat              | Heatmiser Neostat Smart Thermostat                                                        |
| NeoPlug              | Heatmiser NeoPlug Smart Plug                                                              |
| NeoContact           | Heatmiser Contact Sensor, wireless door or window contact                                 |
| NeoTemperatureSensor | Heatmiser Wireless Air Sensor, wireless temperature sensor                                |

## Discovery

The binding automatically searches for NeoHub devices, and puts them in the Main UI Inbox.
Alternatively you can manually create a (Bridge) Thing for the NeoHub.
In either case you need to enter any missing Configuration Parameters (see Thing Configuration for NeoHub below).
Once the Configuration Parameters are all valid, then the NeoHub Thing will automatically attempt to connect and sign on to the hub.
If the sign on succeeds, the Thing will indicate its status as Online, otherwise it will show an error status.

Once the NeoHub Thing has been created and it has successfully signed on, it will automatically interrogate the HeoHub to discover all the respective Heatmiser device Things that are connected to it.
If in the future, you add additional Heatmiser devices to your system, the binding will discover them too.

## Thing Configuration for "NeoHub"

The NeoHub Thing connects to the hub (bridge) to communicate with any respective connected Heatmiser device Things.
It signs on to the hub using the supplied connection parameters, and it polls the hub at regular intervals to read and write the data for each Heatmiser device.
The NeoHub supports two Application Programming Interfaces "API" (an older "legacy" one, and a modern one), and this binding can use either of them to communicate with it.
Before the binding can communicate with the hub, the following Configuration Parameters must be entered.

| Configuration Parameter    | Description                                                                                              |
|----------------------------|----------------------------------------------------------------------------------------------------------|
| hostName                   | Host name (IP address) of the NeoHub (example 192.168.1.123)                                             |
| useWebSocket<sup>1)</sup>  | Use secure WebSocket to connect to  the NeoHub (example `true`)                                          |
| apiToken<sup>1)</sup>      | API Access Token for secure connection to hub. Create the token in the Heatmiser mobile App              |
| pollingInterval            | Time (seconds) between polling requests to the NeoHub (Min=4, Max=60, Default=60)                        |
| socketTimeout              | Time (seconds) to allow for TCP socket connections to the hub to succeed (Min=4, Max=20, Default=5)      |
| preferLegacyApi            | ADVANCED: Prefer to use older API calls; but if not supported, it switches to new calls (Default=false)  |
| portNumber<sup>2)</sup>    | ADVANCED: Port number for connection to the NeoHub (Default=0 (automatic))                               |

<sup>1)</sup> If `useWebSocket` is false, the binding will connect via an older and less secure TCP connection, in which case `apiToken` is not required.
However see the chapter "Connection Refused Errors" below.
Whereas if you prefer to connect via more secure WebSocket connections then an API access token `apiToken` is required.
You can create an API access token in the Heatmiser mobile App (Settings | System | API Access).

<sup>2)</sup> Normally the port number is chosen automatically (for TCP it is 4242 and for WebSocket it is 4243).
But you can override this in special cases if you want to use (say) port forwarding.

## Connection Refused Errors

From early 2022 Heatmiser introduced NeoHub firmware that has the ability to enable / disable connecting to it via a TCP port.
If the TCP port is disabled the openHAB binding cannot connect and the binding will report a _"Connection Refused"_ warning in the log.
In prior firmware versions the TCP port was always enabled.
But in the new firmware the TCP port is initially enabled on power up but if no communication occurs for 48 hours it is automatically disabled.
Alternatively the Heatmiser mobile app has a setting (Settings | System | API Access | Legacy API Enable | On) whereby the TCP port can be permanently enabled.

## Thing Configuration for "NeoStat" and "NeoPlug"

The NeoHub Thing connects to the hub (bridge) to communicate with any Heatmiser devices that are connected to it.
Each such Heatmiser device is identified by means of a unique device name in the hub.
The device name is automatically discovered by the NeoHub Thing, and it is also visible (and changeable) via the Heatmiser App.

| Configuration Parameter | Description                                                                          |
|-------------------------|--------------------------------------------------------------------------------------|
| deviceNameInHub         | Device name that identifies the Heatmiser device in the NeoHub and the Heatmiser App |

## Channels for "NeoHub"

The following Channels, and their associated channel types are shown below.
| Channel        | Data Type            | Description                                                                                                                                  |
|----------------|----------------------|----------------------------------------------------------------------------------------------------------------------------------------------|
| meshNetworkQoS | Number:Dimensionless | RF mesh network Quality-of-Service; this is the percentage of configured devices that are currently connected online via the RF mesh network |

## Channels for "NeoStat" Thermostat

The following Channels, and their associated channel types are shown below.

| Channel               | Data Type          | Description                                                                 |
|-----------------------|--------------------|-----------------------------------------------------------------------------|
| roomTemperature       | Number:Temperature | Actual room temperature                                                     |
| targetTemperature     | Number:Temperature | Target temperature setting for the room                                     |
| floorTemperature      | Number:Temperature | Actual floor temperature                                                    |
| thermostatOutputState | String             | Status of whether the thermostat is Off, or calling for Heat                |
| occupancyModePresent  | Switch             | The thermostat is in the Present Occupancy Mode (Off=Absent, On=Present)    |
| batteryLowAlarm       | Switch             | The battery is low (only applies for battery powered thermostats) (Off=Ok, On=Alarm) |

## Channels for "NeoPlug" Smart Plug

The following Channels, and their associated channel types are shown below.

| Channel              | Data Type | Description                                              |
|----------------------|-----------|----------------------------------------------------------|
| plugOutputState      | Switch    | The output state of the plug switch (Off, On)            |
| plugAutoMode         | Switch    | The plug is in Automatic Mode (Off=Manual, On=Automatic) |

## Channels for "NeoContact" Contact Sensor

The following Channels, and their associated channel types are shown below.

| Channel           | Data Type | Description                                              |
|-------------------|-----------|----------------------------------------------------------|
| contactState      | Contact   | The state of the contact                                 |
| batteryLowAlarm   | Switch    | The battery is low (Off=Ok, On=Alarm)                    |

## Channels for "NeoTemperatureSensor" Wireless Air Sensor

The following Channels, and their associated channel types are shown below.

| Channel           | Data Type          | Description                                     |
|-------------------|--------------------|-------------------------------------------------|
| sensorTemperature | Number:Temperature | Actual measured temperature                     |
| batteryLowAlarm   | Switch             | The battery is low (Off=Ok, On=Alarm)           |

## Channel Configuration (Optional)

The Heatmiser devices are connected to the NeoHub by means of an RF (radio frequency) mesh network.
Occasionally it is possible that a device might drop out of the mesh.
This is usually a temporary issue (e.g. interference), and the device usually reconnects itself automatically.
If a device drops out of the mesh, you can select whether openHAB shall either a) change the Channel value to `UNDEF`, or b) hold the value that it had prior to the drop out.
The choice of a) or b) is determined by an optional Channel configuration parameter `holdOnlineState` as follows.

| Parameter       | Type   | Description                                                                                         |
|-----------------|--------|-----------------------------------------------------------------------------------------------------|
| holdOnlineState | Switch | If the respective device drops out of the RF mesh, the behaviour is as follows:<br> - Off: openHAB  changes the Channel's state to `UNDEF`<br> - On: openHAB holds the Channel's state unchanged<br>The default setting is Off |

The purpose of `holdOnlineState` is so you can choose to overlook value fluctuations if drop outs occur e.g. to "hold" the values in a temperature graph display.

Note: if a drop out occurs, the Thing will always change its status to `OFFLINE` (irrespective of the `holdOnlineState` setting).

## Full Example

### `demo.things` File

```java
Bridge neohub:neohub:myhubname "Heatmiser NeoHub" [ hostName="192.168.1.123", portNumber=4242, pollingInterval=60, socketTimeout=5, preferLegacyApi=true ] {
    Thing neoplug mydownstairs "Downstairs Plug" @ "Hall" [ deviceNameInHub="Hall Plug" ]
    Thing neostat myupstairs "Upstairs Thermostat" @ "Landing" [ deviceNameInHub="Landing Thermostat" ]
    Thing neocontact mycontact "Window Contact" @ "Bedroom" [ deviceNameInHub="Bedroom Window Contact" ]
    Thing neotemperaturesensor mysensor "Kitchen Temperature" @ "Kitchen" [ deviceNameInHub="Kitchen Temperature Sensor" ]
}
```

### `demo.items` File

```java
Number:Temperature Upstairs_RoomTemperature "Room Temperature" { channel="neohub:neostat:myhubname:myupstairs:roomTemperature" }
Number:Temperature Upstairs_TargetTemperature "Target Temperature" { channel="neohub:neostat:myhubname:myupstairs:targetTemperature" }
Number:Temperature Upstairs_FloorTemperature "Floor Temperature" { channel="neohub:neostat:myhubname:myupstairs:floorTemperature" }
String Upstairs_ThermostatOutputState "Heating State" { channel="neohub:neostat:myhubname:myupstairs:thermostatOutputState" }
Switch Upstairs_OccupancyModePresent "Occupancy Mode Present" { channel="neohub:neostat:myhubname:myupstairs:myupstairs:occupancyModePresent" }

Switch Downstairs_PlugAutoMode "Plug Auto Mode" { channel="neohub:neoplug:myhubname:mydownstairs:plugAutoMode" }
Switch Downstairs_PlugOutputState "Plug Output State" { channel="neohub:neoplug:myhubname:mydownstairs:plugOutputState" }

Contact Window_Contact_State "Window Contact State" { channel="neohub:neocontact:myhubname:mycontact:contactState" }
Switch Window_Contact_Battery_Low "Window Contact Battery Low" { channel="neohub:neocontact:myhubname:mycontact:batteryLowAlarm" }

Number:Temperature Kitchen_Temperature "Kitchen Temperature" { channel="neohub:neotemperaturesensor:myhubname:mysensor:sensorTemperature" }
```

### `demo.sitemap` File

```perl
sitemap neohub label="Heatmiser NeoHub"
{
    Frame label="Thermostat" {
        Text      item=Upstairs_RoomTemperature
        Setpoint  item=Upstairs_TargetTemperature minValue=15 maxValue=30 step=1
        Text      item=Upstairs_ThermostatOutputState
        Switch    item=Upstairs_OccupancyModePresent
        Text      item=Upstairs_FloorTemperature
    }

    Frame label="Plug" {
        Switch item=Downstairs_PlugOutputState
        Switch item=Downstairs_PlugAutoMode
    }

    Frame label="Contact" {
        Contact item=Window_Contact_State
        Switch item=Window_Contact_Battery_Low
    }

    Frame label="Sensor" {
        Text item=Kitchen_Temperature
    }
}
```
