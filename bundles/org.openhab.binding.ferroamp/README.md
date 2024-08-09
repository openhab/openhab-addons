## Ferroamp Binding

The Ferroamp binding is used to get live data from Ferroamp EnergyHub

The Ferroamp binding is compatible with EnergyHub Wall and EnergyHub XL, and use connection to your local EnergyHub via LAN or similar.
Data and commands are received/sent using MQTT where the user connects to the MQTT broker residing on the EnergyHub.

Contact Ferroamp support to enable MQTT in the EnergyHub and to get the Username and Password:

https://ferroamp.com/om-ferroamp/

## Supported Things

The binding retrieves data from the different parts of the Ferroamp EnergyHub such as:

- `Ehub`: EnergyHub Wall and EnergyHub XL.
- `Sso`: Solar string optimizer.
- `Eso`: Bidirectional DC/DC converter for connection of battery.
- `Esm`: Energy Storage Module.

## Discovery

Discovery is not supported.

## Thing Configuration

The following configuration-parameters are available.

| Name            | Type    | Description                           | Default | Required | Advanced |
|-----------------|---------|---------------------------------------|---------|----------|----------|
| hostName        | text    | Hostname or IP address of the device  | N/A     | yes      | no       |
| userName        | text    | Username to access the device         | N/A     | yes      | no       |
| password        | text    | Password to access the device         | N/A     | yes      | no       |
| hasBattery      | boolean | Is there a battery connected or not ? | N/A     | no       | yes      |

## Channels

