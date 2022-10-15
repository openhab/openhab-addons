# Speedtest Binding

The Speedtest Binding can be used to perform a network speed test for your openHAB instance.
It is based on the command line interface (CLI) version of Ookla's Speedtest (https://www.speedtest.net/apps/cli).

The Ookla CLI Speedtest application MUST be installed on your openHAB instance when using the Speedtest Binding.


## Why Ookla's Speedtest?

Fully supported and maintained by Ookla with the latest Speedtest technology for best results:

* Consistent measurement, even on high bandwidth
* Consistent measurement, very much independent from the performance of the host system


## What functionality does Ookla's Speedtest offer?

* Output of a timestamp instead of using the openHAB timestamp
* Output of Ping time and Jitter
* Output of Bandwidth, transferred Bytes and elapsed time for Down-/Upload
* Output of the used interface with Internal/External IP, MAC Address and the Internet Service Provider (ISP)
* Output of a result ID and a result URL
* Output of the used Server and location
* Possiblity to pre-select a server used for testing


## What functionality does the Speedtest Binding offers?

* Execute Speedtest time based or triggered
* Provide results via openHAB Channels
* List available Ookla Speedtest servers that can be used for testing (optional)


## Ookla License and Privacy Terms

When using this binding, you automatically accept the license and privacy terms of Ookla's Speedtest.
You can find the latest version of those terms at the following webpages:

* https://www.speedtest.net/about/eula
* https://www.speedtest.net/about/terms
* https://www.speedtest.net/about/privacy


## Supported Things

Speedtest thing.

## Binding Configuration

For this binding to work, you MUST install Ookla's Speedtest command line tool (`speedtest` or `speedtest.exe`).
It will not work with other versions like `speedtest-cli` or other `speedtest` variants.

To install Ookla's version of Speedtest, head to https://www.speedtest.net/apps/cli and follow the instructions for your Operating System.

## Thing Configuration

| Parameter         |  Description                                                                                                                 | Default |
|-------------------|------------------------------------------------------------------------------------------------------------------------------|---------|
| `refreshInterval` | How often to test network speed, in minutes                                                                                  | `60`    |
| `execPath`        | The path of the Ookla Speedtest executable.<br/>Linux machines may leave this blank and it defaults to `/usr/bin/speedtest`. |         |
| `serverID`        | Optional: A specific server that shall be used for testing. You can pick the server ID from the "Thing Properties".<br/>If this is left blank the best option will be selected by Ookla.          |         |

The `refreshInterval` parameter can also be set to "Do not test automatically".
This can be used if you want to use the "Trigger Test" channel in order to test via rules, or an item instead.

Ensure that the user that openHAB is running with, has the permissions to access and execute the executable.

## Properties

| Property            | Description                                                                                                |
|---------------------|------------------------------------------------------------------------------------------------------------|
| Server List 1...10  | A List of Ookla Speedtest servers that can be used in order to specify a specific server for the Speedtest.<br/>Configure the Server ID via the `serverID` Thing Configuration Parameter. |

## Channels

| Channel               | Type                      | Description                                                       |
|-----------------------|---------------------------|-------------------------------------------------------------------|
| `server`              | `String`                  | The remote server that the Speedtest was run against              |
| `pingJitter`          | `Number:Time`             | Ping Jitter - the variation in the response time                  |
| `pingLatency`         | `Number:Time`             | Ping Latency - the reaction time of your internet connection      |
| `downloadBandwidth`   | `Number:DataTransferRate` | Download bandwidth, e.g. in Mbit/s                                |
| `downloadBytes`       | `Number:DataAmount`       | Amount of data that was used for the last download bandwidth test |
| `downloadElapsed`     | `Number:Time`             | Time spent for the last download bandwidth test                   |
| `uploadBandwidth`     | `Number:DataTransferRate` | Upload bandwidth, e.g. in Mbit/s                                  |
| `uploadBytes`         | `Number:DataAmount`       | Amount of data that was used for the last upload bandwidth test   |
| `uploadElapsed`       | `Number:Time`             | Time spent for the last upload bandwidth test                     |
| `isp`                 | `String`                  | Your Internet Service Provider (ISP) as calculated by Ookla       |
| `interfaceInternalIp` | `String`                  | IP address of the internal interface that was used for the test   |
| `interfaceExternalIp` | `String`                  | IP address of the external interface that was used for the test   |
| `resultUrl`           | `String`                  | The URL to the Speedtest results in HTML on the Ookla webserver   |
| `triggerTest`         | `Switch`                  | Trigger in order to run Speedtest manually                        |
