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

| Parameter    | Type    | Required | Description |
|-------------|--------|----------|-------------|
| `serial`    | Text   | Yes      | Unique serial number of the printer. |
| `scheme`    | Text   | No       | URI scheme. (Advanced) |
| `hostname`  | Text   | Yes      | IP address of the printer or `us.mqtt.bambulab.com` for cloud mode. |
| `port`      | Integer| No       | URI port. (Advanced) |
| `username`  | Text   | No       | `bblp` for local mode or your Bambu Lab user (starting with `u_`). (Advanced) |
| `accessCode`| Text   | Yes      | Access code for the printer. The method of obtaining this varies between local and cloud modes. |

## Channels

| Channel ID                  | Type                 | Description |
|-----------------------------|----------------------|-------------|
| `nozzleTemperature`         | Temperature Channel | Current temperature of the nozzle. |
| `nozzleTargetTemperature`   | Temperature Channel | Target temperature of the nozzle. |
| `bedTemperature`            | Temperature Channel | Current temperature of the heated bed. |
| `bedTargetTemperature`      | Temperature Channel | Target temperature of the heated bed. |
| `chamberTemperature`        | Temperature Channel | Current temperature inside the printer chamber. |
| `mcPrintStage`              | String Channel      | Current stage of the print process. |
| `mcPercent`                 | Percent Channel     | Percentage of the print completed. |
| `mcRemainingTime`           | Number Channel      | Estimated time remaining for the print (in seconds). |
| `wifiSignal`                | WiFi Channel        | Current WiFi signal strength. |
| `bedType`                   | String Channel      | Type of the printer's heated bed. |
| `gcodeFile`                 | String Channel      | Name of the currently loaded G-code file. |
| `gcodeState`                | String Channel      | Current state of the G-code execution. |
| `reason`                    | String Channel      | Reason for pausing or stopping the print. |
| `result`                    | String Channel      | Final result or status of the print job. |
| `gcodeFilePreparePercent`   | Percent Channel     | Percentage of G-code file preparation completed. |
| `bigFan1Speed`              | Number Channel      | Speed of the first large cooling fan (RPM). |
| `bigFan2Speed`              | Number Channel      | Speed of the second large cooling fan (RPM). |
| `heatBreakFanSpeed`         | Number Channel      | Speed of the heat break cooling fan (RPM). |
| `layerNum`                  | Number Channel      | Current layer being printed. |
| `speedLevel`                | Number Channel      | Current speed setting of the print job. |
| `timeLaps`                  | Boolean Channel     | Indicates whether timelapse recording is enabled. |
| `useAms`                    | Boolean Channel     | Indicates whether the Automatic Material System (AMS) is active. |
| `vibrationCalibration`      | Boolean Channel     | Indicates whether vibration calibration has been performed. |
| `ledChamber`                | On/Off Command     | Controls the LED lighting inside the printer chamber. |
| `ledWork`                   | On/Off Command     | Controls the LED lighting for the work area. |

## Full Example

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

