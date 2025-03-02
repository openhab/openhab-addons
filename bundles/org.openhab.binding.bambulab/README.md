# BambuLab Binding

This addon supports connecting with BambuLab 3D printers in local mode. While cloud mode is theoretically possible, it is not supported by the addon developers.

## Cloud Mode

Cloud mode is possible but not officially supported by the addon developers.

To use cloud mode, follow these steps:

### Find Username

Log in to Maker World and visit [my-preferences](https://makerworld.com/api/v1/design-user-service/my/preference) to retrieve a JSON response containing your data. The relevant field is `uid`, which represents the unique ID of your account. Use this value as the `username` in the configuration (advanced field) with the prefix `u_`.

### Access Token

To obtain an access token, follow these steps:

1. Log in using your email and password.
2. Confirm the login using a token received via email.

#### Step 1: Login with Email and Password

```shell
curl -X POST "https://api.bambulab.com/v1/user-service/user/login" \
     -H "Content-Type: application/json" \
     -d '{
           "account": "you@email.io",
           "password": "superduperpassword123"
         }'
```

#### Step 2: Confirm Login with Token from Email

```shell
curl -X POST "https://api.bambulab.com/v1/user-service/user/login" \
     -H "Content-Type: application/json" \
     -d '{
           "account": "you@email.io",
           "code": "123456"
         }'
```

You will receive a long access code in the response. Copy it and use it as the `accessCode` parameter.

**Note:** This access code expires after three months. When it expires, repeat the process to obtain a new one.

### Hostname

Use `us.mqtt.bambulab.com` as the hostname.

## Supported Things

- `printer`: Represents a BambuLab 3D printer.

## Thing Configuration

Below are the configuration parameters for the `printer` thing type:

- **serial** (required): Unique serial number of the printer.
- **scheme** (optional): URI scheme (advanced setting).
- **hostname** (required): IP address of the printer or `us.mqtt.bambulab.com` for cloud mode.
- **port** (optional): URI port (advanced setting).
- **username**: `bblp` for local mode or your Bambu Lab user (starting with `u_`).
- **accessCode** (required): Access code for the printer. The method of obtaining this varies between local and cloud modes.

## Channels

The following channels are available:

- **Nozzle Temperature** (`nozzleTemperature`): Current temperature of the nozzle.
- **Nozzle Target Temperature** (`nozzleTargetTemperature`): Target temperature of the nozzle.
- **Bed Temperature** (`bedTemperature`): Current temperature of the heated bed.
- **Bed Target Temperature** (`bedTargetTemperature`): Target temperature of the heated bed.
- **Chamber Temperature** (`chamberTemperature`): Current temperature inside the printer chamber.
- **Print Stage** (`mcPrintStage`): Current stage of the print process.
- **Print Progress** (`mcPercent`): Percentage of the print completed.
- **Remaining Print Time** (`mcRemainingTime`): Estimated time remaining for the print (in seconds).
- **WiFi Signal Strength** (`wifiSignal`): Current WiFi signal strength.
- **Bed Type** (`bedType`): Type of the printer's heated bed.
- **G-code File** (`gcodeFile`): Name of the currently loaded G-code file.
- **G-code State** (`gcodeState`): Current state of the G-code execution.
- **Pause/Stop Reason** (`reason`): Reason for pausing or stopping the print.
- **Print Result** (`result`): Final result or status of the print job.
- **G-code Preparation Progress** (`gcodeFilePreparePercent`): Percentage of G-code file preparation completed.
- **Big Fan 1 Speed** (`bigFan1Speed`): Speed of the first large cooling fan (RPM).
- **Big Fan 2 Speed** (`bigFan2Speed`): Speed of the second large cooling fan (RPM).
- **Heat Break Fan Speed** (`heatBreakFanSpeed`): Speed of the heat break cooling fan (RPM).
- **Current Layer Number** (`layerNum`): Current layer being printed.
- **Print Speed Level** (`speedLevel`): Current speed setting of the print job.
- **Timelapse Enabled** (`timeLaps`): Indicates whether timelapse recording is enabled.
- **AMS System in Use** (`useAms`): Indicates whether the Automatic Material System (AMS) is active.
- **Vibration Calibration** (`vibrationCalibration`): Indicates whether vibration calibration has been performed.
- **Chamber LED** (`ledChamber`): Controls the LED lighting inside the printer chamber.
- **Work Area LED** (`ledWork`): Controls the LED lighting for the work area.

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

The printer thing supports actions:

```java
rule "test"
when
    /* when */
then
    val actions = getActions("bambulab", "bambulab:printer:as8af03m38")
    if (actions !== null) {
        // Refresh all channels
        actions.refreshChannels()
        actions.sendCommand("PushingCommand:1:1")
    }
end
```

