# BambuLab Binding

TODO write that addon supports connecting with printers in local mode. Cloud mode in theory is possible but not supported

## Cloud Mode

TODO cloud mode is possible, but not supported by addon developers

To use cloud mode

### Find Username

Log in to Maker World and visit [my-preferences](https://makerworld.com/api/v1/design-user-service/my/preference) to JSON with your data. 
The interesting field is `uid` which has unique ID of your account. Put it in as `username` in configuration (advanced field) with prefix `u_`

### Access Token

To get access token you need to do 2 steps:

1. login with email/password
2. confirm login with token (from email)

To login use this curl query:

```shell
curl -X POST "https://api.bambulab.com/v1/user-service/user/login" \
     -H "Content-Type: application/json" \
     -d '{
           "account": "you@email.io",
           "password": "superduperpassword123"
         }'
```

after this open your email app, get code and run this curl command:

```shell
curl -X POST "https://api.bambulab.com/v1/user-service/user/login" \
     -H "Content-Type: application/json" \
     -d '{
           "account": "you@email.io",
           "code": "123456"
         }'
```

As a response you will get super long access code. Copy it all to `accessCode` parameter. 

Note: this access code will expire after 3 months and you cannot do anything about it. Just repeat the whole sequence and obtain new access code.

### Hostname

Use `us.mqtt.bambulab.com` as hostname

## Supported Things

- `printer`: TODO: Short description of the printer

## Thing Configuration

TODO generate thing configuration from below XML (remove the XML after)

```xml
<config-description>
	<parameter name="serial" type="text" required="true">
		<label>Serial Number</label>
		<description>Unique serial number of the printer.</description>
	</parameter>

	<parameter name="scheme" type="text" required="false">
		<label>Scheme</label>
		<description>URI scheme.</description>
		<advanced>true</advanced>
	</parameter>

	<parameter name="hostname" type="text" required="true">
		<label>Hostname</label>
		<description>IP address of the printer or `us.mqtt.bambulab.com` for cloud mode.</description>
	</parameter>

	<parameter name="port" type="integer" required="false">
		<label>Port</label>
		<description>URI port.</description>
		<advanced>true</advanced>
	</parameter>

	<parameter name="username" type="text">
		<label>Username</label>
		<description>`bblp` for local mode or your Bambu Lab user (starts with `u_`).</description>
		<advanced>true</advanced>
	</parameter>

	<parameter name="accessCode" type="text" required="true">
		<label>Access Code</label>
		<description>Access code of the printer. The method of obtaining it differs for local and cloud modes.
		</description>
		<context>password</context>
	</parameter>
</config-description>
```

## Channels

TODO generate channels from given XML:

```xml
<channels>
	<channel id="nozzleTemperature" typeId="temperature-channel">
		<label>Nozzle Temperature</label>
		<description>Current temperature of the nozzle.</description>
	</channel>
	<channel id="nozzleTargetTemperature" typeId="temperature-channel">
		<label>Nozzle Target Temperature</label>
		<description>Target temperature of the nozzle.</description>
	</channel>
	<channel id="bedTemperature" typeId="temperature-channel">
		<label>Bed Temperature</label>
		<description>Current temperature of the heated bed.</description>
	</channel>
	<channel id="bedTargetTemperature" typeId="temperature-channel">
		<label>Bed Target Temperature</label>
		<description>Target temperature of the heated bed.</description>
	</channel>
	<channel id="chamberTemperature" typeId="temperature-channel">
		<label>Chamber Temperature</label>
		<description>Current temperature inside the printer chamber.</description>
	</channel>
	<channel id="mcPrintStage" typeId="string-channel">
		<label>Print Stage</label>
		<description>Current stage of the print process.</description>
	</channel>
	<channel id="mcPercent" typeId="percent-channel">
		<label>Print Progress</label>
		<description>Percentage of the print completed.</description>
	</channel>
	<channel id="mcRemainingTime" typeId="number-channel">
		<label>Remaining Print Time</label>
		<description>Estimated time remaining for the print in seconds.</description>
	</channel>
	<channel id="wifiSignal" typeId="wifi-channel">
		<label>WiFi Signal Strength</label>
		<description>Current WiFi signal strength.</description>
	</channel>
	<channel id="bedType" typeId="string-channel">
		<label>Bed Type</label>
		<description>Type of the printer's heated bed.</description>
	</channel>
	<channel id="gcodeFile" typeId="string-channel">
		<label>G-code File</label>
		<description>Name of the currently loaded G-code file.</description>
	</channel>
	<channel id="gcodeState" typeId="string-channel">
		<label>G-code State</label>
		<description>Current state of the G-code execution.</description>
	</channel>
	<channel id="reason" typeId="string-channel">
		<label>Pause/Stop Reason</label>
		<description>Reason for pausing or stopping the print.</description>
	</channel>
	<channel id="result" typeId="string-channel">
		<label>Print Result</label>
		<description>Final result or status of the print job.</description>
	</channel>
	<channel id="gcodeFilePreparePercent" typeId="percent-channel">
		<label>G-code Preparation Progress</label>
		<description>Percentage of G-code file preparation completed.</description>
	</channel>
	<channel id="bigFan1Speed" typeId="number-channel">
		<label>Big Fan 1 Speed</label>
		<description>Speed of the first large cooling fan (RPM).</description>
	</channel>
	<channel id="bigFan2Speed" typeId="number-channel">
		<label>Big Fan 2 Speed</label>
		<description>Speed of the second large cooling fan (RPM).</description>
	</channel>
	<channel id="heatBreakFanSpeed" typeId="number-channel">
		<label>Heat Break Fan Speed</label>
		<description>Speed of the heat break cooling fan (RPM).</description>
	</channel>
	<channel id="layerNum" typeId="number-channel">
		<label>Current Layer Number</label>
		<description>Current layer being printed.</description>
	</channel>
	<channel id="speedLevel" typeId="number-channel">
		<label>Print Speed Level</label>
		<description>Current speed setting of the print job.</description>
	</channel>
	<channel id="timeLaps" typeId="boolean-channel">
		<label>Timelapse Enabled</label>
		<description>Indicates whether timelapse recording is enabled.</description>
	</channel>
	<channel id="useAms" typeId="boolean-channel">
		<label>AMS System in Use</label>
		<description>Indicates whether the Automatic Material System (AMS) is active.</description>
	</channel>
	<channel id="vibrationCalibration" typeId="boolean-channel">
		<label>Vibration Calibration</label>
		<description>Indicates whether vibration calibration has been performed.</description>
	</channel>

	<!-- command channels -->
	<channel id="ledChamber" typeId="on-off-command-channel">
		<label>Chamber LED</label>
		<description>Controls the LED lighting inside the printer chamber.</description>
	</channel>
	<channel id="ledWork" typeId="on-off-command-channel">
		<label>Work Area LED</label>
		<description>Controls the LED lighting for the work area.</description>
	</channel>
</channels>
```

## Full Example

### Thing Configuration

```java
Thing bambulab:printer:44bb12af13 "Bambu Lab Printer" @ "3D Printer" [
serial="xyz",
hostname="192.168.0.123",
accessCode="12345678"] {
Channels:
Type temperature-channel : nozzleTemperature "Nozzle Temperature" [ ]
Type temperature-channel : nozzleTargetTemperature "Nozzle Target Temperature" [ ]
Type temperature-channel : bedTemperature "Bed Temperature" [ ]
Type temperature-channel : bedTargetTemperature "Bed Target Temperature" [ ]
Type temperature-channel : chamberTemperature "Chamber Temperature" [ ]
Type string-channel : mcPrintStage "Print Stage" [ ]
Type percent-channel : mcPercent "Print Progress" [ ]
Type number-channel : mcRemainingTime "Remaining Print Time" [ ]
Type wifi-channel : wifiSignal "WiFi Signal Strength" [ ]
Type string-channel : bedType "Bed Type" [ ]
Type string-channel : gcodeFile "G-code File" [ ]
Type string-channel : gcodeState "G-code State" [ ]
Type string-channel : reason "Pause/Stop Reason" [ ]
Type string-channel : result "Print Result" [ ]
Type percent-channel : gcodeFilePreparePercent "G-code Preparation Progress" [ ]
Type number-channel : bigFan1Speed "Big Fan 1 Speed" [ ]
Type number-channel : bigFan2Speed "Big Fan 2 Speed" [ ]
Type number-channel : heatBreakFanSpeed "Heat Break Fan Speed" [ ]
Type number-channel : layerNum "Current Layer Number" [ ]
Type number-channel : speedLevel "Print Speed Level" [ ]
Type boolean-channel : timeLaps "Timelapse Enabled" [ ]
Type boolean-channel : useAms "AMS System in Use" [ ]
Type boolean-channel : vibrationCalibration "Vibration Calibration" [ ]
Type on-off-command-channel : ledChamber "Chamber LED" [ ]
Type on-off-command-channel : ledWork "Work Area LED" [ ]
        }
```
### Item Configuration

```java
Number:Temperature Bed_Target_Temperature "Bed Target Temperature" (Bambu_Lab_Printer) ["Point"] { category="Temperature" }
```

## Actions

TODO write that printer thing supports actions

```java
rule "test"
when
    /* when */
then
	val actions = getActions("bambulab", "bambulab:printer:as8af03m38")
	if (actions !== null) {
            // Refreshesh all channnels
            actions.refreshChannels()

            actions.sendCommand("PushingCommand:1:1")
	}
end
```
