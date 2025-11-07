
# WorxLandroid Binding

This is the binding for Worx Landroid robotic lawn mowers.
It connects openHAB with your WorxLandroid mower using the API and MQTT.
This binding allows you to integrate, view and control supported Worx lawn mowers with openHAB.

## Supported Things

Currently following Things are supported:

- `bridge`: **Bridge Worx Landroid API** Thing representing the handler for Worx API
- `mower`: One or many Things for supported **Landroid Mower**'s

## Discovery

A Bridge is required to connect to the Worx API.
Here you can provide your credentials for your WorxLandroid account.
Once the Bridge has been added Worx Landroid mowers will be discovered automatically.

## Things Configuration

The following options can be set for the `bridge`:

| Property | Description                              |
|----------|------------------------------------------|
| username | Username to access the WorxLandroid API. |
| password | Password to access the WorxLandroid API. |

The following options can be set for the `mower`:

| Property              | Description                                                                                  | Default | Advanced |
|-----------------------|----------------------------------------------------------------------------------------------|---------|----------|
| serialNumber          | Serial number of the mower                                                                   |      -  |   No     |
| refreshStatusInterval | Interval for refreshing mower status (ONLINE/OFFLINE) and channel 'common#online' in seconds |   3600  |  Yes     |
| pollingInterval       | Interval for polling in seconds (min="30" max="7200").                                       |   1200  |  Yes     |


Default values for `refreshStatusInterval` and `pollingInterval` are the recommended settings in order to prevent a 24h ban from Worx.
Lower polling and refresh values will likely result in a 24h ban for your account.

## Channels

Currently following **Channels** are supported on the **Landroid Mower**:

### Common

| Channel          | Type     | ChannelName             | Values            |
|------------------|----------|-------------------------|-------------------|
| status           | String   | common#status           | *1 (see below)    |
| error            | String   | common#error            | *2 (see below)    |
| online           | Switch   | common#online           |                   |
| online-timestamp | DateTime | common#online-timestamp |                   |
| action           | String   | common#action           | START, STOP, HOME |
| enable           | Switch   | common#enable           |                   |
| lock             | Switch   | common#lock             |                   |

*1: Values for **error** Channel:

UNKNOWN, NO_ERR, TRAPPED, LIFTED, WIRE_MISSING, OUTSIDE_WIRE, RAINING, CLOSE_DOOR_TO_MOW, CLOSE_DOOR_TO_GO_HOME, BLADE_MOTOR_BLOCKED, WHEEL_MOTOR_BLOCKED, TRAPPED_TIMEOUT, UPSIDE_DOWN, BATTERY_LOW, REVERSE_WIRE, CHARGE_ERROR, TIMEOUT_FINDING_HOME, MOWER_LOCKED, BATTERY_OVER_TEMPERATURE, MOWER_OUTSIDE_WIRE

*2: Values for **status** Channel:

UNKNOWN, IDLE, HOME, START_SEQUENCE, LEAVING_HOME, FOLLOW_WIRE, SEARCHING_HOME, SEARCHING_WIRE, MOWING, LIFTED, TRAPPED, BLADE_BLOCKED, DEBUG, REMOTE_CONTROL,  GOING_HOME, ZONE_TRAINING, BORDER_CUT, SEARCHING_ZONE, PAUSE, MANUAL_STOP

### Config

| Channel   | Type     | ChannelName      |
|-----------|----------|------------------|
| timestamp | DateTime | config#timestamp |
| command   | Number   | config#command   |

### Multi-Zones

If Multi Zones are supported, you are able to define 4 separate zones and split working times by 10 to those.

To ease zone configuration, you are able to set distance in meters where a specific zone starts. Bearing in mind that you roughly shall know how many meters of cable have been used (without buffer).

As second step you are able to set time in percent and split in parts of 10 between allocation zones.

| Channel      | Type          | ChannelName              |
|--------------|---------------|--------------------------|
| enable       | Switch        | multi-zones#enable       |
| last-zone    | Number        | multi-zones#last-zone    |
| zone-1       | Number:Length | multi-zones#zone-1       |
| zone-2       | Number:Length | multi-zones#zone-2       |
| zone-3       | Number:Length | multi-zones#zone-3       |
| zone-4       | Number:Length | multi-zones#zone-4       |
| allocation-0 | Number        | multi-zones#allocation-0 |
| allocation-1 | Number        | multi-zones#allocation-1 |
| allocation-2 | Number        | multi-zones#allocation-2 |
| allocation-3 | Number        | multi-zones#allocation-3 |
| allocation-4 | Number        | multi-zones#allocation-4 |
| allocation-5 | Number        | multi-zones#allocation-5 |
| allocation-6 | Number        | multi-zones#allocation-6 |
| allocation-7 | Number        | multi-zones#allocation-7 |
| allocation-8 | Number        | multi-zones#allocation-8 |
| allocation-9 | Number        | multi-zones#allocation-9 |

