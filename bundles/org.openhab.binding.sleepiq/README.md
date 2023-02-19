# SleepIQ Binding

This binding integrates with the SleepIQ system from Select Comfort for Sleep Number beds.

## Introduction

SleepIQ is a service provided by Select Comfort and sold as an option for Sleep Number beds.
The system collects data about the bed (including individual air chamber data for dual chamber beds).
This information includes whether or not a sleeper is in bed, the current sleep number setting, the pressure of the air chamber, and its link status.
This data can then be analyzed for any number of purposes, including improving sleep.

## Supported Things

The SleepIQ cloud service acts as the bridge.
Each SleepIQ-enabled bed (regardless of the number of air chambers) is a Thing.
Currently, only dual-chamber beds are supported by this binding.

## Discovery

The SleepIQ cloud thing must be added manually with the username and password used to register with the service.
After that, beds are discovered automatically by querying the service.

## Binding Configuration

The binding requires no special configuration.

## Thing Configuration

### Bridge (Thing ID: "cloud")

The bridge requires a username and a password.
Optionally, you can also specify a polling interval.

| Configuration Parameter | Type    | Description                                            | Default |
|-------------------------|---------|--------------------------------------------------------|---------|
| username                | text    | Username of a registered SleepIQ account owner         |         |
| password                | text    | Password of a registered SleepIQ account owner         |         |
| pollingInterval         | integer | Seconds between fetching values from the cloud service | 120     |

### Dual-Chamber Bed (Thing ID: "dualBed")

Each bed requires a bed ID as defined by the SleepIQ service.

| Configuration Parameter | Type    | Description                                  | Default |
|-------------------------|---------|----------------------------------------------|---------|
| bedId                   | text    | The bed identifier identifies a specific bed |         |

### Sample Thing Configuration

```java
Bridge sleepiq:cloud:1 [ username="mail@example.com", password="password", pollingInterval=120 ]
{
    Thing dualBed master [ bedId="-9999999999999999999" ]
    Thing dualBed guest [ bedId="-8888888888888888888" ]
}
```

## Channels

### Dual-Chamber Bed

| Channel Group ID | Group Type | Description                |
|------------------|------------|----------------------------|
| left             | Chamber    | The left side air chamber  |
| right            | Chamber    | The right side air chamber |

### Chamber Channel Group

All channels within this group are read-only, except for the sleepNumber and privacyMode channels.

| Channel ID                        | Item Type      | Description  |
|-----------------------------------|----------------|---------------------------------------------------------------------------------------------------------------------|
| inBed                             | Switch         | The presence of a person or object on the chamber  |
| sleepNumber                       | Number         | The Sleep Number setting of the chamber. Set the sleep number of the chamber by sending a command to the sleepNumber channel with a value between 5 and 100. The value must be a multiple of 5  |

| sleepGoalMinutes                  | Number:Time    | The person's sleep goal in minutes |
| pressure                          | Number         | The current pressure inside the chamber |
| privacyMode                       | Switch         | Enable or disable privacy mode |
| lastLink                          | String         | The amount of time that has passed since a connection was made from the chamber to the cloud service (D d HH:MM:SS) |
| alertId                           | Number         | Identifier for an alert condition with the chamber |
| alertDetailedMessage              | String         | A detailed message describing an alert condition with the chamber |
| todaySleepIQ                      | Number         | The Sleep IQ score for the current day |
| todayAverageHeartRate             | Number         | The average heart rate for the current day |
| todayAverageRespirationRate       | Number         | The average respiration rate for the current day |
| todayMessage                      | String         | A description of the sleep quality for the current day |
| todaySleepDurationSeconds         | Number:Time    | The duration of sleep for the current day |
| todaySleepInBedSeconds            | Number:Time    | The duration of time in bed for the current day |
| todaySleepOutOfBedSeconds         | Number:Time    | The duration of time out of bed for the current day |
| todaySleepRestfulSeconds          | Number:Time    | The duration of restful sleep for the current day |
| todaySleepRestlessSeconds         | Number:Time    | The duration of restless sleep for the current day |
| monthlySleepIQ                    | Number         | The average Sleep IQ score for the current month |
| monthlyAverageHeartRate           | Number         | The average heart rate for the current month |
| monthlyAverageRespirationRate     | Number         | The average respiration rate for the current month |

