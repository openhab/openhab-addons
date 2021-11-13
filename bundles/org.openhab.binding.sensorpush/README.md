# SensorPush Binding

[SensorPush](https://www.sensorpush.com/) sells a line of battery-powered wireless sensors that, depending on the model, provide data on temperature, relative humidity, barometric pressure, dew point, and vapor pressure deficit (VPD).
The sensors communicate using Bluetooth LE.
While they can be used directly via BLE, when multiple sensors are in use they are typically configured to relay data to the cloud via the G1 WiFi Gateway.

This binding retrieves sensor data from the SensorPush Gateway Cloud using a published API.
It requires use of the G1 WiFi gateway and a connection to the SensorPush Gateway Cloud.

Supported sensors: HT1, HT.W, and HTP.XW

## Supported Things

The binding supports the following thing types:

* `cloudbridge` - Provides connectivity to the SensorPush Gateway Cloud.
* `sensor` - Represents a HT1, HT.W, or HTP.XW sensor.

## Discovery

Automatic discovery is supported for the sensors, but not for the cloud gateway.
It is recommended that you configure the cloudbridge thing manually using the UI and let the associated sensors be discovered automatically.

## Thing Configuration

It is recommended that the SensorPush binding be configured through the management UI.
There is no easy way for the user to determine the sensor IDs in advance, so it is best to simply auto-discover them.
After configuring the bridge, all active sensors should appear in the discovery inbox.

### Cloudbridge thing

The `cloudbridge` thing is responsible for communicating with the SensorPush Gateway Cloud.
You must supply your user name and password.
The poll and timeout parameters are optional.

Parameters:

| Name         | Required | Type          | Default | Description              |
|--------------|----------|---------------|---------|--------------------------|
| `user`     | Yes      |text           | n/a | Your SensorPush cloud service user name (email address) |
| `password`| Yes      |text           | n/a | Your SensorPush cloud service password |
| `poll`     | No       |integer, 2-60  |  5  | Polling interval in minutes |
| `timeout` | No       |integer, 1-120 | 30  | Timeout period for API requests in seconds. |

**Note:** To activate your API access, you must log in to the [Gateway Cloud Dashboard](https://dashboard.sensorpush.com/) and agree to the terms of service.
Once you've logged in that initial time, your account will have access.

### Sensor thing

The `sensor` thing represents an individual SensorPush sensor.
It has a variety of channels that will be populated with the latest sensor readings at each poll period.
Sensors relay their readings at approximate 1 minute intervals, so in normal operation the oldest a reading should be is approximately 1 minute plus the configured poll interval.
The `time` channel will contain the timestamp of the latest readings.
If your particular sensor model does not support a given channel, the value for that channel will remain NULL.
The id parameter must be supplied.

Parameters:

| Name              | Required | Type          | Default | Description                        |
|-------------------|----------|---------------|---------|------------------------------------|
| `id`             | Yes      | text          | n/a     | The unique ID number of the sensor |
| `pressureMode`| No       | text          | station | The reporting mode for barometric pressure. Must be set to either "station" or "meteorological". The station mode reports the pressure as recorded by the sensor, while the meteorological mode adjusts the reported pressure to mean sea level as is common for weather reporting.|
| `altitude`     | No       | integer       | n/a     | The altitude of the sensor in feet above MSL. It is only necessary to set this parameter if you selected the "meteorological" option for pressureMode and have not set the sensor altitude in the SensorPush app. The altitude set in the SensorPush app will override this value.|

## Channels

The following channels are provided by the `sensor` thing:

| Channel     | Type                     | Description              |
|-------------|--------------------------|--------------------------|
| temperature | Number:Temperature       | Temperature              |
| humidity    | Number:Dimensionless     | Relative humidity        |
| pressure    | Number:Pressure          | Barometric pressure      |
| dewpoint    | Number:Temperature       | Dew point                |
| vpd         | Number:Pressure          | Vapor pressure deficit   |
| time        | DateTime                 | Time of reading          |
| rssi        | Number:Power             | Received signal strength |
| voltage     | Number:ElectricPotential | Battery voltage          |

Note that all channels except `time` use QuantityType values.

No channels are provided by the `cloudbridge` thing.
