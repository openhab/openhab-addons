# OJElectronics Binding

With this binding it is possible to connect [OWD5/MWD5 Thermostat](https://www.ojelectronics.com/business-areas/wifi-thermostat-owd5-prod400) of OJ Electronics.

At this moment all information is read only.

## Supported Things

There are two things:

| Thing                | Type   | Description                         |
|----------------------|--------|-------------------------------------|
| ojcloud              | Bridge | OJ Electronics Cloud Connector      |
| owd5                 | Thing  | OJ Electronics OWD5/MWD5 Thermostat |

## Discovery

Not supported at the moment.

## Thing Configuration

### OJ Electronics Bridge configuration (ojcloud)

| Parameter             | Description                                                              |
|-----------------------|--------------------------------------------------------------------------|
| userName              | user name from the OJElectronics App (required)                          |
| password              | password from the OJElectronics App (required)                           |
| apiKey                | API key. You get the key from your local distributor.                    |
| apiUrl                | URL of the API endpoint. Optional, the default value should always work. |
| refreshDelayInSeconds | Refresh interval in seconds. Optional, the default value is 30 seconds.  |
| customerId            | Customer ID. Optional, the default value should always work.             |
| softwareVersion       | Software version. Optional, the default value should always work.        |

### OJ Electronics OWD5/MWD5 Thermostat configuration (owd5)

| Parameter             | Description                                                              |
|-----------------------|--------------------------------------------------------------------------|
| serialNumber          | serial number from the OJElectronics App or the thermostat (required)    |

## Channels

| Channel            | Type               | Description                                                                        |
|--------------------|--------------------|------------------------------------------------------------------------------------|
| floorTemperature   | Number:Temperature | Floor temperature                                                                  |
| groupName          | Text               | Group name                                                                         |
| groupId            | Number             | Group Id                                                                           |
| online             | Contact            | Online                                                                             |
| heating            | Contact            | Heating                                                                            |
| roomTemperature    | Number:Temperature | Room temperature                                                                   |
| thermostatName     | Text               | Thermostat name                                                                    |
| regulationMode     | Text               | Regulation mode                                                                    |
| serialNumber       | Text               | Serial number                                                                      |
| comfortSetpoint    | Number:Temperature | Target comfort temperature                                                         |
| comfortEndTime     | Date time          | Date and time when the thermostat switchs back from comfort mode to automatic mode |
| boostEndTime       | Date time          | Date and time when the thermostat switchs back from boost mode to automatic mode   |
| manualModeSetpoint | Number:Temperature | Target temperature of the manual mode                                              |
| vacationEnabled    | Switch             | Vacation is enabled                                                                |

## Example

This example shows how to configure the OJElecttronics binding.

### demo.things

```
Binding ojelectronics:ojcloud:myCloud "My Cloud" @ "My Home" [ userName="MyUserName" password="MyPassword" apiKey="The Key" ] {
    Thing owd5 myThermostat [ serialNumber="123" ]
}
```

### demo.items

```
Number Bath_Floor_Temperature "Bathroom: Floor Temperature" {channel="ojelectronics:owd5:myThermostat:floorTemperature"}
String Bath_Mode "Bathroom: Mode" {channel="ojelectronics:owd5:myThermostat:regulationMode"}
```

### demo.sitemap

```
sitemap myHome label="my Home"{
  Text item=Bath_Floor_Temperature
  Text item=Bath_Mode
}
```

