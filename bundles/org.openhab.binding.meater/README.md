# Meater Binding

This is an openHAB binding for the Meater probe, by Apption Labs.

This binding uses the Meater cloud REST API.

![Meater+ Probe](doc/meater-plus-side.png)

## Supported Things

This binding supports the following thing types:

- meaterapi: Bridge - Communicates with the Meater cloud REST API.


- meaterprobe: The Meater probe - Only support for cloud connected Meater probes (Meater+) 

## Discovery

The preferred way of adding Meater probe(s) since the probe IDs are not easily found.

**NOTE**: You need to have your Meater probe(s) connected to the cloud and the Meater app running before you start the discovery.

After the configuration of the Bridge, your Meater probe(s) will be automatically discovered and placed as a thing(s) in the inbox.


## Thing Configuration

#### Bridge

| Parameter | Description                                                  | Type   | Default  | Required | 
|-----------|--------------------------------------------------------------|--------|----------|----------|
| email     | The email used to connect to your Meater cloud account       | String | NA       | yes      |
| password  | The password used to connect to your Meater cloud account    | String | NA       | yes      |
| refresh   | Specifies the refresh interval in second                     | Number | 30       | yes      |

## Channels

| Channel Type ID       | Item Type          | Description                                          | 
|-----------------------|--------------------|------------------------------------------------------|
| internalTemperature   | Number:Temperature | Internal temperature reading of Meater probe         |
| ambientTemperature    | Number:Temperature | Ambient temperature reading of Meater probe          |
| cookTargetTemperature | Number:Temperature | Internal temperature reading of Meater probe         |
| cookPeakTemperature   | Number:Temperature | Peak temperature of current cook                     |
| lastConnection        | DateTime           | Date and time of last probe connection               |
| cookId                | String             | ID of current cook                                   |
| cookName              | String             | Name of current cook                                 |
| cookState             | String             | State of current cook                                |
| cookElapsedTime       | Number:Time        | Elapsed time in seconds for current cook             |
| cookRemainingTime     | Number:Time        | Remaining time in seconds for current cook           |
| cookEstimatedEndTime  | DateTime           | Date and time of estimated end time for current cook |





