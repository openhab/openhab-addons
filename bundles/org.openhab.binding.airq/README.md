# airq Binding

The airq Binding integrates the air analyzer <a href="http://www.air-q.com">air-Q</a> device into the openHAB system.

With the binding, it is possible to subscribe to all data delivered by the air-Q device.

<img src="https://uploads-ssl.webflow.com/5bd9feee2fb42232fe1d0196/5e4a8dc0e322ca33891b51e4_air-Q%20frontal-p-800.png" alt="air-Q image" width="400px" height="324px" />

## Supported Things

One only Thing is supported: the air-Q device.
This Binding was tested with an air-Q Pro device with 14 sensors. It should also work with an air-Q device with 11 sensors, but it was not tested yet.

## Discovery

Auto-discovery is not possible in this version. Since the binding has to be configured at least with the password of the device, auto-discovery would be of limited value anyway.

## Binding Configuration

The binding does not need to be configured.

## Thing Configuration

The air-Q thing must be configured with (both mandatory):
| Parameter | Description                        |
|-----------|------------------------------------|
| ipAddress | Network address, e.g. 192.168.0.68 |
| password  | Password of the air-Q device       |

The Thing provides the following properties:
| Parameter              | Description                   |
|------------------------|-------------------------------|
| id                     | Device ID                     |
| air-Q-Hardware-Version | Hardware version              |
| air-Q-Software-Version | Firmware version              |
| sensors                | Available sensors             |
| SensorInfo             | Information about the sensors |
| Industry               | Industry version              |

## Channels

The air-Q Thing offers access to all sensor data of the air-Q, according to its version. This includes also the Maximum Error per sensor value.

| channel                   | type     | description                                                         |
|---------------------------|----------|---------------------------------------------------------------------|
| DeviceID                  | String   | Individual ID of the device                                         |
| Status                    | String   | Status of the sensors                                               |
| TypPS                     | Number   | Average size of Fine Dust [experimental]                            |
| bat                       | Number   | Battery State                                                       |
| cnt0_3                    | Number   | Fine Dust >0,3 &mu;m                                                |
| cnt0_5                    | Number   | Fine Dust >0,5 &mu;m                                                |
| cnt1                      | Number   | Fine Dust >1 &mu;m                                                  |
| cnt2_5                    | Number   | Fine Dust >2,5 &mu;m                                                |
| cnt5                      | Number   | Fine Dust >5 &mu;m                                                  |
| cnt10                     | Number   | Fine Dust >10 &mu;m                                                 |
| co2                       | Number   | CO<sub>2</sub> concentration                                        |
| dCO2dt                    | Number   | Change of CO<sub>2</sub> concentration                              |
| dHdt                      | Number   | Change of Humidity                                                  |
| dewpt                     | Number   | Dew Point                                                           |
| door_event                | Switch   | Door Event (experimental)                                           |
| health                    | Number   | Health Index                                                        |
| humidity                  | Number   | Humidity in percent                                                 |
| humidity_abs              | Number   | Absolute Humidity                                                   |
| measuretime               | Number   | Milliseconds needed for measurement                                 |
| no2                       | Number   | NO<sub>2</sub> concentration                                        |
| o3                        | Number   | O<sub>3</sub> concentration                                         |
| oxygen                    | Number   | Oxygen concentration                                                |
| performance               | Number   | Performance index                                                   |
| pm1                       | Number   | Fine Dust concentration >1 &mu;m                                    |
| pm2_5                     | Number   | Fine Dust concentration >2.5 &mu;m                                  |
| pm10                      | Number   | Fine Dust concentration >10 &mu;m                                   |
| pressure                  | Number   | Pressure                                                            |
| so2                       | Number   | SO<sub>2</sub> concentration                                        |
| sound                     | Number   | Noise                                                               |
| temperature               | Number   | Temperature                                                         |
| timestamp                 | Time     | Timestamp of measurement                                            |
| tvoc                      | Number   | VOC concentration                                                   |
| uptime                    | Number   | uptime in seconds                                                   |
| Wifi                      | Switch   | WLAN on or off                                                      |
| WLANssid                  | String   | WLAN SSID                                                           |
| pass                      | String   | Device Password                                                     |
| WifiInfo                  | Switch   | Show WLAN status with LED                                           |
| TimeServer                | String   | Name of Timeserver address                                          |
| geopos                    | Location | Location of air-Q device                                            |
| nightmode_StartDay        | String   | Time to start day operation                                         |
| nightmode_StartNight      | String   | End of day operation                                                |
| nightmode_BrightnessDay   | Number   | Brightness of LED during the day                                    |
| nightmode_BrightnessNight | Number   | Brightness of LED at night                                          |
| nightmode_FanNightOff     | Switch   | Switch off fan at night                                             |
| nightmode_WifiNightOff    | Switch   | Switch off WLAN at night                                            |
| devicename                | String   | Device Name                                                         |
| RoomType                  | String   | Type of room                                                        |
| Logging                   | String   | Logging level                                                       |
| DeleteKey                 | String   | Settings to be deleted                                              |
| FireAlarm                 | Switch   | Send Fire Alarm if certain levels are met                           |
| WLAN_config_Gateway       | String   | Network Gateway                                                     |
| WLAN_config_MAC           | String   | MAC Address                                                         |
| WLAN_config_SSID          | String   | WLAN SSID                                                           |
| WLAN_config_IPAddress     | String   | Assigned IP address                                                 |
| WLAN_config_NetMask       | String   | Network mask                                                        |
| WLAN_config_BSSID         | String   | Network BSSID                                                       |
| cloudUpload               | Switch   | Upload to air-q cloud                                               |
| SecondsMeasurementDelay   | Number   | Rhythm of measurement for historic average                          |
| Rejection                 | String   | Power Frequency                                                     |
| AutoDriftCompensation     | Switch   | Compensate automatic drift                                          |
| AutoUpdate                | Switch   | Install Firmware updates automatically                              |
| AdvancedDataProcessing    | Switch   | Use advanced algorithms eg. for open window or presence of a person |
| ppm_and_ppb               | Switch   | Output CO as ppm and NO2, O3 and SO2 as ppb value instead of mg/m3  |
| GasAlarm                  | Switch   | Send Gas Alarm if certain levels are met                            |
| id                        | String   | Device ID, retrieved from configuration                             |
| SoundInfo                 | Switch   | Sound Info                                                          |
| AlarmForwarding           | Switch   | Forward gas or fire alarm to other air-Q devices in the household   |
| usercalib                 | String   | Last sensor calibration                                             |
| InitialCalFinished        | Switch   | Initial calibration has finished                                    |
| Averaging                 | Switch   | Do an average                                                       |
| ErrorBars                 | Switch   | Calculate Maximum Errors                                            |

