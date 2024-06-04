# LinkTap Binding

This binding is for [LinkTap](https://www.link-tap.com/) devices.

**This is for communication over a local area network, that prevents direct access to your gateway / openHAB instance from the internet. E.g. behind a router**

The method of interaction this binding supports is:

**Program and execution of the watering plan within the application**

The currently supported capabilities include where supported by the gateway / device:

- Time syncrhonisation to openHAB
- Child lock controls
- Monitoring and dismissal for device alarms (Water Cut, etc.)
- Monitoring of sensor states (Battery, Zigbee Signal, Flow Meters Statistics, etc.)
- Enable watering based on time duration / volume limits
- Shutdown of active watering

## Requirements

A LinkTap gateway device, in order for openHAB to connect to the system, as a bridge.

## Connection options

LinkTap supports MQTT and a direct interaction via HTTP. 
This binding directly interact's with LinkTap's bridges using the Local HTTP API (HTTP).
The binding connects to the bridge's directly, and the Gateway is configured automatically to push updates to openHAB if
it has a HTTP configured server. (Note HTTPS is not supported).

Should the Gateway device's not be able to connect to the binding it automatically falls-back to a polling
implementation (15 second cycle). The gateway supports 1 Local HTTP API, for an ideal behaviour the Gateway should be able to
connect to OpenHab on an HTTP port from its IP, and only a single OpenHab instance should be connected to a Gateway.

It is recommended that you use **static IP's** for this binding, **for both openHAB and the Gateway device(s)**.

## Supported Things

This binding supports the follow thing types:

| Thing          | Thing Type | Thing Type UID | Discovery          | Description                              |
|----------------|------------|----------------|--------------------|------------------------------------------|
| Bridge         | Bridge     | linkTapBridge  | Manual / Automatic | A connection to a LinkTap Gateway device |
| LinkTap Device | Thing      | linkTapDevice  | Automatic          | A end device such as the Q1              |

**NOTE** This binding was developed and tested using a GW-02 gateway with a Q1 device. 

## Discovery

### Gateways

If mDNS has been enabled on the Gateway device via it's webpage, then the gateway(s) will be discovered, and appear in the inbox
when a manual scan is run when adding a LinkTap Gateway. It is however recommended to use **static IP addresses** and add the
gateways directly using the IP address.

### Devices

Once connected to a LinkTap gateway, the binding will listen for updates of new devices and add them, to the inbox.
If the gateway cannot publish to openHAB, then the gateway is checked every 2 minutes for new devices, and they are added to the inbox when discovered.

## Binding Configuration

### Bridge configuration parameters

| Name       | Type   | Description                                                                       | Recommended Values | Required | Advanced |
|------------|--------|-----------------------------------------------------------------------------------|--------------------|----------|----------|
| host       | String | The hostname / IP address of the gateway device                                   |                    | Yes      | No       |
| username   | String | The username if set for the gateway device                                        |                    | No       | No       |
| password   | String | The password if set for the gateway device                                        |                    | No       | No       |
| enableMDNS | Switch | On connection whether the mDNS responder should be enabled on the gateway device  | ON                 | No       | Yes      |

**NOTE** When enableMDNS is enabled, upon connection to the gateway option "Enable mDNS responder" is switched on 

### LinkTap Device configuration parameters

It is recommended to use the Device Id, for locating devices. This can be found in the LinkTap mobile application under 
Settings->TapLinker / ValveLinker, e.g.

- ValueLinker_1 (D71BC52F004B1200_1-xxxx)
  - has Device Id "ValveLinker_1"
  - has Device Name D71BC52F004B1200_1

| Name         | Type   | Description                                                           | Recommended Values | Required | Advanced |
|--------------|--------|-----------------------------------------------------------------------|--------------------|----------|----------|
| deviceId     | String | The Device Id for the device under the gateway                        |                    | Yes      | No       |
| deviceName   | String | The name allocated to the device by the app. (Must be unique if used) |                    | Yes      | No       |
| enableAlerts | Switch | On connection whether the device should be configured to send alerts  | ON                 | No       | Yes      |

**NOTE**

- a **deviceId** or a **deviceName must be provided** to communicate with the device
- **enableAlerts** allows the binding to **receive updates of Water Cut, and other alerts** conditions are detected for a LinkTap device.

## Thing Configuration

_Describe what is needed to manually configure a thing, either through the UI or via a thing-file._
_This should be mainly about its mandatory and optional configuration parameters._

_Note that it is planned to generate some part of this based on the XML files within ```src/main/resources/OH-INF/thing``` of your binding._

### `sample` Thing Configuration

| Name            | Type    | Description                           | Default | Required | Advanced |
|-----------------|---------|---------------------------------------|---------|----------|----------|
| host            | text    | Hostname or IP address of the device  | N/A     | yes      | no       |
| password        | text    | Password to access the device         | N/A     | yes      | no       |
| refreshInterval | integer | Interval the device is polled in sec. | 600     | no       | yes      |

## Channels

There are 3 different Area of channels:

- RO Data (Read Only Data)
  - These represent data published by the device
- Alerts
  - These are switches that are set to ON by the device when an alert condition is detected, such as a Water Cut.
  - The alert can be dismissed by setting the switch to OFF
- WR Data (Read Write Data)
  - Provides the ability to read data
  - Provides the ability to set a relevant state to the data
- W Data
  - Provides parameter values for the named action, is stored within openHAB is not read from the device
    - E.g. Start Immediate Watering
      - Can be limited by a time duration - ohDurLimit
      - If a flow meter is attached can be limited by a volume limit - ohVolLimit


| Name             | Type                      | Description                                                               | Representation | Read/Write | Write Action                                                                                                                 |
|------------------|---------------------------|---------------------------------------------------------------------------|----------------|------------|------------------------------------------------------------------------------------------------------------------------------|
| water-cut        | Switch                    | Water cut-off alert                                                       | Alert          | Write      | Dismiss alert                                                                                                                |
| shutdown-failure | Switch                    | The device has failed to close the valve                                  | Alert          | Write      | Dismiss alert                                                                                                                |
| high-flow        | Switch                    | Unusually high flow rate detected alert                                   | Alert          | Write      | Dismiss alert                                                                                                                |
| low-flow         | Switch                    | Unusually low flow rate detected alert                                    | Alert          | Write      | Dismiss alert                                                                                                                |
| fall-status      | Switch                    | The device has fallen                                                     | Alert          | Write      | Dismiss alert                                                                                                                |
| flm-linked       | Switch                    | The device has a included flow meter                                      | RO Data        | Read       |                                                                                                                              |
| rf-linked        | Switch                    | Is the device RF linked                                                   | RO Data        | Read       |                                                                                                                              |
| signal           | Number:Dimensionless      | Reception Signal Strength                                                 | RO Data        | Read       |                                                                                                                              |
| battery          | Number:Dimensionless      | Battery Remaining Level                                                   | RO Data        | Read       |                                                                                                                              |
| flow-rate        | Number:VolumetricFlowRate | Current water flow rate                                                   | RO Data        | Read       |                                                                                                                              |
| volume           | Number:Volume             | Accumulated volume of current watering cycle                              | RO Data        | Read       |                                                                                                                              | 
| eco-final        | Switch                    | In ECO mode this is true when the final ON watering on segment is running | RO Data        | Read       |                                                                                                                              |
| remaining        | Number:Time               | Remaining duration of the current watering cycle                          | RO Data        | Read       |                                                                                                                              |
| duration         | Number:Time               | Total duration of current watering cycle                                  | RO Data        | Read       |                                                                                                                              |
| active-watering  | Switch                    | Active watering status                                                    | RW Data        | Write      | True - Start immediate watering, False - Stops the current watering process, the next planned watering will run as scheduled |
| manual-watering  | Switch                    | Manual watering mode status                                               | RW Data        | Write      | False - Stops the current watering process, the next planned watering will run as scheduled                                  |
| child-lock       | Text                      | The child lock mode                                                       | RW Data        | Write      | Unlocked - Button enabled, Partially locked -> 3 second push required, Completely locked -> Button disabled                  |
| oh-dur-limit     | Number:Time               | Max duration allowed for the immediate watering                           | W Data         | Write      | Max Time duration for "Start immediate watering"                                                                             |
| oh-vol-limit     | Number:Volume             | Max Volume limit for immediate watering                                   | W Data         | Write      | Max Volume for "Start immediate watering"                                                                                    |
| water-skip-ts    | DateTime                  | Time when watering was skipped                                            | RO Data        | Read       |                                                                                                                              |
| water-skip-prev  | Number:Length             | Previous rainfall calculated when watering was skipped                    | RO Data        | Read       |                                                                                                                              |
| water-skip-next  | Number:Length             | Future rainfall calculated when watering was skipped                      | RO Data        | Read       |                                                                                                                              |

## Full Example

_Provide a full usage example based on textual configuration files._
_*.things, *.items examples are mandatory as textual configuration is well used by many users._
_*.sitemap examples are optional._

### Thing Configuration

- **Gateway Model**: GW_02
- **Device Model**: Q1

```java
Bridge linktap:bridge:home "LinkTap GW02" [ host="192.168.0.21", enableMDNS=true ] {
  Thing device TapValve1 "Outdoor Tap 1"  [ id="D71BC52E985B1200_1", name="ValveLinker_1", enableAlerts=true ]
  Thing device TapValve2 "Outdoor Tap 2"  [ id="D71BC52E985B1200_2", name="ValveLinker_2", enableAlerts=true ]
  Thing device TapValve3 "Outdoor Tap 3"  [ id="D71BC52E985B1200_3", name="ValveLinker_3", enableAlerts=true ]
  Thing device TapValve4 "Outdoor Tap 4"  [ id="D71BC52E985B1200_4", name="ValveLinker_4", enableAlerts=true ]
}
```

### Item Configuration

```java
Number:Dimensionless   	   Tap1BatteryLevel       "Tap 1 - Battery Level"                 <batterylevel>     ["Point"] { channel="linktap:device:home:tapValve1:battery",unit="%%" }
Number:Dimensionless   	   Tap1SignalLevel        "Tap 1 - Signal Level"                  <qualityofservice> ["Point"] { channel="linktap:device:home:tapValve1:signal",unit="%%" }
Switch                     Tap1RfLinked           "Tap 1 - RF Linked"                     <switch>           ["Point"] { channel="linktap:device:home:tapValve1:rf-linked"}
Switch                     Tap1FlmLinked          "Tap 1 - FLM Linked"                    <switch>           ["Point"] { channel="linktap:device:home:tapValve1:flm-linked"}
Switch                     Tap1WaterCutAlert      "Tap 1 - Water Cut Alert"               <alarm>            ["Point"] { channel="linktap:device:home:tapValve1:water-cut" }
Switch                     Tap1WaterFallAlert     "Tap 1 - Fallen Alert"                  <alarm>            ["Point"] { channel="linktap:device:home:tapValve1:fall-status" }
Switch                     Tap1WaterValveAlert    "Tap 1 - Shutdown Failure Alert"        <alarm>            ["Point"] { channel="linktap:device:home:tapValve1:shutdown-failure" }
Switch                     Tap1WaterLowFlowAlert  "Tap 1 - Low Flow Alert"                <alarm>            ["Point"] { channel="linktap:device:home:tapValve1:low-flow" }
Switch                     Tap1WaterHighFlowAlert "Tap 1 - High Flow Alert"               <alarm>            ["Point"] { channel="linktap:device:home:tapValve1:high-flow" }
String                     Tap1ChildLockMode      "Tap 1 - Child Lock Mode"               <lock>             ["Point"] { channel="linktap:device:home:tapValve1:child-lock" }
Number:VolumetricFlowRate  Tap1FlowRate           "Tap 1 - Flow Rate"                     <flow>             ["Point"] { channel="linktap:device:home:tapValve1:flow-rate",unit="l/min" }
Number:Volume              Tap1WateringVolume     "Tap 1 - Watering Volume"               <water>            ["Point"] { channel="linktap:device:home:tapValve1:volume",unit="l" }
Switch                     Tap1FinalEcoSegment    "Tap 1 - Final ECO Segment"             <switch>           ["Point"] { channel="linktap:device:home:tapValve1:eco-final" }
Switch                     Tap1Watering           "Tap 1 - Watering"                      <water>            ["Point"] { channel="linktap:device:home:tapValve1:active-watering" }
Switch                     Tap1ManualWatering     "Tap 1 - Manual Watering"               <water>            ["Point"] { channel="linktap:device:home:tapValve1:manual-watering" }
String                     Tap1WateringkMode      "Tap 1 - Watering Mode"                 <time>             ["Point"] { channel="linktap:device:home:tapValve1:mode" }
Number:Time                Tap1WateringCycleDur   "Tap 1 - Current Cycle Duration Limit"  <time>             ["Point"] { channel="linktap:device:home:tapValve1:duration",unit="s" }
Number:Volume              Tap1WateringCycleVol   "Tap 1 - Current Cycle Volume Limit"    <water>            ["Point"] { channel="linktap:device:home:tapValve1:vol-limit",unit="l" }
```

### Sitemap Configuration

```perl
Optional Sitemap configuration goes here.
Remove this section, if not needed.
```
