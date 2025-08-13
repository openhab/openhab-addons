# Ferroamp Binding

The Ferroamp binding is used to get live data from Ferroamp EnergyHub

The Ferroamp binding is compatible with EnergyHub Wall and EnergyHub XL, and connects to your local EnergyHub via LAN.
Data and commands are received/sent using MQTT where the user connects to the MQTT broker residing on the EnergyHub.
The communication with the broker might take some minute to establish, so Please just be patient. The Thing will be
in state INITIALIZATION and UNKNOWN during this time and then change to state ONLINE once connection is established.

*note* Contact Ferroamp support to enable MQTT in the EnergyHub and to get the Username and Password:

<https://ferroamp.com/om-ferroamp/>

Every Sso has a unique serial number which is marked on the side of the SSO unit. Ex. PS00990-A04-S20120476.
This number is to identify the respective Sso Pv-string.

## Supported Things

This binding supports one Thing, the `energyhub`. This hub allows data to be captured from the different parts of the energy system such as:

- ehub: EnergyHub Wall and EnergyHub XL.
- sso : Solar string optimizer.
- eso : Bidirectional DC/DC converter for connection of battery.
- esm : Energy Storage Module.

## Discovery

Discovery is not supported.

## Thing Configuration

The following configuration parameters are available.

| Name            | Type    | Description                                           | Default | Required | Advanced |
|-----------------|---------|-------------------------------------------------------|---------|----------|----------|
| hostName        | Text    | Hostname or IP address of the device                  | N/A     | yes      | no       |
| userName        | Text    | Username to access the device                         | N/A     | yes      | no       |
| password        | Text    | Password to access the device                         | N/A     | yes      | no       |
| refreshInterval | Integer | Define polling-interval in seconds                    | 60      | yes      | yes      |
| hasBattery      | boolean | Has the system a battery connected?                   | N/A     | no       | yes      |

## Channels

