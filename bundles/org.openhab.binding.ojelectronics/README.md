# OJElectronics Binding

With this binding it is possible to connect [OWD5/MWD5 Thermostat](https://ojelectronics.com/floorheating/products/wifi-thermostat-owd5/) of OJ Electronics.

## Supported Things

There are two things:

| Thing                | Type   | Description                         |
|----------------------|--------|-------------------------------------|
| ojcloud              | Bridge | OJ Electronics Cloud Connector      |
| owd5                 | Thing  | OJ Electronics OWD5/MWD5 Thermostat |

## Discovery

After the ojcloud bridge is successfully initialized all thermostats will be discovered.

### OJ Electronics Bridge configuration (ojcloud)

| Parameter             | Description                                                              |
|-----------------------|--------------------------------------------------------------------------|
| userName              | user name from the OJElectronics App (required)                          |
| password              | password from the OJElectronics App (required)                           |
| apiKey                | API key. You get the key from your local distributor.                    |
| apiUrl                | URL of the API endpoint. Optional, the default value should always work. |
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
| groupName          | String             | Group name                                                                         |
| groupId            | Number             | Group Id                                                                           |
| online             | Contact            | Online                                                                             |
| heating            | Contact            | Heating                                                                            |
| roomTemperature    | Number:Temperature | Room temperature                                                                   |
| thermostatName     | String             | Thermostat name                                                                    |
| regulationMode     | String             | Regulation mode                                                                    |
| serialNumber       | String             | Serial number                                                                      |
| comfortSetpoint    | Number:Temperature | Target comfort temperature                                                         |
| comfortEndTime     | DateTime           | Date and time when the thermostat switches back from comfort mode to automatic mode |
| boostEndTime       | DateTime           | Date and time when the thermostat switches back from boost mode to automatic mode   |
| manualSetpoint     | Number:Temperature | Target temperature of the manual mode                                              |
| vacationEnabled    | Switch             | Vacation is enabled                                                                |
| vacationBeginDay   | DateTime           | Vacation start date                                                                |
| vacationEndDay     | DateTime           | Vacation end date                                                                  |

## Example

This example shows how to configure the OJElectronics binding.

### `demo.things` Example

```java
Bridge ojelectronics:ojcloud:myCloud "My Cloud" @ "My Home" [ userName="MyUserName", password="MyPassword", apiKey="The Key" ] {
    Thing owd5 myThermostat [ serialNumber="123" ]
}
```

### demo.items

```java
Number Bath_Floor_Temperature "Bathroom: Floor Temperature" {channel="ojelectronics:owd5:myCloud:myThermostat:floorTemperature"}
String Bath_Mode "Bathroom: Mode" {channel="ojelectronics:owd5:myCloud:myThermostat:regulationMode"}
```

### demo.sitemap

```perl
sitemap myHome label="my Home"{
  Text item=Bath_Floor_Temperature
  Text item=Bath_Mode
}
```
