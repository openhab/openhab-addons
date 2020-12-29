# WlanThermo Binding

This binding adds support for the WlanThermo BBQ Thermometer, ref. https://wlanthermo.de/


## Supported Things

The following hardware versions of WlanThermo are supported:

 - WlanThermo Nano
   - V1 (untested, but should be working)
   - V1+
   - V3
 - WlanThermo Mini
   - V1 (untested, but should be working)
   - V2
   - V2 (with ESP32-Upgrade)
 - WlanThermo Link
   - V1

## Discovery

There is no auto-discovery for WlanThermo Things. 
Things must be created manually via Webinterface or .things-file.

## Thing Configuration

WlanThermo things require you to specify the IP-address of your WlanThermo device (the one you enter into your browser to access the WebUI)
The configuration of username/password is optional. 
If ommitted, the binding data will be read-only for all channels. 

## Channels

Depending on the WlanThermo you're using, the following channels are available.

### WlanThermo Nano V1/V1+/V3, Mini V2 (with ESP32-Upgrade), Link V1

If username/password is given in the thing, most channels are writeable. 

#### The device itself provides the following channels:  

| channel             | type                 | description                              |
|---------------------|----------------------|------------------------------------------|
| soc                 | Number:Dimensionless | Battery Level in %                       |
| charging            | Switch               | On, if device is charging, off otherwise |
| rssi_signalstrength | Number               | Signal Strength in range [0 ... 4]       |
| rssi                | Number               | Signal Strength in dBm                   |


#### The following channels apply for all 8 probes of the WlanThermo Nano V1/V1+ and for all up to 24 probes of Nano V3, Mini V2 (with ESP32-Upgrade), Link V1:

| channel            | type               | description                                                          |
|--------------------|--------------------|----------------------------------------------------------------------|
| name               | String             | The name of this probe                                               |
| type               | String             | The type of this probe                                               |
| temp               | Number:Temperature | The current temperature                                              |
| min                | Number:Temperature | The minimum temperature threshold for this probe to trigger an alarm |
| max                | Number:Temperature | The maximum temperature threshold for this probe to trigger an alarm |
| alarm_device       | Switch             | Turn on/off the buzzer alarm on the device for this probe            |
| alarm_push         | Switch             | Turn on/off the push alarm for this probe                            |
| alarm_openhab_low  | Switch             | Will turn on if current temp is below minimum temperature threshold  |
| alarm_openhab_high | Switch             | Will turn on if current temp is above maximum temperature threshold  |
| color              | Color              | The color of this probe. Read only.                                  |
| color_name         | String             | The color name of the probe.                                         |


#### The following channels are available for the Pitmaster:

| channel    | type                 | description                                                                                 |
|------------|----------------------|---------------------------------------------------------------------------------------------|
| state      | String               | Indicates type of the Pitmaster channel. Value can be "off", "manual", "auto" or "autotune" |
| setpoint   | Number:Temperature   | the target temperature of the probe assigned to the pitmaster channel                       |
| duty_cycle | Number:Dimensionless | The current duty cycle of the pitmaster channel                                             |
| channel_id | Number               | The channel id of the probe assigned to the pitmaster channel                               |
| pid_id     | Number               | The number of the PID profile to be used. Check the WlanThermo WebUI for available IDs!     |


### WlanThermo Mini V1/V2

All channels are read only!
#### The device itself provides the following channels:

| channel  | type                 | description                   |
|----------|----------------------|-------------------------------|
| cpu_load | Number:Dimensionless | CPU Load in %                 |
| cpu_temp | Number:Temperature   | CPU Temperature               |


#### The following channels apply for all 10 probes of the WlanThermo Mini V1/V2:

| channel            | type               | description                                                          |
|--------------------|--------------------|----------------------------------------------------------------------|
| name               | String             | The name of this probe                                               |
| temp               | Number:Temperature | The current temperature                                              |
| min                | Number:Temperature | The minimum temperature threshold for this probe to trigger an alarm |
| max                | Number:Temperature | The maximum temperature threshold for this probe to trigger an alarm |
| alarm_device       | Switch             | Turn on/off the buzzer alarm on the device for this probe            |
| alarm_openhab_low  | Switch             | Will turn on if current temp is below minimum temperature threshold  |
| alarm_openhab_high | Switch             | Will turn on if current temp is above maximum temperature threshold  |
| color              | Color              | The color of this probe                                              |
| color_name         | String             | The color name of this probe                                         |


