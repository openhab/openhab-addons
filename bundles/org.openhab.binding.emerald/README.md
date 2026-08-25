# Emerald Binding

A binding that supports the Australian brand Emerald's range of Heat Pump Hot Water Services (HWS), providing information and control into openHAB.

## Supported Things

HWS sold by Emerald. 
In the case of a household with multiple HWS, each HWS is represented by its own 'hws' thing.

## Discovery

After (manually) adding an Emerald Account bridge, registered HWS will be auto discovered.

## `account` Bridge Configuration

As a minimum, a username and email are needed:

| Thing Parameter | Default Value | Required | Advanced | Description                                              |
|-----------------|---------------|----------|----------|----------------------------------------------------------|
| email           | N/A           | Yes      | No       | Email address of the account registered with Emerald     |
| password        | N/A           | Yes      | No       | Password associated with account registered with Emerald |

## Thing Configuration

As a minimum, the uuid is needed:

| Thing Parameter | Default Value | Required | Advanced | Description                                                                          |
|-----------------|---------------|----------|----------|--------------------------------------------------------------------------------------|
| uuid            | N/A           | Yes      | No       | HWS uuid returned by Emerald API                                                     |

## Channels

| channel id          | type               | description                                          |
|---------------------|--------------------|------------------------------------------------------|
| power               | Switch             | Turns on/off the HWS                                 |
| mode                | Number             | Mode the HWS is set to (Normal/Economy/Boost)        |
| current-temperature | Number:Temperature | Current temperature of water in the HWS              |
| set-temperature     | Number:Temperature | Current setpoint for water in the HWS                |
| fault               | Number             | Current fault code (0 = No Fault)                    |
| defrost             | Switch             | Defrost Mode On/Off                                  |
| work-state          | Number             | Current work state of the HWS                        |

## Full Example

### `emerald.things`

```java
Bridge emerald:account:account [ email="username@domain.tld", password="password" ] {
    emerald:hws:hws [ uuid="sdafafasafaXb3" ]
}
```

### `emerald.items`

```java
Switch Emerald_Power { channel="emerald:hws:Emerald:power" }
Number Emerald_Mode { channel="emerald:hws:Emerald:mode" }
Number:Temperature Emerald_Current_Temperature { channel="emerald:hws:Emerald:current-temperature" }
Number:Temperature Emerald_Set_Temperature { channel="emerald:hws:Emerald:set-temperature" }
Number Emerald_Fault { channel="emerald:hws:Emerald:fault" }
Switch Emerald_Defrost { channel="emerald:hws:Emerald:defrost" }
Number Emerald_Work_State { channel="emerald:hws:Emerald:work-state" }
```

### `emerald.sitemap`

```perl
Text item=Emerald_Power label="HWS Power"
Text item=Emerald_Mode label="HWS Mode"
Text item=Emerald_Current_Temperature label="Current HWS Temperature"
Text item=Emerald_Set_Temperature label="Setpoint for HWS Temperature"
Text item=Emerald_Fault label="Faults"
Text item=Emerald_Defrost label="Defrost On/Off"
Text item=Emerald_Work_State label="Current Work State"
```
