# LinkTap Binding

This binding is for [Link-Tap](https://www.link-tap.com/) devices.

**This is for communication over a local area network, that prevents direct access to your gateway / openHAB instance from the internet.
E.g. behind a router**

The method of interaction this binding supports is:

## Program and execution of the watering plan within the application

The currently supported capabilities include where supported by the gateway / device:

- Time synchronisation to openHAB
- Child lock controls
- Monitoring and dismissal for device alarms (Water Cut, etc.)
- Monitoring of sensor states (Battery, Zigbee Signal, Flow Meters Statistics, etc.)
- Enable watering based on time duration / volume limits
- Shutdown of active watering

## Requirements

A LinkTap gateway device such as the GW_02, in order for openHAB to connect to the system, as a gateway.
Older GW_01 gateway devices have not been tested but should work with a static IP setup.

The recommended minimum version of the firmware is:

- **GW_01** is to start with **at least S609**
- **GW_02** is to start with **at least G609**

## Connection Options

LinkTap supports MQTT and a direct interaction via HTTP.
This binding directly interacts with LinkTap's gateway devices using the Local HTTP API (HTTP).
The binding connects to the gateway's directly, and the Gateway is configured automatically to push updates to openHAB if it has a HTTP configured server.
(Note HTTPS is not supported).

Should the Gateway device's not be able to connect to the binding it automatically falls-back to a polling implementation (15 second cycle).
The gateway supports 1 Local HTTP API, for an ideal behavior the Gateway should be able to connect to openHAB on a HTTP port by its IP, and only a single openHAB instance should be connected to a Gateway.
It is recommended that you use **static IP's** for this binding, **for both openHAB and the Gateway device(s)** regardless of the gateway's model.

If dynamic IPs are used for the gateway, the mDNS address is recommended to be used.
This can be found when running a manual scan, for LinkTap Gateways.
This will remove any DNS caching related issues, depending on your setup.

## Supported Things

This binding supports the follow thing types:

| Thing Type | Thing Type UID | Discovery          | Description                                                      |
|------------|----------------|--------------------|------------------------------------------------------------------|
| Bridge     | gateway        | Manual / Automatic | A connection to a LinkTap Gateway device                         |
| Thing      | device         | Automatic          | A end device such as one of the four controlled values on the Q1 |

**NOTE** This binding was developed and tested using a GW-02 gateway with a Q1 device.

## Discovery

### Gateways

If mDNS has been enabled on the Gateway device via it's webpage, then the gateway(s) will be discovered, and appear in the inbox when a manual scan is run when adding a LinkTap Gateway.
It is however recommended to use **static IP addresses** and add the gateways directly using the IP address.

### Devices

Once connected to a LinkTap gateway, the binding will listen for updates of new devices and add them, to the inbox.
If the gateway cannot publish to openHAB, then the gateway is checked every 2 minutes for new devices, and they are added to the inbox when discovered.

## Binding Configuration

### Gateway Configuration

| Name                   | Type    | Description                                                                                                                                                             | Recommended Values | Required | Advanced |
|------------------------|---------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------|----------|----------|
| host                   | text    | The hostname / IP address of the gateway device                                                                                                                         |                    | Yes      | No       |
| username               | text    | The username if set for the gateway device                                                                                                                              |                    | No       | No       |
| password               | text    | The password if set for the gateway device                                                                                                                              |                    | No       | No       |
| enableMDNS             | boolean | On connection whether the mDNS responder should be enabled on the gateway device                                                                                        | true               | No       | Yes      |
| enforceProtocolLimits  | boolean | If true data outside of the allowed ranges against the protocol will be logged and not sent                                                                             | true               | No       | Yes      |
| enableJSONComms        | boolean | false by default for backwards compatibility, if using up to date firmware with no other local network applications set this to true, for more efficient communications | true               | No       | Yes      |
| gatewayResponseTimeout | integer | For slow or heavily loaded systems this may need increasing, if communication errors are seen (seconds allowed for responses from the gateway)                          | 3                  | No       | Yes      |

**NOTE** When enableMDNS is enabled, upon connection to the gateway option "Enable mDNS responder" is switched on.

### Device Configuration

| Name         | Type    | Description                                                           | Recommended Values | Required | Advanced |
|--------------|---------|-----------------------------------------------------------------------|--------------------|----------|----------|
| deviceId     | String  | The Device Id for the device under the gateway                        |                    | No (A,B) | No       |
| deviceName   | String  | The name allocated to the device by the app. (Must be unique if used) |                    | No (B)   | No       |
| enableAlerts | boolean | On connection whether the device should be configured to send alerts  | true               | No       | Yes      |

**NOTE:**

(A) It is recommended to use the Device Id, for locating devices.
This can be found in the LinkTap mobile application under Settings->TapLinker / ValveLinker, e.g.

- ValueLinker_1 (D71BC52F004B1200_1-xxxx)
  - has Device Id "ValveLinker_1"
  - has Device Name D71BC52F004B1200_1

(B) Either a **deviceId or deviceName is required** for the device to be located and used.

## Channels

| Name              | Type                      | Description                                                                         | Representation | Write Action                                                                                                                 | Note                                                                                            |
|-------------------|---------------------------|-------------------------------------------------------------------------------------|----------------|------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------|
| water-cut         | Switch                    | Water cut-off alert                                                                 | Alert          | Dismiss alert                                                                                                                |                                                                                                 |
| shutdown-failure  | Switch                    | The device has failed to close the valve                                            | Alert          | Dismiss alert                                                                                                                |                                                                                                 |
| high-flow         | Switch                    | Unusually high flow rate detected alert                                             | Alert          | Dismiss alert                                                                                                                |                                                                                                 |
| low-flow          | Switch                    | Unusually low flow rate detected alert                                              | Alert          | Dismiss alert                                                                                                                |                                                                                                 |
| fall-status       | Switch                    | The device has fallen                                                               | Alert          | Dismiss alert                                                                                                                |                                                                                                 |
| mode              | Text                      | The current watering plan mode                                                      | R              |                                                                                                                              |                                                                                                 |
| flm-linked        | Switch                    | The device has a included flow meter                                                | R              |                                                                                                                              |                                                                                                 |
| rf-linked         | Switch                    | Is the device RF linked                                                             | R              |                                                                                                                              |                                                                                                 |
| signal            | Number:Dimensionless      | Reception Signal Strength                                                           | R              |                                                                                                                              |                                                                                                 |
| battery           | Number:Dimensionless      | Battery Remaining Level                                                             | R              |                                                                                                                              |                                                                                                 |
| flow-rate         | Number:VolumetricFlowRate | Current water flow rate                                                             | R              |                                                                                                                              |                                                                                                 |
| volume            | Number:Volume             | Accumulated volume of current watering cycle                                        | R              |                                                                                                                              |                                                                                                 |
| eco-final         | Switch                    | In ECO mode this is true when the final ON watering on segment is running           | R              |                                                                                                                              |                                                                                                 |
| remaining         | Number:Time               | Remaining duration of the current watering cycle                                    | R              |                                                                                                                              |                                                                                                 |
| duration          | Number:Time               | Total duration of current watering cycle                                            | R              |                                                                                                                              |                                                                                                 |
| watering          | Switch                    | Active watering status                                                              | RW             | True - Start immediate watering, False - Stops the current watering process, the next planned watering will run as scheduled |                                                                                                 |
| manual-watering   | Switch                    | Manual watering mode status                                                         | R              |                                                                                                                              |                                                                                                 |
| child-lock        | Text                      | The child lock mode                                                                 | RW             | Unlocked - Button enabled, Partially locked -> 3 second push required, Completely locked -> Button disabled                  | If the GW has internet connectivity settings will be reset when it sync's to TapLink's servers. |
| oh-dur-limit      | Number:Time               | Max duration allowed for the immediate watering                                     | W              | Max Time duration for "Start immediate watering"                                                                             |                                                                                                 |
| oh-vol-limit      | Number:Volume             | Max Volume limit for immediate watering                                             | W              | Max Volume for "Start immediate watering"                                                                                    |                                                                                                 |
| plan-pause-enable | Switch                    | When ON will pause the current watering plan for an hour every 55 minutes           | RW             | Pause current watering plan every 55 mins for one hour                                                                       | This disables the TapLink Watering Plan from being run, it is not reflected in the mobile app.  |
| plan-resume-time  | DateTime                  | Displays when the last pause issued will expiry, resuming the current watering plan | R              |                                                                                                                              |                                                                                                 |
| watering-plan-id  | Text                      | Displays the current watering plan id                                               | R              |                                                                                                                              |                                                                                                 |

**NOTE:**
There are 4 different areas of channels:

- R (Read Only Data)
  - These represent data published by the device
- Alerts
  - These are switches that are set to ON by the device when an alert condition is detected, such as a Water Cut.
  - The alert can be dismissed by setting the switch to OFF
- RW (Read Write Data)
  - Provides the ability to read data
  - Provides the ability to set a relevant state to the data
- W Data
  - Provides parameter values for the named action, it is stored within openHAB is not read from the device
    - E.g. Start Immediate Watering
      - Can be limited by a time duration - ohDurLimit
      - If a flow meter is attached can be limited by a volume limit - ohVolLimit

## Full Example

### Thing Configuration

- **Gateway Model**: GW_02
- **Device Model**: Q1

```java
Bridge linktap:gateway:home "LinkTap GW02" [ host="192.168.0.21", enableMDNS=true, enableJSONComms=false, enforceProtocolLimits=true, gatewayResponseTimeout=3 ] {
  Thing device TapValve1 "Outdoor Tap 1"  [ id="D71BC52E985B1200_1", name="ValveLinker_1", enableAlerts=true ]
  Thing device TapValve2 "Outdoor Tap 2"  [ id="D71BC52E985B1200_2", name="ValveLinker_2", enableAlerts=true ]
  Thing device TapValve3 "Outdoor Tap 3"  [ id="D71BC52E985B1200_3", name="ValveLinker_3", enableAlerts=true ]
  Thing device TapValve4 "Outdoor Tap 4"  [ id="D71BC52E985B1200_4", name="ValveLinker_4", enableAlerts=true ]
}
```

### Item Configuration

```java
Number:Dimensionless       Tap1BatteryLevel       "Tap 1 - Battery Level"                 <batterylevel>     ["Point"] { channel="linktap:device:home:tapValve1:battery",unit="%%" }
Number:Dimensionless       Tap1SignalLevel        "Tap 1 - Signal Level"                  <qualityofservice> ["Point"] { channel="linktap:device:home:tapValve1:signal",unit="%%" }
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
Switch                     Tap1Watering           "Tap 1 - Watering"                      <water>            ["Point"] { channel="linktap:device:home:tapValve1:watering" }
Switch                     Tap1ManualWatering     "Tap 1 - Manual Watering"               <water>            ["Point"] { channel="linktap:device:home:tapValve1:manual-watering" }
String                     Tap1WateringMode       "Tap 1 - Watering Mode"                 <time>             ["Point"] { channel="linktap:device:home:tapValve1:mode" }
Number:Time                Tap1TimeDuration       "Tap 1 - Current Cycle Duration"        <time>             ["Point"] { channel="linktap:device:home:tapValve1:duration",unit="s" }
Number:Time                Tap1TimeRemain         "Tap 1 - Current Cycle Remaining"       <time>             ["Point"] { channel="linktap:device:home:tapValve1:remaining",unit="s" }
Number:Time                Tap1WateringCycleDur   "Tap 1 - Current Cycle Duration Limit"  <time>             ["Point"] { channel="linktap:device:home:tapValve1:dur-limit",unit="s" }
Number:Volume              Tap1WateringCycleVol   "Tap 1 - Current Cycle Volume Limit"    <water>            ["Point"] { channel="linktap:device:home:tapValve1:vol-limit",unit="l" }
Number:Time                Tap1ManTimeLimit       "Tap 1 - Instant On Duration Limit"     <time>             ["Point"] { channel="linktap:device:home:tapValve1:oh-dur-limit",unit="s" }
Number:Volume              Tap1ManVolLimit        "Tap 1 - Instant On Volume Limit"       <water>            ["Point"] { channel="linktap:device:home:tapValve1:oh-vol-limit",unit="l" }
Switch                     Tap1PauseWateringPlan  "Tap 1 - Pause Current Plan"            <time>             ["Point"] { channel="linktap:device:home:tapValve1:plan-pause-enable" }
DateTime                   Tap1PauseExpiry        "Tap 1 - Pause Expiry"                  <calendar>         ["Point"] { channel="linktap:device:home:tapValve1:plan-resume-time" }
String                     Tap1WateringPlanId     "Tap 1 - Watering Plan Id"              <calendar>         ["Point"] { channel="linktap:device:home:tapValve1:watering-plan-id" }
```

### Sitemap Configuration

```perl
Text item=Tap1BatteryLevel
Switch item=Tap1WaterCutAlert
Switch item=Tap1WaterFallAlert
Switch item=Tap1WaterValveAlert
Switch item=Tap1WaterLowFlowAlert
Switch item=Tap1WaterHighFlowAlert
Text item=Tap1ChildLockMode
Text item=Tap1FlowRate label="Tap 1 - Flow Rate [%.0f %unit%]"
Text item=Tap1WateringVolume label="Tap 1 - Watering Volume [%.0f %unit%]"
Text item=Tap1FinalEcoSegment label="Tap 1 - Final Segment [%s]"
Switch item=Tap1Watering
Switch item=Tap1ManualWatering
Text item=Tap1WateringMode
Text item=Tap1TimeDuration label="Tap 1 - Time Duration [%.0f %unit%]"
Text item=Tap1TimeRemain label="Tap 1 - Time Remaining [%.0f %unit%]"
Text item=Tap1WateringCycleDur label="Tap 1 - Cycle Duration [%.0f %unit%]"
Text item=Tap1WateringCycleVol label="Tap 1 - Cycle Volume [%.0f %unit%]"
Slider item=Tap1ManTimeLimit minValue=3 maxValue=86340 step=30 releaseOnly label="Tap 1 - Instant On Time Limit [%.0f %unit%]"
Slider item=Tap1ManVolLimit minValue=1 maxValue=5000 step=1 releaseOnly label="Tap 1 - Instant On Volume Limit [%.0f %unit%]"
Switch item=Tap1PauseWateringPlan
Text item=Tap1PauseExpiry
Text item=Tap1WateringPlanId
```

#### Other Models

Please check the [Link-Tap](https://www.link-tap.com/) website.
Presently at this location, the [Link-Tap wireless water timer product chart](https://www.link-tap.com/#!/wireless-water-timer) shows the features available for the products.
If a product such as the G1S is used, it will not support flow based commands or readings.
In this case exclude the volume based Items and Sitemap entries.

Note in cases such as the G1S where flow meters are not included, or are disconnected, the instant watering will be based solely on the time arguments.
Flow data would as expected not be updated.

## Thanks To

A note goes out to Bill at Link-Tap who has been extremely responsive in providing specifications, and quick fixes for a single issue noticed, as well as answering many questions about the behaviours of untested devices.
