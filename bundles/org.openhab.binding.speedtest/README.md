# Speedtest Binding

The Speedtest Binding can be used to perform a network speed test for your openHAB instance.
It is based on the command line interface (CLI) version of Ookla's Speedtest (https://www.speedtest.net/apps/cli).

The Ookla CLI Speedtest application MUST be installed on your openHAB instance when using the Speedtest Binding.

When using this Binding, you automatically accept the License and Privacy Terms of Ookla's Speedtest. You can find the latest version of those terms at the following webpages:
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
| `pingJitter`          | `Number:Time`             | Ping Jitter                                                       |
| `pingLatency`         | `Number:Time`             | Ping Latency                                                      |
| `downloadBandwidth`   | `Number:DataTransferRate` | Download bandwidth, e.g. in Mbit/s                                |
| `downloadBytes`       | `Number:DataAmount`       | Amount of data that were used for the download bandwith test      |
| `downloadElapsed`     | `Number:Time`             | Time spend for the download bandwidth test                         |
| `uploadBandwidth`     | `Number:DataTransferRate` | Upload bandwidth, e.g. in Mbit/s                                  |
| `uploadBytes`         | `Number:DataAmount`       | Amount of data that were used for the upload bandwith test        |
| `uploadElapsed`       | `Number:Time`             | Time spend for the upload bandwidth test                           |
| `isp`                 | `String`                  | Your Internet Service Provider (ISP) as calculated by Ookla       |
| `interfaceInternalIp` | `String`                  | IP address of the internal interface that was used for the test   |
| `interfaceExternalIp` | `String`                  | IP address of the external interface that was used for the test   |
| `resultUrl`           | `String`                  | The URL to the Speedtest results in HTML on the Ookla webserver    |
| `triggerTest`         | `Switch`                  | Trigger in order to run Speedtest manually                        |