### Schedule

| Channel        | Type     | ChannelName             |                   |
|----------------|----------|-------------------------|-------------------|
| mode           | String   | schedule#mode           | ONLY IF SUPPORTED |
| time-extension | Number   | schedule#time-extension |                   |
| next-start     | DateTime | schedule#next-start     |                   |
| next-stop      | DateTime | schedule#next-stop      |                   |

### Aws

| Channel   | Type   | ChannelName   |
|-----------|--------|---------------|
| poll      | Switch | aws#poll      |
| connected | Switch | aws#connected |

### Sunday (Slot 1)

| Channel  | Type        | ChannelName     |
|----------|-------------|-----------------|
| enable   | Switch      | sunday#enable   |
| time     | DateTime    | sunday#time     |
| duration | Number:Time | sunday#duration |
| edgecut  | Switch      | sunday#edgecut  |

### Sunday2 (Slot 2, ONLY IF SUPPORTED)

| Channel  | Type        | ChannelName      |
|----------|-------------|------------------|
| enable   | Switch      | sunday2#enable   |
| time     | DateTime    | sunday2#time     |
| duration | Number:Time | sunday2#duration |
| edgecut  | Switch      | sunday2#edgecut  |

And so on for each day of the week along with the Slot 2 when supported.

### One-Time

| Channel  | Type   | ChannelName       |
|----------|--------|-------------------|
| edgecut  | Switch | one-time#edgecut  |
| duration | Switch | one-time#duration |

### Battery

| Channel             | Type                     | ChannelName                 |
|---------------------|--------------------------|-----------------------------|
| temperature         | Number:Temperature       | battery#temperature         |
| voltage             | Number:ElectricPotential | battery#voltage             |
| level               | Number                   | battery#level               |
| charge-cycles       | Number                   | battery#charge-cycles       |
| charge-cycles-total | Number                   | battery#charge-cycles-total |
| charging            | Switch                   | battery#charging            |

### Orientation

| Channel | Type         | ChannelName       |
|---------|--------------|-------------------|
| pitch   | Number:Angle | orientation#pitch |
| roll    | Number:Angle | orientation#roll  |
| yaw     | Number:Angle | orientation#yaw   |

### Metrics

| Channel          | Type          | ChannelName              |
|------------------|---------------|--------------------------|
| blade-time       | Number:Time   | metrics#blade-time       |
| blade-time-total | Number:Time   | metrics#blade-time-total |
| distance         | Number:Length | metrics#distance         |
| total-time       | Number:Time   | metrics#total-time       |

### Rain (if supported)

| Channel | Type        | ChannelName  |
|---------|-------------|--------------|
| state   | Switch      | rain#state   |
| counter | Number:Time | rain#counter |
| delay   | Number:Time | rain#delay   |

### Wifi

| Channel      | Type         | ChannelName       |
|--------------|--------------|-------------------|
| rssi         | Number:Power | wifi#rssi         |
| wifi-quality | Number       | wifi#wifi-quality |

## Examples

### $OPENHAB_CONF/items/landroid.things

```java
Bridge worxlandroid:bridge:api "Worx Api" [ username="xxxxYYYxxxx", password="dldkssdjldj" ] {
    Thing mower lanmower "Worx M600" [ serialNumber="sdmldksmdskmlsd" ]
}

```


### $OPENHAB_CONF/items/landroid.items

