# air-Q Binding

The air-Q Binding integrates the air analyzer <a href="http://www.air-q.com">air-Q</a> device into the openHAB system.

With the binding, it is possible to subscribe to all data delivered by the air-Q device.

<img src="src/main/resources/image_air-Q.png" alt="air-Q image" width="400px" height="324px" />

## Supported Things

Only one Thing is supported: The `air-Q` device.
This Binding was tested with an `air-Q Pro` device with 14 sensors. It also works with an `air-Q` device with 11 sensors.

## Discovery

Auto-discovery is not yet supported.

## Binding Configuration

The binding does not need to be configured.

## Thing Configuration

The air-Q Thing must be configured with (both mandatory):
| Parameter | Description                        |
|-----------|------------------------------------|
| ipAddress | Network address, e.g. 192.168.0.68 |
| password  | Password of the air-Q device       |

The Thing provides the following properties:
| Parameter              | Description                   |
|------------------------|-------------------------------|
| id                     | Device ID                     |
| hardwareVersion        | Hardware version              |
| softwareVersion        | Firmware version              |
| sensorList             | Available sensors             |
| sensorInfo             | Information about the sensors |
| industryVersion        | Industry version              |

## Channels

The air-Q Thing offers access to all sensor data of the air-Q, according to its version.
This includes also the Maximum Error per sensor value.
For the Maximum Error channels just add `_maxerr` to the channel names.

| channel                   | type     | description                                                         |
|---------------------------|----------|---------------------------------------------------------------------|
| status                    | String   | Status of the sensors                                               |
| avgFineDustSize           | Number   | Average size of Fine Dust [experimental]                            |
| fineDustCnt00_3           | Number   | Fine Dust >0,3 &mu;m                                                |
| fineDustCnt00_5           | Number   | Fine Dust >0,5 &mu;m                                                |
| fineDustCnt01             | Number   | Fine Dust >1 &mu;m                                                  |
| fineDustCnt02_5           | Number   | Fine Dust >2,5 &mu;m                                                |
| fineDustCnt05             | Number   | Fine Dust >5 &mu;m                                                  |
| fineDustCnt10             | Number   | Fine Dust >10 &mu;m                                                 |
| co                        | Number   | CO concentration                                                    |
| co2                       | Number   | CO<sub>2</sub> concentration                                        |
| dCO2dt                    | Number   | Change of CO<sub>2</sub> concentration                              |
| dHdt                      | Number   | Change of Humidity                                                  |
| dewpt                     | Number   | Dew Point                                                           |
| doorEvent                 | Switch   | Door Event (experimental)                                           |
| health                    | Number   | Health Index                                                        |
| humidityRelative          | Number   | Humidity in percent                                                 |
| humidityAbsolute          | Number   | Absolute Humidity                                                   |
| measureTime               | Number   | Milliseconds needed for measurement                                 |
| no2                       | Number   | NO<sub>2</sub> concentration                                        |
| o3                        | Number   | Ozone (<sub>3</sub>) concentration                                  |
| o2                        | Number   | Oxygen (O<sub>2</sub>) concentration                                |
| performance               | Number   | Performance index                                                   |
| fineDustConc01            | Number   | Fine Dust concentration >1 &mu;m                                    |
| fineDustConc02_5          | Number   | Fine Dust concentration >2.5 &mu;m                                  |
| fineDustConc10            | Number   | Fine Dust concentration >10 &mu;m                                   |
| pressure                  | Number   | Pressure                                                            |
| so2                       | Number   | SO<sub>2</sub> concentration                                        |
| sound                     | Number   | Noise                                                               |
| temperature               | Number   | Temperature                                                         |
| timestamp                 | Time     | Timestamp of measurement                                            |
| voc                       | Number   | VOC concentration                                                   |
| uptime                    | Number   | uptime in seconds                                                   |
| wifi                      | Switch   | WLAN on or off                                                      |
| SSID                      | String   | WLAN SSID                                                           |
| password                  | String   | Device Password                                                     |
| wifiInfo                  | Switch   | Show WLAN status with LED                                           |
| timeServer                | String   | Name of Timeserver address                                          |
| location                  | Location | Location of air-Q device                                            |
| nightmode_StartDay        | String   | Time to start day operation                                         |
| nightmode_StartNight      | String   | End of day operation                                                |
| nightmode_BrightnessDay   | Number   | Brightness of LED during the day                                    |
| nightmode_BrightnessNight | Number   | Brightness of LED at night                                          |
| nightmode_FanNightOff     | Switch   | Switch off fan at night                                             |
| nightmode_WifiNightOff    | Switch   | Switch off WLAN at night                                            |
| devicename                | String   | Device Name                                                         |
| roomType                  | String   | Type of room                                                        |
| logLevel                  | String   | Logging level                                                       |
| deleteKey                 | String   | Settings to be deleted                                              |
| fireAlarm                 | Switch   | Send Fire Alarm if certain levels are met                           |
| WLAN_config_gateway       | String   | Network Gateway                                                     |
| WLAN_config_MAC           | String   | MAC Address                                                         |
| WLAN_config_SSID          | String   | WLAN SSID                                                           |
| WLAN_config_IPAddress     | String   | Assigned IP address                                                 |
| WLAN_config_netMask       | String   | Network mask                                                        |
| WLAN_config_BSSID         | String   | Network BSSID                                                       |
| cloudUpload               | Switch   | Upload to air-Q cloud                                               |
| averagingRhythm           | Number   | Rhythm of measurement for historic average                          |
| powerFreqSuppression      | String   | Power Frequency                                                     |
| autoDriftCompensation     | Switch   | Compensate automatic drift                                          |
| autoUpdate                | Switch   | Install Firmware updates automatically                              |
| advancedDataProcessing    | Switch   | Use advanced algorithms eg. for open window or presence of a person |
| ppm_and_ppb               | Switch   | Output CO as ppm and NO2, O3 and SO2 as ppb value instead of mg/m3  |
| gasAlarm                  | Switch   | Send Gas Alarm if certain levels are met                            |
| id                        | String   | Device ID, retrieved from configuration                             |
| soundPressure             | Switch   | Sound Pressure Level                                                |
| alarmForwarding           | Switch   | Forward gas or fire alarm to other air-Q devices in the household   |
| userCalib                 | String   | Last sensor calibration                                             |
| initialCalFinished        | Switch   | Initial calibration has finished                                    |
| averaging                 | Switch   | Do an average                                                       |
| errorBars                 | Switch   | Calculate Maximum Errors                                            |

