## Ferroamp Binding

The Ferroamp binding is used to get live data from Ferroamp EnergyHub

The Ferroamp binding is compatible with EnergyHub Wall and EnergyHub XL, and connects to your local EnergyHub via LAN.
Data and commands are received/sent using MQTT where the user connects to the MQTT broker residing on the EnergyHub.

*note* Contact Ferroamp support to enable MQTT in the EnergyHub and to get the Username and Password:

https://ferroamp.com/om-ferroamp/

## Supported Things

The binding retrieves data from the different parts of the Ferroamp EnergyHub such as:

- `ehub`: EnergyHub Wall and EnergyHub XL.
- `sso`: Solar string optimizer.
- `eso`: Bidirectional DC/DC converter for connection of battery.
- `esm`: Energy Storage Module.

## Discovery

Discovery is not supported.

## Thing Configuration

The following configuration parameters are available.

| Name            | Type    | Description                                           | Default | Required | Advanced |
|-----------------|---------|-------------------------------------------------------|---------|----------|----------|
| hostName        | text    | Hostname or IP address of the device                  | N/A     | yes      | no       |
| userName        | text    | Username to access the device                         | N/A     | yes      | no       |
| password        | text    | Password to access the device                         | N/A     | yes      | no       |
| hasBattery      | boolean | Has the system a battery connected?                   | N/A     | no       | yes      |
| ssoS0           | boolean | Has the system, the 1'st Sso Pv-string connected?     | N/A     | no       | no       |
| ssoS1           | boolean | Has the system, the 2'nd Sso Pv-string connected?     | N/A     | no       | no       |
| ssoS2           | boolean | Has the system, the 3'rd Sso Pv-string connected?     | N/A     | no       | no       |
| ssoS3           | boolean | Has the system, the 4'th Sso Pv-string connected?     | N/A     | no       | no       |
| eso             | boolean | Has the system an Eso unit connected?                 | N/A     | no       | no       |
| esm             | boolean | Has the system an Esm unit connected?                 | N/A     | no       | no       |

| The unique serial number is marked on the side of the SSO unit. Ex. PS00990-A04-S20120476

## Channels