```java
String                     MyMower                                 "MyMower [%s]"
String                     LandroidMowerCommonStatus               "Status code"               {channel="worxlandroid:mower:MyWorxBridge:mymower:common#status"}
String                     LandroidMowerCommonError                "Error code"                {channel="worxlandroid:mower:MyWorxBridge:mymower:common#error"}
Switch                     LandroidMowerCommonOnline               "Online"                    {channel="worxlandroid:mower:MyWorxBridge:mymower:common#online"}
DateTime                   LandroidMowerCommonOnlineTimestamp      "Online status timestamp"   {channel="worxlandroid:mower:MyWorxBridge:mymower:common#online-timestamp"}
String                     LandroidMowerCommonAction               "Action"                    {channel="worxlandroid:mower:MyWorxBridge:mymower:common#action"}
Switch                     LandroidMowerCommonEnable               "Mowing enabled"            {channel="worxlandroid:mower:MyWorxBridge:mymower:common#enable"}
Switch                     LandroidMowerCommonLock                 "Lock mower wifi"           {channel="worxlandroid:mower:MyWorxBridge:mymower:common#lock"}
DateTime                   LandroidMowerConfigTimestamp            "Last update"               {channel="worxlandroid:mower:MyWorxBridge:mymower:config#timestamp"}
Number                     LandroidMowerConfigCommand              "Command"                   {channel="worxlandroid:mower:MyWorxBridge:mymower:config#command"}
Switch                     LandroidMowerMultiZonesEnable           "Multizone enabled"         {channel="worxlandroid:mower:MyWorxBridge:mymower:multi-zones#enable"}
Number                     LandroidMowerMultiZonesLastZone         "Last zone"                 {channel="worxlandroid:mower:MyWorxBridge:mymower:multi-zones#last-zone"}
Number:Length              LandroidMowerMultiZonesZone1            "Meters zone 1"             {channel="worxlandroid:mower:MyWorxBridge:mymower:multi-zones#zone-1"}
Number:Length              LandroidMowerMultiZonesZone2            "Meters zone 2"             {channel="worxlandroid:mower:MyWorxBridge:mymower:multi-zones#zone-2"}
Number:Length              LandroidMowerMultiZonesZone3            "Meters zone 3"             {channel="worxlandroid:mower:MyWorxBridge:mymower:multi-zones#zone-3"}
Number:Length              LandroidMowerMultiZonesZone4            "Meters zone 4"             {channel="worxlandroid:mower:MyWorxBridge:mymower:multi-zones#zone-4"}
Number                     LandroidMowerMultiZonesAllocation0      "Zone allocation 1"         {channel="worxlandroid:mower:MyWorxBridge:mymower:multi-zones#allocation-0"}
Number                     LandroidMowerMultiZonesAllocation1      "Zone allocation 2"         {channel="worxlandroid:mower:MyWorxBridge:mymower:multi-zones#allocation-1"}
Number                     LandroidMowerMultiZonesAllocation2      "Zone allocation 3"         {channel="worxlandroid:mower:MyWorxBridge:mymower:multi-zones#allocation-2"}
Number                     LandroidMowerMultiZonesAllocation3      "Zone allocation 4"         {channel="worxlandroid:mower:MyWorxBridge:mymower:multi-zones#allocation-3"}
Number                     LandroidMowerMultiZonesAllocation4      "Zone allocation 5"         {channel="worxlandroid:mower:MyWorxBridge:mymower:multi-zones#allocation-4"}
Number                     LandroidMowerMultiZonesAllocation5      "Zone allocation 6"         {channel="worxlandroid:mower:MyWorxBridge:mymower:multi-zones#allocation-5"}
Number                     LandroidMowerMultiZonesAllocation6      "Zone allocation 7"         {channel="worxlandroid:mower:MyWorxBridge:mymower:multi-zones#allocation-6"}
Number                     LandroidMowerMultiZonesAllocation7      "Zone allocation 8"         {channel="worxlandroid:mower:MyWorxBridge:mymower:multi-zones#allocation-7"}
Number                     LandroidMowerMultiZonesAllocation8      "Zone allocation 9"         {channel="worxlandroid:mower:MyWorxBridge:mymower:multi-zones#allocation-8"}
Number                     LandroidMowerMultiZonesAllocation9      "Zone allocation 10"        {channel="worxlandroid:mower:MyWorxBridge:mymower:multi-zones#allocation-9"}
String                     LandroidMowerScheduleMode               "Schedule mode"             {channel="worxlandroid:mower:MyWorxBridge:mymower:schedule#mode"}
Number:Dimensionless       LandroidMowerScheduleTimeExtension      "Schedule time extension"   {channel="worxlandroid:mower:MyWorxBridge:mymower:schedule#time-extension"}
DateTime                   LandroidMowerScheduleNextStart          "Next start"                {channel="worxlandroid:mower:MyWorxBridge:mymower:schedule#next-start"}
DateTime                   LandroidMowerScheduleNextStop           "Next stop"                 {channel="worxlandroid:mower:MyWorxBridge:mymower:schedule#next-stop"}
Switch                     LandroidMowerAwsPoll                    "Poll AWS"                  {channel="worxlandroid:mower:MyWorxBridge:mymower:aws#poll"}
Switch                     LandroidMowerAwsConnected               "AWS connected"             {channel="worxlandroid:mower:MyWorxBridge:mymower:aws#connected"}
Switch                     LandroidMowerSundayEnable               "Active"                    {channel="worxlandroid:mower:MyWorxBridge:mymower:sunday#enable"}
DateTime                   LandroidMowerSundayTime                 "Start time"                {channel="worxlandroid:mower:MyWorxBridge:mymower:sunday#time"}
Number:Time                LandroidMowerSundayDuration             "Duration"                  {channel="worxlandroid:mower:MyWorxBridge:mymower:sunday#duration", unit="min"}
Switch                     LandroidMowerSundayEdgecut              "Edgecut"                   {channel="worxlandroid:mower:MyWorxBridge:mymower:sunday#edgecut"}
Switch                     LandroidMowerSunday2Enable              "Active"                    {channel="worxlandroid:mower:MyWorxBridge:mymower:sunday2#enable"}
DateTime                   LandroidMowerSunday2Time                "Start time"                {channel="worxlandroid:mower:MyWorxBridge:mymower:sunday2#time"}
Number:Time                LandroidMowerSunday2Duration            "Duration"                  {channel="worxlandroid:mower:MyWorxBridge:mymower:sunday2#duration", unit="min"}
Switch                     LandroidMowerSunday2Edgecut             "Edgecut"                   {channel="worxlandroid:mower:MyWorxBridge:mymower:sunday2#edgecut"}
Switch                     LandroidMowerMondayEnable               "Active"                    {channel="worxlandroid:mower:MyWorxBridge:mymower:monday#enable"}
DateTime                   LandroidMowerMondayTime                 "Start time"                {channel="worxlandroid:mower:MyWorxBridge:mymower:monday#time"}
Number:Time                LandroidMowerMondayDuration             "Duration"                  {channel="worxlandroid:mower:MyWorxBridge:mymower:monday#duration", unit="min"}
Switch                     LandroidMowerMondayEdgecut              "Edgecut"                   {channel="worxlandroid:mower:MyWorxBridge:mymower:monday#edgecut"}
Switch                     LandroidMowerMonday2Enable              "Active"                    {channel="worxlandroid:mower:MyWorxBridge:mymower:monday2#enable"}
DateTime                   LandroidMowerMonday2Time                "Start time"                {channel="worxlandroid:mower:MyWorxBridge:mymower:monday2#time"}
Number:Time                LandroidMowerMonday2Duration            "Duration"                  {channel="worxlandroid:mower:MyWorxBridge:mymower:monday2#duration", unit="min"}
Switch                     LandroidMowerMonday2Edgecut             "Edgecut"                   {channel="worxlandroid:mower:MyWorxBridge:mymower:monday2#edgecut"}
Switch                     LandroidMowerTuesdayEnable              "Active"                    {channel="worxlandroid:mower:MyWorxBridge:mymower:tuesday#enable"}
DateTime                   LandroidMowerTuesdayTime                "Start time"                {channel="worxlandroid:mower:MyWorxBridge:mymower:tuesday#time"}
Number:Time                LandroidMowerTuesdayDuration            "Duration"                  {channel="worxlandroid:mower:MyWorxBridge:mymower:tuesday#duration", unit="min"}
Switch                     LandroidMowerTuesdayEdgecut             "Edgecut"                   {channel="worxlandroid:mower:MyWorxBridge:mymower:tuesday#edgecut"}
Switch                     LandroidMowerTuesday2Enable             "Active"                    {channel="worxlandroid:mower:MyWorxBridge:mymower:tuesday2#enable"}
DateTime                   LandroidMowerTuesday2Time               "Start time"                {channel="worxlandroid:mower:MyWorxBridge:mymower:tuesday2#time"}
Number:Time                LandroidMowerTuesday2Duration           "Duration"                  {channel="worxlandroid:mower:MyWorxBridge:mymower:tuesday2#duration", unit="min"}
Switch                     LandroidMowerTuesday2Edgecut            "Edgecut"                   {channel="worxlandroid:mower:MyWorxBridge:mymower:tuesday2#edgecut"}
Switch                     LandroidMowerWednesdayEnable            "Active"                    {channel="worxlandroid:mower:MyWorxBridge:mymower:wednesday#enable"}
DateTime                   LandroidMowerWednesdayTime              "Start time"                {channel="worxlandroid:mower:MyWorxBridge:mymower:wednesday#time"}
Number:Time                LandroidMowerWednesdayDuration          "Duration"                  {channel="worxlandroid:mower:MyWorxBridge:mymower:wednesday#duration", unit="min"}
Switch                     LandroidMowerWednesdayEdgecut           "Edgecut"                   {channel="worxlandroid:mower:MyWorxBridge:mymower:wednesday#edgecut"}
Switch                     LandroidMowerWednesday2Enable           "Active"                    {channel="worxlandroid:mower:MyWorxBridge:mymower:wednesday2#enable"}
DateTime                   LandroidMowerWednesday2Time             "Start time"                {channel="worxlandroid:mower:MyWorxBridge:mymower:wednesday2#time"}
Number:Time                LandroidMowerWednesday2Duration         "Duration"                  {channel="worxlandroid:mower:MyWorxBridge:mymower:wednesday2#duration", unit="min"}
Switch                     LandroidMowerWednesday2Edgecut          "Edgecut"                   {channel="worxlandroid:mower:MyWorxBridge:mymower:wednesday2#edgecut"}
Switch                     LandroidMowerThursdayEnable             "Active"                    {channel="worxlandroid:mower:MyWorxBridge:mymower:thursday#enable"}
DateTime                   LandroidMowerThursdayTime               "Start time"                {channel="worxlandroid:mower:MyWorxBridge:mymower:thursday#time"}
Number:Time                LandroidMowerThursdayDuration           "Duration"                  {channel="worxlandroid:mower:MyWorxBridge:mymower:thursday#duration", unit="min"}
Switch                     LandroidMowerThursdayEdgecut            "Edgecut"                   {channel="worxlandroid:mower:MyWorxBridge:mymower:thursday#edgecut"}
Switch                     LandroidMowerThursday2Enable            "Active"                    {channel="worxlandroid:mower:MyWorxBridge:mymower:thursday2#enable"}
DateTime                   LandroidMowerThursday2Time              "Start time"                {channel="worxlandroid:mower:MyWorxBridge:mymower:thursday2#time"}
Number:Time                LandroidMowerThursday2Duration          "Duration"                  {channel="worxlandroid:mower:MyWorxBridge:mymower:thursday2#duration", unit="min"}
Switch                     LandroidMowerThursday2Edgecut           "Edgecut"                   {channel="worxlandroid:mower:MyWorxBridge:mymower:thursday2#edgecut"}
Switch                     LandroidMowerFridayEnable               "Active"                    {channel="worxlandroid:mower:MyWorxBridge:mymower:friday#enable"}
DateTime                   LandroidMowerFridayTime                 "Start time"                {channel="worxlandroid:mower:MyWorxBridge:mymower:friday#time"}
Number:Time                LandroidMowerFridayDuration             "Duration"                  {channel="worxlandroid:mower:MyWorxBridge:mymower:friday#duration", unit="min"}
Switch                     LandroidMowerFridayEdgecut              "Edgecut"                   {channel="worxlandroid:mower:MyWorxBridge:mymower:friday#edgecut"}
Switch                     LandroidMowerFriday2Enable              "Active"                    {channel="worxlandroid:mower:MyWorxBridge:mymower:friday2#enable"}
DateTime                   LandroidMowerFriday2Time                "Start time"                {channel="worxlandroid:mower:MyWorxBridge:mymower:friday2#time"}
Number:Time                LandroidMowerFriday2Duration            "Duration"                  {channel="worxlandroid:mower:MyWorxBridge:mymower:friday2#duration", unit="min"}
Switch                     LandroidMowerFriday2Edgecut             "Edgecut"                   {channel="worxlandroid:mower:MyWorxBridge:mymower:friday2#edgecut"}
Switch                     LandroidMowerSaturdayEnable             "Active"                    {channel="worxlandroid:mower:MyWorxBridge:mymower:saturday#enable"}
DateTime                   LandroidMowerSaturdayTime               "Start time"                {channel="worxlandroid:mower:MyWorxBridge:mymower:saturday#time"}
Number:Time                LandroidMowerSaturdayDuration           "Duration"                  {channel="worxlandroid:mower:MyWorxBridge:mymower:saturday#duration", unit="min"}
Switch                     LandroidMowerSaturdayEdgecut            "Edgecut"                   {channel="worxlandroid:mower:MyWorxBridge:mymower:saturday#edgecut"}
Switch                     LandroidMowerSaturday2Enable            "Active"                    {channel="worxlandroid:mower:MyWorxBridge:mymower:saturday2#enable"}
DateTime                   LandroidMowerSaturday2Time              "Start time"                {channel="worxlandroid:mower:MyWorxBridge:mymower:saturday2#time"}
Number:Time                LandroidMowerSaturday2Duration          "Duration"                  {channel="worxlandroid:mower:MyWorxBridge:mymower:saturday2#duration", unit="min"}
Switch                     LandroidMowerSaturday2Edgecut           "Edgecut"                   {channel="worxlandroid:mower:MyWorxBridge:mymower:saturday2#edgecut"}
Switch                     LandroidMowerOneTimeEdgecut             "Schedule edgecut"          {channel="worxlandroid:mower:MyWorxBridge:mymower:one-time#edgecut"}
Number:Time                LandroidMowerOneTimeDuration            "Edgecut duration"          {channel="worxlandroid:mower:MyWorxBridge:mymower:one-time#duration", unit="min"}
Number:Temperature         LandroidMowerBatteryTemperature         "Battery temperature"       {channel="worxlandroid:mower:MyWorxBridge:mymower:battery#temperature"}
Number:ElectricPotential   LandroidMowerBatteryVoltage             "Battery voltage"           {channel="worxlandroid:mower:MyWorxBridge:mymower:battery#voltage"}
Number                     LandroidMowerBatteryLevel               "Battery level"             {channel="worxlandroid:mower:MyWorxBridge:mymower:battery#level"}
Number                     LandroidMowerBatteryChargeCycles        "Current charge cycles"     {channel="worxlandroid:mower:MyWorxBridge:mymower:battery#charge-cycles"}
Number                     LandroidMowerBatteryChargeCyclesTotal   "Total charge cycles"       {channel="worxlandroid:mower:MyWorxBridge:mymower:battery#charge-cycles-total"}
Switch                     LandroidMowerBatteryCharging            "Battery charging"          {channel="worxlandroid:mower:MyWorxBridge:mymower:battery#charging"}
Number:Angle               LandroidMowerOrientationPitch           "Pitch"                     {channel="worxlandroid:mower:MyWorxBridge:mymower:orientation#pitch"}
Number:Angle               LandroidMowerOrientationRoll            "Roll"                      {channel="worxlandroid:mower:MyWorxBridge:mymower:orientation#roll"}
Number:Angle               LandroidMowerOrientationYaw             "Yaw"                       {channel="worxlandroid:mower:MyWorxBridge:mymower:orientation#yaw"}
Number:Time                LandroidMowerMetricsBladeTime           "Current blade time"        {channel="worxlandroid:mower:MyWorxBridge:mymower:metrics#blade-time", unit="h"}
Number:Time                LandroidMowerMetricsBladeTimeTotal      "Total blade time"          {channel="worxlandroid:mower:MyWorxBridge:mymower:metrics#blade-time-total", unit="h"}
Number:Length              LandroidMowerMetricsDistance            "Total distance"            {channel="worxlandroid:mower:MyWorxBridge:mymower:metrics#distance", unit="km"}
Number:Time                LandroidMowerMetricsTotalTime           "Total time"                {channel="worxlandroid:mower:MyWorxBridge:mymower:metrics#total-time", unit="h"}
Switch                     LandroidMowerRainState                  "State"                     {channel="worxlandroid:mower:MyWorxBridge:mymower:rain#state"}
Number:Time                LandroidMowerRainCounter                "Counter"                   {channel="worxlandroid:mower:MyWorxBridge:mymower:rain#counter", unit="min"}
Number:Time                LandroidMowerRainDelay                  "Delay"                     {channel="worxlandroid:mower:MyWorxBridge:mymower:rain#delay", unit="min"}
Number:Power               LandroidMowerWifiRssi                   "Rssi"                      {channel="worxlandroid:mower:MyWorxBridge:mymower:wifi#rssi", unit="dBm"}
Number                     LandroidMowerWifiWifiQuality            "Wifi quality"              {channel="worxlandroid:mower:MyWorxBridge:mymower:wifi#wifi-quality"}

```