## Example

### air-Q.things

```
Thing airq:airq:1 "air-Q" [ ipAddress="192.168.0.68", password="myAirQPassword" ]
```

### air-Q.items

```
String                airQ_status                 "Status of Sensors"                     {channel="airq:airq:1:status"}
Number:Length         airQ_avgFineDustSize        "Average Size of Fine Dust"             {channel="airq:airq:1:avgFineDustSize"}
Number:Dimensionless  airQ_fineDustCnt00_3        "Fine Dust >0,3 µm"                     {channel="airq:airq:1:fineDustCnt00_3"}
Number:Dimensionless  airQ_fineDustCnt00_5        "Fine Dust >0,5 µm"                     {channel="airq:airq:1:fineDustCnt00_5"}
Number:Dimensionless  airQ_fineDustCnt01          "Fine Dust >1,0 µm"                     {channel="airq:airq:1:fineDustCnt01"}
Number:Dimensionless  airQ_fineDustCnt02_5        "Fine Dust >2,5 µm"                     {channel="airq:airq:1:fineDustCnt02_5"}
Number:Dimensionless  airQ_fineDustCnt05          "Fine Dust >5 µm"                       {channel="airq:airq:1:fineDustCnt05"}
Number:Dimensionless  airQ_fineDustCnt10          "Fine Dust >10 µm"                      {channel="airq:airq:1:fineDustCnt10"}
Number                airQ_co                     "CO Concentration"                      {channel="airq:airq:1:co"}
Number                airQ_co2                    "CO2 Concentration"                     {channel="airq:airq:1:co2"}
Number                airQ_dCO2dt                 "Change of CO2 Concentration"           {channel="airq:airq:1:dCO2dt"}
Number                airQ_dHdt                   "Change of Humidity"                    {channel="airq:airq:1:dHdt"}
Number:Temperature    airQ_dewpt                  "Dew Point"                             {channel="airq:airq:1:dewpt"}
Number                airQ_doorEvent              "Door Event (exp.)"                     {channel="airq:airq:1:doorEvent"}
Number:Dimensionless  airQ_health                 "Health Index"                          {channel="airq:airq:1:health"}
Number:Dimensionless  airQ_humidityRelative       "Humidity"                              {channel="airq:airq:1:humidityRelative"}
Number                airQ_humidityAbsolute       "Absolute Humidity"                     {channel="airq:airq:1:humidityAbsolute"}
Number                airQ_measureTime            "Time needed for measurement"           {channel="airq:airq:1:measureTime"}
Number                airQ_no2                    "NO2 concentration"                     {channel="airq:airq:1:no2"}
Number                airQ_o3                     "O3 concentration"                      {channel="airq:airq:1:o3"}
Number:Dimensionless  airQ_o2                     "Oxygen concentration"                  {channel="airq:airq:1:o2"}
Number:Dimensionless  airQ_performance            "Performance Index"                     {channel="airq:airq:1:performance"}
Number                airQ_fineDustConc01         "Fine Dust Concentration >1µ"           {channel="airq:airq:1:fineDustConc01"}
Number                airQ_fineDustConc02_5       "Fine Dust Concentration >2.5µ"         {channel="airq:airq:1:fineDustConc02_5"}
Number                airQ_fineDustConc10         "Fine Dust Concentration >10µ"          {channel="airq:airq:1:fineDustConc10"}
Number:Pressure       airQ_pressure               "Pressure"                              {channel="airq:airq:1:pressure"}
Number                airQ_so2                    "SO2 concentration"                     {channel="airq:airq:1:so2"}
Number                airQ_sound                  "Noise"                                 {channel="airq:airq:1:sound"}
Number:Temperature    airQ_temperature            "Temperature"                           {channel="airq:airq:1:temperature"}
DateTime              airQ_timestamp              "Time stamp"                            {channel="airq:airq:1:timestamp"}
Number                airQ_voc                    "VOC concentration"                     {channel="airq:airq:1:voc"}
Number                airQ_uptime                 "Uptime"                                {channel="airq:airq:1:uptime"}

Number:Dimensionless  airQ_cnt03_maxerr        "Maximum error of Fine Dust >0,3 µm"             {channel="airq:airq:1:cnt0_3_maxerr"}
Number:Dimensionless  airQ_cnt05_maxerr        "Maximum error of Fine Dust >0,5 µm"             {channel="airq:airq:1:cnt0_5_maxerr"}
Number:Dimensionless  airQ_cnt1_maxerr         "Maximum error of Fine Dust >1,0 µm"             {channel="airq:airq:1:cnt1_maxerr"}
Number:Dimensionless  airQ_cnt25_maxerr        "Maximum error of Fine Dust >2,5 µm"             {channel="airq:airq:1:cnt2_5_maxerr"}
Number:Dimensionless  airQ_cnt5_maxerr         "Maximum error of Fine Dust >5 µm"               {channel="airq:airq:1:cnt5_maxerr"}
Number:Dimensionless  airQ_cnt10_maxerr        "Maximum error of Fine Dust >10 µm"              {channel="airq:airq:1:cnt10_maxerr"}
Number:Dimensionless  airQ_co2_maxerr          "Maximum error of CO2 Concentration"             {channel="airq:airq:1:co2_maxerr"}
Number:Dimensionless  airQ_dewpt_maxerr        "Maximum error of Dew Point"                     {channel="airq:airq:1:dewpt_maxerr"}
Number:Dimensionless  airQ_humidity_maxerr     "Maximum error of Humidity"                      {channel="airq:airq:1:humidity_maxerr"}
Number:Dimensionless  airQ_humidity_abs_maxerr "Maximum error of Absolute Humidity"             {channel="airq:airq:1:humidity_abs_maxerr"}
Number:Dimensionless  airQ_no2_maxerr          "Maximum error of NO2 concentration"             {channel="airq:airq:1:no2_maxerr"}
Number:Dimensionless  airQ_o3_maxerr           "Maximum error of O3 concentration"              {channel="airq:airq:1:o3_maxerr"}
Number:Dimensionless  airQ_oxygen_maxerr       "Maximum error of Oxygen concentration"          {channel="airq:airq:1:oxygen_maxerr"}
Number:Dimensionless  airQ_pm1_maxerr          "Maximum error of Fine Dust Concentration >1µ"   {channel="airq:airq:1:pm1_maxerr"}
Number:Dimensionless  airQ_pm2_5_maxerr        "Maximum error of Fine Dust Concentration >2.5µ" {channel="airq:airq:1:pm2_5_maxerr"}
Number:Dimensionless  airQ_pm10_maxerr         "Maximum error of Fine Dust Concentration >10µ"  {channel="airq:airq:1:pm10_maxerr"}
Number:Dimensionless  airQ_pressure_maxerr     "Maximum error of Pressure"                      {channel="airq:airq:1:pressure_maxerr"}
Number:Dimensionless  airQ_so2_maxerr          "Maximum error of SO2 concentration"             {channel="airq:airq:1:so2_maxerr"}
Number:Dimensionless  airQ_sound_maxerr        "Maximum error of Noise"                         {channel="airq:airq:1:sound_maxerr"}
Number:Dimensionless  airQ_temperature_maxerr  "Maximum error of Temperature"                   {channel="airq:airq:1:temperature_maxerr"}
Number:Dimensionless  airQ_voc_maxerr          "Maximum error of VOC concentration"             {channel="airq:airq:1:voc_maxerr"}

Switch airQ_wifi                    "WLAN on or off"                        {channel="airq:airq:1:wifi"}
String airQ_SSID                    "WLAN SSID"                             {channel="airq:airq:1:SSID"}
String airQ_password                "Device Password"                       {channel="airq:airq:1:password"}
Switch airQ_wifiInfo                "Show WLAN status with LED"             {channel="airq:airq:1:wifiInfo"}
String airQ_timeServer              "Name of Timeserver address"            {channel="airq:airq:1:timeServer"}
Location airQ_location              "Location of air-Q device"              {channel="airq:airq:1:location"}
String airQ_nightMode_startDay      "Time to start day operation"           {channel="airq:airq:1:nightMode_startDay"}
String airQ_nightMode_startNight    "End of day operation"                  {channel="airq:airq:1:nightMode_startNight"}
Number:Dimensionless airQ_nightMode_brightnessDay "Brightness of LED during the day"      {channel="airq:airq:1:nightMode_brightnessDay"}
Number:Dimensionless airQ_nightMode_brightnessNight   "Brightness of LED at night"        {channel="airq:airq:1:nightMode_brightnessNight"}
Switch airQ_nightMode_fanNightOff   "Switch off fan at night"               {channel="airq:airq:1:nightMode_fanNightOff"}
Switch airQ_nightMode_wifiNightOff  "Switch off WLAN at night"              {channel="airq:airq:1:nightMode_wifiNightOff"}
String airQ_devicename              "Device Name"                           {channel="airq:airq:1:devicename"}
String airQ_roomType                "Type of room"                          {channel="airq:airq:1:roomType"}
String airQ_logLevel                "Logging level"                         {channel="airq:airq:1:logLevel"}
String airQ_deleteKey               "Settings to be deleted"                {channel="airq:airq:1:deleteKey"}
Switch airQ_fireAlarm               "Send Fire Alarm if certain levels are met" {channel="airq:airq:1:fireAlarm"}
String airQ_WLAN_config_gateway     "Network Gateway"                       {channel="airq:airq:1:WLAN_config_gateway"}
String airQ_WLAN_config_MAC         "MAC Address"                           {channel="airq:airq:1:WLAN_config_MAC"}
String airQ_WLAN_config_SSID        "WLAN SSID"                             {channel="airq:airq:1:WLAN_config_SSID"}
String airQ_WLAN_config_IPAddress   "Assigned IP address"                   {channel="airq:airq:1:WLAN_config_IPAddress"}
String airQ_WLAN_config_netMask     "Network mask"                          {channel="airq:airq:1:WLAN_config_netMask"}
String airQ_WLAN_config_BSSID       "Network BSSID"                         {channel="airq:airq:1:WLAN_config_BSSID"}
Switch airQ_cloudUpload             "Upload to air-Q cloud"                 {channel="airq:airq:1:cloudUpload"}
Number airQ_averagingRhythm         "Rhythm of measurement for historic average"    {channel="airq:airq:1:averagingRhythm"}
String airQ_powerFreqSuppression    "Power Frequency"                       {channel="airq:airq:1:powerFreqSuppression"}
Switch airQ_autoDriftCompensation   "Compensate automatic drift"            {channel="airq:airq:1:autoDriftCompensation"}
Switch airQ_autoUpdate              "Install Firmware updates automatically"    {channel="airq:airq:1:autoUpdate"}
Switch airQ_advancedDataProcessing  "Use advanced algorithms eg. for open window or presence of a person"   {channel="airq:airq:1:advancedDataProcessing"}
Switch airQ_ppm_and_ppb             "Output CO as ppm and NO2, O3 and SO2 as ppb value instead of mg/m3"    {channel="airq:airq:1:ppm_and_ppb"}
Switch airQ_gasAlarm                "Send Gas Alarm if certain levels are met"  {channel="airq:airq:1:gasAlarm"}
Switch airQ_soundPressure           "Sound Pressure Level"                  {channel="airq:airq:1:soundPressure"}
Switch airQ_alarmForwarding         "Forward gas or fire alarm to other air-Q devices in the household"     {channel="airq:airq:1:alarmForwarding"}
String airQ_userCalib               "Last sensor calibration"               {channel="airq:airq:1:userCalib"}
Switch airQ_initialCalFinished      "Initial calibration has finished"      {channel="airq:airq:1:initialCalFinished"}
Switch airQ_averaging               "Do an average"                         {channel="airq:airq:1:averaging"}
Switch airQ_errorBars               "Calculate Maximum Errors"              {channel="airq:airq:1:errorBars"}
```