| Channel Type ID      | Item Type                  | Read/Write | Description                              |
|---------------------------------------------------|------------|------------------------------------------|
| ehub-wloadconsq-l1   | Number:Energy              | R          |                                          |
| ehub-wloadconsq-l2   | Number:Energy              | R          |                                          |
| ehub-wloadconsq-l3   | Number:Energy              | R          |                                          |
| ehub-iloadd-l1       | Number:ElectricCurrent     | R          |                                          |
| ehub-iloadd-l2       | Number:ElectricCurrent     | R          |                                          |
| ehub-iloadd-l3       | Number:ElectricCurrent     | R          |                                          |
| ehub-winvconsq_3p    | Number:ElectricCurrent     | R          |                                          |
| ehub-wextconsq-l1    | Number:Energy              | R          |                                          |
| ehub-wextconsq-l2    | Number:Energy              | R          |                                          |
| ehub-wextconsq-l3    | Number:Energy              | R          |                                          |
| ehub-winvprodq_3p    | Number:ElectricCurrent     | R          |                                          |
| ehub-winvconsq-l1    | Number:ElectricCurrent     | R          |                                          |
| ehub-winvconsq-l2    | Number:ElectricCurrent     | R          |                                          |
| ehub-winvconsq-l3    | Number:ElectricCurrent     | R          |                                          |
| ehub-iext-l1         | Number:ElectricCurrent     | R          |                                          |
| ehub-iext-l2         | Number:ElectricCurrent     | R          |                                          |
| ehub-iext-l3         | Number:ElectricCurrent     | R          |                                          |
| ehub-iloadq-l1       | Number:ElectricCurrent     | R          |                                          |
| ehub-iloadq-l2       | Number:ElectricCurrent     | R          |                                          |
| ehub-iloadq-l3       | Number:ElectricCurrent     | R          |                                          |
| ehub-wloadprodq_3p   | Number:ElectricCurrent     | R          |                                          |
| ehub-iace-l1         | Number:ElectricCurrent     | R          | ACE equalization L1                      |
| ehub-iace-l2         | Number:ElectricCurrent     | R          | ACE equalization L2                      |
| ehub-iace-l3         | Number:ElectricCurrent     | R          | ACE equalization L3                      |
| ehub-pload-l1        | Number:Power               | R          |                                          |
| ehub-pload-l2        | Number:Power               | R          |                                          |
| ehub-pload-l3        | Number:Power               | R          |                                          |
| ehub-pinvreactive-l1 | Number:Power               | R          | Inverter power, Reactive L1              |
| ehub-pinvreactive-l2 | Number:Power               | R          | Inverter power, Reactive L2              |
| ehub-pinvreactive-l3 | Number:Power               | R          | Inverter power, Reactive L3              |
| ehub-ts              | DateTime                   | R          | Time stamp when message was published    |
| ehub-ploadreactive-l1| Number:Power               | R          |                                          |
| ehub-ploadreactive-l2| Number:Power               | R          |                                          |
| ehub-ploadreactive-l3| Number:Power               | R          |                                          |
| ehub-state           | String                     | R          | State of the system                      |
| ehub-wloadprodq-l1   | Number:Energy              | R          |                                          |
| ehub-wloadprodq-l2   | Number:Energy              | R          |                                          |
| ehub-wloadprodq-l3   | Number:Energy              | R          |                                          |
| ehub-ppv             | Number:Power               | R          | Only sent when system has PV             |
| ehub-pinv-l1         | Number:Power               | R          | Inverter power, active L1                |
| ehub-pinv-l2         | Number:Power               | R          | Inverter power, active L2                |
| ehub-pinv-l3         | Number:Power               | R          | Inverter power, active L3                |
| ehub-iextq-l1        | Number:ElectricCurrent     | R          | External/grid active current L1          |
| ehub-iextq-l2        | Number:ElectricCurrent     | R          | External/grid active current L2          |
| ehub-iextq-l3        | Number:ElectricCurrent     | R          | External/grid active current L3          |
| ehub-pext-l1         | Number:Power               | R          | External/grid power, active L1           |
| ehub-pext-l2         | Number:Power               | R          | External/grid power, active L2           |
| ehub-pext-l3         | Number:Power               | R          | External/grid power, active L3           |
| ehub-wextprodq-l1    | Number:Energy              | R          |                                          |
| ehub-wextprodq-l2    | Number:Energy              | R          |                                          |
| ehub-wextprodq-l3    | Number:Energy              | R          |                                          |
| ehub-wpv             | Number:Energy              | R          | Only sent when system has PV             |
| ehub-pextreactive-l1 | Number:Power               | R          | External/grid power, Reactive L1         |
| ehub-pextreactive-l2 | Number:Power               | R          | External/grid power, Reactive L2         |
| ehub-pextreactive-l3 | Number:Power               | R          | External/grid power, Reactive L3         |
| ehub-udcpos          | Number:ElectricPotential   | R          | Positiv DC link voltage                  |
| ehub-udcneg          | Number:ElectricPotential   | R          | Negativ DC link voltage                  |
| ehub-sext            | Number:Energy              | R          | Apparent power                           |
| ehub-iextd-l1        | Number:ElectricCurrent     | R          | External/grid Reactive current L1        |
| ehub-iextd-l2        | Number:ElectricCurrent     | R          | External/grid Reactive current L2        |
| ehub-iextd-l3        | Number:ElectricCurrent     | R          | External/grid Reactive current L3        |
| ehub-wextconsq_3p    | Number:ElectricCurrent     | R          |                                          |
| ehub-ild-l1          | Number:ElectricCurrent     | R          | Inverter Reactive current L1             |
| ehub-ild-l2          | Number:ElectricCurrent     | R          | Inverter Reactive current L2             |
| ehub-ild-l3          | Number:ElectricCurrent     | R          | Inverter Reactive current L3             |
| ehub-gridfreq        | Number:Frequency           | R          | Estimated Grid Frequency                 |
| ehub-wloadconsq_3p   | Number:ElectricCurrent     | R          |                                          |
| ehub-ul-l1           | Number:ElectricPotential   | R          | External voltage L1                      |
| ehub-ul-l2           | Number:ElectricPotential   | R          | External voltage L2                      |
| ehub-ul-l3           | Number:ElectricPotential   | R          | External voltage L3                      |
| ehub-wextprodq_3p    | Number:ElectricCurrent     | R          |                                          |
| ehub-ilq-l1          | Number:ElectricCurrent     | R          | Inverter active current L1               |
| ehub-ilq-l2          | Number:ElectricCurrent     | R          | Inverter active current L2               |
| ehub-ilq-l3          | Number:ElectricCurrent     | R          | Inverter active current L3               |
| ehub-winvprodq-l1    | Number:Energy              | R          |                                          |
| ehub-winvprodq-l2    | Number:Energy              | R          |                                          |
| ehub-winvprodq-l3    | Number:Energy              | R          |                                          |
| ehub-il-l1           | Number:ElectricCurrent     | R          | Inverter RMS current L1                  |
| ehub-il-l2           | Number:ElectricCurrent     | R          | Inverter RMS current L2                  |
| ehub-il-l3           | Number:ElectricCurrent     | R          | Inverter RMS current L3                  |
| ehub-wbatprod        | Number:Energy              | R          | Only sent when system has batteries      |
| ehub-wpbatcons       | Number:Energy              | R          | Only sent when system has batteries      |
| ehub-soc             | Number:Dimensionless       | R          | State Of Charge for the system           |
| ehub-soh             | Number:Dimensionless       | R          | State Of Health for the system           |
| ehub-pbat            | Number:Power               | R          | Only sent when system has batteries      |
| ehub-ratedcap        | Number:Energy              | R          | Total rated capacity of all batteries in system|
| ssos0-relaystatus    | String                     | R          | 0 = relay closed (i.e running power), 1 = relay open/disconnected, 2 = precharge|
| ssos0-temp           | Number:Temperature         | R          | Temperature measured on PCB of SSO-0     |
| ssos0-wpv            | Number:Energy              | R          | Total energy produced by SSO-0           |
| ssos0-ts             | DateTime                   | R          | Time stamp when message was published    |
| ssos0-udc            | Number:ElectricPotential   | R          | DC link voltage as measured by SSO-0     |
| ssos0-faultcode      | String                     | R          | See section 4.1.3.1                      |
| ssos0-ipv            | Number:ElectricCurrent     | R          | Measured on PV string side               |
| ssos0-upv            | Number:ElectricPotential   | R          | Measured on PV string side               |
| ssos0-id             | String                     | R          | Unique identifier of SSO-0               |
| ssos1-relaystatus    | String                     | R          | 0 = relay closed (i.e running power), 1 = relay open/disconnected, 2 = precharge|
| ssos1-temp           | Number:Temperature         | R          | Temperature measured on PCB of SSO-1     |
| ssos1-wpv            | Number:Energy              | R          | Total energy produced by SSO-1           |
| ssos1-ts             | DateTime                   | R          | Time stamp when message was published    |
| ssos1-udc            | Number:ElectricPotential   | R          | DC link voltage as measured by SSO-1     |
| ssos1-faultcode      | String                     | R          | See section 4.1.3.1                      |
| ssos1-ipv            | Number:ElectricCurrent     | R          | Measured on PV string side               |
| ssos1-upv            | Number:ElectricPotential   | R          | Measured on PV string side               |
| ssos1-id             | String                     | R          | Unique identifier of SSO-1               |
| ssos2-relaystatus    | String                     | R          | 0 = relay closed (i.e running power), 1 = relay open/disconnected, 2 = precharge|
| ssos2-temp           | Number:Temperature         | R          | Temperature measured on PCB of SSO-2     |
| ssos2-wpv            | Number:Energy              | R          | Total energy produced by SSO-2           |
| ssos2-ts             | DateTime                   | R          | Time stamp when message was published    |
| ssos2-udc            | Number:ElectricPotential   | R          | DC link voltage as measured by SSO-2     |
| ssos2-faultcode      | String                     | R          | See section 4.1.3.1                      |
| ssos2-ipv            | Number:ElectricCurrent     | R          | Measured on PV string side               |
| ssos2-upv            | Number:ElectricPotential   | R          | Measured on PV string side               |
| ssos2-id             | String                     | R          | Unique identifier of SSO-2               |
| ssos3-relaystatus    | String                     | R          | 0 = relay closed (i.e running power), 1 = relay open/disconnected, 2 = precharge|
| ssos3-temp           | Number:Temperature         | R          | Temperature measured on PCB of SSO-3     |
| ssos3-wpv            | Number:Energy              | R          | Total energy produced by SSO-3           |
| ssos3-ts             | DateTime                   | R          | Time stamp when message was published    |
| ssos3-udc            | Number:ElectricPotential   | R          | DC link voltage as measured by SSO-3     |
| ssos3-faultcode      | String                     | R          | See section 4.1.3.1                      |
| ssos3-ipv            | Number:ElectricCurrent     | R          | Measured on PV string side               |
| ssos3-upv            | Number:ElectricPotential   | R          | Measured on PV string side               |
| ssos3-id             | String                     | R          | Unique identifier of SSO-3               |
| eso-faultcode        | String                     | R          | See section 4.1.3.1                      |
| eso-id               | String                     | R          | Unique identifier                        |
| eso-ibat             | Number:ElectricCurrent     | R          | Measured on battery side                 |
| eso-ubat             | Number:ElectricPotential   | R          | Measured on battery side                 |
| eso-relaystatus      | String                     | R          | 0 = relay closed (i.e running power), 1 = relay open/disconnected, 2 = precharge|
| eso-soc              | Number:Dimensionless       | R          | State of Charge for ESO                  |
| eso-temp             | Number:Temperature         | R          | Measured inside ESO                      |
| eso-wpbatcons        | Number:Energy              | R          | Total energy produced by ESO, i.e total energy charged|
| eso-wbatprod         | Number:Energy              | R          | Total energy produced by ESO, i.e total energy discharged|
| eso-udc              | Number:Power               | R          | DC link voltage as measured by ESO       |
| eso-ts               | DateTime                   | R          | Time stamp when message was published    |
| esm-soh              | Number:Dimensionless       | R          |                                          |
| esm-soc              | Number:Dimensionless       | R          |                                          |
| esm-rated-capacity   | Number:Energy              | R          | Rated capacity of battery                |
| esm-id               | String                     | R          | Unique identifier of battery. If available, this will be the unique id that the battery reports. |
| esm-rated-power      | Number:Power               | R          | Rated power of battery                   |
| esm-status           | String                     | R          | Dependent on battery manufacturer        |
| esm-ts               | DateTime                   | R          | Time stamp when message was published    |