| Channel Type ID                       | Item Type                  | Read/Write | Label                                    | Description 
|----------------------------------------------------------------------------------------------------------------------------------------------
| grid-frequency                        | Number:Frequency           | R          | Estimated Grid Frequency                 | Estimated Grid Frequency |
| ace-current-l1                        | Number:ElectricCurrent     | R          | ACE Current L1                           | Adaptive Current Equalization (ACE) equalization current set-points in Amps root mean square (Arms) |
| ace-current-l2                        | Number:ElectricCurrent     | R          | ACE Current L2                           | Adaptive Current Equalization (ACE) equalization current set-points in Amps root mean square (Arms) |
| ace-current-l3                        | Number:ElectricCurrent     | R          | ACE Current L3                           | Adaptive Current Equalization (ACE) equalization current set-points in Amps root mean square (Arms) |
| external-voltage-l1                   | Number:ElectricPotential   | R          | External Voltage L1                      | External voltage |
| external-voltage-l2                   | Number:ElectricPotential   | R          | External Voltage L2                      | External voltage |
| external-voltage-l3                   | Number:ElectricPotential   | R          | External Voltage L3                      | External voltage |
| inverter-rms-current-l1               | Number:ElectricCurrent     | R          | Inverter RMS Current L1                  | Inverter RMS current |
| inverter-rms-current-l2               | Number:ElectricCurrent     | R          | Inverter RMS Current L2                  | Inverter RMS current |
| inverter-rms-current-l3               | Number:ElectricCurrent     | R          | Inverter RMS Current L3                  | Inverter RMS current |
| inverter-current-reactive-l1          | Number:ElectricCurrent     | R          | Inverter Current Reactive L1             | Inverter reactive current |
| inverter-current-reactive-l2          | Number:ElectricCurrent     | R          | Inverter Current Reactive L2             | Inverter reactive current |
| inverter-current-reactive-l3          | Number:ElectricCurrent     | R          | Inverter Current Reactive L3             | Inverter reactive current |
| inverter-current-active-l1            | Number:ElectricCurrent     | R          | Inverter Active current L1               | Inverter active current |
| inverter-current-active-l2            | Number:ElectricCurrent     | R          | Inverter Active current L1               | Inverter active current |
| inverter-current-active-l3            | Number:ElectricCurrent     | R          | Inverter Active current L1               | Inverter active current |
| grid-current-l1                       | Number:ElectricCurrent     | R          | Grid Current L1                          | Grid RMS current |
| grid-current-l2                       | Number:ElectricCurrent     | R          | Grid Current L2                          | Grid RMS current |
| grid-current-l3                       | Number:ElectricCurrent     | R          | Grid Current L3                          | Grid RMS current |
| grid-current-reactive-l1              | Number:ElectricCurrent     | R          | Grid Current Reactive L1                 | Grid current reactive |
| grid-current-reactive-l2              | Number:ElectricCurrent     | R          | Grid Current Reactive L2                 | Grid current reactive |
| grid-current-reactive-l3              | Number:ElectricCurrent     | R          | Grid Current Reactive L3                 | Grid current reactive |
| grid-current-active-l1                | Number:ElectricCurrent     | R          | Grid Current Active L1                   | Grid current active |
| grid-current-active-l2                | Number:ElectricCurrent     | R          | Grid Current Active L2                   | Grid current active |
| grid-current-active-l3                | Number:ElectricCurrent     | R          | Grid Current Active L3                   | Grid current active |
| inverter-reactive-current-l1          | Number:ElectricCurrent     | R          | Inverter Reactive Current L1             | 
| inverter-reactive-current-l2          | Number:ElectricCurrent     | R          | Inverter Reactive Current L2             | 
| inverter-reactive-current-l3          | Number:ElectricCurrent     | R          | Inverter Reactive Current L3             | 
| inverter-load-l1                      | Number:ElectricCurrent     | R          | Inverter Load L1                         |
| inverter-load-l2                      | Number:ElectricCurrent     | R          | Inverter Load L2                         |
| inverter-load-l3                      | Number:ElectricCurrent     | R          | Inverter Load L3                         |
| apparent-power                        | Number:Energy              | R          | Apparent Power                           | Apparent power
| grid-power-active-l1                  | Number:Power               | R          | Grid Power Active L1                     | Grid power, active |
| grid-power-active-l2                  | Number:Power               | R          | Grid Power Active L2                     | Grid power, active |
| grid-power-active-l3                  | Number:Power               | R          | Grid Power Active L3                     | Grid power, active |
| grid-power-reactive-l1                | Number:Power               | R          | Grid Power Reactive L1                   | Grid power, reactive |
| grid-power-reactive-l2                | Number:Power               | R          | Grid Power Reactive L2                   | Grid power, reactive |
| grid-power-reactive-l3                | Number:Power               | R          | Grid Power Reactive L3                   | Grid power, reactive |
| inverter-power-active-l1              | Number:Power               | R          | Inverter Power Active L1                 | Inverter power, active |
| inverter-power-active-l2              | Number:Power               | R          | Inverter Power Active L2                 | Inverter power, active |
| inverter-power-active-l3              | Number:Power               | R          | Inverter Power Active L3                 | Inverter power, active |
| inverter-power-reactive-l1            | Number:Power               | R          | Inverter Power Reactive L1               | Inverter power, reactive |
| inverter-power-reactive-l1            | Number:Power               | R          | Inverter Power Reactive L2               | Inverter power, reactive |
| inverter-power-reactive-l1            | Number:Power               | R          | Inverter Power Reactive L3               | Inverter power, reactive |
| consumption-power-l1                  | Number:Power               | R          | Consumption Power L1                     |
| consumption-power-l2                  | Number:Power               | R          | Consumption Power L2                     |
| consumption-power-l3                  | Number:Power               | R          | Consumption Power L3                     |
| consumption-power-reactive-l1         | Number:Power               | R          | Consumption Power Reactive L1            |
| consumption-power-reactive-l2         | Number:Power               | R          | Consumption Power Reactive L2            |
| consumption-power-reactive-l3         | Number:Power               | R          | Consumption Power Reactive L3            |
| solar-pv                              | Number:Power               | R          | Solar Power                              | Only sent when system has PV |
| positive-dc-link-voltage              | Number:ElectricPotential   | R          | Positiv DC Link Voltage                  | Positiv DC link voltage |
| negative-dc-link-voltage              | Number:ElectricPotential   | R          | Negative DC Link Voltage                 | Negative DC link voltage |
| grid-energy-produced-l1               | Number:Energy              | R          | Grid Energy Produced L1                  |
| grid-energy-produced-l2               | Number:Energy              | R          | Grid Energy Produced L2                  |
| grid-energy-produced-l3               | Number:Energy              | R          | Grid Energy Produced L3                  |
| grid-energy-consumed-l1               | Number:Energy              | R          | Grid Energy Consumed L1                  | 
| grid-energy-consumed-l2               | Number:Energy              | R          | Grid Energy Consumed L2                  |
| grid-energy-consumed-l3               | Number:Energy              | R          | Grid Energy Consumed L3                  |
| inverter-energy-produced-l1           | Number:Energy              | R          | Inverter Energy Produced L1              |
| inverter-energy-produced-l2           | Number:Energy              | R          | Inverter Energy Produced L2              |
| inverter-energy-produced-l3           | Number:Energy              | R          | Inverter Energy Produced L3              |
| inverter-energy-consumed-l1           | Number:ElectricCurrent     | R          | Inverter Energy Consumed L1              |
| inverter-energy-consumed-l2           | Number:ElectricCurrent     | R          | Inverter Energy Consumed L2              |
| inverter-energy-consumed-l3           | Number:ElectricCurrent     | R          | Inverter Energy Consumed L3              |
| load-energy-produced-l1               | Number:Energy              | R          | Load Energy Produced L1                  |
| load-energy-produced-l2               | Number:Energy              | R          | Load Energy Produced L2                  |
| load-energy-produced-l3               | Number:Energy              | R          | Load Energy Produced L3                  |
| load-energy-consumed-l1               | Number:Energy              | R          | Load Energy Consumed L1                  |
| load-energy-consumed-l2               | Number:Energy              | R          | Load Energy Consumed L2                  |
| load-energy-consumed-l3               | Number:Energy              | R          | Load Energy Consumed L3                  |
| total-grid-energy-produced            | Number:Energy              | R          | Total produced grid energy               |
| total-grid-energy-consumed            | Number:Energy              | R          | Total consumed grid energy               |
| total-inverter-energy-produced        | Number:Energy              | R          | Total produced inverter energy           |
| total-inverter-energy-consumed        | Number:Energy              | R          | Total consumed inverter energy           |
| total-load-energy-produced            | Number:Energy              | R          | Total produced load energy               |
| total-load-energy-consumed            | Number:Energy              | R          | Total consumed load energy               |
| total-solar-energy                    | Number:Energy              | R          | Total Solar Energy                       | Only sent when system has PV |
| state                                 | String                     | R          | State of the System                      |
| timestamp                             | DateTime                   | R          | Time Stamp When Message was Published    | Time stamp when message was published |
| battery-energy-produced               | Number:Energy              | R          | Battery Energy Produced                  | Only sent when system has batteries |
| battery-energy-consumed               | Number:Energy              | R          | Battery Energy Consumed                  | Only sent when system has batteries |
| soc                                   | Number:Dimensionless       | R          | System State of Check                    | State of the system |
| soh                                   | Number:Dimensionless       | R          | System State of Health                   |
| power-battery                         | Number:Power               | R          | Battery Power                            | Only sent when system has batteries |
| total-capacity-batteries              | Number:Energy              | R          | Total Rated Capacity of All Batteries    |