| Channel Type ID                       | Item Type                  | Read/Write | Label                                    | Description                                                                                          |
|--------------------------------------------------------------------|------------|------------------------------------------|------------------------------------------------------------------------------------------------------|
| grid-frequency                        | Number:Frequency           | R          | Grid Frequency                           | Grid frequency                                                                                       |
| ace-current-l1                        | Number:ElectricCurrent     | R          | ACE Current L1                           | Adaptive Current Equalization (ACE) equalization current set-points in Amps root mean square (Arms)  |
| ace-current-l2                        | Number:ElectricCurrent     | R          | ACE Current L2                           | Adaptive Current Equalization (ACE) equalization current set-points in Amps root mean square (Arms)  |
| ace-current-l3                        | Number:ElectricCurrent     | R          | ACE Current L3                           | Adaptive Current Equalization (ACE) equalization current set-points in Amps root mean square (Arms)  |
| grid-voltage-l1                       | Number:ElectricPotential   | R          | Grid Voltage L1                          | Grid voltage                                                                                         |
| grid-voltage-l2                       | Number:ElectricPotential   | R          | Grid Voltage L2                          | Grid voltage                                                                                         |
| grid-voltage-l3                       | Number:ElectricPotential   | R          | Grid Voltage L3                          | Grid voltage                                                                                         |
| inverter-rms-current-l1               | Number:ElectricCurrent     | R          | Inverter RMS Current L1                  | Inverter RMS current                                                                                 |
| inverter-rms-current-l2               | Number:ElectricCurrent     | R          | Inverter RMS Current L2                  | Inverter RMS current                                                                                 |
| inverter-rms-current-l3               | Number:ElectricCurrent     | R          | Inverter RMS Current L3                  | Inverter RMS current                                                                                 |
| inverter-reactive-current-l1          | Number:ElectricCurrent     | R          | Inverter Reactive Current L1             | Inverter reactive current                                                                            |
| inverter-reactive-current-l2          | Number:ElectricCurrent     | R          | Inverter Reactive Current L2             | Inverter reactive current                                                                            |
| inverter-reactive-current-l3          | Number:ElectricCurrent     | R          | Inverter Reactive Current L3             | Inverter reactive current                                                                            |
| inverter-active-current-l1            | Number:ElectricCurrent     | R          | Inverter Active current L1               | Inverter active current                                                                              |
| inverter-active-current-l2            | Number:ElectricCurrent     | R          | Inverter Active current L2               | Inverter active current                                                                              |
| inverter-active-current-l3            | Number:ElectricCurrent     | R          | Inverter Active current L3               | Inverter active current                                                                              |
| grid-current-l1                       | Number:ElectricCurrent     | R          | Grid Current L1                          | Grid RMS current                                                                                     |
| grid-current-l2                       | Number:ElectricCurrent     | R          | Grid Current L2                          | Grid RMS current                                                                                     |
| grid-current-l3                       | Number:ElectricCurrent     | R          | Grid Current L3                          | Grid RMS current                                                                                     |
| grid-reactive-current-l1              | Number:ElectricCurrent     | R          | Grid Reactive Current L1                 | Grid reactive current                                                                                |
| grid-reactive-current-l2              | Number:ElectricCurrent     | R          | Grid Reactive Current L2                 | Grid reactive current                                                                                |
| grid-reactive-current-l3              | Number:ElectricCurrent     | R          | Grid Reactive Current L3                 | Grid reactive current                                                                                |
| grid-active-current-l1                | Number:ElectricCurrent     | R          | Grid Active Current L1                   | Grid active current                                                                                  |
| grid-active-current-l2                | Number:ElectricCurrent     | R          | Grid Active Current L2                   | Grid active current                                                                                  |
| grid-active-current-l3                | Number:ElectricCurrent     | R          | Grid Active Current L3                   | Grid active current                                                                                  |
| inverter-load-reactive-current-l1     | Number:ElectricCurrent     | R          | Inverter Load Reactive Current L1        |                                                                                                      |
| inverter-load-reactive-current-l2     | Number:ElectricCurrent     | R          | Inverter Load Reactive Current L2        |                                                                                                      |
| inverter-load-reactive-current-l3     | Number:ElectricCurrent     | R          | Inverter Load Reactive Current L3        |                                                                                                      |
| inverter-load-active-current-l1       | Number:ElectricCurrent     | R          | Inverter Load Active Current L1          |                                                                                                      |
| inverter-load-active-current-l2       | Number:ElectricCurrent     | R          | Inverter Load Active Current L2          |                                                                                                      |
| inverter-load-active-current-l3       | Number:ElectricCurrent     | R          | Inverter Load Active Current L3          |                                                                                                      |
| apparent-power                        | Number:Power               | R          | Apparent Power                           | Apparent power                                                                                       |
| grid-power-active-l1                  | Number:Power               | R          | Grid Power Active L1                     | Grid power, active                                                                                   |
| grid-power-active-l2                  | Number:Power               | R          | Grid Power Active L2                     | Grid power, active                                                                                   |
| grid-power-active-l3                  | Number:Power               | R          | Grid Power Active L3                     | Grid power, active                                                                                   |
| grid-power-reactive-l1                | Number:Power               | R          | Grid Power Reactive L1                   | Grid power, reactive                                                                                 |
| grid-power-reactive-l2                | Number:Power               | R          | Grid Power Reactive L2                   | Grid power, reactive                                                                                 |
| grid-power-reactive-l3                | Number:Power               | R          | Grid Power Reactive L3                   | Grid power, reactive                                                                                 |
| inverter-power-active-l1              | Number:Power               | R          | Inverter Power Active L1                 | Inverter power, active                                                                               |
| inverter-power-active-l2              | Number:Power               | R          | Inverter Power Active L2                 | Inverter power, active                                                                               |
| inverter-power-active-l3              | Number:Power               | R          | Inverter Power Active L3                 | Inverter power, active                                                                               |
| inverter-power-reactive-l1            | Number:Power               | R          | Inverter Power Reactive L1               | Inverter power, reactive                                                                             |
| inverter-power-reactive-l1            | Number:Power               | R          | Inverter Power Reactive L2               | Inverter power, reactive                                                                             |
| inverter-power-reactive-l1            | Number:Power               | R          | Inverter Power Reactive L3               | Inverter power, reactive                                                                             |
| consumption-power-l1                  | Number:Power               | R          | Consumption Power L1                     |                                                                                                      |
| consumption-power-l2                  | Number:Power               | R          | Consumption Power L2                     |                                                                                                      |
| consumption-power-l3                  | Number:Power               | R          | Consumption Power L3                     |                                                                                                      |
| consumption-power-reactive-l1         | Number:Power               | R          | Consumption Power Reactive L1            |                                                                                                      |
| consumption-power-reactive-l2         | Number:Power               | R          | Consumption Power Reactive L2            |                                                                                                      |
| consumption-power-reactive-l3         | Number:Power               | R          | Consumption Power Reactive L3            |                                                                                                      |
| solar-pv                              | Number:Power               | R          | Solar Power                              | Only sent when system has PV                                                                         |
| positive-dc-link-voltage              | Number:ElectricPotential   | R          | Positiv DC Link Voltage                  | Positiv DC link voltage                                                                              |
| negative-dc-link-voltage              | Number:ElectricPotential   | R          | Negative DC Link Voltage                 | Negative DC link voltage                                                                             |
| grid-energy-produced-l1               | Number:Energy              | R          | Grid Energy Produced L1                  |                                                                                                      |
| grid-energy-produced-l2               | Number:Energy              | R          | Grid Energy Produced L2                  |                                                                                                      |
| grid-energy-produced-l3               | Number:Energy              | R          | Grid Energy Produced L3                  |                                                                                                      |
| grid-energy-consumed-l1               | Number:Energy              | R          | Grid Energy Consumed L1                  |                                                                                                      | 
| grid-energy-consumed-l2               | Number:Energy              | R          | Grid Energy Consumed L2                  |                                                                                                      |
| grid-energy-consumed-l3               | Number:Energy              | R          | Grid Energy Consumed L3                  |                                                                                                      |
| inverter-energy-produced-l1           | Number:Energy              | R          | Inverter Energy Produced L1              |                                                                                                      |
| inverter-energy-produced-l2           | Number:Energy              | R          | Inverter Energy Produced L2              |                                                                                                      |
| inverter-energy-produced-l3           | Number:Energy              | R          | Inverter Energy Produced L3              |                                                                                                      |
| inverter-energy-consumed-l1           | Number:Energy              | R          | Inverter Energy Consumed L1              |                                                                                                      |
| inverter-energy-consumed-l2           | Number:Energy              | R          | Inverter Energy Consumed L2              |                                                                                                      |
| inverter-energy-consumed-l3           | Number:Energy              | R          | Inverter Energy Consumed L3              |                                                                                                      |
| load-energy-produced-l1               | Number:Energy              | R          | Load Energy Produced L1                  |                                                                                                      |
| load-energy-produced-l2               | Number:Energy              | R          | Load Energy Produced L2                  |                                                                                                      |
| load-energy-produced-l3               | Number:Energy              | R          | Load Energy Produced L3                  |                                                                                                      |
| load-energy-consumed-l1               | Number:Energy              | R          | Load Energy Consumed L1                  |                                                                                                      |
| load-energy-consumed-l2               | Number:Energy              | R          | Load Energy Consumed L2                  |                                                                                                      |
| load-energy-consumed-l3               | Number:Energy              | R          | Load Energy Consumed L3                  |                                                                                                      |
| grid-energy-produced-total            | Number:Energy              | R          | Grid Energy Produced Total               |                                                                                                      |
| grid-energy-consumed-total            | Number:Energy              | R          | Grid Energy Consumed Total               |                                                                                                      |
| inverter-energy-produced-total        | Number:Energy              | R          | Inverter Energy Produced Total           |                                                                                                      |
| inverter-energy-consumed-total        | Number:Energy              | R          | Inverter Energy Consumed Total           |                                                                                                      |
| load-energy-produced-total            | Number:Energy              | R          | Load Energy Produced Total               |                                                                                                      |
| load-energy-consumed-total            | Number:Energy              | R          | Load Energy Consumed Total               |                                                                                                      |
| total-solar-energy                    | Number:Energy              | R          | Total Solar Energy                       | Only sent when system has PV                                                                         |
| state                                 | String                     | R          | State of the System                      |                                                                                                      |
| timestamp                             | DateTime                   | R          | Time Stamp                               | Time stamp when message was published                                                                |
| battery-energy-produced               | Number:Energy              | R          | Battery Energy Produced                  | Only sent when system has batteries                                                                  |
| battery-energy-consumed               | Number:Energy              | R          | Battery Energy Consumed                  | Only sent when system has batteries                                                                  |
| soc                                   | Number:Dimensionless       | R          | System State of Check                    | State of the system                                                                                  |
| soh                                   | Number:Dimensionless       | R          | System State of Health                   |                                                                                                      |
| power-battery                         | Number:Power               | R          | Battery Power                            | Only sent when system has batteries                                                                  |
| total-capacity-batteries              | Number:Energy              | R          | Total Capacity Batteries                 | Total rated capacity of all batteries                                                                |

