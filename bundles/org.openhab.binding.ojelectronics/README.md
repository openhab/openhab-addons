# OJElectronics Binding

With this binding it is possible to connect [OWD5/MWD5 Thermostat](https://www.ojelectronics.com/business-areas/wifi-thermostat-owd5-prod400) of OJ Electronics.

At this moment all information read only.

## Road map

[x] Information of thermostat read only. 
[ ] Change the state of the thermostate.
[ ] Automatic discovery.
[ ] Thermostat group thing.

## Supported Things

There are two things:

| Thing                | Type   | Description                         |
|----------------------|--------|-------------------------------------|
| bridge               | Bridge | OJ Electronics Bridge               |
| owd5                 | Thing  | OJ Electronics OWD5/MWD5 Thermostat |

## Discovery

Not at the moment

## Thing Configuration

### OJ Electronics Bridge configuration

It is necessary to configure at least `username` and `password`.

### OJ Electronics OWD5/MWD5 Thermostat

It is necessary to configure only the `serialNumber` of the thermostat.

## Channels

| channel           | type   | description                  |
|-------------------|--------|------------------------------|
| floorTemperature  | Number | Floor temperature            |
| groupName         | Text   | Group name                   |
| groupId           | Number | Group Id                     |
| online            | Switch | Online                       |
| heating           | Switch | Heating                      |
| roomTemperature   | Number | Room temperature             |
| thermostatName    | Text   | Thermostat name              |
| regulationMode    | Text   | Regulation mode              |