| s0-id                                 | String                     | R          | S0 ID                                    | Unique identifier of SSO-0 |
| s0-pv-voltage                         | Number:ElectricPotential   | R          | S0 Measured Voltage on PV String Side    | Measured on PV string side |
| s0-pv-current | Number:ElectricCurrent     | R          | S0 Measured Current on PV String Side    | Measured on PV string side |
| s0-total-solar-energy                 | Number:Energy              | R          | S0 Total Solar Energy                    | Total energy produced by SSO-0 |
| s0-relay-status                       | Contact                    | R          | S0 Relay Status                          | 0 = relay closed (i.e running power), 1 = relay open/disconnected, 2 = precharge |
| s0-temperature                        | Number:Temperature         | R          | S0 Temperature Measured on PCB           | Temperature Measured on PCB |
| s0-fault-code                         | String                     | R          | S0 FaultCode                             | 0x00 = OK. For all other values Please contact Ferroamp support |
| s0-dc-link-voltage                    | Number:ElectricPotential   | R          | S0 DC Link Voltage                       | DC link voltage as measured by SSO-0 |
| s0-timestamp                          | DateTime                   | R          | S0 Time Stamp When Message was Published | Time stamp when message was published |
 |
| s1-id                                 | String                     | R          | S1 ID                                    | Unique identifier of SSO-1 |
| s1-pv-voltage                         | Number:ElectricPotential   | R          | S1 easured Voltage on PV String Side     | Measured on PV string side |
| s1-pv-current                         | Number:ElectricCurrent     | R          | S1 Measured Current on PV String Side    | Measured on PV string side |
| s1-total-solar-energy                 | Number:Energy              | R          | S1 Total Solar Energy                    | Total energy produced by SSO-1 |
| s1-relay-status                       | Contact                    | R          | S1 Relay Status                          | 0 = relay closed (i.e running power), 1 = relay open/disconnected, 2 = precharge |
| s1-temperature                        | Number:Temperature         | R          | S1 Temperature Measured on PCB           | Temperature Measured on PCB |
| s1-fault-code                         | String                     | R          | S1 FaultCode                             | 0x00 = OK. For all other values Please contact Ferroamp support |
| s1-dc-link-voltage                    | Number:ElectricPotential   | R          | S1 DC Link Voltage                       | DC link voltage as measured by SSO-1 |
| s1-timestamp                          | DateTime                   | R          | S1 Time Stamp When Message was Published | Time stamp when message was published |
 |
 