| s1-id                                 | String                     | R          | S1 ID                                    | Unique identifier of SSO-1                                                                           |
| s1-pv-voltage                         | Number:ElectricPotential   | R          | S1 Voltage on PV String Side             | Measured on PV string side                                                                           |
| s1-pv-current                         | Number:ElectricCurrent     | R          | S1 Current on PV String Side             | Measured on PV string side                                                                           |
| s1-total-solar-energy                 | Number:Energy              | R          | S1 Total Solar Energy                    | Total energy produced by SSO-1                                                                       |
| s1-relay-status                       | Contact                    | R          | S1 Relay Status                          | 0 = relay closed (i.e running power), 1 = relay open/disconnected, 2 = precharge                     |
| s1-temperature                        | Number:Temperature         | R          | S1 Temperature on PCB                    | Temperature Measured on PCB                                                                          |
| s1-fault-code                         | String                     | R          | S1 FaultCode                             | 0x00 = OK. For all other values Please contact Ferroamp support                                      |
| s1-dc-link-voltage                    | Number:ElectricPotential   | R          | S1 DC Link Voltage                       | DC link voltage as measured by SSO-1                                                                 |
| s1-timestamp                          | DateTime                   | R          | S1 Time Stamp                            | Time stamp when message was published                                                                |

| s2-id                                 | String                     | R          | S2 ID                                    | Unique identifier of SSO-2                                                                           |
| s2-pv-voltage                         | Number:ElectricPotential   | R          | S2 Voltage on PV String Side             | Measured on PV string side                                                                           |
| s2-pv-current                         | Number:ElectricCurrent     | R          | S2 Current on PV String Side             | Measured on PV string side                                                                           |
| s2-total-solar-energy                 | Number:Energy              | R          | S2 Total Solar Energy                    | Total energy produced by SSO-2                                                                       |
| s2-relay-status                       | Contact                    | R          | S2 Relay Status                          | 0 = relay closed (i.e running power), 1 = relay open/disconnected, 2 = precharge                     |
| s2-temperature                        | Number:Temperature         | R          | S2 Temperature on PCB                    | Temperature Measured on PCB                                                                          |
| s2-fault-code                         | String                     | R          | S2 FaultCode                             | 0x00 = OK. For all other values Please contact Ferroamp support                                      |
| s2-dc-link-voltage                    | Number:ElectricPotential   | R          | S2 DC Link Voltage                       | DC link voltage as measured by SSO-2                                                                 |
| s2-timestamp                          | DateTime                   | R          | S2 Time Stamp                            | Time stamp when message was published                                                                |
 