The following channels are available for `Ferroamp` EnergyHub configuration. Please, see Ferroamp documentation for more details.

| Channel Type ID      | Item Type                  | Read/Write | Description                              |
|---------------------------------------------------|------------|------------------------------------------|
| request-charge       | String                     | W          | Set charge power, value in Watt          |
| request-discharge    | String                     | W          | Set discharge power, value in Watt       |
| request-auto         | String                     | W          | Set auto power. Returning control of batteries to system, value as auto.|

# Full Example

## Thing

```java
Thing ferroamp:energyhub:myenergyhub [ hostName="energyhub-ip", userName="myUserName", password="myPassword", hasBattery=false ]
```

## Items

```java
Number:Energy Ferroamp "EHUB, WLoadConsq" <energy> { channel="ferroamp:energyhub:myenergyhub:ehub-wloadconsq-l1" }
String Ferroamp "RequestCharge" <energy> { channel="ferroamp:energyhub:myenergyhub:request-charge" }
```

## Rules

Ex. Set Charging with 5000W with cron trigger:

```yaml
triggers:
   id: "1"
    configuration:
      cronExpression: 0 0/2 * * * ? *
    type: timer.GenericCronTrigger
conditions: []
actions:
   inputs: {}
    id: "2"
    configuration:
      type: application/vnd.openhab.dsl.rule
      script: ChargingWith5000W.sendCommand("5000")
    type: script.ScriptAction
```
