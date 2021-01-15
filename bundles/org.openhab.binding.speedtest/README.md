# speedtest Binding

This binding simplifies the addition of network speed tests from your openHAB instance.   Simply put, it's a wrapper around Ookla's SpeedTest ( https://www.speedtest.net/apps/cli ).  

You MUST have the Ookla cli application installed on your machine prior to using this.

## Supported Things

Single thing, the Speedtest Thing.   

## Binding Configuration

For this binding to work, you MUST install Ookla's speedtest, this will not work with other versions of "speedtest-cli" or packaged speedtest varieties.   

To install Ookla's version of speedtest, head to https://www.speedtest.net/apps/cli and follow the instructions for your Operating System.   

Linux based systems will assume a default install location of /usr/bin/speedtest .  If your environment is different, just enter the path in the Thing config.  

## Thing Configuration
| config option  |  description                  |
|----------|------------------------------|
| Refresh Rate  | This will change the refresh rate(or how often) the binding checks the speed  |
| Speedtest Path  | This is the full URL to the speedtest executable ** |

** You must also check that you have permission as the user that openhab is running under to access and run this.   

## Channels

| channel  | type   | description                  |
|----------|--------|------------------------------|
| server  | String | The remote server that the check was run against  |
| ping_jitter  | Number | Ping jitter when the test was run  |
| ping_latency  | Number | Ping latency when the test was run |
| download_bandwidth  | Number | Download bandwidth in MB/s  |
| download_bytes  | Number | The remote server that the check was run against  |
| download_elapsed  | Number | The remote server that the check was run against  |
| upload_bandwidth  | Number | Upload bandwidth in MB/s |
| upload_bytes  | Number | How many bytes were used during the test  |
| upload_elapsed  | Number | The elapsed time the upload portion took  |
| isp  | String | Your ISP as calculated by Ookla  |
| interface_internalIp  | String | The IP of the internal interface that was used for the test  |
| interface_externalIp  | String | The IP of the external interface that was used for the test |
| result_url  | String | The URL of your results of the test on Ookla |

## Full Example

Setup intended to be done through the Main UI.   