| s3-id                                 | String                     | R          | S3 ID                                    | Unique identifier of SSO-3                                                                           |
| s3-pv-voltage                         | Number:ElectricPotential   | R          | S3 Voltage on PV String Side             | Measured on PV string side                                                                           |
| s3-pv-current                         | Number:ElectricCurrent     | R          | S3 Current on PV String Side             | Measured on PV string side                                                                           |
| s3-total-solar-energy                 | Number:Energy              | R          | S3 Total Solar Energy                    | Total energy produced by SSO-3                                                                       |
| s3-relay-status                       | Contact                    | R          | S3 Relay Status                          | 0 = relay closed (i.e running power), 1 = relay open/disconnected, 2 = precharge                     |
| s3-temperature                        | Number:Temperature         | R          | S3 Temperature on PCB                    | Temperature Measured on PCB                                                                          |
| s3-fault-code                         | String                     | R          | S3 FaultCode                             | 0x00 = OK. For all other values Please contact Ferroamp support                                      |
| s3-dc-link-voltage                    | Number:ElectricPotential   | R          | S3 DC Link Voltage                       | DC link voltage as measured by SSO-3                                                                 |
| s3-timestamp                          | DateTime                   | R          | S3 Time Stamp                            | Time stamp when message was published                                                                |

| s4-id                                 | String                     | R          | S4 ID                                    | Unique identifier of SSO-4                                                                           |
| s4-pv-voltage                         | Number:ElectricPotential   | R          | S4 Voltage on PV String Side             | Measured on PV string side                                                                           |
| s4-pv-current                         | Number:ElectricCurrent     | R          | S4 Current on PV String Side             | Measured on PV string side                                                                           |
| s4-total-solar-energy                 | Number:Energy              | R          | S4 Total Solar Energy                    | Total energy produced by SSO-4                                                                       |
| s4-relay-status                       | Contact                    | R          | S4 Relay Status                          | 0 = relay closed (i.e running power), 1 = relay open/disconnected, 2 = precharge                     |
| s4-temperature                        | Number:Temperature         | R          | S4 Temperature on PCB                    | Temperature Measured on PCB                                                                          |
| s4-fault-code                         | String                     | R          | S4 FaultCode                             | 0x00 = OK. For all other values Please contact Ferroamp support                                      |
| s4-dc-link-voltage                    | Number:ElectricPotential   | R          | S4 DC Link Voltage                       | DC link voltage as measured by SSO-4                                                                 |
| s4-timestamp                          | DateTime                   | R          | S4 Time Stamp                            | Time stamp when message was published                                                                |

