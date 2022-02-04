# MCD Binding

This binding allows you to send sensor events from your openHAB environment to the cloud application Managing Care Digital (MCD) by [C&S Computer und Software GmbH](https://www.managingcare.de/). 


## Supported Things

There are two supported things: **MCD Bridge** and **MCD Sensor Thing**. 


## Thing Configuration

This section shows the configuration parameters of both supported things.

### MCD Bridge

The MCD Bridge (`mcdBridge`) needs to be configured with your valid C&S MCD / sync API credentials. 

| parameter | description                        |
|-----------|------------------------------------|
| email     | Email of account                   |
| password  | valid password for the given email |

### MCD Sensor Thing

Each sensor thing (`mcdSensor`) needs to be configured with the identical serial number, that is assigned to this sensor in MCD. 

| parameter      | description                        |
|----------------|------------------------------------|
| parent bridge  | parent MCD bridge is required      |
| serial number  | serial number of the sensor in MCD |

## Channels

The `mcdBridge` supports the following channel:

| channel     | type   | description                                   |
|-------------|--------|-----------------------------------------------|
| loginStatus | Switch | login status of the bridge (ON for logged in) |

The `mcdSensor` thing supports the following channels. Please note that these channels (except from `lastValue`) should only be used 
to send data. To see the sensors' events, please visit [Managing Care Digital](https://cundsdokumentation.de/).

| channel        | type   | description                                                                                                              |
|----------------|--------|--------------------------------------------------------------------------------------------------------------------------|
| lastValue      | String | READONLY; shows the last event that was sent                                                                             |
| ---            | ---    | channels, that send two different states:                                                                                |
| bedStatus      | Switch | set OFF to send bed exit, ON to send bed entry                                                                           |
| sitStatus      | Switch | set OFF to send sit down, ON to send stand up                                                                            |
| openShut       | Switch | set OFF to send shut, ON to send open                                                                                    |
| light          | Switch | send OFF or ON                                                                                                           |
| presence       | Switch | set OFF to send room exit, ON to send room entry                                                                         |
| ---            | ---    | channels, that send only one state when set ON:                                                                          |
| fall           | Switch | set ON to send fall event, will be reset to OFF automatically after sending                                              |
| changePosition | Switch | set ON to send event for position change in bed, will be reset to OFF automatically after sending                        |
| batteryState   | Switch | set ON to send warning for an empty battery, will be reset to OFF automatically after sending                            |
| inactivity     | Switch | set ON to send event for long inactivity, will be reset to OFF automatically after sending                               |
| alarm          | Switch | set ON to send an alarm, will be reset to OFF automatically after sending                                                |
| activity       | Switch | set ON to send event for detected activity, will be reset to OFF automatically after sending                             |
| urine          | Switch | set ON to send event for capacity limit in smart incontinence material, will be reset to OFF automatically after sending |
| gas            | Switch | set ON to send event for detected gas, will be reset to OFF automatically after sending                                  |
| removedSensor  | Switch | set ON to send event for sensor removal, will be reset to OFF automatically after sending                                |
| inactivityRoom | Switch | set ON to send event for long inactivity in a room, will be reset to OFF automatically after sending                     |
| smokeAlarm     | Switch | set ON to send event for smoke detection, will be reset to OFF automatically after sending                               |
| heat           | Switch | set ON to send event for detected heat, will be reset to OFF automatically after sending                                 |
| cold           | Switch | set ON to send event for detected cold, will be reset to OFF automatically after sending                                 |
| alarmAir       | Switch | set ON to send event for bad air quality, will be reset to OFF automatically after sending                               |


## Full Example

Here is an example for the textual configuration. You can of course use the Administration section of the GUI as well.

demo.things:

```
Bridge mcd:mcdBridge:exampleBridge [userEmail="your.email@examle.com", userPassword="your.password"]{
    Thing mcd:mcdSensor:examlpeSensor [serialNumber="123"]
    Thing mcd:mcdSensor:secondExamlpeSensor [serialNumber="456"]
}
```
demo.items:
```
Switch loginStatus "Login Status" {channel="mcd:mcdBridge:exampleBridge:loginStatus"}
String lastValue "Last Value" {channel="mcd:mcdSensor:examlpeSensor:lastValue"}
Switch sitStatus "Sit Status" {channel="mcd:mcdSensor:examlpeSensor:sitStatus"}
```
demo.sitemap:
```
Switch item=loginStatus
Switch item=sitStatus
Text item=lastValue
```

## Further information

This binding was created as part of our recent and current research activities. For further information about our projects please visit our [Website](https://www.managingcare.de/) or write an email to: institut(at)cs-ag.de. 

[//]: # (## Discovery)

[//]: # ()
[//]: # (There is no auto discovery available. )

[//]: # (## Binding Configuration)

[//]: # ()
[//]: # (_If your binding requires or supports general configuration settings, please create a folder ```cfg``` and place the configuration file ```<bindingId>.cfg``` inside it. In this section, you should link to this file and provide some information about the options. The file could e.g. look like:_)

[//]: # ()
[//]: # (```)

[//]: # (# Configuration for the MCD Binding)

[//]: # (#)

[//]: # (# Default secret key for the pairing of the MCD Thing.)

[//]: # (# It has to be between 10-40 &#40;alphanumeric&#41; characters.)

[//]: # (# This may be changed by the user for security reasons.)

[//]: # (secret=openHABSecret)

[//]: # (```)

[//]: # ()
[//]: # (_Note that it is planned to generate some part of this based on the information that is available within ```src/main/resources/OH-INF/binding``` of your binding._)

[//]: # ()
[//]: # (_If your binding does not offer any generic configurations, you can remove this section completely._)
