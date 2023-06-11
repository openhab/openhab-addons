# EchonetLite Binding

This binding supports devices that make use of the Echonet Lite specification (https://echonet.jp/spec_v113_lite_en/).

## Supported Things

* Mitsubishi Electric MAC-568IF-E Wi-Fi interface (common on most Mitsubishi Heat Pumps).

## Discovery

Discovery is supported using UDP Multicast.
When running over Wi-Fi it is advisable to run openHAB on the same network as the Echonet Lite devices.
Multicast traffic doesn't easily route over multiple networks and will often be dropped.
Discovery is handled via the Echonet Lite bridge, which contains the configuration of the multicast address used for discovery and asynchronous device notifications along with the port.
It is unlikely that this configuration will require changing.

## Bridge Configuration

The bridge configuration defaults should be applicable in most scenarios.
If device discovery is not working, this is most likely caused by the inability to receive multicast traffic from the device nodes.

* __port__: Port used for messaging both to and from device nodes, defaults to 3610.
* __multicastAddress__: Multicast address used to discover device nodes and to receive asynchronous notifications from devices.

## Thing Configuration

* __hostname__: Hostname or IP address of the device node.
* __port__: Port used to communicate with the device. 
* __groupCode__: Group code as specified in "APPENDIX Detailed Requirements for ECHONET Device objects" (https://echonet.jp/spec_object_rp1_en/).
For Air Conditioners the value is '1'.
* __classCode__: Class code for the device, see __groupCode__ for reference information.
The value for Home Air Conditioners is '48' (0x30).
* __instance__: Instance identifier if multiple instances are running on the same IP address.
Typically, this value will be '1'.
* __pollIntervalMs__: Interval between polls of the device for its current status.
If multicast is not working this will determine the latency at which changes made directly on the device will be propagated back to openHAB, default is 30 000ms.
* __retryTimeoutMs__: Length of time the bridge will wait before resubmitting a request, default is 2 000ms.

Because the binding uses UDP, packets can be lost on the network, so retries are necessary.
Testing has shown that 2 000ms is a reasonable default that allows for timely retries without rejecting slow, but legitimate responses.

## Channels

Channels are derived from the Echonet Lite specification and vary from device to device depending on capabilities.
The full set of potential channels is available from "APPENDIX Detailed Requirements for ECHONET Device objects" (https://echonet.jp/spec_object_rp1_en/)

The channels currently implemented are:

| Channel                            | Data Type | Description                                                             |
|------------------------------------|-----------|-------------------------------------------------------------------------|
| operationStatus                    | Switch    | Switch On/Off the device                                                |
| installationLocation               | String    | Installation location (option)                                          |
| standardVersionInformation         | String    | Standard Version Information                                            |
| identificationNumber               | String    | Unique id for device (used by auto discovery for the thingId)           |
| manufacturerFaultCode              | String    | Manufacturer Fault Code                                                 |
| faultStatus                        | Switch    | Fault Status                                                            |
| faultDescription                   | String    | Fault Description                                                       |
| manufacturerCode                   | String    | Manufacturer Code                                                       |
| businessFacilityCode               | String    | Business Facility Code                                                  |
| powerSavingOperationSetting        | Switch    | Controls whether the unit is in power saving operation or not           |
| cumulativeOperatingTime            | Number    | Cumulative Operating Time                                               |
| airFlowRate                        | String    | Air Flow Rate                                                           |
| automaticControlOfAirFlowDirection | String    | The type of automatic control applied to the air flow direction, if any |
| automaticSwingOfAirFlow            | String    | Automatic Swing Of Air Flow                                             |
| airFlowDirectionVertical           | String    | Air Flow Direction Vertical                                             |
| airFlowDirectionHorizontal         | String    | Air Flow Direction Horizontal                                           |
| operationMode                      | String    | The current mode for the Home AC unit (heating, cooling, etc.)          |
| setTemperature                     | Number    | Desired target room temperature                                         |
| measuredRoomTemperature            | Number    | Measured Room Temperature                                               |
| measuredOutdoorTemperature         | Number    | Measured Outdoor Temperature                                            |

## Full Example


### Things

```
Bridge echonetlite:bridge:1 [port="3610", multicastAddress="224.0.23.0"] {
    Thing device HeatPump_Bedroom1 "HeatPump Bedroom 1" @ "Bedroom 1" [hostname="192.168.0.55", port="3610", groupCode="1", classCode="48", instance="1", pollIntervalMs="30000", retryTimeoutMs="2000"]
}
```

### Items

```
Switch HeatPumpBedroom1_OperationStatus "HeatPump Bedroom1 Operation Status" {channel="echonetlite:device:1:HeatPump_Bedroom1:operationStatus"}
```