| eso-id                                | String                     | R          | Eso Unique Identifier                    | Unique identifier of ESO                                                                             |
| eso-voltage-battery                   | Number:ElectricPotential   | R          | Eso Voltage on Battery Side              | Measured on battery side                                                                             |
| eso-current-battery                   | Number:ElectricCurrent     | R          | Eso Current on Battery Side              | Measured on battery side                                                                             |
| eso-battery-energy-produced           | Number:Energy              | R          | Eso Battery Energy Produced              | Total energy produced by ESO, i.e total energy charged                                               |
| eso-battery-energy-consumed           | Number:Energy              | R          | Eso Battery Energy Consumed              | Total energy consumed by ESO, i.e total energy discharged                                            |
| eso-soc                               | Number:Dimensionless       | R          | Eso State of Charge                      | State of Charge for ESO                                                                              |
| eso-relay-status                      | Contact                    | R          | Eso Relay Status                         | 0 = relay closed, 1 = relay open                                                                     |
| eso-temperature                       | Number:Temperature         | R          | Eso Temperature on PCB                   | Measured inside ESO                                                                                  |
| eso-fault-code                        | String                     | R          | Eso FaultCode                            | See section 4.1.3.1 in Ferroamp-External-API-specifikation                                           |
| eso-battery-energy-produced           | Number:Energy              | R          | Eso Battery Energy Produced              | Total energy produced by ESO, i.e total energy charged                                               |
| eso-dc-link-voltage                   | Number:ElectricPotential   | R          | Eso Dc Link Voltage                      | DC link voltage as measured by ESO                                                                   |
| eso-timestamp                         | DateTime                   | R          | Eso Time Stamp                           | Time stamp when message was published                                                                |


| esm-id                                | String                     | R          | Esm Unique Identifier                    | Unique identifier of battery. If available, this will be the unique id that the battery reports      |
| esm-soh                               | Number:Dimensionless       | R          | Esm System State of Health               | State of Health for ESM                                                                              |
| esm-soc                               | Number:Dimensionless       | R          | Esm System State of Charge               | State of Charge for ESM                                                                              |
| esm-total-capacity                    | Number:Energy              | R          | Esm Rated Capacity                       | Rated capacity of all batteries                                                                      |
| esm-power-battery                     | Number:Power               | R          | Esm Rated Power of Battery               | Rated power of battery                                                                               |
| esm-status                            | String                     | R          | Esm Status                               | Dependent on battery manufacturer                                                                    |
| esm-timestamp                         | DateTime                   | R          | Esm Time Stamp                           | Time stamp when message was published                                                                |

The following channels are available for `Ferroamp` EnergyHub configuration. Please, see Ferroamp documentation for more details.

| Channel Type ID      | Item Type                  | Read/Write | Description                                                                                                                                                      |
|----------------------|----------------------------|------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| request-charge       | String                     | W          | Set charge power, value in Watt                                                                                                                                  |
| request-discharge    | String                     | W          | Set discharge power, value in Watt                                                                                                                               |
| request-auto         | String                     | W          | Set auto power. Returning control of batteries to system, value as auto 

# Full Example

## `demo.things` Example

```java
Thing ferroamp:energyhub:myenergyhub [ hostName="energyhub-ip", userName="myUserName", password="myPassword", hasBattery=false ]
```

## `demo.items` Example

```java
Number:Energy Ferroamp "Load Energy Consumed L1" <energy> { channel="ferroamp:energyhub:myenergyhub:load-energy-consumed-l1" }
String Ferroamp "RequestCharge" <energy> { channel="ferroamp:energyhub:myenergyhub:request-charge" }
```

## Rules

Ex. Rule name: Set Charge Level.
Set charging level to 5000W when item RequestCharge is updated.

```yaml
configuration: {}
triggers:
  - id: "1"
    configuration:
      itemName: EnergyHub_RequestCharge
    type: core.ItemStateUpdateTrigger
conditions: []
actions:
  - id: "2"
    configuration:
      itemName: EnergyHub_RequestCharge
      command: "5000"
    type: core.ItemCommandAction
```
