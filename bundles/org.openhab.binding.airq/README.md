# air-Q Binding

The air-Q Binding integrates the air analyzer [air-Q](http://www.air-q.com) device into the openHAB system.

With the binding, it is possible to subscribe to supported measurement and configuration data delivered by the air-Q device.

![air-Q image](doc/image_air-Q.png)

## Supported Things

Only one Thing is supported: The `airq` device.
This Binding was tested with an `air-Q Pro` device with 14 sensors. It also works with an `air-Q` device with 11 sensors.

## Discovery

air-Q devices on the local network are discovered automatically via mDNS.
Discovered devices appear in the openHAB Inbox.
To add a discovered device, click **OK → EDIT** in the Inbox dialog, then enter the device password in the Configuration section and save.
The device IP address is filled in automatically by discovery.

If auto-discovery does not work (e.g. due to network segmentation), you can still add the device manually using its IP address.

## Thing Configuration

The air-Q Thing must be configured with:

| Parameter   | Description                          | Required                         |
|-------------|--------------------------------------|----------------------------------|
| ipAddress   | Network address, e.g. 192.168.0.68   | Yes (auto-filled by discovery)   |
| password    | Password of the air-Q device         | Yes                              |

The Thing provides the following properties:

| Parameter         | Description                     |
|-------------------|---------------------------------|
| id                | Device ID                       |
| hardwareVersion   | Hardware version                |
| softwareVersion   | Firmware version                |
| sensorList        | Available sensors               |
| sensorInfo        | Information about the sensors   |
| industry          | Industry version                |
| deviceType        | Product type, e.g. airQ Select  |
| serialNumber      | Serial number                   |

## Channels

The available sensor data depends on the air-Q model and installed sensor package.
This includes also the Maximum Error per sensor value.
Deprecated flat Maximum Error channels use the `_maxerr` suffix; grouped channels use `-maxerr`.
Channels are grouped into Status (read-only device state), Measurements, Events, Maximum Errors, and Settings (device configuration).
Grouped channel IDs use lowercase words separated by hyphens, for example `settings#night-mode-start-day` and `measurements#humidity-relative`.

Existing Things retain their flat channels and Item links when upgraded to Thing version 5.
Flat channels are deprecated; use the grouped channels for new configurations and migrate existing Item links.
Those channels continue receiving states, including their legacy time representations.
To use grouped channels, update the Item links explicitly; links are not automatically moved to the new channel UIDs.
The binding logs a deprecation notice during initialization if flat channels are present.

The rw column is empty if the channel is only readable, w if the channel can be written and rw if it allows both to be read and written.

### Status

| channel                    | type                               | rw    | description                                                                              |
|----------------------------|------------------------------------|-------|------------------------------------------------------------------------------------------|
| status                     | String                             |       | "OK", or "sensor: message" for each sensor that has no reliable data                     |
| uptime                     | Number:Time                        |       | uptime in seconds                                                                        |
| boots                      | String                             |       | Undocumented boot counters                                                               |
| timestamp                  | DateTime                           |       | Timestamp of measurement                                                                 |
| measure-time               | Number:Time                        |       | Milliseconds needed for measurement                                                      |
| daytime                    | Switch                             |       | ON in day operation, OFF in night operation                                              |
| initial-cal-finished       | Switch                             |       | Initial calibration has finished                                                         |
| user-calib                 | String                             |       | User calibrations, "sensor: offset=value [dd.MM.yyyy HH:mm:ss]" for each sensor          |
| wifi-rssi                  | Number:Power                       |       | Wi-Fi signal strength (RSSI) in dBm, UNDEF while not connected                           |
| wlan-config-ssid           | String                             |       | Connected WLAN SSID                                                                      |
| wlan-config-ip-address     | String                             |       | Assigned IP address                                                                      |
| wlan-config-net-mask       | String                             |       | Network mask                                                                             |
| wlan-config-gateway        | String                             |       | Network Gateway                                                                          |
| wlan-config-mac            | String                             |       | MAC Address                                                                              |
| wlan-config-bssid          | String                             |       | Network BSSID                                                                            |
| cloud-connection           | String                             |       | State of the connection to the air-Q cloud, e.g. DISCONNECTED                            |

### Measurements

| channel                    | type                               | rw    | description                                                                              |
|----------------------------|------------------------------------|-------|------------------------------------------------------------------------------------------|
| temperature                | Number:Temperature                 |       | Temperature                                                                              |
| humidity-relative          | Number:Dimensionless               |       | Humidity in percent                                                                      |
| humidity-absolute          | Number:Density                     |       | Absolute Humidity                                                                        |
| dewpt                      | Number:Temperature                 |       | Dew Point                                                                                |
| d-hdt                      | Number                             |       | Change of absolute humidity in mg/m³/s                                                   |
| pressure                   | Number:Pressure                    |       | Barometric Pressure                                                                      |
| pressure-rel               | Number:Pressure                    |       | Relative barometric pressure in hPa (advanced)                                           |
| health-index               | Number:Dimensionless               |       | Health Index in percent, -20 % during a gas alarm and -80 % during a fire alarm          |
| health                     | Number:Dimensionless               |       | Health Index (0 to 1000, -200 for gas alarm, -800 for fire alarm)                        |
| performance-index          | Number:Dimensionless               |       | Performance Index in percent                                                             |
| performance                | Number:Dimensionless               |       | Performance Index (0 to 1000)                                                            |
| virus-free                 | Number:Dimensionless               | r     | Virus-Free index in percent; the lower the index, the higher the potential virus risk.   |
| mold-free                  | Number:Dimensionless               | r     | Mold-Free index in percent; the lower the index, the higher the potential mold risk.     |
| co2                        | Number:Dimensionless               |       | Carbon dioxide (CO₂) concentration                                                       |
| d-co2dt                    | Number                             |       | Change of CO₂ concentration in ppb/s                                                     |
| tvoc                       | Number:Dimensionless               |       | VOC concentration                                                                        |
| tvoc-ionsc                 | Number:Dimensionless               |       | Industrial VOC in ppb (advanced)                                                         |
| co                         | Number:Density                     |       | Carbon monoxide (CO) concentration                                                       |
| no2                        | Number:Density                     |       | Nitrogen Dioxide (NO₂) concentration                                                     |
| o3                         | Number:Density                     |       | Ozone (O₃) concentration                                                                 |
| o2                         | Number:Dimensionless               |       | Oxygen (O₂) concentration                                                                |
| so2                        | Number:Density                     |       | Sulfur dioxide (SO₂) concentration                                                       |
| h2s                        | Number:Density                     |       | Hydrogen sulfide (H₂S)                                                                   |
| c2h4o                      | Number:Density                     |       | Acetaldehyde in µg/m³ (advanced)                                                         |
| nh3-mr100                  | Number:Density                     |       | Ammonia in µg/m³ (advanced)                                                              |
| ash3                       | Number:Density                     |       | Arsine in µg/m³ (advanced)                                                               |
| br2                        | Number:Density                     |       | Bromine in µg/m³ (advanced)                                                              |
| ch4s                       | Number:Density                     |       | Methanethiol in µg/m³ (advanced)                                                         |
| cl2-m20                    | Number:Density                     |       | Chlorine in µg/m³ (advanced)                                                             |
| clo2                       | Number:Density                     |       | Chlorine dioxide in µg/m³ (advanced)                                                     |
| cs2                        | Number:Density                     |       | Carbon disulfide in µg/m³ (advanced)                                                     |
| ethanol                    | Number:Density                     |       | Ethanol in µg/m³ (advanced)                                                              |
| c2h4                       | Number:Density                     |       | Ethylene in µg/m³ (advanced)                                                             |
| ch2o-m10                   | Number:Density                     |       | Formaldehyde in µg/m³ (advanced)                                                         |
| f2                         | Number:Density                     |       | Fluorine in µg/m³ (advanced)                                                             |
| hcl                        | Number:Density                     |       | Hydrochloric acid in µg/m³ (advanced)                                                    |
| hcn                        | Number:Density                     |       | Hydrogen cyanide in µg/m³ (advanced)                                                     |
| hf                         | Number:Density                     |       | Hydrogen fluoride in µg/m³ (advanced)                                                    |
| h2-m1000                   | Number:Density                     |       | Hydrogen in µg/m³ (advanced)                                                             |
| h2o2                       | Number:Density                     |       | Hydrogen peroxide in µg/m³ (advanced)                                                    |
| n2o                        | Number:Density                     |       | Nitrous oxide in µg/m³ (advanced)                                                        |
| no-m250                    | Number:Density                     |       | Nitrogen monoxide in µg/m³ (advanced)                                                    |
| acid-m100                  | Number:Dimensionless               |       | Organic acid in ppb (advanced)                                                           |
| ph3                        | Number:Density                     |       | Hydrogen phosphide in µg/m³ (advanced)                                                   |
| sih4                       | Number:Density                     |       | Silane in µg/m³ (advanced)                                                               |
| ch4-mipex                  | Number:Dimensionless               |       | Methane in percent (advanced)                                                            |
| c3h8-mipex                 | Number:Dimensionless               |       | Propane in percent (advanced)                                                            |
| r32                        | Number:Dimensionless               |       | Refrigerant R32 in percent (advanced)                                                    |
| r454b                      | Number:Dimensionless               |       | Refrigerant R454B in percent (advanced)                                                  |
| r454c                      | Number:Dimensionless               |       | Refrigerant R454C in percent (advanced)                                                  |
| radon                      | Number:RadiationSpecificActivity   |       | Radon concentration                                                                      |
| fine-dust-conc01           | Number:Density                     |       | Fine Dust concentration >1 µm                                                            |
| fine-dust-conc02-5         | Number:Density                     |       | Fine Dust concentration >2.5 µm                                                          |
| fine-dust-conc10           | Number:Density                     |       | Fine Dust concentration >10 µm                                                           |
| fine-dust-cnt00-3          | Number:Dimensionless               |       | Fine Dust >0,3 µm                                                                        |
| fine-dust-cnt00-5          | Number:Dimensionless               |       | Fine Dust >0,5 µm                                                                        |
| fine-dust-cnt01            | Number:Dimensionless               |       | Fine Dust >1 µm                                                                          |
| fine-dust-cnt02-5          | Number:Dimensionless               |       | Fine Dust >2,5 µm                                                                        |
| fine-dust-cnt05            | Number:Dimensionless               |       | Fine Dust >5 µm                                                                          |
| fine-dust-cnt10            | Number:Dimensionless               |       | Fine Dust >10 µm                                                                         |
| avg-fine-dust-size         | Number:Length                      |       | Average size of Fine Dust [experimental]                                                 |
| pm1-sps30                  | Number:Density                     |       | Fine Dust concentration >1 µm of the optional SPS30 sensor (advanced)                    |
| pm2-5-sps30                | Number:Density                     |       | Fine Dust concentration >2.5 µm of the optional SPS30 sensor (advanced)                  |
| pm4-sps30                  | Number:Density                     |       | Fine Dust concentration >4 µm of the optional SPS30 sensor (advanced)                    |
| pm10-sps30                 | Number:Density                     |       | Fine Dust concentration >10 µm of the optional SPS30 sensor (advanced)                   |
| cnt0-5-sps30               | Number                             |       | Fine Dust >0.5 µm per cm³ of the optional SPS30 sensor (advanced)                        |
| cnt1-sps30                 | Number                             |       | Fine Dust >1 µm per cm³ of the optional SPS30 sensor (advanced)                          |
| cnt2-5-sps30               | Number                             |       | Fine Dust >2.5 µm per cm³ of the optional SPS30 sensor (advanced)                        |
| cnt4-sps30                 | Number                             |       | Fine Dust >4 µm per cm³ of the optional SPS30 sensor (advanced)                          |
| cnt10-sps30                | Number                             |       | Fine Dust >10 µm per cm³ of the optional SPS30 sensor (advanced)                         |
| avg-fine-dust-size-sps30   | Number:Length                      |       | Average size of Fine Dust of the optional SPS30 sensor (advanced)                        |
| sound                      | Number:Dimensionless               |       | Noise                                                                                    |
| sound-max                  | Number:Dimensionless               |       | Maximum A-weighted noise in dB (advanced)                                                |
| flow1                      | Number:VolumetricFlowRate          |       | Air flow 1 in standard litres per minute at 20 °C and 1013 hPa (advanced)                |
| flow2                      | Number:VolumetricFlowRate          |       | Air flow 2 in standard litres per minute at 20 °C and 1013 hPa (advanced)                |
| flow3                      | Number:VolumetricFlowRate          |       | Air flow 3 in standard litres per minute at 20 °C and 1013 hPa (advanced)                |
| flow4                      | Number:VolumetricFlowRate          |       | Air flow 4 in standard litres per minute at 20 °C and 1013 hPa (advanced)                |

### Events

| channel                    | type                               | rw    | description                                                                              |
|----------------------------|------------------------------------|-------|------------------------------------------------------------------------------------------|
| fire-alarm-active          | Switch                             |       | ON while the device raises a fire alarm                                                  |
| gas-alarm-active           | Switch                             |       | ON while the device raises a gas alarm                                                   |
| door-event                 | Number                             |       | Door Event (experimental, might not work reliably)                                       |
| window-event               | Number                             |       | Window Event (experimental, might not work reliably)                                     |
| window-open                | Number                             |       | Window Open (experimental, might not work reliably)                                      |
| person                     | Number                             |       | Person (experimental, might not work reliably)                                           |

### Maximum Errors

Each measurement with an uncertainty has an advanced error channel `maxerr#<channel>-maxerr`, for example `maxerr#co2-maxerr`.
Each `-maxerr` channel has the Item type and unit of its measurement, for example `ch2o-m10-maxerr` contains the formaldehyde measurement uncertainty in µg/m³.
Temperature and dew point uncertainties are temperature differences in K.
For maximum noise, the error channel is `sound-max-maxerr`, distinct from `sound-maxerr` of the noise.

### Settings

| channel                    | type                               | rw    | description                                                                              |
|----------------------------|------------------------------------|-------|------------------------------------------------------------------------------------------|
| device-name                | String                             | rw    | Device Name                                                                              |
| room-type                  | String                             | rw    | Type of room                                                                             |
| room-height                | Number:Length                      | rw    | Room height, 0 if not set                                                                |
| room-area                  | Number:Area                        | rw    | Room area, 0 if not set                                                                  |
| location                   | Location                           | rw    | Location of air-Q device                                                                 |
| altitude                   | Number:Length                      | rw    | Height above sea level for the relative pressure, active after a device restart          |
| altitude-error             | Number:Length                      | rw    | Estimated error of the altitude                                                          |
| country-code               | String                             | rw    | Country (ISO 3166) for the summer/winter time switch and the cloud endpoint              |
| time-server                | String                             | rw    | Name of Timeserver address                                                               |
| log-level                  | String                             | w     | Logging level                                                                            |
| wifi                       | Switch                             |       | WLAN on or off, read-only so the device cannot be taken off the network                  |
| ssid                       | String                             |       | Configured WLAN networks, add networks with the `setWifi` action                         |
| wifi-info                  | Switch                             | rw    | Show WLAN status with LED                                                                |
| night-mode-activated       | Switch                             | rw    | Night mode on or off; the other night mode settings are kept while it is off             |
| night-mode-start-day       | Number:Time                        | rw    | Start of day operation, minutes since midnight UTC                                       |
| night-mode-start-night     | Number:Time                        | rw    | End of day operation, minutes since midnight UTC                                         |
| night-mode-brightness-day  | Number                             | rw    | Brightness of LED during the day, 0 (off) to 10                                          |
| night-mode-brightness-night| Number                             | rw    | Brightness of LED at night, 0 (off) to 10                                                |
| night-mode-fan-night-off   | Switch                             | rw    | Switch off fan at night                                                                  |
| night-mode-wifi-night-off  | Switch                             | rw    | Switch off WLAN at night                                                                 |
| night-mode-alarm-night-off | Switch                             | rw    | Switch off acoustic signals at night; fire and gas alarms are still given                |
| night-mode-last-time-switch| Number                             |       | Undocumented, presumably the last summer/winter time switch, 0 if none                   |
| fire-alarm                 | Switch                             | rw    | Send Fire Alarm if certain levels are met                                                |
| gas-alarm                  | Switch                             | rw    | Send Gas Alarm if certain levels are met                                                 |
| alarm-forwarding           | Switch                             | rw    | Forward gas or fire alarm to other air-Q devices in the household                        |
| sound-info                 | String                             |       | Acoustic signals for measurement thresholds                                              |
| averaging                  | Number                             | rw    | Number of measurements averaged, 1 = off (device default 30)                             |
| averaging-rhythm           | Number:Time                        | rw    | Rhythm of measurement for historic average in seconds                                    |
| power-freq-suppression     | String                             | rw    | Power Frequency                                                                          |
| auto-drift-compensation    | Switch                             | rw    | Compensate automatic drift                                                               |
| advanced-data-processing   | Switch                             | rw    | Use advanced algorithms eg. for open window or presence of a person                      |
| building-standard          | String                             | rw    | Building standard for the VOC evaluation: None, WELL or RESET                            |
| ppm-and-ppb                | Switch                             | rw    | Deprecated: CO in ppm, NO₂, O₃ and SO₂ in ppb, unless units are configured on the device |
| warmup-phase               | Switch                             | rw    | Warm-up phase of the sensors, OFF aborts it                                              |
| cloud-upload               | Switch                             | rw    | Upload to air-Q cloud                                                                    |
| auto-update                | Number:Time                        | rw    | Days between automatic firmware updates, 0 = off (device default 90)                     |

Measurements are reported in the units configured on the device (`units`, or the deprecated `ppm&ppb`) and converted to the channel's unit.
Gas concentrations are converted between ppm, ppb or % and mg/m³ or µg/m³ with the sensor's molar mass and the measured temperature and pressure.

These channels support scalar readings as well as `[value, uncertainty]` pairs; for scalars, the error channel is `UNDEF`.
Absent readings for these channels are silently ignored, since not every device has every sensor.
Explicit `null` or malformed readings set these measurements and their error channels to `UNDEF` and recover on the next valid reading.
Other legacy measurement conversions retain their existing behavior.

Day/night start channels use minutes (`min`) since midnight UTC, from 0 to 1439; for example, 480 min corresponds to 08:00.
The binding converts these numeric values to and from the device's `HH:mm` representation.
Time commands accept quantities in compatible units or plain integers in the channel's hinted unit.
Day/night times require whole minutes, and averaging delay requires whole non-negative seconds.
Power-frequency suppression is a String setting accepting `50Hz`, `60Hz`, or `50Hz+60Hz`.

Set the linked TVOC Item's `unit` metadata to `ppb`.
Without it, a `Number:Dimensionless` Item can store the reading in unit `one`: 2353 ppb becomes 0.000002353 and may display as zero when rounded.

### Reporting Missing Measurements

To diagnose unsupported data points, enable trace logging in the openHAB console:

```shell
log:set TRACE org.openhab.binding.airq
```

Look for `Received measurement data` entries after the next poll.
These include numeric values for known measurements and the names of other received fields with their values replaced by `<omitted>`.
Passwords, device identifiers, location, configuration, free-form status text, and unknown values are not included.
Malformed responses are not logged verbatim either.
Include a sanitized trace entry and your device's sensor package when reporting a missing measurement.
Restore normal logging afterwards:

```shell
log:set DEFAULT org.openhab.binding.airq
```

## Actions

The binding registers the [Thing Action](https://www.openhab.org/docs/configuration/rules-dsl.html#thing-actions) `setWifi` under the `airq` scope.
It adds a Wi-Fi network to the air-Q and returns `true` if the device accepted it.

| Parameter          | Description                                                                                          |
|--------------------|------------------------------------------------------------------------------------------------------|
| `String ssid`      | Name of the network, 1 to 32 bytes                                                                   |
| `String password`  | Wi-Fi key, empty for an unsecured network                                                            |
| `String bssid`     | Optional access point address as 12 hex digits, e.g. `a0c86d65ee71`; `null` or empty to omit it      |
| `boolean hidden`   | Whether the network is hidden                                                                        |
| `boolean restart`  | Whether the air-Q restarts to connect; otherwise it remembers the network and stays connected        |

::: warning
Wrong credentials together with `restart` take the air-Q off the network.
openHAB can then no longer reach it, and it can only be set up again with the air-Q app or its hotspot.
For the same reason, the binding does not switch Wi-Fi off.
:::

```java
val airqActions = getActions("airq", "airq:airq:1")
val accepted = airqActions.setWifi("Your Wifi SSID", "Your Wifi key", null, false, true)
```

## Usage with Docker

This binding requires the JVM cryptographic strength policy to be set to "unlimited".
Otherwise the connection to the device will fail.
See the [openHAB Docker image documentation](https://github.com/openhab/openhab-docker/blob/main/README.md#java-cryptographic-strength-policy) for details.

## Example

### air-Q.things

```java
Thing airq:airq:1 "air-Q" [ ipAddress="192.168.0.68", password="myAirQPassword" ]
```

### air-Q.items

```java
String                airQ_status                   "Status of Sensors"                                    {channel="airq:airq:1:status#status"}
Number:Time           airQ_uptime                   "Uptime"                                               {unit="s",channel="airq:airq:1:status#uptime"}
String                airQ_boots                    "Boot counters"                                        {channel="airq:airq:1:status#boots"}
DateTime              airQ_timestamp                "TimeStamp [%1$td.%1$tm.%1$tY %1$tH:%1$tM]"            {channel="airq:airq:1:status#timestamp"}
Number:Time           airQ_measureTime              "Time needed for measurement"                          {unit="ms",channel="airq:airq:1:status#measure-time"}
Switch                airQ_daytime                  "Day operation"                                        {channel="airq:airq:1:status#daytime"}
Switch                airQ_initialCalFinished       "Initial calibration has finished"                     {channel="airq:airq:1:status#initial-cal-finished"}
String                airQ_userCalib                "Last sensor calibration"                              {channel="airq:airq:1:status#user-calib"}
Number:Power          airQ_wifiRssi                 "Wi-Fi Signal Strength"                                {channel="airq:airq:1:status#wifi-rssi"}
String                airQ_WLAN_config_SSID         "Connected WLAN SSID"                                  {channel="airq:airq:1:status#wlan-config-ssid"}
String                airQ_WLAN_config_IPAddress    "Assigned IP address"                                  {channel="airq:airq:1:status#wlan-config-ip-address"}
String                airQ_WLAN_config_netMask      "Network mask"                                         {channel="airq:airq:1:status#wlan-config-net-mask"}
String                airQ_WLAN_config_gateway      "Network Gateway"                                      {channel="airq:airq:1:status#wlan-config-gateway"}
String                airQ_WLAN_config_MAC          "MAC Address"                                          {channel="airq:airq:1:status#wlan-config-mac"}
String                airQ_WLAN_config_BSSID        "Network BSSID"                                        {channel="airq:airq:1:status#wlan-config-bssid"}
String                airQ_cloudConnection          "Connection to the air-Q cloud"                        {channel="airq:airq:1:status#cloud-connection"}

Number:Temperature    airQ_temperature              "Temperature"                                          {channel="airq:airq:1:measurements#temperature"}
Number:Dimensionless  airQ_humidityRelative         "Humidity"                                             {channel="airq:airq:1:measurements#humidity-relative"}
Number:Density        airQ_humidityAbsolute         "Absolute Humidity"                                    {channel="airq:airq:1:measurements#humidity-absolute"}
Number:Temperature    airQ_dewpt                    "Dew Point"                                            {channel="airq:airq:1:measurements#dewpt"}
Number                airQ_dHdt                     "Change of Humidity"                                   {channel="airq:airq:1:measurements#d-hdt"}
Number:Pressure       airQ_pressure                 "Pressure"                                             {channel="airq:airq:1:measurements#pressure"}
Number:Dimensionless  airQ_health                   "Health Index"                                         {channel="airq:airq:1:measurements#health"}
Number:Dimensionless  airQ_performance              "Performance Index"                                    {channel="airq:airq:1:measurements#performance"}
Number:Dimensionless  airQ_Virus_free               "Virus-Free index"                                     {unit="%",channel="airq:airq:1:measurements#virus-free"}
Number:Dimensionless  airQ_Mold_free                "Mold-Free index"                                      {unit="%",channel="airq:airq:1:measurements#mold-free"}
Number:Dimensionless  airQ_co2                      "CO2 Concentration"                                    {channel="airq:airq:1:measurements#co2"}
Number                airQ_dCO2dt                   "Change of CO2 Concentration"                          {channel="airq:airq:1:measurements#d-co2dt"}
Number:Dimensionless  airQ_voc                      "VOC concentration"                                    {unit="ppb",channel="airq:airq:1:measurements#tvoc"}
Number                airQ_co                       "CO Concentration"                                     {channel="airq:airq:1:measurements#co"}
Number:Density        airQ_no2                      "NO2 concentration"                                    {channel="airq:airq:1:measurements#no2"}
Number:Density        airQ_o3                       "O3 concentration"                                     {channel="airq:airq:1:measurements#o3"}
Number:Dimensionless  airQ_o2                       "Oxygen concentration"                                 {channel="airq:airq:1:measurements#o2"}
Number:Density        airQ_so2                      "SO2 concentration"                                    {channel="airq:airq:1:measurements#so2"}
Number:RadiationSpecificActivity airQ_radon                    "Radon Concentration"                                  {channel="airq:airq:1:measurements#radon"}
Number:Density        airQ_fineDustConc01           "Fine Dust Concentration >1µ"                          {channel="airq:airq:1:measurements#fine-dust-conc01"}
Number:Density        airQ_fineDustConc02_5         "Fine Dust Concentration >2.5µ"                        {channel="airq:airq:1:measurements#fine-dust-conc02-5"}
Number:Density        airQ_fineDustConc10           "Fine Dust Concentration >10µ"                         {channel="airq:airq:1:measurements#fine-dust-conc10"}
Number:Dimensionless  airQ_fineDustCnt00_3          "Fine Dust >0,3 µm"                                    {channel="airq:airq:1:measurements#fine-dust-cnt00-3"}
Number:Dimensionless  airQ_fineDustCnt00_5          "Fine Dust >0,5 µm"                                    {channel="airq:airq:1:measurements#fine-dust-cnt00-5"}
Number:Dimensionless  airQ_fineDustCnt01            "Fine Dust >1,0 µm"                                    {channel="airq:airq:1:measurements#fine-dust-cnt01"}
Number:Dimensionless  airQ_fineDustCnt02_5          "Fine Dust >2,5 µm"                                    {channel="airq:airq:1:measurements#fine-dust-cnt02-5"}
Number:Dimensionless  airQ_fineDustCnt05            "Fine Dust >5 µm"                                      {channel="airq:airq:1:measurements#fine-dust-cnt05"}
Number:Dimensionless  airQ_fineDustCnt10            "Fine Dust >10 µm"                                     {channel="airq:airq:1:measurements#fine-dust-cnt10"}
Number:Length         airQ_avgFineDustSize          "Average Size of Fine Dust"                            {channel="airq:airq:1:measurements#avg-fine-dust-size"}
Number:Dimensionless  airQ_sound                    "Noise"                                                {channel="airq:airq:1:measurements#sound"}

Switch                airQ_fireAlarmActive          "Fire Alarm Active"                                    {channel="airq:airq:1:events#fire-alarm-active"}
Switch                airQ_gasAlarmActive           "Gas Alarm Active"                                     {channel="airq:airq:1:events#gas-alarm-active"}
Number                airQ_doorEvent                "Door Event (exp.)"                                    {channel="airq:airq:1:events#door-event"}
Number                airQ_windowEvent              "Window Event (exp.)"                                  {channel="airq:airq:1:events#window-event"}
Number                airQ_windowOpen               "Window Open (exp.)"                                   {channel="airq:airq:1:events#window-open"}
Number                airQ_person                   "Person (exp.)"                                        {channel="airq:airq:1:events#person"}

Number:Temperature    airQ_temperature_maxerr       "Maximum error of Temperature"                         {unit="K",channel="airq:airq:1:maxerr#temperature-maxerr"}
Number:Dimensionless  airQ_humidity_maxerr          "Maximum error of Humidity"                            {channel="airq:airq:1:maxerr#humidity-relative-maxerr"}
Number:Density        airQ_humidity_abs_maxerr      "Maximum error of Absolute Humidity"                   {channel="airq:airq:1:maxerr#humidity-absolute-maxerr"}
Number:Temperature    airQ_dewpt_maxerr             "Maximum error of Dew Point"                           {unit="K",channel="airq:airq:1:maxerr#dewpt-maxerr"}
Number:Pressure       airQ_pressure_maxerr          "Maximum error of Pressure"                            {channel="airq:airq:1:maxerr#pressure-maxerr"}
Number:Dimensionless  airQ_virus_free_maxerr        "Maximum error of Virus-Free"                          {unit="%",channel="airq:airq:1:maxerr#virus-free-maxerr"}
Number:Dimensionless  airQ_mold_free_maxerr         "Maximum error of Mold-Free"                           {unit="%",channel="airq:airq:1:maxerr#mold-free-maxerr"}
Number:Dimensionless  airQ_co2_maxerr               "Maximum error of CO2 Concentration"                   {channel="airq:airq:1:maxerr#co2-maxerr"}
Number:Dimensionless  airQ_voc_maxerr               "Maximum error of VOC concentration"                   {channel="airq:airq:1:maxerr#tvoc-maxerr"}
Number:Density        airQ_no2_maxerr               "Maximum error of NO2 concentration"                   {channel="airq:airq:1:maxerr#no2-maxerr"}
Number:Density        airQ_o3_maxerr                "Maximum error of O3 concentration"                    {channel="airq:airq:1:maxerr#o3-maxerr"}
Number:Dimensionless  airQ_oxygen_maxerr            "Maximum error of Oxygen concentration"                {channel="airq:airq:1:maxerr#o2-maxerr"}
Number:Density        airQ_so2_maxerr               "Maximum error of SO2 concentration"                   {channel="airq:airq:1:maxerr#so2-maxerr"}
Number:Density        airQ_pm1_maxerr               "Maximum error of Fine Dust Concentration >1µ"         {channel="airq:airq:1:maxerr#fine-dust-conc01-maxerr"}
Number:Density        airQ_pm2_5_maxerr             "Maximum error of Fine Dust Concentration >2.5µ"       {channel="airq:airq:1:maxerr#fine-dust-conc02-5-maxerr"}
Number:Density        airQ_pm10_maxerr              "Maximum error of Fine Dust Concentration >10µ"        {channel="airq:airq:1:maxerr#fine-dust-conc10-maxerr"}
Number:Dimensionless  airQ_cnt03_maxerr             "Maximum error of Fine Dust >0,3 µm"                   {channel="airq:airq:1:maxerr#fine-dust-cnt00-3-maxerr"}
Number:Dimensionless  airQ_cnt05_maxerr             "Maximum error of Fine Dust >0,5 µm"                   {channel="airq:airq:1:maxerr#fine-dust-cnt00-5-maxerr"}
Number:Dimensionless  airQ_cnt1_maxerr              "Maximum error of Fine Dust >1,0 µm"                   {channel="airq:airq:1:maxerr#fine-dust-cnt01-maxerr"}
Number:Dimensionless  airQ_cnt25_maxerr             "Maximum error of Fine Dust >2,5 µm"                   {channel="airq:airq:1:maxerr#fine-dust-cnt02-5-maxerr"}
Number:Dimensionless  airQ_cnt5_maxerr              "Maximum error of Fine Dust >5 µm"                     {channel="airq:airq:1:maxerr#fine-dust-cnt05-maxerr"}
Number:Dimensionless  airQ_cnt10_maxerr             "Maximum error of Fine Dust >10 µm"                    {channel="airq:airq:1:maxerr#fine-dust-cnt10-maxerr"}
Number:Dimensionless  airQ_sound_maxerr             "Maximum error of Noise"                               {channel="airq:airq:1:maxerr#sound-maxerr"}

String                airQ_deviceName               "Device Name"                                          {channel="airq:airq:1:settings#device-name"}
String                airQ_roomType                 "Type of room"                                         {channel="airq:airq:1:settings#room-type"}
Number:Length         airQ_roomHeight               "Room height"                                          {unit="m",channel="airq:airq:1:settings#room-height"}
Number:Area           airQ_roomArea                 "Room area"                                            {unit="m²",channel="airq:airq:1:settings#room-area"}
Location              airQ_location                 "Location of air-Q device"                             {channel="airq:airq:1:settings#location"}
Number:Length         airQ_altitude                 "Altitude"                                             {unit="m",channel="airq:airq:1:settings#altitude"}
Number:Length         airQ_altitudeError            "Altitude error"                                       {unit="m",channel="airq:airq:1:settings#altitude-error"}
String                airQ_countryCode              "Country"                                              {channel="airq:airq:1:settings#country-code"}
String                airQ_timeServer               "Name of Timeserver address"                           {channel="airq:airq:1:settings#time-server"}
String                airQ_logLevel                 "Logging level"                                        {channel="airq:airq:1:settings#log-level"}
Switch                airQ_wifi                     "WLAN on or off"                                       {channel="airq:airq:1:settings#wifi"}
String                airQ_SSID                     "Configured WLAN networks"                             {channel="airq:airq:1:settings#ssid"}
Switch                airQ_wifiInfo                 "Show WLAN status with LED"                            {channel="airq:airq:1:settings#wifi-info"}
Switch                airQ_nightMode_activated      "Night mode"                                           {channel="airq:airq:1:settings#night-mode-activated"}
Number:Time           airQ_nightMode_startDay       "Start of day operation"                               {unit="min",channel="airq:airq:1:settings#night-mode-start-day"}
Number:Time           airQ_nightMode_startNight     "End of day operation"                                 {unit="min",channel="airq:airq:1:settings#night-mode-start-night"}
Number                airQ_nightMode_brightnessDay  "Brightness of LED during the day"                     {channel="airq:airq:1:settings#night-mode-brightness-day"}
Number                airQ_nightMode_brightnessNight "Brightness of LED at night"                           {channel="airq:airq:1:settings#night-mode-brightness-night"}
Switch                airQ_nightMode_fanNightOff    "Switch off fan at night"                              {channel="airq:airq:1:settings#night-mode-fan-night-off"}
Switch                airQ_nightMode_wifiNightOff   "Switch off WLAN at night"                             {channel="airq:airq:1:settings#night-mode-wifi-night-off"}
Switch                airQ_nightMode_alarmNightOff  "Switch off acoustic signals at night"                 {channel="airq:airq:1:settings#night-mode-alarm-night-off"}
Number                airQ_nightMode_lastTimeSwitch "Last summer/winter time switch"                       {channel="airq:airq:1:settings#night-mode-last-time-switch"}
Switch                airQ_fireAlarm                "Send Fire Alarm if certain levels are met"            {channel="airq:airq:1:settings#fire-alarm"}
Switch                airQ_gasAlarm                 "Send Gas Alarm if certain levels are met"             {channel="airq:airq:1:settings#gas-alarm"}
Switch                airQ_alarmForwarding          "Forward gas or fire alarm to other air-Q devices in the household" {channel="airq:airq:1:settings#alarm-forwarding"}
String                airQ_soundInfo                "Acoustic threshold signals"                           {channel="airq:airq:1:settings#sound-info"}
Number                airQ_averaging                "Averaging window"                                     {channel="airq:airq:1:settings#averaging"}
Number:Time           airQ_averagingRhythm          "Rhythm of measurement for historic average"           {unit="s",channel="airq:airq:1:settings#averaging-rhythm"}
String                airQ_powerFreqSuppression     "Power Frequency"                                      {channel="airq:airq:1:settings#power-freq-suppression"}
Switch                airQ_autoDriftCompensation    "Compensate automatic drift"                           {channel="airq:airq:1:settings#auto-drift-compensation"}
Switch                airQ_advancedDataProcessing   "Use advanced algorithms eg. for open window or presence of a person" {channel="airq:airq:1:settings#advanced-data-processing"}
String                airQ_buildingStandard         "Building standard"                                    {channel="airq:airq:1:settings#building-standard"}
Switch                airQ_ppm_and_ppb              "Output CO as ppm and NO2, O3 and SO2 as ppb value instead of mg/m3" {channel="airq:airq:1:settings#ppm-and-ppb"}
Switch                airQ_warmupPhase              "Output Data as Warmup Phase"                          {channel="airq:airq:1:settings#warmup-phase"}
Switch                airQ_cloudUpload              "Upload to air-Q cloud"                                {channel="airq:airq:1:settings#cloud-upload"}
Number:Time           airQ_autoUpdate               "Automatic firmware update interval"                   {unit="d",channel="airq:airq:1:settings#auto-update"}
```