## Items

Here is a sample item configuration:

```java
Switch      MasterBR_SleepIQ_InBed_Alice             "In Bed [%s]"                     { channel="sleepiq:dualBed:1:master:left#inBed" }
Number      MasterBR_SleepIQ_SleepNumber_Alice       "Sleep Number [%s]"               { channel="sleepiq:dualBed:1:master:left#sleepNumber" }
Number:Time MasterBR_SleepIQ_SleepGoal_Alice         "Sleep Goal [%d min]"             { channel="sleepiq:dualBed:1:master:left#sleepGoalMinutes"
Number      MasterBR_SleepIQ_Pressure_Alice          "Pressure [%s]"                   { channel="sleepiq:dualBed:1:master:left#pressure" }
Switch      MasterBR_SleepIQ_PrivacyMode_Alice       "Privacy Mode [%s]"               { channel="sleepiq:dualBed:1:master:left#privacyMode" }
String      MasterBR_SleepIQ_LastLink_Alice          "Last Update [%s]"                { channel="sleepiq:dualBed:1:master:left#lastLink" }
Number      MasterBR_SleepIQ_AlertId_Alice           "Alert ID [%s]"                   { channel="sleepiq:dualBed:1:master:left#alertId" }
String      MasterBR_SleepIQ_AlertMessage_Alice      "Alert Message [%s]"              { channel="sleepiq:dualBed:1:master:left#alertDetailedMessage" }
Number      MasterBR_SleepIQ_DailySleepIQ_Alice      "Daily Sleep IQ [%.0f]"           { channel="sleepiq:dualBed:1:master:left#todaySleepIQ" }
Number      MasterBR_SleepIQ_DailyHeartRate_Alice    "Daily Heart Rate [%.0f]"         { channel="sleepiq:dualBed:1:master:left#todayAverageHeartRate" }
Number      MasterBR_SleepIQ_DailyRespRate_Alice     "Daily Respiration Rate [%.0f]"   { channel="sleepiq:dualBed:1:master:left#todayAverageRespirationRate"}
String      MasterBR_SleepIQ_DailyMessage_Alice      "Daily Message [%s]"              { channel="sleepiq:dualBed:1:master:left#todayMessage"}
Number:Time MasterBR_SleepIQ_DailyDuration_Alice     "Daily Sleep Duration [%.0f]"     { channel="sleepiq:dualBed:1:master:left#todaySleepDurationSeconds"}
Number:Time MasterBR_SleepIQ_DailyInBed_Alice        "Daily Sleep In Bed [%.0f]"       { channel="sleepiq:dualBed:1:master:left#todaySleepInBedSeconds"}
Number:Time MasterBR_SleepIQ_DailyOutOfBed_Alice     "Daily Sleep Out Of Bed [%.0f]"   { channel="sleepiq:dualBed:1:master:left#todaySleepOutOfBedSeconds"}
Number:Time MasterBR_SleepIQ_DailyRestful_Alice      "Daily Sleep Restful [%.0f]"      { channel="sleepiq:dualBed:1:master:left#todaySleepRestfulSeconds"}
Number:Time MasterBR_SleepIQ_DailyRestless_Alice     "Daily Sleep Restless [%.0f]"     { channel="sleepiq:dualBed:1:master:left#todaySleepRestlessSeconds"}
Number      MasterBR_SleepIQ_MonthlySleepIQ_Alice    "Monthly Sleep IQ [%d s]"         { channel="sleepiq:dualBed:1:master:left#monthlySleepIQ"}
Number      MasterBR_SleepIQ_MonthlyHeartRate_Alice  "Monthly Heart Rate [%.0f]"       { channel="sleepiq:dualBed:1:master:left#monthlyAverageHeartRate"}
Number      MasterBR_SleepIQ_MonthlyRespRate_Alice   "Monthly Respiration Rate [%.0f]" { channel="sleepiq:dualBed:1:master:left#monthlyAverageRespirationRate"}


Switch      MasterBR_SleepIQ_InBed_Bob               "In Bed [%s]"                     { channel="sleepiq:dualBed:1:master:right#inBed" }
Number      MasterBR_SleepIQ_SleepNumber_Bob         "Sleep Number [%s]"               { channel="sleepiq:dualBed:1:master:right#sleepNumber" }
Number      MasterBR_SleepIQ_SleepGoal_Alice         "Sleep Goal [%d min]"             { channel="sleepiq:dualBed:1:master:left#sleepGoalMinutes"
Number:Time MasterBR_SleepIQ_Pressure_Bob            "Pressure [%s]"                   { channel="sleepiq:dualBed:1:master:right#pressure" }
Switch      MasterBR_SleepIQ_PrivacyMode_Bob         "Privacy Mode [%s]"               { channel="sleepiq:dualBed:1:master:right#privacyMode" }
String      MasterBR_SleepIQ_LastLink_Bob            "Last Update [%s]"                { channel="sleepiq:dualBed:1:master:right#lastLink" }
Number      MasterBR_SleepIQ_AlertId_Bob             "Alert ID [%s]"                   { channel="sleepiq:dualBed:1:master:right#alertId" }
String      MasterBR_SleepIQ_AlertMessage_Bob        "Alert Message [%s]"              { channel="sleepiq:dualBed:1:master:right#alertDetailedMessage" }
Number      MasterBR_SleepIQ_DailySleepIQ_Bob        "Daily Sleep IQ [%.0f]"           { channel="sleepiq:dualBed:1:master:right#todaySleepIQ" }
Number      MasterBR_SleepIQ_DailyHeartRate_Bob      "Daily Heart Rate [%.0f]"         { channel="sleepiq:dualBed:1:master:right#todayAverageHeartRate" }
Number      MasterBR_SleepIQ_DailyRespRate_Bob       "Daily Respiration Rate [%.0f]"   { channel="sleepiq:dualBed:1:master:right#todayAverageRespirationRate"}
String      MasterBR_SleepIQ_DailyMessage_Bob        "Daily Message [%s]"              { channel="sleepiq:dualBed:1:master:right#todayMessage"}
Number:Time MasterBR_SleepIQ_DailyDuration_Bob       "Daily Sleep Duration [%d s]"     { channel="sleepiq:dualBed:1:master:right#todaySleepDurationSeconds"}
Number:Time MasterBR_SleepIQ_DailyInBed_Bob          "Daily Sleep In Bed [%.0f]"       { channel="sleepiq:dualBed:1:master:right#todaySleepInBedSeconds"}
Number:Time MasterBR_SleepIQ_DailyOutOfBed_Bob       "Daily Sleep Out Of Bed [%.0f]"   { channel="sleepiq:dualBed:1:master:right#todaySleepOutOfBedSeconds"}
Number:Time MasterBR_SleepIQ_DailyRestful_Bob        "Daily Sleep Restful [%.0f]"      { channel="sleepiq:dualBed:1:master:right#todaySleepRestfulSeconds"}
Number:Time MasterBR_SleepIQ_DailyRestless_Bob       "Daily Sleep Restless [%.0f]"     { channel="sleepiq:dualBed:1:master:right#todaySleepRestlessSeconds"}
Number      MasterBR_SleepIQ_MonthlySleepIQ_Bob      "Monthly Sleep IQ [%.0f]"         { channel="sleepiq:dualBed:1:master:right#monthlySleepIQ"}
Number      MasterBR_SleepIQ_MonthlyHeartRate_Bob    "Monthly Heart Rate [%.0f]"       { channel="sleepiq:dualBed:1:master:right#monthlyAverageHeartRate"}
Number      MasterBR_SleepIQ_MonthlyRespRate_Bob     "Monthly Respiration Rate [%.0f]" { channel="sleepiq:dualBed:1:master:right#monthlyAverageRespirationRate"}
```