## Example

### airq.things

```
Thing airq:airq:1 "air-Q" [ ipAddress="192.168.0.68", password="myAirQPassword" ]
```

### airq.items

```
String  airQ_DeviceID               "Device ID, retrieved from Data"        {channel="airq:airq:1:DeviceID"}
String  airQ_Status                 "Status of Sensors"                     {channel="airq:airq:1:Status"}
Number  airQ_TypPS                  "Average"                               {channel="airq:airq:1:TypPS"}
Number  airQ_bat                    "Battery State"                         {channel="airq:airq:1:bat"}
Number  airQ_cnt03                  "Fine Dust >0,3 µm"                     {channel="airq:airq:1:cnt0_3"}
Number  airQ_cnt05                  "Fine Dust >0,5 µm"                     {channel="airq:airq:1:cnt0_5"}
Number  airQ_cnt1                   "Fine Dust >1,0 µm"                     {channel="airq:airq:1:cnt1"}
Number  airQ_cnt25                  "Fine Dust >2,5 µm"                     {channel="airq:airq:1:cnt2_5"}
Number  airQ_cnt5                   "Fine Dust >5 µm"                       {channel="airq:airq:1:cnt5"}
Number  airQ_cnt10                  "Fine Dust >10 µm"                      {channel="airq:airq:1:cnt10"}
Number  airQ_co2                    "CO2 Concentration"                     {channel="airq:airq:1:co2"}
Number  airQ_dCO2dt                 "Change of CO2 Concentration"           {channel="airq:airq:1:dCO2dt"}
Number  airQ_dHdt                   "Change of Humidity"                    {channel="airq:airq:1:dHdt"}
Number:Temperature  airQ_dewpt      "Dew Point"                             {channel="airq:airq:1:dewpt"}
Switch  airQ_door_event             "Door Event (exp.)"                     {channel="airq:airq:1:door_event"}
Number  airQ_health                 "Health Index"                          {channel="airq:airq:1:health"}
Number  airQ_humidity               "Humidity"                              {channel="airq:airq:1:humidity"}
Number  airQ_humidity_abs           "Absolute Humidity"                     {channel="airq:airq:1:humidity_abs"}
Number  airQ_measuretime            "Time needed for measurement"           {channel="airq:airq:1:measuretime"}
Number  airQ_no2                    "NO2 concentration"                     {channel="airq:airq:1:no2"}
Number  airQ_o3                     "O3 concentration"                      {channel="airq:airq:1:o3"}
Number:Dimensionless                airQ_oxygen     "Oxygen concentration"  {channel="airq:airq:1:oxygen"}
Number  airQ_performance            "Performance Index"                     {channel="airq:airq:1:performance"}
Number  airQ_pm1                    "Fine Dust Concentration >1µ"           {channel="airq:airq:1:pm1"}
Number  airQ_pm2_5                  "Fine Dust Concentration >2.5µ"         {channel="airq:airq:1:pm2_5"}
Number  airQ_pm10                   "Fine Dust Concentration >10µ"          {channel="airq:airq:1:pm10"}
Number  airQ_pressure               "Pressure"                              {channel="airq:airq:1:pressure"}
Number  airQ_so2                    "SO2 concentration"                     {channel="airq:airq:1:so2"}
Number  airQ_sound                  "Noise"                                 {channel="airq:airq:1:sound"}
Number:Temperature  airQ_temperature    "Temperature"                       {channel="airq:airq:1:temperature"}
DateTime    airQ_timestamp          "Time stamp"                            {channel="airq:airq:1:timestamp"}
Number  airQ_tvoc                   "VOC concentration"                     {channel="airq:airq:1:tvoc"}
Number  airQ_uptime                 "Uptime"                                {channel="airq:airq:1:uptime"}

Number  airQ_bat_maxerr             "'Maximum error' of Battery State, second value"    {channel="airq:airq:1:bat_maxerr"}
Number  airQ_cnt03_maxerr           "Maximum error of Fine Dust >0,3 µm"    {channel="airq:airq:1:cnt0_3_maxerr"}
Number  airQ_cnt05_maxerr           "Maximum error of Fine Dust >0,5 µm"    {channel="airq:airq:1:cnt0_5_maxerr"}
Number  airQ_cnt1_maxerr            "Maximum error of Fine Dust >1,0 µm"    {channel="airq:airq:1:cnt1_maxerr"}
Number  airQ_cnt25_maxerr           "Maximum error of Fine Dust >2,5 µm"    {channel="airq:airq:1:cnt2_5_maxerr"}
Number  airQ_cnt5_maxerr            "Maximum error of Fine Dust >5 µm"      {channel="airq:airq:1:cnt5_maxerr"}
Number  airQ_cnt10_maxerr           "Maximum error of Fine Dust >10 µm"     {channel="airq:airq:1:cnt10_maxerr"}
Number  airQ_co2_maxerr             "Maximum error of CO2 Concentration"    {channel="airq:airq:1:co2_maxerr"}
Number:Temperature  airQ_dewpt_maxerr   "Maximum error of Dew Point"        {channel="airq:airq:1:dewpt_maxerr"}
Number  airQ_humidity_maxerr        "Maximum error of Humidity"             {channel="airq:airq:1:humidity_maxerr"}
Number  airQ_humidity_abs_maxerr    "Maximum error of Absolute Humidity"    {channel="airq:airq:1:humidity_abs_maxerr"}
Number  airQ_no2_maxerr         "Maximum error of NO2 concentration"        {channel="airq:airq:1:no2_maxerr"}
Number  airQ_o3_maxerr          "Maximum error of O3 concentration"         {channel="airq:airq:1:o3_maxerr"}
Number:Dimensionless    airQ_oxygen_maxerr  "Maximum error of Oxygen concentration"     {channel="airq:airq:1:oxygen_maxerr"}
Number  airQ_pm1_maxerr         "Maximum error of Fine Dust Concentration >1µ"  {channel="airq:airq:1:pm1_maxerr"}
Number  airQ_pm2_5_maxerr       "Maximum error of Fine Dust Concentration >2.5µ"    {channel="airq:airq:1:pm2_5_maxerr"}
Number  airQ_pm10_maxerr        "Maximum error of Fine Dust Concentration >10µ" {channel="airq:airq:1:pm10_maxerr"}
Number  airQ_pressure_maxerr    "Maximum error of Pressure"                 {channel="airq:airq:1:pressure_maxerr"}
Number  airQ_so2_maxerr         "Maximum error of SO2 concentration"        {channel="airq:airq:1:so2_maxerr"}
Number  airQ_sound_maxerr       "Maximum error of Noise"                    {channel="airq:airq:1:sound_maxerr"}
Number:Temperature  airQ_temperature_maxerr "Maximum error of Temperature"  {channel="airq:airq:1:temperature_maxerr"}
Number  airQ_tvoc_maxerr        "Maximum error of VOC concentration"        {channel="airq:airq:1:tvoc_maxerr"}

Switch airQ_Wifi                    "WLAN on or off"                        {channel="airq:airq:1:Wifi"}
String airQ_WLANssid                "WLAN SSID"                             {channel="airq:airq:1:WLANssid"}
String airQ_pass                    "Device Password"                       {channel="airq:airq:1:pass"}
Switch airQ_WifiInfo                "Show WLAN status with LED"             {channel="airq:airq:1:WifiInfo"}
String airQ_TimeServer              "Name of Timeserver address"            {channel="airq:airq:1:TimeServer"}
Location airQ_geopos                "Location of air-Q device"              {channel="airq:airq:1:geopos"}
String airQ_nightmode_StartDay      "Time to start day operation"           {channel="airq:airq:1:nightmode_StartDay"}
String airQ_nightmode_StartNight    "End of day operation"                  {channel="airq:airq:1:nightmode_StartNight"}
Number airQ_nightmode_BrightnessDay "Brightness of LED during the day"      {channel="airq:airq:1:nightmode_BrightnessDay"}
Number airQ_nightmode_BrightnessNight   "Brightness of LED at night"        {channel="airq:airq:1:nightmode_BrightnessNight"}
Switch airQ_nightmode_FanNightOff   "Switch off fan at night"               {channel="airq:airq:1:nightmode_FanNightOff"}
Switch airQ_nightmode_WifiNightOff  "Switch off WLAN at night"              {channel="airq:airq:1:nightmode_WifiNightOff"}
String airQ_devicename              "Device Name"                           {channel="airq:airq:1:devicename"}
String airQ_RoomType                "Type of room"                          {channel="airq:airq:1:RoomType"}
String airQ_logging                 "Logging level"                         {channel="airq:airq:1:logging"}
String airQ_DeleteKey               "Settings to be deleted"                {channel="airq:airq:1:DeleteKey"}
Switch airQ_FireAlarm               "Send Fire Alarm if certain levels are met" {channel="airq:airq:1:FireAlarm"}
String airQ_Hardware-Version        "Hardware Version"                      {channel="airq:airq:1:air-Q-Hardware-Version"}
String airQ_WLAN_config_Gateway     "Network Gateway"                       {channel="airq:airq:1:WLAN_config_Gateway"}
String airQ_WLAN_config_MAC         "MAC Address"                           {channel="airq:airq:1:WLAN_config_MAC"}
String airQ_WLAN_config_SSID        "WLAN SSID"                             {channel="airq:airq:1:WLAN_config_SSID"}
String airQ_WLAN_config_IPAddress       "Assigned IP address"               {channel="airq:airq:1:WLAN_config_IPAddress"}
String airQ_WLAN_config_NetMask     "Network mask"                          {channel="airq:airq:1:WLAN_config_NetMask"}
String airQ_WLAN_config_BSSID       "Network BSSID"                         {channel="airq:airq:1:WLAN_config_BSSID"}
Switch airQ_cloudUpload             "Upload to air-q cloud"                 {channel="airq:airq:1:cloudUpload"}
Number airQ_SecondsMeasurementDelay "Rhythm of measurement for historic average"    {channel="airq:airq:1:SecondsMeasurementDelay"}
String airQ_Rejection               "Power Frequency"                       {channel="airq:airq:1:Rejection"}
String airQ_Software-Version        "Firmware version"                      {channel="airq:airq:1:air-Q-Software-Version"}
String airQ_sensors                 "Available sensors"                     {channel="airq:airq:1:sensors"}
Switch airQ_AutoDriftCompensation   "Compensate automatic drift"            {channel="airq:airq:1:AutoDriftCompensation"}
Switch airQ_AutoUpdate              "Install Firmware updates automatically"    {channel="airq:airq:1:AutoUpdate"}
Switch airQ_AdvancedDataProcessing  "Use advanced algorithms eg. for open window or presence of a person"   {channel="airq:airq:1:AdvancedDataProcessing"}
Switch airQ_Industry                "Industry Version"                      {channel="airq:airq:1:Industry"}
Switch airQ_ppm_and_ppb             "Output CO as ppm and NO2, O3 and SO2 as ppb value instead of mg/m3"    {channel="airq:airq:1:ppm_and_ppb"}
Switch airQ_GasAlarm                "Send Gas Alarm if certain levels are met"  {channel="airq:airq:1:GasAlarm"}
String airQ_id                      "Device ID, retrieved from configuration"   {channel="airq:airq:1:id"}
Switch airQ_SoundInfo               "Sound Info"                            {channel="airq:airq:1:SoundInfo"}
Switch airQ_AlarmForwarding         "Forward gas or fire alarm to other air-Q devices in the household"     {channel="airq:airq:1:AlarmForwarding"}
String airQ_usercalib               "Last sensor calibration"               {channel="airq:airq:1:usercalib"}
Switch airQ_InitialCalFinished      "Initial calibration has finished"      {channel="airq:airq:1:InitialCalFinished"}
Switch airQ_Averaging               "Do an average"                         {channel="airq:airq:1:Averaging"}
String airQ_SensorInfo              "Information about the sensors"         {channel="airq:airq:1:SensorInfo"}
Switch airQ_ErrorBars               "Calculate Maximum Errors"              {channel="airq:airq:1:ErrorBars"}
```