#### The following channels apply for both Pitmaster channels of the WlanThermo Mini V1/V2:

| channel    | type                 | description                                                             |
|------------|----------------------|-------------------------------------------------------------------------|
| enabled    | Switch               | Indicates if this pitmaster channel is active                           |
| current    | Number:Temperature   | The current temperature of the probe assigned to this pitmaster channel |
| setpoint   | Number:Temperature   | the target temperature of the probe assigned to this pitmaster channel  |
| duty_cycle | Number:Dimensionless | The current duty cycle of this pitmaster channel                        |
| lid_open   | Switch               | Indicates if Lid-open detection is active                               |
| channel_id | Number               | The channel id of the probe assigned to this pitmaster channel          |


## Triggers

The following trigger apply for all channels of Nano and Mini:

| trigger       | values | description                                                                |
|---------------|--------|----------------------------------------------------------------------------|
| alarm_openhab | MIN    | Triggers repeatedly if current temp is below minimum temperature threshold |
| alarm_openhab | MAX    | Triggers repeatedly if current temp is above maximum temperature threshold |



## Full Example

### Example .things file

```
//WlanThermo Nano V1/V1+
wlanthermo:nano:<nano_thing_id>      "Nano V1"       [ ipAddress="<ip-address>", username="<username>", password="<password>", pollingInterval=10 ]

//WlanThermo Nano V3, Mini V2 (with ESP32-Upgrade), Link V1  
wlanthermo:esp32:<esp32_thing_id>    "Nano V3"       [ ipAddress="<ip-address>", username="<username>", password="<password>", pollingInterval=10 ]

//WlanThermo Mini V1/V2
wlanthermo:mini:<nano_thing_id>      "Mini V1"       [ ipAddress="<ip-address>", pollingInterval=10 ]
```

### Example .items file
Make sure to replace <nano_thing_id>, <esp32_thing_id> or <mini_thing_id> with your individual thing ID!

