# Speedtest Binding

The Speedtest Binding can be used to perform a network speed test for your openHAB instance.
It is based on the command line interface (CLI) version of Ookla's Speedtest (<https://www.speedtest.net/apps/cli>).

The Ookla CLI Speedtest application MUST be installed on your openHAB instance when using the Speedtest Binding.

## Why Ookla's Speedtest?

Fully supported and maintained by Ookla with the latest Speedtest technology for best results:

- Consistent measurement, even on high bandwidth
- Consistent measurement, very much independent from the performance of the host system

## What functionality does Ookla's Speedtest offer?

The Speedtest Binding is using the following functionality, provided by Ookla's Speedtest:

- Output of an accurate timestamp per measurement
- Output of Ping time and Jitter
- Output of Bandwidth, transferred Bytes and elapsed time for Down-/Upload
- Output of the used interface with Internal/External IP, MAC Address and the Internet Service Provider (ISP)
- Output of a result ID and a result URL
- Output of the used Server and location the Speedtest was run against
- Possibility to pre-select a server used for testing

## What interfaces does the Speedtest Binding offer?

The Speedtest Binding provides the Ookla's Speedtest functionality via the following openHAB interface:

- Execute Speedtest time based or triggered
- Provide results via openHAB Channels
- List available Ookla Speedtest servers that can be used for testing (optional)

## Ookla License and Privacy Terms

When using this binding, you automatically accept the license and privacy terms of Ookla's Speedtest.
You can find the latest version of those terms at the following webpages:

- <https://www.speedtest.net/about/eula>
- <https://www.speedtest.net/about/terms>
- <https://www.speedtest.net/about/privacy>

## Supported Things

Speedtest thing.

## Binding Configuration

For this binding to work, you MUST install Ookla's Speedtest command line tool (`speedtest` or `speedtest.exe`).
It will not work with other versions like `speedtest-cli` or other `speedtest` variants.

To install Ookla's version of Speedtest, head to <https://www.speedtest.net/apps/cli> and follow the instructions for your Operating System.

## Thing Configuration

| Parameter         |  Description                                                                                                                 | Default |
|-------------------|------------------------------------------------------------------------------------------------------------------------------|---------|
| `refreshInterval` | How often to test network speed, in minutes                                                                                  | `60`    |
| `execPath`        | The path of the Ookla Speedtest executable.<br/>Linux machines may leave this blank and it defaults to `/usr/bin/speedtest`. |         |
| `serverID`        | Optional: A specific server that shall be used for testing. You can pick the server ID from the "Thing Properties".<br/>If this is left blank the best option will be selected by Ookla.          |         |

The `refreshInterval` parameter can also be set to `0` which means "Do not test automatically".
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
| `timestamp`           | `DateTime`                | Timestamp of the Speedtest run                                    |
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
| `resultImage`         | `Image`                   | The Speedtest results as image                                    |
| `triggerTest`         | `Switch`                  | Trigger in order to run Speedtest manually                        |

## Full Example

### Thing File

```java
Thing   speedtest:speedtest:myspeedtest   "Ookla Speedtest"    [ execPath="/usr/bin/speedtest", refreshInterval=60 ]
```

### Item File

```java
String                    Speedtest_Server                "Server"                { channel="speedtest:speedtest:myspeedtest:server" }
DateTime                  Speedtest_Timestamp             "Timestamp"             { channel="speedtest:speedtest:myspeedtest:timestamp" }
Number:Time               Speedtest_Ping_Jitter           "Ping Jitter"           { channel="speedtest:speedtest:myspeedtest:pingJitter" }
Number:Time               Speedtest_Ping_Latency          "Ping Latency"          { channel="speedtest:speedtest:myspeedtest:pingLatency" }
Number:DataTransferRate   Speedtest_Download_Bandwidth    "Download Bandwidth"    { channel="speedtest:speedtest:myspeedtest:downloadBandwidth" }
Number:DataAmount         Speedtest_Download_Bytes        "Download Bytes"        { channel="speedtest:speedtest:myspeedtest:downloadBytes" }
Number:Time               Speedtest_Download_Elapsed      "Download Elapsed"      { channel="speedtest:speedtest:myspeedtest:downloadElapsed" }
Number:DataTransferRate   Speedtest_Upload_Bandwidth      "Upload Bandwidth"      { channel="speedtest:speedtest:myspeedtest:uploadBandwidth" }
Number:DataAmount         Speedtest_Upload_Bytes          "Upload Bytes"          { channel="speedtest:speedtest:myspeedtest:uploadBytes" }
Number:Time               Speedtest_Upload_Elapsed        "Upload Elapsed"        { channel="speedtest:speedtest:myspeedtest:uploadElapsed" }
String                    Speedtest_ISP                   "ISP"                   { channel="speedtest:speedtest:myspeedtest:isp" }
String                    Speedtest_Interface_InternalIP  "Internal IP Address"   { channel="speedtest:speedtest:myspeedtest:interfaceInternalIp" }
String                    Speedtest_Interface_ExternalIP  "External IP Address"   { channel="speedtest:speedtest:myspeedtest:interfaceExternalIp" }
String                    Speedtest_ResultURL             "Result URL"            { channel="speedtest:speedtest:myspeedtest:resultUrl" }
Image                     Speedtest_ResultImage           "Result Image"          { channel="speedtest:speedtest:myspeedtest:resultImage" }
Switch                    Speedtest_TriggerTest           "Trigger Test"          { channel="speedtest:speedtest:myspeedtest:triggerTest" }
```