| s2-id                                 | String                     | R          | S2 ID                                    | Unique identifier of SSO-2 |
| s2-pv-voltage                         | Number:ElectricPotential   | R          | S2 Measured Voltage on PV String Side    | Measured on PV string side |
| s2-pv-current                         | Number:ElectricCurrent     | R          | S2 Measured Current on PV String Side    | Measured on PV string side |
| s2-total-solar-energy                 | Number:Energy              | R          | S2 Total Solar Energy                    | Total energy produced by SSO-2 |
| s2-relay-status                       | Contact                    | R          | S2 Relay Status                          | 0 = relay closed (i.e running power), 1 = relay open/disconnected, 2 = precharge |
| s2-temperature                        | Number:Temperature         | R          | S2 Temperature Measured on PCB           | Temperature Measured on PCB |
| s2-fault-code                         | String                     | R          | S2 FaultCode                             | 0x00 = OK. For all other values Please contact Ferroamp support |
| s2-dc-link-voltage                    | Number:ElectricPotential   | R          | S2 DC Link Voltage                       | DC link voltage as measured by SSO-2 |
| s2-timestamp                          | DateTime                   | R          | S2 Time Stamp When Message was Published | Time stamp when message was published |
 |

| s3-id                                 | String                     | R          | S3 ID                                    | Unique identifier of SSO-3 |
| s3-pv-voltage                         | Number:ElectricPotential   | R          | S3 Measured Voltage on PV String Side    | Measured on PV string side |
| s3-pv-current                         | Number:ElectricCurrent     | R          | S3 Measured Current on PV String Side    | Measured on PV string side |
| s3-total-solar-energy                 | Number:Energy              | R          | S3 Total Solar Energy                    | Total energy produced by SSO-3 |
| s3-relay-status                       | Contact                    | R          | S3 Relay Status                          | 0 = relay closed (i.e running power), 1 = relay open/disconnected, 2 = precharge |
| s3-temperature                        | Number:Temperature         | R          | S3 Temperature Measured on PCB           | Temperature Measured on PCB |
| s3-fault-code                         | String                     | R          | S3 FaultCode                             | 0x00 = OK. For all other values Please contact Ferroamp support |
| s3-dc-link-voltage                    | Number:ElectricPotential   | R          | S3 DC Link Voltage                       | DC link voltage as measured by SSO-3 |
| s3-timestamp                          | DateTime                   | R          | S3 Time Stamp When Message was Published | Time stamp when message was published |
 |