```
//############################
//#  WlanThermo Nano V1/V1+  #
//############################
Group                   gWlanThermoNano         "WlanThermo Nano"
Number:Dimensionless    nano_soc                "State of Charge"       (gWlanThermoNano)   {channel="wlanthermo:nano:<nano_thing_id>:system#soc"}
Number                  nano_rssi               "Signal Strength"       (gWlanThermoNano)   {channel="wlanthermo:nano:<nano_thing_id>:system#rssi"}
Switch                  nano_charging           "Charging"              (gWlanThermoNano)   {channel="wlanthermo:nano:<nano_thing_id>:system#charge"}

Group                   gProbeNano1             "Probe 1"               (gWlanThermoNano)
String                  nano_name_1             "Name"                  (gProbeNano1)       {channel="wlanthermo:nano:<nano_thing_id>:channel1#name"}
String                  nano_typ_1              "Type"                  (gProbeNano1)       {channel="wlanthermo:nano:<nano_thing_id>:channel1#typ"}
Number:Temperature      nano_temp_1             "Temperature"           (gProbeNano1)       {channel="wlanthermo:nano:<nano_thing_id>:channel1#temp"}
Number:Temperature      nano_min_1              "Min Temperature"       (gProbeNano1)       {channel="wlanthermo:nano:<nano_thing_id>:channel1#min"}
Number:Temperature      nano_max_1              "Max Temperature"       (gProbeNano1)       {channel="wlanthermo:nano:<nano_thing_id>:channel1#max"}
Switch                  nano_alarm_device_1     "Enable Buzzer Alarm"   (gProbeNano1)       {channel="wlanthermo:nano:<nano_thing_id>:channel1#alarm_device"}
Switch                  nano_alarm_push_1       "Enable Push Alarm"     (gProbeNano1)       {channel="wlanthermo:nano:<nano_thing_id>:channel1#alarm_push"}
Switch                  nano_alarm_low_1        "Low Temp. Alarm"       (gProbeNano1)       {channel="wlanthermo:nano:<nano_thing_id>:channel1#alarm_openhab_low"}
Switch                  nano_alarm_high_1       "High Temp. Alarm"      (gProbeNano1)       {channel="wlanthermo:nano:<nano_thing_id>:channel1#alarm_openhab_high"}
Color                   nano_color_1            "Color"                 (gProbeNano1)       {channel="wlanthermo:nano:<nano_thing_id>:channel1#color"}
String                  nano_color_name_1       "Color Name"            (gProbeNano1)       {channel="wlanthermo:nano:<nano_thing_id>:channel1#color_name"}

Group                   gProbeNano2             "Probe 2"               (gWlanThermoNano)
String                  nano_name_2             "Name"                  (gProbeNano2)       {channel="wlanthermo:nano:<nano_thing_id>:channel2#name"}
String                  nano_typ_2              "Type"                  (gProbeNano2)       {channel="wlanthermo:nano:<nano_thing_id>:channel2#typ"}
Number:Temperature      nano_temp_2             "Temperature"           (gProbeNano2)       {channel="wlanthermo:nano:<nano_thing_id>:channel2#temp"}
Number:Temperature      nano_min_2              "Min Temperature"       (gProbeNano2)       {channel="wlanthermo:nano:<nano_thing_id>:channel2#min"}
Number:Temperature      nano_max_2              "Max Temperature"       (gProbeNano2)       {channel="wlanthermo:nano:<nano_thing_id>:channel2#max"}
Switch                  nano_alarm_device_2     "Enable Buzzer Alarm"   (gProbeNano2)       {channel="wlanthermo:nano:<nano_thing_id>:channel2#alarm_device"}
Switch                  nano_alarm_push_2       "Enable Push Alarm"     (gProbeNano2)       {channel="wlanthermo:nano:<nano_thing_id>:channel2#alarm_push"}
Switch                  nano_alarm_low_2        "Low Temp. Alarm"       (gProbeNano2)       {channel="wlanthermo:nano:<nano_thing_id>:channel2#alarm_openhab_low"}
Switch                  nano_alarm_high_2       "High Temp. Alarm"      (gProbeNano2)       {channel="wlanthermo:nano:<nano_thing_id>:channel2#alarm_openhab_high"}
Color                   nano_color_2            "Color"                 (gProbeNano2)       {channel="wlanthermo:nano:<nano_thing_id>:channel2#color"}
String                  nano_color_name_2       "Color Name"            (gProbeNano2)       {channel="wlanthermo:nano:<nano_thing_id>:channel2#color_name"}
//(...)
//other temperature channels accordingly 

Group                   gPitmasterNano1         "Pitmaster Nano"        (gWlanThermoNano)
String                  nano_pit_state          "State"                 (gPitmasterNano1)   {channel="wlanthermo:nano:5af97cb9:pit1#state"}
Number:Temperature      nano_pit_setpoint       "Setpoint"              (gPitmasterNano1)   {channel="wlanthermo:nano:5af97cb9:pit1#setpoint"}
Number                  nano_pit_cycle          "Duty Cycle"            (gPitmasterNano1)   {channel="wlanthermo:nano:5af97cb9:pit1#duty_cycle"}
Number                  nano_pit_pidprofile     "PID Profile"           (gPitmasterNano1)   {channel="wlanthermo:nano:5af97cb9:pit1#pid_id"}
Number                  nano_pit_channel        "Input Channel ID"      (gPitmasterNano1)   {channel="wlanthermo:nano:5af97cb9:pit1#channel_id"}
//(...)
//other Pitmaster channels accordingly

//###############################################################
//#  WlanThermo Nano V3, Mini V2 (with ESP32-Upgrade), Link V1  #
//###############################################################
Group                   gWlanThermoEsp32        "WlanThermo Esp32"
Number:Dimensionless    esp32_soc               "State of Charge"       (gWlanThermoEsp32)   {channel="wlanthermo:esp32:<esp32_thing_id>:system#soc"}
Number                  esp32_rssi              "Signal Strength"       (gWlanThermoEsp32)   {channel="wlanthermo:esp32:<esp32_thing_id>:system#rssi"}
Switch                  esp32_charging          "Charging"              (gWlanThermoEsp32)   {channel="wlanthermo:esp32:<esp32_thing_id>:system#charge"}

Group                   gProbeEsp321            "Probe 1"               (gWlanThermoEsp32)
String                  esp32_name_1            "Name"                  (gProbeEsp321)       {channel="wlanthermo:esp32:<esp32_thing_id>:channel1#name"}
String                  esp32_typ_1             "Type"                  (gProbeEsp321)       {channel="wlanthermo:esp32:<esp32_thing_id>:channel1#typ"}
Number:Temperature      esp32_temp_1            "Temperature"           (gProbeEsp321)       {channel="wlanthermo:esp32:<esp32_thing_id>:channel1#temp"}
Number:Temperature      esp32_min_1             "Min Temperature"       (gProbeEsp321)       {channel="wlanthermo:esp32:<esp32_thing_id>:channel1#min"}
Number:Temperature      esp32_max_1             "Max Temperature"       (gProbeEsp321)       {channel="wlanthermo:esp32:<esp32_thing_id>:channel1#max"}
Switch                  esp32_alarm_device_1    "Enable Buzzer Alarm"   (gProbeEsp321)       {channel="wlanthermo:esp32:<esp32_thing_id>:channel1#alarm_device"}
Switch                  esp32_alarm_push_1      "Enable Push Alarm"     (gProbeEsp321)       {channel="wlanthermo:esp32:<esp32_thing_id>:channel1#alarm_push"}
Switch                  esp32_alarm_low_1       "Low Temp. Alarm"       (gProbeEsp321)       {channel="wlanthermo:esp32:<esp32_thing_id>:channel1#alarm_openhab_low"}
Switch                  esp32_alarm_high_1      "High Temp. Alarm"      (gProbeEsp321)       {channel="wlanthermo:esp32:<esp32_thing_id>:channel1#alarm_openhab_high"}
Color                   esp32_color_1           "Color"                 (gProbeEsp321)       {channel="wlanthermo:esp32:<esp32_thing_id>:channel1#color"}
String                  esp32_color_name_1      "Color Name"            (gProbeEsp321)       {channel="wlanthermo:esp32:<esp32_thing_id>:channel1#color_name"}

Group                   gProbeEsp322            "Probe 2"               (gWlanThermoEsp32)
String                  esp32_name_2            "Name"                  (gProbeEsp322)       {channel="wlanthermo:esp32:<esp32_thing_id>:channel2#name"}
String                  esp32_typ_2             "Type"                  (gProbeEsp322)       {channel="wlanthermo:esp32:<esp32_thing_id>:channel2#typ"}
Number:Temperature      esp32_temp_2            "Temperature"           (gProbeEsp322)       {channel="wlanthermo:esp32:<esp32_thing_id>:channel2#temp"}
Number:Temperature      esp32_min_2             "Min Temperature"       (gProbeEsp322)       {channel="wlanthermo:esp32:<esp32_thing_id>:channel2#min"}
Number:Temperature      esp32_max_2             "Max Temperature"       (gProbeEsp322)       {channel="wlanthermo:esp32:<esp32_thing_id>:channel2#max"}
Switch                  esp32_alarm_device_2    "Enable Buzzer Alarm"   (gProbeEsp322)       {channel="wlanthermo:esp32:<esp32_thing_id>:channel2#alarm_device"}
Switch                  esp32_alarm_push_2      "Enable Push Alarm"     (gProbeEsp322)       {channel="wlanthermo:esp32:<esp32_thing_id>:channel2#alarm_push"}
Switch                  esp32_alarm_low_2       "Low Temp. Alarm"       (gProbeEsp322)       {channel="wlanthermo:esp32:<esp32_thing_id>:channel2#alarm_openhab_low"}
Switch                  esp32_alarm_high_2      "High Temp. Alarm"      (gProbeEsp322)       {channel="wlanthermo:esp32:<esp32_thing_id>:channel2#alarm_openhab_high"}
Color                   esp32_color_2           "Color"                 (gProbeEsp322)       {channel="wlanthermo:esp32:<esp32_thing_id>:channel2#color"}
String                  esp32_color_name_2      "Color Name"            (gProbeEsp322)       {channel="wlanthermo:esp32:<esp32_thing_id>:channel2#color_name"}
//(...)
//other temperature channels accordingly 

Group                   gPitmasterEsp321        "Pitmaster Esp32"       (gWlanThermoEsp32)
String                  esp32_pit_state         "State"                 (gPitmasterEsp321)   {channel="wlanthermo:esp32:5af97cb9:pit1#state"}
Number:Temperature      esp32_pit_setpoint      "Setpoint"              (gPitmasterEsp321)   {channel="wlanthermo:esp32:5af97cb9:pit1#setpoint"}
Number                  esp32_pit_cycle         "Duty Cycle"            (gPitmasterEsp321)   {channel="wlanthermo:esp32:5af97cb9:pit1#duty_cycle"}
Number                  esp32_pit_pidprofile    "PID Profile"           (gPitmasterEsp321)   {channel="wlanthermo:esp32:5af97cb9:pit1#pid_id"}
Number                  esp32_pit_channel       "Input Channel ID"      (gPitmasterEsp321)   {channel="wlanthermo:esp32:5af97cb9:pit1#channel_id"}
//(...)
//other Pitmaster channels accordingly

//###########################
//#  WlanThermo Mini V1/V2  #
//###########################
Group                   gWlanThermoMini         "WlanThermo Mini"
Number                  mini_cpuload            "CPU Load"              (gWlanThermoMini)   {channel="wlanthermo:mini:<mini_thing_id>:system#cpu_load"}
Number:Temperature      mini_cputemp            "CPU Temp"              (gWlanThermoMini)   {channel="wlanthermo:mini:<mini_thing_id>:system#cpu_temp"}

Group                   gProbeMini0             "Probe 0"               (gWlanThermoMini)
String                  mini_name_0             "Name"                  (gProbeMini0)       {channel="wlanthermo:mini:<mini_thing_id>:channel0#name"}
Number:Temperature      mini_temp_0             "Temperature"           (gProbeMini0)       {channel="wlanthermo:mini:<mini_thing_id>:channel0#temp"}
Number:Temperature      mini_min_0              "Min Temperature"       (gProbeMini0)       {channel="wlanthermo:mini:<mini_thing_id>:channel0#min"}
Number:Temperature      mini_max_0              "Max Temperature"       (gProbeMini0)       {channel="wlanthermo:mini:<mini_thing_id>:channel0#max"}
Switch                  mini_alarm_device_0     "Enable Buzzer Alarm"   (gProbeMini0)       {channel="wlanthermo:mini:<mini_thing_id>:channel0#alarm_device"}
Switch                  mini_alarm_low_0        "Low Temp. Alarm"       (gProbeMini0)       {channel="wlanthermo:mini:<mini_thing_id>:channel0#alarm_openhab_low"}
Switch                  mini_alarm_high_0       "High Temp. Alarm"      (gProbeMini0)       {channel="wlanthermo:mini:<mini_thing_id>:channel0#alarm_openhab_high"}
Color                   mini_color_0            "Color"                 (gProbeMini0)       {channel="wlanthermo:mini:<mini_thing_id>:channel0#color"}
String                  mini_color_name_0       "Color Name"            (gProbeMini0)       {channel="wlanthermo:mini:<mini_thing_id>:channel0#color_name"}

Group                   gProbeMini1             "Probe 1"               (gWlanThermoMini)
String                  mini_name_1             "Name"                  (gProbeMini1)       {channel="wlanthermo:mini:<mini_thing_id>:channel1#name"}
Number:Temperature      mini_temp_1             "Temperature"           (gProbeMini1)       {channel="wlanthermo:mini:<mini_thing_id>:channel1#temp"}
Number:Temperature      mini_min_1              "Min Temperature"       (gProbeMini1)       {channel="wlanthermo:mini:<mini_thing_id>:channel1#min"}
Number:Temperature      mini_max_1              "Max Temperature"       (gProbeMini1)       {channel="wlanthermo:mini:<mini_thing_id>:channel1#max"}
Switch                  mini_alarm_device_1     "Enable Buzzer Alarm"   (gProbeMini1)       {channel="wlanthermo:mini:<mini_thing_id>:channel1#alarm_device"}
Switch                  mini_alarm_low_1        "Low Temp. Alarm"       (gProbeMini1)       {channel="wlanthermo:mini:<mini_thing_id>:channel1#alarm_openhab_low"}
Switch                  mini_alarm_high_1       "High Temp. Alarm"      (gProbeMini1)       {channel="wlanthermo:mini:<mini_thing_id>:channel1#alarm_openhab_high"}
Color                   mini_color_1            "Color"                 (gProbeMini1)       {channel="wlanthermo:mini:<mini_thing_id>:channel1#color"}
String                  mini_color_name_1       "Color Name"            (gProbeMini1)       {channel="wlanthermo:mini:<mini_thing_id>:channel1#color_name"}
//(...)
//other temperature channels accordingly 


Group                   gPitmasterMini1         "Pitmaster 1"           (gWlanThermoMini)
Switch                  mini_pit_enabled_1      "Enabled"               (gPitmasterMini1)   {channel="wlanthermo:mini:<mini_thing_id>:pit1#enabled"}
Number:Temperature      mini_pit_current_1      "Temperature"           (gPitmasterMini1)   {channel="wlanthermo:mini:<mini_thing_id>:pit1#current"}
Number:Temperature      mini_pit_setpoint_1     "Setpoint"              (gPitmasterMini1)   {channel="wlanthermo:mini:<mini_thing_id>:pit1#setpoint"}
Number                  mini_pit_cycle_1        "Duty Cycle"            (gPitmasterMini1)   {channel="wlanthermo:mini:<mini_thing_id>:pit1#duty_cycle"}
Switch                  mini_pit_lidopen_1      "Lid Open Detection"    (gPitmasterMini1)   {channel="wlanthermo:mini:<mini_thing_id>:pit1#lid_open"}
Number                  mini_pit_channel_1      "Input Channel ID"      (gPitmasterMini1)   {channel="wlanthermo:mini:<mini_thing_id>:pit1#channel_id"}

//(...)
//other Pitmaster channels accordingly
```