### $OPENHAB_CONF/sitemaps/landroid.sitemap

```perl
sitemap landroid label="Landroid"
{
    Frame label="Worx Landroid Mower" {
        Text label="Status" item=MyMower icon=none {
        Default item=LandroidMowerCommonStatus               
        Default item=LandroidMowerCommonError                
        Default item=LandroidMowerCommonOnline               
        Default item=LandroidMowerCommonOnlineTimestamp   
        Default item=LandroidMowerConfigTimestamp            
        Default item=LandroidMowerConfigCommand    
        Default item=LandroidMowerAwsPoll                    
        Text item=LandroidMowerAwsConnected label="AWS connected [%s]"
        Default item=LandroidMowerOrientationPitch           
        Default item=LandroidMowerOrientationRoll            
        Default item=LandroidMowerOrientationYaw   
        Default item=LandroidMowerMetricsBladeTime           
        Default item=LandroidMowerMetricsBladeTimeTotal      
        Default item=LandroidMowerMetricsDistance            
        Default item=LandroidMowerMetricsTotalTime     
        Default item=LandroidMowerWifiRssi                   
        Default item=LandroidMowerWifiWifiQuality                                                        
        }          
        Text label="Control" icon=none {
        Default item=LandroidMowerCommonAction   
        Default item=LandroidMowerCommonEnable               
        Default item=LandroidMowerCommonLock
        Default item=LandroidMowerOneTimeEdgecut             
        Setpoint item=LandroidMowerOneTimeDuration minValue=30 maxValue=300 step=30                         
        }
        Text label="Multi zones" icon=none {
        Default item=LandroidMowerMultiZonesEnable           
        Default item=LandroidMowerMultiZonesLastZone         
        Default item=LandroidMowerMultiZonesZone1            
        Default item=LandroidMowerMultiZonesZone2            
        Default item=LandroidMowerMultiZonesZone3            
        Default item=LandroidMowerMultiZonesZone4            
        Default item=LandroidMowerMultiZonesAllocation0      
        Default item=LandroidMowerMultiZonesAllocation1      
        Default item=LandroidMowerMultiZonesAllocation2      
        Default item=LandroidMowerMultiZonesAllocation3      
        Default item=LandroidMowerMultiZonesAllocation4      
        Default item=LandroidMowerMultiZonesAllocation5      
        Default item=LandroidMowerMultiZonesAllocation6      
        Default item=LandroidMowerMultiZonesAllocation7      
        Default item=LandroidMowerMultiZonesAllocation8      
        Default item=LandroidMowerMultiZonesAllocation9      
        }
        Text label="Schedule" icon=none {
        Default item=LandroidMowerScheduleMode               
        Setpoint item=LandroidMowerScheduleTimeExtension minValue=-100 maxValue=100 step=10     
        Default item=LandroidMowerScheduleNextStart          
        Default item=LandroidMowerScheduleNextStop
            Text label="Sunday" icon=none {
            Default item=LandroidMowerSundayEnable               
            Default item=LandroidMowerSundayTime                 
            Default item=LandroidMowerSundayDuration             
            Default item=LandroidMowerSundayEdgecut              
            Default item=LandroidMowerSunday2Enable              
            Default item=LandroidMowerSunday2Time                
            Default item=LandroidMowerSunday2Duration            
            Default item=LandroidMowerSunday2Edgecut   
            } 
            Text label="Monday" icon=none {         
            Default item=LandroidMowerMondayEnable               
            Default item=LandroidMowerMondayTime                 
            Default item=LandroidMowerMondayDuration             
            Default item=LandroidMowerMondayEdgecut              
            Default item=LandroidMowerMonday2Enable              
            Default item=LandroidMowerMonday2Time                
            Default item=LandroidMowerMonday2Duration            
            Default item=LandroidMowerMonday2Edgecut             
            }
            Text label="Tuesday" icon=none {
            Default item=LandroidMowerTuesdayEnable              
            Default item=LandroidMowerTuesdayTime                
            Default item=LandroidMowerTuesdayDuration            
            Default item=LandroidMowerTuesdayEdgecut             
            Default item=LandroidMowerTuesday2Enable             
            Default item=LandroidMowerTuesday2Time               
            Default item=LandroidMowerTuesday2Duration           
            Default item=LandroidMowerTuesday2Edgecut   
            }
            Text label="Wednesday" icon=none {         
            Default item=LandroidMowerWednesdayEnable            
            Default item=LandroidMowerWednesdayTime              
            Default item=LandroidMowerWednesdayDuration          
            Default item=LandroidMowerWednesdayEdgecut           
            Default item=LandroidMowerWednesday2Enable           
            Default item=LandroidMowerWednesday2Time             
            Default item=LandroidMowerWednesday2Duration         
            Default item=LandroidMowerWednesday2Edgecut
            }
            Text label="Thursday" icon=none {          
            Default item=LandroidMowerThursdayEnable             
            Default item=LandroidMowerThursdayTime               
            Default item=LandroidMowerThursdayDuration           
            Default item=LandroidMowerThursdayEdgecut            
            Default item=LandroidMowerThursday2Enable            
            Default item=LandroidMowerThursday2Time              
            Default item=LandroidMowerThursday2Duration          
            Default item=LandroidMowerThursday2Edgecut
            }
            Text label="Friday" icon=none {           
            Default item=LandroidMowerFridayEnable               
            Default item=LandroidMowerFridayTime                 
            Default item=LandroidMowerFridayDuration             
            Default item=LandroidMowerFridayEdgecut              
            Default item=LandroidMowerFriday2Enable              
            Default item=LandroidMowerFriday2Time                
            Default item=LandroidMowerFriday2Duration            
            Default item=LandroidMowerFriday2Edgecut
            }
            Text label="Saturday" icon=none {             
            Default item=LandroidMowerSaturdayEnable             
            Default item=LandroidMowerSaturdayTime               
            Default item=LandroidMowerSaturdayDuration           
            Default item=LandroidMowerSaturdayEdgecut            
            Default item=LandroidMowerSaturday2Enable            
            Default item=LandroidMowerSaturday2Time              
            Default item=LandroidMowerSaturday2Duration          
            Default item=LandroidMowerSaturday2Edgecut
            }                      
        }
        Text label="Battery" icon=none {   
        Default item=LandroidMowerBatteryTemperature         
        Default item=LandroidMowerBatteryVoltage             
        Default item=LandroidMowerBatteryLevel               
        Default item=LandroidMowerBatteryChargeCycles        
        Default item=LandroidMowerBatteryChargeCyclesTotal   
        Text item=LandroidMowerBatteryCharging label="Battery charging [%s]"        
        }          
        Text label="Rainsensor" icon=none {   
        Text item=LandroidMowerRainState label="State [%s]"                  
        Default item=LandroidMowerRainCounter                
        Setpoint item=LandroidMowerRainDelay minValue=30 maxValue=600 step=15                  
        }
    }
}

```