| eso-id                                | String                     | R          | Eso Unique Identifier                    | Unique identifier |
| eso-voltage-battery                   | Number:ElectricPotential   | R          | Eso Voltage Measured on Battery Side     | Measured on battery side |
| eso-current-battery                   | Number:ElectricCurrent     | R          | Eso Current Measured on Battery Side     | Measured on battery side |
| eso-battery-energy-produced           | Number:Energy              | R          | Eso Battery Energy Produced              | Total energy produced by ESO, i.e total energy charged |
| eso-battery-energy-consumed           | Number:Energy              | R          | Eso Battery Energy Consumed              | Total energy consumed by ESO, i.e total energy discharged |
| eso-soc                               | Number:Dimensionless       | R          | Eso State of Charge                      | State of Charge for ESO |
| eso-relay-status                      | Contact                    | R          | Eso Relay Status                         | 0 = relay closed, 1 = relay open |
| eso-temperature                       | Number:Temperature         | R          | Eso Temperature Measured on PCB          | Measured inside ESO |
| eso-fault-code                        | String                     | R          | Eso FaultCode                            | See section 4.1.3.1 in Ferroamp-External-API-specifikation |
| eso-battery-energy-produced           | Number:Energy              | R          | Eso Battery Energy Produced              | Total energy produced by ESO, i.e total energy charged |
| eso-dc-link-voltage                   | Number:ElectricPotential   | R          | Eso Dc Link Voltage                      | DC link voltage as measured by ESO |
| eso-timestamp                         | DateTime                   | R          | Eso Time Stamp When Message was Published| Time stamp when message was published |


| esm-id                                | String                     | R          | Esm Unique Identifier                    | Unique identifier of battery. If available, this will be the unique id that the battery reports.|
| esm-soh                               | Number:Dimensionless       | R          | Esm System State of Health               | State of Health for ESM |
| esm-soc                               | Number:Dimensionless       | R          | Esm System State of Charge               | State of Charge for ESM |
| esm-total-capacity                    | Number:Energy              | R          | Esm Rated Capacity                       | Rated capacity of all batteries |
| esm-power-battery                     | Number:Power               | R          | Esm Rated power of battery               | Rated power of battery |
| esm-status                            | String                     | R          | Esm Status                               | Dependent on battery manufacturer |
| esm-timestamp                         | DateTime                   | R          | Esm Time Stamp When Message was Published| Time stamp when message was published |

The following channels are available for `Ferroamp` EnergyHub configuration. Please, see Ferroamp documentation for more details.

| Channel Type ID      | Item Type                  | Read/Write | Description                                                             |
|---------------------------------------------------|------------|-------------------------------------------------------------------------|
| request-charge       | String                     | W          | Set charge power, value in Watt                                         |
| request-discharge    | String                     | W          | Set discharge power, value in Watt                                      |
| request-auto         | String                     | W          | Set auto power. Returning control of batteries to system, value as auto.|

# Full Example

## `demo.things` Example

```java
Thing ferroamp:energyhub:myenergyhub [ hostName="energyhub-ip", userName="myUserName", password="myPassword", hasBattery=false ]
```

```java
Thing ferroamp:energyhub:myenergyhub [ hostName="energyhub-ip", userName="myUserName", password="myPassword", hasBattery=false, ssoS0=true ]
```

```java
Thing ferroamp:energyhub:myenergyhub [ hostName="energyhub-ip", userName="myUserName", password="myPassword", hasBattery=true, ssoS0=true, eso=true ]
```



## `demo.items` Example

```java
Number:Energy Ferroamp "Load Energy Consumed L1" <energy> { channel="ferroamp:energyhub:myenergyhub:load-energy-consumed-l1" }
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