### Example .sitemap file

(Example for WlanThermo Nano)

```
sitemap wlanthermo label="WlanThermo" {
    Frame label="WlanThermo" {
        Default item=nano_rssi icon="qualityofservice"
        Default item=nano_soc icon="batterylevel"
        Default item=nano_charging icon="energy"
        
        Text item=nano_name_1 icon="fire" {
            Frame {
                Default item=nano_typ_1 icon="settings"
                Default item=nano_color_1
                
                Default item=nano_temp_1 icon="temperature"
                Text icon=""
                Setpoint item=nano_min_1 icon="temperature_cold"
                Setpoint item=nano_max_1 icon="temperature_hot"
                
                Default item=nano_alarm_device_1 icon="switch"
                Default item=nano_alarm_push_1 icon="switch"
                Default item=nano_alarm_low_1 icon="siren"
                Default item=nano_alarm_high_1 icon="siren"
            }
        }
        
        Text item=nano_name_2 icon="fire" {
            Frame {
                Default item=nano_typ_2 icon="settings"
                Default item=nano_color_2
                
                Default item=nano_temp_2 icon="temperature"
                Text icon=""
                Setpoint item=nano_min_2 icon="temperature_cold"
                Setpoint item=nano_max_2 icon="temperature_hot"
                
                Default item=nano_alarm_device_2 icon="switch"
                Default item=nano_alarm_push_2 icon="switch"
                Default item=nano_alarm_low_2 icon="siren"
                Default item=nano_alarm_high_2 icon="siren"
            }
        }
    }
}

```