### $OPENHAB_CONF/rules/landroid.rules

```java

rule "Landroid mower status"
when
  Item LandroidMowerCommonError changed or
  Item LandroidMowerCommonStatus changed
then
  if (LandroidMowerCommonError.state != "NO_ERR") {
    MyMower.postUpdate(transform("MAP", "landroid_error_de.map", LandroidMowerCommonError.state.toString))
  } else {
    MyMower.postUpdate(transform("MAP", "landroid_status_de.map", LandroidMowerCommonStatus.state.toString))
  }
end

```

### $OPENHAB_CONF/transform/landroid_error_en.map

```text

UNKNOWN=unknown
NO_ERR=no error
TRAPPED=trapped
LIFTED=lifted
WIRE_MISSING=wire missing
OUTSIDE_WIRE=outside wire
RAINING=raining
CLOSE_DOOR_TO_MOW=close door to mow
CLOSE_DOOR_TO_GO_HOME=close door to go home
BLADE_MOTOR_BLOCKED=blade motor blocked
WHEEL_MOTOR_BLOKED=wheel motor blocked
TRAPPED_TIMEOUT=trapped timeout
UPSIDE_DOWN=upside down
BATTERY_LOW=battery low
REVERSE_WIRE=reverse wire
CHARGE_ERROR=charge error
TIMEOUT_FINDING_HOME=timeout finding home
MOWER_LOCKED=mower locked
BATTERY_OVER_TEMPERATURE=battery over temperature
MOWER_OUTSIDE_WIRE=mower outside wire

```

### $OPENHAB_CONF/transform/landroid_status_en.map

```text

UNKNOWN=unknown
IDLE=idle
HOME=home
START_SEQUENCE=start sequence
LEAVING_HOME=leaving home
FOLLOW_WIRE=follow wire
SEARCHING_HOME=searching home
SEARCHING_WIRE=searching wire
MOWING=mowing
LIFTED=lifted
TRAPPED=trapped
BLADE_BLOCKED=blade blocked
DEBUG=debug
REMOTE_CONTROL=remote control
GOING_HOME=going home
ZONE_TRAINING=zone training
BORDER_CUT=border cut
SEARCHING_ZONE=searching zone
PAUSE=pause
MANUEL_STOP=manuel stop

```
